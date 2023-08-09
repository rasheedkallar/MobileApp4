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
        linearLayout.setPadding(0, 0, 0, 0);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(lllP);

        AlertDialogBuilder = new AlertDialog.Builder(context);
        AlertDialogBuilder.setView(linearLayout);
        if(getOkButton() != null) {
            AlertDialogBuilder.setPositiveButton(getOkButton(),new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onClickOk(dialog,which);
                }
            });
        }
        if(getCancelButton() != null) {
            AlertDialogBuilder.setNegativeButton(getCancelButton(),new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onClickCancel(dialog,which);
                }
            });
        }

        onAddControl(linearLayout);
        AlertDialog = AlertDialogBuilder.create();
        AlertDialog.setTitle(title);
        AlertDialog.setCancelable(false);
        AlertDialog.show();
    }
    public abstract void onAddControl(LinearLayout container);
    public  static abstract   class  onFormPopupListener{
        private Popup _popup;
        public Popup getPopup(){
            return  _popup;
        }
        public void onClickCancel(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
        public void onClickOk(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

}
