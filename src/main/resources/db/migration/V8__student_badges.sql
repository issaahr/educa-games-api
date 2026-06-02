CREATE TABLE IF NOT EXISTS student_badges (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    badge_name VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    badge_order INTEGER NOT NULL,
    earned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_badge_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_badge_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    CONSTRAINT uk_student_badge UNIQUE (student_id, classroom_id, badge_name)
);

CREATE INDEX IF NOT EXISTS idx_student_badges_student ON student_badges(student_id);
CREATE INDEX IF NOT EXISTS idx_student_badges_classroom ON student_badges(classroom_id);
CREATE INDEX IF NOT EXISTS idx_student_badges_category ON student_badges(category);
CREATE INDEX IF NOT EXISTS idx_student_badges_name ON student_badges(badge_name);

