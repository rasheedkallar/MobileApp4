package com.example.myapplication.model;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.myapplication.BaseActivity;
import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Control {
    public static int CONTROL_SIZE_DOUBLE = -2;
    public static int CONTROL_SIZE_SINGLE = -1;








    public static LookupControl getLookupControl( String name, String caption,List<DataService.Lookup> lookups){
        return new LookupControl(name,caption,lookups);
    }
    public static DateTimeControl getDateTimeControl( String name, String caption){
        return new DateTimeControl(name,caption);
    }
    public static DateControl getDateControl( String name, String caption){
        return new DateControl(name,caption);
    }

    public static class LookupControl extends BrowseControlBase<LookupControl,Long>{

        private transient TextView LookupTextView;

        private List<DataService.Lookup> Lookups;
        public List<DataService.Lookup> getLookups() {
            return Lookups;
        }
        public LookupControl setLookups(List<DataService.Lookup> lookups) {
            Lookups = lookups;
            return this;
        }

        public LookupControl(String name, String caption, List<DataService.Lookup> lookups) {
            super(name, caption);
            Lookups = lookups;




        }



        //private Long Value = null;
        //@Override
        //public Long getValue() {
        //    return Value;
        //}

        @Override
        public String getFormatValue(Object value) {
            if(value==null)return super.getFormatValue(null);
            try{
                long longValue = Long.parseLong(value.toString());
                Optional<DataService.Lookup> l = Lookups.stream().filter(itm->itm.getId().equals(longValue)).findAny();
                if(l.isPresent()){
                    return l.get().getName();
                }
                else {
                    return  "[Unknown]";
                }
            }
            catch (Exception e){
                return value.toString();
            }
        }

        @Override
        public void valueChange(Long oldValue, Long newValue) {
            //if(LookupTextView != null) {
            Optional<DataService.Lookup> l = Lookups.stream().filter(itm -> itm.getId().equals(newValue)).findAny();
            if (l.isPresent()) {
                LookupTextView.setText(l.get().getName());
            } else {
                LookupTextView.setText("[Unknown]");
            }
            //}
        }



        @Override
        public LookupControl readValue(Object value) {
            if(value==null)setValue(null);
            else if(value.getClass().equals(Long.class)){
                setValue((Long)value);
            }
            else{
                try{
                    setValue(Long.parseLong(value.toString()));
                }
                catch (Exception e){

                }
            }
            return this;
        }





        @Override
        protected void onBrowse(Button button) {
            BaseActivity activity = (BaseActivity)button.getContext();
            PopupLookup.create(getCaption(),getLookups(),getValue(),(lookup)->{
                setValue(lookup.getId());
                return true;
            }).show(activity.getSupportFragmentManager(),null);
        }
        @Override
        protected void addBrowseView(ViewGroup container) {
            LookupTextView = new TextView(container.getContext());
            LookupTextView.setPadding(5, 5, 5, 5);
            if(getValue()!=null){
                Optional<DataService.Lookup> l = Lookups.stream().filter(itm->itm.getId().equals(getValue())).findAny();
                if(l.isPresent()){
                    LookupTextView.setText(l.get().getName());
                }
                else {
                    LookupTextView.setText("[Unknown]");
                }
            }


            LinearLayout.LayoutParams tvlP= new LinearLayout.LayoutParams(getWidth()- getButtonSize()+10, ViewGroup.LayoutParams.WRAP_CONTENT);
            LookupTextView.setLayoutParams(tvlP);
            container.addView(LookupTextView);
        }
    }
    public static class DateTimeControl extends DateControlBase<DateTimeControl>{
        @Override
        public String getFormat() {
            return "dd/MM/yy HH:mm";
        }
        public DateTimeControl( String name, String caption) {
            super( name, caption);
        }

        @Override
        protected void onBrowse(Button button) {

            BaseActivity activity = (BaseActivity)button.getContext();
            PopupDate.create(getCaption(),getValue(),(value)->{
                setValue(value);
                return true;
            }).show(activity.getSupportFragmentManager(),null);
        }
    }
    public static class DateControl extends DateControlBase<DateControl>{




        @Override
        public String getFormat() {
            return "dd/MM/yy";
        }
        public DateControl( String name, String caption) {
            super( name, caption);
        }


        @Override
        public DateControl setValue(Date value) {
            Date today = value;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try{
                today = sdf.parse(sdf.format(today));
            }
            catch (Exception e){

            }
            super.setValue(today);
            return this ;
        }

        @Override
        protected void onBrowse(Button button) {
            BaseActivity activity = (BaseActivity)button.getContext();
            PopupDate.create(getCaption(),getValue(),(value)->{
                setValue(value);
                return true;
            }).show(activity.getSupportFragmentManager(),null);
        }
    }

    public static abstract class DateControlBase<T extends ControlBase<T,Date>> extends BrowseControlBase<T,Date>{
        private transient EditText EditTextControl;
        private transient boolean isInputValid = true;

        public abstract  String getFormat();


        public DateControlBase( String name, String caption) {
            super( name, caption);
            setControlSize(CONTROL_SIZE_SINGLE);

        }

        @Override
        public T readValue(Object value) {
            if(value==null){
                setValue(null);
            }
            else if(value.getClass().equals(Date.class)){
                setValue((Date)value);
            }
            else{
                HashMap<String,Integer> formats = new HashMap<String,Integer>();
                formats.put("yyyy-MM-dd'T'HH:mm:ss.SSS",23);
                formats.put("dd/MM/yy HH:mm:ss.SSS",21);
                formats.put("yyyy-MM-dd'T'HH:mm:ss",19);
                formats.put("dd/MM/yy HH:mm:ss",17);
                formats.put("yyyy-MM-dd'T'HH:mm",16);
                formats.put("dd/MM/yy HH:mm",14);
                formats.put("yyyy-MM-dd",10);
                formats.put("dd/MM/yy",8);

                Date date = null;
                String input = value.toString().trim();
                for (String format: formats.keySet()) {
                    if(input.length() == formats.get(format).intValue()){
                        DateFormat dateFormat = new SimpleDateFormat(format);
                        try{
                            date = dateFormat.parse(value.toString());
                            setValue(date);
                        }
                        catch (ParseException e){
                        }
                        break;
                    }
                }
            }
            return (T)this;
        }



        @Override
        public String getFormatValue(Object value) {
            if(value==null)super.getFormatValue(null);
            Date dateValue;
            DateFormat format;
            if(value.getClass().equals(Date.class)) {
                dateValue = (Date) value;
            }
            else{
                if(value.toString().length() == 19) {
                    format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                }
                else {
                    format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                }
                try{
                    dateValue = format.parse(value.toString());
                }
                catch (ParseException e){
                    return super.getFormatValue(value);
                }
            }
            format = new SimpleDateFormat(getFormat());
            return format.format(dateValue);
        }

        private Date isInvalidDate(String dateString, String dateFormat) {
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
        private  Date validateDate(Context context,EditText tvl,Date currentDate){
            Calendar currentCalendar = null;
            if(currentDate!=null){
                currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(currentDate);
            }
            String text = null;
            Date date = null;
            boolean invalid = false;
            if(tvl.getText()!=null) text = tvl.getText().toString();
            if(text != null && text.length() != 0){
                date = isInvalidDate(text, "dd/MM/yy");
                if(date==null){
                    date = isInvalidDate(text, "dd/MM/yy HH:mm");
                    if(date==null) {
                        date = isInvalidDate(text, "dd/MM/yy HH:mm:ss");
                        if(date==null){
                            date = isInvalidDate(text, "dd/MM/yy HH:mm:ss.SSS");
                        }
                        else{
                            if(currentCalendar!=null){
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                calendar.set(Calendar.MILLISECOND,currentCalendar.get(Calendar.MILLISECOND));
                                date = calendar.getTime();
                            }
                        }
                    }
                    else {
                        if(currentCalendar!=null ){
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.SECOND,currentCalendar.get(Calendar.SECOND));
                            calendar.set(Calendar.MILLISECOND,currentCalendar.get(Calendar.MILLISECOND));
                            date = calendar.getTime();
                        }
                    }
                }
                if(date==null)invalid = true;
            }
            if(invalid){
                tvl.setTextColor(ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_error));
                isInputValid = false;

                return null;
            }
            else {
                tvl.setTextColor(ContextCompat.getColor(context, R.color.black));
                isInputValid = true;
            }
            return  date;
        }
        @Override
        protected void addBrowseView(ViewGroup container) {

            EditTextControl = new EditText(container.getContext());
            if(getValue()!=null){
                DateFormat dateFormat = new SimpleDateFormat(getFormat());
                String newText = dateFormat.format(getValue());
                EditTextControl.setText(newText);
            }


            EditTextControl.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void afterTextChanged(Editable editable) {

                    if(EditTextControl.getText()==null || EditTextControl.getText().toString().length() == 0){
                        if(getValue()!= null ) setValue(null);
                    }
                    else{
                        Date currentDate = getValue();
                        //if(EditTextControl.getTag() != null) currentDate = (Date)EditTextControl.getTag();
                        Date date = validateDate(container.getContext(), EditTextControl,currentDate);
                        if(date!=null && !date.equals( currentDate))setValue(date);
                    }
                }
            });

            LinearLayout.LayoutParams tvlP= new LinearLayout.LayoutParams(getWidth()- getButtonSize()+10, ViewGroup.LayoutParams.WRAP_CONTENT);
            EditTextControl.setLayoutParams(tvlP);
            container.addView(EditTextControl);
        }



        @Override
        public void valueChange(Date oldValue, Date newValue) {
            //if(EditTextControl != null) {

            if (newValue== null && (EditTextControl.getText()== null || EditTextControl.getText().length() == 0))
                return;
            String currentText = null;
            if (EditTextControl.getText()!= null && EditTextControl.getText().length() != 0)
                currentText = EditTextControl.getText().toString();

            String newText = null;
            if(newValue!=null){
                DateFormat dateFormat = new SimpleDateFormat(getFormat());
                newText = dateFormat.format(newValue);
                //EditTextControl.setText(dateFormat.format(value));
            }
            if(!currentText.equals(newText)){
                EditTextControl.setText(newText);
            }

            //}
        }






        @Override
        public boolean onValidate() {
            if(!isInputValid)return false;
            else return super.onValidate();
        }





    }

    public static abstract class BrowseControlBase<T extends ControlBase<T,U>,U extends  Serializable> extends ControlBase<T,U>{
        private transient Button BrowsButton;
        private int ButtonSize = 100;


        protected Button getBrowsButton() {
            return BrowsButton;
        }

        public int getButtonSize(){
            return  ButtonSize;
        }
        public T setButtonSize(int buttonSize) {
            ButtonSize = buttonSize;
            return (T)this;
        }
        public BrowseControlBase(String name, String caption){

            super(name, caption);
            setControlSize(CONTROL_SIZE_DOUBLE);
        }
        protected abstract void onBrowse(Button button);
        @Override
        protected void addValueView(ViewGroup container) {
            LinearLayout BrowseLayoutControl = new LinearLayout(container.getContext());
            LinearLayout.LayoutParams lllP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            BrowseLayoutControl.setOrientation(LinearLayout.HORIZONTAL);
            BrowseLayoutControl.setLayoutParams(lllP);

            BrowsButton = new Button(container.getContext());
            BrowsButton.setText("...");
            BrowsButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onBrowse((Button)v);
                }
            });
            ViewGroup.LayoutParams btlp= new ViewGroup.LayoutParams(getButtonSize(), getButtonSize());
            BrowsButton.setLayoutParams(btlp);

            addBrowseView(BrowseLayoutControl);

            //View valueView = getBrowseView();
            //if(valueView!=null){
            //    BrowseLayoutControl.addView(valueView);
            //}
            BrowseLayoutControl.addView(BrowsButton);
            container.addView(BrowseLayoutControl);
            //return BrowseLayoutControl;
        }
        protected abstract void addBrowseView(ViewGroup container);
    }






    public static EditTextControl getEditTextControl( String name, String caption){
        return new EditTextControl(name,caption);
    }
    public static class EditTextControl extends ControlBase<EditTextControl,String>{
        public EditTextControl(String name, String caption){
            super( name, caption);
        }
        private transient EditText EditTextControl;
        public EditText getEditTextControl() {
            return EditTextControl;
        }
        @Override
        public EditTextControl readValue(Object value) {
            if(value== null)setValue(null);
            else{
                setValue(value.toString());
            }
            return this;
        }
        @Override
        public void addValueView(ViewGroup container) {
            EditTextControl = new EditText(container.getContext());
            EditTextControl.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void afterTextChanged(Editable editable) {
                    if(EditTextControl.getText() ==null || EditTextControl.getText().toString().length() == 0)setValue(null);
                    else setValue(EditTextControl.getText().toString());
                }
            });
            EditTextControl.setText(getValue());
            TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            EditTextControl.setLayoutParams(txtP);
            container.addView(EditTextControl);
        }
        @Override
        public void valueChange(String oldValue, String newValue) {
            if ((EditTextControl.getText()==null || EditTextControl.getText().toString().length() == 0) && getValue()==null)
                return ;
            if (!EditTextControl.getText().toString().equals(newValue)){
                EditTextControl.setText(newValue);
            }
        }
    }

    public static HiddenControl getHiddenControl( String name, Long value){
        return new HiddenControl(name,value);
    }

    public static class HiddenControl extends ControlBase<HiddenControl,Serializable>{ //<HiddenControl,Long>{
        public HiddenControl( String name, Serializable value){
            super( name, null);
            setValue(value);
            setIsRequired(false);
        }
        @Override
        public void valueChange(Serializable oldValue, Serializable newValue) {

        }

        @Override
        protected void addValueView(ViewGroup container) {

        }
        @Override
        public void addView(ViewGroup container) {

        }
        @Override
        public HiddenControl readValue(Object value) {
            setValue(Long.parseLong(value.toString()));
            return this;
        }
    }


    public static abstract   class ControlBase<T extends ControlBase<T,U>,U extends Serializable> implements Serializable { //<T extends ControlBase<T,U>,U>{
        public ControlBase(String name,String caption){
            Caption = caption;
            Name = name;
        }
        private boolean IsRequired = true;
        public boolean getIsRequired() {
            return IsRequired;
        }
        public T setIsRequired(boolean required) {
            IsRequired = required;
            return  (T)this;
        }
        private int ControlSize = -1;
        public int getControlSize(){
            return ControlSize;
        }
        public T setControlSize(int size){
            ControlSize = size;
            return  (T)this;
        }
        private String Name;
        public String getName(){
            return  Name;
        }
        private String Caption = null;
        public String getCaption(){
            return  Caption;
        }
        public T setCaption(String caption) {
            Caption = caption;
            return (T)this;
        }

        public int getWidth(){
            int singleSize = 463;
            if(ControlSize<0)return Math.abs(ControlSize) * singleSize;
            else return ControlSize;
        }
        public String getFormatValue(Object value)
        {
            if(value==null)return null;
            else return value.toString();
        }
        private U Value;
        public U getValue(){
            return Value;
        }
        public  T setValue(U value){
            if(Value == null && value == null)return (T)this;
            U oldValue = Value;
            Value = value;
            if(viewCreated && (oldValue == null || value == null || !oldValue.equals(value))){

                valueChange(Value,value);
            }

            return (T)this;
        }
        public abstract void valueChange(U oldValue, U newValue);
        private transient boolean viewCreated =false;
        private transient TextView CaptionTextView;
        public TextView getCaptionTextView() {
            return CaptionTextView;
        }
        public void addView(ViewGroup container)
        {
            viewCreated = false;
            LinearLayout ll = new LinearLayout(container.getContext());
            LinearLayout.LayoutParams llParam= new LinearLayout.LayoutParams(getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.setOrientation(LinearLayout.VERTICAL);
            llParam.setMargins(2, 2, 2, 2);
            ll.setLayoutParams(llParam);

            if(Caption != null) {
                CaptionTextView = new TextView(container.getContext());
                TableLayout.LayoutParams cParam= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 60);
                CaptionTextView.setPadding(10, 0, 5, 10);
                CaptionTextView.setLayoutParams(cParam);
                CaptionTextView.setText(Caption);
                CaptionTextView.setTextColor(ContextCompat.getColor(container.getContext(), R.color.white));
                CaptionTextView.setBackgroundColor(Color.parseColor("#008477"));
                ll.addView(CaptionTextView);
            }
            addValueView(ll);
            container.addView(ll);
            viewCreated = true;
        }
        protected abstract void addValueView(ViewGroup container);
        public  String getUrlParam(){
            Object value = getValue();
            if (value==null)return  Name + "=" ;
            else  if(value.getClass().equals(Date.class)){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                return  Name + "=" + dateFormat.format(value);
            }
            else{
                return  Name + "=" + value.toString();
            }
        }
        public abstract T readValue(Object value);
        public  T readValue(JSONObject data){
            try{
                Object value = data.get(getName());
                readValue(value);
            }
            catch (JSONException e){

            }
            return  (T)this;
        }
        public boolean validate(){
            boolean valid = onValidate();
            if(CaptionTextView!=null) {
                if (valid) {
                    CaptionTextView.setBackgroundColor(Color.parseColor("#008477"));
                } else {
                    CaptionTextView.setBackgroundColor(Color.parseColor("#BC3E17"));
                }
            }
            return valid;
        }
        public boolean onValidate(){
            if(getValue()==null && getIsRequired()) return  false;
            else return true;
        }
    }
}
