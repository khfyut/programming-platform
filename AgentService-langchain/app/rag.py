import os
import chromadb
from chromadb.config import Settings
from chromadb.utils import embedding_functions

# ── 初始化 ChromaDB ────────────────────────────

# 数据存到本地磁盘，服务重启不丢
CHROMA_PATH = os.path.join(os.path.dirname(__file__), "..", "data", "chroma")

# 中文向量模型，对中文语义更准确
_embeddings = embedding_functions.SentenceTransformerEmbeddingFunction(
    model_name="shibing624/text2vec-base-chinese",
)

# ChromaDB 客户端
_client = chromadb.PersistentClient(
    path=CHROMA_PATH,
    settings=Settings(anonymized_telemetry=False),
)


def get_collection(name: str):
    """获取或创建一个集合。"""
    return _client.get_or_create_collection(
        name=name,
        embedding_function=_embeddings,
        metadata={"hnsw:space": "cosine"},  # 用余弦相似度
    )

# ── 知识数据 ──────────────────────────────────

# 内置的编程知识点 + 常见错误（后续可对接 Java 数据库）
KNOWLEDGE_ITEMS = [
    # 数据结构
    {
        "id": "k1",
        "title": "数组基础",
        "content": "数组是最基本的数据结构，在内存中连续存储相同类型元素。Java 中 int[] arr = new int[10]。核心操作：遍历（for/增强for）、下标访问（O(1)）、插入删除（O(n)，需要移动元素）。常见错误：数组越界 ArrayIndexOutOfBoundsException、忘记初始化、把 length 和 length() 搞混。",
    },
    {
        "id": "k2",
        "title": "哈希表原理",
        "content": "哈希表（HashMap）通过哈希函数把 key 映射到数组下标，实现 O(1) 的平均查找。常用场景：两数之和（用 map 存 target-nums[i]）、字符频率统计、去重。常见错误：用可变对象做 key 导致哈希值变化、未重写 equals 和 hashCode、并发修改 ConcurrentModificationException。",
    },
    {
        "id": "k3",
        "title": "时间复杂度分析",
        "content": "O(1) 常数、O(log n) 二分、O(n) 单层循环、O(n²) 嵌套循环、O(2^n) 递归暴力。面试高频：看到双重循环先想能不能用 HashMap 优化到 O(n)。常见错误：把循环内的 API 调用当成 O(1)、忽略字符串拼接的 O(n) 开销、递归没有考虑栈深度。",
    },
    {
        "id": "k4",
        "title": "双指针技巧",
        "content": "双指针常用于有序数组问题：左右指针（两数之和II、接雨水）、快慢指针（环形链表、移除元素）、滑动窗口（最长无重复子串）。核心思路：两个指针分别从两端或同向移动，每次移动一个指针缩小搜索空间。常见错误：指针移动条件写反、忘记更新窗口内状态、边界条件没处理（left <= right vs left < right）。",
    },
    # 常见错误
    {
        "id": "e1",
        "title": "NullPointerException",
        "content": "空指针是最常见错误。原因：调用 null 对象的方法、数组元素未初始化、Map.get() 返回 null 后直接操作。解决：使用前判空 if (obj != null)、用 Optional.ofNullable()、初始化时给默认值、用 Objects.requireNonNull() 提前校验。",
    },
    {
        "id": "e2",
        "title": "数组越界",
        "content": "ArrayIndexOutOfBoundsException。常见原因：循环条件写成 i <= arr.length（应该是 <）、下标从 1 开始但数组从 0 开始、循环内修改了数组长度。解决：始终用 i < arr.length、考虑用增强 for 循环、使用 List 替代数组。",
    },
]


def import_knowledge():
    """
    把内置知识点导入 ChromaDB。

    只需要运行一次。重复运行会覆盖旧数据。
    """
    col = get_collection("knowledge_base")

    # 清空旧数据
    try:
        existing = col.get()
        if existing["ids"]:
            col.delete(ids=existing["ids"])
    except Exception:
        pass

    # 导入新数据
    col.add(
        ids=[item["id"] for item in KNOWLEDGE_ITEMS],
        documents=[item["content"] for item in KNOWLEDGE_ITEMS],
        metadatas=[{"title": item["title"]} for item in KNOWLEDGE_ITEMS],
    )
    print(f"[RAG] 已导入 {len(KNOWLEDGE_ITEMS)} 条知识点")

def search_knowledge(query: str, top_k: int = 3) -> str:
    """
    检索相关知识。

    参数:
        query: 学生问题或关键词，如 "哈希表怎么用"
        top_k: 返回最相似的几条，默认 3

    返回:
        拼接好的知识文本，可直接注入 Agent 的 system prompt
    """
    col = get_collection("knowledge_base")

    # ChromaDB 自动把 query 向量化 + 相似度检索
    results = col.query(query_texts=[query], n_results=top_k)

    if not results or not results.get("documents") or not results["documents"][0]:
        return ""

    # 把检索到的知识点拼接成一段文本
    parts = []
    documents = results["documents"][0]   # 最相似的文档列表
    metadatas = results["metadatas"][0]   # 对应的元数据

    for i, doc in enumerate(documents):
        title = metadatas[i].get("title", "知识点") if i < len(metadatas) else "知识点"
        parts.append(f"【{title}】\n{doc}")

    return "\n\n".join(parts)