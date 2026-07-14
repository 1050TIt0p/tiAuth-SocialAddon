package ru.matveylegenda.socialaddon.bungee.listener;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import ru.matveylegenda.socialaddon.bungee.SocialAddon;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.database.model.DiscordUser;
import ru.matveylegenda.socialaddon.common.database.model.MaxUser;
import ru.matveylegenda.socialaddon.common.database.model.TelegramUser;
import ru.matveylegenda.socialaddon.common.social.platform.Discord;
import ru.matveylegenda.socialaddon.common.social.platform.Max;
import ru.matveylegenda.socialaddon.common.social.platform.Telegram;
import ru.matveylegenda.tiauth.bungee.api.event.PlayerAuthEvent;
import ru.matveylegenda.tiauth.bungee.api.event.PlayerRegisterEvent;
import ru.matveylegenda.tiauth.cache.AuthCache;
import ru.matveylegenda.tiauth.cache.SessionCache;
import ru.matveylegenda.tiauth.config.MainConfig;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static ru.matveylegenda.tiauth.util.Utils.COLORIZER;

public class AuthListener implements Listener {
    private final Map<UUID, SocialUserData> userCache = new ConcurrentHashMap<>();

    private final SocialAddon plugin;

    private final Discord discord;
    private final Telegram telegram;
    private final Max max;

    public AuthListener(SocialAddon plugin) {
        this.plugin = plugin;
        this.discord = plugin.getDiscord();
        this.telegram = plugin.getTelegram();
        this.max = plugin.getMax();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        SocialUserData data = new SocialUserData();
        userCache.put(player.getUniqueId(), data);

        plugin.getDatabase().getDiscordUserRepository().getUserByPlayerName(player.getName()).thenAccept(user -> {
            if (user != null) data.discordUser = user;
        });

        plugin.getDatabase().getTelegramUserRepository().getUserByPlayerName(player.getName()).thenAccept(user -> {
            if (user != null) data.telegramUser = user;
        });

        plugin.getDatabase().getMaxUserRepository().getUserByPlayerName(player.getName()).thenAccept(user -> {
            if (user != null) data.maxUser = user;
        });
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        userCache.remove(player.getUniqueId());
        AuthCache.clearPendingVerification(player.getName());
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServerInfo targetServer = event.getTarget();

        SocialUserData data = userCache.get(player.getUniqueId());

        if (ru.matveylegenda.socialaddon.common.config.MainConfig.IMP.linkedOnlyServers.contains(targetServer.getName())) {
            if (data == null || (data.discordUser == null && data.telegramUser == null || data.maxUser == null)) {
                event.setCancelled(true);

                player.sendMessage(TextComponent.fromLegacy(
                        COLORIZER.colorize(
                                MessagesConfig.IMP.joinToLinkedOnlyServer
                        )
                ));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnectedEvent(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        SocialPlayer socialPlayer = plugin.getSocialPlatform().getPlayer(player.getName());

        if (!event.getServer().getInfo().getName().equals(MainConfig.IMP.servers.auth) &&
                AuthCache.isAuthenticated(player.getName())) {
            plugin.getTaskManager().cancelTasks(socialPlayer);
            AuthCache.clearPendingVerification(player.getName());
        }
    }

    @EventHandler
    public void onPlayerRegister(PlayerRegisterEvent event) {
        if (event.isForceLogin()) return;

        ProxiedPlayer player = event.getPlayer();

        SocialUserData data = userCache.get(player.getUniqueId());
        if (data == null) return;

        SocialPlayer socialPlayer = plugin.getSocialPlatform().getPlayer(player.getName());
        if (socialPlayer == null) return;

        if (data.discordUser != null) {
            discord.checkPlayer(data.discordUser.getDiscordId(), socialPlayer, data.discordUser.isTwoFa(), data.discordUser.isAlert());
            if (!data.discordUser.isTwoFa()) {
                AuthCache.setPendingVerification(player.getName());
                event.setMoveToBackendServer(false);
                AuthCache.logout(player.getName());
                SessionCache.removePlayer(player.getName());
            } else {
                discord.startTasks(socialPlayer);
            }
        } else if (data.telegramUser != null) {
            telegram.checkPlayer(data.telegramUser.getTelegramId(), socialPlayer, data.telegramUser.isTwoFa(), data.telegramUser.isAlert());
            if (!data.telegramUser.isTwoFa()) {
                AuthCache.setPendingVerification(player.getName());
                event.setMoveToBackendServer(false);
                AuthCache.logout(player.getName());
                SessionCache.removePlayer(player.getName());
            } else {
                telegram.startTasks(socialPlayer);
            }
        } else if (data.maxUser != null) {
            max.checkPlayer(data.maxUser.getMaxId(), socialPlayer, data.maxUser.isTwoFa(), data.maxUser.isAlert());
            if (!data.maxUser.isTwoFa()) {
                AuthCache.setPendingVerification(player.getName());
                event.setMoveToBackendServer(false);
                AuthCache.logout(player.getName());
                SessionCache.removePlayer(player.getName());
            } else {
                max.startTasks(socialPlayer);
            }
        }
    }

    @EventHandler
    public void onPlayerAuth(PlayerAuthEvent event) {
        if (event.isForceLogin()) return;

        ProxiedPlayer player = event.getPlayer();

        SocialUserData data = userCache.get(player.getUniqueId());
        if (data == null) return;

        SocialPlayer socialPlayer = plugin.getSocialPlatform().getPlayer(player.getName());
        if (socialPlayer == null) return;

        if (data.discordUser != null) {
            discord.checkPlayer(data.discordUser.getDiscordId(), socialPlayer, data.discordUser.isTwoFa(), data.discordUser.isAlert());
            if (data.discordUser.isTwoFa()) {
                AuthCache.setPendingVerification(player.getName());
                event.setMoveToBackendServer(false);
                AuthCache.logout(player.getName());
                SessionCache.removePlayer(player.getName());
                discord.startTasks(socialPlayer);
            }
        } else if (data.telegramUser != null) {
            telegram.checkPlayer(data.telegramUser.getTelegramId(), socialPlayer, data.telegramUser.isTwoFa(), data.telegramUser.isAlert());
            if (data.telegramUser.isTwoFa()) {
                AuthCache.setPendingVerification(player.getName());
                event.setMoveToBackendServer(false);
                AuthCache.logout(player.getName());
                SessionCache.removePlayer(player.getName());
                telegram.startTasks(socialPlayer);
            }
        } else if (data.maxUser != null) {
            max.checkPlayer(data.maxUser.getMaxId(), socialPlayer, data.maxUser.isTwoFa(), data.maxUser.isAlert());
            if (data.maxUser.isTwoFa()) {
                AuthCache.setPendingVerification(player.getName());
                event.setMoveToBackendServer(false);
                AuthCache.logout(player.getName());
                SessionCache.removePlayer(player.getName());
                max.startTasks(socialPlayer);
            }
        }
    }

    public static class SocialUserData {
        public DiscordUser discordUser;
        public TelegramUser telegramUser;
        public MaxUser maxUser;
    }
}
