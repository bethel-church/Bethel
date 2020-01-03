package com.bethel.constants;

import com.bethel.model.CurrencyModel1;

import java.util.ArrayList;

/**
 * Created by krishan on 27-09-2016.
 */

public interface ApiConstants {
    String SUCCESS = "success";
    //end points
    String GET_TIPS_LIST = "/get_trip_list";

    String TRIP_NAME = "trip.name";
    String TRIP_ID = "trip.id";
    String TRIP_USERNAME = "trip.username";
    String Budget_Total= "budget_total";
    String Budget_spent= "budget_spent";


    String PREF_LOGIN_MODEL = "UserModel";
    String USERTYPE_STUDENT = "0";
    String USERTYPE_LEADER = "1";

    String SELECTED_TRIP = "selected_trip";
    ArrayList<String> SELECTED_CURRENCYLIST = new ArrayList<>();
    ArrayList<CurrencyModel1> LEADER_SELECTED_CURRENCYLIST = new ArrayList<>();
}
