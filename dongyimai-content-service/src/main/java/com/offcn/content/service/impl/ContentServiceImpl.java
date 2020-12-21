package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentService;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbContentMapper;
import com.offcn.pojo.TbContent;
import com.offcn.pojo.TbContentExample;
import com.offcn.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;


    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        contentMapper.insert(content);

        redisTemplate.boundHashOps( "content" ).delete( content.getCategoryId() );
    }


    /**
     * 修改
     */
    @Override
    public void update(TbContent content){

        Long categoryIdOld = contentMapper.selectByPrimaryKey( content.getId() ).getCategoryId();
        redisTemplate.boundHashOps( "content" ).delete(categoryIdOld  );

        if(!categoryIdOld.equals( content.getCategoryId() )){
            redisTemplate.boundHashOps( "content" ).delete(content.getCategoryId()  );
        }


        contentMapper.updateByPrimaryKey(content);
    }

    /**
     * 根据ID获取实体
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id){
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for(Long id:ids){

            TbContent tbContent = contentMapper.selectByPrimaryKey( id );
            redisTemplate.boundHashOps( "content" ).delete( tbContent.getCategoryId());

            contentMapper.deleteByPrimaryKey(id);

        }
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example=new TbContentExample();
        Criteria criteria = example.createCriteria();

        if(content!=null){
            if(content.getTitle()!=null && content.getTitle().length()>0){
                criteria.andTitleLike("%"+content.getTitle()+"%");
            }			if(content.getUrl()!=null && content.getUrl().length()>0){
                criteria.andUrlLike("%"+content.getUrl()+"%");
            }			if(content.getPic()!=null && content.getPic().length()>0){
                criteria.andPicLike("%"+content.getPic()+"%");
            }			if(content.getStatus()!=null && content.getStatus().length()>0){
                criteria.andStatusLike("%"+content.getStatus()+"%");
            }
        }

        Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {

        Map map = new HashMap(  );

        //1.从redis查
        List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps( "content" ).get( categoryId );

        if(contentList == null){//从mysql中查询
            //根据广告分类ID查询广告列表
            TbContentExample example=new TbContentExample();
            Criteria criteria2 = example.createCriteria();
            criteria2.andCategoryIdEqualTo(categoryId);

            criteria2.andStatusEqualTo( "1" );

            example.setOrderByClause( "sort_order" );

            contentList = contentMapper.selectByExample( example );

            //3、放入到redis中
            redisTemplate.boundHashOps( "content" ).put( categoryId, contentList );
            System.out.println("从mysql中查询,放入到redis");
        }


        return contentList;
    }

}