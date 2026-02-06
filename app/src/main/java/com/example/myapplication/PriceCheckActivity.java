package com.example.myapplication;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.myapplication.Data.DataService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.function.Function;

public class PriceCheckActivity extends BaseActivity {

    private EditText txtScan;
    private TextView tvStatus, tvError, Description, Rate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_checker);

        txtScan     = findViewById(R.id.txtScan);
        tvStatus    = findViewById(R.id.tvStatus);
        tvError     = findViewById(R.id.tvError);
        Description = findViewById(R.id.Description);
        Rate        = findViewById(R.id.Rate);

        tvStatus.setText("Ready — Scan a barcode");
        tvError.setVisibility(TextView.GONE);
        Description.setText("");
        Rate.setText("—");

        keepFocusOnScanBox();
        setupKeyListener();
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
        txtScan.setOnKeyListener((v, keyCode, event) -> {
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

    private void handleScannedText(String barcode) {
        tvError.setVisibility(TextView.GONE);
        tvStatus.setText("Checking price…");
        Description.setText("");
        Rate.setText("—");

        queryPriceWithExistingService(barcode);
    }

    /** Uses your existing service exactly as requested */
    private void queryPriceWithExistingService(String barcode) {
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
}