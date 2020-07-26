package com.example.demo.serviceimp;

import com.alibaba.fastjson.JSON;
import com.example.demo.bean.H5Info;
import com.example.demo.bean.SenceInfo;
import com.example.demo.bean.WxCusInfoBean;
import com.example.demo.service.OrderCodeService;
import com.example.demo.util.CustomConfig;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.PayCommonUtil;
import com.example.demo.util.XMLUtil4jdom;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class OrderCodeServiceImp implements OrderCodeService {

    @Autowired
    private CustomConfig customConfig;

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public Map<String, String> doUnifiedOrder(Map<String, Object> param) {
        String appid = customConfig.getAppId();
        String mch_id = customConfig.getMchId();
        String key = customConfig.getKey();
        String trade_type = "NATIVE";
        String spbill_create_ip = "192.168.1.66";   // 获取发起电脑 ip
        String notify_url = customConfig.getPayNotifyUrl();
        String currTime = PayCommonUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = PayCommonUtil.buildRandom(4) + "";
        String nonce_str = strTime + strRandom;                             // 随机字符串
        String order_price = (String) param.get("order_price");             // 价格   注意：价格的单位是分
        String body = (String) param.get("body");                           // 商品名称
        String out_trade_no = (String) param.get("out_trade_no");           // 订单号

        String attach = (String) param.get("attach");                       // 附加参数,这里传的是我们的订单号orderId

        SortedMap<Object, Object> packageParams = new TreeMap<>();
        packageParams.put("appid", appid); //公众号ID
        packageParams.put("mch_id", mch_id);//商户号
        packageParams.put("nonce_str", nonce_str);//随机字符串，不长于32位
        // 签名
        String sign = PayCommonUtil.createSign("UTF-8", packageParams, key);
        packageParams.put("sign", sign);//签名
        packageParams.put("body", body);//商品描述,例如  腾讯充值中心-QQ会员充值
        packageParams.put("out_trade_no", out_trade_no);//商户订单号, 商户系统内部的订单号,32个字符内、可包含字母
        packageParams.put("total_fee", order_price);//总金额
        packageParams.put("spbill_create_ip", spbill_create_ip);//终端IP,必须传正确的用户端IP,支持ipv4、ipv6格式，获取方式详见获取用户ip指引
        packageParams.put("notify_url", notify_url);//通知地址
        packageParams.put("trade_type", trade_type);//交易类型,H5支付的交易类型为MWEB
        H5Info h5Info = new H5Info();
        SenceInfo senceInfo = new SenceInfo();
        senceInfo.setType("Wap");
        senceInfo.setWap_name("小商店");
        senceInfo.setWap_url("http://localhost:8080/");
        h5Info.setH5_info(senceInfo);
        String scene_info = JSON.toJSONString(h5Info);

        packageParams.put("scene_info", scene_info);//场景信息


        // 微信支付接口传输数据使用xml方式进行的，此处将参数装换为xml
        // map --> xml
        String requestXML = PayCommonUtil.getRequestXml(packageParams);
        System.out.println("---------- Request XML: " + requestXML);
        String resXml = HttpUtil.postData(customConfig.getPayUrl(), requestXML);
        System.out.println("---------- Response XML: " + resXml);

        // xml --> map
        try {
            return XMLUtil4jdom.doXMLParse(resXml);
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
