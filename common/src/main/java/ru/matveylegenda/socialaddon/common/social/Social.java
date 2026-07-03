package ru.matveylegenda.socialaddon.common.social;

import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.manager.TaskManager;
import ru.matveylegenda.socialaddon.common.utils.Utils;

import java.util.concurrent.CompletableFuture;

import static ru.matveylegenda.tiauth.util.Utils.COLORIZER;

public abstract class Social {
    protected final TaskManager taskManager;

    public Social(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public abstract void enableBot() throws Exception;
    public abstract CompletableFuture<Void> checkPlayer(String socialId, SocialPlayer player, boolean twoFaEnabled, boolean alertEnabled);
    public abstract boolean isEnabled();

    public void startTasks(SocialPlayer player) {
        taskManager.startTimeoutTask(player);
        taskManager.startReminderTask(
                player,
                Utils.LEGACY.deserialize(
                        COLORIZER.colorize(MessagesConfig.IMP.reminder)
                )
        );
        taskManager.startDisplayTimerTask(player);
    }
}
