-- ============================================
-- 数据重构 - 第二阶段：补充参考答案
-- ============================================
-- 目标: 为70道缺失参考答案的题目补充标准答案
-- 策略: 按难度分组，优先简单题目
-- ============================================

-- 先查看需要补充的题目清单
-- SELECT p.id, p.title, p.difficulty, p.content, p.input, p.output 
-- FROM problem p
-- LEFT JOIN reference_solution rs ON p.id = rs.problem_id
-- WHERE rs.problem_id IS NULL
-- ORDER BY p.difficulty, p.id;

-- ============================================
-- 简单难度题目 (difficulty = 0)
-- ============================================

-- 题目23: 求绝对值
INSERT INTO reference_solution 
(problem_id, language, solution_code, time_complexity, space_complexity, explanation, hints, create_time, update_time)
VALUES (
    23, 
    'Java',
    '/**
 * 题目: 求绝对值
 * 难度: 简单
 * 时间复杂度: O(1)
 * 空间复杂度: O(1)
 * 解题思路: 使用条件判断，如果n小于0则返回-n，否则返回n
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        System.out.println(abs(n));
    }
    
    public static int abs(int n) {
        return n < 0 ? -n : n;
    }
}',
    'O(1)',
    'O(1)',
    '使用条件运算符判断数字正负，负数返回其相反数，正数和零返回本身',
    '["可以使用条件运算符 ?: 简化代码", "也可以使用Math.abs()方法", "注意处理0的情况"]',
    NOW(),
    NOW()
);

-- 题目24: 判断闰年
INSERT INTO reference_solution 
(problem_id, language, solution_code, time_complexity, space_complexity, explanation, hints, create_time, update_time)
VALUES (
    24,
    'Java',
    '/**
 * 题目: 判断闰年
 * 难度: 简单
 * 时间复杂度: O(1)
 * 空间复杂度: O(1)
 * 解题思路: 闰年条件：能被4整除但不能被100整除，或者能被400整除
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int year = scanner.nextInt();
        System.out.println(isLeapYear(year) ? "是" : "否");
    }
    
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
}',
    'O(1)',
    'O(1)',
    '闰年判断条件：能被4整除但不能被100整除，或者能被400整除',
    '["注意逻辑运算符的优先级", "可以先判断能被400整除的情况", "注意条件的完整性"]',
    NOW(),
    NOW()
);

-- 题目25: 判断奇偶
INSERT INTO reference_solution 
(problem_id, language, solution_code, time_complexity, space_complexity, explanation, hints, create_time, update_time)
VALUES (
    25,
    'Java',
    '/**
 * 题目: 判断奇偶
 * 难度: 简单
 * 时间复杂度: O(1)
 * 空间复杂度: O(1)
 * 解题思路: 使用取模运算，n % 2 == 0为偶数，否则为奇数
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        System.out.println(isEven(n) ? "偶数" : "奇数");
    }
    
    public static boolean isEven(int n) {
        return n % 2 == 0;
    }
}',
    'O(1)',
    'O(1)',
    '使用取模运算判断奇偶性，余数为0是偶数，余数为1是奇数',
    '["取模运算%可以得到余数", "也可以使用位运算 & 1 来判断", "注意负数的情况"]',
    NOW(),
    NOW()
);

-- ============================================
-- 中等难度题目 (difficulty = 1)
-- ============================================

-- 题目26: 斐波那契数列
INSERT INTO reference_solution 
(problem_id, language, solution_code, time_complexity, space_complexity, explanation, hints, create_time, update_time)
VALUES (
    26,
    'Java',
    '/**
 * 题目: 斐波那契数列
 * 难度: 中等
 * 时间复杂度: O(n)
 * 空间复杂度: O(1)
 * 解题思路: 使用迭代法，从第1项开始递推计算到第n项
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        System.out.println(fibonacci(n));
    }
    
    public static long fibonacci(int n) {
        if (n <= 0) return 0;
        if (n == 1 || n == 2) return 1;
        
        long prev = 1, curr = 1;
        for (int i = 3; i <= n; i++) {
            long temp = prev + curr;
            prev = curr;
            curr = temp;
        }
        return curr;
    }
}',
    'O(n)',
    'O(1)',
    '斐波那契数列：F(1)=1, F(2)=1, F(n)=F(n-1)+F(n-2)。使用迭代法从底向上计算，避免递归的重复计算',
    '["注意使用long类型防止溢出", "迭代法比递归法效率更高", "注意处理n=1和n=2的边界情况"]',
    NOW(),
    NOW()
);

-- 题目27: 素数判断
INSERT INTO reference_solution 
(problem_id, language, solution_code, time_complexity, space_complexity, explanation, hints, create_time, update_time)
VALUES (
    27,
    'Java',
    '/**
 * 题目: 素数判断
 * 难度: 中等
 * 时间复杂度: O(√n)
 * 空间复杂度: O(1)
 * 解题思路: 素数是大于1的自然数，除了1和它本身外没有其他因数
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        System.out.println(isPrime(n) ? "是" : "否");
    }
    
    public static boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
}',
    'O(√n)',
    'O(1)',
    '素数判断：只需要检查到√n即可，因为如果一个数n有大于√n的因数，那么它一定有小于√n的对应因数',
    '["只需检查到√n即可", "先排除2和3的倍数", "注意处理小于2的特殊情况"]',
    NOW(),
    NOW()
);

-- 题目28: 列表最大值
INSERT INTO reference_solution 
(problem_id, language, solution_code, time_complexity, space_complexity, explanation, hints, create_time, update_time)
VALUES (
    28,
    'Java',
    '/**
 * 题目: 列表最大值
 * 难度: 中等
 * 时间复杂度: O(n)
 * 空间复杂度: O(1)
 * 解题思路: 遍历数组，维护当前最大值
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int[] arr = new int[n];
        
        for (int i = 0; i < n; i++) {
            arr[i] = scanner.nextInt();
        }
        
        System.out.println(findMax(arr));
    }
    
    public static int findMax(int[] arr) {
        if (arr == null || arr.length == 0) {
            throw new IllegalArgumentException("数组不能为空");
        }
        
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }
}',
    'O(n)',
    'O(1)',
    '遍历数组一次，用变量记录当前最大值，时间复杂度O(n)，空间复杂度O(1)',
    '["初始化max为第一个元素", "注意数组为空的边界情况", "遍历从第二个元素开始"]',
    NOW(),
    NOW()
);

-- 题目29: 列表最小值
INSERT INTO reference_solution 
(problem_id, language, solution_code, time_complexity, space_complexity, explanation, hints, create_time, update_time)
VALUES (
    29,
    'Java',
    '/**
 * 题目: 列表最小值
 * 难度: 中等
 * 时间复杂度: O(n)
 * 空间复杂度: O(1)
 * 解题思路: 遍历数组，维护当前最小值
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int[] arr = new int[n];
        
        for (int i = 0; i < n; i++) {
            arr[i] = scanner.nextInt();
        }
        
        System.out.println(findMin(arr));
    }
    
    public static int findMin(int[] arr) {
        if (arr == null || arr.length == 0) {
            throw new IllegalArgumentException("数组不能为空");
        }
        
        int min = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < min) {
                min = arr[i];
            }
        }
        return min;
    }
}',
    'O(n)',
    'O(1)',
    '遍历数组一次，用变量记录当前最小值，与求最大值逻辑相同，只是比较符号相反',
    '["初始化min为第一个元素", "注意数组为空的边界情况", "使用<而不是>进行比较"]',
    NOW(),
    NOW()
);

-- 题目30: 列表排序
INSERT INTO reference_solution 
(problem_id, language, solution_code, time_complexity, space_complexity, explanation, hints, create_time, update_time)
VALUES (
    30,
    'Java',
    '/**
 * 题目: 列表排序
 * 难度: 中等
 * 时间复杂度: O(n²)
 * 空间复杂度: O(1)
 * 解题思路: 使用冒泡排序，相邻元素两两比较并交换
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int[] arr = new int[n];
        
        for (int i = 0; i < n; i++) {
            arr[i] = scanner.nextInt();
        }
        
        bubbleSort(arr);
        
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) System.out.print(" ");
            System.out.print(arr[i]);
        }
    }
    
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}',
    'O(n²)',
    'O(1)',
    '冒泡排序：重复遍历数组，比较相邻元素并交换位置，每轮将最大元素冒泡到末尾',
    '["外层循环控制轮数", "内层循环控制每轮比较次数", "注意数组越界问题"]',
    NOW(),
    NOW()
);

-- 题目31: 阶乘计算
INSERT INTO reference_solution 
(problem_id, language, solution_code, time_complexity, space_complexity, explanation, hints, create_time, update_time)
VALUES (
    31,
    'Java',
    '/**
 * 题目: 阶乘计算
 * 难度: 中等
 * 时间复杂度: O(n)
 * 空间复杂度: O(1)
 * 解题思路: 使用循环累乘，n! = 1 × 2 × 3 × ... × n
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        System.out.println(factorial(n));
    }
    
    public static long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n必须是非负数");
        }
        
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}',
    'O(n)',
    'O(1)',
    '阶乘计算：从1乘到n，注意使用long类型防止溢出，0的阶乘定义为1',
    '["使用long类型防止溢出", "0的阶乘等于1", "注意处理负数情况"]',
    NOW(),
    NOW()
);

-- 显示插入结果
SELECT 
    '参考答案补充进度' as report,
    COUNT(*) as total_inserted,
    (SELECT COUNT(*) FROM reference_solution) as total_solutions,
    CONCAT(ROUND((SELECT COUNT(*) FROM reference_solution) * 100.0 / (SELECT COUNT(*) FROM problem), 2), '%') as coverage
FROM reference_solution
WHERE create_time > DATE_SUB(NOW(), INTERVAL 5 MINUTE);
