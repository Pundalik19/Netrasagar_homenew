package com.example.netrasagar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class  syncdb extends AppCompatActivity{
    boolean internet;
    TextView internet_status,server_status,localdb_status,logtextview;
    Connection connection;
    CALC cl;
    COMFUNCTIONS COMFUN;
    boolean ONLINEDBCON;
    Statement statement;
    ProgressDialog progressBar;
    ProgressBar overall_progress;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private long fileSize = 0;
    SQLiteDatabase db;
    Button upload,download,CLR;
    ProgressBar ledgerdata,fault;
    TextView ledgerdata_count,textlog,fault_count;
    EditText ipaddr;
    LinearLayout lin_layout,scroll_log;
    syncmydb sdb;
    uploaddb cls;
    String IMEI,USERNAME;
    int user_type = 0;
    String SERVERNAME,DBNAME,IP,DBUSER,DBPASSWORD;
    int display_log = 1;
    String upload_photo_url = "";
    Thread con_thread, thrd2;
    syncmydb ledgerdata_sdb,pwd_user_sdb,column_constant_updated_sdb,newrevenue_sdb,issue_date_sdb,rates_sdb,category_sdb,fault_sdb,masterdetails_sdb,zone_sdb,additionals_sdb,category_group_sdb,constituency_master_sdb,district_master_sdb,division_sdb,state_master_sdb,taluka_master_sdb,sub_division_sdb,pos_timing_sdb,inspection_rates_sdb,meter_details_sdb,softconfig_sdb,vsr_idc_master_sdb,vsr_idc_sdb,scheme_sdb,holiday_master_sdb,device_details_sdb,notifications_sdb,qr_code_sdb;
    Handler customHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syncdb);

        overall_progress = (ProgressBar) findViewById(R.id.overall_progress);
        lin_layout = (LinearLayout) findViewById(R.id.progressview);
        internet_status = (TextView) findViewById(R.id.internet_status);
        server_status = (TextView) findViewById(R.id.server_status);
        localdb_status = (TextView) findViewById(R.id.localdb_status);
        logtextview = (TextView) findViewById(R.id.logtextview);
        upload = (Button) findViewById(R.id.upload);
        download = (Button) findViewById(R.id.download);
        textlog = (TextView) findViewById(R.id.textlog);
        ipaddr = (EditText) findViewById(R.id.ipadd);
        CLR = (Button) findViewById(R.id.CLEAR);
        scroll_log = (LinearLayout) findViewById(R.id.scroll_log);
        cl = new CALC(this);
        db = cl.getdb();
        scroll_log.setOrientation(LinearLayout.VERTICAL);
        cancel_aynctask();

        SharedPreferences sp_sync = getSharedPreferences("spsync", Context.MODE_PRIVATE);
        SharedPreferences.Editor sp_synceditor = sp_sync.edit();
        sp_synceditor.putString("latest_activity", "syncdb");
        sp_synceditor.commit();

        //cl.update_latest_act("syncdb");

        SharedPreferences session = getSharedPreferences("LOGINCREDENTIAL", Context.MODE_PRIVATE);

        IMEI = session.getString("IMEI",null);

        USERNAME = session.getString("USERNAME",null);

        //ContentValues RIGHTS = cl.SET_RIGHTS(USERNAME);

        user_type = 1;//RIGHTS.getAsInteger("user_type");
        if(user_type == 1)
        {
            ipaddr.setEnabled(true);
        }else
        {
            ipaddr.setEnabled(false);
        }

        String contype = ipaddr.getText().toString();
        String ERRCONF = SETCONFIG(contype,syncdb.this);

        ipaddr.setText(SERVERNAME);

        if(!"".equals(ERRCONF.trim()) )
        {
            CALC.SHOWMSG("error",ERRCONF,"SERVER ERROR",syncdb.this);
        }

        ipaddr.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0)
            {
                String contype = ipaddr.getText().toString();
                String ERRCONF = SETCONFIG(contype,syncdb.this);
                if("".equals(ERRCONF.trim()) )
                {
                    CHECKMYSQLCON chk = new CHECKMYSQLCON(syncdb.this);
                    chk.execute(DBNAME,IP,DBUSER,DBPASSWORD);
                }else
                {
                    Toast.makeText(syncdb.this,ERRCONF,Toast.LENGTH_LONG).show();
                }

            }

        });

        CLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scroll_log.removeAllViews();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @SuppressLint("Range")
            @Override
            public void onClick(View v) {

                upload.setEnabled(false);
                download.setEnabled(false);
                //cl.show_loadingview(syncdb.this,"Please wait for upload to finished.");

                overall_progress.setProgress(0);
                logtextview.setText("");
                lin_layout.removeAllViews();

                String contype = ipaddr.getText().toString();
                String ERRCONF = SETCONFIG(contype,syncdb.this);
                if("".equals(ERRCONF.trim()) )
                {
                    if (!isDbConnected(connection) || !db.isOpen()) {
                        SharedPreferences session = getSharedPreferences("LOGINCREDENTIAL", Context.MODE_PRIVATE);

                        IMEI = session.getString("IMEI", null);
                        String error = "ERROR OCCURED WHILE CONNECTING TO DATABASE.\n";
                        if (!isDbConnected(connection)) {
                            error += "MYSQL DATABASE NOT CONNECTED";
                            create_dynamic_textview(syncdb.this, error, scroll_log);
                            //CHECKMYSQLCON chk = new CHECKMYSQLCON(context);
                            //chk.execute(DBNAME, IP, DBUSER, DBPASSWORD);
                            checkmysqlconn(syncdb.this);
                        }
                        if (!db.isOpen()) {
                            error += "SQLITE DATABASE NOT CONNECTED";
                            create_dynamic_textview(syncdb.this, error, scroll_log);
                            cl = new CALC(syncdb.this);
                            db = cl.getdb();
                        }
                    }

                    //admin_queries aq = new admin_queries("", syncdb.this);
                    //aq.execute("","","");

                    Cursor tables = cl.GET_DOWNLOADABLE_TABLES("UPLOAD");
                    int tabno = 2;
                    int total_tables = tables.getCount();
                    sdb = new syncmydb("column_constant_updated", syncdb.this,1,total_tables+1,"syncdb","frontend");
                    sdb.execute("column_constant_updated", "ID");

                    if (total_tables > 0) {
                        while (tables.moveToNext()) {
                            String tablename = tables.getString(0);
                            Log.d("UPLOADtablename", tablename);
                            if (!"".equals(tablename)) {
                                cls = new uploaddb(tablename, syncdb.this,tabno,total_tables+1,"syncdb","frontend");
                                cls.execute(tablename);
                                //commentedLog.e("uploaddata_func", "uploaddata_func function called");
                                //uploaddata_func(syncdb.this,tablename,tabno,total_tables+1);

                                /*if ("meter_photos".equals(tablename)) {
                                    Log.e("meter_photos table", tablename);
                                    Cursor met_query = cl.GET_METERPHOT_PATH();
                                    String path = "";
                                    Log.e("meter_photos", met_query.getCount() + " ");
                                    if (met_query.getCount() > 0) {
                                        while (met_query.moveToNext()) {
                                            Log.e("meter_path ", path + " ");
                                            path = met_query.getString(met_query.getColumnIndex("path"));
                                            new UploadFileAsync().execute(path);
                                        }
                                    }

                                }*/
                            } else {
                                CALC.SHOWMSG("error","CANNOT UPLOAD DATA.TABLE NAME IS EMPTY", "UPLOAD ERROR", syncdb.this);
                            }
                            tabno++;
                        }
                    } else {
                        CALC.SHOWMSG("error","ERROR OCCURED WHILE UPLOADING DATA TO SERVER.", "UPLOAD ERROR", syncdb.this);
                    }
                }else
                {
                    //Toast.makeText(syncdb.this,ERRCONF,Toast.LENGTH_LONG).show();
                    show_snack(ERRCONF,syncdb.this);
                }

                checkdb();

            }
        });

        BroadcastReceiver connectionBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String contype = ipaddr.getText().toString();
                String ERRCONF = SETCONFIG(contype,syncdb.this);
                if("".equals(ERRCONF.trim()) )
                {
                    if (intent == null || intent.getExtras() == null) {
                        ONLINEDBCON = false;
                        internet_status.setText("NOIntent");
                        checkdb();
                        return;
                    } else {
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED)
                        {
                            if (!isDbConnected(connection)) {
                                create_dynamic_textview(syncdb.this, "MYSQL DATABASE NOT CONNECTED", scroll_log);
                                if(download != null)
                                {
                                    download.setEnabled(false);
                                }
                                if(upload != null)
                                {
                                    upload.setEnabled(false);
                                }
                                //CHECKMYSQLCON chk = new CHECKMYSQLCON(context);
                                //chk.execute(DBNAME, IP, DBUSER, DBPASSWORD);
                                checkmysqlconn(context);

                            } else {
                                create_dynamic_textview(context, "MYSQL DATABASE CONNECTED", scroll_log);
                                Log.d("CONNECTION MYSQL", "CONNECTED");

                                if(download != null)
                                {
                                    download.setEnabled(true);
                                }
                                if(upload != null)
                                {
                                    upload.setEnabled(true);
                                }

                            }
                            checkdb();
                        } else {
                            ONLINEDBCON = false;
                            checkdb();
                            create_dynamic_textview(syncdb.this, "YOU ARE OFFLINE", scroll_log);
                            if(download != null)
                            {
                                download.setEnabled(false);
                            }
                            if(upload != null)
                            {
                                upload.setEnabled(false);
                            }
                            //CALC.SHOWMSG("error","YOU ARE OFFLINE", "PWD", context);

                        }
                    }
                }else
                {
                    //Toast.makeText(context,ERRCONF,Toast.LENGTH_LONG).show();
                    show_snack(ERRCONF,context);
                }
            }
        };

        download.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                download.setEnabled(false);
                upload.setEnabled(false);
                //cl.show_loadingview(syncdb.this,"Please wait for download to finished.");

                overall_progress.setProgress(0);
                logtextview.setText("");

                String contype = ipaddr.getText().toString();
                String ERRCONF = SETCONFIG(contype,syncdb.this);
                if("".equals(ERRCONF.trim()) )
                {

                    if (!isDbConnected(connection) || !db.isOpen()) {
                        SharedPreferences session = getSharedPreferences("LOGINCREDENTIAL", Context.MODE_PRIVATE);

                        IMEI = session.getString("IMEI", null);
                        String error = "ERROR OCCURED WHILE CONNECTING TO DATABASE.\n";
                        if (!isDbConnected(connection))
                        {
                            error += "MYSQL DATABASE NOT CONNECTED";
                            create_dynamic_textview(syncdb.this, error, scroll_log);
                            //CHECKMYSQLCON chk = new CHECKMYSQLCON(syncdb.this);
                            //chk.execute(DBNAME, IP, DBUSER, DBPASSWORD);
                            checkmysqlconn(syncdb.this);
                        }
                        if (!db.isOpen())
                        {
                            error += "SQLITE DATABASE NOT CONNECTED";
                            create_dynamic_textview(syncdb.this, error, scroll_log);
                            cl = new CALC(syncdb.this);
                            db = cl.getdb();
                        }
                        checkdb();
                    } else
                    {
                        lin_layout.removeAllViews();

                        //admin_queries aq = new admin_queries("", syncdb.this);
                        //aq.execute("","","");

                        Cursor tables = cl.GET_DOWNLOADABLE_TABLES("DOWNLOAD");
                        int tabno = 2;
                        int total_tables = tables.getCount();

                        sdb = new syncmydb("column_constant_updated", syncdb.this,1,total_tables+1,"syncdb","frontend");
                        sdb.execute("column_constant_updated", "ID", connection.toString());

                        if (total_tables > 0)
                        {
                            while (tables.moveToNext())
                            {
                                //Toast.makeText(syncdb.this,"TABLE NUMBER "+tabno+"/ "+total_tables,Toast.LENGTH_LONG).show();
                                String tablename = tables.getString(0);
                                if (!"".equals(tablename))
                                {
                                    sdb = new syncmydb(tablename, syncdb.this,tabno,total_tables+1,"syncdb","frontend");
                                    sdb.execute(tablename, "ID", connection.toString());

                                } else
                                {
                                    CALC.SHOWMSG("error","CANNOT DOWNLOAD DATA.TABLE NAME IS EMPTY", "DOWNLOAD ERROR", syncdb.this);
                                }
                                tabno++;
                            }

                        } else
                        {
                            CALC.SHOWMSG("error","ERROR OCCURED WHILE DOWNLOADING DATA TO SERVER.", "DOWNLOAD ERROR", syncdb.this);
                        }
                    }
                }else
                {
                    //Toast.makeText(syncdb.this,ERRCONF,Toast.LENGTH_LONG).show();
                    show_snack(ERRCONF,syncdb.this);
                }

            }
        });

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionBroadcastReceiver, filter);

    }

    public void downloadconsuemr(String tablename,String CONSUMERID,String param2,String SERIAL_NO,Context context,LinearLayout lin_lay)
    {

        cl = new CALC(context);
        db = cl.getdb();
        String contype = "MSS";
        display_log= 0;
        String ERRCONF = SETCONFIG(contype,context);
        if("".equals(ERRCONF.trim()) )
        {

            IMEI = SERIAL_NO;
            CHECKMYSQLCON chk = new CHECKMYSQLCON(context);
            chk.execute(DBNAME,IP,DBUSER,DBPASSWORD);

            syncmydb_specific sdb_sp = new syncmydb_specific(tablename, context,lin_lay);
            sdb_sp.execute(tablename, CONSUMERID,param2);

        }else
        {
            //Toast.makeText(context,ERRCONF,Toast.LENGTH_LONG).show();
            show_snack(ERRCONF,context);
        }

    }

    public void adminqueries(String SERIAL_NO,Context context)
    {

        cl = new CALC(context);
        db = cl.getdb();
        String contype = "MSS";
        display_log = 0;
        String ERRCONF = SETCONFIG(contype,context);
        if("".equals(ERRCONF.trim()) )
        {
            //display_log = 0;
            IMEI = SERIAL_NO;
            CHECKMYSQLCON chk = new CHECKMYSQLCON(context);
            chk.execute(DBNAME,IP,DBUSER,DBPASSWORD);

            admin_queries aq = new admin_queries("", context);
            aq.execute("","","");

        }else
        {
            //Toast.makeText(context,ERRCONF,Toast.LENGTH_LONG).show();
            show_snack(ERRCONF,context);
        }

    }

    public void UPDATESTAT(String SERIAL_NO,Context context,String loginid,String version)
    {

        cl = new CALC(context);
        db = cl.getdb();
        String contype = "MSS";
        display_log = 0;
        String ERRCONF = SETCONFIG(contype,context);
        if("".equals(ERRCONF.trim()) )
        {
            //display_log = 0;
            IMEI = SERIAL_NO;
            CHECKMYSQLCON chk = new CHECKMYSQLCON(context);
            chk.execute(DBNAME,IP,DBUSER,DBPASSWORD);

            UPDATE_USER_STATUS UUS = new UPDATE_USER_STATUS(loginid, context,version);
            UUS.execute(loginid,version,"");

        }else
        {
            //Toast.makeText(context,ERRCONF,Toast.LENGTH_LONG).show();
            show_snack(ERRCONF,context);
        }

    }

    public void scanrcptupload(String CONSUMERID,String BILLID,String SERIAL_NO,Context context)
    {

        cl = new CALC(context);
        db = cl.getdb();
        String contype = "MSS";
        display_log = 0;
        String ERRCONF = SETCONFIG(contype,context);
        if("".equals(ERRCONF.trim()) )
        {
            //display_log = 0;
            IMEI = SERIAL_NO;
            CHECKMYSQLCON chk = new CHECKMYSQLCON(context);
            chk.execute(DBNAME,IP,DBUSER,DBPASSWORD);

            scanrcpt aq = new scanrcpt(SERIAL_NO, context);
            aq.execute(CONSUMERID,BILLID);

        }else
        {
            Toast.makeText(context,ERRCONF,Toast.LENGTH_LONG).show();
        }

    }
    public void cancel_aynctask() {


        //commentedLog.e("cancel_aynctask", "cancel_aynctask");
        if (sdb != null && sdb.getStatus() == AsyncTask.Status.PENDING) {
            //commentedLog.e("sdbAsyncTask", "sdb PENDING");
        }

        if (sdb != null && sdb.getStatus() == AsyncTask.Status.FINISHED) {
            //commentedLog.e("sdbAsyncTask", "sdb FINISHED");
        }

        if (sdb != null && sdb.getStatus() == AsyncTask.Status.RUNNING) {
            //commentedLog.e("sdbAsyncTask", "sdb RUNNING");
            this.sdb.cancel(true);
        }

        if (sdb == null) {
            //commentedLog.e("sdbAsyncTask", "sdb null");
        }

        if (sdb == null) {
            //commentedLog.e("sdbAsyncTask after", "sdb null");
        }

        if (cls != null && cls.getStatus() == AsyncTask.Status.PENDING) {
            //commentedLog.e("sdbAsyncTask", "cls PENDING");

        }

        if (cls != null && cls.getStatus() == AsyncTask.Status.FINISHED) {
            //commentedLog.e("sdbAsyncTask", "cls FINISHED");

        }

        if (cls != null && cls.getStatus() == AsyncTask.Status.RUNNING) {
            //commentedLog.e("clsAsyncTask", "cls RUNNING");
            this.cls.cancel(true);
        }

        if (cls == null) {
            //commentedLog.e("sdbAsyncTask", "cls null");
        }

        if (sdb != null)
        {
            //commentedLog.e("sdb.name", " "+sdb.name+" "+sdb.currenttabno+" "+sdb.context+" ");
            sdb.cancel(true);

            //commentedLog.e("sdb", " "+sdb.getStatus());
            if (sdb.isCancelled())
            {
                //commentedLog.e("sdb can", "isCancelled");
            }

        }
        if (cls != null) {

            //commentedLog.e("cls.name", " "+cls.name+" "+cls.currenttabno+" "+cls.context+" ");
            cls.cancel(true);

            //commentedLog.e("cls", " "+cls.getStatus());
            if (cls.isCancelled())
            {
                //commentedLog.e("cls can", "isCancelled");
            }
        }

    }

    public void downloadsync(String SERIAL_NO,Context context,String activity_name,String update_type){
        cl = new CALC(context);
        db = cl.getdb();
        display_log = 0;
        String contype = "MSS";
        String ERRCONF = SETCONFIG(contype,context);
        if("".equals(ERRCONF.trim()) )
        {

            IMEI = SERIAL_NO;
            CHECKMYSQLCON chk = new CHECKMYSQLCON(context);
            chk.execute(DBNAME,IP,DBUSER,DBPASSWORD);

            admin_queries aq = new admin_queries("", context);
            aq.execute("","","");

            Cursor tables = cl.GET_DOWNLOADABLE_TABLES("DOWNLOAD");
            int tabno = 2;
            int total_tables = tables.getCount();

            sdb = new syncmydb("column_constant_updated", context,1,total_tables+1,activity_name,update_type);
            sdb.execute("column_constant_updated", "ID");

            if (total_tables > 0) {
                while (tables.moveToNext())
                {
                    String tablename = tables.getString(0);
                    if (!"".equals(tablename)) {
                        sdb = new syncmydb(tablename, context,tabno,total_tables+1,activity_name,update_type);
                        sdb.execute(tablename, "ID");

                    } else
                    {
                        show_snack("CANNOT DOWNLOAD DATA.TABLE NAME IS EMPTY",context);
                    }
                    tabno++;
                }

            } else {
                show_snack("ERROR OCCURED WHILE DOWNLOADING DATA TO SERVER",context);
            }
        }else
        {
            show_snack(ERRCONF,context);
        }

    }

    public void downloadsync2(String SERIAL_NO,Context context,ProgressBar overall_progresss,TextView logtextviews,String activity_name,String update_type)
    {

        overall_progress = overall_progresss;
        logtextview = logtextviews;
        overall_progress.setProgress(0);
        logtextview.setText("");
        cl = new CALC(context);
        db = cl.getdb();
        display_log = 0;
        String contype = "MSS";
        String ERRCONF = SETCONFIG(contype,context);
        if("".equals(ERRCONF.trim()) )
        {
            IMEI = SERIAL_NO;
            CHECKMYSQLCON chk = new CHECKMYSQLCON(context);
            chk.execute(DBNAME,IP,DBUSER,DBPASSWORD);

            admin_queries aq = new admin_queries("", context);
            aq.execute("","","");

            Cursor tables = cl.GET_DOWNLOADABLE_TABLES("DOWNLOAD");
            int tabno = 2;
            int total_tables = tables.getCount();

            sdb = new syncmydb("column_constant_updated", context,1,total_tables+1,activity_name,update_type);
            sdb.execute("column_constant_updated", "ID");

            if (total_tables > 0) {
                while (tables.moveToNext())
                {
                    //Toast.makeText(context,"TABLE NUMBER "+tabno+"/ "+total_tables,Toast.LENGTH_LONG).show();
                    String tablename = tables.getString(0);
                    if (!"".equals(tablename)) {
                        sdb = new syncmydb(tablename, context,tabno,total_tables,activity_name,update_type);
                        sdb.execute(tablename, "ID");
                    } else
                    {
                        //Toast.makeText(context,"CANNOT DOWNLOAD DATA.TABLE NAME IS EMPTY",Toast.LENGTH_LONG).show();
                        show_snack("CANNOT DOWNLOAD DATA.TABLE NAME IS EMPTY",context);
                    }
                    tabno++;
                }
            } else {
                //Toast.makeText(context,"ERROR OCCURED WHILE DOWNLOADING DATA TO SERVER.",Toast.LENGTH_LONG).show();
                show_snack("ERROR OCCURED WHILE DOWNLOADING DATA TO SERVER.",context);
            }
        }else
        {
            //Toast.makeText(context,ERRCONF,Toast.LENGTH_LONG).show();
            show_snack(ERRCONF,context);
        }
    }

    public String SETCONFIG(String type,Context context)
    {
        String ERROR = "";
        cl = new CALC(context);


        SERVERNAME = context.getResources().getString(R.string.SERVERNAME);
        DBNAME = context.getResources().getString(R.string.DBNAME);
        IP = context.getResources().getString(R.string.IP);
        DBUSER = context.getResources().getString(R.string.DBUSER);
        DBPASSWORD = context.getResources().getString(R.string.DBPASSWORD);

        upload_photo_url = context.getResources().getString(R.string.METER_PHOTO_UPLOAD_URL);

        return ERROR;
    }

    @SuppressLint("Range")
    public void uploadbillsync(String SERIAL_NO, Context context,String activity_name,String update_type)
    {

        cl = new CALC(context);
        db = cl.getdb();
        display_log = 0;
        String contype = "MSS";
        String ERRCONF = SETCONFIG(contype,context);
        if("".equals(ERRCONF.trim()) )
        {
            IMEI = SERIAL_NO;
            CHECKMYSQLCON chk = new CHECKMYSQLCON(context);
            chk.execute(DBNAME, IP, DBUSER, DBPASSWORD);

            admin_queries aq = new admin_queries("", context);
            aq.execute("","","");

            Cursor tables = cl.GET_DOWNLOADABLE_TABLES("UPLOAD");
            int tabno = 2;
            int total_tables = tables.getCount();
            sdb = new syncmydb("column_constant_updated", context,1,total_tables+1,activity_name,update_type);
            sdb.execute("column_constant_updated", "ID");

            if (tables.getCount() > 0) {
                while (tables.moveToNext()) {
                    String tablename = tables.getString(0);
                    Log.d("UPLOADtablename", tablename);
                    if (!"".equals(tablename))// && ("ledgerdata".equals(tablename) || "ledgerdata".equals(tablename) || "barcode_data".equals(tablename) || "consumer_contact".equals(tablename))) {
                    {
                        cls = new uploaddb(tablename, context ,tabno,total_tables+1,activity_name,update_type);
                        cls.execute(tablename);

                    } else {
                        show_snack("CANNOT UPLOAD DATA.TABLE NAME IS EMPTY",context);
                    }
                    tabno++;
                }
            } else {

                show_snack("10001",context);
            }
        }

    }
    @SuppressLint("Range")
    public void uploadbillsync2(String SERIAL_NO, Context context, ProgressBar overall_progresss, TextView logtextviews,String activity_name,String update_type)
    {

        overall_progress = overall_progresss;
        logtextview = logtextviews;
        overall_progress.setProgress(0);
        logtextview.setText("");

        cl = new CALC(context);
        db = cl.getdb();
        display_log = 0;
        String contype = "MSS";
        String ERRCONF = SETCONFIG(contype,context);
        if("".equals(ERRCONF.trim()) )
        {
            IMEI = SERIAL_NO;
            CHECKMYSQLCON chk = new CHECKMYSQLCON(context);
            chk.execute(DBNAME, IP, DBUSER, DBPASSWORD);

            admin_queries aq = new admin_queries("", context);
            aq.execute("","","");

            Cursor tables = cl.GET_DOWNLOADABLE_TABLES("UPLOAD");
            int tabno = 2;
            int total_tables = tables.getCount();
            sdb = new syncmydb("column_constant_updated", context,1,total_tables+1,activity_name,update_type);
            sdb.execute("column_constant_updated", "ID");

            if (tables.getCount() > 0) {
                while (tables.moveToNext()) {
                    String tablename = tables.getString(0);
                    Log.d("UPLOADtablename", tablename);
                    if (!"".equals(tablename)) {
                        cls = new uploaddb(tablename, context ,tabno,total_tables+1,activity_name,update_type);
                        cls.execute(tablename);

                    } else {
                        //CALC.SHOWMSG("CANNOT UPLOAD DATA.TABLE NAME IS EMPTY", "UPLOAD ERROR", syncdb.this);
                        //Toast.makeText(context, "CANNOT UPLOAD DATA.TABLE NAME IS EMPTY.", Toast.LENGTH_LONG).show();
                        show_snack("CANNOT UPLOAD DATA.TABLE NAME IS EMPTY",context);
                    }
                    tabno++;
                }
            } else {
                //Toast.makeText(context, "10001", Toast.LENGTH_LONG).show();
                show_snack("10001",context);
            }
        }

        /*uploaddb upled = new uploaddb("ledgerdata",context);
        upled.execute("ledgerdata");

        syncmydb sdbmst = new syncmydb("masterdetails",context);
        sdbmst.execute("masterdetails", "ID", "connection");*/


    }

    public void checkdb()
    {
        //commentedLog.e("CALLED","checkdb");
        GradientDrawable shape1 =  new GradientDrawable();
        GradientDrawable shape2 =  new GradientDrawable();
        GradientDrawable shape3 =  new GradientDrawable();
        shape1.setCornerRadius( 40 );
        shape2.setCornerRadius( 40 );
        shape3.setCornerRadius( 40 );

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED)
        {//commentedLog.e("networkInfoins",networkInfo.toString() + " "+networkInfo.getState());

            shape1.setColor(Color.GREEN);
            internet_status.setText("ONLINE");
            internet_status.setBackground(shape1);
            //internet_status.setBackgroundColor(Color.BLUE);
        }else
        {
            shape1.setColor(Color.RED);
            internet_status.setText("OFFLINE");
            internet_status.setBackground(shape1);
            //internet_status.setBackgroundColor(Color.RED);
        }

        if ( connection == null || !isDbConnected(connection))
        {
            //commentedLog.e("CALLED","mysql connection failed "+isDbConnected(connection));
            shape2.setColor(Color.RED);
            server_status.setBackground(shape2);
            //server_status.setBackgroundColor(Color.RED);
        }else
        {
            //commentedLog.e("CALLED","mysql connection successful "+isDbConnected(connection));
            shape2.setColor(Color.GREEN);
            server_status.setBackground(shape2);
            //server_status.setBackgroundColor(Color.GREEN);
        }
        if (db == null ||  !db.isOpen())
        {
            shape3.setColor(Color.RED);
            localdb_status.setBackground(shape3);
        }else
        {
            shape3.setColor(Color.GREEN);
            localdb_status.setBackground(shape3);
        }
    }

    public void onBackPressed()
    {
        if(cls != null)
        {
            if(!cls.isCancelled())
            {
                cls.cancel(true);

            }else
            {
                //commentedLog.e("ASYNCTASK","cls ALREADY CANCELLED");
            }
        }
        if(sdb != null)
        {
            if(!sdb.isCancelled())
            {
                sdb.cancel(true);

            }else
            {
                //commentedLog.e("ASYNCTASK","sdb ALREADY CANCELLED");
            }
        }

        db.close();

        if(connection != null)
        {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        create_dynamic_textview(syncdb.this,"OPERATION CANCELLED",scroll_log);
        checkdb();
        //Intent syncdb = new Intent(syncdb.this, dashboard.class);
        //startActivity(syncdb);
        finish();

    }

    public void uploaddata_func(Context context,String dbtablename,int total_tables,int currenttabno)
    {
        //commentedLog.e("func","uploaddata_func");
        Handler handler = new Handler();
        con_thread = new Thread(new Runnable()
        {
            public void run()
            {
                TextView tablename = null;
                Statement statement;
                String records = "", error = "";
                ProgressBar progress = null;
                TextView textview = null;

                String name = "";
                int total = 0;

                //commentedLog.e("UPLOAD FUN","CALLED BY "+context+" "+new Date(System.currentTimeMillis()).toString());

                if(overall_progress != null)
                {
                    overall_progress.setMax(total_tables);
                }
                if(display_log == 1)
                {
                    overall_progress.setMax(total_tables);
                    tablename = new TextView(syncdb.this);
                    tablename.setTextSize(16);
                    tablename.setText(name);
                    RelativeLayout.LayoutParams paramsnm = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                    tablename.setLayoutParams(paramsnm);
                    lin_layout.addView(tablename);

                    progress = new ProgressBar(syncdb.this, null, android.R.attr.progressBarStyleHorizontal);
                    //progress.setIndeterminate(true);
                    progress.setProgress(0);
                    progress.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                    params1.addRule(RelativeLayout.CENTER_IN_PARENT);
                    ProgressBar finalProgress = progress;
                    lin_layout.addView(finalProgress, params1);
                    handler.post(new Runnable() {
                        public void run() {


                        }
                    });

                    textview = new TextView(syncdb.this);
                    textview.setTextSize(16);
                    textview.setText("");

                    RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                    params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    textview.setLayoutParams(params2);
                    textview.setGravity(Gravity.RIGHT);
                    textview.setRight(100);
                    lin_layout.addView(textview);
                    TextView finalTextview = textview;
                    handler.post(new Runnable() {
                        public void run() {


                        }
                    });

                    progress.setMax(100);
                    progress.setProgress(0);

                    handler.post(new Runnable() {
                        public void run() {
                            create_dynamic_textview(syncdb.this, "Starting download", scroll_log);

                        }
                    });
                }
                try {

                    if (!isDbConnected(connection)) {
                        //publishProgress("CONNECTION TO SERVER FAILED.COULD NOT UPLOAD DATA","0","");
                        //commentedLog.e("MYSQL", "CONNECTION TO MYSQL FAILED.COULD NOT UPLOAD DATA"+new Date(System.currentTimeMillis()).toString());
                        //cancel(true);
                        int finalTotal18 = total;
                        TextView finalTextview19 = textview;
                        ProgressBar finalProgress19 = progress;
                        handler.post(new Runnable() {
                            public void run()
                            {
                                progreessupdate("CONNECTION TO SERVER FAILED.COULD NOT UPLOAD DATA","0","", finalTotal18, finalTextview19, finalProgress19,display_log,context,overall_progress,logtextview,total_tables);
                            }
                        });

                        if(display_log == 1)
                        {

                            handler.post(new Runnable() {
                                public void run()
                                {
                                    create_dynamic_textview(syncdb.this, "CANCELLED BY USER", scroll_log);
                                }
                            });
                            tablename.append(" INCOMPLETE");
                            tablename.setTextColor(Color.RED);

                            //CALC.SHOWMSG("OPERATION CANCELLED BY USER", "RESULT", syncdb.this);
                        }


                    } else
                    {
                        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                        ContentValues savedata = new ContentValues();
                        String sql = "";
                        String COLUMNNAMES = "";
                        String VALUES = "";
                        String updatestr = "";
                        int SUCCESSFUL = 0;
                        int FAILED = 0;
                        int inserted = 0;
                        int updated = 0;
                        int UNACK = 0;
                        String IMAGE_PATH = "";
                        String db_tablename = dbtablename;

                        SQLiteDatabase db = cl.getdb();

                        Cursor constants = null;
                        constants = cl.getcol_const(db_tablename);

                        String select_query = "";
                        String KEY = "";
                        String update_col_UP = "";
                        //commentedLog.e("constants.getCount()", String.valueOf(constants.getCount()));
                        if (constants.getCount() > 0) {
                            while (constants.moveToNext()) {

                                //commentedLog.e("WHILE", "INSIDE");

                                KEY = constants.getString(15);//constants.getColumnIndex("KEY"));

                                update_col_UP = constants.getString(18);//constants.getColumnIndex("KEY"));

                                select_query = constants.getString(19);//constants.getColumnIndex("select_query"));

                            }
                        }

                        Date datetime = new Date(System.currentTimeMillis());

                        String[] argumentsarr;
                        String updatestr_append = "";
                        String VALUES_append = "";
                        String COLUMNNAMES_append = "";


                        if (!"".equals(update_col_UP) && update_col_UP != null) {

                            argumentsarr = update_col_UP.split(",");

                            for (int i = 0; i < argumentsarr.length; i++) {
                                ////commentedLog.e("update_col_UP", argumentsarr[i] + " arrray");
                                if (("POS_UP_AT".equals(argumentsarr[i])) || ("receivedDT".equals(argumentsarr[i]))) {
                                    updatestr_append += ","+argumentsarr[i]+" = " + cl.sqltxt(cl.DB_DATETIME_FORMAT(datetime));

                                    COLUMNNAMES_append += ", "+argumentsarr[i];

                                    VALUES_append += "," + cl.sqltxt(cl.DB_DATETIME_FORMAT(datetime));
                                    ////commentedLog.e("update_POS_UP_AT", updatestr_append + " " + COLUMNNAMES_append + " " + VALUES_append);
                                }

                                if ("uploaded_by".equals(argumentsarr[i].toLowerCase()) || "user".equals(argumentsarr[i].toLowerCase())) {
                                    updatestr_append += ", " + argumentsarr[i] + " = " + cl.sqltxt(USERNAME);

                                    COLUMNNAMES_append += ", " + argumentsarr[i];

                                    VALUES_append += "," + cl.sqltxt(USERNAME);

                                }

                            }
                        } else {
                            //commentedLog.e("argumentsarr", "NO ARGUMENTS");
                        }

                        Cursor led = cl.LEDGERTOUPLOAD(select_query);

                        total = led.getCount();
                        if (display_log == 1) {
                            progress.setMax(total);
                        }

                        if (total > 0) {
                            while (led.moveToNext()) {
                                sql = "";
                                updatestr = "";
                                COLUMNNAMES = "";
                                VALUES = "";
                                //commentedLog.e("starttime key", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                String[] keyupdateval = KEY.split(",");
                                String keys_forupdate = "";
                                String keys_forupdateloc = "";
                                for (int i = 0; i < keyupdateval.length; i++) {
                                    if ("id".equals(keyupdateval[i].toLowerCase())) {
                                        keys_forupdate += "POS_TABLE_ID = '" + led.getString(led.getColumnIndex(keyupdateval[i])) + "' AND ";
                                    } else {
                                        keys_forupdate += keyupdateval[i] + " = '" + led.getString(led.getColumnIndex(keyupdateval[i])) + "' AND ";
                                    }

                                    keys_forupdateloc += keyupdateval[i] + " = '" + led.getString(led.getColumnIndex(keyupdateval[i])) + "' AND ";

                                }

                                keys_forupdate = keys_forupdate.substring(0, keys_forupdate.length() - 4);

                                keys_forupdateloc = keys_forupdateloc.substring(0, keys_forupdateloc.length() - 4);

                                //commentedLog.e("starttime id", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));

                                for (int i = 0; i < led.getColumnCount(); i++) {
                                    String colnm = "";
                                    if ("id".equals(led.getColumnName(i).toLowerCase())) {
                                        colnm = "POS_TABLE_ID";
                                    } else {
                                        colnm = led.getColumnName(i);
                                    }
                                    if ("meter_photos".equals(db_tablename) && "path".equals(colnm)) {
                                        IMAGE_PATH = led.getString(led.getColumnIndex("path"));
                                    }

                                    if (i + 1 == led.getColumnCount()) {
                                        COLUMNNAMES += colnm;

                                        VALUES += cl.sqltxt(led.getString(i));

                                        updatestr += colnm + " = " + cl.sqltxt(led.getString(i)) + "";

                                    } else {

                                        COLUMNNAMES += colnm + ", ";

                                        VALUES += cl.sqltxt(led.getString(i)) + ",";

                                        updatestr += colnm + " = " + cl.sqltxt(led.getString(i)) + ",";

                                    }


                                }

                                //commentedLog.e("IMAGE_PATH", " - " + IMAGE_PATH);

                                COLUMNNAMES += COLUMNNAMES_append;

                                updatestr += updatestr_append;

                                VALUES += VALUES_append;
                                //commentedLog.e("stime before query", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                sql = "(" + COLUMNNAMES + ") VALUES ( " + VALUES + " )";
                                try {

                                    ResultSet rs = statement.executeQuery("SELECT * FROM " + db_tablename + " WHERE " + keys_forupdate);
                                    Log.e("QUERY ", "SELECT * FROM " + db_tablename + " WHERE " + keys_forupdate);
                                    Log.e("stim aft sel query", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                    rs.last();
                                    int countofrow = rs.getRow();
                                    if (countofrow > 0) {

                                        Log.e("updatestr ", updatestr);
                                        Log.e("QUERY ", "UPDATE " + db_tablename + " set " + updatestr + " WHERE " + keys_forupdate);
                                        int updatecount = statement.executeUpdate("UPDATE " + db_tablename + " set " + updatestr + " WHERE " + keys_forupdate);
                                        Log.e("stim aft UPDATE query",cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                        if (updatecount > 0) {
                                            int photoup = 0;
                                            if("meter_photos".equals(db_tablename))
                                            {
                                                String UPPHOTO_STATUS = UPLOAD_PHOTO(context,IMAGE_PATH);

                                                if("SUCCESS".equals(UPPHOTO_STATUS))
                                                {
                                                    photoup = 1;
                                                    //publishProgress("\n PHOTO UPLOADED "+tablename+" "+IMAGE_PATH, "0","");
                                                    TextView finalTablename10 = tablename;
                                                    String finalIMAGE_PATH5 = IMAGE_PATH;
                                                    int finalTotal17 = total;
                                                    TextView finalTextview18 = textview;
                                                    ProgressBar finalProgress18 = progress;
                                                    handler.post(new Runnable() {
                                                        public void run()
                                                        {
                                                            progreessupdate("\n PHOTO UPLOADED "+ finalTablename10 +" "+ finalIMAGE_PATH5, "0","", finalTotal17, finalTextview18, finalProgress18,display_log,context,overall_progress,logtextview,total_tables);
                                                        }
                                                    });
                                                    Log.e("METER PHOTO", IMAGE_PATH+" UPLOADED");
                                                }else
                                                {
                                                    photoup = 0;
                                                    //publishProgress("\n UPLOAD FAILED FOR "+tablename+" "+IMAGE_PATH, "0","");
                                                    Log.e("METER PHOTO", IMAGE_PATH+"NOT UPLOADED");
                                                    TextView finalTablename9 = tablename;
                                                    String finalIMAGE_PATH4 = IMAGE_PATH;
                                                    int finalTotal16 = total;
                                                    TextView finalTextview17 = textview;
                                                    ProgressBar finalProgress17 = progress;
                                                    handler.post(new Runnable() {
                                                        public void run()
                                                        {
                                                            progreessupdate("\n UPLOAD FAILED FOR "+ finalTablename9 +" "+ finalIMAGE_PATH4+" REASON - "+UPPHOTO_STATUS, "0","", finalTotal16, finalTextview17, finalProgress17,display_log,context,overall_progress,logtextview,total_tables);
                                                        }
                                                    });
                                                }

                                            }else
                                            {
                                                photoup = 1;

                                            }

                                            if(photoup == 1)
                                            {
                                                ContentValues updled = new ContentValues();
                                                updled.put("upload_bit", 0);
                                                updled.put("POS_UP_AT", cl.DB_DATETIME_FORMAT(datetime));
                                                updled.put("UPLOADED_BY", USERNAME);

                                                int updateresult = 0;
                                                updateresult = db.update(db_tablename, updled, keys_forupdateloc, new String[]{});
                                                Log.e("stim aft locUPDATE", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                                if (updateresult > 0) {

                                                    SUCCESSFUL += 1;
                                                    updated += 1;
                                                    //publishProgress("", "1", "");
                                                    int finalTotal15 = total;
                                                    TextView finalTextview16 = textview;
                                                    ProgressBar finalProgress16 = progress;
                                                    handler.post(new Runnable() {
                                                        public void run()
                                                        {
                                                            progreessupdate("", "1", "", finalTotal15, finalTextview16, finalProgress16,display_log,context,overall_progress,logtextview,total_tables);
                                                        }
                                                    });

                                                    Log.e(" " + db_tablename, "SUCCESSFUL " + db_tablename);

                                                } else {
                                                    UNACK += 1;
                                                    Log.e("LEDGER UPDATE", "FAILED");
                                                    //publishProgress("\n UPDATE FAILED FOR " + tablename + " " + keys_forupdateloc, "0", "");
                                                    String finalTablename8 = db_tablename;
                                                    String finalKeys_forupdateloc1 = keys_forupdateloc;
                                                    int finalTotal14 = total;
                                                    TextView finalTextview15 = textview;
                                                    ProgressBar finalProgress15 = progress;
                                                    handler.post(new Runnable() {
                                                        public void run()
                                                        {
                                                            progreessupdate("\n UPDATE FAILED FOR " + finalTablename8 + " " + finalKeys_forupdateloc1, "0", "", finalTotal14, finalTextview15, finalProgress15,display_log,context,overall_progress,logtextview,total_tables);
                                                        }
                                                    });
                                                }
                                            }else
                                            {
                                                FAILED += 1;
                                                Log.e("LEDGER UPDATE", "FAILED");
                                                //publishProgress("\n UPLOADED FAILED FOR "+tablename+" "+IMAGE_PATH, "0","");
                                                String finalTablename7 = db_tablename;
                                                String finalIMAGE_PATH3 = IMAGE_PATH;
                                                int finalTotal13 = total;
                                                TextView finalTextview14 = textview;
                                                ProgressBar finalProgress14 = progress;
                                                String finalDb_tablename = db_tablename;
                                                handler.post(new Runnable() {
                                                    public void run()
                                                    {
                                                        progreessupdate("\n UPLOADED FAILED FOR "+ finalDb_tablename +" "+ finalIMAGE_PATH3, "0","", finalTotal13, finalTextview14, finalProgress14,display_log,context,overall_progress,logtextview,total_tables);
                                                    }
                                                });
                                            }

                                        } else
                                        {
                                            FAILED += 1;
                                            //publishProgress("\n\n#### FAILED UPDATE " + tablename + " set " + updatestr + " WHERE " + keys_forupdate, "0","");
                                            String finalTablename6 = db_tablename;
                                            String finalUpdatestr = updatestr;
                                            String finalKeys_forupdate = keys_forupdate;
                                            int finalTotal12 = total;
                                            TextView finalTextview13 = textview;
                                            ProgressBar finalProgress13 = progress;
                                            handler.post(new Runnable() {
                                                public void run()
                                                {
                                                    progreessupdate("\n\n#### FAILED UPDATE " + finalTablename6 + " set " + finalUpdatestr + " WHERE " + finalKeys_forupdate, "0","", finalTotal12, finalTextview13, finalProgress13,display_log,context,overall_progress,logtextview,total_tables);
                                                }
                                            });
                                        }
                                    } else
                                    {
                                        Log.e("INSQUER ", sql);
                                        Log.e("QUERY ", "INSERT INTO " + db_tablename + " " + sql);
                                        Log.e("stim bfr locins", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                        int res = statement.executeUpdate("INSERT INTO " + db_tablename + " " + sql);
                                        Log.d("DOESNT EXISTS ", String.valueOf(res));
                                        Log.e("stim aft locins", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                        if (res > 0) {
                                            int photoup = 0;
                                            if("meter_photos".equals(db_tablename))
                                            {
                                                String UPPHOTO_STATUS = UPLOAD_PHOTO(context,IMAGE_PATH);

                                                if("SUCCESS".equals(UPPHOTO_STATUS))
                                                {
                                                    photoup = 1;
                                                    //publishProgress("\n PHOTO UPLOADED "+tablename+" "+IMAGE_PATH, "0","");
                                                    String finalTablename5 = db_tablename;
                                                    String finalIMAGE_PATH2 = IMAGE_PATH;
                                                    int finalTotal11 = total;
                                                    TextView finalTextview12 = textview;
                                                    ProgressBar finalProgress12 = progress;
                                                    handler.post(new Runnable() {
                                                        public void run()
                                                        {
                                                            progreessupdate("\n PHOTO UPLOADED "+ finalTablename5 +" "+ finalIMAGE_PATH2+" REASON - "+UPPHOTO_STATUS, "0","", finalTotal11, finalTextview12, finalProgress12,display_log,context,overall_progress,logtextview,total_tables);
                                                        }
                                                    });

                                                    Log.e("METER PHOTO", IMAGE_PATH+" UPLOADED");
                                                }else
                                                {
                                                    photoup = 0;
                                                    //publishProgress("\n UPLOAD FAILED FOR "+tablename+" "+IMAGE_PATH, "0","");
                                                    String finalTablename4 = db_tablename;
                                                    String finalIMAGE_PATH1 = IMAGE_PATH;
                                                    int finalTotal10 = total;
                                                    TextView finalTextview11 = textview;
                                                    ProgressBar finalProgress11 = progress;
                                                    handler.post(new Runnable() {
                                                        public void run()
                                                        {
                                                            progreessupdate("\n UPLOAD FAILED FOR "+ finalTablename4 +" "+ finalIMAGE_PATH1, "0","", finalTotal10, finalTextview11, finalProgress11,display_log,context,overall_progress,logtextview,total_tables);
                                                        }
                                                    });
                                                    Log.e("METER PHOTO", IMAGE_PATH+"NOT UPLOADED");
                                                }

                                            }else
                                            {
                                                photoup = 1;

                                            }

                                            if(photoup == 1)
                                            {

                                                ContentValues updled = new ContentValues();
                                                updled.put("POS_UP_BIT", 0);
                                                updled.put("POS_UP_AT", cl.DB_DATETIME_FORMAT(datetime));
                                                updled.put("UPLOADED_BY", USERNAME);

                                                int updateresult = 0;
                                                updateresult = db.update(db_tablename, updled, keys_forupdateloc, new String[]{});
                                                Log.e("stim aft locupd", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                                if (updateresult > 0) {

                                                    SUCCESSFUL += 1;
                                                    inserted += 1;
                                                    //publishProgress("", "1", "");
                                                    int finalTotal9 = total;
                                                    TextView finalTextview10 = textview;
                                                    ProgressBar finalProgress10 = progress;
                                                    handler.post(new Runnable() {
                                                        public void run()
                                                        {
                                                            progreessupdate("", "1", "", finalTotal9, finalTextview10, finalProgress10,display_log,context,overall_progress,logtextview,total_tables);
                                                        }
                                                    });
                                                    Log.e(" " + db_tablename, "SUCCESSFUL " + db_tablename + " " + IMAGE_PATH);

                                                } else {
                                                    UNACK += 1;
                                                    Log.e("LEDGER UPDATE", "FAILED");
                                                    //publishProgress("\n\n#### UPDATE FAILED FOR " + tablename + " " + keys_forupdateloc, "0", "");
                                                    String finalTablename3 = db_tablename;
                                                    String finalKeys_forupdateloc = keys_forupdateloc;
                                                    int finalTotal8 = total;
                                                    TextView finalTextview9 = textview;
                                                    ProgressBar finalProgress9 = progress;
                                                    handler.post(new Runnable() {
                                                        public void run()
                                                        {
                                                            progreessupdate("\n\n#### UPDATE FAILED FOR " + finalTablename3 + " " + finalKeys_forupdateloc, "0", "", finalTotal8, finalTextview9, finalProgress9,display_log,context,overall_progress,logtextview,total_tables);
                                                        }
                                                    });
                                                }
                                            }else
                                            {
                                                FAILED += 1;
                                                Log.e("LEDGER UPDATE", "FAILED");
                                                //publishProgress("\n UPLOADED FAILED FOR "+tablename+" "+IMAGE_PATH, "0","");
                                                String finalTablename2 = db_tablename;
                                                String finalIMAGE_PATH = IMAGE_PATH;
                                                int finalTotal7 = total;
                                                TextView finalTextview8 = textview;
                                                ProgressBar finalProgress8 = progress;
                                                handler.post(new Runnable() {
                                                    public void run()
                                                    {
                                                        progreessupdate("\n UPLOADED FAILED FOR "+ finalTablename2 +" "+ finalIMAGE_PATH, "0","", finalTotal7, finalTextview8, finalProgress8,display_log,context,overall_progress,logtextview,total_tables);
                                                    }
                                                });
                                            }

                                        } else {
                                            FAILED += 1;
                                            //publishProgress("\n\n#### INSERT FAILED FOR "+tablename+" "+sql, "0","");
                                            String finalTablename1 = db_tablename;
                                            String finalSql = sql;
                                            TextView finalTextview7 = textview;
                                            int finalTotal6 = total;
                                            ProgressBar finalProgress7 = progress;
                                            handler.post(new Runnable() {
                                                public void run()
                                                {
                                                    progreessupdate("\n\n#### INSERT FAILED FOR "+ finalTablename1 +" "+ finalSql, "0","", finalTotal6, finalTextview7, finalProgress7,display_log,context,overall_progress,logtextview,total_tables);
                                                }
                                            });
                                        }

                                    }
                                } catch (Exception e) {
                                    Log.e("UPLOADerror ", "\nstart " + e.getMessage() + "  \nbreak\n" + e.getCause() + "  END \n");
                                    //publishProgress("\nstart " + e.getMessage() + "  END \n", "0","");
                                    int finalTotal = total;
                                    TextView finalTextview1 = textview;
                                    ProgressBar finalProgress1 = progress;
                                    handler.post(new Runnable() {
                                         public void run() {
                                             progreessupdate("\nstart " + e.getMessage() + "  END \n", "0", "", finalTotal, finalTextview1, finalProgress1, display_log, context, overall_progress, logtextview, total_tables);
                                         }
                                     });
                                    error += e.getMessage() + "\n" + e.getCause();
                                    //cancel(true);
                                }
                                //if (isCancelled())
                                //    break;
                            }

                            String MESSSAGE = "\n Total " + String.valueOf(total) + " SUCCESSFUL " + String.valueOf(SUCCESSFUL) + " FAILED " + String.valueOf(FAILED) + " INSERTED " + String.valueOf(inserted) + " UPDATED " + String.valueOf(updated) + " UNACK " + String.valueOf(UNACK) + "\n";
                            //publishProgress( MESSSAGE , "0","");
                            int finalTotal1 = total;
                            TextView finalTextview2 = textview;
                            ProgressBar finalProgress2 = progress;
                            handler.post(new Runnable() {
                                 public void run() {
                                     progreessupdate(MESSSAGE, "0", "", finalTotal1, finalTextview2, finalProgress2, display_log, context, overall_progress, logtextview, total_tables);
                                 }
                             });
                            Log.e("total", "total "+total+" inserted"+inserted+" updated"+updated+" UNACK"+UNACK+ " FAILED " + String.valueOf(FAILED));
                            long insval = 0;
                            if(total == inserted+updated+UNACK)
                            {
                                Log.e("success", "success");
                                int suc = currenttabno+1;
                                //publishProgress("OVERALL PROGRESS "+currenttabno+"/ "+total_tables, "0","VAL");
                                int finalTotal2 = total;
                                TextView finalTextview3 = textview;
                                ProgressBar finalProgress3 = progress;
                                handler.post(new Runnable() {
                                     public void run() {
                                         progreessupdate("OVERALL PROGRESS " + currenttabno + "/ " + total_tables, "0", "VAL", finalTotal2, finalTextview3, finalProgress3, display_log, context, overall_progress, logtextview, total_tables);
                                     }
                                 });
                                db_tablename = "";
                                ContentValues syncupdate = new ContentValues();
                                syncupdate.put("user", USERNAME);
                                syncupdate.put("serial_no", IMEI);
                                syncupdate.put("DateTime", cl.DB_DATETIME_FORMAT(datetime));
                                syncupdate.put("remarks", MESSSAGE.trim());
                                syncupdate.put("type", db_tablename+"_UPLOAD");
                                insval = cl.INSERT_SYNC(syncupdate);
                            }else
                            {
                                Log.e("failed", "failed");
                                //publishProgress("UPLOAD FAILED.TABLENAME - "+tablename+",FAILED ENTRIES - "+FAILED+",UNACK ENTRIES - "+UNACK , "0","");
                                //cancel(true);

                                String finalTablename = db_tablename;
                                int finalFAILED = FAILED;
                                int finalUNACK = UNACK;
                                int finalTotal3 = total;
                                TextView finalTextview4 = textview;
                                ProgressBar finalProgress4 = progress;
                                handler.post(new Runnable() {
                                    public void run() {
                                        progreessupdate("UPLOAD FAILED.TABLENAME - " + finalTablename + ",FAILED ENTRIES - " + finalFAILED + ",UNACK ENTRIES - " + finalUNACK, "0", "", finalTotal3, finalTextview4, finalProgress4, display_log, context, overall_progress, logtextview, total_tables);
                                    }
                                });
                            }
                            Log.e("insertsync", "insert "+insval+" total"+total+" inserted"+inserted+" "+updated+" "+UNACK);

                        }else
                        {
                            //publishProgress("OVERALL PROGRESS "+currenttabno+"/ "+total_tables, "0","VAL");

                            int finalTotal4 = total;
                            TextView finalTextview5 = textview;
                            ProgressBar finalProgress5 = progress;
                            handler.post(new Runnable() {
                                 public void run() {
                                     progreessupdate("OVERALL PROGRESS " + currenttabno + "/ " + total_tables, "0", "", finalTotal4, finalTextview5, finalProgress5, display_log, context, overall_progress, logtextview, total_tables);
                                 }
                             });
                            db_tablename = "";
                            ContentValues syncupdate = new ContentValues();
                            syncupdate.put("user", USERNAME);
                            syncupdate.put("serial_no", IMEI);
                            syncupdate.put("DateTime", cl.DB_DATETIME_FORMAT(datetime));
                            syncupdate.put("remarks", "NO RECORDS");
                            syncupdate.put("type", db_tablename+"_UPLOAD");
                            cl.INSERT_SYNC(syncupdate);
                        }

                    }
                }catch (Exception e)
                {
                    Log.e("exception for uplad",e.toString());
                    //publishProgress("\nstart "+e.getMessage() + "  \nbreak\n" + e.getCause()+"  END \n","0");
                    //publishProgress("\n"+e.getMessage()+"\n","0","");

                    int finalTotal5 = total;
                    TextView finalTextview6 = textview;
                    ProgressBar finalProgress6 = progress;
                    handler.post(new Runnable() {
                        public void run()
                        {
                            progreessupdate("\n"+e.getMessage()+"\n","0","", finalTotal5, finalTextview6, finalProgress6,display_log,context,overall_progress,logtextview,total_tables);
                        }
                    });
                    //cancel(true);
                    error += e.getMessage() + "\n" + e.getCause();

                }

            }
        });
        if ((con_thread != null) && (!con_thread.isAlive())) con_thread.start();
    }

    class uploaddb extends AsyncTask<String, String, String> {

        Statement statement;
        String records = "", error = "";
        ProgressBar progress;
        Context context;
        TextView textview;
        TextView tablename;
        String name = "";
        int total = 0;
        int total_tables = 0;
        int currenttabno = 0;
        String activity_name = "";
        String update_type = "";

        public uploaddb(String tablename,Context context,int currenttabno,int total_tables,String act_name,String update_type) {
            this.name = tablename;
            this.context = context;
            this.currenttabno = currenttabno;
            this.total_tables = total_tables;
            this.activity_name = act_name;
            this.update_type = update_type;
        }

        protected void onPreExecute()
        {
            String sp_tablename = this.name;               //table name for shared preferences
            if("pwd_user".equals(this.name))
            {
                sp_tablename = "pwd_user_upload";
            }

            if("ledgerdata".equals(this.name))
            {
                sp_tablename = "ledgerdata_upload";
            }

            if("ADD_AMOUNT".equals(this.name))
            {
                sp_tablename = "ADD_AMOUNT_upload";
            }

            SharedPreferences sp_sync = context.getSharedPreferences("spsync", Context.MODE_PRIVATE);
            String this_tablename = sp_sync.getString(sp_tablename,"");
            String lat_act = sp_sync.getString("latest_activity","");
            //commentedLog.e("SharedPreferences", "lat_act in updb"+lat_act);

            if("".equals(this_tablename) || "0".equals(this_tablename)  || Integer.valueOf(this_tablename) < 0)
            {

                int ct = 1;

                SharedPreferences.Editor spsynceditor = sp_sync.edit();
                spsynceditor.putString(sp_tablename, String.valueOf(ct));
                spsynceditor.commit();
                //commentedLog.e("uplddb sp", "syncing "+tablename+" COUNT "+this_tablename +" "+activity_name+" "+update_type+" tabname  "+sp_tablename);
            }else if(!"syncdb".equals(lat_act))
            {
                //commentedLog.e("uplddb sp", " cancelling "+tablename+" COUNT "+this_tablename +" "+activity_name+" "+update_type+" tabname  "+sp_tablename);
                cancel(true);
            }

            cl = new CALC(context);

            COMFUN = new COMFUNCTIONS();

            //String lat_act = cl.get_latest_act();
            //commentedLog.e("lat_act", "onPreExecute "+lat_act);
            //commentedLog.e("uploaddb", "BACKEND ACTIVITY  "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
            if("syncdb".equals(lat_act) && "backend".equals(update_type))// && !"column_constant_updated".equals(name)
            {
                //commentedLog.e("uploaddb", "BACKEND ACTIVITY CANCELLED "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
                cancel(true);
            }

            if(overall_progress != null)
            {
                overall_progress.setMax(total_tables);
            }
            if(display_log == 1)
            {
                overall_progress.setMax(total_tables);
                tablename = new TextView(syncdb.this);
                tablename.setTextSize(16);
                tablename.setText(name);
                RelativeLayout.LayoutParams paramsnm = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                tablename.setLayoutParams(paramsnm);
                lin_layout.addView(tablename);

                progress = new ProgressBar(syncdb.this, null, android.R.attr.progressBarStyleHorizontal);
                //progress.setIndeterminate(true);
                progress.setProgress(0);
                progress.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                params1.addRule(RelativeLayout.CENTER_IN_PARENT);
                lin_layout.addView(progress, params1);

                textview = new TextView(syncdb.this);
                textview.setTextSize(16);
                textview.setText("");

                RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                textview.setLayoutParams(params2);
                textview.setGravity(Gravity.RIGHT);
                textview.setRight(100);
                lin_layout.addView(textview);

                progress.setMax(100);
                progress.setProgress(0);
                create_dynamic_textview(this.context, "Starting download", scroll_log);
            }
        }
        @SuppressLint("Range")
        @Override

        protected String doInBackground(String... params) {
            SharedPreferences sp_sync = context.getSharedPreferences("spsync", Context.MODE_PRIVATE);
            String lat_act = sp_sync.getString("latest_activity","");
            //commentedLog.e("SharedPreferences", "lat_act in updb"+lat_act);

            //String lat_act = cl.get_latest_act();
            //commentedLog.e("lat_act", "doInBackground "+lat_act);
            //commentedLog.e("uploaddb while loop", "BACKEND ACTIVITY  "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
            if("syncdb".equals(lat_act) && "backend".equals(update_type))// && !"column_constant_updated".equals(name)
            {
                publishProgress(" \nBACKEND ACTIVITY CANCELLED "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name, "0","");
                //commentedLog.e("uploaddb", "BACKEND ACTIVITY CANCELLED "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
                cancel(true);
                return "uploaddb CANCELLED BACKGROUND DOWNLOAD";
            }
            if (isCancelled())
            {
                //commentedLog.e("isCancelled", "uploaddb CANCELLED BACKGROUND DOWNLOAD");
                return "uploaddb CANCELLED BACKGROUND DOWNLOAD";
            }

            try {

                if (!isDbConnected(connection)) {
                    publishProgress("CONNECTION TO SERVER FAILED.COULD NOT UPLOAD DATA","0","");
                    //commentedLog.e("MYSQL", "CONNECTION TO MYSQL FAILED.COULD NOT UPLOAD DATA"+new Date(System.currentTimeMillis()).toString());
                    cancel(true);
                } else
                {
                    statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    ContentValues savedata = new ContentValues();
                    String sql = "";
                    String COLUMNNAMES = "";
                    String VALUES = "";
                    String updatestr = "";
                    int SUCCESSFUL = 0;
                    int FAILED = 0;
                    int inserted = 0;
                    int updated = 0;
                    int UNACK = 0;
                    String IMAGE_PATH = "";
                    String tablename = params[0];

                    SQLiteDatabase db = cl.getdb();

                    Cursor constants = null;
                    constants = cl.getcol_const(tablename);

                    String select_query = "";
                    String KEY = "";
                    String update_col_UP = "";
                    Log.e("constants.getCount()", String.valueOf(constants.getCount()));
                    if (constants.getCount() > 0) {
                        while (constants.moveToNext()) {

                            Log.e("WHILE", "INSIDE");

                            KEY = constants.getString(15);//constants.getColumnIndex("KEY"));

                            update_col_UP = constants.getString(18);//constants.getColumnIndex("KEY"));

                            select_query = constants.getString(19);//constants.getColumnIndex("select_query"));

                        }
                    }

                    Date datetime = new Date(System.currentTimeMillis());

                    String[] argumentsarr;
                    String updatestr_append = "";
                    String VALUES_append = "";
                    String COLUMNNAMES_append = "";


                    if (!"".equals(update_col_UP) && update_col_UP != null) {

                        argumentsarr = update_col_UP.split(",");

                        for (int i = 0; i < argumentsarr.length; i++) {
                            //Log.e("update_col_UP", argumentsarr[i] + " arrray");
                            if (("POS_UP_AT".equals(argumentsarr[i]))  || ("receivedDT".equals(argumentsarr[i])) ) {
                                updatestr_append += ","+argumentsarr[i]+" = " + cl.sqltxt(cl.DB_DATETIME_FORMAT(datetime));

                                COLUMNNAMES_append += ", "+argumentsarr[i];

                                VALUES_append += "," + cl.sqltxt(cl.DB_DATETIME_FORMAT(datetime));
                                //Log.e("update_POS_UP_AT", updatestr_append + " " + COLUMNNAMES_append + " " + VALUES_append);
                            }

                            if ("uploaded_by".equals(argumentsarr[i].toLowerCase())) {
                                updatestr_append += ", " + argumentsarr[i] + " = " + cl.sqltxt(USERNAME);

                                COLUMNNAMES_append += ", " + argumentsarr[i];

                                VALUES_append += "," + cl.sqltxt(USERNAME);

                            }

                        }
                    } else {
                        Log.e("argumentsarr", "NO ARGUMENTS");
                    }

                    Cursor led = cl.LEDGERTOUPLOAD(select_query);

                    total = led.getCount();
                    if (display_log == 1) {
                        progress.setMax(total);
                    }

                    if (total > 0) {
                        while (led.moveToNext()) {

                            sp_sync = context.getSharedPreferences("spsync", Context.MODE_PRIVATE);
                            lat_act = sp_sync.getString("latest_activity","");
                            //commentedLog.e("SharedPreferences", "lat_act in updb"+lat_act);

                            //lat_act = cl.get_latest_act();
                            //commentedLog.e("lat_act", "while "+lat_act);
                            //commentedLog.e("uploaddb while loop", "BACKEND ACTIVITY  "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
                            if("syncdb".equals(lat_act) && "backend".equals(update_type)) //&& !"column_constant_updated".equals(name)
                            {
                                publishProgress(" \nBACKEND ACTIVITY CANCELLED "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name, "0","");
                                //commentedLog.e("uploaddb", "BACKEND ACTIVITY CANCELLED "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
                                cancel(true);
                                return "uploaddb CANCELLED BACKGROUND DOWNLOAD";
                            }

                            sql = "";
                            updatestr = "";
                            COLUMNNAMES = "";
                            VALUES = "";
                            Log.e("starttime key", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                            String[] keyupdateval = KEY.split(",");
                            String keys_forupdate = "";
                            String keys_forupdateloc = "";
                            for (int i = 0; i < keyupdateval.length; i++) {
                                if ("id".equals(keyupdateval[i].toLowerCase())) {
                                    keys_forupdate += "POS_TABLE_ID = '" + led.getString(led.getColumnIndex(keyupdateval[i])) + "' AND ";
                                } else {
                                    keys_forupdate += keyupdateval[i] + " = '" + led.getString(led.getColumnIndex(keyupdateval[i])) + "' AND ";
                                }

                                keys_forupdateloc += keyupdateval[i] + " = '" + led.getString(led.getColumnIndex(keyupdateval[i])) + "' AND ";

                            }

                            keys_forupdate = keys_forupdate.substring(0, keys_forupdate.length() - 4);

                            keys_forupdateloc = keys_forupdateloc.substring(0, keys_forupdateloc.length() - 4);

                            Log.e("starttime id", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));

                            for (int i = 0; i < led.getColumnCount(); i++) {
                                String colnm = "";
                                if ("id".equals(led.getColumnName(i).toLowerCase())) {
                                    colnm = "POS_TABLE_ID";
                                } else {
                                    colnm = led.getColumnName(i);
                                }
                                if ("meter_photos".equals(tablename) && "path".equals(colnm)) {
                                    IMAGE_PATH = led.getString(led.getColumnIndex("path"));
                                }

                                if (i + 1 == led.getColumnCount()) {
                                    COLUMNNAMES += colnm;

                                    VALUES += cl.sqltxt(led.getString(i));

                                    updatestr += colnm + " = " + cl.sqltxt(led.getString(i)) + "";

                                } else {

                                    COLUMNNAMES += colnm + ", ";

                                    VALUES += cl.sqltxt(led.getString(i)) + ",";

                                    updatestr += colnm + " = " + cl.sqltxt(led.getString(i)) + ",";

                                }


                            }

                            Log.e("IMAGE_PATH", " - " + IMAGE_PATH);

                            COLUMNNAMES += COLUMNNAMES_append;

                            updatestr += updatestr_append;

                            VALUES += VALUES_append;
                            Log.e("stime before query", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                            sql = "(" + COLUMNNAMES + ") VALUES ( " + VALUES + " )";
                            try {

                                ResultSet rs = statement.executeQuery("SELECT * FROM " + tablename + " WHERE " + keys_forupdate);
                                Log.e("QUERY ", "SELECT * FROM " + tablename + " WHERE " + keys_forupdate);
                                Log.e("stim aft sel query", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                rs.last();
                                int countofrow = rs.getRow();
                                if (countofrow > 0) {

                                    Log.e("updatestr ", updatestr);
                                    Log.e("QUERY ", "UPDATE " + tablename + " set " + updatestr + " WHERE " + keys_forupdate);
                                    int updatecount = statement.executeUpdate("UPDATE " + tablename + " set " + updatestr + " WHERE " + keys_forupdate);
                                    Log.e("stim aft UPDATE query",cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                    if (updatecount > 0) {
                                        int photoup = 0;
                                        if("meter_photos".equals(tablename))
                                        {
                                            String UPPHOTO_STATUS = UPLOAD_PHOTO(context,IMAGE_PATH);

                                            if("SUCCESS".equals(UPPHOTO_STATUS))
                                            {
                                                photoup = 1;
                                                publishProgress("\n PHOTO UPLOADED "+tablename+" "+IMAGE_PATH, "0","");
                                                Log.e("METER PHOTO", IMAGE_PATH+" UPLOADED");
                                            }else
                                            {
                                                photoup = 0;
                                                publishProgress("\n UPLOAD FAILED FOR "+tablename+" "+IMAGE_PATH+" REASON - "+UPPHOTO_STATUS, "0","");
                                                Log.e("METER PHOTO", IMAGE_PATH+"NOT UPLOADED");
                                            }

                                        }else
                                        {
                                            photoup = 1;

                                        }

                                        if(photoup == 1)
                                        {
                                            ContentValues updled = new ContentValues();
                                            updled.put("upload_bit", 0);
                                            updled.put("POS_UP_AT", cl.DB_DATETIME_FORMAT(datetime));
                                            updled.put("UPLOADED_BY", USERNAME);

                                            int updateresult = 0;
                                            updateresult = db.update(tablename, updled, keys_forupdateloc, new String[]{});
                                            Log.e("stim aft locUPDATE", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                            if (updateresult > 0) {

                                                SUCCESSFUL += 1;
                                                updated += 1;
                                                publishProgress("", "1", "");
                                                Log.e(" " + tablename, "SUCCESSFUL " + tablename);

                                            } else {
                                                UNACK += 1;
                                                Log.e("LEDGER UPDATE", "FAILED");
                                                publishProgress("\n UPDATE FAILED FOR " + tablename + " " + keys_forupdateloc, "0", "");
                                            }
                                        }else
                                        {
                                            FAILED += 1;
                                            Log.e("LEDGER UPDATE", "FAILED");
                                            publishProgress("\n UPLOADED FAILED FOR "+tablename+" "+IMAGE_PATH, "0","");
                                        }

                                    } else
                                    {
                                        FAILED += 1;
                                        publishProgress("\n\n#### FAILED UPDATE " + tablename + " set " + updatestr + " WHERE " + keys_forupdate, "0","");
                                    }
                                } else
                                {
                                    Log.e("INSQUER ", sql);
                                    Log.e("QUERY ", "INSERT INTO " + tablename + " " + sql);
                                    Log.e("stim bfr locins", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                    int res = statement.executeUpdate("INSERT INTO " + tablename + " " + sql);
                                    Log.d("DOESNT EXISTS ", String.valueOf(res));
                                    Log.e("stim aft locins", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                    if (res > 0) {
                                        int photoup = 0;
                                        if("meter_photos".equals(tablename))
                                        {

                                            String UPPHOTO_STATUS = UPLOAD_PHOTO(context,IMAGE_PATH);

                                            if("SUCCESS".equals(UPPHOTO_STATUS))
                                            {
                                                photoup = 1;
                                                publishProgress("\n PHOTO UPLOADED "+tablename+" "+IMAGE_PATH, "0","");
                                                Log.e("METER PHOTO", IMAGE_PATH+" UPLOADED");
                                            }else
                                            {
                                                photoup = 0;
                                                publishProgress("\n UPLOAD FAILED FOR "+tablename+" "+IMAGE_PATH+" REASON - "+UPPHOTO_STATUS, "0","");
                                                Log.e("METER PHOTO", IMAGE_PATH+"NOT UPLOADED");
                                            }

                                        }else
                                        {
                                            photoup = 1;

                                        }

                                        if(photoup == 1)
                                        {

                                            ContentValues updled = new ContentValues();
                                            updled.put("upload_bit", 0);
                                            updled.put("POS_UP_AT", cl.DB_DATETIME_FORMAT(datetime));
                                            updled.put("UPLOADED_BY", USERNAME);

                                            int updateresult = 0;
                                            updateresult = db.update(tablename, updled, keys_forupdateloc, new String[]{});
                                            Log.e("stim aft locupd", cl.DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                                            if (updateresult > 0) {

                                                SUCCESSFUL += 1;
                                                inserted += 1;
                                                publishProgress("", "1", "");
                                                Log.e(" " + tablename, "SUCCESSFUL " + tablename + " " + IMAGE_PATH);

                                            } else {
                                                UNACK += 1;
                                                Log.e("LEDGER UPDATE", "FAILED");
                                                publishProgress("\n\n#### UPDATE FAILED FOR " + tablename + " " + keys_forupdateloc, "0", "");
                                            }
                                        }else
                                        {
                                            FAILED += 1;
                                            Log.e("LEDGER UPDATE", "FAILED");
                                            publishProgress("\n UPLOADED FAILED FOR "+tablename+" "+IMAGE_PATH, "0","");
                                        }

                                    } else {
                                        FAILED += 1;
                                        publishProgress("\n\n#### INSERT FAILED FOR "+tablename+" "+sql, "0","");
                                    }

                                }
                            } catch (Exception e) {
                                Log.e("UPLOADerror ", "\nstart " + e.getMessage() + "  \nbreak\n" + e.getCause() + "  END \n");
                                publishProgress("\nstart " + e.getMessage() + "  END \n", "0","");
                                error += e.getMessage() + "\n" + e.getCause();
                                cancel(true);
                            }
                            if (isCancelled())
                                break;
                        }

                        String MESSSAGE = "\n Total " + String.valueOf(total) + " SUCCESSFUL " + String.valueOf(SUCCESSFUL) + " FAILED " + String.valueOf(FAILED) + " INSERTED " + String.valueOf(inserted) + " UPDATED " + String.valueOf(updated) + " UNACK " + String.valueOf(UNACK) + "\n";
                        publishProgress( MESSSAGE , "0","");
                        Log.e("total", "total "+total+" inserted"+inserted+" updated"+updated+" UNACK"+UNACK+ " FAILED " + String.valueOf(FAILED));
                        long insval = 0;
                        if(total == inserted+updated+UNACK)
                        {
                            Log.e("success", "success");
                            int suc = currenttabno+1;
                            publishProgress("OVERALL PROGRESS "+currenttabno+"/ "+total_tables, "0","VAL");
                            ContentValues syncupdate = new ContentValues();
                            syncupdate.put("user", USERNAME);
                            syncupdate.put("serial_no", IMEI);
                            syncupdate.put("DateTime", cl.DB_DATETIME_FORMAT(datetime));
                            syncupdate.put("remarks", MESSSAGE.trim());
                            syncupdate.put("type", tablename+"_UPLOAD");
                            insval = cl.INSERT_SYNC(syncupdate);
                        }else
                        {
                            Log.e("failed", "failed");
                            publishProgress("UPLOAD FAILED.TABLENAME - "+tablename+",FAILED ENTRIES - "+FAILED+",UNACK ENTRIES - "+UNACK , "0","");
                            cancel(true);
                        }
                        Log.e("insertsync", "insert "+insval+" total"+total+" inserted"+inserted+" "+updated+" "+UNACK);

                    }else
                    {
                        publishProgress("OVERALL PROGRESS "+currenttabno+"/ "+total_tables, "0","VAL");
                        ContentValues syncupdate = new ContentValues();
                        syncupdate.put("user", USERNAME);
                        syncupdate.put("serial_no", IMEI);
                        syncupdate.put("DateTime", cl.DB_DATETIME_FORMAT(datetime));
                        syncupdate.put("remarks", "NO RECORDS");
                        syncupdate.put("type", tablename+"_UPLOAD");
                        cl.INSERT_SYNC(syncupdate);
                    }

                }
            }catch (Exception e)
            {
                Log.e("exception for uplad",e.toString());
                //publishProgress("\nstart "+e.getMessage() + "  \nbreak\n" + e.getCause()+"  END \n","0");
                publishProgress("\n"+e.getMessage()+"\n","0","");
                cancel(true);
                error += e.getMessage() + "\n" + e.getCause();
                return error;
            }

            return error;

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        protected void onProgressUpdate(String...value) {

            Log.e("Uploadstat","UPLOADED" +value[0]);

            if (value[0] != "" && display_log == 0)
            {
                //Toast.makeText(context,value[0],Toast.LENGTH_SHORT).show();
                show_snack(value[0],context);
            }

            if (!"".equals(value[2].trim()) && overall_progress != null) {
                int prog = overall_progress.getProgress()+1;

                logtextview.setText("OVERALL PROGRESS "+prog+" / "+total_tables);
                overall_progress.setProgress(prog);

                if(total_tables == overall_progress.getProgress())
                {
                    overall_progress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#3ba35a")));
                    logtextview.setTextColor(Color.parseColor("#3ba35a"));
                    cl.hide_loadingview(this.context,"Upload finished.");
                    if(display_log == 1)
                    {

                        download.setEnabled(true);
                        upload.setEnabled(true);

                    }
                }else
                {
                    overall_progress.setProgressTintList(ColorStateList.valueOf(Color.RED));
                    logtextview.setTextColor(Color.RED);
                }

            }
            if(display_log == 1)
            {

                if (value[0] != "")
                {
                    create_dynamic_textview(this.context, value[0], scroll_log);
                }
                textview.setText(String.valueOf(progress.getProgress() + Integer.parseInt(value[1]) + " / " + String.valueOf(total)));
                progress.setProgress(progress.getProgress() + Integer.parseInt(value[1]));
            }

        }

        @Override
        protected void onPostExecute(String result) {
            String sp_tablename = this.name;                  //table name for shared preferences
            if("pwd_user".equals(this.name))
            {
                sp_tablename = "pwd_user_upload";
            }

            if("ledgerdata".equals(this.name))
            {
                sp_tablename = "ledgerdata_upload";
            }

            if("ADD_AMOUNT".equals(this.name))
            {
                sp_tablename = "ADD_AMOUNT_upload";
            }

            SharedPreferences sp_sync = context.getSharedPreferences("spsync", Context.MODE_PRIVATE);
            String this_tablename = sp_sync.getString(sp_tablename,"");
            int ct = 0;
            if("".equals(this_tablename) || "0".equals(this_tablename) || Integer.valueOf(this_tablename) < 0)
            {
                ct = 0;
            }else
            {
                ct = Integer.valueOf(this_tablename) - 1;
            }
            SharedPreferences.Editor spsynceditor = sp_sync.edit();
            spsynceditor.putString(sp_tablename, String.valueOf(ct));
            spsynceditor.commit();
            if(display_log == 1)
            {
                create_dynamic_textview(this.context, result, scroll_log);
                tablename.append(" FINISHED");
                tablename.setTextColor(Color.GREEN);

            }
            //commentedLog.e("Uploadsresult",result);
        }
        @Override
        protected void onCancelled() {
            //commentedLog.e("onCancelled",name+" "+currenttabno+context+total_tables+"syncmydb onCancelled");
            cancel(true);
            String sp_tablename = this.name;                  //table name for shared preferences
            if("pwd_user".equals(this.name))
            {
                sp_tablename = "pwd_user_upload";
            }

            if("ledgerdata".equals(this.name))
            {
                sp_tablename = "ledgerdata_upload";
            }

            if("ADD_AMOUNT".equals(this.name))
            {
                sp_tablename = "ADD_AMOUNT_upload";
            }

            SharedPreferences sp_sync = context.getSharedPreferences("spsync", Context.MODE_PRIVATE);
            String this_tablename = sp_sync.getString(sp_tablename,"");
            int ct = 0;
            if("".equals(this_tablename) || "0".equals(this_tablename) || Integer.valueOf(this_tablename) < 0)
            {
                ct = 0;
            }else
            {
                ct = Integer.valueOf(this_tablename) - 1;
            }
            SharedPreferences.Editor spsynceditor = sp_sync.edit();
            spsynceditor.putString(sp_tablename, String.valueOf(ct));
            spsynceditor.commit();
            if(display_log == 1)
            {
                create_dynamic_textview(this.context, "UPLOADING CANCELLED BY USER", scroll_log);
                /*db.close();
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }*/
                tablename.append(" INCOMPLETE");
                tablename.setTextColor(Color.RED);
                //CALC.SHOWMSG("OPERATION CANCELLED BY USER", "RESULT", syncdb.this);
                cl.hide_loadingview(this.context,"Upload cancelled.");
                download.setEnabled(true);
                upload.setEnabled(true);
            }

        }
    }

    public void progreessupdate(String value,String value1,String value2,int total,TextView textview,ProgressBar progress,int display_log,Context context,ProgressBar overall_progress,TextView logtextview,int total_tables)
    {

                //commentedLog.e("Uploadstat","UPLOADED" +value);

                if (value != "" && display_log == 0)
                {
                    //Toast.makeText(context,value[0],Toast.LENGTH_SHORT).show();
                    show_snack(value,context);
                }

                if (!"".equals(value2.trim()) && overall_progress != null) {
                    int prog = overall_progress.getProgress()+1;

                    logtextview.setText("OVERALL PROGRESS "+prog+" / "+total_tables);
                    overall_progress.setProgress(prog);

                    if(total_tables == overall_progress.getProgress())
                    {
                        overall_progress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#3ba35a")));
                        logtextview.setTextColor(Color.parseColor("#3ba35a"));
                    }else
                    {
                        overall_progress.setProgressTintList(ColorStateList.valueOf(Color.RED));
                        logtextview.setTextColor(Color.RED);
                    }

                }
                if(display_log == 1)
                {

                    if (value != "")
                    {
                        create_dynamic_textview(context, value, scroll_log);
                    }
                    textview.setText(String.valueOf(progress.getProgress() + Integer.parseInt(value1) + " / " + String.valueOf(total)));
                    progress.setProgress(progress.getProgress() + Integer.parseInt(value1));
                }
    }

    public void cancelprog(Handler handler,String display_log,TextView view)
    {

    }

    public String compressImage(Context context, String filePath)
    {

        //String filePath = getRealPathFromURI(context, uri);
        Log.e("compressImage ","filePath "+filePath);

        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filePath);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Log.e("comp file",filePath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filePath;

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public static String getRealPathFromURI(Context context, Uri uri) {
        Uri queryUri = MediaStore.Files.getContentUri("external");
        String columnData = MediaStore.Files.FileColumns.DATA;
        String columnSize = MediaStore.Files.FileColumns.SIZE;

        String[] projectionData = {MediaStore.Files.FileColumns.DATA};


        String name = null;
        String size = null;

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if ((cursor != null) && (cursor.getCount() > 0)) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

            cursor.moveToFirst();

            name = cursor.getString(nameIndex);
            size = cursor.getString(sizeIndex);

            cursor.close();
        }

        String imagePath = "";
        if ((name != null) && (size != null)) {
            String selectionNS = columnData + " LIKE '%" + name + "' AND " + columnSize + "='" + size + "'";

            Cursor cursorLike = context.getContentResolver().query(queryUri, projectionData, selectionNS, null, null);

            if ((cursorLike != null) && (cursorLike.getCount() > 0)) {
                cursorLike.moveToFirst();
                int indexData = cursorLike.getColumnIndex(columnData);
                if (cursorLike.getString(indexData) != null) {
                    imagePath = cursorLike.getString(indexData);
                }
                cursorLike.close();
            }
        }

        return imagePath;
    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "/Download/late");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    public String UPLOAD_PHOTO(Context conte,String FILENAMETOUPLOAD)
    {
        String error = "";

        Uri imagepath = Uri.parse(FILENAMETOUPLOAD);

        Log.e("Original image","PATH "+imagepath);

        File sourceFile = new File(FILENAMETOUPLOAD);
        //File file = new File(FILENAMETOUPLOAD);
        if(sourceFile.exists())
        {

            //long fileSize = sourceFile.length();

            //Log.e("Original image","imagepath fileSize "+fileSize);

            String sourceFileUri = FILENAMETOUPLOAD;//compressImage(conte, FILENAMETOUPLOAD);

            //Log.e("COMPRESSED image","PATH "+sourceFileUri);

            //File file2 = new File(sourceFileUri);

            //long fileSize1 = file2.length();

            //Log.e("COMPRESSED image","compimagepath fileSize "+fileSize1);

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            if (sourceFile.isFile())
            {
                try {
                    String upLoadServerUri = upload_photo_url;

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(upLoadServerUri);

                    COMFUN.trustEveryone();
                    // Open a HTTP connection to the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE",
                            "multipart/form-data");
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("bill", sourceFileUri);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"meter_photo\";filename=\""
                            + sourceFileUri + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math
                                .min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0,
                                bufferSize);

                    }

                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens
                            + lineEnd);

                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    if (serverResponseCode == HttpURLConnection.HTTP_OK) {

                        BufferedReader readres = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        Log.e("UPLOAD IMAGE", "File Upload Complete. "+readres.readLine());
                        Log.e("UPLOAD IMAGE", "File Upload Complete. "+serverResponseMessage);
                        error = "SUCCESS";

                    }else
                    {
                        Log.e("UPLOAD IMAGE", "response code "+String.valueOf(serverResponseCode));

                        error = "FAILED TO UPLOAD IMAGE "+ sourceFileUri+" response code "+String.valueOf(serverResponseCode);
                    }

                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (Exception e) {

                    e.printStackTrace();
                    Log.e("UPLOAD IMAGE", "response code "+e.toString());
                    error = "Error while uploading meter photo "+ sourceFileUri +" response code :- "+e.getMessage();
                }
            }else
            {
                error = "NOT A VALID PHOTO";
            }
        }else
        {
            error = "FILE DOESNOT EXISTS";
            Log.e("COMPIMAGE","FILE DOESNOT EXISTS "+FILENAMETOUPLOAD);
        }
        // Do something else.

        return error;
    }



    class syncmydb extends AsyncTask<String,String,String> {
        ProgressBar progress;
        Context context;
        TextView textview;
        TextView tablename;
        String name = "";
        int total = -1;
        int total_tables = 0;
        int currenttabno = 0;
        String activity_name = "";
        String update_type = "";

        public syncmydb(String tablename,Context context,int currenttabno,int total_tables,String act_name,String update_type)
        {

            this.name = tablename;
            this.context = context;
            this.currenttabno = currenttabno;
            this.total_tables = total_tables;
            this.activity_name = act_name;
            this.update_type = update_type;

        }

        String records = "", error = "";

        protected void onPreExecute()
        {
            String sp_tablename = this.name;                  //table name for shared preferences
            if("pwd_user".equals(this.name))
            {
                sp_tablename = "pwd_user_download";
            }

            if("ledgerdata".equals(this.name))
            {
                sp_tablename = "ledgerdata_download";
            }

            if("ADD_AMOUNT".equals(this.name))
            {
                sp_tablename = "ADD_AMOUNT_download";
            }

            SharedPreferences sp_sync = context.getSharedPreferences("spsync", Context.MODE_PRIVATE);
            String this_tablename = sp_sync.getString(sp_tablename,"");
            String lat_act = sp_sync.getString("latest_activity","");
            //commentedLog.e("SharedPreferences", "lat_act in syncdb"+lat_act);

            if("".equals(this_tablename) || "0".equals(this_tablename) || Integer.valueOf(this_tablename) < 0)
            {

                int ct = 0;

                ct = 1;

                SharedPreferences.Editor spsynceditor = sp_sync.edit();
                spsynceditor.putString(sp_tablename, String.valueOf(ct));
                spsynceditor.commit();
                //commentedLog.e("syncdb sp", "syncing syncmydb"+tablename+" COUNT "+this_tablename +" "+activity_name+" "+update_type+" tabname  "+sp_tablename);
            }else if(!"syncdb".equals(lat_act))
            {
                //commentedLog.e("syncdb sp", " cancelling "+tablename+" COUNT "+this_tablename +" "+activity_name+" "+update_type+" tabname  "+name);
                cancel(true);
            }

            CALC cl = new CALC(context);

            //String lat_act = cl.get_latest_act();
            //commentedLog.e("lat_act", "syncmydb onPreExecute "+lat_act);
            //commentedLog.e("syncdb", "BACKEND ACTIVITY  "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
            if("syncdb".equals(lat_act) && "backend".equals(update_type))// && !"column_constant_updated".equals(name)
            {
                //commentedLog.e("syncdb", "BACKEND ACTIVITY CANCELLED "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
                cancel(true);

            }

            if(overall_progress != null)
            {
                overall_progress.setMax(total_tables);
            }

            if(display_log == 1)
            {

                tablename = new TextView(syncdb.this);
                tablename.setTextSize(16);
                tablename.setText(name);
                RelativeLayout.LayoutParams paramsnm = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                tablename.setLayoutParams(paramsnm);

                lin_layout.removeView(tablename);
                lin_layout.addView(tablename);

                progress = new ProgressBar(syncdb.this, null, android.R.attr.progressBarStyleHorizontal);

                progress.setProgress(0);
                progress.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                params1.addRule(RelativeLayout.CENTER_IN_PARENT);
                lin_layout.removeView(progress);
                lin_layout.addView(progress, params1);

                textview = new TextView(syncdb.this);
                textview.setTextSize(16);
                textview.setText("");

                RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                textview.setLayoutParams(params2);
                textview.setGravity(Gravity.RIGHT);
                textview.setRight(100);
                lin_layout.removeView(textview);
                lin_layout.addView(textview);

                progress.setMax(100);
                progress.setProgress(0);
                create_dynamic_textview(this.context, "Starting download", scroll_log);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            SharedPreferences sp_sync = context.getSharedPreferences("spsync", Context.MODE_PRIVATE);
            String lat_act = sp_sync.getString("latest_activity","");
            //commentedLog.e("SharedPreferences", "lat_act in updb"+lat_act);

            //String lat_act = cl.get_latest_act();
            //commentedLog.e("lat_act", "syncmydb doInBackground "+lat_act);
            //commentedLog.e("syncdb while loop", "BACKEND ACTIVITY  "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
            if("syncdb".equals(lat_act) && "backend".equals(update_type))// && !"column_constant_updated".equals(name)
            {
                publishProgress(" \nBACKEND ACTIVITY CANCELLED "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name, "0","");
                //commentedLog.e("syncdb", "BACKEND ACTIVITY CANCELLED "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
                cancel(true);
                return "syncmydb CANCELLED BACKGROUND DOWNLOAD";
            }

            if (isCancelled())
            {
                //commentedLog.e("isCancelled", "syncmydb CANCELLED BACKGROUND DOWNLOAD");
                return "syncmydb CANCELLED BACKGROUND DOWNLOAD";
            }
            String MESSAGE = "";String ERROR;
            try {

                if (!isDbConnected(connection)) {
                    publishProgress("CONNECTION TO SERVER FAILED.COULD NOT DOWNLOAD DATA","0","");
                    //commentedLog.e("MYSQL", "CONNECTION TO MYSQL FAILED");
                    cancel(true);
                } else
                {
                    //commentedLog.e("MYSQL", "CONNECTION TO MYSQL SUCCESSFUL");
                    //publishProgress("CONNECTION TO MYSQL SUCCESSFUL","0");
                    SQLiteDatabase db = cl.getdb();

                    statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    Statement statementup = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    String tablename = params[0];

                    String key = params[1];

                    String ret = "";
                    String keyvalue = "";
                    int inserted = 0;
                    int updated = 0;
                    int failed = 0;
                    int success = 0;
                    int UNACK = 0;

                    try {
                        String whrcon = "";
                        String updstr = "";

                        Cursor constants = null;
                        constants = cl.getcol_const(tablename);

                        String col_to_download = "";
                        String col_to_upload = "";
                        String where_download = "";
                        String where_upload = "";
                        String select_query = "";
                        String select_query_args = "";
                        String rev_update = "";
                        String rev_update_query = "";
                        String KEY = "";
                        String del_and_upd = "";
                        String update_col = "";
                        //Log.e("constants.getCount()", String.valueOf(constants.getCount()));
                        if (constants.getCount() > 0) {
                            while (constants.moveToNext()) {

                                Log.e("WHILE", "INSIDE");

                                /*col_to_download = constants.getString(constants.getColumnIndex("id"));

                                col_to_download = constants.getString(constants.getColumnIndex("tablename"));

                                col_to_download = constants.getString(constants.getColumnIndex("col_to_download"));

                                col_to_upload = constants.getString(constants.getColumnIndex("col_to_upload"));

                                where_download = constants.getString(constants.getColumnIndex(4);//"where_download"));

                                where_upload = constants.getString(5);//constants.getColumnIndex("where_upload"));

                                */
                                select_query = constants.getString(7);//constants.getColumnIndex("select_query"));

                                select_query_args = constants.getString(8);//constants.getColumnIndex("select_query"));

                                rev_update = constants.getString(13);//constants.getColumnIndex("rev_update"));

                                rev_update_query = constants.getString(14);//constants.getColumnIndex("rev_update_query"));

                                KEY = constants.getString(15);//constants.getColumnIndex("KEY"));

                                del_and_upd = constants.getString(16);//constants.getColumnIndex("KEY"));

                                update_col = constants.getString(17);//constants.getColumnIndex("KEY"));
                            }
                        }

                        if ("column_constant_updated".equals(tablename) && "".equals(select_query.trim())) {
                            select_query = "SELECT ID, tablename, table_type, col_to_download, col_to_upload, where_download, where_upload, select_query, select_query_args, serial_no, user, downloaded_at, status, rev_update, rev_update_query, key_value, delete_update, UPDATE_COL_DOWN, UPDATE_COL_UP, select_query_up FROM column_constant_updated";
                            KEY = "id";
                            select_query_args = "";
                            rev_update = "0";
                            del_and_upd = "0";
                        }

                        Log.e("select_query", select_query + " select_query");
                        Log.e("select_query_args", select_query_args + " select_query_args");
                        Log.e("rev_update", rev_update + " rev_update");
                        Log.e("KEY", KEY + " KEY");

                        String[] argumentsarr;
                        String select_where = "";

                        if (!"".equals(select_query_args) && select_query_args != null) {

                            argumentsarr = select_query_args.split(",");

                            for (int i = 0; i < argumentsarr.length; i++) {
                                Log.e("argumentsarr", argumentsarr[i] + " arrray");
                                if ("POS_SERIAL_NO".equals(argumentsarr[i]) || "serial_no".equals(argumentsarr[i].toLowerCase()) || "masterdetails.POS_SERIAL_NO".equals(argumentsarr[i]) || "ledgerdata.POS_SERIAL_NO".equals(argumentsarr[i]) || "newrevenue.POS_SERIAL_NO".equals(argumentsarr[i]))
                                {
                                    select_where = " " + argumentsarr[i] + " = '" + IMEI + "'";
                                    Log.e("select_where", select_where);
                                }

                                if("edited_at".equals(argumentsarr[i]) || "EdtDtTm".equals(argumentsarr[i]) || "UpdatedTm".equals(argumentsarr[i]))
                                {
                                    String LAST_SYNCED = cl.GET_LAST_SYNCED(tablename+"_DOWNLOAD");

                                    if(!"".equals(LAST_SYNCED.trim()))
                                    {
                                        select_where += " WHERE " + argumentsarr[i] + " >= '"+LAST_SYNCED+"'";
                                    }

                                    Log.e("select_where", argumentsarr[i]+" last_down "+select_where);
                                }

                            }
                        } else {
                            Log.e("argumentsarr", "NO ARGUMENTS");
                        }

                        Log.e("DOWNLOAD query", select_query + select_where);
                        ResultSet rs = statement.executeQuery(select_query + select_where);

                        rs.last();
                        Log.e(tablename, "count" + rs.last() + rs.getRow());
                        total = rs.getRow();

                        if (display_log == 1) {
                            progress.setMax(total);
                        }

                        rs.beforeFirst();

                        int iterator = 0;
                        ContentValues master = new ContentValues();

                        if (total > 0)
                        {

                            if ("1".equals(del_and_upd)) {
                                Log.e("delete n update", del_and_upd + "delete n update selected");

                                int deleted_entries = db.delete(tablename, null, new String[]{});
                                Log.e("deleted_entries", "delete entries from " + tablename + " " + deleted_entries);
                            }

                            ResultSetMetaData colnames = rs.getMetaData();

                            while (rs.next())
                            {
                                sp_sync = context.getSharedPreferences("spsync", Context.MODE_PRIVATE);
                                lat_act = sp_sync.getString("latest_activity","");
                                //commentedLog.e("SharedPreferences", "lat_act in updb"+lat_act);

                                //lat_act = cl.get_latest_act();
                                //commentedLog.e("lat_act", "syncmydb while "+lat_act);
                                //commentedLog.e("syncdb while loop", "BACKEND ACTIVITY  "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
                                if("syncdb".equals(lat_act) && "backend".equals(update_type))// && !"column_constant_updated".equals(name)
                                {
                                    publishProgress(" \nBACKEND ACTIVITY CANCELLED "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name, "0","");
                                    //commentedLog.e("syncdb", "BACKEND ACTIVITY CANCELLED "+lat_act+" "+tablename+" "+activity_name+" "+update_type+" tabname  "+name);
                                    cancel(true);
                                    return "wsyncmydb CANCELLED BACKGROUND DOWNLOAD";
                                }

                                iterator++;

                                master.clear();

                                String[] keyupdateval = KEY.split(",");
                                String keys_forupdate = "";
                                String keysvalue_forupdate = "";
                                for (int i = 0; i < keyupdateval.length; i++)
                                {
                                    keys_forupdate += keyupdateval[i] + " = '" + rs.getString(keyupdateval[i]) + "' AND ";

                                    keysvalue_forupdate += rs.getString(keyupdateval[i]) + ", ";

                                }

                                keys_forupdate = keys_forupdate.substring(0, keys_forupdate.length() - 4);

                                keysvalue_forupdate = keysvalue_forupdate.substring(0, keysvalue_forupdate.length() - 2);//keysvalue_forupdate.replace(",","") ;

                                Date currentdttm = new Date(System.currentTimeMillis());

                                //Log.e("colnames.getCount()", colnames.getColumnCount() + " ");
                                for (int k = 1; k <= colnames.getColumnCount(); k++)
                                {
                                    String column_name = "";
                                    column_name = colnames.getColumnName(k);
                                    Cursor chkcol = null;
                                    chkcol = db.rawQuery("Select * from " + tablename + " limit 1", null);
                                    int colIndex = 0;

                                    colIndex = chkcol.getColumnIndex(column_name.trim());
                                    if (colIndex == -1)//k==1 &&
                                    {
                                        Log.e("colnames def", column_name + " COL DOESNOT EXISTS");
                                       /*ResultSet coldef =  statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"+tablename+"' AND COLUMN_NAME ='"+column_name+"'");
                                       if(coldef.getRow() > 0)
                                       {
                                           Log.e("colnames def", coldef.getRow()+" coldef.getRow()");
                                           while(coldef.next())
                                           {
                                               String COLUMN_TYPE = coldef.getString("COLUMN_TYPE");
                                               String IS_NULLABLE = coldef.getString("IS_NULLABLE");
                                               String coldescr = COLUMN_TYPE;
                                               if(!"".equals(IS_NULLABLE))
                                               {
                                                   if("YES".equals(IS_NULLABLE))
                                                   {
                                                       coldescr += " NULL";
                                                   }else
                                                   {
                                                       coldescr += " NOT NULL";
                                                   }

                                                   db.execSQL("ALTER TABLE "+tablename+" ADD COLUMN "+coldescr);
                                                   Log.e("colnames def", "ALTER TABLE "+tablename+" ADD COLUMN "+coldescr);

                                               }else
                                               {
                                                   Log.e("colnames def", COLUMN_TYPE+" "+IS_NULLABLE+" is empty");
                                               }

                                           }

                                       }*/
                                        db.execSQL("ALTER TABLE " + tablename + " ADD COLUMN " + column_name.trim() + " TEXT ");
                                        //Log.e("colnames def", "ALTER TABLE " + tablename + " ADD COLUMN " + column_name + " TEXT ");

                                    }
                                    chkcol.close();

                                    if ("POS_DOWN_AT".equals(column_name))
                                    {
                                        //Log.e("column_name PDA",  column_name );
                                        master.put(column_name, cl.DB_DATETIME_FORMAT(currentdttm));
                                    } else if ("POS_USER".equals(column_name))
                                    {
                                        //Log.e("column_name PU",  column_name );
                                        master.put(column_name, USERNAME);
                                    } else if ("POS_SERIAL_NO".equals(column_name) && ("ledgerdata".equals(tablename) || "newrevenue".equals(tablename)))
                                    {
                                        //Log.e("column_name PSN",  column_name );
                                        master.put(column_name, IMEI);
                                    }else
                                    {
                                        //Log.e("column_name all",  column_name );
                                        master.put(column_name, rs.getString(k));
                                    }
                                }

                                Log.e("keys_forupdate", keys_forupdate + " " + keysvalue_forupdate);

                                Cursor checkentry = db.rawQuery("SELECT * FROM " + tablename + " WHERE " + keys_forupdate, new String[]{});
                                int updateresult = 0;
                                if (checkentry.getCount() > 0)
                                {
                                    //Log.e("UPDATE", checkentry.getCount() + " ");

                                    updateresult = db.update(tablename, master, keys_forupdate, new String[]{});
                                    if (updateresult > 0) {
                                        if ("1".equals(rev_update)) {
                                            int updatecount = statementup.executeUpdate("UPDATE " + tablename + " SET POS_DOWN_BIT='0',POS_DOWN_AT='" + cl.DB_DATETIME_FORMAT(currentdttm) + "' WHERE " + keys_forupdate);
                                            //Log.e("rev_update_query", "UPDATE " + tablename + " set POS_DOWN_BIT='0',POS_DOWN_AT='" + cl.DB_DATETIME_FORMAT(currentdttm) + "' WHERE " + keys_forupdate);
                                            //Log.e("UPDATEONLBIT", String.valueOf(updatecount));
                                            if (updatecount > 0) {
                                                updated += 1;
                                                success += 1;
                                                //Log.e("ONLINESNC", "success");
                                                publishProgress("", "1","");
                                            } else {
                                                //Log.e("ONLINESNC", "failed in update");
                                                UNACK += 1;
                                                publishProgress(" \n\n#### DOWNLOAD UPDATE FAILED FOR "+tablename+" "+keys_forupdate, "0","");
                                            }
                                        } else {
                                            updated += 1;
                                            success += 1;
                                            //Log.e("ONLINESNC", "success");
                                            publishProgress("", "1","");
                                        }
                                    } else {
                                        failed += 1;
                                        //Log.e(tablename + " UPDATE", "FAILED" + iterator);
                                        publishProgress("\n\n#### DOWNLOAD UPDATE FAILED FOR "+tablename+" "+keys_forupdate, "0","");
                                        cancel(true);
                                    }

                                } else if (checkentry.getCount() == 0) {
                                    try
                                    {
                                        //Log.e("insert", checkentry.getCount() + " ");
                                        updateresult = (int) db.insert(tablename, null, master);
                                        if (updateresult != -1) {
                                            if ("1".equals(rev_update)) {
                                                int updatecount = statementup.executeUpdate("UPDATE " + tablename + " set POS_DOWN_BIT='0',POS_DOWN_AT='" + cl.DB_DATETIME_FORMAT(currentdttm) + "' WHERE " + keys_forupdate);
                                                //Log.e("rev_update_query", "UPDATE " + tablename + " set POS_DOWN_BIT='0',POS_DOWN_AT='" + cl.DB_DATETIME_FORMAT(currentdttm) + "' WHERE " + keys_forupdate);
                                                //Log.e("UPDATEONLBIT", String.valueOf(updatecount));
                                                if (updatecount > 0) {
                                                    updated += 1;
                                                    success += 1;
                                                    //Log.e("ONLINESNC", "success");
                                                    publishProgress("", "1","");
                                                } else {
                                                    //Log.e("ONLINESNC", "failed in update");
                                                    UNACK += 1;
                                                    publishProgress("\n\n#### DOWNLOAD UPDATE FAILED FOR "+tablename+" "+keys_forupdate, "0","");
                                                }
                                            } else {
                                                inserted += 1;
                                                success += 1;
                                                //Log.e("ONLINESNC", "success");
                                                publishProgress("", "1","");
                                            }
                                        } else {
                                            failed += 1;
                                            //Log.e("INSERT", "INSERTFAILED");
                                            publishProgress("\n\n####DOWNLOAD insert FAILED FOR "+tablename+" "+keysvalue_forupdate, "0","");
                                            cancel(true);
                                        }
                                    } catch (SQLiteException se) {
                                        publishProgress("\n####DOWNLOAD insert FAILED FOR " + se.getMessage() + "  END \n", "0","");
                                        //Log.e("MYSQL EXCEPTION", se.getMessage() + "  \nbreak\n" + se.getCause());
                                        cancel(true);
                                        MESSAGE += se.getMessage() + "\n" + se.getCause();
                                        return MESSAGE;
                                    }
                                }
                                checkentry.close();

                                if (isCancelled())
                                    break;
                            }

                            MESSAGE += "TOTAL - " + String.valueOf(total) + " UPDATED - " + String.valueOf(updated) + " INSERTED - " + String.valueOf(inserted) + " FAILED - " + String.valueOf(failed) + " UNACKNOWLEDGE - " + String.valueOf(UNACK);

                            publishProgress(MESSAGE, "0","");
                            publishProgress("OVERALL PROGRESS "+currenttabno+"/ "+total_tables, "0","VAL");
                            long insval = 0;
                            if(total == inserted+updated+UNACK)
                            {
                                //publishProgress("OVERALL PROGRESS "+currenttabno+"/ "+total_tables, "0","VAL");
                                Date datetime = new Date(System.currentTimeMillis());
                                ContentValues syncupdate = new ContentValues();
                                syncupdate.put("user", USERNAME);
                                syncupdate.put("serial_no", IMEI);
                                syncupdate.put("DateTime", cl.DB_DATETIME_FORMAT(datetime));
                                syncupdate.put("remarks", MESSAGE.trim());
                                syncupdate.put("type", tablename + "_DOWNLOAD");
                                insval = cl.INSERT_SYNC(syncupdate);
                            }else
                            {
                                //commentedLog.e("SYNCMYDB",tablename+" DOWNLOADING CANCELLED BY USER");
                                publishProgress(tablename+" DOWNLOADING CANCELLED BY USER", "0","");

                            }
                            //Log.e("insertsync", "insert "+insval+" total"+total+" inserted"+inserted+" "+updated+" "+UNACK);
                        }else
                        {
                            publishProgress("OVERALL PROGRESS "+currenttabno+"/ "+total_tables, "0","VAL");
                            Date datetime = new Date(System.currentTimeMillis());
                            ContentValues syncupdate = new ContentValues();
                            syncupdate.put("user", USERNAME);
                            syncupdate.put("serial_no", IMEI);
                            syncupdate.put("DateTime", cl.DB_DATETIME_FORMAT(datetime));
                            syncupdate.put("remarks", "NO DATA");
                            syncupdate.put("type", tablename+"_DOWNLOAD");
                            long insval = cl.INSERT_SYNC(syncupdate);
                            //Log.e("insertsync", "insert "+insval);

                        }
                    } catch (Exception e) {
                        publishProgress("\nstart " + e.getMessage() + "  END \n", "0","");
                        //Log.e("MYSQL EXCEPTION", e.getMessage() + "  \nbreak\n" + e.getCause());
                        cancel(true);
                        MESSAGE += e.getMessage() + "\n" + e.getCause();
                        return MESSAGE;
                    }
                    ONLINEDBCON = true;
                }

            } catch (Exception e) {

                error = e.toString();
                MESSAGE += e.getMessage() ;
                //Log.e("MYSQL EXCEPTION",e.getMessage() + "  \nbreak\n" + e.getCause());
                //publishProgress("\nstart "+e.getLocalizedMessage() + "  \nbreak\n" + e.getCause()+"  END \n","0");
                publishProgress("\n"+e.getMessage(),"0","");
                ONLINEDBCON = false;
                cancel(true);
                return MESSAGE;

            }
            return MESSAGE;
        }
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        protected void onProgressUpdate(String...value) {
            Log.e("DOWNLOADSTAT","DOWNLOADED " +value[0]);
            if (value[0] != "" && display_log == 0) {
                //Toast.makeText(context,value[0],Toast.LENGTH_SHORT).show();
                show_snack(value[0],context);
            }
            if (!"".equals(value[2].trim()) && overall_progress != null)
            {
                int prog = overall_progress.getProgress()+1;

                logtextview.setText("OVERALL PROGRESS "+prog+"/ "+total_tables);
                overall_progress.setProgress(prog);

                if(total_tables == overall_progress.getProgress())
                {
                    overall_progress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#3ba35a")));
                    logtextview.setTextColor(Color.parseColor("#3ba35a"));
                    cl.hide_loadingview(this.context,"Download finished.");

                    if(display_log == 1)
                    {

                        download.setEnabled(true);
                        upload.setEnabled(true);

                    }

                    /*try {
                        connection.close();
                        Log.e("force connection","closed");
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                        Log.e("force connection"," not closed"+throwables.getMessage());
                    }
                    checkdb();*/
                }else
                {
                    overall_progress.setProgressTintList(ColorStateList.valueOf(Color.RED));
                    logtextview.setTextColor(Color.RED);
                }
            }
            if(display_log == 1)
            {

                if (value[0] != "") {
                    create_dynamic_textview(this.context, value[0], scroll_log);
                }
                int prog = progress.getProgress() + Integer.parseInt(value[1]);
                textview.setText(String.valueOf(prog) + " / " + String.valueOf(total));
                progress.setProgress(prog);

                if (prog == total && total >= 0) {
                    tablename.append(" FINISHED "+currenttabno+" / "+total_tables);
                    tablename.setTextColor(Color.GREEN);

                }
            }

        }

        @Override
        protected void onPostExecute(String result)
        {
            String sp_tablename = this.name;                  //table name for shared preferences
            if("pwd_user".equals(this.name))
            {
                sp_tablename = "pwd_user_download";
            }

            if("ledgerdata".equals(this.name))
            {
                sp_tablename = "ledgerdata_download";
            }

            if("ADD_AMOUNT".equals(this.name))
            {
                sp_tablename = "ADD_AMOUNT_download";
            }

            SharedPreferences sp_sync = context.getSharedPreferences("spsync", Context.MODE_PRIVATE);
            String this_tablename = sp_sync.getString(sp_tablename,"");
            int ct = 0;
            if("".equals(this_tablename) || "0".equals(this_tablename) || Integer.valueOf(this_tablename) < 0)
            {
                ct = 0;
            }else
            {
                ct = Integer.valueOf(this_tablename) - 1;
            }
            SharedPreferences.Editor spsynceditor = sp_sync.edit();
            spsynceditor.putString(sp_tablename, String.valueOf(ct));
            spsynceditor.commit();
            if(display_log == 1)
            {
                create_dynamic_textview(this.context, result, scroll_log);
            }

        }
        @Override
        protected void onCancelled()
        {
            String sp_tablename = this.name;                 //table name for shared preferences
            if("pwd_user".equals(this.name))
            {
                sp_tablename = "pwd_user_download";
            }

            if("ledgerdata".equals(this.name))
            {
                sp_tablename = "ledgerdata_download";
            }

            if("ADD_AMOUNT".equals(this.name))
            {
                sp_tablename = "ADD_AMOUNT_download";
            }

            SharedPreferences sp_sync = context.getSharedPreferences("spsync", Context.MODE_PRIVATE);
            String this_tablename = sp_sync.getString(sp_tablename,"");
            int ct = 0;
            if("".equals(this_tablename) || "0".equals(this_tablename) || Integer.valueOf(this_tablename) < 0)
            {
                ct = 0;
            }else
            {
                ct = Integer.valueOf(this_tablename) - 1;
            }
            SharedPreferences.Editor spsynceditor = sp_sync.edit();
            spsynceditor.putString(sp_tablename, String.valueOf(ct));
            spsynceditor.commit();
            //commentedLog.e("onCancelled",sp_tablename+" "+currenttabno+context+total_tables+"syncmydb onCancelled");
            if(display_log == 1)
            {
                create_dynamic_textview(this.context, name+" "+currenttabno+context+total_tables+"DOWNLOADING CANCELLED BY USER", scroll_log);

                tablename.append(" INCOMPLETE");
                tablename.setTextColor(Color.RED);

                cl.hide_loadingview(this.context,"Download cancelled.");
                download.setEnabled(true);
                upload.setEnabled(true);
            }
        }
    }

    class syncmydb_specific extends AsyncTask<String,String,String> {
        ProgressBar progress;
        Context context;
        TextView textview;
        TextView tablename;
        LinearLayout linlayout;
        String name = "";
        int total = -1;

        public syncmydb_specific(String tablename,Context context,LinearLayout linlay)
        {

            this.name = tablename;
            this.context = context;
            this.linlayout = linlay;
        }

        String records = "", error = "";

        protected void onPreExecute()
        {
            if(display_log == 0)
            {
                textview = new TextView(context);
                textview.setTextSize(16);
                textview.setText("");
                linlayout.removeAllViews();
            }
            if(display_log == 1)
            {
                tablename = new TextView(context);
                tablename.setTextSize(16);
                tablename.setText(name);
                RelativeLayout.LayoutParams paramsnm = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                tablename.setLayoutParams(paramsnm);

                lin_layout.removeView(tablename);
                lin_layout.addView(tablename);

                progress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);

                progress.setProgress(0);
                progress.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                params1.addRule(RelativeLayout.CENTER_IN_PARENT);
                lin_layout.removeView(progress);
                lin_layout.addView(progress, params1);

                textview = new TextView(context);
                textview.setTextSize(16);
                textview.setText("");

                RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 50);
                params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                textview.setLayoutParams(params2);
                textview.setGravity(Gravity.RIGHT);
                textview.setRight(100);
                lin_layout.removeView(textview);
                lin_layout.addView(textview);

                progress.setMax(100);
                progress.setProgress(0);
                create_dynamic_textview(context, "Starting download", scroll_log);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String MESSAGE = "";String ERROR;
            try {

                if (!isDbConnected(connection)) {
                    publishProgress("CONNECTION TO SERVER FAILED.COULD NOT DOWNLOAD DATA","0");
                    Log.e("MYSQL", "CONNECTION TO MYSQL FAILED");
                    cancel(true);
                } else
                {
                    Log.e("MYSQL", "CONNECTION TO MYSQL SUCCESSFUL");
                    //publishProgress("CONNECTION TO MYSQL SUCCESSFUL","0");

                    CALC cl = new CALC(context);

                    SQLiteDatabase db = cl.getdb();

                    statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    Statement statementup = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    String tablename = params[0];

                    String VAR1 = params[1];
                    String VAR2 = params[2];
                    String KEY = "";
                    String ret = "";
                    String keyvalue = "";
                    String down_query = "";
                    int inserted = 0;
                    int updated = 0;
                    int failed = 0;
                    int success = 0;
                    int UNACK = 0;

                    if("ledgerdata".equals(tablename))
                    {
                        KEY = "PERDAY1,CONSUMERID";
                        down_query = "SELECT * FROM ledgerdata WHERE CONSUMERID ='"+ VAR1 +"' AND ISSDATE IS NOT NULL AND BILLNO IS NOT NULL ORDER BY PERDAY1 DESC LIMIT 1";
                    }else if("masterdetails".equals(tablename))
                    {
                        KEY = "CONSUMERID";
                        down_query = "SELECT * FROM masterdetails WHERE CONSUMERID ='"+ VAR1 +"'";
                    }else if("issue_date".equals(tablename))
                    {
                        KEY = "PERDAY1,ZONE";
                        down_query = "SELECT * FROM issue_date WHERE PERDAY1 ='"+ VAR1 +"' AND ZONE='"+VAR2+"'";
                    }


                    try {

                        String rev_update = "0";


                        ResultSet rs = statement.executeQuery(down_query);

                        rs.last();
                        //Log.e(tablename, "count" + rs.last() + rs.getRow());
                        total = rs.getRow();

                        if (display_log == 1) {
                            progress.setMax(total);
                        }

                        rs.beforeFirst();

                        int iterator = 0;
                        ContentValues master = new ContentValues();

                        if (total > 0)
                        {
                            publishProgress(total+" RECORDS AVIALABLE FOR "+tablename,"0");
                            ResultSetMetaData colnames = rs.getMetaData();
                            String card = "";
                            while (rs.next()) {
                                card="CARDVIEW"+card;
                                iterator++;

                                master.clear();
                                String[] keyupdateval = KEY.split(",");
                                String keys_forupdate = "";
                                String keysvalue_forupdate = "";
                                for (int i = 0; i < keyupdateval.length; i++) {
                                    keys_forupdate += keyupdateval[i] + " = '" + rs.getString(keyupdateval[i]) + "' AND ";

                                    keysvalue_forupdate += rs.getString(keyupdateval[i]) + ", ";

                                }

                                keys_forupdate = keys_forupdate.substring(0, keys_forupdate.length() - 4);

                                keysvalue_forupdate = keysvalue_forupdate.substring(0, keysvalue_forupdate.length() - 2);//keysvalue_forupdate.replace(",","") ;

                                Date currentdttm = new Date(System.currentTimeMillis());

                                //Log.e("colnames.getCoount()", colnames.getColumnCount() + " ");
                                String disp_msg = "";
                                for (int k = 1; k <= colnames.getColumnCount(); k++)
                                {
                                    String column_name = "";
                                    column_name = colnames.getColumnName(k);
                                    Cursor chkcol = null;
                                    chkcol = db.rawQuery("Select * from " + tablename + " limit 1", null);
                                    int colIndex = 0;

                                    colIndex = chkcol.getColumnIndex(column_name.trim());
                                    if (colIndex == -1)//k==1 &&
                                    {
                                        //Log.e("colnames def", column_name + " COL DOESNOT EXISTS");
                                        db.execSQL("ALTER TABLE " + tablename + " ADD COLUMN " + column_name.trim() + " TEXT ");
                                        //Log.e("colnames def", "ALTER TABLE " + tablename + " ADD COLUMN " + column_name + " TEXT ");

                                    }
                                    chkcol.close();

                                    if ("POS_DOWN_AT".equals(column_name)) {
                                        master.put(column_name, cl.DB_DATETIME_FORMAT(currentdttm));
                                    } else if ("POS_USER".equals(column_name)) {
                                        master.put(column_name, USERNAME);
                                    } else if ("POS_SERIAL_NO".equals(column_name) || "SERIAL_NO".equals(column_name)) {
                                        master.put(column_name, IMEI);
                                    } else {
                                        master.put(column_name, rs.getString(k));
                                    }

                                    if("ledgerdata".equals(tablename))
                                    {
                                        if("TEMP_ISSDATE".equals(column_name))
                                        {
                                            if(!"".equals(rs.getString(k)) && rs.getString(k) != null)
                                            {
                                                disp_msg += "\nISSUE DATE : "+cl.DATETODISPLAY(cl.DATEFROMSTRING(rs.getString(k)));
                                            }else
                                            {
                                                disp_msg += "\nISSUE DATE : ";
                                            }
                                        }
                                        if("TEMP_LASTDATE".equals(column_name))
                                        {
                                            if(!"".equals(rs.getString(k)) && rs.getString(k) != null)
                                            {
                                                disp_msg += "\nLAST DATE : "+cl.DATETODISPLAY(cl.DATEFROMSTRING(rs.getString(k)));
                                            }else
                                            {
                                                disp_msg += "\nLAST DATE : ";
                                            }
                                        }
                                        if("DATE1".equals(column_name))
                                        {
                                            if(!"".equals(rs.getString(k)) && rs.getString(k) != null)
                                            {
                                                disp_msg += "\nFROM DATE : "+cl.DATETODISPLAY(cl.DATEFROMSTRING(rs.getString(k)));
                                            }else
                                            {
                                                disp_msg += "\nFROM DATE : ";
                                            }
                                        }
                                        if("DATE2".equals(column_name))
                                        {
                                            if(!"".equals(rs.getString(k)) && rs.getString(k) != null)
                                            {
                                                disp_msg += "\nTO DATE : "+cl.DATETODISPLAY(cl.DATEFROMSTRING(rs.getString(k)));
                                            }else
                                            {
                                                disp_msg += "\nTO DATE : ";
                                            }
                                        }
                                        if("CONSUMERID".equals(column_name))
                                        {
                                            disp_msg += "\nCONSUMERID : "+rs.getString(k);
                                        }
                                        if("CODY".equals(column_name))
                                        {
                                            disp_msg += "\nCODY : "+rs.getString(k);
                                        }
                                        if("NAME".equals(column_name))
                                        {
                                            disp_msg += "\nNAME : "+rs.getString(k);
                                        }
                                        if("CATEGORY".equals(column_name))
                                        {
                                            disp_msg += "\nCATEGORY : "+rs.getString(k);
                                        }
                                        if("NOFLAT".equals(column_name))
                                        {
                                            disp_msg += "\nFLATS : "+rs.getString(k);
                                        }
                                        if("CUR_READ".equals(column_name))
                                        {
                                            disp_msg += "\nCURRENT : "+rs.getString(k);
                                        }
                                        if("METER_READ".equals(column_name))
                                        {
                                            disp_msg += "\nPREVIOUS : "+rs.getString(k);
                                        }
                                        if("USED".equals(column_name))
                                        {
                                            disp_msg += "\nUNITS BILLED : "+rs.getString(k);
                                        }
                                        if("USED1".equals(column_name))
                                        {
                                            disp_msg += "\nPERMONTH : "+rs.getString(k);
                                        }
                                        if("WC".equals(column_name))
                                        {
                                            disp_msg += "\nWATER CHARGES : "+rs.getString(k);
                                        }
                                        if("METRATE".equals(column_name))
                                        {
                                            disp_msg += "\nMETER RENT : "+rs.getString(k);
                                        }
                                        if("SAMT".equals(column_name))
                                        {
                                            disp_msg += "\nSEWERAGE AMOUNT : "+rs.getString(k);
                                        }
                                        if("ARREARS".equals(column_name))
                                        {
                                            disp_msg += "\nARREARS : "+rs.getString(k);
                                        }
                                        if("DC".equals(column_name))
                                        {
                                            disp_msg += "\nCREDIT : "+rs.getString(k);
                                        }
                                        if("NET".equals(column_name))
                                        {
                                            disp_msg += "\nNET : "+rs.getString(k);
                                        }

                                    }
                                }
                                publishProgress(disp_msg , card);
                                Log.e("keys_forupdate", keys_forupdate.toUpperCase() + " " + keysvalue_forupdate.toUpperCase());

                                Cursor checkentry = db.rawQuery("SELECT * FROM " + tablename + " WHERE " + keys_forupdate, new String[]{});
                                int updateresult = 0;
                                if (checkentry.getCount() > 0)
                                {
                                    updateresult = db.update(tablename, master, keys_forupdate, new String[]{});
                                    if (updateresult > 0) {
                                        if ("1".equals(rev_update)) {
                                            int updatecount = statementup.executeUpdate("UPDATE " + tablename + " SET POS_DOWN_BIT='0',POS_DOWN_AT='" + cl.DB_DATETIME_FORMAT(currentdttm) + "' WHERE " + keys_forupdate);
                                            //Log.e("rev_update_query", "UPDATE " + tablename + " set POS_DOWN_BIT='0',POS_DOWN_AT='" + cl.DB_DATETIME_FORMAT(currentdttm) + "' WHERE " + keys_forupdate);
                                            //Log.e("UPDATEONLBIT", String.valueOf(updatecount));
                                            if (updatecount > 0) {
                                                updated += 1;
                                                success += 1;
                                                //Log.e("ONLINESNC", "success");
                                                publishProgress("", "1");
                                            } else {
                                                //Log.e("ONLINESNC", "failed in update");
                                                UNACK += 1;
                                            }
                                        } else {
                                            updated += 1;
                                            success += 1;
                                            //Log.e("ONLINESNC", "success");
                                            publishProgress("", "1");
                                        }
                                    } else {
                                        failed += 1;
                                        //Log.e(tablename + " UPDATE", "FAILED" + iterator);
                                        cancel(true);
                                    }

                                } else if (checkentry.getCount() == 0) {
                                    //Log.e("insert", checkentry.getCount() + " ");
                                    updateresult = (int) db.insert(tablename, null, master);
                                    if (updateresult != -1) {
                                        if ("1".equals(rev_update))
                                        {
                                            int updatecount = statementup.executeUpdate("UPDATE " + tablename + " set POS_DOWN_BIT='0',POS_DOWN_AT='" + cl.DB_DATETIME_FORMAT(currentdttm) + "' WHERE " + keys_forupdate);
                                            //Log.e("rev_update_query", "UPDATE " + tablename + " set POS_DOWN_BIT='0',POS_DOWN_AT='" + cl.DB_DATETIME_FORMAT(currentdttm) + "' WHERE " + keys_forupdate);
                                            //Log.e("UPDATEONLBIT", String.valueOf(updatecount));
                                            if (updatecount > 0) {
                                                updated += 1;
                                                success += 1;
                                                //Log.e("ONLINESNC", "success");
                                                publishProgress("", "1");
                                            } else {
                                                //Log.e("ONLINESNC", "failed in update");
                                                UNACK += 1;
                                            }
                                        } else
                                        {
                                            inserted += 1;
                                            success += 1;
                                            //Log.e("ONLINESNC", "success");
                                            publishProgress("", "1");
                                        }
                                    } else
                                    {
                                        failed += 1;
                                        //Log.e("INSERT", "INSERTFAILED");
                                        cancel(true);
                                    }
                                }
                                checkentry.close();

                                if (isCancelled())
                                    break;
                            }

                            MESSAGE += "TOTAL - " + String.valueOf(total) + " UPDATED - " + String.valueOf(updated) + " INSERTED - " + String.valueOf(inserted) + " FAILED - " + String.valueOf(failed) + " UNACKNOWLEDGE - " + String.valueOf(UNACK);

                            publishProgress(MESSAGE, "0");
                            long insval = 0;
                            if(total == inserted+updated+UNACK)
                            {
                                Date datetime = new Date(System.currentTimeMillis());
                                ContentValues syncupdate = new ContentValues();
                                syncupdate.put("user", USERNAME);
                                syncupdate.put("serial_no", IMEI);
                                syncupdate.put("DateTime", cl.DB_DATETIME_FORMAT(datetime));
                                syncupdate.put("remarks", MESSAGE.trim());
                                syncupdate.put("type", tablename + "_DOWNLOAD");
                                insval = cl.INSERT_SYNC(syncupdate);
                            }else
                            {
                                publishProgress("CANCELLED BY USER", "0");
                            }
                            //Log.e("insertsync", "insert "+insval+" total"+total+" inserted"+inserted+" "+updated+" "+UNACK);
                        }else
                        {
                            Date datetime = new Date(System.currentTimeMillis());
                            ContentValues syncupdate = new ContentValues();
                            syncupdate.put("user", USERNAME);
                            syncupdate.put("serial_no", IMEI);
                            syncupdate.put("DateTime", cl.DB_DATETIME_FORMAT(datetime));
                            syncupdate.put("remarks", "NO DATA");
                            syncupdate.put("type", tablename+"_DOWNLOAD");
                            long insval = cl.INSERT_SYNC(syncupdate);
                            //Log.e("insertsync", "insert "+insval);

                        }
                    } catch (Exception e)
                    {
                        publishProgress("\nstart " + e.getMessage() + "  END \n", "0");
                        //Log.e("MYSQL EXCEPTION", e.getMessage() + "  \nbreak\n" + e.getCause());
                        cancel(true);
                        MESSAGE += e.getMessage() ;
                        return MESSAGE;
                    }
                    ONLINEDBCON = true;
                }

            } catch (Exception e) {

                error = e.toString();
                MESSAGE += e.getMessage();
                //Log.e("MYSQL EXCEPTION",e.getMessage() + "  \nbreak\n" + e.getCause());
                //publishProgress("\nstart "+e.getLocalizedMessage() + "  \nbreak\n" + e.getCause()+"  END \n","0");
                publishProgress("\nCOULD NOT CONNECT TO SERVER.PLEASE TRY AGAIN LATER \n","0");
                ONLINEDBCON = false;
                cancel(true);
                return MESSAGE;

            }
            return MESSAGE;
        }

        protected void onProgressUpdate(String...value) {

            if (value[0] != "" && display_log == 0) {
                //Toast.makeText(context,value[0],Toast.LENGTH_LONG).show();
                create_dynamic_textview_cardview(context, value[0],value[1], linlayout);
                show_snack(value[0],context);
            }

            if(display_log == 1)
            {
                if (value[0] != "") {
                    create_dynamic_textview(context, value[0], scroll_log);
                }
                int prog = progress.getProgress() + Integer.parseInt(value[1]);
                textview.setText(String.valueOf(prog) + " / " + String.valueOf(total));
                progress.setProgress(prog);

                if (prog == total && total >= 0) {
                    tablename.append(" FINISHED");
                    tablename.setTextColor(Color.GREEN);
                }
            }

        }

        @Override
        protected void onPostExecute(String result)
        {
            if(display_log == 1)
            {
                create_dynamic_textview(context, result, scroll_log);
            }

        }
        @Override
        protected void onCancelled()
        {
            if(display_log == 1)
            {
                create_dynamic_textview(context, "CANCELLED BY USER", scroll_log);

                tablename.append(" INCOMPLETE");
                tablename.setTextColor(Color.RED);

                //CALC.SHOWMSG("OPERATION CANCELLED BY USER","RESULT",syncdb.this);

            }
            //db.close();
            /*try
            {
                //connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }*/

        }
    }

    public void show_snack(String data,Context context)
    {
        /*View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        View v = rootView.findViewById(R.id.scrollview_main);
        View vs = rootView.getRootView();

        Snackbar.make(vs , data, Snackbar.LENGTH_LONG)
                .setAction("CLOSE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(Color.RED)
                .show();*/
    }

    class admin_queries extends AsyncTask<String,String,String> {
        ProgressDialog progressDialog;
        Context context;
        TextView textview;
        TextView tablename;
        String name = "";
        int total = -1;

        public admin_queries(String tablename,Context context)
        {

            this.name = tablename;
            this.context = context;
            display_log = 0;
        }

        String records = "", error = "";

        protected void onPreExecute()
        {
            if(display_log == 1)
            {
                progressDialog= ProgressDialog.show(context, "SYSTEM UPDATE","PLEASE WAIT...", true);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String MESSAGE = "";String ERROR;
            try {

                if (!isDbConnected(connection)) {
                    publishProgress("CONNECTION TO SERVER FAILED.COULD NOT DOWNLOAD DATA","0");
                    Log.e("MYSQL", "CONNECTION TO MYSQL FAILED");
                    cancel(true);
                } else
                {
                    Log.e("MYSQL", "CONNECTION TO MYSQL SUCCESSFUL");
                    //publishProgress("CONNECTION TO MYSQL SUCCESSFUL","0");

                    CALC cl = new CALC(context);

                    SQLiteDatabase db = cl.getdb();

                    statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    Statement statementup = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    String tablename = params[0];

                    String VAR1 = params[1];
                    String VAR2 = params[2];
                    String KEY = "";
                    String ret = "";
                    String keyvalue = "";
                    String down_query = "";
                    int inserted = 0;
                    int updated = 0;
                    int failed = 0;
                    int success = 0;
                    int UNACK = 0;

                    down_query = "SELECT * FROM admin_operations WHERE POS_SERIAL_NO='"+IMEI+"' AND STATUS='1' AND POS_DOWN_BIT='1'";
                    Log.e("admin queries", down_query);
                    Date currentdttm = new Date(System.currentTimeMillis());
                    try {

                        String rev_update = "0";


                        ResultSet rs = statement.executeQuery(down_query);

                        rs.last();
                        Log.e(tablename, "count" + rs.last() + rs.getRow());
                        total = rs.getRow();

                        rs.beforeFirst();

                        String upload_status = "";
                        int iterator = 0;
                        ContentValues master = new ContentValues();

                        if (total > 0)
                        {

                            ResultSetMetaData colnames = rs.getMetaData();

                            while (rs.next())
                            {

                                iterator++;

                                master.clear();
                                String ID = rs.getString("ID");
                                String QUERY = rs.getString("QUERY");
                                String TABLE_NAME = rs.getString("TABLE_NAME");
                                Log.e("admin queries", QUERY +" " +TABLE_NAME + " ");
                                try
                                {
                                    Cursor deleted_entries =null;
                                    if("DELETE_PHOTOS_PATH".equals(TABLE_NAME))
                                    {
                                        Log.e("GOING HERE",   "DELETE_METER_PHOTOS " );
                                        upload_status = cl.DELETE_PHOTOS("",QUERY);

                                    }else if("DELETE_METER_PHOTOS".equals(TABLE_NAME))
                                    {
                                        Log.e("GOING HERE",   "DELETE_METER_PHOTOS " );
                                        upload_status = cl.DELETE_PHOTOS(QUERY,"");

                                    }else if("UPLOAD_DATABASE".equals(TABLE_NAME))
                                    {
                                        Log.e("GOING HERE",   "UPLOAD_DATABASE " );
                                        upload_status = UPLOAD_LOCALDB_ONLINE(context,IMEI,"");
                                    }else if("GET_LOG_FILE".equals(TABLE_NAME))
                                    {
                                        Log.e("GOING HERE",   "GET_LOG_FILE " );
                                        upload_status = UPLOAD_LOCALDB_ONLINE(context,IMEI,QUERY);
                                    }else if("GET_UPLOAD_STATUS".equals(TABLE_NAME))
                                    {
                                        Log.e("GOING HERE",   "GET_UPLOAD_STATUS " );
                                        upload_status = cl.GET_UPLOAD_DATA(USERNAME);
                                        Log.e("GET_UPLOAD_STATUS", upload_status +  " " );

                                    }else
                                    {
                                        deleted_entries = db.rawQuery(QUERY, new String[]{});
                                        Log.e("QUERY", QUERY + TABLE_NAME + " " + deleted_entries.getCount());
                                    }

                                    Log.e("rev_update_query", "UPDATE admin_operations set QUERY_RESPONSE='"+QUERY + TABLE_NAME +" "+upload_status+"', POS_DOWN_BIT='0',POS_DOWN_AT='" + cl.DB_DATETIME_FORMAT(currentdttm) + "', POS_USER='"+USERNAME+"' WHERE ID='" + ID+"'");

                                    int updatecount = statementup.executeUpdate("UPDATE admin_operations set QUERY_RESPONSE='"+QUERY +" "+ TABLE_NAME +" "+upload_status+"', POS_DOWN_BIT='0',POS_DOWN_AT='" + cl.DB_DATETIME_FORMAT(currentdttm) + "', POS_USER='"+USERNAME+"' WHERE ID='" + ID+"'");
                                    String res = QUERY + TABLE_NAME+" "+upload_status;

                                    Log.e("UPDATEONLBIT", String.valueOf(updatecount));
                                    if (updatecount > 0) {
                                        updated += 1;
                                        success += 1;
                                        Log.e("ONLINESNC", "success");
                                        publishProgress("", "1");
                                    } else {
                                        Log.e("ONLINESNC", "failed in update");
                                        UNACK += 1;
                                    }
                                } catch (Exception e)
                                {
                                    MESSAGE += e.getMessage() + "\n" + e.getCause();
                                    int updatecount = statementup.executeUpdate("UPDATE admin_operations set QUERY_RESPONSE='"+MESSAGE+"',POS_DOWN_AT='" + cl.DB_DATETIME_FORMAT(currentdttm) + "', POS_USER='"+USERNAME+"' WHERE ID='" + ID+"'");
                                    Log.e("UPDATEONLBIT", String.valueOf(updatecount));
                                    if (updatecount > 0) {
                                        updated += 1;
                                        success += 1;
                                        Log.e("ONLINESNC", "success");
                                        publishProgress("", "1");
                                    } else {
                                        Log.e("ONLINESNC", "failed in update");
                                        UNACK += 1;
                                    }
                                }

                                if (isCancelled())
                                    break;
                            }

                            MESSAGE += "TOTAL - " + String.valueOf(total) + " UPDATED - " + String.valueOf(updated) + " INSERTED - " + String.valueOf(inserted) + " FAILED - " + String.valueOf(failed) + " UNACKNOWLEDGE - " + String.valueOf(UNACK);

                            publishProgress(MESSAGE, "0");
                            long insval = 0;
                            if(total == inserted+updated+UNACK)
                            {
                                Date datetime = new Date(System.currentTimeMillis());
                                ContentValues syncupdate = new ContentValues();
                                syncupdate.put("user", USERNAME);
                                syncupdate.put("serial_no", IMEI);
                                syncupdate.put("DateTime", cl.DB_DATETIME_FORMAT(datetime));
                                syncupdate.put("remarks", MESSAGE.trim());
                                syncupdate.put("type", tablename + "_DOWNLOAD");
                                insval = cl.INSERT_SYNC(syncupdate);
                            }else
                            {
                                publishProgress("CANCELLED BY USER", "0");
                            }
                            Log.e("insertsync", "insert "+insval+" total"+total+" inserted"+inserted+" "+updated+" "+UNACK);
                        }else
                        {
                            publishProgress("NO RECORDS FOUND", "0");

                        }
                    } catch (Exception e) {
                        publishProgress("\nstart " + e.getMessage()  + "  END \n", "0");
                        Log.e("MYSQL EXCEPTION", e.getMessage() + "  \nbreak\n" + e.getCause());
                        cancel(true);
                        MESSAGE += e.getMessage() + "\n" + e.getCause();
                        return MESSAGE;
                    }
                    ONLINEDBCON = true;
                }

            } catch (Exception e) {

                error = e.toString();
                MESSAGE += e.getMessage();
                Log.e("MYSQL EXCEPTION",e.getMessage() + "  \nbreak\n" + e.getCause());
                //publishProgress("\nstart "+e.getLocalizedMessage() + "  \nbreak\n" + e.getCause()+"  END \n","0");
                publishProgress("\nCOULD NOT CONNECT TO SERVER.PLEASE TRY AGAIN LATER \n","0");
                ONLINEDBCON = false;
                cancel(true);
                return MESSAGE;

            }
            return MESSAGE;
        }

        protected void onProgressUpdate(String...value) {
            if (value[0] != "" && display_log == 0) {
                //Toast.makeText(context,value[0],Toast.LENGTH_LONG).show();
                show_snack(value[0],context);
            }
            if(display_log == 1)
            {

            }

        }

        @Override
        protected void onPostExecute(String result)
        {
            if(display_log == 1)
            {
                this.progressDialog.dismiss();
            }

        }
        @Override
        protected void onCancelled()
        {
            if(display_log == 1)
            {
                this.progressDialog.dismiss();

            }

        }
    }

    class UPDATE_USER_STATUS extends AsyncTask<String,String,String> {
        ProgressDialog progressDialog;
        Context context;
        String username = "";
        int total = -1;

        public UPDATE_USER_STATUS(String username,Context context,String version)
        {

            this.username = username;
            this.context = context;
            display_log = 0;
        }

        String records = "", error = "";

        protected void onPreExecute()
        {
            if(display_log == 1)
            {
                progressDialog= ProgressDialog.show(context, "SYSTEM UPDATE","PLEASE WAIT...", true);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String MESSAGE = "";String ERROR;
            try {

                if (!isDbConnected(connection)) {
                    publishProgress("CONNECTION TO SERVER FAILED.COULD NOT DOWNLOAD DATA","0");
                    Log.e("MYSQL", "CONNECTION TO MYSQL FAILED");
                    cancel(true);
                } else
                {
                    Log.e("MYSQL", "CONNECTION TO MYSQL SUCCESSFUL");
                    //publishProgress("CONNECTION TO MYSQL SUCCESSFUL","0");

                    CALC cl = new CALC(context);

                    SQLiteDatabase db = cl.getdb();

                    statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    Statement statementup = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    String loginid = params[0];
                    String version = params[1];
                    String down_query = "";
                    int inserted = 0;
                    int updated = 0;
                    int failed = 0;
                    int success = 0;
                    int UNACK = 0;

                    down_query = "SELECT * FROM pwd_user WHERE serial_no='"+IMEI+"'";// AND username='"+loginid+"'
                    Log.e("UPDATE STAT", "SELECT * FROM pwd_user WHERE serial_no='"+IMEI+"'");// AND username='"+loginid+"'
                    Date currentdttm = new Date(System.currentTimeMillis());
                    try {

                        String rev_update = "0";

                        ResultSet rs = statement.executeQuery(down_query);

                        rs.last();
                        Log.e(username, "count" + rs.last() + rs.getRow());
                        total = rs.getRow();

                        rs.beforeFirst();

                        int iterator = 0;
                        ContentValues master = new ContentValues();

                        if (total > 0)
                        {

                            ResultSetMetaData colnames = rs.getMetaData();

                            while (rs.next())
                            {
                                iterator++;

                                master.clear();
                                String ID = rs.getString("id");
                                String QUERY = "";
                                String user_status = rs.getString("status");
                                String TABLE_NAME = rs.getString("username");
                                Log.e("admin queries", QUERY +" " +TABLE_NAME + " "+user_status);
                                String statupdate = "";
                                if("9".equals(user_status))
                                {
                                    statupdate = "1";
                                }else
                                {
                                    statupdate = user_status;
                                }

                                try {

                                    int updatecount = statementup.executeUpdate("UPDATE pwd_user set status='"+statupdate+"',version='"+version+"',upload_by='"+username+"',upload_at='"+cl.DB_DATETIME_FORMAT(currentdttm)+"' WHERE serial_no='"+IMEI+"' AND id='"+ID+"'");
                                    Log.e("rev_update_query", "UPDATE pwd_user set  status='"+statupdate+"',version='"+version+"' WHERE serial_no='"+IMEI+"' AND id='"+ID+"'");
                                    Log.e("UPDATEONLBIT", String.valueOf(updatecount));
                                    if (updatecount > 0) {
                                        /*ContentValues master = new ContentValues();
                                        master.put("status",statupdate);
                                        master.put("version",version);
                                        int updateresult = db.update("pwd_user", master, "serial_no='"+IMEI+"' AND id='"+ID+"'", new String[]{});
                                        if (updateresult > 0) {

                                        }*/
                                        String PATH = Environment.getExternalStorageDirectory() + "/Download/";
                                        File file = new File(PATH);
                                        File fdelete = new File(file, "msspwd.apk");
                                        if (fdelete.exists()) {
                                            if (fdelete.delete()) {
                                                publishProgress("file Deleted :"+fdelete.getName(), "0");
                                                Log.e("UPDATE","file Deleted :"+fdelete.getName());
                                            } else {
                                                publishProgress("file not Deleted :"+fdelete.getName(), "0");
                                                Log.e("UPDATE","file not Deleted :"+fdelete.getName());
                                            }
                                        }else {
                                            publishProgress("file not Deleted :"+fdelete.getName(), "0");
                                            Log.e("UPDATE","file not Deleted not found :"+fdelete.getName());
                                        }
                                        updated += 1;
                                        success += 1;
                                        Log.e("ONLINESNC", "success");
                                        publishProgress("USER STATUS UPDATE", "0");
                                    } else {
                                        Log.e("ONLINESNC", "failed in update");
                                        UNACK += 1;
                                    }
                                } catch (Exception e)
                                {
                                    publishProgress(e.toString(), "0");
                                }

                                if (isCancelled())
                                    break;
                            }

                            MESSAGE += "TOTAL - " + String.valueOf(total) + " UPDATED - " + String.valueOf(updated) + " INSERTED - " + String.valueOf(inserted) + " FAILED - " + String.valueOf(failed) + " UNACKNOWLEDGE - " + String.valueOf(UNACK);

                            publishProgress(MESSAGE, "0");
                            long insval = 0;
                            if(total == inserted+updated+UNACK)
                            {
                                Date datetime = new Date(System.currentTimeMillis());
                                ContentValues syncupdate = new ContentValues();
                                syncupdate.put("user", USERNAME);
                                syncupdate.put("serial_no", IMEI);
                                syncupdate.put("DateTime", cl.DB_DATETIME_FORMAT(datetime));
                                syncupdate.put("remarks", MESSAGE.trim());
                                syncupdate.put("type", "USER STAT UPDATE");
                                insval = cl.INSERT_SYNC(syncupdate);
                            }else
                            {
                                publishProgress("CANCELLED BY USER", "0");
                            }
                            Log.e("insertsync", "insert "+insval+" total"+total+" inserted"+inserted+" "+updated+" "+UNACK);
                        }else
                        {
                            publishProgress("NO RECORDS FOUND", "0");

                        }
                    } catch (Exception e) {
                        publishProgress("\nstart " + e.getMessage() + "  \nbreak\n" + e.getCause() + "  END \n", "0");
                        Log.e("MYSQL EXCEPTION", e.getMessage() + "  \nbreak\n" + e.getCause());
                        cancel(true);
                        MESSAGE += e.getMessage() + "\n" + e.getCause();
                        return MESSAGE;
                    }
                    ONLINEDBCON = true;
                }

            } catch (Exception e) {

                error = e.toString();
                MESSAGE += e.getMessage() + "\n" + e.getCause();
                Log.e("MYSQL EXCEPTION",e.getMessage() + "  \nbreak\n" + e.getCause());
                //publishProgress("\nstart "+e.getLocalizedMessage() + "  \nbreak\n" + e.getCause()+"  END \n","0");
                publishProgress("\nCOULD NOT CONNECT TO SERVER.PLEASE TRY AGAIN LATER \n","0");
                ONLINEDBCON = false;
                cancel(true);
                return MESSAGE;

            }
            return MESSAGE;
        }

        protected void onProgressUpdate(String...value) {
            if (value[0] != "" && display_log == 0) {
                //Toast.makeText(context,value[0],Toast.LENGTH_LONG).show();
                show_snack(value[0],context);
            }
            if(display_log == 1)
            {

            }

        }

        @Override
        protected void onPostExecute(String result)
        {
            if(display_log == 1)
            {
                this.progressDialog.dismiss();
            }
        }
        @Override
        protected void onCancelled()
        {
            if(display_log == 1)
            {
                this.progressDialog.dismiss();
            }
        }
    }

    class scanrcpt extends AsyncTask<String,String,String> {
        ProgressDialog progressDialog;
        Context context;
        TextView textview;
        TextView tablename;
        String name = "";
        int total = -1;

        public scanrcpt(String tablename,Context context)
        {

            this.name = tablename;
            this.context = context;
            display_log = 0;
        }

        String records = "", error = "";

        protected void onPreExecute()
        {
            progressDialog= ProgressDialog.show(context, "SYSTEM UPDATE","PLEASE WAIT...", true);

        }

        @Override
        protected String doInBackground(String... params) {

            String MESSAGE = "";String ERROR;
            try {

                if (!isDbConnected(connection)) {
                    publishProgress("CONNECTION TO SERVER FAILED.COULD NOT DOWNLOAD DATA","0");
                    Log.e("MYSQL", "CONNECTION TO MYSQL FAILED");
                    cancel(true);
                } else
                {
                    Log.e("MYSQL", "CONNECTION TO MYSQL SUCCESSFUL");
                    //publishProgress("CONNECTION TO MYSQL SUCCESSFUL","0");

                    CALC cl = new CALC(context);

                    SQLiteDatabase db = cl.getdb();

                    statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    Statement statementup = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    String CONSUMERID = params[0];
                    String ID = params[1];

                    Log.e("SEARCH CONS", "SELECT ledgerdata.NAME,ledgerdata.BILLNO,ledgerdata.DIV_NM,ledgerdata.SUBDIV,barcode_data.* FROM `barcode_data` left join ledgerdata on barcode_data.CONSUMERID=ledgerdata.CONSUMERID AND barcode_data.PERDAY1=ledgerdata.PERDAY1 where barcode_data.CONSUMERID='"+CONSUMERID+"' and barcode_data.POS_TABLE_ID='"+ID+"'");
                    ResultSet rs = statement.executeQuery("SELECT ledgerdata.NAME,ledgerdata.BILLNO,ledgerdata.DIV_NM,ledgerdata.SUBDIV,barcode_data.* FROM `barcode_data` left join ledgerdata on barcode_data.CONSUMERID=ledgerdata.CONSUMERID AND barcode_data.PERDAY1=ledgerdata.PERDAY1 where barcode_data.CONSUMERID='"+CONSUMERID+"' and barcode_data.POS_TABLE_ID='"+ID+"'");

                    ResultSetMetaData rsmd = rs.getMetaData();
                    String result = new String();

                    Date datetime = new Date(System.currentTimeMillis());
                    rs.last();
                    int totalrows = rs.getRow();
                    rs.beforeFirst();
                    Log.e("totalrows"," "+totalrows);
                    if(totalrows > 0)
                    {
                        while (rs.next())
                        {
                            String NAME = rs.getString("NAME");
                            Log.e("fetch res","NAME"+NAME);
                            String BILLNO = rs.getString("BILLNO");
                            String DIV_NM = rs.getString("DIV_NM");
                            String SUBDIV = rs.getString("SUBDIV");
                            String POS_TABLE_ID = rs.getString("POS_TABLE_ID");
                            String ISSDATE = rs.getString("ISSDATE");
                            String TEMP_ISSDATE = rs.getString("TEMP_ISSDATE");
                            String PERDAY1 = rs.getString("PERDAY1");
                            String NET = rs.getString("NET");
                            String WC = rs.getString("WC");
                            String MR = rs.getString("MR");
                            String SEW = rs.getString("SEW");
                            String ARREARS = rs.getString("ARREARS");
                            String POS_SERIAL_NO = rs.getString("POS_SERIAL_NO");
                            Log.e("fetch res"," "+NAME+BILLNO+DIV_NM);

                            //int res = st.executeUpdate("INSERT INTO scan_online_recipt ('SCROLL_ID', 'CONSUMERID', 'BILLNO', 'divi', 'subdiv', 'Issue_Date', 'RECEIPTNO', 'RCPTDT', 'RECEIPT_TYPE', 'RCODE', 'RAMT', 'Bank_ID', 'BANKNAME', 'SCROLLNUM', 'SCROLLDATE', 'WATER_CHARGE', 'METER_RENT', 'ARREAS', 'OUTSTATION', 'SCROLL_UPLOAD', 'SCAN_TYPE', 'upload_by', 'upload_at', 'posted') VALUES (NULL,'"+CONSUMERID+"','"+BILLNO+"','"+DIV_NM+"','"+SUBDIV+"','"+ISSDATE+"','ONLINE','"+cl.DB_DATETIME_FORMAT(datetime)+"','','','"+NET+"','"+WC+"','"+MR+"','"+ARREARS+"','0','',''");
                            Log.e("scan result", "INSERT INTO scan_online_recipt (SCROLL_ID, CONSUMERID, BILLNO, divi, subdiv, Issue_Date, RECEIPTNO, RCPTDT, RECEIPT_TYPE, RCODE, RAMT, Bank_ID, BANKNAME, SCROLLNUM, SCROLLDATE, WATER_CHARGE, METER_RENT, ARREAS, OUTSTATION, SCROLL_UPLOAD, SCAN_TYPE, upload_by, upload_at, posted) VALUES ('12345','" + CONSUMERID + "','" + BILLNO + "','" + DIV_NM + "','" + SUBDIV + "','" + ISSDATE + "','D','" + cl.DB_DATETIME_FORMAT(datetime) + "','DEPARTMENT','1','" + NET + "','12','ADARSH','12333','2021-12-01','" + WC + "','" + MR + "','" + ARREARS + "','0','0','BARCODE','ADMIN','" + cl.DB_DATETIME_FORMAT(datetime) + "','1')");
                            int res = statementup.executeUpdate("INSERT INTO scan_online_recipt (SCROLL_ID, CONSUMERID, BILLNO, divi, subdiv, Issue_Date, RECEIPTNO, RCPTDT, RECEIPT_TYPE, RCODE, RAMT, Bank_ID, BANKNAME, SCROLLNUM, SCROLLDATE, WATER_CHARGE, METER_RENT, ARREAS, OUTSTATION, SCROLL_UPLOAD, SCAN_TYPE, upload_by, upload_at, posted) VALUES ('12345','" + CONSUMERID + "','" + BILLNO + "','" + DIV_NM + "','" + SUBDIV + "','" + ISSDATE + "','D','" + cl.DB_DATETIME_FORMAT(datetime) + "','DEPARTMENT','1','" + NET + "','12','ADARSH','12333','2021-12-01','" + WC + "','" + MR + "','" + ARREARS + "','0','0','BARCODE','ADMIN','" + cl.DB_DATETIME_FORMAT(datetime) + "','1')");


                            Log.e("scan result", "INSERT INTO scan_online_recipt (SCROLL_ID, CONSUMERID, BILLNO, divi, subdiv, Issue_Date, RECEIPTNO, RCPTDT, RECEIPT_TYPE, RCODE, RAMT, WATER_CHARGE, METER_RENT, ARREAS, OUTSTATION, posted) VALUES ('12345','" + CONSUMERID + "','" + BILLNO + "','" + DIV_NM + "','" + SUBDIV + "','" + ISSDATE + "','ONLINE','" + cl.DB_DATETIME_FORMAT(datetime) + "','','','" + NET + "','" + WC + "','" + MR + "','" + ARREARS + "','0','','','1'");

                        }
                    }else
                    {
                        CALC.SHOWMSG("error","NO DATA FOUND","SCAN ERROR",context);
                    }

                    Log.e("scan result", result);

                }

            } catch (Exception e) {

                error = e.toString();
                MESSAGE += e.getMessage() + "\n" + e.getCause();
                Log.e("MYSQL EXCEPTION",e.getMessage() + "  \nbreak\n" + e.getCause());
                //publishProgress("\nstart "+e.getLocalizedMessage() + "  \nbreak\n" + e.getCause()+"  END \n","0");
                publishProgress("\nCOULD NOT CONNECT TO SERVER.PLEASE TRY AGAIN LATER \n","0");
                ONLINEDBCON = false;
                cancel(true);
                return MESSAGE;

            }
            return MESSAGE;
        }

        protected void onProgressUpdate(String...value) {

        }

        @Override
        protected void onPostExecute(String result)
        {
            Log.e("SCAN RCPT","EXECUTION FINISHED");
            this.progressDialog.dismiss();

        }
        @Override
        protected void onCancelled()
        {
            this.progressDialog.dismiss();
        }
    }

    public void create_dynamic_textview_cardview(Context context,String data,String second_param,LinearLayout parent)
    {

        TextView logtext = new TextView(context);
        logtext.setTextSize(20);
        logtext.setText(data);
        logtext.setInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        //logtext.setLines(6);
        logtext.setMaxLines(10);
        logtext.setSingleLine(false);
        RelativeLayout.LayoutParams param_log = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        logtext.setLayoutParams(param_log);
        parent.setOrientation(LinearLayout.VERTICAL);
        logtext.setTypeface(null, Typeface.BOLD);
        parent.addView(logtext);
        /*CardView cardview1 = new CardView(context);

        CardView cardview2 = new CardView(context);
        if("CARDVIEW1".equals(second_param))
        {


            LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            cardview1.setLayoutParams(layoutparams);
            cardview1.setRadius(15);
            cardview1.setPadding(25, 25, 25, 25);
            cardview1.setCardBackgroundColor(Color.WHITE);
            cardview1.setMaxCardElevation(30);
            cardview1.setMaxCardElevation(6);
            cardview1.addView(logtext);
            parent.addView(cardview1);
        }else
        {


            LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            cardview2.setLayoutParams(layoutparams);
            cardview2.setRadius(15);
            cardview2.setPadding(25, 25, 25, 25);
            cardview2.setCardBackgroundColor(Color.WHITE);
            cardview2.setMaxCardElevation(30);
            cardview2.setMaxCardElevation(6);
            cardview2.addView(logtext);
            parent.addView(cardview2);
        }*/

    }

    public void create_dynamic_textview(Context context,String data,LinearLayout parent)
    {

        Log.e("create_dynamic_textview","create_dynamic_textview "+context);

        TextView logtext = new TextView(context);
        logtext.setTextSize(16);
        logtext.setText(data);
        logtext.setInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        //logtext.setLines(6);
        logtext.setMaxLines(10);
        logtext.setSingleLine(false);
        RelativeLayout.LayoutParams param_log = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        logtext.setLayoutParams(param_log);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.addView(logtext);
    }

    public void checkmysqlconn(Context context)
    {
        if(download != null)
        {
            download.setEnabled(false);
        }
        if(upload != null)
        {
            upload.setEnabled(false);
        }
        //commentedLog.e("func","checkmysqlconn");
        Handler handler = new Handler();
        con_thread = new Thread(new Runnable()
        {
            public void run()
            {
                if(display_log == 1)
                {
                    handler.post(new Runnable() {
                        public void run() {
                            create_dynamic_textview(context,"TRYING TO CONNECT TO MYSQL",scroll_log);
                        }
                    });

                }
                String error = "";
                String MESSAGE = "";
                try {
                    connection = null;
                    Log.d("MYSQLERROR", "MYSQLERROR1");
                    if(display_log == 1)
                    {
                        handler.post(new Runnable() {
                            public void run() {
                                create_dynamic_textview(context,"TRYING TO CONNECT",scroll_log);
                            }
                        });
                    }
                    Class.forName("com.mysql.jdbc.Driver");

                    Log.d("MYSQLERROR", "MYSQLERROR2");
                    String CONFIGSTR = IP+"/"+DBNAME;
                    connection = DriverManager.getConnection("jdbc:mysql://"+CONFIGSTR, DBUSER, DBPASSWORD);
                    Log.d("MYSQLERROR", "MYSQLERROR3"+connection.toString());

                    if(!isDbConnected(connection))
                    {
                        ONLINEDBCON = true;
                        MESSAGE += "FAILED TO CONNECT TO MYSQL DATABASE";
                        String finalMESSAGE = MESSAGE;
                        if(display_log == 1)
                        {
                            handler.post(new Runnable() {
                                public void run() {
                                    create_dynamic_textview(context, finalMESSAGE, scroll_log);
                                }
                            });
                        }
                        Log.d("CONNECTION MYSQL in asy","NOT CONNECTED" );

                    }else
                    {
                        ONLINEDBCON = false;
                        MESSAGE += "MYSQL CONNECTION SUCCESSFUL";
                        String finalMESSAGE1 = MESSAGE;
                        if(display_log == 1) {
                            handler.post(new Runnable() {
                                public void run() {
                                    create_dynamic_textview(context, finalMESSAGE1, scroll_log);
                                    if(download != null)
                                    {
                                        download.setEnabled(true);
                                    }
                                    if(upload != null)
                                    {
                                        upload.setEnabled(true);
                                    }
                                }
                            });
                        }
                        Log.d("CONNECTION MYSQL asy","CONNECTED" );
                    }

                } catch (Exception e)
                {

                    error = e.toString();
                    Log.d("errorstr", e.toString() + "\n" + e.getCause());
                    MESSAGE += "CONNECTION TO SERVER FAILED"+e.getMessage();
                    ONLINEDBCON = false;
                    String finalMESSAGE2 = MESSAGE;
                    if(display_log == 1) {
                        handler.post(new Runnable() {
                            public void run() {
                                create_dynamic_textview(context, finalMESSAGE2, scroll_log);
                            }
                        });
                    }

                }
                handler.post(new Runnable() {
                    public void run() {
                        checkdb();
                    }
                });

            }

        });
        if ((con_thread != null) && (!con_thread.isAlive())) con_thread.start();
    }


    class CHECKMYSQLCON extends AsyncTask<String,String,String>
    {

        String error = "";
        Context context;
        public CHECKMYSQLCON(Context context)
        {
            this.context = context;
        }

        protected void onPreExecute() {
            // initialize the progress bar
            // set maximum progress to 100.
            //CALC.SHOWMSG("TRYING TO CONNECT TO MYSQL","PWD",syncdb.this);
            Log.e("AsyncTask","CHECKMYSQLCON");
            if(display_log == 1)
            {
                create_dynamic_textview(this.context,"TRYING TO CONNECT TO MYSQL",scroll_log);
            }

        }

        @Override
        protected String doInBackground(String... params) {

            String MESSAGE = "";
            try {

                Log.d("MYSQLERROR", "MYSQLERROR1");
                publishProgress("TRYING TO CONNECT");
                Class.forName("com.mysql.jdbc.Driver");

                Log.d("MYSQLERROR", "MYSQLERROR2");
                connection = null;
                String CONFIGSTR = IP+"/"+DBNAME;
                connection = DriverManager.getConnection("jdbc:mysql://"+CONFIGSTR, DBUSER, DBPASSWORD);
                Log.d("MYSQLERROR", "MYSQLERROR3"+connection.toString());

                if(!isDbConnected(connection))
                {
                    ONLINEDBCON = true;
                    MESSAGE += "FAILED TO CONNECT TO MYSQL DATABASE";
                    publishProgress("FAILED TO CONNECT");
                    Log.d("CONNECTION MYSQL in asy","NOT CONNECTED" );

                }else
                {
                    ONLINEDBCON = false;
                    MESSAGE += "MYSQL CONNECTION SUCCESSFUL";
                    publishProgress("MYSQL CONNECTION SUCCESSFUL");
                    Log.d("CONNECTION MYSQL asy","CONNECTED" );
                }

            } catch (Exception e)
            {

                error = e.toString();
                Log.d("errorstr", e.toString() + "\n" + e.getCause());
                MESSAGE += "CONNECTION TO SERVER FAILED"+e.getMessage();
                ONLINEDBCON = false;
                return MESSAGE;

            }
            return MESSAGE;
        }
        protected void onProgressUpdate(String...value) {
            if(display_log == 1)
            {
                create_dynamic_textview(this.context,"MYSQL CONNECTION RESULT :- "+value[0],scroll_log);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(display_log == 1)
            {
                create_dynamic_textview(this.context,"\nMYSQL CONNECTION RESULT :- "+result,scroll_log);
                checkdb();
            }

        }
        @Override
        protected void onCancelled() {
            if(display_log == 1)
            {
                create_dynamic_textview(this.context,"MYSQL CONNECTION RESULT :- OPERATION CANCELLED",scroll_log);
            }
        }
    }


    public boolean isDbConnected(Connection con) {
        try {

            return con != null && !con.isClosed();
        } catch (SQLException ignored) {}

        return false;
    }

    private class UploadFileAsync extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... params) {

            Log.e("UPLOAD IMAGE","UPLOAD TASK CALLED");

            try {

                String FILENAMETOUPLOAD = params[0];

                String sourceFileUri = FILENAMETOUPLOAD;//"/mnt/sdcard/DCIM/Camera/IMG_20211005_200838.jpg";

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(sourceFileUri);

                if (sourceFile.isFile()) {

                    try {
                        String upLoadServerUri = upload_photo_url;

                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(sourceFile);
                        URL url = new URL(upLoadServerUri);

                        // Open a HTTP connection to the URL
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data");
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("bill", sourceFileUri);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"meter_photo\";filename=\""
                                + sourceFileUri + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math
                                    .min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0,
                                    bufferSize);

                        }

                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();

                        if (serverResponseCode == HttpURLConnection.HTTP_OK) {

                            BufferedReader readres = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            Log.e("UPLOAD IMAGE", "File Upload Complete. "+readres.readLine());
                            Log.e("UPLOAD IMAGE", "File Upload Complete. "+serverResponseMessage);

                        }else
                        {
                            Log.e("UPLOAD IMAGE", "response code "+String.valueOf(serverResponseCode));
                        }

                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                    } catch (Exception e) {

                        e.printStackTrace();
                        Log.e("UPLOAD IMAGE", "response code "+e.toString());
                    }
                } // End else block


            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("UPLOAD IMAGE", "response code "+ex.toString());
            }
            Log.e("UPLOAD IMAGE", "FINISHED ");
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private class BILLPRINTPAGECALL extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... params) {

            Log.e("UPLOAD IMAGE","UPLOAD TASK CALLED");

            try {

                String con_id = params[0];
                String issue_dt = params[1];
                String type = "spot";

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;

                    try {
                        String upLoadServerUri = "https://www.phe-sgoa.in/bill_pdf.php?";

                        URL url = new URL(upLoadServerUri);

                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data");
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);

                        dos = new DataOutputStream(conn.getOutputStream());
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; con_id="+con_id+"&issue_dt="+ issue_dt + "&type=" + type);
                        dos.writeBytes(lineEnd);

                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();


                        if (serverResponseCode == HttpURLConnection.HTTP_OK) {

                            BufferedReader readres = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            Log.e("PAGE CALL", "PAGE CALL Complete. "+readres.readLine());

                            Log.e("PAGE CALL", "PAGE CALL Complete. "+serverResponseMessage);

                        }else
                        {
                            Log.e("PAGE CALL", "response code "+String.valueOf(serverResponseCode));
                        }

                        dos.flush();
                        dos.close();

                    } catch (Exception e) {

                        e.printStackTrace();
                        Log.e("PAGE CALL", "response code "+e.toString());
                    }
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("PAGE CALL", "response code "+ex.toString());
            }
            Log.e("PAGE CALL", "FINISHED ");
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public void upload_screenshots(Context context,String USERNAME,String IMEI,String filename)
    {
        cl = new CALC(context);
        COMFUN = new COMFUNCTIONS();
        uploadToServer aq = new uploadToServer(context);
        aq.execute(filename,USERNAME+"_"+IMEI);

    }


    class uploadToServer extends AsyncTask<String,String,String> {
        ProgressDialog progressDialog;
        Context context ;
        public uploadToServer(Context contextact)
        {
            this.context = contextact;
        }

        String records = "", error = "";

        protected void onPreExecute()
        {
            progressDialog= ProgressDialog.show(context, "UPLOADING SCREENSHOT","PLEASE WAIT...", true);
        }

        @Override
        protected String doInBackground(String... params) {


            Log.e("uploadToServer","UPLOAD_LOCALDB_ONLINE");
            String response = UPLOAD_LOCALDB_ONLINE(this.context,params[1],params[0]);

            return response;
        }

        protected void onProgressUpdate(String...value) {


        }

        @Override
        protected void onPostExecute(String result)
        {
            CALC.SHOWMSG("info",result,"SCREENSHOT",this.context);
            Log.e("FILE UPLOAD","EXECUTED");
            this.progressDialog.dismiss();
        }
        @Override
        protected void onCancelled()
        {
            this.progressDialog.dismiss();

        }
    }

    public String UPLOAD_LOCALDB_ONLINE(Context conte,String IMEI,String FILENAME)
    {
        String error = "";
        File sourceFile;

        if(!"".equals(FILENAME))
        {
            sourceFile = new File(FILENAME);
        }else
        {
            sourceFile = new File(Environment.getExternalStorageDirectory(), "/PWD/backup.db");
        }

        if(sourceFile.exists())
        {
            String extension = sourceFile.getAbsolutePath().substring(sourceFile.getAbsolutePath().lastIndexOf("."));
            //String extension = FilenameUtils.getExtension(sourceFile.getAbsolutePath());

            String sourceFileUri ;

            if(!"".equals(FILENAME))
            {
                sourceFileUri = IMEI+"_"+FILENAME+cl.DB_DATETIME_FORMATpaytm(new Date(System.currentTimeMillis()))+extension;
            }else
            {
                sourceFileUri = IMEI+"_DATABASE_BACKUP_"+cl.DB_DATETIME_FORMATpaytm(new Date(System.currentTimeMillis()))+".db";
            }



            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            if (sourceFile.isFile())
            {
                try {

                    String upload_db_url = conte.getResources().getString(R.string.DATABASE_UPLOAD_URL);//"https://www.mss-util.in/pwdbilling/UPLOAD_DB.php?";

                    String upLoadServerUri = upload_db_url;

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(upLoadServerUri);
                    COMFUN.trustEveryone();
                    // Open a HTTP connection to the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE",
                            "multipart/form-data");
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("bill", sourceFileUri);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"database\";filename=\""
                            + sourceFileUri + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math
                                .min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0,
                                bufferSize);

                    }

                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens
                            + lineEnd);

                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    if (serverResponseCode == HttpURLConnection.HTTP_OK) {

                        BufferedReader readres = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        Log.e("UPLOAD DATABASE", "File Upload Complete. "+readres.readLine());
                        Log.e("UPLOAD DATABASE", "File Upload Complete. "+serverResponseMessage);
                        error = sourceFileUri+" FILE UPLOADED SUCCESSFULLY";

                    }else
                    {
                        Log.e("UPLOAD DATABASE", "response code "+String.valueOf(serverResponseCode));

                        error = "FAILED TO UPLOAD FILE "+ sourceFileUri+" RESPONSE CODE "+String.valueOf(serverResponseCode);
                    }

                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (Exception e) {

                    e.printStackTrace();
                    Log.e("UPLOAD DATABASE", "response code "+e.toString());
                    error = "Error while uploading file "+ sourceFileUri +" RESPONSE CODE :- "+e.getMessage();
                }
            }else
            {
                error = "NOT A VALID  FILE";
            }
        }else
        {
            error = sourceFile+ " FILE DOESNOT EXISTS";
            Log.e("COMPIMAGE","DATABASE FILE DOESNOT EXISTS ");
        }
        return error;
    }


    public void SHARE_LOG(Context context,String MESSAGE,String IMEI,String USERNAME)
    {
        String contype = "MSS";
        String ERRCONF = SETCONFIG(contype, context);
        if ("".equals(ERRCONF.trim()))
        {
            cl = new CALC(context);

            ProgressDialog progressDialog;
            progressDialog = ProgressDialog.show(context, "",
                    "SENDING ERROR LOG TO MEGASOFT SYSTEMS.PLEASE WAIT...", true);
            Handler handler = new Handler();
            thrd2 = new Thread(new Runnable() {
                public void run() {
                    try {

                        Class.forName("com.mysql.jdbc.Driver");

                        Log.d("MYSQLERROR", "MYSQLERROR2");
                        connection = null;
                        String CONFIGSTR = IP+"/"+DBNAME;
                        connection = DriverManager.getConnection("jdbc:mysql://"+CONFIGSTR, DBUSER, DBPASSWORD);

                        if (!isDbConnected(connection)) {
                            Log.e("isDbConnected", "CONNECTION TO SERVER FAILED.COULD NOT SEND REPORT");

                            handler.post(new Runnable() {
                                public void run() {
                                    CALC.SHOWMSG("info", "CONNECTION TO SERVER FAILED.COULD NOT SEND REPORT", "ERROR LOG", context);
                                }
                            });
                            progressDialog.dismiss();

                        } else {

                            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                            Date tod = new Date(System.currentTimeMillis());
                            String today = cl.DB_DATETIME_FORMAT(tod);

                            Log.e("SHARE REPORT","INSERT INTO device_error_log( MESSAGE,POS_SERIAL_NO, ENTERED_AT, ENTERED_BY) VALUES ('" + MESSAGE + "','" + IMEI + "','" + today + "','" + USERNAME + "')");

                            int res = statement.executeUpdate("INSERT INTO device_error_log( MESSAGE,POS_SERIAL_NO, ENTERED_AT, ENTERED_BY) VALUES ('" + MESSAGE + "','" + IMEI + "','" + today + "','" + USERNAME + "')");


                            Log.e("res", " " + res);
                            if (res > 0) {

                                handler.post(new Runnable() {
                                    public void run() {
                                        CALC.SHOWMSG("info", "ERRO LOG SENT TO MEGASOFT.\n\n", "ERROR LOG", context);
                                        progressDialog.dismiss();
                                    }
                                });

                            } else {
                                handler.post(new Runnable() {
                                    public void run() {
                                        CALC.SHOWMSG("error", "COULD NOT SEND ERROR LOG.PLEASE RETRY AGAIN", "ERROR LOG", context);
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                            statement.close();
                            progressDialog.dismiss();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            public void run() {
                                CALC.SHOWMSG("error", "ERROR OCCURED " + e.toString(), "ERROR", context);
                                progressDialog.dismiss();
                            }
                        });
                    }

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
            if ((thrd2 != null) && (!thrd2.isAlive())) thrd2.start();
        }
    }


}
