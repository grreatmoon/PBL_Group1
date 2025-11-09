package com.example.pbl_gruop1;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

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
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();

        if (args != null) {
            // Bundleから情報を取り出す
            String areaId = args.getString(ARG_AREA_ID);
            String name = args.getString(ARG_NAME);
            boolean isUnlocked = args.getBoolean(ARG_IS_UNLOCKED);
            boolean isChallengeable = args.getBoolean(ARG_IS_ENEMY_HERE);

            builder.setTitle(name); // ダイアログのタイトルは共通

            if (isUnlocked) {
                //解放済みの場合の処理

                Title title = (Title) args.getSerializable("title");

                String titleName;
                if (title != null) {
                    titleName = title.getName(); // 称号が見つかればその名前を取得
                } else {
                    //念のため、見つからない場合の安全な表示を用意
                    titleName = "（称号情報なし）";
                }

                String message = "獲得称号: " + titleName + "\n\n"
                        + "ステータス: 解放済み";

                //もしこのエリアに敵がいるなら戻るボタン+バトル画面への遷移
                if (isChallengeable) {
                    message += "\n\n" + "！！敵が出現しました！！";

                builder.setMessage(message)
                        //戻るボタン
                        .setNegativeButton("戻る", (dialog, id) -> dialog.dismiss())
                        //"敵に挑戦する"ボタン
                        .setPositiveButton("敵に挑戦する(エネルギーを80消費)", (dialog, id) -> {

                            // Contextがnullだとクラッシュするので安全のためにチェック
                            android.content.Context ctx = getActivity();
                            if (ctx == null) return;

                            //最新のプレイヤーデータをその場で読み込む
                            PlayerData currentPlayerData = GameDataManager.getInstance().loadPlayerData(ctx);
                            int battleCost = 80;

                            //エネルギーが足りるかチェック
                            if (currentPlayerData.energy >= battleCost) {
                                //エネルギー消費
                                currentPlayerData.energy -= battleCost;
                                //減らした結果を即座に保存する
                                GameDataManager.getInstance().savePlayerData(ctx, currentPlayerData);

                                //ここからが画面遷移
                                //遷移先のBattleGamenseni.javaは画面遷移を成立させる為だけの空っぽのファイル. 遷移後一瞬で元の画面に戻ってくる
                                Intent intent = new Intent(ctx, BattleGamenseni.class);
                                //遷移先にどのエリアの敵かという情報（areaId）を渡す
                                intent.putExtra("AREA_ID", areaId);
                                //画面遷移の実行
                                ctx.startActivity(intent);
                            } else {
                                //エネルギーが足りない場合
                                Toast.makeText(ctx, "エネルギーが足りません！ (必要: " + battleCost + ", 現在: " + currentPlayerData.energy + ")", Toast.LENGTH_LONG).show();
                            }
                        });
                } else { //このエリアに敵がいないなら戻るボタンのみ
                    builder.setMessage(message)
                            .setPositiveButton("戻る", (dialog, id) -> dialog.dismiss());
                }
            } else {
                //未解放の場合の処理
                String message = "獲得称号: ？？？\n\n"
                        + "ステータス: 未解放";

                builder.setMessage(message)
                        .setPositiveButton("戻る", (dialog, id) -> dialog.dismiss());
            }
            return builder.create();
        }
        builder.setTitle("エラー")
                .setMessage("情報を表示できませんでした。")
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        return builder.create();
    }
}