-- Criação da tabela de avisos e relacionamento com turmas

-- Tabela de avisos
CREATE TABLE IF NOT EXISTS announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    instructor_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcement_instructor FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_announcements_instructor ON announcements(instructor_id);
CREATE INDEX IF NOT EXISTS idx_announcements_created_at ON announcements(created_at DESC);

-- Tabela de relacionamento ManyToMany entre avisos e turmas
CREATE TABLE IF NOT EXISTS announcement_classrooms (
    announcement_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    PRIMARY KEY (announcement_id, classroom_id),
    CONSTRAINT fk_announcement_classrooms_announcement FOREIGN KEY (announcement_id) REFERENCES announcements(id) ON DELETE CASCADE,
    CONSTRAINT fk_announcement_classrooms_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_announcement_classrooms_announcement ON announcement_classrooms(announcement_id);
CREATE INDEX IF NOT EXISTS idx_announcement_classrooms_classroom ON announcement_classrooms(classroom_id);

