package ru.matveylegenda.socialaddon.common.config.social;

import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.SerializerConfig;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.Comment;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.CommentValue;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.NewLine;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.Transient;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.language.object.YamlSerializable;

import java.nio.file.Paths;

public class DiscordConfig extends YamlSerializable {

    @Transient
    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .setCommentValueIndent(1)
            .build();

    @Transient
    public static final DiscordConfig IMP = new DiscordConfig();

    public DiscordConfig() {
        super(Paths.get("plugins/tiAuth-SocialAddon/socials/discord.yml"), CONFIG);
        this.messages = new Messages();
        this.messages.twoFaAlert = new Messages.TwoFaAlert();
        this.messages.twoFaAlert.buttons = new Messages.TwoFaAlert.Buttons();
        this.messages.twoFaAlert.buttons.allow = new Messages.TwoFaAlert.Buttons.Allow();
        this.messages.twoFaAlert.buttons.deny = new Messages.TwoFaAlert.Buttons.Deny();
        this.proxy = new Proxy();
    }

    public boolean enabled = false;

    public String token = "";

    public Proxy proxy;

    @Comment({
            @CommentValue("Настройка SOCKS5 прокси")
    })
    public static class Proxy {
        public boolean enabled = false;

        public String ip = "0.0.0.0";

        public int port = 1080;

        @Comment({
                @CommentValue("Оставить пустым если юзера и пароля нет")
        })
        public String user = "";

        public String password = "";
    }

    @Comment({
            @CommentValue("Максимальное количество привязанных аккаунтов к Discord")
    })
    public int maxLinkAccounts = 3;

    @Comment({
            @CommentValue("Включать ли сразу 2FA после привязки аккаунта к Discord")
    })
    public boolean defaultEnableTwoFa = false;

    @Comment({
            @CommentValue("Включать ли сразу отправку уведомлений о входе/выходе после привязки аккаунта к Discord")
    })
    public boolean defaultEnableAlert = true;

    public Messages messages;
    public static class Messages {
        public TwoFaAlert twoFaAlert;
        public static class TwoFaAlert {
            public String message = "Подтвердите вход на сервер\n\nАккаунт: {player}\nIP: {ip}";

            public Buttons buttons;
            public static class Buttons {
                public Allow allow;
                public static class Allow {
                    public String text = "Подтвердить";
                    public ButtonStyle style = ButtonStyle.SUCCESS;
                    public String emoji = "✅";
                }

                public Deny deny;
                public static class Deny {
                    public String text = "Отклонить";
                    public ButtonStyle style = ButtonStyle.DANGER;
                    public String emoji = "❌";
                }
            }
        }

        public String alert = "Обнаружен вход на сервер\n\nАккаунт: {player}\nIP: {ip}";

        @NewLine
        public String playerNotFound = "Игрок не найден";

        public String allowJoin = "Вход подтвержден";
        public String denyJoin = "Вход отклонен";

        public String accountLimitReached = "К вашему Discord привязано максимальное количество аккаунтов";

        public String codeAccept = "Код принят. Для завершения привязки напишите на сервере: /link accept";
        public String accountUnlinked = "Аккаунт {player} успешно отвязан от вашего Discord";

        public String accountNotFound = "Аккаунт не привязан или не принадлежит вам";

        public String alertEnabled = "Оповещения о входе для аккаунта {player} включены";
        public String alertDisabled = "Оповещения о входе для аккаунта {player} выключены";

        public String twoFaEnabled = "Двухфакторная аутентификация для аккаунта {player} включена";
        public String twoFaDisabled = "Двухфакторная аутентификация для аккаунта {player} выключена";

        public String queryError = "Возникла ошибка при запросе к базе данных";

        @NewLine
        public String commandUnlinkDescription = "Отвязать аккаунт игрока";
        public String commandAlertDescription = "Переключить уведомления о входе";
        public String commandTwoFaDescription = "Переключить двухфакторную аутентификацию";
        public String commandPlayerOptionDescription = "Ник игрока";
    }
}
