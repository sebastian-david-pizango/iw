-- Ejecutar UNA vez en Neon (todo es idempotente: IF NOT EXISTS).
-- users / authorities: esquema estandar que exige JdbcUserDetailsManager.
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50)  NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled  BOOLEAN      NOT NULL
);
CREATE TABLE IF NOT EXISTS authorities (
    username  VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users (username)
);
CREATE UNIQUE INDEX IF NOT EXISTS ix_auth_username ON authorities (username, authority);

-- Personalizacion: datos adicionales del usuario.
CREATE TABLE IF NOT EXISTS user_profile (
    username  VARCHAR(50)  PRIMARY KEY REFERENCES users (username) ON DELETE CASCADE,
    email     VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    created   TIMESTAMP    NOT NULL DEFAULT now()
);
