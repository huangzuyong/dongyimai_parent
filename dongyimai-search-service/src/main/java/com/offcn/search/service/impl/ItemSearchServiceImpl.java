package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

   /* private Map<String, Object> searchList(Map searchMap) {

        Map<String,Object> map = new HashMap<String, Object>();
        Query query = new SimpleQuery();
        // is：基于分词后的结果                                      和           传入的参数匹配keywords
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows", tbItems.getContent() );
        return map;
    }*/
    //高亮查询
    private Map searchList(Map searchMap) {

        Map map = new HashMap();
        SimpleHighlightQuery query = new SimpleHighlightQuery();



        //按分类查询
        if (!"".equals(searchMap.get("brand"))) {
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //按品牌查询
        if (!"".equals(searchMap.get("category"))) {
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //按规格查询
        if (searchMap.get("spec") != null) {
            Map<String,String > spec = (Map<String, String>) searchMap.get("spec");
            for ( String key :spec.keySet()){
                Criteria filterCriteria = new Criteria("item_spec_"+Pinyin.toPinyin(key, "").toLowerCase()).is(spec.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //按价格筛选
        if(!"".equals(searchMap.get("price"))){
            String[] price= ((String) searchMap.get("price")).split("-");
            if (!price[0].equals("0")){
                Criteria filterQueryCriteria = new Criteria("item_price").greaterThan(price[0]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterQueryCriteria) ;
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")){
                Criteria filterQueryCriteria = new Criteria("item_price").lessThan(price[1]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterQueryCriteria) ;
                query.addFilterQuery(filterQuery);
            }
        }

        //分页查询
        Integer  pageNo = (Integer) searchMap.get("pageNo");
        if (pageNo == null) {
            pageNo=1;
        }
        Integer  pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null) {
            pageSize=20;
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);
        //关键字空格处理---去空格
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));

        //排序---价格和更新的时间
        String sortValue= (String) searchMap.get("sort");//ASC  DESC
        String sortField= (String) searchMap.get("sortField");//排序字段
        if(sortValue != null && !"".equals(sortValue)){
            if ("ASC".equals(sortValue)){
                Sort  sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if ("ASC".equals(sortValue)){
                Sort  sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }



        //高亮处理的字段
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //设置高亮的前后缀
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);
        //查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();

        for (HighlightEntry<TbItem> highlightEntry : highlighted) {

            //获取基本数据对象
            TbItem tbItem = highlightEntry.getEntity();
            if (highlightEntry.getHighlights().size() > 0 && highlightEntry.getHighlights().get(0).getSnipplets().size() > 0) {
                List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();
                //高亮结果集合
                List<String> snipplets = highlightList.get(0).getSnipplets();
                //获取第一个高亮字段对应的高亮结果，设置到商品标题
                tbItem.setTitle(snipplets.get(0));
            }
        }
        //把带高亮数据集合存放map
        map.put("rows",page.getContent());
        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数
        return map;
    }

    private List searchCategoryList(Map searchMap){
        List<String> list = new ArrayList<String>();
        //查询条件
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //分条件
        GroupOptions options = new GroupOptions();
        options.addGroupByField("item_category");
        query.setGroupOptions(options);
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //分组集合的入口
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for(GroupEntry<TbItem> entry : content){
            list.add(entry.getGroupValue());//向数组中添加处理的数据
        }
        return list;
    }

    /*
    * 查询品牌和规格列表
    * */

    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        Long typeId  = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId != null) {
            //模板id查品牌
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);
            //模板id查规格
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;

    }




    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String,Object> map=new HashMap<String,Object>();
        //1.查询列表
        map.putAll(searchList(searchMap));

        //2.根据关键字查询商品分类分组
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        //3.查询不为空的品牌和规格数组
        String categoryName=(String) searchMap.get("category");
        if(!"".equals(categoryName)) {
            //按照分类名称重新读取对应品牌、规格
            map.putAll(searchBrandAndSpecList(categoryName));
        }else {
            if (categoryList.size() > 0) {
                Map mapBrandAndSpec = searchBrandAndSpecList((String) categoryList.get(0));
                map.putAll(mapBrandAndSpec);
            }
        }
        return map;
    }

    @Override
    public void ImportItemData(List<TbItem> list) {


            //对列表的数据进行json转换
            for(TbItem item : list){
                System.out.println(item.getTitle());
                //转换成json
                Map<String,String > specMap = JSON.parseObject(item.getSpec(), Map.class);
                //创建集合存储拼音
                Map mapPinyin=new HashMap();
                //遍历map，替换key从汉字变为拼音
                for(String key :specMap.keySet()){
                    mapPinyin.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
                }
                item.setSpecMap(mapPinyin);

            }

            solrTemplate.saveBeans(list);
            solrTemplate.commit();
            System.out.println("-----------------------------------");



    }

    @Override
    public void deleteByGoodsIds(List goodsIds) {
        System.out.println("删除的数据的id"+goodsIds);

        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);

        solrTemplate.commit();
    }


}
