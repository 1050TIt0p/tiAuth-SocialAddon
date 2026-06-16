package ru.matveylegenda.socialaddon.common.config;

import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.SerializerConfig;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.Comment;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.CommentValue;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.NewLine;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.Transient;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.language.object.YamlSerializable;

import java.nio.file.Paths;
import java.util.List;

public class MainConfig extends YamlSerializable {

    @Transient
    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .setCommentValueIndent(1)
            .build();

    @Transient
    public static final MainConfig IMP = new MainConfig();

    public MainConfig() {
        super(Paths.get("plugins/tiAuth-SocialAddon/config.yml"), CONFIG);
        this.code = new Code();
        this.bossBar = new BossBar();
        this.title = new Title();
        this.actionBar = new ActionBar();
    }

    @Comment({
            @CommentValue("Команда используемая для привязки"),
            @CommentValue("После изменения требуется перезагрузить сервер")
    })
    public String command = "link";

    @Comment({
            @CommentValue("Сервера требующие привязку к соц. сети для входа")
    })
    public List<String> linkedOnlyServers = List.of("example");

    public Code code;

    @Comment({
            @CommentValue("Настройки кода для привязки")
    })
    public static class Code {
        @Comment({
                @CommentValue("Символы которые будут содержаться в коде")
        })
        public String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        @Comment({
                @CommentValue("Длина кода")
        })
        public int length = 6;
    }

    @Comment({
            @CommentValue("Сколько игроку дается на подтверждение входа")
    })
    public int timeoutSeconds = 30;

    public BossBar bossBar;

    @NewLine
    @Comment({
            @CommentValue("На BungeeCord возможна нестабильная работа (кик игроков при отправке BossBar'а), на Velocity проблема не наблюдалась")
    })
    public static class BossBar {
        public boolean enabled = false;
        public String text = "&#8833EC2ꜰᴀ &8» &fОсталось &#8833EC2{time} секунд";
        @Comment(
                value = @CommentValue("PINK / BLUE / RED / GREEN / YELLOW / PURPLE / WHITE"),
                at = Comment.At.SAME_LINE
        )
        public net.kyori.adventure.bossbar.BossBar.Color color = net.kyori.adventure.bossbar.BossBar.Color.PURPLE;
        @Comment(
                value = @CommentValue("PROGRESS / NOTCHED_6 / NOTCHED_10 / NOTCHED_12 / NOTCHED_20"),
                at = Comment.At.SAME_LINE
        )
        public net.kyori.adventure.bossbar.BossBar.Overlay style = net.kyori.adventure.bossbar.BossBar.Overlay.NOTCHED_12;
    }

    public Title title;

    @NewLine
    public static class Title {
        public boolean enabled = true;
        public String title = "&#8833EC2ꜰᴀ &8»";
        public String subtitle = "&fОсталось &#8833EC2{time} секунд";
    }

    public ActionBar actionBar;

    @NewLine
    public static class ActionBar {
        public boolean enabled = false;
        public String text = "&#8833EC2ꜰᴀ &8» &fОсталось &#8833EC2{time} секунд";
    }
}
