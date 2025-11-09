package com.example.pbl_gruop1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatImageButton;

//AlphaThresholdで画像の当たり判定を細かく設定。API10以下でも使えるようにするために実装

public class AlphaThresholdImageButton extends AppCompatImageButton {

    // この値よりアルファ値が低い（透明な）ピクセルはタップを無視する (0-255の範囲)
    private static final int ALPHA_THRESHOLD = 10; // 完全に透明でなくても、少しでも透明なら無視するくらいの設定

    public AlphaThresholdImageButton(Context context) {
        super(context);
    }

    public AlphaThresholdImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaThresholdImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //マップ操作モードの時に地図に当たり判定がすわれないようにするためのコード
        if (!isClickable()) {
            return false;
        }

        // 画像が設定されていない、またはBitmapでない場合は通常の動作
        if (getDrawable() == null || !(getDrawable() instanceof BitmapDrawable)) {
            return super.onTouchEvent(event);
        }

        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        if (bitmap == null) {
            return super.onTouchEvent(event);
        }

        // ビュー(ボタン)のサイズ
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // ビットマップ(元画像)のサイズ
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();

        // scaleType="fitCenter" の計算
        float scale;
        float dx = 0, dy = 0; // 画像描画開始位置のオフセット(余白)

        // 画像とビューのアスペクト比を比較
        if (bmpWidth * viewHeight > viewWidth * bmpHeight) {
            // ビューの幅に合わせる (画像が横長 or ビューが縦長)
            scale = (float) viewWidth / (float) bmpWidth;
            dy = (viewHeight - bmpHeight * scale) * 0.5f; // 上下の余白
        } else {
            // ビューの高さに合わせる (画像が縦長 or ビューが横長)
            scale = (float) viewHeight / (float) bmpHeight;
            dx = (viewWidth - bmpWidth * scale) * 0.5f; // 左右の余白
        }

        // 画像が実際に描画されている領域のスケール後のサイズ
        float scaledWidth = bmpWidth * scale;
        float scaledHeight = bmpHeight * scale;

        // タッチされた座標
        float x = event.getX();
        float y = event.getY();

        // 1. まず、画像の外側（fitCenterの余白）をタップしていないかチェック
        if (x < dx || x > dx + scaledWidth || y < dy || y > dy + scaledHeight) {
            return false; // 透明な余白部分をタップしたので無視
        }

        // 2. ビューの座標から、ビットマップのピクセル座標に変換
        // (タップ座標 - 余白) / スケール
        int pixelX = (int) ((x - dx) / scale);
        int pixelY = (int) ((y - dy) / scale);

        // 安全のため、計算後の座標がBitmapの範囲内かチェック
        if (pixelX < 0 || pixelX >= bmpWidth || pixelY < 0 || pixelY >= bmpHeight) {
            return false;
        }

        // ピクセルの色情報を取得
        int pixel = bitmap.getPixel(pixelX, pixelY);

        // ピクセルのアルファ値（透明度）を抽出
        int alpha = (pixel >> 24) & 0xff;

        // アルファ値が閾値より低い（透明）なら、タッチイベントを無視
        if (alpha < ALPHA_THRESHOLD) {
            return false;
        }

        // 閾値より濃い部分がタップされたので、通常のタッチ処理を実行
        return super.onTouchEvent(event);
    }
}
