package com.example.netrasagar;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.netrasagar.Aadhar.AadhaarCard;
import com.example.netrasagar.Aadhar.AadhaarXMLParser;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class scannerView extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    ZXingScannerView scannerView;
    SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat dmy = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView=new ZXingScannerView(this);

        setContentView(scannerView);
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scannerView.getFlash())
                    scannerView.setFlash(false);
                else
                    scannerView.setFlash(true);

            }
        });
        getSupportActionBar().hide();
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        scannerView.this.finish();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    public void handleResult(Result rawResult) {
        try {
            String tmpadd1="",tmpadd2="",tmpadd3="";
            int tmpint=0;
            AadhaarCard aadarCard = new AadhaarXMLParser().parse(rawResult.getText());
            if(aadarCard.name!=null)
                crewaddedit.txt_name.setText(aadarCard.name);
            if(aadarCard.uid!=null)
                crewaddedit.txt_aadhar.setText(aadarCard.uid);
            if(aadarCard.house!=null)
                tmpadd1=aadarCard.house.trim();
            if(aadarCard.street!=null)
                tmpadd2=aadarCard.street.trim();
            if(aadarCard.loc!=null)
                tmpadd3=aadarCard.loc.trim();
            if(aadarCard.vtc!=null) {
                if (tmpadd3.length() > 0) {
                    tmpadd3 += ", ";
                }
                tmpadd3 += aadarCard.vtc.trim();
            }
            if(tmpadd1.length()<=0){
                tmpadd1=tmpadd2;
                tmpadd2=tmpadd3;
                tmpadd3="";
            }
            if(tmpadd2.length()<=0){
                tmpadd2=tmpadd3;
                tmpadd3="";
            }
            crewaddedit.txt_add1.setText(tmpadd1);
            crewaddedit.txt_add2.setText(tmpadd2);
            crewaddedit.txt_add3.setText(tmpadd3);

            if(aadarCard.dob.contains("-") && aadarCard.dob.length()==10) {
                Date dob = ymd.parse(aadarCard.dob);
                crewaddedit.txt_dob.setText(dmy.format(dob));
            }
            if(aadarCard.state!=null) {
                tmpint = crewaddedit.statelist.indexOf(aadarCard.state);
                if (tmpint >= 0) {
                    crewaddedit.dd_state.setText(crewaddedit.statelist.get(tmpint));
                }
            }


        } catch (IOException | XmlPullParserException | ParseException e) {
            e.printStackTrace();
            Log.d("Scanner error",e.getMessage());
        }

        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }
}