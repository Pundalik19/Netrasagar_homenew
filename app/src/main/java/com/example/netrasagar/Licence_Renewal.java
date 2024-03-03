package com.example.netrasagar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class Licence_Renewal extends AppCompatActivity {

    TextView txt_vesselName, txt_vrc, txt_aadhar, txt_address, dd_vessel, txt_owner,
            txt_licenceNo, txt_issueDate, txt_validity, txt_recieptDate, dd_paymentType, txt_licenceYear, txt_licenceFees;
    EditText txte_recieptAmount, txte_recieptNo, txte_remark, txte_mobileno;
    Button btn_save, btn_cancel;
    FloatingActionButton btn_recieptPic;
    ImageView recieptPic;

    private DatePickerDialog dtPickerRecieptDate;

    String vessel_type, societyName, licence_type;

    Uri reciptPicPath;

    SpinnerDialog spin_vessel, spin_payment;
    ArrayList<String> vessel_display = new ArrayList<>();
    ArrayList<String> vessel_id = new ArrayList<>();
    ArrayList<String> paymentType = new ArrayList<>();
    int selected_vessel_position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence_renewal);
        getSupportActionBar().hide();

        dd_vessel = findViewById(R.id.lr_dd_vessel);
        txt_vesselName = findViewById(R.id.lr_txt_vesselName);
        txt_vrc = findViewById(R.id.lr_txt_vrc);
        txt_owner = findViewById(R.id.lr_txt_ownerName);
        txte_mobileno = findViewById(R.id.lr_txt_mobile);
        txt_aadhar = findViewById(R.id.lr_txt_aadhar);
        txt_address = findViewById(R.id.lr_txt_address);
        txt_licenceNo = findViewById(R.id.lr_txt_licenceNumber);
        txt_issueDate = findViewById(R.id.lr_txt_issueDate);
        txt_validity = findViewById(R.id.lr_txt_validTill);
        txt_recieptDate = findViewById(R.id.lr_txt_recieptDate);
        txt_licenceYear = findViewById(R.id.lr_txt_licence_year);
        txt_licenceFees = findViewById(R.id.lr_licenceFees);
        dd_paymentType = findViewById(R.id.lr_dd_paymentType);
        txte_recieptAmount = findViewById(R.id.lr_etxt_amount);
        txte_recieptNo = findViewById(R.id.lr_etxt_recieptNo);
        txte_remark = findViewById(R.id.lr_txt_Remark);
        recieptPic = findViewById(R.id.lr_recieptPic);
        btn_recieptPic = findViewById(R.id.lr_btn_recieptPic);
        btn_save = findViewById(R.id.lr_btn_save);
        btn_cancel = findViewById(R.id.lr_btn_cancel);

        selected_vessel_position = -1;
        vessel_type = societyName = licence_type="";

        paymentType.add("Electronic");
        paymentType.add("Cheque");
        paymentType.add("DD");
        paymentType.add("Cash");
        spin_payment = new SpinnerDialog(Licence_Renewal.this, paymentType,"PAYMENT TYPE",
                in.galaxyofandroid.spinerdialog.R.style.DialogAnimations_SmileWindow, "CLOSE");// With 	Animation
        spin_payment.setCancellable(true); // for cancellable
        spin_payment.setShowKeyboard(false);// for open keyboard by default
        spin_payment.setTitleColor(Color.GRAY);
        spin_payment.setSearchIconColor(Color.GRAY);
        spin_payment.setSearchTextColor(Color.BLACK);
        spin_payment.setItemColor(Color.BLACK);
        spin_payment.setItemDividerColor(Color.GRAY);
        spin_payment.setCloseColor(Color.GRAY);
        spin_payment.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                dd_paymentType.setText(item);
            }
        });

         get_vessel_list();
        initDatePicker();
        txt_recieptDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        dd_vessel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spin_vessel.showSpinerDialog();
            }
        });

        dd_paymentType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spin_payment.showSpinerDialog();
            }
        });

        txt_recieptDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dtPickerRecieptDate.show();
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_renewal();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Licence_Renewal.this.finish();
            }
        });
        btn_recieptPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(Licence_Renewal.this)
                        .compress(60)
                        .cameraOnly()
                        .start(10);
            }
        });
        recieptPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reciptPicPath != null) {
                    Intent fulscreenIntent = new Intent(Licence_Renewal.this, FullscreenActivity.class);
                    fulscreenIntent.putExtra("path", reciptPicPath.toString())
                            .putExtra("title", "RECIEPT");
                    startActivity(fulscreenIntent);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            reciptPicPath = data.getData();
            recieptPic.setImageURI(reciptPicPath);
            recieptPic.setBackground(null);
        }
    }

    void get_vessel_list(){
        try {
            String url = GlobalVar.server_address + "/androidjson/fish_licence_vessel_list.php";
            HttpsTrustManager.allowAllSSL();
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                if (response.contains("value")) {
                                    JSONArray ja = new JSONArray(response);
                                    JSONObject jo;
                                    vessel_display.clear();
                                    vessel_id.clear();
                                    for (int i = 0; i < ja.length(); i++) {
                                        jo = ja.getJSONObject(i);
                                        if (i == 0) {
                                            int val1 = Integer.parseInt(jo.getString("value"));
                                            if (val1 == 0) {
                                                Toast.makeText(getApplicationContext(), jo.getString(
                                                        "message"),
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        } else {
                                            vessel_display.add(Comfun.json_string(jo, "AssetNm") + "\n"
                                                    + Comfun.json_string(jo, "AssetId").replace("MM", "MM "));
                                            vessel_id.add(Comfun.json_string(jo, "ID"));
                                        }
                                    }

                                    spin_vessel = new SpinnerDialog(Licence_Renewal.this, vessel_display,
                                            "SELECT VESSEL",
                                            in.galaxyofandroid.spinerdialog.R.style.DialogAnimations_SmileWindow, "CLOSE");// With 	Animation
                                    spin_vessel.setCancellable(true); // for cancellable
                                    spin_vessel.setShowKeyboard(false);// for open keyboard by default
                                    spin_vessel.setTitleColor(Color.GRAY);
                                    spin_vessel.setSearchIconColor(Color.GRAY);
                                    spin_vessel.setSearchTextColor(Color.BLACK);
                                    spin_vessel.setItemColor(Color.BLACK);
                                    spin_vessel.setItemDividerColor(Color.GRAY);
                                    spin_vessel.setCloseColor(Color.GRAY);
                                    spin_vessel.bindOnSpinerListener(new OnSpinerItemClick() {
                                        @Override
                                        public void onClick(String item, int position) {
                                            selected_vessel_position = position;
                                            dd_vessel.setText(item);
                                            get_vessel_data();
                                        }
                                    });

                                } else {
                                    Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                    " URL",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception ex) {

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("u", GlobalVar.Muser);
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);
        }catch (Exception ex){
            Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    void get_vessel_data(){
        if (selected_vessel_position >= 0 ) {
            String url = GlobalVar.server_address + "/androidjson/vesel_details.php";
            HttpsTrustManager.allowAllSSL();
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                if (response.contains("value")) {
                                    JSONArray ja = new JSONArray(response);
                                    JSONObject jo;
                                    for (int i = 0; i < ja.length(); i++) {
                                        jo = ja.getJSONObject(i);
                                        if (i == 0) {
                                            int val1 = Integer.parseInt(jo.getString("value"));
                                            if (val1 == 0) {
                                                Toast.makeText(getApplicationContext(), jo.getString(
                                                        "message"),
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        } else {
                                            txt_vesselName.setText(Comfun.json_string(jo, "AssetNm") );
                                            txt_vrc.setText(Comfun.json_string(jo, "AssetId"));
                                            txt_owner.setText(Comfun.json_string(jo, "Owner"));
                                            txte_mobileno.setText(Comfun.json_string(jo, "ContactOwner"));
                                            txt_aadhar.setText(Comfun.json_string(jo, "Aadhar"));
                                            txt_address.setText(Comfun.json_string(jo, "OwnerAddress1")+"\n"
                                                                +Comfun.json_string(jo, "OwnerAddress2")+"\n"
                                                                +Comfun.json_string(jo, "OwnerAddress3"));
                                            txt_licenceNo.setText(Comfun.json_string(jo, "fish_lic_no"));
                                            txt_issueDate.setText(Comfun.ConvertDateFormat(Comfun.json_string(jo, "fish_lic_issue_till"),
                                                     "yyyy-MM-dd", "dd/MM/yyyy"));
                                            String Tmp1 = Comfun.json_string(jo, "fish_lic_valid_till");
                                            if (Tmp1.length()==10) {
                                                txt_validity.setText(Comfun.ConvertDateFormat(Tmp1, "yyyy-MM-dd", "dd/MM/yyyy"));
                                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                                Date d = dateFormat.parse(Tmp1);
                                                Calendar licDt = Calendar.getInstance();
                                                licDt.setTime(d);
                                                if (licDt.get(Calendar.YEAR) >= Calendar.getInstance().get(Calendar.YEAR)) {
                                                    txt_licenceYear.setText(String.valueOf(licDt.get(Calendar.YEAR) + 1));
                                                }
                                                else {
                                                    txt_licenceYear.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                                                }
                                            }
                                            else {
                                                txt_validity.setText(null);
                                                txt_licenceYear.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                                            }
                                            txt_licenceFees.setText(Comfun.json_string(jo, "LicFees"));
                                            vessel_type = Comfun.json_string(jo, "Type");
                                            societyName = Comfun.json_string(jo, "SocietyName");
                                            licence_type = Comfun.json_string(jo, "VesselTypeID");;
                                        }
                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                    " URL",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception ex) {

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("u", GlobalVar.Muser);
                    map.put("vid", vessel_id.get(selected_vessel_position));
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);
        }
        else {
            Toast.makeText(this, "SELECT VESSEL FIRST",Toast.LENGTH_SHORT).show();
        }

    }

    void save_renewal(){
        if (selected_vessel_position >= 0 ) {
            if(txte_recieptAmount.getText().toString().trim().length()>0 &&
                txte_recieptNo.getText().toString().trim().length()>0 &&
                txt_recieptDate.getText().toString().trim().length()>0) {
                double lfee = Double.valueOf(txt_licenceFees.getText().toString());
                double ramnt = Double.valueOf(txte_recieptAmount.getText().toString());
                if (lfee == ramnt) {
                    final String RicieptImage;
                    if (reciptPicPath != null) {
                        RicieptImage=Comfun.encodeBitmapImage(reciptPicPath,Licence_Renewal.this);
                    } else {
                        //Toast.makeText(this, "Click Reciept Photo", Toast.LENGTH_SHORT).show();
                        RicieptImage="";
                    }
                        String url = GlobalVar.server_address + "/androidjson/fish_licence_renewal_save.php";
                        HttpsTrustManager.allowAllSSL();
                        StringRequest request = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            if (response.contains("value")) {
                                                JSONArray ja = new JSONArray(response);
                                                JSONObject jo;
                                                for (int i = 0; i < ja.length(); i++) {
                                                    jo = ja.getJSONObject(i);
                                                    if (i == 0) {
                                                        int val1 = Integer.parseInt(jo.getString(
                                                                "value"));
                                                        Toast.makeText(getApplicationContext(), jo.getString("message"),
                                                                    Toast.LENGTH_SHORT).show();
                                                        if (val1 == 1) {
                                                            Licence_Renewal.this.finish();
                                                        }
                                                        return;
                                                    }
                                                }

                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "CONNECTION ERROR : Check" +
                                                                " URL",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception ex) {

                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), error.toString(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("u", GlobalVar.Muser);
                                map.put("vesselId", vessel_id.get(selected_vessel_position));
                                map.put("vesselType", vessel_type);
                                map.put("AssetNm", txt_vesselName.getText().toString());
                                map.put("vrc", txt_vrc.getText().toString());
                                map.put("Owner", txt_owner.getText().toString());
                                map.put("Aadhar", txt_aadhar.getText().toString());
                                map.put("Mobile", txte_mobileno.getText().toString());
                                map.put("Addrres", txt_address.getText().toString());
                                map.put("LicIssDt",
                                        Comfun.ConvertDateFormat(txt_issueDate.getText().toString(), "dd/MM/yyyy", "yyyy-MM-dd"));
                                map.put("LicToDt", txt_licenceYear.getText().toString() + "-12-31");
                                map.put("LicNo", txt_licenceNo.getText().toString());
                                map.put("SocietyNm", societyName);
                                map.put("LicenceType", licence_type);
                                map.put("LicYear", txt_licenceYear.getText().toString());
                                map.put("LicFees", txt_licenceFees.getText().toString());
                                map.put("AmntPaid", txte_recieptAmount.getText().toString());
                                map.put("PmntType", dd_paymentType.getText().toString());
                                map.put("RecNo", txte_recieptNo.getText().toString());
                                map.put("RecDt",
                                        Comfun.ConvertDateFormat(txt_recieptDate.getText().toString(), "dd/MM/yyyy", "yyyy-MM-dd"));
                                map.put("remark", txte_remark.getText().toString());
                                map.put("RecieptImage", RicieptImage);

                                return map;
                            }
                        };
                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        queue.add(request);


                }
                else {
                    Toast.makeText(this, "Reciept amount should be equal to Fees Amount", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this,"Fill reciept details",Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "SELECT VESSEL FIRST",Toast.LENGTH_SHORT).show();
        }

    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener date_Listner_RecieptDate =
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month + 1;
                        txt_recieptDate.setText(String.format("%02d", day) + "/" + String.format("%02d", month) + "/" + String.format("%04d", year));
                    }
                };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        dtPickerRecieptDate = new DatePickerDialog(this, style, date_Listner_RecieptDate, year, month, day);
        dtPickerRecieptDate.getDatePicker().setMaxDate(System.currentTimeMillis());

    }

}