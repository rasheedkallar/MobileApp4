package com.example.myapplication.model;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import kotlin.jvm.functions.Function2;


public class PopupInput extends PopupBase<PopupInput, PopupInput.PopupInputArgs> {

    public static PopupInput create(String header, Function<String,Boolean> onInput){
        PopupInput popup = new PopupInput();
        popup.setOnInput(onInput);
        popup.setArgs(new PopupInput.PopupInputArgs(header));
        return popup;
    }
    public static PopupInput create(PopupInput.PopupInputArgs args, Function<String,Boolean> onInput){
        PopupInput popup = new PopupInput();
        popup.setOnInput(onInput);
        popup.setArgs(args);
        return popup;
    }
    public Function<String,Boolean> OnInput;
    public Function<String, Boolean> getOnInput() {
        return OnInput;
    }
    public PopupInput setOnInput(Function<String, Boolean> onInput) {
        OnInput = onInput;
        return this;
    }
    public Function2<Integer,String,Boolean> InputListener;
    public PopupInput setInputListener(Function2<Integer,String,Boolean> inputListener) {
        InputListener = inputListener;
        return this;
    }
    public Function2<Integer,String,Boolean> getInputListener() {
        return InputListener;
    }
    protected EditText SearchEditText;
    @Override
    public void doOk() {
        if(OnInput.apply(SearchEditText.getText().toString())) PopupInput.super.doOk();
    }

    @Override
    public void AddControls(LinearLayout container) {
        SearchEditText = new EditText(getContext());
        TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        SearchEditText.setLayoutParams(txtP);
        SearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() >= start + after && start + after >1) {
                    int ascii = (int) s.charAt(start + after -1);
                    if(InputListener != null){
                        String str = null;
                        if(SearchEditText.getText() != null)str= SearchEditText.getText().toString();
                        if(InputListener.invoke (ascii,str))PopupInput.super.doOk();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        container.addView(SearchEditText);
        SearchEditText.requestFocus();
    }
    public static class  PopupInputArgs extends PopupArgs<PopupInputArgs> {
        public PopupInputArgs(String header){
            super(header);
            setOkButton("Ok");
        }
        private boolean AllowNull= false;
        public PopupInputArgs setAllowNull(boolean allowNull) {
            AllowNull = allowNull;
            return this;
        }
    }
}
