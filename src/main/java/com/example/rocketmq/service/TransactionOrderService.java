package com.example.rocketmq.service;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Day 12：事务消息。
 * 半消息（Half）对消费者不可见 → 执行本地事务 → 根据结果 Commit/Rollback；
 * 若 Broker 长时间未收到确认，会回调 checkLocalTransaction 回查。
 */
public class TransactionOrderService {

    public static final String NAME_SERVER = "127.0.0.1:9876";

    private final TransactionMQProducer producer;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public TransactionOrderService(String group, TransactionListener listener) throws Exception {
        producer = new TransactionMQProducer(group);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.setExecutorService(executor);
        producer.setTransactionListener(listener);
        producer.start();
    }

    /** 发送事务消息，arg 透传给本地事务/回查 */
    public void sendOrderWithTransaction(String topic, String orderId, String body, Object arg)
            throws Exception {
        Message msg = new Message(topic, "tx", orderId, body.getBytes(StandardCharsets.UTF_8));
        producer.sendMessageInTransaction(msg, arg);
    }

    /** 标准回查实现模板：用本地事务日志判断最终状态 */
    public static LocalTransactionState decideByLocalLog(boolean localSuccess) {
        return localSuccess ? LocalTransactionState.COMMIT_MESSAGE : LocalTransactionState.ROLLBACK_MESSAGE;
    }

    public void close() {
        producer.shutdown();
        executor.shutdown();
    }
}
