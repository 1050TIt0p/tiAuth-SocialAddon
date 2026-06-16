package ru.matveylegenda.socialaddon.bungee.adapter;

import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import ru.matveylegenda.socialaddon.bungee.api.BungeeSocialPlayer;
import ru.matveylegenda.socialaddon.common.api.SocialPlatform;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;

public class BungeePlatformAdapter implements SocialPlatform {
    private final BungeeAudiences audiences;

    public BungeePlatformAdapter(BungeeAudiences audiences) {
        this.audiences = audiences;
    }

    @Override
    public SocialPlayer getPlayer(String playerName) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);

        if (player == null) {
            return null;
        }

        return new BungeeSocialPlayer(player, audiences);
    }
}