package ru.matveylegenda.socialaddon.common.listener.telegram;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.matveylegenda.socialaddon.common.api.SocialPlatform;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.socialaddon.common.manager.TaskManager;
import ru.matveylegenda.socialaddon.common.social.platform.Telegram;
import ru.matveylegenda.socialaddon.common.utils.Utils;
import ru.matveylegenda.tiauth.cache.AuthCache;
import ru.matveylegenda.tiauth.cache.SessionCache;
import ru.matveylegenda.tiauth.config.MainConfig;

import static ru.matveylegenda.tiauth.util.Utils.COLORIZER;

public class TelegramAllowJoinListener {
    private final SocialPlatform socialPlatform;
    private final TaskManager taskManager;

    public TelegramAllowJoinListener(SocialPlatform socialPlatform, TaskManager taskManager) {
        this.socialPlatform = socialPlatform;
        this.taskManager = taskManager;
    }

    public void consume(Update update) {
        if (!update.hasCallbackQuery()) {
            return;
        }

        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callData = callbackQuery.getData();
        String chatId = callbackQuery.getMessage().getChatId().toString();
        String messageId = callbackQuery.getMessage().getMessageId().toString();

        if (callData.startsWith("allow-")) {
            String playerName = callData.replace("allow-", "");
            SocialPlayer player = socialPlatform.getPlayer(playerName);

            if (player == null) {
                Telegram.editMessage(
                        chatId,
                        messageId,
                        TelegramConfig.IMP.messages.playerNotFound
                );
                return;
            }

            AuthCache.setAuthenticated(playerName);
            SessionCache.addPlayer(playerName, player.getIp());
            taskManager.cancelTasks(player);
            player.connect(MainConfig.IMP.servers.backend);
            player.sendMessage(Utils.LEGACY.deserialize(
                    COLORIZER.colorize(MessagesConfig.IMP.allowJoin)
            ));
            Telegram.editMessage(chatId, messageId, TelegramConfig.IMP.messages.allowJoin);
        } else if (callData.startsWith("deny-")) {
            String playerName = callData.replace("deny-", "");
            SocialPlayer player = socialPlatform.getPlayer(playerName);

            if (player == null) {
                Telegram.editMessage(
                        chatId,
                        messageId,
                        TelegramConfig.IMP.messages.playerNotFound
                );
                return;
            }

            taskManager.cancelTasks(player);
            player.disconnect(Utils.LEGACY.deserialize(
                    COLORIZER.colorize(MessagesConfig.IMP.denyJoin)
            ));
            Telegram.editMessage(
                    chatId,
                    messageId,
                    TelegramConfig.IMP.messages.denyJoin
            );
        }
    }
}
