package org.coderoller.springlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.coderoller.springlayout.LayoutMath.Value;
import org.coderoller.springlayout.LayoutMath.ValueWrapper;

public class SpringLayout extends ViewGroup {
    private static int RELATIVE_SIZE_DENOMINATOR = LayoutMath.HUNDRED.getValue();

    public static final int PARENT = -2;
    public static final int TRUE = -1;

    /**
     * Rule that aligns a child's right edge with another child's left edge.
     */
    public static final int LEFT_OF = 0;
    /**
     * Rule that aligns a child's left edge with another child's right edge.
     */
    public static final int RIGHT_OF = 1;
    /**
     * Rule that aligns a child's bottom edge with another child's top edge.
     */
    public static final int ABOVE = 2;
    /**
     * Rule that aligns a child's top edge with another child's bottom edge.
     */
    public static final int BELOW = 3;
    /**
     * Rule that aligns a child's left edge with another child's left edge.
     */
    public static final int ALIGN_LEFT = 4;
    /**
     * Rule that aligns a child's top edge with another child's top edge.
     */
    public static final int ALIGN_TOP = 5;
    /**
     * Rule that aligns a child's right edge with another child's right edge.
     */
    public static final int ALIGN_RIGHT = 6;
    /**
     * Rule that aligns a child's bottom edge with another child's bottom edge.
     */
    public static final int ALIGN_BOTTOM = 7;
    /**
     * Center will be aligned both horizontally and vertically.
     */
    public static final int ALIGN_CENTER = 8;
    /**
     * Center will be aligned horizontally.
     */
    public static final int ALIGN_CENTER_HORIZONTALLY = 9;
    /**
     * Center will be aligned vertically.
     */
    public static final int ALIGN_CENTER_VERTICALLY = 10;

    /**
     * Rule that aligns the child's left edge with its SpringLayout parent's
     * left edge.
     */
    private static final int ALIGN_PARENT_LEFT = 11;
    /**
     * Rule that aligns the child's top edge with its SpringLayout parent's top
     * edge.
     */
    private static final int ALIGN_PARENT_TOP = 12;
    /**
     * Rule that aligns the child's right edge with its SpringLayout parent's
     * right edge.
     */
    private static final int ALIGN_PARENT_RIGHT = 13;
    /**
     * Rule that aligns the child's bottom edge with its SpringLayout parent's
     * bottom edge.
     */
    private static final int ALIGN_PARENT_BOTTOM = 14;

    /**
     * Rule that centers the child with respect to the bounds of its
     * SpringLayout parent.
     */
    public static final int CENTER_IN_PARENT = 15;
    /**
     * Rule that centers the child horizontally with respect to the bounds of
     * its SpringLayout parent.
     */
    public static final int CENTER_HORIZONTAL = 16;
    /**
     * Rule that centers the child vertically with respect to the bounds of its
     * SpringLayout parent.
     */
    public static final int CENTER_VERTICAL = 17;

    private static final int VERB_COUNT = 18;

    private static int[] VALID_RELATIONS = new int[] { LEFT_OF, RIGHT_OF, ALIGN_LEFT, ALIGN_RIGHT, ABOVE, BELOW, ALIGN_TOP, ALIGN_BOTTOM,
            ALIGN_CENTER_HORIZONTALLY, ALIGN_CENTER_VERTICALLY };

    private ViewConstraints mRootMetrics;
    private final SparseIntArray mIdToViewMetrics = new SparseIntArray();
    private ViewConstraints[] mViewMetrics;

    private boolean mDirtyHierarchy = true;
    private boolean mDirtySize = true;

    private int mMinWidth = 0, mMinHeight = 0;

    public SpringLayout(Context context) {
        super(context);
    }

    public SpringLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFromAttributes(context, attrs);
    }

    public SpringLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initFromAttributes(context, attrs);
    }

    private void initFromAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpringLayout);
        setMinimumWidth(a.getDimensionPixelSize(R.styleable.SpringLayout_minWidth, 0));
        setMinimumHeight(a.getDimensionPixelSize(R.styleable.SpringLayout_minHeight, 0));
        a.recycle();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        mDirtyHierarchy = true;
        super.addView(child, index, params);
    }

    @Override
    public void removeView(View view) {
        mDirtyHierarchy = true;
        super.removeView(view);
    }

    @Override
    public void removeViewAt(int index) {
        mDirtyHierarchy = true;
        super.removeViewAt(index);
    }

    @Override
    public void removeViews(int start, int count) {
        mDirtyHierarchy = true;
        super.removeViews(start, count);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        mDirtySize = true;

        if (!mDirtyHierarchy) {
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final LayoutParams params = ((LayoutParams) getChildAt(i).getLayoutParams());
                if (params.dirty) {
                    mDirtyHierarchy = true;
                    params.dirty = false;
                }
            }
        }
    }

    private Stack<ViewConstraints> createViewMetrics() {
        final Stack<ViewConstraints> springMetrics = new Stack<ViewConstraints>();

        mViewMetrics = new ViewConstraints[getChildCount()];
        mIdToViewMetrics.clear();
        mRootMetrics = new ViewConstraints(SpringLayout.this);
        mRootMetrics.x1.setValueObject(LayoutMath.constant(getPaddingLeft()));
        mRootMetrics.y1.setValueObject(LayoutMath.constant(getPaddingTop()));

        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View v = getChildAt(i);
            mIdToViewMetrics.append(v.getId(), i);
            mViewMetrics[i] = new ViewConstraints(v);
        }

        for (int i = 0; i < count; i++) {
            final ViewConstraints viewMetrics = mViewMetrics[i];
            final LayoutParams layoutParams = (LayoutParams) viewMetrics.getView().getLayoutParams();
            int[] childRules = layoutParams.getRelations();
            for (int relation : VALID_RELATIONS) {
                final ViewConstraints metrics = getViewMetrics(childRules[relation]);
                if (metrics != null) {
                    metrics.updateRelation(viewMetrics, relation);
                }
            }
            if (viewMetrics.isSpring()) {
                springMetrics.add(viewMetrics);
            }
        }
        return springMetrics;
    }

    private ViewConstraints getViewMetrics(int id) {
        if (id == PARENT) {
            return mRootMetrics;
        } else if (id > 0 && mIdToViewMetrics.indexOfKey(id) >= 0) {
            return mViewMetrics[mIdToViewMetrics.get(id)];
        }
        return null;
    }

    private void adaptLayoutParameters() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final LayoutParams childParams = (LayoutParams) child.getLayoutParams();
            int[] relations = childParams.getRelations();

            // If view is aligned both to parent's top and bottom (left and
            // right) then its height (width) is MATCH_PARENT and the other way
            // around
            if (relations[ALIGN_PARENT_TOP] != 0 && relations[ALIGN_PARENT_BOTTOM] != 0) {
                childParams.height = LayoutParams.MATCH_PARENT;
            } else if (childParams.height == LayoutParams.MATCH_PARENT) {
                relations[ALIGN_PARENT_TOP] = relations[ALIGN_PARENT_BOTTOM] = TRUE;
            }

            if (relations[ALIGN_PARENT_LEFT] != 0 && relations[ALIGN_PARENT_RIGHT] != 0) {
                childParams.width = LayoutParams.MATCH_PARENT;
            } else if (childParams.width == LayoutParams.MATCH_PARENT) {
                relations[ALIGN_PARENT_LEFT] = relations[ALIGN_PARENT_RIGHT] = TRUE;
            }

            if (relations[ALIGN_PARENT_TOP] == TRUE) {
                relations[ALIGN_TOP] = PARENT;
            }

            if (relations[ALIGN_PARENT_BOTTOM] == TRUE) {
                relations[ALIGN_BOTTOM] = PARENT;
            }

            if (relations[ALIGN_PARENT_LEFT] == TRUE) {
                relations[ALIGN_LEFT] = PARENT;
            }

            if (relations[ALIGN_PARENT_RIGHT] == TRUE) {
                relations[ALIGN_RIGHT] = PARENT;
            }

            if (relations[ALIGN_CENTER] != 0) {
                relations[ALIGN_CENTER_HORIZONTALLY] = relations[ALIGN_CENTER];
                relations[ALIGN_CENTER_VERTICALLY] = relations[ALIGN_CENTER];
            }

            if (relations[CENTER_IN_PARENT] == TRUE) {
                relations[CENTER_HORIZONTAL] = relations[CENTER_VERTICAL] = TRUE;
            }

            if (relations[CENTER_HORIZONTAL] == TRUE) {
                relations[ALIGN_CENTER_HORIZONTALLY] = PARENT;
            }

            if (relations[CENTER_VERTICAL] == TRUE) {
                relations[ALIGN_CENTER_VERTICALLY] = PARENT;
            }

            if (!hasHorizontalRelations(relations)) {
                relations[ALIGN_LEFT] = PARENT;
            }

            if (!hasVerticalRelations(relations)) {
                relations[ALIGN_TOP] = PARENT;
            }
        }
    }

    private boolean hasHorizontalRelations(int[] relations) {
        return relations[LEFT_OF] != 0 || relations[RIGHT_OF] != 0 || relations[ALIGN_LEFT] != 0 || relations[ALIGN_RIGHT] != 0
                || relations[ALIGN_CENTER_HORIZONTALLY] != 0;
    }

    private boolean hasVerticalRelations(int[] relations) {
        return relations[BELOW] != 0 || relations[ABOVE] != 0 || relations[ALIGN_TOP] != 0 || relations[ALIGN_BOTTOM] != 0
                || relations[ALIGN_CENTER_VERTICALLY] != 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int myWidth = -1;
        int myHeight = -1;
        int width = 0;
        int height = 0;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final boolean isWrapContentWidth = widthMode != MeasureSpec.EXACTLY;
        final boolean isWrapContentHeight = heightMode != MeasureSpec.EXACTLY;

        if (mDirtyHierarchy) {
            mDirtyHierarchy = false;
            adaptLayoutParameters();
            final Stack<ViewConstraints> springMetrics = createViewMetrics();
            handleSprings(springMetrics, isWrapContentWidth, isWrapContentHeight);
        }

        // Record our dimensions if they are known;
        if (widthMode != MeasureSpec.UNSPECIFIED) {
            myWidth = widthSize;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED) {
            myHeight = heightSize;
        }

        if (widthMode == MeasureSpec.EXACTLY) {
            width = myWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = myHeight;
        }

        if (mDirtySize) {
            mDirtySize = false;
            invalidateMathCache();
            updateChildrenSize(widthMeasureSpec, heightMeasureSpec);
            updateLayoutSize(isWrapContentWidth, width, isWrapContentHeight, height);
            cacheLayoutPositions();
        }

        setMeasuredDimension(mRootMetrics.x2.getValue() + getPaddingRight(), mRootMetrics.y2.getValue() + getPaddingBottom());
    }

    private void invalidateMathCache() {
        for (int i = 0; i < mViewMetrics.length; i++) {
            final ViewConstraints viewMetrics = mViewMetrics[i];
            viewMetrics.x1.invalidate();
            viewMetrics.x2.invalidate();
            viewMetrics.y1.invalidate();
            viewMetrics.y2.invalidate();
        }
    }

    private void updateChildrenSize(final int widthMeasureSpec, final int heightMeasureSpec) {
        for (int i = 0; i < mViewMetrics.length; i++) {
            final ViewConstraints viewMetrics = mViewMetrics[i];
            if (viewMetrics.isSpring()) {
                if (!viewMetrics.hasHorizontalSibling()) {
                    viewMetrics.setWidth(LayoutMath.constant(0));
                }
                if (!viewMetrics.hasVerticalSibling()) {
                    viewMetrics.setHeight(LayoutMath.constant(0));
                }
            } else {
                final View v = viewMetrics.getView();
                final LayoutParams layoutParams = (LayoutParams) v.getLayoutParams();
                measureChildWithMargins(v, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final int childMeasuredWidth = v.getMeasuredWidth(), childMeasuredHeight = v.getMeasuredHeight();

                Value childWidth, childHeight;
                if (v.getVisibility() == View.GONE) {
                    childWidth = LayoutMath.ZERO;
                } else if (layoutParams.relativeWidth > 0) {
                    childWidth = mRootMetrics.x2.multiply(LayoutMath.constant(layoutParams.relativeWidth)).divide(LayoutMath.HUNDRED);
                } else {
                    childWidth = LayoutMath.wrap(childMeasuredWidth);
                }

                if (v.getVisibility() == View.GONE) {
                    childHeight = LayoutMath.ZERO;
                } else if (layoutParams.relativeHeight > 0) {
                    childHeight = mRootMetrics.y2.multiply(LayoutMath.constant(layoutParams.relativeHeight)).divide(LayoutMath.HUNDRED);
                } else {
                    childHeight = LayoutMath.wrap(childMeasuredHeight);
                }
                viewMetrics.setWidth(childWidth.add(LayoutMath.constant(layoutParams.leftMargin + layoutParams.rightMargin)));
                viewMetrics.setHeight(childHeight.add(LayoutMath.constant(layoutParams.topMargin + layoutParams.bottomMargin)));
            }
        }
    }

    private void handleSprings(final Stack<ViewConstraints> springMetrics, final boolean isWrapContentWidth,
            final boolean isWrapContentHeight) {
        if (!springMetrics.isEmpty()) {
            final Set<ViewConstraints> horizontalChains = new HashSet<ViewConstraints>();
            final Set<ViewConstraints> verticalChains = new HashSet<ViewConstraints>();
            while (!springMetrics.isEmpty()) {
                final ViewConstraints spring = springMetrics.pop();
                final ViewConstraints chainHeadX = getChainHorizontalHead(spring);
                final ViewConstraints chainHeadY = getChainVerticalHead(spring);
                if (chainHeadX != null) {
                    if (isWrapContentWidth && mMinWidth <= 0) {
                        throw new IllegalStateException("Horizontal springs not supported when layout width is wrap_content");
                    }
                    horizontalChains.add(chainHeadX);
                }
                if (chainHeadY != null) {
                    if (isWrapContentHeight && mMinHeight <= 0) {
                        throw new IllegalStateException(
                                "Vertical springs not supported when layout height is wrap_content and minHeight is not defined");
                    }
                    verticalChains.add(chainHeadY);
                }
            }

            for (ViewConstraints chainHead : horizontalChains) {
                int totalWeight = 0;
                Value parentWidth = mRootMetrics.x2;
                final ValueWrapper totalWeightWrapper = LayoutMath.wrap();
                final ValueWrapper parentWidthWrapper = LayoutMath.wrap();
                ViewConstraints chainElem = chainHead;
                while (chainElem != null) {
                    if (chainElem.isSpring()) {
                        final int weight = ((LayoutParams) chainElem.getView().getLayoutParams()).springWeight;
                        totalWeight += weight;
                        chainElem.setWidth(parentWidthWrapper.multiply(LayoutMath.constant(weight)).divide(totalWeightWrapper));
                    } else {
                        parentWidth = parentWidth.subtract(chainElem.getWidth());
                    }
                    chainElem = chainElem.mNextX;
                }
                totalWeightWrapper.setValueObject(LayoutMath.constant(totalWeight));
                parentWidthWrapper.setValueObject(parentWidth);
            }

            for (ViewConstraints chainHead : verticalChains) {
                int totalWeight = 0;
                Value parentHeight = mRootMetrics.y2;
                final ValueWrapper totalWeightWrapper = LayoutMath.wrap();
                final ValueWrapper parentHeightWrapper = LayoutMath.wrap();
                ViewConstraints chainElem = chainHead;
                while (chainElem != null) {
                    if (chainElem.isSpring()) {
                        final int weight = ((LayoutParams) chainElem.getView().getLayoutParams()).springWeight;
                        totalWeight += weight;
                        chainElem.setHeight(parentHeightWrapper.multiply(LayoutMath.constant(weight)).divide(totalWeightWrapper));
                    } else {
                        parentHeight = parentHeight.subtract(chainElem.getHeight());
                    }
                    chainElem = chainElem.mNextY;
                }
                totalWeightWrapper.setValueObject(LayoutMath.constant(totalWeight));
                parentHeightWrapper.setValueObject(parentHeight);
            }
        }
    }

    private void updateLayoutSize(final boolean isWrapContentWidth, int width, final boolean isWrapContentHeight, int height) {
        if (isWrapContentWidth) {
            int maxSize = mMinWidth > 0 ? mMinWidth : -1;
            for (int i = 0; i < mViewMetrics.length; i++) {
                final ViewConstraints viewMetrics = mViewMetrics[i];
                try {
                    maxSize = Math.max(maxSize, viewMetrics.x2.getValue());
                } catch (IllegalStateException e) {
                }
            }
            if (maxSize < 0) {
                throw new IllegalStateException(
                        "Parent layout_width == wrap_content is not supported if width of all children depends on parent width.");
            }
            mRootMetrics.x2.setValueObject(LayoutMath.constant(maxSize - getPaddingRight()));
        } else {
            mRootMetrics.x2.setValueObject(LayoutMath.constant(width - getPaddingRight()));
        }

        if (isWrapContentHeight) {
            int maxSize = mMinHeight > 0 ? mMinHeight : -1;
            for (int i = 0; i < mViewMetrics.length; i++) {
                final ViewConstraints viewMetrics = mViewMetrics[i];
                try {
                    maxSize = Math.max(maxSize, viewMetrics.y2.getValue());
                } catch (IllegalStateException e) {
                }
            }
            if (maxSize < 0) {
                throw new IllegalStateException(
                        "Parent layout_height == wrap_content is not supported if height of all children depends on parent height.");
            }
            mRootMetrics.y2.setValueObject(LayoutMath.constant(maxSize - getPaddingBottom()));
        } else {
            mRootMetrics.y2.setValueObject(LayoutMath.constant(height - getPaddingBottom()));
        }
    }

    private void cacheLayoutPositions() {
        for (int i = 0; i < mViewMetrics.length; i++) {
            final ViewConstraints viewMetrics = mViewMetrics[i];
            final View v = viewMetrics.getView();
            try {
                SpringLayout.LayoutParams st = (SpringLayout.LayoutParams) v.getLayoutParams();
                st.left = viewMetrics.x1.getValue() + st.leftMargin;
                st.right = viewMetrics.x2.getValue() - st.rightMargin;
                st.top = viewMetrics.y1.getValue() + st.topMargin;
                st.bottom = viewMetrics.y2.getValue() - st.bottomMargin;
                v.measure(MeasureSpec.makeMeasureSpec(st.right - st.left, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(st.bottom - st.top, MeasureSpec.EXACTLY));
            } catch (StackOverflowError e) {
                throw new IllegalStateException(
                        "Constraints of a view could not be resolved (circular dependency), please review your layout. Problematic view (please also check other dependant views): "
                                + v);
            }
        }
    }

    private ViewConstraints getChainVerticalHead(ViewConstraints spring) {
        if (spring.mNextY == null && spring.mPrevY == null) {
            return null;
        } else {
            while (spring.mPrevY != null) {
                spring = spring.mPrevY;
            }
            return spring;
        }
    }

    private ViewConstraints getChainHorizontalHead(ViewConstraints spring) {
        if (spring.mNextX == null && spring.mPrevX == null) {
            return null;
        } else {
            while (spring.mPrevX != null) {
                spring = spring.mPrevX;
            }
            return spring;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                SpringLayout.LayoutParams st = (SpringLayout.LayoutParams) child.getLayoutParams();
                child.layout(st.left, st.top, st.right, st.bottom);
            }
        }
    }

    @Override
    public void setMinimumHeight(int minHeight) {
        super.setMinimumHeight(minHeight);
        mMinHeight = minHeight;
    }

    @Override
    public void setMinimumWidth(int minWidth) {
        super.setMinimumWidth(minWidth);
        mMinWidth = minWidth;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new SpringLayout.LayoutParams(getContext(), attrs);
    }

    /**
     * Returns a set of layout parameters with a width of
     * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}, a height of
     * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT} and no spanning.
     */
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    // Override to allow type-checking of LayoutParams.
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof SpringLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    /**
     * Per-child layout information associated with SpringLayout.
     * 
     * @attr ref R.styleable#SpringLayout_Layout_layout_toLeftOf
     * @attr ref R.styleable#SpringLayout_Layout_layout_toRightOf
     * @attr ref R.styleable#SpringLayout_Layout_layout_above
     * @attr ref R.styleable#SpringLayout_Layout_layout_below
     * @attr ref R.styleable#SpringLayout_Layout_layout_alignBaseline
     * @attr ref R.styleable#SpringLayout_Layout_layout_alignLeft
     * @attr ref R.styleable#SpringLayout_Layout_layout_alignTop
     * @attr ref R.styleable#SpringLayout_Layout_layout_alignRight
     * @attr ref R.styleable#SpringLayout_Layout_layout_alignBottom
     * @attr ref R.styleable#SpringLayout_Layout_layout_alignCenterHorizontally
     * @attr ref R.styleable#SpringLayout_Layout_layout_alignCenterVertically
     * @attr ref R.styleable#SpringLayout_Layout_layout_alignParentLeft
     * @attr ref R.styleable#SpringLayout_Layout_layout_alignParentTop
     * @attr ref R.styleable#SpringLayout_Layout_layout_alignParentRight
     * @attr ref R.styleable#SpringLayout_Layout_layout_alignParentBottom
     * @attr ref R.styleable#SpringLayout_Layout_layout_centerInParent
     * @attr ref R.styleable#SpringLayout_Layout_layout_centerHorizontal
     * @attr ref R.styleable#SpringLayout_Layout_layout_centerVertical
     */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        @ViewDebug.ExportedProperty(resolveId = true, indexMapping = { @ViewDebug.IntToString(from = ABOVE, to = "above"),
                @ViewDebug.IntToString(from = BELOW, to = "below"), @ViewDebug.IntToString(from = LEFT_OF, to = "leftOf"),
                @ViewDebug.IntToString(from = RIGHT_OF, to = "rightOf"),
                @ViewDebug.IntToString(from = ALIGN_PARENT_LEFT, to = "alignParentLeft"),
                @ViewDebug.IntToString(from = ALIGN_PARENT_RIGHT, to = "alignParentRight"),
                @ViewDebug.IntToString(from = ALIGN_PARENT_TOP, to = "alignParentTop"),
                @ViewDebug.IntToString(from = ALIGN_PARENT_BOTTOM, to = "alignParentBottom"),
                @ViewDebug.IntToString(from = ALIGN_LEFT, to = "alignLeft"), @ViewDebug.IntToString(from = ALIGN_RIGHT, to = "alignRight"),
                @ViewDebug.IntToString(from = ALIGN_TOP, to = "alignTop"), @ViewDebug.IntToString(from = ALIGN_BOTTOM, to = "alignBottom"),
                @ViewDebug.IntToString(from = ALIGN_CENTER, to = "alignCenter"),
                @ViewDebug.IntToString(from = ALIGN_CENTER_HORIZONTALLY, to = "alignCenterHorizontally"),
                @ViewDebug.IntToString(from = ALIGN_CENTER_VERTICALLY, to = "alignCenterVertically"),
                @ViewDebug.IntToString(from = CENTER_HORIZONTAL, to = "centerHorizontal"),
                @ViewDebug.IntToString(from = CENTER_IN_PARENT, to = "centerInParent"),
                @ViewDebug.IntToString(from = CENTER_VERTICAL, to = "centerVertical"), }, mapping = {
                @ViewDebug.IntToString(from = TRUE, to = "true"), @ViewDebug.IntToString(from = 0, to = "false/NO_ID"),
                @ViewDebug.IntToString(from = PARENT, to = "parent") })
        int[] relations = new int[VERB_COUNT];
        int left, top, right, bottom;
        int relativeHeight, relativeWidth;
        int springWeight = 1;
        boolean dirty = true;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.SpringLayout_Layout);

            final int[] relations = this.relations;

            final int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                case R.styleable.SpringLayout_Layout_layout_toLeftOf:
                    relations[LEFT_OF] = a.getResourceId(attr, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_toRightOf:
                    relations[RIGHT_OF] = a.getResourceId(attr, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_above:
                    relations[ABOVE] = a.getResourceId(attr, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_below:
                    relations[BELOW] = a.getResourceId(attr, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_alignLeft:
                    relations[ALIGN_LEFT] = a.getResourceId(attr, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_alignTop:
                    relations[ALIGN_TOP] = a.getResourceId(attr, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_alignRight:
                    relations[ALIGN_RIGHT] = a.getResourceId(attr, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_alignBottom:
                    relations[ALIGN_BOTTOM] = a.getResourceId(attr, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_alignCenter:
                    relations[ALIGN_CENTER] = a.getResourceId(attr, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_alignCenterHorizontally:
                    relations[ALIGN_CENTER_HORIZONTALLY] = a.getResourceId(attr, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_alignCenterVertically:
                    relations[ALIGN_CENTER_VERTICALLY] = a.getResourceId(attr, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_alignParentLeft:
                    relations[ALIGN_PARENT_LEFT] = a.getBoolean(attr, false) ? TRUE : 0;
                    break;
                case R.styleable.SpringLayout_Layout_layout_alignParentTop:
                    relations[ALIGN_PARENT_TOP] = a.getBoolean(attr, false) ? TRUE : 0;
                    break;
                case R.styleable.SpringLayout_Layout_layout_alignParentRight:
                    relations[ALIGN_PARENT_RIGHT] = a.getBoolean(attr, false) ? TRUE : 0;
                    break;
                case R.styleable.SpringLayout_Layout_layout_alignParentBottom:
                    relations[ALIGN_PARENT_BOTTOM] = a.getBoolean(attr, false) ? TRUE : 0;
                    break;
                case R.styleable.SpringLayout_Layout_layout_centerInParent:
                    relations[CENTER_IN_PARENT] = a.getBoolean(attr, false) ? TRUE : 0;
                    break;
                case R.styleable.SpringLayout_Layout_layout_centerHorizontal:
                    relations[CENTER_HORIZONTAL] = a.getBoolean(attr, false) ? TRUE : 0;
                    break;
                case R.styleable.SpringLayout_Layout_layout_centerVertical:
                    relations[CENTER_VERTICAL] = a.getBoolean(attr, false) ? TRUE : 0;
                    break;
                case R.styleable.SpringLayout_Layout_layout_relativeWidth:
                    relativeWidth = (int) a.getFraction(attr, RELATIVE_SIZE_DENOMINATOR, 1, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_relativeHeight:
                    relativeHeight = (int) a.getFraction(attr, RELATIVE_SIZE_DENOMINATOR, 1, 0);
                    break;
                case R.styleable.SpringLayout_Layout_layout_springWeight:
                    springWeight = a.getInteger(attr, 1);
                    break;
                }
            }

            a.recycle();
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public void addRelation(int relation, int anchor) {
            relations[relation] = anchor;
            dirty = true;
        }

        /**
         * Retrieves a complete list of all supported relations, where the index
         * is the relation verb, and the element value is the value specified,
         * or "false" if it was never set.
         * 
         * @return the supported relations
         * @see #addRelation(int, int)
         */
        public int[] getRelations() {
            return relations;
        }

        public int getRelativeHeight() {
            return relativeHeight;
        }

        public void setRelativeHeight(int relativeHeight) {
            dirty = true;
            this.relativeHeight = relativeHeight;
        }

        public int getRelativeWidth() {
            return relativeWidth;
        }

        public void setRelativeWidth(int relativeWidth) {
            dirty = true;
            this.relativeWidth = relativeWidth;
        }

        public int getSpringWeight() {
            return springWeight;
        }

        public void setSpringWeight(int springWeight) {
            dirty = true;
            this.springWeight = springWeight;
        }

        public void setWidth(int width) {
            dirty = true;
            this.width = width;
        }

        public void setHeight(int height) {
            dirty = true;
            this.height = height;
        }
    }
}
