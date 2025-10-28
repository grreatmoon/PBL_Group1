package com.example.pbl_gruop1;
//敵とのバトル結果に関連する処理を記載したファイル

import android.content.Context;
import android.util.Log;
import java.util.Calendar;

//敵を倒した後の結果を処理し、連続撃破記録を更新するためのクラス。
public class BattleResult{
    /**
     * 敵を倒した際に呼び出されるメソッド。
     * 連続撃破日数の判定と更新、および関連する称号のアンロックを行う。
     * @param context プレイヤーデータを保存するために使用するコンテクスト
     */
    public void Defencebattle(Context context, String areaId){
        //GameDataManagerからプレイヤーデータの取得とロードを行う
        GameDataManager dataManager = GameDataManager.getInstance();
        PlayerData playerData = dataManager.loadPlayerData(context);

        //プレイヤーデータがなければ処理を中断
        if (areaId == null || areaId.isEmpty() ||playerData == null){
            //一応エラーとしてログを出力する
            Log.e("BattleResult", "無効なareaId, またはPlayerDataの読み込みに失敗しました。");
            return;
        }

        long now = System.currentTimeMillis(); //現在の日付(ミリ秒)

        //マップから特定のエリアの最後に防衛した日付を取得
        long lastDefence = playerData.lastDefenceDaysMap.getOrDefault(areaId, 0L);
        //マップから特定のエリアの連続防衛日数を取得
        int consecutiveDays = playerData.consecutiveDefenceDaysMap.getOrDefault(areaId, 0);

        if (lastDefence == 0){ //防衛戦初勝利
            consecutiveDays = 1; //consecutiveDays：連続防衛日数
        }else{ //既に一度防衛成功している場合
            //最後に倒した日と今日の日付を比較
            if (isYesterday(now, lastDefence)){
                //最後に倒したのが昨日なら連続日数をプラス
                consecutiveDays++;
            } else if (!isToday(now, lastDefence)){
                //最後に倒したのが今日ではなく、昨日でもない場合(連続記録が途切れた場合)
                consecutiveDays = 0;
            }
            //最後に倒したのが今日なら連続記録の変更は無し
        }

        // "このエリア" の連続日数と最終日時をマップに保存
        playerData.consecutiveDefenceDaysMap.put(areaId, consecutiveDays);
        playerData.lastDefenceDaysMap.put(areaId, now);

        //2日以上の連続防衛で称号を取得
        if (consecutiveDays >= 2){
            //例えばareaIdが"Myosenji", consecutiveDaysが3の場合、
            // "title_Myosenji_defence_3" というIDが生成される
            String newTitleId = "title_" + areaId + "_defence_" + consecutiveDays;

            //その日数の称号をまだ持っていないかチェック
            if (!playerData.unlockedTitleIds.contains(newTitleId)) {
                // 新しい称号をリストに追加
                playerData.unlockedTitleIds.add(newTitleId);
            }
        }

        //更新した分のプレイヤーデータをファイルに保存する
        dataManager.savePlayerData(context, playerData);
    }

    /**
     * 2つの時刻が同じ "日" であるか判定する。
     * @param time1 時刻1 (ミリ秒)
     * @param time2 時刻2 (ミリ秒)
     * @return 同じ日であれば true
     */
    private boolean isToday(long time1, long time2){
        //二つのカレンダーを定義. (時刻1,2をそれぞれ設定)
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);            //カレンダー1：時刻1にセット
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);            //カレンダー2：時刻2にセット
        // ↓↓完全に同じ日であればreturn
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * time2 がtime1の "昨日" であるか判定する。
     * @param time1 基準となる今日の時刻 (ミリ秒)
     * @param time2 比較対象の過去の時刻 (ミリ秒)
     * @return time2がtime1の "昨日" であればtrue
     */
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
}
