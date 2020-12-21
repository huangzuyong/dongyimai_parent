package com.offcn.util;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SmsUtil {

    @Value("${AppCode}")
    private String AppCode;

    @Value("${tpl}")
    private String tpl;

    private String host="http://dingxin.market.alicloudapi.com";

    public HttpResponse sendSms(String mobile,String param) throws Exception {

        String path = "/dx/sendSms";
        String method = "POST";

        Map<String,String> headers = new HashMap<String,String>();
        headers.put("Authorization", "APPCODE " + AppCode);
        Map<String,String> querys = new HashMap<String,String>();
        querys.put("mobile", mobile);  // 手机号
        querys.put("param", "code:" + param); // 验证码
        querys.put("tpl_id", tpl);   // 默认模板
        Map<String, String> bodys = new HashMap<String, String>();
        HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
        return response;

    }




}
