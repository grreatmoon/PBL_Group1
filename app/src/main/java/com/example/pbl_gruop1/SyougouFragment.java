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

        RecyclerView recyclerView = view.findViewById(R.id.syougou_recycler_view); //RecyclerViewをxmlから見つける
        SyougouAdapter adapter = new SyougouAdapter(getContext(), allTitles, playerData);
        //アダプターを生成. Context, 称号リスト, プレイヤーデータを渡す
        recyclerView.setAdapter(adapter);
        //RecyclerViewにアダプターを設定. RecyclerView が自動的に称号リストを表示してくれる
    }

    @Override
    public void onResume(){
        super.onResume();
        //画面が表示されるたびにデータを再読み込みしてUIを更新
        updateUI();
    }

    //UI更新処理を別メソッドとして定義
    private void updateUI() {
        //onViewCreatedと同じデータ読み込みとアダプター設定の処理を行う
        if (getView() == null) return;  //安全のため

        TitleManager titleManager = TitleManager.getInstance();
        List<Title> allTitles = titleManager.getTitleList();

        GameDataManager dataManager = GameDataManager.getInstance();
        PlayerData playerData = dataManager.loadPlayerData(getContext());

        RecyclerView recyclerView = getView().findViewById(R.id.syougou_recycler_view);

        //アダプターがすでに存在すれば新しいデータで更新,なければ新しく作成して設定する
        if (recyclerView.getAdapter() instanceof SyougouAdapter) {
            SyougouAdapter adapter = new SyougouAdapter(getContext(), allTitles, playerData);
            recyclerView.setAdapter(adapter);
        } else {
            SyougouAdapter adapter = new SyougouAdapter(getContext(),allTitles,playerData);
            recyclerView.setAdapter(adapter);
        }
    }

}