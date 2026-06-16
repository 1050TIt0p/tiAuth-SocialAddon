package ru.matveylegenda.socialaddon.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;
import ru.matveylegenda.socialaddon.common.api.SocialPlatform;
import ru.matveylegenda.socialaddon.common.config.MainConfig;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.manager.TaskManager;
import ru.matveylegenda.socialaddon.common.social.platform.Discord;
import ru.matveylegenda.socialaddon.common.social.platform.Telegram;
import ru.matveylegenda.socialaddon.velocity.adapter.VelocityPlatformAdapter;
import ru.matveylegenda.socialaddon.velocity.adapter.VelocitySchedulerAdapter;
import ru.matveylegenda.socialaddon.velocity.command.LinkCommand;
import ru.matveylegenda.socialaddon.velocity.listener.AuthListener;
import ru.matveylegenda.tiauth.velocity.api.TiAuthAPI;

import java.sql.SQLException;
import java.util.logging.Level;

@Getter
@Plugin(
        id = "tiauth-socialaddon",
        name = "tiAuth-SocialAddon",
        version = "1.0.1",
        dependencies = {
                @Dependency(id = "tiauth")
        }
)
public class SocialAddon {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    private TaskManager taskManager;
    private Database database;
    private SocialPlatform socialPlatform;

    private Discord discord;
    private Telegram telegram;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.taskManager = new TaskManager(new VelocitySchedulerAdapter(this));
        this.socialPlatform = new VelocityPlatformAdapter(server);
        initializeDatabase();

        MainConfig.IMP.reload();
        MessagesConfig.IMP.reload();
        DiscordConfig.IMP.reload();
        TelegramConfig.IMP.reload();

        initializeDiscord();
        initializeTelegram();

        registerListeners();
        registerCommands();
    }

    private void initializeDatabase() {
        try {
            this.database = new Database(
                    TiAuthAPI.getInstance().getDatabase(),
                    TiAuthAPI.getInstance().getDatabase().getConnectionSource(),
                    TiAuthAPI.getInstance().getDatabase().getExecutor()
            );
        } catch (SQLException e) {
            Database.LOGGER.log(Level.SEVERE, "Error during database initialization", e);
        }
    }

    private void initializeDiscord() {
        try {
            discord = new Discord(taskManager, database, socialPlatform);
            discord.enableBot();
        } catch (Exception e) {
            logger.error("Error starting the Discord bot", e);
            server.shutdown();
        }
    }

    private void initializeTelegram() {
        try {
            telegram = new Telegram(taskManager, database, socialPlatform);
            telegram.enableBot();
        } catch (Exception e) {
            logger.error("Error starting the Telegram bot", e);
            server.shutdown();
        }
    }

    private void registerListeners() {
        server.getEventManager().register(this, new AuthListener(this));
    }

    private void registerCommands() {
        CommandManager commandManager = server.getCommandManager();
        commandManager.register(
                commandManager.metaBuilder(MainConfig.IMP.command).build(),
                new LinkCommand(this)
        );
    }
}
