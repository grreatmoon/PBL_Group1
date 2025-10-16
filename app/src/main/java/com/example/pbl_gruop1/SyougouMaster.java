//称号の数が膨大になった時用に称号のフォーマットを設定. SyougouFragment.javaにて引数の指定をして一括管理
package com.example.pbl_gruop1;

public class SyougouMaster{
    private final String id;      //称号ID
    private final String name;    //称号名
    private final String message; //称号の獲得条件
    private final int imageResId; //称号毎の画像ID

    public SyougouMaster(String id, String name, String message, int imageResId){
        this.id = id;
        this.name = name;
        this.message = message;
        this.imageResId = imageResId;
    }

    //各データ取得用のゲッター
    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getMessage(){
        return message;
    }
    public int getImageResId(){
        return imageResId;
    }
    public String getLockedName(){ //称号未取得時は全ての称号の名前を？？？に矯正
        return "???";
    }
}
