package ru.matveylegenda.socialaddon.common.social.platform;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.utils.JDALogger;
import okhttp3.OkHttpClient;
import ru.matveylegenda.socialaddon.common.api.SocialPlatform;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.socialaddon.common.database.Database;
import ru.matveylegenda.socialaddon.common.listener.discord.*;
import ru.matveylegenda.socialaddon.common.manager.TaskManager;
import ru.matveylegenda.socialaddon.common.social.Social;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

public class Discord extends Social {
    @Getter
    private static JDA jda;
    private final Database database;
    private final SocialPlatform socialPlatform;

    public Discord(TaskManager taskManager, Database database, SocialPlatform socialPlatform) {
        super(taskManager);
        this.database = database;
        this.socialPlatform = socialPlatform;
    }

    @Override
    public void enableBot() throws Exception {
        if (!isEnabled()) {
            return;
        }

        JDALogger.setFallbackLoggerEnabled(false);
        JDABuilder jdaBuilder = JDABuilder.createDefault(DiscordConfig.IMP.token)
                .enableIntents(
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .addEventListeners(
                        new DiscordCodeListener(database),
                        new DiscordAllowJoinListener(socialPlatform, getTaskManager()),
                        new DiscordUnlinkListener(database),
                        new DiscordAlertListener(database),
                        new DiscordTwoFaListener(database)
                );

        if (DiscordConfig.IMP.proxy.enabled) {
            String proxyIp = DiscordConfig.IMP.proxy.ip;
            int proxyPort = DiscordConfig.IMP.proxy.port;
            String proxyUser = DiscordConfig.IMP.proxy.user;
            String proxyPassword = DiscordConfig.IMP.proxy.password;

            if (!proxyUser.isEmpty()) {
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        if (getRequestingHost().equalsIgnoreCase(proxyIp) && getRequestingPort() == proxyPort) {
                            return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                        }
                        return null;
                    }
                });
            }

            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                    .proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyIp, proxyPort)));

            jdaBuilder.setHttpClientBuilder(httpClientBuilder);
        }

        jda = jdaBuilder.build();
    }

    @Override
    public void checkPlayer(String socialId, SocialPlayer player, boolean twoFaEnabled, boolean alertEnabled) {
        if (twoFaEnabled) {
            Button allowButton = Button.primary("allow-" + player.getName(), DiscordConfig.IMP.messages.twoFaAlert.buttons.allow.text)
                    .withStyle(DiscordConfig.IMP.messages.twoFaAlert.buttons.allow.style)
                    .withEmoji(Emoji.fromUnicode(DiscordConfig.IMP.messages.twoFaAlert.buttons.allow.emoji));

            Button denyButton = Button.primary("deny-" + player.getName(), DiscordConfig.IMP.messages.twoFaAlert.buttons.deny.text)
                    .withStyle(DiscordConfig.IMP.messages.twoFaAlert.buttons.deny.style)
                    .withEmoji(Emoji.fromUnicode(DiscordConfig.IMP.messages.twoFaAlert.buttons.deny.emoji));

            jda.openPrivateChannelById(socialId).queue(channel -> {
                channel.sendMessage(
                        DiscordConfig.IMP.messages.twoFaAlert.message
                                .replace("{player}", player.getName())
                                .replace("{ip}", player.getIp())
                ).setComponents(
                        ActionRow.of(allowButton, denyButton)
                ).queue();
            });
        } else if (alertEnabled) {
            jda.openPrivateChannelById(socialId).queue(channel -> {
                channel.sendMessage(
                        DiscordConfig.IMP.messages.alert
                                .replace("{player}", player.getName())
                                .replace("{ip}", player.getIp())
                ).queue();
            });
        }
    }

    @Override
    public boolean isEnabled() {
        return DiscordConfig.IMP.enabled;
    }
}
