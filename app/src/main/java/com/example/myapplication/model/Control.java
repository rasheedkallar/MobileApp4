package com.example.myapplication.model;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.renderscript.Sampler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.method.DigitsKeyListener;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.example.myapplication.BaseActivity;

import com.example.myapplication.R;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Control {
    public static String ACTION_SEARCH = "Search";
    public static String ACTION_ADD = "Add";
    public static String ACTION_ADD_SUB = "AddSub";
    public static String ACTION_EDIT = "Edit";
    public static String ACTION_DELETE = "Delete";
    public static String ACTION_REFRESH = "Refresh";
    public static String ACTION_CAMERA = "Camera";
    public static String ACTION_GALLERY = "Gallery";
    public static String ACTION_FILTER = "Filter";
    public static int CONTROL_SIZE_DOUBLE = -20;
    public static int CONTROL_SIZE_SINGLE = -10;

    public static HiddenControl getHiddenControl( String name, Serializable value){
        return new HiddenControl(name,value);
    }
    public static ImageControl getImageControl( String name, String caption,String entityName){
        return new ImageControl(name,caption,entityName);
    }

    public static LookupListControl getLookupListControl( String name, String caption, String displayField,List<DataService.Lookup> lookups){
        return new LookupListControl(name,caption,displayField,lookups);
    }

    public static EditTextControl getEditTextControl( String name, String caption){
        return new EditTextControl(name,caption);
    }

    public static DateTimeControl getDateTimeControl( String name, String caption){
        return new DateTimeControl(name,caption);
    }
    public static DateControl getDateControl( String name, String caption){
        return new DateControl(name,caption);
    }
    public static EditDecimalControl getEditDecimalControl( String name, String caption){
        return new EditDecimalControl(name,caption);
    }

    public static SearchControl getSearchControl( String name, String caption,List<Control.ControlBase> controls,String entityName,String displayFieldName){
        return new SearchControl(name,caption,controls,entityName,displayFieldName);
    }

    public static abstract class DetailedControl extends DetailedControlBase<DetailedControl> {
        public DetailedControl(String name,String caption,String entityName,String foreignFieldName) {
            super(name, caption,entityName,foreignFieldName);
            ArrayList<ActionButton> buttons = new ArrayList<ActionButton>();
            buttons.add(new ActionButton(Control.ACTION_ADD));
            buttons.add(new ActionButton(Control.ACTION_EDIT).setEnabled(false));
            buttons.add(new ActionButton(Control.ACTION_DELETE).setEnabled(false));
            buttons.add(new ActionButton(Control.ACTION_REFRESH));
            setButtons(buttons);
        }

        protected transient TableLayout table_layout;
        protected transient FlexboxLayout filter_layout;
        protected transient List<ControlBase> FilterControls = null;
        public List<ControlBase> getFilterControls() {
            if(FilterControls == null)FilterControls= getControls(Control.ACTION_FILTER);
            return FilterControls;
        }
        public boolean doAfterSaved(Long id,boolean defaultClose){
            setSelectedId(id);
            refreshGrid(table_layout);
            return defaultClose;
        }
        @Override
        public void addValueView(ViewGroup container) {

            filter_layout = new FlexboxLayout(container.getContext());
            TableLayout.LayoutParams fblP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            filter_layout.setLayoutParams(fblP);
            filter_layout.setFlexWrap(FlexWrap.WRAP);
            container.addView(filter_layout);
            table_layout = new TableLayout(container.getContext());
            addHeaderRow(table_layout,getControls(ACTION_REFRESH));

            TableLayout.LayoutParams tlLp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            table_layout.setLayoutParams(tlLp);
            container.addView(table_layout);

            List<ControlBase> controls = getFilterControls();
            if(controls != null){
                for (int i = 0; i < controls.size(); i++) {
                    controls.get(i).addView(filter_layout);
                }
            }

            if(getForeignFieldName() == null || getForeignFieldName().length() == 0 || (getParentId() != null && getParentId() >0))
                refreshGrid(table_layout);
        }
        protected String getRefreshUrl(){
            List<String> values = new ArrayList<>();
            if(getForeignFieldName() != null && getForeignFieldName().length() != 0){
                if(getParentId() == null || getParentId() == 0L){
                    values.add(getForeignFieldName() + "=");
                }
                else{
                    values.add(getForeignFieldName() + "=" + getParentId().toString());
                }
            }
            if(FilterControls != null && FilterControls.size() != 0){
                for (ControlBase c : FilterControls) {
                    values.add(c.getUrlParam());
                }
            }
            if(values.size() ==0)return getEntityName();
            else return getEntityName() + "?" + String.join("&",values);
        }
        protected  void onRowSelected(TableRow row){
            JSONObject obj = (JSONObject)row.getTag();
            try
            {
                setSelectedId(Long.parseLong(obj.get(getIdFieldName()).toString()));

            }
            catch (JSONException e){
                setSelectedId(null);
            }
            selectRow(row,header_row,table_layout);
        }
        private void selectRow(TableRow row, TableRow header, TableLayout tableLayout) {
            row.setBackground(getSelectionBackground());
            if(getActionButton(Control.ACTION_EDIT) != null) getActionButton(Control.ACTION_EDIT).setEnabled(true);
            if(getActionButton(Control.ACTION_DELETE) != null)getActionButton(Control.ACTION_DELETE).setEnabled(true);
            for (int i = 0; i < tableLayout.getChildCount(); i++) {
                TableRow otherRow = (TableRow)tableLayout.getChildAt(i);
                if(row != otherRow && header != otherRow){
                    otherRow.setBackground(null);
                }
            }
        }
        private transient TableRow header_row;
        private void addHeaderRow(TableLayout table,ArrayList<ControlBase> controls){
            header_row = new TableRow(table.getContext());
            table.addView(header_row);
            TableLayout.LayoutParams headerP = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            header_row.setLayoutParams(headerP);
            //header_row.setBackgroundColor(Color.parseColor("#008477"));
            //header_row.setPadding(5, 5, 5, 5);
            for (com.example.myapplication.model.Control.ControlBase control : controls) {
                control.addListHeader(header_row);
            }
        }
        public void refreshGrid(TableLayout table){
            table_layout = table;
            ArrayList<ControlBase> controls = getControls(ACTION_REFRESH);
            String id_field_name = getIdFieldName();
            if(controls != null && controls.size() != 0){
                new DataService().getArray(getRefreshUrl(), new Function<JSONArray, Void>() {
                    @Override
                    public Void apply(JSONArray jsonArray) {
                        try {

                            if(getValue() == null)setValue(new ArrayList<Long>());
                            getValue().clear();
                            table.removeAllViews();

                            addHeaderRow(table,controls);
                            boolean selectionFound = false;
                            final TableLayout parentTable = table;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = (JSONObject) jsonArray.get(i);
                                getValue().add(Long.parseLong(obj.get(getIdFieldName()).toString()));
                                TableRow item = new TableRow(table.getContext());
                                table.addView(item);
                                TableLayout.LayoutParams itemP = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                //item.setWeightSum(controls.size());
                                item.setLayoutParams(itemP);
                                //item.setPadding(5, 5, 5, 5);
                                item.setTag(obj);
                                item.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        //try {
                                            onRowSelected(item);
                                        //}
                                        //catch (Exception e){
                                        //    setSelectedId(null);
                                        //}
                                    }
                                });
                                for (com.example.myapplication.model.Control.ControlBase control : controls) {
                                    control.addListDetails(item,obj);
                                }
                                if(getSelectedId() != null && Long.parseLong(obj.get(getIdFieldName()).toString()) == getSelectedId()){

                                    try {
                                        setSelectedId(Long.parseLong(obj.get(getIdFieldName()).toString()));
                                        selectRow(item, header_row, parentTable);
                                        selectionFound =true;

                                    }
                                    catch (Exception e){
                                        setSelectedId(null);
                                        selectionFound =false;
                                    }
                                }

                            }
                            if(selectionFound){
                                if(getActionButton(Control.ACTION_EDIT) != null) getActionButton(Control.ACTION_EDIT).setEnabled(true);
                                if(getActionButton(Control.ACTION_DELETE) != null)getActionButton(Control.ACTION_DELETE).setEnabled(true);
                            }
                            else{
                                if(getActionButton(Control.ACTION_EDIT) != null)getActionButton(Control.ACTION_EDIT).setEnabled(false);
                                if(getActionButton(Control.ACTION_DELETE) != null)getActionButton(Control.ACTION_DELETE).setEnabled(false);
                            }

                        }
                        catch (JSONException  e)
                        {
                            Toast.makeText(getRootActivity(), "GetListData Failed," + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        return null;
                    }
                },table.getContext());
            }
        }
        @Override
        public void onButtonClick(ActionButton action){
            BaseActivity activity = (BaseActivity)action.button.getContext();
            if(action.getName().equals(Control.ACTION_ADD)){
                ArrayList<ControlBase> controls = getControls(action.getName());
                if(getForeignFieldName() != null && getForeignFieldName().length() != 0 && getParentId() != null && getParentId() != 0L){
                    controls.add(Control.getHiddenControl(getForeignFieldName(),getParentId()));
                }
                new PopupForm().setArgs(new PopupForm.PopupFormArgs(getCaption() + " Add",controls,getEntityName(),0L)).show( activity.getSupportFragmentManager(),null);
            }
            else if(action.getName().equals(Control.ACTION_EDIT)){
                //ArrayList<ControlBase> controls = getControls(action);
                new DataService().getById(activity, getEntityName(), getSelectedId(), new DataService.GetByIdResponse() {
                    @Override
                    public void onSuccess(JSONObject data) {

                        ArrayList<ControlBase> controls = getControls(action.getName());
                        if(getForeignFieldName() != null && getForeignFieldName().length() != 0 && getParentId() != null && getParentId() != 0L){
                            controls.add(Control.getHiddenControl(getForeignFieldName(),getParentId()));
                        }
                        for (int i = 0; i < controls.size(); i++) {
                            controls.get(i).readValueJSONObject(data);
                        }
                        //Utility.applyValues(data,controls);
                        for (int i = 0; i < controls.size(); i++) {
                            if(DetailedControlBase.class.isAssignableFrom(controls.get(i).getClass())){
                                DetailedControlBase ctrl = (DetailedControlBase)controls.get(i);
                                ctrl.setParentId(getSelectedId());
                            }
                        }
                        new PopupForm().setArgs(new PopupForm.PopupFormArgs(getCaption() + " Edit",controls,getEntityName(),getSelectedId())).show( activity.getSupportFragmentManager(),null);
                    }
                });
            }
            else if(action.getName().equals(Control.ACTION_DELETE)){
                PopupConfirmation.create("Delete Confirmation", "Are you sure you want to delete?", (unused)-> {
                    new DataService().deleteById(activity, getEntityName(), getSelectedId(), new DataService.DeleteByIdResponse() {
                        @Override
                        public void onSuccess(Boolean deleted) {
                            setSelectedId(null);
                            refreshGrid(table_layout);
                        }
                    });
                    return true;
                }).show(((BaseActivity)action.button.getContext()).getSupportFragmentManager(), null);

            }
            else if(action.getName().equals(Control.ACTION_REFRESH)){
                refreshGrid(table_layout);
            }
        }
        protected abstract ArrayList<ControlBase> getControls(String action);
    }
    public static class ImageControl extends DetailedControlBase<ImageControl> {
        public ImageControl(String name, String caption,String entityName){
            super(name, caption,entityName,"RefId");
            setValue(new ArrayList<Long>());
            ArrayList<ActionButton> buttons = new ArrayList<ActionButton>();
            buttons.add(new ActionButton(Control.ACTION_CAMERA));
            buttons.add(new ActionButton(Control.ACTION_GALLERY));
            buttons.add(new ActionButton(Control.ACTION_DELETE).setEnabled(false));
            setButtons(buttons);

        }
        public void onCapturedImage(int action, Bitmap image, Long id){
            addImage(id);
            getActionButton(Control.ACTION_DELETE).setEnabled(false);

            //delete_button.setEnabled(false);
            for (int i = 0; i < main_layout.getChildCount(); i++) {
                ImageView iv = (ImageView)main_layout.getChildAt(i);
                Long vId = (Long)iv.getTag();
                if(id.equals(vId)) {
                    setSelectedId(id);
                    iv.setBackgroundColor(Color.parseColor("#225C6E"));
                    getActionButton(Control.ACTION_DELETE).setEnabled(true);
                }
                else{
                    iv.setBackgroundColor(Color.parseColor("#8CD0E4"));
                }
            }
        }

        @Override
        public void onButtonClick(ActionButton button) {
            if(button.Name.equals(Control.ACTION_DELETE)){
                PopupConfirmation.create("Delete Confirmation", "Are you sure you want to delete?", (unused)->{
                    new DataService().deleteById(button.button.getContext(), "RefFile", getSelectedId(), new DataService.DeleteByIdResponse() {
                        @Override
                        public void onSuccess(Boolean deleted) {
                            if(deleted) {
                                getValue().remove(getSelectedId());
                                for (int i = 0; i < main_layout.getChildCount(); i++) {
                                    Object id = main_layout.getChildAt(i).getTag();
                                    if(id != null && id.equals(getSelectedId())){
                                        main_layout.removeView(main_layout.getChildAt(i));
                                        break;
                                    }
                                }
                                getActionButton(Control.ACTION_DELETE).setEnabled(false);
                            }
                        }
                    });
                    return true;
                }).show(getRootActivity().getSupportFragmentManager(),null);
            }
            else if(button.Name.equals(Control.ACTION_CAMERA)) {
                getRootActivity().captureImage(BaseActivity.TAKE_IMAGE_FROM_CAMERA,getEntityName(),getParentId());
            }
            else if(button.Name.equals(Control.ACTION_GALLERY)) {
                getRootActivity().captureImage(BaseActivity.TAKE_IMAGE_FROM_GALLERY,getEntityName(),getParentId());
            }
        }



        protected transient FlexboxLayout main_layout;
        @Override
        public void addValueView(ViewGroup container) {

            main_layout = new FlexboxLayout(container.getContext());
            TableLayout.LayoutParams fblP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            main_layout.setLayoutParams(fblP);
            main_layout.setFlexWrap(FlexWrap.WRAP);
            container.addView(main_layout);
            for (int i = 0; i < getValue().size(); i++) {
                addImage(getValue().get(i));
            }

        }
        private ImageView GetImageView(long id){
            ImageView imageView = new ImageView(main_layout.getContext());
            FlexboxLayout.LayoutParams lllP = new FlexboxLayout.LayoutParams(230, 230);
            lllP.setMargins(2,2,2,2);
            imageView.setLayoutParams(lllP);
            imageView.setTag(id);
            if(id == getSelectedId()){
                imageView.setBackgroundColor(Color.parseColor("#225C6E"));
                getActionButton(Control.ACTION_DELETE).setEnabled(true);
            }
            else{
                imageView.setBackgroundColor(Color.parseColor("#8CD0E4"));
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSelectedId(null);
                    getActionButton(Control.ACTION_DELETE).setEnabled(false);
                    for (int i = 0; i < main_layout.getChildCount(); i++) {
                        ImageView iv = (ImageView)main_layout.getChildAt(i);
                        if(view == iv) {
                            setSelectedId(id);
                            iv.setBackgroundColor(Color.parseColor("#225C6E"));
                            getActionButton(Control.ACTION_DELETE).setEnabled(true);
                        }
                        else{
                            iv.setBackgroundColor(Color.parseColor("#8CD0E4"));
                        }
                    }
                }
            });

            //imageView.setTag(id);
            main_layout.addView(imageView);
            return imageView;
        }
        private void addImage(long id) {
            if(!getValue().contains(id)){
                getValue().add(id);
            }
            ImageView imageView = GetImageView(id);
            new DataService().get("refFile/" + id, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                    imageView.setImageBitmap(bmp);

                    System.out.println(main_layout.getChildCount());
                }
                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
    }
    public static abstract class DetailedControlBase<T extends ControlBase<T,ArrayList<Long>>> extends ControlBase<T, ArrayList<Long>> {

        public static final int HEADER_CONTAINER_ID = 1001;
        public static final int VALUE_CONTAINER_ID = 1002;
        public static final int ACTION_CONTAINER_ID = 1003;



        protected GradientDrawable getHeaderBackground(){
            GradientDrawable orderStyle = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {Color.parseColor("#04263C"),Color.parseColor("#0D78BF"),Color.parseColor("#0D78BF"),Color.parseColor("#04263C")});
            orderStyle.setCornerRadius(0f);
            return orderStyle;
        }

        @Override
        protected Drawable getEditorBackground() {
            return null;
        }

        private boolean initialFocus = false;
        @Override
        protected void requestFocus() {
            if(getButtons() != null && getButtons().size() > 0 && getButtons().get(0).button != null)getButtons().get(0).button.requestFocus();
            else initialFocus = true;
        }


        public DetailedControlBase(String name,String caption,String entityName,String foreignFieldName){
            super(name,caption);
            setEntityName(entityName);
            setForeignFieldName(foreignFieldName);
            setControlSize(RelativeLayout.LayoutParams.MATCH_PARENT);
        }
        public ActionButton getActionButton(String action){
            Optional<ActionButton> button = this.getButtons().stream().filter(i-> i.Name.equals(action)).findFirst();
            if(button.isPresent())return  button.get();
            else return  null;
        }


        @Override
        public boolean validate() {
            boolean valid = false;
            if(getParentId() == null || getParentId() == 0)valid = true;
            else if(!getIsRequired())valid= true;
            else if(getValue() != null && getValue().size() >0)valid = true;
            if(CaptionTextView!=null) {
                if(rl != null && valid)rl.setBackground(getHeaderBackground());
                else if(rl != null)rl.setBackground(getHeaderErrorBackground());
                else if(CaptionTextView != null && valid)CaptionTextView.setBackground(getHeaderBackground());
                else if(CaptionTextView != null )CaptionTextView.setBackground(getHeaderErrorBackground());
            }
            return valid;
        }
        protected transient  TextView CaptionTextView;


        protected transient  LinearLayout ActionLayout;

        private transient RelativeLayout rl = null;









        @Override
        protected void addContentView(ViewGroup container) {

            if(getButtons() != null && getButtons().size() >0){
                rl = new RelativeLayout(container.getContext());
                LinearLayout.LayoutParams rlP = new LinearLayout.LayoutParams(getWidth(), RelativeLayout.LayoutParams.WRAP_CONTENT);
                rl.setBackground(getHeaderBackground());
                rl.setLayoutParams(rlP);
                container.addView(rl);

                ActionLayout = new LinearLayout(container.getContext());
                RelativeLayout.LayoutParams llActionP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                llActionP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                llActionP.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                ActionLayout.setPadding(0,0,0,5);
                ActionLayout.setLayoutParams(llActionP);
                ActionLayout.setId(ACTION_CONTAINER_ID);

                for (int i = 0; i < getButtons().size(); i++) {
                    final ActionButton button = getButtons().get(i);
                    button.addView(ActionLayout, new Function<Button, Void>() {
                        @Override
                        public Void apply(Button btn) {
                            onButtonClick(button);
                            return null;
                        }
                    });
                }
                rl.addView(ActionLayout);
            }

            CaptionTextView = new TextView(container.getContext());
            RelativeLayout.LayoutParams CaptionTextViewP= new  RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


            CaptionTextView.setPadding(10, 0, 5, 10);
            CaptionTextView.setLayoutParams(CaptionTextViewP);
            CaptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,22);
            CaptionTextView.setText(getCaption());
            CaptionTextView.setTextColor(ContextCompat.getColor(container.getContext(), R.color.white));

            CaptionTextView.setId(HEADER_CONTAINER_ID);
            if(getButtons() != null && getButtons().size() >0) {
                CaptionTextViewP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                CaptionTextViewP.addRule(RelativeLayout.LEFT_OF,ACTION_CONTAINER_ID);
                CaptionTextViewP.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                rl.addView(CaptionTextView);
            }
            else{
                CaptionTextView.setBackground(getHeaderBackground());
                container.addView(CaptionTextView);
            }
            LinearLayout llValue = new LinearLayout(container.getContext());
            RelativeLayout.LayoutParams llValueP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            llValueP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            llValueP.addRule(RelativeLayout.BELOW,HEADER_CONTAINER_ID);
            if(getCaption() != null)llValueP.addRule(RelativeLayout.BELOW,ACTION_CONTAINER_ID);
            llValue.setLayoutParams(llValueP);
            llValue.setId(VALUE_CONTAINER_ID);
            llValue.setOrientation(LinearLayout.VERTICAL);
            if(EnableScroll){
                ScrollView sv = new ScrollView(container.getContext());
                RelativeLayout.LayoutParams svP= new  RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                svP.setLayoutDirection(LinearLayout.HORIZONTAL);
                sv.setLayoutParams(svP);
                sv.addView(llValue);
                container.addView(sv);

            }else{
                container.addView(llValue);
            }



            addValueView(llValue);

            if(initialFocus && getButtons() != null && getButtons().size() > 0 && getButtons().get(0).button != null)getButtons().get(0).button.requestFocus();

        }
        @Override
        public void updateSaveParameters(RequestParams params) {
            params.put(getForeignFieldName(),getParentId());
        }


        private boolean  EnableScroll = false;

        public boolean getEnableScroll() {
            return EnableScroll;
        }

        public T setEnableScroll(boolean enableScroll) {
            EnableScroll = enableScroll;
            return (T)this;
        }



        private String IdFieldName = "Id";

        public String getIdFieldName() {
            return IdFieldName;
        }

        public T setIdFieldName(String idFieldName) {
            IdFieldName = idFieldName;
            return (T)this;
        }
        private Long SelectedId = 0L;
        protected void setSelectedId(Long id) {
            SelectedId = id;
        }

        public Long getSelectedId() {
            return SelectedId;
        }
        private Long ParentId;
        public void changeVisibility(boolean visible){
            if(visible && RootLayout != null)RootLayout.setVisibility(View.VISIBLE);
            else if(RootLayout != null) RootLayout.setVisibility(View.GONE);

        }
        private String ForeignFieldName;

        public String getForeignFieldName() {
            return ForeignFieldName;
        }

        public T setForeignFieldName(String foreignFieldName) {
            ForeignFieldName = foreignFieldName;
            return (T)this;
        }

        public T setParentId(Long id) {

            ParentId = id;
            return (T)this;
        }
        public Long getParentId() {
            return ParentId;
        }

        private String EntityName;

        public String getEntityName() {
            return EntityName;
        }

        public T setEntityName(String entityName) {
            EntityName = entityName;
            return (T)this;
        }
        @Override
        public void valueChange(ArrayList<Long> oldValue, ArrayList<Long> newValue) {

        }
        @Override
        protected ArrayList<Long> convertValue(Object value) {
            if(value == null) return new ArrayList<Long>();
            else {
                if (JSONArray.class.isAssignableFrom(value.getClass())) {
                    JSONArray jArray = (JSONArray) value;
                    ArrayList a = new ArrayList<Long>();
                    for (int i = 0; i < jArray.length(); i++) {
                        try {
                            a.add(Long.parseLong(jArray.get(i).toString()));
                        } catch (Exception e) {

                            return new ArrayList<Long>();
                        }
                    }
                    return a;
                }
                else{
                    try {
                        return  (ArrayList<Long>)value;
                    } catch (Exception e) {

                    }
                }
            }
            return new ArrayList<Long>();
        }
    }

    public static class SearchControl extends LookupControlBase<SearchControl>{
        public SearchControl(String name, String caption,List<Control.ControlBase> controls,String entityName,String displayField) {
            super(name, caption,displayField);
            getButtons().add(new ActionButton("Search"));
            setControls(controls);
            setEntityName(entityName);
            setControlSize(Control.CONTROL_SIZE_DOUBLE);
            setIdField("Id");
            setKeywordsField("keyWords");
        }
        private transient TextView LookupTextView;
        private List<Control.ControlBase> Controls;
        public List<Control.ControlBase> getControls() {
            return Controls;
        }

        private String KeywordsField;
        public String getKeywordsField() {
            return KeywordsField;
        }
        public SearchControl setKeywordsField(String keywordsField) {
            KeywordsField = keywordsField;
            return this;
        }
        private String EntityName;
        public String getEntityName() {
            return EntityName;
        }
        public SearchControl setEntityName(String entityName) {
            EntityName = entityName;
            return this;
        }
        private String IdField;
        public String getIdField() {
            return IdField;
        }
        public SearchControl setIdField(String idField) {
            IdField = idField;
            return this;
        }

        public SearchControl setControls(List<Control.ControlBase> controls) {
            Controls = controls;
            return this;
        }

        @Override
        protected void onButtonClick(ActionButton button) {
            if(button.getName() == "Search"){
                PopupSearch ps = PopupSearch.create(getCaption(),getControls() ,getEntityName(),getDisplayField(),(lookup)->{
                    setValue(lookup);
                    return true;
                });
                ps.getArgs().setAllowNull(!getIsRequired());
                ps.getArgs().setIdField(getIdField());
                ps.getArgs().setKeywordsField(getKeywordsField());
                ps.show(((BaseActivity)button.button.getContext()).getSupportFragmentManager(),null);
            }
        }
    }
    public static class DateTimeControl extends DateControlBase<DateTimeControl>{
        @Override
        public String getFormat() {
            return "dd/MM/yy HH:mm";
        }
        public DateTimeControl( String name, String caption) {
            super( name, caption);
        }

        @Override
        protected void onBrowse(ActionButton button) {

            BaseActivity activity = (BaseActivity)button.getButton().getContext();
            PopupDate.create(getCaption(),getValue(),(value)->{
                setValue(value);
                return true;
            }).show(activity.getSupportFragmentManager(),null);
        }
    }
    public static class DateControl extends DateControlBase<DateControl>{

        @Override
        public String getFormat() {
            return "dd/MM/yy";
        }
        public DateControl( String name, String caption) {
            super( name, caption);
        }

        /*

        @Override
        public DateControl setValue(Date value) {
            Date today = value;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try{
                today = sdf.parse(sdf.format(today));
            }
            catch (Exception e){

            }
            super.setValue(today);
            return this ;
        }

         */

        @Override
        protected void onBrowse(ActionButton button) {
            BaseActivity activity = (BaseActivity)button.getButton().getContext();
            PopupDate.create(getCaption(),getValue(),(value)->{
                setValue(value);
                return true;
            }).show(activity.getSupportFragmentManager(),null);
        }
    }

    public static abstract class DateControlBase<T extends DateControlBase<T>> extends EditTextControlBase<T,Date>{

        public abstract  String getFormat();


        public DateControlBase( String name, String caption) {
            super( name, caption);
            setControlSize(CONTROL_SIZE_SINGLE);
            getButtons().add(new ActionButton("Search"));
        }
        @Override
        protected void onButtonClick(ActionButton button) {
            if(button.getName().equals("Search")){
                onBrowse(button);
            }

        }


        protected abstract void onBrowse(ActionButton button);


        @Override
        public void updateSaveParameters(RequestParams params) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            String formattedDate = dateFormat.format(getValue());
            params.put(getName(),formattedDate);
        }

        @Override
        protected Date convertValue(Object value) {
            if(value==null){
                return null;
            }
            else if(value.getClass().equals(Date.class)){
                return  (Date)value;
            }
            else{
                HashMap<String,Integer> formats = new HashMap<String,Integer>();
                formats.put("yyyy-MM-dd'T'HH:mm:ss.SSS",23);
                formats.put("yyyy-MM-dd'T'HH:mm:ss.SS",22);
                formats.put("yyyy-MM-dd'T'HH:mm:ss.S",21);
                formats.put("dd/MM/yy HH:mm:ss.S",21);
                formats.put("yyyy-MM-dd'T'HH:mm:ss",19);
                formats.put("dd/MM/yy HH:mm:ss",17);
                formats.put("yyyy-MM-dd'T'HH:mm",16);
                formats.put("dd/MM/yy HH:mm",14);
                formats.put("yyyy-MM-dd",10);
                formats.put("dd/MM/yy",8);

                Date date = null;
                String input = value.toString().trim();
                for (String format: formats.keySet()) {
                    if(input.length() == formats.get(format).intValue()){
                        DateFormat dateFormat = new SimpleDateFormat(format);
                        try{
                            date = dateFormat.parse(value.toString());
                            return date;
                        }
                        catch (ParseException e){
                        }
                        break;
                    }
                }

            }
            return null;
        }

        @Override
        protected boolean IsInputValid(String value) {
            if(value == null || value.length() == 0)return true;
            try{
                Date newDate = convertValue(value);
                if(newDate == null)return false;
                if(value.equals(getFormatValue(newDate)))return true;
                else return false;
            }
            catch (Exception e){
                return false;
            }
        }


        @Override
        public String getFormatValue(Date value) {
            if(value==null)return null;

            DateFormat format = new SimpleDateFormat(getFormat());
            return format.format(value);
        }



    }

    public static class LookupListControl extends LookupControlBase<LookupListControl> {
        public LookupListControl(String name, String caption, String displayField, List<DataService.Lookup> lookups) {
            super(name, caption, displayField);
            setLookups(lookups);
            getButtons().add(new ActionButton(Control.ACTION_SEARCH));
        }
        private List<DataService.Lookup> Lookups;
        public List<DataService.Lookup> getLookups() {
            return Lookups;
        }
        public LookupListControl setLookups(List<DataService.Lookup> lookups) {
            Lookups = lookups;
            return this;
        }
        @Override
        protected void onButtonClick(ActionButton button) {
            if(button.getName() == Control.ACTION_SEARCH){
                BaseActivity activity = (BaseActivity)button.button.getContext();
                PopupLookup.create(getCaption(),getLookups(),getValue() == null? null : getValue().getId(),(lookup)->{
                    setValue(lookup);
                    return true;
                }).show(activity.getSupportFragmentManager(),null);
            }
        }

        @Override
        protected DataService.Lookup convertValue(Object value) {
            if(value != null && Long.class.isAssignableFrom(value.getClass()) && getLookups() != null){
                Long id = (Long)value;
                Optional<DataService.Lookup> l = Lookups.stream().filter(itm->itm.getId().equals(id)).findAny();
                if(l.isPresent()){
                    return l.get();
                }
            }
            return  super.convertValue(value);
        }
    }

    public static abstract class LookupControlBase<T extends LookupControlBase<T>> extends FieldControlBase<T,DataService.Lookup>{
        protected transient TextView txtValue;
        public LookupControlBase(String name, String caption,String displayField) {
            super(name, caption);
            setDisplayField(displayField);
            setControlSize(CONTROL_SIZE_DOUBLE);
        }
        @Override
        protected Drawable getEditorBackground(){
            GradientDrawable orderStyle = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[] {Color.parseColor("#E3E0E0"),Color.parseColor("#E3E0E0")});
            orderStyle.setCornerRadius(0f);
            return orderStyle;
        }
        private boolean initialFocus = false;
        @Override
        protected void requestFocus() {
            if(getButtons() != null && getButtons().size() > 0 && getButtons().get(0).button != null)getButtons().get(0).button.requestFocus();
            else initialFocus = true;
        }


        @Override
        protected void addValueView(ViewGroup container) {
            txtValue = new TextView(container.getContext());
            txtValue.setPadding(10,0,10,0);
            RelativeLayout.LayoutParams llValueP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            txtValue.setLayoutParams(llValueP);
            if(getValue() != null)txtValue.setText(getFormatValue(getValue()));
            txtValue.setBackground(null);
            container.addView(txtValue);
            if(initialFocus && getButtons() != null && getButtons().size() > 0 && getButtons().get(0).button != null)getButtons().get(0).button.requestFocus();


        }

        @Override
        public void valueChange(DataService.Lookup oldValue, DataService.Lookup newValue) {
            if(txtValue != null)txtValue.setText(getFormatValue(getValue()));
        }
        @Override
        protected DataService.Lookup convertValue(Object value) {
            if(value == null)return null;
            else if(DataService.Lookup.class.isAssignableFrom(value.getClass()))return (DataService.Lookup)value;
            else if(Long.class.isAssignableFrom(value.getClass())){
                DataService.Lookup l = new DataService.Lookup();
                l.setId((Long) value);
                l.setName(value.toString());
                return l;
            }
            return null;
        }



        @Override
        public String getFormatValue(DataService.Lookup value) {
            if(value == null)return null;
            else return value.getName();
        }

        @Override
        protected boolean valueEqual(DataService.Lookup value1, DataService.Lookup value2) {
            boolean equal = super.valueEqual(value1, value2);
            if(!equal && value1 != null && value2 != null && value1.getId() != null && value2.getId() != null && value1.getId().equals(value2.getId())) return true;
            return equal;
        }

        private String DisplayField;
        public String getDisplayField() {
            return DisplayField;
        }
        public T setDisplayField(String displayField) {
            DisplayField = displayField;
            return (T)this;
        }

        @Override
        public void updateSaveParameters(RequestParams params) {
            if(getValue() != null)params.put(getName(),getValue().getId());
        }


        @Override
        public T readValueJSONObject(JSONObject data) {
            try{
                if(getName() != null && getName().length() != 0 && getDisplayField() != null && getDisplayField().length() != 0 && data.has(getName()) && data.has(getDisplayField())){
                    DataService.Lookup l = new DataService.Lookup();
                    String id = data.get(getName()).toString();
                    if(id == "null")return null;
                    l.setId(Long.parseLong(id));
                    l.setName("[Unknown]");
                    try{
                        if(data.has(getDisplayField())){
                            l.setName(data.get(getDisplayField()).toString());
                        }
                    }
                    catch (JSONException e){
                    }
                    return readValueObject(l);
                }
                else if(getName() != null && getName().length() != 0 && data.has(getName()) ){
                    String id = data.get(getName()).toString();
                    if(id == "null")return null;
                    return readValueObject(Long.parseLong(id));
                }
            }
            catch (JSONException e){
            }
            return super.readValueJSONObject(data);
        }
    }

    public static class EditTextControl extends EditTextControlBase<EditTextControl,String> {

        public EditTextControl(String name, String caption) {
            super(name, caption);
        }
        @Override
        protected void onButtonClick(ActionButton button) {

        }

        @Override
        public String convertValue(Object value) {
            if(value == null || value.equals("null"))return null;
            else return value.toString();
        }

        @Override
        protected boolean IsInputValid(String value) {
            return true;
        }

        @Override
        public String getFormatValue(String value) {
            return value;
        }
    }
    public static class EditDecimalControl extends EditTextControlBase<EditDecimalControl,Double> {
        private Integer DecimalPlaces = 2;
        public EditDecimalControl(String name, String caption) {
            super(name, caption);
            setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            setDigits("0123456789.-");
            setDecimalPlaces(2);
            setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }
        @Override
        protected void onButtonClick(ActionButton button) {
        }
        public Integer getDecimalPlaces() {
            return DecimalPlaces;
        }
        public EditDecimalControl setDecimalPlaces(Integer decimalPlaces) {
            DecimalPlaces = decimalPlaces;
            return this;
        }
        @Override
        public Double convertValue(Object value) {
            if(value == null || value.toString().length() == 0)return  null;
            else return Double.parseDouble(value.toString());
        }
        @Override
        protected boolean IsInputValid(String value) {
            try {
                Double.parseDouble(value);
                return true;
            }catch (Exception e){
                return false;
            }
        }
        @Override
        public String getFormatValue(Double value) {
            if(value == null)return null;
            else if(DecimalPlaces == null || DecimalPlaces <0) return value.toString();
            else
            {
                String format = "0";
                if(DecimalPlaces > 0){
                    format = format + ".";
                    for (int i = 0; i < DecimalPlaces; i++) {
                        format = format + "0";
                    }
                }
                DecimalFormat df = new DecimalFormat( format);
                return df.format(value);
            }
        }
    }

    public static class HiddenControl extends FieldControlBase<HiddenControl,Serializable>{
        public HiddenControl(String name, Serializable value) {
            super(name, null);
            setValue(value);
        }

        @Override
        protected void requestFocus() {

        }

        @Override
        protected void onButtonClick(ActionButton button) {
        }
        @Override
        public void addView(ViewGroup container) {
        }
        @Override
        public void valueChange(Serializable oldValue, Serializable newValue) {
        }
        @Override
        protected void addValueView(ViewGroup container) {
        }

        @Override
        protected Serializable convertValue(Object value) {
            return (Serializable)value;
        }
    }
    public static abstract class EditTextControlBase<T extends EditTextControlBase<T,U>,U extends Serializable> extends FieldControlBase<EditTextControlBase<T,U>,U>{

        private String Digits = null;

        public T setDigits(String digits) {
            Digits = digits;
            if(EditTextInput != null){
                EditTextInput.setKeyListener(DigitsKeyListener.getInstance(getDigits()));
            }
            return (T)this;
        }

        public String getDigits() {
            return Digits;
        }



        private int InputType = 1;

        public T setInputType(int inputType) {
            InputType = inputType;
            if(EditTextInput != null){
                EditTextInput.setInputType(getInputType());
            }
            return (T)this;
        }

        public int getInputType() {
            return InputType;
        }



        public EditTextControlBase(String name, String caption){
            super( name, caption);
        }
        private boolean InEditMode = false;
        protected transient  TextView EditTextInput;
        public TextView getEditTextInput() {
            return EditTextInput;
        }
        public T setEditTextInput(TextView editTextInput) {
            EditTextInput = editTextInput;
            return (T)this;
        }
        private boolean SelectAllOnFocus = true;

        public T setSelectAllOnFocus(boolean selectAllOnFocus) {
            SelectAllOnFocus = selectAllOnFocus;
            return (T)this;
        }
        public boolean getSelectAllOnFocus(){
            return SelectAllOnFocus;
        }

        private boolean initialFocus = false;
        @Override
        protected void requestFocus() {
            if(EditTextInput != null)EditTextInput.requestFocus();
            else initialFocus = true;
        }

        //editText.setSelectAllOnFocus(true);



        private boolean InputValid = true;
        @Override
        public boolean validate() {
            boolean valid = super.validate();
            if(!InputValid)valid = false;
            return valid;
        }
        @Override
        public void addValueView(ViewGroup container) {
            EditTextInput = new EditText(container.getContext());
            EditTextInput.setPadding(10,0,10,0);
            RelativeLayout.LayoutParams llValueP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            EditTextInput.setLayoutParams(llValueP);
            EditTextInput.setBackground(null);
            EditTextInput.setInputType(getInputType());
            EditTextInput.setTextAlignment(getTextAlignment());
            EditTextInput.setSelectAllOnFocus(getSelectAllOnFocus());

            if(initialFocus)EditTextInput.requestFocus();

            if(getDigits() != null && getDigits().length() != 0){
                EditTextInput.setKeyListener(DigitsKeyListener.getInstance(getDigits()));
            }
            EditTextInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    InEditMode = true;
                    InputValid = true;
                    U result = null;
                    if(charSequence == null)result = null;
                    else{
                        String text = charSequence.toString();
                        if(text == null || text.length() == 0)result = null;
                        else{
                            InputValid = IsInputValid(text);
                            if(InputValid)result = convertValue(text);
                        }
                    }
                    if(InputValid){
                        EditTextInput.setTextColor(ContextCompat.getColor(EditTextInput.getContext(), R.color.black));
                        setValue(result);
                    }else{
                        EditTextInput.setTextColor(ContextCompat.getColor(EditTextInput.getContext(), com.google.android.material.R.color.design_default_color_error));
                    }
                    InEditMode =false;
                }
                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            if(getValue() != null)EditTextInput.setText(getFormatValue(getValue()));
            container.addView(EditTextInput);
        }
        protected abstract boolean IsInputValid(String value);
        @Override
        public void valueChange(U oldValue, U newValue) {

            if(InEditMode == false && EditTextInput != null){
                EditTextInput.setText(getFormatValue(newValue));
            }
        }
    }
    public static abstract   class FieldControlBase<T extends FieldControlBase<T,U>,U extends Serializable> extends ControlBase<T,U> {
        public static final int HEADER_CONTAINER_ID = 1001;
        public static final int VALUE_CONTAINER_ID = 1002;
        public static final int ACTION_CONTAINER_ID = 1003;
        public FieldControlBase(String name, String caption) {
            super(name, caption);
        }

        protected transient  TextView CaptionTextView;

        @Override
        public boolean validate() {
            boolean valid = super.validate();
            if(CaptionTextView!=null && valid) CaptionTextView.setBackground(getHeaderBackground());
            else if(CaptionTextView!=null)CaptionTextView.setBackground(getHeaderErrorBackground());
            return valid;
        }

        @Override
        protected void addContentView(ViewGroup container)
        {
            if(getCaption() != null) {
                CaptionTextView = new TextView(container.getContext());
                RelativeLayout.LayoutParams CaptionTextViewP= new  RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                CaptionTextView.setPadding(10, 10, 10, 10);
                CaptionTextView.setLayoutParams(CaptionTextViewP);
                CaptionTextView.setText(getCaption());
                CaptionTextView.setTextColor(ContextCompat.getColor(container.getContext(), R.color.white));
                CaptionTextView.setBackground(getHeaderBackground());
                CaptionTextView.setId(HEADER_CONTAINER_ID);
                CaptionTextView.setTextAlignment(getTextAlignment());
                container.addView(CaptionTextView);
            }
            RelativeLayout rl = null;

            if(getButtons() != null && getButtons().size() >0){

                rl = new RelativeLayout(container.getContext());
                LinearLayout.LayoutParams rlP = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                rl.setLayoutParams(rlP);
                container.addView(rl);



                LinearLayout llAction = new LinearLayout(container.getContext());
                RelativeLayout.LayoutParams llActionP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                llActionP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                if(getCaption() != null)llActionP.addRule(RelativeLayout.BELOW,HEADER_CONTAINER_ID);
                llAction.setPadding(0,0,0,5);
                llAction.setLayoutParams(llActionP);
                llAction.setId(ACTION_CONTAINER_ID);
                llAction.setBackgroundColor(Color.parseColor("#008477"));
                llActionP.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                for (int i = 0; i < getButtons().size(); i++) {
                    final ActionButton button = getButtons().get(i);
                    button.addView(llAction, new Function<Button, Void>() {
                        @Override
                        public Void apply(Button btn) {
                            onButtonClick(button);
                            return null;
                        }
                    });
                }
                rl.addView(llAction);
            }
            LinearLayout llValue = new LinearLayout(container.getContext());
            RelativeLayout.LayoutParams llValueP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            llValue.setLayoutParams(llValueP);
            llValue.setId(VALUE_CONTAINER_ID);

            if(getButtons() != null && getButtons().size() >0){
                rl.addView(llValue);
                llValueP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                llValueP.addRule(RelativeLayout.LEFT_OF,ACTION_CONTAINER_ID);
                llValueP.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            }
            else{
                container.addView(llValue);
            }
            addValueView(llValue);
        }
    }

    public static abstract   class ControlBase<T extends ControlBase<T,U>,U extends Serializable> implements Serializable {

        protected abstract void onButtonClick(ActionButton button);




        private int ControlSize = Control.CONTROL_SIZE_SINGLE;
        public int getControlSize(){
            return ControlSize;
        }
        public T setControlSize(int size){
            ControlSize = size;
            return  (T)this;
        }

        public int getWidth(){
            int singleSize = 463;
            if(ControlSize<-5)return Math.abs(ControlSize) * singleSize / 10;
            else return ControlSize;
        }
        protected Drawable getHeaderBackground(){
            GradientDrawable orderStyle = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {Color.parseColor("#012723"),Color.parseColor("#008477"),Color.parseColor("#008477"),Color.parseColor("#012723")});
            orderStyle.setCornerRadius(0f);
            return orderStyle;
        }

        protected Drawable getEditorBackground(){
            GradientDrawable orderStyle = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[] {Color.TRANSPARENT,Color.TRANSPARENT,Color.TRANSPARENT,Color.GRAY});
            orderStyle.setCornerRadius(0f);
            return orderStyle;
        }
//#E3E0E0
        protected Drawable getSelectionBackground(){
            GradientDrawable orderStyle = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {Color.GRAY,Color.TRANSPARENT,Color.TRANSPARENT,Color.GRAY});
            orderStyle.setCornerRadius(0f);
            return orderStyle;
        }
        protected Drawable getHeaderErrorBackground(){
            GradientDrawable orderStyle = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {Color.parseColor("#500505"),Color.parseColor("#D51212"),Color.parseColor("#D51212"),Color.parseColor("#500505")});
            orderStyle.setCornerRadius(0f);
            return orderStyle;
        }



        public ControlBase(String name,String caption){
            Caption = caption;
            Name = name;


        }


        protected transient LinearLayout RootLayout;
        public void addView(ViewGroup container){
            if(getRootActivity() == null)setRootActivity((BaseActivity) container.getContext());
            RootLayout = new LinearLayout(container.getContext());
            RootLayout.setBackground(getEditorBackground());

            RelativeLayout.LayoutParams RootLayoutP = new RelativeLayout.LayoutParams(getWidth(), RelativeLayout.LayoutParams.WRAP_CONTENT);
            RootLayout.setLayoutParams(RootLayoutP);
            RootLayout.setOrientation(LinearLayout.VERTICAL);
            container.addView(RootLayout);
            addContentView(RootLayout);
        }
        protected abstract void addContentView(ViewGroup container);

        private ArrayList<ActionButton> Buttons = new ArrayList<ActionButton>();
        public ArrayList<ActionButton> getButtons() {
            return Buttons;
        }
        public T setButtons(ArrayList<ActionButton> buttons) {
            Buttons = buttons;
            return  (T)this;
        }
        private transient BaseActivity RootActivity;
        public T setRootActivity(BaseActivity rootActivity) {
            RootActivity = rootActivity;
            return (T)this;
        }
        public BaseActivity getRootActivity() {
            return RootActivity;
        }
        private boolean IsRequired = true;
        public boolean getIsRequired() {
            return IsRequired;
        }
        public T setIsRequired(boolean required) {
            IsRequired = required;
            return  (T)this;
        }
        private String Name;
        public String getName(){
            return  Name;
        }
        public T setName(String name) {
            Name = name;
            return (T)this;
        }

        private String Caption = null;
        public String getCaption(){
            return  Caption;
        }
        public T setCaption(String caption) {
            Caption = caption;
            return (T)this;
        }
        public String getFormatValue(U value)
        {
            if(value==null)return null;
            else return value.toString();
        }
        private U Value;
        public U getValue(){
            return Value;
        }
        protected boolean valueEqual(U value1, U value2){
            if(value1 == null && value2 == null)return true;
            if(value1 == null || value2 == null)return false;
            if(value1 == value2)return true;
            if(value1.equals(value2))return true;
            return false;
        }
        public  T setValue(U value){
            if(!valueEqual(Value,value)){
                U oldValue = Value;
                Value = value;
                valueChange(oldValue,value);
            }
            else{
                Value = value;
            }
            return (T)this;
        }

        public void addListHeader(TableRow row){
            TextView hc = new TextView(row.getContext());

            hc.setPadding(10, 10, 10, 10);
            TableRow.LayoutParams hcP = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1);
            hc.setLayoutParams(hcP);
            hc.setTextColor(ContextCompat.getColor(row.getContext(), R.color.white));
            hc.setText(getCaption());
            hc.setTextAlignment(TextAlignment);
            hc.setBackground(getHeaderBackground());
            row.addView(hc);
        }
        private int TextAlignment = View.TEXT_ALIGNMENT_INHERIT;
        public T setTextAlignment(int textAlignment) {
            TextAlignment = textAlignment;
            return (T)this;
        }
        public int getTextAlignment() {
            return TextAlignment;
        }
        public void addListDetails(TableRow row, JSONObject data){
            TextView hc = new TextView(row.getContext());
            TableRow.LayoutParams hcP = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1);
            hc.setLayoutParams(hcP);
            hc.setPadding(10,0,10,0);
            hc.setTextAlignment(TextAlignment);
            setValue(null);
            readValueJSONObject(data);
            hc.setText(getFormatValue(getValue()));
            row.addView(hc);
        }
        public abstract void valueChange(U oldValue, U newValue);
        private transient boolean viewCreated =false;
        protected abstract void addValueView(ViewGroup container);

        protected abstract void requestFocus();
        public  String getUrlParam(){
            Object value = getValue();
            if (value==null)return  Name + "=" ;
            else  if(value.getClass().equals(Date.class)){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                return  Name + "=" + dateFormat.format(value);
            }
            else{
                return  Name + "=" + value.toString();
            }
        }
        public T readValueObject(Object value){
            setValue(convertValue(value));
            return (T)this;
        }
        protected abstract U convertValue(Object value);
        public  T readValueJSONObject(JSONObject data){
            try{
                if(data.has(getName())){
                    Object value = data.get(getName());
                    if(value == null || value.equals(JSONObject.NULL)) readValueObject(null);
                    else readValueObject(value);
                }
            }
            catch (JSONException e){

            }
            return  (T)this;
        }
        public boolean validate(){
            if(getValue()==null && getIsRequired()) return  false;
            else return true;
        }

        public void updateSaveParameters(RequestParams params){
            params.put(getName(), getValue());
        }
    }
    public static class ActionButton implements Serializable {
        public ActionButton(String name)
        {
            setName(name);

        }
        private String Name;
        public String getName() {
            return Name;
        }
        public ActionButton setName(String name) {
            Name = name;
            return this;
        }
        protected transient Button button;
        public Button getButton() {
            return button;
        }
        private int Width = 75;
        public int getWidth() {
            return Width;
        }
        public ActionButton setWidth(int width) {
            Width = width;
            return  this;
        }
        private int Height = 75;
        public int getHeight() {
            return Height;
        }
        public ActionButton setHeight(int height) {
            Height = height;
            return this;
        }
        private boolean Enabled= true;
        public ActionButton setEnabled(boolean enabled) {
            if(Enabled != enabled && button != null){
                setFormat(button,enabled);
            }
            Enabled = enabled;
            return  this;
        }
        public boolean getEnabled(){
            return  Enabled;
        }
        public int ButtonColor = Color.parseColor("#8CD0E4");
        public int getButtonColor() {
            return ButtonColor;
        }

        public ActionButton setButtonColor(int buttonColor) {
            ButtonColor = buttonColor;
            return this;
        }
        public int ButtonColorDisable = Color.parseColor("#6D6F70");
        public ActionButton setButtonColorDisable(int buttonColorDisable) {
            ButtonColorDisable = buttonColorDisable;
            return this;
        }
        public int getButtonColorDisable() {
            return ButtonColorDisable;
        }
        private void setFormat(Button button, boolean enabled){
            int colour = getButtonColor();
            if(!enabled)colour =getButtonColorDisable();

            ArrayList<VectorDrawableCreator.PathData> paths = new ArrayList<VectorDrawableCreator.PathData>();
            paths.add(new VectorDrawableCreator.PathData("M 18 0 L 2 0 c -1.1 0 -2 0.9 -2 2 v 20 c 0 1.1 0.9 2 2 2 h 20 c 1.1 0 2 -0.9 2 -1 L 24 2 c 0 -1.1 -0.9 -2 -3 -2 z M 23 23 L 1 23 L 1 1 h 22 v 22 z", getButtonColor()));

            if(Name != null) {

                if (Name.equals(Control.ACTION_ADD))
                    paths.add(new VectorDrawableCreator.PathData("M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z", colour));
                else if (Name.equals( Control.ACTION_ADD_SUB)) {
                    paths.add(new VectorDrawableCreator.PathData("M4,6L2,6v14c0,1.1 0.9,2 2,2h14v-2L4,20L4,6zM20,2L8,2c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2L22,4c0,-1.1 -0.9,-2 -2,-2zM19,11h-4v4h-2v-4L9,11L9,9h4L13,5h2v4h4v2z", colour));
                }
                else if (Name.equals(Control.ACTION_EDIT))
                    paths.add(new VectorDrawableCreator.PathData("M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z", colour));
                else if (Name.equals(Control.ACTION_DELETE))
                    paths.add(new VectorDrawableCreator.PathData("M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z", colour));
                else if (Name.equals( Control.ACTION_REFRESH))
                    paths.add(new VectorDrawableCreator.PathData("M17.65,6.35C16.2,4.9 14.21,4 12,4c-4.42,0 -7.99,3.58 -7.99,8s3.57,8 7.99,8c3.73,0 6.84,-2.55 7.73,-6h-2.08c-0.82,2.33 -3.04,4 -5.65,4 -3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6c1.66,0 3.14,0.69 4.22,1.78L13,11h7V4l-2.35,2.35z", colour));
                else if (Name.equals( Control.ACTION_CAMERA)) {
                    paths.add(new VectorDrawableCreator.PathData("M12,12m-3.2,0a3.2,3.2 0,1 1,6.4 0a3.2,3.2 0,1 1,-6.4 0", colour));
                    paths.add(new VectorDrawableCreator.PathData("M9,2L7.17,4L4,4c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2L22,6c0,-1.1 -0.9,-2 -2,-2h-3.17L15,2L9,2zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5z", colour));
                }
                else if (Name.equals( Control.ACTION_GALLERY)) {
                    paths.add(new VectorDrawableCreator.PathData("M9,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9s9,-4.03 9,-9S13.97,3 9,3zM11.79,16.21L8,12.41V7h2v4.59l3.21,3.21L11.79,16.21z", colour));
                    paths.add(new VectorDrawableCreator.PathData("M17.99,3.52v2.16C20.36,6.8 22,9.21 22,12c0,2.79 -1.64,5.2 -4.01,6.32v2.16C21.48,19.24 24,15.91 24,12C24,8.09 21.48,4.76 17.99,3.52z",colour));
                }
                else if (Name.equals( Control.ACTION_SEARCH)) {
                   paths.add(new VectorDrawableCreator.PathData("M7,9H2V7h5V9zM7,12H2v2h5V12zM20.59,19l-3.83,-3.83C15.96,15.69 15.02,16 14,16c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5s5,2.24 5,5c0,1.02 -0.31,1.96 -0.83,2.75L22,17.59L20.59,19zM17,11c0,-1.65 -1.35,-3 -3,-3s-3,1.35 -3,3s1.35,3 3,3S17,12.65 17,11zM2,19h10v-2H2V19z", colour));
                }
            }
            Drawable d = VectorDrawableCreator.getVectorDrawable(button.getContext(),24,24,24,24,paths);
            button.setBackground(d);
            button.setEnabled(enabled);
        }
        public void addView(ViewGroup container, Function<Button,Void> buttonClick){

            button = new Button(container.getContext());
            button.setPadding(50,50,50,50);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    buttonClick.apply((Button)v);
                }
            });
            LinearLayout.LayoutParams btLp= new LinearLayout.LayoutParams(Width,Height);
            btLp.setMargins(10,0,10,0);
            button.setLayoutParams(btLp);
            setFormat(button,Enabled);
            container.addView(button);
        }
    }
}
