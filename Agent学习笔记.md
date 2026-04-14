# Agent工作流程学习笔记

## 一、整体调用链

```java
// 1. 前端调用
POST /api/agent/advice/40
Authorization: Bearer <token>

// 2. Controller接收
AgentController.getLearningAdvice(userId=17, submitId=40)

// 3. Service处理
AgentService.processSubmission(17, 40)
    ↓
AgentClient.getDecision(context)
    ↓
HTTP POST → Python:8000/decision
    ↓
AgentService.executeDecision(decision)
    ↓
返回AgentDecisionDTO
```

---

## 二、Java端详细流程

### 2.1 Controller层 - 入口

```java
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    private AgentService agentService;

    /**
     * 获取学习建议
     *
     * 示例请求：
     * POST http://localhost:8080/api/agent/advice/40
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
     *
     * 示例响应：
     * {
     *   "code": 200,
     *   "msg": "操作成功",
     *   "data": {
     *     "selectedStrategy": "FIRST_HINT",
     *     "recommendedActionId": "hint_001",
     *     "content": "请检查数组边界条件"
     *   }
     * }
     */
    @PostMapping("/advice/{submitId}")
    public ResultUtil getLearningAdvice(@RequestAttribute("userId") Long userId,
                                        @PathVariable Long submitId) {
        try {
            AgentDecisionDTO decision = agentService.processSubmission(userId, submitId);
            return ResultUtil.success(decision);
        } catch (Exception e) {
            return ResultUtil.error("获取学习建议失败: " + e.getMessage());
        }
    }
}
```

---

### 2.2 Service层 - 业务核心

```java
@Service
public class AgentService {

    /**
     * 处理用户提交，获取Agent学习建议
     *
     * 完整流程示例：
     *
     * 输入：userId=17, submitId=40
     *
     * 数据库状态（调用前）：
     * submit: {id=40, user_id=17, problem_id=1, result=1}
     * user_problem_interaction: null（首次提交）
     *
     * 执行步骤：
     * 1. 查询submit → 找到记录
     * 2. 权限校验 → 17==17 ✓
     * 3. 查询user → 获取用户信息
     * 4. 查询problem → 获取题目信息
     * 5. 创建interaction → {user_id=17, problem_id=1, hint_count=0, diagnose_count=0}
     * 6. 计算失败次数 → consecutive_failures=1
     * 7. 组装context → 见下方示例
     * 8. 调用Agent → 返回decision
     * 9. 执行动作 → hint_count=1
     * 10. 记录日志 → learning_event_log新增记录
     */
    @Transactional
    public AgentDecisionDTO processSubmission(Long userId, Long submitId) {
        // 步骤1：查询提交记录
        Submit submit = submitMapper.findById(submitId);
        if (submit == null) {
            throw new RuntimeException("提交记录不存在");
        }

        // 步骤2：权限校验（安全关键）
        // 示例：userId=17, submit.userId=17 → 通过
        // 示例：userId=15, submit.userId=17 → 抛出异常
        if (!submit.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该提交记录");
        }

        // 步骤3-4：查询用户和题目
        User user = userMapper.findById(userId);
        Problem problem = problemMapper.findById(submit.getProblemId());

        // 步骤5：查询或创建交互状态
        UserProblemInteraction interaction = getOrCreateInteraction(userId, problem.getId());
        // 示例（首次）：{hint_count=0, diagnose_count=0, consecutive_failures=0}
        // 示例（第二次）：{hint_count=1, diagnose_count=0, consecutive_failures=1}

        // 步骤6：计算连续失败次数
        int consecutiveFailures = calculateConsecutiveFailures(submit, interaction);
        interaction.setConsecutiveFailures(consecutiveFailures);
        interaction.setLastSubmitId(submitId);
        userProblemInteractionMapper.update(interaction);

        // 步骤7：组装AgentContext
        AgentContextDTO context = buildAgentContext(user, problem, submit, interaction);
        /*
         * context示例（JSON格式）：
         * {
         *   "request_id": "req-a1b2c3d4",
         *   "timestamp": "2026-04-13T14:30:00",
         *   "consecutive_failures": 1,
         *   "has_viewed_solution": false,
         *   "problem": {
         *     "problem_id": 1,
         *     "title": "两数之和",
         *     "difficulty": "1",
         *     "knowledge_points": ["array", "hashmap"],
         *     "hint_shown_count": 0,
         *     "diagnosed_count": 0
         *   },
         *   "submission": {
         *     "submit_id": 40,
         *     "status": "1",
         *     "error_message": "WA",
         *     "is_first_attempt": false
         *   }
         * }
         */

        // 步骤8：调用Agent服务
        AgentDecisionDTO decision = agentClient.getDecision(context);

        // 步骤9：执行决策（关键闭环）
        executeDecision(userId, problem.getId(), submitId, decision);

        return decision;
    }
}
```

---

### 2.3 Client层 - HTTP通信

```java
@Component
public class AgentClient {

    private static final String AGENT_SERVICE_URL = "http://localhost:8000/decision";

    /**
     * 调用Python Agent Service
     *
     * 示例流程：
     *
     * 输入：AgentContextDTO对象
     *
     * 处理：
     * 1. 序列化为JSON字符串
     * 2. HTTP POST到Python服务
     * 3. 接收响应
     * 4. 反序列化为AgentDecisionDTO
     *
     * 异常处理：
     * - 如果Python服务不可用（连接超时），返回Fallback决策
     */
    public AgentDecisionDTO getDecision(AgentContextDTO context) {
        try {
            // 序列化（自动处理字段映射）
            String jsonBody = JSON.toJSONString(context);
            /*
             * jsonBody示例：
             * {
             *   "request_id": "req-a1b2c3d4",
             *   "timestamp": "2026-04-13T14:30:00",
             *   "consecutive_failures": 1,
             *   ...
             * }
             */

            RequestBody body = RequestBody.create(jsonBody, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                .url(AGENT_SERVICE_URL)
                .post(body)
                .build();

            // 发送请求
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            /*
             * responseBody示例：
             * {
             *   "response_id": "req-a1b2c3d4",
             *   "timestamp": "2026-04-13T14:30:05",
             *   "selected_strategy": "ERROR_DIAGNOSIS",
             *   "recommended_action_id": "diagnose_001",
             *   "content": "**1. 最可能的错误原因：**\n   数组索引错误...",
             *   "candidate_actions": [...]
             * }
             */

            // 反序列化
            return JSON.parseObject(responseBody, AgentDecisionDTO.class);

        } catch (IOException e) {
            // Fallback：Python不可用时返回默认策略
            System.err.println("调用Agent服务失败: " + e.getMessage());
            return createFallbackDecision(context);
        }
    }

    /**
     * 生成唯一请求ID
     *
     * 示例：req-a1b2c3d4-e5f6-7890-abcd-ef1234567890
     */
    public static String generateRequestId() {
        return "req-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
```

---

## 三、Python端详细流程

### 3.1 FastAPI入口

```python
# app/main.py
from fastapi import FastAPI
from app.schemas.context import AgentContext
from app.schemas.decision import AgentDecision
from app.core.policy import decision_engine

app = FastAPI()

@app.post("/decision", response_model=AgentDecision)
async def decision(context: AgentContext):
    """
    Agent决策接口

    示例请求（JSON）：
    {
        "request_id": "req-a1b2c3d4",
        "timestamp": "2026-04-13T14:30:00",
        "consecutive_failures": 1,
        "has_viewed_solution": false,
        "problem": {
            "problem_id": 1,
            "title": "两数之和",
            "difficulty": "1",
            "knowledge_points": ["array", "hashmap"],
            "hint_shown_count": 0,
            "diagnosed_count": 0
        },
        "submission": {
            "submit_id": 40,
            "status": "1",
            "error_message": "WA",
            "is_first_attempt": false
        }
    }

    示例响应（JSON）：
    {
        "response_id": "req-a1b2c3d4",
        "timestamp": "2026-04-13T14:30:05",
        "selected_strategy": "ERROR_DIAGNOSIS",
        "recommended_action_id": "diagnose_001",
        "content": "**1. 最可能的错误原因：**\n数组索引错误...",
        "content_type": "diagnosis"
    }
    """
    return decision_engine.make_decision(context)
```

---

### 3.2 约束检查器

```python
# app/core/policy.py

class ConstraintChecker:
    """
    硬约束检查器
    规则 > AI，确保安全
    """

    def check(self, context: AgentContext) -> List[str]:
        """
        检查哪些动作被约束阻止

        示例1（首次失败）：
        输入：consecutive_failures=0, has_viewed_solution=False
        返回：["REVEAL_ANSWER"]  # 首次失败不给答案

        示例2（已看答案）：
        输入：consecutive_failures=2, has_viewed_solution=True
        返回：["HINT", "DIAGNOSE"]  # 已看答案不再提示

        示例3（正常情况）：
        输入：consecutive_failures=1, has_viewed_solution=False
        返回：[]  # 无约束
        """
        blocked = []

        # 约束1：如果已查看答案，阻止提示和诊断
        if context.has_viewed_solution:
            blocked.extend(["HINT", "DIAGNOSE"])
            print(f"🔒 约束触发：已查看答案，阻止 {blocked}")

        # 约束2：如果是首次失败，不给答案
        if context.consecutive_failures == 0:
            blocked.append("REVEAL_ANSWER")
            print(f"🔒 约束触发：首次失败，不给答案")

        # 约束3：如果已提示3次以上，不再提示
        if context.problem.hint_shown_count >= 3:
            blocked.append("HINT")
            print(f"🔒 约束触发：已提示{context.problem.hint_shown_count}次，不再提示")

        return blocked
```

---

### 3.3 策略选择器

```python
class StrategySelector:
    """
    策略选择器
    基于状态选择最优策略
    """

    def select(self, context: AgentContext, available_actions: List[str]) -> Tuple[str, List[CandidateAction]]:
        """
        选择策略和候选动作

        示例1（首次失败）：
        输入：consecutive_failures=0
        策略：FIRST_HINT
        原因：首次失败，给简单提示

        示例2（第二次失败）：
        输入：consecutive_failures=1
        策略：ERROR_DIAGNOSIS
        原因：再次失败，需要诊断

        示例3（多次失败）：
        输入：consecutive_failures=3
        策略：KNOWLEDGE_GAP
        原因：连续失败，存在知识漏洞

        示例4（已看答案）：
        输入：has_viewed_solution=True
        策略：REVEAL_ANSWER
        原因：用户已放弃，直接给答案
        """
        cf = context.consecutive_failures
        has_viewed = context.has_viewed_solution

        # 策略路由
        if has_viewed:
            strategy = "REVEAL_ANSWER"
        elif cf == 0:
            strategy = "FIRST_HINT"
        elif cf == 1:
            strategy = "ERROR_DIAGNOSIS"
        else:
            strategy = "KNOWLEDGE_GAP"

        # 生成候选动作
        candidate_actions = self._generate_actions(strategy, context)

        return strategy, candidate_actions

    def _generate_actions(self, strategy: str, context: AgentContext) -> List[CandidateAction]:
        """根据策略生成候选动作列表"""
        actions = []

        if strategy == "FIRST_HINT":
            actions.append(CandidateAction(
                action_id="hint_001",
                action_type="HINT",
                title="💡 提示",
                description="给你一个提示",
                priority="high",
                content="请检查数组边界条件",
                required_conditions=[]
            ))

        elif strategy == "ERROR_DIAGNOSIS":
            actions.append(CandidateAction(
                action_id="diagnose_001",
                action_type="DIAGNOSE",
                title="🔍 错误诊断",
                description="AI分析你的错误",
                priority="high",
                content="**1. 最可能的错误原因：**\n数组索引错误...",
                required_conditions=[]
            ))

        return actions
```

---

### 3.4 诊断引擎

```python
# app/capabilities/diagnose.py

class DiagnosisEngine:
    """
    诊断引擎
    使用LLM生成个性化诊断内容
    """

    def __init__(self):
        self.llm = OllamaClient()

    def generate(self, context: AgentContext, strategy: str) -> str:
        """
        生成诊断内容

        示例（ERROR_DIAGNOSIS策略）：

        输入：
        - 策略：ERROR_DIAGNOSIS
        - 提交状态：WA（答案错误）
        - 错误信息：数组越界

        处理：
        1. 构建Prompt
        2. 调用Ollama（gemma4:e2b模型）
        3. 接收生成内容

        输出：
        "**1. 最可能的错误原因：**
        数组索引错误（Index Out of Bounds）...

        **2. 知识薄弱点：**
        数组索引规则理解不深...

        **3. 学习建议：**
        重点复习数组边界处理..."
        """

        # 构建Prompt
        prompt = self._build_prompt(context, strategy)
        """
        Prompt示例：

        你是一位编程学习助手。请根据以下信息给出诊断：

        【题目】两数之和
        【难度】简单
        【提交结果】答案错误(WA)
        【错误信息】数组越界
        【连续失败次数】2次

        请按以下格式给出诊断：
        1. 最可能的错误原因
        2. 学生当前的知识薄弱点
        3. 应该给出什么建议
        """

        # 调用LLM
        content = self.llm.generate(prompt, temperature=0.7)

        return content
```

---

### 3.5 LLM客户端

```python
# app/core/llm_client.py

class OllamaClient:
    """
    Ollama LLM客户端
    通过HTTP调用本地Ollama服务
    """

    def __init__(self, base_url: str = "http://localhost:11434", model: str = "gemma4:e2b"):
        self.base_url = base_url
        self.model = model
        self.generate_url = f"{base_url}/api/generate"

    def generate(self, prompt: str, temperature: float = 0.7) -> Optional[str]:
        """
        调用Ollama生成文本

        示例调用：

        输入：
        prompt = "你是一位编程学习助手..."
        temperature = 0.7

        处理：
        1. 构造请求体
        2. HTTP POST到localhost:11434/api/generate
        3. 等待响应（10秒超时）

        输出：
        "**1. 最可能的错误原因：**
        数组索引错误..."

        异常：
        - 如果Ollama未启动，返回None
        - 如果超时，返回None
        """
        try:
            print(f"🤖 调用Ollama，模型: {self.model}")
            print(f"📝 Prompt长度: {len(prompt)} 字符")

            # 准备请求数据
            data = {
                "model": self.model,
                "prompt": prompt,
                "stream": False,
                "options": {
                    "temperature": temperature
                }
            }

            # 创建请求
            req = urllib.request.Request(
                self.generate_url,
                data=json.dumps(data).encode('utf-8'),
                headers={'Content-Type': 'application/json'},
                method='POST'
            )

            # 发送请求（10秒超时）
            print("⏳ 等待Ollama响应...")
            with urllib.request.urlopen(req, timeout=10) as response:
                result = json.loads(response.read().decode('utf-8'))
                print("✅ Ollama响应成功")
                return result.get("response", "")

        except urllib.error.URLError as e:
            print(f"❌ 无法连接到Ollama: {e}")
            return None
        except Exception as e:
            print(f"❌ 调用Ollama出错: {e}")
            return None
```

---

## 四、动作执行闭环

```java
// AgentService.java

/**
 * 执行Agent决策
 *
 * 这是Agent闭环的核心：决策必须产生真实的状态变化
 *
 * 示例1（HINT动作）：
 * 输入：action_id="hint_001"
 * 执行：
 *   - userProblemInteractionMapper.incrementHintCount(17, 1)
 *   - 数据库：hint_count 0 → 1
 * 日志：记录learning_event_log
 *
 * 示例2（DIAGNOSE动作）：
 * 输入：action_id="diagnose_001"
 * 执行：
 *   - userProblemInteractionMapper.incrementDiagnoseCount(17, 1)
 *   - 数据库：diagnose_count 0 → 1
 * 日志：记录learning_event_log
 *
 * 示例3（EXPLAIN动作）：
 * 输入：action_id="explain_001"
 * 执行：
 *   - updateLastActionTime(17, 1)
 *   - 数据库：last_action_time = now()
 * 日志：记录learning_event_log
 */
private void executeDecision(Long userId, Long problemId, Long submitId,
                             AgentDecisionDTO decision) {
    String actionType = decision.getRecommendedActionId();
    if (actionType == null) {
        System.out.println("⚠️ 没有推荐动作，跳过执行");
        return;
    }

    // 提取动作类型（如 hint_001 → HINT）
    String actionCategory = extractActionCategory(actionType);

    switch (actionCategory) {
        case "HINT":
            executeHintAction(userId, problemId, submitId, decision);
            break;
        case "DIAGNOSE":
            executeDiagnoseAction(userId, problemId, submitId, decision);
            break;
        case "EXPLAIN":
            executeExplainAction(userId, problemId, submitId, decision);
            break;
        case "RECOMMEND":
            executeRecommendAction(userId, problemId, submitId, decision);
            break;
        case "REVEAL_ANSWER":
            executeRevealAnswerAction(userId, problemId, submitId, decision);
            break;
        default:
            System.out.println("ℹ️ 动作类型 " + actionCategory + " 暂无执行逻辑");
    }

    // 记录学习事件日志（关键：所有动作都记录）
    recordLearningEvent(userId, problemId, submitId, decision, actionCategory);
}

/**
 * 执行HINT动作
 *
 * 数据库变更：
 * UPDATE user_problem_interaction
 * SET hint_count = hint_count + 1,
 *     last_action_time = NOW(),
 *     update_time = NOW()
 * WHERE user_id = 17 AND problem_id = 1;
 */
private void executeHintAction(Long userId, Long problemId, Long submitId,
                               AgentDecisionDTO decision) {
    System.out.println("🎯 执行HINT动作：增加提示计数");

    // 更新数据库：hint_count + 1
    userProblemInteractionMapper.incrementHintCount(userId, problemId);

    System.out.println("✅ HINT动作执行完成：hint_count + 1");
}
```

---

## 五、数据库状态流转示例

```
【初始状态】
user_problem_interaction: 空
learning_event_log: 空

【第一次调用】
用户17提交submit 40 → WA
↓
Agent决策：FIRST_HINT → hint_001
↓
执行动作：
  hint_count: 0 → 1
  consecutive_failures: 0 → 1
↓
数据库状态：
  user_problem_interaction: {hint_count=1, diagnose_count=0, consecutive_failures=1}
  learning_event_log: {id=1, action_type=HINT, strategy=FIRST_HINT}

【第二次调用】
用户17再次提交 → WA
↓
AgentContext包含：hint_count=1, consecutive_failures=1
↓
Agent决策：ERROR_DIAGNOSIS → diagnose_001
↓
执行动作：
  diagnose_count: 0 → 1
  consecutive_failures: 1 → 2
↓
数据库状态：
  user_problem_interaction: {hint_count=1, diagnose_count=1, consecutive_failures=2}
  learning_event_log: {id=2, action_type=DIAGNOSE, strategy=ERROR_DIAGNOSIS}

【第三次调用】
用户17再次提交 → WA
↓
AgentContext包含：hint_count=1, diagnose_count=1, consecutive_failures=2
↓
Agent决策：KNOWLEDGE_GAP → explain_001
↓
执行动作：
  last_action_time = now()
  consecutive_failures: 2 → 3
↓
数据库状态：
  user_problem_interaction: {hint_count=1, diagnose_count=1, consecutive_failures=3}
  learning_event_log: {id=3, action_type=EXPLAIN, strategy=KNOWLEDGE_GAP}
```

---

## 六、关键设计要点

| 设计点 | 代码体现 | 作用 |
|--------|----------|------|
| **状态驱动** | `AgentContext`包含`hint_count`, `consecutive_failures` | 决策基于历史状态 |
| **约束优先** | `ConstraintChecker.check()` | 硬规则 > AI生成 |
| **可信状态分离** | Java管理数据库，Python只做推断 | 防止AI污染业务数据 |
| **动作闭环** | `executeDecision()`更新数据库 | 决策产生真实变化 |
| **可追溯** | `