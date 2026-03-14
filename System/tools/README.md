# 题目工具使用说明

## 📁 工具目录

| 文件 | 说明 |
|------|------|
| `problem_generator.py` | 题目生成器 - 自动生成题目并导出Excel/CSV |
| `test_case_generator.py` | 测试用例生成器 - 为题目生成测试用例 |

---

## 🚀 快速开始

### 1. 安装依赖

```bash
pip install pandas openpyxl
```

### 2. 运行题目生成器

```bash
cd tools
python problem_generator.py
```

会自动生成两个文件：
- `problems_YYYYMMDD_HHMMSS.xlsx` - Excel格式
- `problems_YYYYMMDD_HHMMSS.csv` - CSV格式

---

## 📝 自定义题目

### 方法一：修改代码添加题目

编辑 `problem_generator.py`，在 `main()` 函数中调用 `add_problem()`：

```python
generator.add_problem(
    title="题目名称",
    content="题目描述内容",
    input_example="输入示例",
    output_example="输出示例",
    difficulty=0,  # 0=简单, 1=中等, 2=困难
    language="java",
    tags="标签1,标签2",
    knowledge_points="知识点1,知识点2",
    hints=[{"step": 1, "content": "提示内容"}],
    sample_explanation="示例解释"
)
```

### 方法二：直接在Excel中编辑

1. 运行生成器得到Excel模板
2. 用Excel打开文件
3. 直接添加/修改题目
4. 保存后通过系统导入接口上传

---

## 🧪 生成测试用例

运行测试用例生成器：

```bash
python test_case_generator.py
```

会为题目生成对应的测试用例SQL语句。

---

## 📊 导入到系统

生成Excel/CSV文件后，通过系统的导入接口上传：

- **Excel导入**：`POST /api/admin/problem/import-excel`
- **CSV导入**：`POST /api/admin/problem/import-csv`

参考 `IMPORT_TEMPLATE.md` 了解详细导入步骤。

---

## 💡 扩展题目类型

你可以在 `ProblemGenerator` 类中添加新的生成方法：

```python
def generate_my_custom_problems(self):
    problems_data = [
        # 你的题目数据...
    ]
    for p in problems_data:
        self.add_problem(...)
```

然后在 `main()` 中调用：
```python
generator.generate_my_custom_problems()
```
