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
//        areaList.add(new Area("Knct", "熊本高専",32.8803, 130.7583,3000));
        areaList.add(new Area("Myosenji","妙泉寺",32.8742,130.7819,500));
        areaList.add(new Area("GenkiPark","元気の森公園",32.8883,130.7433,500));
        areaList.add(new Area("KoshiCityHall","合志市役所",32.8847,130.7600,400));
        areaList.add(new Area("LutherChurch","ルーテル合志教会",32.8825,130.7711,300));
        areaList.add(new Area("CountryPark","カントリーパーク",32.8794,130.7589,500));
        areaList.add(new Area("BentenMountain","弁天山",32.8931,130.7589,600));

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

    public Area getAreaById(String areaId) {
        if (areaId == null || areaList == null) {
            return null;
        }
        // areaListをループして、IDが一致する最初のAreaを探す
        for (Area area : areaList) {
            if (areaId.equals(area.getId())) {
                return area;
            }
        }
        // 最後まで見つからなかった場合はnullを返す
        return null;
    }

    //すべてのエリアのリストを取得するメソッド(一応)
    public List<Area> getAreaList(){
        return areaList;
    }
}
