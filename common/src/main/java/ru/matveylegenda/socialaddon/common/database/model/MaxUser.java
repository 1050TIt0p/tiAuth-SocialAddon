package ru.matveylegenda.socialaddon.common.database.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.matveylegenda.socialaddon.common.config.social.MaxConfig;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.field.DatabaseField;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "max_users")
@Data
@NoArgsConstructor
public class MaxUser {
    @DatabaseField(id = true, canBeNull = false)
    private String username;

    @DatabaseField(canBeNull = false)
    private String maxId;

    @DatabaseField(canBeNull = false)
    private boolean twoFa;

    @DatabaseField(canBeNull = false)
    private boolean alert;

    public MaxUser(String username, String maxId) {
        this.username = username;
        this.maxId = maxId;
        this.twoFa = MaxConfig.IMP.defaultEnableTwoFa;
        this.alert = MaxConfig.IMP.defaultEnableAlert;
    }
}
