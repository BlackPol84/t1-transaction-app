package ru.t1.transaction.app.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.t1.transaction.app.model.TransactionType;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDto {

    @JsonProperty("amount")
    @NotNull(message = "Amount should not be null")
    private BigDecimal amount;

    @JsonProperty("client_id")
    @NotNull(message = "Client ID should not be null")
    private Long clientId;

    @JsonProperty("account_id")
    @NotNull(message = "Account ID should not be null")
    private Long accountId;

    @JsonProperty("transaction_type")
    @NotNull(message = "Transaction type should not be null")
    private TransactionType transactionType;
}
