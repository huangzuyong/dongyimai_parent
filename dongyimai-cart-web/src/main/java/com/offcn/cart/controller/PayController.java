package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AliPayService aliPayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = orderService.searchPayLogFromRedis(name);
        if (payLog != null){
            return  aliPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
        }else{
            return new HashMap();
        }

    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus (String out_trade_no) {
        Result result = null;
        int i = 0;
        while (true) {
            //调用查询接口
            Map<String, String> map = null;
            try {
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e1) {
                /*e1.printStackTrace();*/
                System.out.println("调用查询服务出错");
            }
           if (map == null){
                result = new Result(false,"支付出错");
                break;
           }
           if (map.get("tradestatus")!=null && map.get("tradestatus").equals("TRADE_SUCCESS")){
               //修改订单状态  trade_no是在AliPayServiceImpl 中的查询支付状态的时候放进来的
               orderService.updateOrderStatus(out_trade_no, map.get("trade_no"));
               result=new  Result(true, "支付成功");
               break;
           }
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_CLOSED")){//如果成功
                result=new  Result(true, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_FINISHED")){//如果成功
                result=new  Result(true, "交易结束，不可退款");
                break;
            }
            //behind is all access ,sleep 3 S rerun
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            i++;
            if(i>=100){
                result=new  Result(false, "二维码超时");
                break;
            }

        }

        return result;
    }





}
