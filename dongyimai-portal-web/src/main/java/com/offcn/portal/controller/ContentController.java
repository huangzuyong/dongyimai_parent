package com.offcn.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.content.service.ContentService;
import com.offcn.pojo.TbContent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("content")
@ResponseBody
public class ContentController {

@Reference
ContentService contentService;

    @RequestMapping("findByCartegoryId")
    public List<TbContent> findByCartegoryId(Long categoryId) {
        return contentService.findByCategoryId(categoryId);
    }
}
