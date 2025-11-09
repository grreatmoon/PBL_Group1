//バトル画面への遷移処理に用いる(仮置きとしての空っぽのファイル)
package com.example.pbl_gruop1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class BattleGamenseni extends AppCompatActivity {

    private static final String TAG = "BattleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //最小限のセットアップ(Activityとして実行するために記述)
        //まだバトル画面としての記述が0だから一瞬ちらついてすぐ元の画面に戻る
        Log.d(TAG, "BattleActivity に入りました。");

        //バトル後の画面遷移(すぐに終了して元の画面に戻る処理)
        //このActivityを終了し, 呼び出し元に戻る
        finish();
    }
}