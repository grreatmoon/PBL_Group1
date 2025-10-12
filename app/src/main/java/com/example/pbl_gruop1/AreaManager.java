package com.example.pbl_gruop1;

import android.location.Location;
import java.util.ArrayList;
import java.util.List;

public class AreaManager {

    //シングルトン
    private static AreaManager instance;

    //アプリ内の全エリア情報を保持するリスト,DB的なもの
    private List<Area> areaList;

    //プライベートコンストラクタでエリア情報を定義
    private AreaManager() {
        areaList = new ArrayList<>();

        //ここに合志市のエリアデータを追加していく
        areaList.add(new Area("knct", "熊本高専",32.8803, 130.7583,500));//とりあえず半径500で

    }

    public static synchronized AreaManager getInstance() {
        if (instance == null) {
            instance = new AreaManager();
        }
        return instance;
    }

    //現在地がどのエリアにあるか判断するメソッド
    public Area checkCurrentArea(Location currentLocation){
        //すべてのエリアを一つずつチェック
        for (Area area: areaList) {
            //エリアの中心地のLocationオブジェクトを作成
            Location areaLocation = new Location("");
            //areaから緯度と経度をareaLocationに追加
            areaLocation.setLatitude(area.getLatitude());
            areaLocation.setLongitude(area.getLongitude());

            //現在地とエリア中心地との距離を計算(m)
            float distance = currentLocation.distanceTo(areaLocation);

            //もし距離がエリアの半径内ならそのエリアの情報を返す
            if (distance < area.getRadius()) {
                return area;
            }

        }

        //どのエリアにも入っていなければnullを返す
        return null;
    }

    //すべてのエリアのリストを取得するメソッド(一応)
    public List<Area> getAreaList(){
        return areaList;
    }
}
