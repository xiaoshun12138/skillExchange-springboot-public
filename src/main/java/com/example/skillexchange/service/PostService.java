package com.example.skillexchange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillexchange.entity.Post;

import java.util.List;
import java.util.Map;

/**
 * 内容发布表 Service接口
 * 继承MyBatis-Plus的IService，自带增删改查方法
 * 
 * 核心规则：
 * 1. 所有内容必须先调用微信内容安全API审核，审核不通过绝对禁止发布入库
 * 2. 列表查询固定只查南京雨花台区、审核通过的内容
 * 3. 绝对不展示审核不通过、下架、封禁的内容
 * 4. 绝对不做智能匹配、官方推荐、用户撮合
 */
public interface PostService extends IService<Post> {

    /**
     * 发布内容（先调微信内容安全审核，审核通过才入库）
     * @param post 内容信息
     * @param images 图片URL列表
     * @return 发布结果
     */
    boolean publishPost(Post post, List<String> images);

    /**
     * 分页查询内容列表（固定只查南京雨花台区、审核通过的内容）
     * @param type 内容类型：1-技能互换，2-闲置物品，null-全部
     * @param keyword 搜索关键词（可选）
     * @param page 当前页码
     * @param size 每页条数
     * @return 分页结果
     */
    Map<String, Object> listPost(Integer type, String keyword, Integer page, Integer size);

    /**
     * 查询内容详情（审核通过且正常状态的内容才可查看）
     * @param postId 内容ID
     * @param userId 当前登录用户ID（用于判断是否本人发布）
     * @return 内容详情
     */
    Map<String, Object> getPostDetail(Long postId, Long userId);

    /**
     * 查询我发布的内容列表
     * @param userId 用户ID
     * @param page 当前页码
     * @param size 每页条数
     * @return 分页结果
     */
    Map<String, Object> listMyPost(Long userId, Integer page, Integer size);

    /**
     * 用户下架自己发布的内容
     * @param postId 内容ID
     * @param userId 当前登录用户ID
     * @return 下架结果
     */
    boolean offShelfPost(Long postId, Long userId);

    /**
     * 用户删除自己发布的内容（仅已下架的内容可删除）
     * @param postId 内容ID
     * @param userId 当前登录用户ID
     * @return 删除结果
     */
    boolean deletePost(Long postId, Long userId);
}
