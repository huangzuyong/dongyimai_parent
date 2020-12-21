package com.offcn.solrutil;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private TbItemMapper itemMapper;


    public void ImportItemData(){

        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//只导入审核的产品
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        System.out.println("---------------商品列表-------------");
        //对列表的数据进行json转换
        for(TbItem item : tbItems){
            System.out.println(item.getTitle());
            //转换成json
            Map<String,String > specMap = JSON.parseObject(item.getSpec(), Map.class);
            //创建集合存储拼音
            Map<String,String> mapPinyin=new HashMap<>();
            //遍历map，替换key从汉字变为拼音
            for(String key :specMap.keySet()){
                mapPinyin.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
            }
            item.setSpecMap(mapPinyin);

        }

        solrTemplate.saveBeans(tbItems);
        solrTemplate.commit();
        System.out.println("-----------------------------------");

    }


    public static void main(String[] args) {

        //*意味着 1.当前的spring配置文件 2.dao中spring的配置文件
        ApplicationContext context = new ClassPathXmlApplicationContext( "classpath*:spring/applicationContext*.xml" );

        SolrUtil solrUtil = (SolrUtil) context.getBean( "solrUtil" );
        solrUtil.ImportItemData();
    }

}
