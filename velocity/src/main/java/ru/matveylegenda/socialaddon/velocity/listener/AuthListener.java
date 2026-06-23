package ru.matveylegenda.socialaddon.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.database.model.DiscordUser;
import ru.matveylegenda.socialaddon.common.database.model.TelegramUser;
import ru.matveylegenda.socialaddon.common.social.platform.Discord;
import ru.matveylegenda.socialaddon.common.social.platform.Telegram;
import ru.matveylegenda.socialaddon.common.utils.Utils;
import ru.matveylegenda.socialaddon.velocity.SocialAddon;
import ru.matveylegenda.tiauth.cache.AuthCache;
import ru.matveylegenda.tiauth.cache.SessionCache;
import ru.matveylegenda.tiauth.config.MainConfig;
import ru.matveylegenda.tiauth.velocity.api.event.PlayerAuthEvent;
import ru.matveylegenda.tiauth.velocity.api.event.PlayerRegisterEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static ru.matveylegenda.tiauth.util.Utils.COLORIZER;

public class AuthListener {
    private final Map<UUID, SocialUserData> userCache = new ConcurrentHashMap<>();

    private final SocialAddon plugin;

    private final Discord discord;
    private final Telegram telegram;

    public AuthListener(SocialAddon plugin) {
        this.plugin = plugin;
        this.discord = plugin.getDiscord();
        this.telegram = plugin.getTelegram();
    }

    @Subscribe(priority = -100)
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();

        SocialUserData data = new SocialUserData();
        userCache.put(player.getUniqueId(), data);

        plugin.getDatabase().getDiscordUserRepository().getUserByPlayerName(player.getUsername()).thenAccept(user -> {
            if (user != null) data.discordUser = user;
        });

        plugin.getDatabase().getTelegramUserRepository().getUserByPlayerName(player.getUsername()).thenAccept(user -> {
            if (user != null) data.telegramUser = user;
        });
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        userCache.remove(player.getUniqueId());
    }

    @Subscribe
    public void onServerConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        Optional<RegisteredServer> targetOpt = event.getResult().getServer();

        if (targetOpt.isEmpty()) {
            return;
        }

        RegisteredServer targetServer = targetOpt.get();
        String serverName = targetServer.getServerInfo().getName();

        SocialUserData data = userCache.get(player.getUniqueId());

        if (ru.matveylegenda.socialaddon.common.config.MainConfig.IMP.linkedOnlyServers.contains(serverName)) {
            if (data == null || (data.discordUser == null && data.telegramUser == null)) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());

                player.sendMessage(Utils.LEGACY.deserialize(
                        COLORIZER.colorize(MessagesConfig.IMP.joinToLinkedOnlyServer)
                ));
            }
        }
    }

    @Subscribe(priority = -100)
    public void onServerConnectedEvent(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        SocialPlayer socialPlayer = plugin.getSocialPlatform().getPlayer(player.getUsername());

        if (!event.getServer().getServerInfo().getName().equals(MainConfig.IMP.servers.auth) &&
                AuthCache.isAuthenticated(player.getUsername())) {
            plugin.getTaskManager().cancelTasks(socialPlayer);
        }
    }

    @Subscribe
    public void onPlayerRegister(PlayerRegisterEvent event) {
        if (event.isForceLogin()) return;

        Player player = event.getPlayer();

        SocialUserData data = userCache.get(player.getUniqueId());
        if (data == null) return;

        SocialPlayer socialPlayer = plugin.getSocialPlatform().getPlayer(player.getUsername());
        if (socialPlayer == null) return;

        if (data.discordUser != null) {
            discord.checkPlayer(socialPlayer, data.discordUser.getDiscordId(), data.discordUser.isTwoFa(), data.discordUser.isAlert());
            if (!data.discordUser.isTwoFa()) {
                event.setMoveToBackendServer(false);
                AuthCache.logout(player.getUsername());
                SessionCache.removePlayer(player.getUsername());
            }
        } else if (data.telegramUser != null) {
            telegram.checkPlayer(socialPlayer, data.telegramUser.getTelegramId(), data.telegramUser.isTwoFa(), data.telegramUser.isAlert());
            if (!data.telegramUser.isTwoFa()) {
                event.setMoveToBackendServer(false);
                AuthCache.logout(player.getUsername());
                SessionCache.removePlayer(player.getUsername());
            }
        }
    }

    @Subscribe
    public void onPlayerAuth(PlayerAuthEvent event) {
        if (event.isForceLogin()) return;

        Player player = event.getPlayer();

        SocialUserData data = userCache.get(player.getUniqueId());
        if (data == null) return;

        SocialPlayer socialPlayer = plugin.getSocialPlatform().getPlayer(player.getUsername());
        if (socialPlayer == null) return;

        if (data.discordUser != null) {
            discord.checkPlayer(socialPlayer, data.discordUser.getDiscordId(), data.discordUser.isTwoFa(), data.discordUser.isAlert());
            if (data.discordUser.isTwoFa()) {
                event.setMoveToBackendServer(false);
                AuthCache.logout(player.getUsername());
                SessionCache.removePlayer(player.getUsername());
            }
        } else if (data.telegramUser != null) {
            telegram.checkPlayer(socialPlayer, data.telegramUser.getTelegramId(), data.telegramUser.isTwoFa(), data.telegramUser.isAlert());
            if (data.telegramUser.isTwoFa()) {
                event.setMoveToBackendServer(false);
                AuthCache.logout(player.getUsername());
                SessionCache.removePlayer(player.getUsername());
            }
        }
    }

    public static class SocialUserData {
        public DiscordUser discordUser;
        public TelegramUser telegramUser;
    }
}
