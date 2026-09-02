package com.bank.finleaf.service;

import com.bank.finleaf.model.User;
import com.bank.finleaf.util.LoginResult;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    //SignUp
    public void createUser(String mobileNumber, String firstName, String lastName, String pin) {
        String hashedPin = passwordEncoder.encode(pin);

        jdbcTemplate.update("INSERT INTO user (mobileNumber, firstName, lastName, balance, pin) VALUES (?, ?, ?, ?, ?)",
                mobileNumber, firstName, lastName, 0, hashedPin);
    }

    //Find User by Mobile Number
    public User findUserByMobileNumber(String mobileNumber) {
        List<User> users = jdbcTemplate.query("SELECT * FROM user WHERE mobileNumber = ?", (rs, rowNum) -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getDouble(5),
                rs.getString(6)), mobileNumber);

        return users.isEmpty() ? null : users.get(0);
    }

    //Find User by ID
    public User findUserById(long id) {
        List<User> users = jdbcTemplate.query("SELECT * FROM user WHERE id = ?", (rs, rowNum) -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getDouble(5),
                rs.getString(6)), id);

        return users.isEmpty() ? null : users.get(0);
    }

    //Login
    public LoginResult loginUser(String mobileNumber, String pin) {
        List<User> users = jdbcTemplate.query("SELECT * FROM user WHERE mobileNumber = ?", (rs, rowNum) -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getDouble(5),
                rs.getString(6)), mobileNumber);

        if (users.isEmpty()) {
            return new  LoginResult("NOT_FOUND", null);
        }

        User user = users.get(0);

        if (passwordEncoder.matches(pin, user.getPin())) {
            return new LoginResult("SUCCESS", user);
        } else {
            return new LoginResult("INCORRECT", null);
        }
    }
}
