package ru.matveylegenda.socialaddon.common.listener.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.socialaddon.common.database.Database;

public class DiscordSlashCommandListener extends ListenerAdapter {
    private final Database database;

    public DiscordSlashCommandListener(Database database) {
        this.database = database;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.isFromGuild()) {
            event.reply(DiscordConfig.IMP.messages.accountNotFound).setEphemeral(true).queue();
            return;
        }

        OptionMapping option = event.getOption("player");
        if (option == null) {
            event.reply(DiscordConfig.IMP.messages.queryError).setEphemeral(true).queue();
            return;
        }

        String playerName = option.getAsString();

        switch (event.getName()) {
            case "unlink" -> handleUnlink(event, playerName);
            case "alert" -> handleAlert(event, playerName);
            case "2fa" -> handleTwoFa(event, playerName);
        }
    }

    private void handleUnlink(SlashCommandInteractionEvent event, String playerName) {
        String discordId = event.getUser().getId();

        database.getDiscordUserRepository().getUserByPlayerName(playerName).thenAccept(user -> {
            if (user == null || !discordId.equalsIgnoreCase(user.getDiscordId())) {
                event.reply(DiscordConfig.IMP.messages.accountNotFound).setEphemeral(true).queue();
                return;
            }

            database.getDiscordUserRepository().removeUser(playerName).thenAccept(success -> {
                if (!success) {
                    event.reply(DiscordConfig.IMP.messages.queryError).setEphemeral(true).queue();
                    return;
                }

                event.reply(DiscordConfig.IMP.messages.accountUnlinked.replace("{player}", playerName))
                        .setEphemeral(true).queue();
            });
        });
    }

    private void handleAlert(SlashCommandInteractionEvent event, String playerName) {
        String discordId = event.getUser().getId();

        database.getDiscordUserRepository().getUserByPlayerName(playerName).thenAccept(user -> {
            if (user == null || !discordId.equalsIgnoreCase(user.getDiscordId())) {
                event.reply(DiscordConfig.IMP.messages.accountNotFound).setEphemeral(true).queue();
                return;
            }

            database.getDiscordUserRepository().setAlertEnabled(playerName, !user.isAlert()).thenAccept(success -> {
                if (!success) {
                    event.reply(DiscordConfig.IMP.messages.queryError).setEphemeral(true).queue();
                    return;
                }

                if (user.isAlert()) {
                    event.reply(DiscordConfig.IMP.messages.alertDisabled.replace("{player}", playerName))
                            .setEphemeral(true).queue();
                } else {
                    event.reply(DiscordConfig.IMP.messages.alertEnabled.replace("{player}", playerName))
                            .setEphemeral(true).queue();
                }
            });
        });
    }

    private void handleTwoFa(SlashCommandInteractionEvent event, String playerName) {
        String discordId = event.getUser().getId();

        database.getDiscordUserRepository().getUserByPlayerName(playerName).thenAccept(user -> {
            if (user == null || !discordId.equalsIgnoreCase(user.getDiscordId())) {
                event.reply(DiscordConfig.IMP.messages.accountNotFound).setEphemeral(true).queue();
                return;
            }

            database.getDiscordUserRepository().setTwoFaEnabled(playerName, !user.isTwoFa()).thenAccept(success -> {
                if (!success) {
                    event.reply(DiscordConfig.IMP.messages.queryError).setEphemeral(true).queue();
                    return;
                }

                if (user.isTwoFa()) {
                    event.reply(DiscordConfig.IMP.messages.twoFaDisabled.replace("{player}", playerName))
                            .setEphemeral(true).queue();
                } else {
                    event.reply(DiscordConfig.IMP.messages.twoFaEnabled.replace("{player}", playerName))
                            .setEphemeral(true).queue();
                }
            });
        });
    }
}
