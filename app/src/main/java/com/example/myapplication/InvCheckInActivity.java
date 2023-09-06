package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupConfirmation;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupHtml;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.Utility;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class InvCheckInActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FlexboxLayout fbl = new FlexboxLayout(this  );
        FlexboxLayout.LayoutParams fblP= new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        fbl.setLayoutParams(fblP);
        fbl.setFlexWrap(FlexWrap.WRAP);
        Container.addView(fbl);

        //fromControl = new Utility.Control(Utility.ControlType.Date,"from","From Date", Utility.AddDay(new Date(),-10),null,false);
        //toControl  = new Utility.Control(Utility.ControlType.Date,"to","To Date", Utility.AddDay(new Date(),1),null,false);


        //fbl.addView(fromControl.GenerateView(this,310));
        //fbl.addView(toControl.GenerateView(this,310));

        fromControl = Control.getDateControl(this,"from","From").setValue(Utility.AddDay(new Date(),-10)).setControlSize(310);
        toControl = Control.getDateControl(this,"to","To").setValue(Utility.AddDay(new Date(),1)).setControlSize(310);

        fbl.addView(fromControl.getFillView());
        fbl.addView(toControl.getFillView());


        Button btn = new Button(this);
        FlexboxLayout.LayoutParams btlP= new FlexboxLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100);
        btlP.setMargins(0,75,0,0);

        btn.setLayoutParams(btlP);
        btn.setText("Refresh");

        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RefreshList();
            }
        });
        fbl.addView(btn);

        ScrollView sv = new ScrollView(this);
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);

        Container.addView(sv);


        table = new TableLayout(this);
        TableLayout.LayoutParams tableP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        table.setLayoutParams(tableP);

        sv.addView(table);

        final Context context = this;

        System.out.println("hi");

        AddButton.setEnabled(false);
        EditButton.setEnabled(false);
        DeleteButton.setEnabled(false);

        new DataService().getLookups(this,  new String[] {"Supplier", "Employee"}, new DataService.LookupsResponse() {
            @Override
            public void onSuccess(List<DataService.Lookup>[] lookups) {
                suppliers = lookups[0];
                employees = lookups[1];
                AddButton.setEnabled(true);
                RefreshList();
            }
        });
    }
    List<DataService.Lookup> suppliers;
    List<DataService.Lookup> employees;

    Control.DateControl fromControl;
    Control.DateControl toControl;

    TableLayout table;

    private void  RefreshList(){
        Context context = this;

        EditButton.setEnabled(false);
        DeleteButton.setEnabled(false);
        ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
        controls.add(Control.getDateTimeControl(context,"header_date","Date").setValue(new Date()));
        controls.add(Control.getEditTextControl(context,"header_number","Ref#"));
        controls.add(Control.getEditTextControl(context,"header_employee_name","Emp"));
        controls.add(Control.getLookupControl(context,"header_supplier","Supplier",suppliers));
        controls.add(Control.getEditTextControl(context, "header_status", "Status"));



        new DataService().get("InvCheckIn?" + fromControl.getUrlParam() + "&" + toControl.getUrlParam(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONArray data = new JSONArray(result);
                    Utility.CreateGrid(context, table, controls, data, new Utility.onGridListener() {
                        @Override
                        public boolean onRowSelected(TableRow row, JSONObject data) {
                            try {
                                SelectedId = data.getLong("header_id");
                                EditButton.setEnabled(true);
                                DeleteButton.setEnabled(true);
                                return true;
                            }
                            catch (JSONException ex){
                                SelectedId = 0L;
                                EditButton.setEnabled(false);
                                DeleteButton.setEnabled(false);
                                return false;
                            }
                        }

                        @Override
                        public boolean isSelected(TableRow row, JSONObject data)   {
                            System.out.println(data.toString());

                            if(SelectedId == null || SelectedId == 0)return false;

                            try {
                                Long id = data.getLong("header_id");
                                if(id.equals(SelectedId)){
                                    EditButton.setEnabled(true);
                                    DeleteButton.setEnabled(true);
                                    return true;
                                }
                            }
                            catch (JSONException ex){
                                Toast.makeText(InvCheckInActivity.this, "GetListData Failed," + ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            return false;
                        }
                    });
                }
                catch (JSONException | ParseException e)
                {
                    Toast.makeText(InvCheckInActivity.this, "GetListData Failed," + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                String result = new String(responseBody);
                Toast.makeText(InvCheckInActivity.this, "GetListData Failed," + result, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Long SelectedId= 0L;
    private LinearLayout popup_stockReceive;
    private void AddImage(long id,Bitmap image,FlexboxLayout layout,RadioButton delete_button){
        ImageView imageView = GetImageView(id,layout,delete_button);
        imageView.setImageBitmap(image);
    }
    private ImageView GetImageView(long id,FlexboxLayout layout,RadioButton delete_button){
        ImageView imageView = new ImageView(this);
        FlexboxLayout.LayoutParams lllP = new FlexboxLayout.LayoutParams(230, 230);
        lllP.setMargins(2,2,2,2);
        imageView.setBackgroundColor(Color.parseColor("#8CD0E4"));
        imageView.setLayoutParams(lllP);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invCheckInSelectedImage = null;
                delete_button.setEnabled(false);
                for (int i = 0; i < layout.getChildCount(); i++) {
                    ImageView iv = (ImageView)layout.getChildAt(i);
                    if(view == iv) {
                        invCheckInSelectedImage = iv;
                        iv.setBackgroundColor(Color.parseColor("#225C6E"));
                        delete_button.setEnabled(true);

                    }else{
                        iv.setBackgroundColor(Color.parseColor("#8CD0E4"));
                    }
                }
            }
        });

        imageView.setTag(id);
        layout.addView(imageView);
        return imageView;
    }
    private ImageView invCheckInSelectedImage = null;
    private void AddImage(long id,FlexboxLayout layout,RadioButton delete_button) throws MalformedURLException,IOException {
        ImageView imageView = GetImageView(id,layout,delete_button);



        new DataService().get("refFile/" + id, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                Bitmap bmp = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                imageView.setImageBitmap(bmp);

                System.out.println(layout.getChildCount());
            }
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }




    private FlexboxLayout image_layout;
    private void  configPopup_stockReceive(FlexboxLayout container,Control.ControlBase id){
        LayoutInflater li = LayoutInflater.from(this );
        popup_stockReceive = (LinearLayout)li.inflate(R.layout.inv_check_in, null);
        container.addView(popup_stockReceive);
        image_layout = container.findViewById(R.id.image_layout);
        image_delete = container.findViewById(R.id.image_delete);
        final Context context = this;
        image_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new PopupConfirmation(context, "Are you sure you want to delete?", new PopupConfirmation.onFormPopupConfirmListener() {
                    @Override
                    public void onConfirm() {

                        if(invCheckInSelectedImage != null){
                            new DataService().deleteById(context, "refFile", (Long)invCheckInSelectedImage.getTag(), new DataService.DeleteByIdResponse() {
                                @Override
                                public void onSuccess(Boolean deleted) {
                                    image_layout.removeView(invCheckInSelectedImage);
                                    image_delete.setEnabled(false);
                                }
                            });
                        }
                    }
                });

            }
        });


        item_add = container.findViewById(R.id.item_add);
        item_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                ArrayList<Control.ControlBase> newControls = new ArrayList<Control.ControlBase>();

                newControls.add(new Control.HiddenControl(context,"item_id",null));
                newControls.add(new Control.EditTextControl(context,"item_barcode","Barcode"));


                PopupForm from = new PopupForm(context, "Stock Receive New", newControls, new PopupForm.onFormPopupFormListener() {
                    @Override
                    public boolean onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, String result) {
                        Long selid = Long.valueOf(result);
                        RefreshList();
                        SelectedId = selid;
                        if(id.getValue() == null || id.getValue().equals(0L)){
                            id.setValue(selid);
                            this.getPopup().AlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            popup_stockReceive.setVisibility(LinearLayout.VISIBLE);
                            return false;
                        }
                        else{
                            return true;
                        }
                    }

                    @Override
                    public boolean onControlAdded(FlexboxLayout container) {
                        configPopup_stockReceive(container,id);
                        popup_stockReceive.setVisibility(LinearLayout.GONE);
                        return super.onControlAdded(container);
                    }

                    @Override
                    public String getUrl() {
                        return "InvCheckIn";
                    }
                });




            }
        });



        item_edit = container.findViewById(R.id.item_edit);
        item_delete = container.findViewById(R.id.item_delete);

        TableLayout item_table = container.findViewById(R.id.item_table);
        RadioButton image_camera = container.findViewById(R.id.image_camera);
        image_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage("InvCheckIn",Long.parseLong(id.getValue().toString()),new onGetImage() {
                    @Override
                    public void getImage(Bitmap image, long id) {
                        AddImage(id,image,image_layout,item_delete)  ;
                    }
                });
            }
        });
        RadioButton image_gallery = container.findViewById(R.id.image_gallery);
        image_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage("InvCheckIn",Long.parseLong(id.getValue().toString()),new onGetImage() {
                    @Override
                    public void getImage(Bitmap image, long id) {
                        AddImage(id,image,image_layout,item_delete) ;
                    }
                });
            }
        });

    }

    private RadioButton image_delete ;
    private RadioButton item_add  ;
    private RadioButton item_edit ;
    private RadioButton item_delete ;
    @Override
    public void onButtonClick(String action, RadioButton button) {
        final Context context = this;
        if(action == "Add"){
            new PopupLookup(context, "Supplier", suppliers, new PopupLookup.onFormPopupLookupListener() {
                @Override
                public boolean onPick(DataService.Lookup lookup) {
                    final DataService.Lookup suppler = lookup;
                    new PopupLookup(context, "Employee", employees, new PopupLookup.onFormPopupLookupListener() {
                        @Override
                        public boolean onPick(DataService.Lookup lookup) {
                            final DataService.Lookup employee = lookup;
                            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
                            Control.HiddenControl id = Control.getHiddenControl(context, "header_id", null);
                            controls.add(id);
                            controls.add(Control.getDateTimeControl(context,"header_date", "Check In Date").setValue(new Date()));
                            controls.add(Control.getEditTextControl(context, "header_number", "Ref Number"));
                            controls.add(Control.getLookupControl(context, "header_supplier", "Supplier", suppliers).setValue(suppler.Id));
                            controls.add(Control.getLookupControl(context, "header_employee", "Employee", employees).setValue(employee.Id));

                            PopupForm from = new PopupForm(context, "Stock Receive New", controls, new PopupForm.onFormPopupFormListener() {
                                @Override
                                public boolean onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, String result) {
                                    Long selid = Long.valueOf(result);
                                    RefreshList();
                                    SelectedId = selid;
                                    if(id.getValue() == null || id.getValue().equals(0L)){
                                        id.setValue(selid);
                                        this.getPopup().AlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                        popup_stockReceive.setVisibility(LinearLayout.VISIBLE);
                                        return false;
                                    }
                                    else{
                                        return true;
                                    }
                                }

                                @Override
                                public boolean onControlAdded(FlexboxLayout container) {
                                    configPopup_stockReceive(container,id);
                                    popup_stockReceive.setVisibility(LinearLayout.GONE);
                                    return super.onControlAdded(container);
                                }

                                @Override
                                public String getUrl() {
                                    return "InvCheckIn";
                                }
                            });
                            return false;
                        }
                    });
                    return true;
                }
            });
        }
        else if(action == "Edit"){
            new DataService().getById(context, "InvCheckIn", SelectedId, new DataService.GetByIdResponse() {
                @Override
                public void onSuccess(JSONObject data) {
                    ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
                    Control.HiddenControl id = Control.getHiddenControl(context, "header_id", null).readValue(data);
                    controls.add(id);
                    controls.add(Control.getDateTimeControl(context,"header_date", "Check In Date").readValue(data));
                    controls.add(Control.getEditTextControl(context, "header_number", "Ref Number").readValue(data));
                    controls.add(Control.getLookupControl(context, "header_supplier", "Supplier", suppliers).readValue(data));
                    controls.add(Control.getLookupControl(context, "header_employee", "Employee", employees).readValue(data));
                    JSONArray imageList;
                    try {
                        //Utility.applyValues(data,controls);
                        imageList = data.getJSONArray("header_images");
                    }
                    catch ( JSONException ex){
                        Toast.makeText(context, "Error in loading data " +  ex.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new PopupForm(context, "Stock Receive Edit", controls, new PopupForm.onFormPopupFormListener() {
                        @Override
                        public boolean onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, String result) {
                            RefreshList();
                            return true;
                        }

                        @Override
                        public boolean onControlAdded(FlexboxLayout container)  {
                            configPopup_stockReceive(container,id);

                            for (int i = 0; i < imageList.length(); i++) {
                                try {
                                    AddImage(Long.parseLong(imageList.get(i).toString()),image_layout,image_delete);
                                }
                                catch (Exception e){
                                    Toast.makeText(context, "Error in loading images " +  e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            return super.onControlAdded(container);
                        }

                        @Override
                        public String getUrl() {
                            return "InvCheckIn";
                        }
                    });

                }
            });
        }
        else if(action == "Delete"){
            new PopupConfirmation(this, "Are you sure you want to delete?", new PopupConfirmation.onFormPopupConfirmListener() {
                @Override
                public void onConfirm() {
                    new DataService().deleteById(context, "InvCheckIn", SelectedId, new DataService.DeleteByIdResponse() {
                        @Override
                        public void onSuccess(Boolean deleted) {
                            RefreshList();
                        }
                    });
                }
            });
        }
    }
    @Override
    public String getHeaderText() {
        return "Stock Receive";
    }
}
