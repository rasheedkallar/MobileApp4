package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.example.myapplication.Data.DataRepository;
import com.example.myapplication.Data.DataService;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.PopupBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

public class PriceCheckActivity extends BaseActivity {

    private EditText txtScan;
    private TextView tvStatus, tvError, Description, Rate;

    private ImageButton btnKeyboard;
    private  ClearTimer Timer;
    private  MonitorTimer TimerCheckup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MenuBar = getSupportActionBar();
        if(DataRepository.Companies == null || DataRepository.Companies.isEmpty() || DataRepository.CompaniesRefreshDate == null || DataRepository.CompaniesRefreshDate.before(Date.from(Instant.now().minus(5, ChronoUnit.HOURS)))){
            new DataService(this).GetCompanies(null);
        }
        else if(DataRepository.CurrentSettings.Company != null){
            for (DataRepository.Company c : DataRepository.Companies) {
                if (c.code != null && c.code.equals(DataRepository.CurrentSettings.Company)) {
                    DataRepository.setCurrentCompany(c);
                    break;
                }
            }
        }
        else{
            DataRepository.setCurrentCompany(null);
        }



        setContentView(R.layout.activity_price_checker);

        txtScan     = findViewById(R.id.txtScan);
        tvStatus    = findViewById(R.id.tvStatus);
        tvError     = findViewById(R.id.tvError);
        Description = findViewById(R.id.Description);
        Rate        = findViewById(R.id.Rate);




        btnKeyboard = findViewById(R.id.btnKeyboard);

        btnKeyboard.setOnClickListener(v ->{
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            // The button's job is to explicitly SHOW the keyboard for manual entry.
            // The user can hide it with the system back button.
            txtScan.requestFocus();
            imm.showSoftInput(txtScan, InputMethodManager.SHOW_IMPLICIT);


            //txtScan.requestFocus();

        });


        // Disable soft keyboard on focus (scanner still works)
        try {
            txtScan.setShowSoftInputOnFocus(false);
        } catch (Exception ignored) {}


        //txtScan.setShowSoftInputOnFocus(false);

        tvStatus.setText("Ready — Scan a barcode");
        tvError.setVisibility(TextView.GONE);
        Description.setText("");
        Rate.setText("—");

        keepFocusOnScanBox();
        setupKeyListener();


        Timer = new ClearTimer(this);
        Timer.start();

        TimerCheckup = new MonitorTimer(this);
        TimerCheckup.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        keepFocusOnScanBox();
    }

    /** Always keep focus on the scan box */
    private void keepFocusOnScanBox() {
        txtScan.setFocusableInTouchMode(true);
        txtScan.requestFocus();
        txtScan.setSelection(txtScan.getText().length());

        // If anything steals focus (dialogs, etc.), bring it back
        txtScan.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                txtScan.postDelayed(() -> {
                    txtScan.requestFocus();
                    txtScan.setSelection(txtScan.getText().length());
                }, 50);
            }
        });
    }

    /** Submit when Enter/Tab/Space is pressed */
    private void setupKeyListener() {
        txtScan.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean imeAction =
                    actionId == EditorInfo.IME_ACTION_DONE
                            || actionId == EditorInfo.IME_ACTION_GO
                            || actionId == EditorInfo.IME_ACTION_SEND
                            || actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_NEXT
                            || actionId == EditorInfo.IME_ACTION_UNSPECIFIED; // some keyboards use this

                    if(imeAction){
                        final String code = txtScan.getText().toString().trim();
                        android.util.Log.d("SCAN", "Submit: [" + code + "]");

                        if (!code.isEmpty()) {
                            handleScannedText(code);
                        }

                        // Clear and refocus for next scan
                        txtScan.setText("");
                        keepFocusOnScanBox();
                    }
                //System.out.println(i);

                return false;
            }
        });

        txtScan.setOnKeyListener((v, keyCode, event) -> {
            Timer.cancel();
            Timer.start();
            Description.setText("");
            Rate.setText("—");

            if (event.getAction() != KeyEvent.ACTION_DOWN)
                return false;
            // Log only meaningful events
            if (keyCode == KeyEvent.KEYCODE_ENTER ||
                    keyCode == KeyEvent.KEYCODE_TAB ||
                    keyCode == KeyEvent.KEYCODE_SPACE ||
                    keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {

                final String code = txtScan.getText().toString().trim();
                android.util.Log.d("SCAN", "Submit: [" + code + "]");

                if (!code.isEmpty()) {
                    handleScannedText(code);
                }

                // Clear and refocus for next scan
                txtScan.setText("");
                keepFocusOnScanBox();
                return true;
            }

            return false;
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.price_checker_menu,menu);

        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.main_menu,menu);
        ConnectionMenu = menu.findItem(R.id.mnu_net);
        DataRepository.refreshConnectionMenu();
        return true;



        //return true;
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



    private void handleScannedText(String barcode) {
        tvError.setVisibility(TextView.GONE);
        tvStatus.setText("Checking price…");
        Description.setText("");
        Rate.setText("—");
        String finalBarcode = barcode;
        if(finalBarcode.startsWith("#"))finalBarcode = finalBarcode.substring(1);
        queryPriceWithExistingService(finalBarcode);
    }

    /** Uses your existing service exactly as requested */
    private void queryPriceWithExistingService(String barcode) {

        //ClearTimer ThisTimer  = Timer;

        try {
            JSONObject param = new JSONObject();
            param.put("param1", "PriceChecker");
            param.put("param2", barcode);

            new DataService(getBaseContext()).postForExecuteList(
                    "sp_DataInspection",
                    param,

                    // onSuccess(JSONArray)
                    new Function<JSONArray, Void>() {


                        @Override public Void apply(JSONArray jsonArray) {
                            Timer.cancel();
                            Timer.start();

                            runOnUiThread(() -> {
                                try {
                                    if (jsonArray == null || jsonArray.length() == 0) {
                                        Description.setText("Item not found");
                                        Rate.setText("—");
                                        tvStatus.setText("Ready — Scan again");
                                    } else {
                                        JSONObject obj = jsonArray.getJSONObject(0);

                                        String desc = obj.optString("Description", "");
                                        double value1 = obj.optDouble("Value1", Double.NaN);

                                        Description.setText(desc);

                                        if (Double.isNaN(value1)) {
                                            Rate.setText("Price: — AED");
                                        } else {
                                            DecimalFormat df = new DecimalFormat("0.00");
                                            Rate.setText(String.format(
                                                    Locale.getDefault(),
                                                    "Price: %s AED",
                                                    df.format(value1)));
                                        }

                                        tvStatus.setText("Ready — Scan next");
                                    }
                                } catch (JSONException e) {
                                    showError("Invalid response");
                                }
                            });
                            return null;
                        }
                    },

                    // onError(String)
                    new Function<String, Void>() {
                        @Override public Void apply(String err) {
                            runOnUiThread(() -> showError("Server error"));
                            return null;
                        }
                    }
            );

        } catch (JSONException e) {
            showError("Request failed");
        }
    }

    private void showError(String message) {
        Rate.setText("—");
        Description.setText("");
        tvStatus.setText("Ready — Scan again");
        tvError.setText(message);
        tvError.setVisibility(TextView.VISIBLE);
    }

    private class MonitorTimer extends CountDownTimer {

        public MonitorTimer( PriceCheckActivity activity) {
            super(1000 * 60 *  60 * 24 * 60, 1000 * 60 *  10 );

            Activity = activity;
        }


        PriceCheckActivity Activity;

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
                new DataService(getBaseContext()).postForExecuteList("sp_UpdateMonitorStatus", param, new Function<JSONArray, Void>() {
                    @Override
                    public Void apply(JSONArray jsonArray) {
                        System.out.println(jsonArray.toString());

                        return null;
                    }
                }, Activity);




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
    private static class ClearTimer extends CountDownTimer
    {
        PriceCheckActivity Activity;


        public ClearTimer( PriceCheckActivity activity) {
            super(5000, 5000);

            Activity = activity;
        }

        @Override
        public void onFinish() {
            Activity.Description.setText("");
            Activity.txtScan.setText("");
            Activity.Rate.setText("—");
            System.out.println("Timer finish");


        }

        @Override
        public void onTick(long duration) {
            //System.out.println("Timer tick");
            System.out.println("Timer Tick");
        }
    }




}