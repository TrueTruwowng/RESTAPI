-- =====================================================================
-- FIX SCRIPT: Sửa ràng buộc FK blogs.user_id tham chiếu bảng `user`
-- Mục tiêu:
--   1. Xoá/cập nhật bản ghi blog "mồ côi" (user_id không tồn tại)
--   2. Thay foreign key hiện tại bằng ON DELETE CASCADE
--   3. Đảm bảo index & engine đúng
-- Sử dụng:  mysql -u root -p spring_security_demo < fix_blog_fk.sql
-- NOTE: CHẠY TRONG MÔI TRƯỜNG DEV (BACKUP TRƯỚC NẾU PROD)
-- =====================================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0;

-- 1. Đảm bảo engine InnoDB để hỗ trợ FK
ALTER TABLE `user` ENGINE=InnoDB;
ALTER TABLE `blogs` ENGINE=InnoDB;

-- 2. Xoá blog mồ côi (nếu muốn thay vì gán về một user cụ thể)
DELETE b FROM blogs b
LEFT JOIN `user` u ON b.user_id = u.id
WHERE u.id IS NULL;

-- Nếu muốn gán về user id=1 thay vì xoá, dùng lệnh sau (bỏ comment) và bỏ lệnh DELETE ở trên:
-- UPDATE blogs b LEFT JOIN `user` u ON b.user_id = u.id SET b.user_id = 1 WHERE u.id IS NULL;

-- 3. Tìm tên constraint hiện tại (chạy lệnh bên ngoài, ở đây cố gắng DROP nếu tồn tại với tên phổ biến)
-- Bạn có thể cần chỉnh lại tên nếu khác.
-- Lấy tên thực tế: SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='blogs' AND REFERENCED_TABLE_NAME='user';

-- Thử drop các tên thường gặp (chỉ chạy nếu tồn tại; MySQL không hỗ trợ IF EXISTS cho FK cũ nên dùng thủ thuật)
SET @fk_name = NULL;
SELECT CONSTRAINT_NAME INTO @fk_name
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'blogs'
  AND COLUMN_NAME = 'user_id'
  AND REFERENCED_TABLE_NAME = 'user'
LIMIT 1;

SET @sql = IF(@fk_name IS NOT NULL, CONCAT('ALTER TABLE blogs DROP FOREIGN KEY ', @fk_name, ';'), 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4. Đảm bảo có index trên blogs.user_id (nếu Hibernate chưa tạo)
ALTER TABLE blogs ADD INDEX idx_blogs_user_id (user_id);

-- 5. Tạo lại foreign key với ON DELETE CASCADE
ALTER TABLE blogs
  ADD CONSTRAINT fk_blogs_user
  FOREIGN KEY (user_id) REFERENCES `user`(id)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- 6. (Tuỳ chọn) Unique cho username nếu chưa có
ALTER TABLE `user` ADD UNIQUE KEY uq_user_username (username);

SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_NOTES=@OLD_SQL_NOTES;

-- Hoàn tất.
SELECT 'Foreign key blogs.user_id đã được cấu hình ON DELETE CASCADE' AS status_message;

