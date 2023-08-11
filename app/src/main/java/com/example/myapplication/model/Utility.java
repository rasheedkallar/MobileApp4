package com.example.myapplication.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Utility {
    public static void showAlertDialog(Context context,String title,String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public static   View GenerateView(Context context, Control control, int width){
        View view;

        if(control.Type == ControlType.Lookup){

            Integer buttonWidth = 100;

            LinearLayout lll = new LinearLayout(context);
            LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lllp.setMargins(0, 0, 0, 0);
            lll.setPadding(0, 0, 0, 0);
            lll.setOrientation(LinearLayout.HORIZONTAL);
            lll.setLayoutParams(lllp);

            TextView tvl = new TextView(context);
            LinearLayout.LayoutParams tvlp= new LinearLayout.LayoutParams(width- buttonWidth+10, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvlp.setMargins(0, 0, 0, 0);
            tvl.setLayoutParams(tvlp);
            lll.addView(tvl);
            Button btl = new Button(context);
            ViewGroup.LayoutParams btlp= new ViewGroup.LayoutParams(100, 100);
            btl.setLayoutParams(btlp);
            btl.setText("...");
            btl.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    new PopupLookup(context, control.Caption, control.Lookup, new PopupLookup.onFormPopupLookupListener() {
                        @Override
                        public boolean onPick( DataService.Lookup lookup) {
                            tvl.setText(lookup.Name);
                            lll.setTag(lookup);
                            return true;
                        }
                    });
                }
            });
            lll.addView(btl);
            if(control.DefaultValue != null){
                DataService.Lookup lookup = (DataService.Lookup)control.DefaultValue;
                tvl.setText(lookup.Name);
                lll.setTag(lookup);
            }
            view = lll;
        }
        else if(control.Type == ControlType.Date){

            Integer buttonWidth = 100;

            LinearLayout lll = new LinearLayout(context);
            LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lllp.setMargins(0, 0, 0, 0);
            lll.setPadding(0, 0, 0, 0);
            lll.setOrientation(LinearLayout.HORIZONTAL);
            lll.setLayoutParams(lllp);

            EditText tvl = new EditText(context);
            LinearLayout.LayoutParams tvlp= new LinearLayout.LayoutParams(width- buttonWidth+10, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvl.setLayoutParams(tvlp);

            lll.addView(tvl);
            Button btl = new Button(context);
            ViewGroup.LayoutParams btlp= new ViewGroup.LayoutParams(100, 100);
            btl.setLayoutParams(btlp);
            btl.setText("...");
            btl.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Date defaultDate = new Date();
                    if(control.DefaultValue != null)defaultDate = (Date) control.DefaultValue;
                    new PopupDate(context, control.Caption, defaultDate, new PopupDate.onFormPopupDateListener() {
                        @Override
                        public boolean onPick(Date date) {
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                            tvl.setText(dateFormat.format(date));
                            lll.setTag(date);
                            return true;
                        }
                    });

                    /*
                    new PopupLookup(context, control.Caption, control.Lookup, new PopupLookup.onFormPopupLookupListener() {
                        @Override
                        public void onPick(DialogInterface dialog, DataService.Lookup lookup) {
                            tvl.setText(lookup.Name);
                            lll.setTag(lookup);
                        }
                    });
                    */
                }
            });
            lll.addView(btl);
            if(control.DefaultValue != null){
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                Date date = (Date)control.DefaultValue;
                tvl.setText(dateFormat.format(date));
                lll.setTag(date);
            }
            view = lll;
        }
        else{

            EditText txt = new EditText(context);
            TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            txt.setLayoutParams(txtP);
            view = txt;
        }

        control.Control = view;
        LinearLayout ll = new LinearLayout(context);
        LinearLayout.LayoutParams llParam;
        llParam= new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);

        ll.setOrientation(LinearLayout.VERTICAL);
        llParam.setMargins(2, 2, 2, 2);
        ll.setLayoutParams(llParam);
        if(control.Caption != null) {
            TextView caption = new TextView(context);
            TableLayout.LayoutParams cParam= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 70);
            cParam.setMargins(0, 0, 0, 0);
            caption.setPadding(5, 5, 5, 5);
            caption.setLayoutParams(cParam);
            caption.setText(control.Caption);
            caption.setTextColor(ContextCompat.getColor(context, R.color.white));
            caption.setBackgroundColor(ContextCompat.getColor(context, androidx.cardview.R.color.cardview_dark_background));
            ll.addView(caption);
        }
        ll.addView(view);
        return  ll;
    }
    public static enum ControlType{
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
        public Control(ControlType type, String name, String cation) {
            ConfigControl(type,name,cation,null,null,null);
        }
        public Control(ControlType type, String name, String cation, Object defaultValue , List<DataService.Lookup> lookup,Boolean doubleSize) {
            ConfigControl(type,name,cation,defaultValue,lookup,doubleSize);
        }
        private void ConfigControl(ControlType type, String name, String cation, Object defaultValue, List<DataService.Lookup> lookup, Boolean doubleSize) {

            if(doubleSize == null) {
                if(type == ControlType.Text || type == ControlType.Lookup)DoubleSize = true;
                else DoubleSize = false;
            }
            else{
                DoubleSize = doubleSize;
            }
            Type = type;
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
        public View Control;
    }
}
