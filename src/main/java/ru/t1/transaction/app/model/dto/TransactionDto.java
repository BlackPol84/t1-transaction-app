package ru.t1.transaction.app.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.t1.transaction.app.util.TransactionType;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDto {

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("client_id")
    private Long clientId;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("transaction_type")
    private TransactionType transactionType;
}
