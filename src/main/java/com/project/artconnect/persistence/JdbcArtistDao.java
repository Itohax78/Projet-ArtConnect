package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC de l'interface ArtistDao.
 *
 * <p>Cette classe gère la persistance des objets {@link Artist} dans la base
 * de données MySQL. Elle utilise des {@link PreparedStatement} pour toutes les
 * opérations SQL et le pattern try-with-resources pour la gestion automatique
 * des ressources JDBC (Connection, Statement, ResultSet).</p>
 *
 * <h3>Tables utilisées :</h3>
 * <ul>
 *   <li>{@code artist}          – données principales de l'artiste</li>
 *   <li>{@code discipline}      – catalogue des disciplines</li>
 *   <li>{@code artist_discipline} – table de jonction N:M artiste ↔ discipline</li>
 * </ul>
 */
public class JdbcArtistDao implements ArtistDao {

    // =========================================================================
    // Constantes SQL
    // =========================================================================

    /** Sélection de tous les artistes */
    private static final String SQL_FIND_ALL =
            "SELECT artist_id, name, bio, birth_year, contact_email, phone, " +
            "city, website, social_media, is_active FROM artist";

    /** Sélection d'un artiste par son identifiant */
    private static final String SQL_FIND_BY_ID =
            "SELECT artist_id, name, bio, birth_year, contact_email, phone, " +
            "city, website, social_media, is_active FROM artist WHERE artist_id = ?";

    /** Sélection d'un artiste par son nom (utilisé pour le mapping OOP sans id) */
    private static final String SQL_FIND_BY_NAME =
            "SELECT artist_id, name, bio, birth_year, contact_email, phone, " +
            "city, website, social_media, is_active FROM artist WHERE name = ?";

    /** Sélection des artistes d'une ville donnée */
    private static final String SQL_FIND_BY_CITY =
            "SELECT artist_id, name, bio, birth_year, contact_email, phone, " +
            "city, website, social_media, is_active FROM artist WHERE city = ?";

    /** Insertion d'un nouvel artiste (retourne la clé générée) */
    private static final String SQL_INSERT =
            "INSERT INTO artist (name, bio, birth_year, contact_email, phone, " +
            "city, website, social_media, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /** Mise à jour d'un artiste existant (identifié par son nom) */
    private static final String SQL_UPDATE =
            "UPDATE artist SET bio = ?, birth_year = ?, contact_email = ?, phone = ?, " +
            "city = ?, website = ?, social_media = ?, is_active = ? WHERE name = ?";

    /** Suppression d'un artiste par son nom */
    private static final String SQL_DELETE =
            "DELETE FROM artist WHERE name = ?";

    /** Récupération des disciplines associées à un artiste */
    private static final String SQL_FIND_DISCIPLINES =
            "SELECT d.discipline_id, d.name FROM discipline d " +
            "INNER JOIN artist_discipline ad ON d.discipline_id = ad.discipline_id " +
            "WHERE ad.artist_id = ?";

    /** Récupération de toutes les disciplines */
    private static final String SQL_FIND_ALL_DISCIPLINES =
            "SELECT discipline_id, name FROM discipline ORDER BY name";

    /** Insertion d'une association artiste ↔ discipline */
    private static final String SQL_INSERT_ARTIST_DISCIPLINE =
            "INSERT INTO artist_discipline (artist_id, discipline_id) VALUES (?, ?)";

    /** Suppression de toutes les disciplines d'un artiste */
    private static final String SQL_DELETE_ARTIST_DISCIPLINES =
            "DELETE FROM artist_discipline WHERE artist_id = ?";

    /** Recherche d'une discipline par son nom */
    private static final String SQL_FIND_DISCIPLINE_BY_NAME =
            "SELECT discipline_id FROM discipline WHERE name = ?";

    /** Insertion d'une nouvelle discipline */
    private static final String SQL_INSERT_DISCIPLINE =
            "INSERT INTO discipline (name) VALUES (?)";

    // =========================================================================
    // Méthodes CRUD
    // =========================================================================

    /**
     * Récupère la liste de tous les artistes de la base de données,
     * avec leurs disciplines associées.
     *
     * @return une liste de tous les artistes (vide si aucun résultat)
     */
    @Override
    public List<Artist> findAll() {
        List<Artist> artists = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int artistId = rs.getInt("artist_id");
                Artist artist = mapRowToArtist(rs);
                artist.setDisciplines(findDisciplinesByArtistId(conn, artistId));
                artists.add(artist);
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] Erreur lors de findAll() : " + e.getMessage());
            e.printStackTrace();
        }

        return artists;
    }

    /**
     * Recherche un artiste par son nom exact.
     *
     * @param name le nom de l'artiste
     * @return un Optional contenant l'artiste, ou vide si non trouvé
     */
    @Override
    public Optional<Artist> findByName(String name) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_NAME)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int artistId = rs.getInt("artist_id");
                    Artist artist = mapRowToArtist(rs);
                    artist.setDisciplines(findDisciplinesByArtistId(conn, artistId));
                    return Optional.of(artist);
                }
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] Erreur lors de findByName('" + name + "') : " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Recherche un artiste par son identifiant en base de données.
     * <p>Méthode utilitaire non déclarée dans l'interface, mais utile
     * en interne et pour d'autres DAOs (ex : JdbcArtworkDao).</p>
     *
     * @param id l'identifiant de l'artiste (clé primaire)
     * @return l'artiste correspondant, ou {@code null} si non trouvé
     */
    public Artist findById(int id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Artist artist = mapRowToArtist(rs);
                    artist.setDisciplines(findDisciplinesByArtistId(conn, id));
                    return artist;
                }
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] Erreur lors de findById(" + id + ") : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Insère un nouvel artiste dans la base de données.
     * <p>Les disciplines de l'artiste sont également sauvegardées dans la
     * table de jonction {@code artist_discipline}. Si une discipline n'existe
     * pas encore dans la table {@code discipline}, elle est créée à la volée.</p>
     *
     * @param artist l'artiste à sauvegarder
     */
    @Override
    public void save(Artist artist) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, artist.getName());
            ps.setString(2, artist.getBio());

            if (artist.getBirthYear() != null) {
                ps.setInt(3, artist.getBirthYear());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setString(4, artist.getContactEmail());
            ps.setString(5, artist.getPhone());
            ps.setString(6, artist.getCity());
            ps.setString(7, artist.getWebsite());
            ps.setString(8, artist.getSocialMedia());
            ps.setBoolean(9, artist.isActive());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int artistId = generatedKeys.getInt(1);
                        saveDisciplines(conn, artistId, artist.getDisciplines());
                    }
                }
                System.out.println("[JdbcArtistDao] Artiste '" + artist.getName() + "' sauvegardé avec succès.");
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] Erreur lors de save() : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Met à jour un artiste existant dans la base de données.
     * <p>L'artiste est identifié par son nom (conformément à l'architecture OOP
     * du projet où les modèles n'exposent pas d'identifiant numérique).
     * Les disciplines sont entièrement recalculées (suppression puis réinsertion).</p>
     *
     * @param artist l'artiste avec les nouvelles valeurs à persister
     */
    @Override
    public void update(Artist artist) {
        try (Connection conn = ConnectionManager.getConnection()) {
            // 1) Mettre à jour les champs de la table artist
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
                ps.setString(1, artist.getBio());

                if (artist.getBirthYear() != null) {
                    ps.setInt(2, artist.getBirthYear());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }

                ps.setString(3, artist.getContactEmail());
                ps.setString(4, artist.getPhone());
                ps.setString(5, artist.getCity());
                ps.setString(6, artist.getWebsite());
                ps.setString(7, artist.getSocialMedia());
                ps.setBoolean(8, artist.isActive());
                ps.setString(9, artist.getName()); // WHERE name = ?

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    System.err.println("[JdbcArtistDao] Aucun artiste trouvé avec le nom : " + artist.getName());
                    return;
                }
            }

            // 2) Récupérer l'artist_id pour mettre à jour les disciplines
            int artistId = findArtistIdByName(conn, artist.getName());
            if (artistId > 0) {
                deleteDisciplines(conn, artistId);
                saveDisciplines(conn, artistId, artist.getDisciplines());
            }

            System.out.println("[JdbcArtistDao] Artiste '" + artist.getName() + "' mis à jour avec succès.");

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] Erreur lors de update() : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Supprime un artiste de la base de données à partir de son nom.
     * <p>Grâce au {@code ON DELETE CASCADE} défini dans le schéma SQL,
     * les enregistrements liés (artworks, artist_discipline, etc.) sont
     * automatiquement supprimés.</p>
     *
     * @param artistName le nom de l'artiste à supprimer
     */
    @Override
    public void delete(String artistName) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setString(1, artistName);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("[JdbcArtistDao] Artiste '" + artistName + "' supprimé avec succès.");
            } else {
                System.err.println("[JdbcArtistDao] Aucun artiste trouvé avec le nom : " + artistName);
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] Erreur lors de delete('" + artistName + "') : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recherche tous les artistes situés dans une ville donnée.
     *
     * @param city le nom de la ville à filtrer
     * @return la liste des artistes de cette ville (vide si aucun résultat)
     */
    @Override
    public List<Artist> findByCity(String city) {
        List<Artist> artists = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CITY)) {

            ps.setString(1, city);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int artistId = rs.getInt("artist_id");
                    Artist artist = mapRowToArtist(rs);
                    artist.setDisciplines(findDisciplinesByArtistId(conn, artistId));
                    artists.add(artist);
                }
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] Erreur lors de findByCity('" + city + "') : " + e.getMessage());
            e.printStackTrace();
        }

        return artists;
    }

    /**
     * Récupère toutes les disciplines disponibles dans la base de données.
     *
     * @return la liste de toutes les disciplines, triée par nom
     */
    @Override
    public List<Discipline> findAllDisciplines() {
        List<Discipline> disciplines = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL_DISCIPLINES);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                disciplines.add(new Discipline(rs.getString("name")));
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] Erreur lors de findAllDisciplines() : " + e.getMessage());
            e.printStackTrace();
        }

        return disciplines;
    }

    /**
     * Recherche multi-critères : nom partiel, discipline et/ou ville.
     * <p>La requête SQL est construite dynamiquement en fonction des critères
     * non-null/non-vides fournis. Le filtre par discipline utilise une sous-requête
     * EXISTS sur la table de jonction.</p>
     *
     * @param query          recherche partielle sur le nom (LIKE %query%)
     * @param disciplineName nom exact de la discipline à filtrer
     * @param city           nom exact de la ville à filtrer
     * @return la liste des artistes correspondant aux critères
     */
    @Override
    public List<Artist> search(String query, String disciplineName, String city) {
        List<Artist> artists = new ArrayList<>();

        // Construction dynamique de la requête WHERE
        StringBuilder sql = new StringBuilder(
                "SELECT artist_id, name, bio, birth_year, contact_email, phone, " +
                "city, website, social_media, is_active FROM artist WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.add("%" + query.toLowerCase() + "%");
        }

        if (city != null && !city.isBlank()) {
            sql.append(" AND LOWER(city) = LOWER(?)");
            params.add(city);
        }

        if (disciplineName != null && !disciplineName.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM artist_discipline ad " +
                        "INNER JOIN discipline d ON ad.discipline_id = d.discipline_id " +
                        "WHERE ad.artist_id = artist.artist_id AND d.name = ?)");
            params.add(disciplineName);
        }

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Binder les paramètres dynamiques
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int artistId = rs.getInt("artist_id");
                    Artist artist = mapRowToArtist(rs);
                    artist.setDisciplines(findDisciplinesByArtistId(conn, artistId));
                    artists.add(artist);
                }
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] Erreur lors de search() : " + e.getMessage());
            e.printStackTrace();
        }

        return artists;
    }

    // =========================================================================
    // Méthodes utilitaires privées
    // =========================================================================

    /**
     * Mappe la ligne courante d'un {@link ResultSet} vers un objet {@link Artist}.
     *
     * @param rs le ResultSet positionné sur une ligne valide
     * @return un nouvel objet Artist rempli avec les données de la ligne
     * @throws SQLException en cas d'erreur d'accès aux colonnes
     */
    private Artist mapRowToArtist(ResultSet rs) throws SQLException {
        Artist artist = new Artist();

        artist.setName(rs.getString("name"));
        artist.setBio(rs.getString("bio"));

        // birth_year peut être NULL en base → getInt retourne 0, wasNull() le détecte
        int birthYear = rs.getInt("birth_year");
        artist.setBirthYear(rs.wasNull() ? null : birthYear);

        artist.setContactEmail(rs.getString("contact_email"));
        artist.setPhone(rs.getString("phone"));
        artist.setCity(rs.getString("city"));
        artist.setWebsite(rs.getString("website"));
        artist.setSocialMedia(rs.getString("social_media"));
        artist.setActive(rs.getBoolean("is_active"));

        return artist;
    }

    /**
     * Récupère les disciplines associées à un artiste via la table de jonction.
     */
    private List<Discipline> findDisciplinesByArtistId(Connection conn, int artistId) throws SQLException {
        List<Discipline> disciplines = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_DISCIPLINES)) {
            ps.setInt(1, artistId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    disciplines.add(new Discipline(rs.getString("name")));
                }
            }
        }

        return disciplines;
    }

    /**
     * Sauvegarde les associations artiste ↔ disciplines dans la table de jonction.
     * Si une discipline n'existe pas encore en base, elle est créée automatiquement.
     */
    private void saveDisciplines(Connection conn, int artistId, List<Discipline> disciplines) throws SQLException {
        if (disciplines == null || disciplines.isEmpty()) {
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_ARTIST_DISCIPLINE)) {
            for (Discipline discipline : disciplines) {
                int disciplineId = findOrCreateDiscipline(conn, discipline.getName());
                ps.setInt(1, artistId);
                ps.setInt(2, disciplineId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Supprime toutes les associations de disciplines d'un artiste.
     */
    private void deleteDisciplines(Connection conn, int artistId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ARTIST_DISCIPLINES)) {
            ps.setInt(1, artistId);
            ps.executeUpdate();
        }
    }

    /**
     * Recherche une discipline par son nom. Si elle n'existe pas, elle est créée.
     */
    private int findOrCreateDiscipline(Connection conn, String disciplineName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_DISCIPLINE_BY_NAME)) {
            ps.setString(1, disciplineName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("discipline_id");
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_DISCIPLINE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, disciplineName);
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }

        throw new SQLException("Impossible de créer la discipline : " + disciplineName);
    }

    /**
     * Recherche l'identifiant d'un artiste à partir de son nom.
     */
    private int findArtistIdByName(Connection conn, String artistName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("artist_id");
                }
            }
        }
        return -1;
    }
}
