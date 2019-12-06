package com.bethel.ui;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bethel.R;
import com.bethel.adapter.CategoryAdapter;
import com.bethel.adapter.CurrencyAdapter;
import com.bethel.api.ApiClient;
import com.bethel.api.CancelableCallback;
import com.bethel.base.BaseActivity;
import com.bethel.constants.ApiConstants;
import com.bethel.database.AppDatabase;
import com.bethel.interfaces.CategoryCallback;
import com.bethel.model.CurrenciesListModel;
import com.bethel.model.CurrencyModel;
import com.bethel.model.CurrencyModel1;
import com.bethel.model.ReceiptModel;
import com.bethel.model.UserModel;
import com.bethel.utils.MultiPartRequest;
import com.bethel.utils.MultiPartRequest.MultiPartRequestListener;
import com.bethel.utils.SharedPreferencesHandler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import vas.com.currencyconverter.CurrencyConverter;

import static com.bethel.utils.CommonUtils.isNetworkAvailable;
import static com.bethel.utils.CommonUtils.isNetworkAvailableWithNoDialog;
import static com.bethel.utils.CommonUtils.toast;

public class AddReceiptActivity extends BaseActivity implements CategoryCallback, MultiPartRequestListener {


    private CancelableCallback<UserModel> mCallBack;
    //  @InjectView(R.id.canceltv)
    TextView tvCancel;
    // @InjectView(R.id.catlistview)
    ListView lvCateogries;
    //@InjectView(R.id.catspinner)
    RelativeLayout sCategories;
    LinearLayout linearLayout;
    TextView currencyspinner;
    RelativeLayout rlDate;
    TextView tvDate;
    Calendar myCalendar2 = Calendar.getInstance();
    Button btnSave;
    EditText etPrice, etDesc;
    private boolean isEdit,isCursor;
    String strDate;
    ReceiptModel receiptModel;
    int sno=-1;
    Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_reciept_activity);



        tvCancel = (TextView) findViewById(R.id.canceltv);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEdit){
                    if(getIntent().getExtras().containsKey("isTripMember")) {

                        // startActivity(new Intent(AddReceiptActivity.this,ViewMembersTrips.class));
                    }else{
                        Intent intent=new Intent(AddReceiptActivity.this,ViewReceiptsActivity.class);
                        startActivity(intent);
                    }
                }
                finish();
            }
        });
        tvCateogry = (TextView) findViewById(R.id.catspinner);
        tvCurrency = (TextView) findViewById(R.id.currencyspinner);

        sCategories = (RelativeLayout) findViewById(R.id.catrl);
        sCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });
        rlDate = (RelativeLayout) findViewById(R.id.daterl);
        rlDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddReceiptActivity.this, R.style.datepicker, date2, myCalendar2
                        .get(Calendar.YEAR), myCalendar2.get(Calendar.MONTH),
                        myCalendar2.get(Calendar.DAY_OF_MONTH)).show();

            }
        });


        ((TextView)findViewById(R.id.retaketv)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(AddReceiptActivity.this,CameraActivity.class);
                intent.putExtra("isEditCamera",true);
                startActivityForResult(intent,100);
            }
        });

        tvDate = (TextView) findViewById(R.id.datetv);
        etDesc = (EditText) findViewById(R.id.descet);
        updateLabel();
        currencyspinner = (TextView) findViewById(R.id.currencyspinner);

        btnSave = (Button) findViewById(R.id.save);
        etPrice = (EditText) findViewById(R.id.priceet);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isImageLoaded) {
                    if (etPrice.getText().toString().equalsIgnoreCase("")
                            || strDate.toString().equalsIgnoreCase("") ||
                            category.toString().equalsIgnoreCase("") ||
                            currency.toString().equalsIgnoreCase("") ||
                            etDesc.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(AddReceiptActivity.this, "Please enter all fields", Toast.LENGTH_LONG).show();
                    } else {
                        addReceipt();
                    }
                }else{
                    Toast.makeText(AddReceiptActivity.this,"Please wait for image to load.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        ((ImageView) findViewById(R.id.recieptiv)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Convert to byte array
                isImageLoaded=false;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                byte[] byteArray = stream.toByteArray();
                if(((BitmapDrawable) ((ImageView) findViewById(R.id.recieptiv)).getDrawable()).getBitmap()!=null){
                    mergedBitmap = ((BitmapDrawable) ((ImageView) findViewById(R.id.recieptiv)).getDrawable()).getBitmap();
                    isImageLoaded=true;
                    Intent in1 = new Intent(AddReceiptActivity.this, PhotoViewerActivity.class);
                    //  in1.putExtra("image",byteArray);
                    startActivity(in1);
                }else{
                    isImageLoaded=false;
                    Toast.makeText(AddReceiptActivity.this,"Please wait for image to load.",Toast.LENGTH_SHORT).show();
                }


            }
        });




        mCallBack = new CancelableCallback<>(new Callback<UserModel>() {
            @Override
            public void success(UserModel userModel, Response response) {

                if (response.getStatus() == 200) {
                    Toast.makeText(AddReceiptActivity.this, "The receipt has been Successfully submitted.", Toast.LENGTH_LONG).show();
                    hideProgress();
                    finish();
                    //   Toast.makeText(AddReceiptActivity.this,"Success",Toast.LENGTH_LONG).show();
                }
               /* if (userModel.getStatus().equalsIgnoreCase(ApiConstants.SUCCESS)) {
//                    setRecyclerList(userModel.getTrips());
                    SharedPreferencesHandler.setStringValues(LoginActivity.this, ApiConstants.TRIP_ID, tripId);
                    SharedPreferencesHandler.writeObjectOnSharedPreference(LoginActivity.this,ApiConstants.PREF_LOGIN_MODEL,userModel);
                    if (userModel.getTrips().getUser().size() > 0) {
                        startActivity(SelectUserActivity.createIntent(LoginActivity.this, userModel.getTrips().getUser()));
                        finish();
                    }
                    else
                        toast("No user is registered in this Trip.");
                } else {
                    toast(userModel.getMessage());
                }*/
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                toast(getString(R.string.something_went));
            }
        });
        prepareCurrencyView();

        if(getIntent().getExtras()!=null) {
            if (getIntent().getExtras().containsKey("isEdit")) {

                if(getIntent().getExtras().getBoolean("isCursor")){
                    showProgress();
                    etDesc.setText(getIntent().getExtras().getString("desc"));
                    DecimalFormat f = new DecimalFormat("##.00");
                    etPrice.setText(f.format(Double.valueOf(getIntent().getExtras().getString("price"))));
                    tvCurrency.setText(getIntent().getExtras().getString("currency"));
                    tvCateogry.setText(getIntent().getExtras().getString("category"));
                    sno=getIntent().getExtras().getInt("sno");
                    strDate=getIntent().getExtras().getString("date");
                        tvDate.setText("" + parseTodaysDate(strDate));
                    category = getIntent().getExtras().getString("category");
                    if(getIntent().getExtras().containsKey("isUploaded")){
                        isUploaded=getIntent().getExtras().getBoolean("isUploaded");
                    }
//                    currency = cursor.getString(cursor.getColumnIndex("category"));

                    isEdit = true;
                    isCursor=true;
                    path=getIntent().getExtras().getString("imagepath");
                    File file = new File(path);
                    Uri uri = Uri.fromFile(file);
                    //imageView.setImageURI(uri);
                    File cacheDir = StorageUtils.getCacheDirectory(context);
                    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                            .build();
                    ImageLoader.getInstance().init(config);
                    ImageLoader.getInstance().displayImage(path
                            , ((ImageView) findViewById(R.id.recieptiv)), new ImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String s, View view) {

                                }

                                @Override
                                public void onLoadingFailed(String s, View view, FailReason failReason) {

                                }

                                @Override
                                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                                    if(bitmap!=null) {
                                        isImageLoaded=true;
                                        ((ImageView) findViewById(R.id.recieptiv)).setImageBitmap(bitmap);
                                        new SaveImageTask(bitmap).execute();
                                    }
                                    hideProgress();
                                }

                                @Override
                                public void onLoadingCancelled(String s, View view) {

                                }
                            });
                  /*  Glide.with(AddReceiptActivity.this)
                            .load(uri).asBitmap()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    ((ImageView) findViewById(R.id.recieptiv)).setImageBitmap(resource);
                                    hideProgress();
                                    new SaveImageTask(resource).execute();
                                }
                            });*/
                   /* if(isNetworkAvailableWithNoDialog(this)){
                        Glide.with(AddReceiptActivity.this)
                                .load(getIntent().getExtras().getString("imagepath")).asBitmap()
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                        ((ImageView) findViewById(R.id.recieptiv)).setImageBitmap(resource);
                                        hideProgress();
                                        new SaveImageTask(resource).execute();
                                    }
                                });
                    }else{
                        if(!getIntent().getExtras().getString("imagepath").contains("http")) {
                            Glide.with(AddReceiptActivity.this)
                                    .load(getIntent().getExtras().getString("imagepath")).asBitmap()
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                            ((ImageView) findViewById(R.id.recieptiv)).setImageBitmap(resource);
                                            hideProgress();
                                            new SaveImageTask(resource).execute();
                                        }
                                    });
                        }else{
                            hideProgress();
                        }
                    }*/
      hideProgress();

                }else {
                    showProgress();
                    if(getIntent().getExtras().containsKey("isUploaded")){

                        isUploaded=getIntent().getExtras().getBoolean("isUploaded");
                    }
                    receiptModel = (ReceiptModel) getIntent().getExtras().getSerializable("receiptmodel");
                    etDesc.setText(receiptModel.getTransaction().getDescription());
                   // NumberFormat currencyvalue = NumberFormat.getCurrencyInstance();
                    DecimalFormat f = new DecimalFormat("##.00");
                    etPrice.setText(f.format(Double.valueOf(receiptModel.getTransaction().getUsd())));
                    tvCurrency.setText(receiptModel.getTransaction().getForeign_currency());
                    tvCateogry.setText(receiptModel.getTransaction().getType());
                    String wsMyFormat = "MMM d, yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(wsMyFormat);
                    strDate=receiptModel.getTransaction().getReceipt_date();
                    tvDate.setText("" + parseTodaysDate(receiptModel.getTransaction().getReceipt_date()));
                   /* try {
                      //  tvDate.setText("" + sdf.parse(receiptModel.getTransaction().getReceipt_date()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }*/
                    category = receiptModel.getTransaction().getType();
                    currency = receiptModel.getTransaction().getForeign_currency();

                    isEdit = true;
                    if(!isNetworkAvailableWithNoDialog(this)){
                        Toast.makeText(this,"please check your internet connection",Toast.LENGTH_SHORT).show();
                    }
                    path=receiptModel.getTransaction().getReceipt();
                    File cacheDir = StorageUtils.getCacheDirectory(context);
                    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                            .build();
                    ImageLoader.getInstance().init(config);
                    ImageLoader.getInstance().displayImage(receiptModel.getTransaction().getReceipt()
                            , ((ImageView) findViewById(R.id.recieptiv)), new ImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String s, View view) {

                                }

                                @Override
                                public void onLoadingFailed(String s, View view, FailReason failReason) {

                                }

                                @Override
                                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                                    if(bitmap!=null) {
                                        isImageLoaded=true;
                                        ((ImageView) findViewById(R.id.recieptiv)).setImageBitmap(bitmap);
                                        new SaveImageTask(bitmap).execute();
                                    }
                                    hideProgress();
                                }

                                @Override
                                public void onLoadingCancelled(String s, View view) {

                                }
                            });
                   /* Glide.with(AddReceiptActivity.this)
                            .load(receiptModel.getTransaction().getReceipt()).asBitmap()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    if(resource!=null) {
                                        ((ImageView) findViewById(R.id.recieptiv)).setImageBitmap(resource);
                                        new SaveImageTask(resource).execute();
                                    }
                                    hideProgress();
                                }
                            });*/
                    hideProgress();
                }

            } else {
                isImageLoaded=true;
                pathArray = getIntent().getStringArrayListExtra("user");
                new MergeImageTask().execute();

            }
        }
        currencyspinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currencyDialog();
            }
        });

    }
boolean isImageLoaded;

    public  String parseTodaysDate(String time) {



        String inputPattern = "yyyy-MM-dd hh:mm";

        String outputPattern = "MMM d, yyyy";

   time=(time.replace("am","")).replace("pm","").toString().trim();
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.US);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);

            Log.i("mini", "Converted Date Today:" + str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }
    boolean isUploaded;

    static Bitmap mergedBitmap;

    @Override
    public void onBegin() {

    }


    @Override
    public void onBackPressed() {
        if(getIntent().getExtras().containsKey("isFromReceipt")){
            if(getIntent().getExtras().getBoolean("isFromReceipt")){
                if(getIntent().getExtras().containsKey("isTripMember")) {
//                    startActivity(new Intent(this,ViewMembersTrips.class));
                    finish();
                }else{
                    startActivity(new Intent(this,ViewReceiptsActivity.class));
                }

                finish();
            }
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onError()
    {

        Log.e("request result","error");
    }

    @Override
    public void onComplete(String response) {

       /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Receipt added successfully.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();*/
        hideProgress();
        HomeActivity.isRecieptAdded=true;
        if(getIntent().getExtras().containsKey("isFromReceipt")){
            if(getIntent().getExtras().getBoolean("isFromReceipt")){
                if(sno!=-1){
                    AppDatabase appDatabase=new AppDatabase(this);
                    appDatabase.deleteRowValues((sno));
                }
                if(!getIntent().getExtras().containsKey("isTripMember")) {
                    startActivity(new Intent(this,ViewReceiptsActivity.class));
                }else {
                    setResult(100);
                }

                finish();
            }
        }else {
            finish();
        }

//        Log.e("request result",""+response.toString());
    }


    class MergeImageTask extends AsyncTask<Void, Void, Void> {
        Bitmap mergedBitmap;


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgress();
            ((ImageView) findViewById(R.id.recieptiv)).setImageBitmap(mergedBitmap);
            new SaveImageTask(mergedBitmap).execute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mergedBitmap = createMergedImage();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }
    }


    private Bitmap createMergedImage() {

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        for(int i=0;i<pathArray.size();i++){
//          combineImages(BitmapFactory.decodeFile(pathArray.get(i), options),BitmapFactory.decodeFile(pathArray.get(i+1), options));
//        }

        Bitmap cs = null;
        int bitmapCount = pathArray.size();

        Bitmap bitmap = BitmapFactory.decodeFile(pathArray.get(0));

        //ByteArrayOutputStream out = new ByteArrayOutputStream();
        //bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        // Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));


        int canvasWidth = bitmap.getWidth();
        int canvasHeight = bitmap.getHeight() * bitmapCount;
        cs = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);


        Canvas comboImage = new Canvas(cs);


        comboImage.drawBitmap(bitmap, 0f, 0f, null);
        pathArray.remove(0);
        int yOffset = bitmap.getHeight();
        bitmap.recycle();
        for (String str : pathArray) {
            bitmap = BitmapFactory.decodeFile(str);
            comboImage.drawBitmap(bitmap, 0f, yOffset, null);
            yOffset += bitmap.getHeight();
            bitmap.recycle();
        }
        return Bitmap.createScaledBitmap(cs, 4096, 4096, true);

    }

    private ArrayList<String> pathArray;

    public Bitmap combineImages(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs = null;

        int width, height = 0;

        if (c.getWidth() > s.getWidth()) {
            width = c.getWidth();
            height = c.getHeight() + s.getHeight();
        } else {
            width = s.getWidth();
            height = c.getHeight() + s.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, 0f, c.getHeight(), null);

        // this is an extra bit I added, just incase you want to save the new image somewhere and then return the location
    /*String tmpImg = String.valueOf(System.currentTimeMillis()) + ".png";

    OutputStream os = null;
    try {
      os = new FileOutputStream(loc + tmpImg);
      cs.compress(CompressFormat.PNG, 100, os);
    } catch(IOException e) {
      Log.e("combineImages", "problem combining images", e);
    }*/

        return cs;
    }

    double price;

    @Override
    public int getLayout() {
        return R.layout.add_reciept_activity;
    }

    private void addReceipt() {
        if (isNetworkAvailableWithNoDialog(this)) {
            showProgress();
            CurrencyConverter.calculate(Double.parseDouble(etPrice.getText().toString()), currency, "USD", new CurrencyConverter.Callback() {
                @Override
                public void onValueCalculated(Double value, Exception e) {
                    if (e != null) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }else{
                        //Do someting with value.
                        price = value;
                        submitReceipt();
                    }
                }
            });

//            new HttpGetRequest().execute("https://finance.yahoo.com/webservice/v1/symbols/allcurrencies/quote?format=json");
        } else {
            AppDatabase appDatabase=new AppDatabase(AddReceiptActivity.this);
            if(sno!=-1){
                if(isUploaded){
                    appDatabase.updateUploadedTrip(strDate,
                            SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, ApiConstants.TRIP_ID),
                            currency + "",
                            category,
                            etDesc.getText().toString(),
                            etPrice.getText().toString(),
                            path,
                            SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "First_Name"),
                            SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "Middle_Name"),
                            SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "Last_Name"), sno);
                    if(getIntent().getExtras().containsKey("isFromReceipt")){
                        if(getIntent().getExtras().getBoolean("isFromReceipt")){
                            startActivity(new Intent(this,ViewReceiptsActivity.class));
                            finish();
                        }
                    }else {
                        finish();
                    }
                }else {
                    appDatabase.updateTrip(strDate,
                            SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, ApiConstants.TRIP_ID),
                            currency + "",
                            category,
                            etDesc.getText().toString(),
                            etPrice.getText().toString(),
                            path,
                            SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "First_Name"),
                            SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "Middle_Name"),
                            SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "Last_Name"), sno);
                    if(getIntent().getExtras().containsKey("isFromReceipt")){
                        if(getIntent().getExtras().getBoolean("isFromReceipt")){
                            startActivity(new Intent(this,ViewReceiptsActivity.class));
                            finish();
                        }
                    }else {
                        finish();
                    }
                }
            }else {
                appDatabase.insertJsonInStringDatabase(strDate,
                        SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, ApiConstants.TRIP_ID),
                        currency + "",
                        category,
                        etDesc.getText().toString(),
                        etPrice.getText().toString(),
                        path,
                        SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "First_Name"),
                        SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "Middle_Name"),
                        SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "Last_Name"));
                if(SharedPreferencesHandler.getBooleanValues(this,"saveconfirm")){
                    finish();
                }else {
                    startActivity(new Intent(this, SaveConfirmationActivity.class));
                    finish();
                }

            }
            //toast(getString(R.string.internet_not_available));

        }
    }


    private void submitReceipt() {
//        JsonObject jsonRequest = new JsonObject();
//Integer.valueOf(SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, ApiConstants.TRIP_ID))
        HashMap<String, Object> values = new HashMap<>();
        values.put("trip_id", SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, ApiConstants.TRIP_ID));
        if(price <= 0)
        {
            price = Double.valueOf(etPrice.getText().toString());
        }
        try {
            values.put("price_usd",  String.format("%.2f", price));
        } catch (Exception e) {
            values.put("price_usd", price);
        }
        values.put("currency", currency);
        values.put("type", category);
        values.put("description", etDesc.getText().toString());
        values.put("receipt_date", strDate);
        if(getIntent().getExtras().containsKey("isTripMember")) {
            values.put("first_name",getIntent().getExtras().getString("firstname") );
            values.put("middle_name", getIntent().getExtras().getString("middlename") );
            values.put("last_name", getIntent().getExtras().getString("lastname") );

        }else{
            values.put("first_name", SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "First_Name"));
            values.put("middle_name", SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "Middle_Name"));
            values.put("last_name", SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "Last_Name"));

        }


        values.put("price_other_currency", etPrice.getText().toString());
        /*values.put("trip_id", 74);
        values.put("price_usd", 34);
        values.put("currency", "");
        values.put("type", "Transportation");
        values.put("description", "bgjfg");
        values.put("receipt_date", "2016-11-06 06:41 PM");
        values.put("first_name", "Vinod");
        values.put("middle_name","KUMAR");
        values.put("last_name", "Sharma");
        values.put("price_other_currency", "");*/
        /*currency = "";
        description = bgjfg;
        "first_name" = Vinod;
        "last_name" = Sharma;
        "middle_name" = Kumar;
        "price_other_currency" = "";
        "price_usd" = 34;
        "receipt_date" = "2016-11-06 06:41 PM";
        "trip_id" = 74;
        type = Transportation;*/

        String url;
        if(isEdit) {
            if (isUploaded) {
                url = "http://betheltripreceipts.com/services/update_receipt";
                values.put("receipt_id", receiptModel.getTransaction().getId());
            } else {
                url = "http://betheltripreceipts.com/services/add_receipt";
            }
        }else{
            url = "http://betheltripreceipts.com/services/add_receipt";

        }

            /*if(!isCursor) {
                url = "http://betheltripreceipts.com/services/update_receipt";
                values.put("receipt_id", receiptModel.getTransaction().getId());
            }else{
                url="http://betheltripreceipts.com/services/add_receipt";
            }
        }else{
            url="http://betheltripreceipts.com/services/add_receipt";
        }*/
        hitMultipartRequest(AddReceiptActivity.this, url, this, values);
        //jsonRequest.addProperty("trip_id",  SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, ApiConstants.TRIP_ID));
        //jsonRequest.addProperty("price_usd", price);
//        jsonRequest.addProperty("currency", currency);
       /* jsonRequest.addProperty("type",   category);
        jsonRequest.addProperty("description", etDesc.getText().toString());
        jsonRequest.addProperty("receipt_date", strDate);
        jsonRequest.addProperty("first_name", SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "First_Name"));
        jsonRequest.addProperty("middle_name", SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "Last_Name"));
        jsonRequest.addProperty("last_name", SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, "Middle_Name"));
        jsonRequest.addProperty("price_other_currency", "");
       */ //ApiClient.getApiClient().addReceipt(jsonRequest, mCallBack);
    }

    DatePickerDialog.OnDateSetListener date2 = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar2.set(Calendar.YEAR, year);
            myCalendar2.set(Calendar.MONTH, monthOfYear);
            myCalendar2.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    private void updateLabel() {

        String myFormat = "yyyy-MM-dd hh:mm a"; //In which you need put here
        String wsMyFormat = "MMM d, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(wsMyFormat);
        SimpleDateFormat sdfMyFormat = new SimpleDateFormat(myFormat);

        tvDate.setText(sdf.format(myCalendar2.getTime()));
        strDate = sdfMyFormat.format(myCalendar2.getTime());
        Log.e("date", strDate);
    }


    private void prepareCurrencyView() {
        CurrencyModel1 defaultCurrencyModel = new CurrencyModel1();
        defaultCurrencyModel.setName("U.S Dollar");
        defaultCurrencyModel.setCode("USD");
        defaultCurrencyModel.setDisabled(true);

        if (ApiConstants.LEADER_SELECTED_CURRENCYLIST != null) {
            if (ApiConstants.LEADER_SELECTED_CURRENCYLIST.size() == 0) {
                ApiConstants.LEADER_SELECTED_CURRENCYLIST.add(0, defaultCurrencyModel);
            }
        }

        mGetCurrenciesCallBack = new CancelableCallback<>(new Callback<CurrencyModel>() {

            @Override
            public void success(CurrencyModel genricModel, Response response) {

                for (int i = 0; i < genricModel.getTrips().size(); i++) {
                    ApiConstants.SELECTED_CURRENCYLIST.add(genricModel.getTrips().get(i).getCurrency().getCurrency());
                }


                try{
                    ArrayList<CurrencyModel1> currencyModel1List = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(readFromAssets(AddReceiptActivity.this,"country_code.txt"));
                    if(jsonArray.length() > 0)
                    {
                        for(int count=0;count<jsonArray.length(); count++)
                        {
                            JSONObject jsonObject = jsonArray.getJSONObject(count);
                            CurrencyModel1 currencyModel1 = new CurrencyModel1();
                            String name = jsonObject.getString("name");
                            String code= jsonObject.getString("code");
                            for(int selectedCount=0 ; selectedCount<ApiConstants.SELECTED_CURRENCYLIST.size();selectedCount++)
                            {
                                if(code.equalsIgnoreCase(ApiConstants.SELECTED_CURRENCYLIST.get(selectedCount).toString()))
                                {
                                    currencyModel1.setChecked(true);
                                    currencyModel1.setCode(code);
                                    currencyModel1.setName(name);
                                    currencyModel1List.add(currencyModel1);
                                    break;
                                }
                            }

                        }
                        ApiConstants.LEADER_SELECTED_CURRENCYLIST.addAll(currencyModel1List);
                        mSelectedCurrencyModelList.addAll(ApiConstants.LEADER_SELECTED_CURRENCYLIST);
                        hideProgress();
//                setRecyclerList(currencyModel1List);
//                        hideProgress();

                    }
                }catch (JSONException e)
                {

                }catch (IOException e)
                {
                    e.printStackTrace();
                }




            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                toast(getString(R.string.something_went));
            }
        });


        addViews();
    }


    private List<CurrencyModel1> mSelectedCurrencyModelList;

    LayoutInflater mLayoutInflater;
    int temp;

    private void addViews() {
        mSelectedCurrencyModelList = new ArrayList<>();
     /*   if (ApiConstants.LEADER_SELECTED_CURRENCYLIST != null) {
            if (ApiConstants.LEADER_SELECTED_CURRENCYLIST.size() == 0 || ApiConstants.LEADER_SELECTED_CURRENCYLIST.size()==1) {

                getTripCurrencies();
            }else{
                mSelectedCurrencyModelList.addAll(ApiConstants.LEADER_SELECTED_CURRENCYLIST);
            }
        }*/
        mSelectedCurrencyModelList.addAll(ApiConstants.LEADER_SELECTED_CURRENCYLIST);

    }

    private void getTripCurrencies() {
        if (isNetworkAvailableWithNoDialog(this)) {
            showProgress();
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("trip_id", SharedPreferencesHandler.getStringValues(AddReceiptActivity.this, ApiConstants.TRIP_ID));

            ApiClient.getApiClient().getTripCurrencyList(jsonRequest, mGetCurrenciesCallBack);
        } else {
           // toast(getString(R.string.internet_not_available));
        }
    }

    TextView tvCurrency;
    private CancelableCallback<CurrencyModel> mGetCurrenciesCallBack;
    private void currencyDialog() {
        final Dialog alertDialog = new Dialog(this);
        alertDialog.setContentView(R.layout.currency_dialog);
//        ((TextView)alertDialog.findViewById(R.id.selecttv)).setText(Html.fromHtml(""));
        lvCateogries = (ListView) alertDialog.findViewById(R.id.currencylistview);
        ((Button) alertDialog.findViewById(R.id.savecategory)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(AddReceiptActivity.this,currency,Toast.LENGTH_LONG).show();
                alertDialog.dismiss();
            }
        });
        ((ImageView) alertDialog.findViewById(R.id.canceliv)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });


        for(int i=0;i<mSelectedCurrencyModelList.size();i++){
            if(currency.equalsIgnoreCase(mSelectedCurrencyModelList.get(i).getCode())){
                mSelectedCurrencyModelList.get(i).setChecked(true);
            }else{
                mSelectedCurrencyModelList.get(i).setChecked(false);
            }
        }
        CurrencyAdapter categoryAdapter = new CurrencyAdapter(this, mSelectedCurrencyModelList);
        lvCateogries.setAdapter(categoryAdapter);

        alertDialog.show();
    }


    /*@Override
    public int getLayout() {
        return R.layout.add_reciept_activity;
    }
*/
  /*  @OnClick({R.id.canceltv,R.id.catspinner})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.canceltv:
                finish();
                break;
            case R.id.catspinner:
                categoryDialog();
                break;
        }
    }
*/
    ArrayList<String> categories;
    TextView tvCateogry;

    private void categoryDialog() {
        final Dialog alertDialog = new Dialog(this);
        alertDialog.setContentView(R.layout.category_dialog);
        categories = new ArrayList<>();
        categories.add("Food");
        categories.add("Baggage/Visa/Departure Tax");
        categories.add("Airport Fees");
        categories.add("Transportation");
        categories.add("Lodging");
        categories.add("Supplies");
        categories.add("Missions $25 per person (Not 2nd Year)");
        categories.add("Gifts/Donations");
        categories.add("Other Expenses");
        lvCateogries = (ListView) alertDialog.findViewById(R.id.catlistview);
        ((Button) alertDialog.findViewById(R.id.savecategory)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(AddReceiptActivity.this,category,Toast.LENGTH_LONG).show();
                alertDialog.dismiss();
            }
        });
        ((ImageView) alertDialog.findViewById(R.id.canceliv)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });


        CategoryAdapter categoryAdapter = new CategoryAdapter(this, categories);
        lvCateogries.setAdapter(categoryAdapter);

        alertDialog.show();
    }

    String category="", currency = "USD";

    @Override
    public void onCategoryClick(int pos, boolean isCurrency) {
        if (isCurrency) {
            currency = mSelectedCurrencyModelList.get(pos).getCode();
            tvCurrency.setText(currency);
//            Toast.makeText(this,currency,Toast.LENGTH_LONG).show();
        } else {
            this.category = categories.get(pos);
            tvCateogry.setText(category);
//            Toast.makeText(this,category,Toast.LENGTH_LONG).show();
        }
    }


    class SaveImageTask extends AsyncTask<Byte, Void, String> {
        Bitmap bitmap;

        public SaveImageTask(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(isEdit){
                isImageLoaded=true;
                hideProgress();
            }
        }

        @Override
        protected String doInBackground(Byte... params) {

            File root = new File(Environment.getExternalStorageDirectory(), "Receipts");
            if (!root.exists()) {
                root.mkdirs();
            }
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Receipts/" +
                    System.currentTimeMillis() + ".jpg";
            File pictureFile = new File(path);
            if (pictureFile == null) {
                return null;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                fos.flush();
                fos.close();
                return path;
            } catch (FileNotFoundException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isEdit){

                hideProgress();
            }
        }
    }

    static String path;

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }


    public static void hitMultipartRequest(Context context, String url, MultiPartRequestListener receiver, HashMap<String, Object> bodyParams) {

        String fileExt = path.substring(path.lastIndexOf(".") + 1, path.toString().length());

        if (fileExt.equalsIgnoreCase("png")) {
            new MultiPartRequest(context, receiver, url,path, MultiPartRequest.MEDIA_TYPE_PNG, bodyParams).execute();
        } else if (fileExt.equalsIgnoreCase("jpg") || fileExt.equalsIgnoreCase("jpeg")) {
            new MultiPartRequest(context, receiver, url, path, MultiPartRequest.MEDIA_TYPE_JPEG, bodyParams).execute();
        }
    }

    public  String readFromAssets(Context context, String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

        // do reading, usually loop until end of file reading
        StringBuilder sb = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine); // process line
            mLine = reader.readLine();
        }
        reader.close();
        return sb.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100){
            isImageLoaded=true;
            pathArray = (ArrayList<String>) data.getExtras().getSerializable("imagepath");
            new MergeImageTask().execute();
        }
    }


    private class HttpGetRequest extends AsyncTask<String, Void, String> {
        private static final String REQUEST_METHOD = "GET";
        private static final int READ_TIMEOUT = 15000;
        private static final int CONNECTION_TIMEOUT = 15000;
        @Override
        protected String doInBackground(String... params){
            String stringUrl = params[0];
            String result;
            String inputLine;
            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection =(HttpURLConnection)
                        myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //Connect to our url
                connection.connect();
                //Create a new InputStreamReader
                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                //Set our result equal to our stringBuilder
                result = stringBuilder.toString();
            }
            catch(IOException e){
                e.printStackTrace();
                result = null;
            }
            return result;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            try {
                JSONObject jObj = new JSONObject(result);
                JSONObject list = jObj.getJSONObject("list");
                JSONArray resources = list.getJSONArray("resources");

                for(int i=0;i<resources.length();i++)
                {
                    JSONObject inner = resources.getJSONObject(i);
                    JSONObject resource = inner.getJSONObject("resource");
                    JSONObject fields = resource.getJSONObject("fields");
                    String name = fields.getString("name");
                    String _price = fields.getString("price");

                    if (name.equalsIgnoreCase("USD/"+currency)) {
                        Double currencyPrice =
                                Double.valueOf(_price);
                        price = Double.valueOf(etPrice.getText().toString()) / currencyPrice;

                        break;
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }



            submitReceipt();

        }
    }




}

