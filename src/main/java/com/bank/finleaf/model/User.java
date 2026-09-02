package com.bank.finleaf.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@AllArgsConstructor
@Data
public class User {
    private long id;
    private String mobileNumber;
    private String firstName;
    private String lastName;
    private double balance;
    private String pin;
}
