package com.example.pbl_gruop1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;
import android.widget.ImageButton;

import com.example.pbl_gruop1.databinding.FragmentStartBinding;

public class StartFragment extends Fragment {

    //UI部品とReceiverを変数として持てるようにする
    private TextView levelTextView;
    private TextView kaihouritsuTextView;
    private BroadcastReceiver updateReceiver;
    private TextView energyText;
    private ProgressBar energyProgressBar;
    private ProgressBar kaihouritsuProgressBar;
    private TextView statusText;
    private static final String TAG = "StartFragment";
    private FragmentStartBinding binding;
    private PlayerData playerData;

    public StartFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    // ブロードキャストを受け取るためのReceiver
    private BroadcastReceiver energyUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // エネルギー更新の通知を受け取ったら、画面表示を更新する
            Log.d("StartFragment", "エネルギー更新の通知を受信しました。");
            updateEnergyDisplay();
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //UIを取得
        levelTextView = view.findViewById(R.id.level_text);
        kaihouritsuTextView = view.findViewById(R.id.kaihouritsu_text);
        energyText = view.findViewById(R.id.energy_text);
        energyProgressBar = view.findViewById(R.id.energy_progress_bar);
        kaihouritsuProgressBar = view.findViewById(R.id.kaihouritsu_progress_bar);
        statusText = view.findViewById(R.id.status_text);


        //fragment_start.xmlで定義したボタンのIDを指定
        ImageButton toMapButton = view.findViewById(R.id.to_map_button);
        toMapButton.setOnClickListener(v -> {
            //nav_graph.xmlで定義したAction IDによって遷移
            NavHostFragment.findNavController(StartFragment.this)
                    .navigate(R.id.action_startFragment_to_mapFragment);
        });

        ImageButton toSyougouButton = view.findViewById(R.id.to_syougou_button);
        toSyougouButton.setOnClickListener(v -> {
            //nav_graph.xmlで定義したAction IDによって遷移
            NavHostFragment.findNavController(StartFragment.this)
                    .navigate(R.id.action_startFragment_to_syougouFragment);
        });

        //Infoボタンのリスナー(Debugボタン)
        view.findViewById(R.id.debug_button).setOnClickListener(v -> {
            NavHostFragment.findNavController(StartFragment.this)
                    .navigate(R.id.action_startFragment_to_debugFragment);
        });

        view.findViewById(R.id.caption_button).setOnClickListener(v -> {
            NavHostFragment.findNavController(StartFragment.this)
                    .navigate(R.id.action_startFragment_to_captionFragment);
        });

        //Receiverを初期化
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.pbl_gruop1.TITLE_DATA_UPDATED".equals(intent.getAction())){
                    Log.d(TAG, "称号データ更新のお知らせを受け取りました");
                    updateUI();
                }
            }
        };
        updateUI();
    }

    //UI更新処理用メソッド
    private void updateUI() {
     if (getContext() == null)return;
        //データを読み込む
        GameDataManager dataManager = GameDataManager.getInstance();
        PlayerData playerData = dataManager.loadPlayerData(getContext());

        //解放率を計算
        AreaManager areaManager = AreaManager.getInstance();
        int unlockedCount = playerData.unlockedAreaIds.size();  //解放済みのエリア数
        int totalCount = areaManager.getAreaList().size();      //全エリアの数
        double liberationRate = 0.0;
        if (totalCount > 0) {
            liberationRate = (double) unlockedCount / totalCount * 100.0;
        }

        //UIにデータを表示する
        if (levelTextView != null) {
            levelTextView.setText("討伐数：" + playerData.ufoDefeatCount);
        }
        if (kaihouritsuTextView != null) {
            kaihouritsuTextView.setText("解放率");
        }
        if (kaihouritsuProgressBar != null) {
            kaihouritsuProgressBar.setProgress((int) liberationRate);
        }

        //エネルギーのUI更新
        if (energyText != null) {
            energyText.setText(playerData.energy + " / " + playerData.maxEnergy);
        }
        if (energyProgressBar != null) {
            energyProgressBar.setMax(playerData.maxEnergy);
            energyProgressBar.setProgress(playerData.energy);
        }

        //状態テキストを更新(デバッグ用)
        if (statusText != null) {
            statusText.setText(playerData.currentStatus);
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        // 1. 日付更新チェックの前に、まず最新のプレイヤーデータを読み込む
        PlayerData loadedPlayerData = GameDataManager.getInstance().loadPlayerData(requireContext());
        // EnemyManagerに日付が変更されていないか確認させ、変更されていればエネルギーリセットなどを実行させる
        EnemyManager.getInstance().checkAndProcessDailyUpdates(requireContext(), loadedPlayerData);
        Log.d("StartFragment", "日付更新チェックを実行しました。");

        // 2. 画面の表示を最新の状態に更新
        updateEnergyDisplay();

        // 3. ブロードキャストレシーバーを登録
        IntentFilter filter = new IntentFilter("com.example.pbl_gruop1.ENERGY_UPDATED");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(energyUpdateReceiver, filter);
        Log.d("StartFragment", "エネルギー更新用の受信機を登録しました。");
        /*//画面が表示されるときに受信機を登録
        IntentFilter filter = new IntentFilter("com.example.pbl_gruop1.TITLE_DATA_UPDATED");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(updateReceiver, filter);
        Log.d(TAG, "データ更新受信機を登録");
        //画面に戻ってきたときも必ずUIを更新する
        updateUI();*/
    }

    @Override
    public void onPause() {
        super.onPause();
        // アプリが非表示になる際にレシーバーの登録を解除
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(energyUpdateReceiver);
        Log.d("StartFragment", "エネルギー更新用の受信機を解除しました。");
        /*//画面が見えなくなるときに受信機を解除
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateReceiver);
        Log.d(TAG, "データ更新受信機を解除しました。");*/
    }

    private void updateEnergyDisplay() {
        if (getContext() == null) {
            return;
        }
        PlayerData playerData = GameDataManager.getInstance().loadPlayerData(getContext());
        if (playerData == null) {
            return;
        }
        if (energyText != null) {
            energyText.setText(playerData.energy + " / " + playerData.maxEnergy);
        }
        if (energyProgressBar != null) {
            energyProgressBar.setMax(playerData.maxEnergy);
            energyProgressBar.setProgress(playerData.energy);
        }
        Log.d(TAG, "エネルギー表示を更新しました: " + playerData.energy);
    }
}
