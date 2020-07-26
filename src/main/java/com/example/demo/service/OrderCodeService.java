package com.example.demo.service;

import com.example.demo.bean.WxCusInfoBean;
import org.springframework.stereotype.Service;

import java.util.Map;
public interface OrderCodeService {


    public Map<String, String> doUnifiedOrder(Map<String, Object> param) ;

    /**
     * 获取商户的API密钥
     * @return
     */
    public String getKey();
}
