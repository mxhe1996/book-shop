/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.120.149虚拟机
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : 192.168.120.149:3306
 Source Schema         : order

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 26/07/2024 07:37:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for order_info
-- ----------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info`  (
  `order_id` int(0) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `items_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `money` int(0) NULL DEFAULT NULL,
  `user_id` int(0) NULL DEFAULT NULL,
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  `order_status` smallint(0) NULL DEFAULT NULL,
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_processed
-- ----------------------------
DROP TABLE IF EXISTS `order_processed`;
CREATE TABLE `order_processed`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `event_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `event_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `aggregate_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `aggregate_id` int(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `event_id`(`event_id`(7)) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for outbox
-- ----------------------------
DROP TABLE IF EXISTS `outbox`;
CREATE TABLE `outbox`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `topic_name` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `event_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
