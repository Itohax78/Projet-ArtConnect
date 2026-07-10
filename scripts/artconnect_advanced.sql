-- ============================================================
-- ArtConnect Pro — Fonctionnalités SQL Avancées
-- SGBD : MySQL 8.x
-- Étape 3 — Vues, Index, Triggers, Procédures, Transactions
-- ============================================================

USE artconnect_db;

-- ************************************************************
--  PARTIE 1 : VUES
-- ************************************************************

-- ------------------------------------------------------------
-- VUE 1 : v_artwork_catalog
-- Objectif : SIMPLIFICATION — Joindre artwork + artist + tags
--   pour éviter de répéter les JOIN dans chaque requête UI.
--   Masque les champs internes (artist_id).
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_artwork_catalog AS
SELECT
    a.artwork_id,
    a.title,
    a.creation_year,
    a.type,
    a.medium,
    a.dimensions,
    a.price,
    a.status,
    ar.name           AS artist_name,
    ar.city           AS artist_city,
    GROUP_CONCAT(DISTINCT t.name ORDER BY t.name SEPARATOR ', ') AS tags
FROM artwork a
JOIN artist ar ON a.artist_id = ar.artist_id
LEFT JOIN artwork_tag_association ata ON a.artwork_id = ata.artwork_id
LEFT JOIN artwork_tag t ON ata.tag_id = t.tag_id
GROUP BY a.artwork_id, a.title, a.creation_year, a.type, a.medium,
         a.dimensions, a.price, a.status, ar.name, ar.city;

-- ------------------------------------------------------------
-- VUE 2 : v_exhibition_summary
-- Objectif : SIMPLIFICATION — Résumé complet de chaque
--   exposition avec galerie, nombre d'œuvres et durée.
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_exhibition_summary AS
SELECT
    e.exhibition_id,
    e.title           AS exhibition_title,
    e.theme,
    e.start_date,
    e.end_date,
    DATEDIFF(e.end_date, e.start_date) AS duration_days,
    e.curator_name,
    g.name            AS gallery_name,
    g.address         AS gallery_address,
    g.rating          AS gallery_rating,
    COUNT(ea.artwork_id) AS artwork_count
FROM exhibition e
JOIN gallery g ON e.gallery_id = g.gallery_id
LEFT JOIN exhibition_artwork ea ON e.exhibition_id = ea.exhibition_id
GROUP BY e.exhibition_id, e.title, e.theme, e.start_date, e.end_date,
         e.curator_name, g.name, g.address, g.rating;

-- ------------------------------------------------------------
-- VUE 3 : v_workshop_availability
-- Objectif : SÉCURITÉ / SIMPLIFICATION — Affiche les ateliers
--   avec le nombre de places restantes. Masque les détails
--   financiers sensibles pour les visiteurs non-premium.
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_workshop_availability AS
SELECT
    w.workshop_id,
    w.title,
    w.date,
    w.duration_minutes,
    w.level,
    w.location,
    w.price,
    ar.name              AS instructor_name,
    w.max_participants,
    COUNT(b.booking_id)  AS current_bookings,
    (w.max_participants - COUNT(b.booking_id)) AS spots_remaining
FROM workshop w
JOIN artist ar ON w.instructor_id = ar.artist_id
LEFT JOIN booking b ON w.workshop_id = b.workshop_id
                   AND b.payment_status != 'CANCELLED'
GROUP BY w.workshop_id, w.title, w.date, w.duration_minutes, w.level,
         w.location, w.price, ar.name, w.max_participants;

-- ------------------------------------------------------------
-- VUE 4 : v_member_activity
-- Objectif : MASQUAGE — Résumé de l'activité des membres
--   sans exposer les données personnelles (email, téléphone).
--   Utile pour les statistiques publiques.
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_member_activity AS
SELECT
    cm.member_id,
    cm.name,
    cm.city,
    cm.membership_type,
    COUNT(DISTINCT b.booking_id)  AS total_bookings,
    COUNT(DISTINCT r.review_id)   AS total_reviews,
    COALESCE(AVG(r.rating), 0)    AS avg_rating_given
FROM community_member cm
LEFT JOIN booking b ON cm.member_id = b.member_id
LEFT JOIN review r  ON cm.member_id = r.member_id
GROUP BY cm.member_id, cm.name, cm.city, cm.membership_type;

-- ------------------------------------------------------------
-- VUE 5 : v_artist_portfolio
-- Objectif : SIMPLIFICATION — Vue complète du portfolio
--   d'un artiste avec stats (nb œuvres, note moyenne, ateliers).
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_artist_portfolio AS
SELECT
    ar.artist_id,
    ar.name,
    ar.city,
    ar.is_active,
    GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ', ') AS disciplines,
    COUNT(DISTINCT aw.artwork_id) AS artwork_count,
    COALESCE(AVG(r.rating), 0)    AS avg_artwork_rating,
    COUNT(DISTINCT w.workshop_id) AS workshop_count
FROM artist ar
LEFT JOIN artist_discipline ad ON ar.artist_id = ad.artist_id
LEFT JOIN discipline d ON ad.discipline_id = d.discipline_id
LEFT JOIN artwork aw ON ar.artist_id = aw.artist_id
LEFT JOIN review r ON aw.artwork_id = r.artwork_id
LEFT JOIN workshop w ON ar.artist_id = w.instructor_id
GROUP BY ar.artist_id, ar.name, ar.city, ar.is_active;


-- ************************************************************
--  PARTIE 2 : INDEX
-- ************************************************************

-- ------------------------------------------------------------
-- INDEX 1 : idx_artwork_artist
-- Justification : Optimise la jointure artwork ↔ artist,
--   utilisée dans presque toutes les vues et requêtes UI.
--   La colonne artist_id dans artwork est une FK très sollicitée.
-- ------------------------------------------------------------
CREATE INDEX idx_artwork_artist ON artwork(artist_id);

-- ------------------------------------------------------------
-- INDEX 2 : idx_artwork_status
-- Justification : Le filtrage par statut (FOR_SALE, EXHIBITED, SOLD)
--   est fréquent dans l'interface (onglet Artworks).
-- ------------------------------------------------------------
CREATE INDEX idx_artwork_status ON artwork(status);

-- ------------------------------------------------------------
-- INDEX 3 : idx_exhibition_dates
-- Justification : Les requêtes de type "expositions en cours"
--   filtrent sur start_date et end_date. Un index composite
--   accélère ces requêtes de plage.
-- ------------------------------------------------------------
CREATE INDEX idx_exhibition_dates ON exhibition(start_date, end_date);

-- ------------------------------------------------------------
-- INDEX 4 : idx_booking_workshop
-- Justification : Compter les réservations par atelier est
--   fait à chaque affichage de la vue v_workshop_availability.
-- ------------------------------------------------------------
CREATE INDEX idx_booking_workshop ON booking(workshop_id);

-- ------------------------------------------------------------
-- INDEX 5 : idx_review_artwork
-- Justification : Calculer la note moyenne par œuvre (JOIN
--   review ↔ artwork) est une opération fréquente.
-- ------------------------------------------------------------
CREATE INDEX idx_review_artwork ON review(artwork_id);

-- ------------------------------------------------------------
-- INDEX 6 : idx_artist_city
-- Justification : La recherche d'artistes par ville est une
--   fonctionnalité du filtrage dans l'onglet Artists.
-- ------------------------------------------------------------
CREATE INDEX idx_artist_city ON artist(city);

-- ------------------------------------------------------------
-- INDEX 7 : idx_workshop_date
-- Justification : Tri et filtrage des ateliers par date
--   (ateliers à venir, tri chronologique).
-- ------------------------------------------------------------
CREATE INDEX idx_workshop_date ON workshop(date);


-- ************************************************************
--  PARTIE 3 : TRIGGERS
-- ************************************************************

-- ------------------------------------------------------------
-- TRIGGER 1 : trg_check_booking_capacity
-- Objectif : Empêcher la réservation si l'atelier est complet.
--   Vérifie AVANT l'insertion que le nombre de réservations
--   actives (non-CANCELLED) n'a pas atteint max_participants.
-- ------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_check_booking_capacity
BEFORE INSERT ON booking
FOR EACH ROW
BEGIN
    DECLARE current_count INT;
    DECLARE max_count INT;

    SELECT COUNT(*) INTO current_count
    FROM booking
    WHERE workshop_id = NEW.workshop_id
      AND payment_status != 'CANCELLED';

    SELECT max_participants INTO max_count
    FROM workshop
    WHERE workshop_id = NEW.workshop_id;

    IF current_count >= max_count THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Workshop is fully booked. No more spots available.';
    END IF;
END //
DELIMITER ;

-- ------------------------------------------------------------
-- TRIGGER 2 : trg_check_exhibition_dates
-- Objectif : Vérifier la cohérence des dates d'une exposition.
--   La date de fin doit être postérieure à la date de début.
--   (Complète la contrainte CHECK qui n'est pas toujours
--   appliquée strictement par tous les connecteurs JDBC.)
-- ------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_check_exhibition_dates
BEFORE INSERT ON exhibition
FOR EACH ROW
BEGIN
    IF NEW.end_date < NEW.start_date THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Exhibition end date must be after start date.';
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_check_exhibition_dates_update
BEFORE UPDATE ON exhibition
FOR EACH ROW
BEGIN
    IF NEW.end_date < NEW.start_date THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Exhibition end date must be after start date.';
    END IF;
END //
DELIMITER ;

-- ------------------------------------------------------------
-- TRIGGER 3 : trg_audit_artwork_changes
-- Objectif : Audit — Tracer toute modification de prix ou de
--   statut d'une œuvre dans une table de log.
-- ------------------------------------------------------------

-- Table d'audit
CREATE TABLE IF NOT EXISTS artwork_audit_log (
    log_id        INT AUTO_INCREMENT PRIMARY KEY,
    artwork_id    INT NOT NULL,
    field_changed VARCHAR(50) NOT NULL,
    old_value     VARCHAR(255),
    new_value     VARCHAR(255),
    changed_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by    VARCHAR(100) DEFAULT 'SYSTEM'
) ENGINE=InnoDB;

DELIMITER //
CREATE TRIGGER trg_audit_artwork_changes
AFTER UPDATE ON artwork
FOR EACH ROW
BEGIN
    IF OLD.price != NEW.price THEN
        INSERT INTO artwork_audit_log (artwork_id, field_changed, old_value, new_value)
        VALUES (NEW.artwork_id, 'price',
                CAST(OLD.price AS CHAR), CAST(NEW.price AS CHAR));
    END IF;

    IF OLD.status != NEW.status THEN
        INSERT INTO artwork_audit_log (artwork_id, field_changed, old_value, new_value)
        VALUES (NEW.artwork_id, 'status',
                OLD.status, NEW.status);
    END IF;
END //
DELIMITER ;

-- ------------------------------------------------------------
-- TRIGGER 4 : trg_review_rating_check
-- Objectif : Validation renforcée — S'assurer que le rating
--   est entre 1 et 5 avant insertion (sécurité supplémentaire).
-- ------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_review_rating_check
BEFORE INSERT ON review
FOR EACH ROW
BEGIN
    IF NEW.rating < 1 OR NEW.rating > 5 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Rating must be between 1 and 5.';
    END IF;
END //
DELIMITER ;


-- ************************************************************
--  PARTIE 4 : PROCÉDURES ET FONCTIONS STOCKÉES
-- ************************************************************

-- ------------------------------------------------------------
-- PROCÉDURE 1 : sp_create_exhibition
-- Objectif : Créer une exposition et y inscrire automatiquement
--   des œuvres en une seule opération atomique.
-- Paramètres : titre, dates, galerie, curateur, thème,
--   et une liste d'IDs d'œuvres (séparés par des virgules).
-- ------------------------------------------------------------
DELIMITER //
CREATE PROCEDURE sp_create_exhibition(
    IN p_title VARCHAR(300),
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_gallery_id INT,
    IN p_curator_name VARCHAR(200),
    IN p_theme VARCHAR(200),
    IN p_artwork_ids TEXT   -- IDs séparés par des virgules, ex: '1,4,7'
)
BEGIN
    DECLARE v_exhibition_id INT;
    DECLARE v_artwork_id INT;
    DECLARE v_pos INT DEFAULT 1;
    DECLARE v_len INT;
    DECLARE v_id_str VARCHAR(20);

    -- Créer l'exposition
    INSERT INTO exhibition (title, start_date, end_date, gallery_id, curator_name, theme)
    VALUES (p_title, p_start_date, p_end_date, p_gallery_id, p_curator_name, p_theme);

    SET v_exhibition_id = LAST_INSERT_ID();

    -- Parser la liste d'IDs et associer les œuvres
    IF p_artwork_ids IS NOT NULL AND p_artwork_ids != '' THEN
        SET p_artwork_ids = CONCAT(p_artwork_ids, ',');
        SET v_len = LENGTH(p_artwork_ids);

        WHILE v_pos <= v_len DO
            SET v_id_str = SUBSTRING_INDEX(SUBSTRING(p_artwork_ids, v_pos), ',', 1);
            IF v_id_str != '' THEN
                SET v_artwork_id = CAST(TRIM(v_id_str) AS UNSIGNED);
                INSERT IGNORE INTO exhibition_artwork (exhibition_id, artwork_id)
                VALUES (v_exhibition_id, v_artwork_id);
            END IF;
            SET v_pos = v_pos + LENGTH(v_id_str) + 1;
        END WHILE;
    END IF;

    SELECT v_exhibition_id AS new_exhibition_id;
END //
DELIMITER ;

-- ------------------------------------------------------------
-- PROCÉDURE 2 : sp_book_workshop
-- Objectif : Inscrire un membre à un atelier en vérifiant :
--   - que l'atelier existe et n'est pas passé,
--   - que le membre n'est pas déjà inscrit,
--   - qu'il reste des places.
-- ------------------------------------------------------------
DELIMITER //
CREATE PROCEDURE sp_book_workshop(
    IN p_workshop_id INT,
    IN p_member_id INT,
    OUT p_booking_id INT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_date DATETIME;
    DECLARE v_max INT;
    DECLARE v_current INT;
    DECLARE v_existing INT;

    -- Vérifier que l'atelier existe et récupérer ses infos
    SELECT date, max_participants INTO v_date, v_max
    FROM workshop WHERE workshop_id = p_workshop_id;

    IF v_date IS NULL THEN
        SET p_booking_id = NULL;
        SET p_message = 'Workshop not found.';
    ELSEIF v_date < NOW() THEN
        SET p_booking_id = NULL;
        SET p_message = 'Workshop has already taken place.';
    ELSE
        -- Vérifier si le membre est déjà inscrit
        SELECT COUNT(*) INTO v_existing
        FROM booking
        WHERE workshop_id = p_workshop_id
          AND member_id = p_member_id
          AND payment_status != 'CANCELLED';

        IF v_existing > 0 THEN
            SET p_booking_id = NULL;
            SET p_message = 'Member is already booked for this workshop.';
        ELSE
            -- Vérifier la capacité
            SELECT COUNT(*) INTO v_current
            FROM booking
            WHERE workshop_id = p_workshop_id
              AND payment_status != 'CANCELLED';

            IF v_current >= v_max THEN
                SET p_booking_id = NULL;
                SET p_message = 'Workshop is fully booked.';
            ELSE
                -- Effectuer la réservation
                INSERT INTO booking (booking_date, payment_status, workshop_id, member_id)
                VALUES (NOW(), 'PENDING', p_workshop_id, p_member_id);

                SET p_booking_id = LAST_INSERT_ID();
                SET p_message = 'Booking successful!';
            END IF;
        END IF;
    END IF;
END //
DELIMITER ;

-- ------------------------------------------------------------
-- FONCTION 1 : fn_workshop_participant_count
-- Objectif : Retourner le nombre de participants actifs
--   (non annulés) pour un atelier donné.
-- ------------------------------------------------------------
DELIMITER //
CREATE FUNCTION fn_workshop_participant_count(p_workshop_id INT)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count
    FROM booking
    WHERE workshop_id = p_workshop_id
      AND payment_status != 'CANCELLED';
    RETURN v_count;
END //
DELIMITER ;

-- ------------------------------------------------------------
-- FONCTION 2 : fn_artwork_avg_rating
-- Objectif : Retourner la note moyenne d'une œuvre.
-- ------------------------------------------------------------
DELIMITER //
CREATE FUNCTION fn_artwork_avg_rating(p_artwork_id INT)
RETURNS DECIMAL(3,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_avg DECIMAL(3,2);
    SELECT COALESCE(AVG(rating), 0.00) INTO v_avg
    FROM review
    WHERE artwork_id = p_artwork_id;
    RETURN v_avg;
END //
DELIMITER ;

-- ------------------------------------------------------------
-- FONCTION 3 : fn_artist_revenue
-- Objectif : Calculer le revenu total d'un artiste
--   (somme des prix des œuvres vendues).
-- ------------------------------------------------------------
DELIMITER //
CREATE FUNCTION fn_artist_revenue(p_artist_id INT)
RETURNS DECIMAL(15,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(15,2);
    SELECT COALESCE(SUM(price), 0.00) INTO v_total
    FROM artwork
    WHERE artist_id = p_artist_id
      AND status = 'SOLD';
    RETURN v_total;
END //
DELIMITER ;

-- ------------------------------------------------------------
-- PROCÉDURE 3 : sp_search_artworks
-- Objectif : Recherche multicritères d'œuvres (par titre,
--   artiste, type, statut, fourchette de prix).
-- ------------------------------------------------------------
DELIMITER //
CREATE PROCEDURE sp_search_artworks(
    IN p_title VARCHAR(300),
    IN p_artist_name VARCHAR(200),
    IN p_type VARCHAR(100),
    IN p_status VARCHAR(20),
    IN p_min_price DECIMAL(15,2),
    IN p_max_price DECIMAL(15,2)
)
BEGIN
    SELECT
        a.artwork_id, a.title, a.type, a.price, a.status,
        ar.name AS artist_name
    FROM artwork a
    JOIN artist ar ON a.artist_id = ar.artist_id
    WHERE (p_title IS NULL OR a.title LIKE CONCAT('%', p_title, '%'))
      AND (p_artist_name IS NULL OR ar.name LIKE CONCAT('%', p_artist_name, '%'))
      AND (p_type IS NULL OR a.type = p_type)
      AND (p_status IS NULL OR a.status = p_status)
      AND (p_min_price IS NULL OR a.price >= p_min_price)
      AND (p_max_price IS NULL OR a.price <= p_max_price)
    ORDER BY a.title;
END //
DELIMITER ;


-- ************************************************************
--  PARTIE 5 : TRANSACTIONS
-- ************************************************************

-- ------------------------------------------------------------
-- SCÉNARIO TRANSACTIONNEL 1 :
-- Inscription d'un membre à plusieurs ateliers en une
-- seule opération atomique.
--
-- Contexte : Un nouveau membre premium (Sophia) s'inscrit et
-- réserve 3 ateliers d'un coup. Si l'une des réservations
-- échoue (atelier complet, par exemple), TOUT est annulé
-- (le membre n'est pas créé, aucune réservation).
-- ------------------------------------------------------------

DELIMITER //
CREATE PROCEDURE sp_register_and_book_multiple(
    IN p_name VARCHAR(200),
    IN p_email VARCHAR(200),
    IN p_city VARCHAR(100),
    IN p_membership VARCHAR(50),
    IN p_workshop_ids TEXT   -- IDs séparés par virgules, ex: '1,2,5'
)
BEGIN
    DECLARE v_member_id INT;
    DECLARE v_workshop_id INT;
    DECLARE v_pos INT DEFAULT 1;
    DECLARE v_len INT;
    DECLARE v_id_str VARCHAR(20);
    DECLARE v_max INT;
    DECLARE v_current INT;
    DECLARE v_ws_date DATETIME;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Transaction failed — all operations rolled back.';
    END;

    START TRANSACTION;

    -- 1. Créer le membre
    INSERT INTO community_member (name, email, city, membership_type)
    VALUES (p_name, p_email, p_city, p_membership);
    SET v_member_id = LAST_INSERT_ID();

    -- 2. Parser et réserver chaque atelier
    IF p_workshop_ids IS NOT NULL AND p_workshop_ids != '' THEN
        SET p_workshop_ids = CONCAT(p_workshop_ids, ',');
        SET v_len = LENGTH(p_workshop_ids);

        WHILE v_pos <= v_len DO
            SET v_id_str = SUBSTRING_INDEX(SUBSTRING(p_workshop_ids, v_pos), ',', 1);
            IF v_id_str != '' THEN
                SET v_workshop_id = CAST(TRIM(v_id_str) AS UNSIGNED);

                -- Vérifier que l'atelier existe et n'est pas passé
                SELECT date, max_participants INTO v_ws_date, v_max
                FROM workshop WHERE workshop_id = v_workshop_id;

                IF v_ws_date IS NULL THEN
                    SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT = 'One of the workshops does not exist.';
                END IF;

                IF v_ws_date < NOW() THEN
                    SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT = 'One of the workshops has already taken place.';
                END IF;

                -- Vérifier la capacité
                SELECT COUNT(*) INTO v_current
                FROM booking
                WHERE workshop_id = v_workshop_id
                  AND payment_status != 'CANCELLED';

                IF v_current >= v_max THEN
                    SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT = 'One of the workshops is fully booked.';
                END IF;

                -- Créer la réservation
                INSERT INTO booking (booking_date, payment_status, workshop_id, member_id)
                VALUES (NOW(), 'PENDING', v_workshop_id, v_member_id);
            END IF;
            SET v_pos = v_pos + LENGTH(v_id_str) + 1;
        END WHILE;
    END IF;

    COMMIT;

    SELECT v_member_id AS new_member_id, 'Registration and bookings successful!' AS message;
END //
DELIMITER ;


-- ************************************************************
--  PARTIE 6 : SCRIPTS DE TEST
-- ************************************************************

-- === Test des vues ===
-- SELECT * FROM v_artwork_catalog LIMIT 5;
-- SELECT * FROM v_exhibition_summary;
-- SELECT * FROM v_workshop_availability;
-- SELECT * FROM v_member_activity;
-- SELECT * FROM v_artist_portfolio;

-- === Test des fonctions ===
-- SELECT fn_workshop_participant_count(1) AS participants_oil_painting;
-- SELECT fn_artwork_avg_rating(1) AS avg_rating_mona_lisa;
-- SELECT fn_artist_revenue(7) AS banksy_revenue;

-- === Test de la procédure de réservation ===
-- CALL sp_book_workshop(1, 3, @bid, @msg);
-- SELECT @bid AS booking_id, @msg AS message;

-- === Test de la recherche multicritères ===
-- CALL sp_search_artworks(NULL, 'Monet', NULL, NULL, NULL, NULL);
-- CALL sp_search_artworks(NULL, NULL, 'Painting', 'FOR_SALE', 1000000, 200000000);

-- === Test du trigger d'audit (modifier le prix d'une œuvre) ===
-- UPDATE artwork SET price = 900000000.00 WHERE artwork_id = 1;
-- SELECT * FROM artwork_audit_log;

-- === Test du trigger de capacité ===
-- (va échouer si l'atelier 6 est complet, max_participants = 10)
-- INSERT INTO booking (payment_status, workshop_id, member_id) VALUES ('PENDING', 6, 12);

-- === Test de la transaction multi-inscription ===
-- CALL sp_register_and_book_multiple(
--     'Sophia Laurent', 'sophia@artfan.fr', 'Nice', 'premium', '1,4,5'
-- );

-- === Test de la procédure de création d'exposition ===
-- CALL sp_create_exhibition(
--     'Masters of Light', '2026-07-01', '2026-12-31', 1,
--     'Dr. Philippe Blanc', 'Renaissance Light', '1,4,5'
-- );
