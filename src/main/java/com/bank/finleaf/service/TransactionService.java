package com.bank.finleaf.service;

import com.bank.finleaf.model.Transaction;
import com.bank.finleaf.model.User;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
public class TransactionService {
    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;

    // Cash In
    public String cashIn(String mobileNumber, double amount) {
        User user = userService.findUserByMobileNumber(mobileNumber);

        if (amount <= 0) {
            return "INVALID_AMOUNT";
        }

        if (user == null) {
            return "NOT_FOUND";
        }

        double saveBalance = user.getBalance();
        saveBalance += amount;
        jdbcTemplate.update("UPDATE user SET balance = ? WHERE id = ?", saveBalance, user.getId());
        jdbcTemplate.update("INSERT INTO transaction (userId, type, amount, details, date, time) VALUES (?, ?, ?, ?, ?, ?)", user.getId(), "Cash In", amount, "Cash Deposit", LocalDate.now(), LocalTime.now());
        return "SUCCESS";
    }

    public String cashTransfer(String mobileNumber, double amount, long senderId) {
        User receiverUser = userService.findUserByMobileNumber(mobileNumber);
        User senderUser = userService.findUserById(senderId);

        if (amount <= 0) {
            return "INVALID_AMOUNT";
        }

        if (receiverUser == null || senderUser == null) {
            return "NOT_FOUND";
        }

        if (senderUser.getBalance() < amount) {
            return "INSUFFICIENT_BALANCE";
        }

        double saveBalanceReceiver = receiverUser.getBalance();
        double saveBalanceSender = senderUser.getBalance();

        saveBalanceReceiver += amount;
        saveBalanceSender -= amount;

        jdbcTemplate.update("UPDATE user SET balance = ? WHERE id = ?", saveBalanceReceiver, receiverUser.getId());
        jdbcTemplate.update("UPDATE user SET balance = ? WHERE id = ?", saveBalanceSender, senderUser.getId());

        jdbcTemplate.update("INSERT INTO transaction (userId, type, amount, details, date, time) VALUES (?, ?, ?, ?, ?, ?)", receiverUser.getId(), "Cash Transfer", amount, "Received cash from " + senderUser.getFirstName() + " " + senderUser.getLastName(), LocalDate.now(), LocalTime.now());
        jdbcTemplate.update("INSERT INTO transaction (userId, type, amount, details, date, time) VALUES (?, ?, ?, ?, ?, ?)", senderUser.getId(), "Cash Transfer", -amount, "Sent cash to " + receiverUser.getFirstName() + " " + receiverUser.getLastName(), LocalDate.now(), LocalTime.now());
        return "SUCCESS";
    }

    public List<Transaction> transactionLog(long id) {
        List<Transaction> transactions = jdbcTemplate.query("SELECT * FROM transaction WHERE userId = ?", (rs, rowNum) -> new Transaction(
           rs.getLong(1),
           rs.getLong(2),
           rs.getString(3),
           rs.getDouble(4),
           rs.getString(5),
           rs.getDate(6),
           rs.getTime(7)), id);

        return transactions;
    }

    public List<Transaction> recentTransactionLog(long id) {
        List<Transaction> transactions = jdbcTemplate.query("SELECT * FROM transaction WHERE userId = ? ORDER BY date DESC, time DESC LIMIT 5", (rs, rowNum) -> new Transaction(
                rs.getLong(1),
                rs.getLong(2),
                rs.getString(3),
                rs.getDouble(4),
                rs.getString(5),
                rs.getDate(6),
                rs.getTime(7)), id);

        return transactions;
    }
}
