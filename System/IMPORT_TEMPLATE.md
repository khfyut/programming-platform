# 题目导入模板

## Excel模板格式

请按照以下格式准备Excel文件（.xlsx）：

| 标题 | 内容 | 输入示例 | 输出示例 | 难度 | 语言 | 时间限制(ms) | 内存限制(MB) | 标签 | 知识点 | 提示 | 示例解释 |
|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|
| 三数之和 | 给定三个整数a、b、c，计算它们的和并输出。 | 1 2 3 | 6 | 1 | java | 1000 | 256 | 数组,基础算法 | 基础运算,输入输出 | [{"step":1,"content":"考虑使用加法运算"}] | 对于输入1和2，输出应该是它们的和3 |
| 四数之和 | 给定四个整数a、b、c、d，计算它们的和并输出。 | 1 2 3 4 | 10 | 2 | java | 1000 | 256 | 数组,基础算法 | 基础运算,输入输出 | [{"step":1,"content":"考虑使用加法运算"}] | 对于输入1、2、3、4，输出应该是它们的和10 |
| 求最大值 | 给定两个整数a和b，输出较大的那个数。 | 5 3 | 5 | 0 | java | 1000 | 256 | 条件判断,基础算法 | 条件判断,比较运算 | [{"step":1,"content":"使用if-else语句比较大小"}] | 比较a和b的大小，输出较大的那个数 |

**注意事项**：
1. 第一行为表头，从第二行开始是数据
2. 难度说明：0-简单，1-中等，2-困难
3. 语言说明：java、python
4. 所有字段都是必填的
5. 时间限制和内存限制为可选字段，不填则使用默认值（时间限制：1000ms，内存限制：256MB）
6. 标签和知识点使用逗号分隔多个值
7. 提示使用JSON数组格式，可包含多个步骤

## CSV模板格式

请按照以下格式准备CSV文件（.csv）：

标题,内容,输入示例,输出示例,难度,语言,时间限制(ms),内存限制(MB),标签,知识点,提示,示例解释
三数之和,给定三个整数a、b、c，计算它们的和并输出。,1 2 3,6,1,java,1000,256,数组,基础算法,基础运算,输入输出,"[{""step"":1,""content"":""考虑使用加法运算""}]",对于输入1和2，输出应该是它们的和3
四数之和,给定四个整数a、b、c、d，计算它们的和并输出。,1 2 3 4,10,2,java,1000,256,数组,基础算法,基础运算,输入输出,"[{""step"":1,""content"":""考虑使用加法运算""}]",对于输入1、2、3、4，输出应该是它们的和10
求最大值,给定两个整数a和b，输出较大的那个数。,5 3,5,0,java,1000,256,条件判断,基础算法,条件判断,比较运算,"[{""step"":1,""content"":""使用if-else语句比较大小""}]",比较a和b的大小，输出较大的那个数

**注意事项**：
1. 第一行为表头，从第二行开始是数据
2. 使用逗号分隔字段
3. 字段顺序必须一致
4. 所有字段都是必填的
5. 时间限制和内存限制为可选字段，不填则使用默认值（时间限制：1000ms，内存限制：256MB）
6. 标签和知识点使用逗号分隔多个值
7. 提示使用JSON数组格式，可包含多个步骤

## 导入接口说明

### Excel导入接口

**接口地址**：`POST /admin/problem/import-excel`

**请求头**：
```
Authorization: Bearer {admin_token}
Content-Type: multipart/form-data
```

**请求参数**：
- file: Excel文件（.xlsx格式）

### CSV导入接口

**接口地址**：`POST /admin/problem/import-csv`

**请求头**：
```
Authorization: Bearer {admin_token}
Content-Type: multipart/form-data
```

**请求参数**：
- file: CSV文件（.csv格式）

## 使用示例

### 使用curl导入Excel

```bash
curl -X POST http://localhost:8080/api/admin/problem/import-excel \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@/path/to/problems.xlsx"
```

### 使用curl导入CSV

```bash
curl -X POST http://localhost:8080/api/admin/problem/import-csv \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@/path/to/problems.csv"
```

### 使用Postman导入Excel

1. **设置环境变量**
   - `base_url`: `http://localhost:8080/api`
   - `admin_token`: 管理员登录后获取的token

2. **创建请求**
   - Method: POST
   - URL: `{{base_url}}/admin/problem/import-excel`
   - Headers:
     ```
     Authorization: Bearer {{admin_token}}
     ```
   - Body:
     - Type: form-data
     - Key: file
     - Type: File
     - Value: 选择Excel文件

3. **发送请求**
   - 点击Send按钮
   - 查看响应结果

### 使用Postman导入CSV

1. **创建请求**
   - Method: POST
   - URL: `{{base_url}}/admin/problem/import-csv`
   - Headers:
     ```
     Authorization: Bearer {{admin_token}}
     ```
   - Body:
     - Type: form-data
     - Key: file
     - Type: File
     - Value: 选择CSV文件

2. **发送请求**
   - 点击Send按钮
   - 查看响应结果

## 错误处理

### 常见错误

1. **文件格式错误**
   - 错误信息：文件格式不支持
   - 解决方法：确保文件是.xlsx或.csv格式

2. **字段缺失**
   - 错误信息：Excel第X行数据不完整
   - 解决方法：检查Excel文件，确保所有字段都有值

3. **文件过大**
   - 错误信息：文件大小超过限制
   - 解决方法：单个文件不超过10MB

4. **数据格式错误**
   - 错误信息：CSV第X行数据不完整
   - 解决方法：检查CSV文件，确保字段数量正确

## 验证导入结果

导入完成后，可以通过以下方式验证：

```bash
# 查看题目列表
curl -X GET http://localhost:8080/api/problem/list \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 查看系统统计
curl -X GET http://localhost:8080/api/admin/statistics \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## 批量导入对比

| 方式 | 接口 | 文件格式 | 适用场景 |
|:----:|:----:|:----:|:----:|
| Excel导入 | `/admin/problem/import-excel` | .xlsx | 可视化编辑、复杂格式 |
| CSV导入 | `/admin/problem/import-csv` | .csv | 简单格式、文本编辑 |
| JSON批量导入 | `/admin/problem/batch-import` | JSON | API调用、程序化导入 |

## 最佳实践

1. **数据准备**
   - 使用Excel模板准备题目数据
   - 确保所有字段都有值
   - 验证数据格式正确

2. **分批导入**
   - 大量题目（>100个）建议分批导入
   - 每批不超过50个题目

3. **错误处理**
   - 导入前备份现有数据
   - 导入失败时检查错误日志
   - 部分失败时重新导入失败的题目

4. **数据验证**
   - 导入后验证题目数量
   - 抽查几个题目的内容
   - 确认难度和语言设置正确