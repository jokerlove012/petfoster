-- 创建数据库
CREATE DATABASE IF NOT EXISTS pet_foster DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_foster;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` VARCHAR(36) PRIMARY KEY,
    `email` VARCHAR(100),
    `phone` VARCHAR(20) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `avatar` VARCHAR(500),
    `gender` VARCHAR(10) COMMENT 'male, female',
    `birthday` DATE,
    `address` VARCHAR(200),
    `role` VARCHAR(20) NOT NULL COMMENT 'pet_owner, institution_staff, admin',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT 'active, banned',
    `admin_level` VARCHAR(20) COMMENT 'super, normal',
    `institution_id` VARCHAR(36) COMMENT '机构ID(员工)',
    `position` VARCHAR(20) COMMENT 'manager, caretaker, receptionist',
    `permissions` JSON COMMENT '权限列表',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_phone_role` (`phone`, `role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 宠物表
CREATE TABLE IF NOT EXISTS `pet` (
    `id` VARCHAR(36) PRIMARY KEY,
    `user_id` VARCHAR(36) NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `species` VARCHAR(20) NOT NULL COMMENT 'dog, cat, other',
    `breed` VARCHAR(50),
    `age` INT,
    `weight` DECIMAL(5,2),
    `gender` VARCHAR(10) COMMENT 'male, female',
    `avatar` VARCHAR(500),
    `health_notes` TEXT,
    `vaccinated` TINYINT DEFAULT 0,
    `neutered` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 地址表
CREATE TABLE IF NOT EXISTS `address` (
    `id` VARCHAR(36) PRIMARY KEY,
    `user_id` VARCHAR(36) NOT NULL,
    `label` VARCHAR(50),
    `address` VARCHAR(200) NOT NULL,
    `latitude` DECIMAL(10,7),
    `longitude` DECIMAL(10,7),
    `is_default` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 机构表
CREATE TABLE IF NOT EXISTS `institution` (
    `id` VARCHAR(36) PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `logo` VARCHAR(500),
    `images` JSON,
    `address` VARCHAR(200) NOT NULL,
    `latitude` DECIMAL(10,7),
    `longitude` DECIMAL(10,7),
    `phone` VARCHAR(20),
    `email` VARCHAR(100),
    `business_hours` JSON,
    `pet_types` JSON COMMENT '["dog","cat","other"]',
    `capacity` JSON COMMENT '{"dog":20,"cat":15}',
    `current_occupancy` JSON,
    `rating` DECIMAL(2,1) DEFAULT 0,
    `review_count` INT DEFAULT 0,
    `verified` TINYINT DEFAULT 0,
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT 'pending, active, suspended, rejected',
    `features` JSON,
    `licenses` JSON,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_status` (`status`),
    INDEX `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 服务套餐表
CREATE TABLE IF NOT EXISTS `service_package` (
    `id` VARCHAR(36) PRIMARY KEY,
    `institution_id` VARCHAR(36) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `pet_types` JSON,
    `price_per_day` DECIMAL(10,2) NOT NULL,
    `features` JSON,
    `max_weight` DECIMAL(5,2),
    `is_active` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_institution_id` (`institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预约订单表
CREATE TABLE IF NOT EXISTS `booking` (
    `id` VARCHAR(36) PRIMARY KEY,
    `order_number` VARCHAR(20) NOT NULL UNIQUE,
    `user_id` VARCHAR(36) NOT NULL,
    `institution_id` VARCHAR(36) NOT NULL,
    `service_package_id` VARCHAR(36) NOT NULL,
    `pet_id` VARCHAR(36) NOT NULL,
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT 'pending, confirmed, in_progress, completed, cancelled',
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `check_in_time` DATETIME,
    `check_out_time` DATETIME,
    `total_days` INT NOT NULL,
    `base_price` DECIMAL(10,2) NOT NULL,
    `discount` DECIMAL(10,2) DEFAULT 0,
    `total_price` DECIMAL(10,2) NOT NULL,
    `payment_status` VARCHAR(20) DEFAULT 'pending' COMMENT 'pending, paid, refunded, partial_refund',
    `payment_method` VARCHAR(20) COMMENT 'wechat, alipay, card',
    `paid_at` DATETIME,
    `refund_amount` DECIMAL(10,2),
    `refunded_at` DATETIME,
    `cancel_reason` VARCHAR(500),
    `special_requirements` TEXT,
    `emergency_contact` JSON,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_institution_id` (`institution_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 健康记录表
CREATE TABLE IF NOT EXISTS `health_record` (
    `id` VARCHAR(36) PRIMARY KEY,
    `booking_id` VARCHAR(36) NOT NULL,
    `date` DATE NOT NULL,
    `feeding_status` VARCHAR(20) COMMENT 'normal, reduced, increased, refused',
    `activity_level` VARCHAR(20) COMMENT 'high, normal, low, inactive',
    `health_observations` TEXT,
    `mood` VARCHAR(20) COMMENT 'happy, calm, anxious, stressed',
    `weight` DECIMAL(5,2),
    `temperature` DECIMAL(4,1),
    `medications` JSON,
    `photos` JSON,
    `videos` JSON,
    `is_abnormal` TINYINT DEFAULT 0,
    `abnormal_details` TEXT,
    `created_by` VARCHAR(36),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_booking_id` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评价表
CREATE TABLE IF NOT EXISTS `review` (
    `id` VARCHAR(36) PRIMARY KEY,
    `booking_id` VARCHAR(36) NOT NULL,
    `user_id` VARCHAR(36) NOT NULL,
    `institution_id` VARCHAR(36) NOT NULL,
    `rating` JSON NOT NULL COMMENT '{"overall":5,"environment":5,"service":5,"hygiene":5,"communication":5}',
    `content` TEXT NOT NULL,
    `images` JSON,
    `reply` JSON COMMENT '{"content":"...","repliedAt":"..."}',
    `is_anonymous` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_institution_id` (`institution_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` VARCHAR(36) PRIMARY KEY,
    `user_id` VARCHAR(36) NOT NULL,
    `institution_id` VARCHAR(36) NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_institution` (`user_id`, `institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 投诉表
CREATE TABLE IF NOT EXISTS `complaint` (
    `id` VARCHAR(36) PRIMARY KEY,
    `complaint_number` VARCHAR(30) NOT NULL UNIQUE COMMENT '投诉编号',
    `category` VARCHAR(30) NOT NULL COMMENT '投诉类型: service_quality, pet_safety, communication, facility, price, other',
    `user_id` VARCHAR(36) NOT NULL COMMENT '投诉人ID',
    `institution_id` VARCHAR(36) NOT NULL COMMENT '被投诉机构ID',
    `booking_id` VARCHAR(36) COMMENT '相关订单ID',
    `description` TEXT NOT NULL COMMENT '投诉内容',
    `evidence` JSON COMMENT '证据材料',
    `expectation` TEXT COMMENT '期望处理',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending, awaiting_response, under_review, resolved',
    `institution_response` TEXT COMMENT '机构回复',
    `resolution` JSON COMMENT '处理结果: {decision, remedies, resolvedBy, resolvedAt}',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_institution_id` (`institution_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 钱包表
CREATE TABLE IF NOT EXISTS `wallet` (
    `id` VARCHAR(36) PRIMARY KEY,
    `user_id` VARCHAR(36) NOT NULL UNIQUE COMMENT '用户ID',
    `user_type` VARCHAR(20) NOT NULL COMMENT '用户类型: pet_owner, institution_staff',
    `balance` INT DEFAULT 0 COMMENT '可用余额（单位：分）',
    `frozen_balance` INT DEFAULT 0 COMMENT '冻结余额（单位：分）',
    `total_income` INT DEFAULT 0 COMMENT '累计收入（单位：分）',
    `total_withdraw` INT DEFAULT 0 COMMENT '累计提现（单位：分）',
    `withdraw_password` VARCHAR(100) COMMENT '提现密码',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态: active, frozen',
    `daily_withdraw_amount` INT DEFAULT 0 COMMENT '今日已提现金额（单位：分）',
    `daily_withdraw_count` INT DEFAULT 0 COMMENT '今日已提现次数',
    `last_withdraw_date` DATE COMMENT '最后提现日期',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 钱包交易记录表
CREATE TABLE IF NOT EXISTS `wallet_transaction` (
    `id` VARCHAR(36) PRIMARY KEY,
    `wallet_id` VARCHAR(36) NOT NULL COMMENT '钱包ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '交易类型: recharge, withdrawal, income, payment, refund',
    `amount` INT NOT NULL COMMENT '金额（单位：分）',
    `fee` INT DEFAULT 0 COMMENT '手续费（单位：分）',
    `balance_before` INT NOT NULL COMMENT '交易前余额',
    `balance_after` INT NOT NULL COMMENT '交易后余额',
    `status` VARCHAR(20) DEFAULT 'success' COMMENT '状态: pending, success, failed',
    `description` VARCHAR(500) COMMENT '描述',
    `related_order_id` VARCHAR(36) COMMENT '关联订单ID',
    `related_withdrawal_id` VARCHAR(36) COMMENT '关联提现ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_wallet_id` (`wallet_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 充值订单表
CREATE TABLE IF NOT EXISTS `recharge_order` (
    `id` VARCHAR(36) PRIMARY KEY,
    `wallet_id` VARCHAR(36) NOT NULL COMMENT '钱包ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `amount` INT NOT NULL COMMENT '充值金额（单位：分）',
    `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式: alipay, wechat',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending, paid, failed, expired',
    `payment_order_id` VARCHAR(100) COMMENT '第三方支付订单号',
    `paid_at` DATETIME COMMENT '支付时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `expired_at` DATETIME COMMENT '过期时间',
    INDEX `idx_wallet_id` (`wallet_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 提现申请表
CREATE TABLE IF NOT EXISTS `withdrawal` (
    `id` VARCHAR(36) PRIMARY KEY,
    `wallet_id` VARCHAR(36) NOT NULL COMMENT '钱包ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `amount` INT NOT NULL COMMENT '提现金额（单位：分）',
    `fee` INT NOT NULL COMMENT '手续费（单位：分）',
    `actual_amount` INT NOT NULL COMMENT '实际到账金额（单位：分）',
    `account_id` VARCHAR(36) NOT NULL COMMENT '提现账户ID',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending, processing, success, failed, cancelled',
    `audit_remark` VARCHAR(500) COMMENT '审核备注',
    `audited_at` DATETIME COMMENT '审核时间',
    `audited_by` VARCHAR(36) COMMENT '审核人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_wallet_id` (`wallet_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 提现账户表
CREATE TABLE IF NOT EXISTS `withdrawal_account` (
    `id` VARCHAR(36) PRIMARY KEY,
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '账户类型: bank, alipay',
    `account_name` VARCHAR(100) NOT NULL COMMENT '账户姓名',
    `account_number` VARCHAR(100) NOT NULL COMMENT '账户号码',
    `bank_name` VARCHAR(100) COMMENT '银行名称（仅银行卡）',
    `bank_branch` VARCHAR(100) COMMENT '开户支行（仅银行卡）',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认账户',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 钱包操作审计日志表
CREATE TABLE IF NOT EXISTS `wallet_audit_log` (
    `id` VARCHAR(36) PRIMARY KEY,
    `wallet_id` VARCHAR(36) NOT NULL COMMENT '钱包ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '操作人ID',
    `operation` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `details` JSON COMMENT '操作详情',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_wallet_id` (`wallet_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `id` VARCHAR(36) PRIMARY KEY,
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: booking, payment, health, review, system, message',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT COMMENT '内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
    `link` VARCHAR(500) COMMENT '链接',
    `data` JSON COMMENT '额外数据',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
