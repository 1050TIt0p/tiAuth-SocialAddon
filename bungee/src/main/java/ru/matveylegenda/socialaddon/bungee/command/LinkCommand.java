package ru.matveylegenda.socialaddon.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import ru.matveylegenda.socialaddon.bungee.SocialAddon;
import ru.matveylegenda.socialaddon.common.cache.CodeCache;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.socialaddon.common.config.social.TelegramConfig;

import java.util.Locale;

import static ru.matveylegenda.tiauth.util.Utils.COLORIZER;

public class LinkCommand extends Command {
    private final SocialAddon plugin;

    public LinkCommand(SocialAddon plugin, String name) {
        super(name, null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(TextComponent.fromLegacy(
                    COLORIZER.colorize(MessagesConfig.IMP.onlyForPlayer)
            ));
            return;
        }

        plugin.getDatabase().getDiscordUserRepository().getIdByPlayerName(player.getName()).thenAccept(discordId ->
                plugin.getDatabase().getTelegramUserRepository().getIdByPlayerName(player.getName()).thenAccept(telegramId -> {
            if (discordId != null || telegramId != null) {
                sender.sendMessage(TextComponent.fromLegacy(
                        COLORIZER.colorize(MessagesConfig.IMP.alreadyLinked)
                ));
                return;
            }

            if (!DiscordConfig.IMP.enabled && TelegramConfig.IMP.enabled) {
                String code = CodeCache.addCode(player.getName());
                sender.sendMessage(TextComponent.fromLegacy(
                        COLORIZER.colorize(MessagesConfig.IMP.telegram.code
                                        .replace("{code}", code)
                        )
                ));
                return;
            }

            if (!TelegramConfig.IMP.enabled && DiscordConfig.IMP.enabled) {
                String code = CodeCache.addCode(player.getName());
                sender.sendMessage(TextComponent.fromLegacy(
                        COLORIZER.colorize(MessagesConfig.IMP.discord.code
                                .replace("{code}", code)
                        )
                ));
                return;
            }

            if (args.length != 1) {
                sender.sendMessage(TextComponent.fromLegacy(
                        COLORIZER.colorize(MessagesConfig.IMP.usage)
                ));
                return;
            }

            String platform = args[0].toLowerCase(Locale.ROOT);

            switch (platform) {
                case "discord" -> {
                    String code = CodeCache.addCode(player.getName());
                    sender.sendMessage(TextComponent.fromLegacy(
                            COLORIZER.colorize(MessagesConfig.IMP.discord.code
                                            .replace("{code}", code)
                            )
                    ));
                }

                case "telegram" -> {
                    String code = CodeCache.addCode(player.getName());
                    sender.sendMessage(TextComponent.fromLegacy(
                            COLORIZER.colorize(MessagesConfig.IMP.telegram.code
                                            .replace("{code}", code)
                            )
                    ));
                }

                default -> player.sendMessage(TextComponent.fromLegacy(
                        COLORIZER.colorize(MessagesConfig.IMP.usage)
                ));
            }
        }));
    }
}
