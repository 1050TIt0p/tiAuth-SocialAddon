package ru.matveylegenda.socialaddon.common.database.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;

@Data
@NoArgsConstructor
public class TelegramUser {
    private String username;
    private String telegramId;
    private boolean twoFa;
    private boolean alert;

    public TelegramUser(String username, String telegramId) {
        this.username = username;
        this.telegramId = telegramId;
        this.twoFa = TelegramConfig.IMP.defaultEnableTwoFa;
        this.alert = TelegramConfig.IMP.defaultEnableAlert;
    }
}
