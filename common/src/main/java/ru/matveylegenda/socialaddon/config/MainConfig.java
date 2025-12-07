package ru.matveylegenda.socialaddon.config;

import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.SerializerConfig;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.Comment;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.CommentValue;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.NewLine;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.annotations.Transient;
import ru.matveylegenda.tiauth.thirdparty.net.elytrium.serializer.language.object.YamlSerializable;
import ru.matveylegenda.tiauth.util.BarColor;
import ru.matveylegenda.tiauth.util.BarStyle;

import java.nio.file.Paths;

public class MainConfig extends YamlSerializable {

    @Transient
    private static final SerializerConfig CONFIG = new SerializerConfig.Builder()
            .setCommentValueIndent(1)
            .build();

    @Transient
    public static final MainConfig IMP = new MainConfig();

    public MainConfig() {
        super(Paths.get("plugins/tiAuth/config.yml"), CONFIG);
        this.bossBar = new BossBar();
        this.title = new Title();
        this.actionBar = new ActionBar();
    }

    @Comment({
            @CommentValue(" Команда используемая для привязки"),
            @CommentValue(" После изменения требуется перезагрузить сервер")
    })
    public String command = "2fa";

    public BossBar bossBar;

    @NewLine
    public static class BossBar {
        public boolean enabled = true;
        @Comment(
                value = @CommentValue("PINK / BLUE / RED / GREEN / YELLOW / PURPLE / WHITE"),
                at = Comment.At.SAME_LINE
        )
        public BarColor color = BarColor.PURPLE;
        @Comment(
                value = @CommentValue("SOLID / SEGMENTED_6 / SEGMENTED_10 / SEGMENTED_12 / SEGMENTED_20"),
                at = Comment.At.SAME_LINE
        )
        public BarStyle style = BarStyle.SEGMENTED_12;
    }

    public Title title;

    @NewLine
    public static class Title {
        public boolean enabled = false;
    }

    public ActionBar actionBar;

    @NewLine
    public static class ActionBar {
        public boolean enabled = false;
    }
}
