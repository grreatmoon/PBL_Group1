package com.example.pbl_gruop1;

import java.io.Serializable;
public class Title implements Serializable{

    private String id; //称号を識別するためのID
    private String name; //称号の表示名
    private String description; //称号の獲得条件の説明
    private String imageName; //称号のアイコンの画像のリソースID
    private String requiredAreaId;  //この称号を開放するために必要なエリアのID
    private String category;    //称号を整理用のカテゴリ

    public Title(String id, String name, String description, String imageName, String requiredAreaId,String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageName = imageName;
        this.requiredAreaId = requiredAreaId;
        this.category = category;
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

    public String getImageName() {
        return imageName;
    }

    public String getRequiredAreaId() {
        return requiredAreaId;
    }
    public String getCategory() { return category; }

}
