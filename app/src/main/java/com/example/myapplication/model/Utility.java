package com.example.myapplication.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utility {

    public  TableLayout GetTableLayout(Context context, TableLayout table, List<Control> controls, JSONArray data){

        if(table == null){
            table = new TableLayout(context);
            TableLayout.LayoutParams tableP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            table.setLayoutParams(tableP);
        }
        table.removeAllViews();
        TableRow header = new TableRow(context);





        return  table;
        /*
        <TableLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="100dp"
        android:paddingLeft="10dp"
        android:paddingRight="10dp" >
        <TableRow android:background="#0079D6" android:padding="5dp">
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="UserId" />
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="User Name" />
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Location" />
        </TableRow>
        <TableRow android:background="#DAE8FC" android:padding="5dp">
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="1" />
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Suresh Dasari" />
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Hyderabad" />
        </TableRow>
        <TableRow android:background="#DAE8FC" android:padding="5dp">
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="2" />
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Rohini Alavala" />
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Guntur" />
        </TableRow>
        <TableRow android:background="#DAE8FC" android:padding="5dp">
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="3" />
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Trishika Dasari" />
            <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Guntur" />
        </TableRow>
    </TableLayout>

         */


    }




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


    public static Date AddDay(Date date,int day){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }
    public static   Button GenerateButton(Context context,String text, View.OnClickListener listener ) {


        Button btl = new Button(context);
        ViewGroup.LayoutParams btlP= new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100);

        btl.setLayoutParams(btlP);
        btl.setText(text);
        btl.setOnClickListener(listener);
        return  btl;
    }



    private static Date validateDate(Context context,EditText tvl,Date currentDate){
        Calendar currentCalendar = null;
        if(currentDate != null){
            currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(currentDate);
        }
        String text = null;
        Date date = null;
        boolean invalid = false;
        if(tvl.getText() != null) text = tvl.getText().toString();
        if(text != null && text.length() != 0){
            date = isInvalidDate(text, "dd/MM/yy");
            if(date == null){
                date = isInvalidDate(text, "dd/MM/yy HH:mm");
                if(date == null) {
                    date = isInvalidDate(text, "dd/MM/yy HH:mm:ss");
                    if(date == null){
                        date = isInvalidDate(text, "dd/MM/yy HH:mm:ss.SSS");
                    }
                    else{
                        if(currentCalendar != null){
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.MILLISECOND,currentCalendar.get(Calendar.MILLISECOND));
                            date = calendar.getTime();
                        }
                    }
                }
                else {
                    if(currentCalendar != null){
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        calendar.set(Calendar.SECOND,currentCalendar.get(Calendar.SECOND));
                        calendar.set(Calendar.MILLISECOND,currentCalendar.get(Calendar.MILLISECOND));
                        date = calendar.getTime();
                    }
                }
            }
            if(date == null)invalid = true;
        }
        if(invalid){
            tvl.setTextColor(ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_error));
            return null;
        }
        else {
            tvl.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        return  date;
    }
    private static Date isInvalidDate(String dateString, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dateString);
            String parsedDateString = sdf.format(date);
            if(parsedDateString.equals(dateString)){
                return date;
            }else{
                return  null;
            }
        } catch (ParseException e) {
            return null;
        }
    }

    public static enum ControlType{
        Text,
        Date,
        Time,
        DateTime,
        Lookup,
        Int,
        Decimal,
        Password,
        HiddenValue
    }
    public static class  Control {
        public Control(ControlType type, String name, String cation) {
            ConfigControl(type,name,cation,null,null,null);
        }
        public Control(ControlType type, String name, String cation, Object defaultValue , List<DataService.Lookup> lookup,Boolean doubleSize) {
            ConfigControl(type,name,cation,defaultValue,lookup,doubleSize);
        }

        public View GenerateView(Context context, Control control){
            int width = 463;
            if(control.DoubleSize) width = (width * 2) + 2;
            return GenerateView(context,width);

        }

        public  Object getValue(){
            //2023-01-01T00:00:00.000
            if(this.ValueView == null && Type != ControlType.HiddenValue)return  null;
            else{
                if(Type == ControlType.HiddenValue){
                    return  DefaultValue;
                }
                else if(Type == ControlType.Lookup){
                    if(this.ValueView.getTag() == null)return  null;
                    else{
                        DataService.Lookup l = (DataService.Lookup)this.ValueView.getTag();
                        return  l.Id;
                    }
                }
                else if(Type == ControlType.DateTime || Type == ControlType.Date){
                    if(this.ValueView.getTag() == null)return  null;
                    else{
                        return this.ValueView.getTag();
                    }
                }
                else{
                    EditText et = (EditText)this.ValueView;
                    if(et.getText() == null)return  null;
                    else return  et.getText().toString();
                }
            }
        }

        public  View GenerateView(Context context, int width){

            if(Type == ControlType.HiddenValue){
                this.ValueView = null;
                this.Control = null;
                return  null;
            }


            if(this.Control != null)return this.Control;


            View view;
            //width = 440;
            //String.format(width,"")

            if(Type == ControlType.Lookup){

                Integer buttonWidth = 100;

                LinearLayout lll = new LinearLayout(context);
                LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lll.setOrientation(LinearLayout.HORIZONTAL);
                lll.setLayoutParams(lllp);

                TextView tvl = new TextView(context);
                LinearLayout.LayoutParams tvlp= new LinearLayout.LayoutParams(width- buttonWidth+10, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvl.setPadding(5, 5, 5, 5);
                tvl.setLayoutParams(tvlp);
                lll.addView(tvl);
                Button btl = new Button(context);
                ViewGroup.LayoutParams btlp= new ViewGroup.LayoutParams(100, 100);
                btl.setLayoutParams(btlp);
                btl.setText("...");
                btl.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        new PopupLookup(context, Caption, Lookup, new PopupLookup.onFormPopupLookupListener() {
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
                if(DefaultValue != null){
                    DataService.Lookup lookup = (DataService.Lookup)DefaultValue;
                    tvl.setText(lookup.Name);
                    lll.setTag(lookup);
                }
                view = lll;
            }
            else if(Type == ControlType.DateTime || Type == ControlType.Date){

                Integer buttonWidth = 100;

                LinearLayout lll = new LinearLayout(context);
                LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lll.setOrientation(LinearLayout.HORIZONTAL);
                lll.setLayoutParams(lllp);

                EditText tvl = new EditText(context);
                LinearLayout.LayoutParams tvlp= new LinearLayout.LayoutParams(width- buttonWidth+10, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvl.setLayoutParams(tvlp);

                tvl.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        Date currentDate = null;
                        if(lll.getTag() != null) currentDate = (Date)lll.getTag();
                        Date date = validateDate(context, tvl,currentDate);
                        if(date != null || tvl.getText() == null || tvl.getText().toString() == null || tvl.getText().toString().length() == 0)lll.setTag(date);
                    }
                });

                lll.addView(tvl);
                Button btl = new Button(context);
                ViewGroup.LayoutParams btlp= new ViewGroup.LayoutParams(100, 100);
                btl.setLayoutParams(btlp);
                btl.setText("...");
                //btl.setBackgroundColor(Color.parseColor("#008477"));
                //btl.setTextColor(ContextCompat.getColor(context, R.color.white));
                btl.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Date date = (Date)lll.getTag();
                        new PopupDate(context, Caption, date,Type == ControlType.DateTime, new PopupDate.onFormPopupDateListener() {
                            @Override
                            public boolean onPick(Date date) {
                                if(Type == ControlType.Date){
                                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                                    tvl.setText(dateFormat.format(date));
                                    return true;
                                }
                                else{
                                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                                    tvl.setText(dateFormat.format(date));
                                    return true;
                                }
                            }
                        });
                    }
                });
                lll.addView(btl);
                if(DefaultValue != null){
                    if(Type == ControlType.Date){
                        lll.setTag(DefaultValue);
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                        Date date = (Date)DefaultValue;
                        tvl.setText(dateFormat.format(date));
                    }
                    else{
                        lll.setTag(DefaultValue);
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                        Date date = (Date)DefaultValue;
                        tvl.setText(dateFormat.format(date));
                    }
                }
                view = lll;
            }
            else{

                EditText txt = new EditText(context);
                TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                txt.setLayoutParams(txtP);
                view = txt;
            }
            ValueView = view;
            LinearLayout ll = new LinearLayout(context);
            LinearLayout.LayoutParams llParam= new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.setOrientation(LinearLayout.VERTICAL);
            llParam.setMargins(2, 2, 2, 2);
            ll.setLayoutParams(llParam);
            if(Caption != null) {
                TextView caption = new TextView(context);
                TableLayout.LayoutParams cParam= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 60);
                caption.setPadding(10, 0, 5, 10);
                caption.setLayoutParams(cParam);
                caption.setText(Caption);
                caption.setTextColor(ContextCompat.getColor(context, R.color.white));
                caption.setBackgroundColor(Color.parseColor("#008477"));
                //caption.setBackgroundColor(ContextCompat.getColor(context, androidx.cardview.R.color.cardview_dark_background));
                ll.addView(caption);
            }
            ll.addView(view);
            this.Control = ll;
            return  ll;
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
        private   View ValueView;
    }
}
