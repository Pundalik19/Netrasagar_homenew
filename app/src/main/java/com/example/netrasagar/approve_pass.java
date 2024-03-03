package com.example.netrasagar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class approve_pass extends AppCompatActivity {

    TextView vesselName, passNo, passIssuedon, passValDt, diesel,
            crewCount, voyageStatus, passIssuedBy, dd_pass;
    EditText remarks, lifeBouys, voyageDay, lifeJacket;
    Button btn_approve, btn_reject;
    SpinnerDialog spin_pass;
    ArrayList<String> lst_pass_disp = new ArrayList<>();
    ArrayList<String> lst_pass_id = new ArrayList<>();
    LinearLayout progressview;

    int loading_bit=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_pass);
        getSupportActionBar().hide();


        vesselName = findViewById(R.id.ap_vesselName);
        passNo = findViewById(R.id.ap_passNo);
        passIssuedon = findViewById(R.id.ap_issDate);
        passValDt = findViewById(R.id.ap_validityDt);
        diesel = findViewById(R.id.ap_diesel);
        lifeBouys = findViewById(R.id.ap_lifeBouys);
        lifeJacket = findViewById(R.id.ap_lifeJackets);
        remarks = findViewById(R.id.ap_remarks);
        voyageDay = findViewById(R.id.ap_voyageDays);
        crewCount = findViewById(R.id.ap_crewCount);
        voyageStatus = findViewById(R.id.ap_voyageStatus);
        passIssuedBy = findViewById(R.id.ap_passIssueUser);
        progressview = findViewById(R.id.ap_progressbar);
        btn_approve = findViewById(R.id.ap_btnApprove);
        btn_reject = findViewById(R.id.ap_btnReject);
        dd_pass = findViewById(R.id.ap_selPass);

        dd_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spin_pass.showSpinerDialog();
            }
        });
        btn_approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                approve_reject_pass(passNo.getText().toString(),"APPROVED");
            }
        });
        btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                approve_reject_pass(passNo.getText().toString(),"REJECTED");
            }
        });
        get_pass_list();

    }

    void get_pass_list(){
        show_progress();
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
                                lst_pass_disp.clear();
                                lst_pass_id.clear();
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(), jo.getString(
                                                    "message"),
                                                    Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), sel_option.class);
                                            startActivity(intent);
                                            approve_pass.this.finish();
                                            return;
                                        }
                                    } else {
                                        lst_pass_disp.add(Comfun.json_string(jo, "PassNo").replace("/","/ ") +"\n"
                                                + Comfun.json_string(jo,"VesselNm")+"\n"
                                                + Comfun.json_string(jo,"VRCNo").replace("MM","MM ") );
                                        lst_pass_id.add(Comfun.json_string(jo, "PassNo"));
                                    }
                                }

                                spin_pass = new SpinnerDialog(approve_pass.this, lst_pass_disp,
                                        "SELECT VESSEL",
                                        in.galaxyofandroid.spinerdialog.R.style.DialogAnimations_SmileWindow, "CLOSE");// With 	Animation
                                spin_pass.setCancellable(true); // for cancellable
                                spin_pass.setShowKeyboard(false);// for open keyboard by default
                                spin_pass.setTitleColor(Color.GRAY);
                                spin_pass.setSearchIconColor(Color.GRAY);
                                spin_pass.setSearchTextColor(Color.BLACK);
                                spin_pass.setItemColor(Color.BLACK);
                                spin_pass.setItemDividerColor(Color.GRAY);
                                spin_pass.setCloseColor(Color.GRAY);
                                spin_pass.bindOnSpinerListener(new OnSpinerItemClick() {
                                    @Override
                                    public void onClick(String item, int position) {
                                        dd_pass.setText(item);
                                        get_pass_data(lst_pass_id.get(position));
                                    }
                                });
                                dd_pass.setText(lst_pass_disp.get(0));
                                get_pass_data(lst_pass_id.get(0));

                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                " URL",
                                        Toast.LENGTH_SHORT).show();
                            }
                            hide_progress();
                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), "Check internet connection",
                                    Toast.LENGTH_SHORT).show();
                            hide_progress();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                hide_progress();
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

    private void get_pass_data(String pass_no) {
        show_progress();
        String url="";
        if (GlobalVar.state_id.equals("1"))
         url = GlobalVar.server_address + "/fish_pass/pass_details.php";
        else if (GlobalVar.state_id.equals("2"))
            url = GlobalVar.server_address + "/fish_pass_kl/pass_details.php";
        else
            approve_pass.this.finish();


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
                                        }
                                    } else {
                                        vesselName.setText(Comfun.json_string(jo, "VesselNm")+"\n"+Comfun.json_string(jo, "VRCNo"));
                                        passNo.setText(Comfun.json_string(jo, "PassNo"));
                                        Date tmpDt = GlobalVar.ymdt.parse(Comfun.json_string(jo, "StrtTm"));
                                        passIssuedon.setText(GlobalVar.dmyt.format(tmpDt));
                                        tmpDt = GlobalVar.ymdt.parse(Comfun.json_string(jo, "ValTm"));
                                        passValDt.setText(GlobalVar.dmyt.format(tmpDt));
                                        diesel.setText(Comfun.json_string(jo, "Diesel"));
                                        lifeBouys.setText(Comfun.json_string(jo, "LifeBouys"));
                                        lifeJacket.setText(Comfun.json_string(jo, "LifeJackets"));
                                        voyageDay.setText(Comfun.json_string(jo, "Days"));
                                        crewCount.setText(Comfun.json_string(jo, "CrewWent")+" + "+Comfun.json_string(jo, "CrewWentAdd"));
                                        voyageStatus.setText(Comfun.json_string(jo, "VoyageStatus"));
                                        passIssuedBy.setText(Comfun.json_string(jo, "UsrOpen"));

                                    }
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check" +
                                                " URL",
                                        Toast.LENGTH_SHORT).show();
                            }
                            hide_progress();
                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), "Check internet connection",
                                    Toast.LENGTH_SHORT).show();
                            hide_progress();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                hide_progress();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("loginid", GlobalVar.Muser);
                map.put("passno", pass_no);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    private void approve_reject_pass(String pass_no, String apprv) {
        show_progress();
        String url="";
        if(GlobalVar.state_id.equals("1"))
        url = GlobalVar.server_address + "/fish_pass/pass_approve.php";
        else if (GlobalVar.state_id.equals("2"))
            url = GlobalVar.server_address + "/fish_pass_kl/pass_approve.php";

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
                                        Toast.makeText(getApplicationContext(), jo.getString("message"),
                                                Toast.LENGTH_SHORT).show();
                                        if (val1 == 1) {
                                            get_pass_list();
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check URL",
                                        Toast.LENGTH_SHORT).show();
                            }
                            hide_progress();
                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), "Check internet connection",
                                    Toast.LENGTH_SHORT).show();
                            hide_progress();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hide_progress();
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("loginid", GlobalVar.Muser);
                map.put("passno", pass_no);
                map.put("app", apprv);
                map.put("valdays", voyageDay.getText().toString());
                map.put("lifej", lifeJacket.getText().toString());
                map.put("lifeb", lifeBouys.getText().toString());
                map.put("remarks",remarks.getText().toString());
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
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