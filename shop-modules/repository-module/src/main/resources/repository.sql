/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.120.149虚拟机
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : 192.168.120.149:3306
 Source Schema         : repository

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 26/07/2024 07:37:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book
-- ----------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book`  (
  `book_id` int(0) NOT NULL AUTO_INCREMENT,
  `book_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `book_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `price` int(0) NULL DEFAULT NULL,
  `stock` int(0) NULL DEFAULT NULL,
  `tenant_id` int(0) NULL DEFAULT NULL,
  `tenant_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`book_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repository_processed
-- ----------------------------
DROP TABLE IF EXISTS `repository_processed`;
CREATE TABLE `repository_processed`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `event_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `event_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `aggregate_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `aggregate_id` int(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `event_id`(`event_id`(7)) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
