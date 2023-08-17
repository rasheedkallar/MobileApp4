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

import java.util.List;

public class PopupLookup extends Popup{
    public final List<DataService.Lookup> Lookups;
    private FlexboxLayout _container;
    @Override
    public onFormPopupLookupListener getListener(){
        return (onFormPopupLookupListener)super.getListener();
    }
    public PopupLookup(Context context,  String title , List<DataService.Lookup> lookups,onFormPopupLookupListener listener)
    {
        super(context,title,listener);
        Lookups = lookups;

        for (DataService.Lookup lookup : Lookups) {
            onAddLookup(_container,lookup);
        }
    }
    @Override
    public void AddControls(LinearLayout container) {

        ScrollView sv = new ScrollView(Context);
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);


        FlexboxLayout fbl = new FlexboxLayout(Context);
        TableLayout.LayoutParams fblP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fbl.setLayoutParams(fblP);
        fbl.setFlexWrap(FlexWrap.WRAP);


        EditText txt = new EditText(Context);
        TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txt.setLayoutParams(txtP);
        txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        container.addView(txt);
        sv.addView(fbl);
        container.addView(sv);
        _container = fbl;
    }

    public void onAddLookup(FlexboxLayout container, DataService.Lookup lookup){

        Button button = new Button(Context);
        LinearLayout.LayoutParams btlp= new LinearLayout.LayoutParams(GetButtonWidth(), GetButtonHeight());
        btlp.setMargins(5, 5, 5, 5);
        button.setLayoutParams(btlp);
        button.setBackgroundColor(Color.parseColor("#008477"));
        button.setTextColor(ContextCompat.getColor(Context, R.color.white));
        button.setText(lookup.Name);
        button.setTag(lookup);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getTag() == null){
                    Pick(null);
                }
                else{
                    DataService.Lookup l = (DataService.Lookup)view.getTag();
                    Pick(l);
                }

            }
        });
        container.addView(button);
    }

    public void Pick(DataService.Lookup lookup){
        getListener().onPick(lookup);
        super.DoOk();
    }

    public  int GetButtonWidth(){
        return  223;
    }
    public  int GetButtonHeight(){
        return  190;
    }
    public  static abstract  class  onFormPopupLookupListener extends  Popup.onFormPopupListener{

        @Override
        public PopupLookup getPopup(){
            return  (PopupLookup)super.getPopup();
        }

        public abstract boolean onPick(DataService.Lookup lookup);
    }

}
