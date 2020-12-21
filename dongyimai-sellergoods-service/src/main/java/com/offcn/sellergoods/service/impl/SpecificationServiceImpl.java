package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Specification;
import com.offcn.mapper.TbSpecificationMapper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationExample;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
	private TbSpecificationMapper specificationMapper;

    @Autowired
    private TbSpecificationOptionMapper tbSpecificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	public void add(Specification specification) {

		specificationMapper.insert(specification.getSpecification());
		for (TbSpecificationOption specificationOption:specification.getSpecificationOptionLIst()){
            specificationOption.setSpecId(specification.getSpecification().getId());
            tbSpecificationOptionMapper.insert(specificationOption);
        }

	}

	
	/**
	 * 修改
	 */
	public void update(Specification specification){
		specificationMapper.updateByPrimaryKey(specification.getSpecification());
        TbSpecificationOptionExample example =new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(specification.getSpecification().getId());
        tbSpecificationOptionMapper.deleteByExample(example);

        for (TbSpecificationOption specificationOption:specification.getSpecificationOptionLIst()){
            specificationOption.setSpecId(specification.getSpecification().getId());
            tbSpecificationOptionMapper.insert(specificationOption);
        }
	}


	public Specification findOne(Long id){
        Specification specification = new Specification();
        specification.setSpecification(specificationMapper.selectByPrimaryKey(id));

        TbSpecificationOptionExample example =new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<TbSpecificationOption> tbSpecificationOptions = tbSpecificationOptionMapper.selectByExample(example);
        specification.setSpecificationOptionLIst(tbSpecificationOptions);
        return specification;
	}

    public List<Map> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

    /**
	 * 批量删除
	 */
	public void delete(Long[] ids) {
		for(Long id:ids){
			specificationMapper.deleteByPrimaryKey(id);
            TbSpecificationOptionExample example =new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(id);
            tbSpecificationOptionMapper.deleteByExample(example);
		}
	}
	
	
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		TbSpecificationExample.Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
