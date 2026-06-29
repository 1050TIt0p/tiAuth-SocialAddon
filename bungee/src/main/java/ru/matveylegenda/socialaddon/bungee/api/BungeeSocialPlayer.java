package ru.matveylegenda.socialaddon.bungee.api;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.tiauth.config.MainConfig;
import ru.matveylegenda.tiauth.util.Utils;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

public class BungeeSocialPlayer implements SocialPlayer {

    private final ProxiedPlayer handle;
    private final Audience audience;

    public BungeeSocialPlayer(ProxiedPlayer handle, BungeeAudiences audiences) {
        this.handle = handle;
        this.audience = audiences.player(handle);
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public UUID getUniqueId() {
        return handle.getUniqueId();
    }

    @Override
    public String getIp() {
        return ((InetSocketAddress) handle.getSocketAddress()).getAddress().getHostAddress();
    }

    @Override
    public boolean isOnline() {
        return handle.isConnected();
    }

    @Override
    public void disconnect(Component component) {
        String message = LegacyComponentSerializer.builder()
                .character(Utils.COLOR_CHAR)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build()
                .serialize(component);

        handle.disconnect(TextComponent.fromLegacy(message));
    }

    @Override
    public void connect() {
        getBackend().ifPresent(handle::connect);
    }

    private Optional<ServerInfo> getBackend() {
        return getForcedBackend()
                .or(this::getDefaultBackend);
    }

    private Optional<ServerInfo> getForcedBackend() {
        return Optional.ofNullable(handle.getPendingConnection().getVirtualHost())
                .map(addr -> MainConfig.IMP.servers.forcedHosts.get(addr.getHostString().toLowerCase()))
                .flatMap(ProxyServer.getInstance()::getServerInfo);
    }

    private Optional<ServerInfo> getDefaultBackend() {
        return Optional.ofNullable(ProxyServer.getInstance().getServerInfo(MainConfig.IMP.servers.backend));
    }

    @Override
    public @NotNull Audience audience() {
        return audience;
    }
}