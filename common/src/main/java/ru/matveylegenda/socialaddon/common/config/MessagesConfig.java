package ru.matveylegenda.socialaddon.common.config;

import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.SerializerConfig;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.NewLine;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.Transient;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.language.object.YamlSerializable;

import java.nio.file.Paths;

public class MessagesConfig extends YamlSerializable {

    @Transient
    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .setCommentValueIndent(1)
            .build();

    @Transient
    public static final MessagesConfig IMP = new MessagesConfig();

    public MessagesConfig() {
        super(Paths.get("plugins/tiAuth-SocialAddon/messages.yml"), CONFIG);
        this.discord = new Discord();
        this.telegram = new Telegram();
        this.vk = new Vk();
        this.max = new Max();
    }

    public String queryError = "&#8833EC2ꜰᴀ &8» &fПроизошла ошибка при запросе к базе данных. Сообщите администрации!";
    public String onlyForPlayer = "&#8833EC2ꜰᴀ &8» &fКоманда доступна только игрокам";
    public String usage = "&#8833EC2ꜰᴀ &8» &fИспользование: &#8833EC/link <discord|telegram>";
    public String alreadyLinked = "&#8833EC2ꜰᴀ &8» &fВаш аккаунт уже привязан к соц. сети";
    public String allowJoin = "&#8833EC2ꜰᴀ &8» &fВход успешно выполнен";
    public String denyJoin = "&#8833EC2ꜰᴀ &8» &fВход отклонен";
    public String timeout = "&#8833EC2ꜰᴀ &8» &fВы не успели подтвердить вход";
    public String reminder = "&#8833EC2ꜰᴀ &8» &fПодтвердите вход через соц. сеть";
    public String joinToLinkedOnlyServer = "&#8833EC2ꜰᴀ &8» &fДля входа на данный сервер вам нужно привязать аккаунт к соц. сети через команду &#8833EC2/link";
    public String noLinkConfirmation = "&#8833EC2ꜰᴀ &8» &fНет ожидающих привязок";

    public Discord discord;

    @NewLine
    public static class Discord {
        public String code = "&#8833EC2ꜰᴀ &8» &fВаш код: {code}\nОтправьте его боту example#0000";
        public String accountLinked = "&#8833EC2ꜰᴀ &8» &fАккаунт успешно привязан к &#8833ECDiscord";
    }

    public Telegram telegram;

    @NewLine
    public static class Telegram {
        public String code = "&#8833EC2ꜰᴀ &8» &fВаш код: {code}\nОтправьте его боту @example";
        public String accountLinked = "&#8833EC2ꜰᴀ &8» &fАккаунт успешно привязан к &#8833ECTelegram";
    }

    public Vk vk;

    @NewLine
    public static class Vk {
        public String code = "&#8833EC2ꜰᴀ &8» &fВаш код: {code}\nОтправьте его в группу @example";
        public String accountLinked = "&#8833EC2ꜰᴀ &8» &fАккаунт успешно привязан к &#8833ECVK";
    }

    public Max max;

    @NewLine
    public static class Max {
        public String code = "&#8833EC2ꜰᴀ &8» &fВаш код: {code}\nОтправьте его боту @example";
        public String accountLinked = "&#8833EC2ꜰᴀ &8» &fАккаунт успешно привязан к &#8833ECMax";
    }
}
