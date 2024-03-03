package com.example.netrasagar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class reprint_pass extends AppCompatActivity {

    Button btnCancel;
    LinearLayout progressview;

    RecyclerView VesselList;
    RecyclerVesselAdapter vesselPassAdapter;
    ArrayList<VesselPassList> vesselPassList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reprint_pass);
        progressview = findViewById(R.id.rp_progressbar);
        VesselList = findViewById(R.id.rp_vesselList);
        btnCancel = findViewById(R.id.rp_close);

        VesselList.setLayoutManager(new LinearLayoutManager(this));
        vesselPassAdapter = new RecyclerVesselAdapter(this, vesselPassList);
        VesselList.setAdapter(vesselPassAdapter);

        get_vessel_list();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reprint_pass.this.finish();
            }
        });

    }

    private void get_vessel_list() {
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

                                vesselPassList.clear();
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(), jo.getString(
                                                    "message"),
                                                    Toast.LENGTH_SHORT).show();
                                            reprint_pass.this.finish();
                                            return;
                                        }
                                    } else {
                                        vesselPassList.add(new VesselPassList(Comfun.json_string(jo, "ID"),
                                                Comfun.json_string(jo, "VesselNm"),
                                                Comfun.json_string(jo, "VRCNo"),
                                                Comfun.json_string(jo, "Jetty_Name"),
                                                Comfun.json_string(jo, "StrtTm"), true,
                                                Comfun.json_string(jo, "PassNo"),
                                                Comfun.json_string(jo, "state_id")));
                                    }
                                }
                                vesselPassAdapter.notifyDataSetChanged();
                                progressview.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                " URL",
                                        Toast.LENGTH_SHORT).show();
                                reprint_pass.this.finish();
                                return;
                            }
                        } catch (Exception ex) {
                            reprint_pass.this.finish();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                reprint_pass.this.finish();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("pdetails", "YES");
                map.put("stat", "OPEN VALID");
                map.put("user", GlobalVar.Muser);
                map.put("state_id", GlobalVar.state_id);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }


}