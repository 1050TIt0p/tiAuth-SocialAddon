package ru.matveylegenda.socialaddon.common.social.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import okhttp3.OkHttpClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.matveylegenda.socialaddon.common.api.SocialPlatform;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.listener.telegram.*;
import ru.matveylegenda.socialaddon.common.manager.TaskManager;
import ru.matveylegenda.socialaddon.common.social.Social;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Telegram extends Social implements LongPollingSingleThreadUpdateConsumer {
    @Getter
    private static TelegramClient telegramClient;
    private final Database database;
    private final SocialPlatform socialPlatform;

    private TelegramCodeListener codeListener;
    private TelegramAllowJoinListener allowJoinListener;
    private TelegramUnlinkListener unlinkListener;
    private TelegramStartListener startListener;
    private TelegramAccountsListener accountsListener;

    public Telegram(TaskManager taskManager, Database database, SocialPlatform socialPlatform) {
        super(taskManager);
        this.database = database;
        this.socialPlatform = socialPlatform;
    }

    @Override
    public void enableBot() throws Exception {
        if (!isEnabled()) {
            return;
        }

        TelegramBotsLongPollingApplication botApplication;

        if (TelegramConfig.IMP.proxy.enabled) {
            String proxyIp = TelegramConfig.IMP.proxy.ip;
            int proxyPort = TelegramConfig.IMP.proxy.port;
            String proxyUser = TelegramConfig.IMP.proxy.user;
            String proxyPassword = TelegramConfig.IMP.proxy.password;

            if (!proxyUser.isEmpty()) {
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        if (getRequestingHost().equalsIgnoreCase(proxyIp) && getRequestingPort() == proxyPort) {
                            return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                        }
                        return null;
                    }
                });
            }

            OkHttpClient customClient = new TelegramOkHttpClientFactory.SocksProxyOkHttpClientCreator(
                    () -> new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyIp, proxyPort))
            ).get();

            botApplication = new TelegramBotsLongPollingApplication(ObjectMapper::new, () -> customClient);
            telegramClient = new OkHttpTelegramClient(customClient, TelegramConfig.IMP.token);
        } else {
            botApplication = new TelegramBotsLongPollingApplication();
            telegramClient = new OkHttpTelegramClient(TelegramConfig.IMP.token);
        }

        botApplication.registerBot(TelegramConfig.IMP.token, this);

        this.codeListener = new TelegramCodeListener(database);
        this.allowJoinListener = new TelegramAllowJoinListener(socialPlatform, taskManager);
        this.unlinkListener = new TelegramUnlinkListener(database);
        this.startListener = new TelegramStartListener();
        this.accountsListener = new TelegramAccountsListener(database, socialPlatform);
    }

    @Override
    public CompletableFuture<Void> checkPlayer(String socialId, SocialPlayer player, boolean twoFaEnabled, boolean alertEnabled) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (twoFaEnabled) {
            InlineKeyboardButton allowButton = InlineKeyboardButton.builder()
                    .callbackData("allow-" + player.getName())
                    .text(TelegramConfig.IMP.messages.twoFaAlert.buttons.allow.text)
                    .build();

            InlineKeyboardButton denyButton = InlineKeyboardButton.builder()
                    .callbackData("deny-" + player.getName())
                    .text(TelegramConfig.IMP.messages.twoFaAlert.buttons.deny.text)
                    .build();

            SendMessage message = SendMessage.builder()
                    .chatId(socialId)
                    .text(
                            TelegramConfig.IMP.messages.twoFaAlert.message
                                    .replace("{player}", player.getName())
                                    .replace("{ip}", player.getIp())
                    )
                    .replyMarkup(InlineKeyboardMarkup
                            .builder()
                            .keyboardRow(
                                    new InlineKeyboardRow(allowButton, denyButton)
                            )
                            .build())
                    .build();

            try {
                getTelegramClient().execute(message);
                future.complete(null);
            } catch (TelegramApiException e) {
                future.completeExceptionally(e);
            }
        } else if (alertEnabled) {
            sendMessage(socialId, TelegramConfig.IMP.messages.alert
                    .replace("{player}", player.getName())
                    .replace("{ip}", player.getIp())
            );
            future.complete(null);
        } else {
            future.complete(null);
        }

        return future;
    }

    @Override
    public boolean isEnabled() {
        return TelegramConfig.IMP.enabled;
    }

    @Override
    public void consume(Update update) {
        codeListener.consume(update);
        allowJoinListener.consume(update);
        unlinkListener.consume(update);
        startListener.consume(update);
        accountsListener.consume(update);
    }

    public static void sendMessage(String chatId, String content) {
        SendMessage message = SendMessage
                .builder()
                .chatId(chatId)
                .text(content)
                .build();

        try {
            getTelegramClient().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String content, ReplyKeyboardMarkup keyboard, String chatId) {
        SendMessage message = SendMessage
                .builder()
                .chatId(chatId)
                .text(content)
                .replyMarkup(keyboard)
                .build();

        try {
            getTelegramClient().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String content, InlineKeyboardMarkup markup, String chatId) {
        SendMessage message = SendMessage
                .builder()
                .chatId(chatId)
                .text(content)
                .replyMarkup(markup)
                .build();

        try {
            getTelegramClient().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void editMessage(String chatId, String messageId, String content) {
        EditMessageText message = EditMessageText
                .builder()
                .chatId(chatId)
                .messageId(Integer.valueOf(messageId))
                .text(content)
                .replyMarkup(new InlineKeyboardMarkup(List.of()))
                .build();

        try {
            getTelegramClient().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void editMessage(String content, InlineKeyboardMarkup markup, String chatId, Integer messageId) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(content)
                .replyMarkup(markup)
                .build();

        try {
            getTelegramClient().execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
