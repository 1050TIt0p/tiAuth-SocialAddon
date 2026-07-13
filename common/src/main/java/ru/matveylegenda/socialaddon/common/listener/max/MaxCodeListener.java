package ru.matveylegenda.socialaddon.common.listener.max;

import ru.matveylegenda.socialaddon.common.cache.CodeCache;
import ru.matveylegenda.socialaddon.common.cache.LinkConfirmCache;
import ru.matveylegenda.socialaddon.common.config.social.MaxConfig;
import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.social.platform.Max;
import ru.max.botapi.model.MessageCreatedUpdate;
import ru.max.botapi.model.Update;

public class MaxCodeListener {
    private final Database database;

    public MaxCodeListener(Database database) {
        this.database = database;
    }

    public void consume(Update updateRaw) {
        if (updateRaw instanceof MessageCreatedUpdate update) {
            String text = update.message().body().text();
            String chatId = String.valueOf(update.message().recipient().chatId());

            if (text == null) {
                return;
            }

            String playerName = CodeCache.getPlayerName(text);
            if (playerName != null) {
                database.getMaxUserRepository().getAccountCountById(chatId).thenAccept(accountsCount -> {
                    if (accountsCount >= MaxConfig.IMP.maxLinkAccounts) {
                        Max.sendMessage(MaxConfig.IMP.messages.accountLimitReached, chatId);
                        return;
                    }

                    CodeCache.removeCode(text);
                    LinkConfirmCache.add(
                            playerName,
                            new LinkConfirmCache.LinkRequest("max", chatId)
                    );

                    Max.sendMessage(
                            MaxConfig.IMP.messages.codeAccept,
                            chatId
                    );
                });
            }
        }
    }
}
