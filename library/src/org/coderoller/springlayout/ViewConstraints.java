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

    byte mRelationFlags;
    final View mView;
    final boolean mSpring;
    final ValueWrapper x1 = LayoutMath.wrap();
    final ValueWrapper x2 = LayoutMath.wrap();
    final ValueWrapper y1 = LayoutMath.wrap();
    final ValueWrapper y2 = LayoutMath.wrap();
    final ValueWrapper mWidth = LayoutMath.wrap(x2.subtract(x1));
    final ValueWrapper mHeight = LayoutMath.wrap(y2.subtract(y1));

    // Used for building horizontal and vertical view chains.
    ViewConstraints mPrevX, mNextX, mPrevY, mNextY;

    Value mCenterHorizontalAlignment, mCenterVerticalAlignment;

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
            if (mPrevX == null && child.mNextX == null) {
                mPrevX = child;
                child.mNextX = this;
            }
            child.x2.setValueObject(x1);
            break;
        case RIGHT_OF:
            if (mNextX == null && child.mPrevX == null) {
                mNextX = child;
                child.mPrevX = this;
            }
            child.x1.setValueObject(x2);
            break;
        case ALIGN_LEFT:
            child.x1.setValueObject(x1);
            break;
        case ALIGN_RIGHT:
            child.x2.setValueObject(x2);
            break;
        case ABOVE:
            if (mPrevY == null && child.mNextY == null) {
                mPrevY = child;
                child.mNextY = this;
            }
            child.y2.setValueObject(y1);
            break;
        case BELOW:
            if (mNextY == null && child.mPrevY == null) {
                mNextY = child;
                child.mPrevY = this;
            }
            child.y1.setValueObject(y2);
            break;
        case ALIGN_TOP:
            child.y1.setValueObject(y1);
            break;
        case ALIGN_BOTTOM:
            child.y2.setValueObject(y2);
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
            start = x1;
            end = x2;
            alignment = mCenterHorizontalAlignment;
            sizeWrapper = mWidth;
        } else {
            startFlag = TOP_ANCHOR;
            endFlag = BOTTOM_ANCHOR;
            centerFlag = CENTER_VERTICAL_ANCHOR;
            start = y1;
            end = y2;
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
        return x1.add(x2).divide(LayoutMath.TWO);
    }

    Value getVerticalCenter() {
        return y1.add(y2).divide(LayoutMath.TWO);
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
        Log.d(TAG, "x1 = " + x1);
        Log.d(TAG, "x2 = " + x2);
        Log.d(TAG, "y1 = " + y1);
        Log.d(TAG, "y2 = " + y2);
    }

    boolean hasHorizontalSibling() {
        return mNextX != null || mPrevX != null;
    }

    boolean hasVerticalSibling() {
        return mNextY != null || mPrevY != null;
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
