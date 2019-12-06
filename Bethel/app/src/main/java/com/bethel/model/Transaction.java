package com.bethel.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Transaction implements Serializable{
    public int getSno() {
        return sno;
    }

    public void setSno(int sno) {
        this.sno = sno;
    }

    private int sno;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsd() {
        return usd;
    }

    public void setUsd(String usd) {
        this.usd = usd;
    }

    public String getForeign_currency_amount() {
        return foreign_currency_amount;
    }

    public void setForeign_currency_amount(String foreign_currency_amount) {
        this.foreign_currency_amount = foreign_currency_amount;
    }

    public String getForeign_currency() {
        return foreign_currency;
    }

    public void setForeign_currency(String foreign_currency) {
        this.foreign_currency = foreign_currency;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public String getIs_edited() {
        return is_edited;
    }

    public void setIs_edited(String is_edited) {
        this.is_edited = is_edited;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getReceipt_date() {
        return receipt_date;
    }

    public void setReceipt_date(String receipt_date) {
        this.receipt_date = receipt_date;
    }

    @SerializedName("id")
    String id;
    @SerializedName("trip_id")
    String  trip_id;
    @SerializedName("user_id")
    String  user_id;
    @SerializedName("type")
    String  type;
    @SerializedName("description")
    String description;
    @SerializedName("usd")
    String  usd;
    @SerializedName("foreign_currency_amount")
    String  foreign_currency_amount;
    @SerializedName("foreign_currency")
    String  foreign_currency;
    @SerializedName("receipt")
    String  receipt;
    @SerializedName("is_edited")
    String  is_edited;
    @SerializedName("created")
    String  created;
    @SerializedName("receipt_date")
    String  receipt_date;
}
