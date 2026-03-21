/*
 Navicat Premium Data Transfer

 Source Server         : yjh
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : localhost:3306
 Source Schema         : hajimi_adoption

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 21/03/2026 15:09:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for adopt_application
-- ----------------------------
DROP TABLE IF EXISTS `adopt_application`;
CREATE TABLE `adopt_application`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '申请人(领养人)ID',
  `cat_id` bigint NOT NULL COMMENT '申请领养的猫咪ID',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '领养理由',
  `experience` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '养宠经验/饲养计划',
  `housing_condition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '居住情况(如: 自有住房/整租/合租, 已封窗)',
  `feeding_plan` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详细饲养计划(吃什么粮等)',
  `proof_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '证明附件(居住环境、封窗照片等，多图逗号分隔)',
  `signature` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '领养人电子签名',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-审核通过(待签协议), 2-已驳回, 3-已完成领养',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审核人(救助员)ID',
  `review_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核意见/家访记录',
  `apply_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '申请时间',
  `review_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `sign_time` datetime(0) NULL DEFAULT NULL COMMENT '签署协议时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '领养申请与审核表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of adopt_application
-- ----------------------------
INSERT INTO `adopt_application` VALUES (1, 2, 1, '喜欢', '喜欢', NULL, NULL, NULL, 'ZDD', 3, 3, '可以', '2026-03-20 22:27:04', '2026-03-20 22:49:47', '2026-03-20 22:57:37');
INSERT INTO `adopt_application` VALUES (2, 2, 2, '测试', '测试', '测试', '测试', 'http://localhost:8080/uploads/977c4270c7d644e7a9eeb5f85b27480b.png', NULL, 4, NULL, NULL, '2026-03-20 23:06:01', NULL, NULL);
INSERT INTO `adopt_application` VALUES (3, 2, 2, '测试的', '测试', '测试', '测试', 'http://localhost:8080/uploads/357c8b6609f940a9adc8ad3536751f09.png', NULL, 4, NULL, NULL, '2026-03-20 23:55:52', NULL, NULL);
INSERT INTO `adopt_application` VALUES (4, 4, 2, '喜欢', '', '自己的房子', '', '', NULL, 1, 3, '1', '2026-03-21 14:08:36', '2026-03-21 14:09:25', NULL);

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '所属帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论人ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID，NULL表示一级评论',
  `root_id` bigint NULL DEFAULT NULL COMMENT '根评论ID，方便查整棵树',
  `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT '回复目标用户ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_root_id`(`root_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 1, 2, NULL, NULL, NULL, '嘿嘿', 1, '2026-03-21 00:16:52');
INSERT INTO `comment` VALUES (2, 1, 4, NULL, NULL, NULL, '好可爱！', 0, '2026-03-21 00:17:57');
INSERT INTO `comment` VALUES (3, 1, 4, 1, 1, 2, '哈哈', 0, '2026-03-21 00:18:04');
INSERT INTO `comment` VALUES (4, 1, 2, 3, 1, 4, '11', 0, '2026-03-21 14:36:26');

-- ----------------------------
-- Table structure for comment_like
-- ----------------------------
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user`(`comment_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment_like
-- ----------------------------
INSERT INTO `comment_like` VALUES (1, 1, 4, '2026-03-21 00:18:00');

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `cat_id` bigint NOT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_cat`(`user_id`, `cat_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '猫咪收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (2, 2, 1, '2026-03-21 00:30:50');
INSERT INTO `favorite` VALUES (3, 2, 2, '2026-03-21 14:37:05');

-- ----------------------------
-- Table structure for follow_up_record
-- ----------------------------
DROP TABLE IF EXISTS `follow_up_record`;
CREATE TABLE `follow_up_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL COMMENT '关联领养申请ID',
  `cat_id` bigint NOT NULL COMMENT '猫咪ID',
  `user_id` bigint NOT NULL COMMENT '领养人ID',
  `health_status` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '健康状况描述',
  `photo_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '生活照片，逗号分隔',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `submit_type` tinyint NOT NULL DEFAULT 0 COMMENT '0-领养人主动提交 1-救助员代录',
  `volunteer_id` bigint NULL DEFAULT NULL COMMENT '救助员代录时的救助员ID',
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_application_id`(`application_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '领养后回访记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of follow_up_record
-- ----------------------------
INSERT INTO `follow_up_record` VALUES (1, 1, 1, 2, '良好', 'http://localhost:8080/uploads/f19d09c9f7054b20bab02a762040d7e8.png', '好', 0, NULL, '2026-03-20 23:48:07');
INSERT INTO `follow_up_record` VALUES (2, 1, 1, 2, '很好', '', '', 0, NULL, '2026-03-20 23:48:49');

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收通知的用户ID',
  `type` tinyint NOT NULL DEFAULT 0 COMMENT '通知大类: 0-系统通知 1-社区通知',
  `sub_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知子类型: APPLY_SUBMIT/APPLY_APPROVED/APPLY_REJECTED/SIGN_SUCCESS/COMMENT_LIKE/COMMENT_REPLY/POST_COMMENT',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知正文',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联业务ID（申请ID/帖子ID/评论ID）',
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_is_read`(`is_read`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (1, 2, 0, 'APPLY_SUBMIT', '领养申请已提交', '您对猫咪「测试狸花」的领养申请已提交，请耐心等待救助员审核~', 1, 3, '2026-03-20 23:55:52');
INSERT INTO `notification` VALUES (2, 2, 1, 'POST_COMMENT', '有人评论了你的帖子 📝', 'JR 评论道：好可爱！', 1, 1, '2026-03-21 00:17:57');
INSERT INTO `notification` VALUES (3, 2, 1, 'COMMENT_REPLY', '有人回复了你 💬', 'JR 回复了你：哈哈', 1, 1, '2026-03-21 00:18:04');
INSERT INTO `notification` VALUES (4, 2, 1, 'COMMENT_LIKE', '你的帖子被点赞了 ❤️', 'JR 赞了你的帖子', 1, 1, '2026-03-21 00:18:09');
INSERT INTO `notification` VALUES (5, 4, 0, 'APPLY_SUBMIT', '领养申请已提交', '您对猫咪「测试狸花」的领养申请已提交，请耐心等待救助员审核~', 1, 4, '2026-03-21 14:08:37');
INSERT INTO `notification` VALUES (6, 4, 0, 'APPLY_APPROVED', '🎉 领养申请已通过审核', '您对猫咪「测试狸花」的申请已通过审核！请前往「我的领养记录」签署领养协议。', 1, 4, '2026-03-21 14:09:25');
INSERT INTO `notification` VALUES (7, 4, 1, 'COMMENT_REPLY', '有人回复了你 💬', 'ZDD 回复了你：11', 1, 1, '2026-03-21 14:36:26');

-- ----------------------------
-- Table structure for pet_cat
-- ----------------------------
DROP TABLE IF EXISTS `pet_cat`;
CREATE TABLE `pet_cat`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ear_tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '耳标号(可为空)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '猫咪昵称',
  `breed` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品种',
  `age` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '年龄(如: 3个月, 1岁)',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别: 0-未知, 1-公, 2-母',
  `health_status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '健康状况(如: 已驱虫, 已绝育, 疫苗齐全)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '领养状态: 0-待领养, 1-已被申请, 2-已领养',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '猫咪详细描述/性格特点',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面照片URL',
  `photo_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '猫咪相册(多个图片URL以逗号分隔)',
  `creator_id` bigint NOT NULL COMMENT '建档人(救助员)ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '建档时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '猫咪电子档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_cat
-- ----------------------------
INSERT INTO `pet_cat` VALUES (1, 'A01', '小狸1', '狸花猫', '4个月', 2, '已驱虫，良好，已打疫苗', 2, '粘人', 'http://localhost:8080/uploads/a0c3219b6d5440069603de7665d2fed4.png', 'http://localhost:8080/uploads/a0c3219b6d5440069603de7665d2fed4.png', 3, '2026-03-20 22:20:55', '2026-03-20 23:05:29');
INSERT INTO `pet_cat` VALUES (2, 'A02', '测试狸花', '狸花猫', '1个月', 1, '测试的', 1, '测试', 'http://localhost:8080/uploads/3d38cf61e2c34a99a62a83dedc8c8acd.png', 'http://localhost:8080/uploads/3d38cf61e2c34a99a62a83dedc8c8acd.png', 3, '2026-03-20 23:00:16', '2026-03-20 23:00:16');
INSERT INTO `pet_cat` VALUES (3, 'A03', '小菊', '', '3个月', 1, '已绝育/良好', 0, '啊啊啊啊啊啊啊啊啊', 'http://localhost:8080/uploads/6d6e3a92e9254b7196f82e19507ce06f.png', 'http://localhost:8080/uploads/6d6e3a92e9254b7196f82e19507ce06f.png', 3, '2026-03-21 14:39:42', '2026-03-21 14:39:42');

-- ----------------------------
-- Table structure for pet_cat_record
-- ----------------------------
DROP TABLE IF EXISTS `pet_cat_record`;
CREATE TABLE `pet_cat_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cat_id` bigint NOT NULL COMMENT '关联的猫咪ID',
  `record_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '记录类型(如: 疫苗, 驱虫, 就医, 日常更新)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '记录详细内容',
  `media_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '照片或视频的URL(多个用逗号分隔)',
  `creator_id` bigint NOT NULL COMMENT '记录人(救助员)ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '记录时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '猫咪过程性数据履历表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_cat_record
-- ----------------------------
INSERT INTO `pet_cat_record` VALUES (1, 1, '疫苗/驱虫', '驱虫', 'http://localhost:8080/uploads/bcb2aefdf9864a46a82af51881821a4d.png', 3, '2026-03-20 22:45:32');
INSERT INTO `pet_cat_record` VALUES (2, 2, '疫苗/驱虫', '打疫苗', 'http://localhost:8080/uploads/bea1d6b641734241a792ade001e0f28e.png', 3, '2026-03-21 00:32:11');

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发帖人ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子内容',
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '配图，逗号分隔',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT 0 COMMENT '评论数',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post
-- ----------------------------
INSERT INTO `post` VALUES (1, 2, '大家好呀！！！', 'http://localhost:8080/uploads/909a6b77e40441ac9f1cd37cefb55e16.png', 2, 4, '2026-03-21 00:16:45', '2026-03-21 00:16:45');

-- ----------------------------
-- Table structure for post_like
-- ----------------------------
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_like
-- ----------------------------
INSERT INTO `post_like` VALUES (1, 1, 2, '2026-03-21 00:16:47');
INSERT INTO `post_like` VALUES (2, 1, 4, '2026-03-21 00:18:09');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号(可以是手机号)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录密码(MD5加密)',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '家庭住址',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` tinyint NOT NULL DEFAULT 1 COMMENT '角色: 1-领养人, 2-救助员/志愿者, 3-管理员',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '注册时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone`) USING BTREE,
  UNIQUE INDEX `uk_id_card`(`id_card`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户基础信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13800000000', NULL, '超级管理员', NULL, NULL, 3, '2026-03-20 21:40:50', '2026-03-20 21:53:39');
INSERT INTO `sys_user` VALUES (2, 'ZDD', 'e10adc3949ba59abbe56e057f20f883e', '15903852555', '410727200212121212', 'ZDDD', '', 'http://localhost:8080/uploads/727807339de84256992b5feae3bc3c4a.jpg', 1, '2026-03-20 21:42:58', '2026-03-21 14:37:29');
INSERT INTO `sys_user` VALUES (3, '张三', 'e10adc3949ba59abbe56e057f20f883e', '13811111111', '410722200101071514', NULL, NULL, NULL, 2, '2026-03-20 21:56:24', '2026-03-20 21:56:24');
INSERT INTO `sys_user` VALUES (4, 'JR', 'e10adc3949ba59abbe56e057f20f883e', '15903852598', '410727200102011234', '锦然', '', NULL, 1, '2026-03-21 00:17:34', '2026-03-21 14:40:32');
INSERT INTO `sys_user` VALUES (5, 'JJ', 'e10adc3949ba59abbe56e057f20f883e', '13222222222', '410727200101011234', NULL, NULL, NULL, 1, '2026-03-21 14:34:15', '2026-03-21 14:34:15');

SET FOREIGN_KEY_CHECKS = 1;
