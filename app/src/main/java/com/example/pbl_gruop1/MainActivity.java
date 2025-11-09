package com.example.pbl_gruop1;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pbl_gruop1.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private PendingIntent activityTransitionPendingIntent;
    //Intentはメッセージオブジェクト.「今すぐ」何かを実行するためのメッセージ.
    //PendingIntentは「未来のいつか」に実行されることを「予約」するためのメッセージ.「通知をタップしたらアプリを開く」等.
    private ActivityRecognitionClient activityRecognitionClient;
    //Googleの行動認識APIクライアント

    private static final int PERMISSION_REQUEST_CODE = 1001;
    //code for verifying permission request

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    //アプリ起動時の初期化
    // onCrerate関数内でXMLレイアウトを読み込んでいる
        super.onCreate(savedInstanceState);

        checkAndRequestPermissions();
        //gpsやinternet等の許可をチェック

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //デザインしたXMLレイアウトファイルをメモリ上に読み込む
        setContentView(binding.getRoot());



        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });

        binding.fab.setVisibility(View.GONE);   //メールボックスみたいなやつを非表示
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void checkAndRequestPermissions(){
    //このメソッドはREQUIRED_PERMISSIONSに設定した権限が許可されているか確認し、許可されていなければユーザーに要求するメソッド
        List<String> permissionsToRequest = new ArrayList<>();

        // 必須の権限（位置情報）をチェック
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {   //この文で許可がなければ権限をリクエストしている
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // OSがAndroid 12(API 31)以上なら、COARSEもセットで要求する必要がある
        if  (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
        }

        // OSがAndroid 10 (API 29) 以上なら、身体活動の権限もチェック
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION);
            }
        }

        // OSがAndroid 13 (API 33) 以上なら、通知の権限もチェック
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // OSがAndroid9以上ならフォアグラウンドサービスの基本権限をチェック
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE);
            }
        }
        // OSがAndroid 14 (API 34) 以上なら、フォアグラウンドサービスの「位置情報」権限をチェック
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION);
            }
        }

        if(!permissionsToRequest.isEmpty()){
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        } else {
            startAppInitialization();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        if(requestCode == PERMISSION_REQUEST_CODE){
            boolean allGranted = true;
            for(int result:grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    allGranted = false;
                    break;
                }
            }

            if(allGranted){
                startAppInitialization();   //必要な権限がすべてクリアであればアプリを初期化
            } else {
                Toast.makeText(this, "requests for using this app got rejected",Toast.LENGTH_LONG).show();  //必要な権限がすべて許可されていない場合はアプリを終了
                finish();
            }
        }
    }

    private void startAppInitialization(){
        try {
            GameDataManager dataManager = GameDataManager.getInstance();
            PlayerData playerData = dataManager.loadPlayerData(this);
            playerData.energy = 100; // エネルギーを100に強制設定
            dataManager.savePlayerData(this, playerData); // すぐに保存
            Log.d(TAG, "!!!デバッグ用: エネルギーを100に設定しました!!!");
        } catch (Exception e) {
            Log.e(TAG, "デバッグ用のエネルギー設定に失敗", e);
        }//デバッグ用なので後で消す

        Log.d("INIT","All requests got cleared.proceeding to next step");
        Toast.makeText(this,"All requests got cleared.proceeding to next step",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "これからActivityTrackingServiceを開始します...");
        Intent serviceIntent = new Intent(this, ActivityTrackingService.class);
        startService(serviceIntent);
        startTracking();
    }





    private void startTracking() {
    //どんな行動の変化を監視したいかリストを作る
    List<ActivityTransition> transitions = new ArrayList<>();
    transitions.add(
            new ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.WALKING)//歩行
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)//開始時
                    .build());
    transitions.add(
            new ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.WALKING)//歩行
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)//終了時
                    .build());
    transitions.add(
            new ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.STILL)//静止
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)//開始時
                    .build());

    //監視のリクエストを作成
    ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

    //通知を受け取るためのPendingIntentを作成
    Intent intent = new Intent(this, ActivityTransitionReceiver.class);
    intent.setAction("com.example.pbl_group1.TRANSITION_ACTION");
    activityTransitionPendingIntent = PendingIntent.getBroadcast(this, 0, intent,PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

    //APIクライアントを使って監視を開始
    activityRecognitionClient = ActivityRecognition.getClient(this);

// OSバージョンによって権限チェックの要否を判断する.このチェック入れないとエラー出る.
    boolean permissionGranted;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        // Android 10以上：権限があるかチェック
        permissionGranted = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED);
    } else {
        // Android 9以前：権限は不要なので、常に許可されていると見なす
        permissionGranted = true;
    }

    if (permissionGranted) {
        Task<Void> task = activityRecognitionClient.requestActivityTransitionUpdates(request, activityTransitionPendingIntent);

        task.addOnSuccessListener(
                result -> Log.d(TAG, "監視リクエストが正常に登録されました。")
        );
        task.addOnFailureListener(
                e -> Log.e(TAG, "監視リクエストの登録に失敗しました: " + e.getMessage())
        );
    } else {
        Log.e(TAG, "ACTIVITY_RECOGNITION の権限がありません。");
    }
    }


}