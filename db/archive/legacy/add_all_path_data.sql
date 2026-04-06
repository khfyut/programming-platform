-- 为所有学习路径添加章节和关卡数据
-- 基于path_id=1的数据模式为其他路径添加数据

-- 先清理旧数据，只保留path_id=1的原始数据
DELETE FROM path_chapter WHERE path_id != 1;
DELETE FROM path_level WHERE chapter_id NOT IN (SELECT id FROM path_chapter WHERE path_id = 1);

-- 为path_id=2-10和11-20添加数据，复用path_id=1的章节结构

-- 获取path_id=1的章节数据
SET @chap1 = (SELECT id FROM path_chapter WHERE path_id = 1 AND order_num = 1);
SET @chap2 = (SELECT id FROM path_chapter WHERE path_id = 1 AND order_num = 2);
SET @chap3 = (SELECT id FROM path_chapter WHERE path_id = 1 AND order_num = 3);
SET @chap4 = (SELECT id FROM path_chapter WHERE path_id = 1 AND order_num = 4);
SET @chap5 = (SELECT id FROM path_chapter WHERE path_id = 1 AND order_num = 5);

-- 为path_id=2-10和11-20添加章节
-- Java路径 2-10
INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 2, name, order_num, description, NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 3, name, order_num, description, NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 4, name, order_num, description, NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 5, name, order_num, description, NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 6, name, order_num, description, NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 7, name, order_num, description, NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 8, name, order_num, description, NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 9, name, order_num, description, NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 10, name, order_num, description, NOW(), NOW() FROM path_chapter WHERE path_id = 1;

-- Python路径 11-20
INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 11, REPLACE(name, 'Java', 'Python'), order_num, REPLACE(description, 'Java', 'Python'), NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 12, REPLACE(name, 'Java', 'Python'), order_num, REPLACE(description, 'Java', 'Python'), NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 13, REPLACE(name, 'Java', 'Python'), order_num, REPLACE(description, 'Java', 'Python'), NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 14, REPLACE(name, 'Java', 'Python'), order_num, REPLACE(description, 'Java', 'Python'), NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 15, REPLACE(name, 'Java', 'Python'), order_num, REPLACE(description, 'Java', 'Python'), NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 16, REPLACE(name, 'Java', 'Python'), order_num, REPLACE(description, 'Java', 'Python'), NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 17, REPLACE(name, 'Java', 'Python'), order_num, REPLACE(description, 'Java', 'Python'), NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 18, REPLACE(name, 'Java', 'Python'), order_num, REPLACE(description, 'Java', 'Python'), NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 19, REPLACE(name, 'Java', 'Python'), order_num, REPLACE(description, 'Java', 'Python'), NOW(), NOW() FROM path_chapter WHERE path_id = 1;

INSERT INTO path_chapter (path_id, name, order_num, description, create_time, update_time)
SELECT 20, REPLACE(name, 'Java', 'Python'), order_num, REPLACE(description, 'Java', 'Python'), NOW(), NOW() FROM path_chapter WHERE path_id = 1;

-- 现在为新添加的章节添加关卡
-- 首先获取path_id=1的所有关卡
-- 为每个新章节复制关卡数据

-- 为Java路径 2-10 添加关卡
DELIMITER //
CREATE PROCEDURE copy_levels()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE src_chapter_id BIGINT;
    DECLARE dest_chapter_id BIGINT;
    DECLARE dest_path_id INT;
    DECLARE cur CURSOR FOR SELECT id FROM path_chapter WHERE path_id = 1 ORDER BY order_num;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 为path_id 2-10复制关卡
    SET dest_path_id = 2;
    WHILE dest_path_id <= 10 DO
        SET done = FALSE;
        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO src_chapter_id;
            IF done THEN
                LEAVE read_loop;
            END IF;
            
            -- 获取目标章节id
            SELECT id INTO dest_chapter_id FROM path_chapter 
            WHERE path_id = dest_path_id 
            AND order_num = (SELECT order_num FROM path_chapter WHERE id = src_chapter_id);
            
            -- 复制关卡
            INSERT INTO path_level (chapter_id, name, order_num, problem_ids, knowledge_points, unlock_condition, create_time, update_time)
            SELECT dest_chapter_id, name, order_num, problem_ids, knowledge_points, unlock_condition, NOW(), NOW()
            FROM path_level WHERE chapter_id = src_chapter_id;
        END LOOP;
        CLOSE cur;
        
        SET dest_path_id = dest_path_id + 1;
    END WHILE;
    
    -- 为path_id 11-20复制关卡
    SET dest_path_id = 11;
    WHILE dest_path_id <= 20 DO
        SET done = FALSE;
        OPEN cur;
        read_loop2: LOOP
            FETCH cur INTO src_chapter_id;
            IF done THEN
                LEAVE read_loop2;
            END IF;
            
            -- 获取目标章节id
            SELECT id INTO dest_chapter_id FROM path_chapter 
            WHERE path_id = dest_path_id 
            AND order_num = (SELECT order_num FROM path_chapter WHERE id = src_chapter_id);
            
            -- 复制关卡，并将Java相关内容替换为Python
            INSERT INTO path_level (chapter_id, name, order_num, problem_ids, knowledge_points, unlock_condition, create_time, update_time)
            SELECT dest_chapter_id, 
                   REPLACE(name, 'Java', 'Python'), 
                   order_num, 
                   problem_ids, 
                   REPLACE(knowledge_points, 'Java', 'Python'), 
                   unlock_condition, 
                   NOW(), 
                   NOW()
            FROM path_level WHERE chapter_id = src_chapter_id;
        END LOOP;
        CLOSE cur;
        
        SET dest_path_id = dest_path_id + 1;
    END WHILE;
END //
DELIMITER ;

CALL copy_levels();
DROP PROCEDURE copy_levels;

-- 验证数据
SELECT lp.id, lp.name, COUNT(DISTINCT pc.id) as chapter_count, COUNT(DISTINCT pl.id) as level_count 
FROM learning_path lp 
LEFT JOIN path_chapter pc ON lp.id = pc.path_id 
LEFT JOIN path_level pl ON pc.id = pl.chapter_id 
GROUP BY lp.id, lp.name 
ORDER BY lp.id;
