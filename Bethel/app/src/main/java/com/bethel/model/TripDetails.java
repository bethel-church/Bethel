
package com.bethel.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class TripDetails {

    @SerializedName("total_spent")
    @Expose
    private String totalSpent;
    @SerializedName("budget")
    @Expose
    private String budget;

    /**
     * 
     * @return
     *     The totalSpent
     */
    public String getTotalSpent() {
        return totalSpent;
    }

    /**
     * 
     * @param totalSpent
     *     The total_spent
     */
    public void setTotalSpent(String totalSpent) {
        this.totalSpent = totalSpent;
    }

    /**
     * 
     * @return
     *     The budget
     */
    public String getBudget() {
        return budget;
    }

    /**
     * 
     * @param budget
     *     The budget
     */
    public void setBudget(String budget) {
        this.budget = budget;
    }

}
