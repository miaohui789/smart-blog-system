package com.blog.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具 - 运行 main 方法生成 BCrypt 密码
 * 生成后在数据库中执行:
 * UPDATE sys_user SET password = '生成的哈希值' WHERE username = 'admin';
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("===========================================");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("BCrypt哈希: " + encodedPassword);
        System.out.println("===========================================");
        System.out.println();
        System.out.println("请在数据库中执行以下SQL:");
        System.out.println("UPDATE sys_user SET password = '" + encodedPassword + "' WHERE username = 'admin';");
        System.out.println();
        
        // 验证
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("验证结果: " + (matches ? "成功" : "失败"));
    }
}
