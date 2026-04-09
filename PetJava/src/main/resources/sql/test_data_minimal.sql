-- =====================================================
-- 宠物寄养平台测试数据 - 仅保留管理员账号
-- =====================================================

USE pet_foster;

-- =====================================================
-- 清空旧数据（保留表结构）
-- =====================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `wallet_transaction`;
TRUNCATE TABLE `wallet`;
TRUNCATE TABLE `review`;
TRUNCATE TABLE `favorite`;
TRUNCATE TABLE `booking`;
TRUNCATE TABLE `service_package`;
TRUNCATE TABLE `pet`;
TRUNCATE TABLE `institution`;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 1. 用户数据（仅保留管理员）
-- =====================================================
INSERT INTO `user` (id, email, phone, password, name, avatar, gender, birthday, address, role, admin_level, institution_id, position, permissions, status, created_at, updated_at) VALUES
('admin-001', 'admin@petpro.com', '13800000000', '$2a$10$aitvkPRe9s9NSdtgIfvIveg1CUh8zyKjn33bwIRMYfkEtPhQUQLTm', '系统管理员', NULL, NULL, NULL, NULL, 'admin', 'super', NULL, NULL, NULL, 'active', NOW(), NOW());
