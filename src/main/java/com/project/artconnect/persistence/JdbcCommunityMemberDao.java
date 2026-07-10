package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.dao.DaoException;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCommunityMemberDao implements CommunityMemberDao {

    private static final String SQL_FIND_ALL =
            "SELECT member_id, name, email, birth_year, phone, city, membership_type FROM community_member";
    private static final String SQL_FIND_BY_ID = SQL_FIND_ALL + " WHERE member_id = ?";
    private static final String SQL_FIND_BY_NAME = SQL_FIND_ALL + " WHERE name = ?";

    private static final String SQL_INSERT =
            "INSERT INTO community_member (name, email, birth_year, phone, city, membership_type) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE community_member SET email = ?, birth_year = ?, phone = ?, city = ?, " +
            "membership_type = ? WHERE name = ?";
    private static final String SQL_DELETE = "DELETE FROM community_member WHERE name = ?";

    private static final String SQL_FIND_FAV =
            "SELECT d.name FROM discipline d " +
            "INNER JOIN member_favorite_discipline mfd ON d.discipline_id = mfd.discipline_id " +
            "WHERE mfd.member_id = ?";

    @Override
    public List<CommunityMember> findAll() {
        List<CommunityMember> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("member_id");
                CommunityMember m = mapRow(rs);
                m.setFavoriteDisciplines(findFav(c, id));
                list.add(m);
            }
        } catch (SQLException e) {
            System.err.println("[JdbcCommunityMemberDao] findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<CommunityMember> findById(Long id) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int mid = rs.getInt("member_id");
                    CommunityMember m = mapRow(rs);
                    m.setFavoriteDisciplines(findFav(c, mid));
                    return Optional.of(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("[JdbcCommunityMemberDao] findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<CommunityMember> findByName(String name) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_NAME)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int mid = rs.getInt("member_id");
                    CommunityMember m = mapRow(rs);
                    m.setFavoriteDisciplines(findFav(c, mid));
                    return Optional.of(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("[JdbcCommunityMemberDao] findByName: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void save(CommunityMember m) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getEmail());
            if (m.getBirthYear() != null) ps.setInt(3, m.getBirthYear());
            else ps.setNull(3, Types.INTEGER);
            ps.setString(4, m.getPhone());
            ps.setString(5, m.getCity());
            ps.setString(6, m.getMembershipType() != null ? m.getMembershipType() : "free");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur save member", e);
        }
    }

    @Override
    public void update(CommunityMember m) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, m.getEmail());
            if (m.getBirthYear() != null) ps.setInt(2, m.getBirthYear());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, m.getPhone());
            ps.setString(4, m.getCity());
            ps.setString(5, m.getMembershipType() != null ? m.getMembershipType() : "free");
            ps.setString(6, m.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur update member", e);
        }
    }

    @Override
    public void delete(String name) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur delete member", e);
        }
    }

    private CommunityMember mapRow(ResultSet rs) throws SQLException {
        CommunityMember m = new CommunityMember();
        m.setName(rs.getString("name"));
        m.setEmail(rs.getString("email"));
        int by = rs.getInt("birth_year");
        m.setBirthYear(rs.wasNull() ? null : by);
        m.setPhone(rs.getString("phone"));
        m.setCity(rs.getString("city"));
        m.setMembershipType(rs.getString("membership_type"));
        return m;
    }

    private List<Discipline> findFav(Connection c, int memberId) throws SQLException {
        List<Discipline> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(SQL_FIND_FAV)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Discipline(rs.getString("name")));
            }
        }
        return list;
    }
}
