-- ============================================================
-- ArtConnect Pro — Script de création de la base de données
-- SGBD : MySQL 8.x
-- Étape 2 — Modélisation et création du schéma
-- ============================================================

DROP DATABASE IF EXISTS artconnect_db;
CREATE DATABASE artconnect_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE artconnect_db;

-- ============================================================
-- 1. Tables sans dépendances (entités indépendantes)
-- ============================================================

CREATE TABLE discipline (
    discipline_id   INT             AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE artwork_tag (
    tag_id          INT             AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE artist (
    artist_id       INT             AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL,
    bio             TEXT,
    birth_year      INT,
    contact_email   VARCHAR(200),
    phone           VARCHAR(50),
    city            VARCHAR(100),
    website         VARCHAR(255),
    social_media    VARCHAR(255),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_artist_birth_year CHECK (birth_year IS NULL OR birth_year > 0)
) ENGINE=InnoDB;

CREATE TABLE gallery (
    gallery_id      INT             AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL,
    address         VARCHAR(300),
    owner_name      VARCHAR(200),
    opening_hours   VARCHAR(200),
    contact_phone   VARCHAR(50),
    rating          DECIMAL(2,1)    DEFAULT 0.0,
    website         VARCHAR(255),

    CONSTRAINT chk_gallery_rating CHECK (rating >= 0.0 AND rating <= 5.0)
) ENGINE=InnoDB;

CREATE TABLE community_member (
    member_id       INT             AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL,
    email           VARCHAR(200)    NOT NULL UNIQUE,
    birth_year      INT,
    phone           VARCHAR(50),
    city            VARCHAR(100),
    membership_type VARCHAR(50)     NOT NULL DEFAULT 'free',

    CONSTRAINT chk_member_membership CHECK (membership_type IN ('free', 'premium'))
) ENGINE=InnoDB;

-- ============================================================
-- 2. Tables avec dépendances simples (FK vers tables ci-dessus)
-- ============================================================

CREATE TABLE artwork (
    artwork_id      INT             AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(300)    NOT NULL,
    creation_year   INT,
    type            VARCHAR(100),
    medium          VARCHAR(100),
    dimensions      VARCHAR(100),
    description     TEXT,
    price           DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    status          ENUM('FOR_SALE','SOLD','EXHIBITED') NOT NULL DEFAULT 'FOR_SALE',
    artist_id       INT             NOT NULL,

    CONSTRAINT fk_artwork_artist
        FOREIGN KEY (artist_id) REFERENCES artist(artist_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_artwork_price CHECK (price >= 0)
) ENGINE=InnoDB;

CREATE TABLE exhibition (
    exhibition_id   INT             AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(300)    NOT NULL,
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    description     TEXT,
    curator_name    VARCHAR(200),
    theme           VARCHAR(200),
    gallery_id      INT             NOT NULL,

    CONSTRAINT fk_exhibition_gallery
        FOREIGN KEY (gallery_id) REFERENCES gallery(gallery_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_exhibition_dates CHECK (end_date >= start_date)
) ENGINE=InnoDB;

CREATE TABLE workshop (
    workshop_id     INT             AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(300)    NOT NULL,
    date            DATETIME        NOT NULL,
    duration_minutes INT            DEFAULT 60,
    max_participants INT            DEFAULT 10,
    price           DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    location        VARCHAR(300),
    description     TEXT,
    level           ENUM('Beginner','Intermediate','Advanced') NOT NULL DEFAULT 'Beginner',
    instructor_id   INT             NOT NULL,

    CONSTRAINT fk_workshop_instructor
        FOREIGN KEY (instructor_id) REFERENCES artist(artist_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_workshop_price CHECK (price >= 0),
    CONSTRAINT chk_workshop_duration CHECK (duration_minutes > 0),
    CONSTRAINT chk_workshop_participants CHECK (max_participants > 0)
) ENGINE=InnoDB;

-- ============================================================
-- 3. Entités associatives (Booking, Review)
-- ============================================================

CREATE TABLE booking (
    booking_id      INT             AUTO_INCREMENT PRIMARY KEY,
    booking_date    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_status  ENUM('PENDING','PAID','CANCELLED') NOT NULL DEFAULT 'PENDING',
    workshop_id     INT             NOT NULL,
    member_id       INT             NOT NULL,

    CONSTRAINT fk_booking_workshop
        FOREIGN KEY (workshop_id) REFERENCES workshop(workshop_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_booking_member
        FOREIGN KEY (member_id) REFERENCES community_member(member_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT uq_booking_member_workshop UNIQUE (workshop_id, member_id)
) ENGINE=InnoDB;

CREATE TABLE review (
    review_id       INT             AUTO_INCREMENT PRIMARY KEY,
    rating          INT             NOT NULL,
    comment         TEXT,
    review_date     DATE            NOT NULL DEFAULT (CURRENT_DATE),
    member_id       INT             NOT NULL,
    artwork_id      INT             NOT NULL,

    CONSTRAINT fk_review_member
        FOREIGN KEY (member_id) REFERENCES community_member(member_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_review_artwork
        FOREIGN KEY (artwork_id) REFERENCES artwork(artwork_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_review_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT uq_review_member_artwork UNIQUE (member_id, artwork_id)
) ENGINE=InnoDB;

-- ============================================================
-- 4. Tables de jonction (relations N:M)
-- ============================================================

CREATE TABLE artist_discipline (
    artist_id       INT NOT NULL,
    discipline_id   INT NOT NULL,

    PRIMARY KEY (artist_id, discipline_id),

    CONSTRAINT fk_ad_artist
        FOREIGN KEY (artist_id) REFERENCES artist(artist_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ad_discipline
        FOREIGN KEY (discipline_id) REFERENCES discipline(discipline_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE member_favorite_discipline (
    member_id       INT NOT NULL,
    discipline_id   INT NOT NULL,

    PRIMARY KEY (member_id, discipline_id),

    CONSTRAINT fk_mfd_member
        FOREIGN KEY (member_id) REFERENCES community_member(member_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_mfd_discipline
        FOREIGN KEY (discipline_id) REFERENCES discipline(discipline_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE exhibition_artwork (
    exhibition_id   INT NOT NULL,
    artwork_id      INT NOT NULL,

    PRIMARY KEY (exhibition_id, artwork_id),

    CONSTRAINT fk_ea_exhibition
        FOREIGN KEY (exhibition_id) REFERENCES exhibition(exhibition_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ea_artwork
        FOREIGN KEY (artwork_id) REFERENCES artwork(artwork_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE artwork_tag_association (
    artwork_id      INT NOT NULL,
    tag_id          INT NOT NULL,

    PRIMARY KEY (artwork_id, tag_id),

    CONSTRAINT fk_ata_artwork
        FOREIGN KEY (artwork_id) REFERENCES artwork(artwork_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ata_tag
        FOREIGN KEY (tag_id) REFERENCES artwork_tag(tag_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Vérification : liste des tables créées
-- ============================================================
SHOW TABLES;
