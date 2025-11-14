/*
 Navicat Premium Dump SQL

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 90100 (9.1.0)
 Source Host           : localhost:3306
 Source Schema         : angel

 Target Server Type    : MySQL
 Target Server Version : 90100 (9.1.0)
 File Encoding         : 65001

 Date: 09/11/2025 20:52:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID（关联帖子表）',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联用户表）',
  `content` varchar(500) NOT NULL COMMENT '评论内容',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `likes` int DEFAULT '0' COMMENT '点赞数',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`) COMMENT '帖子ID索引，用于查询某帖子的所有评论',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，用于查询某用户的所有评论',
  KEY `idx_comment_post_id` (`post_id`),
  KEY `idx_comment_user_id` (`user_id`),
  KEY `idx_comment_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

-- ----------------------------
-- Table structure for comment_like
-- ----------------------------
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `comment_id` bigint NOT NULL COMMENT '评论ID（关联评论表）',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联用户表）',
  `created_at` datetime DEFAULT NULL COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`,`user_id`) COMMENT '唯一约束：防止用户对同一评论重复点赞',
  KEY `idx_comment_id` (`comment_id`) COMMENT '评论ID索引，用于查询某评论的所有点赞',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，用于查询某用户点赞过的所有评论',
  KEY `idx_comment_like_comment_id` (`comment_id`),
  KEY `idx_comment_like_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论点赞表';

-- ----------------------------
-- Table structure for diary
-- ----------------------------
DROP TABLE IF EXISTS `diary`;
CREATE TABLE `diary` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID（外键，关联用户表user的id字段）',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日记内容',
  `mood_id` tinyint unsigned NOT NULL COMMENT '心情ID（1-开心，2-平静，3-难过，4-愤怒，5-惊喜，6-焦虑）',
  `mood_name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '心情名称（与mood_id一一对应）',
  `mood_icon` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '心情表情（如图标路径/Emoji字符，例："?"或"/icons/happy.png"）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（默认当前时间）',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时自动刷新）',
  `is_draft` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为草稿（0-非草稿，1-草稿）',
  `tags` json DEFAULT NULL COMMENT '标签数组（可选，存储格式：["工作","生活","感悟"]）',
  `date` date DEFAULT NULL COMMENT '日记日期',
  PRIMARY KEY (`id`),
  KEY `idx_diary_user_id` (`user_id`),
  CONSTRAINT `diary_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `diary_chk_1` CHECK ((`mood_id` between 1 and 6))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户日记表';

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息唯一ID（主键）',
  `session_id` bigint NOT NULL COMMENT '关联的会话ID（外键）',
  `role` varchar(20) NOT NULL COMMENT '发送者角色：user（用户）、assistant（AI）',
  `content` longtext NOT NULL COMMENT '消息内容（支持长文本）',
  `timestamp` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '消息发送时间（精确到毫秒）',
  `sequence` int NOT NULL COMMENT '同一会话内的消息序号（1、2、3...用于排序）',
  `metadata` json DEFAULT NULL COMMENT '附加元数据（如：是否有附件、工具调用参数等）',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`) COMMENT '按会话ID查询消息的索引',
  KEY `idx_session_seq` (`session_id`,`sequence`) COMMENT '同一会话内按序号排序的联合索引',
  CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI对话的消息表（存储单条消息内容）';

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID（关联用户表）',
  `content` varchar(255) DEFAULT NULL COMMENT '帖子内容',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `likes` int DEFAULT '0' COMMENT '点赞数',
  `comments_count` int DEFAULT '0' COMMENT '评论数',
  `images` varchar(1000) DEFAULT NULL COMMENT '图片URL数组（JSON字符串）',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签数组（JSON字符串）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，用于查询用户发布的帖子',
  KEY `idx_post_user_id` (`user_id`),
  KEY `idx_post_created_at` (`created_at`),
  KEY `idx_post_likes` (`likes`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子表';

-- ----------------------------
-- Table structure for post_like
-- ----------------------------
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID（关联帖子表）',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联用户表）',
  `created_at` datetime DEFAULT NULL COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`) COMMENT '唯一约束：防止用户对同一帖子重复点赞',
  KEY `idx_post_id` (`post_id`) COMMENT '帖子ID索引，用于查询某帖子的所有点赞',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，用于查询某用户点赞过的所有帖子',
  KEY `idx_post_like_post_id` (`post_id`),
  KEY `idx_post_like_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子点赞表';

-- ----------------------------
-- Table structure for post_save
-- ----------------------------
DROP TABLE IF EXISTS `post_save`;
CREATE TABLE `post_save` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID（关联帖子表）',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联用户表）',
  `created_at` datetime DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`) COMMENT '唯一约束：防止用户对同一帖子重复收藏',
  KEY `idx_post_id` (`post_id`) COMMENT '帖子ID索引，用于查询某帖子的所有收藏记录',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，用于查询某用户收藏的所有帖子',
  KEY `idx_post_save_post_id` (`post_id`),
  KEY `idx_post_save_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子收藏表';

-- ----------------------------
-- Table structure for session
-- ----------------------------
DROP TABLE IF EXISTS `session`;
CREATE TABLE `session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话唯一ID（主键）',
  `user_id` varchar(64) NOT NULL COMMENT '关联的用户ID（如用户系统的唯一标识）',
  `title` varchar(255) DEFAULT NULL COMMENT '会话标题（可选，如自动生成的首条消息摘要）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '会话最后更新时间（随最后一条消息更新）',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '会话状态：0-活跃，1-已结束',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) COMMENT '按用户ID查询会话的索引'
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI对话的会话表（存储对话整体信息）';

-- ----------------------------
-- Table structure for topic
-- ----------------------------
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `participant_count` int DEFAULT '0',
  `is_hot` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `email` varchar(100) NOT NULL COMMENT '登录邮箱（唯一）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `bio` text COMMENT '个人简介',
  `introduction` text COMMENT '个人介绍',
  `remember_login` tinyint(1) DEFAULT '0' COMMENT '记住登录状态',
  `auto_login` tinyint(1) DEFAULT '0' COMMENT '自动登录',
  `show_diaries` tinyint(1) DEFAULT '1' COMMENT '是否显示日记',
  `show_test_results` tinyint(1) DEFAULT '1' COMMENT '是否显示测试结果',
  `community_notifications` tinyint(1) DEFAULT '1' COMMENT '社区通知',
  `tests_notifications` tinyint(1) DEFAULT '1' COMMENT '测试通知',
  `registration_date` datetime DEFAULT NULL COMMENT '注册日期',
  `last_login_date` datetime DEFAULT NULL COMMENT '最后登录日期',
  `password` varchar(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色：0普通用户，1管理员',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '账号状态：0未激活，1正常，2封禁',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号（可选）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`) COMMENT '邮箱唯一索引，避免重复注册',
  KEY `idx_status` (`status`) COMMENT '账号状态索引，便于筛选正常用户'
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表（含管理员）';

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `follower_id` bigint NOT NULL COMMENT '关注者ID（主动关注的用户）',
  `following_id` bigint NOT NULL COMMENT '被关注者ID（被关注的用户）',
  `created_at` datetime DEFAULT NULL COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`,`following_id`) COMMENT '唯一约束：防止重复关注',
  KEY `idx_follower_id` (`follower_id`) COMMENT '关注者ID索引，用于查询用户的关注列表',
  KEY `idx_following_id` (`following_id`) COMMENT '被关注者ID索引，用于查询用户的粉丝列表',
  KEY `idx_user_follow_follower_id` (`follower_id`),
  KEY `idx_user_follow_following_id` (`following_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注关系表';

SET FOREIGN_KEY_CHECKS = 1;
