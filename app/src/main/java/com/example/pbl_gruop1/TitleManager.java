package com.example.pbl_gruop1;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class TitleManager {
    private static TitleManager instance;
    private Map<String, Title> staticTitlesMap;  //静的な称号の保持のみを行う

    private TitleManager() {    //称号の情報を定義
        staticTitlesMap = new HashMap<>();

        // --- 事前に定義できる、数が決まっている称号だけを登録 ---
        staticTitlesMap.put("title_myosenji", new Title("title_myosenji", "妙泉寺の僧侶", "妙泉寺を訪れる", "syougou_image_myosenji", "Myosenji"));
        staticTitlesMap.put("title_genkipark", new Title("title_genkipark", "元気の森の冒険家", "元気の森公園を訪れる", "syougou_image_genkipark", "GenkiPark"));
        staticTitlesMap.put("title_koshicityhall", new Title("title_koshicityhall", "合志市の市民", "合志市役所を訪れる", "syougou_image_koshicityhall", "KoshiCityHall"));
        staticTitlesMap.put("title_lutherchurch", new Title("title_lutherchurch", "教会の聖歌隊", "ルーテル合志教会を訪れる", "syougou_image_lutherchurch", "LutherChurch"));
        staticTitlesMap.put("title_countrypark", new Title("title_countrypark", "パークの探検家", "カントリーパークを訪れる", "syougou_image_countrypark", "CountryPark"));
        staticTitlesMap.put("title_bentenmountain", new Title("title_bentenmountain", "弁天山の征服者", "弁天山を訪れる", "syougou_image_bentenmountain", "BentenMountain"));
        staticTitlesMap.put("title_koshimaster", new Title("title_koshimaster", "合志マスター", "合志市の全スポットを訪れる", "syougou_image_koshimaster", "KoshiMaster"));
    }

    public static synchronized TitleManager getInstance() {
        if (instance == null) {
            instance = new TitleManager();
        }
        return instance;
    }

    public List<Title> getStaticTitles() {
        return new ArrayList<>(staticTitlesMap.values());
    }

//【変更点2】プレイヤーが持っているIDリストから、表示用のTitleリストを生成するメソッド
public List<Title> getUnlockedTitles(List<String> unlockedIds) {
    List<Title> resultList = new ArrayList<>();
    if (unlockedIds == null) {
        return resultList;
    }

    for (String id : unlockedIds) {
        Title title = getTitleById(id); // 下で定義するメソッドを呼び出す
        if (title != null) {
            resultList.add(title);
        }
    }
    return resultList;
}


//【変更点3】称号IDを元に、Titleオブジェクトを返す「翻訳」メソッド
public Title getTitleById(String titleId) {
    // 1. まず、静的な称号リストにIDがあるか探す
    if (staticTitlesMap.containsKey(titleId)) {
        return staticTitlesMap.get(titleId);
    }

    // 2. なければ、動的な「連続防衛称号」の形式かチェックする
    if (titleId != null && titleId.startsWith("title_") && titleId.contains("_defence_")) {
        try {
            String[] parts = titleId.split("_");
            // "title_Myosenji_defence_3" -> parts[0]="title", parts[1]="Myosenji", parts[2]="defence", parts[3]="3"
            String areaId = parts[1];
            int days = Integer.parseInt(parts[3]);

            // ★★★ ここで動的にTitleオブジェクトを生成する ★★★
            return createConsecutiveDefenceTitle(areaId, days);

        } catch (Exception e) {
            Log.e("TitleManager", "不正な形式の動的称号IDです: " + titleId, e);
            return null; // 不正なIDならnullを返す
        }
    }
    // 3. どの形式にも当てはまらなければ、nullを返す
    return null;
}

    //【変更点4】連続防衛称号の情報を動的に生成するヘルパーメソッド
private Title createConsecutiveDefenceTitle(String areaId, int days) {
    String titleId = "title_" + areaId + "_defence_" + days;

    // エリア名と画像名を決定 (もっと良い方法もありますが、まずはswitch文で)
    String titleName;
    String description;
    String imageName;

    switch (areaId) {
        case "myosenji":
            titleName = "妙泉寺の統治者";
            description = "妙泉寺の" + days + "日連続防衛成功"; // ← ここで数字を埋め込む！
            imageName = "syougou_image_myosenji";
            break;
        case "genkiPark":
            titleName = "元気の森公園の統治者";
            description = "元気の森公園" + days + "日連続防衛"; // ← ここで数字を埋め込む！
            imageName = "syougou_image_genkipark";
            break;
        case "koshicityhall":
            titleName = "合志市役所の統治者";
            description = "元気の森公園" + days + "日連続防衛"; // ← ここで数字を埋め込む！
            imageName = "syougou_image_koshicityhall";
            break;
        case "lutherchurch":
            titleName = "ルーテル合志教会の統治者";
            description = "教会" + days + "日連続防衛"; // ← ここで数字を埋め込む！
            imageName = "syougou_image_lutherchurch";
            break;
        case "countrypark":
            titleName = "カントリーパークの統治者";
            description = "カントリーパーク" + days + "日連続防衛"; // ← ここで数字を埋め込む！
            imageName = "syougou_image_countrypark";
            break;
        case "bentenmountain":
            titleName = "弁天山の統治者";
            description = "弁天山" + days + "日連続防衛"; // ← ここで数字を埋め込む！
            imageName = "syougou_image_bentenmountain";
            break;
        default:
            // 未知のエリアIDだった場合
            titleName = areaId + "の統治者";
            description = areaId + "を" + days + "日連続防衛";
            imageName = "default_image"; // デフォルト画像
            break;
    }
    return new Title(titleId, titleName, description, imageName, null);
    }
}