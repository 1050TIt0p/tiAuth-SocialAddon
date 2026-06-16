package ru.matveylegenda.socialaddon.common.listener.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.social.platform.Telegram;

public class TelegramUnlinkListener {
    private final Database database;

    public TelegramUnlinkListener(Database database) {
        this.database = database;
    }

    public void consume(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();
        String[] messageSplit = message.getText().split(" ");
        String chatId = message.getChatId().toString();

        if (!messageSplit[0].equalsIgnoreCase("/unlink")) {
            return;
        }

        String playerName = messageSplit[1];
        database.getTelegramUserRepository().getUserByPlayerName(playerName).thenAccept(user -> {
            if (user == null || !chatId.equalsIgnoreCase(user.getTelegramId())) {
                Telegram.sendMessage(chatId, TelegramConfig.IMP.messages.accountNotFound);
                return;
            }

            database.getTelegramUserRepository().removeUser(playerName).thenAccept(success -> {
                if (!success) {
                    Telegram.sendMessage(chatId, TelegramConfig.IMP.messages.queryError);
                    return;
                }

                Telegram.sendMessage(chatId,
                        TelegramConfig.IMP.messages.accountUnlinked.replace("{player}", playerName)
                );
            });
        });
    }
}
