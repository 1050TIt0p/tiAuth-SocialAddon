package ru.matveylegenda.socialaddon.common.listener.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.matveylegenda.socialaddon.common.cache.CodeCache;
import ru.matveylegenda.socialaddon.common.cache.LinkConfirmCache;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.socialaddon.common.database.Database;

public class DiscordCodeListener extends ListenerAdapter {
    private final Database database;

    public DiscordCodeListener(Database database) {
        this.database = database;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            return;
        }

        User user = event.getAuthor();
        Message message = event.getMessage();

        String playerName = CodeCache.getPlayerName(message.getContentRaw());
        if (playerName != null) {
            database.getDiscordUserRepository().getAccountCountById(user.getId()).thenAccept(accountsCount -> {
                if (accountsCount >= DiscordConfig.IMP.maxLinkAccounts) {
                    message.reply(DiscordConfig.IMP.messages.accountLimitReached)
                            .queue();
                    return;
                }

                CodeCache.removeCode(message.getContentRaw());
                LinkConfirmCache.add(
                        playerName,
                        new LinkConfirmCache.LinkRequest("discord", user.getId())
                );

                message.reply(
                        DiscordConfig.IMP.messages.codeAccept
                ).queue();
//
//                database.getDiscordUserRepository().addUser(playerName, user.getId()).thenAccept(success -> {
//                    if (!success) {
//                        message.reply(DiscordConfig.IMP.messages.queryError)
//                                .queue();
//                        return;
//                    }
//
//                    CodeCache.removeCode(message.getContentRaw());
//                    message.reply(DiscordConfig.IMP.messages.accountLinked.replace("{player}", playerName))
//                            .queue();
//                });
            });
        }
    }
}
