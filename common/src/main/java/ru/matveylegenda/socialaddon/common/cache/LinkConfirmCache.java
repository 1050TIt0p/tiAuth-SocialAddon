package ru.matveylegenda.socialaddon.common.cache;

import lombok.experimental.UtilityClass;
import ru.matveylegenda.tiauth.thirdparty.com.github.benmanes.caffeine.cache.Cache;
import ru.matveylegenda.tiauth.thirdparty.com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class LinkConfirmCache {
    private final Cache<String, LinkRequest> requests =
            Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build();


    public void add(String playerName, LinkRequest request) {
        requests.put(playerName, request);
    }

    public LinkRequest get(String playerName) {
        return requests.getIfPresent(playerName);
    }

    public void remove(String playerName) {
        requests.invalidate(playerName);
    }

    public record LinkRequest(String platform, String accountId) {}
}
