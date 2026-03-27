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
    print(f"登录响应: {response.status_code}")
    print(f"登录结果: {response.text}")
    
    if response.status_code == 200:
        result = response.json()
        if result.get("code") == 200:
            token = result["data"]["token"]
            print(f"获取到token: {token[:20]}...")
            
            # 测试知识点统计API
            stats_url = "http://localhost:8080/api/knowledge-graph/stats"
            headers = {"Authorization": token}
            
            print("\n正在测试知识点统计API...")
            stats_response = requests.get(stats_url, headers=headers)
            print(f"API响应: {stats_response.status_code}")
            
            if stats_response.status_code == 200:
                stats_result = stats_response.json()
                print(f"API结果: {json.dumps(stats_result, indent=2, ensure_ascii=False)[:500]}")
                
                if stats_result.get("code") == 200:
                    data = stats_result.get("data", [])
                    print(f"\n成功获取到 {len(data)} 个知识点")
                    if data:
                        print(f"第一个知识点: {data[0]}")
            else:
                print(f"API请求失败: {stats_response.text}")
        else:
            print(f"登录失败: {result.get('msg')}")
    else:
        print(f"登录请求失败: {response.text}")
        
except Exception as e:
    print(f"测试出错: {e}")
