package ru.matveylegenda.socialaddon.common.database;

import lombok.Getter;
import ru.matveylegenda.socialaddon.common.database.repository.DiscordUserRepository;
import ru.matveylegenda.socialaddon.common.database.repository.MaxUserRepository;
import ru.matveylegenda.socialaddon.common.database.repository.TelegramUserRepository;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

@Getter
public class Database {
    public static final Logger LOGGER = Logger.getLogger("tiAuth-SocialAddon-Database");

    private final ru.matveylegenda.tiauth.database.Database authDatabase;
    private final DiscordUserRepository discordUserRepository;
    private final TelegramUserRepository telegramUserRepository;
    private final MaxUserRepository maxUserRepository;

    public Database(ru.matveylegenda.tiauth.database.Database authDatabase, ConnectionSource connectionSource, ExecutorService executor) throws SQLException {
        this.authDatabase = authDatabase;
        this.discordUserRepository = new DiscordUserRepository(connectionSource, executor);
        this.telegramUserRepository = new TelegramUserRepository(connectionSource, executor);
        this.maxUserRepository = new MaxUserRepository(connectionSource, executor);
    }
}
