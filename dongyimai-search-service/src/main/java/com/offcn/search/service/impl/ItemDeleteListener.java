package com.offcn.search.service.impl;


import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

   /* Inspection info:Checks autowiring problems in a bean class.*/
    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] ids= (Long[]) objectMessage.getObject();
            System.out.println("ItemDeleteListener监听接收到消息...");
            itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            System.out.println("成功删除索引库中的记录");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
