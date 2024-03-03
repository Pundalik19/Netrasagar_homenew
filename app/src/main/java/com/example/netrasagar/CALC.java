
package com.example.netrasagar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CALC extends SQLiteOpenHelper {
    private String CONSUMERID, CODY, TITLE, NAME, ADD1, ADD2, ADD3, METERNO, FCODE, CATEGORY, METERSIZE, AVG_CONS, CURRENT_READING, CURFCODE, REMARKS, CONNOFLAT,HOUSE_CLOSED;

    String PREVREADING, BILLED_AS_CATEGORY, LEDGERID, walking_sequence, form_type, mobno, lastupdated, emailid;
    String REPLACEMENT_DATE, OLDNET;
    Date ISSDATE, LASTDATE, DATE1,ADD_PERDAY1_SKIPPED;;
    int FCOUNT;
    String PAYMENTSTATUS = "";
    private String ERROR = "";
    SQLiteDatabase db = getWritableDatabase();

    syncdb syd;

    public CALC(@Nullable Context context) {
        super(context, "Netrasagar.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public SQLiteDatabase getdb() {
        return this.db;
    }

    public ContentValues GET_LEDGER() {
        ContentValues ledgerdata = new ContentValues();
        ledgerdata.put("LEDGERID", this.LEDGERID);
        ledgerdata.put("walking_sequence", this.walking_sequence);
        ledgerdata.put("CONSUMERID", this.CONSUMERID);
        ledgerdata.put("CODY", this.CODY);
        ledgerdata.put("TITLE", this.TITLE);
        ledgerdata.put("NAME", this.NAME);
        ledgerdata.put("ADD1", this.ADD1);
        ledgerdata.put("ADD2", this.ADD2);
        ledgerdata.put("ADD3", this.ADD3);
        ledgerdata.put("METERNO", this.METERNO);
        ledgerdata.put("CATEGORY", this.CATEGORY);
        ledgerdata.put("FCODE", this.FCODE);
        ledgerdata.put("PREVREADING", this.PREVREADING);
        ledgerdata.put("METERSIZE", this.METERSIZE);
        ledgerdata.put("AVG_CONS", this.AVG_CONS);
        ledgerdata.put("FCOUNT", this.FCOUNT);
        ledgerdata.put("METER_REPLACEMENT_DATE", this.REPLACEMENT_DATE);
        ledgerdata.put("FORM_TYPE", this.form_type);
        ledgerdata.put("MOBILE", this.mobno);
        ledgerdata.put("CURRENT_READING", this.CURRENT_READING);
        ledgerdata.put("CURFCODE", this.CURFCODE);
        ledgerdata.put("lastupdated", this.lastupdated);
        ledgerdata.put("REMARKS", this.REMARKS);
        ledgerdata.put("emailid", this.emailid);
        ledgerdata.put("OLDNET", this.OLDNET);
        ledgerdata.put("CONNOFLAT", this.CONNOFLAT);
        ledgerdata.put("PAYMENTSTATUS", this.PAYMENTSTATUS);
        ledgerdata.put("HOUSE_CLOSED", this.HOUSE_CLOSED);


        if (this.ISSDATE != null) {
            ledgerdata.put("ISSDATE", DB_FORMAT(this.ISSDATE));
        } else {
            ledgerdata.put("ISSDATE", "");
        }

        if (this.LASTDATE != null) {
            ledgerdata.put("LASTDATE", DB_FORMAT(this.LASTDATE));
        } else {
            ledgerdata.put("LASTDATE", "");
        }

        if (this.DATE1 != null) {
            ledgerdata.put("DATE1", DB_FORMAT(this.DATE1));
        } else {
            ledgerdata.put("DATE1", "");
        }

        if (this.ADD_PERDAY1_SKIPPED != null) {
            ledgerdata.put("ADD_PERDAY1_SKIPPED", DB_FORMAT(this.ADD_PERDAY1_SKIPPED));
        } else {
            ledgerdata.put("ADD_PERDAY1_SKIPPED", "");
        }

        ledgerdata.put("ERROR", this.ERROR);

        return ledgerdata;
    }

    public ContentValues SET_LEDGER(Date PERDAY1, String ZONE, String WARD, String USERNAME, String IMEI, String CONSU, String LEDGERID, String DIRECTION, String CORRECTION)
    {
        ERROR = "";
        this.lastupdated = "";
        this.REMARKS = "";
        this.OLDNET = "";
        this.CONNOFLAT = "";
        this.PREVREADING = "";
        this.DATE1 = null;
        this.PAYMENTSTATUS = "";
        //this.CURRENT_READING = "";

        ContentValues ledgerdata = new ContentValues();
        Log.e("CONSUMERID", CONSU + " " + CONSUMERID + " " + PERDAY1 + " " + ZONE);
        SQLiteDatabase db = getWritableDatabase();
        String WARD_CONDITION = "";
        String CONDITION = "";
        if (!"".equals(CONSU.trim())) {
            LEDGERID = "";
            CONDITION = " AND masterdetails.CONSUMERID='" + CONSU.trim() + "' ";
        }
        if (!"".equals(WARD.trim()) && !"ALL".equals(WARD.trim())) {
            WARD_CONDITION = " AND masterdetails.WARD='" + WARD + "' ";
        }

        if (!"".equals(LEDGERID.trim())) {
            if ("NEXT".equals(DIRECTION.trim())) {
                Log.e("DIRECTION", DIRECTION + " " + DIRECTION);
                CONDITION += " AND masterdetails.walking_sequence = ( SELECT min(masterdetails.walking_sequence) from masterdetails where masterdetails.walking_sequence > '" + LEDGERID.trim() + "' AND masterdetails.DELETED='0' AND masterdetails.ZONE='" + ZONE + "' " + WARD_CONDITION + " AND  masterdetails.CONSUMERID NOT IN (SELECT ledgerdata.CONSUMERID FROM ledgerdata where ledgerdata.PERDAY1 = '" + DB_FORMAT(PERDAY1) + "' AND ledgerdata.zone = '" + ZONE + "')) ";// AND BILLNO IS NULL
            } else {
                CONDITION += " AND masterdetails.walking_sequence = ( SELECT max(masterdetails.walking_sequence) from masterdetails where masterdetails.walking_sequence < '" + LEDGERID.trim() + "' AND masterdetails.DELETED='0' AND masterdetails.ZONE='" + ZONE + "' " + WARD_CONDITION + " AND  masterdetails.CONSUMERID NOT IN (SELECT ledgerdata.CONSUMERID FROM ledgerdata where ledgerdata.PERDAY1 = '" + DB_FORMAT(PERDAY1) + "' AND ledgerdata.zone = '" + ZONE + "')) ";// AND BILLNO IS NULL
            }
        } else {
            Log.e("LERID bfr qury", LEDGERID + " " + DIRECTION);
        }

        Log.e("QUERY", "SELECT masterdetails.HOUSE_CLOSED,masterdetails.MOBILE_NO,masterdetails.EMAIL_ID,masterdetails.walking_sequence,ledgerdata.METER_READ,ledgerdata.REDATE,ledgerdata.FCOUNT,ledgerdata.ID,masterdetails.CONSUMERID,masterdetails.CODY,masterdetails.TITLE,CONSFNAME,masterdetails.ADD1,masterdetails.ADD2,masterdetails.ADD3,masterdetails.METER_NO,masterdetails.CATEGORY,ledgerdata.FCODE,ledgerdata.CUR_READ,ledgerdata.DATE1,ledgerdata.DATE2,meter_details.METER_SIZE,ledgerdata.ISSDATE,ledgerdata.LASTDATE,ledgerdata.NEWAVG as LED_NEWAVG,masterdetails.AVG_CONS as MAST_AVG_CONS FROM  masterdetails  LEFT JOIN ledgerdata ON masterdetails.CONSUMERID = ledgerdata.CONSUMERID LEFT JOIN METER_DETAILS ON masterdetails.METCODE = meter_details.METCODE WHERE ( CASE WHEN ledgerdata.PERDAY1 IS NOT NULL THEN ledgerdata.PERDAY1 = (SELECT l2.PERDAY1 from ledgerdata l2 where l2.CONSUMERID = masterdetails.CONSUMERID AND l2.PERDAY1 < ? ORDER BY l2.PERDAY1 DESC LIMIT 1) ELSE ledgerdata.PERDAY1 IS NULL END) AND  masterdetails.zone = ? AND masterdetails.DELETED='0' " + CONDITION + " " + WARD_CONDITION + " GROUP BY masterdetails.CONSUMERID ORDER BY masterdetails.walking_sequence ASC LIMIT 1");

        Cursor cursor = null;
        if ("YES".equals(CORRECTION)) {
            Log.e("CORRECTION", "SELECT masterdetails.HOUSE_CLOSED,masterdetails.MOBILE_NO,masterdetails.EMAIL_ID,masterdetails.walking_sequence,ledgerdata.METER_READ,ledgerdata.POS_DOWN_AT as led_posdownat,ledgerdata.REDATE,ledgerdata.FCOUNT,ledgerdata.ID,masterdetails.CONSUMERID,masterdetails.CODY,masterdetails.TITLE,CONSFNAME,masterdetails.ADD1,masterdetails.ADD2,masterdetails.ADD3,masterdetails.METER_NO,masterdetails.CATEGORY,ledgerdata.FCODE,ledgerdata.CUR_READ,ledgerdata.DATE1,ledgerdata.DATE2,meter_details.METER_SIZE,ledgerdata.ISSDATE,ledgerdata.LASTDATE,ledgerdata.NEWAVG as LED_NEWAVG,masterdetails.AVG_CONS as MAST_AVG_CONS FROM  masterdetails  LEFT JOIN ledgerdata ON masterdetails.CONSUMERID = ledgerdata.CONSUMERID LEFT JOIN METER_DETAILS ON masterdetails.METCODE = meter_details.METCODE WHERE ( CASE WHEN ledgerdata.PERDAY1 IS NOT NULL THEN ledgerdata.PERDAY1 = (SELECT l2.PERDAY1 from ledgerdata l2 where l2.CONSUMERID = masterdetails.CONSUMERID AND l2.PERDAY1 < '" + DB_FORMAT(PERDAY1) + "' ORDER BY l2.PERDAY1 DESC LIMIT 1) ELSE ledgerdata.PERDAY1 IS NULL END) AND  masterdetails.zone = ? AND masterdetails.DELETED='0' " + CONDITION + " " + WARD_CONDITION + " GROUP BY masterdetails.CONSUMERID ORDER BY masterdetails.walking_sequence ASC LIMIT 1");

            cursor = db.rawQuery("SELECT masterdetails.HOUSE_CLOSED,masterdetails.NOFLAT,masterdetails.MOBILE_NO,masterdetails.EMAIL_ID,masterdetails.walking_sequence,masterdetails.POS_DOWN_AT,ledgerdata.POS_DOWN_AT as led_posdownat,ledgerdata.METER_READ,ledgerdata.REDATE,ledgerdata.FCOUNT,ledgerdata.ID,masterdetails.CONSUMERID,masterdetails.CODY,masterdetails.TITLE,CONSFNAME,masterdetails.ADD1,masterdetails.ADD2,masterdetails.ADD3,masterdetails.METER_NO,masterdetails.CATEGORY,ledgerdata.FCODE,ledgerdata.CUR_READ,ledgerdata.DATE1,ledgerdata.DATE2,meter_details.METER_SIZE,ledgerdata.ISSDATE,ledgerdata.LASTDATE,ledgerdata.NEWAVG as LED_NEWAVG,masterdetails.AVG_CONS as MAST_AVG_CONS FROM  masterdetails  LEFT JOIN ledgerdata ON masterdetails.CONSUMERID = ledgerdata.CONSUMERID LEFT JOIN meter_details ON masterdetails.METCODE = meter_details.METCODE WHERE ( CASE WHEN ledgerdata.PERDAY1 IS NOT NULL THEN ledgerdata.PERDAY1 = (SELECT l2.PERDAY1 from ledgerdata l2 where l2.CONSUMERID = masterdetails.CONSUMERID AND l2.PERDAY1 < ? ORDER BY l2.PERDAY1 DESC LIMIT 1) ELSE ledgerdata.PERDAY1 IS NULL END) AND  masterdetails.zone = ? AND masterdetails.DELETED='0' " + CONDITION + " " + WARD_CONDITION + " GROUP BY masterdetails.CONSUMERID ORDER BY masterdetails.walking_sequence ASC LIMIT 1", new String[]{DB_FORMAT(PERDAY1), ZONE});

            int tot = cursor.getCount();
            if(tot==0)
            {
                cursor = db.rawQuery("SELECT masterdetails.HOUSE_CLOSED,masterdetails.NOFLAT,masterdetails.MOBILE_NO,masterdetails.EMAIL_ID,masterdetails.walking_sequence,masterdetails.POS_DOWN_AT,ledgerdata.POS_DOWN_AT as led_posdownat,ledgerdata.METER_READ,ledgerdata.REDATE,ledgerdata.FCOUNT,ledgerdata.ID,masterdetails.CONSUMERID,masterdetails.CODY,masterdetails.TITLE,CONSFNAME,masterdetails.ADD1,masterdetails.ADD2,masterdetails.ADD3,masterdetails.METER_NO,masterdetails.CATEGORY,ledgerdata.FCODE,ledgerdata.CUR_READ,ledgerdata.DATE1,ledgerdata.DATE2,meter_details.METER_SIZE,ledgerdata.ISSDATE,ledgerdata.LASTDATE,ledgerdata.NEWAVG as LED_NEWAVG,masterdetails.AVG_CONS as MAST_AVG_CONS FROM  masterdetails  LEFT JOIN ledgerdata ON masterdetails.CONSUMERID = ledgerdata.CONSUMERID AND ledgerdata.PERDAY1<>? LEFT JOIN meter_details ON masterdetails.METCODE = meter_details.METCODE WHERE   masterdetails.zone = ? AND masterdetails.DELETED='0' " + CONDITION + " " + WARD_CONDITION + " GROUP BY masterdetails.CONSUMERID ORDER BY masterdetails.walking_sequence ASC LIMIT 1", new String[]{DB_FORMAT(PERDAY1), ZONE});
            }

            Log.e("CORRECTION", CORRECTION + " YES");
        } else {
            Log.e("CORRECTION", CORRECTION + " NO");
            Log.e("CORRECTION", "SELECT masterdetails.HOUSE_CLOSED,masterdetails.MOBILE_NO,masterdetails.EMAIL_ID,masterdetails.walking_sequence,masterdetails.POS_DOWN_AT,ledgerdata.POS_DOWN_AT as led_posdownat,ledgerdata.METER_READ,ledgerdata.REDATE,ledgerdata.FCOUNT,ledgerdata.ID,masterdetails.CONSUMERID,masterdetails.CODY,masterdetails.TITLE,CONSFNAME,masterdetails.ADD1,masterdetails.ADD2,masterdetails.ADD3,masterdetails.METER_NO,masterdetails.CATEGORY,ledgerdata.FCODE,ledgerdata.CUR_READ,ledgerdata.DATE1,ledgerdata.DATE2,meter_details.METER_SIZE,ledgerdata.ISSDATE,ledgerdata.LASTDATE,ledgerdata.NEWAVG as LED_NEWAVG,masterdetails.AVG_CONS as MAST_AVG_CONS FROM  masterdetails  LEFT JOIN ledgerdata ON masterdetails.CONSUMERID = ledgerdata.CONSUMERID LEFT JOIN METER_DETAILS ON masterdetails.METCODE = meter_details.METCODE WHERE ( CASE WHEN ledgerdata.PERDAY1 IS NOT NULL THEN ledgerdata.PERDAY1 = (SELECT l2.PERDAY1 from ledgerdata l2 where l2.CONSUMERID = masterdetails.CONSUMERID AND l2.PERDAY1 < '" + DB_FORMAT(PERDAY1) + "' ORDER BY l2.PERDAY1 DESC LIMIT 1) ELSE ledgerdata.PERDAY1 IS NULL END) AND  masterdetails.zone = '" + ZONE + "' AND masterdetails.DELETED='0'  AND masterdetails.CONSUMERID NOT IN (SELECT ledgerdata.CONSUMERID FROM ledgerdata where ledgerdata.PERDAY1 = ? AND ledgerdata.zone = ?) " + CONDITION + " " + WARD_CONDITION + " GROUP BY masterdetails.CONSUMERID ORDER BY masterdetails.walking_sequence ASC LIMIT 1");
            cursor = db.rawQuery("SELECT masterdetails.HOUSE_CLOSED,masterdetails.NOFLAT,masterdetails.MOBILE_NO,masterdetails.EMAIL_ID,masterdetails.walking_sequence,masterdetails.POS_DOWN_AT,ledgerdata.POS_DOWN_AT as led_posdownat,ledgerdata.METER_READ,ledgerdata.REDATE,ledgerdata.FCOUNT,ledgerdata.ID,masterdetails.CONSUMERID,masterdetails.CODY,masterdetails.TITLE,CONSFNAME,masterdetails.ADD1,masterdetails.ADD2,masterdetails.ADD3,masterdetails.METER_NO,masterdetails.CATEGORY,ledgerdata.FCODE,ledgerdata.CUR_READ,ledgerdata.DATE1,ledgerdata.DATE2,meter_details.METER_SIZE,ledgerdata.ISSDATE,ledgerdata.LASTDATE,ledgerdata.NEWAVG as LED_NEWAVG,masterdetails.AVG_CONS as MAST_AVG_CONS FROM  masterdetails  LEFT JOIN ledgerdata ON masterdetails.CONSUMERID = ledgerdata.CONSUMERID LEFT JOIN meter_details ON masterdetails.METCODE = meter_details.METCODE WHERE ( CASE WHEN ledgerdata.PERDAY1 IS NOT NULL THEN ledgerdata.PERDAY1 = (SELECT l2.PERDAY1 from ledgerdata l2 where l2.CONSUMERID = masterdetails.CONSUMERID AND l2.PERDAY1 < ? ORDER BY l2.PERDAY1 DESC LIMIT 1) ELSE ledgerdata.PERDAY1 IS NULL END) AND  masterdetails.zone = ? AND masterdetails.DELETED='0'  AND masterdetails.CONSUMERID NOT IN (SELECT ledgerdata.CONSUMERID FROM ledgerdata where ledgerdata.PERDAY1 = ? AND ledgerdata.zone = ?) " + CONDITION + " " + WARD_CONDITION + " GROUP BY masterdetails.CONSUMERID ORDER BY masterdetails.walking_sequence ASC LIMIT 1", new String[]{DB_FORMAT(PERDAY1), ZONE, DB_FORMAT(PERDAY1), ZONE});
        }

        int tot = cursor.getCount();
        if (!"".equals(DIRECTION) && tot == 0) {
            this.ERROR = "CONSUMER ALREADY BILLED OR NOT FOUND";
            Log.d("ERROR", this.ERROR);
        } else if (tot > 0) {
            while (cursor.moveToNext()) {
                this.CONSUMERID = cursor.getString(cursor.getColumnIndex("CONSUMERID"));

                String ADD_METER_READ = "";
                String ADD_BILLED_AS_CATEGORY = "";
                String ADD_DATE1 = "";
                double ADD_DC = 0;
                double ADD_ARREARS = 0;

                Cursor addque = db.rawQuery("SELECT * FROM additionals WHERE CONSUMERID=? AND PERDAY1=?", new String[]{this.CONSUMERID, DB_FORMAT(PERDAY1)});//AND ledgerdata.PERDAY1 = '2021-04-01'
                if (addque.getCount() > 0) {
                    while (addque.moveToNext()) {
                        ADD_METER_READ = addque.getString(addque.getColumnIndex("METER_READ"));
                        ADD_DATE1 = addque.getString(addque.getColumnIndex("DATE1"));
                        ADD_BILLED_AS_CATEGORY = addque.getString(addque.getColumnIndex("BILLED_AS_CATEGORY"));
                        this.form_type = addque.getString(addque.getColumnIndex("form_type"));
                        ADD_ARREARS = addque.getDouble(addque.getColumnIndex("ARREARS"));
                        ADD_DC = addque.getDouble(addque.getColumnIndex("DC"));
                    }
                } else {
                    ADD_METER_READ = "";
                    ADD_DATE1 = "";
                    ADD_BILLED_AS_CATEGORY = "";
                    this.form_type = "";
                    ADD_ARREARS = 0;
                    ADD_DC = 0;
                }
                addque.close();

                this.HOUSE_CLOSED = cursor.getString(cursor.getColumnIndex("HOUSE_CLOSED"));
                this.LEDGERID = cursor.getString(cursor.getColumnIndex("walking_sequence"));
                this.CODY = cursor.getString(cursor.getColumnIndex("CODY"));
                this.TITLE = cursor.getString(cursor.getColumnIndex("TITLE"));
                this.NAME = cursor.getString(cursor.getColumnIndex("CONSFNAME"));
                this.ADD1 = cursor.getString(cursor.getColumnIndex("ADD1"));
                this.ADD2 = cursor.getString(cursor.getColumnIndex("ADD2"));
                this.ADD3 = cursor.getString(cursor.getColumnIndex("ADD3"));
                this.METERNO = cursor.getString(cursor.getColumnIndex("METER_NO"));
                this.CATEGORY = cursor.getString(cursor.getColumnIndex("CATEGORY"));
                this.FCODE = cursor.getString(cursor.getColumnIndex("FCODE"));
                this.REPLACEMENT_DATE = cursor.getString(cursor.getColumnIndex("REDATE"));
                String LED_NEWAVG = cursor.getString(cursor.getColumnIndex("LED_NEWAVG"));
                String MAST_AVG_CONS = cursor.getString(cursor.getColumnIndex("MAST_AVG_CONS"));
                this.FCOUNT = cursor.getInt(cursor.getColumnIndex("FCOUNT"));
                String LASTC_ISSDATE = cursor.getString(cursor.getColumnIndex("ISSDATE"));
                String LASTC_LASTDATE = cursor.getString(cursor.getColumnIndex("LASTDATE"));
                String LASTTODATE = cursor.getString(cursor.getColumnIndex("DATE2"));
                this.mobno = cursor.getString(cursor.getColumnIndex("MOBILE_NO"));
                this.emailid = cursor.getString(cursor.getColumnIndex("EMAIL_ID"));
                this.CONNOFLAT = cursor.getString(cursor.getColumnIndex("NOFLAT"));

                if (cursor.getString(cursor.getColumnIndex("POS_DOWN_AT")) != null && !"".equals(cursor.getString(cursor.getColumnIndex("POS_DOWN_AT"))) && !"null".equals(cursor.getString(cursor.getColumnIndex("POS_DOWN_AT")))) {
                    Log.e("POS_DOWN_AT", cursor.getString(cursor.getColumnIndex("POS_DOWN_AT")));
                    this.lastupdated += "Masterdetails :- " + DB_DATETIME_FORMAT_DISPLAY(DATE_TIMEFROMSTRING(cursor.getString(cursor.getColumnIndex("POS_DOWN_AT")))) + "\n";
                }
                if (cursor.getString(cursor.getColumnIndex("led_posdownat")) != null && !"".equals(cursor.getString(cursor.getColumnIndex("led_posdownat"))) && !"null".equals(cursor.getString(cursor.getColumnIndex("led_posdownat")))) {
                    Log.e("led_posdownat", cursor.getString(cursor.getColumnIndex("POS_DOWN_AT")));
                    this.lastupdated += "Ledgerdata :- " + DB_DATETIME_FORMAT_DISPLAY(DATE_TIMEFROMSTRING(cursor.getString(cursor.getColumnIndex("led_posdownat"))));
                }

                this.PREVREADING = cursor.getString(cursor.getColumnIndex("CUR_READ"));

                if ("YES".equals(CORRECTION)) {
                    Log.d("CORRECTION", "YES SELECT CUR_READ,FCODE,REMARK FROM ledgerdata WHERE CONSUMERID='" + this.CONSUMERID + "' AND PERDAY1='" + DB_FORMAT(PERDAY1) + "'");
                    try {
                        Cursor getcur = db.rawQuery("SELECT CUR_READ,FCODE,REMARK,NET FROM ledgerdata WHERE CONSUMERID=? AND PERDAY1=?", new String[]{this.CONSUMERID, DB_FORMAT(PERDAY1)});//AND ledgerdata.PERDAY1 = '2021-04-01'
                        if (getcur.getCount() > 0)
                        {
                            while (getcur.moveToNext())
                            {
                                this.CURRENT_READING = getcur.getString(getcur.getColumnIndex("CUR_READ"));
                                this.CURFCODE = getcur.getString(getcur.getColumnIndex("FCODE"));
                                this.REMARKS = getcur.getString(getcur.getColumnIndex("REMARK"));
                                this.OLDNET = getcur.getString(getcur.getColumnIndex("NET"));

                            }
                        } else
                        {
                            this.CURRENT_READING = "";
                            this.CURFCODE = "";
                            this.REMARKS = "";
                            this.OLDNET = "";
                            Log.e("CORRECTION", " NO RECORDS");
                        }
                    } catch (Exception e)
                    {
                        Log.d("CORRECTION ex", e.toString());
                        this.ERROR += e.getCause() + " FOR CONSUMER " + CODY + " - " + CONSUMERID + ".\n";
                    }
                } else
                {
                    this.CURRENT_READING = "";
                    this.CURFCODE = "";
                    this.REMARKS = "";
                    this.OLDNET = "";
                    Log.e("CORRECTION", " NO CORRECTION");
                }

                Log.e("LED_NEWAVG", LED_NEWAVG + " ");

                Log.e("MAST_AVG_CONS", MAST_AVG_CONS + " ");

                if ("".equals(LED_NEWAVG) || LED_NEWAVG == null)
                {
                    Log.d("LED_NEWAVG", ":- NOT FOUND");
                    if (!"".equals(MAST_AVG_CONS) && MAST_AVG_CONS != null) {
                        Log.d("MAST_AVG_CONS", ":- " + MAST_AVG_CONS);
                        this.AVG_CONS = MAST_AVG_CONS;
                    } else {
                        this.ERROR += "AVG NOT FOUND FOR CONSUMER " + CODY + " - " + CONSUMERID + ".\n";
                        Log.d("MAST_AVG_CONS", ":- " + ERROR);
                    }
                } else
                {
                    Log.d("LED_AVG_CONS", ":- " + AVG_CONS);
                    this.AVG_CONS = LED_NEWAVG;
                }

                Log.d("ADD_METER_READ", ":- " + ADD_METER_READ);
                Log.d("ADD_DATE1", ":- " + ADD_DATE1);
                if ("".equals(PREVREADING) || PREVREADING == null || (!"".equals(ADD_METER_READ) && ADD_METER_READ != null)) {
                    this.PREVREADING = ADD_METER_READ;
                }

                if ("".equals(PREVREADING) || PREVREADING == null) {
                    this.PREVREADING = "";
                }

                if (!"".equals(ADD_DATE1) && ADD_DATE1 != null) {
                    this.DATE1 = DATEFROMSTRING(ADD_DATE1);
                } else if (!"".equals(LASTTODATE) && LASTTODATE != null) {

                    this.DATE1 = ADDDAYS(DATEFROMSTRING(LASTTODATE), 1);

                } else {
                    this.DATE1 = null;
                }
                this.ADD_PERDAY1_SKIPPED = null;
                if("".equals(PREVREADING) && DATE1 == null)
                {
                    Cursor addquere = db.rawQuery("SELECT * FROM additionals WHERE CONSUMERID=? ORDER BY ID DESC LIMIT 1", new String[]{CONSUMERID});
                    if (addquere.getCount() > 0)
                    {
                        while (addquere.moveToNext()) {
                            String ADD_METER_READ_re = addquere.getString(addquere.getColumnIndex("METER_READ"));
                            this.PREVREADING = ADD_METER_READ_re;
                            String ADD_DATE1_re = addquere.getString(addquere.getColumnIndex("DATE1"));
                            if (ADD_DATE1_re == null || "".equals(ADD_DATE1_re))
                            {
                                this.ERROR += "FROM DATE NOT FOUND FOR CONSUMER " + CODY + " - " + CONSUMERID + ".\n";
                            }else
                            {
                                this.DATE1 = DATEFROMSTRING(ADD_DATE1_re);
                            }
                            this.form_type = addquere.getString(addquere.getColumnIndex("form_type"));

                            this.ADD_PERDAY1_SKIPPED = DATEFROMSTRING(addquere.getString(addquere.getColumnIndex("PERDAY1")));
                        }
                    }else
                    {
                        this.ADD_PERDAY1_SKIPPED = null;
                        this.ERROR += "PREVIOUS READING & FROM DATE NOT FOUND FOR CONSUMER " + CODY + " - " + CONSUMERID + ".\n";
                    }
                } else
                {
                    if(DATE1 == null)
                    {
                        this.ERROR += "FROM DATE NOT FOUND FOR CONSUMER " + CODY + " - " + CONSUMERID + ".\n";
                    }
                    if("".equals(PREVREADING))
                    {
                        this.ERROR += "PREVIOUS READING NOT FOUND FOR CONSUMER " + CODY + " - " + CONSUMERID + ".\n";
                    }
                }

                Log.e("ADD_PERDAY1_FETCHED",ADD_PERDAY1_SKIPPED+" "+PERDAY1+" "+form_type);

                this.METERSIZE = cursor.getString(cursor.getColumnIndex("METER_SIZE"));

                Date DATETODAY = new Date(System.currentTimeMillis());

                if (!"".equals(PREVREADING.trim()) && LASTC_LASTDATE != null && LASTC_ISSDATE != null && !"".equals(LASTTODATE)) {
                    ContentValues PROC_ARR = PROCESS_ARREARS(ZONE, CONSUMERID, DATEFROMSTRING(LASTC_ISSDATE), DATEFROMSTRING(LASTC_LASTDATE), this.ISSDATE, PERDAY1, DATETODAY, USERNAME, IMEI);
                    String PROCERROR = PROC_ARR.get("ERROR").toString().trim();
                    int RESULT = Integer.parseInt(PROC_ARR.get("RESULT").toString().trim());
                    if (!"".equals(PROCERROR)) {
                        this.ERROR += PROCERROR;
                    } else {
                        ledgerdata.put("PROCESS_ARREARS", RESULT);
                    }
                } else {
                    Log.d("PROC_ARR", "LAST CYCLE DOESNOT EXISTS");
                }
            }

        } else {
            this.ERROR = "ALL READINGS ARE ENTERED FOR " + ZONE;
            Log.d("ERROR", this.ERROR);
        }

        //PAYMENTSTATUS = GETPAYMENTSTATUS(CONSUMERID);

        cursor.close();
        ledgerdata = GET_LEDGER();
        return ledgerdata;
    }

    public String GET_LATEST_PERIOD(String ZONE)
    {
        Log.d("GET_LATEST_PERIOD", "---" + ZONE);
        String LATEST_PERDAY1 = "";
        Date today = new Date(System.currentTimeMillis());
        Log.e("query", "SELECT MAX(PERDAY1) FROM issue_date WHERE ZONE=" + ZONE);
        SQLiteDatabase db = getWritableDatabase();

        try {

            Cursor cursor = db.rawQuery("SELECT MAX(PERDAY1) as PERDAY1 FROM issue_date WHERE ZONE=? ", new String[]{ZONE});

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    if (!"".equals(cursor.getString(cursor.getColumnIndex("PERDAY1"))) && cursor.getString(cursor.getColumnIndex("PERDAY1")) != null) {
                        LATEST_PERDAY1 = cursor.getString(cursor.getColumnIndex("PERDAY1"));
                    } else {
                        return "ERROR :- ISSUE DATE NOT SET FOR ZONE " + ZONE;
                    }
                    Log.d("GET_LATEST_PERIOD", cursor.getString(cursor.getColumnIndex("PERDAY1")) + " ");

                }
                cursor.close();

                return LATEST_PERDAY1;

            } else {

                Log.d("ERROR", this.ERROR);
                cursor.close();
                return "ERROR :- NO RECORDS SET FOR " + ZONE;
            }
        } catch (SQLException e) {
            Log.e("WCLED ERROR", e.getMessage());
            return "ERROR :- " + e.getMessage();
        }

    }

    public String GETBILLINGDATES(Date PERDAY1, String ZONE, String WARD) {
        Log.d("WARD", WARD + "---" + WARD);
        Date MAX_ISSUE_DATE = null;
        Date today = new Date(System.currentTimeMillis());
        String WARDQUE;
        if ("ALL".equals(WARD.trim())) {
            WARDQUE = "";
        } else {
            WARDQUE = "AND WARD = '" + WARD + "'";
        }
        Log.d("PERDAY1-ZONE", PERDAY1.toString() + "---" + ZONE + "---" + WARD + DB_FORMAT(PERDAY1));
        Log.e("query", "SELECT ISSUE_DATE,LASTDATE,MAX_ISSUE_DATE FROM issue_date WHERE PERDAY1=" + DB_FORMAT(PERDAY1) + " AND ZONE=" + ZONE + WARDQUE);
        SQLiteDatabase db = getWritableDatabase();

        try {

            Cursor cursor = db.rawQuery("SELECT ISSUE_DATE,LASTDATE,MAX_ISSUE_DATE FROM issue_date WHERE PERDAY1=? AND ZONE=? ", new String[]{DB_FORMAT(PERDAY1), ZONE});

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    if (!"".equals(cursor.getString(0)) && cursor.getString(0) != null) {
                        this.ISSDATE = DATEFROMSTRING(cursor.getString(0));
                    } else {
                        return "ERROR :- ISSUE DATE NOT FOUND FOR ZONE " + ZONE + " FOR THE PERIOD " + DATETODISPLAY(PERDAY1);
                    }
                    if (!"".equals(cursor.getString(1)) && cursor.getString(1) != null) {
                        this.LASTDATE = DATEFROMSTRING(cursor.getString(1));
                    } else {
                        return "ERROR :- LAST DATE NOT FOUND FOR ZONE " + ZONE + " FOR THE PERIOD " + DATETODISPLAY(PERDAY1);
                    }

                    Log.d("cursor.getString(2)", cursor.getString(2) + " ");

                    if (!"".equals(cursor.getString(2)) && cursor.getString(2) != null) {
                        MAX_ISSUE_DATE = DATE_TIMEFROMSTRING(cursor.getString(2) + " 23:59:59");
                    } else {
                        return "ERROR :- MAX ISSUE DATE DATE NOT FOUND FOR ZONE " + ZONE + " FOR THE PERIOD " + DATETODISPLAY(PERDAY1);
                    }

                    Log.d("ISSDATE-LASTDATE", this.ISSDATE.toString() + "---" + this.LASTDATE.toString() + "---" + MAX_ISSUE_DATE.toString());

                }
                cursor.close();
                if (this.ISSDATE.compareTo(this.LASTDATE) > 0) {
                    return "ERROR :- ISSUE DATE CANNOT BE GREATER THAN LAST DATE";
                }
                Log.d("MAX_ISSUE_DATE", MAX_ISSUE_DATE.toString() + "---" + today.toString());


                if (MAX_ISSUE_DATE.compareTo(today) < 0) {
                    return "ERROR :- CANNOT ISSUE BILLS.MAXIMUM DATE ALLOWED TO ISSUE BILLS EXPIRED ON " + DATETODISPLAY(MAX_ISSUE_DATE) + ".PLEASE CONTACT ADMINISTRATOR.";
                }

                return "SUCCESS";

            } else {

                Log.d("ERROR", this.ERROR);
                cursor.close();
                return "ERROR :- NO RECORDS SET FOR ISSDATE OR LASTDATE";
            }
        } catch (SQLException e) {
            Log.e("WCLED ERROR", e.getMessage());
            return "ERROR :- " + e.getMessage();
        }

    }

    @SuppressLint("Range")
    public String CHECKUPDATE() {
        Date datetime = new Date(System.currentTimeMillis());
        String PENDING_UPDATE = "";
        SQLiteDatabase db = getWritableDatabase();
        Log.d("PENDING_UPDATE QUE", "select tablename,table_type from column_constant_updated where (tablename || '_DOWNLOAD' not in (select trim(distinct(type)) from pwd_sync WHERE date(DateTime)= '" + DB_FORMAT(datetime) + "' ) AND (table_type ='1' OR table_type='2')) OR (tablename || '_UPLOAD' not in (select trim(distinct(type)) from pwd_sync WHERE date(DateTime)= '" + DB_FORMAT(datetime) + "' ) AND (table_type ='2' OR table_type='3'))");
        Cursor cursor = db.rawQuery("select tablename,table_type from column_constant_updated where (tablename || '_DOWNLOAD' not in (select trim(distinct(type)) from pwd_sync WHERE date(DateTime)= ? ) AND (table_type ='1' OR table_type='2')) OR (tablename || '_UPLOAD' not in (select trim(distinct(type)) from pwd_sync WHERE date(DateTime)= ? ) AND (table_type ='2' OR table_type='3'))", new String[]{DB_FORMAT(datetime), DB_FORMAT(datetime)});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                PENDING_UPDATE += cursor.getString(cursor.getColumnIndex("tablename")) + " || ";
            }
            Log.d("PENDING_UPDATE", PENDING_UPDATE);
            cursor.close();
            return PENDING_UPDATE;
        } else {
            return PENDING_UPDATE;
        }

    }


    public String validate(Object str) {
        return (String) (str == null ? "" : str.toString());
    }

    public int validateint(Object str) {
        if (str == "" || str == null) {
            str = 0;
        }
        return Integer.parseInt(str.toString());
    }

    public double validatedouble(Object str) {
        if (str == "" || str == null) {
            str = 0;
        }
        return Double.parseDouble(str.toString());
    }


    public ContentValues GETPREVCYCLE_develop(String CONSUMERID, Date ISSDATE, Date PERDAY1, String USERNAME, String IMEI) {
        ContentValues lastcycle = new ContentValues();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT CUM,METERSTAT,USED1,ARREARS,NEXTARR,NEXTDC,OTS_AMOUNT,ACTUAL_OTS_AMOUNT,ISSDATE,NEWAVG,ZONE,LASTDATE,CONSUMERID FROM ledgerdata WHERE CONSUMERID = ? AND ISSDATE IS NOT NULL AND ISSDATE < ?  AND PERDAY1 <> ? ORDER BY ISSDATE DESC LIMIT 1", new String[]{CONSUMERID, DB_FORMAT(ISSDATE), DB_FORMAT(PERDAY1)});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String CUM = cursor.getString(cursor.getColumnIndex("CUM"));
                String METERSTAT = cursor.getString(cursor.getColumnIndex("METERSTAT"));
                String USED1 = cursor.getString(cursor.getColumnIndex("USED1"));
                String ARREARS = cursor.getString(cursor.getColumnIndex("ARREARS"));
                String LASTC_ISSDATE = cursor.getString(cursor.getColumnIndex("ISSDATE"));
                String NEWAVG = cursor.getString(cursor.getColumnIndex("NEWAVG"));
                String ZONE = cursor.getString(cursor.getColumnIndex("ZONE"));
                String LASTC_LASTDATE = cursor.getString(cursor.getColumnIndex("LASTDATE"));

                Date DATETODAY = new Date(System.currentTimeMillis());

                ContentValues PROC_ARR = PROCESS_ARREARS(ZONE, CONSUMERID, DATEFROMSTRING(LASTC_ISSDATE), DATEFROMSTRING(LASTC_LASTDATE), this.ISSDATE, PERDAY1, DATETODAY, USERNAME, IMEI);
                String PROCERROR = PROC_ARR.get("ERROR").toString().trim();
                int RESULT = Integer.parseInt(PROC_ARR.get("RESULT").toString().trim());
                if (!"".equals(PROCERROR)) {
                    this.ERROR = PROCERROR;
                } else {
                    lastcycle.put("ERROR", "");
                    lastcycle.put("LASTCYCLE", 1);
                    lastcycle.put("PREVCUM", CUM);
                    lastcycle.put("PREVMETERSTAT", METERSTAT);
                    lastcycle.put("PREVUSED1", USED1);
                    lastcycle.put("PREVARREARS", ARREARS);
                    lastcycle.put("PREVNEXTARR", cursor.getString(4));
                    lastcycle.put("PREVNEXTDC", cursor.getString(5));
                    lastcycle.put("PREV_OTSAMOUNT", validate(cursor.getString(6)));
                    lastcycle.put("PREV_ACTUAL_OTS_AMOUNT", validate(cursor.getString(7)));
                    lastcycle.put("PREVISSDATE", LASTC_ISSDATE);
                    lastcycle.put("NEWAVG", NEWAVG);
                    lastcycle.putAll(PROC_ARR);
                }
                Log.e("PREVCY", cursor.getString(cursor.getColumnIndex("NEXTDC")) + " " + cursor.getString(cursor.getColumnIndex("NEXTARR")) + " " + cursor.getString(5) + " " + cursor.getString(12));
            }
        } else {
            lastcycle.put("ERROR", "");
            lastcycle.put("NEWAVG", 0);
            lastcycle.put("LASTCYCLE", 0);
            lastcycle.put("MESSAGE", "NO PREVIOUS CYCLE FOR THIS CONSUMER");
            lastcycle.put("PREVCUM", 0);
            lastcycle.put("PREVMETERSTAT", "");
            lastcycle.put("PREVUSED1", 0);
            lastcycle.put("PREVARREARS", 0);
            lastcycle.put("PREVNEXTARR", 0);
            lastcycle.put("PREVNEXTDC", 0);
            lastcycle.put("PREV_OTSAMOUNT", 0);
            lastcycle.put("PREV_ACTUAL_OTS_AMOUNT", 0);
        }
        cursor.close();
        return lastcycle;
    }

    public ContentValues GETPREVCYCLE(String CONSUMERID, Date ISSDATE, Date PERDAY1, String USERNAME, String IMEI)
    {
        ContentValues lastcycle = new ContentValues();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT CUM,METERSTAT,USED1,ARREARS,NEXTARR,NEXTDC,OTS_AMOUNT,ACTUAL_OTS_AMOUNT,ISSDATE,NEWAVG,ZONE,LASTDATE,CONSUMERID,FCODE FROM ledgerdata WHERE CONSUMERID = ? AND ISSDATE IS NOT NULL AND ISSDATE < ?  AND PERDAY1 <> ? ORDER BY ISSDATE DESC LIMIT 1", new String[]{CONSUMERID, DB_FORMAT(ISSDATE), DB_FORMAT(PERDAY1)});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                lastcycle.put("ERROR", "");
                lastcycle.put("LASTCYCLE", 1);
                lastcycle.put("PREVCUM", cursor.getString(0));
                lastcycle.put("PREVMETERSTAT", cursor.getString(1));
                lastcycle.put("PREVUSED1", cursor.getString(2));
                lastcycle.put("PREVARREARS", cursor.getString(3));
                lastcycle.put("PREVNEXTARR", cursor.getString(4));
                lastcycle.put("PREVNEXTDC", cursor.getString(5));
                lastcycle.put("PREV_OTSAMOUNT", validate(cursor.getString(6)));
                lastcycle.put("PREV_ACTUAL_OTS_AMOUNT", validate(cursor.getString(7)));
                lastcycle.put("PREVISSDATE", cursor.getString(8));
                lastcycle.put("NEWAVG", cursor.getString(9));
                lastcycle.put("LASTFCODE", cursor.getString(cursor.getColumnIndex("FCODE")));
                Log.e("PREVCY", cursor.getString(cursor.getColumnIndex("NEXTDC")) + " " + cursor.getString(cursor.getColumnIndex("NEXTARR")) + " " + cursor.getString(5) + " " + cursor.getString(12));

            }
        } else {
            lastcycle.put("ERROR", "");
            lastcycle.put("NEWAVG", 0);
            lastcycle.put("LASTCYCLE", 0);
            lastcycle.put("MESSAGE", "NO PREVIOUS CYCLE FOR THIS CONSUMER");
            lastcycle.put("PREVCUM", 0);
            lastcycle.put("PREVMETERSTAT", "");
            lastcycle.put("PREVUSED1", 0);
            lastcycle.put("PREVARREARS", 0);
            lastcycle.put("PREVNEXTARR", 0);
            lastcycle.put("PREVNEXTDC", 0);
            lastcycle.put("PREV_OTSAMOUNT", 0);
            lastcycle.put("PREV_ACTUAL_OTS_AMOUNT", 0);
            lastcycle.put("LASTFCODE", "");
        }
        cursor.close();
        return lastcycle;
    }

    public Date DATEFROMSTRING(String DATETOCONVERT) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date DATEFORMAT = null;
        try {
            DATEFORMAT = (Date) sdf.parse(DATETOCONVERT);
        } catch (ParseException e) {
            e.printStackTrace();
            //GRIDDATESSHOWMSG(e.toString(),"ERROR",context);
            return null;
        }
        return DATEFORMAT;
    }

    public String DISPLAY_DATETIMEFORMAT(String DATETOCONVERT) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DATEFORMAT = "";
        try {
            Date DATECONV = (Date) sdf.parse(DATETOCONVERT);
            sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            DATEFORMAT = sdf.format(DATECONV);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return DATEFORMAT;
    }

    public Date DATE_TIMEFROMSTRING(String DATETOCONVERT) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date DATEFORMAT = null;
        try {
            DATEFORMAT = (Date) sdf.parse(DATETOCONVERT);
        } catch (ParseException e) {
            e.printStackTrace();
            //GRIDDATESSHOWMSG(e.toString(),"ERROR",context);
            return null;
        }
        return DATEFORMAT;
    }

    public Date DATEFROMSTRING2(String DATETOCONVERT) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date DATEFORMAT = null;
        try {
            DATEFORMAT = (Date) sdf.parse(DATETOCONVERT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return DATEFORMAT;
    }

    public Date DATE_TIMEFROMSTRING2(String DATETOCONVERT) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date DATEFORMAT = null;
        try {
            DATEFORMAT = (Date) sdf.parse(DATETOCONVERT);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("DATE_TIMEFROMSTRING2", e.getMessage());
        }
        return DATEFORMAT;
    }

    public String DBDATEFORMAT(String DATETOCONVERT) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String DATEFORMAT = "";
        try {
            Date DATECONV = (Date) sdf.parse(DATETOCONVERT);
            sdf = new SimpleDateFormat("dd/MM/yyyy");
            DATEFORMAT = sdf.format(DATECONV);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return DATEFORMAT;
    }

    public String DATETODISPLAY(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String DATEFORMAT = "";
        DATEFORMAT = sdf.format(date);
        return DATEFORMAT;
    }

    public String DB_FORMAT(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String DATEFORMAT = "";
        DATEFORMAT = sdf.format(date);
        return DATEFORMAT;
    }

    public String DB_DATETIME_FORMAT_DISPLAY(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String DATEFORMAT = "";
        DATEFORMAT = sdf.format(date);
        return DATEFORMAT;
    }

    public String DB_DATETIME_FORMATpaytm(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String DATEFORMAT = "";
        DATEFORMAT = sdf.format(date);
        return DATEFORMAT;
    }

    public String DB_DATETIME_FORMAT(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DATEFORMAT = "";
        DATEFORMAT = sdf.format(date);
        return DATEFORMAT;
    }

    public String DISPLAY_ON_BILL(String DATETOCONVERT) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String DATEFORMAT = "";
        try {
            Date DATECONV = (Date) sdf.parse(DATETOCONVERT);
            sdf = new SimpleDateFormat("dd MMM yyyy");
            DATEFORMAT = sdf.format(DATECONV);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return DATEFORMAT;
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public int CALCULATEDAYS(Date DATE1, Date DATE2) {

        long diff = DATE2.getTime() - DATE1.getTime();

        int days = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        return days + 1;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Date MINUSEDAYS(Date DATE1, int DAYS) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(DATE1);
        cal.add(Calendar.DATE, -DAYS);
        DATE1 = cal.getTime();
        System.out.println(DATE1);
        return DATE1;
    }

    public Date ADDDAYS(Date DATE1, int DAYS) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(DATE1);
        cal.add(Calendar.DATE, DAYS);
        DATE1 = cal.getTime();
        System.out.println(DATE1);
        return DATE1;
    }

    public ArrayList<String> faultlist() {
        ArrayList<String> arrlist = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT ID,FAULT,FAULT_NAME FROM fault order by FAULT asc", null);
        if (cursor.getCount() > 0) {
            arrlist.add("SELECT");
            while (cursor.moveToNext()) {
                String FAULT = cursor.getString(cursor.getColumnIndex("FAULT"));

                String FAULT_NAME = cursor.getString(cursor.getColumnIndex("FAULT_NAME"));

                arrlist.add(FAULT + " - " + FAULT_NAME);

            }
            cursor.close();
            return arrlist;
        } else {
            return arrlist;
        }
    }

    public String getfaultname(String FCODE) {
        String faultname = "";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT FAULT_NAME FROM fault WHERE FAULT = ?", new String[]{FCODE});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                faultname = cursor.getString(cursor.getColumnIndex("FAULT_NAME"));
            }
            cursor.close();
            return faultname;
        } else {
            return "-1";
        }
    }

    public int get_printing_exc(String CONSUMERID) {
        int print_except = 0;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT print_except FROM masterdetails WHERE CONSUMERID = ?", new String[]{CONSUMERID});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                print_except = cursor.getInt(cursor.getColumnIndex("print_except"));
            }
            cursor.close();
            return print_except;
        } else {
            return 0;
        }
    }

    @SuppressLint("Range")
    public String get_mob_no(String CONSUMERID) {
        String MOBILE_NO = "";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT MOBILE_NO FROM masterdetails WHERE CONSUMERID = ?", new String[]{CONSUMERID});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                MOBILE_NO = cursor.getString(cursor.getColumnIndex("MOBILE_NO"));
            }
            cursor.close();
            if (!"".equals(MOBILE_NO) && MOBILE_NO != null) {
                return "\nMOBILE NO : " + MOBILE_NO;
            } else {
                return "";
            }

        } else {
            return MOBILE_NO;
        }
    }


    public int GET_CONSUMER_LIST(String ZONE, String POS_SERIAL_NO) {
        int TOTAL_CONSUMERS = 0;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(ID) as TOTAL_CONSUMERS FROM masterdetails WHERE ZONE = ? AND POS_SERIAL_NO = ? AND DELETED=?", new String[]{ZONE, POS_SERIAL_NO, "1"});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                TOTAL_CONSUMERS = cursor.getInt(cursor.getColumnIndex("TOTAL_CONSUMERS"));
            }
            cursor.close();
            return TOTAL_CONSUMERS;
        } else {
            return 0;
        }
    }

    public String GET_CONSUMER_BILLED(String ZONE, String POS_SERIAL_NO, String LASTPERDAY1) {
        Log.e("GET_CON LASTPERDAY2", LASTPERDAY1);
        if (!"".equals(LASTPERDAY1)) {
            Date PERDAY1 = DATEFROMSTRING2(LASTPERDAY1);
            if (PERDAY1 != null) {

                int TOTAL_BILLED = 0;
                SQLiteDatabase db = getWritableDatabase();
                Log.e("GET_CON LASTPERDAY2", "SELECT COUNT(ID) as TOTAL_BILLED FROM ledgerdata WHERE ZONE = ? AND POS_SERIAL_NO = ? AND PERDAY1=?" + ZONE + " " + POS_SERIAL_NO + " " + DB_FORMAT(PERDAY1));
                Cursor cursor = db.rawQuery("SELECT COUNT(ID) as TOTAL_BILLED FROM ledgerdata WHERE ZONE = ? AND POS_SERIAL_NO = ? AND PERDAY1=?", new String[]{ZONE, POS_SERIAL_NO, DB_FORMAT(PERDAY1)});
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        TOTAL_BILLED = cursor.getInt(cursor.getColumnIndex("TOTAL_BILLED"));
                    }

                } else {
                    Log.e("GET_CONSUMER_BILLED", "NO RECORDS");
                    TOTAL_BILLED = 0;
                }
                cursor.close();
                Log.e("TOTAL_CONSUMERS", TOTAL_BILLED + "");
                int TOTAL_CONSUMERS = 0;
                Log.e("GET_CON LASTPERDAY2", "SELECT COUNT(ID) as TOTAL_CONSUMERS FROM masterdetails WHERE ZONE = ? AND POS_SERIAL_NO = ? AND DELETED=?" + ZONE + " " + POS_SERIAL_NO);
                Cursor cursormast = db.rawQuery("SELECT COUNT(ID) as TOTAL_CONSUMERS FROM masterdetails WHERE ZONE = ? AND POS_SERIAL_NO = ? AND DELETED=?", new String[]{ZONE, POS_SERIAL_NO, "0"});
                if (cursormast.getCount() > 0) {
                    while (cursormast.moveToNext()) {
                        TOTAL_CONSUMERS = cursormast.getInt(cursormast.getColumnIndex("TOTAL_CONSUMERS"));
                    }

                } else {
                    Log.e("GET_CONSUMER_BILLED", "NO RECORDS");
                    TOTAL_CONSUMERS = 0;
                }
                cursor.close();
                Log.e("TOTAL_CONSUMERS", TOTAL_CONSUMERS + "");

                int PENDING = 0;

                PENDING = TOTAL_CONSUMERS - TOTAL_BILLED;

                return "TOTAL : " + TOTAL_CONSUMERS + " - BILLED : " + TOTAL_BILLED + " - PENDING :- " + PENDING;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public String GET_PENDING_BILLS(String ZONE, String POS_SERIAL_NO, String FROMDATE) {
        Log.e("GET_PENDING", FROMDATE);
        if (!"".equals(FROMDATE)) {
            Date PERDAY1 = DATEFROMSTRING2(FROMDATE);
            if (PERDAY1 != null) {

                int TOTAL_BILLED = 0;
                SQLiteDatabase db = getWritableDatabase();
                Log.e("GET_CON PERDAY1", "SELECT COUNT(ID) as TOTAL_BILLED FROM ledgerdata WHERE ZONE = ? AND POS_SERIAL_NO = ? AND PERDAY1=?" + ZONE + " " + POS_SERIAL_NO + " " + DB_FORMAT(PERDAY1));
                Cursor cursor = db.rawQuery("SELECT COUNT(ID) as TOTAL_BILLED FROM ledgerdata WHERE ZONE = ? AND POS_SERIAL_NO = ? AND PERDAY1=?", new String[]{ZONE, POS_SERIAL_NO, DB_FORMAT(PERDAY1)});
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        TOTAL_BILLED = cursor.getInt(cursor.getColumnIndex("TOTAL_BILLED"));
                    }

                } else {
                    Log.e("GET_CONSUMER_BILLED", "NO RECORDS");
                    TOTAL_BILLED = 0;
                }
                cursor.close();
                Log.e("TOTAL_CONSUMERS", TOTAL_BILLED + "");
                int TOTAL_CONSUMERS = 0;
                Log.e("GET_CON LASTPERDAY2", "SELECT COUNT(ID) as TOTAL_CONSUMERS FROM masterdetails WHERE ZONE = ? AND POS_SERIAL_NO = ? AND DELETED=?" + ZONE + " " + POS_SERIAL_NO);
                Cursor cursormast = db.rawQuery("SELECT COUNT(ID) as TOTAL_CONSUMERS FROM masterdetails WHERE ZONE = ? AND POS_SERIAL_NO = ? AND DELETED=?", new String[]{ZONE, POS_SERIAL_NO, "0"});
                if (cursormast.getCount() > 0) {
                    while (cursormast.moveToNext()) {
                        TOTAL_CONSUMERS = cursormast.getInt(cursormast.getColumnIndex("TOTAL_CONSUMERS"));
                    }

                } else {
                    Log.e("GET_CONSUMER_BILLED", "NO RECORDS");
                    TOTAL_CONSUMERS = 0;
                }
                cursor.close();
                Log.e("TOTAL_CONSUMERS", TOTAL_CONSUMERS + "");

                int PENDING = 0;

                PENDING = TOTAL_CONSUMERS - TOTAL_BILLED;

                return "TOTAL - " + TOTAL_CONSUMERS + " BILLED :- " + TOTAL_BILLED + " PENDING :- " + PENDING;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public ArrayList<String> categorylist() {
        ArrayList<String> arrlist = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT ID,CATEGORY,CATEGORY_NAME FROM category GROUP BY CATEGORY ORDER BY CATEGORY ASC", null);
        if (cursor.getCount() > 0) {
            arrlist.add("SELECT");
            while (cursor.moveToNext()) {
                String CATEGORY = cursor.getString(cursor.getColumnIndex("CATEGORY"));

                String CATEGORY_NAME = cursor.getString(cursor.getColumnIndex("CATEGORY_NAME"));

                arrlist.add(CATEGORY + " - " + CATEGORY_NAME);

            }
            cursor.close();
            return arrlist;
        } else {
            return arrlist;
        }
    }

    public ArrayList<String> CONSUMERLIST(String ZONE, Date PERDAY1, int list_type) {
        ArrayList<String> arrlist = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Date today = new Date(System.currentTimeMillis());
        Cursor cursor = null;

        Log.e("CONSUMERLIST", ZONE + " " + PERDAY1.toString() + " " + list_type);

        if (list_type == 1) {
            cursor = db.rawQuery("SELECT DISTINCT(masterdetails.CONSUMERID),masterdetails.walking_sequence,masterdetails.CODY,masterdetails.CONSFNAME,masterdetails.ADD1,masterdetails.ADD2,masterdetails.ADD3,masterdetails.METER_NO,masterdetails.MOBILE_NO FROM masterdetails left join ledgerdata on masterdetails.CONSUMERID=ledgerdata.CONSUMERID WHERE masterdetails.ZONE = ? AND masterdetails.DELETED =? AND masterdetails.CONSUMERID NOT IN (SELECT ledgerdata.CONSUMERID FROM ledgerdata where ledgerdata.PERDAY1 = ? AND ledgerdata.zone = ?) ORDER BY masterdetails.walking_sequence ASC", new String[]{ZONE, "0", DB_FORMAT(PERDAY1), ZONE});
            Log.e("CONSUMER list_type1", list_type + " ");
        } else if (list_type == 3) {
            cursor = db.rawQuery("SELECT ledgerdata.CONSUMERID,ledgerdata.CODY,masterdetails.CONSFNAME,ledgerdata.ADD1,ledgerdata.ADD2,ledgerdata.ADD3,ledgerdata.METER_NO,masterdetails.MOBILE_NO FROM ledgerdata left join masterdetails on ledgerdata.CONSUMERID=masterdetails.CONSUMERID  WHERE ledgerdata.ZONE = ? AND ledgerdata.PERDAY1 = ? AND ledgerdata.TEMP_ISSDATE = ? AND ledgerdata.modified_count > 0 ORDER BY ledgerdata.generated_at DESC", new String[]{ZONE, DB_FORMAT(PERDAY1), DB_FORMAT(today)});
            Log.e("CONSUMER list_type2", list_type + " ");
        } else {
            Log.e("CONSUMER list_type3", list_type + " ");
            cursor = db.rawQuery("SELECT ledgerdata.CONSUMERID,ledgerdata.CODY,masterdetails.CONSFNAME,ledgerdata.ADD1,ledgerdata.ADD2,ledgerdata.ADD3,ledgerdata.METER_NO,masterdetails.MOBILE_NO FROM ledgerdata left join masterdetails on ledgerdata.CONSUMERID=masterdetails.CONSUMERID  WHERE ledgerdata.ZONE = ? AND ledgerdata.PERDAY1 = ?  ORDER BY ledgerdata.generated_at DESC", new String[]{ZONE, DB_FORMAT(PERDAY1)});
        }

        if (cursor.getCount() > 0) {
            arrlist.add("SELECT");
            int i = 1;
            while (cursor.moveToNext()) {
                String CONSUMERID = cursor.getString(cursor.getColumnIndex("CONSUMERID"));

                String CODY = cursor.getString(cursor.getColumnIndex("CODY"));

                String CONSFNAME = cursor.getString(cursor.getColumnIndex("CONSFNAME"));

                String ADD1 = cursor.getString(cursor.getColumnIndex("ADD1"));

                String ADD2 = cursor.getString(cursor.getColumnIndex("ADD2"));

                String ADD3 = cursor.getString(cursor.getColumnIndex("ADD3"));

                if (ADD1 == null) {
                    ADD1 = "";
                }

                if (ADD2 == null) {
                    ADD2 = "";
                }

                if (ADD3 == null) {
                    ADD3 = "";
                }

                String METER_NO = cursor.getString(cursor.getColumnIndex("METER_NO"));

                String MOBILE_NO = cursor.getString(cursor.getColumnIndex("MOBILE_NO"));

                if (MOBILE_NO == null || "".equals(MOBILE_NO)) {
                    MOBILE_NO = "";
                } else {
                    MOBILE_NO = " - MOB NO : " + MOBILE_NO;
                }

                arrlist.add(CODY + " - " + CONSUMERID + " - " + CONSFNAME + " - " + ADD1 + " " + ADD2 + " " + ADD3 + MOBILE_NO + " - " + METER_NO);
                i++;
            }
            cursor.close();
            return arrlist;
        } else {
            return arrlist;
        }
    }

    public ArrayList<String> TOTAL_CONSUMERLIST(String ZONE, Date PERDAY1) {
        ArrayList<String> arrlist = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT(masterdetails.CONSUMERID),ledgerdata.perday1,masterdetails.CODY,masterdetails.CONSFNAME,masterdetails.ADD1,masterdetails.ADD2,masterdetails.ADD3,masterdetails.METER_NO,masterdetails.MOBILE_NO FROM masterdetails LEFT JOIN ledgerdata ON masterdetails.CONSUMERID=ledgerdata.CONSUMERID AND ledgerdata.PERDAY1=? WHERE masterdetails.ZONE = ? AND masterdetails.DELETED =? ORDER BY masterdetails.CODY ASC", new String[]{DB_FORMAT(PERDAY1), ZONE, "0"});
        if (cursor.getCount() > 0) {
            arrlist.add("SELECT");
            int i = 1;
            while (cursor.moveToNext()) {
                String CONSUMERID = cursor.getString(cursor.getColumnIndex("CONSUMERID"));

                String CODY = cursor.getString(cursor.getColumnIndex("CODY"));

                String CONSFNAME = cursor.getString(cursor.getColumnIndex("CONSFNAME"));

                String ADD1 = cursor.getString(cursor.getColumnIndex("ADD1"));

                String ADD2 = cursor.getString(cursor.getColumnIndex("ADD2"));

                String ADD3 = cursor.getString(cursor.getColumnIndex("ADD3"));

                String METER_NO = cursor.getString(cursor.getColumnIndex("METER_NO"));

                String MOBILE_NO = cursor.getString(cursor.getColumnIndex("MOBILE_NO"));

                String ZONE_PERDAY1 = cursor.getString(cursor.getColumnIndex("PERDAY1"));

                if (MOBILE_NO == null || "".equals(MOBILE_NO)) {
                    MOBILE_NO = "";
                } else {
                    MOBILE_NO = " - MOB NO : " + MOBILE_NO;
                }

                if ("".equals(ZONE_PERDAY1) || ZONE_PERDAY1 == null) {
                    arrlist.add(CONSUMERID + " - " + CODY + " - " + CONSFNAME + " - " + ADD1 + " " + ADD2 + " " + ADD3 + MOBILE_NO + " - " + METER_NO);

                } else {
                    arrlist.add(" BILLED ----- " + CONSUMERID + " - " + CODY + " - " + CONSFNAME + " - " + ADD1 + " " + ADD2 + " " + ADD3 + MOBILE_NO + " - " + METER_NO);
                }
                i++;
            }
            cursor.close();
            return arrlist;
        } else {
            return arrlist;
        }
    }

    public ArrayList<String> CONSUMERLIST_IDWISE(String ZONE) {
        ArrayList<String> arrlist = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT(CONSUMERID),CODY,CONSFNAME,ADD1,ADD2,ADD3,METER_NO,MOBILE_NO FROM masterdetails WHERE ZONE = ? AND DELETED =? ORDER BY CODY ASC", new String[]{ZONE, "0"});
        if (cursor.getCount() > 0) {
            arrlist.add("SELECT");
            int i = 1;
            while (cursor.moveToNext()) {
                String CONSUMERID = cursor.getString(cursor.getColumnIndex("CONSUMERID"));

                String CODY = cursor.getString(cursor.getColumnIndex("CODY"));

                String CONSFNAME = cursor.getString(cursor.getColumnIndex("CONSFNAME"));

                String ADD1 = cursor.getString(cursor.getColumnIndex("ADD1"));

                String ADD2 = cursor.getString(cursor.getColumnIndex("ADD2"));

                String ADD3 = cursor.getString(cursor.getColumnIndex("ADD3"));

                String METER_NO = cursor.getString(cursor.getColumnIndex("METER_NO"));

                String MOBILE_NO = cursor.getString(cursor.getColumnIndex("MOBILE_NO"));

                if (MOBILE_NO == null) {
                    MOBILE_NO = "";
                }

                arrlist.add(CONSUMERID + " - " + CODY + " - " + CONSFNAME + " - " + ADD1 + " " + ADD2 + " " + ADD3 + MOBILE_NO + " - " + METER_NO);
                i++;
            }
            cursor.close();
            return arrlist;
        } else {
            return arrlist;
        }
    }

    public int check_previous_reading(Date PERDAY1_chk,String CONSUMERID_chk,long PREV_READING_chk,String additional_reading_chk)
    {
        Log.e("check_previous_reading",CONSUMERID_chk+"SELECT CUR_READ FROM ledgerdata WHERE CONSUMERID = ? AND PERDAY1 < ?  ORDER BY PERDAY1 DESC LIMIT 1"+PERDAY1_chk);
        SQLiteDatabase db = getWritableDatabase();
        long CUR_READ = 0;
        int success = 0;
        Cursor cursor = db.rawQuery("SELECT CUR_READ FROM ledgerdata WHERE CONSUMERID = ? AND PERDAY1 < ?  ORDER BY PERDAY1 DESC LIMIT 1", new String[]{CONSUMERID_chk,DB_FORMAT(PERDAY1_chk)});//DIVi='IX' AND SUBDIV='I' AND ZONE='VSR' AND
        if (cursor.getCount() > 0)
        {
            while (cursor.moveToNext())
            {
                CUR_READ = cursor.getLong(cursor.getColumnIndex("CUR_READ"));
            }
        }
        Log.e("CUR_READ","check_previous_reading"+CUR_READ+PREV_READING_chk);
        if(CUR_READ == PREV_READING_chk)
        {
            success = 1;
        }else
        {
            Cursor cursor2 = db.rawQuery("SELECT * FROM additionals WHERE CONSUMERID = ? AND METER_READ = ? ORDER BY PERDAY1 DESC", new String[]{CONSUMERID_chk, String.valueOf(PREV_READING_chk)});
            if (cursor2.getCount() > 0)
            {
                success = 1;
            }else
            {
                success = 0;
            }
        }

        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ContentValues CALCULATE_CHARGES(String FCNT, String USERNAME, String IMEI, String CONSUMERID, long PREV_READING, long CUR_READING, String CUR_FAULT, String BILLED_AS_CATEGORY, Date DATE1, Date DATE2, Date PERDAY1, Date PERDAY2, int MAXSIZE, int EXTRA_UNITS, int FINAL_READ, String AVG_CONS_LC, String MET_REPLACEMENT_DATE, String REMARKS, String ENT_EMAIL_ID, String ENT_MOBILE_NO, Date ADD_PERDAY1_FETCHED, String NEW_FORM_TYPE) {

        ContentValues WAT_SEW_FUN, METER_RENT_RESPONSE, LPCD_RES;
        ContentValues savedata = new ContentValues();

        String CALERROR = "";
        String CATEGORY = "";
        double UNITS_USED_DBS = 0;
        double SUNDRY = 0;
        double ADDCHARGE = 0;
        double PREVARR = 0;
        double NEXTARR = 0;
        double NEXTDC = 0;
        int TEMPDAYS;
        int ARR_TAG = 0;
        int PREV_CUM = 0;
        int iterator = 0;
        String BASE_DB = "";
        double MIN_UNITS_CONSUMER_DB = 0;
        double MIN_CHARGES_DB = 0;
        double LPCD = 0;
        double WATER_CHARGES = 0;
        double SEWARAGE_CHARGES = 0;
        double USED1 = 0;
        double USED = 0;
        double MIN_UNIT_CATEGORY = 0;
        double ARREARS_CREDIT = 0;
        double AMOUNT = 0;
        double RATIO = 0;
        double PERDAY;
        double NEW_AVG = 0;
        double CURRENT_CUM = 0;
        double NET_PAYABLE = 0;
        String CHECK_RATIO = "";
        String BASE = "";
        String CONSUMER_W_TAP_FACTOR = "";
        String CONSUMER_W_MF_FACTOR = "";
        String ADDBILLED_AS_CATEGORY = "";
        double TOTAL_USED = 0;
        double TOTAL_USED1 = 0;
        double MIN_CHARGES = 0;
        double PREVUSED1 = 0;
        double ADD_UNITS_MF_FACTOR = 0;
        double OTSAMOUNT = 0;
        double ACTUAL_OTS_AMOUNT = 0;

        ContentValues CHARGES = new ContentValues();

        double ADDUNITS = 0;
        double EXUNITS = 0;
        double AVGUNIT = 0;
        double ADD_DC = 0;
        double ADD_ARREARS = 0;
        Date REPLACEMENT_DATE = null;
        String METER_STATUS, CAT_CHANGE_DATE, PREVMETERSTAT = "";

        int FCOUNT = Integer.parseInt(FCNT);

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT SEWARAGE_DATE,ID,DIVi,SUBDIV,REV_SUBDIV,CODY,ZONE,WARD,SRLNO,EXT,TITLE,CONSFNAME,CAT_GROUP,ADD1,ADD2,ADD3,METER_NO,INSTALL_DT,MET_STATDT,PINCODE,VILL_CODE,SEC_DEPOSI,METER_MAKE,DMA_NO,NOFLAT,POPULA,MIN_UNIT,AVG_CONS,CATEGORY,METCODE,SEWARAGE,MF_FACTOR,TAP_FACTOR FROM masterdetails WHERE DELETED=0 AND CONSUMERID = ? GROUP BY CONSUMERID ORDER BY CODY ASC", new String[]{CONSUMERID});//DIVi='IX' AND SUBDIV='I' AND ZONE='VSR' AND
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String DIV_NM = cursor.getString(cursor.getColumnIndex("DIVi"));
                String SUBDIV = cursor.getString(cursor.getColumnIndex("SUBDIV"));
                String REV_SUBDIV = cursor.getString(cursor.getColumnIndex("REV_SUBDIV"));
                String CODY = cursor.getString(cursor.getColumnIndex("CODY"));
                //String LEDGERID = cursor.getString(cursor.getColumnIndex("ID"));
                String ZONE = cursor.getString(cursor.getColumnIndex("ZONE"));
                String WARD = cursor.getString(cursor.getColumnIndex("WARD"));
                String SRLNO = cursor.getString(cursor.getColumnIndex("SRLNO"));
                String EXT = cursor.getString(cursor.getColumnIndex("EXT"));
                String TITLE = cursor.getString(cursor.getColumnIndex("TITLE"));
                String NAME = cursor.getString(cursor.getColumnIndex("CONSFNAME"));
                String CATEGORY_GROUP = cursor.getString(cursor.getColumnIndex("CAT_GROUP"));
                String ADD1 = cursor.getString(cursor.getColumnIndex("ADD1"));
                String ADD2 = cursor.getString(cursor.getColumnIndex("ADD2"));
                String ADD3 = cursor.getString(cursor.getColumnIndex("ADD3"));
                String METER_NO = cursor.getString(cursor.getColumnIndex("METER_NO"));
                String INSTALL_DT = cursor.getString(cursor.getColumnIndex("INSTALL_DT"));
                String MET_STATDT = cursor.getString(cursor.getColumnIndex("MET_STATDT"));
                String PINCODE = cursor.getString(cursor.getColumnIndex("PINCODE"));
                String VILL_CODE = cursor.getString(cursor.getColumnIndex("VILL_CODE"));
                String SEC_DEPOSI = cursor.getString(cursor.getColumnIndex("SEC_DEPOSI"));
                String METER_MAKE = cursor.getString(cursor.getColumnIndex("METER_MAKE"));
                String DMA_NO = cursor.getString(cursor.getColumnIndex("DMA_NO"));
                //double AVG_CONS = cursor.getDouble(cursor.getColumnIndex("AVG_CONS"));

                int FLATS = cursor.getInt(cursor.getColumnIndex("NOFLAT"));
                int POPULA = cursor.getInt(cursor.getColumnIndex("POPULA"));
                double MIN_UNIT_CONSUMER = cursor.getDouble(cursor.getColumnIndex("MIN_UNIT"));
                String ACTCATEGORY = cursor.getString(cursor.getColumnIndex("CATEGORY"));
                String METCODE = cursor.getString(cursor.getColumnIndex("METCODE"));
                String SEWARAGE_CON = cursor.getString(cursor.getColumnIndex("SEWARAGE"));
                String SEWARAGE_DATE = cursor.getString(cursor.getColumnIndex("SEWARAGE_DATE"));
                double MF_FACTOR = cursor.getInt(cursor.getColumnIndex("MF_FACTOR"));
                double TAP_FACTOR = cursor.getInt(cursor.getColumnIndex("TAP_FACTOR"));

                int ACTUAL_FLAT = FLATS;

                if (!"".equals(BILLED_AS_CATEGORY)) {
                    CATEGORY = BILLED_AS_CATEGORY.trim();
                    Log.e("CATEGORY1", CATEGORY);
                } else {
                    CATEGORY = ACTCATEGORY.trim();
                    Log.e("CATEGORY2", CATEGORY);
                }

                if (!"".equals(MET_REPLACEMENT_DATE)) {
                    //REPLACEMENT_DATE = DATEFROMSTRING(MET_REPLACEMENT_DATE);
                }

                if (MIN_UNIT_CONSUMER < 1) {
                    CALERROR += "CONSUMER MINIMUM UNITS CANNOT BE ZERO. CONSUMER " + CONSUMERID;
                }
                String additional_reading = "";
                Cursor cursadds;
                //ADD_PERDAY1_FETCHED = null;
                Log.e("ADD_PERDAY1_FETCHED",ADD_PERDAY1_FETCHED+" "+PERDAY1+" "+NEW_FORM_TYPE);
                if(ADD_PERDAY1_FETCHED !=null && "A-FORM".equals(NEW_FORM_TYPE))
                {
                    cursadds = db.rawQuery("SELECT * FROM additionals WHERE CONSUMERID =? AND PERDAY1 = ?", new String[]{CONSUMERID, DB_FORMAT(ADD_PERDAY1_FETCHED)});

                }else
                {
                    cursadds = db.rawQuery("SELECT * FROM additionals WHERE CONSUMERID =? AND PERDAY1 = ?", new String[]{CONSUMERID, DB_FORMAT(PERDAY1)});

                }

                if (cursadds.getCount() > 0) {
                    while (cursadds.moveToNext()) {
                        additional_reading = cursadds.getString(cursadds.getColumnIndex("METER_READ"));
                        ADDUNITS = cursadds.getDouble(cursadds.getColumnIndex("ADDUNITS"));
                        EXUNITS = cursadds.getDouble(cursadds.getColumnIndex("EXUNITS"));
                        ADDCHARGE = cursadds.getDouble(cursadds.getColumnIndex("ADDCHARGE"));
                        SUNDRY = cursadds.getDouble(cursadds.getColumnIndex("SUNDRY"));
                        //ADDBILLED_AS_CATEGORY = cursadds.getString(cursadds.getColumnIndex("BILLED_AS_CATEGORY"));
                        ADD_ARREARS = cursadds.getDouble(cursadds.getColumnIndex("ARREARS"));
                        ADD_DC = cursadds.getDouble(cursadds.getColumnIndex("DC"));
                        String addrepdate = cursadds.getString(cursadds.getColumnIndex("REPLACEMENT_DATE"));

                        if (!"".equals(addrepdate) && addrepdate != null) {
                            REPLACEMENT_DATE = DATEFROMSTRING(addrepdate);
                        } else {
                            REPLACEMENT_DATE = null;
                        }

                        //ADDUNITS = cursor.getDouble(cursor.getColumnIndex("METER_READ"));
                        String ADDITIONALS_DATE1 = cursadds.getString(cursadds.getColumnIndex("DATE1"));
                        if (DATE1 == null && !"".equals(ADDITIONALS_DATE1)) {
                            DATE1 = DATEFROMSTRING(cursadds.getString(cursadds.getColumnIndex("DATE1")));
                        }
                    }
                } else {
                    REPLACEMENT_DATE = null;
                    ADDUNITS = 0;
                    EXUNITS = 0;
                    ADDCHARGE = 0;
                    SUNDRY = 0;
                    ADD_ARREARS = 0;
                    ADD_DC = 0;
                }
                cursadds.close();

                if ("25801001".equals(CONSUMERID)) {
                    double VSR_USEDUNITS = 0;
                    Cursor GETUNITS = db.rawQuery("SELECT USED FROM ledgerdata WHERE CONSUMERID =? AND PERDAY1 = ?", new String[]{"25801002", DB_FORMAT(PERDAY1)});
                    if (GETUNITS.getCount() > 0) {
                        while (GETUNITS.moveToNext()) {
                            VSR_USEDUNITS = GETUNITS.getDouble(GETUNITS.getColumnIndex("USED"));
                            if (VSR_USEDUNITS == 0) {
                                CALERROR += "BITS PILANI GOA CAMPUS (VSR0100675001 - 25801002) UNITS USED IS ZERO";
                            }
                            EXUNITS = EXUNITS - rounddecimal(VSR_USEDUNITS, 0);
                        }
                    } else {
                        CALERROR += "BILL NOT GENERATED FOR BITS PILANI GOA CAMPUS (VSR0100675001 - 25801002).PLEASE GENERATE BILL FOR VSR0100675001 TO GET UNITS USED";
                    }
                    GETUNITS.close();
                }

                double AVG_CONS = Double.parseDouble(AVG_CONS_LC);

                EXUNITS = EXUNITS + EXTRA_UNITS;

                int DAYS = 0;
                if (DATE1 == null) {
                    CALERROR += "DATE1 NOT FOUND IN LEDGER OR ADDITIONALS";
                } else {
                    DAYS = CALCULATEDAYS(DATE1, DATE2);
                }

                if (!"".equals(CUR_FAULT.trim()) && !"TD".equals(CUR_FAULT.trim()) && !"NC".equals(CUR_FAULT.trim())) {
                    FCOUNT = FCOUNT + 1;

                    METER_STATUS = "D";

                } else {
                    FCOUNT = 0;

                    METER_STATUS = "L";

                    Log.e("CUR_FAULT check first", "CUR_FAULT NOT FOUND");

                }

                if (!"SO".equals(CATEGORY) && !"SU".equals(CATEGORY) && !"MF".equals(CATEGORY)) {
                    FLATS = 1;
                    Log.e("FLATS", "NOT SU SO MF " + FLATS);
                } else {
                    Log.e("FLATS", "ITS SU SO MF " + FLATS);

                }

                int SDROOMS = 0;
                if ("SD".equals(CATEGORY)) {
                    SDROOMS = ACTUAL_FLAT;
                    Log.e("SDROOMS", "SD " + FLATS);
                } else {
                    Log.e("SDROOMS", "NOT SD " + SDROOMS);
                }


                ContentValues LASTCYCLE = GETPREVCYCLE(CONSUMERID, ISSDATE, PERDAY1, USERNAME, IMEI);

                CALERROR += LASTCYCLE.get("ERROR").toString();

                if ("".equals(LASTCYCLE.get("PREVCUM")) || LASTCYCLE.get("PREVCUM").equals(null)) {
                    CALERROR += "PREVIOUS CUMULATIVE IS EMPTY";
                } else {
                    PREV_CUM = Integer.parseInt(LASTCYCLE.get("PREVCUM").toString());
                }

                if ("".equals(LASTCYCLE.get("PREVUSED1")) || LASTCYCLE.get("PREVUSED1").equals(null)) {
                    CALERROR += "PREVIOUS USED1 IS EMPTY";
                } else {
                    PREVUSED1 = Double.parseDouble(LASTCYCLE.get("PREVUSED1").toString());
                }

                if (LASTCYCLE.get("PREVMETERSTAT") == null) {

                    if (LASTCYCLE.get("LASTFCODE") != null) {
                        Log.e("LCY PREVMETERSTAT", "LASTCYCLE.get(LASTFCODE)" + LASTCYCLE.get("LASTFCODE"));

                        if (!"".equals(LASTCYCLE.get("LASTFCODE")) && !"TD".equals(LASTCYCLE.get("LASTFCODE")) && !"NC".equals(LASTCYCLE.get("LASTFCODE"))) {
                            PREVMETERSTAT = "D";

                        } else {
                            PREVMETERSTAT = "L";
                            Log.e("LCY PREVMETERSTAT", "B O LASTCODE" + PREVMETERSTAT);
                        }
                    } else {
                        PREVMETERSTAT = "L";
                        Log.e("LCY PREVMETE null", "B O LASTCODE" + PREVMETERSTAT);
                    }
                } else {
                    PREVMETERSTAT = LASTCYCLE.get("PREVMETERSTAT").toString();
                }

                Log.e("PREVMETERSTAT", "PREVMETERSTAT" + PREVMETERSTAT);

                if ("".equals(LASTCYCLE.get("PREVARREARS")) || LASTCYCLE.get("PREVARREARS").equals(null)) {
                    CALERROR += "PREVIOUS ARREARS IS EMPTY";
                } else {
                    PREVARR = Double.parseDouble(LASTCYCLE.get("PREVARREARS").toString());
                }

                if ("".equals(LASTCYCLE.get("NEWAVG")) || LASTCYCLE.get("NEWAVG").equals(null)) {
                    CALERROR += "PREVIOUS CYCLE AVG NOT FOUND";
                } else {
                    AVGUNIT = Double.parseDouble(LASTCYCLE.get("NEWAVG").toString());
                }

                Log.e("UNITSPMONTHRES USED1", LASTCYCLE.get("PREVNEXTARR") + "" + LASTCYCLE.get("PREVNEXTDC"));

                if ("".equals(validate(LASTCYCLE.get("PREVNEXTARR"))) || "".equals(validate(LASTCYCLE.get("PREVNEXTARR")))) {
                    CALERROR += "ARREARS IS EMPTY";
                } else {
                    NEXTARR = Double.parseDouble(LASTCYCLE.get("PREVNEXTARR").toString());
                }

                if ("".equals(validate(LASTCYCLE.get("PREVNEXTDC"))) || "".equals(validate(LASTCYCLE.get("PREVNEXTDC")))) {
                    CALERROR += "CREDIT IS EMPTY";
                } else {
                    NEXTDC = Double.parseDouble(LASTCYCLE.get("PREVNEXTDC").toString());
                }

                NEXTARR = NEXTARR + ADD_ARREARS;

                NEXTDC = NEXTDC + ADD_DC;

                OTSAMOUNT = validatedouble(LASTCYCLE.get("PREV_OTSAMOUNT"));

                ACTUAL_OTS_AMOUNT = validatedouble(LASTCYCLE.get("PREV_ACTUAL_OTS_AMOUNT"));

                int prvcheck = check_previous_reading(PERDAY1,CONSUMERID,PREV_READING,additional_reading);
                if(prvcheck == 0 && MAXSIZE <= 0)
                {
                    CALERROR += "PREVIOUS READING NOT MATCHING.PLEASE REFRESH AND RETRY";
                }

                if ("".equals(CALERROR.trim())) {
                    Date NEWFROMDATE = DATE1;

                    Date NEWTODATE = DATE2;

                    int ACTUALDAYS = CALCULATEDAYS(NEWFROMDATE, NEWTODATE);

                    String FREE_SCHEMEUNITS = "";
                    int FREE_BILL = 0;
                    int FREE_DAYS = 0;

                    Cursor CHKSCHCOUNT = db.rawQuery("SELECT * FROM scheme", null);
                    if (CHKSCHCOUNT.getCount() < 1) {
                        CALERROR += "SCHEME DETAILS ARE EMPTY";
                    }
                    double calculated_min_units_consumer = 0;
                    if ("SO".equals(CATEGORY) || "SU".equals(CATEGORY) || "MF".equals(CATEGORY)) {
                        calculated_min_units_consumer = MIN_UNIT_CONSUMER / FLATS;
                    } else {
                        calculated_min_units_consumer = MIN_UNIT_CONSUMER;
                    }

                    long ACTUAL_CONSUMPTION = CUR_READING - PREV_READING;
                    Log.e("CUR_FAULT", CUR_FAULT.trim() + " " + ACTUAL_CONSUMPTION);
                    if (("".equals(CUR_FAULT.trim()) || "TD".equals(CUR_FAULT.trim()) || "NC".equals(CUR_FAULT.trim())) && calculated_min_units_consumer <= 16) {
                        Cursor getscheme = db.rawQuery("SELECT * FROM scheme WHERE category=? ORDER BY date DESC", new String[]{CATEGORY});
                        int catres = 0;
                        catres = getscheme.getCount();
                        if (catres > 0) {
                            while (getscheme.moveToNext()) {
                                double FREE_UNITS = getscheme.getDouble(getscheme.getColumnIndex("units"));

                                Date SCH_DATE = DATEFROMSTRING(getscheme.getString(getscheme.getColumnIndex("date")));

                                if (FREE_UNITS != 0 && SCH_DATE != null) {
                                    Log.e("SCHEME DATES", NEWFROMDATE + " " + NEWTODATE + " " + SCH_DATE + " " + CONSUMERID);
                                    if (NEWFROMDATE.compareTo(SCH_DATE) >= 0 && NEWTODATE.compareTo(SCH_DATE) >= 0)//condition 1
                                    {
                                        Log.e("SCHEME DATES", "BILLING DATES ARE AFTER SCHEME DATE");
                                        ContentValues schres = FREE_16UNITS_SCHEME(CUR_FAULT, NEWFROMDATE, NEWTODATE, CUR_READING, PREV_READING, FREE_UNITS, PREV_CUM, EXUNITS, ADDUNITS, REPLACEMENT_DATE, CATEGORY, FLATS);
                                        if ("1".equals(schres.get("SCHEME_STATUS").toString())) {

                                            FREE_SCHEMEUNITS = schres.get("UPM").toString();

                                            FREE_BILL = 1;

                                            FREE_DAYS = CALCULATEDAYS(NEWFROMDATE, NEWTODATE);

                                            Log.e("SCHEME", "SCHEME APPLIED " + FREE_SCHEMEUNITS + " FOR " + CONSUMERID);
                                        } else {
                                            Log.e("SCHEME", "SCHEME NOT APPLIED.UNITS MORE THAN   " + FREE_UNITS + " FOR " + CONSUMERID);
                                        }
                                    } else if (NEWFROMDATE.compareTo(SCH_DATE) < 0 && NEWTODATE.compareTo(SCH_DATE) >= 0)//condition 1
                                    {
                                        Log.e("SCHEME DATES", "SCHEME DATE IS BETWEEN BILLING DATE");
                                        ContentValues schres = FREE_16UNITS_SCHEME(CUR_FAULT, NEWFROMDATE, NEWTODATE, CUR_READING, PREV_READING, FREE_UNITS, PREV_CUM, EXUNITS, ADDUNITS, REPLACEMENT_DATE, CATEGORY, FLATS);
                                        if ("1".equals(schres.get("SCHEME_STATUS").toString())) {

                                            FREE_SCHEMEUNITS = schres.get("UPM").toString();

                                            //FREE_BILL = 1;

                                            FREE_DAYS = CALCULATEDAYS(SCH_DATE, NEWTODATE);

                                            NEWTODATE = MINUSEDAYS(SCH_DATE, 1);

                                            Log.e("SCHEME", "SCHEME APPLIED " + FREE_SCHEMEUNITS + " FOR " + CONSUMERID);
                                        } else {
                                            Log.e("SCHEME", "SCHEME NOT APPLIED.UNITS MORE THAN   " + FREE_UNITS + " FOR " + CONSUMERID);
                                        }
                                    } else {
                                        CALERROR += "BILLING DATES NOT CROSSING SCHEME DATE FOR " + CONSUMERID;
                                        Log.e("SCHEME", "BILLING DATES NOT CROSSING SCHEME DATE FOR " + CONSUMERID);
                                    }
                                } else {
                                    if (FREE_UNITS == 0) {
                                        CALERROR += "INVALID FREE UNITS " + FREE_UNITS + " FOR " + CONSUMERID;
                                        Log.e("SCHEME", "INVALID FREE UNITS " + FREE_UNITS + " FOR " + CONSUMERID);
                                    }
                                    if (SCH_DATE != null) {
                                        CALERROR += "SCHEME DATE NOT FOUND FOR " + CONSUMERID;
                                        Log.e("SCHEME", "SCHEME DATE NOT FOUND FOR " + CONSUMERID);
                                    }
                                }
                            }
                        } else {
                            Log.e("SCHEME", "NOT APPLICABLE FOR " + CATEGORY + " CATEGORY");
                        }
                        getscheme.close();

                    } else {

                        Log.e("SCHEME", "FREE UNITS NOT APPLICABLE.REASON FCODE " + CUR_FAULT);
                        Log.e("SCHEME", "MIN UNIT " + MIN_UNIT_CONSUMER);

                    }

                    if ("".equals(CALERROR.trim())) {

                        DAYS = CALCULATEDAYS(NEWFROMDATE, NEWTODATE);

                        ContentValues UNITSPMONTHRES;

                        Log.e("CATEDB_FORMAT(DATE2)", CATEGORY + "" + DB_FORMAT(DATE2));
                        Cursor cursor1 = db.rawQuery("SELECT DISTINCT(CHANGE_DATE),MIN_UNIT,MIN_CHARGES FROM category WHERE CATEGORY=? AND CHANGE_DATE <= ? ORDER BY CHANGE_DATE DESC", new String[]{CATEGORY, DB_FORMAT(NEWTODATE)});
                        if (cursor1.getCount() > 0) {
                            Date TEMP_FROMDATE = NEWFROMDATE;

                            Date TEMP_TODATE = NEWTODATE;

                            while (cursor1.moveToNext()) {
                                CAT_CHANGE_DATE = cursor1.getString(cursor1.getColumnIndex("CHANGE_DATE"));
                                MIN_UNIT_CATEGORY = cursor1.getDouble(cursor1.getColumnIndex("MIN_UNIT"));
                                MIN_CHARGES = cursor1.getDouble(cursor1.getColumnIndex("MIN_CHARGES"));

                                UNITSPMONTHRES = UNITSPERMONTH(CUR_READING, PREV_READING, DATE1, DATE2, ADDUNITS, EXUNITS, MIN_UNIT_CONSUMER, AVG_CONS, CUR_FAULT, MIN_UNIT_CATEGORY, CATEGORY, FLATS, PREV_CUM, REPLACEMENT_DATE, METER_STATUS, PREVMETERSTAT, MF_FACTOR);

                                USED1 = (double) UNITSPMONTHRES.get("USED1");

                                double MINUNITSCAL = (double) UNITSPMONTHRES.get("MIN_UNIT");

                                BASE = UNITSPMONTHRES.get("BASE").toString();

                                UNITS_USED_DBS = (double) UNITSPMONTHRES.get("UNITS_USED_DBS");

                                Log.e("UNITSPMONTHRES USED1", USED1 + " MINUNITSCAL" + MINUNITSCAL);

                                if (iterator == 0) {
                                    BASE_DB = BASE; //IN CASE OF CHANGE RATE,LATEST VALUE TO IN DB

                                    MIN_UNITS_CONSUMER_DB = MINUNITSCAL; //IN CASE OF CHANGE RATE,LATEST VALUE TO SAVE IN DB

                                    MIN_CHARGES_DB = MIN_CHARGES;
                                }

                                iterator++;

                                Date CHANGE_DATE = DATEFROMSTRING(CAT_CHANGE_DATE);

                                Log.e("DATES", TEMP_FROMDATE.toString() + " " + TEMP_TODATE.toString() + " " + CHANGE_DATE.toString());

                                if (TEMP_TODATE.compareTo(CHANGE_DATE) >= 0 && TEMP_FROMDATE.compareTo(CHANGE_DATE) >= 0)//condition 1
                                {

                                    Log.e("MAIN FUNC", "1");

                                    WAT_SEW_FUN = CALCULATE_WATER_CHARGES(CONSUMERID, ISSDATE, TEMP_FROMDATE, TEMP_TODATE, CATEGORY, USED1, FLATS, SEWARAGE_CON, USERNAME, IMEI);

                                    if ("".equals(WAT_SEW_FUN.get("ERROR"))) {
                                        WATER_CHARGES = WATER_CHARGES + (double) WAT_SEW_FUN.get("WATER_CHARGES");
                                        SEWARAGE_CHARGES = SEWARAGE_CHARGES + (double) WAT_SEW_FUN.get("SEW_CHARGES");

                                        TEMPDAYS = CALCULATEDAYS(TEMP_FROMDATE, TEMP_TODATE);

                                        Log.e("TOTAL_USED", TOTAL_USED + " " + USED1 + " " + TEMPDAYS);

                                        TOTAL_USED += (USED1 / 30) * TEMPDAYS;

                                        Log.e("WAT_SEW_FUN 1", WATER_CHARGES + " " + SEWARAGE_CHARGES + " " + TEMPDAYS + " " + TOTAL_USED);

                                    } else {
                                        CALERROR += WAT_SEW_FUN.get("ERROR");
                                    }
                                    break;

                                } else if (TEMP_TODATE.compareTo(CHANGE_DATE) >= 0 && TEMP_FROMDATE.compareTo(CHANGE_DATE) <= 0)//condition 2
                                {
                                    Log.e("MAIN FUNC", "2");
                                    WAT_SEW_FUN = CALCULATE_WATER_CHARGES(CONSUMERID, ISSDATE, CHANGE_DATE, TEMP_TODATE, CATEGORY, USED1, FLATS, SEWARAGE_CON, USERNAME, IMEI);

                                    if ("".equals(WAT_SEW_FUN.get("ERROR"))) {
                                        WATER_CHARGES = WATER_CHARGES + (double) WAT_SEW_FUN.get("WATER_CHARGES");
                                        SEWARAGE_CHARGES = SEWARAGE_CHARGES + (double) WAT_SEW_FUN.get("SEW_CHARGES");

                                        TEMPDAYS = CALCULATEDAYS(CHANGE_DATE, TEMP_TODATE);

                                        TOTAL_USED += (USED1 / 30) * TEMPDAYS;

                                        TEMP_TODATE = MINUSEDAYS(CHANGE_DATE, 1);

                                        Log.e("WAT_SEW_FUN 2", WATER_CHARGES + " " + SEWARAGE_CHARGES + " " + TEMPDAYS + " " + TOTAL_USED + " " + TEMP_TODATE.toString());

                                    } else {
                                        CALERROR += WAT_SEW_FUN.get("ERROR");
                                        break;
                                    }

                                } else if (CHANGE_DATE.toString().isEmpty()) {
                                    CALERROR += "CHANGE DATE IS EMPTY";
                                }
                            }

                            TOTAL_USED1 = (TOTAL_USED / DAYS) * 30;

                            Log.e("UNITSPS TOTAL_USED1", TOTAL_USED1 + " TOTAL_USED" + TOTAL_USED);

                            WATER_CHARGES = rounddecimal(WATER_CHARGES * FLATS, 0);

                            MIN_CHARGES_DB = ((MIN_CHARGES_DB / 30) * DAYS) * FLATS;

                            if (WATER_CHARGES < MIN_CHARGES_DB) {
                                WATER_CHARGES = rounddecimal(MIN_CHARGES_DB, 0);
                            }

                            if (TAP_FACTOR > 1) {
                                WATER_CHARGES = WATER_CHARGES * TAP_FACTOR;

                                CONSUMER_W_TAP_FACTOR += "CONSUMER WITH TAP FACTOR :- " + CONSUMERID + " - " + CODY + " - " + TAP_FACTOR + " - " + WATER_CHARGES + " || ";
                            }

                            SEWARAGE_CHARGES = rounddecimal(SEWARAGE_CHARGES * FLATS, 0);

                            double METER_RENT = 0;

                            if ("00".equals(METCODE)) {
                                METER_RENT = 0;
                            } else {

                                METER_RENT_RESPONSE = CALCULATE_METER_RENT(NEWFROMDATE, NEWTODATE, METCODE);

                                if ("".equals(METER_RENT_RESPONSE.get("ERROR"))) {
                                    METER_RENT = rounddecimal((double) METER_RENT_RESPONSE.get("METER_RENT"), 0);
                                } else {
                                    CALERROR += METER_RENT_RESPONSE.get("ERROR");
                                }
                            }

                            Double CUM_WC = 0.00;
                            Double CUM_SEW = 0.00;
                            Double CUM_MR = 0.00;

                            if (FREE_BILL == 1) {
                                CUM_WC = WATER_CHARGES;
                                CUM_SEW = SEWARAGE_CHARGES;
                                CUM_MR = METER_RENT;

                                WATER_CHARGES = 0;
                                METER_RENT = 0;
                                SEWARAGE_CHARGES = 0;
                            }

                            /*Double MRBALANCE = 0.00;
                            Double MRAMOUNT = 0.00;
                            Double SEAMOUNT = 0.00;
                            Double SEWBALANCE = 0.00;*/
                            ContentValues GETLED = CHECK_SEW_EA(PERDAY1,CONSUMERID);

                            Log.e("LED_SEW_EA",GETLED.get("SEW_EA").toString());
                            Log.e("LED_MR_EA",GETLED.get("MR_EA").toString());
                            Double LED_SEW_EA = Double.parseDouble(GETLED.get("SEW_EA").toString());
                            Double LED_MR_EA = Double.parseDouble(GETLED.get("MR_EA").toString());


                            ContentValues MR_EA = ADD_EXTRA_AMOUNT(CONSUMERID, "MR");
                            Double MRAMOUNT = Double.parseDouble(MR_EA.get("AMOUNT").toString());
                            Double MRBALANCE = Double.parseDouble(MR_EA.get("BALANCE").toString());

                            Log.e("ADD_EXTRA_AMOUNT",MR_EA.get("AMOUNT").toString());
                            Log.e("ADD_EXTRA_AMOUNT",MR_EA.get("BALANCE").toString());
                            if(MRAMOUNT!=0)
                            {
                                if(LED_MR_EA == 0 || LED_MR_EA == null  || "".equals(LED_MR_EA))
                                {
                                    METER_RENT = METER_RENT + MRAMOUNT;
                                    MRBALANCE = MRBALANCE - MRAMOUNT;
                                }else
                                {
                                    if(LED_MR_EA != MRAMOUNT)
                                    {
                                        METER_RENT = METER_RENT + MRAMOUNT;
                                        Double DIFFERENCE = MRAMOUNT - LED_MR_EA;
                                        MRBALANCE = MRBALANCE - DIFFERENCE;
                                    }else
                                    {
                                        MRBALANCE = 0.00;
                                    }
                                }
                            }else
                            {
                                MRAMOUNT = 0.00;
                                MRBALANCE = 0.00;
                            }


                            ContentValues SEW_EA = ADD_EXTRA_AMOUNT(CONSUMERID, "SAMT");
                            Double SEAMOUNT = Double.parseDouble(SEW_EA.get("AMOUNT").toString());
                            Double SEWBALANCE = Double.parseDouble(SEW_EA.get("BALANCE").toString());

                            if(SEAMOUNT!=0)
                            {
                                if(LED_SEW_EA == 0 || LED_SEW_EA == null  || "".equals(LED_SEW_EA))
                                {
                                    SEWARAGE_CHARGES = SEWARAGE_CHARGES + SEAMOUNT;

                                    SEWBALANCE = SEWBALANCE - SEAMOUNT;
                                }else
                                {
                                    if(LED_SEW_EA != SEAMOUNT)
                                    {
                                        SEWARAGE_CHARGES = SEWARAGE_CHARGES + SEAMOUNT;
                                        Double DIFFERENCE = SEAMOUNT - LED_SEW_EA;
                                        SEWBALANCE = SEWBALANCE - DIFFERENCE;
                                    }else
                                    {
                                        SEWBALANCE = 0.00;
                                    }
                                }
                            }else
                            {
                                SEAMOUNT = 0.00;
                                SEWBALANCE = 0.00;
                            }

                            if(SEWBALANCE<0)
                            {
                                CALERROR += "SEWERAGE CHARGES BALANCE GOING NEGETIVE";
                            }
                            if(MRBALANCE<0)
                            {
                                CALERROR += "METER RENT BALANCE GOING NEGETIVE";
                            }


                            ContentValues sdrentcv = null;
                            double sd_rent = 0;

                            sdrentcv = CALCULATE_SD_RENT(NEWFROMDATE, NEWTODATE, SDROOMS, CATEGORY);
                            if (!"NA".equals(sdrentcv.get("ERROR")))
                            {
                                if ("".equals(sdrentcv.get("ERROR"))) {
                                    sd_rent = rounddecimal((double) sdrentcv.get("SD_RENT"), 0);
                                } else {
                                    CALERROR += sdrentcv.get("ERROR");
                                }
                            }


                            LPCD_RES = CALCULATE_LPCD(TOTAL_USED1, FLATS, POPULA, CATEGORY);

                            if ("".equals(LPCD_RES.get("ERROR"))) {
                                LPCD = rounddecimal((double) LPCD_RES.get("LPCD"), 0);
                            } else {
                                CALERROR += LPCD_RES.get("ERROR");
                            }

                            USED = rounddecimal(TOTAL_USED * FLATS, 2);

                            if (MF_FACTOR > 1) {

                                ADD_UNITS_MF_FACTOR = rounddecimal(((USED / MF_FACTOR) * (MF_FACTOR - 1)), 0);

                                CONSUMER_W_MF_FACTOR += "CONSUMER WITH METER FACTOR :- " + CONSUMERID + " - " + CODY + " - " + MF_FACTOR + " || " + ADD_UNITS_MF_FACTOR;

                            }

                            PERDAY = rounddecimal(USED / DAYS, 3);

                            USED1 = rounddecimal(TOTAL_USED1 * FLATS, 2);

                            UNITS_USED_DBS = rounddecimal(UNITS_USED_DBS * FLATS, 2);

                            NEW_AVG = rounddecimal((AVG_CONS + USED1) / 2, 0);

                            if (PREVUSED1 > 0) {
                                RATIO = rounddecimal(USED1 / PREVUSED1, 3);
                            } else {
                                RATIO = 1;
                            }

                            if (RATIO >= 1.8) {
                                CHECK_RATIO = "CHECK(M)";
                            } else if (RATIO <= 0.5) {
                                CHECK_RATIO = "CHECK(L)";
                            }

                            if (!"".equals(CUR_FAULT) && !"TD".equals(CUR_FAULT.trim()) && !"NC".equals(CUR_FAULT.trim())) {
                                Log.e("cum check CFLT", CUR_FAULT);
                                if (REPLACEMENT_DATE != null) {
                                    Log.e("REPLACEMENT_DATE", String.valueOf(REPLACEMENT_DATE));
                                    CURRENT_CUM = rounddecimal(USED, 0);
                                } else {
                                    CURRENT_CUM = rounddecimal(USED + PREV_CUM, 0);
                                }
                            } else {
                                CURRENT_CUM = 0;
                            }

                            ADDCHARGE=ADDCHARGE+sd_rent;

                            AMOUNT = WATER_CHARGES + METER_RENT + SEWARAGE_CHARGES - SUNDRY + ADDCHARGE + NEXTARR;

                            int CALCULATE_INSPECTION_CHARGES = 0;
                            ContentValues INSCH = null;
                            double ins_charges = 0;
                            if (CALCULATE_INSPECTION_CHARGES == 1) {

                                INSCH = CALCULATE_INS_CHARGES(CATEGORY, CONSUMERID, DATE2, PERDAY1, USERNAME, IMEI);

                                if (!"".equals(INSCH.get("ERROR"))) {
                                    CALERROR += INSCH.get("ERROR").toString().trim();
                                } else {
                                    ins_charges = (double) INSCH.get("INS_CHARGES");
                                    AMOUNT = AMOUNT + ins_charges;

                                    //AMOUNT = AMOUNT + (double) INSCH.get("INS_CHGS_SEW");NOT APPLICABLE AS PER DEPT
                                }
                            } else {
                                Log.e("CAL_INS_CHGS", String.valueOf(CALCULATE_INSPECTION_CHARGES));
                            }

                            NET_PAYABLE = AMOUNT - NEXTDC;

                            ARREARS_CREDIT = NEXTARR - NEXTDC;

                            String ASSISTMSGDISC ="";
                            if (PREVARR == 0 && NEXTARR > 0) {
                                ARR_TAG = 1;
                                ASSISTMSGDISC = "CONSUMER HAS ARREARS ON LAST 1 BILL";
                            } else if (PREVARR > 0 && NEXTARR > 0) {
                                ARR_TAG = 2;
                                ASSISTMSGDISC = "CONSUMER HAS ARREARS ON LAST 2 BILL";
                            }

                            String DUEDATE_PAYMENT = "";
                            Date con_lastdate = null;

                            con_lastdate = check_holiday(ADDDAYS(DATE2, 13));
                            if (con_lastdate == null) {
                                CALERROR += "INVALID LAST DATE";
                                DUEDATE_PAYMENT = "";
                            } else {
                                DUEDATE_PAYMENT = DB_FORMAT(con_lastdate);
                                if (DATE2.compareTo(con_lastdate) > 0) {
                                    CALERROR += "ISSUE DATE CANNOT BE GREATER THAN LAST DATE";
                                }
                            }
                            if (REPLACEMENT_DATE != null) {
                                savedata.put("REDATE", DB_FORMAT(REPLACEMENT_DATE));
                            }
                            savedata.put("SEWBALANCE", SEWBALANCE);
                            savedata.put("MRBALANCE", MRBALANCE);
                            savedata.put("SEW_EA", SEAMOUNT);
                            savedata.put("MR_EA", MRAMOUNT);


                            savedata.put("CUM_WC", CUM_WC);
                            savedata.put("CUM_SEW", CUM_SEW);
                            savedata.put("CUM_MR", CUM_MR);

                            savedata.put("DISCON_MSG", ASSISTMSGDISC);
                            savedata.put("sd_rent_calculated", sd_rent);
                            savedata.put("INSCHGS", ins_charges);
                            savedata.put("ACTUAL_OTS_AMOUNT", ACTUAL_OTS_AMOUNT);
                            savedata.put("ADD1", ADD1);
                            savedata.put("ADD2", ADD2);
                            savedata.put("ADD3", ADD3);
                            savedata.put("ADDCHARGE", ADDCHARGE);
                            savedata.put("ADDUNIT", ADDUNITS);
                            savedata.put("ARRDC", ARREARS_CREDIT);
                            savedata.put("ARREARS", NEXTARR);
                            savedata.put("ARRTAG", ARR_TAG);
                            savedata.put("AVG_CONS", AVG_CONS);
                            savedata.put("BASE", BASE_DB);
                            savedata.put("BILLAMT", NET_PAYABLE);
                            savedata.put("BILLED_AS_CATEGORY", CATEGORY);
                            savedata.put("CATEGORY", ACTCATEGORY);
                            savedata.put("CATEGORY_GROUP", CATEGORY_GROUP);
                            savedata.put("CHECK_", CHECK_RATIO);
                            savedata.put("CNET", NET_PAYABLE);
                            savedata.put("CODY", CODY);
                            savedata.put("CONSUMERID", CONSUMERID);
                            savedata.put("CUM", CURRENT_CUM);
                            savedata.put("CUR_READ", CUR_READING);
                            savedata.put("DATE1", DB_FORMAT(DATE1));
                            savedata.put("DATE2", DB_FORMAT(DATE2));
                            savedata.put("DAYS", ACTUALDAYS);
                            savedata.put("DC", NEXTDC);
                            savedata.put("DIV_NM", DIV_NM);
                            savedata.put("DMA_NO", DMA_NO);
                            savedata.put("entered_dt_time", DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                            savedata.put("EXT", EXT);
                            savedata.put("EXUNIT", EXUNITS);
                            savedata.put("FCODE", CUR_FAULT);
                            savedata.put("FCOUNT", FCOUNT);
                            savedata.put("generated_at", DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                            savedata.put("generated_by", USERNAME);
                            savedata.put("INSTALL_DT", INSTALL_DT);
                            savedata.put("ISSDATE", DB_FORMAT(ISSDATE));
                            savedata.put("last_edit_at", DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
                            savedata.put("last_edit_by", USERNAME);
                            savedata.put("LASTDATE", DB_FORMAT(LASTDATE));
                            savedata.put("LPCD", LPCD);
                            savedata.put("MET_STATDT", MET_STATDT);
                            savedata.put("METCODE", METCODE);
                            savedata.put("METER_MAKE", METER_MAKE);
                            savedata.put("METER_NO", METER_NO);
                            savedata.put("METER_READ", PREV_READING);
                            savedata.put("METERSTAT", METER_STATUS);
                            savedata.put("METRATE", METER_RENT);
                            savedata.put("MF_FACTOR", MF_FACTOR);
                            savedata.put("MF_FACTOR_ADD_UNITS", ADD_UNITS_MF_FACTOR);
                            savedata.put("MIN_UNIT", MIN_UNITS_CONSUMER_DB);
                            savedata.put("MIN_UNIT_MAST", MIN_UNIT_CONSUMER);
                            savedata.put("MR_NAME", USERNAME);
                            savedata.put("NAME", NAME);
                            savedata.put("NET", NET_PAYABLE);
                            savedata.put("NETAMT", NET_PAYABLE);
                            savedata.put("NEWAVG", NEW_AVG);
                            savedata.put("NOFLAT", ACTUAL_FLAT);
                            savedata.put("OTS_AMOUNT", OTSAMOUNT);
                            savedata.put("PERDAY", PERDAY);
                            savedata.put("PERDAY1", DB_FORMAT(PERDAY1));
                            savedata.put("PERDAY2", DB_FORMAT(PERDAY2));
                            savedata.put("PINCODE", PINCODE);
                            savedata.put("POPULA", POPULA);
                            savedata.put("POS_CONV", 2);
                            savedata.put("POS_SERIAL_NO", IMEI);
                            savedata.put("POS_UP_BIT", 1);
                            savedata.put("POS_USER", USERNAME);
                            savedata.put("RATIO", RATIO);
                            savedata.put("REMARK", REMARKS);
                            savedata.put("REV_SUBDIV", REV_SUBDIV);
                            savedata.put("SAMT", SEWARAGE_CHARGES);
                            savedata.put("SEC_DEPOSI", SEC_DEPOSI);
                            savedata.put("SEWARAGE", SEWARAGE_CON);
                            savedata.put("SEWARAGE_DATE", SEWARAGE_DATE);
                            savedata.put("SRLNO", SRLNO);
                            savedata.put("SUBDIV", SUBDIV);
                            savedata.put("SUNDRY", SUNDRY);
                            savedata.put("TAP_FACTOR", TAP_FACTOR);
                            savedata.put("TITLE", TITLE);
                            savedata.put("TOTAL", AMOUNT);
                            savedata.put("UNITS_USED", UNITS_USED_DBS);
                            savedata.put("USED", USED);
                            savedata.put("USED1", USED1);
                            savedata.put("VILL_CODE", VILL_CODE);
                            savedata.put("WARD", WARD);
                            savedata.put("WC", WATER_CHARGES);
                            savedata.put("ZONE", ZONE);
                            savedata.put("MAX_METER_UNITS", MAXSIZE);
                            savedata.put("FINALREAD", FINAL_READ);
                            savedata.put("TEMP_ISSDATE", DB_FORMAT(DATE2));
                            savedata.put("TEMP_LASTDATE", DUEDATE_PAYMENT);
                            savedata.put("FREE_UNITS", FREE_SCHEMEUNITS);
                            savedata.put("FREE_DAYS", FREE_DAYS);
                            savedata.put("SCHEME_STATUS", FREE_BILL);
                            savedata.put("PREV_CUM", PREV_CUM);
                            //savedata.put("MOBILE_NO", ENT_MOBILE_NO);
                            //savedata.put("EMAIL_ID", ENT_EMAIL_ID);
                            PAYMENTSTATUS = GETPAYMENTSTATUS(CONSUMERID);

                            savedata.put("PAYMENTSTATUS", PAYMENTSTATUS);
                        } else {
                            CALERROR += "NO RECORDS FOUND IN CATEGORY TABLE FOR CATEGORY " + CATEGORY + " CONSUMERID " + CONSUMERID;
                        }
                        cursor1.close();
                    }
                }
            }
        } else {
            CALERROR += "NO DETAILS FOUND IN LEDGER TABLE FOR THIS CONSUMERID";
        }
        cursor.close();
        savedata.put("ERROR", CALERROR);

        return savedata;
    }

    public Date check_holiday(Date lastdate) {
        Date duedate = lastdate;
        Log.e("DAY", DateFormat.format("EEEE", duedate).toString());
        int count = 0;
        do {
            duedate = ADDDAYS(duedate, 1);
            Log.e("duedate", duedate.toString() + " count" + count + " ");
        } while (holidaycount(duedate) > 0 || "Sunday".equals(DateFormat.format("EEEE", duedate)) || "Saturday".equals(DateFormat.format("EEEE", duedate)));


        Log.e("lastdate", duedate.toString());
        return duedate;
    }

    public int holidaycount(Date duedate) {
        SQLiteDatabase db = getWritableDatabase();
        int count = 0;
        Cursor getled = null;

        getled = db.rawQuery("Select * FROM holiday_master WHERE holiday_date= ? AND status=? ", new String[]{DB_FORMAT(duedate), "1"});
        count = getled.getCount();
        getled.close();

        return count;
    }

    public ContentValues FREE_16UNITS_SCHEME(String CUR_FAULT, Date FROMDATE, Date TODATE, long CURRENT, long PREVIOUS, double FREE_UNITS, double CUMULATIVES, double EXUNITS, double ADDUNITS, Date REPLACEMENT_DATE, String CATEGORY, double FLATS) {
        ContentValues SCHCONT = new ContentValues();
        Date DATETODAY = new Date(System.currentTimeMillis());

        double UPM = 0;
        int SCH_STATUS = 0;

        if (REPLACEMENT_DATE != null) {
            CUMULATIVES = 0;
        }

        int DAYS = CALCULATEDAYS(FROMDATE, TODATE);

        double READING_DIFFERENCE = CURRENT - PREVIOUS;
        Log.e("READING_DIFFERENCE", "before " + READING_DIFFERENCE);
        if ("SO".equals(CATEGORY) || "SU".equals(CATEGORY) || "MF".equals(CATEGORY)) {
            READING_DIFFERENCE = READING_DIFFERENCE / FLATS;
            EXUNITS = EXUNITS / FLATS;
            ADDUNITS = ADDUNITS / FLATS;
            CUMULATIVES = CUMULATIVES / FLATS;
            Log.e("multi flats", "READING_DIFFERENCE " + READING_DIFFERENCE + " " + EXUNITS + " " + ADDUNITS + CUMULATIVES + " FLATS" + FLATS);
        }

        Log.e("READING_DIFFERENCE", "READING_DIFFERENCE " + CURRENT + " " + PREVIOUS + " " + READING_DIFFERENCE);
        if (READING_DIFFERENCE > 0 || "NC".equals(CUR_FAULT.trim())) {
            double USED_UNITS = READING_DIFFERENCE + EXUNITS + ADDUNITS - CUMULATIVES;
            Log.e("CUMULATIVES ", CUMULATIVES + " CUMULATIVES");
            Log.e("FREE 16", "READING_DIFFERENCE " + READING_DIFFERENCE + " reading" + DAYS + FREE_UNITS);
            Log.e("CAL", "USED_UNITS " + USED_UNITS + " EXUNITS" + EXUNITS + " ADDUNITS" + ADDUNITS);

            UPM = (USED_UNITS / DAYS) * 30;

            Log.e("UPM ", UPM + " UPM");

            if (UPM <= FREE_UNITS) {
                SCH_STATUS = 1;
            } else {
                SCH_STATUS = -1;
            }
        } else {
            SCH_STATUS = -1;
        }

        Log.e("UPM SCH_STATUS", String.valueOf(SCH_STATUS) + " SCH_STATUS");

        SCHCONT.put("UPM", UPM);
        SCHCONT.put("SCHEME_STATUS", SCH_STATUS);

        return SCHCONT;
    }


    public ContentValues PROCESS_ARREARS_development(String ZONE, String CONSUMERID, Date L_ISSDATE, Date L_LASTDATE, Date NEW_ISSDATE, Date PERDAY1, Date C_ARRTODATE, String USERNAME, String IMEI) {
        String error = "";
        SQLiteDatabase db = getWritableDatabase();
        ContentValues ARRPROC = new ContentValues();
        ContentValues dcledger = new ContentValues();
        ContentValues ledgerupdate = new ContentValues();
        int ARR_PROC = 0;
        try {

            Cursor getled = db.rawQuery("Select ledgerdata.*,ledgerdata.ID as LEDGER_ID FROM ledgerdata left join masterdetails ON ledgerdata.CONSUMERID=masterdetails.CONSUMERID WHERE masterdetails.ZONE= ? AND ledgerdata.ISSDATE=? AND masterdetails.CONSUMERID=? ", new String[]{ZONE, DB_FORMAT(L_ISSDATE), CONSUMERID});
            if (getled.getCount() > 0) {
                while (getled.moveToNext()) {
                    String LEDGER_ID = getled.getString(getled.getColumnIndex("LEDGER_ID"));
                    String BILLNO = getled.getString(getled.getColumnIndex("BILLNO"));
                    String LASTISSDATE = getled.getString(getled.getColumnIndex("ISSDATE"));
                    String LASTLASTDATEdb = getled.getString(getled.getColumnIndex("LASTDATE"));
                    String PERDAY1db = getled.getString(getled.getColumnIndex("PERDAY1"));
                    double NET = getled.getDouble(getled.getColumnIndex("NET"));
                    String TEMP_ISSDATE = getled.getString(getled.getColumnIndex("TEMP_ISSDATE"));
                    String TEMP_LASTDATE = getled.getString(getled.getColumnIndex("TEMP_LASTDATE"));
                    String ACTUAL_OTS_AMOUNT = getled.getString(getled.getColumnIndex("ACTUAL_OTS_AMOUNT"));
                    String OTS_AMOUNT = getled.getString(getled.getColumnIndex("OTS_AMOUNT"));

                    Date ADATE1 = ADDDAYS(L_LASTDATE, 1);
                    Date ADATE2 = NEW_ISSDATE;
                    Log.e("OTS CALC", ACTUAL_OTS_AMOUNT + " " + OTS_AMOUNT);
                    Log.e("LEDGER_ID", LEDGER_ID + " " + BILLNO + " " + NET);
                    if (BILLNO == null || "".equals(BILLNO)) {
                        BILLNO = "";
                    }

                    if (TEMP_ISSDATE != null) {
                        L_ISSDATE = DATEFROMSTRING(TEMP_ISSDATE.trim());
                    }

                    if (TEMP_LASTDATE != null) {
                        L_LASTDATE = DATEFROMSTRING(TEMP_LASTDATE.trim());
                    }

                    Log.e("L_LASTDATE", L_ISSDATE + " " + L_LASTDATE);

                    int delrows = db.delete("deley_payment_details", "LEDGER_ID = ?", new String[]{LEDGER_ID});
                    if (delrows >= 0) {
                        Log.e("deley_payment_", delrows + " entries deleted");

                    }

                    if (!"".equals(ACTUAL_OTS_AMOUNT) && ACTUAL_OTS_AMOUNT != null) {
                        Log.e("OTS CALC", "ACTUAL_OTS_AMOUNT EXISTS :- " + ACTUAL_OTS_AMOUNT);
                        if (Double.parseDouble(ACTUAL_OTS_AMOUNT) == Double.parseDouble(OTS_AMOUNT)) {
                            Log.e("OTS CALC", "ACTUAL_OTS_AMOUNT EQUALS  OTS_AMOUNT:- " + ACTUAL_OTS_AMOUNT + " " + OTS_AMOUNT);
                            if ("".equals(OTS_AMOUNT.trim())) {
                                Log.e("OTS CALC", "OTS_AMOUNT is empty:-  " + OTS_AMOUNT + " OTS AMOUNT MADE ZERO");
                                OTS_AMOUNT = "0";

                            }
                        } else {
                            Log.e("OTS CALC", "ACTUAL_OTS_AMOUNT NOT EQUAL TO  OTS_AMOUNT" + ACTUAL_OTS_AMOUNT + " " + OTS_AMOUNT);

                            Log.e("OTS CALC", "Select ledgerdata.OTS_AMOUNT FROM ledgerdata WHERE ledgerdata.CONSUMERID= ? AND ledgerdata.PERDAY1 < ? ORDER BY ledgerdata.PERDAY1 DESC LIMIT 1" + CONSUMERID + " " + DB_FORMAT(PERDAY1));

                            Cursor getlastots = db.rawQuery("Select ledgerdata.OTS_AMOUNT FROM ledgerdata WHERE ledgerdata.CONSUMERID= ? AND ledgerdata.PERDAY1 < ? ORDER BY ledgerdata.PERDAY1 DESC LIMIT 1", new String[]{CONSUMERID, PERDAY1db});
                            if (getlastots.getCount() > 0) {
                                Log.e("OTS CAL", "GETTING OTS_AMOUNT FROM LAST CYCLE. ENTRY EXISTS");
                                while (getlastots.moveToNext()) {
                                    String P_OTS = getlastots.getString(getlastots.getColumnIndex("OTS_AMOUNT"));
                                    Log.e("OTS CAL", "LAST CYCLE OTS AMOUNT :- " + P_OTS);
                                    if ("".equals(P_OTS) || P_OTS == null) {
                                        OTS_AMOUNT = ACTUAL_OTS_AMOUNT;
                                        Log.e("OTS CAL", "LAST CYCLE OTS AMOUNT EMPTY." + OTS_AMOUNT);
                                    } else {
                                        OTS_AMOUNT = P_OTS;
                                        Log.e("OTS CAL", "LAST CYCLE OTS AMOUNT EXISTS." + OTS_AMOUNT);
                                    }
                                }
                            } else {
                                OTS_AMOUNT = ACTUAL_OTS_AMOUNT;
                                Log.e("OTS CAL", "LAST CYCLE NO RECORDS FOR OTS." + OTS_AMOUNT);
                            }
                            getlastots.close();
                        }
                    } else {
                        OTS_AMOUNT = "0";
                        Log.e("OTS CAL", "ACTUAL OTS AMOUNT IS EMPTY." + OTS_AMOUNT);
                    }
                    double OTSAMT = 0;
                    OTSAMT = Double.parseDouble(OTS_AMOUNT);

                    double monthdp = 0;
                    double NEXTAREEARS = 0;
                    double NEXTCREDIT = 0;
                    double ARRDC = 0;
                    double DP = 0;
                    double PENDING_PAYMENT = 0;
                    String dpval = "";

                    Date ARREARS_TO_DATE = C_ARRTODATE;
                    Date CHECKDATE1 = L_ISSDATE;
                    Date CHECKDATE2 = L_LASTDATE;

                    Date AADATE1 = ADDDAYS(L_LASTDATE, 1);
                    Date AADATE2 = C_ARRTODATE;

                    Date currentdttm = new Date(System.currentTimeMillis());
                    PENDING_PAYMENT = NET;

                    do {
                        Log.e("query", "SELECT COALESCE(sum(RAMT), 0) as RAMTSUM from newrevenue where RDATE >= " + DB_FORMAT(CHECKDATE1) + " AND RDATE <= " + DB_FORMAT(CHECKDATE2) + " AND BILLNO=" + BILLNO + " AND BILLNO IS NOT NULL AND BILLNO <> '' AND CONSUMERID=" + CONSUMERID + " AND ISSDATE=" + DB_FORMAT(L_ISSDATE));
                        Cursor getnewrev = db.rawQuery("SELECT COALESCE(sum(RAMT), 0) as RAMTSUM from newrevenue where RDATE >= ? AND RDATE <= ? AND BILLNO=? AND BILLNO IS NOT NULL AND BILLNO <> '' AND CONSUMERID=? AND ISSDATE=? ", new String[]{DB_FORMAT(CHECKDATE1), DB_FORMAT(CHECKDATE2), BILLNO, CONSUMERID, DB_FORMAT(L_ISSDATE)});
                        double RAMTSUM = 0;
                        double TEMP = 0;
                        if (getnewrev.getCount() > 0) {
                            while (getnewrev.moveToNext()) {
                                RAMTSUM = getnewrev.getDouble(getnewrev.getColumnIndex("RAMTSUM"));
                                Log.e("RAMTSUM1", String.valueOf(RAMTSUM));
                            }
                        } else {
                            RAMTSUM = 0;
                        }
                        getnewrev.close();

                        Log.e("RAMTSUM2", String.valueOf(RAMTSUM));
                        if (CHECKDATE1.after(L_LASTDATE)) {
                            if (PENDING_PAYMENT > 0 && (CHECKDATE2.before(ARREARS_TO_DATE) || CHECKDATE2.equals(ARREARS_TO_DATE))) {

                                if (OTSAMT > 0) {
                                    TEMP = PENDING_PAYMENT - OTSAMT;
                                    Log.e("TEMP amt", TEMP + " PENDING_PAYMENT" + PENDING_PAYMENT + " " + OTSAMT);
                                } else {
                                    TEMP = PENDING_PAYMENT;
                                    Log.e("TEMP amt", TEMP + " tempamt" + PENDING_PAYMENT);
                                }

                                monthdp = TEMP * 2 / 100;
                                Log.e("monthdp", monthdp + " ");
                                monthdp = rounddecimal(monthdp, 2);
                                Log.e("monthdp", monthdp + " ");
                                DP = DP + monthdp;
                                Log.e("DP", DP + " ");
                                PENDING_PAYMENT = PENDING_PAYMENT + monthdp;
                                Log.e("PENDING_PAYMENT", PENDING_PAYMENT + " ");
                            }
                        }
                        PENDING_PAYMENT = PENDING_PAYMENT - RAMTSUM;
                        Log.e("PENDING_PAYMENT afer", PENDING_PAYMENT + " ");
                        if (OTSAMT > 0) {
                            Log.e("OTS CAL", "PAID AMOUNT TO BE DEDUCTED FROM OTS" + RAMTSUM + " " + OTSAMT);
                            OTSAMT = OTSAMT - RAMTSUM;
                            Log.e("OTSAMT", "after" + OTSAMT);
                            if (OTSAMT < 0) {
                                OTSAMT = 0;
                                ACTUAL_OTS_AMOUNT = "0";
                                Log.e("OTS CAL", OTSAMT + " " + ACTUAL_OTS_AMOUNT);
                            }
                        }

                        if (monthdp > 0) {
                            dpval = "1";

                            dcledger.put("LEDGER_ID", LEDGER_ID);
                            dcledger.put("BILLNO", BILLNO);
                            dcledger.put("CONSUMERID", CONSUMERID);
                            dcledger.put("ISSDATE", DB_FORMAT(L_ISSDATE));
                            dcledger.put("From_Date", DB_FORMAT(CHECKDATE1));
                            dcledger.put("To_Date", DB_FORMAT(CHECKDATE2));
                            dcledger.put("DP_amt", monthdp);
                            dcledger.put("user", USERNAME);
                            dcledger.put("serial_no", IMEI);
                            dcledger.put("datetime", DB_DATETIME_FORMAT(currentdttm));

                            long insres = db.insertOrThrow("deley_payment_details", null, dcledger);
                            if (insres == -1) {
                                error += "FAILED TO SAVE DELAY PAYMENT LEDGER";
                            }
                        }

                        CHECKDATE1 = ADDDAYS(CHECKDATE2, 1);
                        CHECKDATE2 = ADDDAYS(CHECKDATE2, 30);

                        if (CHECKDATE2.after(ARREARS_TO_DATE)) {
                            CHECKDATE2 = ARREARS_TO_DATE;
                        }
                    } while (CHECKDATE1.before(ARREARS_TO_DATE) || CHECKDATE1.equals(ARREARS_TO_DATE));


                    DP = rounddecimal(DP, 0);
                    Log.e("DP", String.valueOf(DP));

                    if (PENDING_PAYMENT > 0) {
                        NEXTAREEARS = rounddecimal(PENDING_PAYMENT, 0);
                    } else {
                        NEXTCREDIT = rounddecimal(PENDING_PAYMENT, 0);
                        NEXTCREDIT = 0 - NEXTCREDIT;

                    }
                    ARRDC = NEXTAREEARS + NEXTCREDIT;
                    if ("".equals(ACTUAL_OTS_AMOUNT)) {
                        OTSAMT = 0;
                    }

                    if ("".equals(error.trim())) {

                        ledgerupdate.put("ADATE1", DB_FORMAT(ADATE1));
                        ledgerupdate.put("ADATE2", DB_FORMAT(ADATE2));
                        ledgerupdate.put("NEXTARR", NEXTAREEARS);
                        ledgerupdate.put("NEXTDC", NEXTCREDIT);
                        ledgerupdate.put("DPC", DP);
                        ledgerupdate.put("arres_process", "1");
                        ledgerupdate.put("OTS_AMOUNT", OTSAMT);
                        ledgerupdate.put("ACTUAL_OTS_AMOUNT", ACTUAL_OTS_AMOUNT);
                        ledgerupdate.put("TEMP_ADATE1", DB_FORMAT(AADATE1));
                        ledgerupdate.put("TEMP_ADATE2", DB_FORMAT(AADATE2));
                        ledgerupdate.put("last_edit_by", USERNAME);
                        ledgerupdate.put("last_edit_at", DB_DATETIME_FORMAT(currentdttm));
                        ledgerupdate.put("POS_USER", USERNAME);
                        ledgerupdate.put("POS_SERIAL_NO", IMEI);
                        ledgerupdate.put("POS_UP_BIT", 1);

                        /*
                        int ledupdatedrows = db.update("ledgerdata", ledgerupdate, "CONSUMERID = ? AND PERDAY1=?", new String[]{CONSUMERID,PERDAY1db});
                        Log.e("PROC ARR res", String.valueOf(ledupdatedrows));
                        if (ledupdatedrows > 0)
                        {
                            ARR_PROC = ledupdatedrows;
                            Log.e("PROC ARR res", String.valueOf(ledupdatedrows));
                        } else
                        {
                            error += "FAILED TO PROCESS ARREARS.NO RECORDS UPDATED.";
                            Log.e("PROC ARR", error);

                        }
                        */
                    } else {
                        error += "FAILED TO PROCESS ARREARS." + error;
                        Log.e("PROC ARR", error);

                    }
                }
            } else {
                error += "FAILED TO PROCESS ARREARS.NO RECORDS FOUND FOR PROCESSING ARREARS FOR CONSUMER " + CONSUMERID;
                Log.e("PROC ARR", error);

            }
            getled.close();
        } catch (SQLException e) {
            error += " CONSUMERID " + CONSUMERID + " ," + e.getMessage();
        }
        ARRPROC.put("ERROR", error);
        ARRPROC.put("RESULT", ARR_PROC);
        ARRPROC.putAll(ledgerupdate);

        return ARRPROC;
    }

    public ContentValues PROCESS_ARREARS(String ZONE, String CONSUMERID, Date L_ISSDATE, Date L_LASTDATE, Date NEW_ISSDATE, Date PERDAY1, Date C_ARRTODATE, String USERNAME, String IMEI) {
        String error = "";
        SQLiteDatabase db = getWritableDatabase();
        ContentValues ARRPROC = new ContentValues();
        ContentValues dcledger = new ContentValues();
        int ARR_PROC = 0;
        try {

            Cursor getled = db.rawQuery("Select ledgerdata.*,ledgerdata.ID as LEDGER_ID FROM ledgerdata left join masterdetails ON ledgerdata.CONSUMERID=masterdetails.CONSUMERID WHERE masterdetails.ZONE= ? AND ledgerdata.ISSDATE=? AND masterdetails.CONSUMERID=? ", new String[]{ZONE, DB_FORMAT(L_ISSDATE), CONSUMERID});
            if (getled.getCount() > 0) {
                while (getled.moveToNext()) {
                    String LEDGER_ID = getled.getString(getled.getColumnIndex("LEDGER_ID"));
                    String BILLNO = getled.getString(getled.getColumnIndex("BILLNO"));
                    String LASTISSDATE = getled.getString(getled.getColumnIndex("ISSDATE"));
                    String LASTLASTDATEdb = getled.getString(getled.getColumnIndex("LASTDATE"));
                    String PERDAY1db = getled.getString(getled.getColumnIndex("PERDAY1"));
                    double NET = getled.getDouble(getled.getColumnIndex("NET"));
                    String TEMP_ISSDATE = getled.getString(getled.getColumnIndex("TEMP_ISSDATE"));
                    String TEMP_LASTDATE = getled.getString(getled.getColumnIndex("TEMP_LASTDATE"));
                    String ACTUAL_OTS_AMOUNT = getled.getString(getled.getColumnIndex("ACTUAL_OTS_AMOUNT"));
                    String OTS_AMOUNT = getled.getString(getled.getColumnIndex("OTS_AMOUNT"));
                    int DPC_WAVEOFF = getled.getInt(getled.getColumnIndex("DPC_WAVEOFF"));

                    if ("".equals(OTS_AMOUNT) || OTS_AMOUNT == null) {
                        OTS_AMOUNT = "0";
                    }

                    Date ADATE1 = ADDDAYS(L_LASTDATE, 1);
                    Date ADATE2 = NEW_ISSDATE;
                    Log.e("OTS CALC", ACTUAL_OTS_AMOUNT + " " + OTS_AMOUNT);
                    Log.e("LEDGER_ID", LEDGER_ID + " " + BILLNO + " " + NET);
                    if (BILLNO == null || "".equals(BILLNO)) {
                        BILLNO = "";
                    }

                    if (TEMP_ISSDATE != null) {
                        L_ISSDATE = DATEFROMSTRING(TEMP_ISSDATE.trim());
                    }

                    if (TEMP_LASTDATE != null) {
                        L_LASTDATE = DATEFROMSTRING(TEMP_LASTDATE.trim());
                    }

                    Log.e("L_LASTDATE", L_ISSDATE + " " + L_LASTDATE);

                    int delrows = db.delete("deley_payment_details", "LEDGER_ID = ?", new String[]{LEDGER_ID});
                    if (delrows >= 0) {
                        Log.e("deley_payment_", delrows + " entries deleted");

                    }

                    if (!"".equals(ACTUAL_OTS_AMOUNT) && ACTUAL_OTS_AMOUNT != null) {
                        Log.e("OTS CALC", "ACTUAL_OTS_AMOUNT EXISTS :- " + ACTUAL_OTS_AMOUNT);
                        if (Double.parseDouble(ACTUAL_OTS_AMOUNT) == Double.parseDouble(OTS_AMOUNT)) {
                            Log.e("OTS CALC", "ACTUAL_OTS_AMOUNT EQUALS  OTS_AMOUNT:- " + ACTUAL_OTS_AMOUNT + " " + OTS_AMOUNT);
                            if ("".equals(OTS_AMOUNT.trim())) {
                                Log.e("OTS CALC", "OTS_AMOUNT is empty:-  " + OTS_AMOUNT + " OTS AMOUNT MADE ZERO");
                                OTS_AMOUNT = "0";

                            }
                        } else {
                            Log.e("OTS CALC", "ACTUAL_OTS_AMOUNT NOT EQUAL TO  OTS_AMOUNT" + ACTUAL_OTS_AMOUNT + " " + OTS_AMOUNT);

                            Log.e("OTS CALC", "Select ledgerdata.OTS_AMOUNT FROM ledgerdata WHERE ledgerdata.CONSUMERID= ? AND ledgerdata.PERDAY1 < ? ORDER BY ledgerdata.PERDAY1 DESC LIMIT 1" + CONSUMERID + " " + DB_FORMAT(PERDAY1));

                            Cursor getlastots = db.rawQuery("Select ledgerdata.OTS_AMOUNT FROM ledgerdata WHERE ledgerdata.CONSUMERID= ? AND ledgerdata.PERDAY1 < ? ORDER BY ledgerdata.PERDAY1 DESC LIMIT 1", new String[]{CONSUMERID, PERDAY1db});
                            if (getlastots.getCount() > 0) {
                                Log.e("OTS CAL", "GETTING OTS_AMOUNT FROM LAST CYCLE. ENTRY EXISTS");
                                while (getlastots.moveToNext()) {
                                    String P_OTS = getlastots.getString(getlastots.getColumnIndex("OTS_AMOUNT"));
                                    Log.e("OTS CAL", "LAST CYCLE OTS AMOUNT :- " + P_OTS);
                                    if ("".equals(P_OTS) || P_OTS == null) {
                                        OTS_AMOUNT = ACTUAL_OTS_AMOUNT;
                                        Log.e("OTS CAL", "LAST CYCLE OTS AMOUNT EMPTY." + OTS_AMOUNT);
                                    } else {
                                        OTS_AMOUNT = P_OTS;
                                        Log.e("OTS CAL", "LAST CYCLE OTS AMOUNT EXISTS." + OTS_AMOUNT);
                                    }
                                }
                            } else {
                                OTS_AMOUNT = ACTUAL_OTS_AMOUNT;
                                Log.e("OTS CAL", "LAST CYCLE NO RECORDS FOR OTS." + OTS_AMOUNT);
                            }
                            getlastots.close();
                        }
                    } else {
                        OTS_AMOUNT = "0";
                        Log.e("OTS CAL", "ACTUAL OTS AMOUNT IS EMPTY." + OTS_AMOUNT);
                    }
                    double OTSAMT = 0;
                    OTSAMT = Double.parseDouble(OTS_AMOUNT);

                    double monthdp = 0;
                    double NEXTAREEARS = 0;
                    double NEXTCREDIT = 0;
                    double ARRDC = 0;
                    double DP = 0;
                    double PENDING_PAYMENT = 0;
                    String dpval = "";

                    Date ARREARS_TO_DATE = C_ARRTODATE;
                    Date CHECKDATE1 = L_ISSDATE;
                    Date CHECKDATE2 = L_LASTDATE;

                    Date AADATE1 = ADDDAYS(L_LASTDATE, 1);
                    Date AADATE2 = C_ARRTODATE;

                    double DIRECT_MINUS_RAMTSUM = 0;
                    Cursor getdirect_payments = db.rawQuery("SELECT COALESCE(sum(RAMT), 0) as RAMTSUM from newrevenue where BILLNO=? AND BILLNO IS NOT NULL AND BILLNO<>'' AND (DEL_BIT='0' OR DEL_BIT IS NULL) AND CONSUMERID=? AND ISSDATE=? AND RAMT>0  AND CONSIDER_PAY_BIT='1'", new String[]{BILLNO,CONSUMERID, DB_FORMAT(L_ISSDATE)});
                    if (getdirect_payments.getCount() > 0)
                    {
                        while (getdirect_payments.moveToNext()) {
                            DIRECT_MINUS_RAMTSUM = getdirect_payments.getDouble(getdirect_payments.getColumnIndex("RAMTSUM"));
                            Log.e("RAMTSUM1", String.valueOf(DIRECT_MINUS_RAMTSUM));
                        }
                    } else {
                        DIRECT_MINUS_RAMTSUM = 0;
                    }

                    if(DIRECT_MINUS_RAMTSUM>0){
                        PENDING_PAYMENT = NET-DIRECT_MINUS_RAMTSUM;
                        if(OTSAMT>0){
                            OTSAMT=OTSAMT-DIRECT_MINUS_RAMTSUM;
                        }
                    }else{
                        PENDING_PAYMENT = NET;
                    }

                    Date currentdttm = new Date(System.currentTimeMillis());

                    do {
                        Log.e("query", "SELECT COALESCE(sum(RAMT), 0) as RAMTSUM from newrevenue where RDATE >= " + DB_FORMAT(CHECKDATE1) + " AND RDATE <= " + DB_FORMAT(CHECKDATE2) + " AND BILLNO=" + BILLNO + " AND BILLNO IS NOT NULL AND BILLNO <> '' AND CONSUMERID=" + CONSUMERID + " AND ISSDATE=" + DB_FORMAT(L_ISSDATE));
                        Cursor getnewrev = db.rawQuery("SELECT COALESCE(sum(RAMT), 0) as RAMTSUM from newrevenue where RDATE >= ? AND RDATE <= ? AND BILLNO=? AND BILLNO IS NOT NULL AND BILLNO <> '' AND CONSUMERID=? AND ISSDATE=? AND CONSIDER_PAY_BIT IS NULL AND (DEL_BIT='0' OR DEL_BIT IS NULL)", new String[]{DB_FORMAT(CHECKDATE1), DB_FORMAT(CHECKDATE2), BILLNO, CONSUMERID, DB_FORMAT(L_ISSDATE)});
                        double RAMTSUM = 0;
                        double TEMP = 0;
                        if (getnewrev.getCount() > 0) {
                            while (getnewrev.moveToNext()) {
                                RAMTSUM = getnewrev.getDouble(getnewrev.getColumnIndex("RAMTSUM"));
                                Log.e("RAMTSUM1", String.valueOf(RAMTSUM));
                            }
                        } else {
                            RAMTSUM = 0;
                        }
                        getnewrev.close();

                        Log.e("RAMTSUM2", String.valueOf(RAMTSUM));
                        if (CHECKDATE1.after(L_LASTDATE)) {
                            if (PENDING_PAYMENT > 0 && (CHECKDATE2.before(ARREARS_TO_DATE) || CHECKDATE2.equals(ARREARS_TO_DATE))) {

                                if (OTSAMT > 0) {
                                    TEMP = PENDING_PAYMENT - OTSAMT;
                                    Log.e("TEMP amt", TEMP + " PENDING_PAYMENT" + PENDING_PAYMENT + " " + OTSAMT);
                                } else {
                                    TEMP = PENDING_PAYMENT;
                                    Log.e("TEMP amt", TEMP + " tempamt" + PENDING_PAYMENT);
                                }

                                if (DPC_WAVEOFF == 0)
                                {
                                    monthdp = TEMP * 2 / 100;
                                    Log.e("monthdp", monthdp + " ");
                                    monthdp = rounddecimal(monthdp, 2);
                                    Log.e("monthdp", monthdp + " ");

                                    Log.e("DP", DP + " ");

                                    DP = DP + monthdp;
                                }

                                PENDING_PAYMENT = PENDING_PAYMENT + monthdp;
                                Log.e("PENDING_PAYMENT", PENDING_PAYMENT + " ");
                            }
                        }
                        PENDING_PAYMENT = PENDING_PAYMENT - RAMTSUM;
                        Log.e("PENDING_PAYMENT afer", PENDING_PAYMENT + " ");
                        if (OTSAMT > 0) {
                            Log.e("OTS CAL", "PAID AMOUNT TO BE DEDUCTED FROM OTS" + RAMTSUM + " " + OTSAMT);
                            OTSAMT = OTSAMT - RAMTSUM;
                            Log.e("OTSAMT", "after" + OTSAMT);
                            if (OTSAMT < 0) {
                                OTSAMT = 0;
                                ACTUAL_OTS_AMOUNT = "0";
                                Log.e("OTS CAL", OTSAMT + " " + ACTUAL_OTS_AMOUNT);
                            }
                        }

                        if (monthdp > 0) {
                            dpval = "1";

                            dcledger.put("LEDGER_ID", LEDGER_ID);
                            dcledger.put("BILLNO", BILLNO);
                            dcledger.put("CONSUMERID", CONSUMERID);
                            dcledger.put("ISSDATE", DB_FORMAT(L_ISSDATE));
                            dcledger.put("From_Date", DB_FORMAT(CHECKDATE1));
                            dcledger.put("To_Date", DB_FORMAT(CHECKDATE2));
                            dcledger.put("DP_amt", monthdp);
                            dcledger.put("user", USERNAME);
                            dcledger.put("serial_no", IMEI);
                            dcledger.put("datetime", DB_DATETIME_FORMAT(currentdttm));

                            long insres = db.insertOrThrow("deley_payment_details", null, dcledger);
                            if (insres == -1) {
                                error += "FAILED TO SAVE DELAY PAYMENT LEDGER";
                            }
                        }

                        CHECKDATE1 = ADDDAYS(CHECKDATE2, 1);
                        CHECKDATE2 = ADDDAYS(CHECKDATE2, 30);

                        if (CHECKDATE2.after(ARREARS_TO_DATE)) {
                            CHECKDATE2 = ARREARS_TO_DATE;
                        }
                    } while (CHECKDATE1.before(ARREARS_TO_DATE) || CHECKDATE1.equals(ARREARS_TO_DATE));

                    DP = rounddecimal(DP, 0);
                    Log.e("DP", String.valueOf(DP));

                    if (PENDING_PAYMENT > 0) {
                        NEXTAREEARS = rounddecimal(PENDING_PAYMENT, 0);
                    } else {
                        NEXTCREDIT = rounddecimal(PENDING_PAYMENT, 0);
                        NEXTCREDIT = 0 - NEXTCREDIT;

                    }
                    ARRDC = NEXTAREEARS + NEXTCREDIT;
                    if ("".equals(ACTUAL_OTS_AMOUNT)) {
                        OTSAMT = 0;
                    }

                    if ("".equals(error.trim())) {
                        ContentValues ledgerupdate = new ContentValues();
                        ledgerupdate.put("ADATE1", DB_FORMAT(ADATE1));
                        ledgerupdate.put("ADATE2", DB_FORMAT(ADATE2));
                        ledgerupdate.put("NEXTARR", NEXTAREEARS);
                        ledgerupdate.put("NEXTDC", NEXTCREDIT);
                        ledgerupdate.put("DPC", DP);
                        ledgerupdate.put("arres_process", "1");
                        ledgerupdate.put("OTS_AMOUNT", OTSAMT);
                        ledgerupdate.put("ACTUAL_OTS_AMOUNT", ACTUAL_OTS_AMOUNT);
                        ledgerupdate.put("TEMP_ADATE1", DB_FORMAT(AADATE1));
                        ledgerupdate.put("TEMP_ADATE2", DB_FORMAT(AADATE2));
                        ledgerupdate.put("last_edit_by", USERNAME);
                        ledgerupdate.put("last_edit_at", DB_DATETIME_FORMAT(currentdttm));
                        ledgerupdate.put("POS_USER", USERNAME);
                        ledgerupdate.put("POS_SERIAL_NO", IMEI);
                        ledgerupdate.put("POS_UP_BIT", 1);

                        int ledupdatedrows = db.update("ledgerdata", ledgerupdate, "CONSUMERID = ? AND PERDAY1=?", new String[]{CONSUMERID, PERDAY1db});
                        Log.e("PROC ARR res", String.valueOf(ledupdatedrows));
                        if (ledupdatedrows > 0) {
                            ARR_PROC = ledupdatedrows;
                            Log.e("PROC ARR res", String.valueOf(ledupdatedrows));
                        } else {
                            error += "FAILED TO PROCESS ARREARS.NO RECORDS UPDATED.";
                            Log.e("PROC ARR", error);

                        }
                    } else {
                        error += "FAILED TO PROCESS ARREARS." + error;
                        Log.e("PROC ARR", error);

                    }
                }
            } else {
                error += "FAILED TO PROCESS ARREARS.NO RECORDS FOUND FOR PROCESSING ARREARS FOR CONSUMER " + CONSUMERID;
                Log.e("PROC ARR", error);

            }
            getled.close();
        } catch (SQLException e) {
            error += " CONSUMERID " + CONSUMERID + " ," + e.getMessage();
        }
        ARRPROC.put("ERROR", error);
        ARRPROC.put("RESULT", ARR_PROC);

        return ARRPROC;
    }


    public ContentValues CALCULATE_INS_CHARGES(String CATEGORY, String CONSUMERID, Date TODATE, Date PERDAY1, String USERNAME, String IMEI) {
        String error = "";
        SQLiteDatabase db = getWritableDatabase();
        ContentValues INS_CHARGES = new ContentValues();
        double INS_CHGS_WATER = -1;
        double INS_CHGS_SEW = -1;
        Log.e("FUNCTION", "CALCULATE_INS_CHARGES");
        int res = -1;
        if ("".equals(CATEGORY)) {
            error = "CATEGORY NOT FOUND FOR CALCULATING INSPECTION CHARGES FOR CONSUMERID " + CONSUMERID;
        } else {
            try {

                int delrows = db.delete("inspection_charges", "CONSUMERID = ? AND PERDAY1 = ?", new String[]{CONSUMERID, DB_FORMAT(PERDAY1)});
                if (delrows >= 0) {
                    Cursor inspque = db.rawQuery("SELECT * FROM inspection_charges WHERE CONSUMERID=? AND ( CASE WHEN strftime('%m', ?) <= 3 " +
                            "THEN strftime('%Y', ?)-1 = ( CASE WHEN strftime('%m', DATE2) <= 3 THEN strftime('%Y', DATE2)-1 ELSE strftime('%Y', DATE2) END) " +
                            "ELSE strftime('%Y', ?) = ( CASE WHEN strftime('%m', DATE2) <= 3 THEN strftime('%Y', DATE2)-1 ELSE strftime('%Y', DATE2) END) END) " +
                            "AND ? <> PERDAY1", new String[]{CONSUMERID, DB_FORMAT(TODATE), DB_FORMAT(TODATE), DB_FORMAT(TODATE), DB_FORMAT(PERDAY1)});
                    if (inspque.getCount() > 0) {
                        INS_CHGS_WATER = 0;

                        INS_CHGS_SEW = 0;

                        res = 1;

                        Log.e("CALCULATE_INS_CHARGES1", INS_CHGS_WATER + " " + INS_CHGS_SEW);
                    } else {
                        Cursor insprate = db.rawQuery("SELECT FOR_WATER,FOR_SEWERAGE,CHANGE_DATE FROM inspection_rates WHERE CATEGORY=? ORDER BY CHANGE_DATE DESC", new String[]{CATEGORY});
                        if (insprate.getCount() > 0) {
                            while (insprate.moveToNext()) {
                                double for_water = insprate.getDouble(insprate.getColumnIndex("FOR_WATER"));

                                double for_sew = insprate.getDouble(insprate.getColumnIndex("FOR_SEWERAGE"));

                                Date ins_chn_date = DATEFROMSTRING(insprate.getString(insprate.getColumnIndex("CHANGE_DATE")));
                                Log.e("CALCULATE_INS_CHARGES", ins_chn_date + " " + TODATE);
                                if (TODATE.equals(ins_chn_date) || TODATE.after(ins_chn_date)) {
                                    INS_CHGS_WATER = for_water;

                                    INS_CHGS_SEW = for_sew;
                                    Log.e("CALCULATE_INS_CHARGES", INS_CHGS_WATER + " " + INS_CHGS_SEW);
                                    Date currentdttm = new Date(System.currentTimeMillis());
                                    ContentValues insquery = new ContentValues();
                                    insquery.put("CONSUMERID", CONSUMERID);
                                    insquery.put("PERDAY1", DB_FORMAT(PERDAY1));
                                    insquery.put("DATE2", DB_FORMAT(TODATE));
                                    insquery.put("INS_CHARGES", INS_CHGS_WATER);
                                    insquery.put("INS_CHARGES_SEW", INS_CHGS_SEW);
                                    insquery.put("OPERATION_TYPE", "SPOT BILLING");
                                    insquery.put("user", USERNAME);
                                    insquery.put("serial_no", IMEI);
                                    insquery.put("edited_at", DB_DATETIME_FORMAT(currentdttm));

                                    long insert_ins_charges = db.insert("inspection_charges", null, insquery);
                                    if (insert_ins_charges != -1) {
                                        res = 1;
                                    } else {
                                        res = -1;
                                        error += "FAILED TO INSERT INSPECTION CHARGES.";
                                    }
                                    break;

                                } else if (ins_chn_date == null) {
                                    error = "INSPECTION RATE CHANGE DATE CANNOT BE EMPTY FOR CONSUMERID " + CONSUMERID;
                                    Log.e("CALCULATE_INS_CHARGES", error);
                                } else {
                                    INS_CHGS_WATER = 0;

                                    INS_CHGS_SEW = 0;

                                    res = 1;

                                    Log.e("CALCULATE_INS_CHARGES", "NOT APP");
                                }
                            }
                        } else {
                            error = "INSPECTION CHARGES NOT FOUND FOR CONSUMERID " + CONSUMERID;
                            Log.e("CALCULATE_INS_CHARGES", error);

                        }
                        insprate.close();
                    }
                    inspque.close();
                } else {
                    error = "ERROR OCCURED WHILE CALCULATING INSPECTION CHARGES FOR " + CONSUMERID + ". ERRORCODE:- INSDELERR";
                    Log.e("CALCULATE_INS_CHARGES", error);

                }
            } catch (SQLException e) {
                error += " CONSUMERID " + CONSUMERID + " ," + e.getMessage();
                Log.e("CALCULATE_INS_CHARGES", " CONSUMERID " + CONSUMERID + " ," + e.getMessage());
            }
        }
        INS_CHARGES.put("ERROR", error);
        INS_CHARGES.put("INS_CHARGES", INS_CHGS_WATER);
        INS_CHARGES.put("INS_CHGS_SEW", INS_CHGS_SEW);
        INS_CHARGES.put("RESULT", res);

        return INS_CHARGES;
    }

    public int SAVEMETERPHOTO(String path, String CON, String USERNAME, String IMEI, Date PERDAY1, Double LAT, Double LON) {

        Log.d("METER PHOTO", "SAVEMETERPHOTO");
        Date currentdttm = new Date(System.currentTimeMillis());
        ContentValues insquery = new ContentValues();
        insquery.put("path", path);
        insquery.put("consumer_id", CON);
        insquery.put("perday1", DB_FORMAT(PERDAY1));
        insquery.put("user", USERNAME);
        insquery.put("imei", IMEI);
        insquery.put("date_time", DB_DATETIME_FORMAT(currentdttm));
        insquery.put("POS_UP_BIT", "1");
        insquery.put("latitude", LAT);
        insquery.put("longitude", LON);
        SQLiteDatabase db = getWritableDatabase();
        int res = 0;
        long insert_mp = db.insert("meter_photos", null, insquery);
        if (insert_mp != -1) {
            res = 1;
        } else {
            res = -1;
        }
        return res;
    }


    public int UPDATELEDGER(ContentValues savedata, String CONSUMERID, Date PERDAY1) {
        SQLiteDatabase db = getWritableDatabase();
        int updateresult = -1;
        int modified_count = 0;
        try {
            ContentValues ADDAMOUNTCHGS = new ContentValues();
            Log.e("SEWBALANCE",savedata.get("SEWBALANCE").toString()+" ");
            Log.e("MRBALANCE",savedata.get("MRBALANCE").toString()+" ");

            String SEWBALANCE = savedata.get("SEWBALANCE").toString();
            String MRBALANCE = savedata.get("MRBALANCE").toString();
            String SEW_EA = savedata.get("SEW_EA").toString();
            String MR_EA = savedata.get("MR_EA").toString();

            ADDAMOUNTCHGS.put("SEWBALANCE", SEWBALANCE);
            ADDAMOUNTCHGS.put("MRBALANCE", MRBALANCE);
            ADDAMOUNTCHGS.put("SEW_EA", SEW_EA);
            ADDAMOUNTCHGS.put("MR_EA", MR_EA);

            ADDAMOUNTCHGS.put("EDITED_BY", savedata.get("MR_NAME").toString());
            ADDAMOUNTCHGS.put("EDITED_AT", DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));
            ADDAMOUNTCHGS.put("POS_SERIAL_NO", savedata.get("POS_SERIAL_NO").toString());
            ADDAMOUNTCHGS.put("POS_UP_BIT", "1");

            savedata.remove("DISCON_MSG");
            savedata.remove("SEWBALANCE");
            savedata.remove("MRBALANCE");
            savedata.remove("PAYMENTSTATUS");
            //savedata.remove("SEWDD");
            //savedata.remove("MRADD");

            ContentValues CONSUMER_CONTACT = new ContentValues();
            String MOBILE_NO = savedata.get("MOBILE_NO").toString();
            String EMAIL_ID = savedata.get("EMAIL_ID").toString();
            savedata.remove("EMAIL_ID");
            savedata.remove("MOBILE_NO");
            if (!"".equals(MOBILE_NO.trim()) || !"".equals(EMAIL_ID.trim())) {
                CONSUMER_CONTACT.put("CONSUMERID", CONSUMERID);
                CONSUMER_CONTACT.put("MOBILE_NO", MOBILE_NO);
                CONSUMER_CONTACT.put("EMAIL_ID", EMAIL_ID);
                CONSUMER_CONTACT.put("POS_UP_BIT", 1);
                CONSUMER_CONTACT.put("POS_USER", savedata.get("MR_NAME").toString());
                CONSUMER_CONTACT.put("POS_SERIAL_NO", savedata.get("POS_SERIAL_NO").toString());
            }

            ContentValues UPDATE_MASTER = new ContentValues();
            if (!"".equals(MOBILE_NO.trim())) {
                UPDATE_MASTER.put("MOBILE_NO", MOBILE_NO);
            }
            if (!"".equals(EMAIL_ID.trim())) {
                UPDATE_MASTER.put("EMAIL_ID", EMAIL_ID);
            }
            UPDATE_MASTER.put("edited_by", savedata.get("MR_NAME").toString());
            UPDATE_MASTER.put("edited_at", DB_DATETIME_FORMAT(new Date(System.currentTimeMillis())));

            Cursor checkentry = db.rawQuery("SELECT * FROM ledgerdata WHERE CONSUMERID=? AND PERDAY1 = ? ", new String[]{CONSUMERID, DB_FORMAT(PERDAY1)});
            if (checkentry.getCount() > 0) {
                int bill_process = 0;
                while (checkentry.moveToNext()) {
                    bill_process = checkentry.getInt(checkentry.getColumnIndex("bill_process"));
                    modified_count = checkentry.getInt(checkentry.getColumnIndex("modified_count"));
                }
                bill_process++;
                modified_count--;
                savedata.put("bill_process", bill_process);
                savedata.put("modified_count", modified_count);
                savedata.put("mssutil_up_bit", 0);
                savedata.put("SMS_BIT", 0);
                savedata.put("EMAIL_BIT", 0);
                updateresult = db.update("ledgerdata", savedata, "CONSUMERID = ? AND PERDAY1 = ?", new String[]{CONSUMERID, DB_FORMAT(PERDAY1)});
                if (updateresult == 1) {
                    updateresult = updatecontact(CONSUMER_CONTACT, CONSUMERID, MOBILE_NO, EMAIL_ID, UPDATE_MASTER);
                    update_add_amount(ADDAMOUNTCHGS,CONSUMERID);
                } else {
                    updateresult = -1;
                    Log.e("LEDGER UPDATE", "FAILED");
                }

            } else {
                savedata.put("bill_process", "1");
                savedata.put("modified_count", 2);
                savedata.put("mssutil_up_bit", 0);
                savedata.put("SMS_BIT", 0);
                savedata.put("EMAIL_BIT", 0);
                updateresult = (int) db.insert("ledgerdata", null, savedata);
                if (updateresult != -1) {
                    updateresult = updatecontact(CONSUMER_CONTACT, CONSUMERID, MOBILE_NO, EMAIL_ID, UPDATE_MASTER);
                    update_add_amount(ADDAMOUNTCHGS,CONSUMERID);
                } else {
                    Log.e("LEDGER INSERT", "FAILED");
                }
            }
            checkentry.close();

        } catch (SQLException e) {
            Log.e("UPDATELEDGER", e.getMessage());
            updateresult = -1;
        }

        return updateresult;
    }

    public int updatecontact(ContentValues CONSUMER_CONTACT, String CONSUMERID, String MOBILE_NO, String EMAIL_ID, ContentValues UPDATE_MASTER) {

        int updateresult = -1;
        if (!"".equals(MOBILE_NO.trim()) || !"".equals(EMAIL_ID.trim())) {
            Cursor checkcont = db.rawQuery("SELECT * FROM consumer_contact WHERE CONSUMERID=? ", new String[]{CONSUMERID});
            if (checkcont.getCount() > 0) {
                Log.e("LEDGER UPDATE", "SUCCESSFUL");
                int consontresult = db.update("consumer_contact", CONSUMER_CONTACT, "CONSUMERID = ? ", new String[]{CONSUMERID});
                if (consontresult == 1) {
                    int mastresult = db.update("masterdetails", UPDATE_MASTER, "CONSUMERID = ? ", new String[]{CONSUMERID});
                    if (mastresult == 1) {
                        updateresult = 1;
                        Log.e("mast UPDATE", "SUCCESSFUL");
                    } else {
                        updateresult = -1;
                        Log.e("mast UPDATE", "FAILED");
                    }

                } else {
                    updateresult = -1;
                    Log.e("LEDGER UPDATE", "FAILED");
                }
            } else {
                int consontresult = (int) db.insert("consumer_contact", null, CONSUMER_CONTACT);
                if (consontresult != -1) {
                    int mastresult = db.update("masterdetails", UPDATE_MASTER, "CONSUMERID = ? ", new String[]{CONSUMERID});
                    if (mastresult == 1) {
                        updateresult = 1;
                        Log.e("mast UPDATE", "SUCCESSFUL");
                    } else {
                        updateresult = -1;
                        Log.e("mast UPDATE", "FAILED");
                    }
                } else {
                    updateresult = -1;
                    Log.e("LEDGER INSERT", "FAILED");
                }
            }
        } else {
            updateresult = 1;
        }
        return updateresult;
    }

    public int update_add_amount(ContentValues EAMOUNT, String CONSUMERID) {
        String SEWBALANCE = EAMOUNT.get("SEWBALANCE").toString();
        String MRBALANCE = EAMOUNT.get("MRBALANCE").toString();
        String SEWDD = EAMOUNT.get("SEW_EA").toString();
        String MRADD = EAMOUNT.get("MR_EA").toString();
        String EDITED_BY = EAMOUNT.get("EDITED_BY").toString();
        String EDITED_AT = EAMOUNT.get("EDITED_AT").toString();
        String POS_SERIAL_NO = EAMOUNT.get("POS_SERIAL_NO").toString();

        ContentValues SWEA = new ContentValues();
        SWEA.put("BALANCE",SEWBALANCE);
        SWEA.put("AMOUNT",SEWDD);
        SWEA.put("EDITED_AT",EDITED_BY);
        SWEA.put("EDITED_BY",EDITED_AT);
        SWEA.put("POS_SERIAL_NO",POS_SERIAL_NO);
        SWEA.put("POS_UP_BIT", "1");

        ContentValues MREA = new ContentValues();
        MREA.put("BALANCE",MRBALANCE);
        MREA.put("AMOUNT",MRADD);
        MREA.put("EDITED_AT",EDITED_BY);
        MREA.put("EDITED_BY",EDITED_AT);
        MREA.put("POS_SERIAL_NO",POS_SERIAL_NO);
        MREA.put("POS_UP_BIT","1");
        int updateresult = -1;

        Cursor checkcont = db.rawQuery("SELECT * FROM ADD_AMOUNT WHERE CONSUMERID=? AND AMT_TYPE=?", new String[]{CONSUMERID,"MR"});
        if (checkcont.getCount() > 0)
        {
            Log.e("LEDGER UPDATE", "SUCCESSFUL");
            int consontresult = db.update("ADD_AMOUNT", MREA, "CONSUMERID = ? AND AMT_TYPE=? ", new String[]{CONSUMERID,"MR"});
            if (consontresult == 1) {
                updateresult = 1;
                Log.e("ADD_AMOUNT UPDATE", "MREA SUCCESSFUL");
            } else {
                updateresult = -1;
                Log.e("ADD_AMOUNT UPDATE", "MREA FAILED");
            }
        }/* else
        {
            int consontresult = (int) db.insert("ADD_AMOUNT", null, MREA);
            if (consontresult != -1) {
                updateresult = 1;
                Log.e("ADD_AMOUNT INSERT", "MREA SUCCESSFUL");
            } else {
                updateresult = -1;
                Log.e("ADD_AMOUNT INSERT", "MREA FAILED");
            }
        }*/

        updateresult = -1;
        Cursor checkcont2 = db.rawQuery("SELECT * FROM ADD_AMOUNT WHERE CONSUMERID=? AND AMT_TYPE=?", new String[]{CONSUMERID,"SAMT"});
        if (checkcont2.getCount() > 0)
        {
            Log.e("LEDGER UPDATE", "SUCCESSFUL");
            int consontresult = db.update("ADD_AMOUNT", SWEA, "CONSUMERID = ? AND AMT_TYPE=? ", new String[]{CONSUMERID,"SAMT"});
            if (consontresult == 1) {
                updateresult = 1;
                Log.e("ADD_AMOUNT UPDATE", "SWEA SUCCESSFUL");
            } else {
                updateresult = -1;
                Log.e("ADD_AMOUNT UPDATE", "SWEA FAILED");
            }
        }/* else
        {
            int consontresult = (int) db.insert("ADD_AMOUNT", null, SWEA);
            if (consontresult != -1) {
                updateresult = 1;
                Log.e("ADD_AMOUNT INSERT", "SWEA SUCCESSFUL");
            } else {
                updateresult = -1;
                Log.e("ADD_AMOUNT INSERT", "SWEA FAILED");
            }
        }*/

        return updateresult;
    }

    public double rounddecimal(double decimalnumber, int places) {
        BigDecimal bd = new BigDecimal(decimalnumber).setScale(places, RoundingMode.HALF_UP);
        decimalnumber = bd.doubleValue();
        return decimalnumber;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ContentValues CALCULATE_WATER_CHARGES(String CONSUMERID, Date ISSDATE, Date TEMP_FROM_DATE, Date TEMP_TO_DATE, String CATEGORY, double UNITSPERMONTH, int FLATS, String SEWARAGE_CON, String USERNAME, String IMEI) {
        ContentValues WC_FUNCTION = new ContentValues();

        double WATER_CHARGES = 0;

        double SEW_CHARGES = 0;

        Date SEWARAGE_DATE = null;

        Date CHANGE_DATE = null;

        String ERR = "";

        int DAYS;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursorsewdate = db.rawQuery("SELECT SEWARAGE_DATE FROM ledgerdata WHERE CONSUMERID=? ORDER BY PERDAY1 DESC LIMIT 1", new String[]{CONSUMERID});
        if (cursorsewdate.getCount() > 0) {
            while (cursorsewdate.moveToNext()) {

                //Log.e("SEWARAGE_DATE",cursorsewdate.getString(cursorsewdate.getColumnIndex("SEWARAGE_DATE")));
                if (!"".equals(validate(cursorsewdate.getString(cursorsewdate.getColumnIndex("SEWARAGE_DATE"))))) {
                    SEWARAGE_DATE = DATEFROMSTRING(cursorsewdate.getString(cursorsewdate.getColumnIndex("SEWARAGE_DATE")));
                } else {
                    SEWARAGE_DATE = null;
                }

            }
        }
        cursorsewdate.close();
        Log.e("WC FUNC CATEGORY", CATEGORY);
        Cursor ratecursor = db.rawQuery("SELECT DISTINCT(CHANGE_DATE_FROM) FROM rates WHERE CATEGORY=? ORDER BY CHANGE_DATE_FROM DESC", new String[]{CATEGORY});
        if (ratecursor.getCount() > 0) {
            while (ratecursor.moveToNext()) {

                CHANGE_DATE = DATEFROMSTRING(ratecursor.getString(ratecursor.getColumnIndex("CHANGE_DATE_FROM")));

                Log.e("WC FUNC CHANGE_DATE", CHANGE_DATE.toString() + " TEMP_TO_DATE" + TEMP_TO_DATE.toString() + " TEMP_FROM_DATE" + TEMP_FROM_DATE.toString());
                if (TEMP_TO_DATE.compareTo(CHANGE_DATE) >= 0 && TEMP_FROM_DATE.compareTo(CHANGE_DATE) >= 0)//condition 1
                {
                    Log.e("ratemain ", "1");
                    WC_FUNCTION = WATER_CHARGES_CAL(TEMP_FROM_DATE, TEMP_TO_DATE, CATEGORY, UNITSPERMONTH, CHANGE_DATE, FLATS, SEWARAGE_CON, SEWARAGE_DATE);//water charges and sewerage charges

                    if ("".equals(WC_FUNCTION.get("ERR"))) {
                        WATER_CHARGES = WATER_CHARGES + (double) WC_FUNCTION.get("WATERCHARGES");
                        SEW_CHARGES = SEW_CHARGES + (double) WC_FUNCTION.get("SEWCHARGES");

                        DAYS = CALCULATEDAYS(TEMP_FROM_DATE, TEMP_TO_DATE);

                        int res = WATERCHARGES_LEDGER(ISSDATE, CONSUMERID, WATER_CHARGES, SEW_CHARGES, DAYS, USERNAME, IMEI);
                        if (res != 1) {
                            ERR += "FAILED TO SAVE LEDGER LOG FOR CONSUMER " + CONSUMERID;
                        }

                    } else {
                        ERR += WC_FUNCTION.get("ERR").toString();
                    }

                    Log.e("WC FUNC 1 WATER_CHARGES", WATER_CHARGES + " SEW_CHARGES" + SEW_CHARGES);

                    break;

                } else if (TEMP_TO_DATE.compareTo(CHANGE_DATE) >= 0 && TEMP_FROM_DATE.compareTo(CHANGE_DATE) <= 0)//condition 2
                {
                    Log.e("ratemain ", "2");

                    WC_FUNCTION = WATER_CHARGES_CAL(CHANGE_DATE, TEMP_TO_DATE, CATEGORY, UNITSPERMONTH, CHANGE_DATE, FLATS, SEWARAGE_CON, SEWARAGE_DATE);//water charges and sewerage charges

                    if ("".equals(WC_FUNCTION.get("ERR"))) {
                        WATER_CHARGES = WATER_CHARGES + (double) WC_FUNCTION.get("WATERCHARGES");
                        SEW_CHARGES = SEW_CHARGES + (double) WC_FUNCTION.get("SEWCHARGES");

                        DAYS = CALCULATEDAYS(CHANGE_DATE, TEMP_TO_DATE);

                        TEMP_TO_DATE = MINUSEDAYS(CHANGE_DATE, 1);

                        int res = WATERCHARGES_LEDGER(ISSDATE, CONSUMERID, WATER_CHARGES, SEW_CHARGES, DAYS, USERNAME, IMEI);
                        if (res != 1) {
                            ERR += "FAILED TO SAVE LEDGER LOG FRO CONSUMER " + CONSUMERID;
                            break;
                        }

                        Log.e("WC FUNC2 WATER_CHARGES", WATER_CHARGES + " SEW_CHARGES" + SEW_CHARGES + " DAYS" + DAYS + " " + TEMP_TO_DATE);

                    } else {
                        ERR += WC_FUNCTION.get("ERR");
                        break;
                    }

                }/* else if (TEMP_TO_DATE.equals(CHANGE_DATE) && TEMP_FROM_DATE.equals(CHANGE_DATE))//condition 3
                {
                    Log.e("ratemain ", "3");
                    WC_FUNCTION = WATER_CHARGES_CAL(TEMP_FROM_DATE, TEMP_TO_DATE, CATEGORY, UNITSPERMONTH, CHANGE_DATE, FLATS, SEWARAGE_CON, SEWARAGE_DATE);//water charges and sewerage charges
                    if (validate(WC_FUNCTION.get(ERR)) == "") {
                        WATER_CHARGES = WATER_CHARGES + (double) WC_FUNCTION.get("WATERCHARGES");
                        SEW_CHARGES = SEW_CHARGES + (double) WC_FUNCTION.get("SEWCHARGES");
                    } else {
                        ERR += WC_FUNCTION.get("ERR").toString();
                    }

                    Log.e("WC FUNC 3 WATER_CHARGES", WATER_CHARGES + " SEW_CHARGES" + SEW_CHARGES);

                    break;
                } else if (TEMP_TO_DATE.equals(CHANGE_DATE) && TEMP_FROM_DATE.before(CHANGE_DATE))//condition 4
                {
                    Log.e("ratemain ", "4");
                    WC_FUNCTION = WATER_CHARGES_CAL(CHANGE_DATE, TEMP_TO_DATE, CATEGORY, UNITSPERMONTH, CHANGE_DATE, FLATS, SEWARAGE_CON, SEWARAGE_DATE);//water charges and sewerage charges
                    if (validate(WC_FUNCTION.get(ERR)) == "") {
                        WATER_CHARGES = WATER_CHARGES + (double) WC_FUNCTION.get("WATERCHARGES");
                        SEW_CHARGES = SEW_CHARGES + (double) WC_FUNCTION.get("SEWCHARGES");

                        DAYS = CALCULATEDAYS(CHANGE_DATE, TEMP_TO_DATE);

                        TEMP_TO_DATE = MINUSEDAYS(CHANGE_DATE, 1);

                        Log.e("WC FUNC4 WATER_CHARGES", WATER_CHARGES + " SEW_CHARGES" + SEW_CHARGES + " DAYS" + DAYS + " " + TEMP_TO_DATE);

                    } else {
                        Log.e("ratemain ", "5");
                        ERR += WC_FUNCTION.get("ERR");
                        break;
                    }
                }*/ else if (CHANGE_DATE.toString().equals("")) {
                    Log.e("ratemain ", "SKIPPING ");
                    ERR += "CHANGE DATE NOT FOUND IN RATES TABLE";
                    break;
                }
                Log.e("ratemain ", "NOT APPLICABLE ");
            }
        }
        ratecursor.close();

        Log.e("WC FUNClast WA_CHA", WATER_CHARGES + " SEW_CHARGES" + SEW_CHARGES);

        WC_FUNCTION.put("WATER_CHARGES", WATER_CHARGES);

        WC_FUNCTION.put("SEW_CHARGES", SEW_CHARGES);

        WC_FUNCTION.put("ERROR", ERR);

        return WC_FUNCTION;

    }

    public Cursor getcol_const(String tablename) {
        Cursor COLS = null;
        if (!"".equals(tablename)) {
            try {
                SQLiteDatabase db = getWritableDatabase();
                COLS = db.rawQuery("SELECT * FROM column_constant_updated WHERE tablename=?", new String[]{tablename});
                Log.e("CONSTANT getcol_const", tablename + " " + COLS);
            } catch (SQLException e) {
                Log.e("CONSTANT getcol_const", e.getMessage());
            }
        }
        return COLS;
    }

    public long BARCODEDATA(String CONSUMERID, String ISSDATE, String TEMP_ISSDATE, String PERDAY1, String NET, String WC, String METRATE, String SEWARAGE, String ARREARS, String IMEI, String USERNAME) {
        Date currentdttm = new Date(System.currentTimeMillis());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues barcode = new ContentValues();
        barcode.put("CONSUMERID", CONSUMERID);
        barcode.put("ISSDATE", ISSDATE);
        barcode.put("TEMP_ISSDATE", TEMP_ISSDATE);
        barcode.put("PERDAY1", PERDAY1);
        barcode.put("NET", NET);
        barcode.put("WC", WC);
        barcode.put("MR", METRATE);
        barcode.put("SEW", SEWARAGE);
        barcode.put("ARREARS", ARREARS);
        barcode.put("POS_SERIAL_NO", IMEI);
        barcode.put("USER", USERNAME);
        barcode.put("ENTERED_AT", DB_DATETIME_FORMAT(currentdttm));
        try {
            long insres = db.insertOrThrow("barcode_data", null, barcode);
            if (insres == -1) {
                Log.e("barcode", "INSERTION ERROR");
                return -1;
            } else {
                Log.e("barcode", "INSERTION SUCCESSFUL");
                return insres;
            }

        } catch (SQLException e)
        {
            Log.e("WCLED ERROR", e.getMessage());
            return -1;
        }
    }

    public int WATERCHARGES_LEDGER(Date ISSDATE, String CONSUMERID, double WC, double SW, int DAYS, String USERNAME, String IMEI) {
        Date currentdttm = new Date(System.currentTimeMillis());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues wcled = new ContentValues();
        wcled.put("ISSDATE", DB_FORMAT(ISSDATE));
        wcled.put("CONSUMERID", CONSUMERID);
        wcled.put("water_charges", WC);
        wcled.put("sw_charges", SW);
        wcled.put("days", String.valueOf(DAYS));
        wcled.put("user", USERNAME);
        wcled.put("serial_no", IMEI);
        wcled.put("edited_date_time", DB_DATETIME_FORMAT(currentdttm));
        try {
            long insres = db.insertOrThrow("water_charges_ledger", null, wcled);
            if (insres == -1) {
                return -1;
            } else {
                return 1;
            }

        } catch (SQLException e) {
            Log.e("WCLED ERROR", e.getMessage());
            return -1;
        }
    }

    public ContentValues WATER_CHARGES_CAL(Date TEMP_FROM_DATE, Date TEMP_TO_DATE, String CATEGORY, double UNITSPERMONTH, Date CHANGE_DATE, int FLATS, String SEWARAGE_CON, Date SEWARAGE_DATE) {
        double SC_TEMP = 0;
        double WC_TEMP_CALCULATED = 0;
        double WC_TEMP = 0;
        double SW_RATE = 0;

        String ERRORSTR = "";

        ContentValues CALCULATED = new ContentValues();

        int DAYS = CALCULATEDAYS(TEMP_FROM_DATE, TEMP_TO_DATE);
        Log.e("WATENCTEMP_FROM_DATE", TEMP_FROM_DATE.toString() + " TEMP_TO_DATE" + TEMP_TO_DATE);
        Log.e("WATER FUNC RATE DAYS", String.valueOf(DAYS));
        Log.e("RATES", "CATEGORY=" + CATEGORY + " AND CHANGE_DATE_FROM=" + CHANGE_DATE + " ");

        SQLiteDatabase db = getWritableDatabase();
        Cursor ratecursor = db.rawQuery("SELECT SLAB, RATE ,SEWERAGE_RATE,FINAL_SLAB  FROM rates WHERE CATEGORY=? AND CHANGE_DATE_FROM=? ORDER BY change_date_from DESC", new String[]{CATEGORY, DB_FORMAT(CHANGE_DATE)});
        if (ratecursor.getCount() > 0) {
            while (ratecursor.moveToNext()) {
                SW_RATE = ratecursor.getDouble(ratecursor.getColumnIndex("SEWERAGE_RATE"));

                int SLAB = ratecursor.getInt(ratecursor.getColumnIndex("SLAB"));

                double RATE = ratecursor.getDouble(ratecursor.getColumnIndex("RATE"));

                int FINAL_SLAB = ratecursor.getInt(ratecursor.getColumnIndex("FINAL_SLAB"));

                Log.e("SW_RATE", SW_RATE + " SLAB RATE " + SLAB + " RATE " + RATE + " FINAL_SLAB " + FINAL_SLAB);
                Log.e("unitspm", rounddecimal(UNITSPERMONTH, 0) + " unitspm " + UNITSPERMONTH);

                if (UNITSPERMONTH > SLAB) {
                    if (FINAL_SLAB == 1) {
                        WC_TEMP = WC_TEMP + (UNITSPERMONTH * RATE);

                        break;
                    } else {
                        WC_TEMP = WC_TEMP + ((UNITSPERMONTH - SLAB) * RATE);
                        UNITSPERMONTH = SLAB;
                    }
                }
            }
        } else {
            ERRORSTR += "NO RECORDS FOUND IN RATES TABLE FOR CATEGORY " + CATEGORY;
        }
        ratecursor.close();
        Log.e("WC_TEMP", WC_TEMP + " DAYS" + DAYS);

        WC_TEMP_CALCULATED = ((WC_TEMP / 30) * DAYS);//*$flats

        Log.e("WC_TEMP_CALCULATED", String.valueOf(WC_TEMP_CALCULATED));

        int SW_TEMP_DAYS;

        if ("Y".equals(SEWARAGE_CON) || "1".equals(SEWARAGE_CON)) {
            SC_TEMP = ((WC_TEMP_CALCULATED / DAYS));
            Log.e("0 SEWARAGE_CON", String.valueOf(SC_TEMP) + SEWARAGE_DATE);
            if (SEWARAGE_DATE != null) {
                if (TEMP_FROM_DATE.compareTo(SEWARAGE_DATE) <= 0 && TEMP_TO_DATE.compareTo(SEWARAGE_DATE) >= 0) {
                    SW_TEMP_DAYS = CALCULATEDAYS(SEWARAGE_DATE, TEMP_TO_DATE);

                    SC_TEMP = SC_TEMP * SW_TEMP_DAYS;
                    Log.e("1 SEWARAGE_CON", TEMP_FROM_DATE + " " + TEMP_TO_DATE + " " + SEWARAGE_DATE + SC_TEMP);

                } else if (TEMP_FROM_DATE.compareTo(SEWARAGE_DATE) > 0 || SEWARAGE_DATE.toString().isEmpty()) {
                    SW_TEMP_DAYS = CALCULATEDAYS(TEMP_FROM_DATE, TEMP_TO_DATE);

                    SC_TEMP = SC_TEMP * SW_TEMP_DAYS;
                    Log.e("2 SEWARAGE_CON", TEMP_FROM_DATE + " " + TEMP_TO_DATE + " " + SEWARAGE_DATE + SC_TEMP);
                } else if (TEMP_FROM_DATE.compareTo(SEWARAGE_DATE) < 0 && TEMP_TO_DATE.compareTo(SEWARAGE_DATE) < 0) {
                    SC_TEMP = 0;
                    Log.e("3 SEWARAGE_CON", TEMP_FROM_DATE + " " + TEMP_TO_DATE + " " + SEWARAGE_DATE + SC_TEMP);
                }
            } else {
                SW_TEMP_DAYS = CALCULATEDAYS(TEMP_FROM_DATE, TEMP_TO_DATE);
                SC_TEMP = SC_TEMP * SW_TEMP_DAYS;
            }

            SC_TEMP = SC_TEMP * (SW_RATE / 100);
            Log.e("SEW RATE", String.valueOf(SW_RATE));
        } else {
            Log.e("No SEWARAGE_CON", String.valueOf(SC_TEMP));
            SC_TEMP = 0;
        }
        Log.e("SEWARAGE_CON", String.valueOf(SC_TEMP));
        Log.e("WC_TEMP_CALCULATED", String.valueOf(WC_TEMP_CALCULATED));

        CALCULATED.put("WATERCHARGES", WC_TEMP_CALCULATED);
        CALCULATED.put("SEWCHARGES", SC_TEMP);
        CALCULATED.put("ERR", ERRORSTR);

        return CALCULATED;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ContentValues CALCULATE_METER_RENT(Date TEMP_FROM_DATE, Date TEMP_TO_DATE, String MET_CODE) {
        String ERR = "";
        double MR_TEMP = 0;

        ContentValues MRCALCULATED = new ContentValues();

        Date CHANGE_DATE;

        SQLiteDatabase db = getWritableDatabase();
        Cursor ratecursor = db.rawQuery("SELECT DISTINCT(CHANGE_DATE) FROM meter_details WHERE METCODE=? ORDER BY CHANGE_DATE DESC", new String[]{MET_CODE});
        if (ratecursor.getCount() > 0) {
            while (ratecursor.moveToNext()) {
                CHANGE_DATE = DATEFROMSTRING(ratecursor.getString(ratecursor.getColumnIndex("CHANGE_DATE")));

                if (TEMP_TO_DATE.compareTo(CHANGE_DATE) >= 0 && TEMP_FROM_DATE.compareTo(CHANGE_DATE) >= 0)//condition 1
                {
                    MRCALCULATED = METER_RENT(TEMP_FROM_DATE, TEMP_TO_DATE, CHANGE_DATE, MET_CODE);

                    MR_TEMP = MR_TEMP + (double) MRCALCULATED.get("METER_RENT");

                    ERR += MRCALCULATED.get("ERROR");

                    break;

                } else if (TEMP_TO_DATE.compareTo(CHANGE_DATE) >= 0 && TEMP_FROM_DATE.compareTo(CHANGE_DATE) <= 0)//condition 2
                {
                    MRCALCULATED = METER_RENT(CHANGE_DATE, TEMP_TO_DATE, CHANGE_DATE, MET_CODE);

                    TEMP_TO_DATE = MINUSEDAYS(CHANGE_DATE, 1);

                    MR_TEMP = MR_TEMP + (double) MRCALCULATED.get("METER_RENT");

                    ERR += MRCALCULATED.get("ERROR");

                }/* else if (TEMP_TO_DATE.equals(CHANGE_DATE) && TEMP_FROM_DATE.equals(CHANGE_DATE))//condition 3
                {
                    MRCALCULATED = METER_RENT(TEMP_FROM_DATE, TEMP_TO_DATE, CHANGE_DATE, MET_CODE);

                    MR_TEMP = MR_TEMP + (double) MRCALCULATED.get("METER_RENT");

                    ERR += MRCALCULATED.get("ERROR");

                    break;
                } else if (TEMP_TO_DATE.equals(CHANGE_DATE) && TEMP_FROM_DATE.before(CHANGE_DATE))//condition 4
                {
                    MRCALCULATED = METER_RENT(CHANGE_DATE, TEMP_TO_DATE, CHANGE_DATE, MET_CODE);

                    TEMP_TO_DATE = MINUSEDAYS(CHANGE_DATE, 1);

                    MR_TEMP = MR_TEMP + (double) MRCALCULATED.get("METER_RENT");

                    ERR += MRCALCULATED.get("ERROR");
                } */ else if ("".equals(CHANGE_DATE.toString())) {
                    ERR += "CHANGE DATE NOT FOUND IN RATES TABLE";
                    break;
                }
            }
        }else
        {
            ERR += "NO RATES FOUND FOR METER CODE "+MET_CODE;
        }
        ratecursor.close();
        MRCALCULATED.put("METER_RENT", MR_TEMP);

        MRCALCULATED.put("ERROR", ERR);

        return MRCALCULATED;
    }

    public ContentValues METER_RENT(Date TEMP_FROM_DATE, Date TEMP_TO_DATE, Date CHANGE_DATE, String MET_CODE) {
        int DAYS = CALCULATEDAYS(TEMP_FROM_DATE, TEMP_TO_DATE);

        double METER_RATE = 0;

        double METER_RENT = 0;

        ContentValues MRCAL = new ContentValues();

        String ERR = "";

        if (!"".equals(MET_CODE.trim())) {
            SQLiteDatabase db = getWritableDatabase();
            Cursor ratecursor = db.rawQuery("SELECT METER_RATES FROM meter_details WHERE METCODE=? AND CHANGE_DATE=? ORDER BY CHANGE_DATE DESC", new String[]{MET_CODE, DB_FORMAT(CHANGE_DATE)});
            if (ratecursor.getCount() > 0) {
                while (ratecursor.moveToNext()) {
                    METER_RATE = ratecursor.getDouble(ratecursor.getColumnIndex("METER_RATES"));
                    METER_RENT = (METER_RATE / 30) * DAYS;
                }
            } else {
                ERR += "NO RECORDS FOUND IN METER DETAILS TABLE FOR METER CODE " + MET_CODE;
            }
            ratecursor.close();

        } else {
            ERR += "METER CODE IS EMPTY";
        }
        MRCAL.put("METER_RENT", METER_RENT);

        MRCAL.put("ERROR", ERR);

        return MRCAL;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ContentValues CALCULATE_SD_RENT(Date TEMP_FROM_DATE, Date TEMP_TO_DATE, int ROOMS, String CATEGORY) {
        String ERR = "";
        double SDR_TEMP = 0;

        ContentValues SDRCALCULATED = new ContentValues();

        Date CHANGE_DATE;

        SQLiteDatabase db = getWritableDatabase();
        Cursor ratecursor = db.rawQuery("SELECT DISTINCT(change_date) FROM rent WHERE CATEGORY=? ORDER BY CHANGE_DATE DESC", new String[]{CATEGORY});
        if (ratecursor.getCount() > 0) {
            while (ratecursor.moveToNext()) {
                CHANGE_DATE = DATEFROMSTRING(ratecursor.getString(ratecursor.getColumnIndex("change_date")));

                if (TEMP_TO_DATE.compareTo(CHANGE_DATE) >= 0 && TEMP_FROM_DATE.compareTo(CHANGE_DATE) >= 0)//condition 1
                {
                    SDRCALCULATED = SD_RENT(TEMP_FROM_DATE, TEMP_TO_DATE, CHANGE_DATE, ROOMS, CATEGORY);

                    SDR_TEMP = SDR_TEMP + (double) SDRCALCULATED.get("SD_RENT");

                    ERR += SDRCALCULATED.get("ERROR");

                    break;

                } else if (TEMP_TO_DATE.compareTo(CHANGE_DATE) >= 0 && TEMP_FROM_DATE.compareTo(CHANGE_DATE) <= 0)//condition 2
                {
                    SDRCALCULATED = SD_RENT(CHANGE_DATE, TEMP_TO_DATE, CHANGE_DATE, ROOMS, CATEGORY);

                    TEMP_TO_DATE = MINUSEDAYS(CHANGE_DATE, 1);

                    SDR_TEMP = SDR_TEMP + (double) SDRCALCULATED.get("SD_RENT");

                    ERR += SDRCALCULATED.get("ERROR");

                }else if ("".equals(CHANGE_DATE.toString())) {
                    ERR += "CHANGE DATE NOT FOUND IN RENT TABLE";
                    break;
                }
            }
        }else
        {
            ERR = "NA";
        }
        ratecursor.close();
        SDRCALCULATED.put("SD_RENT", SDR_TEMP);

        SDRCALCULATED.put("ERROR", ERR);

        return SDRCALCULATED;
    }

    public ContentValues SD_RENT(Date TEMP_FROM_DATE, Date TEMP_TO_DATE, Date CHANGE_DATE,int ROOMS, String CATEGORY) {
        int DAYS = CALCULATEDAYS(TEMP_FROM_DATE, TEMP_TO_DATE);

        double SD_RATE = 0;

        double SD_RENT = 0;

        ContentValues SDRCAL = new ContentValues();

        String ERR = "";

        if (!"".equals(CATEGORY.trim())) {
            SQLiteDatabase db = getWritableDatabase();
            Cursor ratecursor = db.rawQuery("SELECT rent FROM rent WHERE CATEGORY=? AND CHANGE_DATE=? ORDER BY CHANGE_DATE DESC", new String[]{CATEGORY, DB_FORMAT(CHANGE_DATE)});
            if (ratecursor.getCount() > 0) {
                while (ratecursor.moveToNext()) {
                    SD_RATE = ratecursor.getDouble(ratecursor.getColumnIndex("rent"));
                    SD_RENT = ( (SD_RATE / 30) * DAYS ) * ROOMS;
                    Log.e("SD_RATE",SD_RATE+" "+ROOMS+" "+DAYS);
                }
            } else {
                ERR += "RENT:- NO RECORDS FOUND IN RENT TABLE FOR CATEGORY " + CATEGORY;
            }
            ratecursor.close();

        } else {
            ERR += "RENT:- CATEGOARY IS EMPTY";
        }
        SDRCAL.put("SD_RENT", SD_RENT);

        SDRCAL.put("ERROR", ERR);

        return SDRCAL;
    }

    public ContentValues CALCULATE_LPCD(double UNITSPERMONTH, int FLATS, int POPULATION, String CATEGORY) {
        ContentValues LPCDCAL = new ContentValues();
        double UNITS_PERDAY = UNITSPERMONTH / 30;
        double LPCD = 0;
        int MIN_POPULA = 4;
        String CATEGORY_GROUP = "";
        String ERR = "";

        SQLiteDatabase db = getWritableDatabase();
        Cursor ratecursor = db.rawQuery("SELECT DISTINCT(CATEGORY_GROUP) FROM category WHERE CATEGORY=?", new String[]{CATEGORY});
        if (ratecursor.getCount() > 0) {
            while (ratecursor.moveToNext()) {
                CATEGORY_GROUP = ratecursor.getString(ratecursor.getColumnIndex("CATEGORY_GROUP"));
            }
        }
        ratecursor.close();
        if ("".equals(CATEGORY_GROUP.trim())) {
            ERR = "CATEGORY GROUP NOT FOUND WHILE CALCULATIONG LPCD";
        }

        if ("A".equals(CATEGORY_GROUP)) {
            if (POPULATION < 1) {
                LPCD = (UNITS_PERDAY * FLATS * 1000.0) / (MIN_POPULA);
            } else {
                LPCD = (UNITS_PERDAY * FLATS * 1000.0) / (POPULATION);
            }

        }

        LPCDCAL.put("ERROR", ERR);

        LPCDCAL.put("LPCD", LPCD);

        return LPCDCAL;
    }

    public ContentValues UNITSPERMONTH(long CUR_READING, long PREV_READING, Date DATE1, Date DATE2, double ADDUNITS, double EXUNITS, double MIN_UNIT_CONSUMER, double AVGUNIT, String FAULT, double MIN_UNIT_CATEGORY, String CATEGORY, double FLATS, double PREV_CUM, Date REPLACEMENT_DATE, String METER_STATUS, String PREVMETERSTAT, double MF_FACTOR) {
        ContentValues DATAARRAY = new ContentValues();

        double UNITS_USED = 0;
        double UNITS_USED_DB = 0;
        double CURRENT_CUM = 0;
        double NEW_AVG = 0;
        double PERDAY = 0;
        double MIN_UNIT_VAL = 0;
        double USED = 0;
        double USED1 = 0;

        String BASE = "";

        int DAYS = CALCULATEDAYS(DATE1, DATE2);

        if (PREV_CUM > 0)
        {
            PREV_CUM = PREV_CUM / FLATS;
        }

        long READING_DIFFERECE = CUR_READING - PREV_READING;

        Log.e("CALCULATION DEBUG DAYS", DAYS + " reading" + READING_DIFFERECE);

        if (READING_DIFFERECE > 0)
        {

            UNITS_USED = READING_DIFFERECE / FLATS;

            UNITS_USED_DB = READING_DIFFERECE / FLATS;//to save only
        } else
        {
            UNITS_USED = 0;

            UNITS_USED_DB = 0;
        }

        Log.e("CAL DEBUG UNITS_USED", UNITS_USED + " UNITS_USED_DB" + UNITS_USED_DB);

        MIN_UNIT_CONSUMER = MIN_UNIT_CONSUMER / FLATS;

        AVGUNIT = AVGUNIT / FLATS;

        if (ADDUNITS != 0)
        {
            ADDUNITS = ADDUNITS / FLATS;//note ADDUNITS
        }

        if (EXUNITS != 0)
        {
            EXUNITS = EXUNITS / FLATS;//note EXUNITS
        }
        Log.e("FAULT", "CUR_FAULTF" + FAULT);
        if ("".equals(FAULT.trim()) || "TD".equals(FAULT) || "NC".equals(FAULT))
        {
            Log.e("FAULT", "no fault " + FAULT);
            UNITS_USED = UNITS_USED + ADDUNITS + EXUNITS;

            Log.e("REPLACEMENT_DATE", String.valueOf(REPLACEMENT_DATE));
            Log.e("PREVMETERSTAT", PREVMETERSTAT);
            Log.e("METER_STATUS", METER_STATUS);

            if ("D".equals(PREVMETERSTAT) && "L".equals(METER_STATUS) && REPLACEMENT_DATE == null)//note replacement date
            {
                UNITS_USED = UNITS_USED - PREV_CUM;
            }

            if (MIN_UNIT_CATEGORY > MIN_UNIT_CONSUMER)
            {
                MIN_UNIT_VAL = (MIN_UNIT_CATEGORY / 30) * DAYS;
            } else {
                MIN_UNIT_VAL = (MIN_UNIT_CONSUMER / 30) * DAYS;
            }

            if (UNITS_USED < MIN_UNIT_VAL)
            {

                USED = MIN_UNIT_VAL;

                BASE = "M";

            } else
            {

                USED = UNITS_USED;

                BASE = "R";

            }

            USED = USED * MF_FACTOR;

            USED1 = (USED / DAYS) * 30;

            NEW_AVG = (AVGUNIT + USED1) / 2;

            MIN_UNIT_VAL = (MIN_UNIT_VAL / DAYS) * 30;

        } else
        {
            if (MIN_UNIT_CATEGORY > MIN_UNIT_CONSUMER)
            {
                MIN_UNIT_VAL = MIN_UNIT_CATEGORY;

            } else
            {
                MIN_UNIT_VAL = MIN_UNIT_CONSUMER;
            }
            Log.e("CAL DEBUG MIN_UNIT_VAL", String.valueOf(MIN_UNIT_VAL));
            if (MIN_UNIT_VAL < AVGUNIT)
            {
                Log.e("CAL AVGUNIT", "AVG GRET" + AVGUNIT + " " + MIN_UNIT_VAL);
                USED1 = AVGUNIT;
                BASE = "A";
            } else
            {
                USED1 = MIN_UNIT_VAL;
                BASE = "M";
                Log.e("CAL AVGUNIT", "AVG small" + AVGUNIT + " " + MIN_UNIT_VAL);
            }
            Log.e("CAL AVGUNIT", String.valueOf(AVGUNIT));
            USED1 = USED1 * MF_FACTOR;

            USED = (USED1 / 30) * DAYS;

            NEW_AVG = AVGUNIT;

            /*if(!REPLACEMENT_DATE.toString().isEmpty())
            {
                CURRENT_CUM = USED;
            }else
            {
                CURRENT_CUM = USED + PREV_CUM;
            }*/
        }

        Log.e("CAL DEBUG USED", String.valueOf(USED));

        MIN_UNIT_VAL = MIN_UNIT_VAL * FLATS;

        PERDAY = (USED / DAYS) * FLATS;

        DATAARRAY.put("PERDAY", PERDAY);
        DATAARRAY.put("MIN_UNIT", MIN_UNIT_VAL);
        DATAARRAY.put("USED", USED);
        DATAARRAY.put("USED1", USED1);
        DATAARRAY.put("UNITS_USED_DBS", UNITS_USED_DB);
        DATAARRAY.put("NEW_AVG", NEW_AVG);
        DATAARRAY.put("BASE", BASE);
        DATAARRAY.put("CURRENT_CUM", CURRENT_CUM);
        DATAARRAY.put("DAYS", DAYS);
        DATAARRAY.put("FLATS", FLATS);

        return DATAARRAY;
    }

    public ContentValues GETPRINTINGDATA(String CONSUMERID, String ISSDATE) {
        ContentValues PRINTDATA = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        Log.e("GETPRINTINGDATA", "SELECT *  FROM ledgerdata WHERE CONSUMERID="+CONSUMERID+" AND ISSDATE="+ISSDATE);
        Cursor dbdata = db.rawQuery("SELECT *  FROM ledgerdata WHERE CONSUMERID=? AND ISSDATE=?", new String[]{CONSUMERID, ISSDATE});
        int totalcolumn = dbdata.getCount();
        if (totalcolumn > 0) {
            while (dbdata.moveToNext()) {

                for (int i = 0; i < dbdata.getColumnCount(); i++) {
                    PRINTDATA.put(dbdata.getColumnName(i), dbdata.getString(i));
                }
            }
            PRINTDATA.put("ERROR", "");
        } else {
            PRINTDATA.put("ERROR", "NO RECORDS FOUND FOR " + CONSUMERID + " FOR THE ISSUE DATE " + ISSDATE);
        }
        dbdata.close();
        return PRINTDATA;
    }

    public String GET_UPLOAD_DATA(String USERNAME) {

        SQLiteDatabase db = getWritableDatabase();
        String data_to_display = "<H5>DATA UPLOAD STATUS</H5>"+USERNAME+" - "+DB_DATETIME_FORMAT_DISPLAY(new Date(System.currentTimeMillis()))+"<br>";
        Log.e("GETPRINTINGDATA", "SELECT tablename  FROM column_constant_updated WHERE table_type=?");
        Cursor dbdata = db.rawQuery("SELECT tablename  FROM column_constant_updated WHERE table_type=? or table_type=? order by tablename asc", new String[]{"3","2"});
        int totalcolumn = dbdata.getCount();
        if (totalcolumn > 0)
        {
            while (dbdata.moveToNext())
            {
                String tabname = dbdata.getString(dbdata.getColumnIndex("tablename"));
                Cursor updataque = db.rawQuery("SELECT COUNT(POS_UP_BIT) as toupload FROM "+tabname+" WHERE POS_UP_BIT=?", new String[]{"1"});
                if (updataque.getCount() > 0)
                {
                    while (updataque.moveToNext()) {

                        data_to_display += tabname.replace("_"," ").toUpperCase(Locale.ROOT)+" : <b>"+updataque.getString(updataque.getColumnIndex("toupload"))+"</b><br>";
                        //BILLNO = ("000000000" + BILLNO).substring(BILLNO.length());
                    }

                }
            }
        }
        dbdata.close();
        //data_to_display += DB_DATETIME_FORMAT_DISPLAY(new Date(System.currentTimeMillis()))+"<br>";
        return data_to_display;
    }

    public String getmetersize(String metercode) {
        String meter_size = "";
        SQLiteDatabase db = getWritableDatabase();

        Cursor dbdata = db.rawQuery("SELECT DISTINCT(METER_SIZE) FROM meter_details WHERE METCODE=?", new String[]{metercode});
        if (dbdata.getCount() > 0) {
            while (dbdata.moveToNext()) {
                meter_size = dbdata.getString(dbdata.getColumnIndex("METER_SIZE"));
            }
        } else {
            Log.e("ERROR", "METER SIZE NOT FOUND");
            meter_size = "";
        }
        dbdata.close();
        return meter_size;
    }



    String GENERATE_BILL_NO(String ISSDATE, String DIV, String SUBDIV, String ZONE, String IMEI) {
        String BILLCODE = "";
        String oldbillno = "";
        String BILLNO = "";
        String POS_ID = "";

        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor dev_data = db.rawQuery("SELECT POS_ID FROM device_details WHERE POS_SERIAL_NO = ? AND STATUS = ?", new String[]{IMEI, "1"});
            if (dev_data.getCount() > 0) {
                Log.e("dev_data", String.valueOf(dev_data.getCount()));
                while (dev_data.moveToNext()) {
                    POS_ID = dev_data.getString(dev_data.getColumnIndex("POS_ID"));
                    Log.e("POS_ID", POS_ID);
                }
                if (!"".equals(POS_ID)) {
                    Cursor dbdata = db.rawQuery("SELECT BILLCODE FROM zone WHERE ZONE = ?", new String[]{ZONE});
                    if (dbdata.getCount() > 0) {
                        Log.e("BILLGEN", String.valueOf(dbdata.getCount()));
                        while (dbdata.moveToNext()) {
                            BILLCODE = dbdata.getString(dbdata.getColumnIndex("BILLCODE"));
                            Log.e("BILLCODE", BILLCODE);
                        }
                    } else {
                        Log.e("ERROR", "BILLCODE NOT FOUND FOR ZONE " + ZONE);
                        BILLNO = "-1";
                        return BILLNO;
                    }
                    dbdata.close();

                    Cursor bilgen = db.rawQuery("SELECT MAX(CAST(BILLNO AS UNSIGNED)) AS oldbillno FROM ledgerdata WHERE DIV_NM=? AND ZONE=? AND SUBDIV=? AND ISSDATE=?  LIMIT 1", new String[]{DIV, ZONE, SUBDIV, ISSDATE});
                    if (bilgen.getCount() > 0) {
                        Log.e("BILLCODE ERROR", "no ledger");
                        while (bilgen.moveToNext()) {
                            oldbillno = validate(bilgen.getString(bilgen.getColumnIndex("oldbillno")));
                            Log.e("BILLCODE oldbillno", "sad" + oldbillno + "sad");
                            if (!"".equals(oldbillno) && oldbillno != null) {

                                BILLNO = String.valueOf(Integer.parseInt(oldbillno) + 1);
                                BILLNO = ("000000000" + BILLNO).substring(BILLNO.length());
                                Log.e("BILLCODE ERROR", BILLNO);
                            } else {
                                Log.e("BILLCODE BILLNO", BILLNO);
                                BILLNO = POS_ID + BILLCODE + "0001";
                            }
                        }
                    } else {
                        BILLNO = POS_ID + BILLCODE + "0001";
                    }
                    bilgen.close();
                } else {
                    Log.e("ERROR", "POS_ID IS EMPTY FOR SERIAL NO " + IMEI);
                    BILLNO = "-1";
                }
            } else {
                Log.e("BILLNOERROR", "POS_ID NOT FOUND FOR SERIAL NO  " + IMEI);
                BILLNO = "-1";
            }
            dev_data.close();
        } catch (SQLException e) {
            Log.e("BILLNOERROR", e.getMessage());
            return "-1";
        }

        Log.e("BILLCODE BILLNO", BILLNO);
        return BILLNO;

    }

    public Cursor GRIDDATES(String DIV, String SUBDIV, String ZONE, String IMEI) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor COLS = db.rawQuery("SELECT count(ledgerdata.ID) as TOTAL_CONSUMERS,PERDAY1 AS FROM_DATE, PERDAY2 AS TO_DATE, ISSDATE AS ISSUE_DATE, LASTDATE AS LAST_DATE, ADATE1 AS ARREARS_FROM_DATE, ADATE2 AS ARREARS_TO_DATE FROM ledgerdata left join masterdetails ON ledgerdata.CONSUMERID=masterdetails.CONSUMERID WHERE ledgerdata.DIV_NM=? AND ledgerdata.SUBDIV=? AND ledgerdata.ZONE=? AND ledgerdata.POS_SERIAL_NO=? AND ((ledgerdata.DATE2=ledgerdata.PERDAY2) OR ((ledgerdata.PERDAY2!=ledgerdata.DATE2 AND masterdetails.DELETED=0) OR (ledgerdata.PERDAY2!=ledgerdata.DATE2 AND BILLNO IS NOT NULL))) GROUP BY PERDAY1, PERDAY2, ISSDATE, LASTDATE,ADATE1, ADATE2 ORDER BY PERDAY1 DESC", new String[]{DIV, SUBDIV, ZONE, IMEI});//
        Log.e("GRIDDATES", "SELECT count(ledgerdata.ID) as TOTAL_CONSUMERS,PERDAY1 AS FROM_DATE, PERDAY2 AS TO_DATE, ISSDATE AS ISSUE_DATE, LASTDATE AS LAST_DATE, ADATE1 AS ARREARS_FROM_DATE, ADATE2 AS ARREARS_TO_DATE FROM ledgerdata left join masterdetails ON ledgerdata.CONSUMERID=masterdetails.CONSUMERID WHERE ledgerdata.DIV_NM='"+DIV+"' AND ledgerdata.SUBDIV='"+SUBDIV+"' AND ledgerdata.ZONE='"+ZONE+"' AND ledgerdata.POS_SERIAL_NO='"+IMEI+"' AND ((ledgerdata.DATE2=ledgerdata.PERDAY2) OR ((ledgerdata.PERDAY2!=ledgerdata.DATE2 AND masterdetails.DELETED=0) OR (ledgerdata.PERDAY2!=ledgerdata.DATE2 AND BILLNO IS NOT NULL))) GROUP BY PERDAY1, PERDAY2, ISSDATE, LASTDATE,ADATE1, ADATE2 ORDER BY PERDAY1 DESC");
        return COLS;
    }

    public Cursor masterdetails(String text,String limit,String ZONE, String IMEI) {
        SQLiteDatabase db = getWritableDatabase();
        String find="";
        if(!"".equals(text))
        {
            find = " AND ( CONSUMERID='"+ text.toUpperCase()+"' OR CODY ='"+ text.toUpperCase()+"' ) ";
        }
        //limit="";
        Cursor COLS = db.rawQuery("SELECT CONSUMERID,CODY,ZONE,DIVi as DIV,SUBDIV,REV_SUBDIV,TITLE,CONSFNAME as NAME,GENDER,ADD1,ADD2,ADD3,EMAIL_ID,MOBILE_NO,strftime( '%d/%m/%Y',INSTALL_DT) as 'INSTALLATION DATE',NOFLAT as FLATS,SEWARAGE,MIN_UNIT ,VILLAGE,STATE,DISTRICT,TALUKA,CONSTITUENCY,TAP_FACTOR,MF_FACTOR FROM masterdetails  WHERE ZONE=? AND POS_SERIAL_NO=? "+find+" ORDER BY CODY ASC "+limit, new String[]{ZONE, IMEI});//
        Log.e("masterdetails", "SELECT CONSUMERID,CODY,ZONE,DIVi as DIV,SUBDIV,REV_SUBDIV,TITLE,CONSFNAME as NAME,GENDER,ADD1,ADD2,ADD3,EMAIL_ID,MOBILE_NO,strftime( '%d/%m/%Y',INSTALL_DT) as 'INSTALLATION DATE',NOFLAT as FLATS,SEWARAGE,MIN_UNIT ,VILLAGE,STATE,DISTRICT,TALUKA,CONSTITUENCY,TAP_FACTOR,MF_FACTOR FROM masterdetails  WHERE ZONE="+ZONE+" AND POS_SERIAL_NO="+IMEI+" "+find+" ORDER BY CODY ASC "+limit);
        return COLS;
    }

    public int VERSION_INSTALLED(String VERSION,String serial_no)
    {
        ContentValues version_values = new ContentValues();
        version_values.put("installed",1);

        ContentValues pwd_user_values = new ContentValues();
        pwd_user_values.put("version",VERSION);
        pwd_user_values.put("POS_UP_BIT","1");

        Cursor checkentry = db.rawQuery("SELECT * FROM app_version_updates WHERE version = ?  AND status=? AND installed = ? ORDER BY released_on DESC LIMIT 1", new String[]{VERSION,"1","0"});
        int updateresult = 0;
        if (checkentry.getCount() > 0)
        {
            updateresult = db.update("app_version_updates", version_values,"version=? AND status=? AND installed = ?" , new String[]{VERSION,"1","0"});
            if (updateresult >= 1) {
                //commentedLog.e("app_version" + " UPDATE1", "SUCCESSFUL");
                try {
                    updateresult = db.update("pwd_user", pwd_user_values, "serial_no=?", new String[]{serial_no});

                    //commentedLog.e("app_version", "updateresult" + updateresult);
                    if (updateresult >= 1) {
                        //commentedLog.e("app_version" + " UPDATE", "SUCCESSFUL");

                        return 1;
                    }
                }catch (SQLException e)
                {
                    //commentedLog.e("app_version" + " UPDATE", "SQLException"+e.getMessage());
                }
            } else {
                //commentedLog.e("app_version" + " UPDATE", "FAILED" );
                return -1;
            }
        }
        return -1;
    }

    public Cursor ledgerview(String text,String limit,Date PERDAY1, String ZONE, String IMEI) {
        SQLiteDatabase db = getWritableDatabase();
        String find="";
        if(!"".equals(text))
        {
            find = " AND ( CONSUMERID='"+ text.toUpperCase()+"' OR CODY ='"+ text.toUpperCase()+"' ) ";
        }
        //limit = "";
        Cursor COLS = db.rawQuery("SELECT CONSUMERID,CODY,BILLNO as 'BILL NO', strftime( '%d/%m/%Y',TEMP_ISSDATE) as 'ISSUE DATE', strftime( '%d/%m/%Y',TEMP_LASTDATE) as 'LAST DATE', DIV_NM as DIV, SUBDIV as SUB, REV_SUBDIV as REVENUE,   ZONE, TITLE, NAME, ADD1, ADD2, ADD3, CATEGORY, BILLED_AS_CATEGORY, NOFLAT as FLATS, SEWARAGE, strftime( '%d/%m/%Y',SEWARAGE_DATE) as 'SEWERAGE DATE', METER_NO,NEWAVG as AVG, MIN_UNIT as 'MIN UNITS', METER_READ as PREVIOUS, CUR_READ as CURRENT, FCODE as FAULT, USED as 'UNITS USED', USED1 as 'MONTHLY',  ADDUNIT as 'ADD UNITS', EXUNIT as 'EXTRA UNITS', strftime( '%d/%m/%Y',DATE1) as 'FROM DATE', strftime( '%d/%m/%Y',DATE2) as 'TO DATE', DAYS, WC as 'WATER CHARGES', METRATE as 'METER RENT', SUNDRY, SAMT as 'SEWARAGE AMOUNT', ADDCHARGE, TOTAL, ARREARS, DC as 'CREDIT', NET, BASE FROM ledgerdata  WHERE ZONE=? AND POS_SERIAL_NO=? AND PERDAY1=? "+find+" ORDER BY BILLNO DESC "+limit, new String[]{ZONE, IMEI, DB_FORMAT(PERDAY1)});//
        Log.e("masterdetails", "SELECT BILLNO as 'BILL NO', strftime( '%d/%m/%Y',TEMP_ISSDATE) as 'ISSUE DATE', strftime( '%d/%m/%Y',TEMP_LASTDATE) as 'LAST DATE', DIV_NM as DIV, SUBDIV as SUB, REV_SUBDIV as REVENUE, CONSUMERID, CODY, ZONE, TITLE, NAME, ADD1, ADD2, ADD3, CATEGORY, BILLED_AS_CATEGORY, NOFLAT as FLATS, SEWARAGE, strftime( '%d/%m/%Y',SEWARAGE_DATE) as 'SEWERAGE DATE', METER_NO,NEWAVG as AVG, MIN_UNIT as 'MIN UNITS', METER_READ as PREVIOUS, CUR_READ as CURRENT, FCODE as FAULT, USED as 'UNITS USED', USED1 as 'MONTHLY',  ADDUNIT as 'ADD UNITS', EXUNIT as 'EXTRA UNITS', strftime( '%d/%m/%Y',DATE1) as 'FROM DATE', strftime( '%d/%m/%Y',DATE2) as 'TO DATE', DAYS, WC as 'WATER CHARGES', METRATE as 'METER RENT', SUNDRY, SAMT as 'SEWARAGE AMOUNT', ADDCHARGE, TOTAL, ARREARS, DC as 'CREDIT', NET, BASE FROM ledgerdata  WHERE ZONE="+ZONE+" AND POS_SERIAL_NO="+IMEI+" AND PERDAY1="+DB_FORMAT(PERDAY1)+" "+find+" ORDER BY BILLNO DESC"+limit);
        return COLS;
    }


    public int masterdetails_count(String ZONE, String IMEI) {
        SQLiteDatabase db = getWritableDatabase();
        int count=0;

        Cursor COLS = db.rawQuery("SELECT CONSUMERID,CODY,ZONE,DIVi as DIV,SUBDIV,REV_SUBDIV,TITLE,CONSFNAME as NAME,GENDER,ADD1,ADD2,ADD3,EMAIL_ID,MOBILE_NO,strftime( '%d/%m/%Y',INSTALL_DT) as 'INSTALLATION DATE',NOFLAT as FLATS,SEWARAGE,MIN_UNIT ,VILLAGE,STATE,DISTRICT,TALUKA,CONSTITUENCY,TAP_FACTOR,MF_FACTOR FROM masterdetails  WHERE ZONE=? AND POS_SERIAL_NO=?  ORDER BY CODY ASC ", new String[]{ZONE, IMEI});//
        count = COLS.getCount();
        //commentedLog.e("masterdetails", "SELECT CONSUMERID,CODY,ZONE,DIVi as DIV,SUBDIV,REV_SUBDIV,TITLE,CONSFNAME as NAME,GENDER,ADD1,ADD2,ADD3,EMAIL_ID,MOBILE_NO,strftime( '%d/%m/%Y',INSTALL_DT) as 'INSTALLATION DATE',NOFLAT as FLATS,SEWARAGE,MIN_UNIT ,VILLAGE,STATE,DISTRICT,TALUKA,CONSTITUENCY,TAP_FACTOR,MF_FACTOR FROM masterdetails  WHERE ZONE="+ZONE+" AND POS_SERIAL_NO ORDER BY CODY ASC ");
        return count;
    }


    public int ledger_count(String ZONE, String IMEI) {
        SQLiteDatabase db = getWritableDatabase();
        int count=0;

        Cursor COLS = db.rawQuery("SELECT CONSUMERID,CODY,ZONE,DIVi as DIV,SUBDIV,REV_SUBDIV,TITLE,CONSFNAME as NAME,GENDER,ADD1,ADD2,ADD3,EMAIL_ID,MOBILE_NO,strftime( '%d/%m/%Y',INSTALL_DT) as 'INSTALLATION DATE',NOFLAT as FLATS,SEWARAGE,MIN_UNIT ,VILLAGE,STATE,DISTRICT,TALUKA,CONSTITUENCY,TAP_FACTOR,MF_FACTOR FROM masterdetails  WHERE ZONE=? AND POS_SERIAL_NO=?  ORDER BY CODY ASC ", new String[]{ZONE, IMEI});//
        count = COLS.getCount();
        //commentedLog.e("masterdetails", "SELECT CONSUMERID,CODY,ZONE,DIVi as DIV,SUBDIV,REV_SUBDIV,TITLE,CONSFNAME as NAME,GENDER,ADD1,ADD2,ADD3,EMAIL_ID,MOBILE_NO,strftime( '%d/%m/%Y',INSTALL_DT) as 'INSTALLATION DATE',NOFLAT as FLATS,SEWARAGE,MIN_UNIT ,VILLAGE,STATE,DISTRICT,TALUKA,CONSTITUENCY,TAP_FACTOR,MF_FACTOR FROM masterdetails  WHERE ZONE="+ZONE+" AND POS_SERIAL_NO ORDER BY CODY ASC ");
        return count;
    }

    public Cursor LEDGERDATA(String CONSUMERID) {
        SQLiteDatabase db = getWritableDatabase();// AND TEMP_ISSDATE=?
        Cursor COLS = db.rawQuery("SELECT * FROM ledgerdata  WHERE CONSUMERID=? ORDER BY PERDAY1 DESC", new String[]{CONSUMERID});//
        Log.e("LEDGERDATA", "FINISHED");
        return COLS;
    }

    public Cursor getvsrcody() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor COLS = db.rawQuery("SELECT * FROM vsr_idc_master", null);//
        Log.e("getvsrcody", "FINISHED");
        return COLS;
    }

    public int updatedb(String ZONE, Date PERDAY1, String ISSDATE, String LASTDATE) {
        ContentValues savedata = new ContentValues();
        savedata.put("ISSUE_DATE", ISSDATE);
        savedata.put("LASTDATE", LASTDATE);
        savedata.put("PERDAY1", DB_FORMAT(PERDAY1));
        savedata.put("ZONE", ZONE);

        SQLiteDatabase db = getWritableDatabase();
        int updateresult = 0;
        Cursor checkentry = db.rawQuery("SELECT * FROM issue_date WHERE ZONE=? AND PERDAY1 = ? ", new String[]{ZONE, DB_FORMAT(PERDAY1)});
        if (checkentry.getCount() == 1) {
            updateresult = db.update("issue_date", savedata, "ZONE = ? AND PERDAY1 = ?", new String[]{ZONE, DB_FORMAT(PERDAY1)});
            if (updateresult == 1) {
                Log.e("ISSDATE UPDATE", "SUCCESSFUL");
            } else {
                Log.e("LEDGER UPDATE", "FAILED");
            }

        } else if (checkentry.getCount() == 0) {
            updateresult = (int) db.insert("issue_date", null, savedata);
            if (updateresult != -1) {
                Log.e("LEDGER INSERT", "SUCCESSFUL");
            } else {
                Log.e("LEDGER INSERT", "FAILED");
            }
        } else {
            Log.e("LEDGER ERROR", "DUPLICATE RECORDS.COUNT" + (checkentry.getCount()));
        }
        checkentry.close();
        return updateresult;
    }

    public int UPDATE_BILL_DATA(String CONSUMERID, String PERDAY1, String BILLNO) {
        ContentValues savedata = new ContentValues();
        savedata.put("BILLNO", BILLNO);
        savedata.put("POS_UP_BIT", 1);

        Log.e("BILLNO", BILLNO);
        SQLiteDatabase db = getWritableDatabase();

        int updateresult = 0;
        updateresult = db.update("ledgerdata", savedata, "CONSUMERID = ? AND PERDAY1 = ?", new String[]{CONSUMERID, PERDAY1});
        if (updateresult > 0)
        {
            Log.e("LEDGER UPDATE", "SUCCESSFUL");
        } else
        {
            Log.e("LEDGER UPDATE", "FAILED");
        }

        return updateresult;
    }


    public int INSERT_SCANNED_BILL(String CONSUMERID, String TEMP_ISSDATE) {
        String NET = "";
        String CODY = "";
        String WC = "";
        String METRATE = "";
        String ARREARS = "";
        String DIV = "";
        String SUBDIV = "";
        String BILLNO = "";
        ContentValues savedata = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        int updateresult = 0;
        Cursor led = db.rawQuery("SELECT * FROM ledgerdata  WHERE CONSUMERID=? AND TEMP_ISSDATE=?", new String[]{CONSUMERID, DB_FORMAT(DATEFROMSTRING2(TEMP_ISSDATE))});
        if (led.getCount() > 0) {
            Log.e("ledger", "count" + led.getCount());
            while (led.moveToNext()) {
                NET = led.getString(led.getColumnIndex("NET"));
                CODY = led.getString(led.getColumnIndex("CODY"));
                WC = led.getString(led.getColumnIndex("WC"));
                METRATE = led.getString(led.getColumnIndex("METRATE"));
                ARREARS = led.getString(led.getColumnIndex("ARREARS"));
                DIV = led.getString(led.getColumnIndex("DIV_NM"));
                SUBDIV = led.getString(led.getColumnIndex("SUBDIV"));
                BILLNO = led.getString(led.getColumnIndex("BILLNO"));
                savedata.put("CONSUMERID", CONSUMERID);
                savedata.put("Issue_Date", TEMP_ISSDATE);
                savedata.put("RAMT", NET);
                savedata.put("RAMT", WC);
                savedata.put("METER_RENT", METRATE);
                savedata.put("ARREAS", ARREARS);
                savedata.put("divi", DIV);
                savedata.put("subdiv", SUBDIV);
                savedata.put("BILLNO", BILLNO);
                int insertresult = (int) db.insert("scan_online_recipt", null, savedata);
                if (insertresult != -1) {
                    Log.e("scan_online_recipt", "INSERT SUCCESSFUL");
                } else {
                    Log.e("scan_online_recipt", "INSERT FAILED");
                }
            }

        } else {
            Log.e("SCAN BILL", "BILL ALREADY SCANNED");
        }
        led.close();

        return 1;
    }

    public int UPDATEPRINTERCOUNT(String CONSUMERID, String PERDAY1, String BILLNO, String PRINTER_SERIALNO, String IMEI) {
        ContentValues savedata = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        int updateresult = 0;
        Cursor checkentry = db.rawQuery("SELECT * FROM printer_usage WHERE SERIAL_NO=? AND POS_SERIAL_NO =?", new String[]{PRINTER_SERIALNO, IMEI});
        if (checkentry.getCount() > 0) {
            int print_count = 0;
            while (checkentry.moveToNext()) {
                print_count = checkentry.getInt(checkentry.getColumnIndex("PRINT_COUNT"));
            }
            print_count++;
            savedata.put("PRINT_COUNT", print_count);
            savedata.put("POS_UP_BIT", "1");

            updateresult = db.update("printer_usage", savedata, "SERIAL_NO = ? AND POS_SERIAL_NO =?", new String[]{PRINTER_SERIALNO, IMEI});
            if (updateresult == 1) {
                Log.e("PRINTER UPDATE", "SUCCESSFUL");
            } else {
                Log.e("PRINTER UPDATE", "FAILED");
            }

        } else {
            savedata.put("SERIAL_NO", PRINTER_SERIALNO);
            savedata.put("POS_SERIAL_NO", IMEI);
            savedata.put("PRINT_COUNT", "1");
            savedata.put("POS_UP_BIT", "1");
            updateresult = (int) db.insert("printer_usage", null, savedata);
            if (updateresult != -1) {
                Log.e("PRINTER INSERT", "SUCCESSFUL");
            } else {
                Log.e("PRINTER INSERT", "FAILED");
            }
        }
        checkentry.close();

        Cursor checkled = db.rawQuery("SELECT * FROM ledgerdata WHERE CONSUMERID = ? AND PERDAY1 = ?", new String[]{CONSUMERID, PERDAY1});
        if (checkled.getCount() > 0) {
            int BILL_ISSUED = 0;
            while (checkled.moveToNext()) {
                BILL_ISSUED = checkled.getInt(checkled.getColumnIndex("BILL_ISSUED"));
            }
            BILL_ISSUED++;

            ContentValues saveledger = new ContentValues();
            saveledger.put("BILL_ISSUED", BILL_ISSUED);
            int updateled = 0;
            updateled = db.update("ledgerdata", saveledger, "CONSUMERID = ? AND PERDAY1 = ?", new String[]{CONSUMERID, PERDAY1});
            if (updateled == 1) {
                Log.e("LEDGER UPDATE", "BILL_ISSUED SUCCESSFUL");
            } else {
                Log.e("LEDGER UPDATE", "BILL_ISSUED FAILED");
            }

        }
        checkled.close();

        return 1;
    }

    public Cursor LEDGERTOUPLOAD(String QUERY) {
        Log.e("LEDGERTOUPLOAD", "CALLED");
        SQLiteDatabase db = getWritableDatabase();
        Cursor led = db.rawQuery(QUERY, new String[]{});
        Log.e("UPLOADQUERY", QUERY);

        return led;
    }

    public Object sqltxt(Object str) {
        if (str == "" || str == null) {
            str = null;
            return str;
        } else {
            str = str.toString().trim();
            str = "'" + str.toString().replace("'", "''") + "'";
            return str.toString().trim();
        }
    }

    /*public static void SHOWMSG(String message, String title, Context context) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                //.setIcon(R.drawable.cam)
                .setMessage(message)
                //.setView(zonechange)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }*/


    public static void SHOWMSG(String type,String message, String title, Context context) {
        String versionname = "";
        COMFUNCTIONS cm =new COMFUNCTIONS();

        String IMEI = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        SharedPreferences sharedpreferences = context.getSharedPreferences("LOGINCREDENTIAL", Context.MODE_PRIVATE);

        String USERNAME = sharedpreferences.getString("USERNAME",null);
        if(USERNAME == null)
        {
            USERNAME = "";
        }

        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            versionname = pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.bottom_msg_box);
        Activity activity = (Activity) context;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if(activity.getCurrentFocus() != null)
        {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
        TextView mr = bottomSheetDialog.findViewById(R.id.mr);
        TextView psn = bottomSheetDialog.findViewById(R.id.imei);
        TextView bmb_version = bottomSheetDialog.findViewById(R.id.bmb_version);
        TextView msgbox_title = bottomSheetDialog.findViewById(R.id.msgbox_title);
        TextView msgbox_message = bottomSheetDialog.findViewById(R.id.msgbox_message);
        ImageButton cancel_button = bottomSheetDialog.findViewById(R.id.cancel_button);
        TextView timeview = bottomSheetDialog.findViewById(R.id.timeview);
        ImageView share_report = bottomSheetDialog.findViewById(R.id.share_report);
        String finalUSERNAME = USERNAME;

        msgbox_title.setText(title);
        msgbox_message.setText(message);

        share_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("share_report",title+"-"+message);
                syncdb syd = new syncdb();
                syd.SHARE_LOG(context,"share_report"+title+"\n"+message,IMEI, finalUSERNAME);
            }
        });



        bmb_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View rootView = bmb_version.getRootView();
                cm.getScreenShot(rootView,context, finalUSERNAME,IMEI);
            }
        });

        mr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
                cm.getScreenShot(rootView,context, finalUSERNAME,IMEI);
            }
        });

        mr.setText(USERNAME);
        psn.setText(IMEI);
        bmb_version.setText(versionname);
        timeview.setText(DB_DATETIME_FORMAT_DISPLAY_STATIC(new Date(System.currentTimeMillis())));
        if("error".equals(type))
        {
            msgbox_title.setBackgroundColor(Color.RED);
        }else if("success".equals(type))
        {
            msgbox_title.setBackgroundColor(Color.GREEN);
        }else
        {
            msgbox_title.setBackgroundColor(Color.WHITE);
        }

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!bottomSheetDialog.isShowing())
                {
                    bottomSheetDialog.dismiss();

                }
                Log.e("bottomSheetDialog","status "+bottomSheetDialog.isShowing()+" "+bottomSheetDialog);
                bottomSheetDialog.hide();

            }
        });
        if (!((Activity) context).isFinishing())
        {
            bottomSheetDialog.show();
        }

    }

    public static void SendLogcatMail(String message,Context context){

        // save logcat in file
        File outputFile = new File(Environment.getExternalStorageDirectory(),
                "logcat.txt");
        try {
            Runtime.getRuntime().exec(
                    "logcat -f " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //send file using email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // Set type to "email"
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"pundalikbhagat73@gmail.com"};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        //emailIntent .putExtra(Intent.EXTRA_STREAM, outputFile.getAbsolutePath());
        // the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Subject - ERROR LOG"+message);
        context.startActivity(Intent.createChooser(emailIntent , "Send email..."+message));
    }


    public static String DB_DATETIME_FORMAT_DISPLAY_STATIC(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String DATEFORMAT = "";
        DATEFORMAT = sdf.format(date);
        return DATEFORMAT;
    }

    public static void SHOWMSG_ICON(int icon, String message, String title, Context context) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(icon)
                .setMessage(message)
                //.setView(zonechange)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public long INSERT_SYNC(ContentValues data) {
        Log.e("pwd_sync", "CALLED");

        SQLiteDatabase db = getWritableDatabase();
        long insertresult = 0;
        try {
            insertresult = db.insertOrThrow("pwd_sync", null, data);
            if (insertresult != -1) {
                Log.e("pwd_sync", "SUCCESSFUL");
            } else {
                Log.e("pwd_sync", "FAILED");
            }
        } catch (SQLException e) {
            Log.e("pwd_sync", e.toString());
        }

        return insertresult;
    }

    public String update_masters(Statement statement, Connection connection, String tablename, String key) {
        Log.d("calling ", "IN update_masters");

        String ret = "";
        String keyvalue = "";
        int total = 0;
        int inserted = 0;
        int updated = 0;
        int failed = 0;
        try {

            Log.d("tablename", tablename + " key" + key);

            ResultSet rs = statement.executeQuery("SELECT * FROM " + tablename);

            rs.last();

            total = rs.getRow();

            rs.beforeFirst();

            int iterator = 0;
            ContentValues master = new ContentValues();
            SQLiteDatabase db = getWritableDatabase();

            if (total > 0) {
                ResultSetMetaData colnames = rs.getMetaData();
                Log.d("masterdetails colnames", String.valueOf(colnames.getColumnCount()));
                while (rs.next()) {

                    Log.d("calling ", "IN update_masters loop");
                    iterator++;

                    master.clear();

                    keyvalue = rs.getString(key);

                    for (int k = 1; k <= colnames.getColumnCount(); k++) {
                        Log.d("keyvalue", colnames.getColumnName(k) + " " + sqltxt(rs.getString(k)));
                        master.put(colnames.getColumnName(k), rs.getString(k));

                    }

                    Cursor checkentry = db.rawQuery("SELECT * FROM " + tablename + " WHERE " + key + "=?", new String[]{keyvalue});
                    int updateresult = 0;
                    if (checkentry.getCount() == 1) {
                        updateresult = db.update(tablename, master, key + " = ?", new String[]{keyvalue});
                        if (updateresult >= 1) {
                            updated += 1;
                            Log.e(tablename + " UPDATE", "SUCCESSFUL" + iterator);
                        } else {
                            failed += 1;
                            Log.e(tablename + " UPDATE", "FAILED" + iterator);
                        }

                    } else if (checkentry.getCount() == 0) {
                        updateresult = (int) db.insert(tablename, null, master);
                        if (updateresult != -1) {

                            inserted += 1;
                            Log.e(tablename + " INSERT", "SUCCESSFUL" + iterator);

                        } else {
                            failed += 1;
                            Log.e(tablename + " INSERT", "FAILED" + iterator);
                        }
                    }
                    checkentry.close();

                }
                Log.e("TOTAL", total + " UPDATED" + updated + " INSERTED" + inserted + " FAILED" + failed);
            }
        } catch (Exception e) {
            Log.d("RESULTMYSQL exception", e.toString());
        }
        ret = "TOTAL" + total + " UPDATED" + updated + " INSERTED" + inserted + " FAILED" + failed;
        return ret;
    }

    public Cursor GET_DOWNLOADABLE_TABLES(String op_type) {
        String table_type = "";

        Log.e("GET_DOWNLOADABLE_TABLES", op_type + " ");
        if ("DOWNLOAD".equals(op_type)) {
            table_type = "WHERE  table_type = '1' OR table_type = '2'";
        } else if ("UPLOAD".equals(op_type)) {
            table_type = "WHERE table_type = '3' OR table_type = '2'";
        } else {
            return null;
        }

        SQLiteDatabase db = getWritableDatabase();
        Cursor led = db.rawQuery("SELECT tablename FROM column_constant_updated  " + table_type, new String[]{});
        return led;
    }

    public Cursor GET_METERPHOT_PATH() {
        String path = "";
        SQLiteDatabase db = getWritableDatabase();
        Cursor led = db.rawQuery("SELECT path FROM meter_photos  WHERE POS_UP_BIT=?", new String[]{"1"});
        return led;
    }

    public Date GETISSDATEFROMPERDAY1(Date PERDAY1, String ZONE) {
        Date ISSDATE = null;
        SQLiteDatabase db = getWritableDatabase();
        Log.e("GET ISSDATE", "SELECT ISSUE_DATE FROM issue_date  WHERE PERDAY1=?AND ZONE=?" + DB_FORMAT(PERDAY1) + ZONE);
        Cursor led = db.rawQuery("SELECT ISSUE_DATE FROM issue_date  WHERE PERDAY1=? AND ZONE=?", new String[]{DB_FORMAT(PERDAY1), ZONE});
        if (led.getCount() > 0) {
            while (led.moveToNext()) {
                ISSDATE = DATEFROMSTRING(led.getString(led.getColumnIndex("ISSUE_DATE")));
            }
        } else {
            ISSDATE = null;
        }
        led.close();
        return ISSDATE;
    }

    public Date GETISSDATEFROMLEDGER(String CONSUMERID) {
        Date ISSDATE = null;
        SQLiteDatabase db = getWritableDatabase();
        Log.e("GET ISSDATE", "SELECT ISSDATE FROM ledgerdata  WHERE CONSUMERID=?" + CONSUMERID);
        Cursor led = db.rawQuery("SELECT ISSDATE FROM ledgerdata  WHERE CONSUMERID=?", new String[]{CONSUMERID});
        if (led.getCount() > 0) {
            while (led.moveToNext()) {
                ISSDATE = DATEFROMSTRING(led.getString(led.getColumnIndex("ISSDATE")));
            }
        } else {
            ISSDATE = null;
        }
        led.close();
        return ISSDATE;
    }

    public boolean SAVE_USER(String IMEI, String LOGINID, String USERNAME, String PASSWORD) {
        Date datetime = new Date(System.currentTimeMillis());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put("user", LOGINID);
        content.put("username", USERNAME);
        content.put("password", PASSWORD);
        content.put("status", 0);
        content.put("serial_no", IMEI);
        content.put("created_by", LOGINID);
        content.put("created_at", DB_DATETIME_FORMAT(datetime));
        content.put("POS_UP_BIT", 1);
        content.put("billing", 1);
        content.put("system_update", 1);
        content.put("database_update", 1);
        content.put("settings", 1);
        content.put("masters", 1);
        content.put("ledger", 1);
        content.put("meter_photo", 1);

        content.put("receipt", 0);
        content.put("bluetooth", 0);
        content.put("bill_reprint", 0);
        content.put("view_ledger",0);
        content.put("location", 0);

        content.put("internet", 0);
        content.put("time_interval", 900000);
        content.put("meter_photo_view", 0);
        content.put("bill_receipt_print", 1);
        content.put("paytm_payments", 0);
        content.put("paytm_sales", 0);
        content.put("paytm_status",0);
        content.put("paytm_cancel", 0);
        content.put("paytm_print", 0);
        content.put("paytm_delete", 0);
        content.put("paytm_edit", 0);


        long result = db.insert("pwd_user", null, content);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public ContentValues SET_RIGHTS(String username) {
        ContentValues RIGHTS = new ContentValues();

        String ERROR = "";
        try {

            SQLiteDatabase db = getWritableDatabase();
            ContentValues content = new ContentValues();
            Cursor cursor = db.rawQuery("SELECT * FROM pwd_user WHERE user=?", new String[]{username});

            Log.e("RIGHTS","SELECT * FROM pwd_user WHERE user="+username);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    for (int i = 0; i < cursor.getColumnCount(); i++) {

                        Log.e("RIGHTS",cursor.getColumnName(i)+" "+cursor.getString(i));
                        RIGHTS.put(cursor.getColumnName(i), cursor.getString(i));
                    }

                }

            } else {
                ERROR = "USER DOESNOT EXISTS.PLEASE GET REGISTERED";
            }
            cursor.close();
        } catch (SQLiteException e) {
            Log.d("SQLiteException", e.toString());

            ERROR = "ERROR OCCURED WHILE SETTING RIGHTS" + e.toString();
        }

        RIGHTS.put("ERROR", ERROR);

        return RIGHTS;

    }




    public String get_const(String ZONE) {
        String CONSTITUENCY = "";
        String ZONECODE = "";
        SQLiteDatabase db = getWritableDatabase();
        Cursor led = db.rawQuery("SELECT CONSTITUENCY,ZONECODE FROM zone  WHERE ZONE=?", new String[]{ZONE});
        if (led.getCount() > 0) {
            while (led.moveToNext()) {
                CONSTITUENCY = led.getString(led.getColumnIndex("CONSTITUENCY"));
                ZONECODE = led.getString(led.getColumnIndex("ZONECODE"));

                CONSTITUENCY = CONSTITUENCY + "-" + ZONECODE;
            }
        } else {
            CONSTITUENCY = "";
        }
        led.close();
        return CONSTITUENCY;

    }

    public void hide_loadingview(Context context,String message)
    {
        Log.e("hide_loadingview0","context"+context);
        LinearLayout loadingview = (LinearLayout) ((Activity) context).findViewById(R.id.loadingview);
        TextView printer_status = (TextView) ((Activity) context).findViewById(R.id.printer_status);
        if(printer_status != null)
        {
            printer_status.setText(message);
        }


        if(loadingview != null)
        {
            loadingview.postDelayed(new Runnable() {
                public void run() {
                    loadingview.setVisibility(View.GONE);

                }
            }, 3000);
        }

    }

    public String todays_bills_issued(String user)
    {

        String issued_bill = "BILLS ISSUED TODAY : ";
        SQLiteDatabase db = getWritableDatabase();
        Date datetime = new Date(System.currentTimeMillis());
        Cursor led = db.rawQuery("SELECT COUNT(*) as issued_bill FROM ledgerdata WHERE DATE(generated_at)=? AND generated_by=?", new String[]{DB_FORMAT(datetime),user});
        if (led.getCount() > 0)
        {
            while (led.moveToNext())
            {
                issued_bill = led.getString(led.getColumnIndex("issued_bill"));
            }
        } else
        {
            issued_bill += "0";
        }
        led.close();
        return issued_bill;

    }

    public String get_zone(String IMEI) {
        String ZONE = "";
        SQLiteDatabase db = getWritableDatabase();
        Cursor led = db.rawQuery("SELECT DISTINCT(ZONE) FROM ledgerdata  WHERE POS_SERIAL_NO=? ORDER BY ZONE ASC LIMIT 1", new String[]{IMEI});
        if (led.getCount() > 0) {
            while (led.moveToNext()) {
                ZONE = led.getString(led.getColumnIndex("ZONE"));
            }
        } else {
            ZONE = "";
        }
        led.close();
        return ZONE;

    }

    public String UPDATE_USERSTAT(String IMEI, String user, String versionname) {
        String response = "";
        int updateresult = 0;
        SQLiteDatabase db = getWritableDatabase();

        ContentValues savedata = new ContentValues();
        savedata.put("version", versionname);
        savedata.put("status", 1);
        Log.e("SELECT QUERY", "SELECT * FROM pwd_user WHERE username=? AND serial_no=? AND status=? AND version <> ?" + user + " " + IMEI + " " + "3" + " " + versionname);
        Cursor led = db.rawQuery("SELECT * FROM pwd_user WHERE username=? AND serial_no=?", new String[]{user, IMEI});
        if (led.getCount() > 0) {
            updateresult = db.update("pwd_user", savedata, "username=? AND serial_no=? ", new String[]{user, IMEI});
            if (updateresult == 1) {
                Log.e("pwd_user UPDATE", "SUCCESSFUL");
                response = "USER UPDATE SUCCESSFUL";
            } else {
                Log.e("pwd_user UPDATE", "FAILED");
                response = "USER UPDATE FAILED";
            }
        } else {
            response = "";
        }
        led.close();
        return response;

    }

    public Cursor issue_date_list(String IMEI) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor led = db.rawQuery("SELECT ZONE,ISSUE_DATE as ISSUEDATE,MAX_ISSUE_DATE as CLOSINGDATE FROM issue_date  WHERE SERIAL_NO=? order by ISSUE_DATE,ZONE DESC", new String[]{IMEI});
        /*if (led.getCount() > 0),MAX(PERDAY1) as FROMDATE
        {
            while (led.moveToNext())
            {
                ZONE = led.getString(led.getColumnIndex("ZONE"));
                String PERDAY1 = led.getString(led.getColumnIndex("PERDAY1"));
                String ISSUE_DATE = led.getString(led.getColumnIndex("ISSUE_DATE"));
                String MAX_ISSUE_DATE = led.getString(led.getColumnIndex("MAX_ISSUE_DATE"));

                arrlist.add(ZONE+" - "+PERDAY1+" - "+ISSUE_DATE+" - "+MAX_ISSUE_DATE);
            }
        }else
        {
            ZONE = "";
        }*/
        //led.close();

        return led;

    }

    public ContentValues SOFTCONFIG(String type) {
        ContentValues configs = new ContentValues();
        type = "MSS";
        String param = "";
        if ("".equals(type)) {
            param = "name = 'PHE' AND ";
        } else {
            param = "name = '" + type + "' AND ";
        }

        SQLiteDatabase db = getWritableDatabase();
        Cursor led = db.rawQuery("SELECT * FROM softconfig WHERE " + param + " status=?", new String[]{"1"});
        if (led.getCount() > 0) {
            while (led.moveToNext()) {
                String name = led.getString(led.getColumnIndex("name"));
                String dbname = led.getString(led.getColumnIndex("dbname"));
                String ip = led.getString(led.getColumnIndex("ip"));
                String user = led.getString(led.getColumnIndex("user"));
                String password = led.getString(led.getColumnIndex("password"));
                configs.put("name", name);
                configs.put("dbname", dbname);
                configs.put("ip", ip);
                configs.put("user", user);
                configs.put("password", password);

            }
        }
        led.close();
        return configs;

    }

    public ContentValues VERSION_UPDATE(String VERSION)
    {
        ContentValues version_values = new ContentValues();
        String version = "NA";
        String compulsory = "";
        double ver = 0;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM app_version_updates WHERE version > ?  AND status=? ORDER BY released_on DESC LIMIT 1", new String[]{VERSION,"1"});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext())
            {
                version = cursor.getString(cursor.getColumnIndex("version"));
                String released_on = cursor.getString(cursor.getColumnIndex("released_on"));
                compulsory = cursor.getString(cursor.getColumnIndex("compulsory"));
                if(!"".equals(version) && version != null)
                {
                    version =  "LATEST UPDATE AVAILABLE(VER "+version+") RELEASED ON DATE "+DISPLAY_DATETIMEFORMAT(released_on);
                }else
                {
                    version = "NA";
                }
            }
        } else {
            //commentedLog.e("ERROR", "latest_act NOT FOUND");
            version = "NA";
        }
        version_values.put("version",version);
        version_values.put("compulsory",compulsory);
        return version_values;
    }

    public ContentValues notifications(String type, String IMEI, String form_name) {
        ContentValues configs = new ContentValues();
        String param = "";
        String message = "";
        String from_date = "";
        String to_date = "";
        String error = "";
        if (!"".equals(IMEI.trim())) {
            param = "( POS_SERIAL_NO = 'ALL' OR POS_SERIAL_NO = '" + IMEI.trim() + "' ) AND ";
        } else {
            param = " POS_SERIAL_NO = 'ALL'  AND ";
        }

        if (!"".equals(form_name.trim())) {
            param += "form_name = '" + form_name.trim() + "' AND ";
        }
        try {
            Log.d("query", "SELECT * FROM notifications WHERE type ='" + type + "' AND " + param + " status=?");
            SQLiteDatabase db = getWritableDatabase();
            Cursor led = db.rawQuery("SELECT * FROM notifications WHERE type =? AND " + param + " status=?", new String[]{type, "1"});
            if (led.getCount() > 0) {
                while (led.moveToNext()) {
                    message = led.getString(led.getColumnIndex("message"));
                    from_date = led.getString(led.getColumnIndex("from_date"));
                    to_date = led.getString(led.getColumnIndex("to_date"));
                    error = "";


                }
            } else {
                message = "";
                from_date = "";
                to_date = "";
                error = "";
            }
            led.close();

        } catch (SQLiteException e) {
            Log.d("SQLiteException", e.toString());

            error = "ERROR OCCURED WHILE RETRIEVING NOTIFICATIONS " + e.toString();
        }

        configs.put("message", message);
        configs.put("from_date", from_date);
        configs.put("to_date", to_date);
        configs.put("error", error);

        return configs;
    }

    public ContentValues qrcode_generation() {
        ContentValues qr_data = new ContentValues();
        String error = "";

        String account_name="";
        String account_id="";
        String seperator="";
        String mc="";
        String cu="";
        int display=0;
        String status="";
        int dimension=0;
        String start_date="";
        String end_date="";
        String print_location="";

        try {
            Log.d("qr_code query", "SELECT * FROM qr_code");
            SQLiteDatabase db = getWritableDatabase();
            Cursor led = db.rawQuery("SELECT * FROM qr_code", new String[]{});
            if (led.getCount() > 0) {
                while (led.moveToNext()) {
                    account_name = led.getString(led.getColumnIndex("account_name"));
                    account_id = led.getString(led.getColumnIndex("account_id"));
                    seperator = led.getString(led.getColumnIndex("seperator"));
                    mc = led.getString(led.getColumnIndex("mc"));
                    cu = led.getString(led.getColumnIndex("cu"));
                    display = led.getInt(led.getColumnIndex("display"));
                    status = led.getString(led.getColumnIndex("status"));
                    dimension = led.getInt(led.getColumnIndex("dimension"));
                    start_date = led.getString(led.getColumnIndex("start_date"));
                    end_date = led.getString(led.getColumnIndex("end_date"));
                    print_location = led.getString(led.getColumnIndex("print_location"));
                    error = "";


                }
            } else {
                account_name = "";
                account_id = "";
                seperator = "";
                mc = "";
                cu = "";
                display = 0;
                status = "";
                dimension = 0;
                start_date = "";
                end_date = "";
                print_location = "";
                error = "NO RECORDS";
            }
            led.close();

        } catch (SQLiteException e) {
            error = "ERROR OCCURED WHILE RETRIEVING QR CODE DETAILS " + e.toString();
        }

        qr_data.put("account_name", account_name);
        qr_data.put("account_id", account_id);
        qr_data.put("seperator", seperator);
        qr_data.put("mc", mc);
        qr_data.put("cu", cu);
        qr_data.put("display", display);
        qr_data.put("status", status);
        qr_data.put("dimension", dimension);
        qr_data.put("start_date", start_date);
        qr_data.put("end_date", end_date);
        qr_data.put("print_location", print_location);
        qr_data.put("error", error);


        return qr_data;
    }


    String[] GENERATE_RECEIPT_NO(Connection con,String IMEI)
    {
        String BILLCODE = "";
        String oldrecptno = "";
        String RECEIPTNO = "";
        String POS_ID = "";
        String[] response = new String[2];

        SQLiteDatabase db = getWritableDatabase();
        try {
                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("SELECT POS_ID FROM device_details WHERE POS_SERIAL_NO = '"+IMEI+"' AND STATUS = '1'");
                Log.e("device_details", "SELECT POS_ID FROM device_details WHERE POS_SERIAL_NO = '"+IMEI+"' AND STATUS = '1'");
                rs.last();
                int totalrows = rs.getRow();
                rs.beforeFirst();
                Log.e("totalrows", " " + totalrows);
                if (totalrows > 0) {
                    while (rs.next()) {
                        POS_ID = rs.getString("POS_ID");
                        Log.e("POS_ID", POS_ID);
                    }
                }else
                {
                    response[1] = "Error : This device is not registered for accepting payment.Please contact administrator.";
                    return response;
                }
                if (!"".equals(POS_ID))
                {
                    ResultSet rsnewrv;
                    rsnewrv = st.executeQuery("SELECT MAX(CAST(RECEIPT AS UNSIGNED)) AS oldrecptno FROM newrevenue WHERE POS_ENTRY='"+IMEI+"'  LIMIT 1");
                    Log.e("newrevenue", "SELECT MAX(CAST(RECEIPT AS UNSIGNED)) AS oldrecptno FROM newrevenue WHERE POS_ENTRY='"+IMEI+"'  LIMIT 1");
                    rsnewrv.last();
                    int oldrecptnotr = rsnewrv.getRow();
                    rsnewrv.beforeFirst();
                    Log.e("oldrecptnotr", " " + oldrecptnotr);
                    if (oldrecptnotr > 0)
                    {
                        while (rsnewrv.next())
                        {
                            oldrecptno = rsnewrv.getString("oldrecptno");
                            Log.e("oldrecptno", "sad" + oldrecptno + "sad");
                            if (!"".equals(oldrecptno) && oldrecptno != null)
                            {

                                RECEIPTNO = String.valueOf(Integer.parseInt(oldrecptno) + 1);
                                RECEIPTNO = ("000000" + RECEIPTNO).substring(RECEIPTNO.length());
                                Log.e("BILLCODE ERROR", RECEIPTNO);
                            } else {
                                RECEIPTNO = POS_ID + "001";
                                Log.e("RECEIPTNO ", "1st recept "+RECEIPTNO);
                            }
                        }
                    }else
                    {
                        RECEIPTNO = POS_ID + "001";
                    }
                }
        } catch (SQLException | java.sql.SQLException e) {
            Log.e("BILLNOERROR", e.getMessage());
            response[1] = "Error : "+ e.getMessage();
            return response;
        }
        response[0] = RECEIPTNO;
        Log.e("RECEIPTNO", "FINAL "+RECEIPTNO);
        return response;

    }

    public Cursor CONSUMER_HISTORY(String CONSUMERID)
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT (select group_concat(path)  from meter_photos where meter_photos.consumer_id=ledgerdata.CONSUMERID and meter_photos.perday1=ledgerdata.PERDAY1) as photopath,ledgerdata.METER_NO,ledgerdata.REMARK,METER_READ,CUR_READ,USED,USED1,DATE1,DATE2,NET,NEXTARR,NEXTDC,FCODE FROM ledgerdata  WHERE CONSUMERID = ? ORDER BY ledgerdata.PERDAY1 DESC LIMIT 5", new String[]{CONSUMERID});
        return cursor;
    }

    double SplitReceived(double receivedamt, String column, double actamt, String ledgerid,String CONSUMERID,String REV_ISSDATE,Connection con)
    {
        double RCVFIELD = 0;

        ResultSet rs;

        try {
            Statement st = con.createStatement();
            rs = st.executeQuery("SELECT COALESCE(SUM('" + column + "'), 0) AS RSUM FROM newrevenue WHERE CONSUMERID='" + CONSUMERID + "' AND ISSDATE='" + REV_ISSDATE + "' AND (DEL_BIT='0' OR DEL_BIT IS NULL)");
            Log.e("SplitReceived", "SELECT COALESCE(SUM('" + column + "'), 0) AS RSUM FROM newrevenue WHERE CONSUMERID='" + CONSUMERID + "' AND ISSDATE='" + REV_ISSDATE + "' AND (DEL_BIT='0' OR DEL_BIT IS NULL)");
            ResultSetMetaData rsmd = rs.getMetaData();
            String result = new String();
            rs.last();
            int totalrows = rs.getRow();
            rs.beforeFirst();
            Log.e("totalrows", " " + totalrows);
            if (totalrows > 0) {
                while (rs.next()) {
                    RCVFIELD = rs.getDouble("RSUM");
                }
            }

            RCVFIELD = actamt - RCVFIELD;
            if (RCVFIELD > receivedamt) {
                RCVFIELD = receivedamt;
            }
        }catch (Exception e)
        {
            Log.e("Exception", "SplitReceived Exception " + e.getMessage());
        }

        return RCVFIELD;
    }

    int getRcode(Date lastdate, Date receipt_date, double received_amount, double net_amount)
    {
        int rcode = 0;
        if(received_amount < net_amount)
        {
            //'part payment
            if(receipt_date.compareTo(lastdate) <= 0)
            {
                //'ontime
                rcode=3;
                //return("3");
            }else
            {
                //'late
                rcode=4;
                //return("4");
            }
        }else if(received_amount == net_amount)
        {
            //'full payment
            if(receipt_date.compareTo(lastdate) <= 0 ){
                rcode=0;
                //return("0");
            } else {
                //'late
                rcode=1;
                //return("1");
            }
        }else
        {
            //'credit payment
            rcode=5;
            //return("5");
        }
        return rcode;
    }

    public String GETPAYMENTSTATUS(String CONSUMERID)
    {

        int paycount = 0;
        int COUNTER = 0;
        try {
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT l.NET,l.NEXTARR,n.RDATE,n.DEL_BIT FROM ledgerdata l left join newrevenue n on l.CONSUMERID=n.CONSUMERID AND l.TEMP_ISSDATE=n.ISSDATE WHERE l.CONSUMERID=? ORDER BY l.PERDAY1 DESC LIMIT 2", new String[]{CONSUMERID});
            // AND (n.DEL_BIT='0' OR n.DEL_BIT IS NULL) AND n.RAMT > 0
            int total_count = cursor.getCount();
            if (cursor.getCount() > 0)
            {
                int i = 1;
                while (cursor.moveToNext()) {
                    int DEL_BIT = cursor.getInt(cursor.getColumnIndex("DEL_BIT"));

                    Double NET = cursor.getDouble(cursor.getColumnIndex("NET"));
                    Double NEXTARR = cursor.getDouble(cursor.getColumnIndex("NEXTARR"));
                    String RDATE = cursor.getString(cursor.getColumnIndex("RDATE"));
                    Log.e("GETPAYMENTSTATUS",NET+" "+RDATE+" "+DEL_BIT+" NEXTARR"+NEXTARR);
                    if((!"".equals(RDATE) && RDATE!= null && (DEL_BIT==0 || "".equals(DEL_BIT))) || NEXTARR<=0 || NET<=0)
                    {
                        paycount++;
                    }
                }
                cursor.close();

            }
            String response = "";
            Log.e("GETPAYMENTSTATUS", "GETPAYMENTSTATUS " + paycount+" "+total_count);
            if(total_count>=2 && paycount==0)
            {
                response = "LAST 2 BILLS NOT PAID";
            }
            return response;
        }catch (Exception e)
        {
            Log.e("Exception", "SplitReceived Exception " + e.getMessage());
            return "SplitReceived Exception " + e.getMessage();
        }

    }

    public ContentValues ADD_EXTRA_AMOUNT(String CONSUMERID,String type)
    {
        ContentValues EA = new ContentValues();
        Double AMOUNT = 0.00;
        Double BALANCE = 0.00;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ADD_AMOUNT WHERE CONSUMERID=? AND AMT_TYPE=?", new String[]{CONSUMERID,type});
        if (cursor.getCount() > 0)
        {
            while (cursor.moveToNext()) {
                AMOUNT = cursor.getDouble(cursor.getColumnIndex("AMOUNT"));
                BALANCE = cursor.getDouble(cursor.getColumnIndex("BALANCE"));
            }
            cursor.close();
        }
        EA.put("AMOUNT",AMOUNT);
        EA.put("BALANCE",BALANCE);
        return EA;
    }

    public ContentValues CHECK_SEW_EA(Date PERDAY1_chk,String CONSUMERID_chk)
    {
        Log.e("check_previous_reading","SELECT SEW_EA,MR_EA FROM ledgerdata WHERE CONSUMERID = '"+CONSUMERID_chk+"' AND PERDAY1 = '"+PERDAY1_chk+"'");
        SQLiteDatabase db = getWritableDatabase();
        ContentValues LEDGERVAL = new ContentValues();
        String SEW_EA = "";
        String MR_EA = "";
        int success = 0;
        Cursor cursor = db.rawQuery("SELECT SEW_EA,MR_EA FROM ledgerdata WHERE CONSUMERID = ? AND PERDAY1 = ?", new String[]{CONSUMERID_chk,DB_FORMAT(PERDAY1_chk)});
        if (cursor.getCount() > 0)
        {
            while (cursor.moveToNext())
            {
                SEW_EA = cursor.getString(cursor.getColumnIndex("SEW_EA"));
                MR_EA = cursor.getString(cursor.getColumnIndex("MR_EA"));
                Log.e("SEW_EA",SEW_EA+"CHECK_SEW_EA");
                Log.e("MR_EA",MR_EA+"CHECK_SEW_EA");
            }
        }else
        {
            SEW_EA = "0";
            MR_EA = "0";
        }
        LEDGERVAL.put("SEW_EA",SEW_EA);
        LEDGERVAL.put("MR_EA",MR_EA);
        return LEDGERVAL;
    }

    public String GET_TIME_UPDATE()
    {
        String today = "";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM time_update", new String[]{});
        if (cursor.getCount() > 0)
        {
            while (cursor.moveToNext())
            {
                today = cursor.getString(cursor.getColumnIndex("today"));
            }
            cursor.close();
            return today;
        } else {
            return today;
        }
    }

    public ArrayList<String> get_pay_type(Context context, Spinner PAYFOR, String Category)
    {
        ArrayList<String> arrlist = new ArrayList<String>();
        ArrayList<String> sub_pay_arrlist = new ArrayList<String>();
        syd = new syncdb();
        try {
            Thread conthread = null;
            Handler handler = new Handler();
            conthread = new Thread(new Runnable()
            {
                public void run()
                {
                    Connection con = null;
                    try {
                        Statement st = null;


                        syd.SETCONFIG("",context);
                        String CONFIGSTR = syd.IP+"/"+syd.DBNAME;
                        con = DriverManager.getConnection("jdbc:mysql://"+CONFIGSTR, syd.DBUSER, syd.DBPASSWORD);
                        Log.d("MYSQLERROR", "MYSQLERROR3"+con.toString());

                        //con = DriverManager.getConnection("jdbc:mysql://45.113.189.69:3306/phesgoa_pwd", "phesgoa_pwduser", "PWDmss_user#6090");

                        ResultSet rs;
                        ResultSet rs1;
                        st = con.createStatement();
                        if(!"".equals(Category))
                        {
                            rs = st.executeQuery("SELECT * FROM dept_pay_type WHERE category='"+Category+"' AND sub_class='0' ORDER BY pay_type,category ASC");

                            Log.e("dept_pay_type", "SELECT * FROM dept_pay_type WHERE category='"+Category+"' AND sub_class='0'  ORDER BY pay_type,category ASC ");

                        }else
                        {
                            rs = st.executeQuery("SELECT * FROM dept_pay_type ORDER BY pay_type ASC");
                            Log.e("getreceipts", "SELECT * FROM dept_pay_type ORDER BY pay_type ASC");
                        }


                        ResultSetMetaData rsmd = rs.getMetaData();
                        String result = new String();
                        rs.last();
                        int totalrows = rs.getRow();
                        rs.beforeFirst();
                        //commentedLog.e("totalrows", " " + totalrows);

                        arrlist.add("SELECT");

                        if (totalrows > 0) {
                            while (rs.next()) {
                                String pay_type = rs.getString("pay_type");

                                String charges = rs.getString("charges");

                                if(charges == null)
                                {
                                    arrlist.add(pay_type);
                                }else
                                {
                                    arrlist.add(pay_type+"-"+charges);
                                }

                            }
                        }

                        ArrayAdapter<String> msarrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, arrlist);
                        msarrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        handler.post(new Runnable() {
                            public void run() {
                                PAYFOR.setAdapter(msarrayAdapter);

                            }
                        });

                    } catch (java.sql.SQLException e) {
                        e.printStackTrace();
                    }
                }

            });
            if ((conthread != null) && (!conthread.isAlive())) conthread.start();
        }catch (Exception e)
        {
            //commentedLog.e("Exception", "SplitReceived Exception " + e.getMessage());
        }

        return arrlist;
    }

    public ContentValues get_sub_pay_type(String Category, String pay_type, LinearLayout sub_pay_layout, Context context, TextInputEditText paidamount)
    {
        ContentValues sub_pay_arrlist = new ContentValues();
        syd = new syncdb();
        try {
            Thread conthread = null;
            Handler handler = new Handler();
            conthread = new Thread(new Runnable()
            {
                public void run()
                {
                    Connection con = null;
                    try {

                        Statement st1 = null;


                        syd.SETCONFIG("",context);
                        String CONFIGSTR = syd.IP+"/"+syd.DBNAME;
                        con = DriverManager.getConnection("jdbc:mysql://"+CONFIGSTR, syd.DBUSER, syd.DBPASSWORD);
                        Log.d("MYSQLERROR", "MYSQLERROR3"+con.toString());

                        //con = DriverManager.getConnection("jdbc:mysql://45.113.189.69:3306/phesgoa_pwd", "phesgoa_pwduser", "PWDmss_user#6090");

                        ResultSet rs;

                        st1 = con.createStatement();

                        rs = st1.executeQuery("SELECT * FROM dept_pay_type WHERE category='"+Category+"' AND sub_class='1' AND pay_type='"+pay_type+"' ORDER BY pay_type,category ASC");

                        Log.e("dept_pay_type", "subclass SELECT * FROM dept_pay_type WHERE category='"+Category+"' AND sub_class='1' AND pay_type='"+pay_type+"' ORDER BY id ASC ");

                        ResultSetMetaData rsmd = rs.getMetaData();
                        String result = new String();
                        rs.last();
                        int totalrows = rs.getRow();
                        rs.beforeFirst();
                        Double amount = 0.00;
                        if (totalrows > 0) {
                            while (rs.next()) {
                                String pay_type = rs.getString("pay_type");

                                String sub_type = rs.getString("sub_type");

                                Double charges = rs.getDouble("charges");

                                amount = amount +charges;


                                //sub_pay_arrlist(sub_type,charges);


                                //commentedLog.e("get_pay_type", "arrlist"+pay_type+"-"+charges);
                                Log.e("set_subpay_type", "sub_type" + sub_type + " charges" + charges);
                                Double finalAmount = amount;
                                handler.post(new Runnable() {
                                    public void run() {
                                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                                        TextView Tkey = new TextView(context);
                                        Tkey.setLayoutParams(lp);
                                        Tkey.setText(sub_type);
                                        Tkey.setTag("");

                                        EditText Tval = new EditText(context);
                                        Tval.setLayoutParams(lp);
                                        Tval.setTypeface(Typeface.DEFAULT_BOLD);
                                        Tval.setText(String.valueOf(charges));
                                        Tval.setTag(sub_type);
                                        Tval.setInputType(InputType.TYPE_CLASS_NUMBER);

                                        Tval.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                            }

                                            @Override
                                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                            }

                                            @Override
                                            public void afterTextChanged(Editable editable) {
                                                Log.e("Tval addcl",editable+" ");
                                               int childcount = sub_pay_layout.getChildCount();
                                               Double total_amount = 0.00;
                                               for(int i=0;i<=childcount;i++)
                                               {
                                                   View valfield = sub_pay_layout.getChildAt(i);
                                                   if(valfield!=null && valfield.getTag()!="")
                                                   {
                                                       EditText chargesfield = (EditText) valfield;
                                                       Log.e("Tval valfield", valfield.getTag() + " ");

                                                       Double chargefi = 0.00;
                                                       if(!"".equals(chargesfield.getText().toString().trim()))
                                                       {
                                                           chargefi = Double.valueOf( chargesfield.getText().toString());
                                                       }
                                                       total_amount = total_amount + chargefi;
                                                       paidamount.setText(String.valueOf(total_amount));
                                                   }
                                               }
                                            }
                                        });


                                        sub_pay_layout.addView(Tkey);
                                        sub_pay_layout.addView(Tval);
                                        paidamount.setText(String.valueOf(finalAmount));
                                    }
                                });
                            }

                        }

                    } catch (java.sql.SQLException e) {
                        e.printStackTrace();
                    }
                }

            });
            if ((conthread != null) && (!conthread.isAlive())) conthread.start();
        }catch (Exception e)
        {
            Log.e("sub_pay_arrlist",e.getMessage()+" ");
        }

        return sub_pay_arrlist;
    }

    public String DELETE_PHOTOS(String query,String Path)
    {
        int deleted_count = 0;
        String today = "";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;
        String error= "";
        if("1".equals(query))
        {

            Cursor cursor_get_query = null;
            String DELETE_PHOTO_QUERY = "";
            cursor_get_query = db.rawQuery("SELECT col_to_download FROM column_constant_updated  where tablename='meter_photos'", new String[]{});
            Log.e("DELETE PHOTO","cursor_get_query SELECT col_to_download FROM column_constant_updated  where tablename='meter_photos'");
            if (cursor_get_query.getCount() > 0)
            {
                while (cursor_get_query.moveToNext())
                {
                    DELETE_PHOTO_QUERY = cursor_get_query.getString(cursor_get_query.getColumnIndex("col_to_download"));
                }
            }

            Log.e("DELETE PHOTO","DELETE_PHOTO_QUERY "+DELETE_PHOTO_QUERY);

            if(!"".equals(DELETE_PHOTO_QUERY) && DELETE_PHOTO_QUERY!=null)
            {
                cursor = db.rawQuery(DELETE_PHOTO_QUERY, new String[]{});

                Log.e("DELETE PHOTO","QUERY FOUND "+DELETE_PHOTO_QUERY);

            }else
            {
                error = "QUERY NOT FOUND";
                Log.e("DELETE PHOTO","QUERY NOT FOUND");
            }

        }else if(!"1".equals(query) && !"".equals(query))
        {
            cursor = db.rawQuery(query, new String[]{});
            Log.e("DELETE PHOTO","ADMIN QUERY "+query);
        }
        if(!"".equals(Path))
        {
            Log.e("DELETE ON PATH",Path+" ");
            File file = new File(Path);
            String[] myFiles;
            Log.e("DELETE ON PATH",file+" file path");
            myFiles = file.list();
            for (int i=0; i<myFiles.length; i++) {
                File myFile = new File(file, myFiles[i]);

                Log.e("inside DELETE",file+" file path"+myFiles[i]);

                boolean deleted_files= myFile.delete();
                if(deleted_files)
                {
                    deleted_count++;
                }
            }

            return "DELETED on path...TOTAL COUNT "+myFiles.length+" deleted_count"+deleted_count;

        }


        if("".equals(error) )
        {
            int count = cursor.getCount();
            if (cursor.getCount() > 0)
            {

                while (cursor.moveToNext())
                {
                    String path = cursor.getString(cursor.getColumnIndex("path"));
                    String consumer_id = cursor.getString(cursor.getColumnIndex("consumer_id"));
                    String perday1 = cursor.getString(cursor.getColumnIndex("perday1"));
                    Log.e("DELETE_PHOTOS", "DELETE_PHOTOS path" + path + " consumer_id" + consumer_id + " perday1" + perday1);

                    File file = new File(path);
                    boolean deleted = file.delete();
                    if(deleted)
                    {
                        deleted_count++;
                    }
                    Log.e("DELETE_PHOTOS", "deleted " + deleted);
                }
                Log.e("DELETE_PHOTOS", "deleted_count " + deleted_count);
                cursor.close();
                return "DELETED on query...TOTAL COUNT "+count+" deleted_count"+deleted_count;
            } else {

                return "NOT DELETED....COUNT IS O "+count;
            }
        }else
        {

            Log.e("DELETE PHOTO","ERROR "+error);
            return "ERROR "+error;
        }
    }

    public String GET_LAST_SYNCED(String TABLENAME)
    {

        SQLiteDatabase db = getWritableDatabase();
        String LAST_SYNCED = "";
        Log.e("GET_LAST_SYNCED", "SELECT * FROM pwd_sync WHERE type='"+TABLENAME+"' order by DateTime DESC LIMIT 1");
        Cursor dbdata = db.rawQuery("SELECT * FROM pwd_sync WHERE type='"+TABLENAME+"' order by DateTime DESC LIMIT 1", new String[]{});
        int totalcolumn = dbdata.getCount();
        if (totalcolumn > 0)
        {
            while (dbdata.moveToNext())
            {
                LAST_SYNCED = dbdata.getString(dbdata.getColumnIndex("DateTime"));
                Log.e("LAST_SYNCED",LAST_SYNCED+" ");
            }
        }
        dbdata.close();
        return LAST_SYNCED;
    }

}