package com.example.pbl_gruop1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;

public class SyougouFragment extends Fragment {
    private List<SyougouMaster> allTitles;
    public SyougouFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //fragment_syougou.xmlを画面に表示する
        return inflater.inflate(R.layout.fragment_syougou, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSyougou(); //全称号のマスターデータリストを準備する
        PlayerData playerData = loadPlayerDataForTest(); //プレイヤーデータのインスタンスをロード (現状テスト用)
        RecyclerView recyclerView = view.findViewById(R.id.syougou_recycler_view); //RecyclerViewをxmlから見つける
        SyougouAdapter adapter = new SyougouAdapter(getContext(), allTitles, playerData);
        //アダプターを生成. Context, 称号リスト, プレイヤーデータを渡す
        recyclerView.setAdapter(adapter);
        //RecyclerViewにアダプターを設定. RecyclerView が自動的に称号リストを表示してくれる
    }

    //全称号のマスターデータを作成
    private void setupSyougou() {
        allTitles = new ArrayList<>();
        /**SyougouMaster.javaを基に各称号の詳細を設定(称号ID, 称号の名前, 称号の獲得条件, 称号毎の画像ID)
           3つ目以降は画像を仕入れた時に書き換える, 今は動作確認用に1, 2のみ実装*/
        allTitles.add(new SyougouMaster("kuroishi-suya", "黒石・須屋地区の開放", "黒石・須屋地区付近を通行", R.drawable.syougou_image_1));
        allTitles.add(new SyougouMaster("nanbu", "南部地区の解放", "南部地区付近を通行", R.drawable.syougou_image_2));
        allTitles.add(new SyougouMaster("nonoshima", "野々島地区の解放", "野々島地区付近を通行", R.drawable.syougou_image_2));
        allTitles.add(new SyougouMaster("aioi-sakae", "合生・栄地区の解放", "合生・栄地区付近を通行", R.drawable.syougou_image_2));
        allTitles.add(new SyougouMaster("hokubu", "北部地区の解放", "北部地区付近を通行", R.drawable.syougou_image_2));
        allTitles.add(new SyougouMaster("chuou", "中央地区の解放", "中央地区付近を通行", R.drawable.syougou_image_2));
        allTitles.add(new SyougouMaster("area-full", "全エリアの解放", "マップ内全ての地区を踏破", R.drawable.syougou_image_2));
        //新しく称号を増やすときは, このリストに一行ずつ追加する
    }

    //テスト用のプレイヤーデータを作成して返すメソッド. @return テスト用のPlayerDataインスタンス
    private PlayerData loadPlayerDataForTest() {
        // 本来はスマホに保存されたデータを読み込む処理が入る
        // ここではテストのため、新しいデータを作成し、仮に"南部地区の解放"だけ獲得済みにする
        PlayerData data = new PlayerData();
        data.unlockedTitleIds.add("nanbu"); // "南部地区の解放"だけ獲得している状態
        return data;
    }
}