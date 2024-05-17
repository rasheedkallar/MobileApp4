package com.example.myapplication.model;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import kotlin.jvm.functions.Function2;

public class Control {
    public static String ACTION_SEARCH = "Search";
    public static String ACTION_ADD = "Add";
    public static String ACTION_ADD_SUB = "AddSub";
    public static String ACTION_INBOX = "Inbox";
    public static String ACTION_EDIT = "Edit";
    public static String ACTION_DELETE = "Delete";
    public static String ACTION_REFRESH = "Refresh";
    public static String ACTION_CAMERA = "Camera";
    public static String ACTION_GALLERY = "Gallery";
    public static String ACTION_FILTER = "Filter";


    public static String ACTION_STATUS= "Status";

    public static String ACTION_VIEW= "View";
    public static String ACTION_BARCODE= "Barcode";

    public static String ACTION_PERCENT= "Percent";

    public static String ACTION_STOCK= "Stock";
    public static String ACTION_CHECKED= "Checked";



    public static int CONTROL_SIZE_DOUBLE = -20;
    public static int CONTROL_SIZE_SINGLE = -10;

    public static String AGGREGATE_SUM = "Sum";
    public static String AGGREGATE_AVERAGE = "Average";
    public static String AGGREGATE_COUNT = "Count";
    public static String AGGREGATE_MAX = "Max";
    public static String AGGREGATE_MIN = "Min";


    public static HiddenControl getHiddenControl( String name, Serializable value){
        return new HiddenControl(name,value);
    }
    public static ImageControl getImageControl( String name, String caption,String entityName){
        return new ImageControl(name,caption,entityName);
    }

    public static LookupListControl getLookupListControl( String name, String caption, String displayField,List<DataService.Lookup> lookups){
        return new LookupListControl(name,caption,displayField,lookups);
    }

    public static LookupForeignControl getLookupForeignControl( String name, String caption,String displayField){
        return new LookupForeignControl(name,caption,displayField);
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

    public static EditIntegerControl getEditIntegerControl( String name, String caption){
        return new EditIntegerControl(name,caption);
    }

    public static EditTextPickerControl getEditTextPickerControl( String name, String caption,ArrayList<String> options,String defaultValue){
        return new EditTextPickerControl(name,caption,options,defaultValue);
    }








    //public static SearchControl getSearchControl( String name, String caption,List<Control.ControlBase> controls,String entityName,String displayFieldName){
    //    return new SearchControl(name,caption,controls,entityName,displayFieldName);
    //}

    public static abstract class DetailedControl extends DetailedControlBase<DetailedControl> {
        public DetailedControl(String name,String caption) {
            super(name, caption);
            ArrayList<ActionButton> buttons = new ArrayList<ActionButton>();
            buttons.add(new ActionButton(Control.ACTION_ADD));
            buttons.add(new ActionButton(Control.ACTION_EDIT).setEnabled(false));
            buttons.add(new ActionButton(Control.ACTION_DELETE).setEnabled(false));
            buttons.add(new ActionButton(Control.ACTION_REFRESH));
            setButtons(buttons);
        }


        private ArrayList<ControlBase> EditControls= null;



        public <A extends Control.ControlBase> A getControl(String name){
            if(EditControls == null)return null;
            Optional<Control.ControlBase> control = EditControls.stream().filter(i-> i.getName().equals(name)).findFirst();
            if(control.isPresent())return (A)control.get();
            else return null;
        }

        protected transient TableLayout Table;

        public TableLayout getTable() {
            return Table;
        }

        public DetailedControl setTable(TableLayout table) {
            Table = table;
            return  this;
        }

        protected transient FlexboxLayout filter_layout;
        protected transient List<ControlBase> FilterControls = null;
        public List<ControlBase> getFilterControls() {
            if(FilterControls == null)FilterControls= getControls(Control.ACTION_FILTER);
            return FilterControls;
        }
        public boolean doAfterSaved(Long id,boolean defaultClose){
            setValue(id);
            refreshGrid(Table);
            return defaultClose;
        }




        @Override
        public void addValueView(ViewGroup container) {

            filter_layout = new FlexboxLayout(container.getContext());
            TableLayout.LayoutParams fblP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            filter_layout.setLayoutParams(fblP);
            filter_layout.setFlexWrap(FlexWrap.WRAP);
            container.addView(filter_layout);
            Table = new TableLayout(container.getContext());
            Table.setShrinkAllColumns(true);
            Table.setStretchAllColumns(true);
            addHeaderRow(Table,getControls(ACTION_REFRESH));

            TableLayout.LayoutParams tlLp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Table.setLayoutParams(tlLp);
            container.addView(Table);

            List<ControlBase> controls = getFilterControls();
            if(controls != null){
                for (int i = 0; i < controls.size(); i++) {
                    controls.get(i).addView(filter_layout);
                }
            }

            if(getPath() == null && getName() != null && getName().length() != 0 && GridData == null)
                refreshGrid(Table);
            else if(GridData != null)refreshDetailedView(GridData);
        }

        protected  void onRowSelected(TableRow row){
            JSONObject obj = (JSONObject)row.getTag();
            try
            {
                setValue(Long.parseLong(obj.get(getIdFieldName()).toString()));

            }
            catch (JSONException e){
                System.out.println(e.getMessage());
                setValue(null);
            }
            selectRow(row,header_row,Table);
        }
        private transient TableRow SelectedRow;

        public TableRow getSelectedRow() {
            return SelectedRow;
        }

        protected void selectRow(TableRow row, TableRow header, TableLayout tableLayout) {
            SelectedRow = row;
            if(getActionButton(Control.ACTION_EDIT) != null) getActionButton(Control.ACTION_EDIT).setEnabled(true);
            if(getActionButton(Control.ACTION_DELETE) != null)getActionButton(Control.ACTION_DELETE).setEnabled(true);

            int RowIndex = -1;

            for (int i = 0; i < tableLayout.getChildCount(); i++) {



                TableRow otherRow = (TableRow)tableLayout.getChildAt(i);


                if(otherRow.getTag() != null) {
                    RowIndex  ++;

                    if (row == otherRow) {
                        GradientDrawable orderStyle;

                        if (RowIndex % 2 == 0) {
                            orderStyle = new GradientDrawable(
                                    GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{Color.BLACK, Color.parseColor("#9FDBD6"), Color.parseColor("#9FDBD6"), Color.BLACK});


                        } else {

                            orderStyle = new GradientDrawable(
                                    GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{Color.BLACK, Color.parseColor("#FDF8CB"), Color.parseColor("#FDF8CB"), Color.BLACK});

                        }
                        orderStyle.setCornerRadius(0f);
                        otherRow.setBackground(orderStyle);
                        otherRow.setPadding(0,20,0,20);

                        //item
                    }
                    else
                    {
                        otherRow.setBackground(null);
                        otherRow.setPadding(0,0,0,0);

                        if (RowIndex % 2 == 0)
                            otherRow.setBackgroundColor(Color.parseColor("#9FDBD6"));
                        else
                            otherRow.setBackgroundColor(Color.parseColor("#FDF8CB"));
                    }
                }
            }
        }
        private transient TableRow header_row;

        private void addHeaderRow(TableLayout table,ArrayList<ControlBase> controls){
            header_row = new TableRow(table.getContext());

            header_row.setBackground(getHeaderBackground());
            header_row.setGravity(Gravity.CENTER_VERTICAL);
            table.addView(header_row);
            TableLayout.LayoutParams headerP = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            header_row.setLayoutParams(headerP);
            //header_row.setTag(null);
            //header_row.setBackgroundColor(Color.parseColor("#008477"));
            //header_row.setPadding(5, 5, 5, 5);
            for (com.example.myapplication.model.Control.ControlBase control : controls) {
                control.addListHeader(header_row);
            }
        }

        protected void rowAdded(ArrayList<ControlBase> controls,JSONObject data){

        }





        protected String getWhere(String action){
            return  null;





        }
        protected int getTake(){
            return 0;
        }


        protected String getOrderBy(String action){
            return "Id Desc";
        }
        protected String getSelect(String action){
            ArrayList<ControlBase> controls = getControls(action);
            for (int i = 0; i < controls.size(); i++) {
                if(DetailedControlBase.class.isAssignableFrom(controls.get(i).getClass())){
                    DetailedControlBase ctrl = (DetailedControlBase)controls.get(i);
                    ctrl.setParentId(getValue());
                }
            }
            FieldList fields = new FieldList(0);
            fields.Fields.put("Id","it0.Id");
            for (com.example.myapplication.model.Control.ControlBase control : controls) {
                control.addForSelectQuery(fields);
            }
            return "it0 => " + fields.getSelectString();
        }



        @Override
        public void addForSelectQuery(FieldList list) {

            ArrayList<ControlBase> dtControls = getControls(ACTION_REFRESH);
            FieldList fields = new FieldList(list.Index + 1);
            fields.Fields.put("Id", "it" + (list.Index + 1) + ".Id");
            for (com.example.myapplication.model.Control.ControlBase control : dtControls) {
                control.addForSelectQuery(fields);
            }
            list.addForSelectQuery(getName(),getName(), getName() + ".Select(it" + (list.Index + 1) + "=>" + fields.getSelectString() + ")");
        }

        private transient JSONArray GridData = null;


        public static class AggregateValue{
            public AggregateValue(String aggregate){
                Aggregate = aggregate;
            }
            private  Double NumberValue = null;
            private Integer Count = 0;
            //private Object Obj = null;
            private String Aggregate = Control.AGGREGATE_SUM;
            public void putValue(Object value){
                if(value != null){
                    try{
                        Class clazz = value.getClass();
                        if (Aggregate.equals(Control.AGGREGATE_SUM) || Aggregate.equals(Control.AGGREGATE_AVERAGE) &&  Number.class.isAssignableFrom(clazz)) {
                            if(NumberValue == null)NumberValue = 0D;
                            NumberValue = NumberValue + Double.parseDouble(value.toString());
                        }
                        else if(Aggregate.equals(Control.AGGREGATE_MIN)){
                            if(Number.class.isAssignableFrom(clazz) && (NumberValue == null || NumberValue > Double.parseDouble(value.toString()))){
                                NumberValue = Double.parseDouble(value.toString());
                            }

                        }
                        else if(Aggregate.equals(Control.AGGREGATE_MAX)){
                            if(Number.class.isAssignableFrom(clazz) && (NumberValue == null || NumberValue < Double.parseDouble(value.toString()))){
                                NumberValue = Double.parseDouble(value.toString());
                            }
                        }
                        Count++;
                    }
                    catch (Exception e){

                    }
                }
            }
            public Object getValue(){
                if(NumberValue == null){
                    return null;
                }
                else if(Aggregate.equals(Control.AGGREGATE_AVERAGE)){
                    return  NumberValue / Double.parseDouble(Count.toString());
                }
                else if(Aggregate.equals(Control.AGGREGATE_COUNT)){
                    return  Double.parseDouble(Count.toString());
                }
                else {
                    return  NumberValue;
                }
            }



        }
        public Object getAggregateValue(Control.ControlBase control,JSONObject obj){

           return  control.getValue();
        }

        @Override
        public void refreshDetailedView(JSONArray data) {
            if(Table == null)GridData = data;
            else {
                ArrayList<ControlBase> controls = getControls(ACTION_REFRESH);
                String id_field_name = getIdFieldName();
                try {

                    if (getValues() == null) setValues(new ArrayList<Long>());
                    getValues().clear();
                    Table.removeAllViews();

                    addHeaderRow(Table, controls);
                    boolean selectionFound = false;
                    final TableLayout parentTable = Table;

                    ArrayList<ControlBase> AggregateControls = getControls(ACTION_REFRESH);
                    HashMap<String,AggregateValue> Aggregate = new HashMap<>();
                    for (int i = 0; i < AggregateControls.size(); i++) {
                        if(AggregateControls.get(i).getAggregate() != null && AggregateControls.get(i).getAggregate().length() !=0 && !Aggregate.containsKey(AggregateControls.get(i).getName())){


                            Aggregate.put(AggregateControls.get(i).getName(),new AggregateValue(AggregateControls.get(i).getAggregate()));
                        }
                    }
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject obj = (JSONObject) data.get(i);

                        if(obj.has(getIdFieldName()))
                            getValues().add(Long.parseLong(obj.get(getIdFieldName()).toString()));
                        else
                            getValues().add(0L);
                        TableRow item = new TableRow(Table.getContext());
                        item.setGravity(Gravity.CENTER_VERTICAL);
                        Table.addView(item);
                        TableRow.LayoutParams itemP = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        itemP.setMargins(0, 0, 0, 5);
                        item.setLayoutParams(itemP);
                        item.setTag(obj);
                        if (i % 2 == 0) item.setBackgroundColor(Color.parseColor("#9FDBD6"));
                        else item.setBackgroundColor(Color.parseColor("#FDF8CB"));
                        item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onRowSelected(item);
                            }
                        });
                        ArrayList<ControlBase> dtControls = getControls(ACTION_REFRESH);

                        for (com.example.myapplication.model.Control.ControlBase control : dtControls) {
                            control.addListDetails(item, obj);
                            if(Aggregate.containsKey(control.getName())) {
                                Object aggValue = getAggregateValue(control,obj);
                                if(aggValue != null) {
                                    Aggregate.get(control.getName()).putValue(aggValue);
                                }
                            }
                        }
                        rowAdded(dtControls,obj);
                        if (getValue() != null && Long.parseLong(obj.get(getIdFieldName()).toString()) == getValue()) {
                            try {
                                setValue(Long.parseLong(obj.get(getIdFieldName()).toString()));
                                selectRow(item, header_row, parentTable);
                                selectionFound = true;

                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                setValue(null);
                                selectionFound = false;
                            }
                        }

                    }
                    if(Aggregate.size() >0){

                        TableRow.LayoutParams itemP = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        GradientDrawable orderStyle = new GradientDrawable(
                                GradientDrawable.Orientation.TOP_BOTTOM,
                                new int[]{Color.parseColor("#182B36"), Color.parseColor("#6FC1F6"), Color.parseColor("#6FC1F6"), Color.parseColor("#182B36")});
                        TableRow item1 = new TableRow(Table.getContext());
                        item1.setLayoutParams(itemP);
                        item1.setGravity(Gravity.CENTER_VERTICAL);
                        item1.setBackground(orderStyle);
                        item1.setPadding(0,20,0,20);

                        Table.addView(item1,1);

                        TableRow item2 = new TableRow(Table.getContext());
                        item2.setLayoutParams(itemP);
                        item2.setGravity(Gravity.CENTER_VERTICAL);
                        item2.setBackground(orderStyle);
                        item2.setPadding(0,20,0,20);
                        Table.addView(item2);
                        for (com.example.myapplication.model.Control.ControlBase control : AggregateControls) {
                            if(Aggregate.containsKey(control.getName()))control.readValueObject(Aggregate.get(control.getName()).getValue());
                            control.addListDetails(item1);
                            control.addListDetails(item2);
                        }
                    }
                    if (selectionFound) {
                        if (getActionButton(Control.ACTION_EDIT) != null)
                            getActionButton(Control.ACTION_EDIT).setEnabled(true);
                        if (getActionButton(Control.ACTION_DELETE) != null)
                            getActionButton(Control.ACTION_DELETE).setEnabled(true);
                    } else {
                        if (getActionButton(Control.ACTION_EDIT) != null)
                            getActionButton(Control.ACTION_EDIT).setEnabled(false);
                        if (getActionButton(Control.ACTION_DELETE) != null)
                            getActionButton(Control.ACTION_DELETE).setEnabled(false);
                    }

                } catch (JSONException e) {
                    System.out.println(e.getMessage());
                    Toast.makeText(getRootActivity(), "GetListData Failed," + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }




        public void refreshGrid() {
            refreshGrid(getTable());
        }
        public void refreshGrid(TableLayout table){
            Table = table;
            ArrayList<ControlBase> controls = getControls(ACTION_REFRESH);
            String id_field_name = getIdFieldName();
            if(controls != null && controls.size() != 0){
                RequestParams rp = new RequestParams();
                String where = getWhere(ACTION_REFRESH);
                if(where != null && where.length() == 0)where = null;

                DataService.ListParams lp = new DataService.ListParams();
                lp.Select =getSelect(ACTION_REFRESH);
                lp.Where = where;
                lp.OrderBy = getOrderBy(ACTION_REFRESH);
                lp.Take = getTake();
                new DataService().postForList(getFullPathNew(),lp, jsonArray -> {
                    refreshDetailedView(jsonArray);
                    return null;
                },table.getContext());
            }
        }
        private void ShowAdd(ArrayList<LookupControlBase> popupInputs, ArrayList<ControlBase> controls){
            if(popupInputs == null || popupInputs.size() == 0){
                new PopupForm()
                    .setArgs(new PopupForm.PopupFormArgs(getCaption() + " Add",controls,getFullPathNew(),0L))
                    .show( getRootActivity().getSupportFragmentManager(),null);
            }else{
                popupInputs.get(0).onPopupList(getRootActivity(), (Function<DataService.Lookup, Void>) lookup -> {
                    popupInputs.remove(popupInputs.get(0));
                    ShowAdd(popupInputs,controls);
                    return null;
                });
            }
        }
        @Override
        public void onButtonClick(ActionButton action){

            if(action.getName().equals(Control.ACTION_ADD)){
                EditControls = getControls(action.getName());
                //String path = getPath()  == null || getPath().length() == 0 ? getName() + "[]" : getPath() + "." + getName() + "[]";
                for (int i = 0; i < EditControls.size(); i++) {
                    EditControls.get(i).setPath(getFullPath());
                }
                //if(getForeignFieldName() != null && getForeignFieldName().length() != 0 && getParentId() != null && getParentId() != 0L){
                //    EditControls.add(Control.getHiddenControl(getForeignFieldName(),getParentId()));
                //}
                ArrayList<LookupControlBase> popupInputs = (ArrayList<LookupControlBase>)EditControls.stream()
                    .filter(o -> o instanceof LookupControlBase)
                    .map(o -> (LookupControlBase)o)
                    .filter(o -> o.getPopupIndex() >= 0 && o.getValue() == null)
                    .sorted(Comparator.comparing(s -> s.getPopupIndex()))
                    .collect(Collectors.toList());
                ShowAdd(popupInputs,EditControls);
            }
            else if(action.getName().equals(Control.ACTION_EDIT)){

                new DataService().postForSelect(getFullPath(),getSelect(ACTION_EDIT), jsonObject -> {
                    //System.out.println(jsonObject.toString());
                    EditControls = getControls(action.getName());
                    //String path = getPath()  == null || getPath().length() == 0 ? getName()  : getPath() + "." + getName();
                    //path = path + "[" + getSelectedId() + "]";
                    for (int i = 0; i < EditControls.size(); i++) {
                        EditControls.get(i).setPath(getFullPath());
                    }
                    for (int i = 0; i < EditControls.size(); i++) {
                        if(DetailedControlBase.class.isAssignableFrom(EditControls.get(i).getClass())){
                            DetailedControlBase ctrl = (DetailedControlBase)EditControls.get(i);
                            ctrl.setParentId(getValue());
                        }
                    }
                    //if(getForeignFieldName() != null && getForeignFieldName().length() != 0 && getParentId() != null && getParentId() != 0L){
                    //    EditControls.add(Control.getHiddenControl(getForeignFieldName(),getParentId()));
                   //}
                    for (int i = 0; i < EditControls.size(); i++) {
                        EditControls.get(i).readValueJSONObject(jsonObject,EditControls.get(i).getName());
                    }
                    new PopupForm().setArgs(new PopupForm.PopupFormArgs(getCaption() + " Edit",EditControls,getFullPath(),getValue())).show( getRootActivity().getSupportFragmentManager(),null);
                    return null;
                }, getRootActivity());
            }
            else if(action.getName().equals(Control.ACTION_DELETE)){
                PopupConfirmation.create("Delete Confirmation", "Are you sure you want to delete?", new Function<Void, Boolean>() {
                    @Override
                    public Boolean apply(Void unused) {
                        new DataService().postForDelete(getFullPath(), b -> {
                            setValue(null);
                            refreshGrid(Table);
                            return null;
                        },  s -> {
                            PopupHtml.create("Save Error",s).show(getRootActivity().getSupportFragmentManager(),null);
                            return null;
                        });
                        return true;
                    }
                }).show(((BaseActivity)action.button.getContext()).getSupportFragmentManager(), null);
            }
            else if(action.getName().equals(Control.ACTION_REFRESH)){
                refreshGrid(Table);
            }
        }
        protected abstract ArrayList<ControlBase> getControls(String action);
    }
    public static class ImageControl extends DetailedControlBase<ImageControl> {
        public ImageControl(String name, String caption,String entityName){
            super(name, caption);
            setEntityName(entityName);
            ArrayList<ActionButton> buttons = new ArrayList<ActionButton>();
            buttons.add(new ActionButton(Control.ACTION_CAMERA));
            buttons.add(new ActionButton(Control.ACTION_GALLERY));
            buttons.add(new ActionButton(Control.ACTION_VIEW).setEnabled(false));
            buttons.add(new ActionButton(Control.ACTION_DELETE).setEnabled(false));

            setButtons(buttons);

        }

        private String EntityName;

        public String getEntityName() {
            return EntityName;
        }

        public ImageControl setEntityName(String entityName) {
            EntityName = entityName;
            return this;
        }

        public void onCapturedImage(int action, Bitmap image,Long id){
            addImage(id);
            getActionButton(Control.ACTION_DELETE).setEnabled(false);
            getActionButton(Control.ACTION_VIEW).setEnabled(false);

            //delete_button.setEnabled(false);
            for (int i = 0; i < main_layout.getChildCount(); i++) {
                ImageView iv = (ImageView)main_layout.getChildAt(i);
                Long vId = (Long)iv.getTag();
                if(id.equals(vId)) {
                    setValue(id);
                    iv.setBackgroundColor(Color.BLACK);
                    //iv.setBackgroundColor(Color.parseColor("#225C6E"));
                    getActionButton(Control.ACTION_DELETE).setEnabled(true);
                    getActionButton(Control.ACTION_VIEW).setEnabled(true);
                }
                else{
                    iv.setBackground(ContextCompat.getDrawable(main_layout.getContext(), R.drawable.button));


                }
            }
        }
        @Override
        public void addForSelectQuery(FieldList list) {
            String formula = "@0.RefFiles.Where(img=>img.RefId == " + getParentId() + " && img.RefType == \"" + getEntityName() + "\" && img.FileGroup == \"" + getName() + "\").Select(img => new{img.Id,img.FileName})";
            list.addForSelectQuery(getName(),getName(), formula);
        }

        @Override
        public void onButtonClick(ActionButton button) {
            if(button.Name.equals(Control.ACTION_DELETE)){
                PopupConfirmation.create("Delete Confirmation", "Are you sure you want to delete?", (unused)->{
                    new DataService().postForDelete("RefFiles[" + getValue() + "]", new Function<Boolean, Void>() {
                        @Override
                        public Void apply(Boolean s) {
                            getValues().remove(getValue());
                            for (int i = 0; i < main_layout.getChildCount(); i++) {
                                Object id = main_layout.getChildAt(i).getTag();
                                if(id != null && id.equals(getValue())){
                                    main_layout.removeView(main_layout.getChildAt(i));
                                    break;
                                }
                            }
                            setValue(null);
                            getActionButton(Control.ACTION_DELETE).setEnabled(false);
                            return null;
                        }
                    }, s -> {
                        PopupHtml.create("Save Error",s).show(getRootActivity().getSupportFragmentManager(),null);
                        return null;
                    });
                    return true;
                }).show(getRootActivity().getSupportFragmentManager(),null);
            }
            else if(button.Name.equals(Control.ACTION_CAMERA)) {
                getRootActivity().captureImage(BaseActivity.TAKE_IMAGE_FROM_CAMERA,getEntityName(),getName(), getParentId());
            }
            else if(button.Name.equals(Control.ACTION_GALLERY)) {
                getRootActivity().captureImage(BaseActivity.TAKE_IMAGE_FROM_GALLERY,getEntityName(),getName(),getParentId());
            }
            else if(button.Name.equals(Control.ACTION_VIEW)) {
                PopupImage.create("Image View", getValue()).show(getRootActivity().getSupportFragmentManager(),null);
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
            for (int i = 0; i < getValues().size(); i++) {
                addImage(getValues().get(i));
            }
        }



        private ImageView GetImageView(long id){
            ImageView imageView = new ImageView(main_layout.getContext());
            FlexboxLayout.LayoutParams lllP = new FlexboxLayout.LayoutParams(BaseActivity.ButtonWidth, BaseActivity.ButtonWidth);
            //lllP.setP(2,2,2,2);
            imageView.setLayoutParams(lllP);
            imageView.setTag(id);
            imageView.setPadding(2,2,2,2);
            if(getValue() != null && id == getValue()){
                //imageView.setBackgroundColor(Color.parseColor("#225C6E"));
                imageView.setBackgroundColor(Color.BLACK);
                getActionButton(Control.ACTION_DELETE).setEnabled(true);
                getActionButton(Control.ACTION_VIEW).setEnabled(true);
            }
            else{
                imageView.setBackground(ContextCompat.getDrawable(main_layout.getContext(), R.drawable.button));


            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setValue(null);
                    getActionButton(Control.ACTION_DELETE).setEnabled(false);
                    getActionButton(Control.ACTION_VIEW).setEnabled(false);
                    for (int i = 0; i < main_layout.getChildCount(); i++) {
                        ImageView iv = (ImageView)main_layout.getChildAt(i);
                        if(view == iv) {
                            setValue(id);
                            //iv.setBackgroundColor(Color.parseColor("#225C6E"));
                            iv.setBackgroundColor(Color.BLACK);
                            getActionButton(Control.ACTION_DELETE).setEnabled(true);
                            getActionButton(Control.ACTION_VIEW).setEnabled(true);
                        }
                        else{
                            iv.setBackground(ContextCompat.getDrawable(main_layout.getContext(), R.drawable.button));


                        }
                    }
                }
            });

            //imageView.setTag(id);
            main_layout.addView(imageView);
            return imageView;
        }
        private void addImage(long id) {
            if(!getValues().contains(id)){
                getValues().add(id);
            }
            ImageView imageView = GetImageView(id);
            new DataService().get("EntityApi/GetImage/" + id, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                    imageView.setImageBitmap(bmp);

                    //System.out.println(main_layout.getChildCount());
                }
                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }

        @Override
        public void refreshDetailedView(JSONArray data) {

        }
    }
    public static abstract class DetailedControlBase<T extends ControlBase<T,Long>> extends ControlBase<T, Long> {

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
        protected Long convertValue(Object value) {
            if(value == null || value == JSONObject.NULL)return null;
            else if(value.getClass().equals(Long.class))return(Long)value;
            else return null;
        }


        @Override
        public String getFullPath(){

            if(getName() == null)return null;
            if(getName().equals("."))return getPath();

            String path;

            if(getPath() == null || getPath().length() == 0)path = "";
            else path =  getPath() + ".";
            if(getValue() == null || getValue() == 0L){
                path = path + getName() + "[]";
            }else{
                path = path + getName() + "[" + getValue() + "]";
            }
            return path;
        }

        public String getFullPathNew(){
            String path = getFullPath();
            if(path == null)return null;
            if(path.endsWith("[]"))return path;
            else if(path.endsWith("]"))return path.substring(0,path.lastIndexOf("[")) + "[]";
            else return path + "[]";
        }

        @Override
        protected Drawable getEditorBackground() {
            return null;
        }

        private ArrayList<Long> Values = new ArrayList<>();



        public T setValues(ArrayList<Long> values) {
            Values = values;
            return (T)this;
        }

        public ArrayList<Long> getValues() {
            return Values;
        }

        private boolean initialFocus = false;
        @Override
        protected void requestFocus() {
            if(getButtons() != null && getButtons().size() > 0 && getButtons().get(0).button != null)getButtons().get(0).button.requestFocus();
            else initialFocus = true;
        }


        public DetailedControlBase(String name,String caption){
            super(name,caption);


            setControlSize(RelativeLayout.LayoutParams.MATCH_PARENT);
        }

        @Override
        public T setCaption(String caption) {
            super.setCaption(caption);
            if(CaptionTextView != null)CaptionTextView.setText(caption);
            return (T)this;
        }

        @Override
        public boolean validate() {
            boolean valid = true;
            if (getParentId() == null || getParentId() == 0) valid = true;
            else if (!getIsRequired()) valid = true;
            else if (getValues() == null || getValues().size() == 0) valid = false;
            if (CaptionTextView != null) {
                if (rl != null && valid) rl.setBackground(getHeaderBackground());
                else if (rl != null) rl.setBackground(getHeaderErrorBackground());
                else if (CaptionTextView != null && valid)
                    CaptionTextView.setBackground(getHeaderBackground());
                else if (CaptionTextView != null)
                    CaptionTextView.setBackground(getHeaderErrorBackground());
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
                            if(button.getEnabled()){
                                onButtonClick(button);
                            }
                            return null;
                        }
                    });
                    if(button.button != null)button.button.setEnabled(getEnabled());
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

        //@Override
        //public void updateSaveParameters(RequestParams params) {
        //    params.put(getForeignFieldName(),getParentId());
        //}

        public abstract void refreshDetailedView(JSONArray data);

        @Override
        public T readValueObject(Object value) {
            if (value != null && JSONArray.class.isAssignableFrom(value.getClass())) {
                try {
                    JSONArray jArray = (JSONArray) value;
                    ArrayList a = new ArrayList<Long>();
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject obj = (JSONObject) jArray.get(i);
                        a.add(Long.parseLong(obj.get(getIdFieldName()).toString()));
                    }
                    setValues(a);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    setValues(new ArrayList<>());
                }
                refreshDetailedView((JSONArray)value);
            }
            else if (value != null && Long.class.isAssignableFrom(value.getClass())) {
                setValue((Long) value);
            }
            return (T)this;
        }

        private boolean  EnableScroll = false;

        public boolean getEnableScroll() {
            return EnableScroll;
        }

        public T setEnableScroll(boolean enableScroll) {
            EnableScroll = enableScroll;
            return (T)this;
        }
        @Override
        public  T updateValueToJSONObject(JSONObject data) {

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

        private Long ParentId;
        public void changeVisibility(boolean visible){
            if(visible && RootLayout != null)RootLayout.setVisibility(View.VISIBLE);
            else if(RootLayout != null) RootLayout.setVisibility(View.GONE);

        }


        public T setParentId(Long id) {

            ParentId = id;
            return (T)this;
        }
        public Long getParentId() {
            return ParentId;
        }




        @Override
        public void valueChange(Long oldValue, Long newValue) {

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
            //setColumnWidth(195);
        }
        @Override
        protected void onButtonClick(ActionButton button) {
            if(button.getName().equals("Search")){
                onBrowse(button);
            }

        }
        @Override
        public Date getValue() {
            return super.getValue();
        }

        @Override
        public EditTextControlBase<T, Date> setValue(Date value) {
            return super.setValue(value);
        }



        protected abstract void onBrowse(ActionButton button);




        @Override
        protected Date convertValue(Object value) {
            if(value==null || value ==JSONObject.NULL){
                return null;
            }
            else if(value.getClass().equals(Date.class)){
                return  (Date)value;
            }
            else{
                Date date = null;
                String input = value.toString().trim();
                HashMap<String,Integer> formats = new HashMap<String,Integer>();
                if(input.contains("T")){
                    formats.put("yyyy-MM-dd'T'HH:mm:ss.SSS",23);
                    formats.put("yyyy-MM-dd'T'HH:mm:ss.SS",22);
                    formats.put("yyyy-MM-dd'T'HH:mm:ss.S",21);
                    formats.put("yyyy-MM-dd'T'HH:mm:ss",19);
                    formats.put("yyyy-MM-dd'T'HH:mm",16);
                }
                else
                {
                    formats.put("dd/MM/yy HH:mm:ss.S",21);
                    formats.put("dd/MM/yy HH:mm:ss",17);
                    formats.put("dd/MM/yy HH:mm",14);
                    formats.put("yyyy-MM-dd",10);
                    formats.put("dd/MM/yy",8);
                }
                for (String format: formats.keySet()) {
                    if(input.length() == formats.get(format).intValue()){
                        DateFormat dateFormat = new SimpleDateFormat(format);
                        try{
                            date = dateFormat.parse(value.toString());
                            return date;
                        }
                        catch (ParseException e){
                            System.out.println(e.getMessage());
                        }
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
                System.out.println(e.getMessage());
                return false;
            }
        }


        @Override
        public String getFormatValue(Date value) {
            if(value==null)return null;

            DateFormat format = new SimpleDateFormat(getFormat());
            return format.format(value);
        }

        @Override
        public T updateValueToJSONObject(JSONObject data) {

            if(getValue() == null)updateValueToJSONObject(data,getName(),null);
            else {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                updateValueToJSONObject(data,getName(),format.format(getValue()));
            }
            return (T)this;
        }
    }



    public static abstract class SearchControlBase extends LookupForeignControlBase<SearchControlBase>{
        public SearchControlBase(String name, String caption,List<Control.ControlBase> controls,String displayField) {
            super(name, caption,displayField);
            //getButtons().add(new ActionButton("Search"));
            setControls(controls);
            setControlSize(Control.CONTROL_SIZE_DOUBLE);
            setIdField("Id");
            setKeywordsField("keyWords");
            setSearchKey(32);
        }



        private transient TextView LookupTextView;
        private List<Control.ControlBase> Controls;
        public List<Control.ControlBase> getControls() {
            return Controls;
        }

        public <A extends Control.ControlBase> A getControl(String name){
            if(Controls == null)return null;
            Optional<Control.ControlBase> control = Controls.stream().filter(i-> i.getName().equals(name)).findFirst();
            if(control.isPresent())return (A)control.get();
            else return null;
        }

        private String KeywordsField;
        public String getKeywordsField() {
            return KeywordsField;
        }
        public SearchControlBase setKeywordsField(String keywordsField) {
            KeywordsField = keywordsField;
            return this;
        }
        private String IdField;
        public String getIdField() {
            return IdField;
        }
        public SearchControlBase setIdField(String idField) {
            IdField = idField;
            return this;
        }

        private int SearchKey;
        public int getSearchKey() {
            return SearchKey;
        }
        public SearchControlBase setSearchKey(int searchKey) {
            SearchKey = searchKey;
            return this;
        }

        public SearchControlBase setControls(List<Control.ControlBase> controls) {
            Controls = controls;
            return this;
        }
        protected abstract void refreshDetailedView(String keywords, Function<JSONArray, Void> callBack);


        protected transient PopupSearch Popup;

        protected boolean itemSelected(TableRow row, JSONObject data, DataService.Lookup lookup)
        {
            setValue(lookup);
            return true;
        }
        protected void textChange(EditText editor, int keyCode) {
            if(keyCode == getSearchKey()){
                refreshDetailedView(editor.getText().toString(), new Function<JSONArray, Void>() {
                    @Override
                    public Void apply(JSONArray array) {
                        Popup.refreshDetailedView(array);
                        return null;
                    }
                });
            }
        }

        protected PopupSearch.PopupSearchListener createListener(){
            return new PopupSearch.PopupSearchListener() {
                @Override
                public boolean onItemSelected(TableRow row, JSONObject data, DataService.Lookup lookup) {
                    return itemSelected(row,data,lookup);
                }

                @Override
                public void onTextChange(EditText editor, int keyCode) {
                    textChange(editor,keyCode);
                }

                @Override
                public boolean onPressOk() {
                    setValue(null);
                    return true;
                }
            };
        }


        @Override
        protected void onPopupList() {
            Popup = PopupSearch.create(getCaption(), getControls(), getDisplayField());
            Popup.setListener(createListener());
            Popup.getArgs().setAllowNull(!getIsRequired());
            Popup.getArgs().setIdField(getIdField());
            Popup.getArgs().setKeywordsField(getKeywordsField());
            Popup.show(getRootActivity().getSupportFragmentManager(),null);
        }
    }



    public static class LookupForeignControl extends LookupForeignControlBase<LookupForeignControl> {
        public LookupForeignControl(String name, String caption,String displayField) {
            super(name, caption, displayField);

        }
        public void addNewRecord(ArrayList<ControlBase> controls,String actionPath){
            addNewRecord(getFullPath(),controls,actionPath);
        }





        private String Where;
        public String getWhere() {
            return Where;
        }
        public LookupForeignControl setWhere(String where) {
            Where = where;
            return this;
        }
        public String getOrderBy() {
            if(getFormula() == null || getFormula().length() == 0) return getDisplayField();
            else return getFormula();
        }
        public String getSelect() {
            if(getFormula() == null || getFormula().length() == 0) return "new {" + getName() + ".Id," + getName() + "." + getDisplayField() + " as Name}";
            else return "new {it0." + getName() + ".Id, " + getFormula().replace("{0}","it0") + " as Name}";
        }
        protected ArrayList<ControlBase> getAddControls(){
            return  new ArrayList<>();
        }



        @Override
        protected void onPopupList() {
            String select = "it0 => new {it0.Id, it0." + getDisplayField() + " as Name}";
            String orderBy = getDisplayField();
            if(getOrderBy() != null && getOrderBy().length() !=0)orderBy = getOrderBy();
            else if(getFormula() != null && getFormula().length() != 0)orderBy = getFormula().replace("{0}." + getName(),"it0");
            if(getFormula() != null && getFormula().length() != 0){
                select = "it0 => new {it0.Id, it0." + getFormula().replace("{0}." + getName(),"it0") + " as Name}";
            }
            new DataService().postForList(DataService.Lookup.class, getFullPath(), select, getWhere(), orderBy, lookups -> {
                PopupLookup.create(getCaption(),lookups,getValue() == null? null : getValue().getId(),(lookup)->{
                    setValue(lookup);
                    return true;
                }).show(getRootActivity().getSupportFragmentManager(),null);
                return null;
            }, getRootActivity());
        }

    }


    public static abstract class LookupForeignControlBase<T extends LookupForeignControlBase<T>> extends LookupControlBase<T> {
        public LookupForeignControlBase(String name, String caption,String displayField) {
            super(name, caption, displayField);
            //setEntityName(entityName);
        }

        /*

        private String EntityName;
        public String getEntityName() {
            return EntityName;
        }
        public T setEntityName(String entityName) {
            EntityName = entityName;
            return (T)this;
        }

         */

        @Override
        public T readValueObject(Object value) {
            if(value != null  && Long.class.isAssignableFrom(value.getClass())) {
                String json ="new {Id," + getDisplayField() + " as Name}";
                if(getFormula() != null && getFormula().length() != 0) {
                    if(getName().equals("."))
                        json = "it0 => new {it0.Id," + getFormula().replace("{0}", "it0") + " as Name}";
                    else
                        json = "it0 => new {it0.Id," + getFormula().replace("{0}." + getName(), "it0") + " as Name}";
                }
                String path = getFullPath() + "[" + value + "]";
                if(getName().equals("."))path = getPath();
                new DataService().postForSelect(DataService.Lookup.class,path,json, lookup -> {
                    setValue(lookup);
                    if(txtValue != null){
                        txtValue.setText(lookup.getName());
                    }
                    return null;
                }, getRootActivity());
            }
            else {
                super.readValueObject(value);
            }
            return (T)this;
        }
    }




    public static class LookupListControl extends LookupControlBase<LookupListControl> {
        public LookupListControl(String name, String caption, String displayField, List<DataService.Lookup> lookups) {
            super(name, caption, displayField);
            setLookups(lookups);
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
        protected void onPopupList() {
            PopupLookup.create(getCaption(),getLookups(),getValue() == null? null : getValue().getId(),(lookup)->{
                //callBack.apply(lookup);
                setValue(lookup);
                return true;
            }).show(getRootActivity().getSupportFragmentManager(),null);
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

        public static int POPUP_INDEX_HIDE = -1;
        public static int POPUP_INDEX_AUTO = 0;


        protected transient TextView txtValue;
        public LookupControlBase(String name, String caption,String displayField) {
            super(name, caption);
            setDisplayField(displayField);
            setControlSize(CONTROL_SIZE_DOUBLE);
            getButtons().add(new ActionButton(Control.ACTION_SEARCH));
            setPopupIndex(POPUP_INDEX_AUTO);
        }
        protected abstract void onPopupList();


        private transient Function<DataService.Lookup,Void> Notify = null;
        public void onPopupList(BaseActivity activity,Function<DataService.Lookup,Void> notify){
            setRootActivity(activity);
            Notify = notify;
            onPopupList();
        }

        @Override
        protected void onButtonClick(ActionButton button) {
            if(button.Name.equals(Control.ACTION_SEARCH)){
                onPopupList();
            }
        }


        public void addNewRecord(String path,ArrayList<ControlBase> controls,String actionPath){
            for (int i = 0; i < controls.size(); i++) {
                controls.get(i).setPath(path);
            }

            ArrayList<LookupControlBase> popupInputs = (ArrayList<LookupControlBase>)controls.stream()
                    .filter(o -> o instanceof LookupControlBase)
                    .map(o -> (LookupControlBase)o)
                    .filter(o -> o.getPopupIndex() >= 0 && o.getValue() == null)
                    .sorted(Comparator.comparing(s -> s.getPopupIndex()))
                    .collect(Collectors.toList());
            ShowAdd(popupInputs,controls,path,actionPath);
        }

        protected String getSelect(ArrayList<ControlBase> controls){
            FieldList fields = new FieldList(0);
            fields.Fields.put("Id","it0.Id");
            for (com.example.myapplication.model.Control.ControlBase control : controls) {
                control.addForSelectQuery(fields);
            }
            return "it0 => " + fields.getSelectString();
        }

        public void editRecord(String path,ArrayList<ControlBase> controls,String actionPath){
            new DataService().postForSelect(path,getSelect(controls), jsonObject -> {
                try {
                    for (int i = 0; i < controls.size(); i++) {
                        controls.get(i).setPath(path);
                        if(DetailedControlBase.class.isAssignableFrom(controls.get(i).getClass())){
                            DetailedControlBase ctrl = (DetailedControlBase)controls.get(i);
                            ctrl.setParentId(jsonObject.getLong("Id"));
                        }
                        controls.get(i).readValueJSONObject(jsonObject,controls.get(i).getName());
                    }
                    new PopupForm().setArgs(new PopupForm.PopupFormArgs(getCaption() + " Edit",controls,path,jsonObject.getLong("Id")).setActionPath(actionPath))
                            .show( getRootActivity().getSupportFragmentManager(),null);
                } catch (JSONException e) {

                }
               return null;
            }, getRootActivity());
        }
        private void ShowAdd(ArrayList<LookupControlBase> popupInputs, ArrayList<ControlBase> controls,String path,String actionPath){

            if(popupInputs == null || popupInputs.size() == 0){
                new PopupForm()
                        .setArgs(new PopupForm.PopupFormArgs(getCaption() + " Add",controls,path,0L).setActionPath(actionPath))
                        .show( getRootActivity().getSupportFragmentManager(),null);

            }else{
                popupInputs.get(0).onPopupList(getRootActivity(),new Function<DataService.Lookup, Void>() {
                    @Override
                    public Void apply(DataService.Lookup lookup) {
                        popupInputs.remove(popupInputs.get(0));
                        ShowAdd(popupInputs,controls,path,actionPath);
                        return null;
                    }
                });
            }
        }

        @Override
        public DataService.Lookup getValue() {
            return super.getValue();
        }

        @Override
        public T setValue(DataService.Lookup value) {
            super.setValue(value);
            if(Notify != null) {
                Notify.apply(value);
                Notify = null;
            }
            return (T)this;
        }

        private boolean initialFocus = false;
        @Override
        protected void requestFocus() {
            if(getButtons() != null && getButtons().size() > 0 && getButtons().get(0).button != null)getButtons().get(0).button.requestFocus();
            else initialFocus = true;
        }
        @Override
        public void addForSelectQuery(FieldList list) {
            list.addForSelectQuery(getName() + ".Id",getName() + ".Id",null);
            list.addForSelectQuery(getName() + "." + getDisplayField(),getName() + "." + getDisplayField(),getFormula());
        }
        @Override
        public T  updateValueToJSONObject(JSONObject data) {
            if(getValue() == null)
                updateValueToJSONObject(data,getName() + ".Id",null);
            else
                updateValueToJSONObject(data,getName() + ".Id",getValue().getId());
            return (T) this;
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
            if(value == null || value == JSONObject.NULL)return null;
            else if(DataService.Lookup.class.isAssignableFrom(value.getClass()))return (DataService.Lookup)value;
            else if(JSONObject.class.isAssignableFrom(value.getClass())) {
                try{
                    JSONObject vjo = (JSONObject) value;
                    if(vjo.has("Id")){
                        String display = "[UNKNOWN]";
                        if(vjo.has(getDisplayField())){
                            display = vjo.get(getDisplayField()).toString();
                        }
                        return new DataService.Lookup(Long.parseLong(vjo.get("Id").toString()),display);
                    }
                }catch (JSONException e){
                    System.out.println(e.getMessage());

                }
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

        private int PopupIndex;
        public int getPopupIndex() {
            return PopupIndex;
        }
        public T setPopupIndex(int popupIndex) {
            PopupIndex = popupIndex;
            return (T)this;
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
            if(value == null || value == JSONObject.NULL)return null;
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



    public static class EditTextPickerControl extends EditTextControlBase<EditTextPickerControl,String> {

        public EditTextPickerControl(String name, String caption,ArrayList<String> options,String defaultValue) {
            super(name, caption);
            Options = options;
            DefaultValue = defaultValue;
            getButtons().add(new ActionButton(Control.ACTION_SEARCH));
        }

        public String DefaultValue;

        public String getDefaultValue() {
            return DefaultValue;
        }

        public EditTextPickerControl setDefaultValue(String defaultValue) {
            DefaultValue = defaultValue;
            return this;
        }

        ArrayList<String> Options;

        public ArrayList<String> getOptions() {
            return Options;
        }

        public EditTextPickerControl setOptions(ArrayList<String> options) {
            Options = options;
            return this;
        }

        @Override
        protected void onButtonClick(ActionButton button) {
            if(button.getName() == Control.ACTION_SEARCH){
                PopupLookup.create(getCaption(), getOptions(), DefaultValue, new Function<DataService.Lookup, Boolean>() {
                    @Override
                    public Boolean apply(DataService.Lookup lookup) {
                        if(lookup == null)setValue(null);
                        else setValue(lookup.getName());
                        return true;
                    }
                }).show(((BaseActivity)button.button.getContext()).getSupportFragmentManager(),null);

            }
        }

        @Override
        protected String convertValue(Object value) {
            if(value == null || value.equals(JSONObject.NULL))return null;
            else return value.toString();
        }

        @Override
        protected boolean IsInputValid(String value) {
            return true;
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
            if(value == null || value == JSONObject.NULL || value.toString().length() == 0)return  null;
            else return Double.parseDouble(value.toString());
        }
        @Override
        protected boolean IsInputValid(String value) {
            try {
                Double.parseDouble(value);
                return true;
            }catch (Exception e){
                System.out.println(e.getMessage());
                return false;
            }
        }


        private String DecimalFormat = null;
        public String getDecimalFormat() {
            return DecimalFormat;
        }
        public EditDecimalControl setDecimalFormat(String decimalFormat) {
            DecimalFormat = decimalFormat;
            return this;
        }

        @Override
        public String getFormatValue(Double value) {
            if(value == null)return null;
            if(DecimalFormat != null && DecimalFormat.length() !=0){
                DecimalFormat df = new DecimalFormat( DecimalFormat);
                return df.format(value);
            }
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


    public static class EditIntegerControl extends EditTextControlBase<EditIntegerControl,Integer> {

        public EditIntegerControl(String name, String caption) {
            super(name, caption);
            setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            setDigits("0123456789");
            setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }
        @Override
        protected void onButtonClick(ActionButton button) {
        }


        @Override
        public Integer convertValue(Object value) {
            if(value == null || value == JSONObject.NULL || value.toString().length() == 0)return  null;
            else return Integer.parseInt(value.toString());
        }
        @Override
        protected boolean IsInputValid(String value) {
            try {
                Integer.parseInt(value);
                return true;
            }catch (Exception e){
                System.out.println(e.getMessage());
                return false;
            }
        }
        @Override
        public String getFormatValue(Integer value) {
            if(value == null)return null;

            else
            {
                return value.toString();
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
        public void addListDetails(TableRow row, JSONObject data) {

        }

        @Override
        public void addListHeader(TableRow row) {

        }

        @Override
        protected Serializable convertValue(Object value) {
            if(value == null || value == JSONObject.NULL) return  null; else return  (Serializable)value;
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
        protected transient  EditText EditTextInput;
        public EditText getEditTextInput() {
            return EditTextInput;
        }
        public T setEditTextInput(EditText editTextInput) {
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
        public T setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if(EditTextInput != null)EditTextInput.setEnabled(enabled);
            return (T)this;
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
            EditTextInput.setEnabled(getEnabled());
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
                CaptionTextView.setTypeface(null, Typeface.BOLD);
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
                    if(button.button != null)button.button.setEnabled(getEnabled());
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

    public static abstract   class ControlBase<T extends ControlBase<T,U>,U extends Serializable>  implements Serializable {

        protected abstract void onButtonClick(ActionButton button);
        private int ControlSize = Control.CONTROL_SIZE_SINGLE;
        public int getControlSize(){
            return ControlSize;
        }
        public T setControlSize(int size){
            ControlSize = size;
            return  (T)this;
        }
        public int getAction() {
            return Action;
        }
        private int Action;

        public T setAction(int action) {
            Action = action;
            return (T)this;
        }

        private String Formula;

        public String getFormula() {
            return Formula;
        }

        public T setFormula(String formula) {
            Formula = formula;
            return (T)this;
        }

        public int getWidth(){
            int singleSize = BaseActivity.ControlWidth;
            if(ControlSize<-5)return Math.abs(ControlSize) * singleSize / 10;
            else return ControlSize;
        }
        public ActionButton getActionButton(String action){
            Optional<ActionButton> button = this.getButtons().stream().filter(i-> i.Name.equals(action)).findFirst();
            if(button.isPresent())return  button.get();
            else return  null;
        }
        protected Drawable getHeaderBackground(){
            GradientDrawable orderStyle = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {Color.parseColor("#012723"),Color.parseColor("#BA008477"),Color.parseColor("#BA008477"),Color.parseColor("#012723")});
            orderStyle.setCornerRadius(0f);
            return orderStyle;
        }

        protected Drawable getEditorBackground(){
            GradientDrawable orderStyle;

            if(getIsRequired() && getEnabled()){
                orderStyle = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[] {Color.parseColor("#FDF8CB"),Color.parseColor("#FDF8CB"),Color.parseColor("#FDF8CB"),Color.GRAY});


                //#FDF8CB
            }
            else if(getEnabled()){
                orderStyle = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[] {Color.WHITE,Color.WHITE,Color.WHITE,Color.GRAY});

            }else{
                orderStyle = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[] {Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY});

            }


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
                    new int[] {Color.parseColor("#500505"),Color.parseColor("#BAD51212"),Color.parseColor("#BAD51212"),Color.parseColor("#500505")});
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

            RootLayout.setEnabled(getEnabled());
            container.addView(RootLayout);
            //container.setBackground(getEditorBackground());
            addContentView(RootLayout);
        }

        private boolean Enabled = true;
        public boolean getEnabled(){
            return Enabled;
        }
        public T setEnabled(boolean enabled) {
            Enabled = enabled;
            if(RootLayout != null)RootLayout.setEnabled(enabled);
            for (int i = 0; i < getButtons().size(); i++) {
                if(getButtons().get(i).button != null)getButtons().get(i).button.setEnabled(enabled);
            }
            return (T)this;
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

        public T addButton(String name) {

            return addButton(name,null);
        }
        public T addButton(String name,Function<View, Boolean> onClick) {

            Control.ActionButton button = new ActionButton(name);
            button.setOnClick(onClick);
            getButtons().add(button);
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

        private String Path;
        public String getPath(){
            return  Path;
        }
        public String getFullPath(){
            if(getPath() == null || getPath().length() == 0)return getName();
            else return getPath() + "." + getName();
        }


        public T setPath(String path) {
            Path = path;
            return (T)this;
        }


        private String Aggregate = null;
        public String getAggregate(){
            return  Aggregate;
        }
        public T setAggregate(String caption) {
            Aggregate = caption;
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
                if(valueChangedListener != null)valueChangedListener.invoke(oldValue,value);
            }
            else{
                Value = value;
            }
            return (T)this;
        }

        private float ColumnWeight = 1;

        public T setColumnWeight(float columnWeight) {
            ColumnWeight = columnWeight;
            return (T)this;
        }

        public float getColumnWeight() {
            return ColumnWeight;
        }

        private  int ColumnWidth = 0;

        public T setColumnWidth(int columnWidth) {
            ColumnWidth = columnWidth;
            return (T)this;
        }

        public int getColumnWidth() {
            return ColumnWidth;
        }

        public void addListHeader(TableRow row){
            TextView hc = new TextView(row.getContext());

            hc.setPadding(10, 10, 10, 10);
            TableRow.LayoutParams hcP = new TableRow.LayoutParams(ColumnWidth,TableRow.LayoutParams.WRAP_CONTENT,ColumnWeight);
            hc.setLayoutParams(hcP);
            hc.setTextColor(ContextCompat.getColor(row.getContext(), R.color.white));
            hc.setText(getCaption());
            hc.setTextAlignment(TextAlignment);
            hc.setGravity(Gravity.CENTER_VERTICAL);
            //hc.setBackground(getHeaderBackground());
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

        protected transient TextView ListTextView;

        public TextView getListTextView() {
            return ListTextView;
        }

        //private void readValue(JSONObject obj,String field){

        //}

        public Control.ActionButton getButton(String name){
           if(getButtons() == null)return null;
            Optional<Control.ActionButton> button = getButtons().stream().filter(i-> i.getName().equals(name)).findFirst();
            if(button.isPresent())return button.get();
            else return null;
        }


        public void addListDetails(TableRow row, JSONObject data){
            setValue(null);
            readValueJSONObject(data,getName());
            addListDetails(row);
        }

        public void addListDetails(TableRow row){
            ListTextView = new TextView(row.getContext());
            TableRow.LayoutParams hcP = new TableRow.LayoutParams(ColumnWidth, TableRow.LayoutParams.MATCH_PARENT,ColumnWeight);
            ListTextView.setLayoutParams(hcP);
            ListTextView.setPadding(10,0,10,0);
            ListTextView.setGravity(Gravity.CENTER_VERTICAL);
            ListTextView.setTextAlignment(TextAlignment);
            ListTextView.setText(getFormatValue(getValue()));
            row.addView(ListTextView);
        }

        //protected boolean specialCharacterFound(String  word){
        //    Pattern p = Pattern.compile("[^a-z0-9_]", Pattern.CASE_INSENSITIVE);
        //    Matcher m = p.matcher(word);
        ///    return m.find();
        //}

        protected static class FieldList implements  Serializable{
            public FieldList(int index){
                Index  = index;
            }

            public String Root = null;
            public int Index;



            public HashMap<String,String> Fields = new HashMap<>();
            public HashMap<String,FieldList> ForeignFields = new HashMap<>();

            public String getSelectString(){


                List<String> fields = new ArrayList<>();
                for (String key: Fields.keySet()) {
                    fields.add(Fields.get(key));
                }
                for (String key : ForeignFields.keySet()) {
                    fields.add(Root + " == null ? null :" + ForeignFields.get(key).getSelectString() + " as " + key);
                }

                return "new {" + String.join(", ", fields) + "}" ;
            }

            public Serializable Value = null;



            public void addForSelectQuery(String name,String fullName,String formula){
                int dotIndex = name.indexOf('.');
                if(dotIndex<0 && !Fields.containsKey(name))
                    if(formula == null || formula.length() == 0){
                        Fields.put(name,"it" + Index + "." + fullName);
                    }
                    else {
                        String newFormula = formula;
                        for (int i = 0; i <= Index; i++) {
                            formula = formula.replace("{" + (Index - i) + "}","it" + i);
                        }
                        Fields.put(name, "(" + formula + ") as " + name);
                    }
                else if (dotIndex>0) {
                    String forObj = name.substring(0,dotIndex);
                    Root = "it" + Index  + "." + fullName.substring(0,fullName.length() - name.length() + dotIndex);
                    FieldList fl;
                    if(!ForeignFields.containsKey(forObj)) {
                        fl = new FieldList(Index);
                        fl.Fields.put("Id",Root + ".Id");
                        ForeignFields.put(forObj, fl);
                    }
                    else{
                        fl = ForeignFields.get(forObj);
                    }
                    fl.addForSelectQuery(name.substring(dotIndex + 1),fullName,formula);
                }
            }

        }


        public void addForSelectQuery(FieldList list) {
            list.addForSelectQuery(getName(),getName(),getFormula());
        }

        public abstract void valueChange(U oldValue, U newValue);
        private transient boolean viewCreated =false;
        protected abstract void addValueView(ViewGroup container);

        private transient Function2<U,U,Void> valueChangedListener;
        public T setValueChangedListener(Function2<U, U, Void> valueChangedListener) {
            this.valueChangedListener = valueChangedListener;
            return (T)this;
        }

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
        public  String getQueryValue(){
            Object value = getValue();
            if (value==null)return  "null" ;
            else  if(value.getClass().equals(Date.class)){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                return "\"" +  dateFormat.format(value) + "\"";
            }
            else  if(value.getClass().equals(String.class)){
                return "\"" + value.toString() + "\"";
            }
            else{
                return  value.toString();
            }
        }


        public T readValueObject(Object value){
            setValue(convertValue(value));
            return (T)this;
        }
        protected abstract U convertValue(Object value);
        public  T updateValueToJSONObject(JSONObject data) {
            updateValueToJSONObject(data,getName(),getValue());
            return (T)this;
        }
        protected  void updateValueToJSONObject(JSONObject data,String field, Serializable value) {
            int dotIndex = field.indexOf('.');
            try{
                if(dotIndex<0) {
                    data.put(field,value);
                }
                else if(dotIndex >0){
                    String fi = field.substring(0,dotIndex);
                    JSONObject subobj;
                    if(data.has(fi))subobj = (JSONObject) data.get(fi);
                    else{
                        subobj = new JSONObject();
                        data.put(fi,subobj);
                    }
                    updateValueToJSONObject(subobj,field.substring(dotIndex +1),value);
                }
            }
            catch (JSONException e){
                System.out.println(e.getMessage());

            }

        }


        public  T readValueJSONObject(JSONObject data, String field){
            int dotIndex = field.indexOf('.');
            try{
                if(dotIndex<0 && data.has(field)) {
                    readValueObject(data.get(field));
                }
                else if(dotIndex >0){
                    String fi = field.substring(0,dotIndex);
                    if(data.has(fi) && JSONObject.class.isAssignableFrom(data.get(fi).getClass())){
                        readValueJSONObject((JSONObject) data.get(fi),field.substring(dotIndex +1));
                    }
                }
            }
            catch (JSONException e){
                System.out.println(e.getMessage());

            }

            return  (T)this;
        }
        public boolean validate(){
            if(getValue()==null && getIsRequired()) return  false;
            else return true;
        }


    }
    public static class ActionButton implements Serializable {
        public ActionButton(String name)
        {
            setName(name);

        }


        protected transient Function<View,Boolean> onClick;

        public ActionButton setOnClick(Function<View, Boolean> onClick) {
            this.onClick = onClick;
            return this;
        }

        public Function<View, Boolean> getOnClick() {
            return onClick;
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
        private int Width = BaseActivity.ActionButtonWidth;
        public int getWidth() {
            return Width;
        }
        public ActionButton setWidth(int width) {
            Width = width;
            return  this;
        }
        private int Height = BaseActivity.ActionButtonWidth;
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

        private ArrayList<VectorDrawableCreator.PathData> getPaths(String path, boolean enabled){
           return getPaths(new String[]{path},enabled);
        }
        private ArrayList<VectorDrawableCreator.PathData> getPaths(String[] paths, boolean enabled){
            int colour = getButtonColor();
            if(!enabled)colour =getButtonColorDisable();
            ArrayList<VectorDrawableCreator.PathData> pathArray = new ArrayList<VectorDrawableCreator.PathData>();
            pathArray.add(new VectorDrawableCreator.PathData("M 18 0 L 2 0 c -1.1 0 -2 0.9 -2 2 v 20 c 0 1.1 0.9 2 2 2 h 20 c 1.1 0 2 -0.9 2 -1 L 24 2 c 0 -1.1 -0.9 -2 -3 -2 z M 23 23 L 1 23 L 1 1 h 22 v 22 z", getButtonColor()));
            for (int i = 0; i < paths.length; i++) {
                pathArray.add(new VectorDrawableCreator.PathData(paths[i], colour));
            }
            return pathArray;
        }

        private void setFormat(Button button, boolean enabled){



            ArrayList<VectorDrawableCreator.PathData> paths = new ArrayList<>();
            if(Name != null) {
                if (Name.equals(Control.ACTION_ADD))
                    paths = getPaths("M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z",enabled);
                else if (Name.equals( Control.ACTION_INBOX)) {
                    paths = getPaths("M19,3L5,3c-1.1,0 -2,0.9 -2,2v7c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2L21,5c0,-1.1 -0.9,-2 -2,-2zM19,9h-4c0,1.62 -1.38,3 -3,3s-3,-1.38 -3,-3L5,9L5,5h14v4zM15,16h6v3c0,1.1 -0.9,2 -2,2L5,21c-1.1,0 -2,-0.9 -2,-2v-3h6c0,1.66 1.34,3 3,3s3,-1.34 3,-3z",enabled);
                }
                else if (Name.equals( Control.ACTION_ADD_SUB)) {
                    paths = getPaths("M4,6L2,6v14c0,1.1 0.9,2 2,2h14v-2L4,20L4,6zM20,2L8,2c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2L22,4c0,-1.1 -0.9,-2 -2,-2zM19,11h-4v4h-2v-4L9,11L9,9h4L13,5h2v4h4v2z",enabled);
                }
                else if (Name.equals(Control.ACTION_EDIT))
                    paths = getPaths("M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z",enabled);
                else if (Name.equals(Control.ACTION_DELETE))
                    paths = getPaths("M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z",enabled);
                else if (Name.equals( Control.ACTION_REFRESH))
                    paths = getPaths("M17.65,6.35C16.2,4.9 14.21,4 12,4c-4.42,0 -7.99,3.58 -7.99,8s3.57,8 7.99,8c3.73,0 6.84,-2.55 7.73,-6h-2.08c-0.82,2.33 -3.04,4 -5.65,4 -3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6c1.66,0 3.14,0.69 4.22,1.78L13,11h7V4l-2.35,2.35z",enabled);
                else if (Name.equals( Control.ACTION_CAMERA)) {
                    paths = getPaths(new String[]{ "M12,12m-3.2,0a3.2,3.2 0,1 1,6.4 0a3.2,3.2 0,1 1,-6.4 0","M9,2L7.17,4L4,4c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2L22,6c0,-1.1 -0.9,-2 -2,-2h-3.17L15,2L9,2zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5z"},enabled);
                }
                else if (Name.equals( Control.ACTION_GALLERY)) {
                    paths = getPaths(new String[]{"M9,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9s9,-4.03 9,-9S13.97,3 9,3zM11.79,16.21L8,12.41V7h2v4.59l3.21,3.21L11.79,16.21z","M17.99,3.52v2.16C20.36,6.8 22,9.21 22,12c0,2.79 -1.64,5.2 -4.01,6.32v2.16C21.48,19.24 24,15.91 24,12C24,8.09 21.48,4.76 17.99,3.52z"},enabled);
                }
                else if (Name.equals( Control.ACTION_SEARCH)) {
                    paths = getPaths("M7,9H2V7h5V9zM7,12H2v2h5V12zM20.59,19l-3.83,-3.83C15.96,15.69 15.02,16 14,16c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5s5,2.24 5,5c0,1.02 -0.31,1.96 -0.83,2.75L22,17.59L20.59,19zM17,11c0,-1.65 -1.35,-3 -3,-3s-3,1.35 -3,3s1.35,3 3,3S17,12.65 17,11zM2,19h10v-2H2V19z",enabled);
                }
                else if (Name.equals( Control.ACTION_STATUS)) {
                    paths = getPaths("M14.4,6L14,4H5v17h2v-7h5.6l0.4,2h7V6z",enabled);
                }
                else if (Name.equals( Control.ACTION_VIEW)) {
                    paths = getPaths("M2,7h4v10H2V7zM7,19h10V5H7V19zM18,7h4v10h-4V7z",enabled);
                }
                else if (Name.equals( Control.ACTION_BARCODE)) {
                    paths = getPaths(new String[]{"M2,2h2v20h-2z","M5,2h2v18h-2z","M8,2h2v18h-2z","M11,2h2v18h-2z","M14,2h2v18h-2z","M17,2h2v18h-2z","M20,2h2v20h-2z"} ,enabled);
                }
                else if (Name.equals( Control.ACTION_PERCENT)) {
                    paths = getPaths(new String[]{"M7.5,11C9.43,11 11,9.43 11,7.5S9.43,4 7.5,4S4,5.57 4,7.5S5.57,11 7.5,11zM7.5,6C8.33,6 9,6.67 9,7.5S8.33,9 7.5,9S6,8.33 6,7.5S6.67,6 7.5,6z","M4.0025,18.5831l14.5875,-14.5875l1.4142,1.4142l-14.5875,14.5875z","M16.5,13c-1.93,0 -3.5,1.57 -3.5,3.5s1.57,3.5 3.5,3.5s3.5,-1.57 3.5,-3.5S18.43,13 16.5,13zM16.5,18c-0.83,0 -1.5,-0.67 -1.5,-1.5s0.67,-1.5 1.5,-1.5s1.5,0.67 1.5,1.5S17.33,18 16.5,18z"} ,enabled);
                }
                else if (Name.equals( Control.ACTION_STOCK)) {
                    paths = getPaths(new String[]{"M10,4h4v4h-4z","M4,16h4v4h-4z","M4,10h4v4h-4z","M4,4h4v4h-4z","M14,12.42l0,-2.42l-4,0l0,4l2.42,0z","M20.88,11.29l-1.17,-1.17c-0.16,-0.16 -0.42,-0.16 -0.58,0L18.25,11L20,12.75l0.88,-0.88C21.04,11.71 21.04,11.45 20.88,11.29z","M11,18.25l0,1.75l1.75,0l6.67,-6.67l-1.75,-1.75z","M16,4h4v4h-4z"} ,enabled);
                }
                else if (Name.equals( Control.ACTION_CHECKED)) {
                    paths = getPaths(new String[]{"M3,10h11v2h-11z","M3,6h11v2h-11z","M3,14h7v2h-7z","M20.59,11.93l-4.25,4.24l-2.12,-2.12l-1.41,1.41l3.53,3.54l5.66,-5.66z"} ,enabled);
                }

   /*

   <vector android:autoMirrored="true" android:height="24dp"
    android:tint="#AA2626" android:viewportHeight="24"
    android:viewportWidth="24" android:width="24dp" xmlns:android="http://schemas.android.com/apk/res/android">
    <path android:fillColor="@android:color/white" android:pathData="M3,10h11v2h-11z"/>
    <path android:fillColor="@android:color/white" android:pathData="M3,6h11v2h-11z"/>
    <path android:fillColor="@android:color/white" android:pathData="M3,14h7v2h-7z"/>
    <path android:fillColor="@android:color/white" android:pathData="M20.59,11.93l-4.25,4.24l-2.12,-2.12l-1.41,1.41l3.53,3.54l5.66,-5.66z"/>
</vector>


      */




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
                    if(Enabled) {
                        buttonClick.apply((Button) v);
                        if (getOnClick() != null) getOnClick().apply(v);
                    }
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
