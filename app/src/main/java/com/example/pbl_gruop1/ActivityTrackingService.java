package com.example.pbl_gruop1;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
public class ActivityTrackingService extends Service {

    private static final String TAG = "ActivityTrackingService";
    private static final String CHANNEL_ID = "ActivityTrackingChannel";
    private static final long LOCATION_UPDATE_INTERVAL = 30000;    //30秒ごとに位置情報を更新

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("合志市探索アプリ")
                .setContentText("エリアを探索中です...") // 通知テキストを更新
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notification);
        startLocationUpdates(); // 位置情報の定期更新を開始

        return START_STICKY;
    }

    private void startLocationUpdates() {
        // 位置情報リクエストの設定を作成
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL).build();

        // 位置情報が更新されたときに呼び出されるコールバック
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                // 最新の位置情報を取得
                Location currentLocation = locationResult.getLastLocation();
                if (currentLocation != null) {
                    Log.d(TAG, "現在地を更新: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                    checkAreaAndUnlock(currentLocation);
                }
            }
        };

        // 位置情報の権限があるか最終確認
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void checkAreaAndUnlock(Location currentLocation) {
        // エリア管理者を呼び出し、現在地のエリアをチェック
        AreaManager areaManager = AreaManager.getInstance();
        Area currentArea = areaManager.checkCurrentArea(currentLocation);

        // もし何かのエリア内にいれば
        if (currentArea != null) {
            // データ管理者を呼び出し、現在のセーブデータをロード
            GameDataManager dataManager = GameDataManager.getInstance();
            PlayerData playerData = dataManager.loadPlayerData(this);

            // もし、そのエリアがまだ解放されていなければ
            if (!playerData.unlockedAreaIds.contains(currentArea.getId())) {
                Log.d(TAG, "新しいエリアを発見！ -> " + currentArea.getName());
                // エリアIDを解放済みリストに追加
                playerData.unlockedAreaIds.add(currentArea.getId());
                // 変更を保存
                dataManager.savePlayerData(this, playerData);
                Log.d(TAG, currentArea.getName() + " を解放済みとして保存しました。");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // サービスが終了するときは、位置情報の更新を停止してバッテリーを節約
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Activity Tracking Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
