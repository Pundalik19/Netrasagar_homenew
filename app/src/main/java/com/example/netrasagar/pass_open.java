package com.example.netrasagar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cloudpos.DeviceException;
import com.cloudpos.POSTerminal;
import com.cloudpos.TerminalSpec;
import com.cloudpos.card.Card;
import com.cloudpos.led.LEDDevice;
import com.cloudpos.rfcardreader.RFCardReaderDevice;
import com.example.netrasagar.recyclers.CrewDisplayList;
import com.example.netrasagar.recyclers.RecyclerCrewAdapter;
import com.example.netrasagar.rfid.HexString;
import com.example.netrasagar.rfid.RFID_vtek;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class pass_open extends AppCompatActivity {


    private final byte[] mifare_key = HexString.hexToBuffer(GlobalVar.key);
    TextView txtVesselName, txtVrc, txtTotCrew, dd_jetty, dd_vessel;
    EditText edtVoyageDays, edtDiesel, edtLifeJackets, edtLifeBuoys, edtCrewAdditional;
    CheckBox chkGPS, chkVHF, chkAIS;
    Button btnSave, btnCancel, btnCrwAdd, btnCrwCancel, btnRFID;
    ImageButton btnRefreshCrewAdd;
    FloatingActionButton btnAddCrew;
    EditText crwFilter;
    RadioButton rbAll, rbLastVoyage;
    ScrollView passCover;
    ConstraintLayout crewListCover;
    LinearLayout progressview, addCrewButtonCover, saveButtonCover, selectors;
    SpinnerDialog spin_vessel;
    ArrayList<String> lst_vessel_disp = new ArrayList<>();
    ArrayList<vesselDetails> lst_vessel_details = new ArrayList<>();
    SpinnerDialog spin_jetty;
    ArrayList<String> lst_jetty_disp = new ArrayList<>();
    ArrayList<jettyDetails> lst_jetty_details = new ArrayList<>();
    int loading_bit = 0;
    RecyclerView recyclCrew, recycleAddCrew;
    RecyclerCrewAdapter crewAdapter, crewAddAdapter;
    ArrayList<CrewDisplayList> crewArray = new ArrayList<>();
    ArrayList<CrewDisplayList> crewAddArray = new ArrayList<>();
    String rfidCardNo30, vessel_rfidcardNo30;
    private Card mifare_1k_card;
    private RFCardReaderDevice RFID_Device = null;
    private LEDDevice LED_Device = null;
    private MediaPlayer sword_chime;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_open);
        getSupportActionBar().hide();

        vessel_rfidcardNo30 = "";

        txtVrc = findViewById(R.id.po_vrc);
        txtVesselName = findViewById(R.id.po_vesselName);
        edtVoyageDays = findViewById(R.id.po_voyageDays);
        edtDiesel = findViewById(R.id.po_diesel);
        edtLifeBuoys = findViewById(R.id.po_lifeBouys);
        edtLifeJackets = findViewById(R.id.po_life_jacket);
        edtCrewAdditional = findViewById(R.id.po_CrewWentAdd);
        chkGPS = findViewById(R.id.po_gps);
        chkVHF = findViewById(R.id.po_vhf);
        chkAIS = findViewById(R.id.po_ais);
        btnSave = findViewById(R.id.po_btnSave);
        btnCancel = findViewById(R.id.po_btnCancel);
        btnRFID = findViewById(R.id.po_rfid);
        btnAddCrew = findViewById(R.id.po_crewAdd);
        recyclCrew = findViewById(R.id.po_crewList);
        recycleAddCrew = findViewById(R.id.po_crewAddList);
        passCover = findViewById(R.id.po_passOpenCover);
        crewListCover = findViewById(R.id.po_crewAddListCover);
        progressview = findViewById(R.id.po_progressbar);
        rbAll = findViewById(R.id.po_rbAll);
        rbLastVoyage = findViewById(R.id.po_rbLastVoyage);
        crwFilter = findViewById(R.id.po_crwFilter);
        btnCrwAdd = findViewById(R.id.po_CrewAddListAdd);
        btnCrwCancel = findViewById(R.id.po_CrewAddListCancel);
        addCrewButtonCover = findViewById(R.id.po_addCrewbuttonCover);
        saveButtonCover = findViewById(R.id.po_saveBtnCover);
        btnRefreshCrewAdd = findViewById(R.id.po_refreshCrewAdd);
        txtTotCrew = findViewById(R.id.po_TotalCrew);
        dd_vessel = findViewById(R.id.po_selVessel);
        dd_jetty = findViewById(R.id.po_selJetty);
        selectors = findViewById(R.id.po_Selectors);

        sword_chime = MediaPlayer.create(this, R.raw.sword_chime);

        passCover.setVisibility(View.GONE);
        crewListCover.setVisibility(View.GONE);

        recyclCrew.setLayoutManager(new LinearLayoutManager(this));
        crewAdapter = new RecyclerCrewAdapter(this, crewArray, txtTotCrew, edtCrewAdditional);
        recyclCrew.setAdapter(crewAdapter);

        recycleAddCrew.setLayoutManager(new LinearLayoutManager(this));
        crewAddAdapter = new RecyclerCrewAdapter(this, crewAddArray);
        recycleAddCrew.setAdapter(crewAddAdapter);

        get_jetty_list();
        try {

            TerminalSpec pt = POSTerminal.getInstance(getApplicationContext()).getTerminalSpec();
            if (pt.getManufacturer().equals("wizarPOS") && !pt.getSerialNumber().equals("unknown")) {
                if (LED_Device == null) {
                    LED_Device = (LEDDevice) POSTerminal.getInstance(getApplicationContext())
                            .getDevice("cloudpos.device.led");
                    LED_Device.open(LEDDevice.ID_GREEN);
                    LED_Device.turnOn();
                    if (RFID_Device == null) {
                        RFID_Device = (RFCardReaderDevice) POSTerminal.getInstance(getApplicationContext())
                                .getDevice("cloudpos.device.rfcardreader");

                    }
                    LED_Device.turnOff();
                    LED_Device.close();
                }
            } else {
                btnRFID.setVisibility(View.INVISIBLE);
            }

        } catch (DeviceException e) {
            LED_Device = null;
            RFID_Device = null;
            btnRFID.setVisibility(View.INVISIBLE);
            Log.d("POS ERROR", e.getMessage());
        }

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            addCrewButtonCover.setOrientation(LinearLayout.HORIZONTAL);
            saveButtonCover.setOrientation(LinearLayout.HORIZONTAL);
        }

        dd_vessel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lst_vessel_disp.size() > 0)
                    spin_vessel.showSpinerDialog();
                else
                    Toast.makeText(getApplicationContext(), "No vessels to show", Toast.LENGTH_SHORT).show();
            }
        });
        dd_jetty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lst_jetty_disp.size() > 0)
                    spin_jetty.showSpinerDialog();
                else
                    Toast.makeText(getApplicationContext(), "No Jetties to show", Toast.LENGTH_SHORT).show();
            }
        });
        btnCancel.setOnClickListener(view -> pass_open.this.finish());
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if (Integer.valueOf(edtCrewAdditional.getText().toString()) >= 0) {
                        open_pass();

                    }
                } catch (NumberFormatException ne) {
                    Toast.makeText(getApplicationContext(), "Additional crew cannot be blank", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btnAddCrew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passCover.setVisibility(View.GONE);
                selectors.setVisibility(View.GONE);
                crewListCover.setVisibility(View.VISIBLE);
            }
        });
        btnCrwCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passCover.setVisibility(View.VISIBLE);
                selectors.setVisibility(View.VISIBLE);
                crewListCover.setVisibility(View.GONE);
            }
        });
        btnCrwAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_progress();


                for (CrewDisplayList item : crewAddArray) {
                    if (item.checked) {
                        item.checked = false;
                        item.status = false;
                        crewArray.add(item);
                    }
                }
                crewAddAdapter.remove_item(crewArray);
                crewAddAdapter.full_table_copy();
                crewAdapter.notifyDataSetChanged();
                crewAddAdapter.notifyDataSetChanged();

                passCover.setVisibility(View.VISIBLE);
                selectors.setVisibility(View.VISIBLE);
                crewListCover.setVisibility(View.GONE);
                crewAdapter.update_crew_count();
                hide_progress();
            }
        });
        rbLastVoyage.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                get_last_crew_list(txtVrc.getText().toString(), "LAST_VOYAGE");
            }

        });
        rbAll.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                get_last_crew_list(txtVrc.getText().toString(), "ALL");
            }

        });
        btnRefreshCrewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbLastVoyage.isChecked())
                    get_last_crew_list(txtVrc.getText().toString(), "LAST_VOYAGE");
                else if (rbAll.isChecked())
                    get_last_crew_list(txtVrc.getText().toString(), "ALL");
                else
                    Toast.makeText(pass_open.this, "Nothing to refresh", Toast.LENGTH_SHORT).show();
            }
        });
        crwFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                crewAddAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });
        edtCrewAdditional.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                crewAdapter.update_crew_count();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btnRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler1 = new Handler();
                show_progress();
                Log.d("RFID_button", "RFID Button clicked");
                Toast.makeText(getApplicationContext(), "TAP CARD", Toast.LENGTH_SHORT).show();

                Runnable runnable1 = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LED_Device.open(LEDDevice.ID_GREEN);
                            RFID_Device.open();
                            LED_Device.startBlink(150, 150);
                            mifare_1k_card = RFID_vtek.get_RFID_card(RFID_Device);
                            rfidCardNo30 = "";
                            if (RFID_vtek.verify_RFID_card(12, mifare_key, mifare_1k_card)) {
                                rfidCardNo30 = RFID_vtek.read_RFID_card(12, 0, mifare_1k_card).trim();
                            }

                            handler1.post(new Runnable() {
                                @Override
                                public void run() {
                                    int ii;
                                    if (txtVrc.getText().toString().length() < 3) {
                                        if (rfidCardNo30.startsWith("DOFV")) {
                                            vessel_rfidcardNo30 = rfidCardNo30;
                                            for (ii = 0; ii < lst_vessel_details.size(); ii++) {
                                                if (lst_vessel_details.get(ii).RFID.equals(rfidCardNo30)) {
                                                    show_vessel_details(ii);
                                                    get_last_crew_list(txtVrc.getText().toString(), "ALL");
                                                    dd_vessel.setVisibility(View.GONE);
                                                    findViewById(R.id.po_vesselLabel).setVisibility(View.GONE);
                                                    break;
                                                }
                                            }
                                            if (ii >= lst_vessel_details.size()) {
                                                Toast.makeText(getApplicationContext(), "VESSEL NOT FOUND", Toast.LENGTH_SHORT).show();
                                                vessel_rfidcardNo30 = "";
                                            } else {
                                                Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
                                                sword_chime.start();
                                            }
                                        }
                                    } else if (rfidCardNo30.startsWith("DOFC")) {
                                        for (ii = 0; ii < crewAddArray.size(); ii++) {
                                            if (crewAddArray.get(ii).RFID.equals(rfidCardNo30)) {
                                                if (crewAddArray.get(ii).status) {
                                                    crewAddArray.get(ii).checked = false;
                                                    crewAddArray.get(ii).status = false;
                                                    crewArray.add(crewAddArray.get(ii));
                                                    crewAddAdapter.remove_item(crewArray);
                                                    crewAddAdapter.full_table_copy();
                                                    crewAdapter.notifyDataSetChanged();
                                                    crewAddAdapter.notifyDataSetChanged();
                                                } else {
                                                    ii = crewAddArray.size() + 1;
                                                }
                                                break;
                                            }
                                        }
                                        if (ii < crewAddArray.size()) {
                                            Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
                                            sword_chime.start();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "FAILED", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                    hide_progress();
                                }
                            });

                            LED_Device.cancelBlink();
                            LED_Device.close();
                            RFID_Device.close();

                        } catch (Exception ex) {
                            Log.d("RFID_button click", ex.getMessage());
                            try {
                                LED_Device.close();
                            } catch (DeviceException e) {
                                Log.d("POS RFID Error", e.toString());
                            }
                            try {
                                RFID_Device.close();
                            } catch (DeviceException e) {
                                Log.d("POS RFID Error", e.toString());
                            }
                            handler1.post(new Runnable() {
                                @Override
                                public void run() {
                                    hide_progress();
                                }
                            });
                        }
                    }
                };

                Thread thread1 = new Thread(runnable1);
                thread1.start();


            }
        });


    }

    void get_vesselList(int jettyPosition) {
        if (jettyPosition >= 0) {
            String url = GlobalVar.server_address + "/androidjson/vessel_list_jetty_user.php";
            HttpsTrustManager.allowAllSSL();
            lst_vessel_disp.clear();
            lst_vessel_details.clear();
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
                                            lst_vessel_disp.add(T1 + "\n" + T2.replace("MM", "MM "));
                                            lst_vessel_details.add(new pass_open.vesselDetails(Comfun.json_string(jo, "ID"),
                                                    T1, T2, Comfun.json_string(jo, "RFIDCardNo")));
                                        }
                                    }
                                    spin_vessel = new SpinnerDialog(pass_open.this, lst_vessel_disp,
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
                                    spin_vessel.bindOnSpinerListener((item, position) -> {
                                        dd_vessel.setText(item);
                                        show_vessel_details(position);
                                    });
                                    clear_fields();
                                } else {
                                    Toast.makeText(getApplicationContext(), "CONNECTION ERROR : " +
                                            "Check URL", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } catch (Exception ex) {
                                Log.d("Get Vessel List", ex.getMessage());
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
                    map.put("j", lst_jetty_details.get(jettyPosition).code);
                    map.put("fl", "2");
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);

        }
    }

    private void clear_fields() {
        txtVesselName.setText("");
        txtVrc.setText("");
        dd_vessel.setText("");
        passCover.setVisibility(View.GONE);
    }

    private void show_vessel_details(int position) {
        vesselDetails selVeseel = lst_vessel_details.get(position);
        txtVesselName.setText(selVeseel.name);
        txtVrc.setText(selVeseel.vrc);
        passCover.setVisibility(View.VISIBLE);


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
                                lst_jetty_disp.clear();
                                lst_jetty_details.clear();
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
                                        lst_jetty_details.add(new pass_open.jettyDetails(Comfun.json_int(jo, "ID"), T1
                                                , T2));
                                        lst_jetty_disp.add(T1 + " - " + T2);
                                    }
                                }


                                spin_jetty = new SpinnerDialog(pass_open.this, lst_jetty_disp,
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
                                        dd_jetty.setText(item);
                                        get_vesselList(position);
                                    }
                                });
                                dd_jetty.setText(lst_jetty_disp.get(0));
                                get_vesselList(0);

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
                map.put("stateid", GlobalVar.state_id);
                return map;
            }
        };


        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);

    }

    private void get_last_crew_list(String VRC, String Last_voyage) { // yes to show last voyage crew
        show_progress();
        String url = GlobalVar.server_address + "/androidjson/fishpass/pass_crew_list.php";
        crewAddArray.clear();
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("pass_save", "response : " + response);

                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;
                                int val1 = 0;
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 != 1) {
                                            Toast.makeText(getApplicationContext(), Comfun
                                                    .json_string(jo, "message"), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if (val1 == 1) {
                                            String vsl_status = "";
                                            boolean status, checked;
                                            if (Comfun.json_string(jo, "blacklist_bit").equals("1"))
                                                vsl_status += "Crew is Blacklisted\n";
                                            if (Comfun.json_string(jo, "Debarred").equals("1"))
                                                vsl_status += "Crew is Debarred\n";
                                            if (Comfun.json_string(jo, "VoyageStatus").equals("0"))
                                                vsl_status += "Crew last Voyage is still Open\n";

                                            if (vsl_status.equals("")) {
                                                vsl_status = "ACTIVE";
                                                status = true;
                                            } else {
                                                status = false;
                                            }
                                            boolean crewPresent = false;
                                            for (CrewDisplayList item : crewArray) {
                                                if (item.Aadhar.equals(Comfun.json_string(jo, "AadharNo"))) {
                                                    crewPresent = true;
                                                    break;
                                                }
                                            }
                                            if (!crewPresent) {
                                                crewAddArray.add(new CrewDisplayList(Comfun.json_string(jo, "ID"),
                                                        Comfun.json_string(jo, "AadharNo"),
                                                        Comfun.json_string(jo, "Crewname"),
                                                        Comfun.json_string(jo, "Vessel_name"),
                                                        Comfun.json_string(jo, "RFIDCardNo"),
                                                        Comfun.json_string(jo, "ElectionCard"),
                                                        Comfun.json_string(jo, "MobNo"),
                                                        vsl_status, status, false));
                                            }
                                        }
                                    }
                                }
                                crewAddAdapter.notifyDataSetChanged();
                                crewAddAdapter.full_table_copy();
                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check URL",
                                        Toast.LENGTH_SHORT).show();

                                return;
                            }
                            hide_progress();
                        } catch (Exception ex) {
                            Log.d("pass_save", ex.getMessage());
                            Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check Internet Connection",
                                    Toast.LENGTH_SHORT).show();
                            hide_progress();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("crew_display", error.getMessage());
                Intent intent = new Intent(getApplicationContext(), sel_option.class);
                startActivity(intent);
                pass_open.this.finish();
                return;

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user", GlobalVar.Muser);
                map.put("vrc", VRC);
                map.put("lv", Last_voyage);

                return map;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                5000, 3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Log.d("pass_save", "url : " + url);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    private void open_pass() {
        String tmpcrwIds = "";
        if (crewArray.size() > 0) {
            for (CrewDisplayList item : crewArray) {
                tmpcrwIds += item.id + ",";
            }
            tmpcrwIds = tmpcrwIds.substring(0, tmpcrwIds.length() - 1);
        }
        final String crwIds = tmpcrwIds;

        show_progress();
        String url = GlobalVar.server_address + "/androidjson/fishpass/pass_open.php";
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("pass_save", "response : " + response);
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        Toast.makeText(getApplicationContext(), Comfun
                                                .json_string(jo, "message"), Toast.LENGTH_SHORT).show();
                                        if (val1 == 1) {
                                            if (!vessel_rfidcardNo30.equals("")) {
                                                Log.d("thread name open Pass ", Thread.currentThread().getName());
                                                write_pass_details(Comfun.json_string(jo, "passNo"),
                                                        Comfun.json_string(jo, "strttm"),
                                                        Comfun.json_string(jo, "endtm"),
                                                        txtTotCrew.getText().toString(),
                                                        Comfun.json_string(jo, "state_id"));
                                            } else {
                                                Intent intent = new Intent(pass_open.this, print_pass.class);
                                                intent.putExtra("passNo", Comfun.json_string(jo, "passNo"));
                                                intent.putExtra("StateId", Comfun.json_string(jo, "state_id"));
                                                startActivity(intent);
                                                pass_open.this.finish();
                                            }
                                            hide_progress();
                                            return;
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check URL",
                                        Toast.LENGTH_SHORT).show();
                            }
                            hide_progress();
                        } catch (Exception ex) {
                            Log.d("pass_save", ex.getMessage());
                            Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check Internet Connection",
                                    Toast.LENGTH_SHORT).show();
                            hide_progress();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("pass_save", error.getMessage());
                Intent intent = new Intent(getApplicationContext(), sel_option.class);
                startActivity(intent);
                pass_open.this.finish();
                return;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user", GlobalVar.Muser);
                map.put("RFID", "");
                map.put("VesselNm", txtVesselName.getText().toString());
                map.put("VRCNo", txtVrc.getText().toString());
                map.put("Days", edtVoyageDays.getText().toString());
                map.put("diesel", edtDiesel.getText().toString());
                map.put("LifeBouys", edtLifeBuoys.getText().toString());
                map.put("LifeJackets", edtLifeJackets.getText().toString());
                map.put("GPS", (chkGPS.isChecked()) ? "1" : "0");
                map.put("VHF", (chkVHF.isChecked()) ? "1" : "0");
                map.put("AIS", (chkAIS.isChecked()) ? "1" : "0");
                map.put("CrewWent", String.valueOf(crewArray.size()));
                map.put("CrewWentAdd", edtCrewAdditional.getText().toString());
                map.put("C_IDs", crwIds);
                map.put("appType", "APPROVED");
                map.put("utype", "0");

                return map;
            }
        };
        Log.d("pass_save", "url : " + url);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    private void write_pass_details(String passNo, String strtm, String endtm, String crewCnt, String stateid) {
        final String pass_no = passNo;
        final String strt_tm = strtm;
        final String end_tm = endtm;
        final String crew_cnt = crewCnt;
        final String state_id = stateid;
        Log.d("thread name Write Pass ", Thread.currentThread().getName());

        try {
            LED_Device.open(LEDDevice.ID_GREEN);
            RFID_Device.open();
            LED_Device.startBlink(150, 150);
            Toast.makeText(getApplicationContext(), "TAP Vessel CARD", Toast.LENGTH_SHORT).show();
            show_progress();
            Handler handl1 = new Handler();
            Runnable runn = new Runnable() {
                @Override
                public void run() {
                    try {
                        do {
                            Log.d("thread name Write Pass run", Thread.currentThread().getName());
                            mifare_1k_card = RFID_vtek.get_RFID_card(RFID_Device);
                            if (mifare_1k_card != null) {
                                rfidCardNo30 = "";
                                if (RFID_vtek.verify_RFID_card(12, mifare_key, mifare_1k_card)) {
                                    rfidCardNo30 = RFID_vtek.read_RFID_card(12, 0, mifare_1k_card).trim();
                                }
                                if (vessel_rfidcardNo30.equals(rfidCardNo30)) {
                                    String tmppass = pass_no.replace("DOF", "");

                                    if (RFID_vtek.verify_RFID_card(13, mifare_key, mifare_1k_card)) {
                                        boolean retVal = true;
                                        retVal = retVal && RFID_vtek.write_RFID_Data(13, 0, tmppass, mifare_1k_card);
                                        String tmpstr = strt_tm.replaceAll("[-/]", "") + end_tm.replaceAll("[-/]", "");
                                        retVal = retVal && RFID_vtek.write_RFID_Data(13, 1, tmpstr, mifare_1k_card);
                                        retVal = retVal && RFID_vtek.write_RFID_Data(13, 2, crew_cnt, mifare_1k_card);
                                        if (retVal == false) {
                                            handl1.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "Writing to card error. Tap card again",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }
                                        break;
                                    }


                                } else {
                                    handl1.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Incorrect vessel card. TAP correct Vessel CARD",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            } else {
                                rfidCardNo30 = "";
                            }

                        } while (!vessel_rfidcardNo30.equals(rfidCardNo30));


                        LED_Device.cancelBlink();
                        LED_Device.close();
                        RFID_Device.close();
                        handl1.post(new Runnable() {
                            @Override
                            public void run() {
                                hide_progress();
                                Intent intent = new Intent(pass_open.this, print_pass.class);
                                intent.putExtra("passNo", pass_no);
                                intent.putExtra("StateId", state_id);
                                startActivity(intent);
                                pass_open.this.finish();
                            }
                        });

                    } catch (DeviceException e) {
                        Log.d("POS RFID WRITE ERROR", e.toString());
                        hide_progress();
                        try {
                            LED_Device.close();
                        } catch (DeviceException ex) {
                            Log.d("POS RFID Error", ex.toString());
                        }
                        try {
                            RFID_Device.close();
                        } catch (DeviceException ex) {
                            Log.d("POS RFID Error", ex.toString());
                        }
                    }
                }
            };
            Thread thrd1 = new Thread(runn);
            thrd1.start();


        } catch (DeviceException e) {
            Log.d("POS RFID Error", e.toString());
            hide_progress();
            try {
                LED_Device.close();
            } catch (DeviceException ex) {
                Log.d("POS RFID Error", ex.toString());
            }
            try {
                RFID_Device.close();
            } catch (DeviceException ex) {
                Log.d("POS RFID Error", ex.toString());
            }

        }

    }

    private void show_progress() {
        loading_bit += 1;
        progressview.setVisibility(View.VISIBLE);
        progressview.bringToFront();

    }

    private void hide_progress() {
        loading_bit -= 1;
        if (loading_bit <= 0) {
            loading_bit = 0;
            progressview.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            addCrewButtonCover.setOrientation(LinearLayout.HORIZONTAL);
            saveButtonCover.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            addCrewButtonCover.setOrientation(LinearLayout.VERTICAL);
            saveButtonCover.setOrientation(LinearLayout.VERTICAL);
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

    private class vesselDetails {
        String name, vrc, id, RFID;

        vesselDetails(String vessel_ID, String vesselName, String vrc, String RFID) {
            this.id = vessel_ID;
            this.name = vesselName;
            this.vrc = vrc;
            this.RFID = RFID;
        }
    }
}