package com.example.pbl_gruop1;

public class Title {

    private String id; //称号を識別するためのID
    private String name; //称号の表示名
    private String description; //称号の獲得条件の説明
    private int imageResId; //称号のアイコンの画像のリソースID
    private String requiredAreaId;  //この称号を開放するために必要なエリアのID

    public Title(String id, String name, String description, int imageResId, String requiredAreaId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageResId = imageResId;
        this.requiredAreaId = requiredAreaId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getRequiredAreaId() {
        return requiredAreaId;
    }



}
