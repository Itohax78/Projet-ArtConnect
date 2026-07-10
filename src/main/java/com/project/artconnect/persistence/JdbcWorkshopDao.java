package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.dao.DaoException;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcWorkshopDao implements WorkshopDao {

    private static final String SQL_FIND_ALL =
            "SELECT w.workshop_id, w.title, w.date, w.duration_minutes, w.max_participants, " +
            "w.price, w.location, w.description, w.level, w.instructor_id, " +
            "a.name AS instructor_name, a.bio AS instructor_bio, a.city AS instructor_city " +
            "FROM workshop w INNER JOIN artist a ON w.instructor_id = a.artist_id";

    private static final String SQL_FIND_BY_ID = SQL_FIND_ALL + " WHERE w.workshop_id = ?";
    private static final String SQL_FIND_BY_TITLE = SQL_FIND_ALL + " WHERE w.title = ?";

    private static final String SQL_INSERT =
            "INSERT INTO workshop (title, date, duration_minutes, max_participants, price, " +
            "location, description, level, instructor_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE workshop SET date = ?, duration_minutes = ?, max_participants = ?, " +
            "price = ?, location = ?, description = ?, level = ? WHERE title = ?";

    private static final String SQL_DELETE = "DELETE FROM workshop WHERE title = ?";

    private static final String SQL_FIND_ARTIST_ID =
            "SELECT artist_id FROM artist WHERE name = ?";

    @Override
    public List<Workshop> findAll() {
        List<Workshop> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[JdbcWorkshopDao] findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Workshop> findById(Long id) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JdbcWorkshopDao] findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Workshop> findByTitle(String title) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_TITLE)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JdbcWorkshopDao] findByTitle: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void save(Workshop w) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT)) {
            ps.setString(1, w.getTitle());
            ps.setTimestamp(2, Timestamp.valueOf(w.getDate()));
            ps.setInt(3, w.getDurationMinutes());
            ps.setInt(4, w.getMaxParticipants());
            ps.setDouble(5, w.getPrice());
            ps.setString(6, w.getLocation());
            ps.setString(7, w.getDescription());
            ps.setString(8, w.getLevel());
            int instrId = findArtistId(c, w.getInstructor().getName());
            if (instrId <= 0) throw new DaoException("Instructeur non trouvé: " + w.getInstructor().getName());
            ps.setInt(9, instrId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur save workshop", e);
        }
    }

    @Override
    public void update(Workshop w) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
            ps.setTimestamp(1, Timestamp.valueOf(w.getDate()));
            ps.setInt(2, w.getDurationMinutes());
            ps.setInt(3, w.getMaxParticipants());
            ps.setDouble(4, w.getPrice());
            ps.setString(5, w.getLocation());
            ps.setString(6, w.getDescription());
            ps.setString(7, w.getLevel());
            ps.setString(8, w.getTitle());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur update workshop", e);
        }
    }

    @Override
    public void delete(String title) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur delete workshop", e);
        }
    }

    private int findArtistId(Connection c, String name) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_FIND_ARTIST_ID)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("artist_id");
            }
        }
        return -1;
    }

    private Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop w = new Workshop();
        w.setTitle(rs.getString("title"));
        Timestamp ts = rs.getTimestamp("date");
        if (ts != null) w.setDate(ts.toLocalDateTime());
        w.setDurationMinutes(rs.getInt("duration_minutes"));
        w.setMaxParticipants(rs.getInt("max_participants"));
        w.setPrice(rs.getDouble("price"));
        w.setLocation(rs.getString("location"));
        w.setDescription(rs.getString("description"));
        w.setLevel(rs.getString("level"));
        Artist instr = new Artist();
        instr.setName(rs.getString("instructor_name"));
        instr.setBio(rs.getString("instructor_bio"));
        instr.setCity(rs.getString("instructor_city"));
        w.setInstructor(instr);
        return w;
    }
}
