package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC de l'interface ExhibitionDao.
 *
 * <p>Tables utilisées : {@code exhibition}, {@code gallery}, {@code exhibition_artwork}.</p>
 */
public class JdbcExhibitionDao implements ExhibitionDao {

    private static final String SQL_FIND_ALL =
            "SELECT e.exhibition_id, e.title, e.start_date, e.end_date, e.description, " +
            "e.curator_name, e.theme, e.gallery_id, " +
            "g.name AS gallery_name, g.address AS gallery_address, g.rating AS gallery_rating " +
            "FROM exhibition e " +
            "INNER JOIN gallery g ON e.gallery_id = g.gallery_id";

    private static final String SQL_FIND_BY_GALLERY_ID = SQL_FIND_ALL + " WHERE e.gallery_id = ?";

    private static final String SQL_INSERT =
            "INSERT INTO exhibition (title, start_date, end_date, description, curator_name, theme, gallery_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE exhibition SET start_date = ?, end_date = ?, description = ?, " +
            "curator_name = ?, theme = ? WHERE title = ?";

    private static final String SQL_DELETE = "DELETE FROM exhibition WHERE title = ?";

    private static final String SQL_FIND_GALLERY_ID_BY_NAME =
            "SELECT gallery_id FROM gallery WHERE name = ?";

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> exhibitions = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                exhibitions.add(mapRowToExhibition(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JdbcExhibitionDao] Erreur findAll() : " + e.getMessage());
            e.printStackTrace();
        }
        return exhibitions;
    }

    @Override
    public List<Exhibition> findByGalleryId(int galleryId) {
        List<Exhibition> exhibitions = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_GALLERY_ID)) {
            ps.setInt(1, galleryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    exhibitions.add(mapRowToExhibition(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[JdbcExhibitionDao] Erreur findByGalleryId(" + galleryId + ") : " + e.getMessage());
            e.printStackTrace();
        }
        return exhibitions;
    }

    @Override
    public void save(Exhibition exhibition) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, exhibition.getTitle());
            ps.setDate(2, Date.valueOf(exhibition.getStartDate()));
            ps.setDate(3, Date.valueOf(exhibition.getEndDate()));
            ps.setString(4, exhibition.getDescription());
            ps.setString(5, exhibition.getCuratorName());
            ps.setString(6, exhibition.getTheme());

            int galleryId = findGalleryIdByName(conn, exhibition.getGallery().getName());
            if (galleryId <= 0) {
                System.err.println("[JdbcExhibitionDao] Galerie non trouvée : " + exhibition.getGallery().getName());
                return;
            }
            ps.setInt(7, galleryId);
            ps.executeUpdate();
            System.out.println("[JdbcExhibitionDao] Exhibition '" + exhibition.getTitle() + "' sauvegardée.");
        } catch (SQLException e) {
            System.err.println("[JdbcExhibitionDao] Erreur save() : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setDate(1, Date.valueOf(exhibition.getStartDate()));
            ps.setDate(2, Date.valueOf(exhibition.getEndDate()));
            ps.setString(3, exhibition.getDescription());
            ps.setString(4, exhibition.getCuratorName());
            ps.setString(5, exhibition.getTheme());
            ps.setString(6, exhibition.getTitle());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.err.println("[JdbcExhibitionDao] Aucune exhibition trouvée : " + exhibition.getTitle());
            } else {
                System.out.println("[JdbcExhibitionDao] Exhibition '" + exhibition.getTitle() + "' mise à jour.");
            }
        } catch (SQLException e) {
            System.err.println("[JdbcExhibitionDao] Erreur update() : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setString(1, title);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[JdbcExhibitionDao] Exhibition '" + title + "' supprimée.");
            } else {
                System.err.println("[JdbcExhibitionDao] Aucune exhibition trouvée : " + title);
            }
        } catch (SQLException e) {
            System.err.println("[JdbcExhibitionDao] Erreur delete() : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Exhibition mapRowToExhibition(ResultSet rs) throws SQLException {
        Gallery gallery = new Gallery();
        gallery.setName(rs.getString("gallery_name"));
        gallery.setAddress(rs.getString("gallery_address"));
        gallery.setRating(rs.getDouble("gallery_rating"));

        Exhibition exhibition = new Exhibition();
        exhibition.setTitle(rs.getString("title"));
        exhibition.setStartDate(rs.getDate("start_date").toLocalDate());
        exhibition.setEndDate(rs.getDate("end_date").toLocalDate());
        exhibition.setDescription(rs.getString("description"));
        exhibition.setCuratorName(rs.getString("curator_name"));
        exhibition.setTheme(rs.getString("theme"));
        exhibition.setGallery(gallery);
        return exhibition;
    }

    private int findGalleryIdByName(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_GALLERY_ID_BY_NAME)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("gallery_id");
            }
        }
        return -1;
    }
}
