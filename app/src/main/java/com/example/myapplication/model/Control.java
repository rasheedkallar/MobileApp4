package com.example.myapplication.model;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

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


    public static HiddenControl getHiddenControl(Context context, String name, Object value){
        return new HiddenControl(context,name,value);
    }
    public static EditTextControl getEditTextControl(Context context, String name, String caption){
        return new EditTextControl(context,name,caption);
    }
    public static LookupControl getLookupControl(Context context, String name, String caption,List<DataService.Lookup> lookups){
        return new LookupControl(context,name,caption,lookups);
    }
    public static DateTimeControl getDateTimeControl(Context context, String name, String caption){
        return new DateTimeControl(context,name,caption);
    }
    public static DateControl getDateControl(Context context, String name, String caption){
        return new DateControl(context,name,caption);
    }
    public static class EditTextControl extends ControlBase<EditTextControl,String>{
        private EditText EditTextControl;
        public EditText getEditTextControl() {
            return EditTextControl;
        }
        public EditTextControl(Context context, String name, String caption){
            super( context,name, caption);
            EditTextControl = new EditText(getContext());
            TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            EditTextControl.setLayoutParams(txtP);
        }
        @Override
        public String getValue() {
            if(EditTextControl.getText() == null || EditTextControl.getText().toString().length() == 0)return  null;
            else  return EditTextControl.getText().toString();
        }

        @Override
        public EditTextControl setValue(String value) {

            if(value == null)EditTextControl.setText(null);
            else EditTextControl.setText(value.toString());
            return  this;
        }

        @Override
        public Control.EditTextControl readValue(Object value) {
            if(value == null)setValue(null);
            else{
                setValue(value.toString());
            }
            return this;
        }

        @Override
        public View getView() {

            return EditTextControl;
        }
    }
    public static class LookupControl extends BrowseControlBase<LookupControl,Long>{
        private List<DataService.Lookup> Lookups;
        public List<DataService.Lookup> getLookups() {
            return Lookups;
        }
        public LookupControl setLookups(List<DataService.Lookup> lookups) {
            Lookups = lookups;
            return this;
        }
        private TextView LookupTextView;
        public LookupControl(android.content.Context context, String name, String caption, List<DataService.Lookup> lookups) {
            super(context, name, caption);
            Lookups = lookups;

            LookupTextView = new TextView(context);
            LookupTextView.setPadding(5, 5, 5, 5);
        }
        private Long Value = null;
        @Override
        public Long getValue() {
            return Value;
        }

        @Override
        public String getFormatValue(Object value) {
            if(value == null)return super.getFormatValue(null);
            try{
                long longValue = Long.parseLong(value.toString());
                Optional<DataService.Lookup> l = Lookups.stream().filter(itm->itm.Id.equals(longValue)).findAny();
                if(l.isPresent()){
                    return l.get().Name;
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
        public LookupControl setValue(Long value) {
            Value = (Long)value;
            Optional<DataService.Lookup> l = Lookups.stream().filter(itm->itm.Id.equals(Value)).findAny();
            if(l.isPresent()){
                LookupTextView.setText(l.get().Name);
            }
            else {
                LookupTextView.setText("[Unknown]");
            }
            return this;
        }

        @Override
        public LookupControl readValue(Object value) {
            if(value == null)setValue(null);
            else if(value.getClass() == Long.class){
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
        public void onBrowse(Button button) {
            new PopupLookup(getContext(), getCaption(), getLookups(), new PopupLookup.onFormPopupLookupListener() {
                @Override
                public boolean onPick( DataService.Lookup lookup) {
                    setValue(lookup.Id);
                    return true;
                }
            });
        }
        @Override
        public View getBrowseView() {
            LinearLayout.LayoutParams tvlP= new LinearLayout.LayoutParams(getWidth()- getButtonSize()+10, ViewGroup.LayoutParams.WRAP_CONTENT);
            LookupTextView.setLayoutParams(tvlP);
            return LookupTextView;
        }
    }
    public static class DateTimeControl extends DateControlBase<DateTimeControl>{
        @Override
        public String getFormat() {
            return "dd/MM/yy HH:mm";
        }
        public DateTimeControl(android.content.Context context, String name, String caption) {
            super(context, name, caption);
        }
        @Override
        public void onBrowse(Button button) {
            Date date = null;
            if(getValue() != null)date = (Date) getValue();
            new PopupDate(getContext(), getCaption(), date,true, new PopupDate.onFormPopupDateListener() {
                @Override
                public boolean onPick(Date date) {
                    setValue(date);
                    return  true;
                }
            });
        }
    }
    public static class DateControl extends DateControlBase<DateControl>{
        @Override
        public String getFormat() {
            return "dd/MM/yy";
        }
        public DateControl(android.content.Context context, String name, String caption) {
            super(context, name, caption);
        }
        @Override
        public void onBrowse(Button button) {
            Date date = null;
            if(getValue() != null)date = (Date) getValue();
            new PopupDate(getContext(), getCaption(), date,false, new PopupDate.onFormPopupDateListener() {
                @Override
                public boolean onPick(Date date) {
                    setValue(date);
                    return  true;
                }
            });
        }
    }

    public static abstract class DateControlBase<T> extends BrowseControlBase<T,Date>{
        private EditText EditTextControl;
        public abstract  String getFormat();

        private boolean isInputValid = true;
        public DateControlBase(Context context, String name, String caption) {
            super(context, name, caption);
            setControlSize(CONTROL_SIZE_SINGLE);
            EditTextControl = new EditText(context);
            EditTextControl.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void afterTextChanged(Editable editable) {
                    Date currentDate = null;
                    if(EditTextControl.getTag() != null) currentDate = (Date)EditTextControl.getTag();
                    Date date = validateDate(context, EditTextControl,currentDate);
                    if(date != null || EditTextControl.getText() == null || EditTextControl.getText().toString() == null || EditTextControl.getText().toString().length() == 0)EditTextControl.setTag(date);
                }
            });
        }

        @Override
        public T readValue(Object value) {
            if(value == null){
                setValue(null);
            }
            else if(value.getClass() == Date.class){
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
            if(value == null)super.getFormatValue(null);
            Date dateValue;
            DateFormat format;
            if(value.getClass() == Date.class) {
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
        private  Date validateDate(Context context,EditText tvl,Date currentDate){
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
        public View getBrowseView() {
            LinearLayout.LayoutParams tvlP= new LinearLayout.LayoutParams(getWidth()- getButtonSize()+10, ViewGroup.LayoutParams.WRAP_CONTENT);
            EditTextControl.setLayoutParams(tvlP);
            return EditTextControl;
        }

        @Override
        public Date getValue() {
            return (Date)EditTextControl.getTag();
        }

        @Override
        public boolean onValidate() {
            if(!isInputValid)return false;
            else return super.onValidate();
        }

        @Override
        public T setValue(Date value) {

            if(value == null){
                EditTextControl.setText(null);
            }else {

                DateFormat dateFormat = new SimpleDateFormat(getFormat());
                EditTextControl.setText(dateFormat.format(value));
            }
            EditTextControl.setTag(value);
            return (T)this;
        }
    }

    public static abstract class BrowseControlBase<T,U> extends ControlBase<T,U>{
        private Button BrowsButton;
        public Button getBrowsButton() {
            return BrowsButton;
        }

        public int getButtonSize(){
            return  100;
        }

        public BrowseControlBase(Context context, String name, String caption){

            super( context,name, caption);
            setControlSize(CONTROL_SIZE_DOUBLE);
        }
        public abstract void onBrowse(Button button);
        @Override
        public View getView() {

            LinearLayout BrowseLayoutControl = new LinearLayout(getContext());
            LinearLayout.LayoutParams lllP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            BrowseLayoutControl.setOrientation(LinearLayout.HORIZONTAL);
            BrowseLayoutControl.setLayoutParams(lllP);

            BrowsButton = new Button(getContext());
            BrowsButton.setText("...");
            BrowsButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onBrowse((Button)v);
                }
            });
            ViewGroup.LayoutParams btlp= new ViewGroup.LayoutParams(getButtonSize(), getButtonSize());
            BrowsButton.setLayoutParams(btlp);

            View valueView = getBrowseView();
            if(valueView != null){
                BrowseLayoutControl.addView(valueView);
            }
            BrowseLayoutControl.addView(BrowsButton);
            return BrowseLayoutControl;
        }
        public abstract View getBrowseView();
    }


    public static class HiddenControl extends ControlBase<HiddenControl,Object>{

        private Object Value;

        public HiddenControl(Context context, String name, Object value){
            super( context,name, null);
            Value = value;
            setIsRequired(false);
        }
        @Override
        public Object getValue() {
            return Value;
        }

        @Override
        public HiddenControl setValue(Object value) {

            Value = value;
            return  this;
        }

        @Override
        public View getView() {

            return null;
        }
        @Override
        public View getFillView() {
            return null;
        }

        @Override
        public HiddenControl readValue(Object value) {
            setValue(value);
            return this;
        }


    }

    public static abstract   class ControlBase<T,U>{

        public ControlBase(Context context,String name,String caption){
            Caption = caption;
            Context = context;
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
        private Object DefaultValue = null;
        public Object getDefaultValue() {
            return DefaultValue;
        }

        private String Caption = null;
        public String getCaption(){
            return  Caption;
        }

        private Context Context = null;
        public Context getContext(){
            return  Context;
        }
        public abstract View getView();

        public int getWidth(){
            int singleSize = 463;
            if(ControlSize<0)return Math.abs(ControlSize) * singleSize;
            else return ControlSize;
        }

        private TextView CaptionTextView;
        private View ValueView;
        public TextView getCaptionTextView() {
            return CaptionTextView;
        }
        public String getFormatValue(Object value)
        {
            if(value == null)return null;
            else return value.toString();
        }
        public abstract U getValue();
        public abstract T setValue(U value);
        public View getFillView()
        {
            LinearLayout ll = new LinearLayout(Context);
            LinearLayout.LayoutParams llParam= new LinearLayout.LayoutParams(getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.setOrientation(LinearLayout.VERTICAL);
            llParam.setMargins(2, 2, 2, 2);
            ll.setLayoutParams(llParam);
            if(Caption != null) {
                CaptionTextView = new TextView(Context);
                TableLayout.LayoutParams cParam= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 60);
                CaptionTextView.setPadding(10, 0, 5, 10);
                CaptionTextView.setLayoutParams(cParam);
                CaptionTextView.setText(Caption);
                CaptionTextView.setTextColor(ContextCompat.getColor(Context, R.color.white));
                CaptionTextView.setBackgroundColor(Color.parseColor("#008477"));
                ll.addView(CaptionTextView);
            }
            ValueView = getView();
            if(ValueView != null){
                ll.addView(ValueView);
            }
            return ll;
        }

        public  String getUrlParam(){
            Object value = getValue();
            if (value == null)return  Name + "=" ;
            else  if(value.getClass() == Date.class){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                return  Name + "=" + dateFormat.format(value);
            }
            else{
                return  Name + "=" + value.toString();
            }
        }
        public abstract T readValue(Object value);
        public T readValue(JSONObject data){
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

            if(CaptionTextView != null) {
                if (valid) {
                    CaptionTextView.setBackgroundColor(Color.parseColor("#008477"));
                } else {
                    CaptionTextView.setBackgroundColor(Color.parseColor("#BC3E17"));
                }
            }
            return valid;

        }
        public boolean onValidate(){
            if(getValue() == null && getIsRequired()) return  false;
            else return true;
        }


    }



}
