//敵の出現と勝敗毎の処理分岐を担当
//エリアのダイアログ作成時, 確認の為に仮で作っただけだから他で同じようなの作ってたら消していい
package com.example.pbl_gruop1;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
        loadData(context); //EnemyManager自身の状態をロード
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        //日付が変わった時だけ実行
        if (!today.equals(lastUpdateDate)) {
            Log.d("EnemyManager", "日付更新を検知。防M防日数を更新します。");
            long now = System.currentTimeMillis(); // 現在時刻

            //EnemyManager自身の状態を「昨日」のデータとして更新
            this.lastUpdateDate = today;
            String yesterdayEnemyAreaId = this.todayEnemyAreaId; // 昨日のUFO
            boolean yesterdayEnemyDefeated = this.isTodayEnemyDefeated; // 昨日の討伐状況

            this.isTodayEnemyDefeated = false; // 今日の討伐ステータスはリセット

            //更新した「昨日」のデータを使って、UFOの没収処理を実行
            if (yesterdayEnemyAreaId != null && !yesterdayEnemyDefeated) {
                // GameDataManagerにエリア没収と防衛日数リセットを依頼
                Log.d("EnemyManager", yesterdayEnemyAreaId + " が倒されなかったので没収します。");
                GameDataManager.getInstance().confiscateArea(context, playerData, yesterdayEnemyAreaId);
            }

            //没収されなかった全解放済みエリアの防衛日数を更新
            if (playerData.unlockedAreaIds != null) {
                //for文の途中でリストから削除するとエラーになるため、コピーしてループ
                List<String> areaIdsToCheck = new java.util.ArrayList<>(playerData.unlockedAreaIds);

                for (String areaId : areaIdsToCheck) {

                    long lastDefence = playerData.lastDefenceDaysMap.getOrDefault(areaId, 0L);
                    int consecutiveDays = playerData.consecutiveDefenceDaysMap.getOrDefault(areaId, 0);

                    if (lastDefence == 0) {
                        consecutiveDays = 1;
                    } else if (isYesterday(now, lastDefence)) {
                        consecutiveDays++;
                    } else if (!isToday(now, lastDefence)) {
                        consecutiveDays = 1;
                    }

                    playerData.lastDefenceDaysMap.put(areaId, now);
                    playerData.consecutiveDefenceDaysMap.put(areaId, consecutiveDays);
                    checkDefenceMilestoneTitles(playerData, consecutiveDays);
                }
            }

            //新しいUFOの抽選
            if (playerData.unlockedAreaIds != null && !playerData.unlockedAreaIds.isEmpty()) {
                List<String> shuffledList = new java.util.ArrayList<>(playerData.unlockedAreaIds);
                Collections.shuffle(shuffledList);
                this.todayEnemyAreaId = shuffledList.get(0); // 今日のUFOをセット
                Log.d("EnemyManager", "本日のUFOは " + this.todayEnemyAreaId + " に出現");
            } else {
                this.todayEnemyAreaId = null;
            }

            //全ての変更をセーブ
            GameDataManager.getInstance().savePlayerData(context, playerData);

            //(EnemyManager自身の状態もセーブ)
            //(yesterdayEnemyAreaId などを保存するため、saveDataの前にtodayEnemyAreaIdをセットする)
            saveData(context);
        }
    }

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
    private boolean isToday(long time1, long time2){
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.setTimeInMillis(time2);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }

    private boolean isYesterday(long time1, long time2) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(time1);
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.setTimeInMillis(time2);
        return cal.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }
    private void checkDefenceMilestoneTitles(PlayerData playerData, int consecutiveDays) {
        // 10日
        if (consecutiveDays >= 10) {
            String newTitleId = "title_defence_10";
            if (!playerData.unlockedTitleIds.contains(newTitleId)) {
                playerData.unlockedTitleIds.add(newTitleId);
            }
        }
        // 100日
        if (consecutiveDays >= 100) {
            String newTitleId = "title_defence_100";
            if (!playerData.unlockedTitleIds.contains(newTitleId)) {
                playerData.unlockedTitleIds.add(newTitleId);
            }
        }
        // 365日
        if (consecutiveDays >= 365) {
            String newTitleId = "title_defence_365";
            if (!playerData.unlockedTitleIds.contains(newTitleId)) {
                playerData.unlockedTitleIds.add(newTitleId);
            }
        }
    }
}

