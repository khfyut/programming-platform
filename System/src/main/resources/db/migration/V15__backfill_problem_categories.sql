-- Backfill problem_category bindings for the current problem bank.
-- The category table already existed, but most problems had NULL category_id/sub_category_id,
-- so the problem-list category filter had little practical effect.

UPDATE `problem`
SET `category_id` = 3, `sub_category_id` = 18, `update_time` = NOW()
WHERE `id` IN (18, 27, 39, 43)
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 3, `sub_category_id` = 20, `update_time` = NOW()
WHERE `id` IN (19, 20)
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 3, `sub_category_id` = 23, `update_time` = NOW()
WHERE `id` IN (21, 44)
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 3, `sub_category_id` = 21, `update_time` = NOW()
WHERE `id` IN (22, 48)
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 3, `sub_category_id` = 22, `update_time` = NOW()
WHERE `id` = 23
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 2, `sub_category_id` = 15, `update_time` = NOW()
WHERE `id` = 24
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 2, `sub_category_id` = 16, `update_time` = NOW()
WHERE `id` = 25
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 2, `sub_category_id` = 17, `update_time` = NOW()
WHERE `id` = 26
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 2, `sub_category_id` = 13, `update_time` = NOW()
WHERE `id` = 45
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 5, `sub_category_id` = 28, `update_time` = NOW()
WHERE `id` = 28
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 5, `sub_category_id` = 29, `update_time` = NOW()
WHERE `id` = 29
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 5, `sub_category_id` = 31, `update_time` = NOW()
WHERE `id` IN (30, 31)
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 6, `sub_category_id` = 32, `update_time` = NOW()
WHERE `id` IN (32, 49)
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 6, `sub_category_id` = 35, `update_time` = NOW()
WHERE `id` = 33
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 6, `sub_category_id` = 34, `update_time` = NOW()
WHERE `id` = 34
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 6, `sub_category_id` = 33, `update_time` = NOW()
WHERE `id` = 50
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 7, `sub_category_id` = 37, `update_time` = NOW()
WHERE `id` IN (35, 36, 37, 51, 55)
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 7, `sub_category_id` = 38, `update_time` = NOW()
WHERE `id` IN (52, 53, 54, 56)
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 1, `sub_category_id` = 10, `update_time` = NOW()
WHERE `id` IN (38, 47)
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 4, `sub_category_id` = 24, `update_time` = NOW()
WHERE `id` IN (40, 46)
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 4, `sub_category_id` = 25, `update_time` = NOW()
WHERE `id` = 41
  AND `category_id` IS NULL;

UPDATE `problem`
SET `category_id` = 4, `sub_category_id` = 26, `update_time` = NOW()
WHERE `id` = 42
  AND `category_id` IS NULL;
