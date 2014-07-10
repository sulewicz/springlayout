package org.coderoller.springlayoutsample;

public interface MeasurableLayout {
  int getMeasuresCount();
  long getTotalMeasuresTime();
  long getAverageMeasureTime();
  int getLayoutsCount();
  long getTotalLayoutsTime();
  long getAverageLayoutTime();
}
