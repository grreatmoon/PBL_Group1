package com.example.pbl_gruop1;

import android.content.BroadcastReceiver;import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
// import android.view.ScaleGestureDetector; // 不要なため削除
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver; // ★ Viewのサイズ取得のために追加
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.pbl_gruop1.databinding.FragmentMapBinding;
import java.util.List;

// View.OnTouchListener を実装
public class MapFragment extends Fragment implements View.OnTouchListener {

    private FragmentMapBinding binding;

    // --- タッチ操作関連の変数 ---
    private GestureDetector gestureDetector; // ダブルタップ検出用
    private boolean isZooming = false; // ダブルタップ後のズーム操作中かどうかのフラグ
    private static final float ZOOM_SENSITIVITY = 0.005f; // ズームの感度

    private float scaleFactor = 1.0f; // タッチ操作でのスケール
    private float minScaleFactor = 0.5f; // ★ 縮小の最小値を保持する変数
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

        // GestureDetectorを初期化
        gestureDetector = new GestureDetector(requireContext(), new GestureListener());

        // mapContainerではなく、その親であるroot viewにリスナーを設定
        binding.getRoot().setOnTouchListener(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ★★★ マップのレイアウトが完了した後に最小スケールを計算する ★★★
        binding.mapContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 一度だけ実行すれば良いので、リスナーをすぐに削除
                binding.mapContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                View parent = (View) binding.mapContainer.getParent();
                if (parent == null) return;

                float viewWidth = binding.mapContainer.getWidth();
                float viewHeight = binding.mapContainer.getHeight();
                float parentWidth = parent.getWidth();
                float parentHeight = parent.getHeight();

                if (viewWidth > 0 && viewHeight > 0) {
                    // 横幅に合わせる場合のスケール値
                    float scaleX = parentWidth / viewWidth;
                    // 高さで合わせる場合のスケール値
                    float scaleY = parentHeight / viewHeight;
                    // より小さい方を最小スケール値とする (これにより全体が収まる)
                    minScaleFactor = Math.min(scaleX, scaleY);

                    // 初期のスケールが最小値より小さい場合は調整
                    if (scaleFactor < minScaleFactor) {
                        scaleFactor = minScaleFactor;
                        updateScale();
                        clampTranslations();
                        applyTranslation();
                    }
                }
            }
        });


        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.pbl_gruop1.TITLE_DATA_UPDATED".equals(intent.getAction())){
                    Log.d(TAG, "称号データ更新のお知らせを受け取りました");
                    updateUnlockedAreas();
                }
            }
        };
        //初回のマスク更新
        updateUnlockedAreas();

        // 1. XMLで定義した「戻るボタン」をIDで探してくる
        Button backButton = view.findViewById(R.id.button_back_to_start_from_map);
        // 2. ボタンに「クリックリスナー」を設定する
        backButton.setOnClickListener(v -> {
            // 3. NavControllerを使って、指定した画面へ遷移する命令を出す
            //    このActionは nav_graph.xml で定義する必要があります
            NavHostFragment.findNavController(MapFragment.this)
                    .navigate(R.id.action_mapFragment_to_startFragment);
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.pbl_gruop1.TITLE_DATA_UPDATED");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(updateReceiver, filter);
        Log.d(TAG, "データ更新受信機を登録");
        updateUnlockedAreas();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateReceiver);
        Log.d(TAG, "データ更新受信機を解除しました。");
    }

    // --- タッチイベントを処理するメソッド ---
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 最初にジェスチャー検出器にイベントを渡す
        gestureDetector.onTouchEvent(event);

        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isZooming) {
                    // ダブルタップ後のズーム操作
                    float dy = event.getY() - lastTouchY;
                    scaleFactor -= dy * ZOOM_SENSITIVITY; // 下にスライドで縮小、上で拡大
                    updateScale();
                    lastTouchY = event.getY(); // 座標を更新
                } else {
                    // 通常のパン（移動）操作
                    final float dx = event.getX() - lastTouchX;
                    final float dy = event.getY() - lastTouchY;
                    posX += dx;
                    posY += dy;
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                }
                // 移動範囲の制限をかける
                clampTranslations();
                // 実際にViewの位置を更新
                applyTranslation();
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                // 指が離れたらズームモードを解除
                if (isZooming) {
                    isZooming = false;
                }
                // 移動範囲の最終チェック
                clampTranslations();
                applyTranslation();
                break;
            }
        }
        return true;
    }

    // --- ダブルタップを検出するためのインナークラス ---
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                // ダブルタップの2回目のタップが開始された
                isZooming = true;
                // 拡大縮小の中心をタップした場所に設定
                // ピボットの座標は、ビューのローカル座標系で指定する必要がある
                binding.mapContainer.setPivotX(e.getX() - posX);
                binding.mapContainer.setPivotY(e.getY() - posY);
                // Y座標の基準をリセット
                lastTouchY = e.getY();
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // これをtrueにしないと他のジェスチャーが認識されない
            return true;
        }
    }


    // --- スケール更新処理を共通化 ---
    private void updateScale() {
        // ★ 拡大・縮小率に制限を設ける (下限は動的に計算したminScaleFactor)
        scaleFactor = Math.max(minScaleFactor, Math.min(scaleFactor, 3.5f));
        binding.mapContainer.setScaleX(scaleFactor);
        binding.mapContainer.setScaleY(scaleFactor);
    }

    // --- 位置更新処理を共通化 ---
    private void applyTranslation() {
        if (binding == null) return;
        binding.mapContainer.setTranslationX(posX);
        binding.mapContainer.setTranslationY(posY);
    }

    // --- マップの移動範囲を制限するメソッド ---
    private void clampTranslations() {
        if (binding == null) return;

        View parent = (View) binding.mapContainer.getParent();
        if (parent == null) return;

        float viewWidth = binding.mapContainer.getWidth();
        float viewHeight = binding.mapContainer.getHeight();

        float scaledWidth = viewWidth * scaleFactor;
        float scaledHeight = viewHeight * scaleFactor;

        float parentWidth = parent.getWidth();
        float parentHeight = parent.getHeight();

        float minX, maxX, minY, maxY;

        // X軸の移動範囲を補正
        if (scaledWidth > parentWidth) {
            // 画像が画面より大きい場合、はみ出た分だけ移動可能
            minX = parentWidth - scaledWidth;
            maxX = parentWidth;
        } else {
            // 画像が画面より小さい場合、中央に配置
            minX = (parentWidth - scaledWidth) / 2;
            maxX = (parentWidth - scaledWidth) / 2;
        }

        // Y軸の移動範囲を補正
        if (scaledHeight > parentHeight) {
            // 画像が画面より大きい場合、はみ出た分だけ移動可能
            minY = parentHeight - scaledHeight;
            maxY = parentHeight;
        } else {
            // 画像が画面より小さい場合、中央に配置
            minY = (parentHeight - scaledHeight) / 2;
            maxY = (parentHeight - scaledHeight) / 2;
        }

        // 計算した範囲内に posX と posY を収める
        posX = Math.max(minX, Math.min(posX, maxX));
        posY = Math.max(minY, Math.min(posY, maxY));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //エリア解放状態をUIに反映させるメソッド
    private void updateUnlockedAreas() {
        if (getContext() == null || binding == null) return;

        GameDataManager dataManager = GameDataManager.getInstance();
        PlayerData playerData = dataManager.loadPlayerData(getContext());
        List<String> unlockedIds = playerData.unlockedAreaIds;

        AreaManager areaManager = AreaManager.getInstance();
        int unlockedCount = playerData.unlockedAreaIds.size();
        int totalCount = areaManager.getAreaList().size();
        double liberationRate = 0.0;
        if (totalCount > 0) {
            liberationRate = (double) unlockedCount / totalCount * 100.0;
        }

        // UIにデータを表示する
        binding.levelText.setText("Lv. " + playerData.level);
        binding.kaihouritsuText.setText("解放率： " + String.format("%.1f", liberationRate) + "%");
        binding.energyText.setText("エネルギー: " + playerData.energy + " / " + playerData.maxEnergy);
        binding.energyProgressBar.setMax(playerData.maxEnergy);
        binding.energyProgressBar.setProgress(playerData.energy);
        binding.statusText.setText("状態: " + playerData.currentStatus);

        // マスクの表示・非表示を効率的に更新
        updateMaskVisibility(binding.maskMyosenji, unlockedIds.contains("Myosenji"));
        updateMaskVisibility(binding.maskGenkipark, unlockedIds.contains("GenkiPark"));
        updateMaskVisibility(binding.maskKoshiCityHall, unlockedIds.contains("KoshiCityHall"));
        updateMaskVisibility(binding.maskLutherChurch, unlockedIds.contains("LutherChurch"));
        updateMaskVisibility(binding.maskCountryPark, unlockedIds.contains("CountryPark"));
        updateMaskVisibility(binding.maskBentenMountain, unlockedIds.contains("BentenMountain"));
    }

    private void updateMaskVisibility(View maskView, boolean isUnlocked) {
        maskView.setVisibility(isUnlocked ? View.GONE : View.VISIBLE);
    }
}
