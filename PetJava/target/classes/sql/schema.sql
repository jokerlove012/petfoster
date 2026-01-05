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
    `role` VARCHAR(20) NOT NULL COMMENT 'pet_owner, institution_staff, admin',
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
