package ru.matveylegenda.socialaddon.common.api.scheduler;

import java.util.concurrent.TimeUnit;

public interface SocialScheduler {
    SocialTask runLater(Runnable task, long delay, TimeUnit unit);
    SocialTask runTimer(Runnable task, long delay, long period, TimeUnit unit);
    void run(Runnable task);
}
