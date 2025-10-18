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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
        TitleManager titleManager = TitleManager.getInstance();
        List<Title> allTitles = titleManager.getTitleList(); //すべての称号リストを取得

        GameDataManager dataManager = GameDataManager.getInstance();
        PlayerData playerData = dataManager.loadPlayerData(getContext());

        //ローカル変数は削除
//        RecyclerView recyclerView = view.findViewById(R.id.syougou_recycler_view); //RecyclerViewをxmlから見つける
//        SyougouAdapter adapter = new SyougouAdapter(getContext(), allTitles, playerData);
        //クラス変数を使うように修正
        this.recyclerView = view.findViewById(R.id.syougou_recycler_view);

        //最初にアダプターを初期化
        Context context = getContext();
        if (context != null) {
            this.adapter = new SyougouAdapter(context, new ArrayList<>(), new PlayerData());
            this.recyclerView.setAdapter(this.adapter);
        }else{
            Log.e("SyougouFragment", "Contextがnullのためアダプターを初期化できませんでした");
            return;
        }

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //お知らせを受け取ったらUIを更新
                if ("com.example.pbl_gruop1.TITLE_DATA_UPDATED".equals(intent.getAction())) {
                    Log.d("SyougouFragment","データ更新のお知らせを受信。UIを更新します");
                    updateUI();
                }
            }
        };

        updateUI();

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

        TitleManager titleManager = TitleManager.getInstance();
        List<Title> allTitles = titleManager.getTitleList();

        GameDataManager dataManager = GameDataManager.getInstance();
        PlayerData playerData = dataManager.loadPlayerData(getContext());

        adapter.updateData(allTitles, playerData);
    }


}