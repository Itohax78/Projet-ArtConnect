package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC de l'interface GalleryDao.
 *
 * <p>Gère la persistance des objets {@link Gallery} dans MySQL.</p>
 */
public class JdbcGalleryDao implements GalleryDao {

    private static final String SQL_FIND_ALL =
            "SELECT gallery_id, name, address, owner_name, opening_hours, " +
            "contact_phone, rating, website FROM gallery";

    private static final String SQL_FIND_BY_ID = SQL_FIND_ALL + " WHERE gallery_id = ?";
    private static final String SQL_FIND_BY_NAME = SQL_FIND_ALL + " WHERE name = ?";

    private static final String SQL_INSERT =
            "INSERT INTO gallery (name, address, owner_name, opening_hours, contact_phone, rating, website) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE gallery SET address = ?, owner_name = ?, opening_hours = ?, " +
            "contact_phone = ?, rating = ?, website = ? WHERE name = ?";

    private static final String SQL_DELETE = "DELETE FROM gallery WHERE name = ?";

    @Override
    public List<Gallery> findAll() {
        List<Gallery> galleries = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                galleries.add(mapRowToGallery(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] Erreur findAll() : " + e.getMessage());
            e.printStackTrace();
        }
        return galleries;
    }

    @Override
    public Optional<Gallery> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRowToGallery(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] Erreur findById(" + id + ") : " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Gallery> findByName(String name) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRowToGallery(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] Erreur findByName('" + name + "') : " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void save(Gallery gallery) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, gallery.getName());
            ps.setString(2, gallery.getAddress());
            ps.setString(3, gallery.getOwnerName());
            ps.setString(4, gallery.getOpeningHours());
            ps.setString(5, gallery.getContactPhone());
            ps.setDouble(6, gallery.getRating());
            ps.setString(7, gallery.getWebsite());
            ps.executeUpdate();
            System.out.println("[JdbcGalleryDao] Galerie '" + gallery.getName() + "' sauvegardée.");
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] Erreur save() : " + e.getMessage());
            throw new com.project.artconnect.dao.DaoException("Erreur lors de la sauvegarde de la galerie", e);
        }
    }

    @Override
    public void update(Gallery gallery) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, gallery.getAddress());
            ps.setString(2, gallery.getOwnerName());
            ps.setString(3, gallery.getOpeningHours());
            ps.setString(4, gallery.getContactPhone());
            ps.setDouble(5, gallery.getRating());
            ps.setString(6, gallery.getWebsite());
            ps.setString(7, gallery.getName());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.err.println("[JdbcGalleryDao] Aucune galerie trouvée : " + gallery.getName());
            } else {
                System.out.println("[JdbcGalleryDao] Galerie '" + gallery.getName() + "' mise à jour.");
            }
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] Erreur update() : " + e.getMessage());
            throw new com.project.artconnect.dao.DaoException("Erreur lors de la mise à jour de la galerie", e);
        }
    }

    @Override
    public void delete(String name) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setString(1, name);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[JdbcGalleryDao] Galerie '" + name + "' supprimée.");
            } else {
                System.err.println("[JdbcGalleryDao] Aucune galerie trouvée : " + name);
            }
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] Erreur delete() : " + e.getMessage());
            throw new com.project.artconnect.dao.DaoException("Erreur lors de la suppression de la galerie", e);
        }
    }

    /** Utilitaire pour les autres DAOs (ex : JdbcExhibitionDao). */
    public int findGalleryIdByName(String galleryName) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT gallery_id FROM gallery WHERE name = ?")) {
            ps.setString(1, galleryName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("gallery_id");
            }
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] Erreur findGalleryIdByName : " + e.getMessage());
        }
        return -1;
    }

    private Gallery mapRowToGallery(ResultSet rs) throws SQLException {
        Gallery gallery = new Gallery();
        gallery.setName(rs.getString("name"));
        gallery.setAddress(rs.getString("address"));
        gallery.setOwnerName(rs.getString("owner_name"));
        gallery.setOpeningHours(rs.getString("opening_hours"));
        gallery.setContactPhone(rs.getString("contact_phone"));
        gallery.setRating(rs.getDouble("rating"));
        gallery.setWebsite(rs.getString("website"));
        return gallery;
    }
}
