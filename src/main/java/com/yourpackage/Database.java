package com.yourpackage;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;

public class Database {
    private final MoonShard plugin;
    private Connection connection;

    public Database(MoonShard plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        String type = plugin.getConfig().getString("Database.Type", "SQLite");
        try {
            if ("SQLite".equalsIgnoreCase(type)) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/moonshard.db");
                return true;
            } else if ("MySQL".equalsIgnoreCase(type)) {
                String host = plugin.getConfig().getString("Database.MySQL.Host", "127.0.0.1");
                int port = plugin.getConfig().getInt("Database.MySQL.Port", 3306);
                String user = plugin.getConfig().getString("Database.MySQL.Username", "root");
                String pass = plugin.getConfig().getString("Database.MySQL.Password", "");
                String db = plugin.getConfig().getString("Database.MySQL.Database", "moonshard");
                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true",
                        user, pass
                );
                return true;
            } else {
                plugin.getLogger().severe("無效的資料庫類型: " + type + "，支援 SQLite 或 MySQL");
                return false;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("無法連接到資料庫 (" + type + "): " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("無法關閉資料庫連線: " + e.getMessage());
        }
    }

    public void createTable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS moonshard_economy (uuid VARCHAR(36) PRIMARY KEY, balance INTEGER NOT NULL DEFAULT 0)");
        } catch (SQLException e) {
            plugin.getLogger().severe("無法創建資料表: " + e.getMessage());
        }
    }

    public int getBalance(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT balance FROM moonshard_economy WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("balance");
            } else {
                insertPlayer(uuid);
                return plugin.getStartingBalance();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("無法獲取玩家餘額: " + e.getMessage());
            return 0;
        }
    }

    public void updateBalance(UUID uuid, int newBalance) {
        try (PreparedStatement ps = connection.prepareStatement("REPLACE INTO moonshard_economy (uuid, balance) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, newBalance);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("無法更新玩家餘額: " + e.getMessage());
        }
    }

    private void insertPlayer(UUID uuid) {
        updateBalance(uuid, plugin.getStartingBalance());
    }
}