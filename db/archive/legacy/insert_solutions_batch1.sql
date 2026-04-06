-- 第四批：基础语法题（33-42）
INSERT INTO reference_solution 
(problem_id, language, solution_code, time_complexity, space_complexity, explanation)
VALUES
(33, 'Java', '/**
 * 题目: 变量声明与初始化
 * 难度: 简单
 */
public class Solution {
    public static void main(String[] args) {
        int age = 18;
        String name = "张三";
        System.out.println(age);
        System.out.println(name);
    }
}', 'O(1)', 'O(1)', '声明变量并初始化，然后输出'),

(34, 'Java', '/**
 * 题目: 变量交换
 * 难度: 简单
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        int temp = a;
        a = b;
        b = temp;
        System.out.println(a + " " + b);
    }
}', 'O(1)', 'O(1)', '使用临时变量交换两个数的值'),

(35, 'Java', '/**
 * 题目: 基本数据类型练习
 * 难度: 简单
 */
public class Solution {
    public static void main(String[] args) {
        byte b = 10;
        short s = 100;
        int i = 1000;
        long l = 10000L;
        float f = 3.14f;
        double d = 3.1415926;
        boolean bool = true;
        char c = ''A'';
        System.out.println(b);
        System.out.println(s);
        System.out.println(i);
        System.out.println(l);
        System.out.println(f);
        System.out.println(d);
        System.out.println(bool);
        System.out.println(c);
    }
}', 'O(1)', 'O(1)', '声明各种基本数据类型变量并输出'),

(36, 'Java', '/**
 * 题目: 读取输入并输出
 * 难度: 简单
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int num = sc.nextInt();
        String str = sc.next();
        System.out.println("你输入的整数是：" + num + "，字符串是：" + str);
    }
}', 'O(1)', 'O(1)', '使用Scanner读取输入，格式化输出'),

(37, 'Java', '/**
 * 题目: 格式化输出
 * 难度: 简单
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        double r = sc.nextDouble();
        double area = Math.PI * r * r;
        System.out.printf("%.2f", area);
    }
}', 'O(1)', 'O(1)', '计算圆面积，使用printf格式化输出保留2位小数'),

(38, 'Java', '/**
 * 题目: 判断正数负数
 * 难度: 简单
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        if (n > 0) {
            System.out.println("这是一个正数");
        } else if (n < 0) {
            System.out.println("这是一个负数");
        } else {
            System.out.println("这是零");
        }
    }
}', 'O(1)', 'O(1)', '使用if-else判断正数、负数或零'),

(39, 'Java', '/**
 * 题目: 成绩等级判断
 * 难度: 简单
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int score = sc.nextInt();
        if (score >= 90) {
            System.out.println("A");
        } else if (score >= 80) {
            System.out.println("B");
        } else if (score >= 70) {
            System.out.println("C");
        } else if (score >= 60) {
            System.out.println("D");
        } else {
            System.out.println("E");
        }
    }
}', 'O(1)', 'O(1)', '使用if-else if多分支判断成绩等级'),

(40, 'Java', '/**
 * 题目: 闰年判断
 * 难度: 简单
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int year = sc.nextInt();
        if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
            System.out.println(year + "是闰年");
        } else {
            System.out.println(year + "不是闰年");
        }
    }
}', 'O(1)', 'O(1)', '根据闰年条件判断并输出结果'),

(41, 'Java', '/**
 * 题目: 计算1到n的和
 * 难度: 简单
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += i;
        }
        System.out.println(sum);
    }
}', 'O(n)', 'O(1)', '使用for循环累加求和'),

(42, 'Java', '/**
 * 题目: 输出乘法表
 * 难度: 简单
 */
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print(j + "×" + i + "=" + (i * j));
                if (j < i) System.out.print(" ");
            }
            if (i < n) System.out.println();
        }
    }
}', 'O(n²)', 'O(1)', '使用双重循环输出乘法表');
