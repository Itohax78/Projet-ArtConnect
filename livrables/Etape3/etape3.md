# Étape 3 — Implémentation de la Base de Données et Fonctionnalités Avancées



## 1. Création et Remplissage de la Base

### 1.1 Prompt utilisé pour la génération des données

Le prompt suivant a été utilisé pour guider la génération des données d'exemple :

> « Génère un script SQL d'insertion de données d'exemple pour la base ArtConnect Pro. La base contient les tables : discipline, artwork_tag, artist, artwork, gallery, exhibition, workshop, community_member, booking, review, et les tables de jonction artist_discipline, member_favorite_discipline, exhibition_artwork, artwork_tag_association.
>
> Génère au moins :
> - 8 disciplines et 10 tags
> - 10 artistes (inspirés de vrais artistes historiques avec des données fictives adaptées), dont certains inactifs
> - 19 œuvres variées (peinture, sculpture, photo, street art, performance) avec des statuts mixtes (FOR_SALE, SOLD, EXHIBITED)
> - 5 galeries dans différentes villes du monde
> - 7 expositions avec des thèmes variés et des associations d'œuvres
> - 8 ateliers avec différents niveaux et instructeurs
> - 12 membres avec un mix de free/premium dans différentes villes
> - 18 réservations avec des statuts variés (PAID, PENDING, CANCELLED)
> - 18 avis avec des notes de 3 à 5 et des commentaires réalistes
>
> Assure des cas intéressants : participations croisées (un membre inscrit à plusieurs ateliers), un artiste animant plusieurs ateliers, des œuvres apparaissant dans plusieurs expositions, etc. »

### 1.2 Résumé des données insérées

| Table | Nombre d'enregistrements | Remarques |
|---|---|---|
| discipline | 8 | Painting, Sculpture, Photography, Digital Art, Music, Ceramics, Printmaking, Textile Art |
| artwork_tag | 10 | Renaissance, Impressionism, Modern, Abstract, Portrait, Landscape, etc. |
| artist | 10 | 8 actifs, 2 inactifs (O'Keeffe, Basquiat) |
| artist_discipline | 13 | Kusama a 3 disciplines, Leonardo et Banksy en ont 2 chacun |
| artwork | 19 | Mix de statuts : 7 EXHIBITED, 6 FOR_SALE, 5 SOLD, 1 à prix 0 (performance) |
| artwork_tag_association | 27 | Plusieurs tags par œuvre |
| gallery | 5 | Paris, London, New York, Tokyo, Mexico City |
| exhibition | 7 | Thèmes variés, durées de 2.5 à 5 mois |
| exhibition_artwork | 17 | L'œuvre "Pumpkin" apparaît dans 2 expositions |
| workshop | 8 | 3 niveaux, Leonardo anime 2 ateliers |
| community_member | 12 | 6 premium, 6 free |
| member_favorite_discipline | 18 | 1 à 2 disciplines par membre |
| booking | 18 | 12 PAID, 4 PENDING, 1 CANCELLED ; Alice inscrite à 3 ateliers |
| review | 18 | Notes de 3 à 5, Alice et Greta ont chacune 2 avis |

### 1.3 Cas intéressants dans les données

- **Participations croisées** : Alice est inscrite aux ateliers 1, 2 et 8 (3 réservations).
- **Artiste multi-ateliers** : Leonardo Vinci anime les ateliers 1 et 8.
- **Œuvre multi-expositions** : "Pumpkin" de Kusama apparaît dans "Sculpting the Soul" ET "Infinity and Beyond".
- **Artiste multi-disciplines** : Kusama pratique Painting, Sculpture et Digital Art.
- **Réservation annulée** : Charlie a annulé sa réservation au Neo-Expressionist workshop.
- **Membre avec multiple reviews** : Alice a reviewé Mona Lisa et Jimson Weed ; Greta a reviewé Monolith et Moonrise.



## 2. Vues

### Vue 1 : `v_artwork_catalog`
**Objectif : SIMPLIFICATION** — Fournit un catalogue complet des œuvres avec le nom de l'artiste et les tags concaténés, évitant de répéter les 3 JOIN (artwork → artist, artwork → artwork_tag_association → artwork_tag) dans chaque requête.

```sql
SELECT * FROM v_artwork_catalog WHERE status = 'FOR_SALE' ORDER BY price DESC;
```

### Vue 2 : `v_exhibition_summary`
**Objectif : SIMPLIFICATION** — Résumé de chaque exposition avec la galerie, la durée en jours et le nombre d'œuvres exposées. Utilisée dans l'onglet Exhibitions de l'UI.

```sql
SELECT * FROM v_exhibition_summary WHERE gallery_name = 'Louvre Art House';
```

### Vue 3 : `v_workshop_availability`
**Objectif : SÉCURITÉ / SIMPLIFICATION** — Calcule en temps réel les places restantes pour chaque atelier. Permet d'afficher la disponibilité sans exposer les détails des réservations individuelles.

```sql
SELECT title, spots_remaining FROM v_workshop_availability WHERE spots_remaining > 0;
```

### Vue 4 : `v_member_activity`
**Objectif : MASQUAGE** — Résumé de l'activité des membres (nombre de réservations, nombre d'avis, note moyenne donnée) sans exposer les données personnelles sensibles (email, téléphone). Utilisable pour des statistiques publiques.

```sql
SELECT * FROM v_member_activity ORDER BY total_reviews DESC;
```

### Vue 5 : `v_artist_portfolio`
**Objectif : SIMPLIFICATION** — Portfolio complet d'un artiste avec ses disciplines, nombre d'œuvres, note moyenne et nombre d'ateliers. Évite de multiplier les sous-requêtes dans le code Java.

```sql
SELECT * FROM v_artist_portfolio WHERE is_active = TRUE ORDER BY avg_artwork_rating DESC;
```



## 3. Index

| Index | Table | Colonne(s) | Justification |
|---|---|---|---|
| `idx_artwork_artist` | artwork | artist_id | Optimise la jointure artwork ↔ artist, utilisée dans toutes les vues et l'affichage UI |
| `idx_artwork_status` | artwork | status | Accélère le filtrage par statut (FOR_SALE/SOLD/EXHIBITED) dans l'onglet Artworks |
| `idx_exhibition_dates` | exhibition | (start_date, end_date) | Index composite pour les requêtes "expositions en cours" (filtrage de plage) |
| `idx_booking_workshop` | booking | workshop_id | Accélère le comptage des réservations par atelier (vue v_workshop_availability) |
| `idx_review_artwork` | review | artwork_id | Optimise le calcul de la note moyenne par œuvre |
| `idx_artist_city` | artist | city | Accélère la recherche/filtrage d'artistes par ville (fonctionnalité ArtistController) |
| `idx_workshop_date` | workshop | date | Optimise le tri chronologique et le filtrage des ateliers à venir |



## 4. Déclencheurs (Triggers)

### Trigger 1 : `trg_check_booking_capacity`
**Type :** BEFORE INSERT sur `booking`

**Objectif :** Empêcher la réservation si l'atelier a atteint sa capacité maximale. Compte les réservations actives (non annulées) et compare avec `max_participants`.

**Comportement :**
- Si `current_count >= max_participants` → lève une erreur SQLSTATE 45000.
- Les réservations avec `payment_status = 'CANCELLED'` ne sont pas comptées.

### Trigger 2 : `trg_check_exhibition_dates` / `trg_check_exhibition_dates_update`
**Type :** BEFORE INSERT et BEFORE UPDATE sur `exhibition`

**Objectif :** Garantir que la date de fin d'une exposition est toujours postérieure à la date de début. Double sécurité avec la contrainte CHECK.

### Trigger 3 : `trg_audit_artwork_changes`
**Type :** AFTER UPDATE sur `artwork`

**Objectif :** Audit — enregistre dans `artwork_audit_log` toute modification du prix ou du statut d'une œuvre. Permet de tracer l'historique des changements.

**Table de log créée :**
```
artwork_audit_log (log_id, artwork_id, field_changed, old_value, new_value, changed_at, changed_by)
```

### Trigger 4 : `trg_review_rating_check`
**Type :** BEFORE INSERT sur `review`

**Objectif :** Validation renforcée du rating (entre 1 et 5). Couche de sécurité supplémentaire en plus de la contrainte CHECK.



## 5. Procédures et Fonctions Stockées

### Procédure 1 : `sp_create_exhibition`
**Objectif :** Créer une exposition et associer automatiquement des œuvres en une seule opération.

**Paramètres :**
- `p_title`, `p_start_date`, `p_end_date`, `p_gallery_id`, `p_curator_name`, `p_theme`
- `p_artwork_ids` : liste d'IDs séparés par des virgules (ex: `'1,4,7'`)

**Exemple d'appel :**
```sql
CALL sp_create_exhibition('Masters of Light', '2026-07-01', '2026-12-31', 1,
    'Dr. Philippe Blanc', 'Renaissance Light', '1,4,5');
```

### Procédure 2 : `sp_book_workshop`
**Objectif :** Inscrire un membre à un atelier avec toutes les vérifications métier (existence, date, doublon, capacité).

**Paramètres IN/OUT :**
- IN : `p_workshop_id`, `p_member_id`
- OUT : `p_booking_id`, `p_message`

**Exemple d'appel :**
```sql
CALL sp_book_workshop(1, 3, @bid, @msg);
SELECT @bid AS booking_id, @msg AS message;
```

### Procédure 3 : `sp_search_artworks`
**Objectif :** Recherche multicritères d'œuvres (titre, artiste, type, statut, fourchette de prix). Tous les paramètres sont optionnels (NULL = pas de filtre).

**Exemple d'appel :**
```sql
CALL sp_search_artworks(NULL, 'Monet', NULL, NULL, NULL, NULL);
CALL sp_search_artworks(NULL, NULL, 'Painting', 'FOR_SALE', 1000000, 200000000);
```

### Fonction 1 : `fn_workshop_participant_count(p_workshop_id)`
**Retourne :** INT — nombre de participants actifs (non annulés) pour un atelier.

### Fonction 2 : `fn_artwork_avg_rating(p_artwork_id)`
**Retourne :** DECIMAL(3,2) — note moyenne d'une œuvre (0.00 si aucun avis).

### Fonction 3 : `fn_artist_revenue(p_artist_id)`
**Retourne :** DECIMAL(15,2) — revenu total d'un artiste (somme des prix des œuvres vendues).



## 6. Transactions

### Scénario transactionnel : `sp_register_and_book_multiple`

**Contexte :** Un nouveau membre souhaite s'inscrire sur la plateforme et réserver plusieurs ateliers en une seule opération. Si l'une des étapes échoue, TOUT doit être annulé (atomicité).

**Opérations atomiques :**
1. Création du membre dans `community_member`
2. Pour chaque atelier de la liste :
   - Vérification de l'existence de l'atelier
   - Vérification que l'atelier n'est pas passé
   - Vérification de la capacité disponible
   - Création de la réservation

**Gestion des erreurs :** Un `EXIT HANDLER FOR SQLEXCEPTION` effectue un `ROLLBACK` automatique si n'importe quelle étape échoue.

**Exemple d'appel :**
```sql
CALL sp_register_and_book_multiple(
    'Sophia Laurent', 'sophia@artfan.fr', 'Nice', 'premium', '1,4,5'
);
```

**Cas de test pour le ROLLBACK :**
Si on tente de réserver un atelier complet (par exemple atelier 6 si ses 10 places sont prises), la transaction entière est annulée : Sophia n'est pas créée et aucune réservation n'existe.

### Script de test des transactions

```sql
-- Avant la transaction
SELECT COUNT(*) AS members_before FROM community_member;
SELECT COUNT(*) AS bookings_before FROM booking;

-- Exécuter la transaction
CALL sp_register_and_book_multiple(
    'Sophia Laurent', 'sophia@artfan.fr', 'Nice', 'premium', '1,4,5'
);

-- Après la transaction (vérifier les nouvelles lignes)
SELECT COUNT(*) AS members_after FROM community_member;
SELECT COUNT(*) AS bookings_after FROM booking;
SELECT * FROM community_member WHERE email = 'sophia@artfan.fr';
SELECT * FROM booking WHERE member_id = (
    SELECT member_id FROM community_member WHERE email = 'sophia@artfan.fr'
);
```



## 7. Récapitulatif des Objets Créés

| Type | Nombre | Noms |
|---|---|---|
| **Vues** | 5 | v_artwork_catalog, v_exhibition_summary, v_workshop_availability, v_member_activity, v_artist_portfolio |
| **Index** | 7 | idx_artwork_artist, idx_artwork_status, idx_exhibition_dates, idx_booking_workshop, idx_review_artwork, idx_artist_city, idx_workshop_date |
| **Triggers** | 4 | trg_check_booking_capacity, trg_check_exhibition_dates (+ update), trg_audit_artwork_changes, trg_review_rating_check |
| **Procédures** | 4 | sp_create_exhibition, sp_book_workshop, sp_search_artworks, sp_register_and_book_multiple |
| **Fonctions** | 3 | fn_workshop_participant_count, fn_artwork_avg_rating, fn_artist_revenue |
| **Table d'audit** | 1 | artwork_audit_log |

Le sujet exige au minimum 3 objets de chaque type — tous les minimums sont dépassés.
