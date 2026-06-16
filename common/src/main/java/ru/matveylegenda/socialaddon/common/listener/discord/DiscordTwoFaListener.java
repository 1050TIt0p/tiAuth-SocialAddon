package ru.matveylegenda.socialaddon.common.listener.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.socialaddon.common.database.Database;

public class DiscordTwoFaListener extends ListenerAdapter {
    private final Database database;

    public DiscordTwoFaListener(Database database) {
        this.database = database;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            return;
        }

        User discordUser = event.getAuthor();
        Message message = event.getMessage();
        String[] messageSplit = message.getContentRaw().split(" ");

        if (messageSplit.length != 2) {
            return;
        }

        if (!messageSplit[0].equalsIgnoreCase("/2fa")) {
            return;
        }

        String playerName = messageSplit[1];
        database.getDiscordUserRepository().getUserByPlayerName(playerName).thenAccept(user -> {
            if (user == null || !discordUser.getId().equalsIgnoreCase(user.getDiscordId())) {
                message.reply(DiscordConfig.IMP.messages.accountNotFound)
                        .queue();

                return;
            }

            database.getDiscordUserRepository().setTwoFaEnabled(playerName, !user.isTwoFa()).thenAccept(success -> {
                if (!success) {
                    message.reply(DiscordConfig.IMP.messages.queryError)
                            .queue();
                    return;
                }

                if (user.isTwoFa()) {
                    message.reply(DiscordConfig.IMP.messages.twoFaDisabled.replace("{player}", playerName))
                            .queue();
                } else {
                    message.reply(DiscordConfig.IMP.messages.twoFaEnabled.replace("{player}", playerName))
                            .queue();
                }
            });
        });
    }
}
