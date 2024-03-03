package com.example.netrasagar;

import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class pass_close extends AppCompatActivity {

    String passNo;
    TextView txtVesselNm, txtPassNo, txtPassDate, txtTotCrew, txtCrewWent,dd_vessel;
    EditText crewReturned;
    Button btnSave, btnCancel, btnRFID;
    LinearLayout saveButtonCover, progressview;
    int crewLost, loading_bit=0;

    RecyclerView recyclCrew;
    RecyclerCrewAdapter crewAdapter;
    ArrayList<CrewDisplayList> crewArray = new ArrayList<>();

    SpinnerDialog spin_vessel;
    ArrayList<String> lst_vessel_disp = new ArrayList<>();
    ArrayList<String> lst_vessel_pass = new ArrayList<>();
    ArrayList<String> lst_vessel_stateId = new ArrayList<>();

    private MediaPlayer sword_chime ;

    //******* RFID Variable Starts ************//
    private Card mifare_1k_card;
    private RFCardReaderDevice RFID_Device = null;
    private LEDDevice LED_Device = null;
    private final byte[] mifare_key = HexString.hexToBuffer(GlobalVar.key);
    String rfidCardNo30;

    //******* RFID Variable Ends ************//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_close);
        getSupportActionBar().hide();

        crewLost = 0;

        btnSave = findViewById(R.id.pc_btnSave);
        btnCancel = findViewById(R.id.pc_btnCancel);
        btnRFID = findViewById(R.id.pc_rfid);
        txtVesselNm = findViewById(R.id.pc_vesselName);
        txtPassNo = findViewById(R.id.pc_passNo);
        txtPassDate = findViewById(R.id.pc_passDate);
        txtTotCrew = findViewById(R.id.pc_totCrew);
        txtCrewWent = findViewById(R.id.pc_crewWent);
        crewReturned = findViewById(R.id.pc_crewReturned);
        saveButtonCover = findViewById(R.id.pc_saveBtnCover);
        recyclCrew = findViewById(R.id.pc_crwList);
        dd_vessel = findViewById(R.id.pc_selVessel);
        progressview = findViewById(R.id.pc_progressbar);

        sword_chime = MediaPlayer.create(this,R.raw.sword_chime);

        get_vessel_list();

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

        recyclCrew.setLayoutManager(new LinearLayoutManager(this));
        crewAdapter = new RecyclerCrewAdapter(this,crewArray);
        recyclCrew.setAdapter(crewAdapter);

        if(this.getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE){
            saveButtonCover.setOrientation(LinearLayout.HORIZONTAL);
        }
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pass_close.this.finish();
            }
        });
        dd_vessel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lst_vessel_disp.size()>0)
                    spin_vessel.showSpinerDialog();
                else
                    Toast.makeText(getApplicationContext(),"No vessels to show",Toast.LENGTH_SHORT).show();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int total_returned;
                    if (Integer.parseInt(crewReturned.getText().toString()) == Integer.parseInt(txtCrewWent.getText().toString())) {
                        total_returned = Integer.valueOf(crewReturned.getText().toString());
                        for (CrewDisplayList item : crewArray) {
                            if (item.checked)
                                total_returned += 1;
                        }
                        if (total_returned == Integer.valueOf(txtTotCrew.getText().toString()))
                            close_pass();
                        else
                            Toast.makeText(getApplicationContext(), "Total returned crew not equal", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getApplicationContext(), "Returned Additional Crew must be equal to Went additional crew", Toast.LENGTH_LONG).show();
                }catch (NumberFormatException ne){
                    Toast.makeText(getApplicationContext(),"Returned Additional Crew cannot be blank",Toast.LENGTH_LONG).show();
                }

            }
        });
        btnRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler1 = new Handler();
                Log.d("RFID_button", "RFID Button clicked");
                Toast.makeText(getApplicationContext(), "TAP CARD", Toast.LENGTH_SHORT).show();
                show_progress();

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
                                    int ii=0;
                                    if(txtVesselNm.getText().toString().length()<3) {
                                        if(rfidCardNo30.startsWith("DOFV")){
                                            passNo="";
                                            if (RFID_vtek.verify_RFID_card(13, mifare_key, mifare_1k_card)) {
                                                passNo ="DOF"+ RFID_vtek.read_RFID_card(13, 0, mifare_1k_card).trim();
                                            }
                                            if(!passNo.equals("")){
                                                for (ii = 0; ii < lst_vessel_pass.size(); ii++) {
                                                    if (lst_vessel_pass.get(ii).equals(passNo)) {
                                                        load_pass_details();
                                                        dd_vessel.setVisibility(View.GONE);
                                                        findViewById(R.id.pc_vesselLabel).setVisibility(View.GONE);
                                                        break;
                                                    }
                                                }
                                                if(ii>=lst_vessel_pass.size()) {
                                                    Toast.makeText(getApplicationContext(), "VESSEL NOT FOUND", Toast.LENGTH_SHORT).show();
                                                }
                                                else {
                                                    Toast.makeText(getApplicationContext(),"SUCCESS",Toast.LENGTH_SHORT).show();
                                                    sword_chime.start();
                                                }
                                            }

                                        }
                                    }else{
                                        if(rfidCardNo30.startsWith("DOFC")){
                                            for (ii = 0; ii < crewArray.size(); ii++) {
                                                if (crewArray.get(ii).RFID.equals(rfidCardNo30)) {
                                                    crewArray.get(ii).checked = true;
                                                    crewAdapter.notifyDataSetChanged();
                                                    Toast.makeText(getApplicationContext(),"SUCCESS",Toast.LENGTH_SHORT).show();
                                                    sword_chime.start();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    hide_progress();
                                }
                            });

                            LED_Device.cancelBlink();
                            LED_Device.close();
                            RFID_Device.close();

                        }catch (Exception ex){
                            Log.d("RFID_button click",ex.getMessage());
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

    private void get_vessel_list() {
        show_progress();
        String url = GlobalVar.server_address + "/androidjson/pass_open.php";
        HttpsTrustManager.allowAllSSL();
        lst_vessel_disp.clear();
        lst_vessel_pass.clear();
        lst_vessel_stateId.clear();
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
                                            hide_progress();
                                            return;
                                        }
                                    } else {
                                        String T1 = Comfun.json_string(jo, "VesselNm");
                                        String T2 = Comfun.json_string(jo, "VRCNo");
                                        String PS = Comfun.json_string(jo, "PassNo");
                                        lst_vessel_pass.add(PS);
                                        lst_vessel_stateId.add(Comfun.json_string(jo,"state_id"));
                                        PS = PS.replace(PS.substring(PS.length()-5)," "+PS.substring(PS.length()-5));
                                        lst_vessel_disp.add(PS + "\n " +T1 + "\n " + T2.replace("MM", "MM "));
                                    }
                                }
                                spin_vessel = new SpinnerDialog(pass_close.this, lst_vessel_disp,
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
                                        dd_vessel.setText(item);
                                        passNo = lst_vessel_pass.get(position);

                                        load_pass_details();
                                    }
                                });

                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : " +
                                        "Check URL", Toast.LENGTH_SHORT).show();
                                pass_close.this.finish();
                            }
                            hide_progress();
                        } catch (Exception ex) {
                            hide_progress();

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                pass_close.this.finish();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user", GlobalVar.Muser);
                map.put("pdetails", "YES");
                map.put("stat", "CLOSING PASS");
                map.put("state_id", GlobalVar.state_id);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }


    private void close_pass() {
        show_progress();
        String tmpcrewId="", tmpCrewReturns = "";
        if(crewArray.size()>0) {
            for (CrewDisplayList item : crewArray) {
                tmpcrewId += item.id + ",";
                if (item.checked)
                    tmpCrewReturns += "1,";
                else
                    tmpCrewReturns += "0,";
            }
            tmpcrewId = tmpcrewId.substring(0, tmpcrewId.length() - 1);
            tmpCrewReturns = tmpCrewReturns.substring(0, tmpCrewReturns.length() - 1);
        }
        final String crewIds = tmpcrewId;
        final String crewReturns = tmpCrewReturns;
        final  int lost_crew_count = crewLost- Integer.parseInt( crewReturned.getText().toString())-crewArray.size();
        String url = GlobalVar.server_address + "/androidjson/fishpass/pass_close.php";
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
                                        Toast.makeText(getApplicationContext(), Comfun.json_string(jo, "message"), Toast.LENGTH_SHORT).show();
                                        if (val1 == 1) {
                                            pass_close.this.finish();

                                            return;
                                        }
                                    }

                                }
                                hide_progress();
                            } else {
                                pass_close.this.finish();
                                hide_progress();
                                return;
                            }
                        } catch (Exception ex) {
                            pass_close.this.finish();
                            hide_progress();
                            return;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pass_close.this.finish();
                hide_progress();
                return;

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user", GlobalVar.Muser);
                map.put("passno", passNo);
                map.put("CrewArrAdd", crewReturned.getText().toString());
                map.put("CrewLost", String.valueOf(lost_crew_count));
                map.put("CrewIDs", crewIds);
                map.put("CrewReturns", crewReturns);
                map.put("state_id", GlobalVar.state_id);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    private void load_pass_details( ) {
        show_progress();
        String url = GlobalVar.server_address + "/androidjson/fishpass/pass_close_details.php";
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            crewArray.clear();
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(), Comfun.json_string(jo, "message"), Toast.LENGTH_SHORT).show();
                                            pass_close.this.finish();
                                            return;
                                        }
                                    }
                                    else if (i == 1){
                                        txtVesselNm.setText(Comfun.json_string(jo, "vesselName"));
                                        txtPassNo.setText(Comfun.json_string(jo, "PassNo"));
                                        Date tmpDt = GlobalVar.ymdt.parse(Comfun.json_string(jo, "StrtTm"));
                                        txtPassDate.setText(GlobalVar.dmyt.format(tmpDt));
                                        txtCrewWent.setText(Comfun.json_string(jo, "CrewWentAdd"));
                                        txtTotCrew.setText(Comfun.json_string(jo, "TotCrew"));
                                        crewLost = Comfun.json_int(jo, "TotCrew");
                                    } else{
                                        crewArray.add(new CrewDisplayList(Comfun.json_string(jo, "ID"),
                                                Comfun.json_string(jo, "AadharNo"),
                                                Comfun.json_string(jo, "CrewNm"),"",
                                                Comfun.json_string(jo, "RFID"),
                                                Comfun.json_string(jo, "ElectionCard"),"",
                                                "", true,
                                                Comfun.json_string(jo,"CrwArr").equals("1")));
                                    }
                                }
                                crewAdapter.notifyDataSetChanged();
                                crewAdapter.full_table_copy();
                                hide_progress();

                            } else {
                                pass_close.this.finish();
                                return;
                            }
                        } catch (Exception ex) {
                            pass_close.this.finish();
                            return;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pass_close.this.finish();
                return;

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("passNo", passNo);
                map.put("state_id", GlobalVar.state_id);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation== Configuration.ORIENTATION_LANDSCAPE){
            saveButtonCover.setOrientation(LinearLayout.HORIZONTAL);
        }else{
            saveButtonCover.setOrientation(LinearLayout.VERTICAL);
        }
    }
    private void show_progress(){
        loading_bit += 1;
        progressview.setVisibility(View.VISIBLE);
        progressview.bringToFront();

    }

    private void hide_progress(){
        loading_bit -= 1;
        if (loading_bit <= 0) {
            loading_bit = 0;
            progressview.setVisibility(View.GONE);
        }
    }


}