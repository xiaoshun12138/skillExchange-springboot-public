package com.example.skillexchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillexchange.entity.Image;
import com.example.skillexchange.entity.Post;
import com.example.skillexchange.mapper.ImageMapper;
import com.example.skillexchange.mapper.PostMapper;
import com.example.skillexchange.service.ImageService;
import com.example.skillexchange.service.PostService;
import com.example.skillexchange.utils.SensitiveWordUtil;
import com.example.skillexchange.utils.WxApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 内容发布表 Service实现类
 * 
 * 核心规则：
 * 1. 所有内容必须先调用微信内容安全API审核，审核不通过绝对禁止发布入库
 * 2. 列表查询固定只查南京雨花台区、审核通过的内容
 * 3. 绝对不展示审核不通过、下架、封禁的内容
 * 4. 绝对不做智能匹配、官方推荐、用户撮合
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private ImageService imageService;

    @Autowired
    private WxApiUtil wxApiUtil;

    /**
     * 发布内容（先调微信内容安全审核，审核通过才入库）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishPost(Post post, List<String> images) {
        // 第一步：调用微信文字内容安全审核
        boolean textPass = wxApiUtil.checkTextContent(post.getTitle() + " " + post.getContent() + " " 
                + post.getSkillProvide() + " " + post.getSkillWant());
        if (!textPass) {
            // 审核不通过，直接驳回，绝对禁止入库
            post.setIsAudit(2);  // 2-审核驳回
            post.setStatus(0);   // 0-下架
            save(post);
            return false;
        }

        // 第二步：调用微信图片的内容安全审核（有图片时）
        if (images != null && !images.isEmpty()) {
            for (String imageUrl : images) {
                boolean imagePass = wxApiUtil.checkImageContent(imageUrl);
                if (!imagePass) {
                    // 图片审核不通过，直接驳回
                    post.setIsAudit(2);
                    post.setStatus(0);
                    save(post);
                    return false;
                }
            }
        }

        // 第三步：本地敏感词二次检测（双保险）
        boolean hasSensitive = SensitiveWordUtil.containsSensitiveWord(
                post.getTitle() + " " + post.getContent() + " " 
                + post.getSkillProvide() + " " + post.getSkillWant());
        if (hasSensitive) {
            post.setIsAudit(2);
            post.setStatus(0);
            save(post);
            return false;
        }

        // 第四步：审核通过，固定区域为南京雨花台区
        post.setIsAudit(1);            // 1-审核通过
        post.setStatus(1);             // 1-正常
        post.setRegion("南京雨花台区"); // 固定写死，只服务这一个区
        save(post);

        // 第五步：保存图片关联
        if (images != null && !images.isEmpty()) {
            imageService.saveBatch(post.getId(), post.getUserId(), images);
        }

        return true;
    }

    /**
     * 分页查询内容列表
     * 固定只查南京雨花台区、审核通过且正常状态的内容
     * 绝对不做智能匹配、官方推荐、用户撮合，仅提供基础列表分页查询
     */
    @Override
    public Map<String, Object> listPost(Integer type, String keyword, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        // 固定只查南京雨花台区
        wrapper.eq(Post::getRegion, "南京雨花台区");
        // 只查审核通过的内容
        wrapper.eq(Post::getIsAudit, 1);
        // 只查正常状态的内容（不展示下架的）
        wrapper.eq(Post::getStatus, 1);
        // 按类型筛选（null则查全部）
        if (type != null) {
            wrapper.eq(Post::getType, type);
        }
        // 搜索关键词（搜索标题和内容）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword)
                    .or().like(Post::getSkillProvide, keyword).or().like(Post::getSkillWant, keyword));
        }
        // 按发布时间倒序
        wrapper.orderByDesc(Post::getCreateTime);

        Page<Post> pageResult = page(new Page<>(page, size), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("pages", pageResult.getPages());
        return result;
    }

    /**
     * 查询内容详情
     * 审核通过且正常状态的内容才可查看，否则拒绝
     */
    @Override
    public Map<String, Object> getPostDetail(Long postId, Long userId) {
        Post post = getById(postId);
        if (post == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        // 只有审核通过且正常状态的内容才能被非发布者查看
        if (post.getIsAudit() == 1 && post.getStatus() == 1) {
            // 增加查看次数
            post.setViewCount(post.getViewCount() + 1);
            updateById(post);

            result.put("post", post);
            result.put("images", imageService.listByPostId(postId));
            result.put("isOwner", post.getUserId().equals(userId));
            return result;
        }

        // 下架、审核不通过的内容，只有发布者本人能看到
        if (post.getUserId().equals(userId)) {
            result.put("post", post);
            result.put("images", imageService.listByPostId(postId));
            result.put("isOwner", true);
            return result;
        }

        // 其他人绝对看不到
        return null;
    }

    /**
     * 查询我发布的内容列表
     */
    @Override
    public Map<String, Object> listMyPost(Long userId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId);
        wrapper.orderByDesc(Post::getCreateTime);

        Page<Post> pageResult = page(new Page<>(page, size), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("pages", pageResult.getPages());
        return result;
    }

    /**
     * 用户下架自己发布的内容
     */
    @Override
    public boolean offShelfPost(Long postId, Long userId) {
        Post post = getById(postId);
        if (post == null || !post.getUserId().equals(userId)) {
            return false;
        }
        post.setStatus(0); // 0-用户主动下架
        return updateById(post);
    }

    /**
     * 用户删除自己发布的内容（仅已下架的内容可删除）
     */
    @Override
    public boolean deletePost(Long postId, Long userId) {
        Post post = getById(postId);
        if (post == null || !post.getUserId().equals(userId)) {
            return false;
        }
        // 安全措施：只有已下架的内容才能删除，防止误删在线内容
        if (post.getStatus() != 0) {
            return false;
        }
        // 删除关联图片
        imageService.deleteByPostId(postId);
        // 删除帖子
        return removeById(postId);
    }
}
