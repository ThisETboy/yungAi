package com.example.smarthub.module.system.service;

import com.example.smarthub.module.system.vo.CaptchaVO;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 生成验证码，返回 UUID 和 Base64 图片
     */
    CaptchaVO generateCaptcha();

    /**
     * 校验验证码，校验成功后立即删除（一次性使用）
     *
     * @param uuid 验证码唯一标识
     * @param code 用户输入的验证码
     * @return 是否校验通过
     */
    boolean verifyCaptcha(String uuid, String code);
}
