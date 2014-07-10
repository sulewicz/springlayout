package org.coderoller.springlayoutsample;

import org.coderoller.springlayout.SpringLayout;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TestSandboxActivity extends Activity implements OnClickListener {
  private static final SparseIntArray TEST_LAYOUTS = new SparseIntArray();
  static {
    TEST_LAYOUTS.append(R.string.margin_accommodation_test, R.layout.margin_accommodation_test);
    TEST_LAYOUTS.append(R.string.relative_position_test, R.layout.relative_position_test);
    TEST_LAYOUTS.append(R.string.parent_alignment_test, R.layout.parent_alignment_test);
    TEST_LAYOUTS.append(R.string.alignment_test, R.layout.alignment_test);
    TEST_LAYOUTS.append(R.string.relative_size_test, R.layout.relative_size_test);
    TEST_LAYOUTS.append(R.string.visibility_test, R.layout.visibility_test);
    TEST_LAYOUTS.append(R.string.spring_test, R.layout.spring_test);
    TEST_LAYOUTS.append(R.string.dynamic_test, R.layout.dynamic_test);
    TEST_LAYOUTS.append(R.string.readme_example_center_alignment, R.layout.readme_example_center_alignment);
    TEST_LAYOUTS.append(R.string.readme_example_relative_size, R.layout.readme_example_relative_size);
    TEST_LAYOUTS.append(R.string.readme_example_wrap_content_size, R.layout.readme_example_wrap_content_size);
    TEST_LAYOUTS.append(R.string.readme_example_springs, R.layout.readme_example_springs);
    TEST_LAYOUTS.append(R.string.comparison_test1, R.layout.comparison_test1);
    TEST_LAYOUTS.append(R.string.comparison_test2, R.layout.comparison_test2);
  }
  private static final int[] CLICKABLE_TESTS = new int[] {
    R.id.relative_position_test,
    R.id.visibility_test,
    R.id.dynamic_test,
    R.id.spring_test,
    R.id.comparison_test_1,
    R.id.comparison_test_2
  };
  private static final int DYNAMIC_TEST_STEP_COUNT = 4;
  private int mDynamicTestStep = 0;
  
  private LinearLayout mContainer; 

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.test_sandbox);
    
    mContainer = (LinearLayout) findViewById(R.id.test_layouts_container);
    LayoutInflater inflater = LayoutInflater.from(this);
    for (int i = 0; i < TEST_LAYOUTS.size(); ++i) {
        addLayout(inflater, TEST_LAYOUTS.keyAt(i), TEST_LAYOUTS.valueAt(i));
    }

    for (int id : CLICKABLE_TESTS) {
      setClickListener(id);
    }
  }
  
  private void setClickListener(final int id) {
    View v = findViewById(id);
    if (v != null) {
      v.setOnClickListener(this);
    }
  }
  
  private void addLayout(LayoutInflater inflater, int titleResId, int layoutResId) {
    TextView title = new TextView(this);
    title.setText(titleResId);
    title.setTextColor(Color.BLACK);
    title.setBackgroundResource(R.color.title_background);
    mContainer.addView(title);
    inflater.inflate(R.layout.horizontal_divider, mContainer);
    View layout = inflater.inflate(layoutResId, mContainer);
    layout.setOnClickListener(this);
    inflater.inflate(R.layout.horizontal_divider, mContainer);
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
    case R.id.comparison_test_1: {
        TextView tv = (TextView) v.findViewById(R.id.comparison_spring_layout1).findViewById(R.id.A);
        tv.setText(tv.getText() + "_");
        tv = (TextView) v.findViewById(R.id.comparison_relative_layout1).findViewById(R.id.A);
        tv.setText(tv.getText() + "_");
        break;
    }
    case R.id.comparison_test_2: {
        TextView tv = (TextView) v.findViewById(R.id.comparison_spring_layout2).findViewById(R.id.A);
        tv.setText(tv.getText() + "_");
        tv = (TextView) v.findViewById(R.id.comparison_relative_layout2).findViewById(R.id.A);
        tv.setText(tv.getText() + "_");
        break;
    }
    }
  }
}
