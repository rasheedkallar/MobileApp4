package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Activity.Item;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupInput;
import com.example.myapplication.model.PopupSearch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InspectUnitActivity extends BaseActivity {
    //protected transient EditText SearchEditText;
    protected transient LinearLayout RootLayout;
    protected transient ScrollView Scroll;

    protected static String Select = "it0=> new {it0.InvItemUnits.OrderByDescending(Id).Select(it1=> new {it1.Id, it1.ItemNumber.ToString() + \" \" + it1.Code +  it1.Fraction.ToString() as Description,it1.SalesRate,it1.SalesRate1, it0.Stock/it1.Fraction as Stock }) as InvItemUnits,it0.Id,it0.Description + \"(\" + it0.InvItemGroup.Code + \")\" as Description}";



    public static class PopupItemNewForm extends Item.PopupItemForm
    {
        public PopupItemNewForm() {
            super("New Item","InvItemUnits[]");
            getArgs().getControls().add(new Item.BarcodeDetailedControl("InvItemBarcodes","Barcode").setIsRequired(false));
        }

        @Override
        protected void doAfterSaved(Long id) {
            super.doAfterSaved(id);
            new DataService().postForList("InvItems[]", Select, "it0=> it0 = @0.InvItemUnits.Where(Id=" + id + ").Select(InvItem).FirstOrDefault()", null, array -> {
                ((InspectUnitActivity)getRootActivity()).SearchItem(array,id);
                ((InspectUnitActivity)getRootActivity()).SearchControl.setValue(null);
                return null;
            }, getRootActivity());
        }
    }




    private static Control.LookupForeignControl ItemControl;



    private void  SearchItem(JSONArray data,Long selectId)  {
        RootLayout.removeAllViews();
        if(data.length() != 0){
            try {
                JSONObject obj = (JSONObject)data.get(0);
                Long id = obj.getLong("Id");

                DataService.Lookup item = new DataService.Lookup(id,obj.getString("Description"));
                ItemControl = new Control.LookupForeignControl(".",null,"Description").setFormula("{0}.Description + \"(\" + {0}.InvItemGroup.Code + \")\"").setControlSize(ViewGroup.LayoutParams.MATCH_PARENT);
                ItemControl.setPath("InvItems[" + id + "]");
                ItemControl.setValue(item);
                ItemControl.getButtons().clear();
                ItemControl.addButton(Control.ACTION_EDIT, view -> {
                    ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
                    controls.add(Control.getEditTextControl("Description","Description").setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS).setControlSize(Control.CONTROL_SIZE_DOUBLE));
                    controls.add(Control.getLookupForeignControl("InvItemGroup", "Item Group",  "Code"));
                    ItemControl.editRecord("InvItems[" + id + "]",controls,ItemControl.getFullPath());
                    return null;
                });

                ItemControl.addView(RootLayout);
                Controls.clear();
                Item.InvItemUnitDetails dc = new Item.InvItemUnitDetails(ItemControl.getValue());
                dc.setPath("InvItems[" + id + "]");
                dc.setValue(selectId);
                Controls.add(dc);
                Controls.add(ItemControl);
                dc.refreshDetailedView(obj.getJSONArray("InvItemUnits"));
                dc.addView(RootLayout);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }



    public Control.EditTextControl SearchControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);





        SearchControl = new Control.EditTextControl("Barcode","Search");
        SearchControl.setControlSize(ViewGroup.LayoutParams.MATCH_PARENT);
        SearchControl.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        SearchControl.addButton(Control.ACTION_ADD, view -> {

            PopupItemNewForm item = new PopupItemNewForm();
            item.show(this);
            return null;
        });



        SearchControl.addButton(Control.ACTION_SEARCH, view -> {

            ArrayList<Control.ControlBase> searchControls = new ArrayList<Control.ControlBase>();
            searchControls.add(Control.getEditTextControl("Unit","Unit").setColumnWidth(100));
            searchControls.add(Control.getEditDecimalControl("Fraction","Frac").setDecimalPlaces(3).setColumnWidth(200));
            searchControls.add(Control.getEditTextControl("Description","Description"));
            PopupSearch Popup = PopupSearch.create("Item Search", searchControls, "Description");

            Popup.setListener(new PopupSearch.PopupSearchListener() {
                @Override
                public boolean onItemSelected(TableRow row, JSONObject data, DataService.Lookup lookup) {

                    if(lookup != null){
                        new DataService().postForList("InvItems[]", Select, "it0=> it0 = @0.InvItemUnits.Where(it1=> it1.Id = " + lookup.getId() + ").Select(it1=> it1.InvItem).FirstOrDefault()", null, array -> {
                            SearchItem(array,lookup.getId());
                            SearchControl.setValue(null);
                            return null;
                        }, getBaseContext());
                    }
                    return true;
                }

                @Override
                public void onTextChange(EditText editor, int keyCode) {
                    if(keyCode== 32){
                        String text = editor.getText().toString().trim();
                        if(text != null && text.length() !=0){

                            new DataService().getList("InvItem/Get?keyWords=" + text, array -> {
                                Popup.refreshDetailedView(array);
                                return null;
                            }, getBaseContext());
                        }
                    }
                }
                @Override
                public boolean onPressOk() {
                    return false;
                }
            });
            Popup.show(this.getSupportFragmentManager(),null);
            return null;
        });
        SearchControl.addView(Container);
        Scroll = new ScrollView(this);
        ScrollView.LayoutParams scP = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        Scroll.setLayoutParams(scP);
        Container.addView(Scroll);
        RootLayout = new LinearLayout(this);
        LinearLayout.LayoutParams RootLayoutP = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RootLayout.setLayoutParams(RootLayoutP);

        RootLayout.setOrientation(LinearLayout.VERTICAL);
        Scroll.addView(RootLayout);


        SearchControl.getEditTextInput().setMaxLines(2);
        SearchControl.getEditTextInput().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() >= start + after && start + after >1) {
                    int ascii = (int) s.charAt(start + after -1);
                    if(ascii == 10){
                        String barcode = null;
                        if(SearchControl.getEditTextInput().getText() != null)barcode = SearchControl.getEditTextInput().getText().toString().trim();
                        SearchControl.getEditTextInput().setText(barcode);
                        SearchControl.getEditTextInput().selectAll();
                        if(barcode.length() != 0) {

                            String sel = Select.substring(0,Select.length() -1);
                            sel = sel + ",@0.InvItemBarcodes.Where(it1=> it1.Code = \"" + barcode + "\").Select(it1=> new {InvItemUnit.Id}).FirstOrDefault() as UnitId}";

                            new DataService().postForList("InvItems[]", sel, "it0=> it0 = @0.InvItemBarcodes.Where(it1=> it1.Code = \"" + barcode + "\").Select(it1=> it1.InvItemUnit.InvItem).FirstOrDefault()", null, array -> {
                                Long unitId = null;
                                if(array.length() !=0) {
                                    try {
                                        unitId = ((JSONObject)array.get(0)).getJSONObject("UnitId").getLong("Id");
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                SearchItem(array,unitId);
                                return null;
                            }, getBaseContext());
                        }

                    }

                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        SearchControl.getEditTextInput().requestFocus();

    }







}
