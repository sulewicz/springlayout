package org.coderoller.springlayoutsample;

import org.coderoller.springlayout.SpringLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

public class ProxySpringLayout extends SpringLayout {
  private static final String TAG = ProxySpringLayout.class.getSimpleName();

  public ProxySpringLayout(Context context) {
    super(context);
  }

  public ProxySpringLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ProxySpringLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final long start = System.nanoTime();
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    Log.d(TAG, "onMeasure(): " + (System.nanoTime() - start));
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final long start = System.nanoTime();
    requestLayout();
    super.onLayout(changed, l, t, r, b);
    Log.d(TAG, "onLayout(): " + (System.nanoTime() - start));
  }
}
