package com.example.pbl_gruop1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class SpeedChecker {

    private static final String TAG = "SpeedChecker";
    private static final int CHECK_INTERVAL_MS = 20000; // 速度をチェックする間隔（20秒）
    private static final double SPEED_THRESHOLD_KMH = 15.0; // 歩行か車かを判断するしきい値（時速15km）

    // 速度チェックの結果を呼び出し元に伝えるための「合図」の仕組み
    //インタフェース使ってとりあえず歩きか車かtrue,falseを送る.MainActivity.javaで中身はかく.
    //
    public interface SpeedCheckCallback {
        void onSpeedCheckResult(boolean isWalking);
    }

    public void checkSpeed(Context context, SpeedCheckCallback callback) {
        // 位置情報が許可されているか確認
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "位置情報の権限がありません。速度チェックを中止します。");
            callback.onSpeedCheckResult(true); // 権限がない場合、一旦「歩行」と見なしておく
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        // 1. 最初の位置情報を取得
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(locationA -> {
                    if (locationA == null) {
                        Log.e(TAG, "最初の位置情報が取得できませんでした。");
                        callback.onSpeedCheckResult(true);
                        return;
                    }

                    Log.d(TAG, "最初の位置情報を取得しました。20秒後に再度取得します。");

                    // 2. 20秒待つ
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // 3. 20秒後の位置情報を取得
                        CancellationTokenSource cancellationTokenSourceB = new CancellationTokenSource();
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSourceB.getToken())
                                .addOnSuccessListener(locationB -> {
                                    if (locationB == null) {
                                        Log.e(TAG, "20秒後の位置情報が取得できませんでした。");
                                        callback.onSpeedCheckResult(true);
                                        return;
                                    }

                                    // 4. 距離と速度を計算
                                    float distanceMeters = locationA.distanceTo(locationB);
                                    double speedKmh = (distanceMeters / (CHECK_INTERVAL_MS / 1000.0)) * 3.6;
                                    Log.d(TAG, "計算された速度: " + speedKmh + " km/h");

                                    // 5. 結果を判断して合図を送る
                                    if (speedKmh < SPEED_THRESHOLD_KMH) {
                                        callback.onSpeedCheckResult(true); // 歩行と判断
                                    } else {
                                        callback.onSpeedCheckResult(false); // 車と判断
                                    }
                                });
                    }, CHECK_INTERVAL_MS);
                });
    }
}