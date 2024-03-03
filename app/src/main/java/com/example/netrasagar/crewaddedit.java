package com.example.netrasagar;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class crewaddedit extends AppCompatActivity {

    String state_id;
    public static final int CrewPic_REQ_CODE = 10;
    public static final int Aadharpic_REQ_CODE = 11;
    public static final int AddressPicREQ_CODE = 12;
    public static EditText txt_name, txt_add1, txt_add2, txt_add3, txt_aadhar;
    EditText txt_healthcardno, txt_height, txt_mobileno, txt_identificationMark;
    public static TextView txt_dob, dd_state;
    TextView dd_vessel, txt_age, dd_jetty, txt_healthcarddate, txt_joiningDate, txt_valDt;
    Button btn_saveEdit, btn_cancel, btn_crewValUpdate, btn_scanAadhar;
    ImageView crewPic, idPic, addressPic;
    FloatingActionButton btn_crewPic, btn_aadhar, btn_address;
    String jettyCode, vesselID, crewID;
    SpinnerDialog spin_jetty, spin_vessel, spin_state;
    public static ArrayList<String> statelist = new ArrayList<>();
    ArrayList<String> jetty_list = new ArrayList<>();
    ArrayList<String> vessel_list = new ArrayList<>();
    ArrayList<vesselDetails> vessels = new ArrayList<>();
    ArrayList<jettyDetails> jettys = new ArrayList<>();
    ArrayList<String> stateids = new ArrayList<>();
    Uri crewPicPath, idPicPath, addressPicPath;
    private DatePickerDialog dtPickerDOB;
    private DatePickerDialog dtPickerHlthCard;
    private DatePickerDialog dtPickerJoining;

    SimpleDateFormat ymdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat dmyt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    SimpleDateFormat dmy = new SimpleDateFormat("dd/MM/yyyy");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crewaddedit);
        getSupportActionBar().hide();
        Intent intent = this.getIntent();

        jettyCode = vesselID = crewID = "";

        txt_name = findViewById(R.id.txt_nameC);
        txt_add1 = findViewById(R.id.txt_address1);
        txt_add2 = findViewById(R.id.txt_address2);
        txt_add3 = findViewById(R.id.txt_address3);
        dd_jetty = findViewById(R.id.dd_jetty);
        dd_vessel = findViewById(R.id.dd_vessel);
        dd_state = findViewById(R.id.dd_state);
        txt_dob = findViewById(R.id.txt_dob);
        txt_age = findViewById(R.id.txt_age);
        txt_height = findViewById(R.id.txt_height);
        txt_mobileno = findViewById(R.id.txt_mobileno);
        txt_aadhar = findViewById(R.id.txt_aadhar);
        txt_joiningDate = findViewById(R.id.txt_joiningDate);
        txt_healthcardno = findViewById(R.id.txt_healthcardno);
        txt_healthcarddate = findViewById(R.id.txt_healthCardDate);
        txt_identificationMark = findViewById(R.id.txt_identificationMark);
        crewPic = findViewById(R.id.crewPic);
        idPic = findViewById(R.id.CA_id_proof);
        addressPic = findViewById(R.id.CA_address_proof);
        txt_valDt = findViewById(R.id.txt_Crewvaldt);
        btn_crewValUpdate = findViewById(R.id.btn_crewValUpdt);
        btn_scanAadhar = findViewById(R.id.cae_scanaadhar);

        btn_cancel = findViewById(R.id.btn_cancel);
        btn_saveEdit = findViewById(R.id.btn_saveEdit);
        btn_aadhar = findViewById(R.id.CA_btn_id_proof);
        btn_address = findViewById(R.id.CA_btn_address_proof);
        btn_crewPic = findViewById(R.id.btn_crewPic);

         initDatePicker();

        get_stateList();

        String aadharNo = (intent.getStringExtra("name"));
        if (aadharNo.equals("NEW")) {
            state_id="";
            get_jetty_list();
            enable_fields();
            btn_saveEdit.setText("SAVE");
            btn_saveEdit.setEnabled(GlobalVar.crew_add);
            btn_scanAadhar.setVisibility(View.VISIBLE);
        } else {
            aadharNo = aadharNo.substring(0, aadharNo.indexOf("-"));
            state_id = (intent.getStringExtra("state_id"));
            txt_aadhar.setText(aadharNo);
            show_crew_details(aadharNo,state_id);
            btn_saveEdit.setText("EDIT");
            btn_saveEdit.setEnabled(GlobalVar.crew_edit);
        }


        crewPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (crewPicPath != null) {
                    Intent fulscreenIntent = new Intent(crewaddedit.this, FullscreenActivity.class);
                    fulscreenIntent.putExtra("path", crewPicPath.toString())
                            .putExtra("title", txt_name.getText().toString());
                    startActivity(fulscreenIntent);
                }
            }
        });
        idPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (idPicPath != null) {
                    Intent fulscreenIntent = new Intent(crewaddedit.this, FullscreenActivity.class);
                    fulscreenIntent.putExtra("path", idPicPath.toString())
                            .putExtra("title", txt_name.getText().toString());
                    startActivity(fulscreenIntent);
                }
            }
        });
        addressPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addressPicPath != null) {
                    Intent fulscreenIntent = new Intent(crewaddedit.this, FullscreenActivity.class);
                    fulscreenIntent.putExtra("path", addressPicPath.toString())
                            .putExtra("title", txt_name.getText().toString());
                    startActivity(fulscreenIntent);
                }
            }
        });

        dd_jetty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                get_vesselList();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btn_crewPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(crewaddedit.this)
                            .compress(20)
                            .crop(214, 222)
                            .maxResultSize(214, 222)
                            .start(CrewPic_REQ_CODE);
            }
        });

        btn_aadhar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(crewaddedit.this)
                        .crop(9f,16f)
                        .compress(500)
                        .start(Aadharpic_REQ_CODE);
            }
        });

        btn_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(crewaddedit.this)
                        .crop(9f,16f)
                        .compress(500)
                        .start(AddressPicREQ_CODE);
            }
        });

        btn_crewValUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_crew_validity();
            }
        });

        btn_scanAadhar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),scannerView.class));
            }
        });

        txt_dob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    Calendar cal_dob = Calendar.getInstance();
                    cal_dob.setTime(dmy.parse(txt_dob.getText().toString()));
                    Calendar today = Calendar.getInstance();
                    int yrs = today.get(Calendar.YEAR) - cal_dob.get(Calendar.YEAR);
                    if (cal_dob.get(Calendar.MONTH) > today.get(Calendar.MONTH))
                        yrs -= 1;

                    txt_age.setText(String.format("%02d", yrs));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CrewPic_REQ_CODE) {
            crewPicPath = data.getData();
            crewPic.setImageURI(crewPicPath);
            crewPic.setBackground(null);
        }
        else if (requestCode == Aadharpic_REQ_CODE) {
            idPicPath = data.getData();
            idPic.setImageURI(idPicPath);
            idPic.setBackground(null);
        }
        else if (requestCode == AddressPicREQ_CODE) {
            addressPicPath = data.getData();
            addressPic.setImageURI(addressPicPath);
            addressPic.setBackground(null);
        }

    }

    public void ca_btn_cancel_clik(View v) {
        if (btn_saveEdit.getText().equals("UPDATE")) {
            disable_fields();
            btn_saveEdit.setText("EDIT");
        } else {
            this.finish();
        }
    }

    public void ca_btn_saveEdit_clik(View v) {
        if (btn_saveEdit.getText().equals("EDIT")) {
            enable_fields();
            btn_saveEdit.setText("UPDATE");
        } else if (btn_saveEdit.getText().equals("UPDATE")) {
            btn_saveEdit.setEnabled(false);
            btn_saveEdit.setText("EDIT");
            update_crew_Data();
            disable_fields();
        } else if (btn_saveEdit.getText().equals("SAVE")) {
            add_crew_Data();
        }
    }

    public void ca_select_jetty(View v) {
        spin_jetty.showSpinerDialog();

    }

    public void ca_select_State(View v) {
        spin_state.showSpinerDialog();

    }

    public void ca_select_vessel(View v) {
        if (!dd_jetty.getText().toString().equals("")) {
            if (!vessels.isEmpty()) {
                spin_vessel.showSpinerDialog();
            } else
                Toast.makeText(getApplicationContext(), "No vessel to show. Try selecting " +
                        "different jetty", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Select Jetty First", Toast.LENGTH_SHORT).show();

    }

    void get_jetty_list() {
        String url = GlobalVar.server_address + "/androidjson/jetty_list.php";
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;
                                jettys.clear();
                                jetty_list.clear();
                                stateids.clear();
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
                                        String T1 = Comfun.json_string(jo, "Jetty_Name");
                                        String T2 = Comfun.json_string(jo, "JettyCode");
                                        jettys.add(new jettyDetails(Comfun.json_int(jo, "ID"), T1
                                                , T2));
                                        jetty_list.add(T1 + " - " + T2);
                                        stateids.add(Comfun.json_string(jo,"state_id"));
                                    }
                                }

                                spin_jetty = new SpinnerDialog(crewaddedit.this, jetty_list,
                                        "SELECT JETTY",
                                        in.galaxyofandroid.spinerdialog.R.style.DialogAnimations_SmileWindow, "CLOSE");// With 	Animation
                                spin_jetty.setCancellable(true); // for cancellable
                                spin_jetty.setShowKeyboard(false);// for open keyboard by default
                                spin_jetty.setTitleColor(Color.GRAY);
                                spin_jetty.setSearchIconColor(Color.GRAY);
                                spin_jetty.setSearchTextColor(Color.BLACK);
                                spin_jetty.setItemColor(Color.BLACK);
                                spin_jetty.setItemDividerColor(Color.GRAY);
                                spin_jetty.setCloseColor(Color.GRAY);
                                spin_jetty.bindOnSpinerListener(new OnSpinerItemClick() {
                                    @Override
                                    public void onClick(String item, int position) {
                                        jettyCode = jettys.get(position).code;
                                        dd_jetty.setText(item);
                                        state_id=stateids.get(position);
                                    }
                                });
                                if (!jettyCode.equals("")) {
                                    for (int ii = 0; ii < jettys.size(); ii++) {
                                        if (jettys.get(ii).code.equals(jettyCode)) {
                                            dd_jetty.setText(jetty_list.get(ii));
                                        }
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
                map.put("stateid", state_id);
                return map;
            }
        };


        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);

    }

    void get_vesselList() {
        if (!jettyCode.equals("")) {
            String url = GlobalVar.server_address + "/androidjson/vessel_list_jetty_user.php";
            HttpsTrustManager.allowAllSSL();
            vessels.clear();
            vessel_list.clear();
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
                                                Toast.makeText(getApplicationContext(),
                                                        jo.getString("message"),
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        } else {
                                            String T1 = Comfun.json_string(jo, "AssetNm");
                                            String T2 = Comfun.json_string(jo, "AssetId");
                                            vessel_list.add(T1 + "\n" + T2.replace("MM", "MM "));
                                            vessels.add(new vesselDetails(Comfun.json_string(jo,
                                                    "ID"), T1, T2));
                                        }
                                    }

                                    spin_vessel = new SpinnerDialog(crewaddedit.this, vessel_list,
                                            "SELECT VESSEL",
                                            in.galaxyofandroid.spinerdialog.R.style.DialogAnimations_SmileWindow, "CLOSE");// With 	Animation
                                    spin_vessel.setCancellable(true); // for cancellable
                                    spin_vessel.setShowKeyboard(false);// for open keyboard by
                                    spin_vessel.setTitleColor(Color.GRAY);
                                    spin_vessel.setSearchIconColor(Color.GRAY);
                                    spin_vessel.setSearchTextColor(Color.BLACK);
                                    spin_vessel.setItemColor(Color.BLACK);
                                    spin_vessel.setItemDividerColor(Color.GRAY);
                                    spin_vessel.setCloseColor(Color.GRAY);
                                    // default
                                    spin_vessel.bindOnSpinerListener(new OnSpinerItemClick() {
                                        @Override
                                        public void onClick(String item, int position) {
                                            vesselID = vessels.get(position).id;
                                            dd_vessel.setText(vessels.get(position).name);
                                        }
                                    });
                                    if (!vesselID.equals("")) {
                                        for (int ii = 0; ii < vessels.size(); ii++) {
                                            if (vessels.get(ii).id.equals(vesselID)) {
                                                dd_vessel.setText(vessels.get(ii).name);
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "CONNECTION ERROR : " +
                                                    "Check URL",
                                            Toast.LENGTH_SHORT).show();
                                    return;
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
                    map.put("j", jettyCode);
                    map.put("fl", "0");
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);

        }
    }

    void get_stateList() {
        String url = GlobalVar.server_address + "/androidjson/state_list.php";
        HttpsTrustManager.allowAllSSL();
        statelist.clear();
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;
                                statelist.clear();
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(),
                                                    jo.getString("message"),
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    } else {
                                        statelist.add(Comfun.json_string(jo, "states"));
                                    }
                                }
                                spin_state = new SpinnerDialog(crewaddedit.this, statelist,
                                        "SELECT VESSEL",
                                        in.galaxyofandroid.spinerdialog.R.style.DialogAnimations_SmileWindow, "CLOSE");// With 	Animation
                                spin_state.setCancellable(true); // for cancellable
                                spin_state.setShowKeyboard(false);// for open keyboard by default
                                spin_state.setTitleColor(Color.GRAY);
                                spin_state.setSearchIconColor(Color.GRAY);
                                spin_state.setSearchTextColor(Color.BLACK);
                                spin_state.setItemColor(Color.BLACK);
                                spin_state.setItemDividerColor(Color.GRAY);
                                spin_state.setCloseColor(Color.GRAY);
                                spin_state.bindOnSpinerListener(new OnSpinerItemClick() {
                                    @Override
                                    public void onClick(String item, int position) {
                                        dd_state.setText(item);
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : " +
                                                "Check URL",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (Exception ex) {

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    void enable_fields() {
        txt_name.setEnabled(true);
        txt_add1.setEnabled(true);
        txt_add2.setEnabled(true);
        txt_add3.setEnabled(true);
        txt_dob.setEnabled(true);
        txt_mobileno.setEnabled(true);
        txt_height.setEnabled(true);
        txt_aadhar.setEnabled(true);
        txt_healthcardno.setEnabled(true);
        txt_healthcarddate.setEnabled(true);
        txt_joiningDate.setEnabled(true);
        txt_identificationMark.setEnabled(true);
        dd_jetty.setEnabled(true);
        dd_vessel.setEnabled(true);
        dd_state.setEnabled(true);
        btn_crewPic.setEnabled(true);
        btn_aadhar.setEnabled(true);
        btn_address.setEnabled(true);
        btn_scanAadhar.setVisibility(View.VISIBLE);

        //txt_.setEnabled(true);
    }

    void disable_fields() {
        txt_name.setEnabled(false);
        txt_add1.setEnabled(false);
        txt_add2.setEnabled(false);
        txt_add3.setEnabled(false);
        txt_dob.setEnabled(false);
        txt_mobileno.setEnabled(false);
        txt_height.setEnabled(false);
        txt_aadhar.setEnabled(false);
        txt_healthcardno.setEnabled(false);
        txt_healthcarddate.setEnabled(false);
        txt_joiningDate.setEnabled(false);
        txt_identificationMark.setEnabled(false);
        dd_jetty.setEnabled(false);
        dd_vessel.setEnabled(false);
        dd_state.setEnabled(false);
        btn_crewPic.setEnabled(false);
        btn_aadhar.setEnabled(false);
        btn_address.setEnabled(false);
        btn_scanAadhar.setVisibility(View.GONE);

        //txt_.setEnabled(true);
    }

    void add_crew_Data() {
        String url = GlobalVar.server_address + "/androidjson/crew_save.php";
        String dobDate = Comfun.ConvertDateFormat(txt_dob.getText().toString(), "dd/MM/yyyy", "yyyy-MM" +
                "-dd");
        String joiningDate = Comfun.ConvertDateFormat(txt_joiningDate.getText().toString(), "dd/MM/yyyy"
                , "yyyy-MM-dd");
        String healthDate = Comfun.ConvertDateFormat(txt_healthcarddate.getText().toString(), "dd/MM" +
                "/yyyy", "yyyy-MM-dd");
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
                                        Toast.makeText(getApplicationContext(), jo.getString(
                                                "message"),
                                                Toast.LENGTH_SHORT).show();
                                        if (val1 == 1) {
                                            crewaddedit.this.finish();
                                            return;
                                        }
                                    }
                                }


                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check URL",
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
                map.put("addedit","ADD");
                map.put("crewname", txt_name.getText().toString());
                map.put("address1", txt_add1.getText().toString());
                map.put("address2", txt_add2.getText().toString());
                map.put("address3", txt_add3.getText().toString());
                map.put("state", dd_state.getText().toString());
                map.put("jetty", jettyCode);
                map.put("dob", dobDate);
                map.put("heightcm", txt_height.getText().toString());
                map.put("mobileno", txt_mobileno.getText().toString());
                map.put("aadcard", txt_aadhar.getText().toString());
                map.put("hltno", txt_healthcardno.getText().toString());
                map.put("hltdt", healthDate);
                map.put("joining_date", joiningDate);
                map.put("vessel", vesselID);
                map.put("indent_mark", txt_identificationMark.getText().toString());
                if (crewPicPath != null)
                    map.put("profipic", Comfun.encodeBitmapImage(crewPicPath, crewaddedit.this));
                if (idPicPath != null)
                    map.put("idproof", Comfun.encodeBitmapImage(idPicPath, crewaddedit.this));
                if (addressPicPath != null)
                    map.put("addrproof", Comfun.encodeBitmapImage(addressPicPath, crewaddedit.this));
                map.put("state_id", state_id);
                map.put("user", GlobalVar.Muser);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    void update_crew_validity(){
        String url;
        if (state_id=="1")
                url= GlobalVar.server_address + "/crew/crew_renew.php";
        else if(state_id=="2")
            url= GlobalVar.server_address + "/crew_kl/crew_renew.php";
        else
            url="";
        if(url!="") {
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
                                            Toast.makeText(getApplicationContext(), jo.getString(
                                                    "message"),
                                                    Toast.LENGTH_SHORT).show();
                                            if (val1 == 1) {
                                                btn_crewValUpdate.setVisibility(View.INVISIBLE);
                                                show_crew_details(txt_aadhar.getText().toString(), state_id);
                                                return;
                                            }
                                        }
                                    }


                                } else {
                                    Toast.makeText(getApplicationContext(), "CONNECTION ERROR : " +
                                                    "Check URL",
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
                    map.put("aadhar", txt_aadhar.getText().toString());
                    map.put("user", GlobalVar.Muser);
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);
        }
    }

    void update_crew_Data() {
        String url = GlobalVar.server_address + "/androidjson/crew_save.php";
        String dobDate = Comfun.ConvertDateFormat(txt_dob.getText().toString(), "dd/MM/yyyy", "yyyy-MM" +
                "-dd");
        String joiningDate = Comfun.ConvertDateFormat(txt_joiningDate.getText().toString(), "dd/MM/yyyy"
                , "yyyy-MM-dd");
        String healthDate = Comfun.ConvertDateFormat(txt_healthcarddate.getText().toString(), "dd/MM" +
                "/yyyy", "yyyy-MM-dd");
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
                                        Toast.makeText(getApplicationContext(), jo.getString(
                                                "message"),
                                                Toast.LENGTH_SHORT).show();
                                        if (val1 == 1) {
                                            crewaddedit.this.finish();
                                            return;
                                        }else{
                                            btn_saveEdit.setEnabled(true);
                                        }
                                    }
                                }


                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                " URL",
                                        Toast.LENGTH_SHORT).show();
                                btn_saveEdit.setEnabled(true);
                            }
                        } catch (Exception ex) {

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                btn_saveEdit.setEnabled(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("addedit","UPDATE");
                map.put("crewid",crewID);
                map.put("crewname", txt_name.getText().toString());
                map.put("address1", txt_add1.getText().toString());
                map.put("address2", txt_add2.getText().toString());
                map.put("address3", txt_add3.getText().toString());
                map.put("state", dd_state.getText().toString());
                map.put("jetty", jettyCode);
                map.put("dob", dobDate);
                map.put("heightcm", txt_height.getText().toString());
                map.put("mobileno", txt_mobileno.getText().toString());
                map.put("aadcard", txt_aadhar.getText().toString());
                map.put("hltno", txt_healthcardno.getText().toString());
                map.put("hltdt", healthDate);
                map.put("joining_date", joiningDate);
                map.put("vessel", vesselID);
                map.put("indent_mark", txt_identificationMark.getText().toString());
                if (crewPicPath != null && btn_crewPic.getVisibility()==View.VISIBLE)
                    map.put("profipic", Comfun.encodeBitmapImage(crewPicPath, crewaddedit.this));
                if (idPicPath != null && btn_aadhar.getVisibility()==View.VISIBLE)
                    map.put("idproof", Comfun.encodeBitmapImage(idPicPath, crewaddedit.this));
                if (addressPicPath != null && btn_address.getVisibility()==View.VISIBLE)
                    map.put("addrproof", Comfun.encodeBitmapImage(addressPicPath, crewaddedit.this));
                map.put("state_id", state_id);
                map.put("user", GlobalVar.Muser);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    void show_crew_details(String Aadhar, String stateId) {
        String url = GlobalVar.server_address + "/androidjson/crewview.php";
        String postval = "?&p=1234&a=" + Aadhar+"&s=" + stateId;

        class dbManager extends AsyncTask<String, Void, String> {
            protected void onPostExecute(String data) {
                try {
                    if (data.contains("value")) {
                        JSONArray ja = new JSONArray(data);
                        JSONObject jo = null;
                        for (int i = 0; i < ja.length(); i++) {
                            jo = ja.getJSONObject(i);
                            if (i == 0) {
                                int val1 = Integer.parseInt(jo.getString("value"));
                                if (val1 == 0) {
                                    Toast.makeText(getApplicationContext(), jo.getString("message"),
                                            Toast.LENGTH_SHORT).show();
                                    crewaddedit.this.finish();
                                    return;
                                }
                            } else {
                                crewID = Comfun.json_string(jo, "ID");
                                txt_name.setText(Comfun.json_string(jo, "Crewname"));
                                txt_add1.setText(Comfun.json_string(jo, "CrewAddress1"));
                                txt_add2.setText(Comfun.json_string(jo, "CrewAddress2"));
                                txt_add3.setText(Comfun.json_string(jo, "CrewAddress3"));
                                txt_dob.setText(Comfun.ConvertDateFormat(Comfun.json_string(jo, "DOB"),
                                        "yyyy-MM-dd", "dd/MM/yyyy"));
                                txt_mobileno.setText(Comfun.json_string(jo, "MobNo"));
                                txt_height.setText(Comfun.json_string(jo, "Height"));
                                txt_aadhar.setText(Comfun.json_string(jo, "AadharNo"));
                                txt_joiningDate.setText(Comfun.ConvertDateFormat(Comfun.json_string(jo,
                                        "joining_date"), "yyyy-MM-dd", "dd/MM/yyyy"));
                                txt_healthcardno.setText(Comfun.json_string(jo, "HealthCrdNo"));
                                txt_healthcarddate.setText(Comfun.ConvertDateFormat(Comfun.json_string(jo, "HealthCrdDt"), "yyyy-MM-dd", "dd/MM/yyyy"));
                                txt_identificationMark.setText(Comfun.json_string(jo, "Body_mark"));
                                dd_state.setText(Comfun.json_string(jo, "state"));
                                jettyCode = Comfun.json_string(jo, "JettyCode");
                                vesselID = Comfun.json_string(jo, "Vessel_id");
                                try{
                                    Date cVal = ymdt.parse(Comfun.json_string(jo, "RFIDValDt"));
                                    txt_valDt.setText(dmy.format(cVal));
                                    if(GlobalVar.crewValUpdate) {
                                        Date today = Calendar.getInstance().getTime();
                                        if (cVal.compareTo(today) > 0) {
                                            btn_crewValUpdate.setVisibility(View.INVISIBLE);
                                        } else {
                                            btn_crewValUpdate.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }catch (Exception e){
                                    txt_valDt.setText("");
                                    if(GlobalVar.crewValUpdate){
                                        btn_crewValUpdate.setVisibility(View.VISIBLE);
                                    }
                                }

                                get_jetty_list();


                                String imgLoc = Comfun.json_string(jo, "PhotoPath");
                                if (imgLoc.trim().length() > 3) {
                                    btn_crewPic.setVisibility(View.GONE);
                                    imgLoc = GlobalVar.server_address + "/" + imgLoc;
                                    ImageRequest imageRequest = new ImageRequest(imgLoc,
                                            new Response.Listener<Bitmap>() {
                                                @Override
                                                public void onResponse(Bitmap response) {
                                                    crewPic.setForeground(null);
                                                    crewPic.setImageBitmap(response);
                                                    try {
                                                        File file =
                                                                new File(crewaddedit.this.getCacheDir(), "tmpcrew.png");
                                                        if (file.exists()) {
                                                            file.delete();
                                                            file.createNewFile();
                                                        }
                                                        FileOutputStream fos =
                                                                new FileOutputStream(file);
                                                        response.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                                        crewPicPath = Uri.fromFile(file);
                                                    } catch (Exception ex) {

                                                    }
                                                }
                                            }, 0, 0, ImageView.ScaleType.FIT_CENTER, null,
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Toast.makeText(getApplicationContext(),
                                                            error.toString(), Toast.LENGTH_LONG).show();

                                                }
                                            });
                                    MySingleton.getInstance(crewaddedit.this).addToRequestQue(imageRequest);
                                }
                                imgLoc = Comfun.json_string(jo, "IDproofPath");
                                if (imgLoc.trim().length() > 3) {
                                    btn_aadhar.setVisibility(View.GONE);
                                    imgLoc = GlobalVar.server_address + "/" + imgLoc;
                                    ImageRequest imageRequest = new ImageRequest(imgLoc,
                                            new Response.Listener<Bitmap>() {
                                                @Override
                                                public void onResponse(Bitmap response) {
                                                    idPic.setForeground(null);
                                                    idPic.setImageBitmap(response);
                                                    try {
                                                        File file =
                                                                new File(crewaddedit.this.getCacheDir(), "tmpidprof.png");
                                                        if (file.exists()) {
                                                            file.delete();
                                                            file.createNewFile();
                                                        }
                                                        FileOutputStream fos =
                                                                new FileOutputStream(file);
                                                        response.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                                        idPicPath = Uri.fromFile(file);
                                                    } catch (Exception ex) {

                                                    }
                                                }
                                            }, 0, 0, ImageView.ScaleType.FIT_CENTER, null,
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Toast.makeText(getApplicationContext(),
                                                            error.toString(), Toast.LENGTH_LONG).show();

                                                }
                                            });
                                    MySingleton.getInstance(crewaddedit.this).addToRequestQue(imageRequest);
                                }
                                 imgLoc = Comfun.json_string(jo, "AddrsPath");
                                if (imgLoc.trim().length() > 3) {
                                    btn_address.setVisibility(View.GONE);
                                    imgLoc = GlobalVar.server_address + "/" + imgLoc;
                                    ImageRequest imageRequest = new ImageRequest(imgLoc,
                                            new Response.Listener<Bitmap>() {
                                                @Override
                                                public void onResponse(Bitmap response) {
                                                    addressPic.setForeground(null);
                                                    addressPic.setImageBitmap(response);
                                                    try {
                                                        File file =
                                                                new File(crewaddedit.this.getCacheDir(), "tmpaddr.png");
                                                        if (file.exists()) {
                                                            file.delete();
                                                            file.createNewFile();
                                                        }
                                                        FileOutputStream fos =
                                                                new FileOutputStream(file);
                                                        response.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                                        addressPicPath = Uri.fromFile(file);
                                                    } catch (Exception ex) {

                                                    }
                                                }
                                            }, 0, 0, ImageView.ScaleType.FIT_CENTER, null,
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Toast.makeText(getApplicationContext(),
                                                            error.toString(), Toast.LENGTH_LONG).show();

                                                }
                                            });
                                    MySingleton.getInstance(crewaddedit.this).addToRequestQue(imageRequest);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "NO MATCHING RECORDS FOUND",
                                Toast.LENGTH_SHORT).show();
                        crewaddedit.this.finish();
                        return;
                    }
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                    crewaddedit.this.finish();
                    return;
                }
            }

            @Override
            protected String doInBackground(String... param) {
                try {
                    URL url = new URL(param[0]);
                    HttpsTrustManager.allowAllSSL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    BufferedReader br =
                            new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuffer data = new StringBuffer();
                    String line;

                    while ((line = br.readLine()) != null) {
                        data.append(line + "\n");
                    }
                    br.close();
                    return data.toString();
                } catch (Exception ex) {
                    return ex.getMessage();
                }
            }
        }
        dbManager obj1 = new dbManager();
        obj1.execute(url + postval);

    }

    public void ca_add_dob(View view) {
        dtPickerDOB.show();
    }

    public void ca_add_joining_date(View view) {
        dtPickerJoining.show();
    }

    public void ca_add_HealthCard(View view) {
        dtPickerHlthCard.show();
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener date_Listner_DOB =
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                txt_dob.setText(String.format("%02d", day) + "/" + String.format("%02d", month) + "/" + String.format("%04d", year));


            }
        };
        DatePickerDialog.OnDateSetListener date_Listner_Joining =
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                txt_joiningDate.setText(String.format("%02d", day) + "/" + String.format("%02d",
                        month) + "/" + String.format("%04d", year));

            }
        };
        DatePickerDialog.OnDateSetListener date_Listner_Healthcard =
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                txt_healthcarddate.setText(String.format("%02d", day) + "/" + String.format("%02d"
                        , month) + "/" + String.format("%04d", year));

            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        dtPickerDOB = new DatePickerDialog(this, style, date_Listner_DOB, year - 18, month, day);
        dtPickerDOB.getDatePicker().setMaxDate(System.currentTimeMillis());
        dtPickerJoining = new DatePickerDialog(this, style, date_Listner_Joining, year, month, day);
        dtPickerHlthCard = new DatePickerDialog(this, style, date_Listner_Healthcard, year, month, day);

    }

    private class vesselDetails {
        String name, vrc, id;

        vesselDetails(String vessel_ID, String vesselName, String vrc) {
            this.id = vessel_ID;
            this.name = vesselName;
            this.vrc = vrc;
        }
    }

    private class jettyDetails {
        String name, code;
        int id;

        jettyDetails(int id, String jettyName, String jettyCode) {
            this.id = id;
            this.name = jettyName;
            this.code = jettyCode;
        }
    }
}