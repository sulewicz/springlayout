package org.coderoller.springlayoutsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class PerformanceTest2Activity extends Activity implements OnClickListener {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_performance_test2);
    findViewById(R.id.performance_spring_layout2).setOnClickListener(this);
    findViewById(R.id.performance_relative_layout2).setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    TextView tv = (TextView) v.findViewById(R.id.A);
    tv.setText(tv.getText() + "_");
  }
}
