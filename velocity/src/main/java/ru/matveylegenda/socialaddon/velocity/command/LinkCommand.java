package ru.matveylegenda.socialaddon.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import ru.matveylegenda.socialaddon.common.cache.CodeCache;
import ru.matveylegenda.socialaddon.common.cache.LinkConfirmCache;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.socialaddon.common.config.social.MaxConfig;
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

        if (invocation.arguments().length == 1 && invocation.arguments()[0].equalsIgnoreCase("accept")) {

            LinkConfirmCache.LinkRequest request = LinkConfirmCache.get(player.getUsername());

            if (request == null) {
                sender.sendMessage(Utils.LEGACY.deserialize(
                        COLORIZER.colorize(
                                COLORIZER.colorize(
                                        MessagesConfig.IMP.noLinkConfirmation
                                )
                        )
                ));
                return;
            }

            switch (request.platform()) {

                case "discord" -> {
                    plugin.getDatabase().getDiscordUserRepository().addUser(player.getUsername(), request.accountId()).thenAccept(success -> {
                        if (!success) {
                            sender.sendMessage(Utils.LEGACY.deserialize(
                                    COLORIZER.colorize(
                                            MessagesConfig.IMP.queryError
                                    )
                            ));
                            return;
                        }

                        LinkConfirmCache.remove(player.getUsername());

                        sender.sendMessage(Utils.LEGACY.deserialize(
                                COLORIZER.colorize(
                                        MessagesConfig.IMP.discord.accountLinked
                                )
                        ));
                    });
                }

                case "telegram" -> {
                    plugin.getDatabase().getTelegramUserRepository().addUser(player.getUsername(), request.accountId()).thenAccept(success -> {
                        if (!success) {
                            sender.sendMessage(Utils.LEGACY.deserialize(
                                    COLORIZER.colorize(
                                            MessagesConfig.IMP.queryError
                                    )
                            ));
                            return;
                        }

                        LinkConfirmCache.remove(player.getUsername());

                        sender.sendMessage(Utils.LEGACY.deserialize(
                                COLORIZER.colorize(
                                        MessagesConfig.IMP.telegram.accountLinked
                                )
                        ));
                    });
                }

                case "max" -> {
                    plugin.getDatabase().getMaxUserRepository().addUser(player.getUsername(), request.accountId()).thenAccept(success -> {
                        if (!success) {
                            sender.sendMessage(Utils.LEGACY.deserialize(
                                    COLORIZER.colorize(
                                            MessagesConfig.IMP.queryError
                                    )
                            ));
                            return;
                        }

                        LinkConfirmCache.remove(player.getUsername());

                        sender.sendMessage(Utils.LEGACY.deserialize(
                                COLORIZER.colorize(
                                        MessagesConfig.IMP.max.accountLinked
                                )
                        ));
                    });
                }
            }

            return;
        }

        plugin.getDatabase().getDiscordUserRepository().getIdByPlayerName(player.getUsername()).thenAccept(discordId -> {
            plugin.getDatabase().getTelegramUserRepository().getIdByPlayerName(player.getUsername()).thenAccept(telegramId -> {
                plugin.getDatabase().getMaxUserRepository().getIdByPlayerName(player.getUsername()).thenAccept(maxId -> {
                    if (discordId != null || telegramId != null || maxId != null) {
                        player.sendMessage(Utils.LEGACY.deserialize(
                                COLORIZER.colorize(MessagesConfig.IMP.alreadyLinked)
                        ));
                        return;
                    }

                    if (!DiscordConfig.IMP.enabled && !MaxConfig.IMP.enabled && TelegramConfig.IMP.enabled) {
                        String code = CodeCache.addCode(player.getUsername());
                        player.sendMessage(Utils.LEGACY.deserialize(
                                COLORIZER.colorize(MessagesConfig.IMP.telegram.code
                                        .replace("{code}", code)
                                )
                        ));
                        return;
                    }

                    if (!TelegramConfig.IMP.enabled && !MaxConfig.IMP.enabled && DiscordConfig.IMP.enabled) {
                        String code = CodeCache.addCode(player.getUsername());
                        player.sendMessage(Utils.LEGACY.deserialize(
                                COLORIZER.colorize(MessagesConfig.IMP.discord.code
                                        .replace("{code}", code)
                                )
                        ));
                        return;
                    }

                    if (!DiscordConfig.IMP.enabled && !TelegramConfig.IMP.enabled && MaxConfig.IMP.enabled) {
                        String code = CodeCache.addCode(player.getUsername());
                        player.sendMessage(Utils.LEGACY.deserialize(
                                COLORIZER.colorize(MessagesConfig.IMP.max.code
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

                        case "max" -> {
                            String code = CodeCache.addCode(player.getUsername());
                            player.sendMessage(Utils.LEGACY.deserialize(
                                    COLORIZER.colorize(MessagesConfig.IMP.max.code
                                            .replace("{code}", code)
                                    )
                            ));
                        }

                        default -> player.sendMessage(Utils.LEGACY.deserialize(
                                COLORIZER.colorize(MessagesConfig.IMP.usage)
                        ));
                    }
                });
            });
        });
    }
}
