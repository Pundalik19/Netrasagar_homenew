package com.example.netrasagar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudpos.DeviceException;
import com.cloudpos.POSTerminal;
import com.cloudpos.TerminalSpec;
import com.cloudpos.card.Card;
import com.cloudpos.led.LEDDevice;
import com.cloudpos.rfcardreader.RFCardReaderDevice;
import com.example.netrasagar.rfid.HexString;
import com.example.netrasagar.rfid.RFID_vtek;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class vessel extends AppCompatActivity {

    EditText txt_search;
    TextView txv_pages;
    Button btn_search, btn_next, btn_prev, btnRFID;
    ListView lv_vessel;

    ArrayList<String> holder = new ArrayList<>() ;

    int pageNo, totpage;
    String rfidCardNo30;

    private final byte[] mifare_key = HexString.hexToBuffer(GlobalVar.key);
    private Card mifare_1k_card;
    private RFCardReaderDevice RFID_Device = null;
    private LEDDevice LED_Device = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_vessel);

        txt_search=findViewById(R.id.txt_searchV);
        btn_search=findViewById(R.id.btn_searchV);
        lv_vessel=findViewById(R.id.lv_vessel);
        txv_pages=findViewById(R.id.txv_pagesV);
        btn_next=findViewById(R.id.btn_nextV);
        btn_prev=findViewById(R.id.btn_previousV);
        btnRFID = findViewById(R.id.btn_rfidV);

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


        pageNo=0;
        totpage=0;

        btnRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler1 = new Handler();
                //show_progress();
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
                                    if (rfidCardNo30.startsWith("DOFV")) {
                                        if (RFID_vtek.verify_RFID_card(12, mifare_key, mifare_1k_card)) {
                                            String vrc_no = RFID_vtek.read_RFID_card(12, 2, mifare_1k_card).trim();
                                            view_vessel_details(vrc_no + "-");
                                        }
                                    }else if (rfidCardNo30.startsWith("DOFC")) {
                                        if (RFID_vtek.verify_RFID_card(12, mifare_key, mifare_1k_card)) {
                                            String depStateId = RFID_vtek.read_RFID_card(12, 2, mifare_1k_card).trim();
                                            if (RFID_vtek.verify_RFID_card(2, mifare_key, mifare_1k_card)) {
                                                String AadharNo = RFID_vtek.read_RFID_card(2, 2, mifare_1k_card).trim();
                                                view_crew_details(AadharNo+"-",depStateId);
                                            }
                                        }
                                    }
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

                        }
                    }
                };

                Thread thread1 = new Thread(runnable1);
                thread1.start();


            }
        });



        lv_vessel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                view_vessel_details(((TextView) view).getText().toString());
            }
        });
    }

    private void view_vessel_details(String name){
        Intent intent = new Intent(vessel.this, vesseladdedit.class);
        intent.putExtra("name",name);
        startActivity(intent);
    }

    private void view_crew_details(String name, String state_id){
        Intent intent = new Intent(vessel.this, crewaddedit.class);
        intent.putExtra("name",name);
        intent.putExtra("state_id",state_id);
        startActivity(intent);
    }

    public void selectshow(View v){
        this.finish();
    }

    public void searchvessel(View v)   {
        pageNo=1;
        show_records();
    }

    public void nextpageV(View v)   {
        if (pageNo<totpage){
            pageNo += 1;
            show_records();
        }
    }

    public void prevpageV(View v)   {
        if (pageNo>1){
            pageNo -= 1;
            show_records();
        }
    }



    void show_records(){
        String url = GlobalVar.server_address +"/androidjson/vessellist.php";
        String srch = txt_search.getText().toString();
        String postval =  "?&p=1234&g="+ String.valueOf(pageNo)+"&s="+srch+"&u="+GlobalVar.Muser;

        class dbManager extends AsyncTask<String,Void,String>
        {
            protected void onPostExecute(String data){
                try{
                    if (data.contains("value") ){
                        JSONArray ja = new JSONArray(data);
                        JSONObject jo = null;
                        holder.clear();
                        for (int i = 0; i < ja.length(); i++) {
                            jo = ja.getJSONObject(i);
                            if(i==0){
                                if(jo.getString("value").equals("1")){
                                    int totrec = Integer.parseInt(jo.getString("records"));
                                    int pagerec = Integer.parseInt(jo.getString("pagerec"));
                                    if(totrec<=0){
                                        search_error("NO MATCHING RECORDS");
                                        return;
                                    }totpage = totrec / pagerec;
                                    if(totrec % pagerec > 0)
                                        totpage += 1;
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), jo.getString("message"), Toast.LENGTH_SHORT).show();
                                    search_error("ERROR");
                                    return;
                                }
                            }
                            else{
                                String name = jo.getString("AssetNm");
                                String vrc = jo.getString("AssetId");
                                holder.add(vrc + "- " + name);
                            }
                        }
                        ArrayAdapter at = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, holder);
                        lv_vessel.setAdapter(at);
                        txv_pages.setText(String.valueOf(pageNo) + " of " + String.valueOf(totpage));
                        btn_next.setVisibility(View.VISIBLE);
                        btn_prev.setVisibility(View.VISIBLE);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                        search_error("NO RECORDS");
                    }
                }catch (Exception ex){
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                    search_error("ERROR");
                }
            }

            @Override
            protected String doInBackground(String... param) {
                try {
                    URL url = new URL(param[0]);
                    HttpsTrustManager.allowAllSSL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuffer data = new StringBuffer();
                    String line;

                    while ((line=br.readLine())!=null){
                        data.append(line+"\n");
                    }
                    br.close();
                    return data.toString();
                }
                catch (Exception ex){
                    return ex.getMessage();
                }
            }
        }
        dbManager obj1 = new dbManager();
        obj1.execute(url+postval);

    }

    void search_error(String errStr){
        lv_vessel.setAdapter(null);
        txv_pages.setText(errStr);
        btn_next.setVisibility(View.INVISIBLE);
        btn_prev.setVisibility(View.INVISIBLE);
    }
}