package ru.matveylegenda.socialaddon.common.database.repository;

import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.database.model.DiscordUser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

public class DiscordUserRepository {
    private final ExecutorService executor;
    private final DataSource dataSource;

    public DiscordUserRepository(DataSource dataSource, ExecutorService executor) throws SQLException {
        this.dataSource = dataSource;
        this.executor = executor;
        createTable();
    }

    private void createTable() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS discord_users (" +
                    "username VARCHAR(255) PRIMARY KEY," +
                    "discordId VARCHAR(255) NOT NULL," +
                    "twoFa BOOLEAN DEFAULT FALSE," +
                    "alert BOOLEAN DEFAULT FALSE" +
                    ")"
            );
        }
    }

    public CompletableFuture<Boolean> addUser(String playerName, String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO discord_users (username, discordId) VALUES (?, ?)"
                 )) {
                statement.setString(1, playerName);
                statement.setString(2, discordId);
                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error adding discord user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> removeUser(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM discord_users WHERE username = ?"
                 )) {
                statement.setString(1, playerName);
                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error removing discord user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<DiscordUser> getUserByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM discord_users WHERE username = ?"
                 )) {
                statement.setString(1, playerName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return mapDiscordUser(resultSet);
                    }
                    return null;
                }
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting discord user", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<String> getIdByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT discordId FROM discord_users WHERE username = ?"
                 )) {
                statement.setString(1, playerName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("discordId");
                    }
                    return null;
                }
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting discord ID", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<Integer> getAccountCountById(String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT COUNT(*) FROM discord_users WHERE discordId = ?"
                 )) {
                statement.setString(1, discordId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                    return 0;
                }
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting account count", e);
                return 0;
            }
        }, executor);
    }

    public CompletableFuture<List<String>> getAccountsById(String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT username FROM discord_users WHERE discordId = ?"
                 )) {
                statement.setString(1, discordId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<String> playerNames = new ArrayList<>();
                    while (resultSet.next()) {
                        playerNames.add(resultSet.getString("username"));
                    }
                    return playerNames;
                }
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting accounts list", e);
                return new ArrayList<>();
            }
        }, executor);
    }

    public CompletableFuture<Boolean> isTwoFaEnabled(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT twoFa FROM discord_users WHERE username = ?"
                 )) {
                statement.setString(1, playerName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() && resultSet.getBoolean("twoFa");
                }
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error checking 2FA status", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> isAlertEnabled(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT alert FROM discord_users WHERE username = ?"
                 )) {
                statement.setString(1, playerName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() && resultSet.getBoolean("alert");
                }
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error checking alert status", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> setTwoFaEnabled(String playerName, boolean enabled) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE discord_users SET twoFa = ? WHERE username = ?"
                 )) {
                statement.setBoolean(1, enabled);
                statement.setString(2, playerName);
                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error updating 2FA status for " + playerName, e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> setAlertEnabled(String playerName, boolean enabled) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE discord_users SET alert = ? WHERE username = ?"
                 )) {
                statement.setBoolean(1, enabled);
                statement.setString(2, playerName);
                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error updating alert status for " + playerName, e);
                return false;
            }
        }, executor);
    }

    private DiscordUser mapDiscordUser(ResultSet resultSet) throws SQLException {
        DiscordUser user = new DiscordUser();
        user.setUsername(resultSet.getString("username"));
        user.setDiscordId(resultSet.getString("discordId"));
        user.setTwoFa(resultSet.getBoolean("twoFa"));
        user.setAlert(resultSet.getBoolean("alert"));
        return user;
    }
}
