package com.example.pbl_gruop1;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

public class AreaInfoDialogFragment extends DialogFragment {

    //ダイアログのイベントを呼び出し元（MapFragment）に通知するインターフェース
    public interface AreaDialogListener {
        void onAreaUnlock(String areaId);
    }

    private AreaDialogListener listener;

    //ダイアログに渡す情報を定義する定数
    private static final String ARG_AREA_ID = "areaId";
    private static final String ARG_NAME = "name";
    private static final String ARG_IS_UNLOCKED = "isUnlocked";
    private static final String ARG_IS_ENEMY_HERE = "isEnemyHere";

    //ダイアログのインスタンスを生成(情報をBundleに詰める)
    public static AreaInfoDialogFragment newInstance(String areaId, String name, boolean isUnlocked, Title title, boolean isEnemyHere) {
        AreaInfoDialogFragment fragment = new AreaInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_AREA_ID, areaId);
        args.putString(ARG_NAME, name);
        args.putBoolean(ARG_IS_UNLOCKED, isUnlocked);
        args.putBoolean(ARG_IS_ENEMY_HERE, isEnemyHere);
        if (title != null) {
            args.putSerializable("title", title);
        }
        fragment.setArguments(args);
        return fragment;
    }

    //ダイアログの見た目を設定
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_area_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ダイアログの背景を透明にして、CardViewの角丸が活きるようにする
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // XMLから部品を見つける
        TextView titleText = view.findViewById(R.id.dialog_area_title);
        TextView messageText = view.findViewById(R.id.dialog_area_message);
        TextView enemyAlertText = view.findViewById(R.id.dialog_area_enemy_alert);
        Button positiveButton = view.findViewById(R.id.dialog_area_positive_button);
        Button negativeButton = view.findViewById(R.id.dialog_area_negative_button);

        // Bundleから情報を取り出す (以前onCreateDialogにあったもの)
        Bundle args = getArguments();
        if (args == null) {
            dismiss(); // データがなければ閉じる
            return;
        }

        String areaId = args.getString(ARG_AREA_ID);
        String name = args.getString(ARG_NAME);
        boolean isUnlocked = args.getBoolean(ARG_IS_UNLOCKED);
        boolean isChallengeable = args.getBoolean(ARG_IS_ENEMY_HERE);
        Title title = (Title) args.getSerializable("title");

        //防衛日数の取得
        PlayerData playerData = GameDataManager.getInstance().loadPlayerData(getContext());
        int defenceDays = playerData.consecutiveDefenceDaysMap.getOrDefault(areaId, 0);
        String defenceDaysText = "連続防衛日数: " + defenceDays + "日";

        // タイトルを設定
        titleText.setText(name);

        // 状態に応じてメッセージとボタンを設定
        if (isUnlocked) {
            // --- 解放済みの場合 ---
            String titleName = (title != null) ? title.getName() : "（称号情報なし）";
            String message = "獲得称号: " + titleName + "\n\n"
                    + "ステータス: 解放済み" + "\n\n"
                    + defenceDaysText;

            enemyAlertText.setVisibility(View.GONE); // まずは非表示にする

            if (isChallengeable) {
                // (敵がいる場合)
                messageText.setText(message);
                enemyAlertText.setText("※敵に挑戦するには\nエネルギーを80消費します");
                enemyAlertText.setVisibility(View.VISIBLE);
                //非表示→表示

                positiveButton.setText("挑戦");
                positiveButton.setOnClickListener(v -> {
                    handleBattleClick(areaId); // バトル処理を別メソッドに
                });

                negativeButton.setText("戻る");
                negativeButton.setOnClickListener(v -> dismiss());

            } else {
                // (敵がいない場合)
                messageText.setText(message);
                positiveButton.setText("OK");
                positiveButton.setOnClickListener(v -> dismiss());
                negativeButton.setVisibility(View.GONE); // 戻るボタンは1つで良い
            }

        } else {
            // --- 未解放の場合 ---
            String message = "獲得称号: ？？？\n\n"
                    + "ステータス: 未解放" + "\n\n"
                    + defenceDaysText;
            messageText.setText(message);

            positiveButton.setText("OK");
            positiveButton.setOnClickListener(v -> dismiss());
            negativeButton.setVisibility(View.GONE); // 戻るボタンは1つで良い
        }
    }
    private void handleBattleClick(String areaId) {
        Context ctx = getActivity();
        if (ctx == null) return;

        PlayerData currentPlayerData = GameDataManager.getInstance().loadPlayerData(ctx);
        int battleCost = 80;

        if (currentPlayerData.energy >= battleCost) {
            // エネルギー消費
            currentPlayerData.energy -= battleCost;
            GameDataManager.getInstance().savePlayerData(ctx, currentPlayerData);

            // BattleFragmentに渡すためのデータ（Bundle）を作成
            Bundle bundle = new Bundle();
            bundle.putString("BATTLE_AREA_ID", areaId);

            // NavControllerを使って BattleFragment に遷移する
            try {
                NavHostFragment.findNavController(AreaInfoDialogFragment.this)
                        .navigate(R.id.action_mapFragment_to_battleFragment, bundle);
            } catch (Exception e) {
                Log.e("AreaInfoDialog", "BattleFragmentへの遷移に失敗", e);
                Toast.makeText(ctx, "バトル画面への遷移に失敗しました", Toast.LENGTH_SHORT).show();
            }

            dismiss(); // 遷移と同時にダイアログを閉じる

        } else {
            // エネルギーが足りない場合
            Toast.makeText(ctx, "エネルギーが足りません！ (必要: " + battleCost + ", 現在: " + currentPlayerData.energy + ")", Toast.LENGTH_LONG).show();
        }
    }

}