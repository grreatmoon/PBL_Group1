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
import android.view.ScaleGestureDetector;
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

//View.OnTouchListener を実装
public class MapFragment extends Fragment implements View.OnTouchListener {

    //マップ操作にタッチイベントが全部吸われてるからマップ操作とボタン操作を切り替えられるように変更
    private boolean isMapInteractionEnabled = true; //trueならマップ操作モード, falseならエリア操作モード
    private static final float PAN_SENSITIVITY = 1.5f; //ドラッグ感度
    private FragmentMapBinding binding;

    private View.OnTouchListener mapTouchListener;
    private android.animation.AnimatorSet ufoAnimatorSet;

    //タッチ操作関連の変数
    private GestureDetector gestureDetector; //ダブルタップ検出用
    private boolean isZooming = false; //ダブルタップ後のズーム操作中かどうかのフラグ
    private static final float ZOOM_SENSITIVITY = 0.005f; // ズームの感度

    private float scaleFactor = 1.0f; //タッチ操作でのスケール
    private float minScaleFactor = 0.5f; //縮小の最小値を保持する変数
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

        //GestureDetectorを初期化
        gestureDetector = new GestureDetector(requireContext(), new GestureListener());

        //binding.mapContainer.setOnTouchListener(this);
        //mapContainerではなく、その親であるroot viewにリスナーを設定
        //binding.getRoot().setOnTouchListener(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //マップのレイアウトが完了した後に最小スケールを計算する
        binding.mapContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //一度だけ実行すれば良いので、リスナーをすぐに削除
                binding.mapContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                View parent = (View) binding.mapContainer.getParent();
                if (parent == null) return;

                float viewWidth = binding.mapContainer.getWidth();
                float viewHeight = binding.mapContainer.getHeight();
                float parentWidth = parent.getWidth();
                float parentHeight = parent.getHeight();

                if (viewWidth > 0 && viewHeight > 0) {
                    //横幅に合わせる場合のスケール値
                    float scaleX = parentWidth / viewWidth;
                    //高さで合わせる場合のスケール値
                    float scaleY = parentHeight / viewHeight;
                    //より小さい方を最小スケール値とする (これにより全体が収まる)
                    minScaleFactor = Math.min(scaleX, scaleY);

                    //初期のスケールが最小値より小さい場合は調整
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
                if ("com.example.pbl_gruop1.TITLE_DATA_UPDATED".equals(intent.getAction())) {
                    Log.d(TAG, "称号データ更新のお知らせを受け取りました");
                    updateUnlockedAreas();
                }
            }
        };
        //初回のマスク更新
        updateUnlockedAreas();

        //戻るボタン
        Button backButton = view.findViewById(R.id.button_back_to_start_from_map);
        backButton.setOnClickListener(v ->
            NavHostFragment.findNavController(MapFragment.this)
                    .navigate(R.id.action_mapFragment_to_startFragment));

        //モード切替ボタンの処理
        binding.buttonToggleMode.setOnClickListener(v -> {
            //フラグを反転させる
            isMapInteractionEnabled = !isMapInteractionEnabled;

            if (isMapInteractionEnabled) {
                //マップ操作モード
                binding.buttonToggleMode.setText("マップ操作モード");
                //エリアボタンのクリックを無効化し、ドラッグの邪魔をさせない
                setMaskButtonClickable(false);
            } else {
                //エリア選択モード
                binding.buttonToggleMode.setText("エリア選択モード");
                //エリアボタンのクリックを有効化
                setMaskButtonClickable(true);
            }
        });

        //エリアボタンのクリックリスナーを設定
        setupAreaButtonClickListeners();
        //タッチリスナーを最初から設定
        binding.mapContainer.setOnTouchListener(this);
        //起動時はマップ操作モード (isMapInteractionEnabled = true) なので、
        //エリアボタンを最初から「クリック不可」に設定しておく
        setMaskButtonClickable(false);
        binding.zoomInButton.setOnClickListener(v -> {
            zoomMap(1.25f); // 1.25倍
        });

        binding.zoomOutButton.setOnClickListener(v -> {
            zoomMap(0.8f); // 0.8倍 (1 / 1.25)
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

    //タッチイベントを処理するメソッド
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private void setMaskButtonClickable(boolean clickable) {
        if (binding == null) return;
        binding.maskMyosenji.setClickable(clickable);
        binding.maskGenkipark.setClickable(clickable);
        binding.maskKoshiCityHall.setClickable(clickable);
        binding.maskLutherChurch.setClickable(clickable);
        binding.maskCountryPark.setClickable(clickable);
        binding.maskBentenMountain.setClickable(clickable);
    }

    //ダブルタップとスクロール(ドラッグ移動)を検出するためのインナークラス
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //マップ操作モードでなければ、何もせずfalseを返す
            if (!isMapInteractionEnabled) {
                return false;
            }

            //マップを移動させる.感度を少し高めてる
            posX -= distanceX * PAN_SENSITIVITY;
            posY -= distanceY * PAN_SENSITIVITY;
            clampTranslations();
            applyTranslation();
            return true; //イベントを処理したのでtrue
        }


        @Override
        public boolean onDown(MotionEvent e) {
            //マップ操作モードの時だけtrueを返す
            //マップ操作モードの時はtrue,エリア選択モードの時はfalseを返す
            return isMapInteractionEnabled;
        }
    }


    //onTouchメソッド⇒GestureListener, ScaleListenerへイベントを知らせる受付係を担当
    //GestureListener⇒onScroll, onDoubleTapEvent, onDownの指一本での操作を担当
    //ScaleListener⇒onScaleの指二本操作を担当
    //ここから以前の記述を復帰させるならコメントアウト外す

        /*if (!isMapInteractionEnabled) {
            return false;
        }
        // 【マップ操作モード】の場合
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // ピンチ操作中でなければ、マップを移動させる
                if (!scaleDetector.isInProgress()) {
                    final float dx = event.getX() - lastTouchX;
                    final float dy = event.getY() - lastTouchY;
                    posX += dx;
                    posY += dy;
                }
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                clampTranslations();
                applyTranslation();
                break;
        }
        // マップ操作モードでは、イベントを消費したことを示すためにtrueを返す
        return true;
    }

    // --- ダブルタップを検出するためのインナークラス ---
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            // ダブルタップされたら、現在のスケールに応じて拡大または縮小する
            if (scaleFactor > minScaleFactor * 1.5) { // ある程度拡大されていたら
                scaleFactor = minScaleFactor; // 最小サイズに戻す
            } else {
                scaleFactor = scaleFactor * 2.0f; // 2倍に拡大
            }
            // ピボット（拡大中心）をタップした点に設定
            binding.mapContainer.setPivotX(e.getX());
            binding.mapContainer.setPivotY(e.getY());
            updateScale();
            clampTranslations();
            applyTranslation();
            return true;
            /*
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
        }*/

/*
        @Override
        public boolean onDown(MotionEvent e) {
            // これをtrueにしないと他のジェスチャーが認識されない
            return true;
        }
    }

    //ピンチ操作でズームする
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            // ピボット（拡大中心）をピンチ操作の中心点に設定
            binding.mapContainer.setPivotX(detector.getFocusX());
            binding.mapContainer.setPivotY(detector.getFocusY());
            updateScale();
            return true;
        }
    }*/

    //エリアボタンのリスナー設定をまとめるメソッド
    private void setupAreaButtonClickListeners() {
        binding.maskMyosenji.setOnClickListener(v -> showAreaInfoDialog("Myosenji"));
        binding.maskGenkipark.setOnClickListener(v -> showAreaInfoDialog("GenkiPark"));
        binding.maskKoshiCityHall.setOnClickListener(v -> showAreaInfoDialog("KoshiCityHall"));
        binding.maskLutherChurch.setOnClickListener(v -> showAreaInfoDialog("LutherChurch"));
        binding.maskCountryPark.setOnClickListener(v -> showAreaInfoDialog("CountryPark"));
        binding.maskBentenMountain.setOnClickListener(v -> showAreaInfoDialog("BentenMountain"));
    }

    private void showAreaInfoDialog(String areaId) {
        if (getContext() == null) return;

        //モードチェック: エリア選択モードでなければダイアログは表示しない
        if (isMapInteractionEnabled) {
            // 親切なフィードバックをユーザーに与える
            android.widget.Toast.makeText(getContext(), "エリア選択モードに切り替えてください", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        Area targetArea = AreaManager.getInstance().getAreaById(areaId);
        if (targetArea == null) {
            Log.e(TAG, "指定されたエリアIDが見つかりません: " + areaId);
            return;
        }

        //プレイヤーデータをロード
        PlayerData playerData = GameDataManager.getInstance().loadPlayerData(getContext());

//        //EnemyManagerに日付更新とそれに伴うペナルティ, 再抽選処理を実行させる
//        EnemyManager.getInstance().checkAndProcessDailyUpdates(getContext(), playerData);
//
//        //上の処理でエリアが没収されている可能性を考慮してプレイヤーデータを再読み込みする
//        playerData = GameDataManager.getInstance().loadPlayerData(getContext());

        //エリアIDがリストに含まれているかを .contains() でチェック
        boolean isUnlocked = playerData.unlockedAreaIds.contains(areaId);

        //このエリアに今日, 挑戦可能な敵がいるか確認する
        boolean isChallengeable = EnemyManager.getInstance().isEnemyChallengeable(areaId);

        //対応する称号情報を取得
        Title title = TitleManager.getInstance().getTitleByAreaId(areaId);

        AreaInfoDialogFragment dialog = AreaInfoDialogFragment.newInstance(
                areaId,
                targetArea.getName(),
                isUnlocked,
                title,
                isChallengeable
        );
        dialog.show(getChildFragmentManager(), "AreaInfoDialog");
    }

    //スケール更新処理を共通化
    private void updateScale() {
        //拡大・縮小率に制限を設ける (下限はminScaleFactor)
        scaleFactor = Math.max(minScaleFactor, Math.min(scaleFactor, 3.5f));
        binding.mapContainer.setScaleX(scaleFactor);
        binding.mapContainer.setScaleY(scaleFactor);
    }
    private void zoomMap(float multiplier) {
        if (binding == null) return;

        //画面中央をズームの中心にする
        binding.mapContainer.setPivotX(binding.mapContainer.getWidth() / 2f);
        binding.mapContainer.setPivotY(binding.mapContainer.getHeight() / 2f);

        //スケールを更新
        scaleFactor *= multiplier;

        //共通メソッド呼び出し
        updateScale();
        clampTranslations();
        applyTranslation();
    }
    private void startFloatingAnimation(View ufoView) {
        //既に別のアニメーションが動いていたら止める
        if (ufoAnimatorSet != null && ufoAnimatorSet.isRunning()) {
            ufoAnimatorSet.cancel();
        }

        //上下（Y軸）の動き (1.5秒で10px下に)
        android.animation.ObjectAnimator floatY = android.animation.ObjectAnimator.ofFloat(ufoView, "translationY", 0f, 50f);
        floatY.setDuration(1500);
        floatY.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        floatY.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        floatY.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        //左右（X軸）の動き (2秒で左右に5pxずつ)
        android.animation.ObjectAnimator floatX = android.animation.ObjectAnimator.ofFloat(ufoView, "translationX", -25f, 25f);
        floatX.setDuration(2000);
        floatX.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        floatX.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        floatX.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        //上下と左右の動きを同時に再生する
        ufoAnimatorSet = new android.animation.AnimatorSet();
        ufoAnimatorSet.playTogether(floatY, floatX);
        ufoAnimatorSet.start();
    }


    //位置更新処理を共通化
    private void applyTranslation() {
        if (binding == null) return;
        binding.mapContainer.setTranslationX(posX);
        binding.mapContainer.setTranslationY(posY);
    }

    //マップの移動範囲を制限するメソッド
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

        //X軸の移動範囲を補正
        if (scaledWidth > parentWidth) {
            //画像が画面より大きい場合、はみ出た分だけ移動可能
            minX = parentWidth - scaledWidth;
            maxX = parentWidth;
        } else {
            //画像が画面より小さい場合、中央に配置
            minX = (parentWidth - scaledWidth) / 2;
            maxX = (parentWidth - scaledWidth) / 2;
        }

        //Y軸の移動範囲を補正
        if (scaledHeight > parentHeight) {
            //画像が画面より大きい場合、はみ出た分だけ移動可能
            minY = parentHeight - scaledHeight;
            maxY = parentHeight;
        } else {
            //画像が画面より小さい場合、中央に配置
            minY = (parentHeight - scaledHeight) / 2;
            maxY = (parentHeight - scaledHeight) / 2;
        }

        //計算した範囲内に posX と posY を収める
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

        //敵の出現チェックをダイアログからこちらに移動
        EnemyManager.getInstance().checkAndProcessDailyUpdates(getContext(), playerData);
        //エリア没収が反映された可能性があるので、playerDataを再読み込み
        playerData = dataManager.loadPlayerData(getContext());

        List<String> unlockedIds = playerData.unlockedAreaIds;
        AreaManager areaManager = AreaManager.getInstance();

        //解放率の計算・表示
        int unlockedCount = playerData.unlockedAreaIds.size();
        int totalCount = areaManager.getAreaList().size();
        double liberationRate = 0.0;
        if (totalCount > 0) {
            liberationRate = (double) unlockedCount / totalCount * 100.0;
        }
        binding.kaihouritsuText.setText("解放率");
        binding.kaihouritsuProgressBar.setProgress((int) liberationRate);

        //ステータスの表示
        binding.levelText.setText("討伐数：" + playerData.ufoDefeatCount);
        binding.energyText.setText(playerData.energy + " / " + playerData.maxEnergy);
        binding.energyProgressBar.setMax(playerData.maxEnergy);
        binding.energyProgressBar.setProgress(playerData.energy);
        binding.statusText.setText(playerData.currentStatus);

        //マスクの表示・非表示
        updateMaskVisibility(binding.maskMyosenji, unlockedIds.contains("Myosenji"));
        updateMaskVisibility(binding.maskGenkipark, unlockedIds.contains("GenkiPark"));
        updateMaskVisibility(binding.maskKoshiCityHall, unlockedIds.contains("KoshiCityHall"));
        updateMaskVisibility(binding.maskLutherChurch, unlockedIds.contains("LutherChurch"));
        updateMaskVisibility(binding.maskCountryPark, unlockedIds.contains("CountryPark"));
        updateMaskVisibility(binding.maskBentenMountain, unlockedIds.contains("BentenMountain"));


        //既存のアニメーションを止める
        if (ufoAnimatorSet != null) {
            ufoAnimatorSet.cancel();
            ufoAnimatorSet = null;
        }

        //各エリアにUFOがいるかチェック
        boolean ufoMyosenji = EnemyManager.getInstance().isEnemyChallengeable("Myosenji");
        boolean ufoGenkipark = EnemyManager.getInstance().isEnemyChallengeable("GenkiPark");
        boolean ufoKoshiCityHall = EnemyManager.getInstance().isEnemyChallengeable("KoshiCityHall");
        boolean ufoLutherChurch = EnemyManager.getInstance().isEnemyChallengeable("LutherChurch");
        boolean ufoCountryPark = EnemyManager.getInstance().isEnemyChallengeable("CountryPark");
        boolean ufoBentenMountain = EnemyManager.getInstance().isEnemyChallengeable("BentenMountain");

        //表示・非表示を切り替え
        binding.ufoMyosenji.setVisibility(ufoMyosenji ? View.VISIBLE : View.GONE);
        binding.ufoGenkipark.setVisibility(ufoGenkipark ? View.VISIBLE : View.GONE);
        binding.ufoKoshiCityHall.setVisibility(ufoKoshiCityHall ? View.VISIBLE : View.GONE);
        binding.ufoLutherChurch.setVisibility(ufoLutherChurch ? View.VISIBLE : View.GONE);
        binding.ufoCountryPark.setVisibility(ufoCountryPark ? View.VISIBLE : View.GONE);
        binding.ufoBentenMountain.setVisibility(ufoBentenMountain ? View.VISIBLE : View.GONE);

        //表示したUFOのアニメーションを開始 (EnemyManagerはUFOが1体だけなので、if elseでOK)
        if (ufoMyosenji) {
            startFloatingAnimation(binding.ufoMyosenji);
        } else if (ufoGenkipark) {
            startFloatingAnimation(binding.ufoGenkipark);
        } else if (ufoKoshiCityHall) {
            startFloatingAnimation(binding.ufoKoshiCityHall);
        } else if (ufoLutherChurch) {
            startFloatingAnimation(binding.ufoLutherChurch);
        } else if (ufoCountryPark) {
            startFloatingAnimation(binding.ufoCountryPark);
        } else if (ufoBentenMountain) {
            startFloatingAnimation(binding.ufoBentenMountain);
        }
    }

    private void updateMaskVisibility(View maskView, boolean isUnlocked) {
        if (isUnlocked) {
            maskView.setAlpha(0.0f); //見た目を完全に透明にする
            maskView.setVisibility(View.VISIBLE); //ただし、存在はさせ続ける
        } else {
            maskView.setAlpha(1.0f); //見た目を不透明に戻す
            maskView.setVisibility(View.VISIBLE); //存在させる
        }
        //解放後もダイアログ表示させるためにgoneからvisible+alpha0に変更
    }
}