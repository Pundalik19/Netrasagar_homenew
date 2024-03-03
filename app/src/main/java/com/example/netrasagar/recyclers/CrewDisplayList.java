package com.example.netrasagar.recyclers;

public class CrewDisplayList {
    public String id,Aadhar, Name, vessel, RFID, election_card, mobileNo, vesselStatus;
    public boolean checked=false, status;

    public CrewDisplayList(String id, String Aadhar, String Name, String vessel, String RFID,
                           String election_card,
                           String mobileNo, String vesselStatus, boolean status, boolean checked){
        this.id=id;
        this.Aadhar=Aadhar;
        this.Name=Name;
        this.vessel=vessel;
        this.RFID=RFID;
        this.election_card=election_card;
        this.checked=checked;
        this.mobileNo=mobileNo;
        this.vesselStatus=vesselStatus;
        this.status=status;
    }


}
