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
            System.out.println("[Day02] sendStatus=" + r.getSendStatus()
                    + ", msgId=" + r.getMsgId()
                    + ", offsetMsgId=" + r.getOffsetMsgId()
                    + ", queue=" + r.getMessageQueue()
                    + ", queueOffset=" + r.getQueueOffset());
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

        System.out.println("[Day02] SendStatus 枚举值一览：");
        System.out.println("  - SEND_OK            : 发送成功，刷盘/同步从节点均达标");
        System.out.println("  - FLUSH_DISK_TIMEOUT : 刷盘超时（Broker 未在超时时间内完成落盘）");
        System.out.println("  - FLUSH_SLAVE_TIMEOUT: 同步从节点超时（主从同步未在超时时间内完成）");
        System.out.println("  - SLAVE_NOT_AVAILABLE: 从节点不可用（未配置或未存活）");
        System.out.println("[Day02] 实际取值：SEND_OK=" + SendStatus.SEND_OK
                + ", FLUSH_DISK_TIMEOUT=" + SendStatus.FLUSH_DISK_TIMEOUT
                + ", FLUSH_SLAVE_TIMEOUT=" + SendStatus.FLUSH_SLAVE_TIMEOUT
                + ", SLAVE_NOT_AVAILABLE=" + SendStatus.SLAVE_NOT_AVAILABLE);
    }
}
