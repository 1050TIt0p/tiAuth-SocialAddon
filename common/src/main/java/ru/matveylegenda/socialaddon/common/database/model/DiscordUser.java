package ru.matveylegenda.socialaddon.common.database.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.field.DatabaseField;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "discord_users")
@Data
@NoArgsConstructor
public class DiscordUser {
    @DatabaseField(id = true, canBeNull = false)
    private String username;

    @DatabaseField(canBeNull = false)
    private String discordId;

    @DatabaseField(canBeNull = false)
    private boolean twoFa;

    @DatabaseField(canBeNull = false)
    private boolean alert;

    public DiscordUser(String username, String discordId) {
        this.username = username;
        this.discordId = discordId;
        this.twoFa = DiscordConfig.IMP.defaultEnableTwoFa;
        this.alert = DiscordConfig.IMP.defaultEnableAlert;
    }
}
