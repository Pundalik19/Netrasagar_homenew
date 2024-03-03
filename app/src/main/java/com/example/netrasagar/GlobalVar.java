package com.example.netrasagar;

import android.graphics.Color;

import java.text.SimpleDateFormat;

public class GlobalVar {
    public static String Muser, User_FullName, state_id;
    public static boolean crew_view, vessel_view, crew_edit, vessel_edit, crew_add, vessel_add,
                        fish_catch_view, fish_catch_add, fish_catch_edit, fish_licence_renewal,
                        view_report, view_approvePass,pass_open,pass_close, crewValUpdate;
    public static String phppass = "1234";
    public static final String key = "47444D534644";
    //public static String server_address = "https://100.168.10.208:444";
    public static String server_address = "https://netrasagar.in";

    public static SimpleDateFormat ymdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat dmyt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static SimpleDateFormat dmy = new SimpleDateFormat("dd/MM/yyyy");
    public static SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");


    public static int[] CHART_COLORS = {
            Color.parseColor("#228B22"),
            Color.parseColor("#FFFF00"),
            Color.parseColor("#FF00FF"),
            Color.parseColor("#FFA500"),
            Color.parseColor("#C04000"),
            Color.parseColor("#6C2DC7"),
            Color.parseColor("#FF6700"),
            Color.parseColor("#FAF0DD"),
            Color.parseColor("#FEA3AA"),
            Color.parseColor("#2B65EC"),
            Color.rgb(193, 37, 82),
            Color.rgb(255, 102, 0),
            Color.rgb(245, 199, 0),
            Color.rgb(106, 150, 31),
            Color.rgb(179, 100, 53)
    };


}


