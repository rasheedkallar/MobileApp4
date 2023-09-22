package com.example.myapplication;

import android.content.Context;
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

import androidx.annotation.Nullable;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupConfirmation;
import com.example.myapplication.model.PopupForm;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public  class InvCheckInActivity extends BaseActivity {

    public static class PopupFormInvCheckIn extends  PopupForm{
        private View popup_stockReceive;
        public FlexboxLayout image_layout;
        public RadioButton item_delete;
        public View.OnClickListener imageClick;


        public RadioButton item_add;
        @Override
        public void AddControls(LinearLayout container) {
            super.AddControls(container);
            PopupFormArgsInvCheckIn args = (PopupFormArgsInvCheckIn)this.getArgs();

            LayoutInflater li = LayoutInflater.from(getActivity());
            popup_stockReceive = (LinearLayout)li.inflate(R.layout.inv_check_in, null);
            getFieldsContainer().addView(popup_stockReceive);
            if(args.getValue() == null || args.getValue() == 0){
                popup_stockReceive.setVisibility(LinearLayout.GONE);
            }
            item_add = popup_stockReceive.findViewById(R.id.item_add);
            item_delete = popup_stockReceive.findViewById(R.id.item_delete);
            TableLayout item_table = popup_stockReceive.findViewById(R.id.item_table);
            RadioButton image_camera = popup_stockReceive.findViewById(R.id.image_camera);
            image_layout = container.findViewById(R.id.image_layout);
            RadioButton image_delete = container.findViewById(R.id.image_delete);
            RadioButton image_gallery = popup_stockReceive.findViewById(R.id.image_gallery);


            item_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
                    controls.add(Control.getHiddenControl("Id", null));
                    controls.add(Control.getHiddenControl("CheckInId", getArgs().getValue()));
                    controls.add(Control.getDateTimeControl("Barcode", "Barcode").setValue(new Date()));
                    controls.add(Control.getEditTextControl("Description", "Description"));
                    controls.add(Control.getEditDecimalControl("Qty", "Qty"));
                    new PopupForm().setArgs(new PopupFormArgs("Receive Item",controls,0L)).show(getRootActivity().getSupportFragmentManager(),null);
                    //new PopupFormInvCheckIn().setArgs(new PopupFormArgsInvCheckIn(controls,id,images)).show(getSupportFragmentManager(),null);
                }
            });



            imageClick = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    invCheckInSelectedImage = null;
                    image_delete.setEnabled(false);
                    for (int i = 0; i < image_layout.getChildCount(); i++) {
                        ImageView iv = (ImageView)image_layout.getChildAt(i);
                        if(view == iv) {
                            invCheckInSelectedImage = iv;
                            iv.setBackgroundColor(Color.parseColor("#225C6E"));
                            image_delete.setEnabled(true);

                        }else{
                            iv.setBackgroundColor(Color.parseColor("#8CD0E4"));
                        }
                    }
                }
            };
            for (int i = 0; i < args.Images.size(); i++) {
                try {
                    AddImage(Long.parseLong(args.Images.get(i).toString()));
                }
                catch (Exception e){
                    Toast.makeText(getContext(), "Error in loading images " +  e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            image_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupConfirmation.create("Delete Confirmation", "Are you sure you want to delete?", (unused)->{
                        Long id = (Long)invCheckInSelectedImage.getTag();
                        new DataService().deleteById(getActivity(), "RefFile", id, new DataService.DeleteByIdResponse() {
                            @Override
                            public void onSuccess(Boolean deleted) {
                                if(deleted) {
                                    args.Images.remove(id);
                                    image_layout.removeView(invCheckInSelectedImage);
                                    image_delete.setEnabled(false);
                                }
                            }
                        });
                        return true;
                    }).show(getRootActivity().getSupportFragmentManager(),null);
                }
            });
            image_camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getRootActivity().captureImage(BaseActivity.TAKE_IMAGE_FROM_CAMERA,args.getValue());
                }
            });
            image_gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getRootActivity().captureImage(BaseActivity.TAKE_IMAGE_FROM_GALLERY,args.getValue());
                }
            });
        }
        public void AddImage(long id) {
            if(!getArgs().getImages().contains(id)){
                getArgs().getImages().add(id);
            }
            ImageView imageView = GetImageView(id,image_layout,item_delete,imageClick);
            new DataService().get("refFile/" + id, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                    imageView.setImageBitmap(bmp);

                    System.out.println(image_layout.getChildCount());
                }
                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
        private ImageView GetImageView( long id, FlexboxLayout layout, RadioButton delete_button, View.OnClickListener listener){
            ImageView imageView = new ImageView(getContext());
            FlexboxLayout.LayoutParams lllP = new FlexboxLayout.LayoutParams(230, 230);
            lllP.setMargins(2,2,2,2);
            imageView.setBackgroundColor(Color.parseColor("#8CD0E4"));
            imageView.setLayoutParams(lllP);
            imageView.setTag(id);
            imageView.setOnClickListener(listener);

            //imageView.setTag(id);
            layout.addView(imageView);
            return imageView;
        }
        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            getRootActivity().checkForm = this;

        }

        @Override
        public PopupFormArgsInvCheckIn getArgs() {
            return (PopupFormArgsInvCheckIn)super.getArgs();
        }

        @Override
        public InvCheckInActivity getRootActivity() {
            return (InvCheckInActivity)super.getRootActivity();
        }
        @Override
        public void doAfterSaved(Long id) {

            PopupFormArgsInvCheckIn args = (PopupFormArgsInvCheckIn)this.getArgs();
            if(args.getValue() == null || args.getValue() ==0){
                args.setValue(id);
                getPopup().getButton(android.app.AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                popup_stockReceive.setVisibility(LinearLayout.VISIBLE);
                getRootActivity().afterInvCheckInSaved(id);
            }
            else{
                getRootActivity().afterInvCheckInSaved(id);
                dismiss();
            }
        }
        private ImageView invCheckInSelectedImage = null;
    }
    public void afterInvCheckInSaved(Long id){
        SelectedId = id;
        RefreshList();
    }
    public static class  PopupFormArgsInvCheckIn extends PopupForm.PopupFormArgs {
        public PopupFormArgsInvCheckIn(List<Control.ControlBase> controls,Long value,List<Long> images){
            super( "Stock Receive", controls, value);
            setImages(images);
        }

        private List<Long> Images;
        public List<Long> getImages() {
            return Images;
        }
        public PopupForm.PopupFormArgs setImages(List<Long> images) {
            Images = images;
            return this;
        }


    }



    public PopupFormInvCheckIn checkForm;
    @Override
    public void onCapturedImage(int requestId, Bitmap image, Long id) {
        //super.onCapturedImage(requestId, image, id);
        checkForm.AddImage(id);


        //public FlexboxLayout image_layout;
        //public RadioButton item_delete;
        //public View.OnClickListener imageClick;

    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FlexboxLayout fbl = new FlexboxLayout(this  );
        FlexboxLayout.LayoutParams fblP= new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        fbl.setLayoutParams(fblP);
        fbl.setFlexWrap(FlexWrap.WRAP);
        Container.addView(fbl);

        fromControl =  Control.getDateControl("from","From").setValue(Utility.AddDay(new Date(),-10)).setControlSize(310);
        toControl = Control.getDateControl("to","To").setValue(Utility.AddDay(new Date(),1)).setControlSize(310);

        fromControl.addView(fbl);
        toControl.addView(fbl);
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
    private List<DataService.Lookup> suppliers;
    private List<DataService.Lookup> employees;
    private Control.DateControl fromControl;
    private Control.DateControl toControl;

    private TableLayout table;

    private void  RefreshList(){
        Context context = this;

        EditButton.setEnabled(false);
        DeleteButton.setEnabled(false);
        ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
        controls.add(Control.getHiddenControl("Id",1L));


        controls.add(Control.getDateTimeControl("CheckInTime","Date"));
        controls.add(Control.getEditTextControl("RefNum","Ref#"));
        controls.add(Control.getEditTextControl("EmpName","Emp"));
        controls.add(Control.getLookupControl("SupId","Supplier",suppliers));
        controls.add(Control.getEditTextControl( "Status", "Status"));

        new DataService().get(getEntityName() + "?" + fromControl.getUrlParam() + "&" + toControl.getUrlParam(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONArray data = new JSONArray(result);
                    Utility.CreateGrid(context, table, "Id",SelectedId,controls, data, new Utility.onGridListener() {
                        @Override
                        public boolean onRowSelected(TableRow row, JSONObject data) {
                            try {
                                SelectedId = data.getLong("Id");
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
                                Long id = data.getLong("Id");
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

    private void ShowForm(ArrayList<Control.ControlBase> controls,Long id,List<Long> images){
        //new PopupFormInvCheckIn().setArgs(new PopupFormArgsInvCheckIn(controls,id,images)).show(getSupportFragmentManager(),null);
        new PopupFormInvCheckIn().setArgs(new PopupFormArgsInvCheckIn(controls,id,images)).show(getSupportFragmentManager(),null);

    }
    public static int ID_INDEX = 0;
    public static int SUPP_INDEX = 3;
    public static int EMP_INDEX = 4;

    @Override
    public void onButtonClick(String action, RadioButton button) {
        final InvCheckInActivity activity = this;
        if(action == "Add" || action == "Edit"){
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            controls.add(Control.getHiddenControl("Id", null));
            controls.add(Control.getDateTimeControl("CheckInTime", "Check In Date").setValue(new Date()));
            controls.add(Control.getEditTextControl("RefNum", "Ref Number"));
            controls.add(Control.getLookupControl( "SupId", "Supplier", suppliers));
            controls.add(Control.getLookupControl( "EmpId", "Employee", employees));
            if(action == "Add") {
                PopupLookup.create("SupplierPicker",suppliers,0L,(supplier)->{
                    controls.get(SUPP_INDEX).setValue(supplier.getId());
                    PopupLookup.create("EmployeePicker",employees,0L,(employee)->{
                        controls.get(EMP_INDEX).setValue(employee.getId());
                        ShowForm(controls,0L,new ArrayList<>());
                        return  true;
                    }).show(getSupportFragmentManager(),null);
                    return  true;
                }).show(getSupportFragmentManager(),null);
            }
            else
            {
                controls.get(ID_INDEX).setValue(SelectedId);
                new DataService().getById(activity, "InvCheckIn", SelectedId, new DataService.GetByIdResponse() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            Utility.applyValues(data,controls);
                            List<Long> images = new ArrayList<>();
                            JSONArray imageList = data.getJSONArray("Images");
                            if(imageList != null){
                                for (int i = 0; i < imageList.length(); i++) {
                                    try {
                                        images.add(Long.parseLong(imageList.get(i).toString()));
                                    }
                                    catch (Exception e){
                                        Toast.makeText(activity, "Error in loading images " +  e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            ShowForm(controls,SelectedId,images);
                        }
                        catch ( JSONException ex){
                            Toast.makeText(activity, "Error in loading data " +  ex.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
            }
        }
        else if(action == "Delete"){
            PopupConfirmation.create("Delete Confirmation", "Are you sure you want to delete?", (unused)-> {
                new DataService().deleteById(activity, "InvCheckIn", SelectedId, new DataService.DeleteByIdResponse() {
                    @Override
                    public void onSuccess(Boolean deleted) {
                        SelectedId = null;
                        RefreshList();
                    }
                });
                return true;
            }).show(getSupportFragmentManager(), null);
        }
    }
    @Override
    public String getHeaderText() {
        return "Stock Receive";
    }
}
