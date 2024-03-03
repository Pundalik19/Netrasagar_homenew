package com.example.netrasagar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;


public class sel_option extends AppCompatActivity {

    Button btn_depart, btn_arrive, btn_fishcatch, btn_fishcatch_kl, btn_report, btn_report_kl, btn_crew, btn_vessel, btn_fishlicence;
    Button btn_approvePass, btn_reprint_pass, approvePassCount;
    ImageButton btn_logout;

    TextView txtv_welcom;

    DBHelper DB;




    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sel_option);
        getSupportActionBar().hide();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        txtv_welcom=findViewById(R.id.txtv_welcom);

        btn_arrive=findViewById(R.id.btn_arrival);
        btn_depart=findViewById(R.id.btn_depart);
        btn_fishcatch =  findViewById(R.id.btn_fishcatch);
        btn_fishcatch_kl =  findViewById(R.id.btn_fishcatch_kl);
        btn_report=findViewById(R.id.btn_report);
        btn_report_kl=findViewById(R.id.btn_report_kl);
        btn_crew=findViewById(R.id.btn_crewentry);
        btn_vessel=findViewById(R.id.btn_vesselentry);
        btn_fishlicence=findViewById(R.id.btn_licence_renewal);
        btn_logout=findViewById(R.id.btn_logout);
        btn_approvePass=findViewById(R.id.btn_approvepass);
        approvePassCount=findViewById(R.id.approvePassCount);
        btn_reprint_pass=findViewById(R.id.btn_reprint_pass);



        DB = new DBHelper(this);
        txtv_welcom.setText(getString(R.string.welcome)+" "+GlobalVar.User_FullName);
        if (!GlobalVar.crew_view)
            btn_crew.setVisibility(View.GONE);
        if (!GlobalVar.vessel_view)
            btn_vessel.setVisibility(View.GONE);
        if (!GlobalVar.fish_catch_view || !GlobalVar.state_id.contains("1"))
            btn_fishcatch.setVisibility(View.GONE);
        if (!GlobalVar.fish_catch_view || !GlobalVar.state_id.contains("2"))
            btn_fishcatch_kl.setVisibility(View.GONE);

        if (!GlobalVar.fish_licence_renewal)
            btn_fishlicence.setVisibility(View.GONE);
        if (!GlobalVar.view_report || !GlobalVar.state_id.contains("1"))
            btn_report.setVisibility(View.GONE);
        if (!GlobalVar.view_report || !GlobalVar.state_id.contains("2"))
            btn_report_kl.setVisibility(View.GONE);

        if (!GlobalVar.view_approvePass )
            btn_approvePass.setVisibility(View.GONE);
        else{
            get_pending_pass_count();
        }
        if (!GlobalVar.pass_open) {
            btn_depart.setVisibility(View.GONE);
            btn_reprint_pass.setVisibility(View.GONE);

        }
        if (!GlobalVar.pass_close)
            btn_arrive.setVisibility(View.GONE);


        if(!GlobalVar.server_address.equals("https://netrasagar.in")){
            txtv_welcom.setText("TEST APPLICATION");
        }

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB.write_constant("Keep_Signed", "");
                Intent intent = new Intent(sel_option.this, MainActivity.class);
                startActivity(intent);
                sel_option.this.finish();
            }
        });
        btn_depart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sel_option.this, pass_open.class);
                startActivity(intent);
            }
        });
        btn_reprint_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sel_option.this, reprint_pass.class);
                startActivity(intent);
            }
        });
        btn_arrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sel_option.this, pass_close.class);
                startActivity(intent);
            }
        });
        btn_crew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sel_option.this, crew.class);
                startActivity(intent);
            }
        });
        btn_vessel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sel_option.this, vessel.class);
                startActivity(intent);
            }
        });
        btn_fishcatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sel_option.this, FishCatchSummary.class);
                intent.putExtra("state_id","1");
                startActivity(intent);
            }
        });
        btn_fishcatch_kl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sel_option.this, FishCatchSummary.class);
                intent.putExtra("state_id","2");
                startActivity(intent);
            }
        });
        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sel_option.this, reports.class);
                intent.putExtra("state_id","1");
                startActivity(intent);
            }
        });
        btn_report_kl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sel_option.this, reports.class);
                intent.putExtra("state_id","2");
                startActivity(intent);
            }
        });
        btn_fishlicence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sel_option.this, Licence_Renewal.class);
                startActivity(intent);
            }
        });
        btn_approvePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sel_option.this, approve_pass.class);
                startActivity(intent);
            }
        });

    }

    private void get_pending_pass_count() {

        approvePassCount.setVisibility(View.GONE);
        String url = "";
        if(GlobalVar.state_id.equals("1"))
            url=GlobalVar.server_address + "/fish_pass/approve_list.php";
        else if(GlobalVar.state_id.equals("2"))
            url=GlobalVar.server_address + "/fish_pass_kl/approve_list.php";
        else
            return;

        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;
                                jo = ja.getJSONObject(0);
                                int val1 = Integer.parseInt(jo.getString("value"));
                                if (val1 == 1) {
                                    if(ja.length()>1){
                                        approvePassCount.setText(String.valueOf(ja.length()-1));
                                        approvePassCount.setVisibility(View.VISIBLE);
                                    }
                                    return;
                                }

                            }


                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), "Check internet connection",
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
                map.put("loginid", GlobalVar.Muser);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        get_pending_pass_count();
    }
}