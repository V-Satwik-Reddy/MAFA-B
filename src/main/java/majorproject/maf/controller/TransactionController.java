package majorproject.maf.controller;

import majorproject.maf.dto.TransactionDto;
import majorproject.maf.model.Transaction;
import majorproject.maf.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TransactionController {

    TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(Authentication auth) {

        String email = auth.getName(); // from JWT
        List<TransactionDto> txns = transactionService.getUserTransactions(email);
        return ResponseEntity.ok(txns);
    }

}
