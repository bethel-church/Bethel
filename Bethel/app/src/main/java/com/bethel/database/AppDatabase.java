package com.bethel.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;


import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppDatabase extends SQLiteOpenHelper {

    // Database Name
    private static final String DATABASE_NAME = "bethel";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database table name
    private static final String TABLE = "betheltrips";
    private static final String SELECTED_CURRENCY_TABLE = "CurrencyTable";
    private static final String UPLOADED_TABLE = "uploadedtrips";

    //table cols.
    private static final String ROW = "trip_id";
    private static final String JSON = "imagepath";
    private static final String IMAGEPATH = "imagepath";
    private static final String DATE = "date";
    private static final String PRICE = "price";
    private static final String CATEGORY = "category";
    private static final String CURRENCY = "currency";
    private static final String DECRIPTION = "description";
    private static final String TYPE = "type";
    private static final String FIRSTNAME = "firstname";
    private static final String MIDDLENAME = "middlename";
    private static final String LASTNAME = "lastname";
    private static final String LASTUPDATED = "lastupdated";
    private static final String SNO = "sno";
    private static final String ID = "id";


    public AppDatabase(Context context) {
/*
        super(context, Environment.getExternalStorageDirectory()
                + File.separator + "BethelDb"
                + File.separator + DATABASE_NAME, null, DATABASE_VERSION);*/
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE + "("
                + ROW + " TEXT,"
                + IMAGEPATH + " TEXT,"
                + SNO + " INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 1,"
                + TYPE + " TEXT,"
                + PRICE + " TEXT,"
                + CATEGORY + " TEXT,"
                + DECRIPTION + " TEXT,"
                + CURRENCY + " TEXT,"
                + FIRSTNAME + " TEXT,"
                + MIDDLENAME + " TEXT,"
                + LASTNAME + " TEXT,"
                + DATE+ " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

        String CREATE_UPLOADED_TRIPS_TABLE = "CREATE TABLE " + UPLOADED_TABLE + "("
                + ROW + " TEXT,"
                + IMAGEPATH + " TEXT,"
                + SNO + " INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 1,"
                + TYPE + " TEXT,"
                + PRICE + " TEXT,"
                + CATEGORY + " TEXT,"
                + DECRIPTION + " TEXT,"
                + CURRENCY + " TEXT,"
                + FIRSTNAME + " TEXT,"
                + MIDDLENAME + " TEXT,"
                + LASTNAME + " TEXT,"
                + ID + " TEXT,"
                + DATE+ " TEXT" + ")";



        db.execSQL(CREATE_UPLOADED_TRIPS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        // Create tables again
        onCreate(db);
    }

    public void insertJsonInStringDatabase(String date,
                                           String row,
                                           String currency,
                                           String category,
                                           String description,
                                           String price,
                                           String imagepath,
                                           String firstname,
                                           String middlename,
                                           String lastname) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ROW, row);
        values.put(DATE, date);
        values.put(CATEGORY, category);
        values.put(CURRENCY, currency);
        values.put(DECRIPTION, description);
        values.put(PRICE, price);
        values.put(IMAGEPATH, imagepath);
        values.put(FIRSTNAME, firstname);
        values.put(MIDDLENAME, middlename);
        values.put(LASTNAME, lastname);

        // Inserting Row
        db.insert(TABLE, null, values);
        // Closing database connection
        db.close();
    }





    public void insertDataInStringDatabase(String date,
                                           String row,
                                           String currency,
                                           String category,
                                           String description,
                                           String price,
                                           String imagepath,
                                           String firstname,
                                           String middlename,
                                           String lastname,String id) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ROW, row);
        values.put(DATE, date);
        values.put(ID, id);

        values.put(CATEGORY, category);
        values.put(CURRENCY, currency);
        values.put(DECRIPTION, description);
        values.put(PRICE, price);
        values.put(IMAGEPATH, imagepath);
        values.put(FIRSTNAME, firstname);
        values.put(MIDDLENAME, middlename);
        values.put(LASTNAME, lastname);

        // Inserting Row
        db.insert(UPLOADED_TABLE, null, values);
        // Closing database connection
        db.close();
    }

    public void updateTrip(String date,
                                           String row,
                                           String currency,
                                           String category,
                                           String description,
                                           String price,
                                           String imagepath,
                                           String firstname,
                                           String middlename,
                                           String lastname,int sno) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ROW, row);
        values.put(DATE, date);
        values.put(CATEGORY, category);
        values.put(CURRENCY, currency);
        values.put(DECRIPTION, description);
        values.put(PRICE, price);
        values.put(IMAGEPATH, imagepath);
        values.put(FIRSTNAME, firstname);
        values.put(MIDDLENAME, middlename);
        values.put(LASTNAME, lastname);

        // Inserting Row
          db.update(TABLE, values, SNO + " = ?",
                new String[] { String.valueOf(sno) });
        // Closing database connection

    }

    public void updateUploadedTrip(String date,
                           String row,
                           String currency,
                           String category,
                           String description,
                           String price,
                           String imagepath,
                           String firstname,
                           String middlename,
                           String lastname,int sno) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ROW, row);
        values.put(DATE, date);
        values.put(CATEGORY, category);
        values.put(CURRENCY, currency);
        values.put(DECRIPTION, description);
        values.put(PRICE, price);
        values.put(IMAGEPATH, imagepath);
        values.put(FIRSTNAME, firstname);
        values.put(MIDDLENAME, middlename);
        values.put(LASTNAME, lastname);

        // Inserting Row
        db.update(UPLOADED_TABLE, values, SNO + " = ?",
                new String[] { String.valueOf(sno) });
        // Closing database connection

    }


    public void insertJsonInDatabase(String json, int row, String type) {
        if (json.isEmpty()) {
            return;
        }
        if (getCount(type) > 0) {
            updateContact(json,row);
        } else {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ROW, row);
            values.put(TYPE, type);
            values.put(JSON, json);
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            values.put(LASTUPDATED, dateFormat.format(date));
            // Inserting Row
            db.insert(TABLE, null, values);
            // Closing database connection
            db.close();
        }
    }

    public int updateContact(String feedJson, int row) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(JSON, feedJson);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        values.put(LASTUPDATED, dateFormat.format(date));
        // updating row
        return db.update(TABLE, values, ROW + " = ?",
                new String[] { String.valueOf(row) });
    }
    public int updateContact(String feedJson, String row) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(JSON, feedJson);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        values.put(LASTUPDATED, dateFormat.format(date));
        // updating row
        return db.update(TABLE, values, ROW + " = ?",
                new String[] { String.valueOf(row) });

    }

 /*   // Getting feed
    public String getJson(String row) {
        String selectQuery = "SELECT * FROM " + TABLE + " WHERE " + ROW + "='" + row + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String feedJson = AppConstants.DEFAULT;
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndex(JSON)) != null
                        && cursor.getString(cursor.getColumnIndex(JSON)).length() > 0) {
                    feedJson = cursor.getString(cursor.getColumnIndex(JSON));
                }
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        // return contact
        return feedJson;
    }
*/
   /* // Getting feed
    public String getUpdatedDate(String row) {
        String selectQuery = "SELECT * FROM " + TABLE + " WHERE " + ROW + "='" + row + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String lastUpdatedDate = AppConstants.DEFAULT;
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndex(LASTUPDATED)) != null
                        && cursor.getString(cursor.getColumnIndex(LASTUPDATED)).length() > 0) {
                    lastUpdatedDate = cursor.getString(cursor.getColumnIndex(LASTUPDATED));
                }
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        // return contact
        return lastUpdatedDate;
    }

*/

    // Getting contacts Count
    public int getCount(String type) {
        int count;
        String selectQuery = "SELECT * FROM " + TABLE + " WHERE " + ROW + "='" + type + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        count = cursor.getCount();
        db.close();
        cursor.close();
        return count;
    }


    // Getting contacts Count
    public Cursor getRowCount(String row) {
        int count;
        String selectQuery = "SELECT * FROM " + TABLE + " WHERE " + ROW + "='" + row + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        count = cursor.getCount();
        db.close();
        return cursor;
    }


    // Getting contacts Count
    public Cursor getUploadedRowCount(String row,String cat) {
      /*  int count;
        String selectQuery;
        if(cat.equalsIgnoreCase("")|| cat.equalsIgnoreCase("All")){
            selectQuery = "SELECT * FROM " + UPLOADED_TABLE + " WHERE " + ROW + "='" + row+ "'";
        }else{
            selectQuery = "SELECT * FROM " + UPLOADED_TABLE + " WHERE " + ROW + "='" + row + "' and "
                    + CATEGORY+ "='" + cat + "'";
            //  select * from table where row='abc' & type='xyz'
        }
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        count = cursor.getCount();*/
        int count;
        String selectQuery;
            selectQuery = "SELECT * FROM " + UPLOADED_TABLE;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        count = cursor.getCount();
        db.close();
        return cursor;
    }


    //delete values from table
    public void deleteValues(String type){
        SQLiteDatabase db = getWritableDatabase();
        String deleteQuery = "DELETE FROM " + TABLE + " WHERE " + TYPE + "='" + type + "'";
        db.execSQL(deleteQuery);
        db.close();
    }

    //delete values from table
    public void deleteRowValues(int row){
        try {
            SQLiteDatabase db = getWritableDatabase();
            String deleteQuery = "DELETE FROM " + TABLE + " WHERE " + SNO + "='" + row + "'";
            db.execSQL(deleteQuery);
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //delete values from table
    public void deleteUploadedRowValues(String row){
        SQLiteDatabase db = getWritableDatabase();
        String deleteQuery = "DELETE FROM " + UPLOADED_TABLE + " WHERE " + SNO + "='" + row + "'";
        db.execSQL(deleteQuery);
        db.close();
    }

    public void clearUploadedRowValues(){
        SQLiteDatabase db = getWritableDatabase();
        String deleteQuery = "DELETE FROM " + UPLOADED_TABLE ;
        db.execSQL(deleteQuery);
        db.close();
    }

    public void clearSavedTable(){
        SQLiteDatabase db = getWritableDatabase();
        String deleteQuery = "DELETE FROM " + TABLE ;
        db.execSQL(deleteQuery);
        db.close();
    }



}
