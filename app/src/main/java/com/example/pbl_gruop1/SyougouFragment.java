package com.example.pbl_gruop1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class SyougouFragment extends Fragment {
    public SyougouFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_syougou, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button syougou1Button = view.findViewById(R.id.syougou_1_button);
        Button syougou2Button = view.findViewById(R.id.syougou_2_button);

        syougou1Button.setOnClickListener(v -> {
            showDetailDialog("称号１（仮）", "（施設名）開放", R.drawable.syougou_image_1);
        });
        syougou2Button.setOnClickListener(v -> {
            showDetailDialog("？？？", "プレイヤーレベル〇到達", R.drawable.syougou_image_2);
        });
    }

    /**
      称号の詳細ダイアログを表示する為のメソッド
      @param title 称号名
      @param message 称号の説明文
      @param imageResId 画像のリソース？ID
     */
    private void showDetailDialog(String title, String message, int imageResId) {
        if (getContext() == null) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_syougou_detail, null);

        ImageView dialogImage = dialogView.findViewById(R.id.dialog_image);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);

        dialogImage.setImageResource(imageResId);
        dialogMessage.setText(message);

        new AlertDialog.Builder(getContext())
                .setTitle(title) // タイトル設定
                //.setMessage(message) // メッセージ設定
                .setView(dialogView)
                .setPositiveButton("閉じる", (dialog, which) -> {
                    // 閉じるボタンの処理
                    dialog.dismiss();
                })
                .show(); // ダイアログの表示
    }

}