ALTER TABLE `learning_event_log`
ADD COLUMN `policy_profile` VARCHAR(50) NOT NULL DEFAULT 'PROBLEM_COACH' COMMENT 'Agent策略域：PROBLEM_COACH, WRONG_BOOK_REFLECTION, GLOBAL_COACH, LEARNING_PATH';

UPDATE `learning_event_log`
SET `policy_profile` = 'WRONG_BOOK_REFLECTION'
WHERE `wrong_item_id` IS NOT NULL
   OR `entry_ref_type` = 'WRONG_BOOK';

UPDATE `learning_event_log`
SET `policy_profile` = 'LEARNING_PATH'
WHERE `entry_ref_type` = 'LEARNING_PATH_LEVEL';

CREATE INDEX `idx_learning_event_policy_profile`
ON `learning_event_log` (`policy_profile`);
