UPDATE user_knowledge_mastery ukm
JOIN (
    SELECT
        s.user_id,
        pkp.knowledge_point_id AS knowledge_id,
        CASE
            WHEN ROUND(SUM(CASE WHEN s.result = 0 THEN 1 ELSE 0 END) / COUNT(*) * 100) >= 90 THEN 3
            WHEN ROUND(SUM(CASE WHEN s.result = 0 THEN 1 ELSE 0 END) / COUNT(*) * 100) >= 70 THEN 2
            WHEN ROUND(SUM(CASE WHEN s.result = 0 THEN 1 ELSE 0 END) / COUNT(*) * 100) >= 40 THEN 1
            ELSE 0
        END AS mastery_level,
        ROUND(SUM(CASE WHEN s.result = 0 THEN 1 ELSE 0 END) / COUNT(*) * 100) AS score,
        MAX(COALESCE(s.create_time, NOW())) AS last_practice_time,
        COUNT(*) AS practice_count,
        SUM(CASE WHEN s.result = 0 THEN 1 ELSE 0 END) AS correct_count
    FROM submit s
    JOIN problem_knowledge_point pkp ON pkp.problem_id = s.problem_id
    GROUP BY s.user_id, pkp.knowledge_point_id
) derived
    ON ukm.user_id = derived.user_id
    AND ukm.knowledge_id = derived.knowledge_id
SET
    ukm.mastery_level = derived.mastery_level,
    ukm.score = derived.score,
    ukm.last_practice_time = derived.last_practice_time,
    ukm.practice_count = derived.practice_count,
    ukm.correct_count = derived.correct_count,
    ukm.update_time = NOW();

INSERT INTO user_knowledge_mastery (
    user_id,
    knowledge_id,
    mastery_level,
    score,
    last_practice_time,
    practice_count,
    correct_count,
    create_time,
    update_time
)
SELECT
    derived.user_id,
    derived.knowledge_id,
    derived.mastery_level,
    derived.score,
    derived.last_practice_time,
    derived.practice_count,
    derived.correct_count,
    NOW(),
    NOW()
FROM (
    SELECT
        s.user_id,
        pkp.knowledge_point_id AS knowledge_id,
        CASE
            WHEN ROUND(SUM(CASE WHEN s.result = 0 THEN 1 ELSE 0 END) / COUNT(*) * 100) >= 90 THEN 3
            WHEN ROUND(SUM(CASE WHEN s.result = 0 THEN 1 ELSE 0 END) / COUNT(*) * 100) >= 70 THEN 2
            WHEN ROUND(SUM(CASE WHEN s.result = 0 THEN 1 ELSE 0 END) / COUNT(*) * 100) >= 40 THEN 1
            ELSE 0
        END AS mastery_level,
        ROUND(SUM(CASE WHEN s.result = 0 THEN 1 ELSE 0 END) / COUNT(*) * 100) AS score,
        MAX(COALESCE(s.create_time, NOW())) AS last_practice_time,
        COUNT(*) AS practice_count,
        SUM(CASE WHEN s.result = 0 THEN 1 ELSE 0 END) AS correct_count
    FROM submit s
    JOIN problem_knowledge_point pkp ON pkp.problem_id = s.problem_id
    GROUP BY s.user_id, pkp.knowledge_point_id
) derived
LEFT JOIN user_knowledge_mastery ukm
    ON ukm.user_id = derived.user_id
    AND ukm.knowledge_id = derived.knowledge_id
WHERE ukm.id IS NULL;

SELECT
    user_id,
    COUNT(*) AS mastery_item_count,
    SUM(CASE WHEN mastery_level < 2 THEN 1 ELSE 0 END) AS weak_item_count
FROM user_knowledge_mastery
GROUP BY user_id
ORDER BY user_id;
