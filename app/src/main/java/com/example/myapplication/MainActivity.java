package com.example.myapplication;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.PopupConfirmation;
import com.example.myapplication.model.PopupDate;
import com.example.myapplication.model.PopupHtml;

import java.util.Date;


public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText edittext = findViewById(R.id.myNumber);
    }
}