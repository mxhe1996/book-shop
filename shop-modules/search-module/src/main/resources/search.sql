/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.120.149虚拟机
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : 192.168.120.149:3306
 Source Schema         : search

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 12/07/2024 05:40:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for search_processed
-- ----------------------------
DROP TABLE IF EXISTS `search_processed`;
CREATE TABLE `search_processed`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `max_event_id` int(0) NULL DEFAULT NULL,
  `event_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `aggregate_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `aggregate_id` int(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
