package com.bethel.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ReceiptModel implements Serializable {
    public com.bethel.model.Transaction getTransaction() {
        return Transaction;
    }

    public void setTransaction(com.bethel.model.Transaction transaction) {
        Transaction = transaction;
    }

    @SerializedName("Transaction")
    Transaction Transaction;

    public com.bethel.model.User getUser() {
        return User;
    }

    public void setUser(com.bethel.model.User user) {
        User = user;
    }

    @SerializedName("User")
    User User;

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    boolean isVisible;
}
