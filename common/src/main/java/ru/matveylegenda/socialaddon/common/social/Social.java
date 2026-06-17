package ru.matveylegenda.socialaddon.common.social;

import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.manager.TaskManager;
import ru.matveylegenda.socialaddon.common.utils.Utils;

import static ru.matveylegenda.tiauth.util.Utils.COLORIZER;

public abstract class Social {
    private final TaskManager taskManager;

    public Social(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public abstract void enableBot() throws Exception;
    public abstract void checkPlayer(String id, SocialPlayer player, boolean twoFaEnabled, boolean alertEnabled);

    public TaskManager getTaskManager() {
        return taskManager;
    }
    public abstract boolean isEnabled();

    public void checkPlayer(SocialPlayer player, String socialId, boolean twoFaEnabled, boolean alertEnabled) {
        if (!isEnabled()) {
            return;
        }

        checkPlayer(socialId, player, twoFaEnabled, alertEnabled);
        if (twoFaEnabled) {
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
}
