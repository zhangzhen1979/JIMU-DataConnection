package com.thinkdifferent.data.scheduled;

import java.util.concurrent.ScheduledFuture;

/**
 * 任务执行结果
 *
 * @author ltian
 * @version 1.0
 * @date 2021/11/17 18:09
 */
public final class ScheduledTask {
    volatile ScheduledFuture<?> future;

    /**
     * 取消定时任务
     */
    public void cancel() {
        ScheduledFuture<?> future = this.future;
        if (future != null) {
            future.cancel(true);
        }
    }

}
