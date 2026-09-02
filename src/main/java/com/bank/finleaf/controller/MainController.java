package com.bank.finleaf.controller;

import com.bank.finleaf.model.Transaction;
import com.bank.finleaf.model.User;
import com.bank.finleaf.service.TransactionService;
import com.bank.finleaf.service.UserService;
import com.bank.finleaf.util.LoginResult;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final UserService userService;
    private final TransactionService transactionService;
    private final JdbcTemplate jdbcTemplate;
    private int counter = 0;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);

        List<Transaction> transactions = transactionService.recentTransactionLog(user.getId());
        model.addAttribute("transactions", transactions);

        String message = (String) session.getAttribute("message");
        if (message != null) {
            model.addAttribute("message", message);
            session.removeAttribute("message");
        }
        return "dashboard";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(String mobileNumber, String firstName, String lastName, String pin, HttpSession session) {
        userService.createUser(mobileNumber, firstName, lastName, pin);
        session.setAttribute("message", "Account successfully created!");

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        String message = (String) session.getAttribute("message");
        if (message != null) {
            model.addAttribute("message", message);
            session.removeAttribute("message");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(String mobileNumber, String pin, HttpSession session) {
        LoginResult result = userService.loginUser(mobileNumber, pin);

            switch (result.getStatus()) {
                case "NOT_FOUND": {
                    return "login";
                }
                case "SUCCESS": {
                    User user = result.getUser();

                    session.setAttribute("user", user);

                    counter = 0;
                    return "redirect:/dashboard";
                }
                case "INCORRECT": {
                    counter++;

                    if (counter >= 3) {
                        counter = 0;
                        return "forgotpassword";
                    } else {
                        return "login";
                    }
                }
            }
        return "login";
    }

    @GetMapping("/cashin")
    public String cashinPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);

        return "cashin";
    }

    @PostMapping("/cashin")
    public String cashin(double amount, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        String result = transactionService.cashIn(user.getMobileNumber(), amount);
            switch (result) {
                case "SUCCESS":
                    User updatedUser = userService.findUserByMobileNumber(user.getMobileNumber());
                    session.setAttribute("user", updatedUser);
                    session.setAttribute("message", "Cash successfully deposited!");
                    return "redirect:/dashboard";
                case "NOT_FOUND":
                    return "cashin";
                case "INVALID_AMOUNT":
                    return "cashin";
            }
        return "cashin";
    }

    @GetMapping("/cashtransfer")
    public String cashTransferPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);

        return "cashtransfer";
    }

    @PostMapping("/cashtransfer")
    public String cashTransfer(String mobileNumber, Double amount, HttpSession session, Model model) {
        User userSender = (User) session.getAttribute("user");
        String result = transactionService.cashTransfer(mobileNumber, amount, userSender.getId());
        switch (result) {
            case "SUCCESS":
                User updatedUser = userService.findUserByMobileNumber(userSender.getMobileNumber());
                session.setAttribute("user", updatedUser);
                session.setAttribute("message", "Cash successfully transferred!");
                return "redirect:/dashboard";
            case "NOT_FOUND":
                return "cashtransfer";
            case "INVALID_AMOUNT":
                return "cashtransfer";
            case "INSUFFICIENT_BALANCE":
                return "cashtransfer";
        }
        return "cashtransfer";
    }

    @PostMapping("/cashtransfer/review")
    public String cashTransferReview(String mobileNumber, Double amount,
                                     HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        User receiver = userService.findUserByMobileNumber(mobileNumber);

        model.addAttribute("user", user);
        model.addAttribute("receiver", receiver);
        model.addAttribute("mobileNumber", mobileNumber);
        model.addAttribute("amount", amount);

        return "cashtransfer";
    }

    @GetMapping("/transactions")
    public String transactionLogs(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        List<Transaction> transactions = transactionService.transactionLog(user.getId());
        model.addAttribute("transactions", transactions);
        return "transactions";
    }

    @PostMapping("/logout")
    public String logout() {
        return "login";
    }
}