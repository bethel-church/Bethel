package com.bethel.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {
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

    @SerializedName("first_name")
    String first_name;
    @SerializedName("middle_name")
    String   middle_name;
    @SerializedName("last_name")
    String  last_name;
}
