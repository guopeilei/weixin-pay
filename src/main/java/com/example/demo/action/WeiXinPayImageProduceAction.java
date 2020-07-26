package com.example.demo.action;

import com.example.demo.bean.ReturnNotify;
import com.example.demo.bean.URLCode;
import com.example.demo.bean.WxCusInfoBean;
import com.example.demo.service.OrderCodeService;
import com.example.demo.util.CustomConfig;
import com.example.demo.util.PayCommonUtil;
import com.example.demo.util.XMLUtil4jdom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
@RestController
@RequestMapping(value = "/pay")
public class WeiXinPayImageProduceAction {

    @Autowired
    private OrderCodeService orderCodeService;

    @Autowired
    CustomConfig customConfig;
    /**
     * 生成动态的二维码
     *
     * @return 返回支付链接
     * @throws Exception
     */

    @RequestMapping(value = "/getPayWxqrcode", method = RequestMethod.POST)
    @ResponseBody
    public URLCode wxQrCode(@RequestParam Map<String,String> paramMap) throws Exception {
        String payAmount = paramMap.get("payAmount");
        String order_price = "";
        try{
            //上传的金额单位是分，是个整型
            BigDecimal fen = (new BigDecimal(payAmount)).multiply(new BigDecimal(100));
            fen = fen.setScale(0, BigDecimal.ROUND_HALF_UP);
            order_price = fen.toString();

        }catch (Exception e){
            e.printStackTrace();
        }
        // 组装参数
        Map<String, Object> param = new HashMap<>();
        param.put("order_price", order_price);
        param.put("body", "腾讯充值中心-QQ会员充值");
        param.put("out_trade_no", PayCommonUtil.create_trade_no());//交易订单号

        // 生成微信支付二维码链接
        Map<String, String> result = orderCodeService.doUnifiedOrder(param);
        URLCode urlCode = new URLCode();
        String return_code = result.get("return_code");
        if ("FAIL".equals(return_code)) {
            urlCode.setError("error");
            return urlCode;
        } else {
            //业务结果正确
            if ("SUCCESS".equals(result.get("result_code"))) {

                String urlcode = result.get("code_url");//二维码链接
                String prepay_id = result.get("prepay_id");//预支付交易会话标识
                String nonce_str = result.get("nonce_str");//随机字符串
                urlCode.setPrepayId(prepay_id);
                urlCode.setCodeUrl(urlcode);
                urlCode.setNonce_str(nonce_str);
                urlCode.setSign(result.get("sign"));
            } else {
                urlCode.setErr_code(result.get("err_code"));
                urlCode.setErr_code_des(result.get("err_code_des"));
            }
            return urlCode;// 生成微信二维码
        }
    }


    /**
     * 微信支付成功后的回调方法
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/wxnotify", method = RequestMethod.POST,produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public ReturnNotify wxNotify(@RequestBody String request) throws Exception {
        ReturnNotify returnNotify = new ReturnNotify();
        //解析xml成map
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap = XMLUtil4jdom.doXMLParse(request);

        //过滤空 设置 TreeMap
        SortedMap<Object, Object> packageParams = new TreeMap<>();
        Iterator it = paramMap.keySet().iterator();
        while (it.hasNext()) {
            String parameter = (String) it.next();
            String parameterValue = paramMap.get(parameter);

            String v = "";
            if (null != parameterValue) {
                v = parameterValue.trim();
            }
            packageParams.put(parameter, v);
        }
        String return_code = (String) packageParams.get("return_code");
        String return_msg = (String) packageParams.get("return_msg");
        if ("FAIL".equals(return_code)) {
            returnNotify.setReturn_code("FAIL");
            returnNotify.setReturn_msg(return_msg);
            return returnNotify;
        }
        //此处根据AppID换取商户的APIKey（已在数据库里存好），业务逻辑自己写即可
        String appid = (String) packageParams.get("appid");
        String mch_id = (String) packageParams.get("mch_id");
        String openid = (String) packageParams.get("openid");
        String is_subscribe = (String) packageParams.get("is_subscribe");
        String out_trade_no = (String) packageParams.get("out_trade_no");
        BigDecimal total_fee = new BigDecimal((String) packageParams.get("total_fee"));
        String transaction_id = (String) packageParams.get("transaction_id");
        String key = orderCodeService.getKey();//获取商户自己的key
        //判断签名是否正确
        if (PayCommonUtil.isTenpaySign("UTF-8", packageParams, key)) {
            //处理业务开始

            returnNotify.setReturn_code("SUCCESS");
            returnNotify.setReturn_msg("OK");
        } else {
            returnNotify.setReturn_code("FAIL");
            returnNotify.setReturn_msg("参数格式校验错误");
            return returnNotify;
        }
        return returnNotify;
    }

}
