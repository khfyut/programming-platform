-- 2026-03-27 problem detail and reference solution patch
SET NAMES utf8mb4;

CREATE TEMPORARY TABLE tmp_problem_detail_seed (
  problem_title VARCHAR(100) PRIMARY KEY,
  content TEXT NOT NULL,
  input_desc TEXT,
  output_desc TEXT,
  sample_explanation TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO tmp_problem_detail_seed (problem_title, content, input_desc, output_desc, sample_explanation) VALUES
('两数之和', '给定一行形如 nums = [2,7,11,15], target = 9 的输入。请返回两个下标，使得对应元素之和等于 target。题目保证每组输入都存在唯一解，输出格式为 [i,j]。', '一行字符串，包含数组 nums 和目标值 target。', '满足条件的两个下标，格式为 [i,j]。', '样例中 nums[0] + nums[1] = 9，因此输出 [0,1]。'),
('求最大值', '输入一行形如 a = 5, b = 3 的内容，输出两个整数中的较大值。', '一行字符串，包含两个整数 a 和 b。', '较大的那个整数。', '样例中 5 大于 3，因此输出 5。'),
('求绝对值', '输入一行形如 n = -5 的内容，输出该整数的绝对值。', '一行字符串，包含一个整数 n。', 'n 的绝对值。', '样例中 |-5| = 5。'),
('判断闰年', '输入一行形如 year = 2020 的内容，判断该年份是否为闰年。满足能被 400 整除，或能被 4 整除但不能被 100 整除时输出 true，否则输出 false。', '一行字符串，包含年份 year。', '若为闰年输出 true，否则输出 false。', '2020 能被 4 整除且不能被 100 整除，因此输出 true。'),
('判断奇偶', '输入一行形如 n = 7 的内容。如果 n 为奇数输出 true，否则输出 false。', '一行字符串，包含整数 n。', '若 n 为奇数输出 true，否则输出 false。', '7 是奇数，因此输出 true。'),
('冒泡排序', '输入一行形如 arr = [5,3,8,4,2] 的数组，按升序排序后输出，格式保持为 [2,3,4,5,8]。', '一行字符串，包含数组 arr。', '按升序排列后的数组，格式与输入一致。', '样例排序后得到 [2,3,4,5,8]。'),
('二分查找', '输入一行形如 nums = [-1,0,3,5,9,12], target = 9 的内容。在有序数组中查找 target，找到则输出下标，否则输出 -1。', '一行字符串，包含有序数组 nums 和目标值 target。', 'target 的下标；若不存在输出 -1。', '样例中 9 位于下标 4。'),
('链表反转', '输入一行由空格分隔的整数序列，视为单链表从头到尾的节点值。请输出反转后的结果，节点之间仍使用空格分隔。若输入为空，输出 EMPTY。', '一行字符串，包含若干个由空格分隔的整数。', '反转后的整数序列；若为空输出 EMPTY。', '输入 1 2 3 4 5 时，反转后输出 5 4 3 2 1。'),
('斐波那契数列', '输入一个整数 n，输出第 n 个斐波那契数。规定 F(1)=1，F(2)=1。', '一个整数 n。', '第 n 个斐波那契数。', '当 n=10 时，斐波那契数列第 10 项为 55。'),
('HTTP请求', '输入一行标准 HTTP 请求行，例如 GET /api/users?id=1 HTTP/1.1。请提取请求方法和路径，并输出 method + 空格 + path。', '一行 HTTP 请求行。', '提取后的方法与路径，例如 GET /api/users?id=1。', '样例中方法是 GET，路径是 /api/users?id=1。'),
('SQL查询', '第一行输入 n 和最低分 threshold，接下来 n 行每行包含一个姓名和一个分数。请输出分数大于等于 threshold 的学生姓名，按输入顺序以空格分隔；若无人满足则输出 EMPTY。', '第一行是 n 和 threshold；接下来 n 行是 name score。', '满足分数条件的姓名列表，按输入顺序输出；若为空输出 EMPTY。', '样例中 Alice 和 Carol 的分数不低于阈值，因此输出 Alice Carol。'),
('数组最大值', '第一行输入数组长度 n，第二行输入 n 个整数，输出其中的最大值。', '第一行一个整数 n，第二行 n 个整数。', '数组中的最大值。', '样例数组中的最大值是 9。'),
('有效括号', '输入一个只包含 ()[]{} 的字符串。若括号可以完全正确匹配则输出 valid，否则输出 invalid。', '一行括号字符串。', '匹配成功输出 valid，否则输出 invalid。', '样例 ([]{}) 中所有括号都能正确配对。'),
('队列模拟', '第一行输入操作数 n，接下来 n 行每行是一条操作：push x、pop、front。每次遇到 pop 或 front 时输出结果；若队列为空则输出 EMPTY。', '第一行是整数 n，接下来 n 行是队列操作。', '按顺序输出所有 pop 和 front 的结果，每个结果占一行。', '样例中先查看到 1，再依次弹出 1、2，最后队列为空。'),
('词频统计', '第一行输入单词数量 n，第二行输入 n 个单词。请输出出现次数最多的单词和次数；若次数相同，输出字典序更小的单词。', '第一行一个整数 n，第二行 n 个由空格分隔的单词。', '输出“单词 次数”。', 'apple 出现 3 次，为最高频单词。'),
('二叉树节点总和', '输入一行用逗号分隔的层序遍历结果，null 表示空节点。请计算所有非空节点值的总和。', '一行字符串，如 1,2,3,null,4。', '所有非空节点值之和。', '样例中 1 + 2 + 3 + 4 = 10。'),
('图的最短路径步数', '第一行输入 n 和 m，接下来 m 行是无向边 u v，最后一行输入起点 s 和终点 t。输出从 s 到 t 的最短边数；不可达时输出 -1。', '第一行 n m；接下来 m 行每行两个整数表示一条无向边；最后一行 s t。', '最短路径经过的边数；若不可达输出 -1。', '样例中 1 到 4 的最短路径长度为 2。'),
('最长递增子序列', '第一行输入数组长度 n，第二行输入 n 个整数，输出最长严格递增子序列的长度。', '第一行一个整数 n，第二行 n 个整数。', '最长严格递增子序列长度。', '样例中最长递增子序列长度为 4。'),
('区间调度', '第一行输入区间数量 n，接下来 n 行每行给出一个闭区间 [l, r]。请选出尽可能多的互不重叠区间，输出最多可以选择的数量。端点相接视为不重叠。', '第一行 n，接下来 n 行每行两个整数 l 和 r。', '最多可选择的区间数量。', '样例可选 [1,3]、[3,5]、[6,7]，共 3 个区间。'),
('全排列数量', '输入一个整数 n，输出 1 到 n 的全排列数量。', '一个整数 n。', '全排列数量。', '全排列数量等于 n 的阶乘；4! = 24。'),
('第K大元素', '第一行输入数组长度 n 和整数 k，第二行输入 n 个整数，输出数组中的第 k 大元素。', '第一行 n 和 k，第二行 n 个整数。', '数组中的第 k 大元素。', '样例中第 2 大元素是 5。'),
('HTTP状态码分类', '输入一个 HTTP 状态码，输出其所属类别：INFORMATIONAL、SUCCESS、REDIRECT、CLIENT_ERROR、SERVER_ERROR 或 UNKNOWN。', '一个整数状态码。', '状态码所属类别。', '404 属于 4xx，因此输出 CLIENT_ERROR。'),
('IP地址分类', '输入一个 IPv4 地址，按首段范围输出 A、B、C、D、E；若地址非法则输出 INVALID。', '一行 IPv4 地址。', '地址类别或 INVALID。', '192.168.1.10 的首段为 192，属于 C 类地址。'),
('RESTful路径拆解', '输入一条 RESTful 路径，按顺序输出其中所有非空路径段，段之间用空格分隔。', '一行路径字符串，如 /users/42/orders/7。', '所有非空路径段，使用空格连接。', '样例的非空路径段依次为 users、42、orders、7。'),
('查询参数解析', '输入形如 key=value&key2=value2 的查询串，按 key 的字典序输出 key=value，项之间使用英文逗号连接。', '一行查询字符串。', '按 key 排序后的查询项。', 'page=2,size=10,sort=desc 已经按键名字典序排列。'),
('学生成绩分组统计', '第一行输入学生数量 n，第二行输入 n 个整数成绩。输出平均分和最高分，格式为 avg max，其中平均分保留两位小数。', '第一行 n，第二行 n 个整数成绩。', '平均分和最高分，以空格分隔。', '样例平均分为 85.00，最高分为 100。'),
('事务日志汇总', '第一行输入日志条数 n，接下来 n 行每行为 IN x 或 OUT x。请输出最终余额变化值。', '第一行整数 n，后续每行是一条收入或支出日志。', '最终余额变化值。', '收入记为加，支出记为减，样例结果为 85。'),
('ORM字段映射检查', '输入一个下划线命名的字段名，输出对应的驼峰命名结果。', '一行下划线命名字段名。', '对应的驼峰命名结果。', 'user_name 转换后为 userName。'),
('Flask路由匹配', '第一行输入 Flask 风格路由模板，第二行输入访问路径。模板中的 <id> 视为单段占位符。若路径可以匹配模板则输出 MATCH，否则输出 404。', '两行字符串，分别为路由模板和访问路径。', '匹配成功输出 MATCH，否则输出 404。', '模板 /users/<id> 可以匹配 /users/42，因此输出 MATCH。'),
('FastAPI参数校验', '输入用户名和年龄。用户名非空且年龄在 1 到 120 之间输出 valid，否则输出 invalid。', '一行包含用户名和年龄。', '若参数合法输出 valid，否则输出 invalid。', 'alice 非空且年龄 23 合法，因此输出 valid。');

UPDATE problem p
JOIN tmp_problem_detail_seed s
  ON s.problem_title COLLATE utf8mb4_unicode_ci = p.title COLLATE utf8mb4_unicode_ci
SET
  p.content = s.content,
  p.input = s.input_desc,
  p.output = s.output_desc,
  p.sample_explanation = s.sample_explanation,
  p.status = 'PUBLISHED',
  p.supported_languages = 'Java,Python',
  p.update_time = NOW();

CREATE TEMPORARY TABLE tmp_test_case_seed (
  problem_title VARCHAR(100) NOT NULL,
  case_input TEXT NOT NULL,
  case_output TEXT NOT NULL,
  is_sample TINYINT(1) NOT NULL,
  sort_order INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO tmp_test_case_seed (problem_title, case_input, case_output, is_sample, sort_order) VALUES
('链表反转', '1 2 3 4 5', '5 4 3 2 1', 1, 1),
('链表反转', '7', '7', 0, 2),
('链表反转', '', 'EMPTY', 0, 3),
('斐波那契数列', '10', '55', 1, 1),
('斐波那契数列', '1', '1', 0, 2),
('斐波那契数列', '2', '1', 0, 3),
('HTTP请求', 'GET /api/users?id=1 HTTP/1.1', 'GET /api/users?id=1', 1, 1),
('HTTP请求', 'POST /login HTTP/1.1', 'POST /login', 0, 2),
('SQL查询', '3 80
Alice 90
Bob 70
Carol 85', 'Alice Carol', 1, 1),
('SQL查询', '2 90
Tom 88
Jerry 95', 'Jerry', 0, 2),
('SQL查询', '2 96
Tom 88
Jerry 95', 'EMPTY', 0, 3);

INSERT INTO test_case (
  problem_id, input, output, is_sample, sort_order, create_time, update_time
)
SELECT
  p.id,
  s.case_input,
  s.case_output,
  s.is_sample,
  s.sort_order,
  NOW(),
  NOW()
FROM tmp_test_case_seed s
JOIN problem p
  ON p.title COLLATE utf8mb4_unicode_ci = s.problem_title COLLATE utf8mb4_unicode_ci
LEFT JOIN test_case t
  ON t.problem_id = p.id
 AND t.input = s.case_input
 AND t.output = s.case_output
WHERE t.id IS NULL;

CREATE TEMPORARY TABLE tmp_reference_solution_seed (
  problem_title VARCHAR(100) NOT NULL,
  language VARCHAR(20) NOT NULL,
  solution_code LONGTEXT NOT NULL,
  time_complexity VARCHAR(50) NOT NULL,
  space_complexity VARCHAR(50) NOT NULL,
  explanation TEXT NOT NULL,
  hints JSON
) DEFAULT CHARSET=utf8mb4;

INSERT INTO tmp_reference_solution_seed (
  problem_title, language, solution_code, time_complexity, space_complexity, explanation, hints
) VALUES
('两数之和', 'Java', 'import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next() : "";
        Matcher matcher = Pattern.compile("-?\\\\d+").matcher(input);
        List<Integer> nums = new ArrayList<>();
        while (matcher.find()) {
            nums.add(Integer.parseInt(matcher.group()));
        }
        if (nums.size() < 2) {
            System.out.print("[-1,-1]");
            return;
        }
        int target = nums.remove(nums.size() - 1);
        Map<Integer, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < nums.size(); i++) {
            int current = nums.get(i);
            int need = target - current;
            if (indexMap.containsKey(need)) {
                System.out.print("[" + indexMap.get(need) + "," + i + "]");
                return;
            }
            indexMap.putIfAbsent(current, i);
        }
        System.out.print("[-1,-1]");
    }
}', 'O(n)', 'O(n)', '先把输入中的整数全部提取出来，最后一个整数作为 target，其余整数作为数组。使用哈希表记录已经遍历过的数字位置，边扫描边判断 target - 当前值 是否已经出现过，即可在线找到答案。', JSON_ARRAY('最后一个整数是目标值 target。', '哈希表存储已经扫描过的数字和下标。', '输出格式需要和题目样例一致，例如 [0,1]。')),
('求最大值', 'Java', 'import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next() : "";
        Matcher matcher = Pattern.compile("-?\\\\d+").matcher(input);
        int ans = Integer.MIN_VALUE;
        while (matcher.find()) {
            ans = Math.max(ans, Integer.parseInt(matcher.group()));
        }
        System.out.print(ans);
    }
}', 'O(1)', 'O(1)', '题目本质是从输入中取出两个整数并输出较大者。直接扫描所有整数并维护当前最大值即可。', JSON_ARRAY('题目里的 a = 5, b = 3 只是展示格式。', '提取出整数后比较大小即可。', '注意负数场景同样成立。')),
('求绝对值', 'Java', 'import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next() : "";
        Matcher matcher = Pattern.compile("-?\\\\d+").matcher(input);
        int value = matcher.find() ? Integer.parseInt(matcher.group()) : 0;
        System.out.print(Math.abs(value));
    }
}', 'O(1)', 'O(1)', '从输入中提取唯一的整数，然后调用 Math.abs 计算绝对值即可。', JSON_ARRAY('先从 n = -5 这种格式中拿到数字。', '绝对值永远不小于 0。', '0 的绝对值仍然是 0。')),
('判断闰年', 'Java', 'import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next() : "";
        Matcher matcher = Pattern.compile("-?\\\\d+").matcher(input);
        int year = matcher.find() ? Integer.parseInt(matcher.group()) : 0;
        boolean leap = (year % 400 == 0) || (year % 4 == 0 && year % 100 != 0);
        System.out.print(leap ? "true" : "false");
    }
}', 'O(1)', 'O(1)', '闰年的判断规则是：能被 400 整除，或者能被 4 整除但不能被 100 整除。根据规则返回 true 或 false。', JSON_ARRAY('2000 是闰年，因为它能被 400 整除。', '1900 不是闰年，因为它虽然能被 100 整除，但不能被 400 整除。', '输出是小写 true / false。')),
('判断奇偶', 'Java', 'import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next() : "";
        Matcher matcher = Pattern.compile("-?\\\\d+").matcher(input);
        int n = matcher.find() ? Integer.parseInt(matcher.group()) : 0;
        System.out.print(n % 2 != 0 ? "true" : "false");
    }
}', 'O(1)', 'O(1)', '根据样例可知，本题要求在输入为奇数时输出 true，为偶数时输出 false。直接使用 n % 2 判断即可。', JSON_ARRAY('7 的输出是 true，说明题目判断的是奇数。', '8 和 0 都是偶数，所以输出 false。', '输出是布尔字符串，不是 odd / even。')),
('冒泡排序', 'Java', 'import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next() : "";
        Matcher matcher = Pattern.compile("-?\\\\d+").matcher(input);
        List<Integer> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(Integer.parseInt(matcher.group()));
        }
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j + 1 < arr.length - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    int tmp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = tmp;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(arr[i]);
        }
        sb.append("]");
        System.out.print(sb);
    }
}', 'O(n^2)', 'O(1)', '先把 arr = [5,3,8,4,2] 这种格式中的整数提取出来，再按冒泡排序的规则两两交换相邻逆序元素。排序结束后按 [a,b,c] 的格式输出。', JSON_ARRAY('提取出的所有整数都属于数组元素。', '冒泡排序会把当前轮最大的数冒到末尾。', '输出要保留方括号和逗号。')),
('二分查找', 'Java', 'import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next() : "";
        Matcher matcher = Pattern.compile("-?\\\\d+").matcher(input);
        List<Integer> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(Integer.parseInt(matcher.group()));
        }
        if (list.size() < 2) {
            System.out.print(-1);
            return;
        }
        int target = list.remove(list.size() - 1);
        int left = 0;
        int right = list.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            int value = list.get(mid);
            if (value == target) {
                System.out.print(mid);
                return;
            } else if (value < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        System.out.print(-1);
    }
}', 'O(log n)', 'O(1)', '从输入中提取出有序数组和 target，使用经典二分查找。每次比较中间值与目标值，根据大小关系缩小搜索区间。', JSON_ARRAY('最后一个整数是 target。', '数组已经有序，适合使用二分查找。', '如果找不到，输出 -1。')),
('链表反转', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next().trim() : "";
        if (input.isEmpty()) {
            System.out.print("EMPTY");
            return;
        }
        String[] parts = input.split("\\\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i].isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(parts[i]);
        }
        System.out.print(sb.length() == 0 ? "EMPTY" : sb.toString());
    }
}', 'O(n)', 'O(1)', '在线判题的输入是用空格分隔的节点序列，所以可以把它看作链表的顺序表示。把序列逆序输出，就等价于完成链表反转。', JSON_ARRAY('空输入时输出 EMPTY。', '输入中的每个数字都代表一个链表节点。', '逆序输出时元素之间使用单个空格连接。')),
('斐波那契数列', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        if (n <= 0) {
            System.out.print(0);
            return;
        }
        if (n <= 2) {
            System.out.print(1);
            return;
        }
        long a = 1;
        long b = 1;
        for (int i = 3; i <= n; i++) {
            long c = a + b;
            a = b;
            b = c;
        }
        System.out.print(b);
    }
}', 'O(n)', 'O(1)', '使用迭代方式计算第 n 项。前两项都是 1，从第 3 项开始每一项等于前两项之和。', JSON_ARRAY('n = 1 和 n = 2 时答案都是 1。', '用两个变量滚动保存前两项即可。', '样例 n = 10 时答案是 55。')),
('HTTP请求', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next().trim() : "";
        if (input.isEmpty()) {
            return;
        }
        String[] parts = input.split("\\\\s+");
        if (parts.length >= 2) {
            System.out.print(parts[0] + " " + parts[1]);
        } else {
            System.out.print(input);
        }
    }
}', 'O(n)', 'O(n)', '题目当前的评测输入是一行 HTTP 请求行，例如 GET /api/users?id=1 HTTP/1.1。取前两个字段，分别作为方法和路径输出即可。', JSON_ARRAY('请求行一般由 方法、路径、协议版本 三部分组成。', '本题只需要前两个字段。', '保留方法和路径之间的单个空格。')),
('SQL查询', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        int threshold = sc.hasNextInt() ? sc.nextInt() : 0;
        List<String> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String name = sc.hasNext() ? sc.next() : "";
            int score = sc.hasNextInt() ? sc.nextInt() : 0;
            if (score >= threshold) {
                result.add(name);
            }
        }
        System.out.print(result.isEmpty() ? "EMPTY" : String.join(" ", result));
    }
}', 'O(n)', 'O(n)', '虽然题目名叫 SQL 查询，但当前评测数据是结构化文本输入。首行给出学生数量和分数阈值，后续每行是 姓名 分数，筛出分数不低于阈值的学生并按输入顺序输出。', JSON_ARRAY('第一行包含 n 和 threshold。', '后续每行是一名学生的姓名和分数。', '满足 score >= threshold 的姓名按原顺序输出。')),
('数组最大值', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        int ans = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            ans = Math.max(ans, sc.nextInt());
        }
        System.out.print(ans);
    }
}', 'O(n)', 'O(1)', '读入 n 个整数后，线性扫描并维护当前最大值即可。', JSON_ARRAY('初始化最大值为一个足够小的数。', '每读到一个新数就和当前最大值比较。', '遍历结束后的最大值就是答案。')),
('有效括号', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String s = sc.hasNext() ? sc.next().trim() : "";
        Map<String, String> pairs = new HashMap<>();
        pairs.put(")", "(");
        pairs.put("]", "[");
        pairs.put("}", "{");
        Deque<String> stack = new ArrayDeque<>();
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            if (pairs.containsValue(ch)) {
                stack.push(ch);
            } else {
                if (stack.isEmpty() || !stack.peek().equals(pairs.get(ch))) {
                    System.out.print("invalid");
                    return;
                }
                stack.pop();
            }
        }
        System.out.print(stack.isEmpty() ? "valid" : "invalid");
    }
}', 'O(n)', 'O(n)', '用栈维护尚未匹配的左括号。遇到左括号入栈，遇到右括号时检查是否和栈顶左括号匹配，最后栈为空则说明完全匹配。', JSON_ARRAY('左括号入栈，右括号尝试和栈顶匹配。', '如果栈空或括号类型不匹配，立即判 invalid。', '遍历结束后栈必须为空。')),
('队列模拟', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        sc.nextLine();
        Deque<String> queue = new ArrayDeque<>();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < n; i++) {
            String line = sc.hasNextLine() ? sc.nextLine().trim() : "";
            if (line.startsWith("push ")) {
                queue.offer(line.substring(5));
            } else if (line.equals("pop")) {
                String value = queue.isEmpty() ? "EMPTY" : queue.poll();
                if (out.length() > 0) out.append("\\n");
                out.append(value);
            } else if (line.equals("front")) {
                String value = queue.isEmpty() ? "EMPTY" : queue.peek();
                if (out.length() > 0) out.append("\\n");
                out.append(value);
            }
        }
        System.out.print(out);
    }
}', 'O(n)', 'O(n)', '使用双端队列模拟普通队列。push 把元素放到队尾，front 查看队首，pop 删除并输出队首。队列为空时按题意输出 EMPTY。', JSON_ARRAY('队列遵循先进先出。', 'front 只查看不删除元素。', 'pop 和 front 都可能产生输出。')),
('词频统计', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        Map<String, Integer> freq = new HashMap<>();
        String bestWord = "";
        int bestCount = -1;
        for (int i = 0; i < n; i++) {
            String word = sc.next();
            int count = freq.getOrDefault(word, 0) + 1;
            freq.put(word, count);
            if (count > bestCount || (count == bestCount && word.compareTo(bestWord) < 0)) {
                bestCount = count;
                bestWord = word;
            }
        }
        System.out.print(bestWord + " " + bestCount);
    }
}', 'O(n)', 'O(n)', '使用哈希表统计每个单词出现次数，同时维护当前最高频单词。若出现次数相同，就比较字典序，保留更小的那个单词。', JSON_ARRAY('先统计次数，再处理并列情况。', '并列时选择字典序最小的单词。', '输出格式为 单词 次数。')),
('二叉树节点总和', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next().trim() : "";
        if (input.isEmpty()) {
            System.out.print(0);
            return;
        }
        String[] parts = input.split(",");
        long sum = 0;
        for (String part : parts) {
            String value = part.trim();
            if (!value.equalsIgnoreCase("null") && !value.isEmpty()) {
                sum += Long.parseLong(value);
            }
        }
        System.out.print(sum);
    }
}', 'O(n)', 'O(1)', '输入已经是二叉树的层序数组表示，本题不要求真正建树。只要遍历每个节点值，跳过 null，把所有非空节点求和即可。', JSON_ARRAY('输入中的 null 表示空节点。', '不需要真正构造树结构。', '直接累计所有非 null 数值。')),
('图的最短路径步数', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        int m = sc.hasNextInt() ? sc.nextInt() : 0;
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            graph.add(new ArrayList<>());
        }
        for (int i = 0; i < m; i++) {
            int u = sc.nextInt();
            int v = sc.nextInt();
            graph.get(u).add(v);
            graph.get(v).add(u);
        }
        int s = sc.nextInt();
        int t = sc.nextInt();
        int[] dist = new int[n + 1];
        Arrays.fill(dist, -1);
        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(s);
        dist[s] = 0;
        while (!queue.isEmpty()) {
            int cur = queue.poll();
            if (cur == t) {
                break;
            }
            for (int next : graph.get(cur)) {
                if (dist[next] == -1) {
                    dist[next] = dist[cur] + 1;
                    queue.offer(next);
                }
            }
        }
        System.out.print(dist[t]);
    }
}', 'O(n+m)', 'O(n+m)', '无权图最短路使用 BFS。起点先入队，每次向外扩展一层，第一次到达某个节点时记录的距离就是最短边数。', JSON_ARRAY('无权图最短路优先考虑 BFS。', 'dist 数组初始化为 -1 表示未访问。', '终点不可达时答案保持为 -1。')),
('最长递增子序列', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }
        if (n == 0) {
            System.out.print(0);
            return;
        }
        int[] dp = new int[n];
        Arrays.fill(dp, 1);
        int ans = 1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            ans = Math.max(ans, dp[i]);
        }
        System.out.print(ans);
    }
}', 'O(n^2)', 'O(n)', '定义 dp[i] 为以 nums[i] 结尾的最长严格递增子序列长度。枚举前面的 j，如果 nums[j] < nums[i]，就尝试用 dp[j] 更新 dp[i]。', JSON_ARRAY('dp[i] 至少是 1。', '只有前一个数更小时才能接在后面。', '答案是所有 dp[i] 的最大值。')),
('区间调度', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        int[][] intervals = new int[n][2];
        for (int i = 0; i < n; i++) {
            intervals[i][0] = sc.nextInt();
            intervals[i][1] = sc.nextInt();
        }
        Arrays.sort(intervals, (a, b) -> a[1] == b[1] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
        int ans = 0;
        int end = Integer.MIN_VALUE;
        for (int[] interval : intervals) {
            if (interval[0] >= end) {
                ans++;
                end = interval[1];
            }
        }
        System.out.print(ans);
    }
}', 'O(n log n)', 'O(1)', '经典区间调度问题使用贪心：优先选择结束时间最早的区间。这样能尽量为后续区间留下空间，从而得到最大可选数量。', JSON_ARRAY('先按右端点从小到大排序。', '端点相接不算重叠，所以可以选。', '每次选中区间后更新当前结束位置。')),
('全排列数量', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        long ans = 1;
        for (int i = 2; i <= n; i++) {
            ans *= i;
        }
        System.out.print(ans);
    }
}', 'O(n)', 'O(1)', '1 到 n 的全排列数量等于 n 的阶乘，因此顺序连乘 1 到 n 即可。', JSON_ARRAY('全排列数量本质上是 n!。', 'n = 4 时答案是 24。', '从 2 开始累乘即可。')),
('第K大元素', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        int k = sc.hasNextInt() ? sc.nextInt() : 0;
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        for (int i = 0; i < n; i++) {
            heap.offer(sc.nextInt());
            if (heap.size() > k) {
                heap.poll();
            }
        }
        System.out.print(heap.peek());
    }
}', 'O(n log k)', 'O(k)', '维护一个大小为 k 的最小堆。遍历数组时把元素放入堆中，如果堆大小超过 k，就弹出最小值。最终堆顶就是第 k 大元素。', JSON_ARRAY('最小堆里始终保留当前最大的 k 个数。', '当堆大小超过 k 时删掉最小值。', '最后堆顶就是第 k 大。')),
('HTTP状态码分类', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int code = sc.hasNextInt() ? sc.nextInt() : -1;
        int prefix = code / 100;
        String ans;
        switch (prefix) {
            case 1: ans = "INFORMATIONAL"; break;
            case 2: ans = "SUCCESS"; break;
            case 3: ans = "REDIRECT"; break;
            case 4: ans = "CLIENT_ERROR"; break;
            case 5: ans = "SERVER_ERROR"; break;
            default: ans = "UNKNOWN";
        }
        System.out.print(ans);
    }
}', 'O(1)', 'O(1)', 'HTTP 状态码通常按百位数分类。取状态码的 code / 100，再映射到对应类别即可。', JSON_ARRAY('2xx 对应 SUCCESS。', '4xx 对应 CLIENT_ERROR。', '不在 1xx 到 5xx 范围内时输出 UNKNOWN。')),
('IP地址分类', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next().trim() : "";
        String[] parts = input.split("\\\\.");
        if (parts.length != 4) {
            System.out.print("INVALID");
            return;
        }
        int[] nums = new int[4];
        for (int i = 0; i < 4; i++) {
            if (parts[i].isEmpty() || !parts[i].matches("\\\\d+")) {
                System.out.print("INVALID");
                return;
            }
            nums[i] = Integer.parseInt(parts[i]);
            if (nums[i] < 0 || nums[i] > 255) {
                System.out.print("INVALID");
                return;
            }
        }
        int first = nums[0];
        if (first >= 1 && first <= 126) System.out.print("A");
        else if (first >= 128 && first <= 191) System.out.print("B");
        else if (first >= 192 && first <= 223) System.out.print("C");
        else if (first >= 224 && first <= 239) System.out.print("D");
        else if (first >= 240 && first <= 255) System.out.print("E");
        else System.out.print("INVALID");
    }
}', 'O(1)', 'O(1)', '先校验 IPv4 地址格式是否为 4 段且每段在 0 到 255 之间。然后根据首段范围判断地址类别。', JSON_ARRAY('必须先过格式校验。', '192 到 223 属于 C 类地址。', '超出 0 到 255 的段直接判 INVALID。')),
('RESTful路径拆解', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String path = sc.hasNext() ? sc.next().trim() : "";
        String[] parts = path.split("/");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.add(part);
            }
        }
        System.out.print(String.join(" ", result));
    }
}', 'O(n)', 'O(n)', 'RESTful 路径的关键是按 / 分段，再过滤掉首尾或重复斜杠产生的空字符串，最后按空格拼接输出。', JSON_ARRAY('按 / split 之后会出现空字符串。', '只保留非空路径段。', '输出路径段之间用空格连接。')),
('查询参数解析', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next().trim() : "";
        if (input.isEmpty()) {
            return;
        }
        List<String[]> items = new ArrayList<>();
        for (String pair : input.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = kv.length > 0 ? kv[0] : "";
            String value = kv.length > 1 ? kv[1] : "";
            items.add(new String[]{key, value});
        }
        items.sort((a, b) -> a[0].compareTo(b[0]));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(items.get(i)[0]).append("=").append(items.get(i)[1]);
        }
        System.out.print(sb);
    }
}', 'O(n log n)', 'O(n)', '先按 & 拆出多个 key=value，再按 key 的字典序排序，最后重新拼接成逗号分隔的结果。', JSON_ARRAY('每个参数都是 key=value 形式。', '排序依据是 key，不是整个字符串。', '输出项之间使用英文逗号连接。')),
('学生成绩分组统计', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        int sum = 0;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            int score = sc.nextInt();
            sum += score;
            max = Math.max(max, score);
        }
        double avg = n == 0 ? 0.0 : (sum * 1.0 / n);
        System.out.print(String.format(Locale.US, "%.2f %d", avg, max));
    }
}', 'O(n)', 'O(1)', '扫描所有成绩时同时维护总分和最高分，最后计算平均分并保留两位小数。', JSON_ARRAY('总分用于计算平均值。', '最高分可以在遍历时同步维护。', '格式化输出时要固定保留两位小数。')),
('事务日志汇总', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        int balance = 0;
        for (int i = 0; i < n; i++) {
            String op = sc.next();
            int amount = sc.nextInt();
            if ("IN".equals(op)) {
                balance += amount;
            } else {
                balance -= amount;
            }
        }
        System.out.print(balance);
    }
}', 'O(n)', 'O(1)', '把 IN 视为收入、OUT 视为支出。逐条累加或累减，最终余额变化值就是答案。', JSON_ARRAY('IN 对余额做加法。', 'OUT 对余额做减法。', '从 0 开始累计即可。')),
('ORM字段映射检查', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next().trim() : "";
        String[] parts = input.split("_");
        if (parts.length == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            sb.append(parts[i].substring(0, 1).toUpperCase());
            if (parts[i].length() > 1) {
                sb.append(parts[i].substring(1));
            }
        }
        System.out.print(sb);
    }
}', 'O(n)', 'O(n)', '将下划线命名拆分成多个单词，首个单词保持不变，后续单词首字母大写后再拼接，就得到驼峰命名。', JSON_ARRAY('第一个单词保持小写。', '下划线后的单词首字母改成大写。', 'created_at_time 会变成 createdAtTime。')),
('Flask路由匹配', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in).useDelimiter("\\\\A");
        String input = sc.hasNext() ? sc.next() : "";
        String[] lines = input.split("\\\\R");
        if (lines.length < 2) {
            System.out.print("404");
            return;
        }
        String[] pattern = split(lines[0].trim());
        String[] path = split(lines[1].trim());
        if (pattern.length != path.length) {
            System.out.print("404");
            return;
        }
        for (int i = 0; i < pattern.length; i++) {
            String p = pattern[i];
            String real = path[i];
            boolean wildcard = p.startsWith("<") && p.endsWith(">");
            if (!wildcard && !p.equals(real)) {
                System.out.print("404");
                return;
            }
            if (wildcard && real.isEmpty()) {
                System.out.print("404");
                return;
            }
        }
        System.out.print("MATCH");
    }

    private static String[] split(String path) {
        return Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}', 'O(n)', 'O(n)', '把模板路径和访问路径都按 / 拆成路径段，逐段比较。模板中的 <id> 这类占位符视为通配一段，普通文本段必须完全相同。', JSON_ARRAY('先按 / 拆分成路径段。', '占位符段可以匹配任意非空段。', '路径段数量不同则一定不匹配。')),
('FastAPI参数校验', 'Java', 'import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String name = sc.hasNext() ? sc.next() : "";
        int age = sc.hasNextInt() ? sc.nextInt() : -1;
        boolean valid = !name.trim().isEmpty() && age >= 1 && age <= 120;
        System.out.print(valid ? "valid" : "invalid");
    }
}', 'O(1)', 'O(1)', '按照题意同时校验两个字段：用户名必须非空，年龄必须在 1 到 120 之间。两个条件都满足才输出 valid。', JSON_ARRAY('用户名不能为空字符串。', '年龄必须落在 1 到 120 之间。', '任一条件不满足都输出 invalid。'));

UPDATE reference_solution rs
JOIN problem p ON p.id = rs.problem_id
JOIN tmp_reference_solution_seed s
  ON s.problem_title COLLATE utf8mb4_unicode_ci = p.title COLLATE utf8mb4_unicode_ci
 AND s.language COLLATE utf8mb4_unicode_ci = rs.language COLLATE utf8mb4_unicode_ci
SET
  rs.solution_code = s.solution_code,
  rs.time_complexity = s.time_complexity,
  rs.space_complexity = s.space_complexity,
  rs.explanation = s.explanation,
  rs.hints = s.hints,
  rs.update_time = NOW();

INSERT INTO reference_solution (
  problem_id, language, solution_code, time_complexity, space_complexity, explanation, hints, create_time, update_time
)
SELECT
  p.id,
  s.language,
  s.solution_code,
  s.time_complexity,
  s.space_complexity,
  s.explanation,
  s.hints,
  NOW(),
  NOW()
FROM tmp_reference_solution_seed s
JOIN problem p
  ON p.title COLLATE utf8mb4_unicode_ci = s.problem_title COLLATE utf8mb4_unicode_ci
LEFT JOIN reference_solution rs
  ON rs.problem_id = p.id
 AND rs.language COLLATE utf8mb4_unicode_ci = s.language COLLATE utf8mb4_unicode_ci
WHERE rs.id IS NULL;

SELECT COUNT(*) AS total_reference_solutions FROM reference_solution;

SELECT COUNT(*) AS problems_without_java_solution
FROM problem p
LEFT JOIN reference_solution rs
  ON rs.problem_id = p.id
 AND rs.language = 'Java'
WHERE rs.id IS NULL;

SELECT COUNT(*) AS problems_without_test_cases
FROM problem p
LEFT JOIN test_case t ON t.problem_id = p.id
WHERE t.id IS NULL;
