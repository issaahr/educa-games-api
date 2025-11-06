DO $$
DECLARE
    admin_exists BOOLEAN;
BEGIN
    SELECT EXISTS(SELECT 1 FROM users WHERE email = 'admin@educagames.com') INTO admin_exists;

    IF NOT admin_exists THEN
        INSERT INTO users (name, email, password, role, active, created_at, updated_at)
        VALUES (
            'Admin Educagames',
            'admin@educagames.com',
            '$2a$10$TEMP.PASSWORD.WILL.BE.UPDATED.BY.SEEDER',
            'ADMIN',
            true,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END IF;
END $$;

