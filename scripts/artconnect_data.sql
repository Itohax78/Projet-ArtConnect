-- ============================================================
-- ArtConnect Pro — Script d'insertion de données d'exemple
-- SGBD : MySQL 8.x
-- Étape 3 — Données d'exemple
-- ============================================================

USE artconnect_db;

-- ============================================================
-- 1. Disciplines
-- ============================================================
INSERT INTO discipline (name) VALUES
    ('Painting'),
    ('Sculpture'),
    ('Photography'),
    ('Digital Art'),
    ('Music'),
    ('Ceramics'),
    ('Printmaking'),
    ('Textile Art');

-- ============================================================
-- 2. Tags pour les œuvres
-- ============================================================
INSERT INTO artwork_tag (name) VALUES
    ('Renaissance'),
    ('Impressionism'),
    ('Modern'),
    ('Abstract'),
    ('Portrait'),
    ('Landscape'),
    ('Still Life'),
    ('Surrealism'),
    ('Realism'),
    ('Contemporary');

-- ============================================================
-- 3. Artistes
-- ============================================================
INSERT INTO artist (name, bio, birth_year, contact_email, phone, city, website, social_media, is_active) VALUES
    ('Leonardo Vinci', 'Renaissance master and polymath, known for iconic works spanning painting, sculpture, and invention.', 1452, 'leo@vincistudio.it', '+39 055 123456', 'Florence', 'https://vincistudio.it', '@leo_vinci', TRUE),
    ('Claude Monet', 'Founder of French Impressionist painting, celebrated for his plein air landscapes and water lily series.', 1840, 'claude@monet.fr', '+33 1 23456789', 'Giverny', 'https://monet-art.fr', '@claude_monet', TRUE),
    ('Ansel Adams', 'American landscape photographer and environmentalist, famous for black-and-white images of the American West.', 1902, 'ansel@adams.co', '+1 415 5551234', 'San Francisco', 'https://anseladams.co', '@ansel_adams', TRUE),
    ('Frida Kahlo', 'Mexican painter known for her vivid self-portraits and works inspired by Mexican folk art.', 1907, 'frida@kahlo.mx', '+52 55 12345678', 'Mexico City', 'https://fridakahlo.mx', '@frida_kahlo', TRUE),
    ('Auguste Rodin', 'French sculptor, generally considered the founder of modern sculpture.', 1840, 'auguste@rodin.fr', '+33 1 98765432', 'Paris', 'https://rodin-art.fr', '@auguste_rodin', TRUE),
    ('Yayoi Kusama', 'Japanese contemporary artist known for her polka dots, infinity rooms, and immersive installations.', 1929, 'yayoi@kusama.jp', '+81 3 12345678', 'Tokyo', 'https://kusama.jp', '@yayoi_kusama', TRUE),
    ('Banksy', 'Anonymous England-based street artist known for satirical and subversive works combining dark humour with graffiti.', NULL, 'contact@banksy.co.uk', NULL, 'Bristol', 'https://banksy.co.uk', '@banksy', TRUE),
    ('Georgia O''Keeffe', 'American modernist artist known for her paintings of enlarged flowers, New York skyscrapers, and New Mexico landscapes.', 1887, 'georgia@okeeffe.us', '+1 505 5559876', 'Santa Fe', 'https://okeeffe.us', '@georgia_okeeffe', FALSE),
    ('Jean-Michel Basquiat', 'American artist of Haitian and Puerto Rican descent known for his neo-expressionist paintings.', 1960, 'jm@basquiat.art', '+1 212 5554321', 'New York', 'https://basquiat.art', '@jm_basquiat', FALSE),
    ('Marina Abramovic', 'Serbian performance artist whose work explores the relationship between performer and audience.', 1946, 'marina@abramovic.org', '+381 11 1234567', 'Belgrade', 'https://abramovic.org', '@marina_abramovic', TRUE);

-- ============================================================
-- 4. Association artiste — discipline
-- ============================================================
INSERT INTO artist_discipline (artist_id, discipline_id) VALUES
    (1, 1), (1, 2),          -- Leonardo: Painting, Sculpture
    (2, 1),                   -- Monet: Painting
    (3, 3),                   -- Ansel: Photography
    (4, 1),                   -- Frida: Painting
    (5, 2),                   -- Rodin: Sculpture
    (6, 1), (6, 2), (6, 4),  -- Kusama: Painting, Sculpture, Digital Art
    (7, 1), (7, 7),          -- Banksy: Painting, Printmaking
    (8, 1),                   -- O'Keeffe: Painting
    (9, 1),                   -- Basquiat: Painting
    (10, 1);                  -- Abramovic: Painting (performance)

-- ============================================================
-- 5. Œuvres d'art
-- ============================================================
INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id) VALUES
    -- Leonardo (artist_id = 1)
    ('Mona Lisa', 1503, 'Painting', 'Oil on poplar panel', '77 x 53 cm', 'The most famous portrait in the world, known for the enigmatic smile of its subject.', 850000000.00, 'EXHIBITED', 1),
    ('The Last Supper', 1498, 'Painting', 'Tempera on gesso', '460 x 880 cm', 'A monumental mural depicting the final meal of Jesus with his apostles.', 450000000.00, 'EXHIBITED', 1),
    ('Vitruvian Man', 1490, 'Drawing', 'Pen and ink on paper', '34.6 x 25.5 cm', 'A study of the proportions of the human body based on the writings of Vitruvius.', 50000000.00, 'EXHIBITED', 1),
    -- Monet (artist_id = 2)
    ('Water Lilies', 1919, 'Painting', 'Oil on canvas', '200 x 180 cm', 'Part of a series of approximately 250 oil paintings depicting Monet''s flower garden at Giverny.', 40000000.00, 'EXHIBITED', 2),
    ('Impression, Sunrise', 1872, 'Painting', 'Oil on canvas', '48 x 63 cm', 'The painting that gave the Impressionist movement its name.', 120000000.00, 'EXHIBITED', 2),
    ('Haystacks', 1890, 'Painting', 'Oil on canvas', '60 x 100 cm', 'A series showing haystacks at different times of day and seasons.', 110000000.00, 'FOR_SALE', 2),
    -- Ansel Adams (artist_id = 3)
    ('Monolith, The Face of Half Dome', 1927, 'Photography', 'Silver gelatin print', '40 x 50 cm', 'A dramatic photograph of Half Dome in Yosemite National Park.', 100000.00, 'FOR_SALE', 3),
    ('Moonrise, Hernandez', 1941, 'Photography', 'Silver gelatin print', '40 x 50 cm', 'One of the most famous photographs ever taken, showing a moonrise over a New Mexico village.', 500000.00, 'SOLD', 3),
    -- Frida Kahlo (artist_id = 4)
    ('The Two Fridas', 1939, 'Painting', 'Oil on canvas', '173.5 x 173 cm', 'A double self-portrait showing two versions of the artist with their hearts exposed.', 5000000.00, 'EXHIBITED', 4),
    ('Self-Portrait with Thorn Necklace', 1940, 'Painting', 'Oil on canvas', '63.5 x 49.5 cm', 'An iconic self-portrait featuring the artist with a thorn necklace and hummingbird.', 8000000.00, 'FOR_SALE', 4),
    -- Rodin (artist_id = 5)
    ('The Thinker', 1904, 'Sculpture', 'Bronze', '186 x 102 x 144 cm', 'One of the most recognized works in all of sculpture, depicting a man in deep meditation.', 15000000.00, 'EXHIBITED', 5),
    ('The Kiss', 1882, 'Sculpture', 'Marble', '181.5 x 112.5 x 117 cm', 'A marble sculpture showing two lovers embracing, originally part of The Gates of Hell.', 12000000.00, 'EXHIBITED', 5),
    -- Kusama (artist_id = 6)
    ('Infinity Mirror Room', 2013, 'Installation', 'Mixed media with LED', 'Room-scale', 'An immersive installation creating the illusion of infinite space through mirrors and lights.', 3000000.00, 'EXHIBITED', 6),
    ('Pumpkin', 1994, 'Sculpture', 'Painted fiberglass', '200 x 250 cm', 'A giant yellow pumpkin with black polka dots, an iconic Kusama motif.', 2000000.00, 'FOR_SALE', 6),
    -- Banksy (artist_id = 7)
    ('Girl with Balloon', 2002, 'Street Art', 'Spray paint on wall', '100 x 70 cm', 'A stencil mural showing a young girl reaching for a red heart-shaped balloon.', 25000000.00, 'SOLD', 7),
    ('Love is in the Bin', 2018, 'Mixed Media', 'Spray paint on canvas in frame', '101 x 78 x 18 cm', 'Originally Girl with Balloon, it self-destructed through a shredder hidden in its frame at auction.', 21000000.00, 'SOLD', 7),
    -- O'Keeffe (artist_id = 8)
    ('Jimson Weed', 1932, 'Painting', 'Oil on canvas', '121.9 x 101.6 cm', 'A large-scale flower painting that became the most expensive work by a female artist when sold in 2014.', 44000000.00, 'SOLD', 8),
    -- Basquiat (artist_id = 9)
    ('Untitled (Skull)', 1981, 'Painting', 'Acrylic and mixed media on canvas', '205.7 x 175.9 cm', 'A powerful neo-expressionist skull painting.', 110000000.00, 'SOLD', 9),
    -- Abramovic (artist_id = 10)
    ('The Artist Is Present', 2010, 'Performance Art', 'Live performance', 'Variable', 'A 736-hour performance where the artist sat immobile while visitors took turns sitting opposite her.', 0.00, 'EXHIBITED', 10);

-- ============================================================
-- 6. Association œuvre — tag
-- ============================================================
INSERT INTO artwork_tag_association (artwork_id, tag_id) VALUES
    (1, 1), (1, 5),         -- Mona Lisa: Renaissance, Portrait
    (2, 1),                   -- Last Supper: Renaissance
    (3, 1), (3, 9),          -- Vitruvian Man: Renaissance, Realism
    (4, 2), (4, 6),          -- Water Lilies: Impressionism, Landscape
    (5, 2), (5, 6),          -- Impression Sunrise: Impressionism, Landscape
    (6, 2), (6, 6),          -- Haystacks: Impressionism, Landscape
    (7, 6), (7, 9),          -- Monolith: Landscape, Realism
    (8, 6),                   -- Moonrise: Landscape
    (9, 8), (9, 5),          -- Two Fridas: Surrealism, Portrait
    (10, 8), (10, 5),        -- Self-Portrait Thorn: Surrealism, Portrait
    (11, 3), (11, 9),        -- The Thinker: Modern, Realism
    (12, 3),                  -- The Kiss: Modern
    (13, 10), (13, 4),       -- Infinity Mirror: Contemporary, Abstract
    (14, 10), (14, 4),       -- Pumpkin: Contemporary, Abstract
    (15, 10),                 -- Girl with Balloon: Contemporary
    (16, 10),                 -- Love is in the Bin: Contemporary
    (17, 3), (17, 4),        -- Jimson Weed: Modern, Abstract
    (18, 3), (18, 4),        -- Untitled Skull: Modern, Abstract
    (19, 10);                 -- Artist Is Present: Contemporary

-- ============================================================
-- 7. Galeries
-- ============================================================
INSERT INTO gallery (name, address, owner_name, opening_hours, contact_phone, rating, website) VALUES
    ('Louvre Art House', 'Rue de Rivoli, 75001 Paris, France', 'Marie Dupont', 'Mon-Sun 9:00-18:00', '+33 1 40205050', 4.9, 'https://louvre-arthouse.fr'),
    ('The British Gallery', 'Great Russell St, London WC1B 3DG, UK', 'James Whitfield', 'Mon-Sat 10:00-17:30', '+44 20 73231234', 4.7, 'https://british-gallery.co.uk'),
    ('Metropolitan Hub', '1000 5th Ave, New York, NY 10028, USA', 'Sarah Mitchell', 'Thu-Tue 10:00-17:00', '+1 212 5357710', 4.8, 'https://methub.org'),
    ('Tokyo Modern Space', '1-1-1 Roppongi, Minato-ku, Tokyo 106-6108, Japan', 'Haruki Tanaka', 'Wed-Mon 10:00-22:00', '+81 3 57771234', 4.6, 'https://tokyomodern.jp'),
    ('Galeria CDMX', 'Av. Paseo de la Reforma 51, Mexico City, Mexico', 'Carlos Mendoza', 'Tue-Sun 10:00-18:00', '+52 55 41221234', 4.5, 'https://galeria-cdmx.mx');

-- ============================================================
-- 8. Expositions
-- ============================================================
INSERT INTO exhibition (title, start_date, end_date, description, curator_name, theme, gallery_id) VALUES
    ('Renaissance Revival', '2026-01-15', '2026-06-30', 'A celebration of the Renaissance masters and their enduring influence on modern art.', 'Dr. Elena Rossi', 'Classic Renaissance', 1),
    ('Sculpting the Soul', '2026-03-01', '2026-05-15', 'An exploration of sculpture from Rodin to contemporary artists, examining the human condition.', 'Marcus Thorne', 'Modern & Classical Sculpture', 2),
    ('Impressionist Dreams', '2026-02-01', '2026-07-31', 'A journey through the light and color of the Impressionist movement.', 'Sarah Jenkins', 'Light and Color', 3),
    ('Infinity and Beyond', '2026-04-01', '2026-09-30', 'An immersive exhibition featuring Kusama''s iconic installations and contemporary digital art.', 'Yuki Sato', 'Contemporary Immersion', 4),
    ('Street to Gallery', '2026-05-01', '2026-08-31', 'Tracing the journey of street art from urban walls to prestigious gallery spaces.', 'Tom Rivera', 'Urban Art Movement', 3),
    ('Voices of Mexico', '2026-03-15', '2026-06-15', 'Showcasing the rich artistic heritage of Mexico through painting and mixed media.', 'Ana Gutierrez', 'Mexican Art Heritage', 5),
    ('The Lens of Nature', '2026-06-01', '2026-10-31', 'A photographic exploration of the natural world through the eyes of master photographers.', 'David Chen', 'Nature Photography', 2);

-- ============================================================
-- 9. Association exposition — œuvre
-- ============================================================
INSERT INTO exhibition_artwork (exhibition_id, artwork_id) VALUES
    -- Renaissance Revival (expo 1): Mona Lisa, Last Supper, Vitruvian Man
    (1, 1), (1, 2), (1, 3),
    -- Sculpting the Soul (expo 2): The Thinker, The Kiss, Pumpkin
    (2, 11), (2, 12), (2, 14),
    -- Impressionist Dreams (expo 3): Water Lilies, Impression Sunrise, Haystacks
    (3, 4), (3, 5), (3, 6),
    -- Infinity and Beyond (expo 4): Infinity Mirror, Pumpkin
    (4, 13), (4, 14),
    -- Street to Gallery (expo 5): Girl with Balloon, Love is in the Bin, Untitled Skull
    (5, 15), (5, 16), (5, 18),
    -- Voices of Mexico (expo 6): Two Fridas, Self-Portrait Thorn
    (6, 9), (6, 10),
    -- The Lens of Nature (expo 7): Monolith, Moonrise
    (7, 7), (7, 8);

-- ============================================================
-- 10. Ateliers (Workshops)
-- ============================================================
INSERT INTO workshop (title, date, duration_minutes, max_participants, price, location, description, level, instructor_id) VALUES
    ('Mastering Oil Painting', '2026-04-20 10:00:00', 180, 10, 150.00, 'Florence Studio, Italy', 'Learn the techniques of the Old Masters with modern applications. Focus on glazing, layering, and color mixing.', 'Intermediate', 1),
    ('Impressionist Landscapes', '2026-04-25 14:00:00', 120, 15, 120.00, 'Giverny Gardens, France', 'Paint en plein air in the gardens that inspired Monet. Capture light and atmosphere.', 'Beginner', 2),
    ('Sculpting Modernity', '2026-05-05 09:00:00', 240, 8, 200.00, 'Paris Workshop, France', 'Explore modern sculpture techniques from clay modeling to bronze casting concepts.', 'Advanced', 5),
    ('Street Art Workshop', '2026-05-10 11:00:00', 150, 20, 80.00, 'Shoreditch Studio, London', 'Introduction to stencil art, wheat pasting, and responsible urban art practices.', 'Beginner', 7),
    ('Nature Photography Masterclass', '2026-05-15 07:00:00', 300, 12, 175.00, 'Yosemite Valley, USA', 'Master landscape photography techniques including composition, exposure, and post-processing.', 'Intermediate', 3),
    ('Infinity Art: Dots and Mirrors', '2026-06-01 13:00:00', 180, 10, 250.00, 'Tokyo Modern Space, Japan', 'Create your own infinity-inspired artwork using polka dots, mirrors, and mixed media.', 'Beginner', 6),
    ('Neo-Expressionist Painting', '2026-05-20 10:00:00', 180, 8, 160.00, 'Brooklyn Studio, New York', 'Explore raw, emotive painting styles inspired by the neo-expressionist movement.', 'Intermediate', 4),
    ('Advanced Portrait Techniques', '2026-06-10 09:00:00', 240, 6, 300.00, 'Florence Studio, Italy', 'Deep dive into portraiture: anatomy, expression, and capturing personality on canvas.', 'Advanced', 1);

-- ============================================================
-- 11. Membres de la communauté
-- ============================================================
INSERT INTO community_member (name, email, birth_year, phone, city, membership_type) VALUES
    ('Alice Wonderland', 'alice@art.com', 1990, '+33 6 12345678', 'Paris', 'premium'),
    ('Bob Ross', 'bob@happytrees.com', 1985, '+44 7 98765432', 'London', 'premium'),
    ('Charlie Brown', 'charlie@peanuts.com', 1995, '+1 212 5551234', 'New York', 'free'),
    ('Diana Prince', 'diana@amazon.art', 1988, '+81 3 55556789', 'Tokyo', 'premium'),
    ('Eduardo Silva', 'eduardo@arte.br', 1992, '+55 11 91234567', 'São Paulo', 'free'),
    ('Fatima Al-Hassan', 'fatima@artsoul.ae', 1997, '+971 50 1234567', 'Dubai', 'premium'),
    ('Greta Lindberg', 'greta@konst.se', 1993, '+46 70 1234567', 'Stockholm', 'free'),
    ('Hiroshi Tanaka', 'hiroshi@art.jp', 1980, '+81 90 12345678', 'Osaka', 'premium'),
    ('Isabella Rossi', 'isabella@arte.it', 1991, '+39 333 1234567', 'Rome', 'free'),
    ('Jacques Moreau', 'jacques@galerie.fr', 1987, '+33 7 87654321', 'Lyon', 'premium'),
    ('Keiko Yamamoto', 'keiko@artworld.jp', 1994, '+81 80 98765432', 'Tokyo', 'free'),
    ('Lucas Martin', 'lucas@artlover.de', 1996, '+49 170 1234567', 'Berlin', 'free');

-- ============================================================
-- 12. Disciplines favorites des membres
-- ============================================================
INSERT INTO member_favorite_discipline (member_id, discipline_id) VALUES
    (1, 1), (1, 2),          -- Alice: Painting, Sculpture
    (2, 1), (2, 6),          -- Bob: Painting, Ceramics
    (3, 3), (3, 4),          -- Charlie: Photography, Digital Art
    (4, 4), (4, 1),          -- Diana: Digital Art, Painting
    (5, 1), (5, 7),          -- Eduardo: Painting, Printmaking
    (6, 2), (6, 8),          -- Fatima: Sculpture, Textile Art
    (7, 3),                   -- Greta: Photography
    (8, 1), (8, 2),          -- Hiroshi: Painting, Sculpture
    (9, 1),                   -- Isabella: Painting
    (10, 2), (10, 1),        -- Jacques: Sculpture, Painting
    (11, 4), (11, 1),        -- Keiko: Digital Art, Painting
    (12, 3), (12, 5);        -- Lucas: Photography, Music

-- ============================================================
-- 13. Réservations (Bookings)
-- ============================================================
INSERT INTO booking (booking_date, payment_status, workshop_id, member_id) VALUES
    ('2026-04-01 10:30:00', 'PAID', 1, 1),       -- Alice → Oil Painting
    ('2026-04-02 14:00:00', 'PAID', 1, 9),       -- Isabella → Oil Painting
    ('2026-04-03 09:00:00', 'PAID', 2, 1),       -- Alice → Impressionist Landscapes
    ('2026-04-03 11:00:00', 'PAID', 2, 10),      -- Jacques → Impressionist Landscapes
    ('2026-04-05 16:00:00', 'PENDING', 3, 2),    -- Bob → Sculpting Modernity
    ('2026-04-06 10:00:00', 'PAID', 3, 6),       -- Fatima → Sculpting Modernity
    ('2026-04-07 12:00:00', 'PAID', 4, 3),       -- Charlie → Street Art
    ('2026-04-07 13:00:00', 'PAID', 4, 5),       -- Eduardo → Street Art
    ('2026-04-08 08:00:00', 'PAID', 5, 7),       -- Greta → Nature Photography
    ('2026-04-08 08:30:00', 'PENDING', 5, 12),   -- Lucas → Nature Photography
    ('2026-04-10 14:00:00', 'PAID', 6, 4),       -- Diana → Infinity Art
    ('2026-04-10 14:30:00', 'PAID', 6, 11),      -- Keiko → Infinity Art
    ('2026-04-10 15:00:00', 'PAID', 6, 8),       -- Hiroshi → Infinity Art
    ('2026-04-12 10:00:00', 'CANCELLED', 7, 3),  -- Charlie → Neo-Expressionist (cancelled)
    ('2026-04-12 11:00:00', 'PAID', 7, 5),       -- Eduardo → Neo-Expressionist
    ('2026-04-15 09:00:00', 'PENDING', 8, 1),    -- Alice → Advanced Portrait
    ('2026-04-15 09:30:00', 'PENDING', 8, 8),    -- Hiroshi → Advanced Portrait
    ('2026-04-15 10:00:00', 'PAID', 8, 9);       -- Isabella → Advanced Portrait

-- ============================================================
-- 14. Avis (Reviews)
-- ============================================================
INSERT INTO review (rating, comment, review_date, member_id, artwork_id) VALUES
    (5, 'An absolute masterpiece. The detail in the eyes is mesmerizing.', '2026-03-01', 1, 1),        -- Alice → Mona Lisa
    (4, 'The colors are stunning, truly captures the essence of Impressionism.', '2026-03-05', 2, 4),  -- Bob → Water Lilies
    (5, 'Deeply moving sculpture that captures the essence of human contemplation.', '2026-03-10', 3, 11),  -- Charlie → The Thinker
    (5, 'An immersive experience like no other. Lost all sense of space.', '2026-03-12', 4, 13),       -- Diana → Infinity Mirror
    (4, 'Powerful political statement through art. Banksy at his finest.', '2026-03-15', 5, 15),       -- Eduardo → Girl with Balloon
    (5, 'The emotional depth of Frida''s self-portraits is unparalleled.', '2026-03-18', 6, 9),        -- Fatima → Two Fridas
    (4, 'A breathtaking photograph that captures the grandeur of nature.', '2026-03-20', 7, 7),        -- Greta → Monolith
    (5, 'A revolutionary performance that redefines what art can be.', '2026-03-22', 8, 19),           -- Hiroshi → Artist Is Present
    (4, 'Beautiful restoration of the Renaissance genius.', '2026-03-25', 9, 2),                        -- Isabella → Last Supper
    (5, 'The destruction became the art. Absolutely brilliant concept.', '2026-03-28', 10, 16),        -- Jacques → Love is in the Bin
    (3, 'Impressive technique but I expected more from the series.', '2026-04-01', 11, 6),             -- Keiko → Haystacks
    (5, 'Raw energy and emotion on canvas. Basquiat was a true genius.', '2026-04-03', 12, 18),        -- Lucas → Untitled Skull
    (4, 'The enlarged flower reveals details invisible to the naked eye.', '2026-04-05', 1, 17),       -- Alice → Jimson Weed
    (5, 'A perfect romantic marble sculpture. Rodin captured pure emotion.', '2026-04-07', 2, 12),     -- Bob → The Kiss
    (4, 'Haunting and beautiful. The moonlight is captured perfectly.', '2026-04-08', 7, 8),            -- Greta → Moonrise
    (5, 'The polka dots create a hypnotic, meditative experience.', '2026-04-10', 4, 14),             -- Diana → Pumpkin
    (4, 'A bold self-portrait that challenges the viewer to look deeper.', '2026-04-12', 6, 10),       -- Fatima → Self-Portrait Thorn
    (5, 'Seeing it in person gave me chills. The painting that named Impressionism.', '2026-04-14', 10, 5);  -- Jacques → Impression Sunrise

-- ============================================================
-- Vérification : nombre d'enregistrements par table
-- ============================================================
SELECT 'discipline' AS tbl, COUNT(*) AS cnt FROM discipline
UNION ALL SELECT 'artwork_tag', COUNT(*) FROM artwork_tag
UNION ALL SELECT 'artist', COUNT(*) FROM artist
UNION ALL SELECT 'artist_discipline', COUNT(*) FROM artist_discipline
UNION ALL SELECT 'artwork', COUNT(*) FROM artwork
UNION ALL SELECT 'artwork_tag_association', COUNT(*) FROM artwork_tag_association
UNION ALL SELECT 'gallery', COUNT(*) FROM gallery
UNION ALL SELECT 'exhibition', COUNT(*) FROM exhibition
UNION ALL SELECT 'exhibition_artwork', COUNT(*) FROM exhibition_artwork
UNION ALL SELECT 'workshop', COUNT(*) FROM workshop
UNION ALL SELECT 'community_member', COUNT(*) FROM community_member
UNION ALL SELECT 'member_favorite_discipline', COUNT(*) FROM member_favorite_discipline
UNION ALL SELECT 'booking', COUNT(*) FROM booking
UNION ALL SELECT 'review', COUNT(*) FROM review;
