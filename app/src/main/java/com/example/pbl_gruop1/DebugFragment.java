package com.example.pbl_gruop1;

import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar; // SeekBarをインポート
import android.widget.TextView; // TextViewをインポート
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat; // SwitchCompatをインポート
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

public class DebugFragment extends Fragment {

    private GameDataManager dataManager;
    private Context appContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_debug, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dataManager = GameDataManager.getInstance();
        appContext = getContext().getApplicationContext();

        //戻るボタン
        view.findViewById(R.id.debug_back_button).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        //エネルギーMAXボタン
        view.findViewById(R.id.debug_energy_max_button).setOnClickListener(v -> {
            PlayerData data = dataManager.loadPlayerData(appContext);
            data.energy = data.maxEnergy;
            dataManager.savePlayerData(appContext, data);

            //SeekBarにも反映させる
            SeekBar energySeekBar = view.findViewById(R.id.debug_energy_seekbar);
            TextView energyValueText = view.findViewById(R.id.debug_energy_value_text);
            energySeekBar.setProgress(data.energy);
            energyValueText.setText(String.valueOf(data.energy));

            showToast("エネルギーをMAXにしました");
        });

        //エネルギーSeekBar
        setupEnergySeekBar(view);

        //各エリアの反転スイッチ (ロジック変更)
        setupToggleSwitch(view, R.id.debug_toggle_myosenji, "Myosenji", "妙泉寺");
        setupToggleSwitch(view, R.id.debug_toggle_genkipark, "GenkiPark", "元気の森公園");
        setupToggleSwitch(view, R.id.debug_toggle_koshicityhall, "KoshiCityHall", "合志市役所");
        setupToggleSwitch(view, R.id.debug_toggle_lutherchurch, "LutherChurch", "ルーテル教会");
        setupToggleSwitch(view, R.id.debug_toggle_countrypark, "CountryPark", "カントリーパーク");
        setupToggleSwitch(view, R.id.debug_toggle_bentenmountain, "BentenMountain", "弁天山");
    }

    //エネルギーSeekBarのメソッド
    private void setupEnergySeekBar(View view) {
        TextView energyValueText = view.findViewById(R.id.debug_energy_value_text);
        SeekBar energySeekBar = view.findViewById(R.id.debug_energy_seekbar);

        //現在のエネルギー値をSeekBarとTextViewに反映
        PlayerData initialData = dataManager.loadPlayerData(appContext);
        energySeekBar.setMax(initialData.maxEnergy); //最大値をPlayerDataに合わせる
        energySeekBar.setProgress(initialData.energy);
        energyValueText.setText(String.valueOf(initialData.energy));

        //SeekBarが操作された時のリスナー
        energySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //スライド中は上の数字だけ変える
                if (fromUser) {
                    energyValueText.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 操作終了時、データをセーブする
                PlayerData data = dataManager.loadPlayerData(appContext);
                int newEnergy = seekBar.getProgress();
                data.energy = newEnergy;
                dataManager.savePlayerData(appContext, data);
                showToast("エネルギーを " + newEnergy + " に設定しました");
            }
        });
    }

    //Switchのメソッド
    private void setupToggleSwitch(View rootView, int switchId, String areaId, String areaName) {
        SwitchCompat toggleSwitch = rootView.findViewById(switchId);
        if (toggleSwitch == null) return;

        //現在の状態をスイッチに反映 (リスナーを一時的に無効化)
        toggleSwitch.setOnCheckedChangeListener(null);
        PlayerData data = dataManager.loadPlayerData(appContext);
        boolean isUnlocked = data.unlockedAreaIds.contains(areaId);
        toggleSwitch.setChecked(isUnlocked);

        //スイッチが操作された時のリスナーを再設定
        toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //isChecked が true なら「解放済み」にしたい
            PlayerData currentData = dataManager.loadPlayerData(appContext);

            if (isChecked) {
                //スイッチがONになった
                if (!currentData.unlockedAreaIds.contains(areaId)) {
                    currentData.unlockedAreaIds.add(areaId);

                    //エリア訪問称号のチェック
                    String areaTitleId = "title_" + areaId.toLowerCase();
                    if (!currentData.unlockedTitleIds.contains(areaTitleId)) {
                        TitleManager titleManager = TitleManager.getInstance();
                        Title title = titleManager.getTitleById(areaTitleId);
                        if (title != null) {
                            currentData.unlockedTitleIds.add(areaTitleId);
                            Log.d("DebugFragment", "デバッグ: 称号「" + title.getName() + "」を付与");
                        }
                    }

                    //「合志マスター」のチェック
                    List<String> requiredTitles = new ArrayList<>();
                    requiredTitles.add("title_myosenji");
                    requiredTitles.add("title_genkipark");
                    requiredTitles.add("title_koshicityhall");
                    requiredTitles.add("title_lutherchurch");
                    requiredTitles.add("title_countrypark");
                    requiredTitles.add("title_bentenmountain");

                    if (currentData.unlockedTitleIds.containsAll(requiredTitles)) {
                        String koshiMasterId = "title_koshimaster";
                        if (!currentData.unlockedTitleIds.contains(koshiMasterId)) {
                            currentData.unlockedTitleIds.add(koshiMasterId);
                            Log.d("DebugFragment", "デバッグ: 称号「合志マスター」を付与");
                        }
                    }

                    dataManager.savePlayerData(appContext, currentData);
                    showToast(areaName + " を解放しました");
                }
            } else {
                //スイッチがOFFになった
                if (currentData.unlockedAreaIds.contains(areaId)) {
                    currentData.unlockedAreaIds.remove(areaId);
                    dataManager.savePlayerData(appContext, currentData);
                    showToast(areaName + " を未解放にしました");
                }
            }

            //試用会のため
            //UFOの整合性をチェック
            EnemyManager.getInstance().validateCurrentUfo(appContext, currentData);
        });
    }

    //トーストを表示して、UI更新のブロードキャストを送信
    private void showToast(String message) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
        //変更をStartFragmentやMapFragmentに通知
        Intent intent = new Intent("com.example.pbl_gruop1.TITLE_DATA_UPDATED");
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
    }
}