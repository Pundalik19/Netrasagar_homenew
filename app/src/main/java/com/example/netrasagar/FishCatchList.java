package com.example.netrasagar;

public class FishCatchList {
    int id;
    double qty, rate;
    String fishname;
    String fish_catch_id;

    FishCatchList(int id, double qty, double rate, String fishname) {
        this.id = id;
        this.qty = qty;
        this.rate=rate;
        this.fishname = fishname;
    }

    FishCatchList(int id, double qty, double rate, String fishname, String fish_catch_id) {
        this.id = id;
        this.qty = qty;
        this.rate= rate;
        this.fishname = fishname;
        this.fish_catch_id = fish_catch_id;
    }

}
