-- ============================================================
-- SmartHub 词云功能 — 建表 + 种子数据 + 菜单配置
-- ============================================================
USE `smarthub`;

-- 词云热词表
DROP TABLE IF EXISTS `sys_word_cloud`;
CREATE TABLE `sys_word_cloud` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `word` VARCHAR(50) NOT NULL COMMENT '词语',
    `category` VARCHAR(30) NOT NULL COMMENT '分类（科技/娱乐/体育/财经）',
    `popularity` INT NOT NULL DEFAULT 50 COMMENT '热度（0-100）',
    `color` VARCHAR(20) DEFAULT NULL COMMENT '自定义颜色（可选）',
    `source` TINYINT NOT NULL DEFAULT 0 COMMENT '来源（0=手动 1=AI提取）',
    `source_text` TEXT DEFAULT NULL COMMENT '来源文本（AI提取时记录原文）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0=禁用 1=启用）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word_category` (`word`, `category`, `deleted`),
    KEY `idx_category_status` (`category`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词云热词表';

-- 种子数据：科技领域
INSERT INTO `sys_word_cloud` (`id`, `word`, `category`, `popularity`, `color`, `source`, `sort_order`) VALUES
(1001, '人工智能', '科技', 95, '#FF6B6B', 0, 1),
(1002, '大模型', '科技', 92, '#4ECDC4', 0, 2),
(1003, '区块链', '科技', 88, '#45B7D1', 0, 3),
(1004, '云计算', '科技', 85, '#96CEB4', 0, 4),
(1005, '物联网', '科技', 82, '#FFEAA7', 0, 5),
(1006, '量子计算', '科技', 78, '#DDA0DD', 0, 6),
(1007, '数字孪生', '科技', 75, '#98D8C8', 0, 7),
(1008, '边缘计算', '科技', 72, '#F7DC6F', 0, 8);

-- 种子数据：娱乐领域
INSERT INTO `sys_word_cloud` (`id`, `word`, `category`, `popularity`, `color`, `source`, `sort_order`) VALUES
(2001, '电影票房', '娱乐', 90, '#FF6B6B', 0, 1),
(2002, '综艺节目', '娱乐', 87, '#4ECDC4', 0, 2),
(2003, '音乐排行榜', '娱乐', 84, '#45B7D1', 0, 3),
(2004, '游戏电竞', '娱乐', 81, '#96CEB4', 0, 4),
(2005, '短视频', '娱乐', 78, '#FFEAA7', 0, 5),
(2006, '明星八卦', '娱乐', 75, '#DDA0DD', 0, 6),
(2007, '动漫二次元', '娱乐', 72, '#98D8C8', 0, 7),
(2008, '直播打赏', '娱乐', 69, '#F7DC6F', 0, 8);

-- 种子数据：体育领域
INSERT INTO `sys_word_cloud` (`id`, `word`, `category`, `popularity`, `color`, `source`, `sort_order`) VALUES
(3001, '世界杯', '体育', 93, '#FF6B6B', 0, 1),
(3002, 'NBA', '体育', 90, '#4ECDC4', 0, 2),
(3003, '足球转会', '体育', 87, '#45B7D1', 0, 3),
(3004, '奥运会', '体育', 84, '#96CEB4', 0, 4),
(3005, '马拉松', '体育', 81, '#FFEAA7', 0, 5),
(3006, '篮球季后赛', '体育', 78, '#DDA0DD', 0, 6),
(3007, '网球大满贯', '体育', 75, '#98D8C8', 0, 7),
(3008, '电竞比赛', '体育', 72, '#F7DC6F', 0, 8);

-- 种子数据：财经领域
INSERT INTO `sys_word_cloud` (`id`, `word`, `category`, `popularity`, `color`, `source`, `sort_order`) VALUES
(4001, '股票市场', '财经', 94, '#FF6B6B', 0, 1),
(4002, '加密货币', '财经', 91, '#4ECDC4', 0, 2),
(4003, '基金投资', '财经', 88, '#45B7D1', 0, 3),
(4004, '房地产', '财经', 85, '#96CEB4', 0, 4),
(4005, '通货膨胀', '财经', 82, '#FFEAA7', 0, 5),
(4006, '利率调整', '财经', 79, '#DDA0DD', 0, 6),
(4007, 'IPO上市', '财经', 76, '#98D8C8', 0, 7),
(4008, '跨境电商', '财经', 73, '#F7DC6F', 0, 8);

-- 菜单配置：词云中心
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(80, 0, 1, '词云中心', '/wordcloud', NULL, 'collection', 8, '', 1),
(81, 80, 2, '词云展示', '/wordcloud', 'cloud/WordCloudView', 'chart', 1, 'sys:cloud:list', 1);

-- 词云管理按钮权限
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(800, 81, 3, '新增热词', '', '', '', 1, 'sys:cloud:add', 1),
(801, 81, 3, '编辑热词', '', '', '', 2, 'sys:cloud:edit', 1),
(802, 81, 3, '删除热词', '', '', '', 3, 'sys:cloud:delete', 1),
(803, 81, 3, 'AI提取关键词', '', '', '', 4, 'sys:cloud:ai', 1);

-- 角色-菜单关联：管理员拥有词云中心所有权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `menu_id`)
SELECT 3000 + m.id, 1, m.id FROM `sys_menu` m
WHERE m.id IN (80, 81, 800, 801, 802, 803)
  AND m.deleted = 0;
