package com.offcn.pay.service;

import java.util.Map;

public interface AliPayService {

    /*alibaba interface entry*/

    public Map createNative(String out_trade_no, String total_fee);

    /**
     * 查询支付状态
     * @param out_trade_no
     */
    public Map queryPayStatus(String out_trade_no);



}
