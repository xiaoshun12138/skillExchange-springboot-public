package com.example.skillexchange.controller;

import com.example.skillexchange.common.R;
import com.example.skillexchange.common.UserContext;
import com.example.skillexchange.entity.Post;
import com.example.skillexchange.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 内容发布Controller
 * 
 * 核心接口：
 * 1. POST /api/v1/post/publish - 发布内容
 * 2. GET  /api/v1/post/list - 内容列表查询
 * 3. GET  /api/v1/post/detail/{id} - 内容详情查询
 * 4. GET  /api/v1/post/my - 我的发布列表
 * 
 * 核心规则：
 * 1. 所有内容必须先调用微信内容安全API审核，审核不通过绝对禁止发布入库
 * 2. 列表查询固定只查南京雨花台区、审核通过的内容
 * 3. 绝对不展示审核不通过、下架、封禁的内容
 * 4. 绝对不做智能匹配、官方推荐、用户撮合，仅提供基础的列表分页查询
 * 5. 绝对不做多区域切换、LBS定位、距离排序，固定只服务南京雨花台区
 */
@RestController
@RequestMapping("/api/v1/post")
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * 发布内容
     * 必须内置微信内容安全审核，审核不通过绝对禁止发布入库
     * 前端传参格式：{"type":1, "title":"...", "content":"...", "images":["url1","url2"], ...}
     * 
     * @param params 请求参数（包含内容信息和可选的图片URL列表）
     * @return 发布结果
     */
    @PostMapping("/publish")
    public R publish(@RequestBody Map<String, Object> params) {
        // 提取内容信息
        Post post = new Post();
        Object typeObj = params.get("type");
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        String skillProvide = (String) params.get("skillProvide");
        String skillWant = (String) params.get("skillWant");
        Object priceObj = params.get("price");
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) params.get("images");

        // 参数校验
        if (typeObj == null) {
            return R.error("内容类型不能为空");
        }
        if (title == null || title.trim().isEmpty()) {
            return R.error("标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return R.error("内容详情不能为空");
        }

        Integer type = Integer.valueOf(typeObj.toString());
        post.setType(type);
        post.setTitle(title);
        post.setContent(content);
        post.setSkillProvide(skillProvide != null ? skillProvide : "");
        post.setSkillWant(skillWant != null ? skillWant : "");
        if (priceObj != null) {
            post.setPrice(new java.math.BigDecimal(priceObj.toString()));
        } else {
            post.setPrice(java.math.BigDecimal.ZERO);
        }

        // 技能互换类型校验
        if (type == 1) {
            if (skillProvide == null || skillProvide.trim().isEmpty()) {
                return R.error("请填写你会的技能");
            }
            if (skillWant == null || skillWant.trim().isEmpty()) {
                return R.error("请填写你想学的技能");
            }
        }

        // 设置发布用户ID
        post.setUserId(UserContext.getUserId());

        // 调用Service发布内容（内置微信内容安全审核+本地敏感词检测）
        boolean success = postService.publishPost(post, images);
        if (!success) {
            return R.error("内容审核未通过，请修改后重新发布");
        }

        return R.success("发布成功", null);
    }

    /**
     * 分页查询内容列表
     * 固定只查南京雨花台区、审核通过且正常状态的内容
     * 绝对不做智能匹配、官方推荐、用户撮合
     * 
     * @param type 内容类型：1-技能互换，2-闲置物品，null-全部
     * @param keyword 搜索关键词（可选，搜索标题和内容）
     * @param page 当前页码，默认1
     * @param size 每页条数，默认10
     * @return 分页结果
     */
    @GetMapping("/list")
    public R list(@RequestParam(value = "type", required = false) Integer type,
                  @RequestParam(value = "keyword", required = false) String keyword,
                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                  @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Map<String, Object> result = postService.listPost(type, keyword, page, size);
        return R.success(result);
    }

    /**
     * 查询内容详情
     * 审核通过且正常状态的内容才可查看，否则拒绝
     * 
     * @param id 内容ID
     * @return 内容详情（包含图片列表）
     */
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        Map<String, Object> result = postService.getPostDetail(id, userId);
        if (result == null) {
            return R.error("内容不存在或已被下架");
        }
        return R.success(result);
    }

    /**
     * 查询我发布的内容列表
     * 
     * @param page 当前页码，默认1
     * @param size 每页条数，默认10
     * @return 分页结果
     */
    @GetMapping("/my")
    public R myPost(@RequestParam(value = "page", defaultValue = "1") Integer page,
                    @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        Map<String, Object> result = postService.listMyPost(userId, page, size);
        return R.success(result);
    }

    /**
     * 下架自己发布的内容
     * 只有内容发布者本人可以下架
     * 
     * @param id 内容ID
     * @return 下架结果
     */
    @PutMapping("/offShelf/{id}")
    public R offShelf(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        boolean success = postService.offShelfPost(id, userId);
        if (!success) {
            return R.error("下架失败，内容不存在或非本人发布");
        }
        return R.success("下架成功", null);
    }

    /**
     * 删除自己发布的内容（仅已下架的内容可删除）
     * 安全措施：防止误删在线展示的内容
     * 
     * @param id 内容ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        boolean success = postService.deletePost(id, userId);
        if (!success) {
            return R.error("删除失败，内容不存在、非本人发布或未先下架");
        }
        return R.success("删除成功", null);
    }
}
