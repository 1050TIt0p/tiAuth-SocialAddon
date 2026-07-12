package ru.matveylegenda.socialaddon.common.database.repository;

import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.database.model.TelegramUser;

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

public class TelegramUserRepository {
    private final ExecutorService executor;
    private final DataSource dataSource;

    public TelegramUserRepository(DataSource dataSource, ExecutorService executor) throws SQLException {
        this.dataSource = dataSource;
        this.executor = executor;
        createTable();
    }

    private void createTable() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS telegram_users (" +
                    "username VARCHAR(255) PRIMARY KEY," +
                    "telegramId VARCHAR(255) NOT NULL," +
                    "twoFa BOOLEAN DEFAULT FALSE," +
                    "alert BOOLEAN DEFAULT FALSE" +
                    ")"
            );
        }
    }

    public CompletableFuture<Boolean> addUser(String playerName, String telegramId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO telegram_users (username, telegramId) VALUES (?, ?)"
                 )) {
                statement.setString(1, playerName);
                statement.setString(2, telegramId);
                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error adding telegram user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> removeUser(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM telegram_users WHERE username = ?"
                 )) {
                statement.setString(1, playerName);
                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error removing telegram user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<TelegramUser> getUserByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM telegram_users WHERE username = ?"
                 )) {
                statement.setString(1, playerName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return mapTelegramUser(resultSet);
                    }
                    return null;
                }
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting telegram user", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<String> getIdByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT telegramId FROM telegram_users WHERE username = ?"
                 )) {
                statement.setString(1, playerName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("telegramId");
                    }
                    return null;
                }
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting telegram ID", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<Integer> getAccountCountById(String telegramId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT COUNT(*) FROM telegram_users WHERE telegramId = ?"
                 )) {
                statement.setString(1, telegramId);
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

    public CompletableFuture<List<String>> getAccountsById(String telegramId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT username FROM telegram_users WHERE telegramId = ?"
                 )) {
                statement.setString(1, telegramId);
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
                         "SELECT twoFa FROM telegram_users WHERE username = ?"
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
                         "SELECT alert FROM telegram_users WHERE username = ?"
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
                         "UPDATE telegram_users SET twoFa = ? WHERE username = ?"
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
                         "UPDATE telegram_users SET alert = ? WHERE username = ?"
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

    private TelegramUser mapTelegramUser(ResultSet resultSet) throws SQLException {
        TelegramUser user = new TelegramUser();
        user.setUsername(resultSet.getString("username"));
        user.setTelegramId(resultSet.getString("telegramId"));
        user.setTwoFa(resultSet.getBoolean("twoFa"));
        user.setAlert(resultSet.getBoolean("alert"));
        return user;
    }
}
