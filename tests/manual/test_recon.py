
from playwright.sync_api import sync_playwright
import os

# 创建截图目录
screenshot_dir = "d:/Desktop/毕业设计/Code/test_screenshots"
os.makedirs(screenshot_dir, exist_ok=True)

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False)
    page = browser.new_page()
    
    print("=== 开始前端应用侦察 ===")
    
    # 1. 导航到登录页面
    print("\n1. 导航到登录页面...")
    page.goto('http://localhost:3000')
    page.wait_for_load_state('networkidle')
    page.screenshot(path=f'{screenshot_dir}/01_login_page.png', full_page=True)
    print(f"   截图已保存: {screenshot_dir}/01_login_page.png")
    
    # 发现页面元素
    print("\n2. 发现页面元素:")
    
    # 按钮
    buttons = page.locator('button').all()
    print(f"   按钮数量: {len(buttons)}")
    for i, button in enumerate(buttons):
        try:
            text = button.inner_text() if button.is_visible() else "[隐藏]"
            print(f"     [{i}] {text}")
        except:
            print(f"     [{i}] [无法获取文本]")
    
    # 链接
    links = page.locator('a[href]').all()
    print(f"\n   链接数量: {len(links)}")
    for i, link in enumerate(links[:10]):  # 显示前10个
        try:
            text = link.inner_text().strip()
            href = link.get_attribute('href')
            print(f"     [{i}] {text} -> {href}")
        except:
            print(f"     [{i}] [无法获取链接信息]")
    
    # 输入框
    inputs = page.locator('input, textarea, select').all()
    print(f"\n   输入字段数量: {len(inputs)}")
    for i, input_elem in enumerate(inputs):
        try:
            name = input_elem.get_attribute('name') or input_elem.get_attribute('id') or "[未命名]"
            input_type = input_elem.get_attribute('type') or 'text'
            placeholder = input_elem.get_attribute('placeholder') or ""
            print(f"     [{i}] {name} ({input_type}) - {placeholder}")
        except:
            print(f"     [{i}] [无法获取输入字段信息]")
    
    # 获取页面标题和内容
    title = page.title()
    print(f"\n3. 页面标题: {title}")
    
    # 获取控制台日志
    print("\n4. 检查控制台:")
    logs = []
    def log_listener(msg):
        logs.append(f"{msg.type}: {msg.text}")
    
    page.on("console", log_listener)
    
    # 尝试点击登录/注册相关按钮
    print("\n5. 尝试交互:")
    try:
        # 查找可能的登录或注册按钮
        login_button = page.locator('button:has-text("登录")')
        register_button = page.locator('button:has-text("注册")')
        
        if login_button.count() > 0:
            print("   找到登录按钮")
            page.screenshot(path=f'{screenshot_dir}/02_before_login_click.png')
        
        if register_button.count() > 0:
            print("   找到注册按钮")
    except Exception as e:
        print(f"   交互过程中的异常: {e}")
    
    # 尝试导航到注册页面
    print("\n6. 尝试访问注册页面:")
    try:
        # 查找注册链接
        register_link = page.locator('a:has-text("注册")')
        if register_link.count() > 0:
            register_link.click()
            page.wait_for_load_state('networkidle')
            page.screenshot(path=f'{screenshot_dir}/03_register_page.png')
            print(f"   注册页面截图已保存: {screenshot_dir}/03_register_page.png")
    except Exception as e:
        print(f"   访问注册页面时出错: {e}")
    
    # 回到登录页
    page.goto('http://localhost:3000')
    page.wait_for_load_state('networkidle')
    
    # 获取页面内容
    content = page.content()
    with open(f'{screenshot_dir}/page_source.html', 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"\n7. 页面源代码已保存: {screenshot_dir}/page_source.html")
    
    print("\n=== 初步侦察完成 ===")
    
    browser.close()

