package com.example.netrasagar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.location.LocationRequest;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class COMFUNCTIONS extends AppCompatActivity {
    public final static String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";
    private String msMAC;
    /**Bluetooth connection status*/
    private boolean mbConectOk = false;
    private BluetoothSocket mbsSocket = null;
    public static InputStream misIn = null;
    /** Output stream object */
    public static OutputStream mosOut = null;
    /**Constant: The current Android SDK version number*/

    public AlertDialog myAlertDialog;
    Connection con = null;
    CALC cal;
    syncdb syd;

    private static final String[] numNames = {
            " Zero",
            " One",
            " Two",
            " Three",
            " Four",
            " Five",
            " Six",
            " Seven",
            " Eight",
            " Nine",
    };

    public Date DATEFROMSTRING(String DATETOCONVERT) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date DATEFORMAT = null;
        try {
            DATEFORMAT = (Date) sdf.parse(DATETOCONVERT);
        } catch (ParseException e) {
            e.printStackTrace();
            CALC.SHOWMSG("error", e.toString(), "ERROR", this);
            return null;
        }
        return DATEFORMAT;
    }

    public int check_bluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return 2;
        } else if (!mBluetoothAdapter.isEnabled()) {
            return -1;
        } else {
            return 1;
        }
    }

    String validate(Object str) {
        return (String) (str == null ? "" : str.toString());
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private Bitmap align_image(Bitmap firstImage) {
        Bitmap result = Bitmap.createBitmap(firstImage.getWidth() + 426, firstImage.getHeight(), firstImage.getConfig());
        result = Bitmap.createScaledBitmap(result, 576, 300, false);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(-1);
        canvas.drawBitmap(firstImage, null, new RectF(180, 0, 356, 350), null);
        return result;
    }


    public String numtoword(String amount) {
        String word = "";
        Log.e("amount", amount);
        String amountlist[] = amount.split("");
        for (int x = 0; x < amountlist.length; x++) {
            if (!"-".equals(amountlist[x]) && !"".equals(amountlist[x])) {
                word += numNames[Integer.parseInt(amountlist[x])];
            }
        }
        return "Rupees " + word + " Only";
    }

    String print_bill_items(String[] string, String[] position, String[] total_space) {
        String space_to_print1 = "";
        int space_needed = 0;
        String space_to_print2 = "";
        int space_print = 0;
        int max_char = 0;

        int error = 0;
        String final_string = "";

        for (int str = 0; str < string.length; str++) {
            space_to_print1 = "";
            space_needed = Integer.parseInt(total_space[str]) - string[str].length();

            for (int i = 0; i < space_needed; i++) {
                space_to_print1 += " ";
            }
            if (position[str] == "LEFT") {
                string[str] = string[str] + space_to_print1;
            } else if (position[str] == "RIGHT") {
                string[str] = space_to_print1 + string[str];
            }

            final_string += string[str];
        }
        return final_string;
    }

    Button cancel_button(Context context, FrameLayout fl , ConstraintLayout billayout, LinearLayout searchform, ScrollView imglayout)
    {
        Button button = new Button(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(70,70,1.0f);
        button.setGravity(Gravity.RIGHT);
        button.setLayoutParams(params);
        button.setBackground(ContextCompat.getDrawable(context,R.drawable.cancel));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(searchform != null)
                {
                    billayout.removeView(searchform);
                }
                if(imglayout != null)
                {
                    imglayout.setVisibility(View.GONE);
                }
                if(fl != null)
                {
                    fl.setVisibility(View.VISIBLE);
                }
            }
        });
        return button;
    }


    private static Uri uriFromFile(Context context, File file)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            Log.e("TAG", "uriFromFile1");

            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else
        {
            Log.e("TAG", "uriFromFile2");

            return Uri.fromFile(file);
        }
    }

    public void update_act(String activity,Context context)
    {
        SharedPreferences sp_sync = context.getSharedPreferences("spsync", Context.MODE_PRIVATE);
        SharedPreferences.Editor spsynceditor = sp_sync.edit();
        spsynceditor.putString("latest_activity", activity);
        spsynceditor.commit();
    }

    public String installApk(Context context,Handler back_app_upd,Runnable backAppUpd) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("APP UPDATE")
                .setMessage("Latest version is available.Please update")
                .setPositiveButton("Ok",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedpreferences = context.getSharedPreferences("LOGINCREDENTIAL", Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = sharedpreferences.edit();
                        edit.putString("APP_INSTALLATION", "1");
                        edit.commit();
                        Log.e("installApk", "Error in opening the file!");
                        try {
                            Intent intent_install = new Intent(Intent.ACTION_VIEW);
                            intent_install.setDataAndType(uriFromFile(context, new File(Environment.getExternalStorageDirectory() + "/Download/msspwd.apk")), "application/vnd.android.package-archive");

                            //intent_install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent_install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            try {
                                edit.putString("APP_INSTALLATION", "2");
                                edit.commit();
                                context.startActivity(intent_install);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                Log.e("TAG", "Error in opening the file!");
                                edit.putString("APP_INSTALLATION", "-1");
                                edit.commit();
                                //return "ERROR IN OPENING FILE";
                            }

                        } catch (Exception e) {
                            Log.e("UpdateAPP", "Update error! " + e.getMessage());
                            edit.putString("APP_INSTALLATION", "-1");
                            edit.commit();
                            //return "UPDATE ERROR."+e.getMessage();
                        }
                        edit.putString("APP_INSTALLATION", "2");
                        edit.commit();
                        //return "SUCCESS";
                    }
                } )
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(back_app_upd != null)
                        {
                            back_app_upd.postDelayed(backAppUpd, 5000);
                        }
                    }
                });


        myAlertDialog = dialog.create();
        myAlertDialog.show();

        return "1";
    }

    public String DB_BACKUP(Context context)
    {
        try
        {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite())
            {
                String currentDBPath = "/data/data/" + context.getPackageName() + "/databases/pwddb.db";
                String backupDBPath = "PWD";

                File file = new File(sd,backupDBPath);
                backupDBPath = "/PWD/backup.db";
                if (!file.exists()) {
                    file.mkdirs();
                }

                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);


                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();


            }
            return "";
        } catch (Exception e) {
            Log.e("BKUPDB", "exception "+e.getMessage()+e.getStackTrace());
            return "";
        }
    }

    public void SHOW_HIDE_BUTTON_ON_RIGHTS(View view,Integer status,String cur_status)
    {
        Log.e("SHOW_HIDE",view+" "+status);
        if(status==1)
        {
            if("NOT PAID".equals(cur_status) || "FAIL".equals(cur_status) || "".equals(cur_status) )
            {
                view.setEnabled(true);
            }else
            {
                view.setEnabled(false);
            }
            view.setVisibility(View.VISIBLE);
        }else
        {
            view.setVisibility(View.INVISIBLE);
        }
    }

    public ArrayList<String> get_pay_type(Context context, String IMEI, Spinner PAYFOR, String CATEGORY)
    {

        CALC cal = new CALC(context);
        Log.e("get_pay_type", " "+new Date(System.currentTimeMillis()).toString());
        ArrayList<String> arrayList = new ArrayList<String>();

        arrayList = cal.get_pay_type(context,PAYFOR,CATEGORY);
        Log.e("get_pay_type", " get_pay_type "+arrayList.size());

        return arrayList;
    }

    public Connection create_con(Context context)
    {
        syd = new syncdb();
        Thread conthread = null;
        conthread = new Thread(new Runnable()
        {
            public void run()
            {

                con = null;
                try
                {
                    Class.forName("com.mysql.jdbc.Driver");
                    Log.e("Class", "com.mysql.jdbc.Driver");
                } catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                    Log.e("Classerr", e.toString() + " ");
                    con = null;
                }

                if (con == null) {
                    Log.e("con", "create_con NOT AVAILABLE");

                    try
                    {

                        syd.SETCONFIG("",context);
                        String CONFIGSTR = syd.IP+"/"+syd.DBNAME;
                        con = DriverManager.getConnection("jdbc:mysql://"+CONFIGSTR, syd.DBUSER, syd.DBPASSWORD);
                        Log.d("MYSQLERROR", "MYSQLERROR3"+con.toString());

                        //con = DriverManager.getConnection("jdbc:mysql://45.113.189.69:3306/mssutil_pwd", "mssutil_pwduser", "PWDmss_user#6090");
                        Log.e("Class", "create_con successful "+ con);

                    } catch (SQLException e)
                    {
                        e.printStackTrace();
                        con = null;
                        Log.e("conexc", "create_con NOT AVAILABLE" + e.toString());
                    }
                }
            }

        });
        if ((conthread != null) && (!conthread.isAlive())) conthread.start();
        return con;
    }

    public void APP_NOTIFICATIONS(String PAGE,String IMEI,String FORM,Context context)
    {
        CALC cl = new CALC(context);
        String notif_msg = "";
        Date from_date = null;
        Date to_date = null;
        Date datetime = new Date(System.currentTimeMillis());
        ContentValues notif = cl.notifications(PAGE, IMEI, FORM);
        if ("".equals(notif.get("error")))
        {
            notif_msg = notif.get("message").toString();

            if(!"".equals(notif.get("from_date")) && notif.get("from_date")!=null)
            {
                String strdatefom = notif.get("from_date").toString();
                String[] dateform;
                if(strdatefom.contains("."))
                {
                    dateform = strdatefom.split("[.]",0);

                    from_date = cl.DATE_TIMEFROMSTRING(dateform[0]);
                }else
                {
                    from_date = cl.DATE_TIMEFROMSTRING(strdatefom);
                }

                Log.e("from_date", from_date.toString()+" from_date");
            }
            if(!"".equals(notif.get("to_date")) && notif.get("to_date")!=null)
            {

                String dateto = notif.get("to_date").toString();
                String[] date_to;
                if(dateto.contains("."))
                {
                    date_to = dateto.split("[.]",0);
                    to_date = cl.DATE_TIMEFROMSTRING(date_to[0]);
                }else
                {
                    to_date = cl.DATE_TIMEFROMSTRING(dateto);
                }

                Log.e("to_date", to_date.toString()+" to_date");
            }

        }else
        {
            CALC.SHOWMSG("error",notif.get("error").toString(),"NOTIFICATION",context);
        }
        Log.e("notif_msg", notif_msg+" blankl");
        if(!"".equals(notif_msg.trim()))
        {
            if(from_date != null && to_date != null)
            {
                if (datetime.compareTo(from_date) >= 0 && datetime.compareTo(to_date) <= 0)
                {
                    CALC.SHOWMSG("info",notif_msg,"NOTIFICATION",context);
                }
            }else
            {
                CALC.SHOWMSG("info",notif_msg,"NOTIFICATION",context);
            }
        }
    }

    public Bitmap getScreenShot(View view,Context context,String USERNAME,String IMEI)
    {

        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);


        ConnectivityManager cmu = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cmu.getActiveNetworkInfo();
        if (check_internet(context))
        {

            store(bitmap, "screenshot"+new Date(System.currentTimeMillis()) + ".jpg",context,USERNAME,IMEI);


        } else {
            CALC.SHOWMSG("error", "YOU ARE OFFLINE", "PWD", context);
        }


        return bitmap;
    }

    public void store(Bitmap bm, String fileName,Context context,String USERNAME,String IMEI){


        //final  String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PWD_SCREENSHOTS";

        //final  String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PWD_SCREENSHOTS";
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "PWD_SCREENSHOTS");
        //File dir = new File(Environment.getExternalStorageDirectory() + "/PWD_SCREENSHOTS");
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, fileName);
        try {

            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

            syncdb syd = new syncdb();
            syd.upload_screenshots(context,USERNAME,IMEI,file.getAbsolutePath());
        } catch (Exception e) {

            Log.e("getScreenShot","Exception e"+e);

            e.printStackTrace();
        }
    }

    public void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType){}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }

    public void LOGFILE(String IMEI,String text)
    {
        File logFile = new File(Environment.getExternalStorageDirectory() + "/PWD/"+IMEI+"_log.txt");

        if(!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
                Log.e("logFile","CREATING LOG FILE");
                text = IMEI+"_FILE CREATED"+new Date(System.currentTimeMillis());
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e("logFile","EXCEPTION LOG FILE");
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(new Date(System.currentTimeMillis())+" : "+text+"\n");
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("logFile","EXCEPTION WRITING LOG FILE");
        }
    }

    public void LOCATION_FETCH(Context context,String IMEI)
    {
        Log.e("LOCATION_FETCH","LOCATION_FETCH");
        EasyLocationProvider easyLocationProvider = new EasyLocationProvider.Builder(context,IMEI)
                .setInterval(5000)
                .setFastestInterval(2000)
                .setListener(new EasyLocationProvider.EasyLocationCallback() {
                    @Override
                    public void onGoogleAPIClient(GoogleApiClient googleApiClient, String message) {
                        Log.e("EasyLocationProvider","onGoogleAPIClient: "+message);

                    }

                    @Override
                    public void onLocationUpdated(double latitude, double longitude) {

                        Log.e("EasyLocationProvider","onLocationUpdated:: "+ "Latitude: "+latitude+","+longitude+new Date(System.currentTimeMillis()));

                    }

                    @Override
                    public void onLocationUpdateRemoved() {
                        Log.e("EasyLocationProvider","onLocationUpdateRemoved");

                    }
                }).build();
        Log.e("LOCATION_FETCH","LOCATION_FETCH");
        getLifecycle().addObserver(easyLocationProvider);
    }

    public static boolean isLocationEnabled(Context context) {
        Log.e("isLocationEnabled"," isLocationEnabled");
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            Log.e("locationProviders"," "+locationProviders);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public boolean check_internet(Context context)
    {
        ConnectivityManager cmu = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cmu.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }else
        {
            return false;
        }
    }
}
