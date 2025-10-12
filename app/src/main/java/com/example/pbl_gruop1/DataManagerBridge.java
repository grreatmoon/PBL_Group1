package com.example.pbl_gruop1;

import android.content.Context;
import android.util.Log;

// 他のクラスから、データ管理機能への「橋渡し」を行うクラス
public class DataManagerBridge {

    private static final String TAG = "DataManagerBridge";

    // エネルギーを追加するための唯一の窓口
    public static void addEnergy(Context context, long energyAmount) {
        Log.d(TAG, energyAmount + "エネルギーの追加リクエストを受け取りました。");

        // TODO: この部分を、実際のGameDataManagerを呼び出すコードに書き換える
        // GameDataManager dataManager = GameDataManager.getInstance();
        // dataManager.addEnergy(context, energyAmount);
    }
}