package ru.matveylegenda.socialaddon.common.listener.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.socialaddon.common.social.platform.Telegram;

import java.util.ArrayList;
import java.util.List;

public class TelegramStartListener {

    public void consume(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();
        String chatId = message.getChatId().toString();

        if (!message.getText().equals("/start")) {
            return;
        }

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(TelegramConfig.IMP.messages.keyboard.accounts));
        keyboard.add(row);

        ReplyKeyboardMarkup replyKeyboard = ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .build();

        Telegram.sendMessage(TelegramConfig.IMP.messages.start, replyKeyboard, chatId);
    }
}
