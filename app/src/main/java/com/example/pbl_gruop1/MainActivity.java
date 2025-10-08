package com.example.pbl_gruop1;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Test Nishino
    //test Oishi
    private static final int PERMISSION_REQUEST_CODE = 1001;
    //code for verifying permission request

    private final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
    };

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//  onCrerate関数内でXMLレイアウトを読み込んで
        super.onCreate(savedInstanceState);

        checkAndRequestPermissions();
        //gpsやinternet等の許可をチェック

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //デザインしたXMLレイアウトファイルをメモリ上に読み込む
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });
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

        for(String permission : REQUIRED_PERMISSIONS){
           if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
               permissionsToRequest.add(permission);
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
                startAppInitialization();
            } else {
                Toast.makeText(this, "requests for using this app got rejected",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startAppInitialization(){
        Log.d("INIT","All requests got cleared.proceeding to next step");
        Toast.makeText(this,"All requests got cleared.proceeding to next step",Toast.LENGTH_SHORT).show();
    }

}