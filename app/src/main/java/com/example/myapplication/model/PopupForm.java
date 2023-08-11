package com.example.myapplication.model;
import android.content.Context;
import android.content.DialogInterface;
import android.opengl.Visibility;
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
    public final List<Utility.Control> Controls;
    private FlexboxLayout _container;
    public final String PostUrl;
    public PopupForm(android.content.Context context, String title , List<Utility.Control> controls, String postUrl, onFormPopupFormListener listener){
        super(context,title,listener);
        Controls = controls;
        PostUrl = postUrl;
        for (Utility.Control control : Controls) {
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

        EditText txt = new EditText(Context);
        TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txt.setLayoutParams(txtP);
        txt.setVisibility(View.GONE);
        fbl.addView(txt);

        _container = fbl;
    }
    public void AddFormControl(Utility.Control control,FlexboxLayout container){
        container.addView(Utility.GenerateView(Context, control,getControlWidth(control)));
    }
    public  int getControlWidth(Utility.Control control){
        if(control.DoubleSize) return 922;
        else return 460;
    }

    public  static abstract  class  onFormPopupFormListener extends  Popup.onFormPopupListener{
        @Override
        public PopupForm getPopup(){
            return  (PopupForm)super.getPopup();
        }
    }


}
