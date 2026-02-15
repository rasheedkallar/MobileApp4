package com.example.myapplication;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.Data.DataRepository;
import com.example.myapplication.model.Control;
import com.example.myapplication.Data.DataService;
import com.example.myapplication.model.PopupBase;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.PopupSearch;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class BaseActivity extends AppCompatActivity  {


    private boolean  EnableScroll = false;
    public boolean getEnableScroll() {
        return EnableScroll;
    }
    public void setEnableScroll(boolean enableScroll) {
        EnableScroll = enableScroll;
    }




    public static  final int TAKE_IMAGE_FROM_CAMERA = 1;
    public static  final int TAKE_IMAGE_FROM_GALLERY = -1;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE  = 2;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    //public LinearLayout Container;
    public FlexboxLayout Container;
    public ArrayList<Control.ControlBase> Controls = new ArrayList<>();



    public static class SettingsPopupForm extends PopupForm
    {
        public SettingsPopupForm(){
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            controls.add(Control.getEditTextControl("IpAddress","Ip Address").setValue(DataRepository.CurrentSettings.IpAddress));
            controls.add(Control.getEditIntegerControl("Port","Port").setValue(DataRepository.CurrentSettings.Port));
            controls.add(Control.getEditIntegerControl("ControlWidth","Control Width").setValue(DataRepository.CurrentSettings.ControlWidth));
            controls.add(Control.getEditIntegerControl("ButtonWidth","Button Width").setValue(DataRepository.CurrentSettings.ButtonWidth));

            controls.add(Control.getEditIntegerControl("ActionButtonWidth","Action Button Width").setValue(DataRepository.CurrentSettings.IconButtonWidth));
            controls.add(Control.getEditIntegerControl("AppMode","App Mode").setValue(DataRepository.CurrentSettings.AppMode));
            controls.add(Control.getEditTextControl("Company","Company").setValue(DataRepository.CurrentSettings.Company));





            controls.add(Control.getEditTextControl("User","User").setValue(DataRepository.CurrentSettings.User));
            setArgs(new PopupFormArgs("Settings",controls,"Settings",null));
        }



        @Override
        public void doOk() {





            //SharedPreferences sharedPref = getRootActivity().getSharedPreferences("Settings",Context.MODE_PRIVATE);
            //SharedPreferences.Editor editor = sharedPref.edit();
            Control.EditTextControl ipAddress = getControl("IpAddress");
            Control.EditTextControl user = getControl("User");
            Control.EditIntegerControl port = getControl("Port");
            Control.EditIntegerControl controlWidth  = getControl("ControlWidth");
            Control.EditIntegerControl buttonWidth = getControl("ButtonWidth");
            Control.EditIntegerControl actionButtonWidth = getControl("ActionButtonWidth");
            Control.EditIntegerControl appMode = getControl("AppMode");
            Control.EditTextControl company = getControl("Company");
            DataRepository.CurrentSettings.IpAddress = ipAddress.getValue();
            DataRepository.CurrentSettings.User = user.getValue();
            DataRepository.CurrentSettings.Port = port.getValue();
            DataRepository.CurrentSettings.ControlWidth = controlWidth.getValue();
            DataRepository.CurrentSettings.ButtonWidth = buttonWidth.getValue();
            DataRepository.CurrentSettings.IconButtonWidth = actionButtonWidth.getValue();
            DataRepository.CurrentSettings.AppMode = appMode.getValue();
            DataRepository.CurrentSettings.Company = company.getValue();
            new DataRepository().saveSettings(DataRepository.CurrentSettings,getRootActivity());
            DataRepository.setCurrentConnection(null);
            if(DataRepository.CurrentSettings.Company != null && DataRepository.Companies != null && DataRepository.getCurrentCompany() == null){
                for (DataRepository.Company c : DataRepository.Companies) {
                    if (c.code != null && c.code.equals(DataRepository.CurrentSettings.Company)) {
                        DataRepository.setCurrentCompany(c,getRootActivity());
                        break;
                    }
                }
            }
            else{
                DataRepository.setCurrentCompany(null,getRootActivity());
            }
            Intent intent = new Intent(getRootActivity(), MainActivity.class); // the activity to launch if logged in
            startActivity(intent);
            dismiss();
        }
    }
    public  static MenuItem ConnectionMenu = null;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        ConnectionMenu = menu.findItem(R.id.mnu_net);
        DataRepository.refreshConnectionMenu();
        return true;
    }



    public transient  static androidx.appcompat.app.ActionBar MenuBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Controls != null){
            for (int i = 0; i < Controls.size(); i++) {
                Control.ControlBase ctrl = Controls.get(i);
                ctrl.setRootActivity(this);
            }
        }
        DataRepository.Companies = new DataRepository().getSavedCompanies(this);
        DataRepository.Connections = new DataRepository().getSavedConnections(this);
        DataRepository.CurrentSettings = new DataRepository().getSavedSettings(this);
        MenuBar = getSupportActionBar();
        BaseActivity context = this;

        MakeSureCompanies(new Function<Boolean, Void>() {
            @Override
            public Void apply(Boolean aBoolean) {
                if(aBoolean) {
                    if (DataRepository.Connections == null || DataRepository.Connections.isEmpty() ) {
                        new DataService(context).GetConnections(new Function<List<DataRepository.MobileConnection>, Void>() {
                            @Override
                            public Void apply(List<DataRepository.MobileConnection> mobileConnections) {
                                if(DataRepository.getCurrentConnection() == null){
                                    DataRepository.ChooseBestConnection(context,DataRepository.Connections,null);
                                }

                                return null;
                            }
                        });
                    }
                    else if (DataRepository.getCurrentConnection() == null) {
                        DataRepository.ChooseBestConnection(context, DataRepository.Connections, null);
                        if(DataRepository.ConnectionsRefreshDate == null || DataRepository.ConnectionsRefreshDate.before(Date.from(Instant.now().minus(5, ChronoUnit.HOURS)))){
                            new DataService(context).GetConnections(null);
                        }
                    }
                }
                return null;
            }
        });

        if(savedInstanceState != null) {
            Controls = (ArrayList<Control.ControlBase>) savedInstanceState.getSerializable("Controls");
        }


        super.onCreate(savedInstanceState);

        Intent intent;
        if (DataRepository.CurrentSettings.AppMode== 2) {
            if(this.getClass().isAssignableFrom(PriceCheckActivity.class)){
                return;
            }
            else{
                intent = new Intent(this, PriceCheckActivity.class); // the activity to launch if logged in
                startActivity(intent);
                finish();
                return;
            }
        }


        Container = new FlexboxLayout(this);
        TableLayout.LayoutParams fblP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Container.setLayoutParams(fblP);
        Container.setFlexDirection(FlexDirection.ROW);
        Container.setFlexWrap(FlexWrap.WRAP);
        if(EnableScroll){
            ScrollView sv = new ScrollView(this);
            RelativeLayout.LayoutParams svP= new  RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            svP.setLayoutDirection(LinearLayout.HORIZONTAL);
            sv.setLayoutParams(svP);
            sv.addView(Container);
            setContentView(sv);

        }else{
            setContentView(Container);
        }

        final BaseActivity activity = this;

        takePictureLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
                String newFileName = dateFormat.format(new Date());
                new DataService(this).upload(image_file, newFileName, image_entityName,  image_entity_guid,image_fileGroup, null, new Function<JSONObject, Void>() {
                    @Override
                    public Void apply(JSONObject lookup) {
                        Bitmap imageBitmap = null;
                        try {
                            imageBitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), image_uri);
                        }
                        catch (IOException e){

                        }
                        onCapturedImage( image_action ,imageBitmap,image_entityName,image_fileGroup,image_entity_guid,lookup);
                        return null;
                    }
                }, this);
            }
        });
        pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Uri uri = result.getData().getData();

                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    File file;
                    try {
                        file = File.createTempFile("Image-AbuNaser",".jpg");
                    }
                    catch (IOException e){
                        Toast.makeText(activity,e.getMessage(),Toast.LENGTH_LONG).show();
                        return;
                    }
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        Toast.makeText(activity,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
                    String newFileName = dateFormat.format(new Date());
                    new DataService(this).upload(file, newFileName, image_entityName, image_entity_guid,image_fileGroup, null, new Function<JSONObject, Void>() {
                        @Override
                        public Void apply(JSONObject lookup) {
                            onCapturedImage( image_action ,imageBitmap,image_entityName,image_fileGroup,image_entity_guid,lookup);
                            return null;
                        }
                    }, this);
                }catch (IOException e){
                }
            }
        });
        if(Controls != null){
            for (int i = 0; i < Controls.size(); i++) {
                Control.ControlBase ctrl = Controls.get(i);
                //ctrl.setRootActivity(this);
                ctrl.addView(Container);
                //ctrl.setRootActivity(this);
            }
        }
    }

    private void  MakeSureCompanies(Function<Boolean,Void> callBack){
        if(DataRepository.Companies == null || DataRepository.Companies.isEmpty()){
            new DataService(this).GetCompanies(new Function<List<DataRepository.Company>, Void>() {
                @Override
                public Void apply(List<DataRepository.Company> companies) {
                    if(companies == null || companies.isEmpty()) {
                        if (callBack != null) callBack.apply(false);
                    }
                    else MakeSureCompany(callBack);
                    return null;
                }
            });
        }
        else{
            MakeSureCompany(callBack);
            if(DataRepository.CompaniesRefreshDate == null || DataRepository.CompaniesRefreshDate.before(Date.from(Instant.now().minus(5, ChronoUnit.HOURS)))){
                new DataService(this).GetCompanies(null);
            }

            //if(DataRepository.Company != null)se
        }
    }



    private  void MakeSureCompany(Function<Boolean,Void> callBack){
        if(DataRepository.getCurrentCompany() != null) {
            DataRepository.setCurrentCompany(DataRepository.getCurrentCompany(),this);
            if(callBack != null)callBack.apply(true);
        }
        else if(DataRepository.Companies != null) {
            if(DataRepository.CurrentSettings.Company != null){
                for (DataRepository.Company c : DataRepository.Companies) {
                    if (c.code != null && c.code.equals(DataRepository.CurrentSettings.Company)) {
                        DataRepository.setCurrentCompany(c,this);
                        if(callBack != null)callBack.apply(true);
                        return;
                    }
                }
            }
            List<DataService.Lookup> lookups = new ArrayList<>();
            for (int i = 0; i < DataRepository.Companies.size(); i++) {
                DataService.Lookup lookup = new DataService.Lookup();
                lookup.setId((long)i);
                lookup.setName(DataRepository.Companies.get(i).name);
                lookups.add(lookup);
            }
            PopupLookup pl = PopupLookup.create("Select Company",lookups,0L,(company)->{
                if(company != null){
                    int index = (int)(long) company.getId();
                    DataRepository.Company com = DataRepository.Companies.get(index);
                    DataRepository.CurrentSettings.Company = com.code;
                    new DataRepository().saveSettings(DataRepository.CurrentSettings,this);
                    DataRepository.setCurrentCompany(com,this);
                    if(callBack != null) callBack.apply(true);
                }
                else{
                    DataRepository.setCurrentCompany(null,this);
                    if(callBack != null)callBack.apply(false);
                }
                return true;
            });
            var fragment = this.getSupportFragmentManager();
            try {
                pl.show(fragment,null);
            }catch (Exception e) {
                DataRepository.setCurrentCompany(null,this);
                if(callBack != null)callBack.apply(false);
            }
        }
        else{
            DataRepository.setCurrentCompany(null,this);
            if(callBack != null)callBack.apply(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        String image_uri_string = null;
        if(image_uri != null)image_uri_string = image_uri.getPath();
        savedInstanceState.putString("image_uri_string", image_uri_string);

        String image_file_string = null;
        if(image_file != null)
            image_file_string =image_file.getAbsolutePath();
        savedInstanceState.putString("image_file_string", image_file_string);


        savedInstanceState.putString("image_entity_guid",image_entity_guid);
        savedInstanceState.putInt("image_action",image_action);
        savedInstanceState.putString("image_entityName",image_entityName);
        savedInstanceState.putString("image_fileGroup",image_fileGroup);



        savedInstanceState.putSerializable("Controls",Controls);




    }




    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String image_uri_string = savedInstanceState.getString("image_uri_string");
        image_uri = null;
        if(image_uri_string != null)
            image_uri = Uri.parse(image_uri_string);


        String image_file_string = savedInstanceState.getString("image_file_string");
        image_file = null;
        if(image_file_string != null)
            image_file = new File(image_file_string);
        image_entity_guid = savedInstanceState.getString("image_entity_guid");
        image_action = savedInstanceState.getInt("image_action");
        image_entityName = savedInstanceState.getString("image_entityName");
        image_fileGroup = savedInstanceState.getString("image_fileGroup");



        Controls = (ArrayList<Control.ControlBase>) savedInstanceState.getSerializable("Controls");


    }



    private String image_entity_guid;
    private String image_entityName;
    private String image_fileGroup;
    private int image_action = 0;
    private  File image_file;
    private Uri image_uri;
    private    void  ImageCapture(){
        // Camera permission is granted, you can proceed with camera-related operations
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                image_file = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getBaseContext(),"Error in image capture." + ex.getMessage(),Toast.LENGTH_LONG).show();
            }
            if (image_file != null) {

                image_uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", image_file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    takePictureLauncher.launch(takePictureIntent);
                }catch (Exception err){
                    int a = 0;
                }



            }
        //}
    }
    private    void  ImagePick(){
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(pickImageIntent);
    }


    //private Uri photoThumbnailURI;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }



    protected String getEntityName(){
        String name = this.getClass().getName();
        int dot = name.lastIndexOf('.');
        if(dot >0)name= name.substring(dot + 1);
        if(name.endsWith("Activity"))name = name.substring(0,name.length() - 8);
        return name;
    }



    public void  captureImage(int action,String entityName,String fileGroup,String guid){
        image_action = action;
        image_entityName  = entityName;
        image_fileGroup = fileGroup;
        if(image_action < 0){

            image_entity_guid = guid;

            String permission;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission = Manifest.permission.READ_MEDIA_IMAGES;
            } else {
                permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            }

            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                ImagePick();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, GALLERY_PERMISSION_REQUEST_CODE);
            }
        }
        else{

            image_entity_guid = guid;

            int cameraPermission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
            if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                ImageCapture();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        }
    }

    public ArrayList<PopupBase> Popups = new ArrayList<PopupBase>();
    public void onCapturedImage(int action, Bitmap image, String entityName, String fileGroup, String guid, JSONObject lookup){
        for (int i = 0; i < Popups.size(); i++) {
            if(PopupForm.class.isAssignableFrom(Popups.get(i).getClass())){
                PopupForm form = (PopupForm)Popups.get(i);
                form.onCapturedImage(action,image,entityName,fileGroup,guid,lookup);
            }



        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    ImageCapture();
                }
            }
        }
        else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePick();
            }
        }
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.mnu_net && DataRepository.Connections != null && !DataRepository.Connections.isEmpty()){
            List<DataService.Lookup> lookups = new ArrayList<>();

            for (int i = 0; i < DataRepository.Connections.size(); i++) {
                DataService.Lookup lookup = new DataService.Lookup();
                lookup.setId((long)i);
                lookup.setName(DataRepository.Connections.get(i).name);
                lookups.add(lookup);
            }
            PopupLookup pl = PopupLookup.create("Select Connection",lookups,0L,(connection)->{
                if(connection != null){
                    int index = (int)(long) connection.getId();
                    DataRepository.MobileConnection mc = DataRepository.Connections.get(index);
                    mc.Valid = false;
                    DataRepository.setCurrentConnection(mc);
                    Intent intent = new Intent(getBaseContext(), MainActivity.class); // the activity to launch if logged in
                    startActivity(intent);
                    finish();

                    mc.ValidateConnection(this, new Function<Boolean, Void>() {
                        @Override
                        public Void apply(Boolean aBoolean) {

                            return null;
                        }
                    });
                }
                return true;
            });
            pl.show(this.getSupportFragmentManager(),null);
        }
        else {
            Intent intent;
            switch (item.getTitle().toString()) {
                case "Home":
                    intent = new Intent(this, MainActivity.class);
                    break;
                case "Stock Receive":
                    intent = new Intent(this, InvCheckInActivity.class);
                    break;

                case "Inspect Unit":
                    intent = new Intent(this, InspectUnitActivity.class);
                    break;
                case "Account Reconciliation":
                    intent = new Intent(this, AccountReconciliation.class);
                    break;
                case "Sales Preview":
                    intent = new Intent(this, SalesPreview.class);
                    break;
                case "Transaction Monitor":
                    intent = new Intent(this, TransactionMonitor.class);
                    break;
                case "Test":
                    intent = new Intent(this, TestActivity.class);
                    break;
                case "Settings":
                    SettingsPopupForm ps = new SettingsPopupForm();
                    ps.show(getSupportFragmentManager(), null);
                    return true;
                case "Exit":
                    exitAppCleanly();
                    return true;
                default:
                    super.onOptionsItemSelected(item);
                    return false;
            }
            startActivity(intent);
        }
        return  true;
    }

    public void exitAppCleanly() {
        // 1) Stop Lock Task (screen pinning) if active
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            try { stopLockTask(); } catch (IllegalStateException ignored) {}
        }

        // 2) Stop your timers / workers / services here
        // Example:
        // if (priceCheckerTimer != null) { priceCheckerTimer.cancel(); }
        // WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag("pricechecker");
        // stopService(new Intent(this, PriceCheckerService.class));

        // 3) Finish all activities and remove the task from Recents
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask(); // finishes this activity and removes the task
        } else {
            finishAffinity();      // API 16+: close this and all parent activities
            moveTaskToBack(true);  // push task to background (older devices)
        }

        // 4) OPTIONAL: Hard exit (use only if you must guarantee process ends)
        // new Handler(Looper.getMainLooper()).postDelayed(() -> {
        //     android.os.Process.killProcess(android.os.Process.myPid());
        //     System.exit(0);
        // }, 150);
    }
}