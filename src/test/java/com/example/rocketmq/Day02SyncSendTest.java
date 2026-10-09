package com.example.rocketmq;

import com.example.rocketmq.service.SyncProducerService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Day02SyncSendTest {

    @Test
    void syncSendReturnsResult() throws Exception {
        SyncProducerService svc = new SyncProducerService("day02-producer");
        try {
            SendResult r = svc.send("TopicDay02", "order", "k1", "order-created");
            assertNotNull(r);
            assertNotNull(r.getMsgId());
            assertNotNull(r.getSendStatus());
            assertNotNull(r.getMessageQueue());
        } finally {
            svc.close();
        }
    }

    @Test
    void sendStatusValuesExist() {
        // SEND_OK 正常；后三者代表刷盘/同步从节点未达标，业务要告警或补偿
        assertSame(SendStatus.SEND_OK, SendStatus.SEND_OK);
        assertNotNull(SendStatus.FLUSH_DISK_TIMEOUT);
        assertNotNull(SendStatus.FLUSH_SLAVE_TIMEOUT);
        assertNotNull(SendStatus.SLAVE_NOT_AVAILABLE);
    }
}
