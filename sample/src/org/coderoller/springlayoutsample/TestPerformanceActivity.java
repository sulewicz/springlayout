package org.coderoller.springlayoutsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class TestPerformanceActivity extends Activity {
  private View mSpringLayoutAnimatedView;
  private View mRelativeLayoutAnimatedView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.test_performance);
    mSpringLayoutAnimatedView = findViewById(R.id.performance_spring_layout).findViewById(R.id.B);
    mRelativeLayoutAnimatedView = findViewById(R.id.performance_relative_layout).findViewById(R.id.B);
    
    WidthChangeAnimation springLayoutAnimation = new WidthChangeAnimation(mSpringLayoutAnimatedView, 
        getResources().getDimensionPixelSize(R.dimen.performance_resize_width));
    springLayoutAnimation.setDuration(1000);
    springLayoutAnimation.setRepeatCount(Animation.INFINITE);
    springLayoutAnimation.setRepeatMode(Animation.REVERSE);
    mSpringLayoutAnimatedView.startAnimation(springLayoutAnimation);
    
    WidthChangeAnimation relativeLayoutAnimation = new WidthChangeAnimation(mRelativeLayoutAnimatedView, 
        getResources().getDimensionPixelSize(R.dimen.performance_resize_width));
    relativeLayoutAnimation.setDuration(1000);
    relativeLayoutAnimation.setRepeatCount(Animation.INFINITE);
    relativeLayoutAnimation.setRepeatMode(Animation.REVERSE);
    mRelativeLayoutAnimatedView.startAnimation(relativeLayoutAnimation);
  }
}

class WidthChangeAnimation extends Animation {
  private int mWidth;
  private View mView;

  public WidthChangeAnimation(View view, int width) {
    mView = view;
    mWidth = width;
  }

  @Override
  protected void applyTransformation(float interpolatedTime, Transformation t) {
    int newWidth = (int) (mWidth * interpolatedTime);
    mView.getLayoutParams().width = newWidth;
    mView.requestLayout();
  }

  @Override
  public boolean willChangeBounds() {
    return true;
  }
}