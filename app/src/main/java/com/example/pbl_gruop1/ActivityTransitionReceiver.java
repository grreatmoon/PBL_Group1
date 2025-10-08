package com.example.pbl_gruop1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityTransitionReceiver extends BroadcastReceiver {

    private static final String TAG = "ActivityTransition";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 通知にデータが含まれているか確認
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            // 念のため、結果がnullでないことも確認
            if (result != null) {
                // 通知されたイベント（歩き始めた、止まったなど）を一つずつ取り出す
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {

                    // どんな行動（アクティビティ）かを取得
                    String activity = toActivityString(event.getActivityType());

                    // その行動を「開始した」のか「終了した」のかを取得
                    String transitionType = toTransitionTypeString(event.getTransitionType());

                    // ログに記録して、動作しているか確認する
                    Log.d(TAG, "検知した行動: " + activity + ", 状態: " + transitionType);

                    //もし「歩行」を「開始」したなら、速度チェックを行う
                    if (event.getActivityType() == DetectedActivity.WALKING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        Log.d(TAG, "歩行開始を検知。GPSによる速度チェックを開始します。");

                        SpeedChecker speedChecker = new SpeedChecker();
                        speedChecker.checkSpeed(context, new SpeedChecker.SpeedCheckCallback() {
                            @Override
                            public void onSpeedCheckResult(boolean isWalking) {
                                if (isWalking) {
                                    Log.d(TAG, "速度チェックの結果：「歩行」と判断されました。エネルギー計算を開始します。");
                                    //ここにエネルギー計算(タイマー開始)のロジックを後で追加
                                } else {
                                    Log.d(TAG, "速度チェックの結果：「車」と判断されました。エネルギー計算は行いません。");
                                    //エネルギー計算はしない
                                }
                            }
                        });

                    }
                }
            }
        }
    }
    // ログを見やすくするための補助機能
    private String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.WALKING:
                return "歩行";
            case DetectedActivity.STILL:
                return "静止";
            case DetectedActivity.IN_VEHICLE:
                return "乗り物";
            default:
                return "不明";
        }
    }

    // ログを見やすくするための補助機能
    private String toTransitionTypeString(int transitionType) {
        switch (transitionType) {
            case 0: // ENTER
                return "開始";
            case 1: // EXIT
                return "終了";
            default:
                return "不明";
        }
    }
}