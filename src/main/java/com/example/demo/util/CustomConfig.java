package com.example.demo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
// 指定配置文件路径
@PropertySource(value = "classpath:custom.properties",encoding = "UTF-8")
public class CustomConfig {
    @Value("${custom.appid}")
    private String appId;
    @Value("${custom.mch_id}")
    private String mchId;
    @Value("${custom.key}")
    private String key;
    @Value("${custom.secret}")
    private String secret;
    @Value("${h5.payurl}")
    private String payUrl;
    @Value("${h5.notify}")
    private String payNotifyUrl;

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getPayNotifyUrl() {
        return payNotifyUrl;
    }

    public void setPayNotifyUrl(String payNotifyUrl) {
        this.payNotifyUrl = payNotifyUrl;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

}
