package com.example.skillexchange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillexchange.entity.Image;

import java.util.List;

/**
 * 图片表 Service接口
 * 继承MyBatis-Plus的IService，自带增删改查方法
 * 
 * 图片必须经过微信内容安全审核才能关联到内容发布
 */
public interface ImageService extends IService<Image> {

    /**
     * 根据内容ID查询图片列表
     * @param postId 内容ID
     * @return 图片列表
     */
    List<Image> listByPostId(Long postId);

    /**
     * 批量保存图片
     * @param postId 内容ID
     * @param userId 用户ID
     * @param imageUrls 图片URL列表
     */
    void saveBatch(Long postId, Long userId, List<String> imageUrls);

    /**
     * 根据内容ID删除所有关联图片
     * @param postId 内容ID
     */
    void deleteByPostId(Long postId);
}
