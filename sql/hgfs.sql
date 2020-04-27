/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 50624
 Source Host           : localhost
 Source Database       : fastdfs

 Target Server Type    : MySQL
 Target Server Version : 50624
 File Encoding         : utf-8

 Date: 01/16/2020 11:15:54 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `tb_app`
-- ----------------------------
DROP TABLE IF EXISTS `tb_app`;
CREATE TABLE `tb_app` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app_no` varchar(42) NOT NULL,
  `app_desc` varchar(255) DEFAULT NULL,
  `file_type` enum('LOCAL','FTP','FASTDFS') DEFAULT 'FASTDFS',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
--  Table structure for `tb_file`
-- ----------------------------
DROP TABLE IF EXISTS `tb_file`;
CREATE TABLE `tb_file` (
  `id` bigint(18) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `size` bigint(20) DEFAULT NULL,
  `md5` varchar(255) DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `block` bit(1) DEFAULT NULL,
  `last_ver` varchar(30) DEFAULT NULL,
  `record` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
--  Table structure for `tb_profession`
-- ----------------------------
DROP TABLE IF EXISTS `tb_profession`;
CREATE TABLE `tb_profession` (
  `id` bigint(18) NOT NULL AUTO_INCREMENT,
  `app_no` varchar(42) DEFAULT NULL,
  `app_file_id` varchar(42) DEFAULT NULL,
  `file_id` bigint(18) DEFAULT NULL,
  `app_ip` varchar(255) DEFAULT NULL,
  `last_ver` varchar(30) DEFAULT NULL,
  `pipe` varchar(255) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `file_id` (`file_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

SET FOREIGN_KEY_CHECKS = 1;
