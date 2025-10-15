//allTitlesリストとbutton_syougou_detail.xmlのレイアウトをつなぎ合わせてボタンの一括作成をする
package com.example.pbl_gruop1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

//RecyclerViewと称号データリスト(allTitles)を結びつけるためのアダプタークラス
public class SyougouAdapter extends RecyclerView.Adapter<SyougouAdapter.SyougouViewHolder>{
    private final List<SyougouMaster> syougouList;
    private final PlayerData playerData;
    private final Context context; //ダイアログ表示のためにContextを受け取る

    /**
     * アダプターのコンストラクタ
     * @param context 表示元のContext
     * @param syougouList 表示したい称号のマスターデータリスト
     * @param playerData プレイヤーの所持データ
     */
    public SyougouAdapter(Context context, List<SyougouMaster> syougouList, PlayerData playerData) {
        this.context = context;
        this.syougouList = syougouList;
        this.playerData = playerData;
    }

    /**
     * ViewHolderが新しく作成されるときに呼び出されるメソッド。
     * button_syougou_detail.xmlからレイアウトを読み込み、ViewHolderを生成する。
     */
    @NonNull
    @Override
    public SyougouViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.button_syougou_detail, parent, false);
        return new SyougouViewHolder(view);
    }

    /**
     * ViewHolderにデータが結びつけられるときに呼び出されるメソッド。
     * @param holder データがセットされるViewHolder
     * @param position リスト内のデータの位置(0スタート)
     */
    @Override
    public void onBindViewHolder(@NonNull SyougouViewHolder holder, int position) {
        // 表示する称号データをリストから取得
        SyougouMaster currentSyougou = syougouList.get(position); //リストの(position)番目の称号データを取得

        final String currentSyougouName = currentSyougou.getName(); //SyougouMasterから称号名を取得
        final String dialogMessage = currentSyougou.getMessage(); //SyougouMasterから称号毎の獲得条件を取得
        final int imageResId = currentSyougou.getImageResId();    //SyougouMasterから称号毎の画像IDを取得

        // プレイヤーが称号を持っているかどうかの判定
        if (playerData.unlockedTitleIds.contains(currentSyougou.getId())) {
            //取得済みなら...
            holder.syougouButton.setText(currentSyougou.getName()); //称号名をそれぞれ設定
            holder.syougouButton.setOnClickListener(v -> {
                showDetailDialog(currentSyougouName, dialogMessage, imageResId);
            });
        } else {
            //未取得なら...
            holder.syougouButton.setText(currentSyougou.getLockedName()); //称号名を？？？に設定
            holder.syougouButton.setOnClickListener(v -> {
                showDetailDialog("？？？", dialogMessage, imageResId);
            });
        }
    }

    //RecyclerViewに表示すべき=作成すべき称号の数を数える
    @Override
    public int getItemCount() {
        //リストがnullの場合も考慮して0を返す
        return syougouList != null ? syougouList.size() : 0;
    }

    //1個分のボタンを保持するためのインナークラス
    public static class SyougouViewHolder extends RecyclerView.ViewHolder {
        public Button syougouButton;

        public SyougouViewHolder(@NonNull View itemView) {
            super(itemView);
            //button_syougou_detail.xmlの中にあるボタンを見つけて保持する
            syougouButton = itemView.findViewById(R.id.syougou_item_button);
        }
    }


    /**
     * 称号の詳細ダイアログを表示する為のメソッド
     * SyougouFragmentからこのクラスに移動させた
     * @param title 称号名
     * @param message 称号の説明文
     * @param imageResId 画像のリソースID
     */
    private void showDetailDialog(String title, String message, int imageResId) {
        //contextがnullの場合は、処理を中断してクラッシュを防ぐ
        if (context == null) return;

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_syougou_detail, null);

        ImageView dialogImage = dialogView.findViewById(R.id.dialog_image);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);

        dialogImage.setImageResource(imageResId);
        dialogMessage.setText(message);

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("閉じる", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
