package com.example.myapplication.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

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
            onAddControl(control,_container);
        }
    }
    @Override
    public  String getOkButton(){
        return "Save";
    }
    @Override
    public void onAddControl(LinearLayout container) {
        ScrollView sv = new ScrollView(Context);
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);
        container.addView(sv);

        FlexboxLayout fbl = new FlexboxLayout(Context);
        TableLayout.LayoutParams fblP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fbl.setLayoutParams(fblP);
        fbl.setFlexWrap(FlexWrap.WRAP);
        sv.addView(sv);

        _container = fbl;

    }

    public void onAddControl(PopupForm.Control control,FlexboxLayout container){
        container.addView(GenerateView(control));
    }
    public  int GetControlWidth(PopupForm.Control control){
        if(control.DoubleSize) return 802;
        else return 400;
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
            LinearLayout.LayoutParams tvlp= new LinearLayout.LayoutParams(GetControlWidth(control)- buttonWidth+10, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                        public void onPick(DataService.Lookup lookup) {
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
        return  view;
    }

    public  static abstract  class  onFormPopupFormListener extends  Popup.onFormPopupListener{


        @Override
        public PopupForm getPopup(){
            return  (PopupForm)super.getPopup();
        }
         /*
        @Override
        public void onAddControl(LinearLayout container) {

            ScrollView sv = new ScrollView(getPopup().Context);
            ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
            scP.setLayoutDirection(LinearLayout.HORIZONTAL);
            sv.setLayoutParams(scP);
            container.addView(sv);

            FlexboxLayout fbl = new FlexboxLayout(getPopup().Context);
            TableLayout.LayoutParams fblP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fbl.setLayoutParams(fblP);
            fbl.setFlexWrap(FlexWrap.WRAP);
            sv.addView(sv);
            for (PopupForm.Control control : getPopup().Controls) {
                onAddControl(control,fbl);
            }


        }


        public  int GetControlWidth(PopupForm.Control control){
            if(control.DoubleSize) return 802;
            else return 400;
        }


        public void onAddControl(PopupForm.Control control,FlexboxLayout container){
            container.addView(GenerateView(control));
        }
        public  View GenerateView(PopupForm.Control control){
            View view;

            if(control.Type == PopupForm.ControlType.Lookup){

                Integer buttonWidth = 100;

                LinearLayout lll = new LinearLayout(getPopup().Context);
                LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lllp.setMargins(0, 0, 0, 0);
                lll.setPadding(0, 0, 0, 0);
                lll.setOrientation(LinearLayout.HORIZONTAL);
                lll.setLayoutParams(lllp);

                TextView tvl = new TextView(getPopup().Context);
                LinearLayout.LayoutParams tvlp= new LinearLayout.LayoutParams(GetControlWidth(control)- buttonWidth+10, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvlp.setMargins(0, 0, 0, 0);
                tvl.setLayoutParams(tvlp);
                lll.addView(tvl);
                Button btl = new Button(getPopup().Context);
                ViewGroup.LayoutParams btlp= new ViewGroup.LayoutParams(100, 100);
                btl.setLayoutParams(btlp);
                btl.setText("...");
                btl.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        new PopupLookup(getPopup().Context, control.Caption, control.Lookup, new PopupLookup.onFormPopupLookupListener() {
                            @Override
                            public void onPick(DataService.Lookup lookup) {
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
                EditText txt = new EditText(getPopup().Context);
                TableLayout.LayoutParams txtp= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                txtp.setMargins(0, 0, 0, 0);
                txt.setLayoutParams(txtp);
                view = txt;
            }
            return  view;
        }


          */
        public abstract void onPick(DataService.Lookup lookup);
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
