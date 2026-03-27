
from playwright.sync_api import sync_playwright
import os
import time

# 创建截图目录
screenshot_dir = "d:/Desktop/毕业设计/Code/test_screenshots/remaining_pages"
os.makedirs(screenshot_dir, exist_ok=True)

# 要测试的页面路由
pages_to_test = [
    {"path": "/code-run", "name": "在线运行"},
    {"path": "/ai", "name": "AI答疑"},
    {"path": "/wrong-book", "name": "错题本"},
    {"path": "/submissions", "name": "提交记录"},
    {"path": "/profile", "name": "个人主页"}
]

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False)
    page = browser.new_page()
    
    print("=== 开始测试剩余页面 ===")
    
    for page_info in pages_to_test:
        path = page_info["path"]
        name = page_info["name"]
        
        print(f"\n测试 {name} 页面 ({path})...")
        
        # 导航到页面
        page.goto(f'http://localhost:3001{path}')
        
        # 等待页面加载
        page.wait_for_load_state('networkidle', timeout=10000)
        
        # 等待2秒确保页面完全渲染
        time.sleep(2)
        
        # 截图
        screenshot_path = f"{screenshot_dir}/{name.replace(' ', '_').lower()}.png"
        page.screenshot(path=screenshot_path, full_page=True)
        print(f"  截图已保存: {screenshot_path}")
        
        # 检查页面元素
        try:
            # 检查导航栏是否存在
            nav_links = page.locator('nav a').all()
            print(f"  导航栏链接数量: {len(nav_links)}")
            
            # 检查页面标题
            title = page.title()
            print(f"  页面标题: {title}")
            
            # 检查主要内容区域
            main_content = page.locator('main').first
            if main_content.is_visible():
                print(f"  主内容区域: 可见")
            else:
                print(f"  主内容区域: 不可见")
                
        except Exception as e:
            print(f"  检查页面元素时出错: {e}")
        
        # 检查控制台日志
        console_logs = []
        def log_listener(msg):
            if msg.type == "error":
                console_logs.append(f"ERROR: {msg.text}")
            elif msg.type == "warning":
                console_logs.append(f"WARNING: {msg.text}")
        
        page.on("console", log_listener)
        
        # 等待1秒收集日志
        time.sleep(1)
        
        if console_logs:
            print(f"  控制台日志:")
            for log in console_logs[:5]:  # 只显示前5条
                print(f"    {log}")
        else:
            print(f"  控制台无错误或警告")
    
    print("\n=== 测试完成 ===")
    browser.close()

