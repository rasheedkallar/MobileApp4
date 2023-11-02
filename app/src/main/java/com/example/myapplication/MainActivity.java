package com.example.myapplication;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.PathParser;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.PopupConfirmation;
import com.example.myapplication.model.PopupDate;
import com.example.myapplication.model.PopupHtml;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.PopupSearch;
import com.example.myapplication.model.VectorDrawableCreator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Function;


public class MainActivity extends BaseActivity {
    //public static class ConfigForm


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);





        setContentView(R.layout.activity_main);


        LinearLayout ll =  findViewById(R.id.container);

        Control.EditTextControl etc = Control.getEditTextControl("test","test");
















    }
}