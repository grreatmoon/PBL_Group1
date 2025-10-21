package com.example.pbl_gruop1;

import android.content.Context;
import android.util.Log;

// 他のクラスから、データ管理機能への「橋渡し」を行うクラス
public class DataManagerBridge {

    private static final String TAG = "DataManagerBridge";

    // エネルギーを追加するための唯一の窓口
    public static void addEnergy(Context context, long energyAmount) {
        Log.d(TAG, energyAmount + "エネルギーの追加リクエストを受け取りました。");


        //GameDataManagerのインスタンス取得
         GameDataManager dataManager = GameDataManager.getInstance();
         //現在のセーブデータを読み込む
        PlayerData currentData = dataManager.loadPlayerData(context);
        //読み込んだデータに獲得したエネルギーを加算
        currentData.energy += (int) energyAmount;
        if (currentData.energy > currentData.maxEnergy) {
            currentData.energy = currentData.maxEnergy;
        }
        //新しいデータを保存
        dataManager.savePlayerData(context, currentData);

        Log.d(TAG, "データを更新しました。現在の合計エネルギー：　" + currentData.energy);
    }
}