package com.example.pbl_gruop1;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

public class GameDataManager {

    private static final String PREFS_NAME = "KoshiExplorePrefs";
    private static final String PLAYER_DATA_KEY = "PlayerData";

    //シングルトン
    private static GameDataManager instance;
    private Gson gson;

    //プライベートコンストラクタ
    private GameDataManager() {
        gson = new Gson();
    }

    //シングルトンのコア
    public static synchronized GameDataManager getInstance() {
        if (instance == null) {
            instance = new GameDataManager();
        }
        return instance;
    }

    //PlayerDataをJSON形式で保存するメソッド
    //SharedPreferencesはAndroidアプリ専用の小さなメモ帳.少量のデータを「Key」と「Value」のペアで保存できる
    //PlayerDataオブジェクト等をSharedPreferencesに書き込むために、Gsonを使ってオブジェクトをJSONに変換している.

    public void savePlayerData(Context context, PlayerData playerData) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        //PlayerDataをJSON文字列に変換
        String json = gson.toJson(playerData);

        //JSON文字列をSharedPreferencesに保存
        editor.putString(PLAYER_DATA_KEY, json);
        editor.apply();
    }

    //SharedPreferencesからPlayerDataを読み込むメソッド
    public PlayerData loadPlayerData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        //保存されているJSON文字列を取得(なければnull)
        String json = prefs.getString(PLAYER_DATA_KEY, null);

        if (json == null) {
            //セーブデータがない場合(初回起動時)は、新しいPlayerDataを返す
            return new PlayerData();
        } else {
            //JSON文字列をPlayerDataオブジェクトに変換して返す
            return gson.fromJson(json, PlayerData.class);
        }
    }

}
