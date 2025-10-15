package com.example.pbl_gruop1;

public class Area {

    //エリアを識別するためのID
    private String id;

    //エリアの表示名
    private String name;

    //エリア中心の緯度
    private double latitude;

    //エリア中心の経度
    private double longitude;

    //エリアの半径(m)
    private double radius;

    //Areaオブジェクトを作成するためのコンストラクタ
    public Area(String id, String name, double latitude, double longitude, double radius){
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    //ゲッターセッター
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getRadius() {
        return radius;
    }
}
