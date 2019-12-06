package com.bethel.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ReceiptDetails implements Serializable{
 public String getStatus() {
  return status;
 }

 public void setStatus(String status) {
  this.status = status;
 }

 public ArrayList<ReceiptModel> getReceipts() {
  return receipts;
 }

 public void setReceipts(ArrayList<ReceiptModel> receipts) {
  this.receipts = receipts;
 }

 @SerializedName("status")
String status;
 @SerializedName("receipts")
 ArrayList<ReceiptModel>receipts;
}
