package ru.t1.transaction.app.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.t1.transaction.app.model.TransactionType;
import ru.t1.transaction.app.model.dto.TransactionDto;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionKafkaProducerTest {

    @Mock
    private KafkaTemplate<Long, TransactionDto> template;

    @InjectMocks
    private TransactionKafkaProducer producer;

    private final String topic = "t1_demo_client_transactions";

    @Test
    public void sendTo_whenSuccess_thenFlushCalled() {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAmount(new BigDecimal("100.00"));
        transactionDto.setClientId(1L);
        transactionDto.setAccountId(1L);
        transactionDto.setTransactionType(TransactionType.DEPOSIT);

        CompletableFuture<SendResult<Long, TransactionDto>> future = new CompletableFuture<>();
        future.complete(new SendResult<>(null, null));
        when(template.send(topic, transactionDto.getClientId(), transactionDto)).thenReturn(future);

        producer.sendTo(topic, transactionDto);

        verify(template, times(1))
                .send(topic, transactionDto.getClientId(), transactionDto);
        verify(template, times(1)).flush();
    }

    @Test
    public void sendTo_whenInvalidTopic_thenReturnException() {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAmount(new BigDecimal("100.00"));
        transactionDto.setClientId(1L);
        transactionDto.setAccountId(1L);
        transactionDto.setTransactionType(TransactionType.DEPOSIT);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> producer.sendTo(null, transactionDto));

        assertEquals("Topic must not be null or empty", exception.getMessage());
    }

    @Test
    public void sendTo_whenNullDto_thenReturnException() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> producer.sendTo(topic, null));

        assertEquals("TransactionDto must not be null", exception.getMessage());
    }

    @Test
    void sendTo_whenFailure_thenFlushNotCalled() {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAmount(new BigDecimal("100.00"));
        transactionDto.setClientId(1L);
        transactionDto.setAccountId(1L);
        transactionDto.setTransactionType(TransactionType.DEPOSIT);

        when(template.send(topic, transactionDto.getClientId(), transactionDto))
                .thenThrow(new RuntimeException("Kafka error"));

        producer.sendTo(topic, transactionDto);

        verify(template).send(topic, transactionDto.getClientId(), transactionDto);
        verify(template, never()).flush();
    }
}
