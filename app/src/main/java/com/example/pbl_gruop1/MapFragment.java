package com.example.pbl_gruop1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.fragment.app.Fragment;

import com.example.pbl_gruop1.databinding.FragmentMapBinding;
import java.util.List;

// View.OnTouchListener を実装
public class MapFragment extends Fragment implements View.OnTouchListener {

    private FragmentMapBinding binding;

    // --- タッチ操作関連の変数 ---
    private ScaleGestureDetector scaleGestureDetector;
    private boolean isTouchMoveEnabled = false;
    private float scaleFactor = 1.0f; // タッチ操作でのスケール
    private float lastTouchX;
    private float lastTouchY;
    private float posX = 0;
    private float posY = 0;

    private BroadcastReceiver updateReceiver;
    private static final String TAG = "MapFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);

        // ScaleGestureDetectorを初期化
        scaleGestureDetector = new ScaleGestureDetector(requireContext(), new ScaleListener());

        // mapContainerがタッチイベントを受け取れるようにリスナーを設定
        binding.mapContainer.setOnTouchListener(this);

        // 正しいreturn文
        return binding.getRoot();
    } // ★★★ onCreateViewはここで閉じる ★★★

    // onCreateViewの外に、onViewCreatedを正しく配置
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 移動量を定義
        final float moveDistance = 30f;

        // --- 十字キーボタンの処理 (移動方向を修正) ---
        // button1 (上へ移動)
        binding.button1.setOnClickListener(v -> binding.mapContainer.setTranslationY(binding.mapContainer.getTranslationY() + moveDistance));
        // button2 (下へ移動)
        binding.button2.setOnClickListener(v -> binding.mapContainer.setTranslationY(binding.mapContainer.getTranslationY() - moveDistance));
        // button4 (左へ移動)
        binding.button4.setOnClickListener(v -> binding.mapContainer.setTranslationX(binding.mapContainer.getTranslationX() + moveDistance));
        // button3 (右へ移動)
        binding.button3.setOnClickListener(v -> binding.mapContainer.setTranslationX(binding.mapContainer.getTranslationX() - moveDistance));

        // --- 拡大・縮小ボタンの処理 (scaleFactor変数で統一) ---
        binding.buttonBottomRightUpper.setOnClickListener(v -> {
            scaleFactor += 0.1f;
            updateScale();
        });

        binding.buttonBottomRightLower.setOnClickListener(v -> {
            // 縮小の下限を設定
            if (scaleFactor > 0.5f) {
                scaleFactor -= 0.1f;
                updateScale();
            }
        });

        // タッチ操作切り替えボタン
        binding.buttonToggleTouch.setOnClickListener(v -> {
            // isTouchMoveEnabled の true/false を反転させる
            isTouchMoveEnabled = !isTouchMoveEnabled;

            // ボタンの見た目を変えて、現在の状態を分かりやすくする
            if (isTouchMoveEnabled) {
                // オン（有効）のとき: syugou_image_1.png を設定
                binding.buttonToggleTouch.setImageResource(R.drawable.syougou_image_1);
            } else {
                // オフ（無効）のとき: not_available.png を設定
                binding.buttonToggleTouch.setImageResource(R.drawable.not_available);
            }
        });

        updateReceiver = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
              if ("com.example.pbl_gruop1.TITLE_DATA_UPDATED".equals(intent.getAction())){
                  Log.d(TAG, "称号データ更新のお知らせを受け取りました");
                  updateUnlockedAreas();    //お知らせが来たらマスクを更新
              }
          }
        };
        //初回のマスク更新
        updateUnlockedAreas();

    }

    @Override
    public void onResume() {
        super.onResume();
        // 画面が表示されるときに受信機を登録
        IntentFilter filter = new IntentFilter("com.example.pbl_gruop1.TITLE_DATA_UPDATED");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(updateReceiver, filter);
        Log.d(TAG, "データ更新受信機を登録");
        // 画面に戻ってきたときも必ずマスクを更新する
        updateUnlockedAreas();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 画面が見えなくなるときに受信機を解除
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateReceiver);
        Log.d(TAG, "データ更新受信機を解除しました。");
    }

    // --- タッチイベントを処理するメソッド ---
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // ピンチ操作の検出をScaleGestureDetectorに任せる
        scaleGestureDetector.onTouchEvent(event);

        if (!isTouchMoveEnabled) {
            return true; // イベントは処理済みとして終了
        }

        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // タッチ開始点を記録 (画面上の絶対座標であるgetRawX/Yが安定)
                lastTouchX = event.getRawX();
                lastTouchY = event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                // ピンチ操作中は移動させない
                if (scaleGestureDetector.isInProgress()) {
                    break;
                }
                // 移動後の座標を取得
                final float rawX = event.getRawX();
                final float rawY = event.getRawY();

                // 前回の座標からの移動量を計算
                final float dx = rawX - lastTouchX;
                final float dy = rawY - lastTouchY;

                // 画像の現在位置に移動量を加算
                posX += dx;
                posY += dy;
                binding.mapContainer.setTranslationX(posX);
                binding.mapContainer.setTranslationY(posY);

                // 現在の座標を次の計算のために保存
                lastTouchX = rawX;
                lastTouchY = rawY;
                break;
            }
        }
        return true; // イベントが処理されたことをシステムに伝える
    }

    // --- ピンチ操作を処理するインナークラス ---
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            updateScale();
            return true;
        }
    }

    // --- スケール更新処理を共通化 ---
    private void updateScale() {
        // 拡大・縮小率に制限を設ける (例: 0.5倍から3倍まで)
        scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));
        binding.mapContainer.setScaleX(scaleFactor);
        binding.mapContainer.setScaleY(scaleFactor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //エリア解放状態をUIに反映させるメソッド
    private void updateUnlockedAreas() {
        //Fragmentの環境情報(コンテキスト)やbindingがない場合は処理を中断
        if (getContext() == null || binding == null) return;

        //セーブデータを読み込む
        GameDataManager dataManager = GameDataManager.getInstance();
        PlayerData playerData = dataManager.loadPlayerData(getContext());
        List<String> unlockedIds = playerData.unlockedAreaIds;  //解放済みのIDリストを取得

        AreaManager areaManager = AreaManager.getInstance();
        int unlockedCount = playerData.unlockedAreaIds.size();  //解放済みのエリア数
        int totalCount = areaManager.getAreaList().size();      //全エリアの数
        double liberationRate = 0.0;
        if (totalCount > 0) {
            //割り算をして％を計算
            liberationRate = (double) unlockedCount / totalCount * 100.0;
        }

        // UIにデータを表示する
        binding.levelText.setText("Lv. " + playerData.level);
        binding.kaihouritsuText.setText("解放率： " + String.format("%.1f", liberationRate) + "%");

        //エネルギーのUI更新
        binding.energyText.setText("エネルギー: " + playerData.energy + " / " + playerData.maxEnergy);
        binding.energyProgressBar.setMax(playerData.maxEnergy);
        binding.energyProgressBar.setProgress(playerData.energy);
        binding.statusText.setText("状態: " + playerData.currentStatus);

        // まず、全てのマスクを一度「表示」状態に戻す
        binding.maskMyosenji.setVisibility(View.VISIBLE);
        binding.maskGenkipark.setVisibility(View.VISIBLE);
        binding.maskKoshiCityHall.setVisibility(View.VISIBLE);
        binding.maskLutherChurch.setVisibility(View.VISIBLE);
        binding.maskCountryPark.setVisibility(View.VISIBLE);
        binding.maskBentenMountain.setVisibility(View.VISIBLE);

        //解放済みのIDリストを元に対応するマスク画像を非表示にする
        for (String areaId : unlockedIds) {
            switch (areaId) {
                case "Myosenji":
                    binding.maskMyosenji.setVisibility(View.GONE);
                    break;
                case "GenkiPark":
                    binding.maskGenkipark.setVisibility(View.GONE);
                    break;
                case "KoshiCityHall":
                    binding.maskKoshiCityHall.setVisibility(View.GONE);
                    break;
                case "LutherChurch":
                    binding.maskLutherChurch.setVisibility(View.GONE);
                    break;
                case "CountryPark":
                    binding.maskCountryPark.setVisibility(View.GONE);
                    break;
                case "BentenMountain":
                    binding.maskBentenMountain.setVisibility(View.GONE);
                    break;
            }
        }
    }

}
