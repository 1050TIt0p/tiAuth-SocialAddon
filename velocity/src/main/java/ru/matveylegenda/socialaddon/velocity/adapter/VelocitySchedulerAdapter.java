package ru.matveylegenda.socialaddon.velocity.adapter;

import com.velocitypowered.api.scheduler.ScheduledTask;
import ru.matveylegenda.socialaddon.common.api.scheduler.SocialScheduler;
import ru.matveylegenda.socialaddon.common.api.scheduler.SocialTask;
import ru.matveylegenda.socialaddon.velocity.SocialAddon;

import java.util.concurrent.TimeUnit;

public class VelocitySchedulerAdapter implements SocialScheduler {
    private final SocialAddon plugin;

    public VelocitySchedulerAdapter(SocialAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public SocialTask runLater(Runnable task, long delay, TimeUnit unit) {
        ScheduledTask scheduledTask = plugin.getServer().getScheduler()
                .buildTask(plugin, task)
                .delay(delay, unit)
                .schedule();
        return new VelocityTask(scheduledTask);
    }

    @Override
    public SocialTask runTimer(Runnable task, long delay, long period, TimeUnit unit) {
        ScheduledTask scheduledTask = plugin.getServer().getScheduler()
                .buildTask(plugin, task)
                .delay(delay, unit)
                .repeat(period, unit)
                .schedule();
        return new VelocityTask(scheduledTask);
    }

    @Override
    public void run(Runnable task) {
        plugin.getServer().getScheduler()
                .buildTask(plugin, task)
                .schedule();
    }

    private record VelocityTask(ScheduledTask handle) implements SocialTask {
        @Override
        public void cancel() {
            handle.cancel();
        }
    }
}