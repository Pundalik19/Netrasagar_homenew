package com.example.netrasagar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class vesseladdedit extends AppCompatActivity {

    ImageView img_vessel;
    TextView vesselName, vrc, societyName, vesselType, owner, mobile, alternateNo, aadhar, fishLicenceValidity;
    Button btn_cancel;
    ImageButton mobileWhatsapp;
    URI imgVessel = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vesseladdedit);
        getSupportActionBar().hide();
        Intent intent = this.getIntent();

        vesselName=findViewById(R.id.txt_vesselNameV);
        vrc=findViewById(R.id.txt_vrcV);
        societyName=findViewById(R.id.txt_societyNameV);
        vesselType=findViewById(R.id.txt_vesselTypeV);
        owner=findViewById(R.id.txt_ownerNameV);
        mobile=findViewById(R.id.txt_mobileNoV);
        alternateNo=findViewById(R.id.txt_alternateNoV);
        aadhar=findViewById(R.id.txt_aadharV);
        fishLicenceValidity=findViewById(R.id.txt_fish_licence_validityV);
        mobileWhatsapp = findViewById(R.id.whatsapp_mobilNoV);

        img_vessel = findViewById(R.id.img_vesselV);

        btn_cancel=findViewById(R.id.btn_cancelV);


        img_vessel.setImageResource(R.drawable.trawler);

        String vrcNo = (intent.getStringExtra("name"));
        vrcNo = vrcNo.substring(0, vrcNo.indexOf("-"));
        get_vessel_data(vrcNo);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               vesseladdedit.this.finish();
            }
        });
        img_vessel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (imgVessel != null) {
                   Intent fulscreenIntent = new Intent(vesseladdedit.this, FullscreenActivity.class);
                   fulscreenIntent.putExtra("path", imgVessel.toString())
                           .putExtra("title", vesselName.getText().toString());
                   startActivity(fulscreenIntent);
               }
            }
        });
        mobileWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://api.whatsapp.com/send/";

                Uri uri = Uri.parse(url)
                        .buildUpon()
                        .appendQueryParameter("phone", "+91"+mobile.getText().toString())
                        .appendQueryParameter("text", " ")
                        .appendQueryParameter("app_absent", "1")
                        .build();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
            }
        });

    }



    void get_vessel_data(String vrc_no){
        String urlVesselDetails="";
        try {
            urlVesselDetails = GlobalVar.server_address + "/androidjson/vesselview.php?"
                    + "&u=" + URLEncoder.encode(GlobalVar.Muser,"utf-8")
                    + "&i=" + URLEncoder.encode(vrc_no,"utf-8");
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), "ERROR GENERATING URL" , Toast.LENGTH_SHORT).show();
            vesseladdedit.this.finish();
        }


        HttpsTrustManager.allowAllSSL();
        StringRequest request=new StringRequest(Request.Method.GET, urlVesselDetails,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String imgLoc="";
                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo = null;
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(), jo.getString("message"),
                                                    Toast.LENGTH_SHORT).show();
                                            vesseladdedit.this.finish();
                                            return;
                                        }
                                    } else {
                                        vesselName.setText(Comfun.json_string(jo, "AssetNm"));
                                        vrc.setText(Comfun.json_string(jo, "AssetId"));
                                        societyName.setText(Comfun.json_string(jo, "SocietyNm"));
                                        vesselType.setText(Comfun.json_string(jo, "Type"));
                                        owner.setText(Comfun.json_string(jo, "Owner"));
                                         mobile.setText(Comfun.json_string(jo, "ContactOwner"));
                                        alternateNo.setText(Comfun.json_string(jo, "AlternateContact"));
                                        aadhar.setText(Comfun.json_string(jo, "Aadhar"));
                                        String chklicence = Comfun.ConvertDateFormat(Comfun.json_string(jo, "fish_lic_valid_till"),
                                                "yyyy-MM-dd", "dd/MM/yyyy");
                                        if (!chklicence.trim().equals("")){
                                            Date valDt=new SimpleDateFormat("dd/MM/yyyy").parse(chklicence);
                                            if (Calendar.getInstance().getTime().compareTo(valDt)>0){
                                                fishLicenceValidity.setText(chklicence + " VALIDITY OVER");
                                                fishLicenceValidity.setTextColor(Color.RED);
                                            }
                                            else {
                                                fishLicenceValidity.setText(chklicence);
                                            }
                                        }
                                        else {
                                            fishLicenceValidity.setText("LICENCE NOT UPDATED");
                                            fishLicenceValidity.setTextColor(Color.RED);
                                        }


                                        imgLoc = Comfun.json_string(jo, "PhotoPath");

                                    }
                                }
                                if (imgLoc.trim().length() > 3) {
                                    ImageRequest imageRequest = new ImageRequest(imgLoc,
                                            new Response.Listener<Bitmap>() {
                                                @Override
                                                public void onResponse(Bitmap response) {
                                                    img_vessel.setForeground(null);
                                                    img_vessel.setImageBitmap(response);
                                                    try{
                                                        File file = new File(vesseladdedit.this.getCacheDir(), "tmpvessel.png");
                                                         if (file.exists()) {
                                                            file.delete();
                                                            file.createNewFile();
                                                         }
                                                        FileOutputStream fos = new FileOutputStream(file);
                                                        response.compress(Bitmap.CompressFormat.PNG,100,fos);
                                                        imgVessel = file.toURI();
                                                    }
                                                    catch (Exception ex){

                                                    }

                                                }
                                            }, 0, 0, ImageView.ScaleType.FIT_CENTER, null,
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Toast.makeText(getApplicationContext(), "CONNECTION ERROR" + error.toString(), Toast.LENGTH_LONG).show();

                                                }
                                            });
                                    MySingleton.getInstance(vesseladdedit.this).addToRequestQue(imageRequest);
                                }
                                else{
                                    img_vessel.setBackgroundResource(R.drawable.pic_border);
                                    imgVessel=null;
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "CONNECTION ERROR : Check URL" ,
                                        Toast.LENGTH_SHORT).show();
                                vesseladdedit.this.finish();
                                return;
                            }
                        }catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), ex.toString(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "CONNECTION ERROR : " + error.toString() ,
                                Toast.LENGTH_SHORT).show();
                        vesseladdedit.this.finish();
                        return;
                    }
                });

        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        queue.add(request);

    }
}