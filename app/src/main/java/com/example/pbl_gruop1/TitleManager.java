package com.example.pbl_gruop1;

import java.util.ArrayList;
import java.util.List;

public class TitleManager {

    private static TitleManager instance;
    private List<Title> titleList;  //アプリ内の全称号情報を保持するリスト

    private TitleManager() {    //称号の情報を定義
        titleList = new ArrayList<>();

        titleList.add(new Title("title_myosenji", "妙泉寺の僧侶", "妙泉寺を訪れる",R.drawable.syougou_image_myosenji, "Myosenji"));
        titleList.add(new Title("title_genkipark", "元気の森の冒険家", "元気の森公園を訪れる", R.drawable.syougou_image_genkipark, "GenkiPark"));
        titleList.add(new Title("title_koshicityhall", "合志市の市民", "合志市役所を訪れる", R.drawable.syougou_image_koshicityhall, "KoshiCityHall"));
        titleList.add(new Title("title_lutherchurch", "教会の聖歌隊", "ルーテル合志教会を訪れる", R.drawable.syougou_image_lutherchurch, "LutherChurch"));
        titleList.add(new Title("title_countrypark", "パークの探検家", "カントリーパークを訪れる", R.drawable.syougou_image_countrypark, "CountryPark"));
        titleList.add(new Title("title_bentenmountain", "弁天山の征服者", "弁天山を訪れる", R.drawable.syougou_image_bentenmountain, "BentenMountain"));


    }

    public static synchronized TitleManager getInstance() {
        if (instance == null) {
            instance = new TitleManager();
        }
        return instance;
    }

    // 全ての称号リストを取得するメソッド
    public List<Title> getTitleList() {
        return titleList;
    }

}
