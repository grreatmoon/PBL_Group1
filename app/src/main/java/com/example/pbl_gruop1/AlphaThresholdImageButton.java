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
        // 画像が設定されていない、またはBitmapでない場合は通常の動作
        if (getDrawable() == null || !(getDrawable() instanceof BitmapDrawable)) {
            return super.onTouchEvent(event);
        }

        // タッチされた座標を取得
        int x = (int) event.getX();
        int y = (int) event.getY();

        // 画像の範囲外がタップされた場合はイベントを無視
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return false;
        }

        // 表示されている画像のBitmapを取得
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();

        // 表示サイズと画像の元サイズから、タップされた位置に対応するピクセルの座標を計算
        int pixelX = (int) (x * ((float) bitmap.getWidth() / getWidth()));
        int pixelY = (int) (y * ((float) bitmap.getHeight() / getHeight()));

        // 安全のため、計算後の座標がBitmapの範囲内かチェック
        if (pixelX < 0 || pixelX >= bitmap.getWidth() || pixelY < 0 || pixelY >= bitmap.getHeight()) {
            return false;
        }

        // ピクセルの色情報を取得
        int pixel = bitmap.getPixel(pixelX, pixelY);

        // ピクセルのアルファ値（透明度）を抽出
        int alpha = (pixel >> 24) & 0xff;

        // アルファ値が閾値より低い（透明）なら、タッチイベントを無視して親にも伝えない
        if (alpha < ALPHA_THRESHOLD) {
            return false;
        }

        // 閾値より濃い部分がタップされたので、通常のタッチ処理を実行
        return super.onTouchEvent(event);
    }
}
