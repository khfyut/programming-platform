import requests, json
r = requests.post("http://127.0.0.1:8766/decision", json={
    "user_message": "有哪些关于数组的题目",
    "consecutive_failures": 3, "hint_count": 2,
    "has_viewed_solution": False
})
print(json.dumps(r.json(), ensure_ascii=False, indent=2))