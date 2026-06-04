package com.example.skillexchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillexchange.entity.Image;
import com.example.skillexchange.mapper.ImageMapper;
import com.example.skillexchange.service.ImageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片表 Service实现类
 * 
 * 图片必须经过微信内容安全审核才能关联到内容发布
 */
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {

    /**
     * 根据内容ID查询图片列表
     */
    @Override
    public List<Image> listByPostId(Long postId) {
        return list(new LambdaQueryWrapper<Image>().eq(Image::getPostId, postId));
    }

    /**
     * 批量保存图片
     */
    @Override
    public void saveBatch(Long postId, Long userId, List<String> imageUrls) {
        List<Image> imageList = new ArrayList<>();
        for (String url : imageUrls) {
            Image image = new Image();
            image.setPostId(postId);
            image.setUserId(userId);
            image.setImageUrl(url);
            imageList.add(image);
        }
        saveBatch(imageList);
    }

    /**
     * 根据内容ID删除所有关联图片
     */
    @Override
    public void deleteByPostId(Long postId) {
        remove(new LambdaQueryWrapper<Image>().eq(Image::getPostId, postId));
    }
}
