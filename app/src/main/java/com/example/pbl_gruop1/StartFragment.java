package com.example.pbl_gruop1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
