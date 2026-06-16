package ru.matveylegenda.socialaddon.common.cache;

import lombok.experimental.UtilityClass;
import ru.matveylegenda.socialaddon.common.config.MainConfig;
import ru.matveylegenda.tiauth.thirdparty.com.github.benmanes.caffeine.cache.Cache;
import ru.matveylegenda.tiauth.thirdparty.com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class CodeCache {
    private static final Random random = new Random();
    private final Cache<String, String> codes = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public String addCode(String playerName) {
        String code = generateCode(MainConfig.IMP.code.chars, MainConfig.IMP.code.length);
        codes.put(code, playerName);

        return code;
    }

    public String getPlayerName(String code) {
        return codes.getIfPresent(code);
    }

    public void removeCode(String code) {
        codes.invalidate(code);
    }

    private String generateCode(String chars, int length) {
        StringBuilder codeBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            codeBuilder.append(chars.charAt(index));
        }

        return codeBuilder.toString();
    }
}
