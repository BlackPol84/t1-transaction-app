package ru.t1.transaction.app.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.transaction.app.kafka.TransactionKafkaProducer;
import ru.t1.transaction.app.model.dto.TransactionDto;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/transactions")
public class TransactionController {

    @Value("${spring.kafka.topic.client_transactions}")
    private String topic;
    private final TransactionKafkaProducer producer;

    @PostMapping()
    public ResponseEntity<String> sendTransaction(@RequestBody TransactionDto requestTransaction) {
        try {
            producer.sendTo(topic, requestTransaction);
            log.info("Transaction sent successfully: {}", requestTransaction);
            return ResponseEntity.status(HttpStatus.CREATED).body("Transaction processed successfully");
        } catch (Exception e) {
            log.error("Error sending transaction: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process transaction");
        }
    }
}