package com.thinkdifferent.data.conf;

import com.thinkdifferent.data.extend.OneTableExtend;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 数据缓冲队列
 *
 * @author ltian
 * @version 1.0
 * @date 2022/1/4 10:13
 */
@Component
@Slf4j
public class DataPipe {
    private static final BlockingQueue<OneTableExtend> blockingQueue = new ArrayBlockingQueue<>(1024);

    private static ConsumerManager consumerManager;

    @Resource
    public static void setConsumerManager(ConsumerManager consumerManager) {
        DataPipe.consumerManager = consumerManager;
    }

    @SneakyThrows
    public static void write(OneTableExtend oneTableExtend) {
        blockingQueue.put(oneTableExtend);
    }

    @PostConstruct
    @SneakyThrows
    public void read() {
        while (true) {
            final OneTableExtend oneTable = blockingQueue.poll(10L, TimeUnit.SECONDS);
            if (Objects.isNull(oneTable)){
                Thread.sleep(10000);
                continue;
            }
            consumerManager.consume(oneTable);
        }
    }
}
