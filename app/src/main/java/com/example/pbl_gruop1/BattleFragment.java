package com.example.pbl_gruop1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

//バトル画面の操作
public class BattleFragment extends Fragment {

    private static final String TAG = "BattleFragment";
    private static final long GAME_TIME_MS = 10000; // 制限時間 (10秒)
    private static final int REQUIRED_TAPS = 30; // 勝利に必要なタップ数

    // UI部品
    private TextView timerText;
    private TextView tapCountText;
    private Button attackButton;
    private ImageView ufoImage;
    private ProgressBar timeProgressBar;

    // ゲームロジック用
    private CountDownTimer gameTimer;
    private int tapCount = 0;
    private boolean isGameFinished = false;
    private String battleAreaId; // MapFragmentから渡された「戦うUFOのエリアID」

    // アニメーション用
    private Handler animationHandler = new Handler(Looper.getMainLooper());
    private boolean isUfoImage1 = true;

    //ふわふわ移動用
    private android.animation.AnimatorSet ufoFloatAnimatorSet;
    public BattleFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_battle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI部品をレイアウト(XML)から見つける
        timerText = view.findViewById(R.id.timer_text);
        tapCountText = view.findViewById(R.id.tap_count_text);
        attackButton = view.findViewById(R.id.attack_button);
        timeProgressBar = view.findViewById(R.id.time_progress_bar);
        ufoImage = view.findViewById(R.id.ufo_image);
        timeProgressBar.setMax((int) GAME_TIME_MS);
        timeProgressBar.setProgress((int) GAME_TIME_MS);
        timeProgressBar.setMax((int) GAME_TIME_MS);
        timeProgressBar.setProgress((int) GAME_TIME_MS);
        // MapFragmentから渡された「BATTLE_AREA_ID」を受け取る
        if (getArguments() != null) {
            battleAreaId = getArguments().getString("BATTLE_AREA_ID");
        }
        if (battleAreaId == null) {
            Log.e(TAG, "エリアIDが渡されませんでした。マップに戻ります。");
            navigateBackToMap();
            return;
        }

        // 連打ボタンの処理
        attackButton.setOnClickListener(v -> {
            if (!isGameFinished) {
                tapCount++;
                tapCountText.setText("カウント: " + tapCount);
            }
        });

        //UFOアニメーションを開始(回転しているように見えるやつ)
        startUfoAnimation();
        //ふわふわ動くアニメーションを開始
        startFloatingAnimation();

        //ゲームタイマーを開始
        startGameTimer();
    }

    //UFOを上下左右に動かすアニメーション
    private void startFloatingAnimation() {
        if (ufoImage == null) return; // UFO画像がなければ何もしない

        //上下（Y軸）の動き
        //1.5秒かけて、現在の位置(0f)から 30f 下の位置まで移動する
        android.animation.ObjectAnimator floatY = android.animation.ObjectAnimator.ofFloat(ufoImage, "translationY", 0f, 120f);
        floatY.setDuration(1500); // 1.5秒
        floatY.setRepeatCount(android.animation.ObjectAnimator.INFINITE); // 無限に繰り返す
        floatY.setRepeatMode(android.animation.ObjectAnimator.REVERSE); // 往復（下まで行ったら上に戻る）
        floatY.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator()); // ゆっくり始まってゆっくり終わる

        //左右（X軸）の動き
        //2秒かけて、左(-20f)から右(20f)まで移動する
        android.animation.ObjectAnimator floatX = android.animation.ObjectAnimator.ofFloat(ufoImage, "translationX", -120f, 120f);
        floatX.setDuration(2000); // 2秒（Y軸とタイミングをずらす）
        floatX.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        floatX.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        floatX.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        //上下と左右の動きを同時に再生する
        ufoFloatAnimatorSet = new android.animation.AnimatorSet();
        ufoFloatAnimatorSet.playTogether(floatY, floatX);
        ufoFloatAnimatorSet.start();
    }

    // 10秒のカウントダウンタイマー
    private void startGameTimer() {
        gameTimer = new CountDownTimer(GAME_TIME_MS, 100) { // 10秒間、0.1秒ごとに更新

            // 0.1秒ごとに呼ばれる
            @Override
            public void onTick(long millisUntilFinished) {
                // 残り時間を秒単位で表示
                timerText.setText("残り時間: " + (millisUntilFinished / 1000 + 1));

                timeProgressBar.setProgress((int) millisUntilFinished);
            }

            // 10秒経ったら呼ばれる
            @Override
            public void onFinish() {
                isGameFinished = true;
                timerText.setText("終了！");
                attackButton.setEnabled(false); // ボタンを押せなくする
                stopUfoAnimation(); // アニメーション停止

                timeProgressBar.setProgress(0);

                // 勝敗判定
                checkResult();
            }
        };
        gameTimer.start();
    }

    // 勝敗判定と結果の処理
    private void checkResult() {
        if (getContext() == null) return;

        GameDataManager dataManager = GameDataManager.getInstance();
        PlayerData playerData = dataManager.loadPlayerData(getContext());
        boolean titleAdded = checkTapTitle(playerData, tapCount);
        //タップ数を確認して称号を与えるか与えないか判断
        if (tapCount >= REQUIRED_TAPS) {
            // 勝利
            Log.d(TAG, "勝利！ " + battleAreaId + " のUFOを倒した。");
            //敵を倒したことを通知
            EnemyManager.getInstance().setTodayEnemyAsDefeated(getContext());
            // 防衛日数カウンターを更新する
            new BattleResult().Defencebattle(getContext(), playerData,battleAreaId);
            // PlayerDataのUFOリストから、倒したエリアIDを削除
            playerData.ufoAreaIds.remove(battleAreaId);
            dataManager.savePlayerData(getContext(), playerData); // 保存

            // 勝利ダイアログ
            new AlertDialog.Builder(getContext())
                    .setTitle("勝利！")
                    .setMessage("UFOを撃退した！\n（" + tapCount + "回タップ）")
                    .setPositiveButton("OK", (dialog, which) -> navigateBackToMap())
                    .setCancelable(false) // 戻るボタンで消せないように
                    .show();

        } else {
            // ★ 敗北
            Log.d(TAG, "敗北... UFOはまだ残っている。");

            if (titleAdded) {
                dataManager.savePlayerData(getContext(), playerData);
            }

            // 敗北ダイアログ（データは変更しない）
            new AlertDialog.Builder(getContext())
                    .setTitle("敗北...")
                    .setMessage("UFOの撃退に失敗した...\n（" + tapCount + "回タップ）")
                    .setPositiveButton("OK", (dialog, which) -> navigateBackToMap())
                    .setCancelable(false)
                    .show();
        }

        // どちらの場合もUI更新のお知らせを送信（マップ画面のUFOを消すため）
        Intent intent = new Intent("com.example.pbl_gruop1.TITLE_DATA_UPDATED");
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    // マップ画面に戻る処理
    private void navigateBackToMap() {
        if (isAdded()) { // FragmentがActivityに追加されているか確認
            NavHostFragment.findNavController(BattleFragment.this).popBackStack();
        }
    }

    // ステップ6: UFOアニメーションのロジック
    private Runnable ufoAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            if (ufoImage == null) return;

            if (isUfoImage1) {
                ufoImage.setImageResource(R.drawable.ufo_image_2); // 2枚目の画像
            } else {
                ufoImage.setImageResource(R.drawable.ufo_image_1); // 1枚目の画像
            }
            isUfoImage1 = !isUfoImage1;

            // 200ミリ秒 (0.2秒) ごとに画像を切り替える
            animationHandler.postDelayed(this, 200);
        }
    };

    private void startUfoAnimation() {
        animationHandler.post(ufoAnimationRunnable);
    }

    private void stopUfoAnimation() {
        animationHandler.removeCallbacks(ufoAnimationRunnable);
        if (ufoFloatAnimatorSet != null) {
            ufoFloatAnimatorSet.cancel();
        }
    }

    // 画面が破棄されるときにタイマーを止める（メモリリーク防止）
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        stopUfoAnimation();
    }
    private boolean checkTapTitle(PlayerData playerData, int taps) {
        if (taps > 80) {
            String newTitleId = "title_god_finger";
            if (!playerData.unlockedTitleIds.contains(newTitleId)) {
                playerData.unlockedTitleIds.add(newTitleId);

                // (任意) 称号獲得をトーストで通知
                // android.widget.Toast.makeText(getContext(), "称号「神の指」を獲得！", android.widget.Toast.LENGTH_SHORT).show();

                return true; // データが変更された
            }
        }
        return false; // データ変更なし
    }
}