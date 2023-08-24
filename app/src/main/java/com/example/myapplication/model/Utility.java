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
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class Utility {
    private static void selectRow(TableRow row, TableRow header, TableLayout tableLayout) {
        row.setBackgroundColor(Color.parseColor("#C5C6C6"));

        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            TableRow otherRow = (TableRow)tableLayout.getChildAt(i);
            if(row != otherRow && header != otherRow){
                otherRow.setBackgroundResource(android.R.color.transparent);
            }
        }
    }

    public  static abstract  class  onGridListener  {
        public abstract boolean onRowSelected(TableRow row, JSONObject data);

        public  boolean isSelected(TableRow row, JSONObject data){
            return  false;
        }
    }

    public static  boolean validate(List<Control> controls){

        boolean valid = true;

        for (Control control: controls) {
            if(!control.validate())valid = false;
        }
        return valid;
    }


    public static   void CreateGrid(Context context, TableLayout table, List<Control> controls, JSONArray list,onGridListener listener) throws JSONException, ParseException {

        if(table == null){
            table = new TableLayout(context);
            TableLayout.LayoutParams tableP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            table.setLayoutParams(tableP);
        }
        table.removeAllViews();

        final TableRow header = new TableRow(context);
        table.addView(header);
        TableLayout.LayoutParams headerP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        header.setLayoutParams(headerP);
        header.setBackgroundColor(Color.parseColor("#054678"));
        header.setPadding(5,5,5,5);
        final TableLayout  parentTable = table;
        for (Control control: controls) {
            float weight = 1f;
            if(control.DoubleSize)weight = 2;
            TextView hc = new TextView(context);
            TableRow.LayoutParams hcP= new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,weight);
            hc.setLayoutParams(hcP);
            hc.setText(control.Caption);
            hc.setTextColor(Color.parseColor("#C3DEF3"));
            header.addView(hc);
        }

        for (int i = 0; i < list.length(); i++) {
            JSONObject obj = (JSONObject)list.get(i);
            TableRow item = new TableRow(context);
            table.addView(item);
            TableLayout.LayoutParams itemP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            item.setLayoutParams(itemP);
            item.setPadding(5,5,5,5);
            item.setTag(obj);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectRow(item,header,parentTable);
                    listener.onRowSelected(item,(JSONObject) item.getTag());
                }
            });
            for (Control control: controls) {
                float weight = 1f;
                if(control.DoubleSize)weight = 2;
                TextView hc = new TextView(context);
                TableRow.LayoutParams hcP= new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,weight);
                hc.setLayoutParams(hcP);
                Object value = obj.get(control.Name);
                if(value != null){
                    if(control.Type == ControlType.DateTime || control.Type == ControlType.Date || control.Type == ControlType.Time){

                       Date datevalue = null;

                        if(value.getClass() == Date.class) {
                            datevalue = (Date) value;
                        }
                        else{
                            DateFormat mainformat;
                            if(value.toString().length() == 19) {
                                mainformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            }
                            else {
                                mainformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                            }
                            datevalue = mainformat.parse(value.toString());
                        }



                        if(control.Type == ControlType.DateTime ){
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                            hc.setText(dateFormat.format(datevalue));
                        }
                        else if(control.Type == ControlType.Date){
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                            hc.setText(dateFormat.format(datevalue));
                        }
                        else if(control.Type == ControlType.Time){
                            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                            hc.setText(dateFormat.format(datevalue));
                        }
                    }
                    else if(control.Type == ControlType.Lookup){
                        Long id = Long.parseLong(value.toString());
                        Optional<DataService.Lookup> l = control.Lookup.stream().filter(itm->itm.Id.equals(id)).findAny();
                        if(l.isPresent()){
                            hc.setText(l.get().Name);
                        }
                    }
                    else{
                        hc.setText(value.toString());
                    }
                }

                //hc.setText("hi");
                item.addView(hc);
                if(listener.isSelected(item, obj)){

                    System.out.println("machid");
                    item.setBackgroundColor(Color.parseColor("#C5C6C6"));
                    //selectRow(item,header,parentTable);
                }
            }
            //break;
        }
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

    public static void showConfirmDialog(Context context,String title,String message) {
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


    public static void applyValues(JSONObject value, ArrayList<Control> controls) throws JSONException {
        for (Iterator<String> it = value.keys(); it.hasNext(); ) {
            String key = it.next();
            Optional<Control> control = controls.stream().filter(i->i.Name.equals(key)).findAny();
            if(control.isPresent()){
                control.get().DefaultValue = value.get(key);
            }
        }
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

        public  String GetUrlParam(){

            Object value = this.ValueView.getTag();
            if (value == null)return  Name + "=" ;
            else  if(Type == ControlType.DateTime || Type == ControlType.Date || Type == ControlType.Time){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                return  Name + "=" + dateFormat.format(value);
            }
            else{
                return  Name + "=" + value.toString();
            }


        }


        public  boolean validate(){
            boolean isValid = true;
            if(Type == ControlType.HiddenValue){
                 return  true;
            }
            else if(Type == ControlType.Lookup){
                if(!this.AllowNull && this.ValueView.getTag() == null)isValid = false;
            }
            else if(Type == ControlType.DateTime || Type == ControlType.Date){
                if(!this.AllowNull && this.ValueView.getTag() == null)isValid = false;
            }
            else{
                EditText txt  = (EditText)this.ValueView;
                if(!this.AllowNull && (txt.getText() == null || txt.getText().length() == 0 ))isValid = false;
            }
            if(caption != null){
                if(isValid){
                    caption.setBackgroundColor(Color.parseColor("#008477"));
                }else{
                    caption.setBackgroundColor(Color.parseColor("#BC3E17"));
                }

           }
            return  isValid;
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

        public  View GenerateView(Context context, int width)   {

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
                if(DefaultValue != null && DefaultValue.getClass() == DataService.Lookup.class){
                    DataService.Lookup lookup = (DataService.Lookup)DefaultValue;
                    tvl.setText(lookup.Name);
                    lll.setTag(lookup);
                }
                else if(DefaultValue != null){
                    Long id = Long.parseLong(DefaultValue.toString());
                    Optional<DataService.Lookup> lookup = Lookup.stream().filter(i-> i.Id.equals(id)).findAny();
                    if(lookup.isPresent()) {
                        tvl.setText(lookup.get().Name);
                        lll.setTag(lookup.get());
                    }
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
                    System.out.println(DefaultValue.toString());
                    Date value = null;
                    if(DefaultValue.getClass() == Date.class){
                        value  = (Date)DefaultValue;
                    }
                    else{

                        DateFormat defFormat ;
                        if(DefaultValue.toString().length() == 19) {
                            defFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        }
                        else {
                            defFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        }
                        try{
                            value = defFormat.parse(DefaultValue.toString());
                        }catch (ParseException ex){
                            System.out.println(ex.toString());
                        }
                    }
                    //System.out.println(value.toString());
                    if(value != null){
                        //lll.setTag(value);
                        if(Type == ControlType.Date){
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                            tvl.setText(dateFormat.format(value));
                        }
                        else if(Type == ControlType.DateTime){
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                            tvl.setText(dateFormat.format(value));
                        }
                        else{
                            DateFormat dateFormat = new SimpleDateFormat("HH:mm.ss");
                            tvl.setText(dateFormat.format(value));
                        }
                    }
                    lll.setTag(value);
                }
                view = lll;
            }
            else{
                EditText txt = new EditText(context);
                TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                txt.setLayoutParams(txtP);
                if(DefaultValue != null){
                    txt.setText(DefaultValue.toString());
                }
                view = txt;
            }
            ValueView = view;
            LinearLayout ll = new LinearLayout(context);
            LinearLayout.LayoutParams llParam= new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.setOrientation(LinearLayout.VERTICAL);
            llParam.setMargins(2, 2, 2, 2);
            ll.setLayoutParams(llParam);
            if(Caption != null) {
                caption = new TextView(context);
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

        private TextView caption;
    }
}
