package ru.t1.transaction.app.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.transaction.app.model.dto.TransactionDto;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionKafkaProducer {

    private final KafkaTemplate<String, TransactionDto> transactionKafkaTemplate;

    public void sendTo(String topic, TransactionDto transactionDto) {
        try {
            transactionKafkaTemplate.send(topic, transactionDto).get();
            transactionKafkaTemplate.flush();
            log.debug("Transaction has been sent");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
