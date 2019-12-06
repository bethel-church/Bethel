package com.bethel.model;

/**
 * Created by kuljeetsingh on 10/2/16.
 */

public class GenricModel {
    /**
     * status : success
     * message : Budget set successfully
     */

    private String status;
    private String message;

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
