package com.bethel.model;


public class UserDetailsModel {
    String id;
    String first_name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getTotal_spent() {
        return total_spent;
    }

    public void setTotal_spent(String total_spent) {
        this.total_spent = total_spent;
    }

    String middle_name;
    String last_name;
    String total_spent;

}
