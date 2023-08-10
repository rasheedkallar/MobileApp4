package com.example.myapplication.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.myapplication.R;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONObject;

import java.util.List;

public abstract class Popup {
    public final Context Context;
    public final String Title;
    public final AlertDialog AlertDialog;
    public final AlertDialog.Builder AlertDialogBuilder;

    private onFormPopupListener _listener;

    public onFormPopupListener getListener(){
        return _listener;
    }

    public String getCancelButton()
    {
        return "Cancel";
    }
    public String getOkButton()
    {
        return null;
    }
    public Popup(Context context, String title , onFormPopupListener listener){

        listener._popup = this;
        _listener = listener;
        Context = context;
        Title = title;

        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams lllP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lllP.setMargins(0, 0, 0, 0);
        linearLayout.setPadding(5, 5, 5, 5);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(lllP);

        AlertDialogBuilder = new AlertDialog.Builder(context);
        AlertDialogBuilder.setView(linearLayout);
        if(getOkButton() != null) {
            AlertDialogBuilder.setPositiveButton(getOkButton(),new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DoOk(dialog,which);
                }
            });
        }
        if(getCancelButton() != null) {
            AlertDialogBuilder.setNegativeButton(getCancelButton(),new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DoCancel(dialog,which);
                }
            });
        }

        AddControls(linearLayout);
        AlertDialog = AlertDialogBuilder.create();
        AlertDialog.setTitle(title);
        AlertDialog.setCancelable(false);
        AlertDialog.show();
    }
    public void DoOk(DialogInterface dialog, int which){
        if(getListener().onDoOk())dialog.dismiss();
    }
    public void DoCancel(DialogInterface dialog, int which){
        if(getListener().onDoCancel())dialog.dismiss();
    }

    public abstract void AddControls(LinearLayout container);
    public  static abstract   class  onFormPopupListener{
        private Popup _popup;
        public Popup getPopup(){
            return  _popup;
        }
        public boolean onDoCancel() {
            return true;

        }
        public boolean onDoOk() {
            return true;

        }
    }

}