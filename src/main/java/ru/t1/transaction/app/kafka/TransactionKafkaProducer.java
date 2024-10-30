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

    private final KafkaTemplate<Long, TransactionDto> template;

    public void sendTo(String topic, TransactionDto transactionDto) {

        validateInputs(topic, transactionDto);

        try {
            template.send
                    (topic, transactionDto.getClientId(), transactionDto).get();
            template.flush();
            log.debug("Transaction has been sent");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void validateInputs(String topic, TransactionDto dto) {
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic must not be null or empty");
        }
        if (dto == null) {
            throw new IllegalArgumentException("TransactionDto must not be null");
        }
    }
}
