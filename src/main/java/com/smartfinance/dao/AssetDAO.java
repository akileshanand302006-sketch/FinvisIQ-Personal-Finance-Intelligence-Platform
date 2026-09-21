package com.smartfinance.dao;

import com.smartfinance.model.Asset;
import java.sql.*;
import java.util.ArrayList;

/**
 * Asset DAO — Database operations for user assets.
 */
public class AssetDAO {
    private final DatabaseManager dbManager;

    public AssetDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(Asset asset) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().createAsset(asset);
        }
        String sql = "INSERT INTO assets (user_id, name, type, value, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, asset.getUserId());
            pstmt.setString(2, asset.getName());
            pstmt.setString(3, asset.getType());
            pstmt.setDouble(4, asset.getValue());
            pstmt.setString(5, asset.getNotes());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                asset.setAssetId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting asset: " + e.getMessage());
        }
        return -1;
    }

    public ArrayList<Asset> findByUserId(int userId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().getAssets(userId);
        }
        ArrayList<Asset> list = new ArrayList<>();
        String sql = "SELECT * FROM assets WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching assets: " + e.getMessage());
        }
        return list;
    }

    public Asset findById(int assetId) {
        String sql = "SELECT * FROM assets WHERE asset_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, assetId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching asset by id: " + e.getMessage());
        }
        return null;
    }

    public boolean update(Asset asset) {
        String sql = "UPDATE assets SET name=?, type=?, value=?, notes=? WHERE asset_id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, asset.getName());
            pstmt.setString(2, asset.getType());
            pstmt.setDouble(3, asset.getValue());
            pstmt.setString(4, asset.getNotes());
            pstmt.setInt(5, asset.getAssetId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating asset: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int assetId) {
        String sql = "DELETE FROM assets WHERE asset_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, assetId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting asset: " + e.getMessage());
        }
        return false;
    }

    private Asset mapRow(ResultSet rs) throws SQLException {
        return new Asset(
            rs.getInt("asset_id"),
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("type"),
            rs.getDouble("value"),
            rs.getString("notes")
        );
    }
}
