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

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import android.os.Handler;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.Objects;

public class ActivityTrackingService extends Service {

    private static final String TAG = "ActivityTrackingService";
    private static final String CHANNEL_ID = "ActivityTrackingChannel";
    private static final long LOCATION_UPDATE_INTERVAL = 15000;    //15秒ごとに位置情報を更新

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private final Handler energyHandler = new Handler(Looper.getMainLooper());
    private Runnable energyRunnable;
    private boolean isEnergyTimerRunning = false;
    private static final long ENERGY_INTERVAL = 10000; // 10秒 (10000ミリ秒)


    //onCreateはサービスが一番最初に作成される時に一回だけ呼ばれる
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {

        // ActivityTransitionReceiverからの合図かどうかをチェック
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (Objects.equals(action, "START_ENERGY_TIMER")) {
                startEnergyTimer();
                return START_STICKY; // サービスは起動済みなのでタイマー開始だけして終了
            } else if (Objects.equals(action, "STOP_ENERGY_TIMER")) {
                stopEnergyTimer();
                return START_STICKY; // タイマー停止だけして終了
            }
        }

        Log.d(TAG, "ActivityTrackingServiceが開始されました。");
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("合志市探索アプリ")
                .setContentText("エリアを探索中です...") // 通知テキストを更新
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .build();

        startForeground(1, notification);
        Log.d(TAG, "startLocationUpdatesメソッドを呼び出します。");
        startLocationUpdates(); // 位置情報の定期更新を開始

        return START_STICKY;
    }

    private void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdatesメソッドが実行されました。");
        // 位置情報リクエストの設定を作成
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL).build();

        // 位置情報が更新されたときに呼び出されるコールバック
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "onLocationResultが呼び出されました。");
                if (locationResult == null) {
                    Log.w(TAG, "locationResultがnullです。");
                    return;
                }
                // 最新の位置情報を取得
                Location currentLocation = locationResult.getLastLocation();
                if (currentLocation != null) {
                    Log.d(TAG, "現在地を更新: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                    checkAreaAndUnlock(currentLocation);
                }else{
                    Log.w(TAG, "getLastLocation()がnullを返しました。");
                }
            }
        };

        // 位置情報の権限があるか最終確認
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "位置情報の権限あり。fusedLocationClient.requestLocationUpdatesを呼び出します。");
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper()).addOnFailureListener(e -> {
                Log.e(TAG, "GPSの定期更新リクエストの開始に失敗",e);
            });
        }else{
            Log.e(TAG, "位置情報の権限がありません！定期更新を開始できませんでした。");
        }
    }

    private void checkAreaAndUnlock(Location currentLocation) {

        Log.d(TAG, "checkAreaAndUnlockメソッド開始。現在地: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());

        // エリア管理者を呼び出し、現在地のエリアをチェック
        AreaManager areaManager = AreaManager.getInstance();
        Area currentArea = areaManager.checkCurrentArea(currentLocation);

        // もし何かのエリア内にいれば
        if (currentArea != null) {
            Log.d(TAG, "エリア内にいます: " + currentArea.getName() + " (ID: " + currentArea.getId() + ")");
            // データ管理者を呼び出し、現在のセーブデータをロード
            GameDataManager dataManager = GameDataManager.getInstance();
            PlayerData playerData = dataManager.loadPlayerData(this);
            Log.d(TAG, "現在の解放済みエリアID: " + playerData.unlockedAreaIds.toString());

            // もし、そのエリアが「まだ解放されていなければ」
            if (!playerData.unlockedAreaIds.contains(currentArea.getId())) {
                Log.d(TAG, "新しいエリアを発見！ -> " + currentArea.getName());
                // エリアIDを解放済みリストに追加
                playerData.unlockedAreaIds.add(currentArea.getId());

                // 新しいエリアを発見した時だけ、称号チェックを実行
                // エリア訪問によって獲得できる称号のIDを組み立てる
                // 例: Myosenjiエリアなら "title_myosenji" というIDを生成
                String areaTitleId = "title_" + currentArea.getId().toLowerCase();

                // その称号をまだ持っていないかチェック
                if (!playerData.unlockedTitleIds.contains(areaTitleId)) {
                    // TitleManagerに問い合わせて、そのIDが本当に実在する称号か確認する
                    TitleManager titleManager = TitleManager.getInstance();
                    Title title = titleManager.getTitleById(areaTitleId);

                    // TitleManagerが称号情報を返してくれたら（実在するIDだったら）
                    if (title != null) {
                        Log.d(TAG, "新しい称号を獲得! -> " + title.getName());
                        // プレイヤーの所持リストにIDを追加
                        playerData.unlockedTitleIds.add(areaTitleId);
                    } else {
                        Log.w(TAG, "ID: " + areaTitleId + " に対応する称号が見つかりませんでした。");
                    }
                } else {
                    Log.d(TAG, "エリア訪問称号 '" + areaTitleId + "' は既に獲得済みです");
                }

                // 変更を保存
                dataManager.savePlayerData(this, playerData);
                Log.d(TAG, currentArea.getName() + " を解放済みとして保存しました。");

                //称号データが更新されたことをアプリ自身に知らせる
                Intent intent = new Intent("com.example.pbl_gruop1.TITLE_DATA_UPDATED");
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                Log.d(TAG, "称号データ更新のお知らせを送信しました");
            } else {
                Log.d(TAG, "エリア '" + currentArea.getName() + "' は既に解放済みです");
            }

        } else {
            Log.d(TAG, "エリア内にいません");
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopEnergyTimer(); // タイマーを停止
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

    private void startEnergyTimer() {
        // 既にタイマーが作動中なら何もしない
        if (isEnergyTimerRunning) return;
        isEnergyTimerRunning = true;
        Log.d(TAG, "10秒ごとのエネルギー加算タイマーを開始します。");

        energyRunnable = new Runnable() {
            @Override
            public void run() {
                // 1エネルギーを追加
                DataManagerBridge.addEnergy(ActivityTrackingService.this, 1);
                // UIに更新を通知
                Intent intent = new Intent("com.example.pbl_gruop1.TITLE_DATA_UPDATED");
                LocalBroadcastManager.getInstance(ActivityTrackingService.this).sendBroadcast(intent);

                // 10秒後にこのRunnableを再度実行
                energyHandler.postDelayed(this, ENERGY_INTERVAL);
            }
        };
        // タイマーを即時実行
        energyHandler.post(energyRunnable);
    }

    private void stopEnergyTimer() {
        // タイマーが作動していなければ何もしない
        if (!isEnergyTimerRunning) return;
        isEnergyTimerRunning = false;
        // 予約されていたRunnableをすべてキャンセル
        energyHandler.removeCallbacks(energyRunnable);
        Log.d(TAG, "エネルギー加算タイマーを停止しました。");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    }




