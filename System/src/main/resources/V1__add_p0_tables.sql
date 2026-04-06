-- 学习路径相关表
CREATE TABLE IF NOT EXISTS learning_path (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS path_chapter (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    path_id BIGINT NOT NULL,
    chapter_name VARCHAR(255) NOT NULL,
    chapter_order INT NOT NULL,
    FOREIGN KEY (path_id) REFERENCES learning_path(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS path_level (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chapter_id BIGINT NOT NULL,
    level_name VARCHAR(255) NOT NULL,
    level_order INT NOT NULL,
    problem_ids TEXT,
    FOREIGN KEY (chapter_id) REFERENCES path_chapter(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_path_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    path_id BIGINT NOT NULL,
    current_level_id BIGINT,
    completed_levels TEXT,
    progress INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (path_id) REFERENCES learning_path(id) ON DELETE CASCADE
);

-- 知识点相关表
CREATE TABLE IF NOT EXISTS knowledge_point (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id BIGINT,
    difficulty INT DEFAULT 0,
    FOREIGN KEY (parent_id) REFERENCES knowledge_point(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS knowledge_relationship (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    relationship_type VARCHAR(50),
    FOREIGN KEY (source_id) REFERENCES knowledge_point(id) ON DELETE CASCADE,
    FOREIGN KEY (target_id) REFERENCES knowledge_point(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_knowledge_mastery (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    knowledge_id BIGINT NOT NULL,
    mastery_level DOUBLE DEFAULT 0,
    last_practiced TIMESTAMP,
    practice_count INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (knowledge_id) REFERENCES knowledge_point(id) ON DELETE CASCADE
);

-- 错题本相关表
CREATE TABLE IF NOT EXISTS wrong_book (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) DEFAULT '我的错题本',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS wrong_book_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wrong_book_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    submit_id BIGINT NOT NULL,
    user_answer TEXT,
    correct_answer TEXT,
    explanation TEXT,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    review_status INT DEFAULT 0,
    review_count INT DEFAULT 0,
    FOREIGN KEY (wrong_book_id) REFERENCES wrong_book(id) ON DELETE CASCADE,
    FOREIGN KEY (problem_id) REFERENCES problem(id) ON DELETE CASCADE,
    FOREIGN KEY (submit_id) REFERENCES submit(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    wrong_book_item_id BIGINT NOT NULL,
    review_time TIMESTAMP,
    status INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (wrong_book_item_id) REFERENCES wrong_book_item(id) ON DELETE CASCADE
);

-- AI相关表
CREATE TABLE IF NOT EXISTS ai_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ai_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    FOREIGN KEY (session_id) REFERENCES ai_session(session_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ai_collection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES ai_session(session_id) ON DELETE CASCADE
);

-- 管理员相关表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- 班级相关表
CREATE TABLE IF NOT EXISTS class_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_name VARCHAR(255) NOT NULL,
    teacher_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES user(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES class_info(id) ON DELETE CASCADE
);

-- 审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(255) NOT NULL,
    target_type VARCHAR(100),
    target_id BIGINT,
    details TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL
);

-- 系统监控相关表
CREATE TABLE IF NOT EXISTS system_monitor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cpu_usage DOUBLE,
    memory_usage DOUBLE,
    disk_usage DOUBLE,
    network_traffic DOUBLE,
    timestamp BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS api_metric (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    api_path VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    response_time DOUBLE,
    status_code INT,
    request_count INT DEFAULT 1,
    timestamp BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS sandbox_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sandbox_id VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    cpu_usage DOUBLE,
    memory_usage DOUBLE,
    last_used BIGINT,
    timestamp BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS system_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    level VARCHAR(20) NOT NULL,
    category VARCHAR(100),
    message TEXT NOT NULL,
    details TEXT,
    timestamp BIGINT NOT NULL
);

-- 内容管理相关表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cover_image VARCHAR(255),
    author_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES user(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS course_chapter (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    chapter_name VARCHAR(255) NOT NULL,
    chapter_order INT NOT NULL,
    content TEXT,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS learning_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_name VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    description TEXT,
    uploader_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploader_id) REFERENCES user(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_solution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    code TEXT NOT NULL,
    language VARCHAR(50) NOT NULL,
    explanation TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    audit_status INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (problem_id) REFERENCES problem(id) ON DELETE CASCADE
);

-- 测评相关表
CREATE TABLE IF NOT EXISTS assessment_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_text TEXT NOT NULL,
    options TEXT,
    correct_answer TEXT,
    difficulty INT DEFAULT 0,
    knowledge_point_id BIGINT,
    FOREIGN KEY (knowledge_point_id) REFERENCES knowledge_point(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    assessment_id BIGINT,
    score INT,
    answers TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- 告警相关表
CREATE TABLE IF NOT EXISTS alert_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alert_type VARCHAR(100) NOT NULL,
    threshold DOUBLE,
    enabled BOOLEAN DEFAULT TRUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS alert_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alert_type VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(50),
    status INT DEFAULT 0,
    created_at BIGINT NOT NULL,
    resolved_at BIGINT
);

-- 修改现有表
ALTER TABLE user ADD COLUMN ability_profile TEXT;
ALTER TABLE user ADD COLUMN status INT DEFAULT 1;
ALTER TABLE problem ADD COLUMN chapter_id BIGINT;
ALTER TABLE problem ADD COLUMN level_id BIGINT;
