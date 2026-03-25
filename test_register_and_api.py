import os
import traceback

import requests

API_BASE_URL = os.getenv("API_BASE_URL", "http://127.0.0.1:8080")

register_url = f"{API_BASE_URL}/api/user/register"
login_url = f"{API_BASE_URL}/api/user/login"
stats_url = f"{API_BASE_URL}/api/knowledge-graph/stats"

register_data = {
    "username": "testuser123",
    "password": "testpass123"
}

try:
    print("registering test user...")
    response = requests.post(register_url, json=register_data, timeout=10)
    print("register status:", response.status_code)
    print("register body:", response.text)

    print("logging in...")
    response = requests.post(login_url, json=register_data, timeout=10)
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
            print("stats body:", stats_response.text[:500])
except Exception as exc:
    print("test failed:", exc)
    traceback.print_exc()
