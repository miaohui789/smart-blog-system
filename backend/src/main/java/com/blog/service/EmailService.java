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
    private static final int CODE_EXPIRE_MINUTES = 5;

    /**
     * 发送验证码邮件（注册用）
     */
    public boolean sendVerifyCode(String toEmail) {
        return sendCode(toEmail, CODE_PREFIX, "注册验证码", "您的注册验证码是：");
    }

    /**
     * 发送重置密码验证码
     */
    public boolean sendResetPasswordCode(String toEmail) {
        return sendCode(toEmail, RESET_CODE_PREFIX, "重置密码验证码", "您的重置密码验证码是：");
    }

    /**
     * 通用发送验证码方法
     */
    private boolean sendCode(String toEmail, String prefix, String subject, String description) {
        // 检查是否频繁发送（90秒内只能发一次）
        String limitKey = prefix + "limit:" + toEmail;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            return false;
        }

        // 生成6位验证码
        String code = generateCode();
        
        try {
            // 发送邮件
            sendEmail(toEmail, subject, description, code);
            
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
     * 验证重置密码验证码
     */
    public boolean verifyResetCode(String email, String code) {
        return verifyCodeInternal(RESET_CODE_PREFIX + email, code);
    }

    /**
     * 通用验证码验证方法
     */
    private boolean verifyCodeInternal(String codeKey, String code) {
        String savedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (savedCode != null && savedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(codeKey);
            return true;
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
    private void sendEmail(String toEmail, String subject, String description, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("【我的博客】" + subject);
        
        String content = buildEmailContent(description, code);
        helper.setText(content, true);
        
        mailSender.send(message);
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String description, String code) {
        return """
            <div style="max-width: 500px; margin: 0 auto; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #3b82f6; margin: 0;">我的博客</h1>
                </div>
                <div style="background: #f8fafc; border-radius: 12px; padding: 30px; text-align: center;">
                    <p style="color: #64748b; margin: 0 0 20px;">%s</p>
                    <div style="font-size: 36px; font-weight: bold; color: #1e293b; letter-spacing: 8px; margin: 20px 0;">
                        %s
                    </div>
                    <p style="color: #94a3b8; font-size: 14px; margin: 20px 0 0;">
                        验证码有效期为 %d 分钟，请勿泄露给他人
                    </p>
                </div>
                <p style="color: #94a3b8; font-size: 12px; text-align: center; margin-top: 20px;">
                    如果这不是您的操作，请忽略此邮件
                </p>
            </div>
            """.formatted(description, code, CODE_EXPIRE_MINUTES);
    }
}
