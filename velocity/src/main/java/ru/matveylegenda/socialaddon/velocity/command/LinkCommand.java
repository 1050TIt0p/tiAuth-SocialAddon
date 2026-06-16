package ru.matveylegenda.socialaddon.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import ru.matveylegenda.socialaddon.common.cache.CodeCache;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;
import ru.matveylegenda.socialaddon.common.utils.Utils;
import ru.matveylegenda.socialaddon.velocity.SocialAddon;

import java.util.Locale;

import static ru.matveylegenda.tiauth.util.Utils.COLORIZER;

public class LinkCommand implements SimpleCommand {
    private final SocialAddon plugin;

    public LinkCommand(SocialAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Utils.LEGACY.deserialize(COLORIZER.colorize(MessagesConfig.IMP.onlyForPlayer)));
            return;
        }

        plugin.getDatabase().getDiscordUserRepository().getIdByPlayerName(player.getUsername()).thenAccept(discordId ->
                plugin.getDatabase().getTelegramUserRepository().getIdByPlayerName(player.getUsername()).thenAccept(telegramId -> {
            if (discordId != null || telegramId != null) {
                player.sendMessage(Utils.LEGACY.deserialize(
                        COLORIZER.colorize(MessagesConfig.IMP.alreadyLinked)
                ));
                return;
            }

            if (!DiscordConfig.IMP.enabled && TelegramConfig.IMP.enabled) {
                String code = CodeCache.addCode(player.getUsername());
                player.sendMessage(Utils.LEGACY.deserialize(
                        COLORIZER.colorize(MessagesConfig.IMP.telegram.code
                                        .replace("{code}", code)
                        )
                ));
                return;
            }

            if (!TelegramConfig.IMP.enabled && DiscordConfig.IMP.enabled) {
                String code = CodeCache.addCode(player.getUsername());
                player.sendMessage(Utils.LEGACY.deserialize(
                        COLORIZER.colorize(MessagesConfig.IMP.discord.code
                                        .replace("{code}", code)
                        )
                ));
                return;
            }

            if (invocation.arguments().length != 1) {
                player.sendMessage(Utils.LEGACY.deserialize(
                        COLORIZER.colorize(MessagesConfig.IMP.usage)
                ));
                return;
            }

            String platform = invocation.arguments()[0].toLowerCase(Locale.ROOT);

            switch (platform) {
                case "discord" -> {
                    String code = CodeCache.addCode(player.getUsername());
                    player.sendMessage(Utils.LEGACY.deserialize(
                            COLORIZER.colorize(MessagesConfig.IMP.discord.code
                                            .replace("{code}", code)
                            )
                    ));
                }

                case "telegram" -> {
                    String code = CodeCache.addCode(player.getUsername());
                    player.sendMessage(Utils.LEGACY.deserialize(
                            COLORIZER.colorize(MessagesConfig.IMP.telegram.code
                                            .replace("{code}", code)
                            )
                    ));
                }

                default -> player.sendMessage(Utils.LEGACY.deserialize(
                        COLORIZER.colorize(MessagesConfig.IMP.usage)
                ));
            }
        }));
    }
}
