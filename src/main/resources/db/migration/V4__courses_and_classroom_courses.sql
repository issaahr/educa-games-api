-- Criação das tabelas de cursos e vínculo many-to-many com turmas
-- Compatível com Postgres e com as entidades do projeto

-- Tabela de cursos
CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description TEXT,
    instructor_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_instructor FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_courses_instructor ON courses(instructor_id);

-- Tabela de junção curso <-> turma
CREATE TABLE IF NOT EXISTS classroom_courses (
    course_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    CONSTRAINT fk_classroom_courses_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_classroom_courses_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    CONSTRAINT uk_classroom_courses UNIQUE (course_id, classroom_id)
);

CREATE INDEX IF NOT EXISTS idx_classroom_courses_course ON classroom_courses(course_id);
CREATE INDEX IF NOT EXISTS idx_classroom_courses_classroom ON classroom_courses(classroom_id);