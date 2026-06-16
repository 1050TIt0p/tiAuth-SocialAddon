package ru.matveylegenda.socialaddon.common.database.repository;

import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.database.model.DiscordUser;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.dao.Dao;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.dao.DaoManager;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.support.ConnectionSource;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

public class DiscordUserRepository {
    private final ExecutorService executor;
    private final Dao<DiscordUser, String> discordUserDao;

    public DiscordUserRepository(ConnectionSource connectionSource, ExecutorService executor) throws SQLException {
        discordUserDao = DaoManager.createDao(connectionSource, DiscordUser.class);
        TableUtils.createTableIfNotExists(connectionSource, DiscordUser.class);
        this.executor = executor;
    }

    public CompletableFuture<Boolean> addUser(String playerName, String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DiscordUser user = new DiscordUser(playerName, discordId);
                discordUserDao.create(user);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error adding discord user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> removeUser(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                discordUserDao.deleteById(playerName);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error removing discord user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<DiscordUser> getUserByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return discordUserDao.queryForId(playerName);
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting discord user", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<String> getIdByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DiscordUser user = discordUserDao.queryForId(playerName);
                return user != null ? user.getDiscordId() : null;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting discord ID", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<Integer> getAccountCountById(String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long count = discordUserDao.queryBuilder()
                        .where()
                        .eq("discordId", discordId)
                        .countOf();
                return (int) count;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting account count", e);
                return 0;
            }
        }, executor);
    }

    public CompletableFuture<List<String>> getAccountsById(String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<DiscordUser> users = discordUserDao.queryBuilder()
                        .where()
                        .eq("discordId", discordId)
                        .query();

                List<String> playerNames = new ArrayList<>();
                for (DiscordUser user : users) {
                    playerNames.add(user.getUsername());
                }
                return playerNames;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting accounts list", e);
                return new ArrayList<>();
            }
        }, executor);
    }

    public CompletableFuture<Boolean> isTwoFaEnabled(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DiscordUser user = discordUserDao.queryForId(playerName);
                return user != null && user.isTwoFa();
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error checking 2FA status", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> isAlertEnabled(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DiscordUser user = discordUserDao.queryForId(playerName);
                return user != null && user.isAlert();
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error checking alert status", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> setTwoFaEnabled(String playerName, boolean enabled) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DiscordUser user = discordUserDao.queryForId(playerName);
                if (user == null) {
                    return false;
                }

                user.setTwoFa(enabled);
                discordUserDao.update(user);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error updating 2FA status for " + playerName, e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> setAlertEnabled(String playerName, boolean enabled) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DiscordUser user = discordUserDao.queryForId(playerName);
                if (user == null) {
                    return false;
                }

                user.setAlert(enabled);
                discordUserDao.update(user);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error updating alert status for " + playerName, e);
                return false;
            }
        }, executor);
    }
}
