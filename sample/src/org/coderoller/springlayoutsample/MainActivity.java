package org.coderoller.springlayoutsample;

import org.coderoller.springlayout.SpringLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
  private static final int DYNAMIC_TEST_STEP_COUNT = 4;
  private int mDynamicTestStep = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.relative_position_test).setOnClickListener(this);
    findViewById(R.id.visibility_test).setOnClickListener(this);
    findViewById(R.id.dynamic_test).setOnClickListener(this);
    findViewById(R.id.spring_test).setOnClickListener(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.action_performance_test1:
      startActivity(new Intent(this, PerformanceTest1Activity.class));
      return true;
    case R.id.action_performance_test2:
      startActivity(new Intent(this, PerformanceTest2Activity.class));
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onClick(View v) {
    final ViewGroup viewGroup = (ViewGroup) v;
    switch (v.getId()) {
    case R.id.relative_position_test: {
      TextView a = (TextView) v.findViewById(R.id.A);
      a.setText(a.getText() + "_");
      break;
    }
    case R.id.visibility_test: {
      View c = v.findViewById(R.id.C);
      View g = v.findViewById(R.id.G);
      c.setVisibility(c.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
      g.setVisibility(g.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
      break;
    }
    case R.id.dynamic_test: {
      SpringLayout.LayoutParams params = ((SpringLayout.LayoutParams) v.findViewById(R.id.A).getLayoutParams());
      switch (mDynamicTestStep) {
      case 0:
        TextView tv = new TextView(this);
        tv.setText("TEXT VIEW");
        tv.setBackgroundColor(Color.BLUE);
        viewGroup.addView(tv);
        break;
      case 1:
        viewGroup.removeViewAt(viewGroup.getChildCount() - 1);
        break;
      case 2:
        params.setRelativeWidth(50);
        v.requestLayout();
        break;
      case 3:
        params.setRelativeWidth(0);
        v.requestLayout();
        break;
      }
      mDynamicTestStep = (mDynamicTestStep + 1) % DYNAMIC_TEST_STEP_COUNT;
      break;
    }
    case R.id.spring_test: {
        View a = v.findViewById(R.id.A);
        a.setVisibility(a.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        break;
    }
    }
  }
}
