package org.coderoller.springlayout;

import static org.coderoller.springlayout.SpringLayout.ABOVE;
import static org.coderoller.springlayout.SpringLayout.ALIGN_BOTTOM;
import static org.coderoller.springlayout.SpringLayout.ALIGN_CENTER_HORIZONTALLY;
import static org.coderoller.springlayout.SpringLayout.ALIGN_CENTER_VERTICALLY;
import static org.coderoller.springlayout.SpringLayout.ALIGN_LEFT;
import static org.coderoller.springlayout.SpringLayout.ALIGN_RIGHT;
import static org.coderoller.springlayout.SpringLayout.ALIGN_TOP;
import static org.coderoller.springlayout.SpringLayout.BELOW;
import static org.coderoller.springlayout.SpringLayout.LEFT_OF;
import static org.coderoller.springlayout.SpringLayout.RIGHT_OF;

import org.coderoller.springlayout.LayoutMath.Value;
import org.coderoller.springlayout.LayoutMath.ValueWrapper;

import android.util.Log;
import android.view.View;

/**
 * Object that describes view constraints in the SpringLayout
 * @author sulewicz
 *
 */
public class ViewConstraints {
    private static final String TAG = ViewConstraints.class.getSimpleName();
    static final byte LEFT_ANCHOR = 1;
    static final byte RIGHT_ANCHOR = 1 << 1;
    static final byte TOP_ANCHOR = 1 << 2;
    static final byte BOTTOM_ANCHOR = 1 << 3;
    static final byte CENTER_HORIZONTAL_ANCHOR = 1 << 4;
    static final byte CENTER_VERTICAL_ANCHOR = 1 << 5;

    private byte mRelationFlags;
    private final View mView;
    private final boolean mSpring;
    final ValueWrapper left = LayoutMath.wrap();
    final ValueWrapper right = LayoutMath.wrap();
    final ValueWrapper top = LayoutMath.wrap();
    final ValueWrapper bottom = LayoutMath.wrap();
    final ValueWrapper topMargin = LayoutMath.wrap(LayoutMath.ZERO);
    final ValueWrapper bottomMargin = LayoutMath.wrap(LayoutMath.ZERO);
    final ValueWrapper leftMargin = LayoutMath.wrap(LayoutMath.ZERO);
    final ValueWrapper rightMargin = LayoutMath.wrap(LayoutMath.ZERO);
    final ValueWrapper mWidth = LayoutMath.wrap(right.subtract(left));
    final ValueWrapper mHeight = LayoutMath.wrap(bottom.subtract(top));
    
    final Value innerLeft = left.add(leftMargin);
    final Value innerRight = right.subtract(rightMargin);
    final Value innerTop = top.add(topMargin);
    final Value innerBottom = bottom.subtract(bottomMargin);

    // Used for building horizontal and vertical view chains.
    ViewConstraints prevX, nextX, prevY, nextY;

    private Value mCenterHorizontalAlignment, mCenterVerticalAlignment;

    public ViewConstraints(View view) {
        mView = view;
        mSpring = view instanceof Spring;
    }

    /**
     * Update child metrics based on relation to this view.
     * 
     * @param child
     *            Child relates to this view as specifies.
     * @param relation
     *            Relation type to this view.
     */
    public void updateRelation(ViewConstraints child, int relation) {
        if (!updateFlags(child, relation)) {
            throw new IllegalStateException(relationTypeToString(relation) + " relation " + child.getView() + " to " + mView
                    + " already exists! Failed on " + relationToString(relation) + ", please review your layout.");
        }
        switch (relation) {
        case LEFT_OF:
            if (prevX == null && child.nextX == null) {
                prevX = child;
                child.nextX = this;
            }
            child.right.setValueObject(left);
            break;
        case RIGHT_OF:
            if (nextX == null && child.prevX == null) {
                nextX = child;
                child.prevX = this;
            }
            child.left.setValueObject(right);
            break;
        case ALIGN_LEFT:
            child.left.setValueObject(innerLeft);
            break;
        case ALIGN_RIGHT:
            child.right.setValueObject(innerRight);
            break;
        case ABOVE:
            if (prevY == null && child.nextY == null) {
                prevY = child;
                child.nextY = this;
            }
            child.bottom.setValueObject(top);
            break;
        case BELOW:
            if (nextY == null && child.prevY == null) {
                nextY = child;
                child.prevY = this;
            }
            child.top.setValueObject(bottom);
            break;
        case ALIGN_TOP:
            child.top.setValueObject(innerTop);
            break;
        case ALIGN_BOTTOM:
            child.bottom.setValueObject(innerBottom);
            break;
        case ALIGN_CENTER_HORIZONTALLY:
            child.mCenterHorizontalAlignment = getHorizontalCenter();
            break;
        case ALIGN_CENTER_VERTICALLY:
            child.mCenterVerticalAlignment = getVerticalCenter();
            break;
        }
    }

    View getView() {
        return mView;
    }

    void setWidth(Value width) {
        setDimension(width, true);
    }

    void setHeight(Value height) {
        setDimension(height, false);
    }

    void setDimension(Value size, boolean horizontal) {
        final byte startFlag, endFlag, centerFlag;
        ValueWrapper start, end, sizeWrapper;
        Value alignment;
        if (horizontal) {
            startFlag = LEFT_ANCHOR;
            endFlag = RIGHT_ANCHOR;
            centerFlag = CENTER_HORIZONTAL_ANCHOR;
            start = left;
            end = right;
            alignment = mCenterHorizontalAlignment;
            sizeWrapper = mWidth;
        } else {
            startFlag = TOP_ANCHOR;
            endFlag = BOTTOM_ANCHOR;
            centerFlag = CENTER_VERTICAL_ANCHOR;
            start = top;
            end = bottom;
            alignment = mCenterVerticalAlignment;
            sizeWrapper = mHeight;
        }
        if ((mRelationFlags & centerFlag) != 0) {
            Value halfSize = size.divide(LayoutMath.TWO);
            start.setValueObject(alignment.subtract(halfSize));
            end.setValueObject(alignment.add(halfSize));
            sizeWrapper.setValueObject(size);
        } else if ((mRelationFlags & startFlag) == 0 && (mRelationFlags & endFlag) == 0) {
            throw new IllegalStateException("No anchor known!");
        } else if ((mRelationFlags & startFlag) == 0 || (mRelationFlags & endFlag) == 0) {
            if ((mRelationFlags & endFlag) == 0) {
                end.setValueObject(start.add(size));
            } else {
                start.setValueObject(end.subtract(size));
            }
            sizeWrapper.setValueObject(size);
        }
    }

    private boolean updateFlags(ViewConstraints childMetrics, int relation) {
        byte flags = 0;
        switch (relation) {
        case ALIGN_RIGHT:
        case LEFT_OF:
            flags = RIGHT_ANCHOR;
            break;
        case ALIGN_LEFT:
        case RIGHT_OF:
            flags = LEFT_ANCHOR;
            break;
        case ALIGN_BOTTOM:
        case ABOVE:
            flags = BOTTOM_ANCHOR;
            break;
        case ALIGN_TOP:
        case BELOW:
            flags = TOP_ANCHOR;
            break;
        case ALIGN_CENTER_HORIZONTALLY:
            flags = LEFT_ANCHOR | CENTER_HORIZONTAL_ANCHOR | RIGHT_ANCHOR;
            break;
        case ALIGN_CENTER_VERTICALLY:
            flags = TOP_ANCHOR | CENTER_VERTICAL_ANCHOR | BOTTOM_ANCHOR;
            break;
        }

        if ((childMetrics.mRelationFlags & flags) == 0) {
            childMetrics.mRelationFlags |= flags;
            return true;
        } else {
            // Some anchors already exist
            return false;
        }
    }

    Value getHorizontalCenter() {
        return innerLeft.add(innerRight).divide(LayoutMath.TWO);
    }

    Value getVerticalCenter() {
        return innerTop.add(innerBottom).divide(LayoutMath.TWO);
    }

    boolean isSpring() {
        return mSpring;
    }

    Value getWidth() {
        return mWidth;
    }

    Value getHeight() {
        return mHeight;
    }

    void dump() {
        Log.d(TAG, "mView = " + mView);
        Log.d(TAG, "x1 = " + left);
        Log.d(TAG, "x2 = " + right);
        Log.d(TAG, "y1 = " + top);
        Log.d(TAG, "y2 = " + bottom);
    }

    boolean hasHorizontalSibling() {
        return nextX != null || prevX != null;
    }

    boolean hasVerticalSibling() {
        return nextY != null || prevY != null;
    }
    
    static String relationToString(int relation) {
        switch (relation) {
        case ABOVE:
            return "ABOVE";
        case BELOW:
            return "BELOW";
        case LEFT_OF:
            return "LEFT_OF";
        case RIGHT_OF:
            return "RIGHT_OF";
        case ALIGN_LEFT:
            return "ALIGN_LEFT";
        case ALIGN_RIGHT:
            return "ALIGN_RIGHT";
        case ALIGN_TOP:
            return "ALIGN_TOP";
        case ALIGN_BOTTOM:
            return "ALIGN_BOTTOM";
        case ALIGN_CENTER_HORIZONTALLY:
            return "ALIGN_CENTER_HORIZONTALLY";
        case ALIGN_CENTER_VERTICALLY:
            return "ALIGN_CENTER_VERTICALLY";
        default:
            return "UNKNOWN";
        }
    }

    static String relationTypeToString(int relation) {
        switch (relation) {
        case ABOVE:
        case BELOW:
        case ALIGN_TOP:
        case ALIGN_BOTTOM:
        case ALIGN_CENTER_VERTICALLY:
            return "Vertical";
        case LEFT_OF:
        case RIGHT_OF:
        case ALIGN_LEFT:
        case ALIGN_RIGHT:
        case ALIGN_CENTER_HORIZONTALLY:
            return "Horizontal";
        default:
            return "Unknown";
        }
    }
}
