package ru.matveylegenda.socialaddon.common.listener.max;

import ru.matveylegenda.socialaddon.common.config.social.MaxConfig;
import ru.matveylegenda.socialaddon.common.social.platform.Max;
import ru.max.botapi.model.BotStartedUpdate;
import ru.max.botapi.model.MessageCreatedUpdate;
import ru.max.botapi.model.Update;

public class MaxStartListener {

    public void consume(Update updateRaw) {
        if (updateRaw instanceof BotStartedUpdate update) {
            Max.sendMessage(
                    MaxConfig.IMP.messages.start,
                    String.valueOf(update.chatId())
            );
            return;
        }

        if (updateRaw instanceof MessageCreatedUpdate update) {
            String text = update.message().body().text();

            if (text == null) {
                return;
            }

            if (!text.equals("/start")) {
                return;
            }

            Max.sendMessage(
                    MaxConfig.IMP.messages.start,
                    String.valueOf(update.message().recipient().chatId())
            );
        }
    }
}
