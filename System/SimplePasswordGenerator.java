import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SimplePasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "admin123";
        String hashedPassword = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt加密后的密码: " + hashedPassword);
        System.out.println();
        System.out.println("请使用以下SQL更新数据库密码：");
        System.out.println("UPDATE user SET password = '" + hashedPassword + "' WHERE username = 'admin';");
        System.out.println();
        
        // 验证密码
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("密码验证结果: " + matches);
    }
}