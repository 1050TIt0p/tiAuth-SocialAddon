package ru.matveylegenda.socialaddon.common.listener.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.matveylegenda.socialaddon.common.cache.CodeCache;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.social.platform.Telegram;

public class TelegramCodeListener {
    private final Database database;

    public TelegramCodeListener(Database database) {
        this.database = database;
    }

    public void consume(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();
        String chatId = message.getChatId().toString();

        String playerName = CodeCache.getPlayerName(message.getText());
        if (playerName != null) {
            database.getTelegramUserRepository().getAccountCountById(chatId).thenAccept(accountsCount -> {
                if (accountsCount >= TelegramConfig.IMP.maxLinkAccounts) {
                    Telegram.sendMessage(chatId, TelegramConfig.IMP.messages.accountLimitReached);
                    return;
                }

                database.getTelegramUserRepository().addUser(playerName, chatId).thenAccept(success -> {
                    if (!success) {
                        Telegram.sendMessage(chatId, TelegramConfig.IMP.messages.queryError);
                        return;
                    }

                    CodeCache.removeCode(message.getText());
                    Telegram.sendMessage(
                            chatId,
                            TelegramConfig.IMP.messages.accountLinked.replace("{player}", playerName)
                    );
                });
            });
        }
    }
}
