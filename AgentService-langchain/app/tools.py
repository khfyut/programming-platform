import os
import httpx
from langchain.tools import tool

# Java 后端地址 + 内部密钥
JAVA_BACKEND = "http://127.0.0.1:8080"
INTERNAL_KEY = os.getenv("INTERNAL_API_KEY", "phoebe-shared-secret-2026")
# 公共请求头：每个工具请求都带上
AUTH_HEADER = {"X-Internal-Key": INTERNAL_KEY}

@tool
async def search_problems(keyword: str = "", difficulty: str = "") -> str:
    """
    搜索题库中的题目。

    参数:
        keyword: 搜索关键词，如 "数组"、"排序"、"动态规划"。可以为空。
        difficulty: 难度筛选，可选 "easy"、"medium"、"hard"。可以为空。

    返回: 匹配的题目列表，每行格式：ID:xxx 标题 (难度)
    """
    params = {"page": 1, "size": 5}
    if keyword:
        params["keyword"] = keyword
    if difficulty:
        params["difficulty"] = difficulty

    async with httpx.AsyncClient(timeout=10) as client:
        resp = await client.get(
            f"{JAVA_BACKEND}/api/problem/list",
            params=params,
            headers=AUTH_HEADER,
        )
        data = resp.json()
        if data.get("code") == 200 and data.get("data"):
            problems = data["data"].get("records", [])[:5]
            if not problems:
                return "未找到匹配的题目"
            return "\n".join(
                f"ID:{p.get('id')} {p.get('title')} (难度:{p.get('difficulty','?')})"
                for p in problems
            )
        return "搜索题目失败"


@tool
async def get_problem_detail(problem_id: int) -> str:
    """
    获取某道题的完整信息。

    参数:
        problem_id: 题目 ID，必须是整数

    返回: 题目标题、描述、难度、输入输出说明
    """
    async with httpx.AsyncClient(timeout=10) as client:
        resp = await client.get(
            f"{JAVA_BACKEND}/api/problem/detail/{problem_id}",
            headers=AUTH_HEADER,
        )
        data = resp.json()
        if data.get("code") == 200 and data.get("data"):
            p = data["data"]
            return (
                f"标题: {p.get('title')}\n"
                f"难度: {p.get('difficulty')}\n"
                f"描述: {p.get('description', '')[:500]}\n"
                f"输入说明: {p.get('inputDescription', '无')}\n"
                f"输出说明: {p.get('outputDescription', '无')}"
            )
        return f"未找到 ID 为 {problem_id} 的题目"


@tool
async def get_categories() -> str:
    """
    获取所有题目分类。

    返回: 分类名称列表，如 "数组, 字符串, 动态规划, ..."
    """
    async with httpx.AsyncClient(timeout=10, proxy=None) as client:
        resp = await client.get(
            f"{JAVA_BACKEND}/api/problem/categories",
            headers=AUTH_HEADER,
        )
        data = resp.json()
        if data.get("code") == 200 and data.get("data"):
            categories = [c.get("name", str(c)) for c in data["data"]]
            return "题目分类: " + ", ".join(categories)
        return "获取分类失败"


# 所有工具，给 create_agent 用
TOOLS = [search_problems, get_problem_detail, get_categories]