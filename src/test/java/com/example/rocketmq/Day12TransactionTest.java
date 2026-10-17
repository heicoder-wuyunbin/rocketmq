package com.example.rocketmq;

import com.example.rocketmq.service.TransactionOrderService;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day12TransactionTest {

    @Test
    void transactionListenerContract() {
        // 本地事务成功 → 提交；回查也成功 → 提交
        TransactionListener listener = new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        };
        assertEquals(LocalTransactionState.COMMIT_MESSAGE,
                TransactionOrderService.decideByLocalLog(true));
        assertEquals(LocalTransactionState.ROLLBACK_MESSAGE,
                TransactionOrderService.decideByLocalLog(false));
        System.out.println("[Day12] localLogSuccess=true  -> state=" + TransactionOrderService.decideByLocalLog(true)
                + " (提交消息)");
        System.out.println("[Day12] localLogSuccess=false -> state=" + TransactionOrderService.decideByLocalLog(false)
                + " (回滚消息)");
    }
}
