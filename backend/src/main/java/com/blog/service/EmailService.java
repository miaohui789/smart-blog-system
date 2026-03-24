package com.blog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String CODE_PREFIX = "email:code:";
    private static final String RESET_CODE_PREFIX = "email:reset:";
    private static final String LOGIN_CODE_PREFIX = "email:login:";
    private static final int CODE_EXPIRE_MINUTES = 5;

    /**
     * 发送验证码邮件（注册用）
     */
    public boolean sendVerifyCode(String toEmail) {
        return sendCode(toEmail, CODE_PREFIX, "注册验证码", "您正在注册账号，验证码是：", "register");
    }

    /**
     * 发送登录验证码
     */
    public boolean sendLoginCode(String toEmail) {
        return sendCode(toEmail, LOGIN_CODE_PREFIX, "登录验证码", "您正在登录账号，验证码是：", "login");
    }

    /**
     * 发送重置密码验证码
     */
    public boolean sendResetPasswordCode(String toEmail) {
        return sendCode(toEmail, RESET_CODE_PREFIX, "重置密码验证码", "您正在重置密码，验证码是：", "reset");
    }

    /**
     * 通用发送验证码方法
     */
    private boolean sendCode(String toEmail, String prefix, String subject, String description, String type) {
        // 检查是否频繁发送（90秒内只能发一次）
        String limitKey = prefix + "limit:" + toEmail;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            return false;
        }

        // 生成6位验证码
        String code = generateCode();
        
        try {
            // 发送邮件
            sendEmail(toEmail, subject, description, code, type);
            
            // 存储验证码到Redis，5分钟过期
            String codeKey = prefix + toEmail;
            redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            // 设置发送限制，90秒内不能重复发送
            redisTemplate.opsForValue().set(limitKey, "1", 90, TimeUnit.SECONDS);
            
            log.info("验证码已发送至: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("发送验证码失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证注册验证码
     */
    public boolean verifyCode(String email, String code) {
        return verifyCodeInternal(CODE_PREFIX + email, code);
    }

    /**
     * 验证登录验证码
     */
    public boolean verifyLoginCode(String email, String code) {
        return verifyCodeInternal(LOGIN_CODE_PREFIX + email, code);
    }

    /**
     * 验证重置密码验证码
     */
    public boolean verifyResetCode(String email, String code) {
        return verifyCodeInternal(RESET_CODE_PREFIX + email, code);
    }

    /**
     * 通用验证码验证方法
     */
    private boolean verifyCodeInternal(String codeKey, String code) {
        if (code == null || code.trim().isEmpty()) {
            log.warn("验证码为空, key: {}", codeKey);
            return false;
        }
        
        // 去除前后空格
        code = code.trim();
        
        String savedCode = redisTemplate.opsForValue().get(codeKey);
        log.info("验证码校验 - key: {}, 输入: {}, Redis中: {}", codeKey, code, savedCode);
        
        if (savedCode != null && savedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(codeKey);
            return true;
        }
        
        if (savedCode == null) {
            log.warn("验证码不存在或已过期, key: {}", codeKey);
        } else {
            log.warn("验证码不匹配, key: {}, 输入: [{}], 期望: [{}]", codeKey, code, savedCode);
        }
        return false;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送邮件
     */
    private void sendEmail(String toEmail, String subject, String description, String code, String type) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("【我的博客】" + subject);
        
        String content = buildEmailContent(description, code, type);
        helper.setText(content, true);
        
        mailSender.send(message);
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String description, String code, String type) {
        // 根据类型选择不同的颜色和SVG图标
        String themeColor = switch (type) {
            case "register" -> "#3b82f6"; // 蓝色 - 注册
            case "login" -> "#a855f7";    // 紫色 - 登录
            case "reset" -> "#ef4444";    // 红色 - 重置密码
            default -> "#3b82f6";
        };
        
        // 使用SVG图标替代表情符号
        String iconSvg = switch (type) {
            case "register" -> """
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12Z" fill="%s"/>
                    <path d="M12 14C6.48 14 2 16.48 2 19.5V22H22V19.5C22 16.48 17.52 14 12 14Z" fill="%s"/>
                </svg>
                """.formatted(themeColor, themeColor);
            case "login" -> """
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M18 8H17V6C17 3.24 14.76 1 12 1C9.24 1 7 3.24 7 6V8H6C4.9 8 4 8.9 4 10V20C4 21.1 4.9 22 6 22H18C19.1 22 20 21.1 20 20V10C20 8.9 19.1 8 18 8ZM12 17C10.9 17 10 16.1 10 15C10 13.9 10.9 13 12 13C13.1 13 14 13.9 14 15C14 16.1 13.1 17 12 17ZM15.1 8H8.9V6C8.9 4.29 10.29 2.9 12 2.9C13.71 2.9 15.1 4.29 15.1 6V8Z" fill="%s"/>
                </svg>
                """.formatted(themeColor);
            case "reset" -> """
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12.65 10C11.7 7.31 8.9 5.5 5.77 6.12C3.48 6.58 1.62 8.41 1.14 10.7C0.32 14.57 3.26 18 7 18C9.61 18 11.83 16.33 12.65 14H16.92C17.65 16.16 19.66 17.74 22 17.74V15.74C20.39 15.74 19.09 14.44 19.09 12.83C19.09 11.22 20.39 9.92 22 9.92V7.92C19.66 7.92 17.65 9.5 16.92 11.66H12.65V10ZM7 16C4.79 16 3 14.21 3 12C3 9.79 4.79 8 7 8C9.21 8 11 9.79 11 12C11 14.21 9.21 16 7 16Z" fill="%s"/>
                </svg>
                """.formatted(themeColor);
            default -> "";
        };
        
        String title = switch (type) {
            case "register" -> "欢迎注册";
            case "login" -> "安全登录";
            case "reset" -> "重置密码";
            default -> "验证码";
        };
        
        return """
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <div style="margin-bottom: 16px;">%s</div>
                    <h1 style="color: %s; margin: 0; font-size: 28px; font-weight: 700;">%s</h1>
                    <p style="color: #94a3b8; margin: 10px 0 0; font-size: 14px;">我的博客 - MyBlog</p>
                </div>
                <div style="background: linear-gradient(135deg, #f8fafc 0%%, #f1f5f9 100%%); border-radius: 16px; padding: 40px; text-align: center; border: 2px solid %s; box-shadow: 0 4px 20px rgba(0,0,0,0.08);">
                    <p style="color: #475569; margin: 0 0 24px; font-size: 16px; font-weight: 500;">%s</p>
                    <div style="background: white; border-radius: 12px; padding: 24px; margin: 20px 0; box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
                        <div style="font-size: 48px; font-weight: bold; color: %s; letter-spacing: 12px; font-family: 'Courier New', monospace;">
                            %s
                        </div>
                    </div>
                    <div style="background: rgba(255,255,255,0.6); border-radius: 8px; padding: 16px; margin-top: 24px;">
                        <p style="color: #64748b; font-size: 14px; margin: 0; line-height: 1.8;">
                            <span style="display: inline-block; margin-right: 8px;">⏰</span> 验证码有效期为 <strong style="color: %s;">%d 分钟</strong><br/>
                            <span style="display: inline-block; margin-right: 8px;">🔒</span> 请勿泄露给他人
                        </p>
                    </div>
                </div>
                <div style="text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e2e8f0;">
                    <p style="color: #94a3b8; font-size: 13px; margin: 0 0 8px;">
                        如果这不是您的操作，请忽略此邮件
                    </p>
                    <p style="color: #cbd5e1; font-size: 12px; margin: 0;">
                        © 2026 MyBlog. All rights reserved.
                    </p>
                </div>
            </div>
            """.formatted(iconSvg, themeColor, title, themeColor, description, themeColor, code, themeColor, CODE_EXPIRE_MINUTES);
    }
}
