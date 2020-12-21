package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 搜索
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);
    /*
    * 导入数据
    * */
    public void ImportItemData(List<TbItem> list);
    /*
    * 删除数据
    * */
    public void deleteByGoodsIds(List goodsIds);
}
