package ru.matveylegenda.socialaddon.common.api;

import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public interface SocialPlayer extends ForwardingAudience.Single {
    String getName();
    UUID getUniqueId();
    String getIp();
    boolean isOnline();
    void disconnect(Component component);
    void connect();
}