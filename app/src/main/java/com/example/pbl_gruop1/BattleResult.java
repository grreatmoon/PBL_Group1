package com.example.pbl_gruop1;
//敵とのバトル結果に関連する処理を記載したファイル

import android.content.Context;
import android.util.Log;
import java.util.Calendar;

//敵を倒した後の結果を処理し、連続撃破記録を更新するためのクラス。
public class BattleResult{
    public void Defencebattle(Context context, PlayerData playerData, String areaId){

        //プレイヤーデータがなければ処理を中断
        if (areaId == null || areaId.isEmpty() || playerData == null){
            Log.e("BattleResult", "無効なareaId, またはPlayerDataがnullです。");
            return;
        }

        long now = System.currentTimeMillis(); //現在の日付(ミリ秒)
        long lastDefence = playerData.lastDefenceDaysMap.getOrDefault(areaId, 0L);
        int consecutiveDays = playerData.consecutiveDefenceDaysMap.getOrDefault(areaId, 0);

        if (lastDefence == 0){ //防衛戦初勝利
            consecutiveDays = 1;
        }else{
            if (isYesterday(now, lastDefence)){
                //最後に倒したのが昨日なら連続日数をプラス
                consecutiveDays++;
            } else if (!isToday(now, lastDefence)){
                //連続記録が途切れた場合、0ではなく1にリセット
                consecutiveDays = 1;
            }
            //最後に倒したのが今日なら連続記録の変更は無し
        }

        playerData.consecutiveDefenceDaysMap.put(areaId, consecutiveDays);
        playerData.lastDefenceDaysMap.put(areaId, now);

        checkDefenceMilestoneTitles(playerData, consecutiveDays);

        //dataManager.savePlayerData(context, playerData);

    }


    private boolean isToday(long time1, long time2){
        //二つのカレンダーを定義. (時刻1,2をそれぞれ設定)
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);            //時刻1にセット
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);            //時刻2にセット
        //完全に同じ日であればreturn
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    private boolean isYesterday(long time1, long time2) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time1);
        cal.add(Calendar.DAY_OF_YEAR, -1); //基準日(今日)を1日前に設定
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);
        //基準日と比較対象の日が同じ日かどうか判定
        return cal.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    private void checkDefenceMilestoneTitles(PlayerData playerData, int consecutiveDays) {
        //10日
        if (consecutiveDays >= 10) {
            String newTitleId = "title_defence_10";
            if (!playerData.unlockedTitleIds.contains(newTitleId)) {
                playerData.unlockedTitleIds.add(newTitleId);
            }
        }
        //100日
        if (consecutiveDays >= 100) {
            String newTitleId = "title_defence_100";
            if (!playerData.unlockedTitleIds.contains(newTitleId)) {
                playerData.unlockedTitleIds.add(newTitleId);
            }
        }
        //365日
        if (consecutiveDays >= 365) {
            String newTitleId = "title_defence_365";
            if (!playerData.unlockedTitleIds.contains(newTitleId)) {
                playerData.unlockedTitleIds.add(newTitleId);
            }
        }
    }
}
