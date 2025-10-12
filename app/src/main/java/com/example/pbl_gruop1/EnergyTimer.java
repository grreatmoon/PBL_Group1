package com.example.pbl_gruop1;

import android.util.Log;

public class EnergyTimer {

    private static final String TAG = "EnergyTimer";

    //シングルトン(アプリ内に一つだけ存在するインスタンス)
    private static EnergyTimer instance;

    //歩行開始時刻を記録する変数
    private long startTime = 0;

    //プライベートコンストラクタ(外部から勝手に新しいインスタンスを作れないようにするためにprivate)
    private EnergyTimer() {}

    //シングルトンのコア
    public static synchronized  EnergyTimer getInstance() {
        if (instance == null) {
            instance = new EnergyTimer();
        }
        return instance;
    }

    public void start() {
        //すでにタイマーが作動中であれば何もしない
        if (startTime != 0){
            Log.w(TAG,"タイマーはすでに作動中です。");
            return;
        }

        //現在時刻を記録してタイマーを開始
        startTime = System.currentTimeMillis();
        Log.d(TAG,"タイマーを開始しました。");
    }

    //タイマーを停止し、経過時間を秒単位で返すメソッド
    public long stop() {
        //タイマーが作動していない場合は0を返す
        if (startTime == 0) {
            Log.w(TAG, "タイマーは作動していません");
            return 0;
        }
        //経過時間を計算
        long elapsedTime = System.currentTimeMillis() - startTime;
        //タイマーをリセット
        startTime = 0;
        //経過時間をログに出力
        Log.d(TAG, "歩行タイマーを停止しました。経過時間： " + (elapsedTime / 1000) + "秒");
        //経過時間を秒単位で返す
        return elapsedTime / 1000;
    }


}
