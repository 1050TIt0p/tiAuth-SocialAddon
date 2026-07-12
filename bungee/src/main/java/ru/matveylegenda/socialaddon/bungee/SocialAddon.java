package ru.matveylegenda.socialaddon.bungee;

import lombok.Getter;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import ru.matveylegenda.socialaddon.bungee.adapter.BungeePlatformAdapter;
import ru.matveylegenda.socialaddon.bungee.adapter.BungeeSchedulerAdapter;
import ru.matveylegenda.socialaddon.bungee.command.LinkCommand;
import ru.matveylegenda.socialaddon.bungee.listener.AuthListener;
import ru.matveylegenda.socialaddon.common.api.SocialPlatform;
import ru.matveylegenda.socialaddon.common.config.MainConfig;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.manager.TaskManager;
import ru.matveylegenda.socialaddon.common.social.platform.Discord;
import ru.matveylegenda.socialaddon.common.social.platform.Telegram;
import ru.matveylegenda.tiauth.bungee.api.TiAuthAPI;

import java.sql.SQLException;
import java.util.logging.Level;

@Getter
public final class SocialAddon extends Plugin {
    private BungeeAudiences audiences;

    private TaskManager taskManager;
    private Database database;
    private SocialPlatform socialPlatform;

    private Discord discord;
    private Telegram telegram;

    @Override
    public void onEnable() {
        this.audiences = BungeeAudiences.create(this);
        this.taskManager = new TaskManager(new BungeeSchedulerAdapter(this));
        this.socialPlatform = new BungeePlatformAdapter(audiences);
        initializeDatabase();

        MainConfig.IMP.reload();
        MessagesConfig.IMP.reload();
        DiscordConfig.IMP.reload();
        TelegramConfig.IMP.reload();

        initializeDiscord();
        initializeTelegram();

        PluginManager pluginManager = getProxy().getPluginManager();
        registerListeners(pluginManager);
        registerCommands(pluginManager);
    }

    @Override
    public void onDisable() {

    }

    private void initializeDatabase() {
        try {
            this.database = new Database(
                    TiAuthAPI.getInstance().getDatabase(),
                    TiAuthAPI.getInstance().getDatabase().getDataSource(),
                    TiAuthAPI.getInstance().getDatabase().getExecutor()
            );
        } catch (SQLException e) {
            Database.LOGGER.log(Level.SEVERE, "Error during database initialization", e);
            getProxy().stop();
        }
    }

    private void initializeDiscord() {
        try {
            discord = new Discord(taskManager, database, socialPlatform);
            discord.enableBot();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error starting the Discord bot", e);
            getProxy().stop();
        }
    }

    private void initializeTelegram() {
        try {
            telegram = new Telegram(taskManager, database, socialPlatform);
            telegram.enableBot();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error starting the Telegram bot", e);
            getProxy().stop();
        }
    }

    private void registerListeners(PluginManager pluginManager) {
        pluginManager.registerListener(this, new AuthListener(this));
    }

    private void registerCommands(PluginManager pluginManager) {
        pluginManager.registerCommand(
                this,
                new LinkCommand(this, MainConfig.IMP.command)
        );
    }
}
