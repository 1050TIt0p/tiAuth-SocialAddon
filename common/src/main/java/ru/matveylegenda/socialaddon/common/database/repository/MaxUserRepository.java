package ru.matveylegenda.socialaddon.common.database.repository;

import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.database.model.MaxUser;
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

public class MaxUserRepository {
    private final ExecutorService executor;
    private final Dao<MaxUser, String> maxUserDao;

    public MaxUserRepository(ConnectionSource connectionSource, ExecutorService executor) throws SQLException {
        maxUserDao = DaoManager.createDao(connectionSource, MaxUser.class);
        TableUtils.createTableIfNotExists(connectionSource, MaxUser.class);
        this.executor = executor;
    }

    public CompletableFuture<Boolean> addUser(String playerName, String maxId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MaxUser user = new MaxUser(playerName, maxId);
                maxUserDao.create(user);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error adding max user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> removeUser(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                maxUserDao.deleteById(playerName);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error removing max user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<MaxUser> getUserByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return maxUserDao.queryForId(playerName);
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting max user", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<String> getIdByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MaxUser user = maxUserDao.queryForId(playerName);
                return user != null ? user.getMaxId() : null;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting max ID", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<Integer> getAccountCountById(String maxId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long count = maxUserDao.queryBuilder()
                        .where()
                        .eq("maxId", maxId)
                        .countOf();
                return (int) count;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting account count", e);
                return 0;
            }
        }, executor);
    }

    public CompletableFuture<List<String>> getAccountsById(String maxId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<MaxUser> users = maxUserDao.queryBuilder()
                        .where()
                        .eq("maxId", maxId)
                        .query();

                List<String> playerNames = new ArrayList<>();
                for (MaxUser user : users) {
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
                MaxUser user = maxUserDao.queryForId(playerName);
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
                MaxUser user = maxUserDao.queryForId(playerName);
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
                MaxUser user = maxUserDao.queryForId(playerName);
                if (user == null) {
                    return false;
                }

                user.setTwoFa(enabled);
                maxUserDao.update(user);
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
                MaxUser user = maxUserDao.queryForId(playerName);
                if (user == null) {
                    return false;
                }

                user.setAlert(enabled);
                maxUserDao.update(user);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error updating alert status for " + playerName, e);
                return false;
            }
        }, executor);
    }
}
