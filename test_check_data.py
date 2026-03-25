import requests
import json

# 登录获取token
login_url = "http://localhost:8080/api/user/login"
login_data = {
    "username": "testuser123",
    "password": "testpass123"
}

try:
    response = requests.post(login_url, json=login_data)
    result = response.json()
    token = result["data"]["token"]
    headers = {"Authorization": token}
    
    # 获取题目列表
    problems_url = "http://localhost:8080/api/problem/list?page=1&size=20"
    print("正在获取题目列表...")
    problems_response = requests.get(problems_url, headers=headers)
    problems_result = problems_response.json()
    
    if problems_result.get("code") == 200:
        problems = problems_result.get("data", {}).get("list", [])
        print(f"\n获取到 {len(problems)} 道题目")
        
        for i, problem in enumerate(problems[:5]):
            print(f"\n题目 {i+1}: {problem.get('title')}")
            print(f"  ID: {problem.get('id')}")
            print(f"  知识点: {problem.get('knowledgePoints')}")
            print(f"  标签: {problem.get('tags')}")
    
    # 获取知识点列表
    knowledge_url = "http://localhost:8080/api/knowledge-graph"
    print("\n\n正在获取知识点列表...")
    knowledge_response = requests.get(knowledge_url, headers=headers)
    knowledge_result = knowledge_response.json()
    
    if knowledge_result.get("code") == 200:
        nodes = knowledge_result.get("data", {}).get("nodes", [])
        print(f"\n获取到 {len(nodes)} 个知识点")
        
        for i, node in enumerate(nodes[:10]):
            print(f"  {i+1}. {node.get('name')} (ID: {node.get('id')}, Level: {node.get('level')})")
        
except Exception as e:
    print(f"测试出错: {e}")
    import traceback
    traceback.print_exc()
