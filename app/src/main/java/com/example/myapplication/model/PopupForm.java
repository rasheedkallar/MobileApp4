package com.example.myapplication.model;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

public class PopupForm extends Popup {
    public final List<PopupForm.Control> Controls;
    private FlexboxLayout _container;
    public final String PostUrl;
    public PopupForm(android.content.Context context, String title , List<PopupForm.Control> controls, String postUrl, onFormPopupFormListener listener){
        super(context,title,listener);
        Controls = controls;
        PostUrl = postUrl;
        for (PopupForm.Control control : Controls) {
            AddFormControl(control,_container);
        }
    }
    @Override
    public  String getOkButton(){
        return "Save";
    }
    @Override
    public void AddControls(LinearLayout container) {



        ScrollView sv = new ScrollView(Context);
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);
        container.addView(sv);



        FlexboxLayout fbl = new FlexboxLayout(Context);
        TableLayout.LayoutParams fblP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fbl.setLayoutParams(fblP);
        fbl.setFlexWrap(FlexWrap.WRAP);
        sv.addView(fbl);
        _container = fbl;



    }

    public void AddFormControl(PopupForm.Control control,FlexboxLayout container){
        container.addView(GenerateView(control));
    }
    public  int getControlWidth(PopupForm.Control control){
        if(control.DoubleSize) return 802;
        else return 400;
    }

    @Override
    public void DoOk(DialogInterface dialog, int which) {
        super.DoOk(dialog, which);
    }

    public  View GenerateView(PopupForm.Control control){
        View view;

        if(control.Type == PopupForm.ControlType.Lookup){

            Integer buttonWidth = 100;

            LinearLayout lll = new LinearLayout(Context);
            LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lllp.setMargins(0, 0, 0, 0);
            lll.setPadding(0, 0, 0, 0);
            lll.setOrientation(LinearLayout.HORIZONTAL);
            lll.setLayoutParams(lllp);

            TextView tvl = new TextView(Context);
            LinearLayout.LayoutParams tvlp= new LinearLayout.LayoutParams(getControlWidth(control)- buttonWidth+10, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvlp.setMargins(0, 0, 0, 0);
            tvl.setLayoutParams(tvlp);
            lll.addView(tvl);
            Button btl = new Button(Context);
            ViewGroup.LayoutParams btlp= new ViewGroup.LayoutParams(100, 100);
            btl.setLayoutParams(btlp);
            btl.setText("...");
            btl.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    new PopupLookup(Context, control.Caption, control.Lookup, new PopupLookup.onFormPopupLookupListener() {
                        @Override
                        public void onPick(DialogInterface dialog, DataService.Lookup lookup) {
                            tvl.setText(lookup.Name);
                            lll.setTag(lookup);
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
        else{
            EditText txt = new EditText(Context);
            TableLayout.LayoutParams txtp= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            txtp.setMargins(0, 0, 0, 0);
            txt.setLayoutParams(txtp);
            view = txt;
        }

        control.Control = view;
        LinearLayout ll = new LinearLayout(Context);
        LinearLayout.LayoutParams llParam;
        llParam= new LinearLayout.LayoutParams(getControlWidth(control), LinearLayout.LayoutParams.WRAP_CONTENT);

        ll.setOrientation(LinearLayout.VERTICAL);
        llParam.setMargins(2, 2, 2, 2);
        ll.setLayoutParams(llParam);
        if(control.Caption != null) {
            TextView caption = new TextView(Context);
            TableLayout.LayoutParams cParam= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 70);
            cParam.setMargins(0, 0, 0, 0);
            caption.setPadding(5, 5, 5, 5);
            caption.setLayoutParams(cParam);
            caption.setText(control.Caption);
            caption.setTextColor(ContextCompat.getColor(Context, R.color.white));
            caption.setBackgroundColor(ContextCompat.getColor(Context, androidx.cardview.R.color.cardview_dark_background));
            ll.addView(caption);
        }
        ll.addView(view);
        return  ll;
    }

    public  static abstract  class  onFormPopupFormListener extends  Popup.onFormPopupListener{
        @Override
        public PopupForm getPopup(){
            return  (PopupForm)super.getPopup();
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
        public Control(PopupForm.ControlType type, String name, String cation) {
            ConfigControl(type,name,cation,null,null);
        }
        public Control(PopupForm.ControlType type, String name, String cation, Object defaultValue , List<DataService.Lookup> lookup) {
            ConfigControl(type,name,cation,defaultValue,lookup);
        }
        private void ConfigControl(PopupForm.ControlType type, String name, String cation, Object defaultValue, List<DataService.Lookup> lookup) {
            Type = type;
            if(type == PopupForm.ControlType.Text || type == PopupForm.ControlType.Lookup)DoubleSize = true;


            else DoubleSize = false;
            Name = name;
            Caption = cation;
            AllowNull = false;
            DecimalPlace = 2;
            DefaultValue = defaultValue;
            Lookup = lookup;
        }


        public PopupForm.ControlType Type;
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
