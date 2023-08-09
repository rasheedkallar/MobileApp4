package com.example.myapplication.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.security.AppUriAuthenticationPolicy;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.unusedapprestrictions.IUnusedAppRestrictionsBackportService;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.*;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.List;
//import java.util.ListIterator;

import kotlin.text.UStringsKt;

public class FormEditor {

    public FormEditor(Context context, List<Control> controls, String postUrl, onFormEditorListener listner){




        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.editor_form, null);
        FlexboxLayout flexboxLayout = popupView.findViewById(R.id.flexboxLayout);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        for (Control c : controls) {

            Integer controlWidth = 400;
            if(c.DoubleSize) controlWidth = (controlWidth * 2) + 2;


            View view;
            ViewGroup.LayoutParams vParam= new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            /*
                  <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:background="@color/design_default_color_primary_dark"
            android:padding="0dp">
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="@color/white"
                android:text="Purchase Check In"
                android:textSize="25sp"
                android:padding="2dp"
                android:layout_gravity="start"/>

            <!-- Empty View to Fill Space -->
            <View
                android:layout_width="0dp"
                android:layout_height="0dp"
                android:layout_weight="1"/>




            <Button
                android:id="@+id/btn_new"
                android:layout_width="wrap_content"
                android:layout_height="40dp"
                android:layout_gravity="end"


                android:text="New" />

        </LinearLayout>


            */
            if(c.Type == ControlType.Lookup){

                Integer buttonWidth = 100;

                LinearLayout lll = new LinearLayout(context);
                LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lllp.setMargins(0, 0, 0, 0);
                lll.setPadding(0, 0, 0, 0);
                lll.setOrientation(LinearLayout.HORIZONTAL);
                lll.setLayoutParams(lllp);

                TextView tvl = new TextView(context);
                LinearLayout.LayoutParams tvlp= new LinearLayout.LayoutParams(controlWidth- buttonWidth+10, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvlp.setMargins(0, 0, 0, 0);
                tvl.setLayoutParams(tvlp);
                lll.addView(tvl);
                Button btl = new Button(context);
                ViewGroup.LayoutParams btlp= new ViewGroup.LayoutParams(100, 100);
                btl.setLayoutParams(btlp);
                btl.setText("...");
                btl.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        G.lookupPicker(context, c.Lookup, new G.onLookupPickistener() {
                            @Override
                            public void onPick(DataService.Lookup lookup) {
                                tvl.setText(lookup.Name);
                                lll.setTag(lookup);
                            }
                        });
                    }
                });
                lll.addView(btl);
                EditText txt = new EditText(context);
                txt.setLayoutParams(vParam);
                if(c.DefaultValue != null){
                    DataService.Lookup lookup = (DataService.Lookup)c.DefaultValue;
                    tvl.setText(lookup.Name);
                    lll.setTag(lookup);
                }
                view = lll;
            }
            else{
                EditText txt = new EditText(context);
                TableLayout.LayoutParams txtp= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                txtp.setMargins(0, 0, 0, 0);
                txt.setLayoutParams(txtp);



                view = txt;
            }
            c.Control = view;
            LinearLayout ll = new LinearLayout(context);
            LinearLayout.LayoutParams llParam;
            llParam= new LinearLayout.LayoutParams(controlWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

            ll.setOrientation(LinearLayout.VERTICAL);
            llParam.setMargins(2, 2, 2, 2);
            ll.setLayoutParams(llParam);
            if(c.Caption != null) {




                TextView caption = new TextView(context);
                TableLayout.LayoutParams cParam= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 70);
                cParam.setMargins(0, 0, 0, 0);
                caption.setPadding(5, 5, 5, 5);
                caption.setLayoutParams(cParam);
                caption.setText(c.Caption);
                caption.setTextColor(ContextCompat.getColor(context, R.color.white));
                caption.setBackgroundColor(ContextCompat.getColor(context, androidx.cardview.R.color.cardview_dark_background));
                ll.addView(caption);
            }
            ll.addView(view);
            flexboxLayout.addView(ll);
        }
        alertDialogBuilder.setView(popupView);
        alertDialogBuilder
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Close the dialog
                    }
                });
                if(listner.getPositiveButton() != null) {
                    alertDialogBuilder.setPositiveButton(listner.getPositiveButton(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(postUrl != null){
                                listner.onPost(new JSONObject(),postUrl);
                            }
                            dialog.dismiss();
                        }
                    });
                }
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setTitle("My Title");
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    //private Hashtable<FormEditor.Control,View> controlViews = new Hashtable<FormEditor.Control,View>();
    public abstract static class onFormEditorListener {

        public void  onPost(JSONObject json,String url){

        }
        public String getPositiveButton(){
            return "Save";
        }
    }
    public enum ControlType{
        Text,
        Date,
        Time,
        DateTime,
        Lookup,
        Int,
        Decimal,
        Password
    }
    public static class  Control {
        public Control(ControlType type,String name,String cation) {
            ConfigControl(type,name,cation,null,null);
        }
        public Control(ControlType type,String name,String cation,Object defaultValue ,List<DataService.Lookup> lookup) {
            ConfigControl(type,name,cation,defaultValue,lookup);
        }
        private void ConfigControl(ControlType type,String name,String cation,Object defaultValue, List<DataService.Lookup> lookup) {
            Type = type;
            if(type == ControlType.Text || type == ControlType.Lookup)DoubleSize = true;


            else DoubleSize = false;
            Name = name;
            Caption = cation;
            AllowNull = false;
            DecimalPlace = 2;
            DefaultValue = defaultValue;
            Lookup = lookup;
        }


        public ControlType Type;
        public String Name;
        public String Caption;
        public Boolean AllowNull;
        public int DecimalPlace;
        public List<DataService.Lookup> Lookup;
        public Object DefaultValue;
        public Boolean DoubleSize;

        public  View Control;
    }

}
