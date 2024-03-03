package com.example.netrasagar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class FishCatchAdd extends AppCompatActivity {

    SpinnerDialog dd_jetty, dd_vessel, dd_fish;
    TextView jettyTextView, vesselTextView, vesselName, vesselVRC, fishTextView;
    ConstraintLayout fish_catch_box;
    int jettyPosition, vesselPosition;
    EditText txt_qty;
    EditText txt_rate;
    Button btn_save;

    int fish_dd_Position;

    String fishCatchAssetID, collectDate;

    RecyclerView fish_catch_recycler;
    RecyclerFishCatchAdapter fishCatchAdapter;

    ArrayList<String> jettylist = new ArrayList<>();
    ArrayList<String> vessellist = new ArrayList<>();
    ArrayList<String> fishlist = new ArrayList<>();
    ArrayList<vesselDetails> vessels = new ArrayList<>();
    ArrayList<jettyDetails> jettys = new ArrayList<>();
    ArrayList<fishDetails> fishes = new ArrayList<>();

    ArrayList<FishCatchList> fishCatchs = new ArrayList<>();
    private String state_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_catch_add);
        getSupportActionBar().hide();
        Intent intent = this.getIntent();

        jettyTextView = findViewById(R.id.fc_dd_jetty);
        vesselTextView = findViewById(R.id.fc_dd_vessel);
        vesselName = findViewById(R.id.fc_vesselName);
        vesselVRC = findViewById(R.id.fc_vrc);
        fishTextView = findViewById(R.id.fc_dd_fish_select);
        txt_qty = findViewById(R.id.fc_qty);
        txt_rate = findViewById(R.id.fc_rate);
        fish_catch_recycler = findViewById(R.id.fc_fishList);
        fish_catch_box = findViewById(R.id.fc_fish_catch_box);
        btn_save = findViewById(R.id.fc_btn_save);

        fishCatchAssetID = intent.getStringExtra("fishCatchAssetID");
        collectDate = intent.getStringExtra("collectDate");
        state_id = intent.getStringExtra("state_id");
        if (fishCatchAssetID.equals("")) {
            get_jetty_list();
            get_fish_list();
        } else {
            jettyTextView.setEnabled(false);
            vesselTextView.setEnabled(false);
            fish_catch_box.setVisibility(View.GONE);
            get_fish_catch_data();
        }


        fish_dd_Position = -1;
        jettyPosition = -1;
        vesselPosition = -1;

        fish_catch_recycler.setLayoutManager(new LinearLayoutManager(this));

        fishCatchAdapter = new RecyclerFishCatchAdapter(this, fishCatchs);
        fish_catch_recycler.setAdapter(fishCatchAdapter);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fishCatchAssetID.equals("")) {
                    save_fishCatch();
                } else {
                    update_fishcatch();
                }
            }
        });

        txt_qty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b)
                    hideKeyboard(view);
            }
        });
        txt_rate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b)
                    hideKeyboard(view);
            }
        });

    }

    private void update_fishcatch() {
        if (fishCatchs.size() > 0 && !fishCatchAssetID.equals("")) {
            String ufishCatchID = "", uQty = "", urate="";
            for (int ii = 0; ii < fishCatchs.size(); ii++) {
                ufishCatchID += fishCatchs.get(ii).fish_catch_id + ",";
                uQty += fishCatchs.get(ii).qty + ",";
                urate += fishCatchs.get(ii).rate + ",";
            }
            String finalQty = uQty.substring(0, uQty.length() - 1);
            String finalRate = urate.substring(0, urate.length() - 1);
            String finalFishCatchId = ufishCatchID.substring(0, ufishCatchID.length() - 1);

            String url = GlobalVar.server_address + "/androidjson/fish_catch_edit.php";
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
                                                Intent intent = new Intent(FishCatchAdd.this,
                                                        FishCatchSummary.class);
                                                intent.putExtra("state_id",state_id);
                                                startActivity(intent);
                                                FishCatchAdd.this.finish();
                                                return;
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), jo.getString(
                                                    "message"),
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "CONNECTION ERROR : " +
                                                    "Check" +
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
                    map.put("fish_catch", finalFishCatchId);
                    map.put("quantity", finalQty);
                    map.put("rate", finalRate);
                    map.put("state_id", state_id);
                    return map;
                }
            };


            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);
        } else {
            Toast.makeText(this, "SOMETHING WENT WRONG.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FishCatchAdd.this, FishCatchSummary.class);
            intent.putExtra("state_id",state_id);
            startActivity(intent);
            FishCatchAdd.this.finish();

        }
    }

    private void get_fish_catch_data() {
        if (!fishCatchAssetID.equals("")) {
            String url = GlobalVar.server_address + "/androidjson/fish_catch_edit_data.php";
            HttpsTrustManager.allowAllSSL();
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                if (response.contains("value")) {
                                    JSONArray ja = new JSONArray(response);
                                    JSONObject jo;
                                    fishCatchs.clear();
                                    for (int i = 0; i < ja.length(); i++) {
                                        jo = ja.getJSONObject(i);
                                        if (i == 0) {
                                            int val1 = Integer.parseInt(jo.getString("value"));
                                            if (val1 == 0) {
                                                Toast.makeText(getApplicationContext(),
                                                        jo.getString("message"),
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(FishCatchAdd.this,
                                                        FishCatchSummary.class);
                                                intent.putExtra("state_id",state_id);
                                                startActivity(intent);
                                                FishCatchAdd.this.finish();
                                                return;
                                            }
                                        } else {
                                            if (i == 1) {
                                                jettyTextView.setText(Comfun.json_string(jo,
                                                        "jetty"));
                                                vesselTextView.setText(Comfun.json_string(jo,
                                                        "Vessel_Name"));
                                                vesselName.setText(Comfun.json_string(jo,
                                                        "Vessel_Name"));
                                                vesselVRC.setText(Comfun.json_string(jo, "AssetId"
                                                ));

                                            }
                                            fishCatchs.add(new FishCatchList(Comfun.json_int(jo,
                                                    "FishID"),
                                                    Double.valueOf(Comfun.json_string(jo, "Qty")),
                                                    Double.valueOf(Comfun.json_string(jo, "rate")),
                                                    Comfun.json_string(jo, "FishName"),
                                                    Comfun.json_string(jo, "ID")));

                                        }
                                    }
                                    fishCatchAdapter.notifyDataSetChanged();

                                } else {
                                    Toast.makeText(getApplicationContext(), "CONNECTION ERROR : " +
                                            "Check URL", Toast.LENGTH_SHORT).show();
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
                    map.put("fca", fishCatchAssetID);
                    map.put("cld", collectDate);
                    map.put("state_id", state_id);
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);

        }
    }

    public void select_jetty(View v) {
        dd_jetty.showSpinerDialog();

    }

    public void select_vessel(View v) {
        if (jettyPosition >= 0) {
            dd_vessel.showSpinerDialog();
        } else
            Toast.makeText(getApplicationContext(), "Select Jetty First", Toast.LENGTH_SHORT).show();

    }

    public void select_fishList(View view) {
        dd_fish.showSpinerDialog();

    }

    void get_jetty_list() {
        String url = GlobalVar.server_address + "/androidjson/jetty_list.php";
        HttpsTrustManager.allowAllSSL();
        jettylist.clear();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;
                                jettylist.clear();
                                jettys.clear();
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
                                        jettylist.add(T1 + " - " + T2);
                                    }
                                }
                                if (jettyPosition < 0) {
                                    jettyPosition = 0;
                                    jettyTextView.setText(jettylist.get(0));
                                    get_vesselList();
                                }
                                dd_jetty = new SpinnerDialog(FishCatchAdd.this, jettylist,
                                        "SELECT" +
                                                " JETTY",
                                        in.galaxyofandroid.spinerdialog.R.style.DialogAnimations_SmileWindow, "CLOSE");// With 	Animation
                                dd_jetty.setCancellable(true); // for cancellable
                                dd_jetty.setShowKeyboard(false);// for open keyboard by default
                                dd_jetty.setTitleColor(Color.GRAY);
                                dd_jetty.setSearchIconColor(Color.GRAY);
                                dd_jetty.setSearchTextColor(Color.BLACK);
                                dd_jetty.setItemColor(Color.BLACK);
                                dd_jetty.setItemDividerColor(Color.GRAY);
                                dd_jetty.setCloseColor(Color.GRAY);
                                dd_jetty.bindOnSpinerListener(new OnSpinerItemClick() {
                                    @Override
                                    public void onClick(String item, int position) {
                                        jettyPosition = position;
                                        jettyTextView.setText(item);
                                        get_vesselList();
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
                map.put("stateid", state_id);
                return map;
            }
        };


        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);

    }

    void get_vesselList() {
        if (jettyPosition >= 0) {
            String url = GlobalVar.server_address + "/androidjson/vessel_list_jetty_user.php";
            HttpsTrustManager.allowAllSSL();
            vessellist.clear();
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                if (response.contains("value")) {
                                    JSONArray ja = new JSONArray(response);
                                    JSONObject jo;
                                    vessellist.clear();
                                    vessels.clear();
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
                                            vessellist.add(T1 + "\n" + T2.replace("MM", "MM "));
                                            vessels.add(new vesselDetails(Comfun.json_string(jo,
                                                    "ID"), T1, T2));
                                        }
                                    }
                                    dd_vessel = new SpinnerDialog(FishCatchAdd.this, vessellist,
                                            "SELECT VESSEL",
                                            in.galaxyofandroid.spinerdialog.R.style.DialogAnimations_SmileWindow, "CLOSE");// With 	Animation
                                    dd_vessel.setCancellable(true); // for cancellable
                                    dd_vessel.setShowKeyboard(false);// for open keyboard by default
                                    dd_vessel.setTitleColor(Color.GRAY);
                                    dd_vessel.setSearchIconColor(Color.GRAY);
                                    dd_vessel.setSearchTextColor(Color.BLACK);
                                    dd_vessel.setItemColor(Color.BLACK);
                                    dd_vessel.setItemDividerColor(Color.GRAY);
                                    dd_vessel.setCloseColor(Color.GRAY);
                                    dd_vessel.bindOnSpinerListener(new OnSpinerItemClick() {
                                        @Override
                                        public void onClick(String item, int position) {
                                            vesselPosition = position;
                                            vesselTextView.setText(vessels.get(position).name +
                                                    "(" + vessels.get(position).vrc + ")");
                                            vesselName.setText(vessels.get(position).name);
                                            vesselVRC.setText(vessels.get(position).vrc);
                                        }
                                    });
                                } else {
                                    Toast.makeText(getApplicationContext(), "CONNECTION ERROR : " +
                                            "Check URL", Toast.LENGTH_SHORT).show();
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
                    map.put("j", jettys.get(jettyPosition).code);
                    map.put("fl", "1");
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);

        }
    }

    void get_fish_list() {
        String url="";
        try {
            url = GlobalVar.server_address + "/androidjson/fish_list.php"
                    + "?&s=" + URLEncoder.encode(state_id,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            FishCatchAdd.this.finish();
        }
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;
                                fishlist.clear();
                                fishes.clear();
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
                                        int T1 = Comfun.json_int(jo, "ID");
                                        String T2 = Comfun.json_string(jo, "FishName");
                                        fishes.add(new fishDetails(T1, T2));
                                        T2 = T2.replace("/", " / ");
                                        T2 = T2.replace(")", " ) ");
                                        fishlist.add(T2);
                                    }
                                }
                                dd_fish = new SpinnerDialog(FishCatchAdd.this, fishlist, "SELECT " +
                                        "Fish",
                                        in.galaxyofandroid.spinerdialog.R.style.DialogAnimations_SmileWindow, "CLOSE");// With 	Animation
                                dd_fish.setCancellable(true); // for cancellable
                                dd_fish.setShowKeyboard(false);// for open keyboard by default
                                dd_fish.setTitleColor(Color.GRAY);
                                dd_fish.setSearchIconColor(Color.GRAY);
                                dd_fish.setSearchTextColor(Color.BLACK);
                                dd_fish.setItemColor(Color.BLACK);
                                dd_fish.setItemDividerColor(Color.GRAY);
                                dd_fish.setCloseColor(Color.GRAY);
                                dd_fish.bindOnSpinerListener(new OnSpinerItemClick() {
                                    @Override
                                    public void onClick(String item, int position) {
                                        fish_dd_Position = position;
                                        fishTextView.setText(item);
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
        });


        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);

    }

    public void add_fish_catch(View view) {
        if (fish_dd_Position >= 0 && txt_qty.getText().toString().matches("-?\\d+(\\.\\d+)?") && txt_rate.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
            double qty = Double.valueOf(txt_qty.getText().toString());
            double rate = Double.valueOf(txt_rate.getText().toString());
            int exist_p = -1;
            for (int ii = 0; ii < fishCatchs.size(); ii++) {
                if (fishCatchs.get(ii).id == fishes.get(fish_dd_Position).id) {
                    exist_p = ii;
                    break;
                }
            }
            final int exist_pos = exist_p;
            if (exist_pos > -1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("UPDATE RECORD")
                        .setMessage("Do you want to update " + fishes.get(fish_dd_Position).name + " Qty to " + txt_qty.getText().toString() + "kg and Rate to "+txt_rate.getText().toString()+"?")
                        .setIcon(R.drawable.ic_refresh)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                fishCatchs.get(exist_pos).qty = qty;
                                fishCatchs.get(exist_pos).rate = rate;
                                fishCatchAdapter.notifyDataSetChanged();
                                fish_catch_recycler.scrollToPosition(exist_pos);
                                txt_rate.setText("");
                                txt_qty.setText("");
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                builder.show();

            } else {
                fishCatchs.add(new FishCatchList(fishes.get(fish_dd_Position).id, qty, rate,
                        fishes.get(fish_dd_Position).name));
                fishCatchAdapter.notifyItemInserted(fishCatchs.size() - 1);
                fish_catch_recycler.scrollToPosition(fishCatchs.size() - 1);
                txt_rate.setText("");
                txt_qty.setText("");
            }
        } else {
            Toast.makeText(this, "Select fishname and enter qty and Rate greater than 0",
                    Toast.LENGTH_LONG).show();
        }
    }

    void save_fishCatch() {
        if (fishCatchs.size() > 0 && jettyPosition >= 0 && vesselPosition >= 0 && fishCatchAssetID.equals("")) {
            String fishid = "", fishNm = "", qtys = "", rates = "";
            for (int ii = 0; ii < fishCatchs.size(); ii++) {
                fishNm += fishCatchs.get(ii).fishname + ",";
                qtys += fishCatchs.get(ii).qty + ",";
                rates += fishCatchs.get(ii).rate + ",";
                fishid += fishCatchs.get(ii).id + ",";
            }
            String finalQtys = qtys.substring(0, qtys.length() - 1);
            String finalRate = rates.substring(0, rates.length() - 1);
            String finalFishNm = fishNm.substring(0, fishNm.length() - 1);
            String finalFishId = fishid.substring(0, fishid.length() - 1);
            String finalDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

            String url = GlobalVar.server_address + "/androidjson/fish_catch_add.php";
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
                                                Intent intent = new Intent(FishCatchAdd.this,
                                                        FishCatchSummary.class);
                                                intent.putExtra("state_id",state_id);
                                                startActivity(intent);
                                                FishCatchAdd.this.finish();
                                                return;
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), jo.getString(
                                                    "message"),
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "CONNECTION ERROR : " +
                                                    "Check URL",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception ex) {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : " +
                                                ex.getMessage(),
                                        Toast.LENGTH_SHORT).show();
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
                    //map.put("addedit", "ADD");
                    map.put("u", GlobalVar.Muser);
                    map.put("jetty_name", jettys.get(jettyPosition).name);
                    map.put("jetty_code", jettys.get(jettyPosition).code);
                    map.put("assetno", vessels.get(vesselPosition).id);
                    map.put("asset_name", vessels.get(vesselPosition).name);
                    map.put("vrc", vessels.get(vesselPosition).vrc);
                    map.put("rec_date", finalDate);
                    map.put("fish_id", finalFishId);
                    map.put("fish_nm", finalFishNm);
                    map.put("quantity", finalQtys);
                    map.put("rate", finalRate);
                    map.put("state_id", state_id);
                    return map;
                }
            };


            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);
        } else {
            Toast.makeText(this, "Select Jetty, Vessel and Add atleast 1 catch.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void fc_cancel_event(View view) {
        Intent intent = new Intent(FishCatchAdd.this, FishCatchSummary.class);
        intent.putExtra("state_id",state_id);
        startActivity(intent);
        FishCatchAdd.this.finish();
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

    private class fishDetails {
        String name;
        int id;

        fishDetails(int id, String fishName) {
            this.id = id;
            this.name = fishName;

        }


    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}