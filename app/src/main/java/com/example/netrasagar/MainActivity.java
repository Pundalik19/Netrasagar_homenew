package com.example.netrasagar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dcastalia.localappupdate.DownloadApk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_STORAGE_CODE = 1000;
    Button btn_login, btn_update;
    EditText txt_user, txt_pass;
    CheckBox chk_Keepsigned;
    //String stringIMEI;
    TextView version;


    DBHelper DB;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DB = new DBHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        version = findViewById(R.id.txt_version);
        version.setText(BuildConfig.VERSION_NAME);

        DB.CREATETABLES();

        GlobalVar.Muser = DB.read_constant("Keep_Signed");
        if (GlobalVar.Muser.equals("") == false) {
            findViewById(R.id.ma_login_page).setVisibility(View.GONE);
            findViewById(R.id.ma_progress).setVisibility(View.VISIBLE);
            String Passswrd = DB.read_constant("Keep_Signed_pass");
            String url = GlobalVar.server_address + "/androidjson/user_auth.php";
            try {
                String postval = "?&p=" + URLEncoder.encode(GlobalVar.phppass, "utf-8") +
                        "&u=" + URLEncoder.encode(GlobalVar.Muser, "utf-8") +
                        "&w=" + URLEncoder.encode(Passswrd, "utf-8");

                class dbManager extends AsyncTask<String, Void, String> {
                    protected void onPostExecute(String data) {
                        try {
                            if (data.contains("value")) {
                                JSONArray ja = new JSONArray(data);
                                JSONObject jo = ja.getJSONObject(0);
                                if (jo.getString("value").equals("1")) {
                                    String msgs = jo.getString("message");
                                    jo = ja.getJSONObject(1);
                                    if(Comfun.json_int(jo, "app_version") == BuildConfig.VERSION_CODE) {
                                        Toast.makeText(getApplicationContext(), msgs,Toast.LENGTH_SHORT).show();
                                        set_global_var(jo);
                                        option_page();
                                    } else {
                                        Toast.makeText(getApplicationContext(),"New version of app available. Please Update",Toast.LENGTH_SHORT).show();
                                        btn_update.setVisibility(View.VISIBLE);
                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(), jo.getString("message"),
                                            Toast.LENGTH_SHORT).show();

                                }
                            } else {
                                if(data.contains("Unable to resolve host"))
                                    Toast.makeText(getApplicationContext(),"CHECK YOUR INTERNET CONNECTION",Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getApplicationContext(), "NO RECORDS FOUND",Toast.LENGTH_SHORT).show();

                                findViewById(R.id.ma_login_page).setVisibility(View.VISIBLE);
                            }
                            findViewById(R.id.ma_progress).setVisibility(View.INVISIBLE);


                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), ex.toString(),
                                    Toast.LENGTH_SHORT).show();
                            findViewById(R.id.ma_progress).setVisibility(View.INVISIBLE);
                            findViewById(R.id.ma_login_page).setVisibility(View.VISIBLE);

                        }
                    }

                    @Override
                    protected String doInBackground(String... param) {
                        try {
                            URL url = new URL(param[0]);
                            HttpsTrustManager.allowAllSSL();
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            BufferedReader br =
                                    new BufferedReader(new InputStreamReader(conn.getInputStream()));

                            StringBuffer data = new StringBuffer();
                            String line;

                            while ((line = br.readLine()) != null) {
                                data.append(line + "\n");
                            }
                            br.close();
                            return data.toString();
                        } catch (Exception ex) {
                            return ex.getMessage();
                        }
                    }
                }
                dbManager obj1 = new dbManager();
                obj1.execute(url + postval);
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        btn_login = (Button) findViewById(R.id.btn_login);
        chk_Keepsigned = (CheckBox) findViewById(R.id.chk_keepSigned);
        txt_user = (EditText) findViewById(R.id.txt_user);
        txt_pass = (EditText) findViewById(R.id.txt_pass);
        btn_update = findViewById(R.id.main_updateButton);

        TextView titextView = (TextView) findViewById(R.id.ma_title);
        //stringIMEI= Settings.Secure.getString(this.getContentResolver(), Settings.Secure
        // .ANDROID_ID);

        titextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, syncdb.class);
                startActivity(intent);
            }
        });

        txt_user.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    hideKeyboard(view);
                }
            }
        });
        txt_pass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    hideKeyboard(view);
                }
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_STORAGE_CODE);
                    }
                    else {
                        startDownload();
                    }
                }
                else {
                    startDownload();
                }

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDownload();
                } else {
                    Toast.makeText(getApplicationContext(), "Storage Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startDownload() {
        String url = String.format("%s/APK/netrasagar.apk", GlobalVar.server_address);
        DownloadApk downloadApk = new DownloadApk(MainActivity.this);
        // With standard fileName 'App Update.apk'
        HttpsTrustManager.allowAllSSL();
        //downloadApk.startDownloadingApk(url);
        // With custom fileName, e.g. 'Update 2.0'
        downloadApk.startDownloadingApk(url, "netrasagar");

    }


    public void LoginPage(View view) throws UnsupportedEncodingException {
        findViewById(R.id.ma_login_page).setVisibility(View.GONE);
        findViewById(R.id.ma_progress).setVisibility(View.VISIBLE);
        String url = GlobalVar.server_address + "/androidjson/user_auth.php";
        String passwrd = Comfun.md5hash(txt_pass.getText().toString());
        String postval = "?&p=" + URLEncoder.encode(GlobalVar.phppass, "utf-8") +
                "&u=" + URLEncoder.encode(txt_user.getText().toString(), "utf-8") +
                "&w=" + URLEncoder.encode(passwrd, "utf-8");
        class dbManager extends AsyncTask<String, Void, String> {
            protected void onPostExecute(String data) {
                try {
                    if (data.contains("value")) {
                        JSONArray ja = new JSONArray(data);
                        JSONObject jo = ja.getJSONObject(0);
                        if (jo.getString("value").equals("1")) {
                            String msgs = jo.getString("message");
                            jo = ja.getJSONObject(1);
                            if(Comfun.json_int(jo, "app_version") == BuildConfig.VERSION_CODE) {
                                Toast.makeText(getApplicationContext(), msgs,Toast.LENGTH_SHORT).show();
                                set_global_var(jo);
                                if (chk_Keepsigned.isChecked()) {
                                    DB.write_constant("Keep_Signed", GlobalVar.Muser);
                                    DB.write_constant("Keep_Signed_pass", passwrd);
                                } else {
                                    DB.write_constant("Keep_Signed", "");
                                }
                                option_page();
                            }else {
                                Toast.makeText(getApplicationContext(),"New version of app available. Please Update",Toast.LENGTH_SHORT).show();
                                btn_update.setVisibility(View.VISIBLE);
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), jo.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                            findViewById(R.id.ma_login_page).setVisibility(View.VISIBLE);
                        }
                        findViewById(R.id.ma_progress).setVisibility(View.GONE);
                    } else {
                        if(data.contains("Unable to resolve host"))
                            Toast.makeText(getApplicationContext(),"CHECK YOUR INTERNET CONNECTION",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), "NO RECORDS FOUND",
                                Toast.LENGTH_SHORT).show();
                        findViewById(R.id.ma_login_page).setVisibility(View.VISIBLE);
                        findViewById(R.id.ma_progress).setVisibility(View.GONE);
                    }

                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                    findViewById(R.id.ma_progress).setVisibility(View.INVISIBLE);
                    findViewById(R.id.ma_login_page).setVisibility(View.VISIBLE);

                }
                data = "";
            }

            @Override
            protected String doInBackground(String... param) {
                try {
                    URL url = new URL(param[0]);
                    HttpsTrustManager.allowAllSSL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    BufferedReader br =
                            new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuffer data = new StringBuffer();
                    String line;

                    while ((line = br.readLine()) != null) {
                        data.append(line + "\n");
                    }
                    br.close();
                    return data.toString();
                } catch (Exception ex) {
                    return ex.getMessage();
                }
            }
        }
        dbManager obj1 = new dbManager();
        obj1.execute(url + postval);
    }

    void option_page() {
        Intent intent = new Intent(this, sel_option.class);
        startActivity(intent);
        this.finish();
    }

    void set_global_var(JSONObject obj) throws JSONException {
        GlobalVar.Muser = obj.getString("loginid");
        GlobalVar.User_FullName = obj.getString("username");

        GlobalVar.crew_view = obj.getString("viewcrew").equals("1");
        GlobalVar.vessel_view = obj.getString("viewvessel").equals("1");
        GlobalVar.crew_edit = obj.getString("Editcrew").equals("1");
        GlobalVar.vessel_edit = obj.getString("Editvessel").equals("1");
        GlobalVar.crew_add = obj.getString("Addcrew").equals("1");
        GlobalVar.vessel_add = obj.getString("Addvessel").equals("1");
        GlobalVar.fish_catch_view = obj.getString("fish_catch_view").equals("1");
        GlobalVar.fish_catch_add = obj.getString("fish_catch_add").equals("1");
        GlobalVar.fish_catch_edit = obj.getString("fish_catch_edit").equals("1");
        GlobalVar.fish_licence_renewal = obj.getString("vessel_fish_lic_renewal").equals("1");
        GlobalVar.view_report = obj.getString("report").equals("1");
        GlobalVar.view_approvePass = obj.getString("pass_approve").equals("1");
        GlobalVar.pass_open = obj.getString("pass_open").equals("1");
        GlobalVar.pass_close = obj.getString("pass_close").equals("1");
        GlobalVar.crewValUpdate = obj.getString("crew_validity_update").equals("1");
        GlobalVar.state_id = obj.getString("state_dep");

    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}