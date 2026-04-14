ALTER TABLE community_post
    ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'public' COMMENT 'public or private' AFTER type,
    ADD COLUMN tags VARCHAR(500) NULL COMMENT 'comma separated free-form tags' AFTER visibility,
    ADD COLUMN related_problem_id BIGINT NULL COMMENT 'optional linked problem' AFTER tags,
    ADD COLUMN related_path_id BIGINT NULL COMMENT 'optional linked learning path' AFTER related_problem_id,
    ADD COLUMN related_level_id BIGINT NULL COMMENT 'optional linked learning level' AFTER related_path_id;

UPDATE community_post
SET visibility = 'public'
WHERE visibility IS NULL OR visibility = '';

CREATE INDEX idx_community_post_visibility_create_time
    ON community_post (visibility, create_time);

CREATE INDEX idx_community_post_related_problem
    ON community_post (related_problem_id);
