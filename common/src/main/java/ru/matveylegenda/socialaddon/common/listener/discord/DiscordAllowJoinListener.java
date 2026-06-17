package ru.matveylegenda.socialaddon.common.listener.discord;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.matveylegenda.socialaddon.common.api.SocialPlatform;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.config.social.DiscordConfig;
import ru.matveylegenda.socialaddon.common.manager.TaskManager;
import ru.matveylegenda.socialaddon.common.utils.Utils;
import ru.matveylegenda.tiauth.cache.AuthCache;
import ru.matveylegenda.tiauth.cache.SessionCache;
import ru.matveylegenda.tiauth.config.MainConfig;

import static ru.matveylegenda.tiauth.util.Utils.COLORIZER;

public class DiscordAllowJoinListener extends ListenerAdapter {
    private final SocialPlatform socialPlatform;
    private final TaskManager taskManager;

    public DiscordAllowJoinListener(SocialPlatform socialPlatform, TaskManager taskManager) {
        this.socialPlatform = socialPlatform;
        this.taskManager = taskManager;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.isFromGuild()) {
            return;
        }

        if (event.isAcknowledged()) {
            return;
        }

        String componentId = event.getComponentId();
        if (componentId.startsWith("allow-")) {
            String playerName = componentId.replace("allow-", "");
            SocialPlayer player = socialPlatform.getPlayer(playerName);

            if (player == null) {
                event.editMessage(DiscordConfig.IMP.messages.playerNotFound)
                        .setComponents()
                        .queue();
                return;
            }

            AuthCache.setAuthenticated(playerName);
            SessionCache.addPlayer(playerName, player.getIp());
            taskManager.cancelTasks(player);
            player.connect(MainConfig.IMP.servers.backend);
            player.sendMessage(Utils.LEGACY.deserialize(
                    COLORIZER.colorize(MessagesConfig.IMP.allowJoin)
            ));
            event.editMessage(DiscordConfig.IMP.messages.allowJoin)
                    .setComponents()
                    .queue();
        } else if (componentId.startsWith("deny-")) {
            String playerName = componentId.replace("deny-", "");
            SocialPlayer player = socialPlatform.getPlayer(playerName);

            if (player == null) {
                event.editMessage(DiscordConfig.IMP.messages.playerNotFound)
                        .setComponents()
                        .queue();
                return;
            }

            taskManager.cancelTasks(player);
            player.disconnect(Utils.LEGACY.deserialize(
                    COLORIZER.colorize(MessagesConfig.IMP.denyJoin)
            ));
            event.editMessage(DiscordConfig.IMP.messages.denyJoin)
                    .setComponents()
                    .queue();
        }
    }
}
