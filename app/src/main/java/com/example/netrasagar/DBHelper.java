package com.example.netrasagar;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {


    public DBHelper(Context context) {
        super(context, "Netrasagar.db", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase DB) {
       /* DB.execSQL("CREATE TABLE DofUsers (id INTEGER PRIMARY KEY, loginid TEXT UNIQUE, username " +
                "TEXT, password TEXT, mobile TEXT, email TEXT, status INTEGER DEFAULT 0, viewcrew" +
                " INTEGER DEFAULT 0, Addcrew INTEGER DEFAULT 0, Editcrew INTEGER DEFAULT 0, " +
                "Deletecrew INTEGER DEFAULT 0, last_edit_at REAL)");*/

        DB.execSQL("CREATE TABLE ConstantValues (ID INTEGER PRIMARY KEY, Description TEXT UNIQUE, ValData TEXT)");
        DB.execSQL( " CREATE TABLE IF NOT EXISTS 'column_constant_updated' ('ID' INTEGER PRIMARY KEY autoincrement,'tablename'	TEXT,'table_type'	INTEGER DEFAULT 0,'col_to_download'	TEXT,'col_to_upload'	TEXT,'where_download'	TEXT,'where_upload'	TEXT,'select_query'	TEXT,'select_query_args'	TEXT,'serial_no'	TEXT,'user'	TEXT,'downloaded_at'	TEXT,'status'	INTEGER DEFAULT 1,'rev_update'	INTEGER DEFAULT 0,'rev_update_query'	TEXT,'key_value'	TEXT,'delete_update'	INTEGER DEFAULT 0,'UPDATE_COL_DOWN'	TEXT,'UPDATE_COL_UP'	TEXT,'select_query_up'	TEXT)" );
    }


    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
       // DB.execSQL("drop Table if exists DofUsers");
        DB.execSQL("drop Table if exists ConstantValues");
    }



    public Cursor getalldata(String tblnm, String where_clause) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM " + tblnm + " WHERE " + where_clause, null);
        return cursor;
    }

    public boolean write_constant(String Description, String Values) {
        SQLiteDatabase DB = this.getWritableDatabase();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("Description", Description);
            contentValues.put("ValData", Values);
            DB.replace("ConstantValues", null, contentValues);

            return true;
        } catch (SQLException sqle) {
            return false;
        }
    }

    @SuppressLint("Range")
    public String read_constant(String Description) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor =
                DB.rawQuery("SELECT * FROM ConstantValues WHERE Description = '" + Description +
                        "'", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex("ValData"));
        } else
            return "";
    }

    public String CREATETABLES()
    {
        SQLiteDatabase db = getWritableDatabase();


        try
        {
            db.execSQL( " CREATE TABLE IF NOT EXISTS 'column_constant_updated' ('ID' INTEGER PRIMARY KEY autoincrement,'tablename'	TEXT,'table_type'	INTEGER DEFAULT 0,'col_to_download'	TEXT,'col_to_upload'	TEXT,'where_download'	TEXT,'where_upload'	TEXT,'select_query'	TEXT,'select_query_args'	TEXT,'serial_no'	TEXT,'user'	TEXT,'downloaded_at'	TEXT,'status'	INTEGER DEFAULT 1,'rev_update'	INTEGER DEFAULT 0,'rev_update_query'	TEXT,'key_value'	TEXT,'delete_update'	INTEGER DEFAULT 0,'UPDATE_COL_DOWN'	TEXT,'UPDATE_COL_UP'	TEXT,'select_query_up'	TEXT)" );
            Log.e("column_constant_updated","column_constant_updated CREATED SUCCESSFULLY");

            db.execSQL( " CREATE TABLE IF NOT EXISTS 'crew_data' ('ID' INTEGER PRIMARY KEY autoincrement,'RFIDCardNo'	TEXT,'PhotoPath'	TEXT,'Crewname'	TEXT,'AadharNo'	TEXT,'Debarred'	INTEGER DEFAULT 0,'DebarReason'	TEXT,'status'		INTEGER DEFAULT 0,'DOB'	TEXT,'MobNo'	TEXT)" );
            Log.e("crew_data","crew_data CREATED SUCCESSFULLY");


            db.execSQL( " CREATE TABLE IF NOT EXISTS 'vessel_voyage' ('ID' INTEGER PRIMARY KEY autoincrement,'PassNo'	TEXT,'VesselNm'	TEXT,'StrtTm'	TEXT,'ValTm'	TEXT,'VoyageStatus'	INTEGER DEFAULT 0)" );
            Log.e("vessel_voyage","vessel_voyage CREATED SUCCESSFULLY");

            db.execSQL( " CREATE TABLE IF NOT EXISTS 'vessel_voyage' ('ID' TEXT,'PassNo'	TEXT,'VesselNm'	TEXT,'StrtTm'	TEXT,'ValTm'	TEXT,'VoyageStatus'	INTEGER DEFAULT 0)" );
            Log.e("vessel_voyage","vessel_voyage CREATED SUCCESSFULLY");

            db.execSQL( " CREATE TABLE IF NOT EXISTS pos_user (id INTEGER PRIMARY KEY autoincrement,user varchar(100) DEFAULT NULL,serial_no varchar(50) NOT NULL,username varchar(50) DEFAULT NULL,password varchar(50) DEFAULT NULL,status tinyint(1) NOT NULL DEFAULT '0',gps tinyint(1) NOT NULL DEFAULT '0',trip_details tinyint(1) NOT NULL DEFAULT '0' ,gps_count int(11) NOT NULL DEFAULT '5',edited_by varchar(50) NOT NULL,edited_datetime datetime NOT NULL )" );
            Log.e("pos_user","pos_user CREATED SUCCESSFULLY");

            db.execSQL( " CREATE TABLE IF NOT EXISTS vessel_master(ID INTEGER PRIMARY KEY autoincrement," +
                    "  AssetNm varchar(100) NOT NULL," +
                    "  Type varchar(150) NOT NULL," +
                    "  AssetId varchar(50) NOT NULL," +
                    "  VHF tinyint(1) NOT NULL DEFAULT '0'," +
                    "  GPS tinyint(1) NOT NULL DEFAULT '0'," +
                    "  VTS tinyint(1) NOT NULL DEFAULT '0'," +
                    "  GroupID int(11) DEFAULT NULL," +
                    "  Owner varchar(80) NOT NULL," +
                    "  ContactOwner varchar(20) NOT NULL," +
                    "  AlternateContact varchar(15) DEFAULT NULL," +
                    "  Aadhar varchar(15) DEFAULT NULL," +
                    "  AlertMail varchar(50) DEFAULT NULL," +
                    "  Gender varchar(7) NOT NULL," +
                    "  OwnerAddress1 varchar(100) NOT NULL," +
                    "  OwnerAddress2 varchar(100) DEFAULT NULL," +
                    "  OwnerAddress3 text," +
                    "  BirthDate date DEFAULT NULL," +
                    "  CrewNo int(11) NOT NULL," +
                    "  RFIDMUser varchar(50) DEFAULT NULL," +
                    "  RFIDEdtTm datetime DEFAULT NULL," +
                    "  RFIDIssued tinyint(1) NOT NULL DEFAULT '0'," +
                    "  RFIDCardNo varchar(50) DEFAULT NULL," +
                    "  RFIDIssUsr varchar(50) DEFAULT NULL," +
                    "  RFIDPntUsr varchar(50) DEFAULT NULL," +
                    "  Remark text," +
                    "  RFIDIssDt date DEFAULT NULL," +
                    "  RFIDValDt date DEFAULT NULL," +
                    "  RFIDDup tinyint(1) NOT NULL DEFAULT '0'," +
                    "  EdtRFIDCard tinyint(1) NOT NULL DEFAULT '0'," +
                    "  Debarred tinyint(1) NOT NULL DEFAULT '0'," +
                    "  DebarUser varchar(80) DEFAULT NULL," +
                    "  DebarTill date DEFAULT NULL," +
                    "  Asset_GPS_Name varchar(100) DEFAULT NULL," +
                    "  register_dt date DEFAULT NULL," +
                    "  buildyard_name varchar(200) DEFAULT NULL," +
                    "  buildyard_addr text," +
                    "  build_yr varchar(50) DEFAULT NULL," +
                    "  hull_material varchar(100) NOT NULL," +
                    "  length double(12,3) NOT NULL," +
                    "  breadth double(12,3) NOT NULL," +
                    "  depth double(12,3) NOT NULL," +
                    "  engine1_mk varchar(100) DEFAULT NULL," +
                    "  engine1_yr varchar(100) DEFAULT NULL," +
                    "  engine1_no varchar(100) DEFAULT NULL," +
                    "  engine1_cylinder int(11) DEFAULT NULL," +
                    "  engin1_HorsePower decimal(12,2) DEFAULT NULL," +
                    "  engin1_RPM int(11) DEFAULT NULL," +
                    "  engine2_mk varchar(100) DEFAULT NULL," +
                    "  engine2_yr varchar(100) DEFAULT NULL," +
                    "  engine2_no varchar(100) DEFAULT NULL," +
                    "  engine2_cylinder int(11) DEFAULT NULL," +
                    "  engine2_HorsePower decimal(12,2) DEFAULT NULL," +
                    "  engin2_RPM int(11) DEFAULT NULL," +
                    "  groass_tons double(12,3) NOT NULL," +
                    "  FuelUsed varchar(30) DEFAULT NULL ," +
                    "  FuelCapacity int(11) DEFAULT NULL," +
                    "  life_jacket int(11) DEFAULT NULL," +
                    "  life_bouys int(11) DEFAULT NULL," +
                    "  first_aid int(11) DEFAULT NULL," +
                    "  Flags int(11) DEFAULT NULL," +
                    "  vrc_file_path text," +
                    "  aadhar_file_path varchar(150) DEFAULT NULL," +
                    "  subs_avail varchar(10) NOT NULL," +
                    "  blacklist_bit int(1) DEFAULT '0'," +
                    "  blacklist_remark text," +
                    "  blacklist_count int(11) DEFAULT '0'," +
                    "  Impound_till date DEFAULT NULL," +
                    "  vts_valid_till date DEFAULT NULL," +
                    "  fish_lic_no varchar(100) DEFAULT NULL," +
                    "  fish_lic_valid_till date DEFAULT NULL," +
                    "  fish_lic_issue_till date DEFAULT NULL," +
                    "  hull_colour varchar(100) DEFAULT NULL," +
                    "  super_structure_colour varchar(100) DEFAULT NULL," +
                    "  state varchar(100) DEFAULT NULL," +
                    "  vess_photo_path text," +
                    "  AIS_details varchar(15) DEFAULT NULL," +
                    "  callsign varchar(10) DEFAULT NULL," +
                    "  del_dt datetime DEFAULT NULL," +
                    "  del_reason text," +
                    "  EdtDtTm datetime DEFAULT NULL," +
                    "  EdtDtUsr varchar(100) DEFAULT NULL" +
                    ")" );
            Log.e("pos_user","vessel_master CREATED SUCCESSFULLY");


            db.execSQL( " CREATE TABLE IF NOT EXISTS 'pwd_sync' ('id' INTEGER PRIMARY KEY autoincrement,'user'	TEXT,'serial_no'	TEXT,'DateTime'	TEXT,'type'	TEXT,'remarks'	TEXT,'UPLOADED_BY'	TEXT,'POS_UP_AT'	TEXT,upload_bit INTEGER DEFAULT 1)" );

            db.execSQL( " CREATE TABLE IF NOT EXISTS trip_verif_report ( id INTEGER PRIMARY KEY autoincrement ," +
                    "  trip_id int(11) NOT NULL," +
                    "  rfid_card_no varchar(100) NOT NULL," +
                    "  type varchar(100) NOT NULL," +
                    "  crew_vessel_name varchar(100) NOT NULL," +
                    "  trip_no varchar(50) DEFAULT NULL," +
                    "  trip_iss_date date DEFAULT NULL," +
                    "  trip_val_date date DEFAULT NULL," +
                    "  comment text," +
                    "  lat_value varchar(50) DEFAULT NULL," +
                    "  long_value varchar(50) DEFAULT NULL," +
                    "  verification_date datetime NOT NULL," +
                    "  user varchar(100) NOT NULL," +
                    "  IMEI_No varchar(50) NOT NULL," +
                    "  lat_long geometry DEFAULT NULL," +
                    "  receivedDT datetime NOT NULL," +
                    "  upload_bit INTEGER DEFAULT 1," +
                    "  POS_UP_AT datetime DEFAULT NULL," +
                    "  UPLOADED_BY TEXT DEFAULT NULL" +
                    ")" );
            Log.e("pos_user","trip_verif_report CREATED SUCCESSFULLY");

        }catch (SQLException e)
        {
            Log.e("CREATETABLE",e.getMessage());

        }


        return "";
    }



}
