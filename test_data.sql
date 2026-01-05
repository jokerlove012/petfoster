-- =====================================================
-- 宠物寄养平台测试数据 - 管理员仪表盘测试
-- =====================================================

-- 8月订单
INSERT INTO booking (id, order_number, user_id, institution_id, service_package_id, pet_id, status, start_date, end_date, total_days, base_price, total_price, payment_status, payment_method, paid_at, created_at, updated_at) VALUES
('bk-aug-1', 'PF202508001', '369fa50e5db32db33518d3cbfc4c805f', 'inst-1', 'pkg-1', 'pet-1', 'completed', '2025-08-01', '2025-08-05', 4, 88.00, 352.00, 'paid', 'wechat', '2025-08-01 10:00:00', '2025-08-01 09:00:00', '2025-08-05 18:00:00'),
('bk-aug-2', 'PF202508002', 'owner-1', 'inst-2', 'pkg-2', 'pet-4', 'completed', '2025-08-10', '2025-08-15', 5, 68.00, 340.00, 'paid', 'alipay', '2025-08-10 09:00:00', '2025-08-10 08:00:00', '2025-08-15 18:00:00'),
('bk-aug-3', 'PF202508003', 'owner-2', 'inst-3', 'pkg-3', 'pet-6', 'completed', '2025-08-20', '2025-08-25', 5, 128.00, 640.00, 'paid', 'wechat', '2025-08-20 10:00:00', '2025-08-20 09:00:00', '2025-08-25 18:00:00')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 9月订单
INSERT INTO booking (id, order_number, user_id, institution_id, service_package_id, pet_id, status, start_date, end_date, total_days, base_price, total_price, payment_status, payment_method, paid_at, created_at, updated_at) VALUES
('bk-sep-1', 'PF202509001', '369fa50e5db32db33518d3cbfc4c805f', 'inst-1', 'pkg-1', 'pet-1', 'completed', '2025-09-01', '2025-09-07', 6, 88.00, 528.00, 'paid', 'wechat', '2025-09-01 10:00:00', '2025-09-01 09:00:00', '2025-09-07 18:00:00'),
('bk-sep-2', 'PF202509002', 'owner-1', 'inst-3', 'pkg-3', 'pet-5', 'completed', '2025-09-10', '2025-09-14', 4, 128.00, 512.00, 'paid', 'alipay', '2025-09-10 09:00:00', '2025-09-10 08:00:00', '2025-09-14 18:00:00'),
('bk-sep-3', 'PF202509003', 'owner-2', 'inst-5', 'pkg-6', 'pet-6', 'completed', '2025-09-15', '2025-09-20', 5, 78.00, 390.00, 'paid', 'wechat', '2025-09-15 10:00:00', '2025-09-15 09:00:00', '2025-09-20 18:00:00'),
('bk-sep-4', 'PF202509004', 'owner-3', 'inst-2', 'pkg-2', 'pet-7', 'cancelled', '2025-09-25', '2025-09-30', 5, 68.00, 340.00, 'refunded', 'wechat', '2025-09-25 10:00:00', '2025-09-25 09:00:00', '2025-09-26 10:00:00')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 10月订单
INSERT INTO booking (id, order_number, user_id, institution_id, service_package_id, pet_id, status, start_date, end_date, total_days, base_price, total_price, payment_status, payment_method, paid_at, created_at, updated_at) VALUES
('bk-oct-1', 'PF202510001', '369fa50e5db32db33518d3cbfc4c805f', 'inst-1', 'pkg-1', 'pet-1', 'completed', '2025-10-01', '2025-10-07', 6, 88.00, 528.00, 'paid', 'wechat', '2025-10-01 10:00:00', '2025-10-01 09:00:00', '2025-10-07 18:00:00'),
('bk-oct-2', 'PF202510002', 'owner-1', 'inst-2', 'pkg-2', 'pet-4', 'completed', '2025-10-08', '2025-10-12', 4, 68.00, 272.00, 'paid', 'alipay', '2025-10-08 09:00:00', '2025-10-08 08:00:00', '2025-10-12 18:00:00'),
('bk-oct-3', 'PF202510003', 'owner-2', 'inst-3', 'pkg-3', 'pet-6', 'completed', '2025-10-15', '2025-10-20', 5, 128.00, 640.00, 'paid', 'wechat', '2025-10-15 10:00:00', '2025-10-15 09:00:00', '2025-10-20 18:00:00'),
('bk-oct-4', 'PF202510004', 'owner-3', 'inst-5', 'pkg-6', 'pet-7', 'completed', '2025-10-22', '2025-10-28', 6, 78.00, 468.00, 'paid', 'alipay', '2025-10-22 10:00:00', '2025-10-22 09:00:00', '2025-10-28 18:00:00')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 11月订单
INSERT INTO booking (id, order_number, user_id, institution_id, service_package_id, pet_id, status, start_date, end_date, total_days, base_price, total_price, payment_status, payment_method, paid_at, created_at, updated_at) VALUES
('bk-nov-1', 'PF202511001', '369fa50e5db32db33518d3cbfc4c805f', 'inst-1', 'pkg-1', 'pet-1', 'completed', '2025-11-01', '2025-11-05', 4, 88.00, 352.00, 'paid', 'wechat', '2025-11-01 10:00:00', '2025-11-01 09:00:00', '2025-11-05 18:00:00'),
('bk-nov-2', 'PF202511002', 'owner-1', 'inst-3', 'pkg-3', 'pet-5', 'completed', '2025-11-10', '2025-11-15', 5, 128.00, 640.00, 'paid', 'alipay', '2025-11-10 09:00:00', '2025-11-10 08:00:00', '2025-11-15 18:00:00'),
('bk-nov-3', 'PF202511003', 'owner-2', 'inst-2', 'pkg-2', 'pet-6', 'completed', '2025-11-18', '2025-11-22', 4, 68.00, 272.00, 'paid', 'wechat', '2025-11-18 10:00:00', '2025-11-18 09:00:00', '2025-11-22 18:00:00'),
('bk-nov-4', 'PF202511004', 'owner-3', 'inst-5', 'pkg-6', 'pet-7', 'completed', '2025-11-25', '2025-11-30', 5, 78.00, 390.00, 'paid', 'alipay', '2025-11-25 10:00:00', '2025-11-25 09:00:00', '2025-11-30 18:00:00')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 12月订单
INSERT INTO booking (id, order_number, user_id, institution_id, service_package_id, pet_id, status, start_date, end_date, total_days, base_price, total_price, payment_status, payment_method, paid_at, created_at, updated_at) VALUES
('bk-dec-1', 'PF202512001', '369fa50e5db32db33518d3cbfc4c805f', 'inst-1', 'pkg-1', 'pet-1', 'completed', '2025-12-01', '2025-12-05', 4, 88.00, 352.00, 'paid', 'wechat', '2025-12-01 10:00:00', '2025-12-01 09:00:00', '2025-12-05 18:00:00'),
('bk-dec-2', 'PF202512002', 'owner-1', 'inst-2', 'pkg-2', 'pet-4', 'completed', '2025-12-08', '2025-12-12', 4, 68.00, 272.00, 'paid', 'alipay', '2025-12-08 09:00:00', '2025-12-08 08:00:00', '2025-12-12 18:00:00'),
('bk-dec-3', 'PF202512003', 'owner-2', 'inst-3', 'pkg-3', 'pet-6', 'completed', '2025-12-15', '2025-12-20', 5, 128.00, 640.00, 'paid', 'wechat', '2025-12-15 10:00:00', '2025-12-15 09:00:00', '2025-12-20 18:00:00'),
('bk-dec-4', 'PF202512004', 'owner-3', 'inst-5', 'pkg-6', 'pet-7', 'completed', '2025-12-22', '2025-12-28', 6, 78.00, 468.00, 'paid', 'alipay', '2025-12-22 10:00:00', '2025-12-22 09:00:00', '2025-12-28 18:00:00')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 1月订单（当前月）
INSERT INTO booking (id, order_number, user_id, institution_id, service_package_id, pet_id, status, start_date, end_date, total_days, base_price, total_price, payment_status, payment_method, paid_at, created_at, updated_at) VALUES
('bk-jan-1', 'PF202601001', '369fa50e5db32db33518d3cbfc4c805f', 'inst-1', 'pkg-1', 'pet-1', 'in_progress', '2026-01-02', '2026-01-08', 6, 88.00, 528.00, 'paid', 'wechat', '2026-01-02 10:00:00', '2026-01-02 09:00:00', '2026-01-02 10:00:00'),
('bk-jan-2', 'PF202601002', 'owner-1', 'inst-3', 'pkg-3', 'pet-5', 'confirmed', '2026-01-05', '2026-01-10', 5, 128.00, 640.00, 'paid', 'alipay', '2026-01-04 09:00:00', '2026-01-04 08:00:00', '2026-01-04 10:00:00'),
('bk-jan-3', 'PF202601003', 'owner-2', 'inst-2', 'pkg-2', 'pet-6', 'pending', '2026-01-06', '2026-01-10', 4, 68.00, 272.00, 'pending', NULL, NULL, '2026-01-04 10:00:00', '2026-01-04 10:00:00'),
('bk-jan-4', 'PF202601004', 'owner-3', 'inst-5', 'pkg-6', 'pet-7', 'pending', '2026-01-08', '2026-01-12', 4, 78.00, 312.00, 'pending', NULL, NULL, '2026-01-04 11:00:00', '2026-01-04 11:00:00')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 添加待审核机构
INSERT INTO institution (id, name, description, address, latitude, longitude, phone, status, verified, rating, review_count, created_at, updated_at) VALUES
('inst-p1', '萌宠之家', '专业宠物寄养服务', '广州市海珠区新港西路135号', 23.0989, 113.3215, '13800001111', 'pending', 0, 0, 0, NOW(), NOW()),
('inst-p2', '爱宠乐园', '高端宠物酒店', '广州市越秀区环市东路339号', 23.1356, 113.2789, '13800002222', 'pending', 0, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 更新机构评分
UPDATE institution SET rating = 4.8, review_count = 25 WHERE id = 'b710e5149d49a230b01c14462a5478d6';

-- 清空旧通知，添加新通知
DELETE FROM notification;
INSERT INTO notification (id, user_id, type, title, content, link, is_read, created_at) VALUES
(REPLACE(UUID(),'-',''), '369fa50e5db32db33518d3cbfc4c805f', 'booking', '订单进行中', '您的订单 PF202601001 宠物已入住', '/order/bk-jan-1', 0, NOW()),
(REPLACE(UUID(),'-',''), '369fa50e5db32db33518d3cbfc4c805f', 'health', '健康记录更新', '小白 今日健康记录已更新', '/order/bk-jan-1', 0, NOW()),
(REPLACE(UUID(),'-',''), 'owner-1', 'booking', '订单已确认', '您的订单 PF202601002 已被机构确认', '/order/bk-jan-2', 0, NOW()),
(REPLACE(UUID(),'-',''), 'owner-2', 'booking', '订单待确认', '您的订单 PF202601003 等待机构确认', '/order/bk-jan-3', 0, NOW()),
(REPLACE(UUID(),'-',''), '9af627b9a71e2ab2c169762d59b048e8', 'booking', '新订单通知', '收到新订单 PF202601003', '/institution/orders', 0, NOW()),
(REPLACE(UUID(),'-',''), 'admin-1', 'system', '机构入驻申请', '萌宠之家 申请入驻平台', '/admin/institutions/review', 0, NOW()),
(REPLACE(UUID(),'-',''), 'admin-1', 'system', '机构入驻申请', '爱宠乐园 申请入驻平台', '/admin/institutions/review', 0, NOW());
