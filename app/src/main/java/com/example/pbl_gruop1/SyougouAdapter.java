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
import java.util.ArrayList;

//RecyclerViewと称号データリスト(allTitles)を結びつけるためのアダプタークラス
public class SyougouAdapter extends RecyclerView.Adapter<SyougouAdapter.SyougouViewHolder>{
    private final List<Title>  titleList;
    private final PlayerData playerData;
    private final Context context; //ダイアログ表示のためにContextを受け取る

    //ダイアログの複数表示防ぐため
    private static long lastClickTime = 0;
    private static final long CLICK_DEBOUNCE_INTERVAL = 1000;//(1s)

    public SyougouAdapter(Context context, List<Title> titleList, PlayerData playerData) {
        this.context = context;
        this.titleList = titleList;
        this.playerData = playerData;

        //称号:合志マスターの取得条件を追記
        //プレイヤーが現在持っている称号IDのリストを取得
        List<String> unlockedIds = this.playerData.unlockedTitleIds;
        //取得条件となる6つの称号IDのリストを作成
        List<String> requiredTitles = new ArrayList<>();
        requiredTitles.add("title_myosenji");
        requiredTitles.add("title_genkipark");
        requiredTitles.add("title_koshicityhall");
        requiredTitles.add("title_lutherchurch");
        requiredTitles.add("title_countrypark");
        requiredTitles.add("title_bentenmountain");

        //プレイヤーが全ての必須称号を持っているかチェック
        if (unlockedIds.containsAll(requiredTitles)) {
            //条件を満たしていれば、「合志マスター」のIDをプレイヤーデータに追加
            unlockedIds.add("title_koshimaster");
        }
    }
    //合志マスターの取得状況チェックここまで

    @NonNull
    @Override
    public SyougouViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.button_syougou_detail, parent, false);
        return new SyougouViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SyougouViewHolder holder, int position) {
        // 表示する称号データをリストから取得
        Title currentTitle = titleList.get(position); //リストの(position)番目の称号データを取得

        final String currentTitleName = currentTitle.getName(); //Titleから称号名を取得
        final String dialogMessage = currentTitle.getDescription(); //Titleから称号毎の獲得条件を取得
        final String imageName = currentTitle.getImageName();
        final int imageResId = getImageResourceIdByName(imageName);    //Titleから称号毎の画像IDを取得
        final int lockedImageResId = R.drawable.syougou_image_locked;   //未開放用画像ID


        //プレイヤーが称号を持っているかどうかの判定
        if (playerData.unlockedTitleIds.contains(currentTitle.getId())) {
            //取得済みなら...
            holder.syougouButton.setText(currentTitle.getName()); //称号名をそれぞれ設定
            holder.syougouButton.setOnClickListener(v -> {
                if (System.currentTimeMillis() - lastClickTime <CLICK_DEBOUNCE_INTERVAL) {
                    return;
                    //1s以内の連続タップを無視
                }
                lastClickTime = System.currentTimeMillis(); //タップ時刻を更新
                showDetailDialog(currentTitleName, dialogMessage, imageResId);
            });
        } else {
            //未取得なら...
            holder.syougouButton.setText("???"); //称号名を？？？に設定
            holder.syougouButton.setOnClickListener(v -> {
                if (System.currentTimeMillis() - lastClickTime <CLICK_DEBOUNCE_INTERVAL) {
                    return;
                    //1s以内の連続タップを無視
                }
                lastClickTime = System.currentTimeMillis(); //タップ時刻を更新
                showDetailDialog("？？？", dialogMessage, lockedImageResId);
            });
        }
    }

    //RecyclerViewに表示すべき=作成すべき称号の数を数える
    @Override
    public int getItemCount() {
        //リストがnullの場合も考慮して0を返す
        return titleList != null ? titleList.size() : 0;
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

    //画像ファイル名からリソースIDを取得するメソッド
    private int getImageResourceIdByName(String imageName){
        if (context == null || imageName == null || imageName.isEmpty()) {
            return 0;   //エラー時は0を返す
        }
        android.content.res.Resources resources = context.getResources();
        //getIdentifierは文字列からリソースIDを見つけるためのAndroid標準機能
        return resources.getIdentifier(imageName, "drawable",context.getPackageName());
    }

    public void updateData(List<Title> newTitleList, PlayerData newPlayerData) {

        //称号の並び替え表示
        java.util.Collections.sort(newTitleList, (t1, t2) -> {
            //カテゴリで並び替え
            int categoryCompare = t1.getCategory().compareTo(t2.getCategory());
            if (categoryCompare != 0) {
                return categoryCompare;
            }
            //同じカテゴリ内なら、名前（ID）で並び替え
            //return t1.getId().compareTo(t2.getId());
            return Integer.compare(t1.getDisplayOrder(), t2.getDisplayOrder());
        });

        //既存のリストをクリアして新しいデータを追加
        this.titleList.clear();
        this.titleList.addAll(newTitleList);

        //プレイヤーデータを更新
        //PlayerDataクラスにコピー用のメソッドがあるとより安全だが、今回は直接代入する
        this.playerData.unlockedTitleIds = newPlayerData.unlockedTitleIds;

        //RecyclerViewにデータセットが変更されたことを通知する
        notifyDataSetChanged();
    }


}
