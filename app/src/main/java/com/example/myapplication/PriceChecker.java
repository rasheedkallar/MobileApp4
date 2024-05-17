package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.Activity.Item;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupBase;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.function.Function;

public class PriceChecker extends BaseActivity {

    private Bitmap image;

    private TextView Barcode;

    private TextView Description ;
    private TextView Rate ;

    private ClearTimer Timer;

    private  MonitorTimer TimerCheckup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_checker);
        ImageView imageView =  findViewById(R.id.image);
        new DataService().get("refFile?imagePath=~\\Images\\PriceChecker\\Sample1.jpg" , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                Bitmap bmp = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                imageView.setImageBitmap(bmp);
            }
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

        Barcode = (TextView)findViewById(R.id.barcode);

        Description = (TextView)findViewById(R.id.description);
        Rate = (TextView)findViewById(R.id.price);

        Timer = new ClearTimer(this);
        Timer.start();

        TimerCheckup = new MonitorTimer(this);
        TimerCheckup.start();

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Timer.cancel();
        Timer.start();
        Description.setText("");
        Rate.setText("");

        char unicodeChar = (char) event.getUnicodeChar();
        if(unicodeChar == '\n'){
            try {
                JSONObject param = new JSONObject();
                param.put("param1","PriceChecker");
                param.put("param2",Barcode.getText().toString().trim());
                new DataService().postForExecuteList("sp_DataInspection", param, jsonArray -> {
                    Timer.cancel();
                    Timer.start();

                    if (jsonArray.length() == 0) {
                        Description.setText("Item not found");
                    }
                    else{
                        try {
                            JSONObject obj = (JSONObject) jsonArray.get(0);
                            Description.setText(obj.getString("Description"));
                            DecimalFormat df = new DecimalFormat("0.00");
                            Rate.setText("Price: " + df.format(obj.getDouble("Value1")) + " AED");

                        }
                        catch (JSONException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    return null;
                }, s -> {
                    System.out.println(s);
                    return null;
                });
            } catch (JSONException e) {
                System.out.println(e.getMessage());
            }
            Barcode.setText("");
        }
        else {
            Barcode.setText(Barcode.getText().toString() + unicodeChar);
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MonitorTimer extends CountDownTimer {

        public MonitorTimer( PriceChecker activity) {
            super(1000 * 60 *  60 * 24 * 60, 1000 * 60 *  10 );

            Activity = activity;
        }


        PriceChecker Activity;

        @Override
        public void onTick(long l) {

            JSONObject param = new JSONObject();
            try {
                LocalDateTime now = LocalDateTime.now();
                param.put("monitorType","Price Checker");
                if( Activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)){
                    param.put("staus","Active");
                }
                else{
                    param.put("staus","Error");
                }



                param.put("expiryDate",now.plusMinutes(10));
                new DataService().postForExecuteList("sp_UpdateMonitorStatus", param, new Function<JSONArray, Void>() {
                    @Override
                    public Void apply(JSONArray jsonArray) {
                        System.out.println(jsonArray.toString());

                        return null;
                    }
                }, Activity);
            }
            catch (JSONException e) {
                System.out.println(e.getMessage());
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }




            System.out.println("Monitor Tick");
        }

        @Override
        public void onFinish() {

            this.start();

            System.out.println("Monitor finish");
        }
    }


    private class ClearTimer extends CountDownTimer
    {
        PriceChecker Activity;


        public ClearTimer( PriceChecker activity) {
            super(5000, 5000);

            Activity = activity;
        }

        @Override
        public void onFinish() {
            Activity.Description.setText("");
            Activity.Barcode.setText("");
            Activity.Rate.setText("");
            System.out.println("Timer finish");


        }

        @Override
        public void onTick(long duration) {
            //System.out.println("Timer tick");
            System.out.println("Timer Tick");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.price_checker_menu,menu);
        return true;
    }

    public String PinNumber = null;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        PopupPassword pib = new PopupPassword();
        pib.setArgs(new PopupPassword.PopupPasswordArgs( ));
        pib.setOnDoOk(new Function<Void, Boolean>() {
            @Override
            public Boolean apply(Void unused) {
                if(pib.PasswordInput.getValue().equals("76167616")){
                    SettingsPopupForm ps = new SettingsPopupForm();
                    ps.show(getSupportFragmentManager(),null);
                }
                return true;
            }
        });
        pib.show(getSupportFragmentManager(),null);
        //Intent intent  = new Intent(this,MainActivity.class);
        //startActivity(intent);
        return  true;

    }
    public  static  class PopupPassword extends PopupBase<PopupPassword, PopupPassword.PopupPasswordArgs>
    {
        public Control.EditTextControl PasswordInput;
        public static class  PopupPasswordArgs extends PopupBase.PopupArgs<PopupPasswordArgs> {
            public PopupPasswordArgs(){
                super("Pin Input");
                setCancelButton("Close");
                setOkButton("Login");
            }
        }
        @Override
        public void AddControls(LinearLayout container) {
            PasswordInput = Control.getEditTextControl("PinNUmber","Pin Number");
            PasswordInput.addView(container);
        }
    }
}