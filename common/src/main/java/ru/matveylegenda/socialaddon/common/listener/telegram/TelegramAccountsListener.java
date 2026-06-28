package ru.matveylegenda.socialaddon.common.listener.telegram;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.matveylegenda.socialaddon.common.api.SocialPlatform;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.social.platform.Telegram;
import ru.matveylegenda.socialaddon.common.utils.Utils;
import ru.matveylegenda.tiauth.cache.AuthCache;
import ru.matveylegenda.tiauth.config.MainConfig;
import ru.matveylegenda.tiauth.hash.Hash;
import ru.matveylegenda.tiauth.hash.HashFactory;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.matveylegenda.tiauth.util.Utils.COLORIZER;

public class TelegramAccountsListener {
    private final Database database;
    private final SocialPlatform socialPlatform;

    public TelegramAccountsListener(Database database, SocialPlatform socialPlatform) {
        this.database = database;
        this.socialPlatform = socialPlatform;
    }

    public void consume(Update update) {
        if (update.hasMessage()) {

            Message message = update.getMessage();
            String chatId = message.getChatId().toString();

            if (!message.getText().equalsIgnoreCase(TelegramConfig.IMP.messages.keyboard.accounts)) {
                return;
            }

            database.getTelegramUserRepository().getAccountsById(chatId).thenAccept(accounts -> {
                if (accounts == null || accounts.isEmpty()) {
                    Telegram.sendMessage(chatId, TelegramConfig.IMP.messages.noLinked);
                    return;
                }

                var markupBuilder = InlineKeyboardMarkup.builder();

                for (String account : accounts) {
                    InlineKeyboardButton button = InlineKeyboardButton
                            .builder()
                            .text(account)
                            .callbackData("account-" + account)
                            .build();

                    InlineKeyboardRow row = new InlineKeyboardRow();
                    row.add(button);

                    markupBuilder.keyboardRow(row);
                }

                Telegram.sendMessage(TelegramConfig.IMP.messages.selectAccount, markupBuilder.build(), chatId);
            });
        }

        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callData = callbackQuery.getData();
            String chatId = callbackQuery.getMessage().getChatId().toString();
            Integer messageId = callbackQuery.getMessage().getMessageId();

            if (callData.startsWith("account-")) {
                String playerName = callData.replace("account-", "");

                InlineKeyboardButton btnNotify = InlineKeyboardButton
                        .builder()
                        .text(TelegramConfig.IMP.messages.keyboard.alert)
                        .callbackData("toggle_notify-" + playerName)
                        .build();

                InlineKeyboardButton btn2FA = InlineKeyboardButton
                        .builder()
                        .text(TelegramConfig.IMP.messages.keyboard.twoFa)
                        .callbackData("toggle_2fa-" + playerName)
                        .build();

                InlineKeyboardRow row1 = new InlineKeyboardRow();
                row1.add(btnNotify);
                row1.add(btn2FA);

                InlineKeyboardButton btnPass = InlineKeyboardButton
                        .builder()
                        .text(TelegramConfig.IMP.messages.keyboard.changePassword)
                        .callbackData("change_pass-" + playerName)
                        .build();

                InlineKeyboardRow row2 = new InlineKeyboardRow();
                row2.add(btnPass);

                InlineKeyboardButton btnKick = InlineKeyboardButton
                        .builder()
                        .text(TelegramConfig.IMP.messages.keyboard.kick)
                        .callbackData("kick-" + playerName)
                        .build();

                InlineKeyboardRow row3 = new InlineKeyboardRow();
                row3.add(btnKick);

                InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                        .keyboardRow(row1)
                        .keyboardRow(row2)
                        .keyboardRow(row3)
                        .build();

                Telegram.editMessage(
                        TelegramConfig.IMP.messages.selectedAccount.replace("{player}", playerName),
                        markup,
                        chatId,
                        messageId
                );
            }

            if (callData.startsWith("toggle_notify-")) {
                String playerName = callData.replace("toggle_notify-", "");

                database.getTelegramUserRepository().getUserByPlayerName(playerName).thenAccept(user -> {
                    if (user == null || !chatId.equalsIgnoreCase(user.getTelegramId())) {
                        Telegram.editMessage(chatId, String.valueOf(messageId), TelegramConfig.IMP.messages.accountNotFound);
                        return;
                    }

                    database.getTelegramUserRepository().setAlertEnabled(playerName, !user.isAlert()).thenAccept(success -> {
                        if (!success) {
                            Telegram.editMessage(chatId, String.valueOf(messageId), TelegramConfig.IMP.messages.queryError);
                            return;
                        }

                        if (user.isAlert()) {
                            Telegram.editMessage(chatId, String.valueOf(messageId),
                                    TelegramConfig.IMP.messages.alertDisabled.replace("{player}", playerName)
                            );
                        } else {
                            Telegram.editMessage(chatId, String.valueOf(messageId),
                                    TelegramConfig.IMP.messages.alertEnabled.replace("{player}", playerName)
                            );
                        }
                    });
                });
            }

            if (callData.startsWith("toggle_2fa-")) {
                String playerName = callData.replace("toggle_2fa-", "");

                database.getTelegramUserRepository().getUserByPlayerName(playerName).thenAccept(user -> {
                    if (user == null || !chatId.equalsIgnoreCase(user.getTelegramId())) {
                        Telegram.editMessage(chatId, String.valueOf(messageId), TelegramConfig.IMP.messages.accountNotFound);
                        return;
                    }

                    database.getTelegramUserRepository().setTwoFaEnabled(playerName, !user.isTwoFa()).thenAccept(success -> {
                        if (!success) {
                            Telegram.editMessage(chatId, String.valueOf(messageId), TelegramConfig.IMP.messages.queryError);
                            return;
                        }

                        if (user.isTwoFa()) {
                            Telegram.editMessage(chatId, String.valueOf(messageId),
                                    TelegramConfig.IMP.messages.twoFaDisabled.replace("{player}", playerName)
                            );
                        } else {
                            Telegram.editMessage(chatId, String.valueOf(messageId),
                                    TelegramConfig.IMP.messages.twoFaEnabled.replace("{player}", playerName)
                            );
                        }
                    });
                });
            }

            if (callData.startsWith("change_pass-")) {
                String playerName = callData.replace("change_pass-", "");

                database.getTelegramUserRepository().getUserByPlayerName(playerName).thenAccept(user -> {
                    if (user == null || !chatId.equalsIgnoreCase(user.getTelegramId())) {
                        Telegram.editMessage(chatId, String.valueOf(messageId), TelegramConfig.IMP.messages.accountNotFound);
                        return;
                    }

                    String newPass = generatePassword();
                    Hash hash = HashFactory.create(MainConfig.IMP.auth.hashAlgorithm);
                    database.getAuthDatabase().getAuthUserRepository().updatePassword(playerName, hash.hashPassword(newPass)).thenRun(() -> {
                        Telegram.editMessage(chatId, String.valueOf(messageId), TelegramConfig.IMP.messages.newPassword.replace("{password}", newPass));
                    }).exceptionally(e -> {
                        Telegram.editMessage(chatId, String.valueOf(messageId), TelegramConfig.IMP.messages.queryError);
                        return null;
                    });
                });
            }

            if (callData.startsWith("kick-")) {
                String playerName = callData.replace("kick-", "");

                database.getTelegramUserRepository().getUserByPlayerName(playerName).thenAccept(user -> {
                    if (user == null || !chatId.equalsIgnoreCase(user.getTelegramId())) {
                        Telegram.editMessage(chatId, String.valueOf(messageId), TelegramConfig.IMP.messages.accountNotFound);
                        return;
                    }

                    SocialPlayer player = socialPlatform.getPlayer(playerName);

                    if (player == null) {
                        Telegram.editMessage(chatId, String.valueOf(messageId), TelegramConfig.IMP.messages.playerNotFound);
                        return;
                    }

                    player.disconnect(Utils.LEGACY.deserialize(
                            COLORIZER.colorize(MessagesConfig.IMP.denyJoin)
                    ));
                    AuthCache.logout(playerName);

                    Telegram.editMessage(chatId, String.valueOf(messageId), TelegramConfig.IMP.messages.playerKicked);
                });
            }
        }
    }

    private String generatePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String allChars = upper + lower + digits;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));

        for (int i = 0; i < 13; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        List<Character> chars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars, random);

        StringBuilder finalPassword = new StringBuilder();
        for (Character c : chars) {
            finalPassword.append(c);
        }

        return finalPassword.toString();
    }
}
