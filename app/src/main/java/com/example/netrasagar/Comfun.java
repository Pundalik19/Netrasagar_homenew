package com.example.netrasagar;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Comfun {

    public static String md5hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            BigInteger md5Data = new BigInteger(1, md.digest(input.getBytes()));
            return String.format("%032X", md5Data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String json_string(JSONObject J, String ID ){
        if(J.isNull(ID))
            return "";
        else
            try {
                return J.getString(ID);
            }
            catch(Exception ex) {
                return null;
            }

    }
    public static Integer json_int(JSONObject J, String ID ){
        if(J.isNull(ID))
            return 0;
        else
            try {
                return J.getInt(ID);
            }
            catch(Exception ex) {
                return null;
            }

    }

    public static String encodeBitmapImage(Uri filepath, Activity activity) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(filepath);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] bytesofimage = byteArrayOutputStream.toByteArray();
            return android.util.Base64.encodeToString(bytesofimage, Base64.DEFAULT);
        }
        catch (Exception Ex){
            Toast.makeText(activity,"IMAGE ENCODING ERROR : " + Ex.toString(),Toast.LENGTH_SHORT);
            return "";
        }
    }

    public static String ConvertDateFormat(String date, String from, String to) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(to);
            SimpleDateFormat dmy = new SimpleDateFormat(from);
            return sdf.format(dmy.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }



}
