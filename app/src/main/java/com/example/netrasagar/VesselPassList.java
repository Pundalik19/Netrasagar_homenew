package com.example.netrasagar;

public class VesselPassList {
    boolean vesselstatus;
    String id, vesselName, vrc, passStatus, jetty;
    String passNo, state_id;

    VesselPassList(String id, String vesselName, String vrc, String jetty, String passStatus, boolean vesselstatus){
        this.id=id;
        this.vesselName=vesselName;
        this.vrc=vrc;
        this.jetty=jetty;
        this.passStatus=passStatus;
        this.vesselstatus=vesselstatus;
    }
    VesselPassList(String id, String vesselName, String vrc, String jetty, String passStatus,
                   boolean vesselstatus, String passNo, String state_id){
        this.id=id;
        this.vesselName=vesselName;
        this.vrc=vrc;
        this.jetty=jetty;
        this.passStatus=passStatus;
        this.vesselstatus=vesselstatus;
        this.passNo=passNo;
        this.state_id=state_id;
    }
}
