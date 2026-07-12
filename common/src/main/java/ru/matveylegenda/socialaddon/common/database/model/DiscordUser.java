package ru.matveylegenda.socialaddon.common.database.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;

@Data
@NoArgsConstructor
public class DiscordUser {
    private String username;
    private String discordId;
    private boolean twoFa;
    private boolean alert;

    public DiscordUser(String username, String discordId) {
        this.username = username;
        this.discordId = discordId;
        this.twoFa = DiscordConfig.IMP.defaultEnableTwoFa;
        this.alert = DiscordConfig.IMP.defaultEnableAlert;
    }
}
