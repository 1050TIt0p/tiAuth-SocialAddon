package ru.matveylegenda.socialaddon.velocity.adapter;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import ru.matveylegenda.socialaddon.common.api.SocialPlatform;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.velocity.api.VelocitySocialPlayer;

import java.util.Optional;

public class VelocityPlatformAdapter implements SocialPlatform {
    private final ProxyServer proxy;

    public VelocityPlatformAdapter(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Override
    public SocialPlayer getPlayer(String playerName) {
        Optional<Player> player = proxy.getPlayer(playerName);

        return player.map(p -> new VelocitySocialPlayer(p, proxy)).orElse(null);
    }
}