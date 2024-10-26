package ru.t1.transaction.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.t1.transaction.app.kafka.TransactionKafkaProducer;
import ru.t1.transaction.app.model.dto.TransactionDto;
import ru.t1.transaction.app.model.TransactionType;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionKafkaProducer mockProducer;

    @InjectMocks
    private TransactionController transactionController;

    @Value("${spring.kafka.topic.client_transactions}")
    private String topic;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    @Test
    public void testSendTransaction_Success() throws Exception {

        TransactionDto requestTransaction = new TransactionDto();
        requestTransaction.setAmount(new BigDecimal("100.00"));
        requestTransaction.setClientId(1L);
        requestTransaction.setAccountId(1L);
        requestTransaction.setTransactionType(TransactionType.DEPOSIT);

        mockMvc.perform(post("/transactions/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestTransaction)))
                        .andExpect(status().isCreated())
                        .andExpect(MockMvcResultMatchers.content()
                        .string("Transaction processed successfully"));

        verify(mockProducer, times(1))
                .sendTo(topic, requestTransaction);
    }

    @Test
    public void testSendTransaction_Error() throws Exception {

        TransactionDto requestTransaction = new TransactionDto();
        requestTransaction.setAmount(new BigDecimal("100.00"));
        requestTransaction.setClientId(1L);
        requestTransaction.setAccountId(1L);
        requestTransaction.setTransactionType(TransactionType.DEPOSIT);

        doThrow(new RuntimeException("Kafka send error"))
                .when(mockProducer).sendTo(topic, requestTransaction);

        mockMvc.perform(post("/transactions/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestTransaction)))
                        .andExpect(status().isInternalServerError())
                        .andExpect(MockMvcResultMatchers.content()
                        .string("Failed to process transaction"));
    }

    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
