-- 为post表添加状态相关字段
ALTER TABLE post 
ADD COLUMN status TINYINT DEFAULT 0 COMMENT '帖子状态：0-待审核，1-已通过，2-已拒绝' AFTER tags,
ADD COLUMN reject_reason VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因' AFTER status,
ADD COLUMN reviewer_id INT DEFAULT NULL COMMENT '审核员ID' AFTER reject_reason,
ADD COLUMN review_time DATETIME DEFAULT NULL COMMENT '审核时间' AFTER reviewer_id;

-- 添加索引以提高查询性能
CREATE INDEX idx_post_status ON post(status);