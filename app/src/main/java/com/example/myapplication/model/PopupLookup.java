package com.example.myapplication.model;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.TableLayout;

import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class PopupLookup extends PopupBase<PopupLookup, PopupLookup.PopupLookupArgs>{

    public static PopupLookup create(String header, List<DataService.Lookup> lookups,Long value,Function<DataService.Lookup,Boolean> onLookupChanged){
        PopupLookup popup = new PopupLookup();
        popup.setArgs(new PopupLookupArgs(header,lookups,value));
        popup.setOnLookupChanged(onLookupChanged);
        return popup;
    }
    public static PopupLookup create(PopupLookupArgs args,Function<DataService.Lookup,Boolean> onLookupChanged){
        PopupLookup popup = new PopupLookup();
        popup.setArgs(args);
        popup.setOnLookupChanged(onLookupChanged);
        return popup;
    }

    private FlexboxLayout _container;
    @Override
    public void AddControls(LinearLayout container) {
        PopupLookupArgs args = getArgs();

        ScrollView sv = new ScrollView(getContext());
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);

        FlexboxLayout fbl = new FlexboxLayout(getContext());
        TableLayout.LayoutParams fblP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fbl.setLayoutParams(fblP);
        fbl.setFlexWrap(FlexWrap.WRAP);

        EditText txt = new EditText(getContext());
        TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txt.setLayoutParams(txtP);
        txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = charSequence.toString().toUpperCase();
                for (int i4 = 0; i4 < fbl.getChildCount(); i4++) {
                    View childView = fbl.getChildAt(i4);
                    if (childView instanceof Button) {
                        Button button = (Button) childView;
                        if (button.getText().toString().toLowerCase().contains(text.toLowerCase())) {
                            button.setVisibility(View.VISIBLE);
                        } else {
                            button.setVisibility(View.GONE);
                        }
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {


            }
        });
        container.addView(txt);


        sv.addView(fbl);
        container.addView(sv);
        _container = fbl;
        if(getArgs().getNullCaption() != null && getArgs().getNullCaption().length() !=0){
            onAddLookup(_container,null);
        }
        for (DataService.Lookup lookup : args.getLookups()) {
            onAddLookup(_container,lookup);
        }
    }

    public void onAddLookup(FlexboxLayout container, DataService.Lookup lookup){
        Button button = new Button(getContext());
        LinearLayout.LayoutParams btlp= new LinearLayout.LayoutParams(GetButtonWidth(), GetButtonHeight());
        btlp.setMargins(5, 5, 5, 5);
        button.setLayoutParams(btlp);
        button.setBackgroundColor(Color.parseColor("#008477"));
        button.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        if(lookup == null){
            button.setText(getArgs().getNullCaption());
            button.setTag(null);
        }
        else{
            button.setText(lookup.getName());
            button.setTag(lookup);
        }



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLookupChanged(lookup);
            }
        });
        container.addView(button);
    }

    public  int GetButtonWidth(){
        return  223;
    }
    public  int GetButtonHeight(){
        return  190;
    }

    public void doLookupChanged(DataService.Lookup lookup){
        if(onLookupChanged == null)super.doOk();
        else if(onLookupChanged.apply(lookup))super.doOk();
    }

    public Function<DataService.Lookup,Boolean> onLookupChanged;

    public Function<DataService.Lookup, Boolean> getOnLookupChanged() {
        return onLookupChanged;
    }

    public PopupLookup setOnLookupChanged(Function<DataService.Lookup, Boolean> onLookupChanged) {
        this.onLookupChanged = onLookupChanged;
        return this;
    }

    public static class  PopupLookupArgs extends PopupArgs<PopupLookupArgs> {
        public PopupLookupArgs( String header, List<DataService.Lookup> lookups,Long value){
            super(header);
            setCancelButton("Close");
            setValue(value);
            if(lookups == null)setLookups(new ArrayList<DataService.Lookup>());
            else setLookups(lookups);
        }
        private List<DataService.Lookup> Lookups;
        public List<DataService.Lookup> getLookups() {
            return Lookups;
        }

        private String NullCaption = null;

        public String getNullCaption() {
            return NullCaption;
        }

        public PopupLookupArgs setNullCaption(String nullCaption) {
            NullCaption = nullCaption;
            return this;
        }

        private Long Value;
        public Long getValue() {
            return Value;
        }
        public PopupLookupArgs setValue(Long value) {
            Value = value;
            return this;
        }

        public PopupLookupArgs setLookups(List<DataService.Lookup> lookups) {
            Lookups = lookups;
            return this;
        }
    }


}
