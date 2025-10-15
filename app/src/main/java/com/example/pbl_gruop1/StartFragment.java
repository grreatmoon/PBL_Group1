package com.example.pbl_gruop1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class StartFragment extends Fragment {
    public StartFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //UIを取得
        TextView levelTextView = view.findViewById(R.id.level_text);
        TextView kaihouritsuTextView = view.findViewById(R.id.kaihouritsu_text);

        //データを読み込む
        GameDataManager dataManager = GameDataManager.getInstance();
        PlayerData playerData = dataManager.loadPlayerData(getContext());

        //解放率を計算
        AreaManager areaManager = AreaManager.getInstance();
        int unlockedCount = playerData.unlockedAreaIds.size();  //解放済みのエリア数
        int totalCount = areaManager.getAreaList().size();      //全エリアの数
        double liberationRate = 0.0;
        if (totalCount > 0) {
            //割り算をして％を計算
            liberationRate = (double) unlockedCount / totalCount * 100.0;
        }

        //UIにデータを表示する
        levelTextView.setText("Lv. " + playerData.level);
        kaihouritsuTextView.setText("解放率： " + String.format("%.1f", liberationRate) + "%");

        // fragment_start.xmlで定義したボタンのIDを指定
        Button toMapButton = view.findViewById(R.id.to_map_button);
        toMapButton.setOnClickListener(v -> {
            // nav_graph.xmlで定義したAction IDによって遷移
            NavHostFragment.findNavController(StartFragment.this)
                    .navigate(R.id.action_startFragment_to_mapFragment);
        });

        Button toSyougouButton = view.findViewById(R.id.to_syougou_button);
        toSyougouButton.setOnClickListener(v -> {
            // nav_graph.xmlで定義したAction IDによって遷移
            NavHostFragment.findNavController(StartFragment.this)
                    .navigate(R.id.action_startFragment_to_syougouFragment);
        });
    }
}
