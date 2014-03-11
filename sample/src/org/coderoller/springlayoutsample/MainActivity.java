package org.coderoller.springlayoutsample;

import org.coderoller.springlayout.SpringLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class MainActivity extends Activity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    RelativeWidthChangeAnim aAnimation = new RelativeWidthChangeAnim(findViewById(R.id.A), 10, 50);
    aAnimation.setDuration(500);
    aAnimation.setRepeatCount(Animation.INFINITE);
    aAnimation.setRepeatMode(Animation.REVERSE);
    findViewById(R.id.A).startAnimation(aAnimation);
    
    RelativeWidthChangeAnim bAnimation = new RelativeWidthChangeAnim(findViewById(R.id.B), 50, 10);
    bAnimation.setDuration(1000);
    bAnimation.setRepeatCount(Animation.INFINITE);
    bAnimation.setRepeatMode(Animation.REVERSE);
    findViewById(R.id.B).startAnimation(bAnimation);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.test_sandbox:
      startActivity(new Intent(this, TestSandboxActivity.class));
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }
}

class RelativeWidthChangeAnim extends Animation {
    View mView;
    SpringLayout.LayoutParams mLayoutParams;
    private int mFrom, mTo;
    
    public RelativeWidthChangeAnim(View view, int from, int to) {
        mView = view;
        mLayoutParams = (SpringLayout.LayoutParams) view.getLayoutParams();
        mFrom = from;
        mTo = to;
    }
    
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int relativeWidth;
        relativeWidth = (int) (mFrom + interpolatedTime * (mTo - mFrom));
        mLayoutParams.setRelativeWidth(relativeWidth);
        mView.requestLayout();
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
