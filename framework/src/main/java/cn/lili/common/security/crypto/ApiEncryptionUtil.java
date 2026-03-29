package cn.lili.common.security.crypto;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * API接口加密工具类
 * 提供AES加密解密、签名验证、防重放等功能
 * 
 * @author Qoder
 * @since 2026-01-09
 */
@Slf4j
public class ApiEncryptionUtil {
    
    /**
     * AES加密算法
     */
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    
    /**
     * AES密钥算法
     */
    private static final String AES_KEY_ALGORITHM = "AES";
    
    /**
     * HMAC-SHA256算法
     */
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    
    /**
     * IV长度(字节)
     */
    private static final int IV_LENGTH = 16;
    
    /**
     * AES加密
     * 
     * @param plainText 明文
     * @param key AES密钥(32字节Base64编码)
     * @return Base64编码的密文(IV + 加密数据)
     * @throws Exception 加密异常
     */
    public static String encrypt(String plainText, String key) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            // 解码Base64密钥
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, AES_KEY_ALGORITHM);
            
            // 生成随机IV
            byte[] iv = generateRandomIV();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            // 加密
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // 拼接IV和加密数据
            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);
            
            // Base64编码
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new Exception("数据加密失败", e);
        }
    }
    
    /**
     * AES解密
     * 
     * @param cipherText Base64编码的密文(IV + 加密数据)
     * @param key AES密钥(32字节Base64编码)
     * @return 明文
     * @throws Exception 解密异常
     */
    public static String decrypt(String cipherText, String key) throws Exception {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        
        try {
            // 解码Base64密钥
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, AES_KEY_ALGORITHM);
            
            // 解码Base64密文
            byte[] combined = Base64.getDecoder().decode(cipherText);
            
            // 提取IV
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // 提取加密数据
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            // 解密
            byte[] decrypted = cipher.doFinal(encrypted);
            
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new Exception("数据解密失败", e);
        }
    }
    
    /**
     * 生成随机IV
     * 
     * @return 16字节的随机IV
     */
    private static byte[] generateRandomIV() {
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }
    
    /**
     * 生成随机nonce
     * 
     * @return 16位随机字符串
     */
    public static String generateNonce() {
        return RandomUtil.randomString(16);
    }
    
    /**
     * 验证时间戳是否在有效期内
     * 
     * @param timestamp 请求时间戳(毫秒)
     * @param allowedMinutes 允许的时间差(分钟)
     * @return true表示时间戳有效
     */
    public static boolean verifyTimestamp(long timestamp, int allowedMinutes) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = Math.abs(currentTime - timestamp);
        long allowedMillis = allowedMinutes * 60 * 1000L;
        return timeDiff <= allowedMillis;
    }
    
    /**
     * 生成HMAC-SHA256签名
     * 
     * @param data 待签名数据
     * @param secret 签名密钥
     * @return Base64编码的签名
     * @throws Exception 签名异常
     */
    public static String generateSign(String data, String secret) throws Exception {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), 
                HMAC_SHA256_ALGORITHM
            );
            mac.init(secretKeySpec);
            byte[] signBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signBytes);
        } catch (Exception e) {
            log.error("生成签名失败", e);
            throw new Exception("生成签名失败", e);
        }
    }
    
    /**
     * 验证HMAC-SHA256签名
     * 
     * @param data 原始数据
     * @param sign 待验证的签名
     * @param secret 签名密钥
     * @return true表示签名有效
     */
    public static boolean verifySign(String data, String sign, String secret) {
        try {
            String expectedSign = generateSign(data, secret);
            return expectedSign.equals(sign);
        } catch (Exception e) {
            log.error("验证签名失败", e);
            return false;
        }
    }
}
