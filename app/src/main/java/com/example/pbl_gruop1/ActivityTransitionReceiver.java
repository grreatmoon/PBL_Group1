package com.example.pbl_gruop1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

                GameDataManager dataManager = GameDataManager.getInstance();
                PlayerData playerData = dataManager.loadPlayerData(context);
                boolean dataChanged = false;    //データが変更されたか追跡するフラグ

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

                        if (!"歩行中".equals(playerData.currentStatus)) {
                            playerData.currentStatus = "歩行中";
                            dataChanged = true;
                        }

                        SpeedChecker speedChecker = new SpeedChecker();
                        speedChecker.checkSpeed(context, new SpeedChecker.SpeedCheckCallback() {
                            @Override
                            public void onSpeedCheckResult(boolean isWalking) {
                                if (isWalking) {
                                    Log.d(TAG, "速度チェックの結果：「歩行」と判断されました。エネルギー計算を開始します。");
                                    Intent startIntent = new Intent(context, ActivityTrackingService.class);
                                    startIntent.setAction("START_ENERGY_TIMER");
                                    context.startService(startIntent);
                                } else {
                                    Log.d(TAG, "速度チェックの結果：「車」と判断されました。エネルギー計算は行いません。");
                                    Intent stopIntent = new Intent(context, ActivityTrackingService.class);
                                    stopIntent.setAction("STOP_ENERGY_TIMER");
                                    context.startService(stopIntent);
                                }
                            }
                        });

                    }

                    //もし「歩行」を「終了」または「静止」を「開始」したらタイマーを停止
                    if ((event.getActivityType() == DetectedActivity.WALKING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT) || (event.getActivityType() == DetectedActivity.STILL && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER)) {

                        if (!"停止中".equals(playerData.currentStatus)) {
                            playerData.currentStatus = "停止中";
                            dataChanged = true;
                        }
                        Log.d(TAG, "静止を検知。サービスにタイマー停止を命令します。");
                        Intent stopIntent = new Intent(context, ActivityTrackingService.class);
                        stopIntent.setAction("STOP_ENERGY_TIMER");
                        context.startService(stopIntent);
                    }
                }
                if (dataChanged) {
                    Log.d(TAG, "状態を更新: " + playerData.currentStatus);
                    dataManager.savePlayerData(context, playerData);
                    // UI更新のお知らせを送信（エネルギー獲得がなくても状態変更を通知するため）
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("com.example.pbl_gruop1.TITLE_DATA_UPDATED"));
                    Log.d(TAG, "UI更新のためのお知らせ（Broadcast）を送信しました。");
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