package ru.matveylegenda.socialaddon.common.social.platform;

import api.longpoll.bots.LongPollBot;
import api.longpoll.bots.exceptions.VkApiException;
import api.longpoll.bots.model.events.messages.MessageEvent;
import api.longpoll.bots.model.events.messages.MessageNew;
import api.longpoll.bots.model.objects.additional.Keyboard;
import api.longpoll.bots.model.objects.additional.buttons.Button;
import api.longpoll.bots.model.objects.additional.buttons.CallbackButton;
import com.google.gson.JsonObject;
import lombok.Getter;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.config.social.VkConfig;
import ru.matveylegenda.socialaddon.common.manager.TaskManager;
import ru.matveylegenda.socialaddon.common.social.Social;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Vk extends Social {
    @Getter
    private final VkBot vkBot = new VkBot();

    public Vk(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void enableBot() throws Exception {
        if (!isEnabled()) {
            return;
        }

        vkBot.startPolling();
    }

    @Override
    public CompletableFuture<Void> checkPlayer(String socialId, SocialPlayer player, boolean twoFaEnabled, boolean alertEnabled) {
        return vkBot.checkPlayer(socialId, player, twoFaEnabled, alertEnabled);
    }

    @Override
    public boolean isEnabled() {
        return VkConfig.IMP.enabled;
    }

    public static class VkBot extends LongPollBot {

        public CompletableFuture<Void> checkPlayer(String socialId, SocialPlayer player, boolean twoFaEnabled, boolean alertEnabled) {
            CompletableFuture<Void> future = new CompletableFuture<>();

            if (twoFaEnabled) {
                JsonObject allowButtonPayload = new JsonObject();
                allowButtonPayload.addProperty("buttonId", "allow-" + player.getName());

                Button allowButton = new CallbackButton(
                        Button.Color.POSITIVE,
                        new CallbackButton.Action(
                                VkConfig.IMP.messages.twoFaAlert.buttons.allow.text,
                                allowButtonPayload
                        )
                );

                JsonObject denyButtonPayload = new JsonObject();
                denyButtonPayload.addProperty("buttonId", "deny-" + player.getName());

                Button denyButton = new CallbackButton(
                        Button.Color.NEGATIVE,
                        new CallbackButton.Action(
                                VkConfig.IMP.messages.twoFaAlert.buttons.deny.text,
                                denyButtonPayload
                        )
                );

                try {
                    vk.messages.send()
                            .setUserId(Integer.parseInt(socialId))
                            .setMessage(
                                    VkConfig.IMP.messages.twoFaAlert.message
                                            .replace("{player}", player.getName())
                                            .replace("{ip}", player.getIp())
                            )
                            .setKeyboard(
                                    new Keyboard(List.of(
                                            List.of(allowButton, denyButton)
                                    )).setInline(false)
                            )
                            .execute();
                } catch (VkApiException e) {
                    future.completeExceptionally(e);
                }
            } else if (alertEnabled) {
                try {
                    vk.messages.send()
                            .setUserId(Integer.parseInt(socialId))
                            .setMessage(
                                    VkConfig.IMP.messages.alert
                                            .replace("{player}", player.getName())
                                            .replace("{ip}", player.getIp())
                            )
                            .execute();
                } catch (VkApiException e) {
                    future.completeExceptionally(e);
                }

                future.complete(null);
            } else {
                future.complete(null);
            }

            return future;
        }

        @Override
        public String getAccessToken() {
            return VkConfig.IMP.token;
        }

        @Override
        public void onMessageNew(MessageNew messageNew) {

        }

        @Override
        public void onMessageEvent(MessageEvent messageEvent) {

        }
    }
}
