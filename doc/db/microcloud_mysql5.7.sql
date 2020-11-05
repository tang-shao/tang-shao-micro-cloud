DROP DATABASE IF EXISTS `micro-cloud`;
create database `micro-cloud` default character set utf8mb4 collate utf8mb4_general_ci;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `micro-cloud`;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键id',
  `username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录账号',
  `realname` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `birthday` datetime(0) NULL DEFAULT NULL COMMENT '生日',
  `sex` tinyint(1) NULL DEFAULT NULL COMMENT '性别(0-默认未知,1-男,2-女)',
  `email` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '电子邮件',
  `phone` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '电话',
  `org_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '机构编码',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '性别(1-正常,2-冻结)',
  `del_flag` tinyint(1) NULL DEFAULT NULL COMMENT '删除状态(0-正常,1-已删除)',
  `third_id` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第三方登录的唯一标识',
  `third_type` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第三方类型',
  `activiti_sync` tinyint(1) NULL DEFAULT NULL COMMENT '同步工作流引擎(1-同步,0-不同步)',
  `work_no` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '工号，唯一键',
  `post` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '职务，关联职务表',
  `telephone` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '座机号',
  `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `user_identity` tinyint(1) NULL DEFAULT NULL COMMENT '身份（1普通成员 2上级）',
  `depart_ids` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '负责部门',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `index_user_name`(`username`) USING BTREE,
  UNIQUE INDEX `uniq_sys_user_work_no`(`work_no`) USING BTREE,
  UNIQUE INDEX `uniq_sys_user_username`(`username`) USING BTREE,
  UNIQUE INDEX `uniq_sys_user_phone`(`phone`) USING BTREE,
  UNIQUE INDEX `uniq_sys_user_email`(`email`) USING BTREE,
  INDEX `index_user_status`(`status`) USING BTREE,
  INDEX `index_user_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

INSERT INTO `sys_user` VALUES ('42d153bffeea74f72a9c1697874fa4a7', 'test22', '23232', 'uaaIHVXKz5plwekhq_yK6w', 'user/20190314/ly-plate-e_1552531617500.png', '2019-2-9 00:00:00', 1, 'zhangdaiscott@163.com', '18611782222', NULL, 1, 1, NULL, NULL, 1, NULL, NULL, NULL, 'admin', '2019-1-26 18:01:10', 'admin', '2019-3-23 15:05:50', NULL, NULL);
INSERT INTO `sys_user` VALUES ('a75d45a015c44384a04449ee80dc3503', 'jeecg', 'jeecg', 'uaaIHVXKz5plwekhq_yK6w', 'user/20190220/e1fe9925bc315c60addea1b98eb1cb1349547719_1550656892940.jpg', NULL, 1, NULL, NULL, '财务部', 1, 0, NULL, NULL, 1, '00002', 'devleader', NULL, 'admin', '2019-2-13 16:02:36', 'admin', '2020-5-2 15:34:30', 1, '');
INSERT INTO `sys_user` VALUES ('e9ca23d68d884d4ebb19d07889727dae', 'admin', '管理员', 'uaaIHVXKz5plwekhq_yK6w', 'http://minio.jeecg.com/otatest/temp/lgo33_1583397323099.png', '2018-12-5 00:00:00', 1, 'jeecg@163.com', '18611111111', 'A01', 1, 0, NULL, NULL, 1, '00001', '总经理', NULL, NULL, '2038-6-21 17:54:10', 'admin', '2020-5-2 18:19:41', 2, 'c6d7cb4deeac411cb3384b1b31278596');
INSERT INTO `sys_user` VALUES ('f0019fdebedb443c98dcb17d88222c38', 'zhagnxiao', '张小红', 'uaaIHVXKz5plwekhq_yK6w', 'user/20190401/20180607175028Fn1Lq7zw_1554118444672.png', '2019-4-1 00:00:00', NULL, NULL, NULL, '研发部,财务部', 1, 0, NULL, NULL, 1, '00003', '', NULL, 'admin', '2023-10-1 19:34:10', 'admin', '2020-5-2 15:34:51', 1, '');



-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `log_type` int(2) NULL DEFAULT NULL COMMENT '日志类型（1登录日志，2操作日志）',
  `log_content` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '日志内容',
  `operate_type` int(2) NULL DEFAULT NULL COMMENT '操作类型',
  `userid` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '操作用户账号',
  `username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '操作用户名称',
  `ip` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'IP',
  `method` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求java方法',
  `request_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求路径',
  `request_param` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '请求参数',
  `request_type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求类型',
  `cost_time` bigint(20) NULL DEFAULT NULL COMMENT '耗时',
  `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_table_userid`(`userid`) USING BTREE,
  INDEX `index_logt_ype`(`log_type`) USING BTREE,
  INDEX `index_operate_type`(`operate_type`) USING BTREE,
  INDEX `index_log_type`(`log_type`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '系统日志表' ROW_FORMAT = Dynamic;




