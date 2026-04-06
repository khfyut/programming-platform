-- Extend User table, add profile related fields
ALTER TABLE user ADD COLUMN bio TEXT COMMENT 'personal bio';
ALTER TABLE user ADD COLUMN avatar_url VARCHAR(255) COMMENT 'avatar URL';
ALTER TABLE user ADD COLUMN github_url VARCHAR(255) COMMENT 'GitHub URL';
ALTER TABLE user ADD COLUMN blog_url VARCHAR(255) COMMENT 'blog URL';
ALTER TABLE user ADD COLUMN total_solved INT DEFAULT 0 COMMENT 'total solved problems';
ALTER TABLE user ADD COLUMN total_submissions INT DEFAULT 0 COMMENT 'total submissions';
ALTER TABLE user ADD COLUMN study_hours INT DEFAULT 0 COMMENT 'study hours';
ALTER TABLE user ADD COLUMN ranking INT DEFAULT 0 COMMENT 'ranking';

-- Favorite table
CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'user ID',
    target_type VARCHAR(20) NOT NULL COMMENT 'target type: POST, PROBLEM',
    target_id BIGINT NOT NULL COMMENT 'target ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Achievement table
CREATE TABLE IF NOT EXISTS achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT 'achievement name',
    description TEXT COMMENT 'achievement description',
    icon VARCHAR(255) COMMENT 'achievement icon',
    condition_type VARCHAR(50) NOT NULL COMMENT 'condition type: SOLVED_COUNT, SUBMIT_COUNT, etc',
    condition_value INT NOT NULL COMMENT 'condition value',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- User achievement relation table
CREATE TABLE IF NOT EXISTS user_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'user ID',
    achievement_id BIGINT NOT NULL COMMENT 'achievement ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_user_achievement (user_id, achievement_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES achievement(id) ON DELETE CASCADE
);

-- Insert achievement data
INSERT INTO achievement (name, description, icon, condition_type, condition_value) VALUES
('First Step', 'Solve first problem', 'trophy', 'SOLVED_COUNT', 1),
('Getting Started', 'Solve 10 problems', 'trophy', 'SOLVED_COUNT', 10),
('Making Progress', 'Solve 50 problems', 'trophy', 'SOLVED_COUNT', 50),
('Master', 'Solve 100 problems', 'trophy', 'SOLVED_COUNT', 100),
('Persistent', 'Study for 7 consecutive days', 'fire', 'CONTINUOUS_DAYS', 7),
('Committed', 'Study for 30 consecutive days', 'fire', 'CONTINUOUS_DAYS', 30),
('Community Contributor', 'Post 10 posts', 'star', 'POST_COUNT', 10),
('Helper', 'Post 50 comments', 'star', 'COMMENT_COUNT', 50);