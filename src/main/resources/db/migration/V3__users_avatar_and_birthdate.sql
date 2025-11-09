DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'image_url'
    )
    AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'avatar_url'
    )
    THEN
        EXECUTE 'ALTER TABLE public.users RENAME COLUMN image_url TO avatar_url';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'birth_date'
          AND data_type IN ('timestamp without time zone', 'timestamp with time zone')
    ) THEN
        EXECUTE 'ALTER TABLE public.users ALTER COLUMN birth_date TYPE DATE USING birth_date::date';
    END IF;
END $$;
