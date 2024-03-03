package com.example.netrasagar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.cloudpos.led.LEDDevice;
import com.cloudpos.printer.Format;
import com.cloudpos.printer.PrinterDevice;
import com.cloudpos.rfcardreader.RFCardReaderDevice;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class print_pass extends AppCompatActivity {


    Button btn_print, btn_cancel;
    TextView confirmation;
    ConstraintLayout print_cover;
    String passNo, StateId;
    ArrayList<crewDetails> crewlist =new ArrayList<>();
    vesselDetails vesselData = new vesselDetails();

    LinearLayout progressview;
    int loading_bit=0;

    //******* POS Variable Ends ************//
    private PrinterDevice Printdevice = null;
    private LEDDevice LED_Device = null;
    //******* POS Variable Ends ************//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_pass);
        print_cover=findViewById(R.id.pp_printCover);
        print_cover.setVisibility(View.GONE);

        try {
            TerminalSpec pt = POSTerminal.getInstance(getApplicationContext()).getTerminalSpec();
            if( pt.getManufacturer().equals("wizarPOS") && !pt.getSerialNumber().equals("unknown") ){
                if (LED_Device == null) {
                    LED_Device = (LEDDevice) POSTerminal.getInstance(getApplicationContext())
                            .getDevice("cloudpos.device.led");
                    LED_Device.open(LEDDevice.ID_YELLOW);
                    LED_Device.turnOn();
                    if (Printdevice == null) {
                        Printdevice = (PrinterDevice) POSTerminal.getInstance(getApplicationContext())
                                .getDevice("cloudpos.device.printer");
                    }
                    LED_Device.turnOff();
                    LED_Device.close();
                    print_cover.setVisibility(View.VISIBLE);
                }
            }else{
                Toast.makeText(this,"This is not WizarPos",Toast.LENGTH_SHORT).show();
                print_pass.this.finish();
            }

        }catch (DeviceException e){
            Log.d("POS ERROR", e.getMessage());
            print_pass.this.finish();
        }
        btn_print = findViewById(R.id.pp_btnPrint);
        btn_cancel = findViewById(R.id.pp_btnCancel);
        progressview = findViewById(R.id.pp_progressbar);
        confirmation = findViewById(R.id.pp_confirmMessage);

        Intent intent = this.getIntent();
        passNo=intent.getStringExtra("passNo");
        StateId=intent.getStringExtra("StateId");
        confirmation.setText(getString(R.string.printConfirmMessage)+ " "+passNo+"?");



        btn_print.setOnClickListener(view -> {
            get_vessel_details();
            btn_cancel.setText(print_pass.this.getResources().getString(R.string.done));
        });
        btn_cancel.setOnClickListener(view -> print_pass.this.finish());

    }


    private void get_vessel_details() {
        show_progress();
        String url = GlobalVar.server_address + "/androidjson/fishpass/pass_close_details.php";
        HttpsTrustManager.allowAllSSL();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            vesselData = new vesselDetails();
                            crewlist.clear();
                            int ii=1;

                            if (response.contains("value")) {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo;
                                for (int i = 0; i < ja.length(); i++) {
                                    jo = ja.getJSONObject(i);
                                    if (i == 0) {
                                        int val1 = Integer.parseInt(jo.getString("value"));
                                        if (val1 == 0) {
                                            Toast.makeText(getApplicationContext(), Comfun.json_string(jo, "message"), Toast.LENGTH_SHORT).show();
                                            hide_progress();
                                            return;
                                        }
                                    }
                                    else if (i == 1){
                                        vesselData.name=Comfun.json_string(jo,"VesselNm");
                                        vesselData.vrc=Comfun.json_string(jo,"VRCNo");
                                        vesselData.passno=passNo;
                                        vesselData.fishing_licence=Comfun.json_string(jo,"fish_lic_no");
                                        vesselData.fishing_licence_validity=Comfun.json_string(jo,"fish_lic_valid_till");
                                        vesselData.society=Comfun.json_string(jo,"society_nm");
                                        vesselData.jetty=Comfun.json_string(jo,"jetty_nm");
                                        vesselData.owner= Comfun.json_string(jo,"owner_nm");
                                        vesselData.ownerContact=Comfun.json_string(jo,"owner_contact");
                                        vesselData.diesel=Comfun.json_string(jo,"Diesel");
                                        vesselData.lifeBouys=Comfun.json_string(jo,"LifeBouys");
                                        vesselData.lifeJackets=Comfun.json_string(jo,"LifeJackets");
                                        vesselData.gps=Comfun.json_string(jo,"GPS");
                                        vesselData.vhf=Comfun.json_string(jo,"VHF");
                                        vesselData.ais=Comfun.json_string(jo,"AIS");
                                        vesselData.departure=Comfun.json_string(jo,"StrtTm");
                                        vesselData.validity=Comfun.json_string(jo,"ValTm");
                                        vesselData.voyageDays=Comfun.json_string(jo,"Days");
                                        vesselData.crew_on_vessel=Comfun.json_string(jo,"CrewWent");
                                        vesselData.additional_crew=Comfun.json_string(jo,"CrewWentAdd");
                                        vesselData.fishermanLogin = Comfun.json_string(jo,"fisherman_open").equals("1");

                                    } else{
                                        crewlist.add(new crewDetails(ii,
                                                Comfun.json_string(jo,"RFID"),
                                                Comfun.json_string(jo,"AadharNo"),
                                                Comfun.json_string(jo,"ElectionCard"),
                                                Comfun.json_string(jo,"CrewNm")));
                                        ii++;
                                    }
                                }
                                Printdevice.open();
                                LED_Device.open(LEDDevice.ID_YELLOW);
                                LED_Device.startBlink(150,150);
                                if(print_vessel_pass()){
                                    if(crewlist.size()>0) {
                                        if (print_crew_pass()){
                                            print_pass_footer();
                                        }
                                    }else {
                                        Toast.makeText(getApplicationContext(),"No Crew to print",Toast.LENGTH_SHORT).show();
                                        print_pass_footer();
                                    }

                                }

                                LED_Device.cancelBlink();
                                LED_Device.close();
                                Printdevice.close();
                                hide_progress();
                            } else {
                                print_pass.this.finish();
                            }
                        } catch (Exception ex) {
                            print_pass.this.finish();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                print_pass.this.finish();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("passNo", passNo);
                map.put("state_id", StateId);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);


    }

    private boolean print_pass_footer() {
        try {
            if (Printdevice.queryStatus()==PrinterDevice.STATUS_PAPER_EXIST){
                Format format_data = new Format();
                format_data.setParameter(Format.FORMAT_FONT_SIZE,Format.FORMAT_FONT_SIZE_MEDIUM);
                if (StateId.equals("1")){
                    Printdevice.printText(format_data, "\nUntil and unless this pass is \nreturned new pass will not be \nissued.\n" +
                            "Issued By DIRECTORATE OF\nFISHERIES, Panaji Goa.\nTel - +91-0832 2425263");

                }else if (StateId.equals("2")){
                    Printdevice.printText(format_data, "\nUntil and unless this pass is \nreturned new pass will not be \nissued.\n" +
                            "Issued By DIRECTORATE OF\nFISHERIES, Kerala");

                }
                Printdevice.printText("\n\n\n\n\n\n\n");
                return true;
            }else
            {
                Toast.makeText(print_pass.this,  "!!! Paper not Present !!!", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (DeviceException e) {
            Log.d("POS PRINTING", e.toString());
            return false;
        }

    }

    private boolean print_crew_pass() {

        try
        {
            if (Printdevice.queryStatus()==PrinterDevice.STATUS_PAPER_EXIST)
            {
                Format format_header = new Format();
                format_header.setParameter(Format.FORMAT_FONT_SIZE,Format.FORMAT_FONT_SIZE_LARGE);
                format_header.setParameter(Format.FORMAT_ALIGN, Format.FORMAT_ALIGN_CENTER);
                //format_header.setParameter(Format.FORMAT_FONT_BOLD, Format.FORMAT_FONT_VAL_TRUE);
                Format format_data = new Format();
                format_data.setParameter(Format.FORMAT_FONT_SIZE,Format.FORMAT_FONT_SIZE_MEDIUM);

                Printdevice.printText(format_header, "\n\nCrew List\n");
                String tmpSrNoPad="";
                for(int i=0;i<crewlist.size();i++)
                {
                    if(crewlist.get(i).srno<10)
                        tmpSrNoPad=" ";
                    else
                        tmpSrNoPad="";


                    if (Printdevice.queryStatus()==PrinterDevice.STATUS_OUT_OF_PAPER){
                        Toast.makeText(print_pass.this,  "!!! Paper not Present !!!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Printdevice.printText(format_data, tmpSrNoPad + String.valueOf(crewlist.get(i).srno)+". "+crewlist.get(i).name +"\n");
                    Printdevice.printText(format_data, "    ("+crewlist.get(i).aadhar+")\n");
                }
                return true;

            }else
            {
                Toast.makeText(print_pass.this, "!!! Paper not Present !!!", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (DeviceException e) {
            Log.d("POS PRINTING", e.toString());
            return false;
        }

    }

    private boolean print_vessel_pass(){

        try
        {
            String tmptxt;
            String govt_name="";
            if (Printdevice.queryStatus()==PrinterDevice.STATUS_PAPER_EXIST)
            {   Bitmap bitmap = null;
                if (StateId.equals("1")) {
                    bitmap =BitmapFactory.decodeStream(getAssets().open("logo.png"));
                    govt_name=getString(R.string.governmentGoa);
                } else if (StateId.equals("2")) {
                    bitmap =BitmapFactory.decodeStream(getAssets().open("gkl.png"));
                    govt_name = getString(R.string.governmentKerala);
                }else
                    print_pass.this.finish();

                Format format_header = new Format();
                format_header.setParameter(Format.FORMAT_FONT_SIZE,Format.FORMAT_FONT_SIZE_LARGE);
                Format format_data = new Format();
                format_data.setParameter(Format.FORMAT_FONT_SIZE,Format.FORMAT_FONT_SIZE_MEDIUM);
                format_data.setParameter(Format.FORMAT_ALIGN, Format.FORMAT_ALIGN_CENTER);

                bitmap = Bitmap.createScaledBitmap(bitmap, 245, 250, false);
                Printdevice.printBitmap(format_data,bitmap);
                Printdevice.printText(format_data, getString(R.string.dof)+"\n"+govt_name);

                format_header.setParameter(Format.FORMAT_FONT_BOLD, Format.FORMAT_FONT_VAL_TRUE);
                format_header.setParameter(Format.FORMAT_ALIGN, Format.FORMAT_ALIGN_CENTER);
                Printdevice.printText(format_header, "FISHING PASS");

                format_header.setParameter(Format.FORMAT_ALIGN, Format.FORMAT_ALIGN_LEFT);
                format_header.setParameter(Format.FORMAT_FONT_SIZE,Format.FORMAT_FONT_SIZE_SMALL);
                format_data.setParameter(Format.FORMAT_ALIGN, Format.FORMAT_ALIGN_LEFT);
                format_data.setParameter(Format.FORMAT_FONT_SIZE,Format.FORMAT_FONT_SIZE_LARGE);

                Printdevice.printText(format_header, "Pass No :\n");
                Printdevice.printText(format_data, vesselData.passno);

                if (Printdevice.queryStatus()==PrinterDevice.STATUS_OUT_OF_PAPER){
                    Toast.makeText(print_pass.this,  "!!! Paper not Present !!!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Printdevice.printText(format_header, "Vessel Name :\n");
                Printdevice.printText(format_data, vesselData.name);

                Printdevice.printText(format_header, "Vessel Reg. No.:\n");
                Printdevice.printText(format_data, vesselData.vrc);

                Printdevice.printText(format_header, "Fishing Licence No.:\n");
                Printdevice.printText(format_data, vesselData.fishing_licence);


                tmptxt = Comfun.ConvertDateFormat(vesselData.fishing_licence_validity, "yyyy-MM-dd","dd-MM-yyyy");
                Printdevice.printText(format_header, "Fishing Licence Validity :\n");
                Printdevice.printText(format_data, tmptxt);

                format_data.setParameter(Format.FORMAT_FONT_SIZE,Format.FORMAT_FONT_SIZE_MEDIUM);
                Printdevice.printText(format_header, "Society Name :\n");
                Printdevice.printText(format_data, vesselData.society);

                Printdevice.printText(format_header, "Jetty Name :\n");
                Printdevice.printText(format_data, vesselData.jetty);

                Printdevice.printText(format_header, "Owner Name :\n");
                Printdevice.printText(format_data, vesselData.owner);

                if (Printdevice.queryStatus()==PrinterDevice.STATUS_OUT_OF_PAPER){
                    Toast.makeText(print_pass.this,  "!!! Paper not Present !!!", Toast.LENGTH_SHORT).show();
                    return false;
                }

                format_data.setParameter(Format.FORMAT_FONT_BOLD, Format.FORMAT_FONT_VAL_FALSE);
                Printdevice.printText(format_data, "Owner Contact  : "+vesselData.ownerContact+"\n");

                Printdevice.printText(format_data, "Crew On Vessel : "+vesselData.crew_on_vessel+"+"+vesselData.additional_crew+"\n");

                Printdevice.printText(format_data, "Life Jackets   : "+vesselData.lifeJackets+"\n");

                Printdevice.printText(format_data, "Life Bouys     : "+vesselData.lifeBouys+"\n");

                Printdevice.printText(format_data, "Diesel         : "+vesselData.diesel+"\n");

                Printdevice.printText(format_data, "GPS : "+vesselData.gps+" VHF : "+vesselData.vhf+" AIS : "+vesselData.ais+"\n");

                if (Printdevice.queryStatus()==PrinterDevice.STATUS_OUT_OF_PAPER){
                    Toast.makeText(print_pass.this,  "!!! Paper not Present !!!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                tmptxt = Comfun.ConvertDateFormat(vesselData.departure, "yyyy-MM-dd HH:mm:ss","dd-MM-yyyy HH:mm:ss");
                Printdevice.printText(format_header, "\nDeparture Time :\n");
                Printdevice.printText(format_data, vesselData.departure);

                tmptxt = Comfun.ConvertDateFormat(vesselData.validity, "yyyy-MM-dd HH:mm:ss","dd-MM-yyyy HH:mm:ss");
                Printdevice.printText(format_header, "Expected Arrival :\n");
                Printdevice.printText(format_data, vesselData.validity);

                Printdevice.printText(format_data, "Voyage Time : "+vesselData.voyageDays+" Days");
                return true;

            }else
            {
                Toast.makeText(print_pass.this,  "!!! Paper not Present !!!", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (DeviceException | IOException e) {
            Log.d("POS PRINTING", e.toString());
            return false;
        }
    }




    @Override
    protected void onStop() {
        try {
            Printdevice.close();
        } catch (DeviceException e) {
            Log.d("POS PRINTER", e.toString());
        }
        try {
            LED_Device.close();
        } catch (DeviceException e) {
            Log.d("POS LED", e.toString());
        }
        super.onStop();
    }

    private class vesselDetails {
        String passno, name, vrc, fishing_licence, fishing_licence_validity, society, jetty;
        String owner, ownerContact,  diesel, lifeBouys, lifeJackets, gps, vhf, ais;
        String departure, validity, voyageDays,crew_on_vessel,additional_crew;
        Boolean fishermanLogin;

    }
    private class crewDetails {
        String rfid, aadhar, electioncard, name;
        int srno;

        crewDetails(int srno,String rfid, String aadhar, String electioncard, String name) {
            this.srno=srno;
            this.rfid=rfid;
            this.aadhar = aadhar;
            this.electioncard=electioncard;
            this.name = name;
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