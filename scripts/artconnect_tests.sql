-- ============================================================
-- ArtConnect Pro — Script de Tests
-- Étape 3 — Tests des fonctionnalités avancées
-- ============================================================

USE artconnect_db;

-- ============================================================
-- TEST 1 : Vues
-- ============================================================

SELECT '=== TEST VUES ===' AS section;

-- Vue catalogue d'œuvres
SELECT '--- v_artwork_catalog (œuvres à vendre) ---' AS test;
SELECT title, artist_name, price, status, tags
FROM v_artwork_catalog
WHERE status = 'FOR_SALE'
ORDER BY price DESC;

-- Vue résumé des expositions
SELECT '--- v_exhibition_summary ---' AS test;
SELECT exhibition_title, gallery_name, artwork_count, duration_days
FROM v_exhibition_summary
ORDER BY start_date;

-- Vue disponibilité des ateliers
SELECT '--- v_workshop_availability ---' AS test;
SELECT title, instructor_name, spots_remaining, price
FROM v_workshop_availability
ORDER BY date;

-- Vue activité des membres
SELECT '--- v_member_activity ---' AS test;
SELECT name, total_bookings, total_reviews, avg_rating_given
FROM v_member_activity
ORDER BY total_bookings DESC
LIMIT 5;

-- Vue portfolio artiste
SELECT '--- v_artist_portfolio ---' AS test;
SELECT name, disciplines, artwork_count, avg_artwork_rating, workshop_count
FROM v_artist_portfolio
WHERE is_active = TRUE
ORDER BY avg_artwork_rating DESC;

-- ============================================================
-- TEST 2 : Fonctions stockées
-- ============================================================

SELECT '=== TEST FONCTIONS ===' AS section;

-- Nombre de participants à l'atelier "Oil Painting" (id=1)
SELECT fn_workshop_participant_count(1) AS participants_oil_painting;

-- Note moyenne de Mona Lisa (id=1)
SELECT fn_artwork_avg_rating(1) AS avg_rating_mona_lisa;

-- Note moyenne de Water Lilies (id=4)
SELECT fn_artwork_avg_rating(4) AS avg_rating_water_lilies;

-- Revenu de Banksy (id=7) — 2 œuvres vendues
SELECT fn_artist_revenue(7) AS banksy_revenue;

-- Revenu de Monet (id=2) — 0 œuvre vendue
SELECT fn_artist_revenue(2) AS monet_revenue;

-- ============================================================
-- TEST 3 : Procédure de réservation
-- ============================================================

SELECT '=== TEST PROCÉDURE sp_book_workshop ===' AS section;

-- Cas 1 : Réservation normale (Charlie → Oil Painting)
CALL sp_book_workshop(1, 3, @bid, @msg);
SELECT @bid AS booking_id, @msg AS message;

-- Cas 2 : Tentative de doublon (Charlie → Oil Painting, déjà inscrit)
CALL sp_book_workshop(1, 3, @bid2, @msg2);
SELECT @bid2 AS booking_id, @msg2 AS message;

-- ============================================================
-- TEST 4 : Recherche multicritères
-- ============================================================

SELECT '=== TEST PROCÉDURE sp_search_artworks ===' AS section;

-- Toutes les peintures de Monet
CALL sp_search_artworks(NULL, 'Monet', NULL, NULL, NULL, NULL);

-- Peintures à vendre entre 1M et 200M
CALL sp_search_artworks(NULL, NULL, 'Painting', 'FOR_SALE', 1000000, 200000000);

-- Recherche par titre partiel
CALL sp_search_artworks('Water', NULL, NULL, NULL, NULL, NULL);

-- ============================================================
-- TEST 5 : Trigger d'audit
-- ============================================================

SELECT '=== TEST TRIGGER AUDIT ===' AS section;

-- Modifier le prix de Mona Lisa
UPDATE artwork SET price = 900000000.00 WHERE artwork_id = 1;

-- Modifier le statut de Haystacks (id=6)
UPDATE artwork SET status = 'SOLD' WHERE artwork_id = 6;

-- Vérifier le log
SELECT * FROM artwork_audit_log ORDER BY changed_at;

-- Remettre les valeurs originales
UPDATE artwork SET price = 850000000.00 WHERE artwork_id = 1;
UPDATE artwork SET status = 'FOR_SALE' WHERE artwork_id = 6;

-- ============================================================
-- TEST 6 : Procédure de création d'exposition
-- ============================================================

SELECT '=== TEST PROCÉDURE sp_create_exhibition ===' AS section;

CALL sp_create_exhibition(
    'Masters of Light', '2026-07-01', '2026-12-31', 1,
    'Dr. Philippe Blanc', 'Renaissance Light', '1,4,5'
);

-- Vérifier la nouvelle exposition
SELECT * FROM v_exhibition_summary WHERE exhibition_title = 'Masters of Light';

-- ============================================================
-- TEST 7 : Transaction multi-inscription
-- ============================================================

SELECT '=== TEST TRANSACTION sp_register_and_book_multiple ===' AS section;

-- Compter avant
SELECT COUNT(*) AS members_before FROM community_member;
SELECT COUNT(*) AS bookings_before FROM booking;

-- Inscrire Sophia et réserver 3 ateliers
CALL sp_register_and_book_multiple(
    'Sophia Laurent', 'sophia@artfan.fr', 'Nice', 'premium', '1,4,5'
);

-- Compter après
SELECT COUNT(*) AS members_after FROM community_member;
SELECT COUNT(*) AS bookings_after FROM booking;

-- Vérifier les données de Sophia
SELECT * FROM community_member WHERE email = 'sophia@artfan.fr';
SELECT b.*, w.title AS workshop_title
FROM booking b
JOIN workshop w ON b.workshop_id = w.workshop_id
WHERE b.member_id = (
    SELECT member_id FROM community_member WHERE email = 'sophia@artfan.fr'
);

-- ============================================================
-- TEST 8 : Trigger de vérification des dates d'exposition
-- ============================================================

SELECT '=== TEST TRIGGER DATES EXPOSITION ===' AS section;

-- Cette insertion DOIT échouer (end_date avant start_date)
-- Décommenter pour tester :
-- INSERT INTO exhibition (title, start_date, end_date, gallery_id)
-- VALUES ('Bad Exhibition', '2026-12-31', '2026-01-01', 1);

SELECT '--- Tous les tests sont terminés ---' AS result;
