package ru.matveylegenda.socialaddon.common.database.repository;

import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.database.model.VkUser;
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

public class VkUserRepository {
    private final ExecutorService executor;
    private final Dao<VkUser, String> vkUserDao;

    public VkUserRepository(ConnectionSource connectionSource, ExecutorService executor) throws SQLException {
        vkUserDao = DaoManager.createDao(connectionSource, VkUser.class);
        TableUtils.createTableIfNotExists(connectionSource, VkUser.class);
        this.executor = executor;
    }

    public CompletableFuture<Boolean> addUser(String playerName, String vkId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                VkUser user = new VkUser(playerName, vkId);
                vkUserDao.create(user);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error adding vk user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> removeUser(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                vkUserDao.deleteById(playerName);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error removing vk user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<VkUser> getUserByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return vkUserDao.queryForId(playerName);
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting vk user", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<String> getIdByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                VkUser user = vkUserDao.queryForId(playerName);
                return user != null ? user.getVkId() : null;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting vk ID", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<Integer> getAccountCountById(String vkId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long count = vkUserDao.queryBuilder()
                        .where()
                        .eq("vkId", vkId)
                        .countOf();
                return (int) count;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting account count", e);
                return 0;
            }
        }, executor);
    }

    public CompletableFuture<List<String>> getAccountsById(String vkId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<VkUser> users = vkUserDao.queryBuilder()
                        .where()
                        .eq("vkId", vkId)
                        .query();

                List<String> playerNames = new ArrayList<>();
                for (VkUser user : users) {
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
                VkUser user = vkUserDao.queryForId(playerName);
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
                VkUser user = vkUserDao.queryForId(playerName);
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
                VkUser user = vkUserDao.queryForId(playerName);
                if (user == null) {
                    return false;
                }

                user.setTwoFa(enabled);
                vkUserDao.update(user);
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
                VkUser user = vkUserDao.queryForId(playerName);
                if (user == null) {
                    return false;
                }

                user.setAlert(enabled);
                vkUserDao.update(user);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error updating alert status for " + playerName, e);
                return false;
            }
        }, executor);
    }
}
