-- ============================================
-- 数据重构 - 第一阶段：数据备份
-- ============================================
-- 执行时间: 2026-03-21
-- 备份原因: 题目数据重构前的安全检查点
-- ============================================

-- 1. 备份题目表
DROP TABLE IF EXISTS problem_backup_20260321;
CREATE TABLE problem_backup_20260321 AS SELECT * FROM problem;

-- 2. 备份参考答案表
DROP TABLE IF EXISTS reference_solution_backup_20260321;
CREATE TABLE reference_solution_backup_20260321 AS SELECT * FROM reference_solution;

-- 3. 备份知识点关联表
DROP TABLE IF EXISTS problem_knowledge_point_backup_20260321;
CREATE TABLE problem_knowledge_point_backup_20260321 AS SELECT * FROM problem_knowledge_point;

-- 4. 验证备份完整性
SELECT 
    '题目表' as table_name,
    (SELECT COUNT(*) FROM problem) as original_count,
    (SELECT COUNT(*) FROM problem_backup_20260321) as backup_count,
    CASE 
        WHEN (SELECT COUNT(*) FROM problem) = (SELECT COUNT(*) FROM problem_backup_20260321) 
        THEN '✅ 备份成功' 
        ELSE '❌ 备份异常' 
    END as status
UNION ALL
SELECT 
    '参考答案表',
    (SELECT COUNT(*) FROM reference_solution),
    (SELECT COUNT(*) FROM reference_solution_backup_20260321),
    CASE 
        WHEN (SELECT COUNT(*) FROM reference_solution) = (SELECT COUNT(*) FROM reference_solution_backup_20260321) 
        THEN '✅ 备份成功' 
        ELSE '❌ 备份异常' 
    END
UNION ALL
SELECT 
    '知识点关联表',
    (SELECT COUNT(*) FROM problem_knowledge_point),
    (SELECT COUNT(*) FROM problem_knowledge_point_backup_20260321),
    CASE 
        WHEN (SELECT COUNT(*) FROM problem_knowledge_point) = (SELECT COUNT(*) FROM problem_knowledge_point_backup_20260321) 
        THEN '✅ 备份成功' 
        ELSE '❌ 备份异常' 
    END;

-- 5. 显示当前数据完整性统计
SELECT 
    '数据完整性统计' as report,
    (SELECT COUNT(*) FROM problem) as total_problems,
    (SELECT COUNT(DISTINCT problem_id) FROM reference_solution) as problems_with_solution,
    (SELECT COUNT(DISTINCT problem_id) FROM problem_knowledge_point) as problems_with_kp,
    CONCAT(ROUND((SELECT COUNT(DISTINCT problem_id) FROM reference_solution) * 100.0 / (SELECT COUNT(*) FROM problem), 2), '%') as solution_coverage,
    CONCAT(ROUND((SELECT COUNT(DISTINCT problem_id) FROM problem_knowledge_point) * 100.0 / (SELECT COUNT(*) FROM problem), 2), '%') as kp_coverage;
