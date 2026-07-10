package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC de l'interface ArtworkDao.
 *
 * <p>Gère la persistance des objets {@link Artwork} dans la base de données MySQL.
 * Utilise des {@link PreparedStatement} et le pattern try-with-resources.</p>
 *
 * <h3>Tables utilisées :</h3>
 * <ul>
 *   <li>{@code artwork}              – données principales de l'œuvre</li>
 *   <li>{@code artist}               – artiste créateur (FK)</li>
 *   <li>{@code artwork_tag}          – catalogue des tags</li>
 *   <li>{@code artwork_tag_association} – table de jonction N:M œuvre ↔ tag</li>
 * </ul>
 */
public class JdbcArtworkDao implements ArtworkDao {

    // =========================================================================
    // Constantes SQL
    // =========================================================================

    private static final String SQL_FIND_ALL =
            "SELECT a.artwork_id, a.title, a.creation_year, a.type, a.medium, " +
            "a.dimensions, a.description, a.price, a.status, a.artist_id, " +
            "ar.name AS artist_name, ar.bio, ar.birth_year, ar.contact_email, " +
            "ar.phone, ar.city, ar.website, ar.social_media, ar.is_active " +
            "FROM artwork a " +
            "INNER JOIN artist ar ON a.artist_id = ar.artist_id";

    private static final String SQL_FIND_BY_ARTIST_NAME =
            SQL_FIND_ALL + " WHERE ar.name = ?";

    private static final String SQL_FIND_ARTIST_ID_BY_NAME =
            "SELECT artist_id FROM artist WHERE name = ?";

    private static final String SQL_INSERT =
            "INSERT INTO artwork (title, creation_year, type, medium, dimensions, " +
            "description, price, status, artist_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE artwork SET creation_year = ?, type = ?, medium = ?, dimensions = ?, " +
            "description = ?, price = ?, status = ? WHERE title = ?";

    private static final String SQL_DELETE =
            "DELETE FROM artwork WHERE title = ?";

    private static final String SQL_FIND_TAGS_BY_ARTWORK_ID =
            "SELECT t.tag_id, t.name FROM artwork_tag t " +
            "INNER JOIN artwork_tag_association ata ON t.tag_id = ata.tag_id " +
            "WHERE ata.artwork_id = ?";

    private static final String SQL_INSERT_TAG_ASSOCIATION =
            "INSERT INTO artwork_tag_association (artwork_id, tag_id) VALUES (?, ?)";

    private static final String SQL_DELETE_TAG_ASSOCIATIONS =
            "DELETE FROM artwork_tag_association WHERE artwork_id = ?";

    private static final String SQL_FIND_TAG_BY_NAME =
            "SELECT tag_id FROM artwork_tag WHERE name = ?";

    private static final String SQL_INSERT_TAG =
            "INSERT INTO artwork_tag (name) VALUES (?)";

    private static final String SQL_FIND_ARTWORK_ID_BY_TITLE =
            "SELECT artwork_id FROM artwork WHERE title = ?";

    // =========================================================================
    // Méthodes CRUD
    // =========================================================================

    @Override
    public List<Artwork> findAll() {
        List<Artwork> artworks = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int artworkId = rs.getInt("artwork_id");
                Artwork artwork = mapRowToArtwork(rs);
                artwork.setTags(findTagsByArtworkId(conn, artworkId));
                artworks.add(artwork);
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtworkDao] Erreur lors de findAll() : " + e.getMessage());
            e.printStackTrace();
        }

        return artworks;
    }

    @Override
    public void save(Artwork artwork) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, artwork.getTitle());

            if (artwork.getCreationYear() != null) {
                ps.setInt(2, artwork.getCreationYear());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setString(3, artwork.getType());
            ps.setString(4, artwork.getMedium());
            ps.setString(5, artwork.getDimensions());
            ps.setString(6, artwork.getDescription());
            ps.setDouble(7, artwork.getPrice());
            ps.setString(8, artwork.getStatus() != null ? artwork.getStatus().name() : "FOR_SALE");

            // Résoudre l'artist_id à partir du nom de l'artiste
            int artistId = findArtistIdByName(conn, artwork.getArtist().getName());
            if (artistId <= 0) {
                System.err.println("[JdbcArtworkDao] Artiste non trouvé : " + artwork.getArtist().getName());
                return;
            }
            ps.setInt(9, artistId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int artworkId = generatedKeys.getInt(1);
                        saveTags(conn, artworkId, artwork.getTags());
                    }
                }
                System.out.println("[JdbcArtworkDao] Œuvre '" + artwork.getTitle() + "' sauvegardée avec succès.");
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtworkDao] Erreur lors de save() : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(Artwork artwork) {
        try (Connection conn = ConnectionManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
                if (artwork.getCreationYear() != null) {
                    ps.setInt(1, artwork.getCreationYear());
                } else {
                    ps.setNull(1, Types.INTEGER);
                }

                ps.setString(2, artwork.getType());
                ps.setString(3, artwork.getMedium());
                ps.setString(4, artwork.getDimensions());
                ps.setString(5, artwork.getDescription());
                ps.setDouble(6, artwork.getPrice());
                ps.setString(7, artwork.getStatus() != null ? artwork.getStatus().name() : "FOR_SALE");
                ps.setString(8, artwork.getTitle()); // WHERE title = ?

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    System.err.println("[JdbcArtworkDao] Aucune œuvre trouvée avec le titre : " + artwork.getTitle());
                    return;
                }
            }

            // Mettre à jour les tags (delete-then-insert)
            int artworkId = findArtworkIdByTitle(conn, artwork.getTitle());
            if (artworkId > 0) {
                deleteTags(conn, artworkId);
                saveTags(conn, artworkId, artwork.getTags());
            }

            System.out.println("[JdbcArtworkDao] Œuvre '" + artwork.getTitle() + "' mise à jour avec succès.");

        } catch (SQLException e) {
            System.err.println("[JdbcArtworkDao] Erreur lors de update() : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setString(1, title);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("[JdbcArtworkDao] Œuvre '" + title + "' supprimée avec succès.");
            } else {
                System.err.println("[JdbcArtworkDao] Aucune œuvre trouvée avec le titre : " + title);
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtworkDao] Erreur lors de delete('" + title + "') : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> artworks = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ARTIST_NAME)) {

            ps.setString(1, artistName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int artworkId = rs.getInt("artwork_id");
                    Artwork artwork = mapRowToArtwork(rs);
                    artwork.setTags(findTagsByArtworkId(conn, artworkId));
                    artworks.add(artwork);
                }
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtworkDao] Erreur lors de findByArtistName('" + artistName + "') : " + e.getMessage());
            e.printStackTrace();
        }

        return artworks;
    }

    // =========================================================================
    // Méthodes utilitaires privées
    // =========================================================================

    /**
     * Mappe la ligne courante d'un ResultSet vers un objet Artwork.
     * Le ResultSet doit contenir les colonnes de artwork jointes avec artist.
     */
    private Artwork mapRowToArtwork(ResultSet rs) throws SQLException {
        Artwork artwork = new Artwork();

        artwork.setTitle(rs.getString("title"));

        int creationYear = rs.getInt("creation_year");
        artwork.setCreationYear(rs.wasNull() ? null : creationYear);

        artwork.setType(rs.getString("type"));
        artwork.setMedium(rs.getString("medium"));
        artwork.setDimensions(rs.getString("dimensions"));
        artwork.setDescription(rs.getString("description"));
        artwork.setPrice(rs.getDouble("price"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            artwork.setStatus(Artwork.Status.valueOf(statusStr));
        }

        // Reconstruire l'objet Artist associé
        Artist artist = new Artist();
        artist.setName(rs.getString("artist_name"));
        artist.setBio(rs.getString("bio"));
        int birthYear = rs.getInt("birth_year");
        artist.setBirthYear(rs.wasNull() ? null : birthYear);
        artist.setContactEmail(rs.getString("contact_email"));
        artist.setPhone(rs.getString("phone"));
        artist.setCity(rs.getString("city"));
        artist.setWebsite(rs.getString("website"));
        artist.setSocialMedia(rs.getString("social_media"));
        artist.setActive(rs.getBoolean("is_active"));
        artwork.setArtist(artist);

        return artwork;
    }

    private List<ArtworkTag> findTagsByArtworkId(Connection conn, int artworkId) throws SQLException {
        List<ArtworkTag> tags = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_TAGS_BY_ARTWORK_ID)) {
            ps.setInt(1, artworkId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tags.add(new ArtworkTag(rs.getString("name")));
                }
            }
        }

        return tags;
    }

    private void saveTags(Connection conn, int artworkId, List<ArtworkTag> tags) throws SQLException {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_TAG_ASSOCIATION)) {
            for (ArtworkTag tag : tags) {
                int tagId = findOrCreateTag(conn, tag.getName());
                ps.setInt(1, artworkId);
                ps.setInt(2, tagId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteTags(Connection conn, int artworkId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_TAG_ASSOCIATIONS)) {
            ps.setInt(1, artworkId);
            ps.executeUpdate();
        }
    }

    private int findOrCreateTag(Connection conn, String tagName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_TAG_BY_NAME)) {
            ps.setString(1, tagName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("tag_id");
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_TAG, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tagName);
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }

        throw new SQLException("Impossible de créer le tag : " + tagName);
    }

    private int findArtistIdByName(Connection conn, String artistName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ARTIST_ID_BY_NAME)) {
            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("artist_id");
                }
            }
        }
        return -1;
    }

    private int findArtworkIdByTitle(Connection conn, String title) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ARTWORK_ID_BY_TITLE)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("artwork_id");
                }
            }
        }
        return -1;
    }
}
