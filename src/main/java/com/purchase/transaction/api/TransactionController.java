package com.purchase.transaction.api;

import com.purchase.transaction.api.payload.TransactionPayload;
import com.purchase.transaction.api.response.TransactionResponse;
import com.purchase.transaction.entity.Transaction;
import com.purchase.transaction.exception.ExchangeRateNotFoundException;
import com.purchase.transaction.exception.TransactionNotFoundException;
import com.purchase.transaction.service.TransactionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;

@AllArgsConstructor
@RestController("V1/purchase/transaction")
@Validated
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private TransactionService transactionService;

    @PostMapping()
    @ApiOperation(value = "Create a transaction", notes = "Endpoint to create a new transaction")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Transaction created successfully"),
            @ApiResponse(code = 400, message = "Invalid request payload"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<String> createTransaction(@Valid @RequestBody TransactionPayload payload) {
        logger.info("Received request to create a transaction {}", payload);
        try {
            Transaction savedTransaction = transactionService.saveTransaction(payload);
            logger.info("Transaction {} saved successfully", savedTransaction.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body("Transaction " + savedTransaction.getId() + " saved successfully");
        } catch (Exception e) {
            logger.error("Failed to save transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save transaction");
        }
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Get transaction by ID", notes = "Endpoint to retrieve transaction details by ID")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Transaction not found"),
            @ApiResponse(code = 400, message = "Invalid currency or unsupported currency"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<?> getTransactionById(
            @PathVariable Long id,
            @RequestParam(name = "currency") String currency) {
        logger.info("Received request to get transaction by ID: {}", id);
        try {
            TransactionResponse response = transactionService.getTransactionById(id, currency);
            logger.info("Transaction retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (TransactionNotFoundException e) {
            logger.error("Transaction not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found");
        } catch (ExchangeRateNotFoundException e) {
            logger.error("Currency not supported", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Currency not supported");
        } catch (Exception e) {
            logger.error("An error occurred", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }
}