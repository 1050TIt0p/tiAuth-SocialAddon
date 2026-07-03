package ru.matveylegenda.socialaddon.common.listener.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.socialaddon.common.database.Database;

public class DiscordAlertListener extends ListenerAdapter {
    private final Database database;

    public DiscordAlertListener(Database database) {
        this.database = database;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            return;
        }

        User discordUser = event.getAuthor();
        Message message = event.getMessage();
        String[] messageSplit = message.getContentRaw().strip().split("\\s+");

        if (messageSplit.length < 2) {
            return;
        }

        if (!messageSplit[0].equalsIgnoreCase("/alert")) {
            return;
        }

        String playerName = messageSplit[1];
        database.getDiscordUserRepository().getUserByPlayerName(playerName).thenAccept(user -> {
            if (user == null || !discordUser.getId().equalsIgnoreCase(user.getDiscordId())) {
                message.reply(DiscordConfig.IMP.messages.accountNotFound)
                        .queue();

                return;
            }

            database.getDiscordUserRepository().setAlertEnabled(playerName, !user.isAlert()).thenAccept(success -> {
                if (!success) {
                    message.reply(DiscordConfig.IMP.messages.queryError)
                            .queue();
                    return;
                }

                if (user.isAlert()) {
                    message.reply(DiscordConfig.IMP.messages.alertDisabled.replace("{player}", playerName))
                            .queue();
                } else {
                    message.reply(DiscordConfig.IMP.messages.alertEnabled.replace("{player}", playerName))
                            .queue();
                }
            });
        });
    }
}
