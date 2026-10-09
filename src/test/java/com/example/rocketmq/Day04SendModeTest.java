package com.example.rocketmq;

import com.example.rocketmq.service.SendModeService;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Day04SendModeTest {

    @Test
    void asyncSendInvokesCallback() throws Exception {
        SendModeService svc = new SendModeService("day04-producer");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<SendResult> ref = new AtomicReference<>();
        try {
            svc.asyncSend("TopicDay04", "tagA", "async-body", new SendCallback() {
                @Override
                public void onSuccess(SendResult result) {
                    ref.set(result);
                    latch.countDown();
                }

                @Override
                public void onException(Throwable e) {
                    latch.countDown();
                }
            });
            assertTrue(latch.await(15, TimeUnit.SECONDS), "异步回调应在 15s 内触发");
            assertNotNull(ref.get());
        } finally {
            svc.close();
        }
    }

    @Test
    void onewaySendDoesNotThrow() throws Exception {
        SendModeService svc = new SendModeService("day04-producer-2");
        try {
            // 单向发送无返回值，只保证发出去，用于日志等可丢场景
            svc.onewaySend("TopicDay04", "tagB", "log-line");
        } finally {
            svc.close();
        }
    }
}
