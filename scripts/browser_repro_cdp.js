const fs = require('fs')
const path = require('path')
const { spawn } = require('child_process')

const CHROME_PATHS = [
  'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
  'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe'
]

const BASE_URL = 'http://127.0.0.1:3000'
const DEBUG_PORT = 9222
const OUTPUT_DIR = path.join(__dirname, '..', 'test_screenshots', 'browser_repro')
const RUN_ID = `${Date.now()}-${process.pid}`
const USER_DATA_DIR = path.join(OUTPUT_DIR, 'chrome-profile', RUN_ID)
const REPORT_PATH = path.join(OUTPUT_DIR, 'report.json')

const ADMIN_USERNAME = 'admin'
const ADMIN_PASSWORD = '123456'

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

const ensureDir = (dirPath) => {
  fs.mkdirSync(dirPath, { recursive: true })
}

class CDPClient {
  constructor(wsUrl) {
    this.wsUrl = wsUrl
    this.ws = null
    this.id = 0
    this.pending = new Map()
    this.listeners = new Map()
  }

  async connect() {
    await new Promise((resolve, reject) => {
      this.ws = new WebSocket(this.wsUrl)

      this.ws.onopen = () => resolve()
      this.ws.onerror = (error) => reject(error)
      this.ws.onmessage = (event) => this.#handleMessage(event.data)
      this.ws.onclose = () => {
        for (const [, pending] of this.pending) {
          pending.reject(new Error('CDP connection closed'))
        }
        this.pending.clear()
      }
    })
  }

  #handleMessage(raw) {
    const payload = JSON.parse(raw)

    if (payload.id) {
      const pending = this.pending.get(payload.id)
      if (!pending) {
        return
      }

      this.pending.delete(payload.id)
      if (payload.error) {
        pending.reject(new Error(payload.error.message || 'CDP command failed'))
        return
      }

      pending.resolve(payload.result)
      return
    }

    const callbacks = this.listeners.get(payload.method) || []
    callbacks.forEach((callback) => callback(payload.params || {}))
  }

  on(method, callback) {
    const callbacks = this.listeners.get(method) || []
    callbacks.push(callback)
    this.listeners.set(method, callbacks)
    return () => {
      const current = this.listeners.get(method) || []
      this.listeners.set(
        method,
        current.filter((item) => item !== callback)
      )
    }
  }

  waitForEvent(method, predicate = () => true, timeoutMs = 10000) {
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        unsubscribe()
        reject(new Error(`Timed out waiting for event: ${method}`))
      }, timeoutMs)

      const unsubscribe = this.on(method, (params) => {
        if (!predicate(params)) {
          return
        }
        clearTimeout(timer)
        unsubscribe()
        resolve(params)
      })
    })
  }

  send(method, params = {}) {
    const id = ++this.id
    const message = JSON.stringify({ id, method, params })

    return new Promise((resolve, reject) => {
      this.pending.set(id, { resolve, reject })
      this.ws.send(message)
    })
  }

  async close() {
    if (!this.ws) {
      return
    }

    await new Promise((resolve) => {
      this.ws.onclose = () => resolve()
      this.ws.close()
    })
  }
}

const chromePath = CHROME_PATHS.find((candidate) => fs.existsSync(candidate))
if (!chromePath) {
  throw new Error('No Chrome/Edge executable found on this machine.')
}

const launchChrome = () => {
  ensureDir(USER_DATA_DIR)

  return spawn(
    chromePath,
    [
      '--headless=new',
      '--disable-gpu',
      '--no-first-run',
      '--no-default-browser-check',
      '--disable-background-networking',
      '--disable-sync',
      '--remote-allow-origins=*',
      `--remote-debugging-port=${DEBUG_PORT}`,
      `--user-data-dir=${USER_DATA_DIR}`,
      `${BASE_URL}/login`
    ],
    {
      stdio: 'ignore'
    }
  )
}

const fetchJson = async (url, options) => {
  const response = await fetch(url, options)
  if (!response.ok) {
    throw new Error(`Request failed: ${url} (${response.status})`)
  }
  return response.json()
}

const waitForDebuggerTarget = async () => {
  const deadline = Date.now() + 20000

  while (Date.now() < deadline) {
    try {
      const targets = await fetchJson(`http://127.0.0.1:${DEBUG_PORT}/json/list`)
      const pageTarget = targets.find((item) => item.type === 'page' && item.webSocketDebuggerUrl)
      if (pageTarget) {
        return pageTarget.webSocketDebuggerUrl
      }
    } catch (_error) {
      // Chrome may still be starting.
    }
    await sleep(300)
  }

  throw new Error('Timed out waiting for Chrome remote debugging target.')
}

const normalizeText = (value) => String(value || '').replace(/\s+/g, ' ').trim()

async function main() {
  ensureDir(OUTPUT_DIR)

  const chromeProcess = launchChrome()
  let cdp = null
  const report = {
    generatedAt: new Date().toISOString(),
    browser: path.basename(chromePath),
    baseUrl: BASE_URL,
    results: [],
    consoleMessages: []
  }

  const addResult = (page, action, ok, details = {}) => {
    report.results.push({
      page,
      action,
      ok,
      ...details
    })
  }

  try {
    const wsUrl = await waitForDebuggerTarget()
    cdp = new CDPClient(wsUrl)
    await cdp.connect()

    await cdp.send('Page.enable')
    await cdp.send('Runtime.enable')
    await cdp.send('Network.enable')
    await cdp.send('Log.enable')

    cdp.on('Runtime.consoleAPICalled', (params) => {
      const text = (params.args || [])
        .map((item) => item.value || item.description || '')
        .join(' ')
      report.consoleMessages.push({
        type: params.type,
        text,
        when: new Date().toISOString()
      })
    })

    cdp.on('Runtime.exceptionThrown', (params) => {
      report.consoleMessages.push({
        type: 'exception',
        text: params.exceptionDetails?.text || 'Runtime exception',
        when: new Date().toISOString()
      })
    })

    const evaluate = async (expression) => {
      const result = await cdp.send('Runtime.evaluate', {
        expression,
        returnByValue: true,
        awaitPromise: true
      })

      if (result.exceptionDetails) {
        throw new Error(result.exceptionDetails.text || 'Runtime.evaluate failed')
      }

      return result.result ? result.result.value : undefined
    }

    const waitFor = async (expression, timeoutMs = 10000) => {
      const deadline = Date.now() + timeoutMs
      while (Date.now() < deadline) {
        const value = await evaluate(expression)
        if (value) {
          return value
        }
        await sleep(200)
      }
      throw new Error(`Timed out waiting for condition: ${expression}`)
    }

    const navigate = async (url) => {
      const loadPromise = cdp.waitForEvent('Page.loadEventFired', () => true, 15000)
      await cdp.send('Page.navigate', { url })
      await loadPromise
      await sleep(1200)
      await waitFor('document.readyState === "complete"', 5000)
      await sleep(500)
    }

    const locationPath = () => evaluate('location.pathname + location.search')

    const visibleDialogCount = () =>
      evaluate(`
        Array.from(document.querySelectorAll('.el-dialog, .el-message-box'))
          .filter((el) => el.offsetParent !== null)
          .length
      `)

    const clickSelector = async (selector, index = 0) => {
      const clicked = await evaluate(`
        (() => {
          const list = Array.from(document.querySelectorAll(${JSON.stringify(selector)}))
            .filter((el) => el.offsetParent !== null)
          const target = list[${index}]
          if (!target) {
            return null
          }
          target.scrollIntoView({ block: 'center', inline: 'center' })
          target.click()
          return {
            text: (target.innerText || target.textContent || '').replace(/\\s+/g, ' ').trim(),
            tag: target.tagName
          }
        })()
      `)

      if (!clicked) {
        throw new Error(`Selector not found: ${selector} [${index}]`)
      }

      await sleep(900)
      return clicked
    }

    const clickText = async (text) => {
      const clicked = await evaluate(`
        (() => {
          const targetText = ${JSON.stringify(text)}
          const selectors = 'button, a, [role="button"], .menu-item, .panel-link-button, .prompt-card, .submission-link-button'
          const candidates = Array.from(document.querySelectorAll(selectors))
            .filter((el) => el.offsetParent !== null)
          const target = candidates.find((el) => {
            const value = (el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim()
            return value.includes(targetText)
          })
          if (!target) {
            return null
          }
          target.scrollIntoView({ block: 'center', inline: 'center' })
          target.click()
          return (target.innerText || target.textContent || '').replace(/\\s+/g, ' ').trim()
        })()
      `)

      if (!clicked) {
        throw new Error(`Text target not found: ${text}`)
      }

      await sleep(900)
      return clicked
    }

    const fillVisibleInputs = async (values) => {
      await evaluate(`
        (() => {
          const values = ${JSON.stringify(values)}
          const inputs = Array.from(document.querySelectorAll('input'))
            .filter((el) => el.offsetParent !== null && !el.disabled && el.type !== 'hidden')
          values.forEach((value, index) => {
            const input = inputs[index]
            if (!input) {
              return
            }
            input.focus()
            input.value = value
            input.dispatchEvent(new Event('input', { bubbles: true }))
            input.dispatchEvent(new Event('change', { bubbles: true }))
          })
          return inputs.length
        })()
      `)
      await sleep(400)
    }

    const getText = (selector) =>
      evaluate(`
        (() => {
          const target = document.querySelector(${JSON.stringify(selector)})
          if (!target) {
            return null
          }
          return (target.innerText || target.textContent || '').replace(/\\s+/g, ' ').trim()
        })()
      `)

    const getValue = (selector) =>
      evaluate(`
        (() => {
          const target = document.querySelector(${JSON.stringify(selector)})
          return target ? target.value : null
        })()
      `)

    const hasClass = (selector, className) =>
      evaluate(`
        (() => {
          const target = document.querySelector(${JSON.stringify(selector)})
          return target ? target.classList.contains(${JSON.stringify(className)}) : false
        })()
      `)

    const screenshot = async (name) => {
      const result = await cdp.send('Page.captureScreenshot', {
        format: 'png',
        captureBeyondViewport: true
      })
      fs.writeFileSync(path.join(OUTPUT_DIR, `${name}.png`), Buffer.from(result.data, 'base64'))
    }

    const runStep = async (page, action, fn) => {
      try {
        const details = await fn()
        addResult(page, action, true, details || {})
      } catch (error) {
        addResult(page, action, false, { error: error.message })
      }
    }

    await navigate(`${BASE_URL}/login`)
    await screenshot('01-login')

    await runStep('login', 'register link navigates', async () => {
      await clickSelector('a.register-link')
      await waitFor('location.pathname === "/register"', 5000)
      return { path: await locationPath() }
    })

    await runStep('register', 'login link navigates back', async () => {
      await clickSelector('a.login-link')
      await waitFor('location.pathname === "/login"', 5000)
      return { path: await locationPath() }
    })

    await runStep('login', 'submit button logs in', async () => {
      await fillVisibleInputs([ADMIN_USERNAME, ADMIN_PASSWORD])
      await clickSelector('.login-button')
      await waitFor('location.pathname !== "/login"', 10000)
      return { path: await locationPath() }
    })

    await navigate(`${BASE_URL}/learn`)
    await screenshot('02-learn')

    await runStep('learn', 'my plan button navigates', async () => {
      await clickSelector('.my-plan-btn')
      await waitFor('location.pathname === "/learn/assessment"', 5000)
      return { path: await locationPath() }
    })

    await navigate(`${BASE_URL}/problems`)
    await screenshot('03-problems')

    await runStep('problems', 'sort button changes label/state', async () => {
      const before = await getText('.filter-actions .action-btn .action-text')
      await clickSelector('.filter-actions .action-btn', 0)
      const after = await getText('.filter-actions .action-btn .action-text')
      if (normalizeText(before) === normalizeText(after)) {
        const active = await hasClass('.filter-actions .action-btn', 'active')
        if (!active) {
          throw new Error('Sort button click produced no visible label or state change')
        }
      }
      return { before, after }
    })

    await runStep('problems', 'favorite button stays on list and toggles', async () => {
      const beforePath = await locationPath()
      const beforeActive = await hasClass('.problem-item .favorite-btn', 'active')
      await clickSelector('.problem-item .favorite-btn')
      const afterPath = await locationPath()
      const afterActive = await hasClass('.problem-item .favorite-btn', 'active')
      if (beforePath !== afterPath) {
        throw new Error(`Favorite click navigated away: ${beforePath} -> ${afterPath}`)
      }
      if (beforeActive === afterActive) {
        throw new Error('Favorite button click did not toggle active state')
      }
      return { beforeActive, afterActive }
    })

    await runStep('problems', 'problem card opens detail page', async () => {
      await clickSelector('.problem-item', 0)
      await waitFor('location.pathname.startsWith("/problem/")', 5000)
      return { path: await locationPath() }
    })

    await screenshot('04-problem-detail')
    await navigate(`${BASE_URL}/wrong-book`)
    await screenshot('05-wrong-book')

    await runStep('wrong-book', 'review plan button opens dialog', async () => {
      const before = await visibleDialogCount()
      await clickText('复习计划')
      const after = await visibleDialogCount()
      if (after <= before) {
        throw new Error('Review plan button did not open a dialog')
      }
      await clickSelector('.plan-dialog .el-dialog__headerbtn')
      await waitFor(`
        Array.from(document.querySelectorAll('.plan-dialog .el-dialog'))
          .filter((el) => el.offsetParent !== null)
          .length === 0
      `, 5000)
      return { beforeDialogs: before, afterDialogs: after }
    })

    await runStep('wrong-book', 'review action does not accidentally open detail', async () => {
      const reviewButtonCount = await evaluate(`
        Array.from(document.querySelectorAll('.wrong-right .el-button--primary:not(.is-disabled)'))
          .filter((el) => el.offsetParent !== null)
          .length
      `)
      if (reviewButtonCount === 0) {
        return { skipped: true, reason: 'No pending review button available' }
      }

      const beforeDetailDialogs = await evaluate(`
        Array.from(document.querySelectorAll('.detail-dialog .el-dialog'))
          .filter((el) => el.offsetParent !== null)
          .length
      `)
      await clickSelector('.wrong-right .el-button--primary:not(.is-disabled)', 0)
      await sleep(600)
      const afterDetailDialogs = await evaluate(`
        Array.from(document.querySelectorAll('.detail-dialog .el-dialog'))
          .filter((el) => el.offsetParent !== null)
          .length
      `)
      if (afterDetailDialogs > beforeDetailDialogs) {
        throw new Error('Inner review button opened detail dialog instead of handling review')
      }

      return { beforeDetailDialogs, afterDetailDialogs }
    })

    await runStep('wrong-book', 'wrong item opens detail dialog', async () => {
      const before = await visibleDialogCount()
      await clickSelector('.wrong-item', 0)
      const after = await visibleDialogCount()
      if (after <= before) {
        throw new Error('Wrong item click did not open detail dialog')
      }
      await clickSelector('.detail-dialog .el-dialog__footer .el-button', 0)
      return { beforeDialogs: before, afterDialogs: after }
    })

    await navigate(`${BASE_URL}/submissions`)
    await screenshot('06-submissions')

    await runStep('submissions', 'submission card opens detail dialog', async () => {
      const before = await visibleDialogCount()
      await clickSelector('.submission-card', 0)
      const after = await visibleDialogCount()
      if (after <= before) {
        throw new Error('Submission card click did not open detail dialog')
      }
      return { beforeDialogs: before, afterDialogs: after }
    })

    await navigate(`${BASE_URL}/community`)
    await screenshot('07-community')

    await runStep('community', 'create post button opens dialog', async () => {
      const before = await visibleDialogCount()
      await clickText('发布帖子')
      const after = await visibleDialogCount()
      if (after <= before) {
        throw new Error('Create post button did not open dialog')
      }
      await clickText('取消')
      return { beforeDialogs: before, afterDialogs: after }
    })

    await runStep('community', 'post card opens detail page', async () => {
      await clickSelector('.post-card', 0)
      await waitFor('location.pathname.startsWith("/community/post/")', 5000)
      return { path: await locationPath() }
    })

    await navigate(`${BASE_URL}/ai`)
    await screenshot('08-ai')

    await runStep('ai', 'quick prompt button fills textarea', async () => {
      const before = await getValue('textarea')
      await clickSelector('.prompt-card', 0)
      const after = await getValue('textarea')
      if (!normalizeText(after) || normalizeText(after) === normalizeText(before)) {
        throw new Error('Quick prompt button did not fill the message textarea')
      }
      return { before, after }
    })

    await runStep('ai', 'session delete button opens confirm without switching session', async () => {
      const sessionCount = await evaluate(`
        Array.from(document.querySelectorAll('.session-item'))
          .filter((el) => el.offsetParent !== null)
          .length
      `)
      if (sessionCount === 0) {
        return { skipped: true, reason: 'No saved sessions available' }
      }

      const beforeSelected = await evaluate(`
        (() => {
          const active = document.querySelector('.session-item.active')
          return active ? (active.innerText || active.textContent || '').replace(/\\s+/g, ' ').trim() : ''
        })()
      `)
      const beforeDialogs = await visibleDialogCount()
      await clickSelector('.session-item .el-button', 0)
      const afterDialogs = await visibleDialogCount()
      if (afterDialogs <= beforeDialogs) {
        throw new Error('Session delete button did not open confirmation dialog')
      }

      const afterSelected = await evaluate(`
        (() => {
          const active = document.querySelector('.session-item.active')
          return active ? (active.innerText || active.textContent || '').replace(/\\s+/g, ' ').trim() : ''
        })()
      `)
      if (normalizeText(beforeSelected) !== normalizeText(afterSelected)) {
        throw new Error('Clicking delete changed the selected session unexpectedly')
      }

      await clickSelector('.el-message-box__btns .el-button', 0)
      return { beforeSelected, afterSelected, beforeDialogs, afterDialogs }
    })

    await navigate(`${BASE_URL}/problem/1`)
    await screenshot('08b-problem-detail-menu')

    await runStep('problem-detail', 'user menu opens language dialog', async () => {
      const beforeDialogs = await visibleDialogCount()
      await clickSelector('.user-info')
      await sleep(400)
      await clickText('设置语言')
      const afterDialogs = await visibleDialogCount()
      if (afterDialogs <= beforeDialogs) {
        throw new Error('Language menu item did not open a dialog')
      }
      await clickSelector('.el-dialog__footer .el-button', 0)
      return { beforeDialogs, afterDialogs }
    })

    await runStep('problem-detail', 'user menu profile item navigates', async () => {
      await clickSelector('.user-info')
      await sleep(400)
      await clickText('个人主页')
      await waitFor('location.pathname === "/profile"', 5000)
      return { path: await locationPath() }
    })

    await navigate(`${BASE_URL}/profile`)
    await screenshot('09-profile')

    await runStep('profile', 'settings button navigates to settings page', async () => {
      await clickText('资料设置')
      await waitFor('location.pathname === "/profile/settings"', 5000)
      return { path: await locationPath() }
    })

    await navigate(`${BASE_URL}/profile`)

    await runStep('profile', 'recent submission inner problem button opens problem page', async () => {
      await waitFor('document.querySelectorAll(".submission-link-button").length > 0', 5000)
      await clickSelector('.submission-link-button', 0)
      await sleep(1200)
      const pathValue = await locationPath()
      if (!pathValue.startsWith('/problem/')) {
        throw new Error(`Expected /problem/... but got ${pathValue}`)
      }
      return { path: pathValue }
    })

    await navigate(`${BASE_URL}/admin`)
    await screenshot('10-admin')

    await runStep('admin', 'user role edit button opens dialog', async () => {
      const before = await visibleDialogCount()
      await clickSelector('.admin-table .action-btn', 0)
      const after = await visibleDialogCount()
      if (after <= before) {
        throw new Error('Modify role button did not open dialog')
      }
      await clickSelector('.role-dialog .el-dialog__footer .el-button', 0)
      return { beforeDialogs: before, afterDialogs: after }
    })

    await runStep('admin', 'role tab add button opens dialog', async () => {
      await clickSelector('.admin-tabs .el-tabs__item', 3)
      await sleep(1000)
      const before = await visibleDialogCount()
      await clickSelector('.add-btn')
      const after = await visibleDialogCount()
      if (after <= before) {
        throw new Error('Add role button did not open dialog')
      }
      await clickSelector('.el-dialog__footer .el-button', 0)
      return { beforeDialogs: before, afterDialogs: after }
    })

    await screenshot('11-admin-final')
  } finally {
    if (cdp) {
      await cdp.close().catch(() => {})
    }

    if (chromeProcess && !chromeProcess.killed) {
      chromeProcess.kill('SIGTERM')
    }

    fs.writeFileSync(REPORT_PATH, JSON.stringify(report, null, 2), 'utf8')
    console.log(JSON.stringify(report, null, 2))
  }
}

main().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
