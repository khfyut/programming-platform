import json
import os

import requests

API_BASE_URL = os.getenv("API_BASE_URL", "http://127.0.0.1:8080")

login_url = f"{API_BASE_URL}/api/user/login"
stats_url = f"{API_BASE_URL}/api/knowledge-graph/stats"

login_data = {
    "username": os.getenv("TEST_USERNAME", "admin"),
    "password": os.getenv("TEST_PASSWORD", "admin123")
}

try:
    print("logging in...")
    response = requests.post(login_url, json=login_data, timeout=10)
    print("login status:", response.status_code)
    print("login body:", response.text)

    if response.status_code == 200:
        result = response.json()
        if result.get("code") == 200:
            token = result["data"]["token"]
            headers = {"Authorization": f"Bearer {token}"}

            print("checking knowledge graph stats...")
            stats_response = requests.get(stats_url, headers=headers, timeout=10)
            print("stats status:", stats_response.status_code)

            if stats_response.status_code == 200:
                stats_result = stats_response.json()
                print(json.dumps(stats_result, indent=2, ensure_ascii=False)[:500])
except Exception as exc:
    print("test failed:", exc)
