package com.bank.finleaf.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Time;
import java.util.Date;

@AllArgsConstructor
@Data
public class Transaction {
    private long id;
    private long userId;
    private String type;
    private double amount;
    private String details;
    private Date date;
    private Time time;
}
