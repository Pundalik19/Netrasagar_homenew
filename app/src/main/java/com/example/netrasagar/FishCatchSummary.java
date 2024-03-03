package com.example.netrasagar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class FishCatchSummary extends AppCompatActivity {

    PieChart pie_todays_catch;
    ArrayList<PieEntry> records = new ArrayList<>();
    PieDataSet dataSet;
    PieData pieData;

    String state_id;

    SpinnerDialog dd_fishcatch;
    ArrayList<String> fishCatchDisplay = new ArrayList<>();
    ArrayList<fishCatchDetails> fishCatchAssetID = new ArrayList<>();

      Button btn_add, btn_edit;

    SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_catch_summary);
        getSupportActionBar().hide();
        pie_todays_catch=findViewById(R.id.pie_todays_Catch);
        btn_add=findViewById(R.id.btn_add_fish);
        btn_edit=findViewById(R.id.btn_edit_fish);

        Intent intent = this.getIntent();
        state_id = intent.getStringExtra("state_id");

        get_fish_catch_data();

       //******Setting PieChart
        dataSet= new PieDataSet(records,"");
        dataSet.setColors(GlobalVar.CHART_COLORS);
        dataSet.setValueFormatter(new ValueFormatter() {
            private final DecimalFormat format = new DecimalFormat("###");
            @Override
            public String getFormattedValue(float value) {
                return format.format(value);
            }
        });
        pieData = new PieData(dataSet);
        pieData.setValueTextSize(14);
        pie_todays_catch.setData(pieData);
        pie_todays_catch.setDrawHoleEnabled(false);
        pie_todays_catch.setDrawEntryLabels(false);
        pie_todays_catch.getDescription().setEnabled(false);
        pie_todays_catch.setUsePercentValues(false);
        Legend l = pie_todays_catch.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setMaxSizePercent(10);
        l.setTextSize(14);
        l.setXOffset(10);
        l.setTextColor(Color.WHITE);

        pie_todays_catch.post(new Runnable() {
            @Override
            public void run() {
                float lo = pie_todays_catch.getWidth();
                 lo = 0- (lo/14);
                l.setXOffset(0);
                pie_todays_catch.setExtraOffsets(lo,0,0,0);
                pie_todays_catch.animate();
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FishCatchSummary.this, FishCatchAdd.class);
                intent.putExtra("fishCatchAssetID","");
                intent.putExtra("state_id",state_id);
                startActivity(intent);
                FishCatchSummary.this.finish();
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_fish_catch_list();
            }
        });


    }



     void get_fish_catch_data(){
        Calendar cal = Calendar.getInstance();
         String date_today = ymd.format(cal.getTime());
         cal.add(Calendar.DATE,-1);
         String date_yesterday = ymd.format(cal.getTime());

         //String date_today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        HttpsTrustManager.allowAllSSL();
        StringRequest request=new StringRequest(Request.Method.POST, GlobalVar.server_address+"/androidjson/fish_catch_total_jettywise.php",
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                try{
                    if (response.contains("value")) {
                        JSONArray ja = new JSONArray(response);
                        JSONObject jo ;

                        records.clear();
                        for (int i = 0; i < ja.length(); i++) {
                            jo = ja.getJSONObject(i);
                            if (i == 0) {
                                int val1 = Integer.parseInt(jo.getString("value"));
                                if (val1 == 0) {
                                    Toast.makeText(getApplicationContext(), jo.getString("message"),
                                            Toast.LENGTH_SHORT).show();
                                    FishCatchSummary.this.finish();
                                    return;
                                }
                            } else {
                                records.add(new PieEntry( Comfun.json_int(jo,"totQty"), Comfun.json_string(jo, "Jetty")));

                            }
                        }
                        pie_todays_catch.notifyDataSetChanged();
                        pie_todays_catch.invalidate();

                    }
                    else {
                        Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check URL" ,
                                Toast.LENGTH_SHORT).show();
                        FishCatchSummary.this.finish();
                        return;
                    }
                }
                catch (Exception ex){

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String,String> map=new HashMap<String, String>();
                map.put("fDt",date_yesterday);
                map.put("tDt",date_today);
                map.put("state_id", state_id);
                return map;
            }
        };


        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        queue.add(request);


    }

    void get_fish_catch_list(){
        String url = GlobalVar.server_address + "/androidjson/fish_catch_entry_editlist.php";
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;
                                fishCatchAssetID.clear();
                                fishCatchDisplay.clear();
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
                                        fishCatchAssetID.add(new fishCatchDetails( Comfun.json_string(jo, "AssetId"),Comfun.json_string(jo, "Collection_date")));
                                        fishCatchDisplay.add(Comfun.json_string(jo, "Vessel_Name") + "\n" +
                                                Comfun.json_string(jo, "AssetId") + "\n" +
                                                "Qty = " + Comfun.json_string(jo, "totQty") + "KG");

                                    }
                                }

                                dd_fishcatch = new SpinnerDialog(FishCatchSummary.this, fishCatchDisplay,
                                        "SELECT FISH CATCH",
                                        in.galaxyofandroid.spinerdialog.R.style.DialogAnimations_SmileWindow, "CLOSE");// With 	Animation
                                dd_fishcatch.setCancellable(true); // for cancellable
                                dd_fishcatch.setShowKeyboard(false);// for open keyboard by default
                                dd_fishcatch.setTitleColor(Color.GRAY);
                                dd_fishcatch.setSearchIconColor(Color.GRAY);
                                dd_fishcatch.setSearchTextColor(Color.BLACK);
                                dd_fishcatch.setItemColor(Color.BLACK);
                                dd_fishcatch.setItemDividerColor(Color.GRAY);
                                dd_fishcatch.setCloseColor(Color.GRAY);
                                dd_fishcatch.bindOnSpinerListener(new OnSpinerItemClick() {
                                    @Override
                                    public void onClick(String item, int position) {
                                        Intent intent = new Intent(FishCatchSummary.this, FishCatchAdd.class);
                                        intent.putExtra("fishCatchAssetID",fishCatchAssetID.get(position).vesselId);
                                        intent.putExtra("collectDate",fishCatchAssetID.get(position).collectionDate);
                                        intent.putExtra("state_id",state_id);
                                        startActivity(intent);
                                        FishCatchSummary.this.finish();
                                    }
                                });
                                dd_fishcatch.showSpinerDialog();

                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                " URL", Toast.LENGTH_SHORT).show();
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
                map.put("state_id", state_id);
                return map;
            }
        };


        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    private class fishCatchDetails {
        String vesselId, collectionDate;

        fishCatchDetails(String vesselId, String collectionDate) {
            this.vesselId = vesselId;
            this.collectionDate = collectionDate;

        }
    }

}