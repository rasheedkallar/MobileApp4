package com.example.myapplication.model;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;

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

        //G.showAlertDialog(Context,"hi",  "hi");

        EditText txt = new EditText(Context);
        TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txt.setLayoutParams(txtP);
        container.addView(txt);


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

    public void onAddLookup(FlexboxLayout container, DataService.Lookup lookup){

        Button button = new Button(Context);
        button.setWidth(GetButtonWidth());
        button.setHeight(GetButtonHeight());
        button.setText(lookup.Name);
        button.setTag(lookup);
        button.setPadding(0, 0, 0, 0);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getTag() == null){
                    Pick(AlertDialog,null);
                }
                else{
                    DataService.Lookup l = (DataService.Lookup)view.getTag();
                    Pick(AlertDialog,l);
                }

            }
        });
        container.addView(button);
    }

    public void Pick(DialogInterface dialog,DataService.Lookup lookup){
        getListener().onPick(dialog,lookup);
        AlertDialog.dismiss();
    }

    public  int GetButtonWidth(){
        return  100;
    }
    public  int GetButtonHeight(){
        return  200;
    }
    public  static abstract  class  onFormPopupLookupListener extends  Popup.onFormPopupListener{

        @Override
        public PopupLookup getPopup(){
            return  (PopupLookup)super.getPopup();
        }

        public abstract void onPick(DialogInterface dialog,DataService.Lookup lookup);
    }

}
