package com.example.netrasagar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class reports extends AppCompatActivity {

    PieChart pie_catch_jetty, pie_pass_open, pie_pass_jetty;
    ArrayList<PieEntry> rec_catch_jetty = new ArrayList<>();
    ArrayList<PieEntry> rec_pass_open = new ArrayList<>();
    ArrayList<PieEntry> rec_pass_jetty = new ArrayList<>();
    PieDataSet ds_catch_jetty, ds_pass_open, ds_pass_jetty;
    PieData pd_catch_jetty, pd_pass_open, pd_pass_jetty;

    ArrayList<FishCatchList> fishCatchs = new ArrayList<>();
    RecyclerView fish_catch_recycler;
    RecyclerFishCatchAdapter fishCatchAdapter;

    RecyclerView detailsList_recycler;
    ArrayList<VesselPassList> vesselPassList = new ArrayList<>();
    RecyclerVesselAdapter vesselPassAdapter;
    ArrayList<FishCatchList> fishDetailList = new ArrayList<>();
    RecyclerFishCatchAdapter fishDetailAdapter;

    int loading_bit;

    LinearLayout progressview, detailsView;
    TextView detailsHeader;

    Spinner spn_period;
    ArrayList<String> lst_period = new ArrayList<>();
    ArrayAdapter<String> adp_period;

    String state_id;

    String date_from, date_to;
    SimpleDateFormat ymdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat dmyt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_reports);
        pie_catch_jetty = findViewById(R.id.ar_pie_jettywise_fishCatch);
        pie_pass_open = findViewById(R.id.ar_pie_pass_open);
        pie_pass_jetty = findViewById(R.id.ar_pie_passIssued);
        spn_period = findViewById(R.id.ar_dd_time);
        fish_catch_recycler = findViewById(R.id.ar_fishList);
        progressview = findViewById(R.id.rep_progressbar);
        detailsView = findViewById(R.id.rep_details);
        detailsList_recycler = findViewById(R.id.ar_detail_list);
        detailsHeader = findViewById(R.id.ar_detailsHeader);

        Intent intent = this.getIntent();
        state_id=intent.getStringExtra("state_id");

        lst_period.add("24 Hours");
        lst_period.add("48 Hours");
        lst_period.add("1 Week");
        lst_period.add("1 Month");
        lst_period.add("3 Months");
        lst_period.add("6 Months");

        adp_period = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lst_period);
        adp_period.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_period.setAdapter(adp_period);

        fish_catch_recycler.setLayoutManager(new LinearLayoutManager(this));
        detailsList_recycler.setLayoutManager(new LinearLayoutManager(this));

        fishCatchAdapter = new RecyclerFishCatchAdapter(this, fishCatchs);
        fish_catch_recycler.setAdapter(fishCatchAdapter);
        vesselPassAdapter = new RecyclerVesselAdapter(this, vesselPassList);
        fishDetailAdapter = new RecyclerFishCatchAdapter(this, fishDetailList);

        findViewById(R.id.ar_closeDetails).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailsView.setVisibility(View.GONE);
            }
        });

        set_pie_catch_jettywise();
        set_pie_pass_issued();
        set_pie_pass_open();

        get_pass_open_data();


        spn_period.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String selData = lst_period.get(pos);
                Calendar cal = Calendar.getInstance();
                if (selData.equals("24 Hours")) {
                    date_to = ymdt.format(cal.getTime());
                    cal.add(Calendar.HOUR,-24);
                    date_from = ymdt.format(cal.getTime() );
                } else if (selData.equals("48 Hours")) {
                    date_to = ymdt.format(cal.getTime());
                    cal.add(Calendar.HOUR,-48);
                    date_from = ymdt.format(cal.getTime() );
                } else if (selData.equals("1 Week")) {
                    date_to = ymdt.format(cal.getTime());
                    cal.add(Calendar.DATE,-7);
                    date_from = ymd.format(cal.getTime() );
                } else if (selData.equals("1 Month")) {
                    date_to = ymdt.format(cal.getTime());
                    cal.add(Calendar.MONTH,-1);
                    date_from = ymd.format(cal.getTime() );
                } else if (selData.equals("3 Months")) {
                    date_to = ymdt.format(cal.getTime());
                    cal.add(Calendar.MONTH,-3);
                    date_from = ymd.format(cal.getTime() );
                } else if (selData.equals("6 Months")) {
                    date_to = ymdt.format(cal.getTime());
                    cal.add(Calendar.MONTH,-6);
                    date_from = ymd.format(cal.getTime() );
                } else {
                    Toast.makeText(reports.this, "Module Not developed for selection.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                get_fish_catch_data(date_from, date_to);
                get_pass_issued_data(date_from, date_to);
                get_top_fish_catch(date_from, date_to);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    void get_fish_catch_data(String date_from, String date_to) {
        loading_bit += 1;
        progressview.setVisibility(View.VISIBLE);
        progressview.bringToFront();
        //String date_today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new
        // Date());
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST,
                GlobalVar.server_address + "/androidjson/fish_catch_total_jettywise.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;

                                rec_catch_jetty.clear();
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(), jo.getString(
                                                    "message"),
                                                    Toast.LENGTH_SHORT).show();
                                            reports.this.finish();
                                            return;
                                        }
                                    } else {
                                        rec_catch_jetty.add(new PieEntry(Comfun.json_int(jo,
                                                "totQty"), Comfun.json_string(jo, "Jetty")));

                                    }
                                }
                                pie_catch_jetty.notifyDataSetChanged();
                                pie_catch_jetty.invalidate();

                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                " URL",
                                        Toast.LENGTH_SHORT).show();
                                reports.this.finish();
                                return;
                            }
                        } catch (Exception ex) {

                        }
                        loading_bit -= 1;
                        if (loading_bit <= 0) {
                            loading_bit = 0;
                            progressview.setVisibility(View.GONE);
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
                map.put("fDt", date_from);
                map.put("tDt", date_to);
                map.put("state_id", state_id);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);


    }

    void get_fish_catch_details(String date_from, String date_to, String jetty_Name) {
        loading_bit += 1;
        progressview.setVisibility(View.VISIBLE);
        progressview.bringToFront();
        //String date_today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new
        // Date());
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST,
                GlobalVar.server_address + "/androidjson/fish_catch_total_jettywise.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;

                                fishDetailList.clear();
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(), jo.getString(
                                                    "message"),
                                                    Toast.LENGTH_SHORT).show();
                                            reports.this.finish();
                                            return;
                                        }
                                    } else {
                                        fishDetailList.add(new FishCatchList(Comfun.json_int(jo,"FishID"),
                                                Double.valueOf(Comfun.json_string(jo, "totQty")),0,
                                                Comfun.json_string(jo, "FishName")));
                                    }
                                }
                                fishDetailAdapter.notifyDataSetChanged();
                                detailsList_recycler.setAdapter(fishDetailAdapter);
                                detailsView.setVisibility(View.VISIBLE);

                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                " URL",
                                        Toast.LENGTH_SHORT).show();
                                reports.this.finish();
                                return;
                            }
                        } catch (Exception ex) {

                        }
                        loading_bit -= 1;
                        if (loading_bit <= 0) {
                            loading_bit = 0;
                            progressview.setVisibility(View.GONE);
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
                map.put("fDt", date_from);
                map.put("tDt", date_to);
                map.put("details", "YES");
                map.put("jety", jetty_Name);
                map.put("state_id", state_id);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);


    }

    void get_pass_issued_data(String date_from, String date_to) {
        loading_bit += 1;
        progressview.setVisibility(View.VISIBLE);
        progressview.bringToFront();

        //String date_today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new
        // Date());
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST,
                GlobalVar.server_address + "/androidjson/pass_issued_total_jettywise.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;

                                rec_pass_jetty.clear();
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(), jo.getString(
                                                    "message"),
                                                    Toast.LENGTH_SHORT).show();
                                            reports.this.finish();
                                            return;
                                        }
                                    } else {
                                        rec_pass_jetty.add(new PieEntry(Comfun.json_int(jo,
                                                "totQty"), Comfun.json_string(jo, "Jetty")));

                                    }
                                }
                                pie_pass_jetty.notifyDataSetChanged();
                                pie_pass_jetty.invalidate();

                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                " URL",
                                        Toast.LENGTH_SHORT).show();
                                reports.this.finish();
                                return;
                            }

                        } catch (Exception ex) {

                        }
                        loading_bit -= 1;
                        if (loading_bit <= 0) {
                            loading_bit = 0;
                            progressview.setVisibility(View.GONE);
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
                map.put("fDt", date_from);
                map.put("tDt", date_to);
                map.put("state_id", state_id);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);


    }

    void get_pass_issued_details(String date_from, String date_to, String jetty_Name) {
        loading_bit += 1;
        progressview.setVisibility(View.VISIBLE);
        progressview.bringToFront();

        //String date_today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new
        // Date());
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST,
                GlobalVar.server_address + "/androidjson/pass_issued_total_jettywise.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;

                                Boolean Vessel_status;
                                vesselPassList.clear();
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(), jo.getString(
                                                    "message"),
                                                    Toast.LENGTH_SHORT).show();
                                            reports.this.finish();
                                            return;
                                        }
                                    } else {
                                        String valTm = Comfun.ConvertDateFormat(Comfun.json_string(jo, "ValTm"),
                                                "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss");
                                        if (!valTm.trim().equals("")){
                                            Date cVal = dmyt.parse(valTm);
                                            Date today = Calendar.getInstance().getTime();
                                            if (cVal.compareTo(today)>0 && !Comfun.json_string(jo, "VoyageStatus").equals("CLOSED")){
                                                Vessel_status = false;
                                            }
                                            else {
                                                Vessel_status = true;
                                            }
                                        }
                                        else {
                                            Vessel_status = false;
                                        }

                                        Date tmpDt = GlobalVar.ymdt.parse(Comfun.json_string(jo, "StrtTm"));
                                        vesselPassList.add(new VesselPassList(Comfun.json_string(jo, "ID"),
                                                Comfun.json_string(jo, "VesselNm"),
                                                Comfun.json_string(jo, "VRCNo"),
                                                Comfun.json_string(jo, "VoyageStatus"),
                                                GlobalVar.dmyt.format(tmpDt),Vessel_status));
                                    }
                                }
                                vesselPassAdapter.notifyDataSetChanged();
                                detailsList_recycler.setAdapter(vesselPassAdapter);
                                detailsView.setVisibility(View.VISIBLE);

                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check URL",
                                        Toast.LENGTH_SHORT).show();
                                reports.this.finish();
                                return;
                            }

                        } catch (Exception ex) {

                        }
                        loading_bit -= 1;
                        if (loading_bit <= 0) {
                            loading_bit = 0;
                            progressview.setVisibility(View.GONE);
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
                map.put("fDt", date_from);
                map.put("tDt", date_to);
                map.put("pdetails", "YES");
                map.put("jety", jetty_Name);
                map.put("state_id", state_id);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);


    }

    void get_pass_open_data() {
        loading_bit += 1;
        progressview.setVisibility(View.VISIBLE);
        progressview.bringToFront();

        //String date_today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new
        // Date());
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST, GlobalVar.server_address +
                "/androidjson/pass_open.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;

                                rec_pass_open.clear();
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(), jo.getString(
                                                    "message"),
                                                    Toast.LENGTH_SHORT).show();
                                            reports.this.finish();
                                            return;
                                        }
                                    } else {
                                        rec_pass_open.add(new PieEntry(Comfun.json_int(jo,
                                                "TotPass"), Comfun.json_string(jo, "Stat")));

                                    }
                                }
                                pie_pass_open.notifyDataSetChanged();
                                pie_pass_open.invalidate();

                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                " URL",
                                        Toast.LENGTH_SHORT).show();
                                reports.this.finish();
                                return;
                            }
                        } catch (Exception ex) {

                        }
                        loading_bit -= 1;
                        if (loading_bit <= 0) {
                            loading_bit = 0;
                            progressview.setVisibility(View.GONE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("state_id", state_id);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);


    }

    void get_pass_open_details(String pass_status) {
        loading_bit += 1;
        progressview.setVisibility(View.VISIBLE);
        progressview.bringToFront();

        //String date_today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new
        // Date());
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST,
                GlobalVar.server_address + "/androidjson/pass_open.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;
                                Boolean Vessel_status;
                                if (pass_status.equals("VALIDITY OVER")) {
                                    Vessel_status = false;
                                } else {
                                    Vessel_status = true;
                                }
                                vesselPassList.clear();
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(), jo.getString(
                                                    "message"),
                                                    Toast.LENGTH_SHORT).show();
                                            reports.this.finish();
                                            return;
                                        }
                                    } else {
                                        Date tmpDt = GlobalVar.ymdt.parse(Comfun.json_string(jo, "StrtTm"));
                                        vesselPassList.add(new VesselPassList(Comfun.json_string(jo, "ID"),
                                                Comfun.json_string(jo, "VesselNm"),
                                                Comfun.json_string(jo, "VRCNo"),
                                                Comfun.json_string(jo, "Jetty_Name"),
                                                GlobalVar.dmyt.format(tmpDt), Vessel_status));
                                    }
                                }
                                vesselPassAdapter.notifyDataSetChanged();
                                detailsList_recycler.setAdapter(vesselPassAdapter);
                                detailsView.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                " URL",
                                        Toast.LENGTH_SHORT).show();
                                reports.this.finish();
                                return;
                            }
                        } catch (Exception ex) {

                        }
                        loading_bit -= 1;
                        if (loading_bit <= 0) {
                            loading_bit = 0;
                            progressview.setVisibility(View.GONE);
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
                map.put("pdetails", "YES");
                map.put("user", GlobalVar.Muser);
                map.put("stat", pass_status);
                map.put("state_id", GlobalVar.state_id);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);


    }

    void get_top_fish_catch(String date_from, String date_to) {
        loading_bit += 1;
        progressview.setVisibility(View.VISIBLE);
        progressview.bringToFront();
        String url = GlobalVar.server_address + "/androidjson/top_fish_catch.php";
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
                                            Intent intent = new Intent(reports.this,
                                                    FishCatchSummary.class);
                                            startActivity(intent);
                                            reports.this.finish();
                                            return;
                                        }
                                    } else {

                                        fishCatchs.add(new FishCatchList(Comfun.json_int(jo,
                                                "FishID"),
                                                Double.valueOf(Comfun.json_string(jo, "totQty")),0,
                                                Comfun.json_string(jo, "FishName")));

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
                        loading_bit -= 1;
                        if (loading_bit <= 0) {
                            loading_bit = 0;
                            progressview.setVisibility(View.GONE);
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
                map.put("fDt", date_from);
                map.put("tDt", date_to);
                map.put("state_id", state_id);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);

    }

    void set_pie_catch_jettywise() {
        //******Setting PieChart
        ds_catch_jetty = new PieDataSet(rec_catch_jetty, "");
        ds_catch_jetty.setColors(GlobalVar.CHART_COLORS);
        ds_catch_jetty.setValueFormatter(new ValueFormatter() {
            private final DecimalFormat format = new DecimalFormat("###");

            @Override
            public String getFormattedValue(float value) {
                return format.format(value);
            }
        });
        pd_catch_jetty = new PieData(ds_catch_jetty);
        pd_catch_jetty.setValueTextSize(14);
        pie_catch_jetty.setData(pd_catch_jetty);
        pie_catch_jetty.setDrawHoleEnabled(true);
        pie_catch_jetty.setHoleRadius(40);
        pie_catch_jetty.setHoleColor(Color.TRANSPARENT);
        pie_catch_jetty.setDrawEntryLabels(false);
        pie_catch_jetty.getDescription().setEnabled(false);
        pie_catch_jetty.setUsePercentValues(false);
        Legend l = pie_catch_jetty.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setMaxSizePercent(10);
        l.setTextSize(14);
        l.setXOffset(10);
        l.setTextColor(Color.WHITE);

        pie_catch_jetty.post(new Runnable() {
            @Override
            public void run() {
                float lo = pie_catch_jetty.getWidth();
                lo = 0 - (lo / 14);
                l.setXOffset(0);
                pie_catch_jetty.setExtraOffsets(lo, 0, 0, 0);
                pie_catch_jetty.animate();
            }
        });
        pie_catch_jetty.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                get_fish_catch_details(date_from, date_to, rec_catch_jetty.get((int) h.getX()).getLabel());
                detailsHeader.setText(rec_catch_jetty.get((int) h.getX()).getLabel());
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    void set_pie_pass_issued() {
        //******Setting PieChart
        ds_pass_jetty = new PieDataSet(rec_pass_jetty, "");
        ds_pass_jetty.setColors(GlobalVar.CHART_COLORS);
        ds_pass_jetty.setValueFormatter(new ValueFormatter() {
            private final DecimalFormat format = new DecimalFormat("###");

            @Override
            public String getFormattedValue(float value) {
                return format.format(value);
            }
        });
        pd_pass_jetty = new PieData(ds_pass_jetty);
        pd_pass_jetty.setValueTextSize(14);
        pie_pass_jetty.setData(pd_pass_jetty);
        pie_pass_jetty.setDrawHoleEnabled(true);
        pie_pass_jetty.setHoleRadius(40);
        pie_pass_jetty.setHoleColor(Color.TRANSPARENT);
        pie_pass_jetty.setDrawEntryLabels(false);
        pie_pass_jetty.getDescription().setEnabled(false);
        pie_pass_jetty.setUsePercentValues(false);
        Legend l = pie_pass_jetty.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setMaxSizePercent(10);
        l.setTextSize(14);
        l.setXOffset(10);
        l.setTextColor(Color.WHITE);

        pie_pass_jetty.post(new Runnable() {
            @Override
            public void run() {
                float lo = pie_pass_jetty.getWidth();
                lo = 0 - (lo / 14);
                l.setXOffset(0);
                pie_pass_jetty.setExtraOffsets(lo, 0, 0, 0);
                pie_pass_jetty.animate();
            }
        });
        pie_pass_jetty.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                get_pass_issued_details(date_from, date_to, rec_pass_jetty.get((int) h.getX()).getLabel());
                detailsHeader.setText(rec_pass_jetty.get((int) h.getX()).getLabel());
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    void set_pie_pass_open() {
        //******Setting PieChart
        ds_pass_open = new PieDataSet(rec_pass_open, "");
        ds_pass_open.setColors(GlobalVar.CHART_COLORS);
        ds_pass_open.setValueFormatter(new ValueFormatter() {
            private final DecimalFormat format = new DecimalFormat("###");

            @Override
            public String getFormattedValue(float value) {
                return format.format(value);
            }
        });
        pd_pass_open = new PieData(ds_pass_open);
        pd_pass_open.setValueTextSize(14);
        pie_pass_open.setData(pd_pass_open);
        pie_pass_open.setDrawHoleEnabled(true);
        pie_pass_open.setHoleRadius(40);
        pie_pass_open.setHoleColor(Color.TRANSPARENT);
        pie_pass_open.setDrawEntryLabels(false);
        pie_pass_open.getDescription().setEnabled(false);
        pie_pass_open.setUsePercentValues(false);
        Legend l = pie_pass_open.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setMaxSizePercent(10);
        l.setTextSize(14);
        l.setXOffset(10);
        l.setTextColor(Color.WHITE);

        pie_pass_open.post(new Runnable() {
            @Override
            public void run() {
                float lo = pie_pass_open.getWidth();
                lo = 0 - (lo / 14);
                l.setXOffset(0);
                pie_pass_open.setExtraOffsets(lo, 0, 0, 0);
                pie_pass_open.animate();
            }
        });

        pie_pass_open.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                get_pass_open_details(rec_pass_open.get((int) h.getX()).getLabel());
                detailsHeader.setText(rec_pass_open.get((int) h.getX()).getLabel());
            }

            @Override
            public void onNothingSelected() {

            }
        });

    }

}