package ru.matveylegenda.socialaddon.common.database.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.field.DatabaseField;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "telegram_users")
@Data
@NoArgsConstructor
public class TelegramUser {
    @DatabaseField(id = true, canBeNull = false)
    private String username;

    @DatabaseField(canBeNull = false)
    private String telegramId;

    @DatabaseField(canBeNull = false)
    private boolean twoFa;

    @DatabaseField(canBeNull = false)
    private boolean alert;

    public TelegramUser(String username, String telegramId) {
        this.username = username;
        this.telegramId = telegramId;
        this.twoFa = TelegramConfig.IMP.defaultEnableTwoFa;
        this.alert = TelegramConfig.IMP.defaultEnableAlert;
    }
}
