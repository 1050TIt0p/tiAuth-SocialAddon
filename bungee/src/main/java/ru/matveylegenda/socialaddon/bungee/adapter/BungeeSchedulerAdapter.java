package ru.matveylegenda.socialaddon.bungee.adapter;

import net.md_5.bungee.api.scheduler.ScheduledTask;
import ru.matveylegenda.socialaddon.bungee.SocialAddon;
import ru.matveylegenda.socialaddon.common.api.scheduler.SocialScheduler;
import ru.matveylegenda.socialaddon.common.api.scheduler.SocialTask;

import java.util.concurrent.TimeUnit;

public class BungeeSchedulerAdapter implements SocialScheduler {
    private final SocialAddon plugin;

    public BungeeSchedulerAdapter(SocialAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public SocialTask runLater(Runnable task, long delay, TimeUnit unit) {
        ScheduledTask scheduledTask = plugin.getProxy().getScheduler().schedule(plugin, task, delay, unit);
        return new BungeeTask(scheduledTask);
    }

    @Override
    public SocialTask runTimer(Runnable task, long delay, long period, TimeUnit unit) {
        ScheduledTask scheduledTask = plugin.getProxy().getScheduler().schedule(plugin, task, delay, period, unit);
        return new BungeeTask(scheduledTask);
    }

    @Override
    public void run(Runnable task) {
        plugin.getProxy().getScheduler().runAsync(plugin, task);
    }

    private record BungeeTask(ScheduledTask handle) implements SocialTask {
        @Override
        public void cancel() {
            handle.cancel();
        }
    }
}