package com.example.myapplication.Activity;


import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myapplication.BaseActivity;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupBase;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupHtml;
import com.example.myapplication.model.PopupInput;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

import cz.msebera.android.httpclient.Header;

public class Item {

    public static class PopupItemForm extends PopupEntityForm {

        public PopupItemForm(String header, String path) {
            super(header, path, null);
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            controls.add(Control.getEditTextControl("InvItem.Description","Description").setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS).setControlSize(Control.CONTROL_SIZE_DOUBLE));
            controls.add(Control.getEditTextPickerControl("Code", "Unit", getUnits(), null).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS).setValue("PCS"));
            controls.add(Control.getEditDecimalControl("Fraction", "Fraction").setDecimalPlaces(3).setValue(1.0));
            controls.add(Control.getEditDecimalControl("SalesRate","Rate1").setColumnWidth(200).setIsRequired(false));
            controls.add(Control.getEditDecimalControl("SalesRate1","Rate2").setColumnWidth(200).setIsRequired(false));
            controls.add(Control.getLookupForeignControl("InvItem.InvItemGroup", "Item Group",  "Code").setEnabled(false));
            controls.add(Control.getHiddenControl("InvItem.ItemTaxId", 1));

            getArgs().setControls(controls);
        }
    }


    public static class PopupEntityForm extends PopupForm
    {
        public PopupEntityForm(String header,String path,ArrayList<Control.ControlBase> controls){
            PopupFormArgs ppf = new PopupFormArgs(header,controls,path,null);
            setArgs(ppf);
        }


        public void show(BaseActivity activity){
            addNewRecord(activity,getArgs().getPath(),getArgs().getControls(),null);
        }



        public void addNewRecord(BaseActivity activity, String path, ArrayList<Control.ControlBase> controls, String actionPath){
            for (int i = 0; i < controls.size(); i++) {
                controls.get(i).setPath(path);
            }

            ArrayList<Control.LookupControlBase> popupInputs = (ArrayList<Control.LookupControlBase>)controls.stream()
                    .filter(o -> o instanceof Control.LookupControlBase)
                    .map(o -> (Control.LookupControlBase)o)
                    .filter(o -> o.getPopupIndex() >= 0 && o.getValue() == null)
                    .sorted(Comparator.comparing(s -> s.getPopupIndex()))
                    .collect(Collectors.toList());
            ShowAdd(activity,popupInputs,controls,path,actionPath);
        }
        private void ShowAdd(BaseActivity activity,ArrayList<Control.LookupControlBase> popupInputs, ArrayList<Control.ControlBase> controls, String path, String actionPath){

            if(popupInputs == null || popupInputs.size() == 0){
                //PopupForm pf =   new PopupForm();
                //pf.setArgs(new PopupForm.PopupFormArgs( "Item Add",controls,path,0L).setActionPath(actionPath));

            /*
            pf.setSaveListener(new Function<Long, Boolean>() {
                @Override
                public Boolean apply(Long aLong) {
                    new DataService().postForList("InvItems[]", Select, "it0=> it0 = @0.InvItemUnits.Where(it1=> it1.Id = " + aLong + ").Select(it1=> it1.InvItem).FirstOrDefault()", null, array -> {
                        SearchItem(array);
                        return null;
                    }, getBaseContext());
                    return true;
                }
            });

             */
                show( activity.getSupportFragmentManager(),null);

            }else{
                popupInputs.get(0).onPopupList(activity,new Function<DataService.Lookup, Void>() {
                    @Override
                    public Void apply(DataService.Lookup lookup) {
                        popupInputs.remove(popupInputs.get(0));
                        ShowAdd(activity,popupInputs,controls,path,actionPath);
                        return null;
                    }
                });
            }
        }

    }



    public static ArrayList<String> getUnits(){
        ArrayList<String> units = new ArrayList<>();
        units.add("PCS");
        units.add("BAG");
        units.add("BOX");
        units.add("CMR");
        units.add("CTN");
        units.add("KG");
        units.add("MTR");
        units.add("PKT");
        units.add("OFR");
        return units;
    }






    public  static  class PopupItemBarcode extends PopupBase<PopupItemBarcode, PopupItemBarcode.PopupItemBarcodeArgs>
    {


        public static class  PopupItemBarcodeArgs extends PopupBase.PopupArgs<PopupItemBarcodeArgs> {
            public PopupItemBarcodeArgs(String header,String path){
                super(header);
                setCancelButton("Close");
                setOkButton(null);
                setPath(path);
            }
            private String Path;
            public String getPath() {
                return Path;
            }

            public void setPath(String path) {
                Path = path;
            }
        }

        //
        @Override
        public void AddControls(LinearLayout container) {
            //Long invCheckInLineId = getArgs().getInvCheckInLineId();
            String s ="it0 => new{it0.Id,it0.Description,it0.InvItemUnits.OrderBy(Fraction).Select(it1 => new {it1.Id,it1.ItemNumber + \" \" + it1.Code + \" \" + it1.Fraction as Unit,it1.InvItemBarcodes.OrderByDescending(Id).Select(it2 => new{it2.Id,it2.Code}) as InvItemBarcodes}) as InvItemUnits}";
            new DataService().postForSelect(getArgs().getPath(), s, jsonObject -> {
                try{
                    Title.setText(jsonObject.getString("Description"));
                    JSONArray units = (JSONArray)jsonObject.get("InvItemUnits");
                    for (int i = 0; i < units.length(); i++) {
                        JSONObject unit = (JSONObject)units.get(i);
                        BarcodeDetailedControl dc = new BarcodeDetailedControl("InvItemUnits[" + unit.get("Id") + "].InvItemBarcodes" ,unit.getString("Unit"));
                        dc.readValueJSONObject(unit,"InvItemBarcodes");
                        dc.addView(container);
                    }
                }catch (JSONException e){

                    System.out.println(e.getMessage());
                }
                return null;
            }, getContext());
        }
    }



    public static class BarcodeDetailedControl extends  Control.DetailedControl{
        public BarcodeDetailedControl(String name, String caption) {
            super(name, caption);
            getButtons().remove(getButton(Control.ACTION_REFRESH));
            getButtons().remove(getButton(Control.ACTION_EDIT));
        }
        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> barcodeControls = new ArrayList<Control.ControlBase>();
            if(action == Control.ACTION_REFRESH) {
                barcodeControls.add(Control.getEditTextControl("Code", "Barcode"));
            }
            return barcodeControls;
        }

        private void AddBarcode(String barcode){
            JSONObject json = new JSONObject();
            try{
                json.put("Code",barcode.trim());
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
            new DataService().postForSave(getFullPathNew(), json, aLong -> {
                readValueObject(aLong);
                refreshGrid(Table);
                return null;
            }, s -> {
                PopupHtml.create("Error",s).show(getRootActivity().getSupportFragmentManager(),null);
                return null;
            });
        }

        @Override
        public void onButtonClick(Control.ActionButton action) {
            if(action.getName().equals(Control.ACTION_ADD)){
                PopupInput pi =  PopupInput.create("Barcode", s -> {
                    AddBarcode(s);
                    return true;
                });
                pi.setInputListener((integer, s) -> {
                    if(integer == 10){
                        AddBarcode(s);
                        return true;
                    }
                    else{
                        return false;
                    }
                });
                pi.show(getRootActivity().getSupportFragmentManager(),null);
            }
            else {
                super.onButtonClick(action);
            }
        }
    }



    public static class InvItemUnitDetails extends Control.DetailedControl
    {



        public InvItemUnitDetails(DataService.Lookup item) {
            super("InvItemUnits", "Units");
            getButtons().add(3,new Control.ActionButton(Control.ACTION_BARCODE));
            getButtons().add(4,new Control.ActionButton(Control.ACTION_PRINT));
            Item =item;
        }

        private DataService.Lookup Item;

        @Override
        public void onButtonClick(Control.ActionButton action) {
            if(action.getName() == Control.ACTION_BARCODE){
                Item.PopupItemBarcode pib = new Item.PopupItemBarcode();
                pib.setArgs(new Item.PopupItemBarcode.PopupItemBarcodeArgs("Barcode",getPath()).setEnableScroll(true));
                pib.show(((BaseActivity)action.getButton().getContext()).getSupportFragmentManager(),null);
            }
            else if(action.getName().equals(Control.ACTION_PRINT)){

                new DataService().getString("InvItem/GetShelfEdgePrint?unitId=" + this.getValue(), s -> {
                    PopupHtml.create("Message",s).show(getRootActivity().getSupportFragmentManager(),null);
                    return null;
                }, action.getButton().getContext());
            }
            else {
                super.onButtonClick(action);
            }
        }

        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action.equals(Control.ACTION_FILTER))return controls;
            if(action.equals(Control.ACTION_REFRESH)){
                controls.add(Control.getEditTextControl("Description","Description").setFormula("{0}.ItemNumber + \" \" + {0}.Code +  {0}.Fraction").setColumnWidth(400));
            }
            else{
                Control.LookupForeignControl item =Control.getLookupForeignControl("InvItem","Unit","Description");
                item.getButtons().clear();
                if(action.equals(Control.ACTION_ADD))item.setValue(Item);
                controls.add(item);
            }
            if(!action.equals(Control.ACTION_REFRESH)){
                controls.add(Control.getEditTextPickerControl("Code", "Unit", getUnits(), null).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS));
                controls.add(Control.getEditDecimalControl("Fraction", "Fraction").setDecimalPlaces(3));
            }
            controls.add(Control.getEditDecimalControl("SalesRate","Rate1").setColumnWidth(150).setIsRequired(false));
            controls.add(Control.getEditDecimalControl("SalesRate1","Rate2").setColumnWidth(150).setIsRequired(false));
            if(action.equals(Control.ACTION_REFRESH)) {
                controls.add(Control.getEditDecimalControl("Cost","Cost").setFormula("{0}.InvItem.PurchaseRate * {0}.Fraction").setColumnWidth(150));
                controls.add(Control.getEditDecimalControl("Stock","Stock").setDecimalPlaces(3).setFormula("{0}.InvItem.Stock/{0}.Fraction").setColumnWidth(200));
            }
            else{
                controls.add(new Item.BarcodeDetailedControl("InvItemBarcodes","Barcode").setIsRequired(false));
            }

            return controls;
        }


    }





}
