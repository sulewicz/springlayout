package org.coderoller.springlayoutsample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

public class TestPerformanceActivity extends Activity implements OnClickListener {
  private static final long STATS_REFRESH_TIME = 1000;
  private MeasurableLayout mSpringLayout;
  private MeasurableLayout mRelativeLayout;
  private TextView mPerformanceStatsTextView;
  private View mSpringLayoutAnimatedView;
  private View mRelativeLayoutAnimatedView;
  private Handler mHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.test_performance);
    mPerformanceStatsTextView = (TextView) findViewById(R.id.performance_stats);
    mPerformanceStatsTextView.setOnClickListener(this);
    ViewGroup springLayout = (ViewGroup) findViewById(R.id.performance_spring_layout);
    mSpringLayout = (MeasurableLayout) springLayout;
    mSpringLayoutAnimatedView = springLayout.findViewById(R.id.B);
    
    ViewGroup relativeLayout = (ViewGroup) findViewById(R.id.performance_relative_layout);
    mRelativeLayout = (MeasurableLayout) relativeLayout;
    mRelativeLayoutAnimatedView = relativeLayout.findViewById(R.id.B);
    
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
  
  @Override
  protected void onResume() {
    super.onResume();
    mHandler = new Handler();
    mHandler.postDelayed(mStatsRefresher, STATS_REFRESH_TIME);
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    mHandler.removeCallbacks(mStatsRefresher);
    mHandler = null;
  }
  
  private void refreshStats() {
    mPerformanceStatsTextView.setText(getString(R.string.performance_stats_text,
        mSpringLayout.getAverageMeasureTime(),
        mSpringLayout.getAverageLayoutTime(),
        mRelativeLayout.getAverageMeasureTime(),
        mRelativeLayout.getAverageLayoutTime()
    ));
  }

  @Override
  public void onClick(View view) {
    refreshStats();
  }
  
  private final Runnable mStatsRefresher = new Runnable() {
    @Override
    public void run() {
      refreshStats();
      if (mHandler != null) {
        mHandler.postDelayed(mStatsRefresher, STATS_REFRESH_TIME);
      }
    }
  };
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