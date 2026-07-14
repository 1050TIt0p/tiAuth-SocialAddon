package ru.matveylegenda.socialaddon.common.database.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.matveylegenda.socialaddon.common.config.social.VkConfig;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.field.DatabaseField;
import ru.matveylegenda.tiauth.thirdparty.com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "vk_users")
@Data
@NoArgsConstructor
public class VkUser {
    @DatabaseField(id = true, canBeNull = false)
    private String username;

    @DatabaseField(canBeNull = false)
    private String vkId;

    @DatabaseField(canBeNull = false)
    private boolean twoFa;

    @DatabaseField(canBeNull = false)
    private boolean alert;

    public VkUser(String username, String vkId) {
        this.username = username;
        this.vkId = vkId;
        this.twoFa = VkConfig.IMP.defaultEnableTwoFa;
        this.alert = VkConfig.IMP.defaultEnableAlert;
    }
}
