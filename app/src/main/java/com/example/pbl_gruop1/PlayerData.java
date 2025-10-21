package com.example.pbl_gruop1;

import java.util.ArrayList;
import java.util.List;

// ゲーム内の基本的なデータを保持するクラス
public class PlayerData {

    // プレイヤー名, いらない可能性大？
    public String playerName;

    // プレイヤーレベル
    public int level;

    // 現在の所持エネルギー
    public int energy;

    // エネルギーの最大値
    public int maxEnergy;

    // 現実世界での総移動距離（メートル単位？）
    public double realWorldDistance;

    // 解放済みのエリアIDのリスト
    public List<String> unlockedAreaIds;

    // 獲得済みの称号IDのリスト
    public List<String> unlockedTitleIds;

    // デフォルトコンストラクタ
    // 新規プレイヤー作成時の初期値を設定
    public PlayerData() {
        this.playerName = "New Player";
        this.level = 1;
        this.energy = 0;
        this.maxEnergy = 100;
        this.realWorldDistance = 0.0; // 移動距離を0.0で初期化
        this.unlockedAreaIds = new ArrayList<>();
        this.unlockedTitleIds = new ArrayList<>();
    }
}
