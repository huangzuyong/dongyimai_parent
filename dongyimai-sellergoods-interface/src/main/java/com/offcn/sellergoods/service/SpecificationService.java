package com.offcn.sellergoods.service;

import com.offcn.entity.PageResult;
import com.offcn.group.Specification;
import com.offcn.pojo.TbSpecification;

import java.util.List;
import java.util.Map;


public interface SpecificationService {

	public List<TbSpecification> findAll();
	

	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(Specification specification);


	/**
	 * 修改
	 */
	public void update(Specification specification);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Specification findOne(Long id);
    List<Map> selectOptionList();

	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize);
	
}
