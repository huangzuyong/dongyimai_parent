package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private AlipayClient alipayClient ;




    @Override
    public Map createNative(String out_trade_no, String total_fee) {

        Map<String,String> map = new HashMap<String, String>();
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

        long total = Long.parseLong(total_fee);
        BigDecimal bigTotal = BigDecimal.valueOf(total);
        BigDecimal doubleValue = BigDecimal.valueOf(100d);
        BigDecimal bigYuan = bigTotal.divide(doubleValue);

        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"total_amount\":\""+bigYuan.doubleValue()+"\"," +
                "    \"subject\":\"测试购买商品001\"," +
                "    \"store_id\":\"xa_001\"," +
                "    \"timeout_express\":\"90m\"}");//设置业务参数
        //发出预下单业务请求
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            String code = response.getCode();
            System.out.println("响应码:"+code);
            String body = response.getBody();
            System.out.println("返回结果:"+body);

            if (code.equals("10000")){
                //支付需要的地址
                map.put("qrcode", response.getQrCode());
                map.put("out_trade_no", response.getOutTradeNo());
                map.put("total_fee", total_fee);
                System.out.println("qrcode:"+response.getQrCode());
                System.out.println("out_trade_no:"+response.getOutTradeNo());
                System.out.println("total_fee:"+total_fee);
            }else {
                System.out.println("预下单接口调用失败:"+body);
            }



        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        return map;
    }

    @Override
    public Map<String,String> queryPayStatus(String out_trade_no) {
        Map<String,String> map = new HashMap<String, String>();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent(
                "{" +
                        "    \"out_trade_no\":\""+out_trade_no+"\"," +
                        "    \"trade_no\":\"\"}"
        );

        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            String code = response.getCode();
            System.out.println("返回值1:"+response.getBody());
            if (code.equals("10000")){
                map.put("out_trade_no", out_trade_no);
                map.put("tradestatus", response.getTradeStatus());
                map.put("trade_no",response.getTradeNo());//获取流水号放到集合中去
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
            return map;
    }


}
