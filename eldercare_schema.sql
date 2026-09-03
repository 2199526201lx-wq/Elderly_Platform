-- =============================================================================
-- AI 智能养老社区管理系统 - 数据库建表脚本
-- 依据：《AI 智能养老社区管理系统详细设计文档》第 6 章 数据库设计
-- 数据库类型 : MySQL 9.7.2
-- 字符集      : utf8mb4
-- 排序规则    : utf8mb4_0900_ai_ci
-- 存储引擎    : InnoDB
-- 数据表数量  : 24
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 6.2.1 创建数据库
-- ---------------------------------------------------------------------------
-- 注意：下面这句 DROP 会删除已存在的同名数据库及其全部数据，
--       用于重复导入时重建（解决 1050 "Table already exists" 报错）。
--       如不希望删除已有数据，请删除下面这一行。
DROP DATABASE IF EXISTS eldercare;

CREATE DATABASE IF NOT EXISTS eldercare
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

USE eldercare;

-- =============================================================================
-- 6.3 数据表详细设计（24 张表）
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 6.3.1 用户表（user）
-- ---------------------------------------------------------------------------
CREATE TABLE `user` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    `phone`             VARCHAR(20)  NOT NULL                COMMENT '手机号',
    `password`          VARCHAR(255) NOT NULL                COMMENT '密码（BCrypt 加密）',
    `real_name`         VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `gender`            VARCHAR(10)  DEFAULT NULL            COMMENT '性别',
    `birth_date`        DATE         DEFAULT NULL            COMMENT '出生日期',
    `height`            DECIMAL(5,1) DEFAULT NULL            COMMENT '身高（cm），用于计算 BMI',
    `avatar`            VARCHAR(500) DEFAULT NULL            COMMENT '头像 URL',
    `emergency_contact` VARCHAR(20)  DEFAULT NULL            COMMENT '紧急联系人电话',
    `member_level`      VARCHAR(20)  DEFAULT '普通'          COMMENT '会员等级：普通/白银/黄金/铂金/钻石',
    `points`            INT          DEFAULT 0               COMMENT '积分余额（与 point_transaction 流水对账）',
    `status`            VARCHAR(20)  DEFAULT '启用'          COMMENT '状态：启用/禁用',
    `role`              VARCHAR(20)  NOT NULL                COMMENT '角色编码冗余字段（MEMBER/ADMIN）',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT      DEFAULT 0               COMMENT '逻辑删除：0 未删除/1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_role` (`role`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表';

-- ---------------------------------------------------------------------------
-- 6.3.2 刷新令牌表（refresh_token）
-- ---------------------------------------------------------------------------
CREATE TABLE `refresh_token` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录 ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户 ID',
    `token`       VARCHAR(500) NOT NULL                COMMENT 'Refresh Token 值',
    `expire_time` DATETIME     NOT NULL                COMMENT '过期时间',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '刷新令牌表';

-- ---------------------------------------------------------------------------
-- 6.3.3 短信验证码表（sms_code）
-- ---------------------------------------------------------------------------
CREATE TABLE `sms_code` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '记录 ID',
    `phone`       VARCHAR(20) NOT NULL                COMMENT '手机号',
    `code`        VARCHAR(10) NOT NULL                COMMENT '验证码',
    `expire_time` DATETIME    NOT NULL                COMMENT '过期时间',
    `used`        TINYINT     DEFAULT 0               COMMENT '是否已使用：0 未使用/1 已使用',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '短信验证码表';

-- ---------------------------------------------------------------------------
-- 6.3.4 健康记录表（health_record）
-- ---------------------------------------------------------------------------
CREATE TABLE `health_record` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录 ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户 ID',
    `systolic`      INT          DEFAULT NULL            COMMENT '收缩压（mmHg）',
    `diastolic`     INT          DEFAULT NULL            COMMENT '舒张压（mmHg）',
    `blood_sugar`   DECIMAL(4,1) DEFAULT NULL            COMMENT '血糖（mmol/L）',
    `heart_rate`    INT          DEFAULT NULL            COMMENT '心率（次/分）',
    `weight`        DECIMAL(4,1) DEFAULT NULL            COMMENT '体重（kg）',
    `bmi`           DECIMAL(3,1) DEFAULT NULL            COMMENT 'BMI 指数',
    `memo`          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    `recorded_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_recorded` (`user_id`, `recorded_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康记录表';

-- ---------------------------------------------------------------------------
-- 6.3.5 问卷表（questionnaire）
-- ---------------------------------------------------------------------------
CREATE TABLE `questionnaire` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '问卷 ID',
    `title`       VARCHAR(200)  NOT NULL                COMMENT '问卷标题',
    `description` VARCHAR(1000) DEFAULT NULL            COMMENT '问卷描述',
    `status`      VARCHAR(20)   DEFAULT '草稿'          COMMENT '状态：草稿/已发布',
    `total_score` INT           NOT NULL DEFAULT 0      COMMENT '问卷满分（仅统计计分题 max_score 之和）',
    `pass_score`  INT           NOT NULL DEFAULT 60     COMMENT '及格分数线（百分制）',
    `grade_rules` JSON          DEFAULT NULL            COMMENT '评分等级规则 JSON',
    `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT       DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '问卷表';

-- ---------------------------------------------------------------------------
-- 6.3.6 题目表（question）
-- ---------------------------------------------------------------------------
CREATE TABLE `question` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '题目 ID',
    `questionnaire_id` BIGINT       NOT NULL                COMMENT '问卷 ID',
    `content`          VARCHAR(500) NOT NULL                COMMENT '题目内容',
    `type`             VARCHAR(20)  NOT NULL                COMMENT '类型：单选/多选/文本',
    `options`          JSON         DEFAULT NULL            COMMENT '选项 JSON 数组',
    `score_mode`       VARCHAR(20)  NOT NULL DEFAULT '计分' COMMENT '计分模式：计分/非计分',
    `max_score`        INT          NOT NULL DEFAULT 0      COMMENT '题目满分',
    `sort_order`       INT          DEFAULT 0               COMMENT '排序号',
    `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_questionnaire_id` (`questionnaire_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目表';

-- ---------------------------------------------------------------------------
-- 6.3.7 评测结果表（assessment_result）
-- ---------------------------------------------------------------------------
CREATE TABLE `assessment_result` (
    `id`               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评测结果 ID',
    `user_id`          BIGINT   NOT NULL                COMMENT '用户 ID',
    `questionnaire_id` BIGINT   NOT NULL                COMMENT '问卷 ID',
    `answers`          JSON     DEFAULT NULL            COMMENT '答案快照 JSON',
    `rule_score`       INT      DEFAULT NULL            COMMENT '规则分（百分制）',
    `ai_score`         INT      DEFAULT NULL            COMMENT 'AI 智能评分（百分制，最终展示分）',
    `ai_suggestion`    TEXT     DEFAULT NULL            COMMENT 'AI 建议',
    `create_time`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT  DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_questionnaire_id` (`questionnaire_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评测结果表';

-- ---------------------------------------------------------------------------
-- 6.3.8 体检套餐表（appointment_package）
-- ---------------------------------------------------------------------------
CREATE TABLE `appointment_package` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '套餐 ID',
    `name`            VARCHAR(200) NOT NULL                COMMENT '套餐名称',
    `cover_url`       VARCHAR(500) DEFAULT NULL            COMMENT '封面图 URL',
    `description`     TEXT         DEFAULT NULL            COMMENT '套餐描述',
    `price`           INT          DEFAULT 0               COMMENT '价格（积分抵扣）',
    `suitable_people` VARCHAR(200) DEFAULT NULL            COMMENT '适合人群',
    `items`           JSON         DEFAULT NULL            COMMENT '包含项目列表 JSON',
    `status`          VARCHAR(20)  DEFAULT '启用'          COMMENT '状态：启用/禁用',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '体检套餐表';

-- ---------------------------------------------------------------------------
-- 6.3.9 预约时段表（appointment_slot）
-- ---------------------------------------------------------------------------
CREATE TABLE `appointment_slot` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '时间段 ID',
    `package_id`    BIGINT      NOT NULL                COMMENT '套餐 ID',
    `appoint_date`  DATE        NOT NULL                COMMENT '预约日期',
    `time_range`    VARCHAR(50) NOT NULL                COMMENT '时间段，如 "09:00-10:00"',
    `max_count`     INT         DEFAULT 10              COMMENT '最大预约人数',
    `current_count` INT         DEFAULT 0               COMMENT '当前已预约人数',
    `status`        VARCHAR(20) DEFAULT '可预约'         COMMENT '状态：可预约/已满/已关闭',
    `create_time`   DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT     DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_package_date` (`package_id`, `appoint_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约时段表';

-- ---------------------------------------------------------------------------
-- 6.3.10 预约表（appointment）
-- ---------------------------------------------------------------------------
CREATE TABLE `appointment` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '预约 ID',
    `user_id`            BIGINT       NOT NULL                COMMENT '用户 ID',
    `slot_id`            BIGINT       NOT NULL                COMMENT '时间段 ID',
    `package_id`         BIGINT       NOT NULL                COMMENT '套餐 ID',
    `status`             VARCHAR(20)  DEFAULT '待确认'        COMMENT '状态：待确认/已确认/已取消/已完成',
    `report_url`         VARCHAR(500) DEFAULT NULL            COMMENT '体检报告 URL',
    `original_filename`  VARCHAR(255) DEFAULT NULL            COMMENT '体检报告原始文件名',
    `report_upload_time` DATETIME     DEFAULT NULL            COMMENT '报告上传时间',
    `upload_admin_id`    BIGINT       DEFAULT NULL            COMMENT '上传管理员用户 ID（关联 user.id）',
    `create_time`        DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`            TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_slot_id` (`slot_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约表';

-- ---------------------------------------------------------------------------
-- 6.3.11 社区活动表（community_activity）
-- ---------------------------------------------------------------------------
CREATE TABLE `community_activity` (
    `id`                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '活动 ID',
    `title`                   VARCHAR(200) NOT NULL                COMMENT '活动标题',
    `cover_url`               VARCHAR(500) DEFAULT NULL            COMMENT '封面图 URL',
    `content`                 TEXT         DEFAULT NULL            COMMENT '活动内容',
    `location`                VARCHAR(200) DEFAULT NULL            COMMENT '活动地点',
    `registration_start_time` DATETIME     DEFAULT NULL            COMMENT '报名开始时间',
    `registration_end_time`   DATETIME     DEFAULT NULL            COMMENT '报名结束时间',
    `activity_start_time`     DATETIME     DEFAULT NULL            COMMENT '活动开始时间',
    `activity_end_time`       DATETIME     DEFAULT NULL            COMMENT '活动结束时间',
    `max_participants`        INT          DEFAULT NULL            COMMENT '人数上限',
    `current_participants`    INT          DEFAULT 0               COMMENT '当前报名人数',
    `status`                  VARCHAR(20)  DEFAULT '草稿'          COMMENT '状态：草稿/报名中/进行中/已结束',
    `create_time`             DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`             DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`                 TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区活动表';

-- ---------------------------------------------------------------------------
-- 6.3.12 活动报名表（activity_registration）
-- ---------------------------------------------------------------------------
CREATE TABLE `activity_registration` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '报名 ID',
    `user_id`         BIGINT      NOT NULL                COMMENT '用户 ID',
    `activity_id`     BIGINT      NOT NULL                COMMENT '活动 ID',
    `check_in_status` VARCHAR(20) DEFAULT '未签到'        COMMENT '签到状态：未签到/已签到',
    `check_in_time`   DATETIME    DEFAULT NULL            COMMENT '签到时间（未签到为 NULL）',
    `create_time`     DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `update_time`     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT     DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`),
    KEY `idx_activity_id` (`activity_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动报名表';

-- ---------------------------------------------------------------------------
-- 6.3.13 健康指导表（health_guidance）
-- ---------------------------------------------------------------------------
CREATE TABLE `health_guidance` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '指导 ID',
    `user_id`     BIGINT      NOT NULL                COMMENT '用户 ID',
    `type`        VARCHAR(20) NOT NULL                COMMENT '类型：饮食/运动/作息/数据小结',
    `indicator`   VARCHAR(20) DEFAULT NULL            COMMENT '触发指标：收缩压/舒张压/血糖/心率/BMI 体质指数/体重',
    `content`     TEXT        NOT NULL                COMMENT '指导内容',
    `is_read`     TINYINT     DEFAULT 0               COMMENT '是否已读：0 未读/1 已读',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT     DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_type` (`user_id`, `type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康指导表';

-- ---------------------------------------------------------------------------
-- 6.3.14 AI 会话表（ai_conversation_session）
-- ---------------------------------------------------------------------------
CREATE TABLE `ai_conversation_session` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话 ID',
    `user_id`      BIGINT       NOT NULL                COMMENT '用户 ID',
    `session_name` VARCHAR(100) DEFAULT NULL            COMMENT '会话名称',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 会话表';

-- ---------------------------------------------------------------------------
-- 6.3.15 AI 对话消息表（ai_conversation_message）
-- ---------------------------------------------------------------------------
CREATE TABLE `ai_conversation_message` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '消息 ID',
    `session_id`  BIGINT      NOT NULL                COMMENT '会话 ID',
    `user_id`     BIGINT      NOT NULL                COMMENT '用户 ID',
    `role`        VARCHAR(20) NOT NULL                COMMENT '角色：用户/助手',
    `message`     TEXT        NOT NULL                COMMENT '消息内容',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT     DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_user_session` (`user_id`, `session_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 对话消息表';

-- ---------------------------------------------------------------------------
-- 6.3.16 消息表（message）
-- ---------------------------------------------------------------------------
CREATE TABLE `message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息 ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户 ID',
    `title`       VARCHAR(200) NOT NULL                COMMENT '消息标题',
    `content`     TEXT         DEFAULT NULL            COMMENT '消息内容',
    `type`        VARCHAR(20)  DEFAULT NULL            COMMENT '消息类型：预约/活动/系统/健康提醒',
    `is_read`     TINYINT      DEFAULT 0               COMMENT '是否已读：0 未读/1 已读',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息表';

-- ---------------------------------------------------------------------------
-- 6.3.17 系统配置表（sys_config）
-- ---------------------------------------------------------------------------
CREATE TABLE `sys_config` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置 ID',
    `config_key`   VARCHAR(100) NOT NULL                COMMENT '配置键',
    `config_value` TEXT         DEFAULT NULL            COMMENT '配置值',
    `description`  VARCHAR(500) DEFAULT NULL            COMMENT '配置描述',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表';

-- ---------------------------------------------------------------------------
-- 6.3.18 积分流水表（point_transaction）
-- ---------------------------------------------------------------------------
CREATE TABLE `point_transaction` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '流水 ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户 ID',
    `type`          VARCHAR(20)  NOT NULL                COMMENT '类型：注册赠送/活动签到/评测完成/体检预约/管理员调整/积分过期',
    `change_amount` INT          NOT NULL                COMMENT '变动积分（正=获得，负=扣减）',
    `balance_after` INT          NOT NULL                COMMENT '变动后积分余额',
    `remain_amount` INT          NOT NULL DEFAULT 0      COMMENT '获得类流水剩余可用积分',
    `expire_time`   DATETIME     DEFAULT NULL            COMMENT '获得类流水过期时间（获得时间 + 1 年）',
    `batch_tx_id`   BIGINT       DEFAULT NULL            COMMENT '消耗类流水关联的被扣获得批次流水 ID',
    `description`   VARCHAR(200) DEFAULT NULL            COMMENT '业务说明',
    `ref_id`        BIGINT       DEFAULT NULL            COMMENT '关联业务记录 ID',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT      DEFAULT 0               COMMENT '逻辑删除（取消退还时消费流水标记 deleted=1）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_expire_remain` (`expire_time`, `remain_amount`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分流水表';

-- ---------------------------------------------------------------------------
-- 6.3.19 角色表（role）
-- ---------------------------------------------------------------------------
CREATE TABLE `role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
    `role_code`   VARCHAR(50)  NOT NULL                COMMENT '角色编码：MEMBER 会员/ADMIN 管理员',
    `role_name`   VARCHAR(50)  NOT NULL                COMMENT '角色名称',
    `description` VARCHAR(200) DEFAULT NULL            COMMENT '角色描述',
    `status`      TINYINT      DEFAULT 1               COMMENT '状态：1 启用/0 停用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表';

-- ---------------------------------------------------------------------------
-- 6.3.20 权限表（permission）
-- ---------------------------------------------------------------------------
CREATE TABLE `permission` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限 ID',
    `permission_code` VARCHAR(100) NOT NULL                COMMENT '权限编码（域:模块:操作，如 member:health:list）',
    `permission_name` VARCHAR(100) NOT NULL                COMMENT '权限名称',
    `description`     VARCHAR(200) DEFAULT NULL            COMMENT '权限描述',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表';

-- ---------------------------------------------------------------------------
-- 6.3.21 资源表（resource）
-- ---------------------------------------------------------------------------
CREATE TABLE `resource` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '资源 ID',
    `resource_code` VARCHAR(100) NOT NULL                COMMENT '资源编码（api:member:health / menu:admin:dashboard / btn:assessment:publish）',
    `resource_name` VARCHAR(100) NOT NULL                COMMENT '资源名称',
    `resource_type` VARCHAR(20)  NOT NULL                COMMENT '资源类型：接口/菜单/按钮',
    `path`          VARCHAR(200) DEFAULT NULL            COMMENT '资源路径（接口类型为接口路径模式）',
    `parent_id`     BIGINT       DEFAULT 0               COMMENT '父资源 ID（菜单树形结构）',
    `sort_order`    INT          DEFAULT 0               COMMENT '排序号',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`resource_type`),
    KEY `idx_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '资源表';

-- ---------------------------------------------------------------------------
-- 6.3.22 用户角色关联表（user_role）
-- ---------------------------------------------------------------------------
CREATE TABLE `user_role` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户 ID（关联 user.id）',
    `role_id`     BIGINT   NOT NULL                COMMENT '角色 ID（关联 role.id）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表';

-- ---------------------------------------------------------------------------
-- 6.3.23 角色权限关联表（role_permission）
-- ---------------------------------------------------------------------------
CREATE TABLE `role_permission` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
    `role_id`       BIGINT   NOT NULL                COMMENT '角色 ID（关联 role.id）',
    `permission_id` BIGINT   NOT NULL                COMMENT '权限 ID（关联 permission.id）',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表';

-- ---------------------------------------------------------------------------
-- 6.3.24 权限资源关联表（permission_resource）
-- ---------------------------------------------------------------------------
CREATE TABLE `permission_resource` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
    `permission_id` BIGINT   NOT NULL                COMMENT '权限 ID（关联 permission.id）',
    `resource_id`   BIGINT   NOT NULL                COMMENT '资源 ID（关联 resource.id）',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_resource` (`permission_id`, `resource_id`),
    KEY `idx_resource_id` (`resource_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限资源关联表';

-- =============================================================================
-- 6.8 数据初始化脚本
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 6.8.1 系统配置初始化
-- ---------------------------------------------------------------------------
INSERT INTO sys_config (config_key, config_value, description) VALUES
('ai_chat_system_prompt', '你是一位专业的健康顾问，请用亲切、易懂的语言回答用户的健康问题。', 'AI 对话系统提示词'),
('register_bonus_points', '100', '注册赠送积分'),
('checkin_bonus_points', '50', '活动签到赠送积分'),
('health_assessment_min_score', '60', '健康评测及格分数线'),
('access_token_expire_hours', '2', 'Access Token 有效期（小时）'),
('refresh_token_expire_days', '7', 'Refresh Token 有效期（天）');

-- ---------------------------------------------------------------------------
-- 6.8.2 默认用户初始化
-- ---------------------------------------------------------------------------
-- 创建管理员用户（密码：Admin@123456，BCrypt 加密）
INSERT INTO user (phone, password, real_name, member_level, points, status, role) VALUES
('13800000000', '$2b$10$5xxJYAxX3bB35VkjlRAuauILyrcKEUXJINVQXrWPYl6vhfZlIiy46', '系统管理员', '铂金', 99999, '启用', 'ADMIN');

-- 创建测试会员（密码：Test@123456，BCrypt 加密）
INSERT INTO user (phone, password, real_name, member_level, points, status, role) VALUES
('13800138000', '$2b$10$La.Q.aZ.SUB5Ej3neFdzGOUYLva/QuO7sALyOPaBCxyYkro9Cpzjm', '测试用户', '普通', 1000, '启用', 'MEMBER');

-- ---------------------------------------------------------------------------
-- 6.8.3 示例问卷初始化
-- ---------------------------------------------------------------------------
INSERT INTO questionnaire (title, description, status, total_score, pass_score, grade_rules) VALUES
('基础健康状况调查问卷', '通过简单的问题了解您的基本健康状况', '已发布', 20, 60,
 '[{"min":90,"label":"优秀","description":"整体健康状况良好"},{"min":80,"label":"良好","description":"整体状况较好，个别指标需关注"},{"min":60,"label":"及格","description":"整体状况一般，建议加强健康管理"},{"min":0,"label":"需关注","description":"整体状况欠佳，建议及时就医咨询"}]');

INSERT INTO question (questionnaire_id, content, type, score_mode, options, max_score, sort_order) VALUES
(1, '您的年龄是？', '单选', '非计分', '[{"text":"18-30岁","meaning":"青壮年人群，身体机能良好"},{"text":"31-45岁","meaning":"中年人群，机能开始缓慢下降"},{"text":"46-60岁","meaning":"中老年人群，慢病风险逐渐上升"},{"text":"60岁以上","meaning":"老年人群，慢病风险相对较高"}]', 0, 1),
(1, '您的睡眠质量如何？', '单选', '计分', '[{"text":"很好，每天睡 7-8 小时","meaning":"睡眠充足，质量良好","score":10},{"text":"一般，偶尔失眠","meaning":"睡眠基本正常，偶有波动","score":6},{"text":"较差，经常失眠","meaning":"存在睡眠障碍，需引起关注","score":3},{"text":"非常差，严重影响生活","meaning":"严重睡眠障碍，建议就医","score":1}]', 10, 2),
(1, '您每周运动几次？', '单选', '计分', '[{"text":"几乎不运动","meaning":"缺乏运动，体能下降风险高","score":1},{"text":"1-2 次","meaning":"运动量偏少，建议增加","score":4},{"text":"3-4 次","meaning":"运动量适中，习惯良好","score":8},{"text":"5 次以上","meaning":"运动频繁，习惯优良","score":10}]', 10, 3),
(1, '请简要描述您目前的健康状况', '文本', '非计分', NULL, 10, 4);

-- ---------------------------------------------------------------------------
-- 6.8.4 示例体检套餐初始化
-- ---------------------------------------------------------------------------
INSERT INTO appointment_package (name, description, price, suitable_people, items, status) VALUES
('基础体检套餐', '适合健康人群的基础体检，包含常规检查项目', 500, '所有人群',
 '["血常规", "尿常规", "肝功能", "肾功能", "心电图", "胸部 X 光"]', '启用'),
('中老年体检套餐', '针对中老年人的全面体检，包含心脑血管等专项检查', 1000, '45 岁以上人群',
 '["基础套餐全部项目", "肿瘤标志物", "颈动脉彩超", "骨密度检测", "甲状腺功能"]', '启用'),
('女性专属套餐', '针对女性健康特点设计的专项体检套餐', 800, '女性人群',
 '["基础套餐全部项目", "妇科检查", "乳腺彩超", "HPV 检测", "TCT 检查"]', '启用');
