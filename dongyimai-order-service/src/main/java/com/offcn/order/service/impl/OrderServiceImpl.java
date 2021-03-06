package com.offcn.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.Cart;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbOrderItemMapper;
import com.offcn.mapper.TbOrderMapper;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.order.service.OrderService;
import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbOrderExample;
import com.offcn.pojo.TbOrderExample.Criteria;
import com.offcn.pojo.TbOrderItem;
import com.offcn.pojo.TbPayLog;
import com.offcn.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sound.midi.Soundbank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
    private TbOrderItemMapper orderItemMapper;

	@Autowired
    private IdWorker idWorker;

	@Autowired
    private  RedisTemplate redisTemplate;

    @Autowired
    private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
	    List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        List<String> orderIdList= new ArrayList<String>();
        double total_money = 0 ;

	    for (Cart cart :cartList){
            long orderId = idWorker.nextId();
            TbOrder tborder = new TbOrder();
            tborder.setOrderId(orderId);// 订单ID
            tborder.setUserId(order.getUserId());// 用户名
            tborder.setPaymentType(order.getPaymentType());// 支付类型
            tborder.setStatus("1");// 状态：未付款
            tborder.setCreateTime(new Date());// 订单创建日期
            tborder.setUpdateTime(new Date());// 订单更新日期
            tborder.setReceiverAreaName(order.getReceiverAreaName());// 地址
            tborder.setReceiverMobile(order.getReceiverMobile());// 手机号
            tborder.setReceiver(order.getReceiver());// 收货人
            tborder.setSourceType(order.getSourceType());// 订单来源
            tborder.setSellerId(cart.getSellerId());// 商家ID
            // 循环购物车明细
            double money = 0;

            for (TbOrderItem orderItem :cart.getOrderItemList()){

                orderItem.setId(idWorker.nextId());
                orderItem.setOrderId(orderId);// 订单ID
                orderItem.setSellerId(cart.getSellerId());
                money += orderItem.getTotalFee().doubleValue();// 金额累加
                orderItemMapper.insert(orderItem);

            }

            tborder.setPayment(new BigDecimal(money));
            orderMapper.insert(tborder);
            orderIdList.add(orderId+"");//添加到订单列表
            total_money+=money;//累加到总金额

	    }
	    if ("1".equals(order.getPaymentType())){

            TbPayLog payLog = new TbPayLog();
            String outTradeNo = idWorker.nextId() + "";//change id to snowfilerID
            payLog.setOutTradeNo(outTradeNo);
            payLog.setCreateTime(new Date());
            String orderIds = orderIdList.toString();
            orderIds.replace("[","" ).replace("]","" ).replace(" ","" );
            payLog.setOrderList(orderIds);
            payLog.setPayType("1");////支付类型---为支付宝

            System.out.println("合计金额:"+total_money);
            BigDecimal total_money1 = BigDecimal.valueOf(total_money);
            BigDecimal cj = BigDecimal.valueOf(100d);
            //高精度乘法
            BigDecimal bigDecimal = total_money1.multiply(cj);

            double hj=total_money*100;
            System.out.println("合计:"+hj);
            System.out.println("高精度处理:"+bigDecimal.toBigInteger().longValue());
            payLog.setTotalFee(bigDecimal.toBigInteger().longValue());

            payLog.setTradeState("0");//wait to pay for the order
            payLog.setUserId(order.getUserId());
            payLogMapper.insert(payLog);
            redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);


        }
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());
        payLog.setTradeState("1");
        payLog.setTransactionId(transaction_id);
        payLogMapper.updateByPrimaryKey(payLog);

        /*修改订单号*/
        String orderList = payLog.getOrderList();
        String[] orderIds = orderList.split(",");

        for (String order:orderIds){
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(order));
            if (tbOrder != null){
                tbOrder.setStatus("2");//付款成功的订单
                orderMapper.updateByPrimaryKey(tbOrder);
            }
        }
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());

    }

}
