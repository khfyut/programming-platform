-- Java-first Agent content quality migration.
-- Scope: published Java problems, Java learning path bindings, and Agent-ready recommendation data.

UPDATE `problem`
SET `status` = 'DRAFT', `update_time` = NOW()
WHERE `id` = 37;

UPDATE `problem_supported_language` psl
INNER JOIN `problem` p ON p.`id` = psl.`problem_id`
SET psl.`status` = CASE WHEN LOWER(psl.`language_code`) = 'java' THEN 'ACTIVE' ELSE 'INACTIVE' END,
    psl.`is_default` = CASE WHEN LOWER(psl.`language_code`) = 'java' THEN 1 ELSE 0 END,
    psl.`update_time` = NOW()
WHERE COALESCE(p.`status`, 'PUBLISHED') = 'PUBLISHED';

INSERT INTO `knowledge_point` (`name`, `parent_id`, `level`, `category`, `difficulty`, `description`, `create_time`, `update_time`)
SELECT v.`name`, NULL, 1, 'java-agent', 0, v.`description`, NOW(), NOW()
FROM (
  SELECT '控制流' AS `name`, 'Java control flow basics' AS `description` UNION ALL
  SELECT '方法', 'Java method decomposition' UNION ALL
  SELECT '数组', 'Java array operations' UNION ALL
  SELECT '类和对象', 'Java class and object modeling' UNION ALL
  SELECT '继承', 'Java inheritance' UNION ALL
  SELECT '多态', 'Java polymorphism' UNION ALL
  SELECT '接口', 'Java interface abstraction' UNION ALL
  SELECT '抽象类', 'Java abstract class modeling' UNION ALL
  SELECT 'List', 'Java List collection' UNION ALL
  SELECT 'Set', 'Java Set collection' UNION ALL
  SELECT 'Map', 'Java Map collection' UNION ALL
  SELECT '集合工具类', 'Java collection utility practice' UNION ALL
  SELECT 'SQL', 'SQL query basics' UNION ALL
  SELECT '数据库设计', 'Database schema design basics' UNION ALL
  SELECT 'Spring Boot', 'Spring Boot backend basics' UNION ALL
  SELECT 'Spring MVC', 'Spring MVC request response practice' UNION ALL
  SELECT 'Spring Cloud', 'Spring Cloud service collaboration basics' UNION ALL
  SELECT '服务注册与发现', 'Service discovery basics' UNION ALL
  SELECT '配置中心', 'Configuration center basics' UNION ALL
  SELECT '微服务', 'Microservice collaboration practice' UNION ALL
  SELECT '二叉树', 'Binary tree traversal and recursion'
) v
WHERE NOT EXISTS (
  SELECT 1 FROM `knowledge_point` kp WHERE kp.`name` = v.`name`
);

INSERT INTO `problem` (`title`, `content`, `input`, `output`, `difficulty`, `language`, `time_limit`, `memory_limit`, `tags`, `knowledge_points`, `hints`, `sample_explanation`, `status`)
SELECT v.`title`, v.`content`, v.`input_desc`, v.`output_desc`, v.`difficulty`, 'java', 1000, 256, v.`tags`, v.`knowledge_points`, v.`hints`, v.`sample_explanation`, 'PUBLISHED'
FROM (
  SELECT '成绩等级判断' AS `title`, '读取一个 0 到 100 的整数成绩，按照 A/B/C/D/F 输出等级。90 分及以上为 A，80 到 89 为 B，70 到 79 为 C，60 到 69 为 D，其余为 F。' AS `content`, '一个整数 score' AS `input_desc`, '一个等级字符' AS `output_desc`, 0 AS `difficulty`, 'Java,控制流,条件判断' AS `tags`, '控制流' AS `knowledge_points`, '先处理高分区间，再逐步向下判断。' AS `hints`, '95 属于 90 分及以上，所以输出 A。' AS `sample_explanation` UNION ALL
  SELECT '数组区间求和', '读取数组长度、数组元素以及左右边界 l r，输出闭区间 [l,r] 的元素和。下标从 1 开始。', 'n、n 个整数、l r', '区间元素和', 0, 'Java,数组,方法', '数组,方法', '可以用一个方法接收数组和边界后返回求和结果。', '数组 1 2 3 4 5 的 2 到 4 位之和为 9。' UNION ALL
  SELECT '商品库存统计', '读取若干商品的名称、单价和库存数量，使用对象保存商品信息，输出库存总价值，保留 1 位小数。', 'n 以及 n 行 name price count', '库存总价值', 0, 'Java,类和对象,对象建模', '类和对象', '定义 Product 类保存 price 和 count。', 'pen 总价值 15，book 总价值 24，总计 39。' UNION ALL
  SELECT '员工薪资计算', '读取若干员工类型及薪资字段，用继承和多态建模，输出所有员工月薪总和。developer 的薪资为 base+bonus，manager 的薪资为 base+allowance。', 'n 以及 n 行 type base extra', '总薪资整数', 1, 'Java,继承,多态', '继承,多态', '定义抽象 Employee，并让不同角色覆写 salary 方法。', '不同员工通过同一个 salary 调用计算薪资。' UNION ALL
  SELECT '图形面积计算', '读取图形类型并计算面积。circle 后跟半径，rectangle 后跟长和宽。圆周率取 3.14159，结果保留 2 位小数。', '图形类型和参数', '面积，保留 2 位小数', 1, 'Java,接口,抽象类', '接口,抽象类', '可以定义 Shape 接口，分别实现 Circle 和 Rectangle。', '半径为 2 的圆面积约为 12.57。' UNION ALL
  SELECT '列表去重保持顺序', '读取一个整数列表，去除重复元素并保持第一次出现的顺序，输出去重后的列表。', 'n 以及 n 个整数', '去重后的整数列表', 0, 'Java,List,Set,集合', 'List,Set', 'LinkedHashSet 可以既去重又保序。', '1 2 1 3 会输出 1 2 3。' UNION ALL
  SELECT '单词频次查询', '读取 n 个单词和 1 个查询单词，输出该查询单词出现次数。', 'n、n 个单词、查询单词', '出现次数', 0, 'Java,Map,哈希表', 'Map', '用 Map<String,Integer> 累计词频。', 'java 出现 3 次则输出 3。' UNION ALL
  SELECT '学生成绩排序', '读取若干学生姓名和成绩，按照成绩降序、姓名字典序升序输出学生姓名。', 'n 以及 n 行 name score', '排序后的姓名，每行一个', 1, 'Java,List,集合工具类,排序', 'List,集合工具类', '使用 Collections.sort 或 List.sort 自定义比较器。', '同分时 Alice 排在 Bob 前面。' UNION ALL
  SELECT '名片字段格式化', '读取姓名、年龄、城市，使用对象保存字段并按固定格式输出个人名片。', 'name age city', '格式化名片', 0, 'Java,类和对象,字符串', '类和对象', '定义 Card 类并提供 format 方法。', '输入 Tom 18 Beijing 后按字段名输出。' UNION ALL
  SELECT '学生成绩等级统计', '读取多个成绩，统计 A/B/C/D/F 五个等级的人数并按固定格式输出。', 'n 以及 n 个成绩', 'A:x B:x C:x D:x F:x', 0, 'Java,控制流,数组,项目实战', '控制流,数组', '先写等级判断函数，再统计每个等级。', '每个成绩映射到一个等级计数。' UNION ALL
  SELECT '二叉树最大深度', '读取二叉树节点关系，每行给出 node left right，-1 表示空节点。根节点固定为第一行的 node，输出二叉树最大深度。', 'n 以及 n 行 node left right', '最大深度', 1, 'Java,二叉树,递归', '二叉树', '用 Map 记录左右孩子，再从根节点递归求深度。', '根节点有两个叶子节点时深度为 2。' UNION ALL
  SELECT 'SQL条件语句生成', '读取字段名、比较运算符和值，输出一条 student 表的 SELECT 条件查询语句。', 'field op value', 'SELECT SQL', 0, 'SQL,数据库,字符串', 'SQL', '注意 SQL 末尾分号。', 'age >= 18 生成 SELECT * FROM student WHERE age >= 18;' UNION ALL
  SELECT '表字段规范检查', '读取若干字段名，统计符合 snake_case 且以小写字母开头的字段数量。字段只允许小写字母、数字和下划线。', 'n 以及 n 个字段名', '合法字段数量', 0, '数据库设计,命名规范,字符串', '数据库设计', '可以使用正则表达式 [a-z][a-z0-9_]*。', 'user_id 和 created_at 合法。' UNION ALL
  SELECT 'Spring控制器响应封装', '读取状态码和消息，模拟 Spring Boot 控制器统一响应体，输出 JSON 字符串。', 'code message', 'JSON 响应字符串', 0, 'Spring Boot,Spring MVC,后端', 'Spring Boot,Spring MVC', '统一响应结构通常包含 code 和 message。', '200 ok 输出 code/message 字段。' UNION ALL
  SELECT '服务路由选择', '读取若干服务实例名称和当前负载，输出负载最小的实例名称。', 'n 以及 n 行 service load', '负载最低的服务名', 1, 'Spring Cloud,微服务,路由', 'Spring Cloud,微服务', '服务调用前常需要根据负载选择目标实例。', 'order 的负载最低则输出 order。' UNION ALL
  SELECT '服务实例发现', '读取若干服务实例的服务名、host 和 port，再读取目标服务名，输出所有匹配实例地址，逗号分隔。', 'n、实例列表、目标服务名', '匹配实例地址列表', 1, '服务注册与发现,Spring Cloud,微服务', '服务注册与发现,微服务', '注册中心本质上维护服务名到实例地址的映射。', 'user 服务有两个实例时输出两个地址。' UNION ALL
  SELECT '配置项合并查询', '读取共享配置和应用配置，再读取查询 key。应用配置覆盖共享配置，输出最终配置值。', '共享配置数量、应用配置数量、查询 key', '最终配置值', 1, '配置中心,Map,Spring Cloud', '配置中心,Map', '后写入的应用配置覆盖共享配置即可。', 'timeout 被应用配置覆盖为 60。' UNION ALL
  SELECT '订单优惠结算', '读取商品单价、数量和优惠金额，输出订单应付金额，保留 2 位小数。', 'price count discount', '应付金额', 0, 'Spring Boot,电商系统,控制流', 'Spring Boot,控制流', '后端订单接口常把业务计算封装在服务层。', '19.9*3-5.0=54.70。' UNION ALL
  SELECT '服务调用链耗时汇总', '读取一次调用链上的服务名和耗时，输出总耗时以及耗时最高的服务名。', 'n 以及 n 行 service millis', 'total=x max=service', 1, '微服务,调用链,监控', '微服务', '链路追踪会汇总总耗时并定位慢服务。', 'order 耗时最高则 max=order。'
) v
WHERE NOT EXISTS (
  SELECT 1 FROM `problem` p WHERE p.`title` = v.`title`
);

INSERT INTO `problem_supported_language` (`problem_id`, `language_code`, `is_default`, `starter_code`, `starter_filename`, `status`, `sort_order`, `create_time`, `update_time`)
SELECT p.`id`, 'java', 1, 'public class Main {\n    public static void main(String[] args) throws Exception {\n    }\n}\n', 'Main.java', 'ACTIVE', 0, NOW(), NOW()
FROM `problem` p
WHERE p.`title` IN (
  '成绩等级判断','数组区间求和','商品库存统计','员工薪资计算','图形面积计算','列表去重保持顺序','单词频次查询','学生成绩排序','名片字段格式化','学生成绩等级统计','二叉树最大深度','SQL条件语句生成','表字段规范检查','Spring控制器响应封装','服务路由选择','服务实例发现','配置项合并查询','订单优惠结算','服务调用链耗时汇总'
)
AND NOT EXISTS (
  SELECT 1 FROM `problem_supported_language` psl
  WHERE psl.`problem_id` = p.`id` AND LOWER(psl.`language_code`) = 'java'
);

INSERT INTO `test_case` (`problem_id`, `input`, `output`, `is_sample`, `sort_order`)
SELECT p.`id`, v.`input`, v.`output`, v.`is_sample`, v.`sort_order`
FROM `problem` p
JOIN (
  SELECT '成绩等级判断' AS `title`, '95' AS `input`, 'A' AS `output`, 1 AS `is_sample`, 1 AS `sort_order` UNION ALL SELECT '成绩等级判断','61','D',0,2 UNION ALL
  SELECT '数组区间求和','5\n1 2 3 4 5\n2 4','9',1,1 UNION ALL SELECT '数组区间求和','4\n10 -2 7 5\n1 3','15',0,2 UNION ALL
  SELECT '商品库存统计','2\npen 1.5 10\nbook 12.0 2','39.0',1,1 UNION ALL SELECT '商品库存统计','1\nmouse 49.9 3','149.7',0,2 UNION ALL
  SELECT '员工薪资计算','2\ndeveloper 8000 1200\nmanager 10000 3000','22200',1,1 UNION ALL SELECT '员工薪资计算','1\ndeveloper 5000 600','5600',0,2 UNION ALL
  SELECT '图形面积计算','circle 2','12.57',1,1 UNION ALL SELECT '图形面积计算','rectangle 3 4','12.00',0,2 UNION ALL
  SELECT '列表去重保持顺序','7\n1 2 1 3 2 4 4','1 2 3 4',1,1 UNION ALL SELECT '列表去重保持顺序','5\n5 5 5 6 5','5 6',0,2 UNION ALL
  SELECT '单词频次查询','6\njava spring java map java map\njava','3',1,1 UNION ALL SELECT '单词频次查询','4\na b c a\nd','0',0,2 UNION ALL
  SELECT '学生成绩排序','3\nTom 80\nAlice 95\nBob 95','Alice\nBob\nTom',1,1 UNION ALL SELECT '学生成绩排序','2\nCindy 70\nBen 90','Ben\nCindy',0,2 UNION ALL
  SELECT '名片字段格式化','Tom 18 Beijing','Name: Tom, Age: 18, City: Beijing',1,1 UNION ALL SELECT '名片字段格式化','Amy 20 Shanghai','Name: Amy, Age: 20, City: Shanghai',0,2 UNION ALL
  SELECT '学生成绩等级统计','5\n95 82 77 61 40','A:1 B:1 C:1 D:1 F:1',1,1 UNION ALL SELECT '学生成绩等级统计','3\n100 90 59','A:2 B:0 C:0 D:0 F:1',0,2 UNION ALL
  SELECT '二叉树最大深度','3\n1 2 3\n2 -1 -1\n3 -1 -1','2',1,1 UNION ALL SELECT '二叉树最大深度','4\n1 2 -1\n2 3 -1\n3 4 -1\n4 -1 -1','4',0,2 UNION ALL
  SELECT 'SQL条件语句生成','age >= 18','SELECT * FROM student WHERE age >= 18;',1,1 UNION ALL SELECT 'SQL条件语句生成','score < 60','SELECT * FROM student WHERE score < 60;',0,2 UNION ALL
  SELECT '表字段规范检查','4\nuser_id\nUserName\n2age\ncreated_at','2',1,1 UNION ALL SELECT '表字段规范检查','3\nname\norder_id\nbad-flag','2',0,2 UNION ALL
  SELECT 'Spring控制器响应封装','200 ok','{"code":200,"message":"ok"}',1,1 UNION ALL SELECT 'Spring控制器响应封装','404 not_found','{"code":404,"message":"not_found"}',0,2 UNION ALL
  SELECT '服务路由选择','3\nuser 30\norder 12\npay 18','order',1,1 UNION ALL SELECT '服务路由选择','2\nconfig 9\nregistry 3','registry',0,2 UNION ALL
  SELECT '服务实例发现','3\nuser 10.0.0.1 8080\norder 10.0.0.2 8081\nuser 10.0.0.3 8082\nuser','10.0.0.1:8080,10.0.0.3:8082',1,1 UNION ALL SELECT '服务实例发现','2\npay 10.0.0.4 9000\ncart 10.0.0.5 9001\nuser','',0,2 UNION ALL
  SELECT '配置项合并查询','2\ntimeout 30\nregion cn\n2\ntimeout 60\nfeature on\ntimeout','60',1,1 UNION ALL SELECT '配置项合并查询','1\nregion cn\n1\nfeature on\nregion','cn',0,2 UNION ALL
  SELECT '订单优惠结算','19.9 3 5.0','54.70',1,1 UNION ALL SELECT '订单优惠结算','10.0 2 25.0','0.00',0,2 UNION ALL
  SELECT '服务调用链耗时汇总','3\nuser 20\norder 35\npay 15','total=70 max=order',1,1 UNION ALL SELECT '服务调用链耗时汇总','2\ngateway 8\nauth 12','total=20 max=auth',0,2
) v ON v.`title` = p.`title`
WHERE NOT EXISTS (
  SELECT 1 FROM `test_case` tc
  WHERE tc.`problem_id` = p.`id` AND tc.`input` = v.`input` AND tc.`output` = v.`output`
);

INSERT INTO `reference_solution` (`problem_id`, `language`, `solution_code`, `time_complexity`, `space_complexity`, `explanation`, `hints`)
SELECT p.`id`, 'java', v.`solution_code`, v.`time_complexity`, v.`space_complexity`, v.`explanation`, JSON_ARRAY(v.`hint`)
FROM `problem` p
JOIN (
  SELECT '成绩等级判断' AS `title`, 'import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    int score = sc.nextInt();\n    char grade = score >= 90 ? ''A'' : score >= 80 ? ''B'' : score >= 70 ? ''C'' : score >= 60 ? ''D'' : ''F'';\n    System.out.print(grade);\n  }\n}\n' AS `solution_code`, 'O(1)' AS `time_complexity`, 'O(1)' AS `space_complexity`, 'Use ordered condition checks from high score to low score.' AS `explanation`, 'Check higher score ranges first.' AS `hint` UNION ALL
  SELECT '数组区间求和','import java.util.*;\npublic class Main {\n  static int rangeSum(int[] a, int l, int r) { int sum = 0; for (int i = l; i <= r; i++) sum += a[i]; return sum; }\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in); int n = sc.nextInt(); int[] a = new int[n + 1];\n    for (int i = 1; i <= n; i++) a[i] = sc.nextInt(); int l = sc.nextInt(), r = sc.nextInt();\n    System.out.print(rangeSum(a, l, r));\n  }\n}\n','O(n)','O(n)','Store numbers in a 1-indexed array and sum the requested interval.','Keep the input index convention.' UNION ALL
  SELECT '商品库存统计','import java.util.*;\nclass Product { double price; int count; Product(double price, int count) { this.price = price; this.count = count; } double value() { return price * count; } }\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); int n = sc.nextInt(); double total = 0; for (int i = 0; i < n; i++) { sc.next(); Product p = new Product(sc.nextDouble(), sc.nextInt()); total += p.value(); } System.out.printf("%.1f", total); } }\n','O(n)','O(1)','Use a Product object to hold inventory fields and accumulate value.','Model data before calculating.' UNION ALL
  SELECT '员工薪资计算','import java.util.*;\nabstract class Employee { int base, extra; Employee(int base, int extra) { this.base = base; this.extra = extra; } abstract int salary(); }\nclass Developer extends Employee { Developer(int b, int e) { super(b, e); } int salary() { return base + extra; } }\nclass Manager extends Employee { Manager(int b, int e) { super(b, e); } int salary() { return base + extra; } }\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); int n = sc.nextInt(), total = 0; for (int i = 0; i < n; i++) { String type = sc.next(); int b = sc.nextInt(), e = sc.nextInt(); Employee emp = type.equals("manager") ? new Manager(b, e) : new Developer(b, e); total += emp.salary(); } System.out.print(total); } }\n','O(n)','O(1)','Call salary through the parent type to practice polymorphism.','The two roles share a parent abstraction.' UNION ALL
  SELECT '图形面积计算','import java.util.*;\ninterface Shape { double area(); }\nclass Circle implements Shape { double r; Circle(double r) { this.r = r; } public double area() { return 3.14159 * r * r; } }\nclass Rectangle implements Shape { double w, h; Rectangle(double w, double h) { this.w = w; this.h = h; } public double area() { return w * h; } }\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); String type = sc.next(); Shape s = type.equals("circle") ? new Circle(sc.nextDouble()) : new Rectangle(sc.nextDouble(), sc.nextDouble()); System.out.printf("%.2f", s.area()); } }\n','O(1)','O(1)','Use a Shape interface so each shape supplies its own area calculation.','Dispatch on the shape type only once.' UNION ALL
  SELECT '列表去重保持顺序','import java.util.*;\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); int n = sc.nextInt(); LinkedHashSet<Integer> set = new LinkedHashSet<>(); for (int i = 0; i < n; i++) set.add(sc.nextInt()); StringBuilder sb = new StringBuilder(); for (int x : set) { if (sb.length() > 0) sb.append(" "); sb.append(x); } System.out.print(sb); } }\n','O(n)','O(n)','LinkedHashSet removes duplicates while preserving insertion order.','Use an ordered set.' UNION ALL
  SELECT '单词频次查询','import java.util.*;\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); int n = sc.nextInt(); Map<String, Integer> cnt = new HashMap<>(); for (int i = 0; i < n; i++) { String w = sc.next(); cnt.put(w, cnt.getOrDefault(w, 0) + 1); } String q = sc.next(); System.out.print(cnt.getOrDefault(q, 0)); } }\n','O(n)','O(n)','Count words with a map and query with getOrDefault.','Map key is the word.' UNION ALL
  SELECT '学生成绩排序','import java.util.*;\nclass Student { String name; int score; Student(String name, int score) { this.name = name; this.score = score; } }\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); int n = sc.nextInt(); List<Student> list = new ArrayList<>(); for (int i = 0; i < n; i++) list.add(new Student(sc.next(), sc.nextInt())); list.sort((a, b) -> a.score != b.score ? b.score - a.score : a.name.compareTo(b.name)); StringBuilder sb = new StringBuilder(); for (Student s : list) sb.append(s.name).append("\\n"); System.out.print(sb.toString().trim()); } }\n','O(n log n)','O(n)','Sort a List with a comparator that handles score and name.','Comparator order matters.' UNION ALL
  SELECT '名片字段格式化','import java.util.*;\nclass Card { String name, city; int age; Card(String name, int age, String city) { this.name = name; this.age = age; this.city = city; } String format() { return "Name: " + name + ", Age: " + age + ", City: " + city; } }\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); Card card = new Card(sc.next(), sc.nextInt(), sc.next()); System.out.print(card.format()); } }\n','O(1)','O(1)','Encapsulate card fields in a class and expose a format method.','Keep output formatting in one method.' UNION ALL
  SELECT '学生成绩等级统计','import java.util.*;\npublic class Main { static int idx(int s) { if (s >= 90) return 0; if (s >= 80) return 1; if (s >= 70) return 2; if (s >= 60) return 3; return 4; } public static void main(String[] args) { Scanner sc = new Scanner(System.in); int n = sc.nextInt(); int[] c = new int[5]; for (int i = 0; i < n; i++) c[idx(sc.nextInt())]++; System.out.printf("A:%d B:%d C:%d D:%d F:%d", c[0], c[1], c[2], c[3], c[4]); } }\n','O(n)','O(1)','Reuse the grade mapping and count each bucket.','Turn grade into array index.' UNION ALL
  SELECT '二叉树最大深度','import java.util.*;\npublic class Main { static Map<Integer, int[]> tree = new HashMap<>(); static int depth(int node) { if (node == -1) return 0; int[] child = tree.get(node); if (child == null) return 1; return Math.max(depth(child[0]), depth(child[1])) + 1; } public static void main(String[] args) { Scanner sc = new Scanner(System.in); int n = sc.nextInt(); int root = -1; for (int i = 0; i < n; i++) { int node = sc.nextInt(), left = sc.nextInt(), right = sc.nextInt(); if (i == 0) root = node; tree.put(node, new int[]{left, right}); } System.out.print(depth(root)); } }\n','O(n)','O(n)','Store child links and recursively compute max depth.','-1 means empty child.' UNION ALL
  SELECT 'SQL条件语句生成','import java.util.*;\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); String field = sc.next(), op = sc.next(), value = sc.next(); System.out.print("SELECT * FROM student WHERE " + field + " " + op + " " + value + ";"); } }\n','O(1)','O(1)','Concatenate the normalized SQL condition fields.','Preserve spaces and semicolon.' UNION ALL
  SELECT '表字段规范检查','import java.util.*;\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); int n = sc.nextInt(), ok = 0; for (int i = 0; i < n; i++) if (sc.next().matches("[a-z][a-z0-9_]*")) ok++; System.out.print(ok); } }\n','O(nL)','O(1)','Use a regex to validate database field names.','Uppercase and leading digits fail.' UNION ALL
  SELECT 'Spring控制器响应封装','import java.util.*;\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); int code = sc.nextInt(); String message = sc.next(); System.out.printf("{\\\"code\\\":%d,\\\"message\\\":\\\"%s\\\"}", code, message); } }\n','O(1)','O(1)','Build a consistent response body with code and message.','This simulates a controller response DTO.' UNION ALL
  SELECT '服务路由选择','import java.util.*;\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); int n = sc.nextInt(); String best = ""; int bestLoad = Integer.MAX_VALUE; for (int i = 0; i < n; i++) { String name = sc.next(); int load = sc.nextInt(); if (load < bestLoad) { bestLoad = load; best = name; } } System.out.print(best); } }\n','O(n)','O(1)','Pick the service instance with the smallest load.','Track current best candidate.' UNION ALL
  SELECT '服务实例发现','import java.util.*;\nclass Instance { String service, host; int port; Instance(String s, String h, int p) { service = s; host = h; port = p; } }\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); int n = sc.nextInt(); List<Instance> list = new ArrayList<>(); for (int i = 0; i < n; i++) list.add(new Instance(sc.next(), sc.next(), sc.nextInt())); String target = sc.next(); List<String> out = new ArrayList<>(); for (Instance it : list) if (it.service.equals(target)) out.add(it.host + ":" + it.port); System.out.print(String.join(",", out)); } }\n','O(n)','O(n)','Filter registry entries by service name and render addresses.','Service name is the lookup key.' UNION ALL
  SELECT '配置项合并查询','import java.util.*;\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); Map<String, String> cfg = new HashMap<>(); int shared = sc.nextInt(); for (int i = 0; i < shared; i++) cfg.put(sc.next(), sc.next()); int app = sc.nextInt(); for (int i = 0; i < app; i++) cfg.put(sc.next(), sc.next()); String key = sc.next(); System.out.print(cfg.getOrDefault(key, "")); } }\n','O(n)','O(n)','Load shared config first, then override with app config.','Later put wins.' UNION ALL
  SELECT '订单优惠结算','import java.util.*;\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); double price = sc.nextDouble(); int count = sc.nextInt(); double discount = sc.nextDouble(); double amount = Math.max(0, price * count - discount); System.out.printf("%.2f", amount); } }\n','O(1)','O(1)','Compute order amount and never return a negative payment.','Clamp the result at zero.' UNION ALL
  SELECT '服务调用链耗时汇总','import java.util.*;\npublic class Main { public static void main(String[] args) { Scanner sc = new Scanner(System.in); int n = sc.nextInt(), total = 0, max = -1; String maxService = ""; for (int i = 0; i < n; i++) { String name = sc.next(); int cost = sc.nextInt(); total += cost; if (cost > max) { max = cost; maxService = name; } } System.out.print("total=" + total + " max=" + maxService); } }\n','O(n)','O(1)','Accumulate total latency and track the slowest service.','Keep both total and max while scanning.'
) v ON v.`title` = p.`title`
WHERE NOT EXISTS (
  SELECT 1 FROM `reference_solution` rs
  WHERE rs.`problem_id` = p.`id` AND LOWER(rs.`language`) = 'java'
);

UPDATE `path_level`
SET `knowledge_points` = CASE `id`
  WHEN 2 THEN '控制流'
  WHEN 3 THEN '数组,方法'
  WHEN 4 THEN '类和对象'
  WHEN 5 THEN '继承,多态'
  WHEN 6 THEN '接口,抽象类'
  WHEN 7 THEN 'List,Set'
  WHEN 8 THEN 'Set,Map'
  WHEN 9 THEN 'List,集合工具类'
  WHEN 10 THEN '类和对象'
  WHEN 11 THEN '控制流,数组'
  WHEN 31 THEN '二叉树'
  WHEN 42 THEN 'SQL'
  WHEN 43 THEN '数据库设计'
  WHEN 46 THEN 'SQL,数据库设计'
  WHEN 47 THEN 'Spring Boot'
  WHEN 48 THEN 'Spring MVC'
  WHEN 49 THEN '数据库设计'
  WHEN 50 THEN 'Spring Cloud,微服务'
  WHEN 51 THEN '服务注册与发现,微服务'
  WHEN 52 THEN '配置中心,Map'
  WHEN 53 THEN 'Spring Boot,控制流'
  WHEN 54 THEN '微服务'
  ELSE `knowledge_points`
END,
`update_time` = NOW()
WHERE `id` IN (2,3,4,5,6,7,8,9,10,11,31,42,43,46,47,48,49,50,51,52,53,54);

DELETE FROM `path_level_problem`
WHERE `level_id` IN (2,3,4,5,6,7,8,9,10,11,31,42,43,46,47,50,51,52,53,54);

INSERT INTO `path_level_problem` (`path_id`, `chapter_id`, `level_id`, `problem_id`, `order_num`, `create_time`, `update_time`)
SELECT pc.`path_id`, pl.`chapter_id`, pl.`id`, p.`id`, v.`order_num`, NOW(), NOW()
FROM (
  SELECT 2 AS `level_id`, '判断闰年' AS `title`, 1 AS `order_num` UNION ALL
  SELECT 2, '判断奇偶', 2 UNION ALL
  SELECT 2, '成绩等级判断', 3 UNION ALL
  SELECT 3, '数组区间求和', 1 UNION ALL
  SELECT 4, '商品库存统计', 1 UNION ALL
  SELECT 5, '员工薪资计算', 1 UNION ALL
  SELECT 6, '图形面积计算', 1 UNION ALL
  SELECT 7, '列表去重保持顺序', 1 UNION ALL
  SELECT 8, '单词频次查询', 1 UNION ALL
  SELECT 8, '词频统计', 2 UNION ALL
  SELECT 9, '学生成绩排序', 1 UNION ALL
  SELECT 9, '单词频次查询', 2 UNION ALL
  SELECT 10, '名片字段格式化', 1 UNION ALL
  SELECT 11, '学生成绩等级统计', 1 UNION ALL
  SELECT 11, '学生成绩管理系统', 2 UNION ALL
  SELECT 31, '二叉树节点总和', 1 UNION ALL
  SELECT 31, '二叉树最大深度', 2 UNION ALL
  SELECT 42, 'SQL条件语句生成', 1 UNION ALL
  SELECT 43, '表字段规范检查', 1 UNION ALL
  SELECT 46, '简单ORM映射', 1 UNION ALL
  SELECT 46, '学生成绩管理系统', 2 UNION ALL
  SELECT 47, 'Spring控制器响应封装', 1 UNION ALL
  SELECT 50, '服务路由选择', 1 UNION ALL
  SELECT 51, '服务实例发现', 1 UNION ALL
  SELECT 52, '配置项合并查询', 1 UNION ALL
  SELECT 53, '订单优惠结算', 1 UNION ALL
  SELECT 54, '服务调用链耗时汇总', 1
) v
INNER JOIN `path_level` pl ON pl.`id` = v.`level_id`
INNER JOIN `path_chapter` pc ON pc.`id` = pl.`chapter_id`
INNER JOIN `problem` p ON p.`title` = v.`title`
WHERE COALESCE(p.`status`, 'PUBLISHED') = 'PUBLISHED'
  AND NOT EXISTS (
    SELECT 1 FROM `path_level_problem` existing
    WHERE existing.`level_id` = pl.`id` AND existing.`problem_id` = p.`id`
  );

UPDATE `path_level` pl
LEFT JOIN (
  SELECT `level_id`, GROUP_CONCAT(`problem_id` ORDER BY `order_num`) AS `problem_ids`
  FROM `path_level_problem`
  WHERE `level_id` IN (2,3,4,5,6,7,8,9,10,11,31,42,43,46,47,50,51,52,53,54)
  GROUP BY `level_id`
) bound ON bound.`level_id` = pl.`id`
SET pl.`problem_ids` = bound.`problem_ids`,
    pl.`update_time` = NOW()
WHERE pl.`id` IN (2,3,4,5,6,7,8,9,10,11,31,42,43,46,47,50,51,52,53,54);
