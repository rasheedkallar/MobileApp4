package com.example.myapplication.model;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.DateFormat;
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

public class Control {
    public static ImageControl getImageControl( String name, String caption,String entityName){
        return new ImageControl(name,caption,entityName);
    }
    public static abstract class DetailedControl extends DetailedControlBase<ImageControl> {
        public DetailedControl(String name,String caption,String entityName,String foreignFieldName) {
            super(name, caption,entityName,foreignFieldName);
        }
        protected transient RadioButton add_button;
        protected transient RadioButton edit_button;
        protected transient RadioButton delete_button;
        protected transient RadioButton refresh_button;
        protected transient TableLayout table_layout;
        protected transient FlexboxLayout filter_layout;
        protected transient TextView header_text;
        private transient RelativeLayout header_panel;
        protected List<ControlBase> FilterControls = null;
        public List<ControlBase> getFilterControls() {
            if(FilterControls == null)FilterControls= getControls("Filter");
            return FilterControls;
        }
        @Override
        public boolean onValidate() {
            boolean valid = super.onValidate();
            if(header_panel!=null) {
                if (valid) {
                    header_panel.setBackgroundColor(Color.parseColor("#008477"));
                } else {
                    header_panel.setBackgroundColor(Color.parseColor("#BC3E17"));
                }
            }
            return valid;
        }

        public boolean doAfterSaved(Long id,boolean defaultClose){
            setSelectedId(id);
            refreshGrid(table_layout);
            return defaultClose;
        }


        @Override
        public void addDetailedView(ViewGroup container) {

            LayoutInflater li = LayoutInflater.from(container.getContext());
            View control_detailed = li.inflate(R.layout.control_detailed, null);
            header_panel =  control_detailed.findViewById(R.id.header_panel);
            table_layout = control_detailed.findViewById(R.id.table_layout);
            add_button = control_detailed.findViewById(R.id.add_button);
            add_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onButtonClick("Add",(RadioButton)view);
                }
            });

            edit_button = control_detailed.findViewById(R.id.edit_button);
            edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onButtonClick("Edit",(RadioButton)view);
                }
            });
            delete_button = control_detailed.findViewById(R.id.delete_button);



            delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onButtonClick("Delete",(RadioButton)view);
                }
            });
            refresh_button = control_detailed.findViewById(R.id.refresh_button);
            refresh_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onButtonClick("Refresh",(RadioButton)view);
                }
            });
            filter_layout = control_detailed.findViewById(R.id.filter_layout);
            List<ControlBase> controls = getFilterControls();
            if(controls != null){
                for (int i = 0; i < controls.size(); i++) {
                    controls.get(i).addView(filter_layout);
                }
            }
            header_text = control_detailed.findViewById(R.id.header_text);
            header_text.setText(getCaption());
            container.addView(control_detailed);

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
        private void selectRow(TableRow row, TableRow header, TableLayout tableLayout) {
            row.setBackgroundColor(Color.parseColor("#C5C6C6"));
            edit_button.setEnabled(true);
            delete_button.setEnabled(true);
            for (int i = 0; i < tableLayout.getChildCount(); i++) {
                TableRow otherRow = (TableRow)tableLayout.getChildAt(i);
                if(row != otherRow && header != otherRow){
                    otherRow.setBackgroundResource(android.R.color.transparent);
                }
            }
        }

        private transient TableRow header_row;

        protected void refreshGrid(TableLayout table){
            table_layout = table;

            ArrayList<ControlBase> controls = getControls("List");
            String id_field_name = getIdFieldName();
            if(controls != null && controls.size() != 0){
                new DataService().get(getRefreshUrl(), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                        String result = new String(responseBody);
                        try {

                            if(getValue() == null)setValue(new ArrayList<Long>());
                            getValue().clear();
                            JSONArray data = new JSONArray(result);
                            table_layout.removeAllViews();
                            header_row = new TableRow(table_layout.getContext());
                            table_layout.addView(header_row);
                            TableLayout.LayoutParams headerP = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            header_row.setLayoutParams(headerP);
                            header_row.setBackgroundColor(Color.parseColor("#054678"));
                            header_row.setPadding(5, 5, 5, 5);
                            final TableLayout parentTable = table_layout;
                            boolean selectionFound = false;
                            for (com.example.myapplication.model.Control.ControlBase control : controls) {
                                float weight = 1f;
                                if (control.getControlSize() < -1) weight = 2f;

                                TextView hc = new TextView(table_layout.getContext());
                                hc.setPadding(5, 5, 5, 5);
                                TableRow.LayoutParams hcP = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
                                hc.setLayoutParams(hcP);
                                hc.setTextColor(ContextCompat.getColor(table_layout.getContext(), R.color.white));
                                hc.setText(control.getCaption());
                                hc.setBackgroundColor(Color.parseColor("#008477"));
                                header_row.addView(hc);
                            }

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = (JSONObject) data.get(i);
                                getValue().add(Long.parseLong(obj.get(getIdFieldName()).toString()));
                                TableRow item = new TableRow(table_layout.getContext());
                                table_layout.addView(item);
                                TableLayout.LayoutParams itemP = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                item.setLayoutParams(itemP);
                                item.setPadding(5, 5, 5, 5);
                                item.setTag(obj);
                                item.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        try {
                                            setSelectedId(Long.parseLong(obj.get(getIdFieldName()).toString()));
                                            selectRow(item, header_row, parentTable);
                                        }
                                        catch (Exception e){
                                            setSelectedId(null);
                                        }
                                    }
                                });
                                for (com.example.myapplication.model.Control.ControlBase control : controls) {
                                    float weight = 1f;
                                    if (control.getControlSize() < -1) weight = 2f;
                                    TextView hc = new TextView(table_layout.getContext());
                                    TableRow.LayoutParams hcP = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
                                    hc.setLayoutParams(hcP);
                                    if(obj.has(control.getName())) {
                                        Object value = obj.get(control.getName());
                                        if (value != null) {
                                            hc.setText(control.getFormatValue(value));
                                        }
                                    }
                                    item.addView(hc);
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
                                edit_button.setEnabled(true);
                                delete_button.setEnabled(true);
                            }
                            else{
                                edit_button.setEnabled(false);
                                delete_button.setEnabled(false);
                            }

                        }
                        catch (JSONException  e)
                        {
                            Toast.makeText(getRootActivity(), "GetListData Failed," + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                        String result = new String(responseBody);
                        Toast.makeText(getRootActivity(), "GetListData Failed," + result, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        protected void onButtonClick(String action, RadioButton button){
            BaseActivity activity = (BaseActivity)button.getContext();
            if(action.equals("Add")){
                ArrayList<ControlBase> controls = getControls(action);
                if(getForeignFieldName() != null && getForeignFieldName().length() != 0 && getParentId() != null && getParentId() != 0L){
                    controls.add(Control.getHiddenControl(getForeignFieldName(),getParentId()));
                }
                new PopupForm().setArgs(new PopupForm.PopupFormArgs(getCaption() + " Add",controls,getEntityName(),0L)).show( activity.getSupportFragmentManager(),null);
            }
            else if(action.equals("Edit")){
                //ArrayList<ControlBase> controls = getControls(action);
                new DataService().getById(activity, getEntityName(), getSelectedId(), new DataService.GetByIdResponse() {
                    @Override
                    public void onSuccess(JSONObject data) {
                    try {
                        ArrayList<ControlBase> controls = getControls(action);
                        if(getForeignFieldName() != null && getForeignFieldName().length() != 0 && getParentId() != null && getParentId() != 0L){
                            controls.add(Control.getHiddenControl(getForeignFieldName(),getParentId()));
                        }
                        Utility.applyValues(data,controls);
                        for (int i = 0; i < controls.size(); i++) {
                            if(DetailedControlBase.class.isAssignableFrom(controls.get(i).getClass())){
                                DetailedControlBase ctrl = (DetailedControlBase)controls.get(i);
                                ctrl.setParentId(getSelectedId());
                            }
                        }
                        new PopupForm().setArgs(new PopupForm.PopupFormArgs(getCaption() + " Edit",controls,getEntityName(),getSelectedId())).show( activity.getSupportFragmentManager(),null);
                    }
                    catch ( JSONException ex){
                        Toast.makeText(activity, "Error in loading data " +  ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    }
                });
            }
            else if(action.equals("Delete")){
                PopupConfirmation.create("Delete Confirmation", "Are you sure you want to delete?", (unused)-> {
                    new DataService().deleteById(activity, getEntityName(), getSelectedId(), new DataService.DeleteByIdResponse() {
                        @Override
                        public void onSuccess(Boolean deleted) {
                            setSelectedId(null);
                            refreshGrid(table_layout);
                        }
                    });
                    return true;
                }).show(((BaseActivity)button.getContext()).getSupportFragmentManager(), null);

            }
            else if(action.equals("Refresh")){
                refreshGrid(table_layout);
            }
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

        protected abstract ArrayList<ControlBase> getControls(String action);
    }


    public static class ImageControl extends DetailedControlBase<ImageControl>
    {
        public ImageControl(String name, String caption,String entityName){
            super(name, caption,entityName,"RefId");
            setValue(new ArrayList<Long>());
        }
        private transient View image_control;
        private transient RadioButton delete_button;
        private transient FlexboxLayout main_layout;

        private transient RelativeLayout header_panel;
        public void onCapturedImage(int action, Bitmap image, Long id){
            addImage(id);
            delete_button.setEnabled(false);
            for (int i = 0; i < main_layout.getChildCount(); i++) {
                ImageView iv = (ImageView)main_layout.getChildAt(i);
                Long vId = (Long)iv.getTag();
                if(id.equals(vId)) {
                    setSelectedId(id);
                    iv.setBackgroundColor(Color.parseColor("#225C6E"));
                    delete_button.setEnabled(true);
                }
                else{
                    iv.setBackgroundColor(Color.parseColor("#8CD0E4"));
                }
            }
        }
        @Override
        public boolean onValidate() {
            boolean valid = super.onValidate();
            if(header_panel!=null) {
                if (valid) {
                    header_panel.setBackgroundColor(Color.parseColor("#008477"));
                } else {
                    header_panel.setBackgroundColor(Color.parseColor("#BC3E17"));
                }
            }
            return valid;
        }


        @Override
        public void addDetailedView(ViewGroup container) {
            LayoutInflater li = LayoutInflater.from(container.getContext());

            image_control = li.inflate(R.layout.control_images, null);
            header_panel =  image_control.findViewById(R.id.header_panel);
            main_layout = image_control.findViewById(R.id.main_layout);
            delete_button = image_control.findViewById(R.id.delete_button);
            delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupConfirmation.create("Delete Confirmation", "Are you sure you want to delete?", (unused)->{
                        new DataService().deleteById(view.getContext(), "RefFile", getSelectedId(), new DataService.DeleteByIdResponse() {
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
                                delete_button.setEnabled(false);
                            }
                            }
                        });
                        return true;
                    }).show(getRootActivity().getSupportFragmentManager(),null);
                }
            });

            RadioButton gallery_button = image_control.findViewById(R.id.gallery_button);
            gallery_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getRootActivity().captureImage(BaseActivity.TAKE_IMAGE_FROM_GALLERY,getEntityName(),getParentId());
                }
            });

            RadioButton camera_button = image_control.findViewById(R.id.camera_button);
            camera_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getRootActivity().captureImage(BaseActivity.TAKE_IMAGE_FROM_CAMERA,getEntityName(),getParentId());
                }
            });
            for (int i = 0; i < getValue().size(); i++) {
                addImage(getValue().get(i));
            }
            container.addView(image_control);


        }
        private ImageView GetImageView(long id){
            ImageView imageView = new ImageView(main_layout.getContext());
            FlexboxLayout.LayoutParams lllP = new FlexboxLayout.LayoutParams(230, 230);
            lllP.setMargins(2,2,2,2);
            imageView.setLayoutParams(lllP);
            imageView.setTag(id);
            if(id == getSelectedId()){
                imageView.setBackgroundColor(Color.parseColor("#225C6E"));
                delete_button.setEnabled(true);
            }
            else{
                imageView.setBackgroundColor(Color.parseColor("#8CD0E4"));
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   setSelectedId(null);
                    delete_button.setEnabled(false);
                    for (int i = 0; i < main_layout.getChildCount(); i++) {
                        ImageView iv = (ImageView)main_layout.getChildAt(i);
                        if(view == iv) {
                            setSelectedId(id);
                            iv.setBackgroundColor(Color.parseColor("#225C6E"));
                            delete_button.setEnabled(true);
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

        @Override
        public boolean onValidate() {
            boolean valid = false;
            if(getParentId() == null || getParentId() == 0)valid = true;
            else if(!getIsRequired())valid= true;
            else if(getValue() != null && getValue().size() >0)valid = true;
            return valid;
        }
        private transient  LinearLayout detailedLayout;

        @Override
        public void addView(ViewGroup container) {
            if(getRootActivity() == null)setRootActivity((BaseActivity) container.getContext());

            detailedLayout = new LinearLayout(container.getContext());
            LinearLayout.LayoutParams llParam= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            detailedLayout.setOrientation(LinearLayout.VERTICAL);
            //llParam.setMargins(2, 2, 2, 2);
            detailedLayout.setLayoutParams(llParam);

            container.addView(detailedLayout);
            addDetailedView(detailedLayout);
        }


        @Override
        public void updateSaveParameters(RequestParams params) {
            params.put(getForeignFieldName(),getParentId());
        }

        private String IdFieldName = "Id";

        public String getIdFieldName() {
            return IdFieldName;
        }

        public T setIdFieldName(String idFieldName) {
            IdFieldName = idFieldName;
            return (T)this;
        }

        public abstract void addDetailedView(ViewGroup container);
        private Long SelectedId = 0L;
        protected void setSelectedId(Long selectedImage) {
            SelectedId = selectedImage;
        }

        public Long getSelectedId() {
            return SelectedId;
        }
        private Long ParentId;
        public void changeVisibility(boolean visible){
            if(visible)detailedLayout.setVisibility(View.VISIBLE);
            else detailedLayout.setVisibility(View.GONE);

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
        protected void addValueView(ViewGroup container) {

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
        public String getFormatValue(Object value) {
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
        public String getFormatValue(Object value) {
            if(value==null)super.getFormatValue(null);
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

        @Override
        protected String getTextValue(String value) {
            return value;
        }



    }
    public static class EditDecimalControl extends EditTextControlBase<EditDecimalControl,Double> {
        public EditDecimalControl(String name, String caption){
            super( name, caption);
            setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            setDigits("0123456789.-");
        }

        @Override
        protected Double convertValue(Object value) {
            if(value == null)return  null;
            else return Double.parseDouble(value.toString());
        }

        @Override
        protected String getTextValue(Double value) {
            return value.toString();
        }




    }



    public static abstract class EditTextControlBase<T extends ControlBase<T,U>,U extends  Serializable> extends ControlBase<T,U>{
        public EditTextControlBase(String name, String caption){
            super( name, caption);
        }
        private transient EditText EditTextControl;
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
                EditTextControl.setText(getTextValue(newValue));
            }


        }

        protected abstract String getTextValue(U value);


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
        public String getFormatValue(Object value)
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
                Object value = data.get(getName());
                readValue(value);
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
}
