package com.example.myapplication.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.BaseActivity;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class PopupBase<T extends PopupBase<T,U>,U extends PopupBase.PopupArgs<U>> extends DialogFragment {
    public U getArgs() {
        Bundle arg = getArguments();
        return (U)arg.get("args");
    }
    public PopupBase<T,U> setArgs(U args) {
        Serializable obj = (Serializable)args;
        Bundle a = new Bundle();
        a.putSerializable("args", obj);
        setArguments(a);
        return this;
    }
    private BaseActivity RootActivity;
    public BaseActivity getRootActivity(){
        return RootActivity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //OnAction = getArgs().getOnAction();
        try {
            RootActivity=(BaseActivity) context;
            RootActivity.Popups.add(this);

        }
        catch (ClassCastException e) {
            System.out.println(e.getMessage());
            Log.d("MyDialog", "Popup allowed only under BaseActivity");
        }
    }
    @Override
    public void onDestroy() {
        if(RootActivity.Popups.contains(this))
            RootActivity.Popups.remove(this);
        super.onDestroy();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if(RootActivity.Popups.contains(this))
            RootActivity.Popups.remove(this);
        super.onDismiss(dialog);
    }

    private AlertDialog Popup;

    public AlertDialog getPopup() {
        return Popup;
    }

    public LinearLayout container;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && getArgs().getCancelOnDestroyView()) doCancel();
    }

    protected TextView Title;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        PopupBase.PopupArgs args = getArgs();
        ScrollView sv = null;
        LinearLayout linearLayout  = null;
        linearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lllP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //linearLayout.setPadding(2, 2, 2, 2);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(lllP);
        if(args.getEnableScroll()) {
            sv = new ScrollView(getContext());
            ScrollView.LayoutParams scP = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
            scP.setLayoutDirection(LinearLayout.HORIZONTAL);
            sv.setLayoutParams(scP);
            sv.addView(linearLayout);
        }
        container = linearLayout;

        Title = new TextView(getActivity());
        TableLayout.LayoutParams cParam = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500);
        Title.setPadding(10, 10, 10, 10);
        Title.setLayoutParams(cParam);
        Title.setText(args.getHeader());
        Title.setTextSize(30);
        Title.setTextColor(Color.parseColor("#8CD0E4"));
        Title.setBackgroundColor(Color.parseColor("#225C6E"));

        AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder(getActivity());
        AlertDialogBuilder.setCustomTitle(Title);
        if(args.getEnableScroll()) {
            AlertDialogBuilder.setView(sv);
        }
        else{
            AlertDialogBuilder.setView(linearLayout);
        }


        if (args.getOkButton() != null) {
            AlertDialogBuilder.setPositiveButton(args.getOkButton(), null);
        }
        if (args.getCancelButton() != null) {
            AlertDialogBuilder.setNegativeButton(args.getCancelButton(), null);
        }
        AddControls(linearLayout);

        Popup = AlertDialogBuilder.create();
        Popup.setCancelable(false);
        if (args.getCancelButton() != null) {
            Popup.setOnShowListener(dialog -> {
                Popup.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                    Popup.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                    doCancel();

                });
            });
        }
        if (args.getOkButton() != null) {

            Popup.setOnShowListener(dialog -> {
                Popup.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    Popup.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    doOk();
                });
            });
        }
        Popup.setCanceledOnTouchOutside(args.getCanceledOnTouchOutside());
        return Popup;
    }
    public Function<Void,Boolean> onDoOk;

    public Function<Void, Boolean> getOnDoOk() {
        return onDoOk;
    }

    public void setOnDoOk(Function<Void, Boolean> onDoOk) {
        this.onDoOk = onDoOk;
    }

    public Function<Void,Boolean> onDoCancel;

    public Function<Void, Boolean> getOnDoCancel() {
        return onDoCancel;
    }

    public void setOnDoCancel(Function<Void, Boolean> onDoCancel) {
        this.onDoOk = onDoCancel;
    }

    public void doOk(){
        if(onDoOk == null)dismiss();
        else if(onDoOk.apply(null))dismiss();
    }
    public void doCancel( ){
        if(onDoCancel == null)dismiss();
        else if(onDoCancel.apply(null))dismiss();
    }
    public abstract void AddControls(LinearLayout container);


    public static class  PopupArgs<U extends PopupArgs<U>> implements Serializable{

        public PopupArgs( String header){
            setHeader(header);
            Header = header;
        }
        private Boolean EnableScroll = false;

        public U setEnableScroll(Boolean enableScroll) {
            EnableScroll = enableScroll;
            return (U)this;
        }

        public  Boolean getEnableScroll() {
            return EnableScroll;
        }

        private boolean CancelOnDestroyView = true;
        public boolean getCancelOnDestroyView() {
            return CancelOnDestroyView;
        }
        public PopupArgs<U> setCancelOnDestroyView(boolean cancelOnDestroyView) {
            CancelOnDestroyView = cancelOnDestroyView;
            return this;
        }
        private boolean CanceledOnTouchOutside = true;
        public boolean getCanceledOnTouchOutside(){
            return CanceledOnTouchOutside;
        }

        public PopupArgs<U> setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
            CanceledOnTouchOutside = canceledOnTouchOutside;
            return  this;
        }

        private String Header;
        public U setHeader(String header) {
            Header = header;
            return (U)this;
        }
        public String getHeader() {
            return Header;
        }

        private String CancelButton = "Cancel";

        public String getCancelButton() {
            return CancelButton;
        }

        public PopupArgs<U> setCancelButton(String cancelButton) {
            CancelButton = cancelButton;
            return this;
        }

        private String OkButton = null;

        public String getOkButton() {
            return OkButton;
        }

        public U setOkButton(String okButton) {
            OkButton = okButton;
            return (U)this;
        }
    }

}


