
package com.bethel.model.studentbudget;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class TripDetails {

    @SerializedName("total_spent")
    @Expose
    private String totalSpent;

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

}
