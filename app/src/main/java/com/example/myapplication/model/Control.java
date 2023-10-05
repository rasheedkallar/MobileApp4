package com.example.myapplication.model;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.renderscript.Sampler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.method.DigitsKeyListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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


    public static String ACTION_ADD = "Add";
    public static String ACTION_EDIT = "Edit";
    public static String ACTION_DELETE = "Delete";
    public static String ACTION_REFRESH = "Refresh";
    public static String ACTION_CAMERA = "Camera";
    public static String ACTION_GALLERY = "Gallery";
    public static String ACTION_FILTER = "Filter";
    public static ImageControl getImageControl( String name, String caption,String entityName){
        return new ImageControl(name,caption,entityName);
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
            row.setBackgroundColor(Color.parseColor("#C5C6C6"));
            if(getActionButton(Control.ACTION_EDIT) != null) getActionButton(Control.ACTION_EDIT).setEnabled(true);
            if(getActionButton(Control.ACTION_DELETE) != null)getActionButton(Control.ACTION_DELETE).setEnabled(true);
            for (int i = 0; i < tableLayout.getChildCount(); i++) {
                TableRow otherRow = (TableRow)tableLayout.getChildAt(i);
                if(row != otherRow && header != otherRow){
                    otherRow.setBackgroundResource(android.R.color.transparent);
                }
            }
        }
        private transient TableRow header_row;
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
                            header_row = new TableRow(table.getContext());
                            table.addView(header_row);
                            TableLayout.LayoutParams headerP = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            header_row.setLayoutParams(headerP);
                            header_row.setBackgroundColor(Color.parseColor("#008477"));
                            header_row.setPadding(5, 5, 5, 5);
                            final TableLayout parentTable = table;
                            boolean selectionFound = false;
                            
                            for (com.example.myapplication.model.Control.ControlBase control : controls) {

                                control.addListHeader(header_row);

                            }
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = (JSONObject) jsonArray.get(i);
                                getValue().add(Long.parseLong(obj.get(getIdFieldName()).toString()));
                                TableRow item = new TableRow(table.getContext());
                                table.addView(item);
                                TableLayout.LayoutParams itemP = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                item.setLayoutParams(itemP);
                                item.setPadding(5, 5, 5, 5);
                                item.setTag(obj);
                                item.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        try {
                                            onRowSelected(item);
                                        }
                                        catch (Exception e){
                                            setSelectedId(null);
                                        }
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
        public void doAction(ActionButton action){
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
                            controls.get(i).readValue(data);
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
    public static class ImageControl extends DetailedControlBase<ImageControl>
    {
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
        public void doAction(ActionButton button) {
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
        public DetailedControlBase(String name,String caption,String entityName,String foreignFieldName){
            super(name,caption);
            setEntityName(entityName);
            setForeignFieldName(foreignFieldName);
        }
        public ActionButton getActionButton(String action){
            Optional<ActionButton> button = this.getButtons().stream().filter(i-> i.Name.equals(action)).findFirst();
            if(button.isPresent())return  button.get();
            else return  null;
        }


        @Override
        public boolean onValidate() {
            boolean valid = false;
            if(getParentId() == null || getParentId() == 0)valid = true;
            else if(!getIsRequired())valid= true;
            else if(getValue() != null && getValue().size() >0)valid = true;
            if(header_panel!=null) {
                if (valid) {
                    header_panel.setBackgroundColor(Color.parseColor("#225C6E"));
                } else {
                    header_panel.setBackgroundColor(Color.parseColor("#BC3E17"));
                }
            }
            return valid;
        }
        protected transient  RelativeLayout header_panel;
        protected transient  TextView header_text;
        protected transient  LinearLayout action_panel;
        protected transient  LinearLayout container;
        protected transient  LinearLayout detailed_panel;
        protected transient  LinearLayout detailed_container;




        @Override
        public void addView(ViewGroup container) {

            LayoutInflater li = LayoutInflater.from(container.getContext());
            detailed_panel = (LinearLayout)li.inflate(R.layout.control_detailed, null);

            header_panel =  detailed_panel.findViewById(R.id.header_panel);
            header_text = detailed_panel.findViewById(R.id.header_text);
            action_panel = detailed_panel.findViewById(R.id.action_panel);
            detailed_container = detailed_panel.findViewById(R.id.detailed_container);

            header_text.setText(getCaption());
            for (int i = 0; i < Buttons.size(); i++) {
                final ActionButton ab = Buttons.get(i);
                ab.addView(action_panel, new Function<Button, Void>() {
                    @Override
                    public Void apply(Button button) {
                        doAction(ab);
                        return null;
                    }
                });
            }
            container.addView(detailed_panel);
            addValueView(detailed_container);
        }

        public abstract void doAction(ActionButton button);


        @Override
        public void updateSaveParameters(RequestParams params) {
            params.put(getForeignFieldName(),getParentId());
        }

        private ArrayList<ActionButton> Buttons = new ArrayList<ActionButton>();

        public ArrayList<ActionButton> getButtons() {
            return Buttons;
        }

        public T setButtons(ArrayList<ActionButton> buttons) {
            Buttons = buttons;
            return  (T)this;
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
            if(visible && detailed_panel != null)detailed_panel.setVisibility(View.VISIBLE);
            else if(detailed_panel != null) detailed_panel.setVisibility(View.GONE);

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



    public static int CONTROL_SIZE_DOUBLE = -2;
    public static int CONTROL_SIZE_SINGLE = -1;
    public static LookupControl getLookupControl( String name, String caption,List<DataService.Lookup> lookups){
        return new LookupControl(name,caption,lookups);
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
    public static class SearchControl extends BrowseControlBase<SearchControl, DataService.Lookup>{
        public SearchControl(String name, String caption,List<Control.ControlBase> controls,String entityName,String displayFieldName) {
            super(name, caption);
            setControls(controls);
            setEntityName(entityName);
            setDisplayField(displayFieldName);
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

        private String DisplayField;
        public String getDisplayField() {
            return DisplayField;
        }
        public SearchControl setDisplayField(String displayField) {
            DisplayField = displayField;
            return this;
        }
        public SearchControl setControls(List<Control.ControlBase> controls) {
            Controls = controls;
            return this;
        }

        @Override
        public void updateSaveParameters(RequestParams params) {
            if(getValue() != null)params.put(getName(),getValue().getId());
        }
        @Override
        public void valueChange(DataService.Lookup oldValue, DataService.Lookup newValue) {
            if(LookupTextView != null) {
                if(newValue == null){
                    LookupTextView.setText(null);
                }else{
                    LookupTextView.setText(newValue.getName());
                }
            }
        }

        @Override
        public SearchControl readValue(JSONObject data) {
            return super.readValue(convertValue(data));
        }
        @Override
        protected DataService.Lookup convertValue(Object value) {
            if(value == null)return  null;
            else if(DataService.Lookup.class.isAssignableFrom(value.getClass()))return (DataService.Lookup) value;
            else if(JSONObject.class.isAssignableFrom(value.getClass())){
                JSONObject data = (JSONObject)value;
                DataService.Lookup l = new DataService.Lookup();
                if (!data.has(getName())) return null;
                l.setName("[Unknown]");
                try {
                    Object datavalue = data.get(getName());
                    if(datavalue == null || datavalue.toString().length() == 0 || datavalue.toString().equals("null")){
                         return null;
                    }
                    else{
                        l.setId( Long.parseLong(datavalue.toString()));
                        if (data.has(getDisplayField())) {
                            l.setName(data.get(getDisplayField()).toString());
                        }
                        return l;
                    }
                } catch (JSONException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        public String getFormatValue(DataService.Lookup value) {
            if(value == null)return  null;
            else return value.getName();
        }

        @Override
        protected void onBrowse(Button button) {
            PopupSearch ps = PopupSearch.create(getCaption(),getControls() ,getEntityName(),getDisplayField(),(lookup)->{
                setValue(lookup);
                return true;
            });
            ps.getArgs().setAllowNull(!getIsRequired());
            ps.getArgs().setIdField(getIdField());
            ps.getArgs().setKeywordsField(getKeywordsField());
            ps.show(((BaseActivity)button.getContext()).getSupportFragmentManager(),null);
        }
        @Override
        protected void addBrowseView(ViewGroup container) {
            LookupTextView = new TextView(container.getContext());
            LookupTextView.setPadding(5, 5, 5, 5);
            LookupTextView.setText(getFormatValue(getValue()));
            LinearLayout.LayoutParams tvlP= new LinearLayout.LayoutParams(getWidth()- getButtonSize()+10, ViewGroup.LayoutParams.WRAP_CONTENT);
            LookupTextView.setLayoutParams(tvlP);



            container.addView(LookupTextView);
        }
    }






    public static class LookupControl extends BrowseControlBase<LookupControl,Long>{
        private transient TextView LookupTextView;
        private List<DataService.Lookup> Lookups;
        public List<DataService.Lookup> getLookups() {
            return Lookups;
        }
        public LookupControl setLookups(List<DataService.Lookup> lookups) {
            Lookups = lookups;
            return this;
        }
        public LookupControl(String name, String caption, List<DataService.Lookup> lookups) {
            super(name, caption);
            Lookups = lookups;
        }



        @Override
        public String getFormatValue(Long value) {
            if(value==null)return super.getFormatValue(null);
            try{
                long longValue = Long.parseLong(value.toString());
                Optional<DataService.Lookup> l = Lookups.stream().filter(itm->itm.getId().equals(longValue)).findAny();
                if(l.isPresent()){
                    return l.get().getName();
                }
                else {
                    return  "[Unknown]";
                }
            }
            catch (Exception e){
                return value.toString();
            }
        }

        @Override
        public void valueChange(Long oldValue, Long newValue) {
            //if(LookupTextView != null) {
            Optional<DataService.Lookup> l = Lookups.stream().filter(itm -> itm.getId().equals(newValue)).findAny();
            if (l.isPresent()) {
                LookupTextView.setText(l.get().getName());
            } else {
                LookupTextView.setText("[Unknown]");
            }
        }
        @Override
        protected Long convertValue(Object value) {
            if(value==null)setValue(null);
            else if(value.getClass().equals(Long.class)){
                return  (Long)value;
            }
            else{
                try{
                    return Long.parseLong(value.toString());
                }
                catch (Exception e){

                }
            }
            return null;
        }
        @Override
        protected void onBrowse(Button button) {
            BaseActivity activity = (BaseActivity)button.getContext();
            PopupLookup.create(getCaption(),getLookups(),getValue(),(lookup)->{
                setValue(lookup.getId());
                return true;
            }).show(activity.getSupportFragmentManager(),null);
        }
        @Override
        protected void addBrowseView(ViewGroup container) {
            LookupTextView = new TextView(container.getContext());
            LookupTextView.setPadding(5, 5, 5, 5);
            if(getValue()!=null && Lookups != null){
                Optional<DataService.Lookup> l = Lookups.stream().filter(itm->itm.getId().equals(getValue())).findAny();
                if(l.isPresent()){
                    LookupTextView.setText(l.get().getName());
                }
                else {
                    LookupTextView.setText("[Unknown]");
                }
            }


            LinearLayout.LayoutParams tvlP= new LinearLayout.LayoutParams(getWidth()- getButtonSize()+10, ViewGroup.LayoutParams.WRAP_CONTENT);
            LookupTextView.setLayoutParams(tvlP);
            container.addView(LookupTextView);
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
        protected void onBrowse(Button button) {

            BaseActivity activity = (BaseActivity)button.getContext();
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

        @Override
        protected void onBrowse(Button button) {
            BaseActivity activity = (BaseActivity)button.getContext();
            PopupDate.create(getCaption(),getValue(),(value)->{
                setValue(value);
                return true;
            }).show(activity.getSupportFragmentManager(),null);
        }
    }

    public static abstract class DateControlBase<T extends ControlBase<T,Date>> extends BrowseControlBase<T,Date>{
        private transient EditText EditTextControl;
        private transient boolean isInputValid = true;

        public abstract  String getFormat();


        public DateControlBase( String name, String caption) {
            super( name, caption);
            setControlSize(CONTROL_SIZE_SINGLE);

        }

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
        public String getFormatValue(Date value) {
            if(value==null)return null;
            Date dateValue;
            DateFormat format;
            if(value.getClass().equals(Date.class)) {
                dateValue = (Date) value;
            }
            else{
                if(value.toString().length() == 19) {
                    format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                }
                else {
                    format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                }
                try{
                    dateValue = format.parse(value.toString());
                }
                catch (ParseException e){
                    return super.getFormatValue(value);
                }
            }
            format = new SimpleDateFormat(getFormat());
            return format.format(dateValue);
        }

        private Date isInvalidDate(String dateString, String dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            sdf.setLenient(false);
            try {
                Date date = sdf.parse(dateString);
                String parsedDateString = sdf.format(date);
                if(parsedDateString.equals(dateString)){
                    return date;
                }else{
                    return  null;
                }
            } catch (ParseException e) {
                return null;
            }
        }
        private  Date validateDate(Context context,EditText tvl,Date currentDate){
            Calendar currentCalendar = null;
            if(currentDate!=null){
                currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(currentDate);
            }
            String text = null;
            Date date = null;
            boolean invalid = false;
            if(tvl.getText()!=null) text = tvl.getText().toString();
            if(text != null && text.length() != 0){
                date = isInvalidDate(text, "dd/MM/yy");
                if(date==null){
                    date = isInvalidDate(text, "dd/MM/yy HH:mm");
                    if(date==null) {
                        date = isInvalidDate(text, "dd/MM/yy HH:mm:ss");
                        if(date==null){
                            date = isInvalidDate(text, "dd/MM/yy HH:mm:ss.SSS");
                        }
                        else{
                            if(currentCalendar!=null){
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                calendar.set(Calendar.MILLISECOND,currentCalendar.get(Calendar.MILLISECOND));
                                date = calendar.getTime();
                            }
                        }
                    }
                    else {
                        if(currentCalendar!=null ){
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.SECOND,currentCalendar.get(Calendar.SECOND));
                            calendar.set(Calendar.MILLISECOND,currentCalendar.get(Calendar.MILLISECOND));
                            date = calendar.getTime();
                        }
                    }
                }
                if(date==null)invalid = true;
            }
            if(invalid){
                tvl.setTextColor(ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_error));
                isInputValid = false;

                return null;
            }
            else {
                tvl.setTextColor(ContextCompat.getColor(context, R.color.black));
                isInputValid = true;
            }
            return  date;
        }
        @Override
        protected void addBrowseView(ViewGroup container) {

            EditTextControl = new EditText(container.getContext());
            if(getValue()!=null){
                DateFormat dateFormat = new SimpleDateFormat(getFormat());
                String newText = dateFormat.format(getValue());
                EditTextControl.setText(newText);
            }


            EditTextControl.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void afterTextChanged(Editable editable) {

                    if(EditTextControl.getText()==null || EditTextControl.getText().toString().length() == 0){
                        if(getValue()!= null ) setValue(null);
                    }
                    else{
                        Date currentDate = getValue();
                        //if(EditTextControl.getTag() != null) currentDate = (Date)EditTextControl.getTag();
                        Date date = validateDate(container.getContext(), EditTextControl,currentDate);
                        if(date!=null && !date.equals( currentDate))setValue(date);
                    }
                }
            });

            LinearLayout.LayoutParams tvlP= new LinearLayout.LayoutParams(getWidth()- getButtonSize()+10, ViewGroup.LayoutParams.WRAP_CONTENT);
            EditTextControl.setLayoutParams(tvlP);
            container.addView(EditTextControl);
        }



        @Override
        public void valueChange(Date oldValue, Date newValue) {
            //if(EditTextControl != null) {

            if (newValue== null && (EditTextControl.getText()== null || EditTextControl.getText().length() == 0))
                return;
            String currentText = null;
            if (EditTextControl.getText()!= null && EditTextControl.getText().length() != 0)
                currentText = EditTextControl.getText().toString();

            String newText = null;
            if(newValue!=null){
                DateFormat dateFormat = new SimpleDateFormat(getFormat());
                newText = dateFormat.format(newValue);
                //EditTextControl.setText(dateFormat.format(value));
            }
            if(!currentText.equals(newText)){
                EditTextControl.setText(newText);
            }

            //}
        }






        @Override
        public boolean onValidate() {
            if(!isInputValid)return false;
            else return super.onValidate();
        }





    }

    public static abstract class BrowseControlBase<T extends ControlBase<T,U>,U extends  Serializable> extends ControlBase<T,U>{
        private transient Button BrowsButton;
        private int ButtonSize = 100;


        protected Button getBrowsButton() {
            return BrowsButton;
        }

        public int getButtonSize(){
            return  ButtonSize;
        }
        public T setButtonSize(int buttonSize) {
            ButtonSize = buttonSize;
            return (T)this;
        }
        public BrowseControlBase(String name, String caption){

            super(name, caption);
            setControlSize(CONTROL_SIZE_DOUBLE);
        }
        protected abstract void onBrowse(Button button);
        @Override
        protected void addValueView(ViewGroup container) {
            LinearLayout BrowseLayoutControl = new LinearLayout(container.getContext());
            LinearLayout.LayoutParams lllP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            BrowseLayoutControl.setOrientation(LinearLayout.HORIZONTAL);
            BrowseLayoutControl.setLayoutParams(lllP);

            BrowsButton = new Button(container.getContext());
            BrowsButton.setText("...");
            BrowsButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onBrowse((Button)v);
                }
            });
            ViewGroup.LayoutParams btlp= new ViewGroup.LayoutParams(getButtonSize(), getButtonSize());
            BrowsButton.setLayoutParams(btlp);

            addBrowseView(BrowseLayoutControl);

            //View valueView = getBrowseView();
            //if(valueView!=null){
            //    BrowseLayoutControl.addView(valueView);
            //}
            BrowseLayoutControl.addView(BrowsButton);
            container.addView(BrowseLayoutControl);
            //return BrowseLayoutControl;
        }
        protected abstract void addBrowseView(ViewGroup container);
    }






    public static EditTextControl getEditTextControl( String name, String caption){
        return new EditTextControl(name,caption);
    }
    public static class EditTextControl extends EditTextControlBase<EditTextControl,String> {
        public EditTextControl(String name, String caption){
            super( name, caption);
        }

        @Override
        protected String convertValue(Object value) {
            if(value == null)return  null;
            else return value.toString();
        }



    }
    public static class EditDecimalControl extends EditTextControlBase<EditDecimalControl,Double> {

        private Integer DecimalPlaces;

        public Integer getDecimalPlaces() {
            return DecimalPlaces;
        }

        public EditDecimalControl setDecimalPlaces(Integer decimalPlaces) {
            DecimalPlaces = decimalPlaces;
            return this;
        }

        public EditDecimalControl(String name, String caption){
            super( name, caption);
            setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            setDigits("0123456789.-");
            setDecimalPlaces(2);
            setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }

        @Override
        protected Double convertValue(Object value) {
            if(value == null)return  null;
            else return Double.parseDouble(value.toString());
        }



        @Override
        public String getFormatValue(Double value) {
            if(DecimalPlaces == null || DecimalPlaces <0) return value.toString();
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
    public static abstract class EditTextControlBase<T extends ControlBase<T,U>,U extends  Serializable> extends ControlBase<T,U>{
        public EditTextControlBase(String name, String caption){
            super( name, caption);
        }
        protected transient EditText EditTextControl;
        public EditText getEditTextControl() {
            return EditTextControl;
        }

        private String Digits = null;

        public T setDigits(String digits) {
            Digits = digits;
            return (T)this;
        }

        public String getDigits() {
            return Digits;
        }

        private int InputType = 1;

        public T setInputType(int inputType) {
            InputType = inputType;
            return (T)this;
        }

        public int getInputType() {
            return InputType;
        }

        private  boolean isInputValid = true;

        @Override
        public boolean onValidate() {
            if(!isInputValid)return false;
            else return super.onValidate();
        }

        private boolean typing = false;

        @Override
        public void addValueView(ViewGroup container) {
            EditTextControl = new EditText(container.getContext());
            TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            EditTextControl.setLayoutParams(txtP);
            if(getDigits() != null && getDigits().length() != 0){
                EditTextControl.setKeyListener(DigitsKeyListener.getInstance(getDigits()));
            }
            EditTextControl.setInputType(getInputType());
            if(getValue() != null)EditTextControl.setText(getValue().toString());
            EditTextControl.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void afterTextChanged(Editable editable) {
                    typing = true;

                    if(EditTextControl.getText() ==null || EditTextControl.getText().toString().length() == 0) {
                        isInputValid = true;
                        EditTextControl.setTextColor(ContextCompat.getColor(EditTextControl.getContext(), R.color.black));
                        if(getValue() !=null) setValue(null);
                    }
                    else {
                        try{

                            U value = convertValue(EditTextControl.getText());
                            isInputValid = true;
                            EditTextControl.setTextColor(ContextCompat.getColor(EditTextControl.getContext(), R.color.black));
                            if(getValue() != null && value == null)setValue(null);
                            else if(getValue() == null && value != null)setValue(value);
                            else if(!getValue().equals(value))setValue(value);

                        }
                        catch (Exception e){
                            isInputValid = false;
                            EditTextControl.setTextColor(ContextCompat.getColor(EditTextControl.getContext(), com.google.android.material.R.color.design_default_color_error));

                        }
                    }
                    typing = false;
                }
            });


            container.addView(EditTextControl);
        }
        @Override
        public void valueChange(U oldValue, U newValue) {

            if(typing = false){
                EditTextControl.setText(getFormatValue(newValue));
            }


        }




    }

    public static HiddenControl getHiddenControl( String name, Serializable value){
        return new HiddenControl(name,value);
    }

    public static class HiddenControl extends ControlBase<HiddenControl,Serializable>{ //<HiddenControl,Long>{
        public HiddenControl( String name, Serializable value){
            super( name, null);
            setValue(value);
            setIsRequired(false);
        }
        @Override
        public void valueChange(Serializable oldValue, Serializable newValue) {

        }

        @Override
        protected void addValueView(ViewGroup container) {

        }
        @Override
        public void addView(ViewGroup container) {
            if(getRootActivity() == null)setRootActivity((BaseActivity) container.getContext());

        }
        @Override
        protected Serializable convertValue(Object value) {
            if(value == null)return null;
            else if(Serializable.class.isAssignableFrom(value.getClass()))return (Serializable)value;
            else return value.toString();
        }
    }


    public static abstract   class ControlBase<T extends ControlBase<T,U>,U extends Serializable> implements Serializable { //<T extends ControlBase<T,U>,U>{
        public ControlBase(String name,String caption){
            Caption = caption;
            Name = name;
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
        private int ControlSize = -1;
        public int getControlSize(){
            return ControlSize;
        }
        public T setControlSize(int size){
            ControlSize = size;
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

        public int getWidth(){
            int singleSize = 463;
            if(ControlSize<0)return Math.abs(ControlSize) * singleSize;
            else return ControlSize;
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
        public  T setValue(U value){
            if(Value == null && value == null)return (T)this;
            U oldValue = Value;
            Value = value;
            if(viewCreated && (oldValue == null || value == null || !oldValue.equals(value))){

                valueChange(Value,value);
            }

            return (T)this;
        }

        public void addListHeader(TableRow row){
            float weight = 1f;
            if (getControlSize() < -1) weight = 2f;

            TextView hc = new TextView(row.getContext());
            hc.setPadding(5, 5, 5, 5);
            TableRow.LayoutParams hcP = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
            hc.setLayoutParams(hcP);
            hc.setTextColor(ContextCompat.getColor(row.getContext(), R.color.white));
            hc.setText(getCaption());
            hc.setBackgroundColor(Color.parseColor("#008477"));
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
            float weight = 1f;
            if (getControlSize() < -1) weight = 2f;
            TextView hc = new TextView(row.getContext());
            TableRow.LayoutParams hcP = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
            hc.setLayoutParams(hcP);
            hc.setTextAlignment(TextAlignment);
            if(data.has(getName())) {
                try{
                    Object value = data.get(getName());
                    if (value != null) {
                        hc.setText(getFormatValue(convertValue(value)));
                    }
                }
                catch (JSONException e){

                }

            }
            row.addView(hc);

        }

        public abstract void valueChange(U oldValue, U newValue);
        private transient boolean viewCreated =false;
        private transient TextView CaptionTextView;
        public TextView getCaptionTextView() {
            return CaptionTextView;
        }
        public void addView(ViewGroup container)
        {
            if(getRootActivity() == null)setRootActivity((BaseActivity) container.getContext());
            viewCreated = false;
            LinearLayout ll = new LinearLayout(container.getContext());
            LinearLayout.LayoutParams llParam= new LinearLayout.LayoutParams(getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.setOrientation(LinearLayout.VERTICAL);
            llParam.setMargins(2, 2, 2, 2);
            ll.setLayoutParams(llParam);

            if(Caption != null) {
                CaptionTextView = new TextView(container.getContext());
                TableLayout.LayoutParams cParam= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 60);
                CaptionTextView.setPadding(10, 0, 5, 10);
                CaptionTextView.setLayoutParams(cParam);
                CaptionTextView.setText(Caption);
                CaptionTextView.setTextColor(ContextCompat.getColor(container.getContext(), R.color.white));
                CaptionTextView.setBackgroundColor(Color.parseColor("#008477"));
                ll.addView(CaptionTextView);
            }
            addValueView(ll);
            container.addView(ll);
            viewCreated = true;
        }
        protected abstract void addValueView(ViewGroup container);
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
        public T readValue(Object value){
            setValue(convertValue(value));
            return (T)this;
        }

        protected abstract U convertValue(Object value);

        public  T readValue(JSONObject data){
            try{
                if(data.has(getName())){
                    Object value = data.get(getName());
                    readValue(value);
                }
            }
            catch (JSONException e){

            }
            return  (T)this;
        }
        public boolean validate(){
            boolean valid = onValidate();
            if(CaptionTextView!=null) {
                if (valid) {
                    CaptionTextView.setBackgroundColor(Color.parseColor("#008477"));
                } else {
                    CaptionTextView.setBackgroundColor(Color.parseColor("#BC3E17"));
                }
            }
            return valid;
        }
        public boolean onValidate(){
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

        private int Width = 100;

        public int getWidth() {
            return Width;
        }

        public ActionButton setWidth(int width) {
            Width = width;
            return  this;
        }

        private int Height = 100;

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



        private void setFormat(Button button, boolean enabled){
            String foreColour = "#8CD0E4";
            if(!enabled)foreColour = "#6D6F70";
            ArrayList<VectorDrawableCreator.PathData> paths = new ArrayList<VectorDrawableCreator.PathData>();
            paths.add(new VectorDrawableCreator.PathData("M 18 0 L 2 0 c -1.1 0 -2 0.9 -2 2 v 20 c 0 1.1 0.9 2 2 2 h 20 c 1.1 0 2 -0.9 2 -1 L 24 2 c 0 -1.1 -0.9 -2 -3 -2 z M 23 23 L 1 23 L 1 1 h 22 v 22 z", Color.parseColor(foreColour)));

            if(Name != null) {

                if (Name.equals(Control.ACTION_ADD))
                    paths.add(new VectorDrawableCreator.PathData("M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z", Color.parseColor(foreColour)));
                else if (Name.equals(Control.ACTION_EDIT))
                    paths.add(new VectorDrawableCreator.PathData("M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z", Color.parseColor(foreColour)));
                else if (Name.equals(Control.ACTION_DELETE))
                    paths.add(new VectorDrawableCreator.PathData("M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z", Color.parseColor(foreColour)));
                else if (Name.equals( Control.ACTION_REFRESH))
                    paths.add(new VectorDrawableCreator.PathData("M17.65,6.35C16.2,4.9 14.21,4 12,4c-4.42,0 -7.99,3.58 -7.99,8s3.57,8 7.99,8c3.73,0 6.84,-2.55 7.73,-6h-2.08c-0.82,2.33 -3.04,4 -5.65,4 -3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6c1.66,0 3.14,0.69 4.22,1.78L13,11h7V4l-2.35,2.35z", Color.parseColor(foreColour)));
                else if (Name.equals( Control.ACTION_CAMERA)) {
                    paths.add(new VectorDrawableCreator.PathData("M12,12m-3.2,0a3.2,3.2 0,1 1,6.4 0a3.2,3.2 0,1 1,-6.4 0", Color.parseColor(foreColour)));
                    paths.add(new VectorDrawableCreator.PathData("M9,2L7.17,4L4,4c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2L22,6c0,-1.1 -0.9,-2 -2,-2h-3.17L15,2L9,2zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5z", Color.parseColor(foreColour)));
                } else if (Name.equals( Control.ACTION_GALLERY)) {
                    paths.add(new VectorDrawableCreator.PathData("M9,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9s9,-4.03 9,-9S13.97,3 9,3zM11.79,16.21L8,12.41V7h2v4.59l3.21,3.21L11.79,16.21z", Color.parseColor(foreColour)));
                    paths.add(new VectorDrawableCreator.PathData("M17.99,3.52v2.16C20.36,6.8 22,9.21 22,12c0,2.79 -1.64,5.2 -4.01,6.32v2.16C21.48,19.24 24,15.91 24,12C24,8.09 21.48,4.76 17.99,3.52z", Color.parseColor(foreColour)));
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
                    //onBrowse((Button)v);
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
