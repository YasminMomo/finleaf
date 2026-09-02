package com.bank.finleaf.util;

import com.bank.finleaf.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LoginResult {
    private String status;
    private User user;
}
