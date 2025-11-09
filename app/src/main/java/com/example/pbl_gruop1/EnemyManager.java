//敵の出現と勝敗毎の処理分岐を担当
//エリアのダイアログ作成時, 確認の為に仮で作っただけだから他で同じようなの作ってたら消していい
package com.example.pbl_gruop1;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Collections;

public class EnemyManager {

    private static EnemyManager instance;
    private static final String PREFS_NAME = "EnemyPrefs";
    private static final String KEY_LAST_UPDATE_DATE = "lastUpdateDate";
    private static final String KEY_ENEMY_AREA_ID = "enemyAreaId";
    private static final String KEY_ENEMY_DEFEATED = "enemyDefeated";
    private static final String KEY_PREVIOUS_ENEMY_AREA_ID = "previousEnemyAreaId";
    private static final String KEY_PREVIOUS_ENEMY_DEFEATED = "previousEnemyDefeated";


    private String todayEnemyAreaId = null; // 今日の敵出現エリアIDを保持する変数
    private String previousEnemyAreaId = null; //前日の敵エリアID
    private boolean isTodayEnemyDefeated = false;
    private boolean wasPreviousEnemyDefeated = false; // 前日
    private String lastUpdateDate = "";   // 最後に更新した日付を保持する変数

    // シングルトンパターン
    public static synchronized EnemyManager getInstance() {
        if (instance == null) {
            instance = new EnemyManager();
        }
        return instance;
    }

    private EnemyManager() {}

    public void checkAndProcessDailyUpdates(Context context, PlayerData playerData) {
        loadData(context);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!today.equals(lastUpdateDate)) {
            //一日たっても敵が討伐されていなかった場合
            if (previousEnemyAreaId != null && !wasPreviousEnemyDefeated) {
                //GameDataManagerにエリア没収を依頼
                GameDataManager.getInstance().confiscateArea(context, playerData, previousEnemyAreaId);
            }

            //日付更新の処理
            this.lastUpdateDate = today;
            this.previousEnemyAreaId = this.todayEnemyAreaId; // 今日の敵を「前日」の記録へ
            this.wasPreviousEnemyDefeated = this.isTodayEnemyDefeated; // 今日の討伐状況を「前日」の記録へ
            this.isTodayEnemyDefeated = false; // 今日の討伐ステータスはリセット

            //新しい敵の抽選
            if (playerData.unlockedAreaIds != null && !playerData.unlockedAreaIds.isEmpty()) {
                List<String> shuffledList = new java.util.ArrayList<>(playerData.unlockedAreaIds);
                Collections.shuffle(shuffledList);
                this.todayEnemyAreaId = shuffledList.get(0);
            } else {
                this.todayEnemyAreaId = null;
            }
            saveData(context); //全ての変更を保存
        }
    }

    /** 指定されたエリアIDが、今日の敵出現エリアかどうかを判定する */
    public boolean isEnemyChallengeable (String areaId) {
        //areaIdがnullでない、かつ、今日の敵エリアIDと一致するかどうかを返す
        //「まだ倒されていない」という条件を追加
        return areaId != null && areaId.equals(todayEnemyAreaId) && !isTodayEnemyDefeated;
    }

    public void setTodayEnemyAsDefeated(Context context) {
        this.isTodayEnemyDefeated = true;
        saveData(context);
    }

    private void saveData(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_LAST_UPDATE_DATE, lastUpdateDate);
        editor.putString(KEY_ENEMY_AREA_ID, todayEnemyAreaId);
        editor.putBoolean(KEY_ENEMY_DEFEATED, isTodayEnemyDefeated);
        editor.putString(KEY_PREVIOUS_ENEMY_AREA_ID, previousEnemyAreaId);
        editor.putBoolean(KEY_PREVIOUS_ENEMY_DEFEATED, wasPreviousEnemyDefeated);
        editor.apply();
    }

    private void loadData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.lastUpdateDate = prefs.getString(KEY_LAST_UPDATE_DATE, "");
        this.todayEnemyAreaId = prefs.getString(KEY_ENEMY_AREA_ID, null);
        this.isTodayEnemyDefeated = prefs.getBoolean(KEY_ENEMY_DEFEATED, false);
        this.previousEnemyAreaId = prefs.getString(KEY_PREVIOUS_ENEMY_AREA_ID, null);
        this.wasPreviousEnemyDefeated = prefs.getBoolean(KEY_PREVIOUS_ENEMY_DEFEATED, false);
    }
}

