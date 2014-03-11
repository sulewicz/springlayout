package org.coderoller.springlayoutsample;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

public class ProxyRelativeLayout extends RelativeLayout {
  private static final String TAG = ProxyRelativeLayout.class.getSimpleName();

  public ProxyRelativeLayout(Context context) {
    super(context);
  }

  public ProxyRelativeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ProxyRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final long start = System.currentTimeMillis();
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    Log.d(TAG, "onMeasure(): " + (System.currentTimeMillis() - start));
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final long start = System.currentTimeMillis();
    requestLayout();
    super.onLayout(changed, l, t, r, b);
    Log.d(TAG, "onLayout(): " + (System.currentTimeMillis() - start));
  }
}
