package org.coderoller.springlayout;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Spring view used for setting other views constraints.
 * @author sulewicz
 *
 */
public class Spring extends View {
    public Spring(Context context) {
        super(context);
    }

    public Spring(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Spring(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
    }
}
