-- Selected core problems only: problem 1, 2, 3
-- This script refreshes the shared problem statement and seeds
-- multi-language starter code + reference solutions.

UPDATE `problem`
SET
  `title` = '两数求和',
  `content` = '给定两个整数 a 和 b，输出它们的和。',
  `input` = '一行，包含两个整数 a 和 b，用空格分隔。',
  `output` = '输出一个整数，表示 a + b 的结果。',
  `sample_explanation` = '示例：输入 3 5 时，输出 8。',
  `difficulty` = 0,
  `language` = 'java',
  `time_limit` = 1000,
  `memory_limit` = 262144
WHERE `id` = 1;

UPDATE `problem`
SET
  `title` = '求最大值',
  `content` = '给定两个整数 a 和 b，输出其中较大的那个数。',
  `input` = '一行，包含两个整数 a 和 b，用空格分隔。',
  `output` = '输出一个整数，表示较大的那个数。',
  `sample_explanation` = '示例：输入 3 5 时，输出 5。',
  `difficulty` = 0,
  `language` = 'java',
  `time_limit` = 1000,
  `memory_limit` = 262144
WHERE `id` = 2;

UPDATE `problem`
SET
  `title` = '求绝对值',
  `content` = '给定一个整数 x，输出它的绝对值。',
  `input` = '一行，包含一个整数 x。',
  `output` = '输出一个整数，表示 |x|。',
  `sample_explanation` = '示例：输入 -7 时，输出 7。',
  `difficulty` = 0,
  `language` = 'java',
  `time_limit` = 1000,
  `memory_limit` = 262144
WHERE `id` = 3;

DELETE FROM `problem_supported_language`
WHERE `problem_id` IN (1, 2, 3);

DELETE FROM `reference_solution`
WHERE `problem_id` IN (1, 2, 3);

INSERT INTO `problem_supported_language`
(`problem_id`, `language_code`, `is_default`, `starter_code`, `starter_filename`, `status`, `sort_order`, `create_time`, `update_time`)
SELECT 1, 'java', 1, 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) throws Exception {\n        Scanner sc = new Scanner(System.in);\n        \n    }\n}\n', 'Main.java', 'ACTIVE', 0, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 1, 'python', 0, 'def main():\n    \n\nif __name__ == "__main__":\n    main()\n', 'main.py', 'ACTIVE', 1, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 1, 'cpp', 0, '#include <iostream>\nusing namespace std;\n\nint main() {\n    \n    return 0;\n}\n', 'main.cpp', 'ACTIVE', 2, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 1, 'javascript', 0, 'function main() {\n  \n}\n\nmain();\n', 'main.js', 'ACTIVE', 3, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 1, 'typescript', 0, 'function main(): void {\n  \n}\n\nmain();\n', 'main.ts', 'ACTIVE', 4, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 1, 'go', 0, 'package main\n\nfunc main() {\n\t\n}\n', 'main.go', 'ACTIVE', 5, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 2, 'java', 1, 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) throws Exception {\n        Scanner sc = new Scanner(System.in);\n        \n    }\n}\n', 'Main.java', 'ACTIVE', 0, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 2, 'python', 0, 'def main():\n    \n\nif __name__ == "__main__":\n    main()\n', 'main.py', 'ACTIVE', 1, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 2, 'cpp', 0, '#include <iostream>\nusing namespace std;\n\nint main() {\n    \n    return 0;\n}\n', 'main.cpp', 'ACTIVE', 2, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 2, 'javascript', 0, 'function main() {\n  \n}\n\nmain();\n', 'main.js', 'ACTIVE', 3, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 2, 'typescript', 0, 'function main(): void {\n  \n}\n\nmain();\n', 'main.ts', 'ACTIVE', 4, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 2, 'go', 0, 'package main\n\nfunc main() {\n\t\n}\n', 'main.go', 'ACTIVE', 5, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 3, 'java', 1, 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) throws Exception {\n        Scanner sc = new Scanner(System.in);\n        \n    }\n}\n', 'Main.java', 'ACTIVE', 0, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3)
UNION ALL
SELECT 3, 'python', 0, 'def main():\n    \n\nif __name__ == "__main__":\n    main()\n', 'main.py', 'ACTIVE', 1, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3)
UNION ALL
SELECT 3, 'cpp', 0, '#include <iostream>\nusing namespace std;\n\nint main() {\n    \n    return 0;\n}\n', 'main.cpp', 'ACTIVE', 2, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3)
UNION ALL
SELECT 3, 'javascript', 0, 'function main() {\n  \n}\n\nmain();\n', 'main.js', 'ACTIVE', 3, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3)
UNION ALL
SELECT 3, 'typescript', 0, 'function main(): void {\n  \n}\n\nmain();\n', 'main.ts', 'ACTIVE', 4, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3)
UNION ALL
SELECT 3, 'go', 0, 'package main\n\nfunc main() {\n\t\n}\n', 'main.go', 'ACTIVE', 5, NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3);

INSERT INTO `reference_solution`
(`problem_id`, `language`, `solution_code`, `time_complexity`, `space_complexity`, `explanation`, `hints`, `create_time`, `update_time`)
SELECT 1, 'java', 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        long a = sc.nextLong();\n        long b = sc.nextLong();\n        System.out.println(a + b);\n    }\n}\n', 'O(1)', 'O(1)', '读入两个整数并直接输出它们的和。', '{"1":"先读入两个整数。","2":"使用加法运算求和。","3":"输出 a + b 即可。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 1, 'python', 'def main():\n    a, b = map(int, input().split())\n    print(a + b)\n\nif __name__ == "__main__":\n    main()\n', 'O(1)', 'O(1)', '读取两个整数后直接相加输出。', '{"1":"使用 split 读取两个整数。","2":"把输入转换为 int。","3":"print(a + b)。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 1, 'cpp', '#include <iostream>\nusing namespace std;\n\nint main() {\n    long long a, b;\n    cin >> a >> b;\n    cout << a + b << endl;\n    return 0;\n}\n', 'O(1)', 'O(1)', '使用标准输入读取两个整数并输出它们的和。', '{"1":"用 cin 读取 a 和 b。","2":"答案就是 a + b。","3":"别忘记输出换行。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 1, 'javascript', 'function main() {\n  const fs = require("fs");\n  const data = fs.readFileSync(0, "utf8").trim().split(/\\s+/).map(Number);\n  const [a, b] = data;\n  console.log(a + b);\n}\n\nmain();\n', 'O(1)', 'O(1)', '读取标准输入中的两个数字并输出和。', '{"1":"使用 fs.readFileSync(0, \"utf8\") 读入数据。","2":"按空白切分并转为数字。","3":"输出 a + b。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 1, 'typescript', 'function main(): void {\n  const fs = require("fs");\n  const data: number[] = fs.readFileSync(0, "utf8").trim().split(/\\s+/).map(Number);\n  const [a, b] = data;\n  console.log(a + b);\n}\n\nmain();\n', 'O(1)', 'O(1)', 'TypeScript 方案与 JavaScript 一致，只增加类型标注。', '{"1":"先读取标准输入。","2":"将字符串数组映射为数字数组。","3":"输出 a + b。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 1, 'go', 'package main\n\nimport \"fmt\"\n\nfunc main() {\n\tvar a, b int\n\tfmt.Scan(&a, &b)\n\tfmt.Println(a + b)\n}\n', 'O(1)', 'O(1)', 'Go 中使用 fmt.Scan 读取两个整数，然后输出和。', '{"1":"定义两个整型变量。","2":"用 fmt.Scan 读入。","3":"使用 fmt.Println 输出。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 1)
UNION ALL
SELECT 2, 'java', 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        long a = sc.nextLong();\n        long b = sc.nextLong();\n        System.out.println(Math.max(a, b));\n    }\n}\n', 'O(1)', 'O(1)', '读取两个整数，比较后输出较大值。', '{"1":"需要比较两个整数。","2":"可以使用 Math.max。","3":"输出较大的那个数。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 2, 'python', 'def main():\n    a, b = map(int, input().split())\n    print(max(a, b))\n\nif __name__ == "__main__":\n    main()\n', 'O(1)', 'O(1)', '用 max 函数直接输出两个整数中的较大值。', '{"1":"读取两个整数。","2":"使用 max(a, b)。","3":"输出结果即可。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 2, 'cpp', '#include <iostream>\nusing namespace std;\n\nint main() {\n    long long a, b;\n    cin >> a >> b;\n    cout << (a > b ? a : b) << endl;\n    return 0;\n}\n', 'O(1)', 'O(1)', '通过条件运算符输出两个数中的较大值。', '{"1":"输入 a 和 b。","2":"比较 a > b。","3":"输出较大值。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 2, 'javascript', 'function main() {\n  const fs = require("fs");\n  const data = fs.readFileSync(0, "utf8").trim().split(/\\s+/).map(Number);\n  const [a, b] = data;\n  console.log(Math.max(a, b));\n}\n\nmain();\n', 'O(1)', 'O(1)', '读取两个数字后输出较大值。', '{"1":"先解析标准输入。","2":"Math.max 可以直接比较。","3":"输出结果。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 2, 'typescript', 'function main(): void {\n  const fs = require("fs");\n  const data: number[] = fs.readFileSync(0, "utf8").trim().split(/\\s+/).map(Number);\n  const [a, b] = data;\n  console.log(Math.max(a, b));\n}\n\nmain();\n', 'O(1)', 'O(1)', '与 JavaScript 一样，解析两个数字后输出较大值。', '{"1":"读取并拆分输入。","2":"转成 number 数组。","3":"Math.max(a, b)。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 2, 'go', 'package main\n\nimport \"fmt\"\n\nfunc main() {\n\tvar a, b int\n\tfmt.Scan(&a, &b)\n\tif a > b {\n\t\tfmt.Println(a)\n\t} else {\n\t\tfmt.Println(b)\n\t}\n}\n', 'O(1)', 'O(1)', '读取两个整数后，通过 if 判断输出较大值。', '{"1":"先读入两个整数。","2":"if a > b 输出 a。","3":"否则输出 b。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 2)
UNION ALL
SELECT 3, 'java', 'import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        long x = sc.nextLong();\n        System.out.println(Math.abs(x));\n    }\n}\n', 'O(1)', 'O(1)', '读取一个整数后输出其绝对值。', '{"1":"先读入 x。","2":"绝对值可以使用 Math.abs。","3":"输出结果。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3)
UNION ALL
SELECT 3, 'python', 'def main():\n    x = int(input().strip())\n    print(abs(x))\n\nif __name__ == "__main__":\n    main()\n', 'O(1)', 'O(1)', '读取整数并调用 abs 输出绝对值。', '{"1":"输入只有一个整数。","2":"用 int 转换输入。","3":"print(abs(x))。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3)
UNION ALL
SELECT 3, 'cpp', '#include <iostream>\n#include <cstdlib>\nusing namespace std;\n\nint main() {\n    long long x;\n    cin >> x;\n    cout << llabs(x) << endl;\n    return 0;\n}\n', 'O(1)', 'O(1)', '读取一个整数并输出它的绝对值。', '{"1":"先读取 x。","2":"可以使用 llabs。","3":"输出绝对值。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3)
UNION ALL
SELECT 3, 'javascript', 'function main() {\n  const fs = require("fs");\n  const x = Number(fs.readFileSync(0, "utf8").trim());\n  console.log(Math.abs(x));\n}\n\nmain();\n', 'O(1)', 'O(1)', 'JavaScript 可直接使用 Math.abs。', '{"1":"从标准输入读取一个数字。","2":"调用 Math.abs。","3":"输出结果。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3)
UNION ALL
SELECT 3, 'typescript', 'function main(): void {\n  const fs = require("fs");\n  const x: number = Number(fs.readFileSync(0, "utf8").trim());\n  console.log(Math.abs(x));\n}\n\nmain();\n', 'O(1)', 'O(1)', 'TypeScript 方案同样直接调用 Math.abs。', '{"1":"先读取数字。","2":"保持 number 类型。","3":"输出绝对值。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3)
UNION ALL
SELECT 3, 'go', 'package main\n\nimport \"fmt\"\n\nfunc main() {\n\tvar x int\n\tfmt.Scan(&x)\n\tif x < 0 {\n\t\tx = -x\n\t}\n\tfmt.Println(x)\n}\n', 'O(1)', 'O(1)', 'Go 中判断 x 是否小于 0，若是则取相反数。', '{"1":"先定义并读取 x。","2":"如果 x < 0，则 x = -x。","3":"最后输出 x。"}', NOW(), NOW() FROM DUAL WHERE EXISTS (SELECT 1 FROM `problem` WHERE `id` = 3);
