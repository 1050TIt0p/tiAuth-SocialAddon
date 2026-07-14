package ru.matveylegenda.socialaddon.common.config.social;

import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.SerializerConfig;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.Comment;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.CommentValue;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.NewLine;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.Transient;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.language.object.YamlSerializable;

import java.nio.file.Paths;

public class MaxConfig extends YamlSerializable {

    @Transient
    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .setCommentValueIndent(1)
            .build();

    @Transient
    public static final MaxConfig IMP = new MaxConfig();

    public MaxConfig() {
        super(Paths.get("plugins/tiAuth-SocialAddon/socials/max.yml"), CONFIG);
        this.messages = new Messages();
        this.messages.twoFaAlert = new Messages.TwoFaAlert();
        this.messages.twoFaAlert.buttons = new Messages.TwoFaAlert.Buttons();
        this.messages.twoFaAlert.buttons.allow = new Messages.TwoFaAlert.Buttons.Allow();
        this.messages.twoFaAlert.buttons.deny = new Messages.TwoFaAlert.Buttons.Deny();
        this.messages.keyboard = new Messages.Keyboard();
    }

    public boolean enabled = false;

    public String token = "";

    @Comment({
            @CommentValue("Максимальное количество привязанных аккаунтов к Telegram")
    })
    public int maxLinkAccounts = 3;

    @Comment({
            @CommentValue("Включать ли сразу 2FA после привязки аккаунта к Telegram")
    })
    public boolean defaultEnableTwoFa = false;

    @Comment({
            @CommentValue("Включать ли сразу отправку уведомлений о входе/выходе после привязки аккаунта к Telegram")
    })
    public boolean defaultEnableAlert = true;

    public Messages messages;
    public static class Messages {
        public String start = "Добро пожаловать!";

        public TwoFaAlert twoFaAlert;
        public static class TwoFaAlert {
            public String message = "Подтвердите вход на сервер\n\nАккаунт: {player}\nIP: {ip}";

            public Buttons buttons;
            public static class Buttons {
                public Allow allow;
                public static class Allow {
                    public String text = "✅ Подтвердить";
                }

                public Deny deny;
                public static class Deny {
                    public String text = "❌ Отклонить";
                }
            }
        }

        public String alert = "Обнаружен вход на сервер\n\nАккаунт: {player}\nIP: {ip}";

        @NewLine
        public String playerNotFound = "Игрок не найден";

        public String allowJoin = "Вход подтвержден";
        public String denyJoin = "Вход отклонен";

        public String accountLimitReached = "К вашему Max привязано максимальное количество аккаунтов";

        public String codeAccept = "Код принят. Для завершения привязки напишите на сервере: /link accept";
        public String accountUnlinked = "Аккаунт {player} успешно отвязан от вашего Max";

        public String accountNotFound = "Аккаунт не привязан или не принадлежит вам";

        public String alertEnabled = "Оповещения о входе для аккаунта {player} включены";
        public String alertDisabled = "Оповещения о входе для аккаунта {player} выключены";

        public String twoFaEnabled = "Двухфакторная аутентификация для аккаунта {player} включена";
        public String twoFaDisabled = "Двухфакторная аутентификация для аккаунта {player} выключена";

        public String queryError = "Возникла ошибка при запросе к базе данных";

        public Keyboard keyboard;
        public static class Keyboard {
            public String accounts = "👤 Управление аккаунтами";
            public String alert = "🔔 Оповещения";
            public String twoFa = "🔐 2FA";
            public String changePassword = "🔑 Изменить пароль";
            public String kick = "🦵 Кикнуть";
        }

        public String noLinked = "У вас нет привязанных аккаунтов";
        public String selectAccount = "Выберите аккаунт:";
        public String selectedAccount = "Управление аккаунтом {player}";
        public String newPassword = "Ваш новый пароль: {password}";
        public String playerKicked = "Игрок кикнут";
    }
}
