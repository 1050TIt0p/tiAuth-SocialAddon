package ru.matveylegenda.socialaddon.common.manager;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import ru.matveylegenda.socialaddon.common.api.SocialPlayer;
import ru.matveylegenda.socialaddon.common.api.scheduler.SocialScheduler;
import ru.matveylegenda.socialaddon.common.api.scheduler.SocialTask;
import ru.matveylegenda.socialaddon.common.config.MainConfig;
import ru.matveylegenda.socialaddon.common.config.MessagesConfig;
import ru.matveylegenda.socialaddon.common.utils.Utils;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.matveylegenda.tiauth.util.Utils.COLORIZER;

public class TaskManager {
    private final Map<UUID, SocialTask> authTimeoutTasks = new ConcurrentHashMap<>();
    private final Map<UUID, SocialTask> authReminderTasks = new ConcurrentHashMap<>();
    private final Map<UUID, SocialTask> displayTimerTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();

    private final SocialScheduler scheduler;

    public TaskManager(SocialScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void startTimeoutTask(SocialPlayer player) {
        UUID uuid = player.getUniqueId();

        SocialTask task = scheduler.runLater(() -> {
            if (!player.isOnline()) {
                SocialTask oldTask = authTimeoutTasks.remove(uuid);
                if (oldTask != null) oldTask.cancel();
                return;
            }

            player.disconnect(Utils.LEGACY.deserialize(COLORIZER.colorize(MessagesConfig.IMP.timeout)));
        }, MainConfig.IMP.timeoutSeconds, TimeUnit.SECONDS);

        authTimeoutTasks.put(uuid, task);
    }

    public void startReminderTask(SocialPlayer player, Component reminder) {
        UUID uuid = player.getUniqueId();

        SocialTask task = scheduler.runTimer(() -> {
            if (!player.isOnline()) {
                SocialTask oldTask = authReminderTasks.remove(uuid);
                if (oldTask != null) oldTask.cancel();
                return;
            }
            player.sendMessage(reminder);
        }, 0, 5, TimeUnit.SECONDS);

        authReminderTasks.put(uuid, task);
    }

    public void startDisplayTimerTask(SocialPlayer player) {
        AtomicInteger counter = new AtomicInteger(MainConfig.IMP.timeoutSeconds);

        // костыль с задержкой, потом убрать! адвенчур кал бля пиперов рот ебал
        if (MainConfig.IMP.bossBar.enabled) {
            scheduler.runLater(() -> {
                BossBar bar = BossBar.bossBar(
                        Component.text(" "),
                        1.0f,
                        MainConfig.IMP.bossBar.color,
                        MainConfig.IMP.bossBar.style
                );
                bossBars.put(player.getUniqueId(), bar);
                player.showBossBar(bar);
            }, 50, TimeUnit.MILLISECONDS);
        }

        SocialTask task = scheduler.runTimer(() -> {
            int c = counter.get();
            if (c <= 0 || !player.isOnline()) {
                clearDisplays(player);
                SocialTask old = displayTimerTasks.remove(player.getUniqueId());
                if (old != null) old.cancel();
                return;
            }

            if (MainConfig.IMP.title.enabled) {
                sendTitle(player, c);
            }
            if (MainConfig.IMP.actionBar.enabled) {
                sendActionBar(player, c);
            }
            if (MainConfig.IMP.bossBar.enabled) {
                updateBossBar(player, c);
            }

            counter.decrementAndGet();
        }, 0, 1, TimeUnit.SECONDS);

        displayTimerTasks.put(player.getUniqueId(), task);
    }

    private void sendTitle(SocialPlayer player, int counter) {
        Title componentTitle = Title.title(
                Utils.LEGACY.deserialize(COLORIZER.colorize(MainConfig.IMP.title.title)).replaceText(builder -> builder
                        .match(Utils.TIME)
                        .replacement(String.valueOf(counter))),
                Utils.LEGACY.deserialize(COLORIZER.colorize(MainConfig.IMP.title.subtitle)).replaceText(builder -> builder
                        .match(Utils.TIME)
                        .replacement(String.valueOf(counter))),
                Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1050), Duration.ofMillis(0))
        );
        player.showTitle(componentTitle);
    }

    private void sendActionBar(SocialPlayer player, int counter) {
        Component comp = Utils.LEGACY.deserialize(COLORIZER.colorize(MainConfig.IMP.actionBar.text)).replaceText(builder -> builder
                .match(Utils.TIME)
                .replacement(String.valueOf(counter)));
        player.sendActionBar(comp);
    }

    private void updateBossBar(SocialPlayer player, int counter) {
        BossBar bar = bossBars.get(player.getUniqueId());
        if (bar != null) {
            bar.name(Utils.LEGACY.deserialize(COLORIZER.colorize(MainConfig.IMP.bossBar.text)).replaceText(builder -> builder
                    .match(Utils.TIME)
                    .replacement(String.valueOf(counter))));
            bar.progress((float) counter / (float) MainConfig.IMP.timeoutSeconds);
        }
    }

    private void clearDisplays(SocialPlayer player) {
        player.sendActionBar(Component.empty());
        UUID pid = player.getUniqueId();
        BossBar bar = bossBars.remove(pid);
        if (bar != null) {
            player.hideBossBar(bar);
        }
    }

    public void cancelTasks(SocialPlayer player) {
        UUID pid = player.getUniqueId();

        SocialTask task;

        task = authTimeoutTasks.remove(pid);
        if (task != null) task.cancel();

        task = authReminderTasks.remove(pid);
        if (task != null) task.cancel();

        task = displayTimerTasks.remove(pid);
        if (task != null) task.cancel();

        BossBar bar = bossBars.remove(pid);
        if (bar != null) {
            player.hideBossBar(bar);
        }
    }
}