-- MySQL dump 10.13  Distrib 8.0.45, for macos26.3 (arm64)
--
-- Host: 127.0.0.1    Database: skill_exchange
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `image`
--

DROP TABLE IF EXISTS `image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增，唯一标识）',
  `post_id` bigint NOT NULL COMMENT '关联内容ID（关联post.id）',
  `user_id` bigint NOT NULL COMMENT '上传用户ID（关联wx_user.id，责任追溯用）',
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片访问URL（仅存地址，不存图片本身，节省数据库空间）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '图片上传时间（自动生成）',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`) COMMENT '关联内容ID索引（内容详情页图片轮播用）',
  KEY `idx_user_id` (`user_id`) COMMENT '上传用户ID索引（责任追溯用）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增，唯一标识）',
  `from_user_id` bigint NOT NULL COMMENT '发送人ID（关联wx_user.id）',
  `to_user_id` bigint NOT NULL COMMENT '接收人ID（关联wx_user.id）',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容（限制500字）',
  `is_sensitive` tinyint DEFAULT '0' COMMENT '是否含敏感词：0-否，1-是（拦截后标记）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间（自动生成）',
  PRIMARY KEY (`id`),
  KEY `idx_from_user` (`from_user_id`) COMMENT '发送人ID索引（我的私信列表用）',
  KEY `idx_to_user` (`to_user_id`) COMMENT '接收人ID索引（我的私信列表用）',
  KEY `idx_create_time` (`create_time`) COMMENT '发送时间索引（按时间倒序用）'
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `operation_log`
--

DROP TABLE IF EXISTS `operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增，唯一标识）',
  `user_id` bigint DEFAULT '0' COMMENT '操作用户ID（未登录用户为0）',
  `ip` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作IP地址（责任追溯用）',
  `uri` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '请求接口地址（责任追溯用）',
  `method` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '请求方式（GET/POST/PUT/DELETE）',
  `params` text COLLATE utf8mb4_unicode_ci COMMENT '请求参数（脱敏后存储，责任追溯用）',
  `result` text COLLATE utf8mb4_unicode_ci COMMENT '响应结果（脱敏后存储，责任追溯用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间（自动生成）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) COMMENT '操作用户ID索引（查询用户操作记录用）',
  KEY `idx_create_time` (`create_time`) COMMENT '操作时间索引（自动清理6个月前的日志用）'
) ENGINE=InnoDB AUTO_INCREMENT=196 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增，唯一标识）',
  `user_id` bigint NOT NULL COMMENT '发布用户ID（关联wx_user.id）',
  `type` tinyint NOT NULL COMMENT '内容类型：1-技能互换，2-闲置物品',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容标题（必填，限制100字）',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容详情（必填，限制500字）',
  `region` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '南京雨花台区' COMMENT '区域（固定写死，不支持修改，责任规避+精准定位）',
  `skill_provide` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '我会的技能（技能类型必填，闲置类型留空）',
  `skill_want` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '我想学的技能（技能类型必填，闲置类型留空）',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '闲置价格（闲置类型必填，单位元，技能类型留空）',
  `is_audit` tinyint DEFAULT '0' COMMENT '审核状态：0-审核中（刚发布，机器审核中），1-审核通过（正常展示），2-审核驳回（违规，不展示）',
  `status` tinyint DEFAULT '1' COMMENT '内容状态：0-用户主动下架，1-正常展示，2-平台强制下架',
  `view_count` int DEFAULT '0' COMMENT '查看次数（自动累加）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '内容发布时间（自动生成）',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '内容最后更新时间（自动生成）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) COMMENT '发布用户ID索引（我的发布列表用）',
  KEY `idx_type` (`type`) COMMENT '类型索引（首页tab筛选用）',
  KEY `idx_region_status_audit` (`region`,`status`,`is_audit`) COMMENT '复合索引（首页列表查询用，固定区域+正常状态+审核通过，查询速度极快）',
  KEY `idx_create_time` (`create_time`) COMMENT '发布时间索引（按时间倒序用）'
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容发布表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `report`
--

DROP TABLE IF EXISTS `report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增，唯一标识）',
  `report_user_id` bigint NOT NULL COMMENT '举报人ID（关联wx_user.id）',
  `reported_post_id` bigint NOT NULL COMMENT '被举报内容ID（关联post.id）',
  `reported_user_id` bigint NOT NULL COMMENT '被举报用户ID（关联wx_user.id，责任追溯用）',
  `reason` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报原因（必填，限制200字）',
  `status` tinyint DEFAULT '0' COMMENT '处理状态：0-待处理，1-已处理（已下架内容/封禁用户），2-驳回（举报不成立）',
  `handle_result` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '处理结果（必填，限制200字）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间（自动生成）',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '处理时间（自动生成）',
  PRIMARY KEY (`id`),
  KEY `idx_report_user` (`report_user_id`) COMMENT '举报人ID索引（我的举报列表用）',
  KEY `idx_reported_post` (`reported_post_id`) COMMENT '被举报内容ID索引（快速定位被举报内容用）',
  KEY `idx_status` (`status`) COMMENT '处理状态索引（待处理举报快速过滤用）'
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户举报表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `wx_user`
--

DROP TABLE IF EXISTS `wx_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wx_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增，唯一标识）',
  `openid` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '微信用户唯一标识（不可修改，微信官方返回）',
  `nickname` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户昵称（脱敏展示，不强制修改）',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户头像URL（脱敏展示，不强制修改）',
  `phone` varchar(11) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '绑定手机号（仅用于实名校验，不对外展示）',
  `is_real_name` tinyint DEFAULT '0' COMMENT '是否实名：0-未实名（拦截所有业务操作），1-已实名（可正常使用）',
  `status` tinyint DEFAULT '1' COMMENT '用户状态：0-永久封禁，1-正常使用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建时间（自动生成）',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '账号最后更新时间（自动生成）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `openid` (`openid`),
  KEY `idx_openid` (`openid`) COMMENT 'openid索引（登录、查询用户用）',
  KEY `idx_phone` (`phone`) COMMENT '手机号索引（实名校验用）',
  KEY `idx_status` (`status`) COMMENT '状态索引（封禁用户快速过滤用）'
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信用户表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-01 16:33:14
