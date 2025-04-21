-- 删除原有的 folder_id 字段（如果已存在）
ALTER TABLE files DROP COLUMN IF EXISTS folder_id;

-- 多对多关系中间表
CREATE TABLE folder_file (
    folder_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    PRIMARY KEY (folder_id, file_id),
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE CASCADE,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE
);
