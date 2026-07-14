package ru.matveylegenda.socialaddon.common.social.platform;

import lombok.Getter;
import ru.matveylegenda.socialaddon.common.api.SocialPlatform;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.config.social.MaxConfig;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.listener.max.MaxCodeListener;
import ru.matveylegenda.socialaddon.common.listener.max.MaxStartListener;
import ru.matveylegenda.socialaddon.common.manager.TaskManager;
import ru.matveylegenda.socialaddon.common.social.Social;
import ru.max.botapi.client.MaxBotAPI;
import ru.max.botapi.longpolling.MaxLongPollingConsumer;
import ru.max.botapi.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Max extends Social {
    @Getter
    private static MaxBotAPI maxApi;
    private final Database database;
    private final SocialPlatform socialPlatform;

    private MaxStartListener maxStartListener;
    private MaxCodeListener maxCodeListener;

    public Max(TaskManager taskManager, Database database, SocialPlatform socialPlatform) {
        super(taskManager);
        this.database = database;
        this.socialPlatform = socialPlatform;
    }

    @Override
    public void enableBot() throws Exception {
        if (!isEnabled()) {
            return;
        }

        this.maxStartListener = new MaxStartListener();
        this.maxCodeListener = new MaxCodeListener(database);

        maxApi = MaxBotAPI.create(MaxConfig.IMP.token);

        MaxLongPollingConsumer consumer = MaxLongPollingConsumer.builder()
                .api(maxApi)
                .handler(update -> {
                    // Тут листенеры будут
                    maxStartListener.consume(update);
                    maxCodeListener.consume(update);
                })
                .build();

        consumer.start();
    }

    @Override
    public CompletableFuture<Void> checkPlayer(String socialId, SocialPlayer player, boolean twoFaEnabled, boolean alertEnabled) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (twoFaEnabled) {
            CallbackButton allowButton = new CallbackButton(
                    TelegramConfig.IMP.messages.twoFaAlert.buttons.allow.text,
                    "allow-" + player.getName(),
                    ButtonIntent.POSITIVE
            );

            CallbackButton denyButton = new CallbackButton(
                    TelegramConfig.IMP.messages.twoFaAlert.buttons.deny.text,
                    "deny-" + player.getName(),
                    ButtonIntent.NEGATIVE
            );

            sendMessage(
                    MaxConfig.IMP.messages.twoFaAlert.message
                            .replace("{player}", player.getName())
                            .replace("{ip}", player.getIp()),
                    new InlineKeyboardAttachmentRequest(
                            new InlineKeyboardAttachment.KeyboardPayload(List.of(
                                    List.of(allowButton, denyButton)
                            ))
                    ),
                    socialId
            );
        } else if (alertEnabled) {
            sendMessage(
                    MaxConfig.IMP.messages.alert
                            .replace("{player}", player.getName())
                            .replace("{ip}", player.getIp()),
                    socialId
            );

            future.complete(null);
        } else {
            future.complete(null);
        }

        return future;
    }

    @Override
    public boolean isEnabled() {
        return MaxConfig.IMP.enabled;
    }

    public static void sendMessage(String content, String chatId) {
        maxApi.sendMessage(new NewMessageBody(
                        content,
                        null,
                        null,
                        null,
                        null
                ))
                .chatId(Long.parseLong(chatId))
                .execute();
    }

    public static void sendMessage(String content, InlineKeyboardAttachmentRequest keyboard, String chatId) {
        maxApi.sendMessage(new NewMessageBody(
                        content,
                        List.of(keyboard),
                        null,
                        null,
                        null
                ))
                .chatId(Long.parseLong(chatId))
                .execute();
    }
}
