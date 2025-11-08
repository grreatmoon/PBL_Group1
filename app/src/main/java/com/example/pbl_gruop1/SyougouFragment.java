package com.example.pbl_gruop1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;

public class SyougouFragment extends Fragment {

    private RecyclerView recyclerView;
    private SyougouAdapter adapter;
    private BroadcastReceiver updateReceiver;

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

        // RecyclerViewの初期設定
        this.recyclerView = view.findViewById(R.id.syougou_recycler_view);
        //LinearLayoutManagerの設定を忘れないように追加
        //this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //最初にアダプターを初期化
        Context context = getContext();
        if (context != null) {
            this.adapter = new SyougouAdapter(context, new ArrayList<>(), new PlayerData());
            this.recyclerView.setAdapter(this.adapter);
        }else{
            Log.e("SyougouFragment", "Contextがnullのためアダプターを初期化できませんでした");
            return;
        }

        // データ更新の通知を受け取るための受信機を設定
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.pbl_gruop1.TITLE_DATA_UPDATED".equals(intent.getAction())) {
                    Log.d("SyougouFragment","データ更新のお知らせを受信。UIを更新します");
                    updateUI();
                }
            }
        };
        updateUI();

        // 追加した戻るボタンのIDを見つけてくる
        Button backButton = view.findViewById(R.id.button_back_to_start);
        // ボタンにクリックリスナーを設定
        backButton.setOnClickListener(v -> {
            // NavControllerを使って、指定したIDの画面（fragment_start）へ遷移する
            NavHostFragment.findNavController(SyougouFragment.this)
                    .navigate(R.id.action_syougouFragment_to_startFragment); // このIDはnav_graph.xmlで定義します
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        //画面が表示されるときに受信機を登録
        IntentFilter filter = new IntentFilter("com.example.pbl_gruop1.TITLE_DATA_UPDATED");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(updateReceiver,filter);
        Log.d("SyougouFragment","データ更新受信機を登録");
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        // ★ 画面が見えなくなるときに受信機を解除 (重要！) ★
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateReceiver);
        Log.d("SyougouFragment", "データ更新受信機を解除しました。");
    }

    //UI更新処理を別メソッドとして定義
    private void updateUI() {
        //onViewCreatedと同じデータ読み込みとアダプター設定の処理を行う
        if (getView() == null || getContext() == null || adapter == null) {
            Log.w("SyougouFragment", "updateUI: View, Context, or Adapter is null. Skipping UI update.");
            return; //必要なものがそろっていなければ更新しない
        }

        GameDataManager dataManager = GameDataManager.getInstance();
        PlayerData playerData = dataManager.loadPlayerData(getContext());
        TitleManager titleManager = TitleManager.getInstance();

        if (playerData == null) {
            Log.e("SyougouFragment", "PlayerDataの読み込みに失敗しました。UIを更新できません。");
            return;
        }

        // 1. 全ての「静的な」称号リストを取得
        List<Title> allStaticTitles = titleManager.getStaticTitles();

        // 2. プレイヤーがアンロック済みの「動的な」称号リストを取得
        //    getUnlockedTitlesは静的なものも含むため、動的なものだけをフィルタリングするか、
        //    Adapter側でIDの重複をうまく処理する必要があります。
        //    ここでは、よりシンプルな「獲得済みIDリスト」そのものを渡す方法を採用します。
        List<String> unlockedIds = playerData.unlockedTitleIds;

        // 3. Adapterに「全静的称号リスト」と「獲得済みIDリスト」を渡してUIを更新する
        adapter.updateData(allStaticTitles, playerData);
        Log.d("SyougouFragment", "UIを更新しました。静的称号数: " + allStaticTitles.size());
    }


}