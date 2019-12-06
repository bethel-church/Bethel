
package com.bethel.model.studentbudget;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class StudentBudgetDetails {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("trip_details")
    @Expose
    private TripDetails tripDetails;

    /**
     * 
     * @return
     *     The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     *     The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 
     * @return
     *     The tripDetails
     */
    public TripDetails getTripDetails() {
        return tripDetails;
    }

    /**
     * 
     * @param tripDetails
     *     The trip_details
     */
    public void setTripDetails(TripDetails tripDetails) {
        this.tripDetails = tripDetails;
    }

}
