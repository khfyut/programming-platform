import requests
import json

# 测试登录
login_url = "http://localhost:8080/api/user/login"
login_data = {
    "username": "admin",
    "password": "admin123"
}

try:
    print("正在登录...")
    response = requests.post(login_url, json=login_data)
    
    if response.status_code == 200:
        result = response.json()
        if result.get("code") == 200:
            token = result["data"]["token"]
            print(f"登录成功，获取到token")
            
            # 获取所有题目
            problems_url = "http://localhost:8080/api/problem/list?page=1&size=100"
            headers = {"Authorization": f"Bearer {token}"}
            
            print("\n正在获取题目列表...")
            problems_response = requests.get(problems_url, headers=headers)
            
            if problems_response.status_code == 200:
                problems_result = problems_response.json()
                
                if problems_result.get("code") == 200:
                    data = problems_result.get("data", {})
                    problem_list = data.get("list", [])
                    total = data.get("total", 0)
                    
                    print(f"\n总共 {total} 道题目")
                    print(f"获取到 {len(problem_list)} 道题目\n")
                    
                    # 显示所有题目及其语言
                    print("=" * 100)
                    print(f"{'ID':<5} {'标题':<40} {'语言':<15}")
                    print("=" * 100)
                    
                    for problem in problem_list:
                        problem_id = problem.get("id", "")
                        title = problem.get("title", "")
                        language = problem.get("language", "")
                        
                        print(f"{problem_id:<5} {title[:38]:<40} {language:<15}")
                    
                    # 按语言分组统计
                    print("\n" + "=" * 100)
                    print("按语言统计：")
                    print("=" * 100)
                    
                    language_stats = {}
                    for problem in problem_list:
                        lang = problem.get("language", "未知")
                        if lang not in language_stats:
                            language_stats[lang] = 0
                        language_stats[lang] += 1
                    
                    for lang, count in language_stats.items():
                        print(f"{lang:<20} {count} 道题")
                else:
                    print(f"获取题目失败: {problems_result.get('msg')}")
            else:
                print(f"API请求失败: {problems_response.text}")
        else:
            print(f"登录失败: {result.get('msg')}")
    else:
        print(f"登录请求失败: {response.text}")
        
except Exception as e:
    print(f"出错: {e}")
