package majorproject.maf.service;

import majorproject.maf.dto.TransactionDto;
import majorproject.maf.model.Transaction;
import majorproject.maf.model.User;
import org.springframework.stereotype.Service;
import majorproject.maf.repository.*;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;

    public TransactionService(TransactionRepository transactionRepo, UserRepository userRepo) {
        this.transactionRepo = transactionRepo;
        this.userRepo = userRepo;
    }

    public List<TransactionDto> getUserTransactions(String email) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return transactionRepo.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(txn -> new TransactionDto(
                        txn.getId(),
                        txn.getType(),
                        txn.getAsset(),
                        txn.getAssetQuantity(),
                        txn.getAmount(),
                        txn.getCreatedAt()
                ))
                .toList();
    }

    public Transaction createTransaction(Transaction transaction) {
        return transactionRepo.save(transaction);
    }
}
