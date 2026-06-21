package ru.matveylegenda.socialaddon.velocity.api;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;

import java.util.Optional;
import java.util.UUID;

public class VelocitySocialPlayer implements SocialPlayer {

    private final Player handle;
    private final ProxyServer proxy;

    public VelocitySocialPlayer(Player handle, ProxyServer proxy) {
        this.handle = handle;
        this.proxy = proxy;
    }

    @Override
    public String getName() {
        return handle.getUsername();
    }

    @Override
    public UUID getUniqueId() {
        return handle.getUniqueId();
    }

    @Override
    public String getIp() {
        return handle.getRemoteAddress().getAddress().getHostAddress();
    }

    @Override
    public boolean isOnline() {
        return handle.isActive();
    }

    @Override
    public String getVirtualHostString() {
        return handle.getVirtualHost()
                .map(addr -> addr.getHostString())
                .orElse(null);
    }

    @Override
    public void disconnect(Component component) {
        handle.disconnect(component);
    }

    @Override
    public void connect(String serverName) {
        Optional<RegisteredServer> server = proxy.getServer(serverName);
        if (server.isPresent()) {
            handle.createConnectionRequest(server.get()).connect();
        }
    }

    @Override
    public @NotNull Audience audience() {
        // В Velocity Player уже наследует Audience
        return handle;
    }
}