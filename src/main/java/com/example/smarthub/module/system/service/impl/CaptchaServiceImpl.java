package com.example.smarthub.module.system.service.impl;

import com.example.smarthub.module.system.service.CaptchaService;
import com.example.smarthub.module.system.vo.CaptchaVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 * 使用 Java AWT 绘制随机字符图片，SHA-256 哈希存入 Redis，一次性校验
 */
@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${sys.captcha.length:4}")
    private int captchaLength;

    @Value("${sys.captcha.width:120}")
    private int captchaWidth;

    @Value("${sys.captcha.height:40}")
    private int captchaHeight;

    @Value("${sys.captcha.ttl:5}")
    private int captchaTtlMinutes;

    private static final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final Color[] COLORS = {
            new Color(220, 38, 38),   // 红色
            new Color(37, 99, 235),   // 蓝色
            new Color(22, 163, 74),   // 绿色
            new Color(202, 138, 4),   // 黄色
            new Color(124, 58, 237),  // 紫色
            new Color(219, 112, 147), // 粉色
    };

    public CaptchaServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public CaptchaVO generateCaptcha() {
        String uuid = UUID.randomUUID().toString();
        String code = generateRandomCode();

        try {
            String base64Image = drawCaptchaImage(code);

            // 将验证码的 SHA-256 哈希存入 Redis
            String hash = sha256(code.toLowerCase());
            String redisKey = "captcha:" + uuid;
            stringRedisTemplate.opsForValue().set(redisKey, hash, captchaTtlMinutes, TimeUnit.MINUTES);

            log.debug("Generated captcha for uuid={}", uuid);

            CaptchaVO captchaVO = new CaptchaVO();
            captchaVO.setUuid(uuid);
            captchaVO.setImage(base64Image);
            return captchaVO;
        } catch (Exception e) {
            log.error("Failed to generate captcha image", e);
            throw new RuntimeException("验证码生成失败", e);
        }
    }

    @Override
    public boolean verifyCaptcha(String uuid, String code) {
        if (uuid == null || uuid.isBlank() || code == null || code.isBlank()) {
            return false;
        }

        String redisKey = "captcha:" + uuid;
        String storedHash = stringRedisTemplate.opsForValue().get(redisKey);

        if (storedHash == null) {
            log.warn("Captcha expired or not found, uuid={}", uuid);
            return false;
        }

        String inputHash = sha256(code.trim().toLowerCase());
        boolean valid = storedHash.equals(inputHash);

        if (valid) {
            stringRedisTemplate.delete(redisKey);
            log.info("Captcha verified successfully, uuid={}", uuid);
        } else {
            log.warn("Captcha verification failed, uuid={}, input={}", uuid, code);
        }

        return valid;
    }

    /**
     * 生成随机验证码字符串
     */
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(captchaLength);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < captchaLength; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    /**
     * 绘制验证码图片并返回 Base64
     */
    private String drawCaptchaImage(String code) throws Exception {
        BufferedImage image = new BufferedImage(captchaWidth, captchaHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 白色背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, captchaWidth, captchaHeight);

        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 绘制干扰线
        for (int i = 0; i < 5; i++) {
            g2d.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawLine(random.nextInt(captchaWidth), random.nextInt(captchaHeight),
                    random.nextInt(captchaWidth), random.nextInt(captchaHeight));
        }

        // 绘制干扰点
        for (int i = 0; i < 30; i++) {
            g2d.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g2d.fillOval(random.nextInt(captchaWidth), random.nextInt(captchaHeight), 2, 2);
        }

        // 绘制验证码文字
        int fontSize = captchaHeight - 10;
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);

        int charWidth = captchaWidth / (captchaLength + 1);
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            g2d.setColor(COLORS[random.nextInt(COLORS.length)]);

            // 随机旋转
            double angle = random.nextDouble(-15.0, 15.0);
            AffineTransform transform = g2d.getTransform();
            g2d.rotate(Math.toRadians(angle), charWidth * (i + 1), captchaHeight / 2);
            g2d.drawString(String.valueOf(c), charWidth * (i + 1) - fontSize / 3, captchaHeight / 2 + fontSize / 3);
            g2d.setTransform(transform);
        }

        g2d.dispose();

        // 转为 Base64 PNG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * SHA-256 哈希
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 hashing failed", e);
        }
    }
}
