package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.example.myapplication.Activity.Item;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Function;

public class PriceChecker extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_checker);
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