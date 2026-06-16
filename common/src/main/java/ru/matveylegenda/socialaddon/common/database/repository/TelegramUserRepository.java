package ru.matveylegenda.socialaddon.common.database.repository;

import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.database.model.TelegramUser;
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

public class TelegramUserRepository {
    private final ExecutorService executor;
    private final Dao<TelegramUser, String> telegramUserDao;

    public TelegramUserRepository(ConnectionSource connectionSource, ExecutorService executor) throws SQLException {
        telegramUserDao = DaoManager.createDao(connectionSource, TelegramUser.class);
        TableUtils.createTableIfNotExists(connectionSource, TelegramUser.class);
        this.executor = executor;
    }

    public CompletableFuture<Boolean> addUser(String playerName, String telegramId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TelegramUser user = new TelegramUser(playerName, telegramId);
                telegramUserDao.create(user);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error adding telegram user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> removeUser(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                telegramUserDao.deleteById(playerName);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error removing telegram user", e);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<TelegramUser> getUserByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return telegramUserDao.queryForId(playerName);
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting telegram user", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<String> getIdByPlayerName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TelegramUser user = telegramUserDao.queryForId(playerName);
                return user != null ? user.getTelegramId() : null;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting telegram ID", e);
                return null;
            }
        }, executor);
    }

    public CompletableFuture<Integer> getAccountCountById(String telegramId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long count = telegramUserDao.queryBuilder()
                        .where()
                        .eq("telegramId", telegramId)
                        .countOf();
                return (int) count;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error getting account count", e);
                return 0;
            }
        }, executor);
    }

    public CompletableFuture<List<String>> getAccountsById(String telegramId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<TelegramUser> users = telegramUserDao.queryBuilder()
                        .where()
                        .eq("telegramId", telegramId)
                        .query();

                List<String> playerNames = new ArrayList<>();
                for (TelegramUser user : users) {
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
                TelegramUser user = telegramUserDao.queryForId(playerName);
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
                TelegramUser user = telegramUserDao.queryForId(playerName);
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
                TelegramUser user = telegramUserDao.queryForId(playerName);
                if (user == null) {
                    return false;
                }

                user.setTwoFa(enabled);
                telegramUserDao.update(user);
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
                TelegramUser user = telegramUserDao.queryForId(playerName);
                if (user == null) {
                    return false;
                }

                user.setAlert(enabled);
                telegramUserDao.update(user);
                return true;
            } catch (SQLException e) {
                Database.LOGGER.log(Level.WARNING, "Error updating alert status for " + playerName, e);
                return false;
            }
        }, executor);
    }
}
