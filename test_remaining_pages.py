import os
import time
from pathlib import Path

from playwright.sync_api import sync_playwright

BASE_URL = os.getenv("FRONTEND_BASE_URL", "http://127.0.0.1:3000")
SCREENSHOT_DIR = Path("test_screenshots/remaining_pages")
SCREENSHOT_DIR.mkdir(parents=True, exist_ok=True)

PAGES_TO_TEST = [
    {"path": "/dashboard/code-run", "name": "code-run"},
    {"path": "/dashboard/ai", "name": "ai"},
    {"path": "/dashboard/wrong-book", "name": "wrong-book"},
    {"path": "/dashboard/submissions", "name": "submissions"},
    {"path": "/dashboard/profile/overview", "name": "profile-overview"},
]

with sync_playwright() as playwright:
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()

    print("=== start remaining pages smoke test ===")

    for item in PAGES_TO_TEST:
        url = f"{BASE_URL}{item['path']}"
        print(f"testing {url}")

        page.goto(url, wait_until="networkidle", timeout=15000)
        time.sleep(1)

        screenshot_path = SCREENSHOT_DIR / f"{item['name']}.png"
        page.screenshot(path=str(screenshot_path), full_page=True)
        print(f"screenshot saved: {screenshot_path}")

    browser.close()
    print("=== smoke test finished ===")
