//敵の出現と勝敗毎の処理分岐を担当
//エリアのダイアログ作成時, 確認の為に仮で作っただけだから他で同じようなの作ってたら消していい
package com.example.pbl_gruop1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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


    private String todayEnemyAreaId = null; //今日の敵出現エリアIDを保持する変数
    private String previousEnemyAreaId = null; //前日の敵エリアID
    private boolean isTodayEnemyDefeated = false;
    private boolean wasPreviousEnemyDefeated = false; //前日
    private String lastUpdateDate = "";   //最後に更新した日付を保持する変数

    //シングルトントントン
    public static synchronized EnemyManager getInstance() {
        if (instance == null) {
            instance = new EnemyManager();
        }
        return instance;
    }

    private EnemyManager() {}

    public void checkAndProcessDailyUpdates(Context context, PlayerData playerData) {
        loadData(context); //自身の状態をロード
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        //lastUpdateDateが空(初回起動)または今日の日付なら、何もせず終了
        if (lastUpdateDate.isEmpty() || lastUpdateDate.equals(todayStr)) {
            if (lastUpdateDate.isEmpty()) {
                //初回起動時の処理
                Log.d("EnemyManager", "初回起動です。");
                this.lastUpdateDate = todayStr;
                playerData.energy = 0; //エネルギーリセット
                spawnNewUfo(context, playerData); //UFO抽選
                GameDataManager.getInstance().savePlayerData(context, playerData);
                saveData(context); //今日の日付とUFOを保存
            }
            return; //日付更新は不要
        }


        Log.d("EnemyManager", "日付更新を検知。最後に更新した日: " + lastUpdateDate);

        // 日付を扱うため、CalendarとSimpleDateFormatを準備
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        try {
            //最後に更新した日 (lastUpdateDate) を Date型にパース
            Date lastUpdateDateParsed = sdf.parse(this.lastUpdateDate);
            cal.setTime(lastUpdateDateParsed);

            //「今日」のCalendarインスタンスも用意
            Calendar todayCal = Calendar.getInstance();
            todayCal.setTime(sdf.parse(todayStr)); // 時分秒をリセットした「今日」

            //lastUpdateDateのCalendarが、「今日」になるまでループ
            while (cal.before(todayCal)) {

                //Calendarを1日進める (処理対象日)
                cal.add(Calendar.DAY_OF_YEAR, 1);
                String processingDateStr = sdf.format(cal.getTime()); // これが処理対象の日付(昨日、一昨日...)
                Log.d("EnemyManager", processingDateStr + " の日付更新処理を実行します。");

                //没収処理 (EnemyManagerが保持してる昨日の情報を使う)
                if (this.todayEnemyAreaId != null && !this.isTodayEnemyDefeated) {
                    Log.d("EnemyManager", this.todayEnemyAreaId + " が倒されなかったので没収します。");
                    GameDataManager.getInstance().confiscateArea(context, playerData, this.todayEnemyAreaId);
                }

                //防衛日数更新 (没収されなかったエリア)
                //    (引数に "cal" (処理対象日) を渡すのが重要)
                updateDefenceDays(context, playerData, cal);

                //えネルギーを0にリセット
                playerData.energy = 0;
                Log.d("EnemyManager", processingDateStr + " のためエネルギーが0にリセットされました。");

                //UIに通知 (エネルギーリセットを反映させるため)
                Intent intent = new Intent("com.example.pbl_gruop1.ENERGY_UPDATED");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                //新しいUFOを抽選
                spawnNewUfo(context, playerData);

                //EnemyManager自身の状態を「処理した日付」で更新
                this.lastUpdateDate = processingDateStr;
                this.isTodayEnemyDefeated = false; //討伐ステータスは常にリセット

                //ループの都度、変更を保存する (PlayerとEnemyManager)
                GameDataManager.getInstance().savePlayerData(context, playerData);
                saveData(context);

            }

        } catch (java.text.ParseException e) {
            Log.e("EnemyManager", "日付のパースに失敗しました。", e);
            //エラーが発生した場合、強制的に今日の日付で上書きしてリセットする
            this.lastUpdateDate = todayStr;
            saveData(context);
        }
    }

    private void spawnNewUfo(Context context, PlayerData playerData) {
        //抽選の前に、playerDataが最新（没収処理後）であることを確認
        //(checkAndProcessDailyUpdates内で呼び出すならplayerDataは最新のはず)
        if (playerData.unlockedAreaIds != null && !playerData.unlockedAreaIds.isEmpty()) {
            List<String> shuffledList = new java.util.ArrayList<>(playerData.unlockedAreaIds);
            Collections.shuffle(shuffledList);
            this.todayEnemyAreaId = shuffledList.get(0); //今日のUFOをセット
            Log.d("EnemyManager", "UFOが " + this.todayEnemyAreaId + " に出現");
        } else {
            this.todayEnemyAreaId = null;
        }
    }

    private void updateDefenceDays(Context context, PlayerData playerData, Calendar processingCal) {
        //processingCal(処理対象日) の時刻を使う
        long now = processingCal.getTimeInMillis();

        if (playerData.unlockedAreaIds != null) {
            //areaIdsToCheckは、エリア没収（confiscateArea）が実行された「後」の
            //解放済みエリアリストを正しく使えている
            List<String> areaIdsToCheck = new java.util.ArrayList<>(playerData.unlockedAreaIds);

            for (String areaId : areaIdsToCheck) {
                long lastDefence = playerData.lastDefenceDaysMap.getOrDefault(areaId, 0L);
                int consecutiveDays = playerData.consecutiveDefenceDaysMap.getOrDefault(areaId, 0);

                //処理日が最後にセーブした日の翌日なら
                if (lastDefence == 0) {
                    consecutiveDays = 1;
                } else if (isYesterday(now, lastDefence)) {
                    consecutiveDays++;
                } else if (!isToday(now, lastDefence)) { //最後にセーブした日と処理日が同日でもなく、翌日でもない（＝途切れた）
                    consecutiveDays = 1;
                }
                //(同日の場合は何もしない)

                playerData.lastDefenceDaysMap.put(areaId, now); //処理対象日のタイムスタンプを保存
                playerData.consecutiveDefenceDaysMap.put(areaId, consecutiveDays);
                checkDefenceMilestoneTitles(playerData, consecutiveDays);
            }
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
        editor.putString(KEY_PREVIOUS_ENEMY_AREA_ID, previousEnemyAreaId); // (メモ: 現在この変数は使われていない)
        editor.putBoolean(KEY_PREVIOUS_ENEMY_DEFEATED, wasPreviousEnemyDefeated); // (メモ: 現在この変数は使われていない)
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
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1); // 基準日を1日前にする

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

    public void validateCurrentUfo(Context context, PlayerData playerData) {
        loadData(context); //EnemyManagerの現在のUFO情報をロード

        if (todayEnemyAreaId == null) {
            return; //もともとUFOがいないなら何もしない
        }

        //現在UFOがいるエリア(todayEnemyAreaId)が、
        //最新の解放済みエリアリスト(playerData.unlockedAreaIds)に含まれているかチェック
        if (!playerData.unlockedAreaIds.contains(todayEnemyAreaId)) {
            //含まれていない = 未開放になったエリアにUFOがいた
            Log.d("EnemyManager", "デバッグ操作によりUFOのいたエリアが未開放になりました。UFOを削除します。");

            todayEnemyAreaId = null; //今日のUFOを消す
            isTodayEnemyDefeated = false;// 念のためリセット

            saveData(context); //変更を保存
        }
    }
}