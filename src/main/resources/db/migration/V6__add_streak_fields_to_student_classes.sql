-- Adiciona campos de streak e acesso à tabela student_classes
ALTER TABLE student_classes
ADD COLUMN IF NOT EXISTS actual_login_streak INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS longest_login_streak INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS last_streak_date DATE,
ADD COLUMN IF NOT EXISTS last_access_at TIMESTAMP;

