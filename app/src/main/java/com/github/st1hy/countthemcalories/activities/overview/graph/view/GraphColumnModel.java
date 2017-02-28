package com.github.st1hy.countthemcalories.activities.overview.graph.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.github.st1hy.countthemcalories.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import rx.Observable;
import rx.subjects.PublishSubject;

public class GraphColumnModel {

    private static final float[] EMPTY_POINTS = new float[0];
    private static int[] numberTable = new int[]{1, 2, 5, 10};

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FLAG_NONE, FLAG_COLUMN, FLAG_LINE, FLAG_POINTS, FLAG_ALL})
    public @interface VisibilityFlags {
    }

    public static final int FLAG_NONE = 0;
    public static final int FLAG_COLUMN = 1;

    public static final int FLAG_LINE = 2;
    public static final int FLAG_POINTS = 4;
    public static final int FLAG_ALL = 7;
    final Paint columnColor;
    final RectF columnSize;

    float[] legendVector = EMPTY_POINTS;
    float[] lineVector = EMPTY_POINTS;
    float[] rowLegendVector = EMPTY_POINTS;
    final Paint linePaint;
    final Paint columnLegendLinePaint;
    final Paint rowLegendLinePaint;
    final Paint pointColor;
    final RectF pointSizeBounds;
    private boolean columnSizeDirty;
    private boolean lineVectorDirty;

    private boolean pointSizeDirty;
    private boolean columnLegendDirty;
    private boolean rowsLegendDirty;
    private float[] linePoints = EMPTY_POINTS;

    private float value;
    private float maxValue;
    private float pointProgress;
    private final float pointSize;
    private final float legendLineWidthHalf;
    private final float legendSeparatorSize;
    private final Orientation orientation;
    private int flags;

    private final float minLineSpaces;

    private final PublishSubject<Void> invalidate = PublishSubject.create();

    GraphColumnModel(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GraphColumn, defStyleAttr, R.style.GraphColumn_Default);
        int gc_orientation = a.getInt(R.styleable.GraphColumn_gc_orientation, Orientation.RIGHT_TO_LEFT.gc_orientation);
        int color = a.getColor(R.styleable.GraphColumn_gc_color, Color.GREEN);
        float gc_progress = a.getFloat(R.styleable.GraphColumn_gc_progress, 0.5f);
        int gc_mode = a.getInteger(R.styleable.GraphColumn_gc_mode, FLAG_ALL);
        float gc_lineWidth = a.getDimension(R.styleable.GraphColumn_gc_line_width, 1f);
        int gc_lineColor = a.getColor(R.styleable.GraphColumn_gc_line_color, Color.BLACK);
        int gc_legend_line_color = a.getColor(R.styleable.GraphColumn_gc_legend_line_color, Color.BLACK);
        float gc_legend_line_width = a.getDimensionPixelSize(R.styleable.GraphColumn_gc_legend_line_width, 1);
        int gc_row_legend_line_color = a.getColor(R.styleable.GraphColumn_gc_legend_line_overlay_color, Color.BLACK);
        int gc_value_legend_text_color = a.getColor(R.styleable.GraphColumn_gc_legend_text_value_color, Color.BLACK);
        int gc_point_legend_text_color = a.getColor(R.styleable.GraphColumn_gc_legend_text_point_color, Color.BLACK);
        legendSeparatorSize = a.getDimension(R.styleable.GraphColumn_gc_legend_separator_size, 10f);
        int gc_point_color = a.getColor(R.styleable.GraphColumn_gc_line_color, Color.BLACK);
        pointSize = a.getDimension(R.styleable.GraphColumn_gc_point_size, 10f);
        minLineSpaces = a.getDimension(R.styleable.GraphColumn_gc_legend_min_legend_line_space, 100);
        a.recycle();
        columnColor = new Paint();
        columnColor.setColor(color);
        value = gc_progress;
        columnSize = new RectF();
        orientation = Orientation.valueOf(gc_orientation);
        flags = gc_mode;
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(gc_lineWidth);
        linePaint.setColor(gc_lineColor);
        columnLegendLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        columnLegendLinePaint.setStyle(Paint.Style.STROKE);
        columnLegendLinePaint.setStrokeWidth(gc_legend_line_width);
        columnLegendLinePaint.setColor(gc_legend_line_color);
        legendVector = new float[8];
        legendLineWidthHalf = gc_legend_line_width / 2f;
        pointColor = new Paint();
        pointColor.setColor(gc_point_color);
        pointSizeBounds = new RectF();
        rowLegendLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rowLegendLinePaint.setStyle(Paint.Style.STROKE);
        rowLegendLinePaint.setStrokeWidth(gc_legend_line_width);
        rowLegendLinePaint.setColor(gc_row_legend_line_color);
    }

    public void setFlags(@GraphColumnModel.VisibilityFlags int flags) {
        if (this.flags != flags) {
            this.flags = flags;
            invalidate();
        }
    }

    @GraphColumnModel.VisibilityFlags
    public int getFlags() {
        return flags;
    }

    void setDirty() {
        columnSizeDirty = true;
        lineVectorDirty = true;
        columnLegendDirty = true;
        rowsLegendDirty = true;
        pointSizeDirty = true;
    }

    public void setValue(float value, float maxValue) {
        if (maxValue < 0.001f) maxValue = 0.001f;
        if (value < 0f) value = 0f;
        else if (value > maxValue) value = maxValue;
        this.value = value;
        this.maxValue = maxValue;
        columnSizeDirty = true;
        rowsLegendDirty = true;
        if (showColumn()) invalidate();
    }

    public void setColumnColor(@ColorInt int color) {
        columnColor.setColor(color);
        if (showColumn()) invalidate();
    }

    public void setLineColor(@ColorInt int color) {
        linePaint.setColor(color);
        if (showLine()) invalidate();
    }

    public void setPoint(float point) {
        this.pointProgress = point;
        pointSizeDirty = true;
        if (showPoint()) invalidate();
    }

    /**
     * Format [x1, y1, x2, y2, ...]
     * Values need to be within range 0f, 1f. They represent percentage of width and height.
     */
    public void setLinePoints(@NonNull float[] points) {
        if (points.length % 4 != 0) {
            throw new IllegalArgumentException("Illegal points size, must be multiple of 4");
        }
        linePoints = points;
        if (lineVector == null || lineVector.length != linePoints.length) {
            lineVector = new float[linePoints.length];
        }
        lineVectorDirty = true;
        if (showLine()) invalidate();
    }

    private void invalidate() {
        invalidate.onNext(null);
    }

    @NonNull
    @CheckResult
    Observable<Void> invalidated() {
        return invalidate;
    }


    void setColumnSizeBounds(float width, float height) {
        if (!columnSizeDirty) return;
        float progress = value / maxValue;
        switch (orientation) {
            case RIGHT_TO_LEFT:
                columnSize.set((1f - progress) * width, 0, width, height);
                break;
            case LEFT_TO_RIGHT:
                columnSize.set(0f, 0f, progress * width, height);
                break;
            case TOP_TO_BOTTOM:
                columnSize.set(0f, 0f, width, progress * height);
                break;
            case BOTTOM_TO_TOP:
                columnSize.set(0f, (1f - progress) * height, width, height);
                break;
        }
        columnSizeDirty = false;
    }

    public void setPointSize(float width, float height) {
        if (!pointSizeDirty) return;
        float pointSizeHalf = pointSize / 2;
        float left, right, top, bottom;
        switch (orientation) {
            case RIGHT_TO_LEFT:
            case LEFT_TO_RIGHT:
                float curWidth = pointProgress * width;
                if (orientation == Orientation.RIGHT_TO_LEFT)
                    curWidth = width - curWidth;
                float halfHeight = height / 2;
                left = curWidth - pointSizeHalf;
                right = curWidth + pointSizeHalf;
                top = halfHeight + pointSizeHalf;
                bottom = halfHeight - pointSizeHalf;
                break;
            case TOP_TO_BOTTOM:
            case BOTTOM_TO_TOP:
                float curHeight = pointProgress * height;
                if (orientation == Orientation.BOTTOM_TO_TOP)
                    curHeight = height - curHeight;
                float halfWidth = width / 2;
                left = halfWidth - pointSizeHalf;
                right = halfWidth + pointSizeHalf;
                top = curHeight + pointSizeHalf;
                bottom = curHeight - pointSizeHalf;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        pointSizeBounds.set(left, top, right, bottom);
        pointSizeDirty = false;
    }

    void setLineVector(float width, float height) {
        if (!lineVectorDirty) return;
        for (int i = 0; i < linePoints.length; i += 2) {
            float x = linePoints[i];
            float y = linePoints[i + 1];
            float dx, dy;
            switch (orientation) {
                case RIGHT_TO_LEFT:
                    dx = (1f - y) * width;
                    dy = x * height;
                    break;
                case LEFT_TO_RIGHT:
                    dx = y * width;
                    dy = x * height;
                    break;
                case TOP_TO_BOTTOM:
                    dx = x * width;
                    dy = y * height;
                    break;
                case BOTTOM_TO_TOP:
                    dx = x * width;
                    dy = (1f - y) * height;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            lineVector[i] = dx;
            lineVector[i + 1] = dy;
        }
        lineVectorDirty = false;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
        //readability
    void setLegendBounds(float width, float height) {
        setColumnLegendBounds(width, height);
        setRowColumnLegendBounds(width, height);
    }

    private void setColumnLegendBounds(float width, float height) {
        if (!columnLegendDirty) return;
        float min = legendLineWidthHalf;
        float minWidth = min;
        float maxWidth = width - min;
        float minHeight = min;
        float maxHeight = height - min;
        switch (orientation) {
            case RIGHT_TO_LEFT:
                //underline
                legendVector[0] = maxWidth;
                legendVector[1] = 0f;
                legendVector[2] = maxWidth;
                legendVector[3] = height;
                //separator
                legendVector[4] = width - legendSeparatorSize;
                legendVector[5] = minHeight;
                legendVector[6] = width;
                legendVector[7] = minHeight;
                break;
            case LEFT_TO_RIGHT:
                //underline
                legendVector[0] = minWidth;
                legendVector[1] = 0f;
                legendVector[2] = minWidth;
                legendVector[3] = height;
                //separator
                legendVector[4] = 0;
                legendVector[5] = minHeight;
                legendVector[6] = legendSeparatorSize;
                legendVector[7] = minHeight;
                break;
            case TOP_TO_BOTTOM:
                //underline
                legendVector[0] = 0f;
                legendVector[1] = minHeight;
                legendVector[2] = width;
                legendVector[3] = minHeight;
                //separator
                legendVector[4] = minWidth;
                legendVector[5] = 0;
                legendVector[6] = minWidth;
                legendVector[7] = legendSeparatorSize;
                break;
            case BOTTOM_TO_TOP:
                //underline
                legendVector[0] = 0;
                legendVector[1] = maxHeight;
                legendVector[2] = width;
                legendVector[3] = maxHeight;
                //separator
                legendVector[4] = minWidth;
                legendVector[5] = height - legendSeparatorSize;
                legendVector[6] = minWidth;
                legendVector[7] = height;
                break;
        }
        columnLegendDirty = false;
    }

    private void setRowColumnLegendBounds(float width, float height) {
        if (!rowsLegendDirty) return;
        float[] autoScale = setRowsLegendVector(width, height);
        for (int i = 0; i < autoScale.length; i++) {
            int x = i * 4;
            int y = x + 1;
            float sizeNormal = autoScale[i] / maxValue;
            switch (orientation) {
                case RIGHT_TO_LEFT:
                case LEFT_TO_RIGHT:
                    float curWidth = width * sizeNormal;
                    if (orientation == Orientation.RIGHT_TO_LEFT) {
                        curWidth = width - curWidth;
                    }
                    rowLegendVector[x] = curWidth;
                    rowLegendVector[y] = 0f;
                    rowLegendVector[x+2] = curWidth;
                    rowLegendVector[y+2] = height;
                    break;
                case TOP_TO_BOTTOM:
                case BOTTOM_TO_TOP:
                    float curHeight = height * sizeNormal;
                    if (orientation == Orientation.BOTTOM_TO_TOP) {
                        curHeight = height - curHeight;
                    }
                    rowLegendVector[x] = 0f;
                    rowLegendVector[y] = curHeight;
                    rowLegendVector[x+2] = width;
                    rowLegendVector[y+2] = curHeight;
                    break;
            }
        }
        rowsLegendDirty = false;
    }

    boolean showColumn() {
        return (flags & FLAG_COLUMN) > 0;
    }

    boolean showLine() {
        return (flags & FLAG_LINE) > 0;
    }

    boolean showPoint() {
        return (flags & FLAG_POINTS) > 0;
    }

    private float[] setRowsLegendVector(float width, float height) {
        float totalViewSize;
        switch (orientation) {
            case RIGHT_TO_LEFT:
            case LEFT_TO_RIGHT:
                totalViewSize = width;
                break;
            case TOP_TO_BOTTOM:
            case BOTTOM_TO_TOP:
                totalViewSize = height;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        float[] autoScale = sensibleScale(totalViewSize);
        int newLineSize = autoScale.length * 4;
        if (lineVector.length != newLineSize) {
            rowLegendVector = new float[newLineSize];
        }
        return autoScale;
    }


    public float[] sensibleScale(float totalViewSize) {
        float valueResolution = totalViewSize / maxValue;
        float idealDelta = (minLineSpaces / valueResolution);
        float delta = computeDelta(idealDelta);
        int size = (int) Math.floor(maxValue / delta);
        float[] values = new float[size];
        for (int i = 0; i < size; i++) {
            values[i] = (i + 1) * delta;
        }
        return values;
    }

    public static float computeDelta(float value) {
        int power = (int) (Math.floor(Math.log10(value)));
        double vector = Math.pow(10, power);
        int firstNumber = (int) Math.round(value / vector);
        firstNumber = closestLarger(firstNumber);
        return (float) (firstNumber * vector);
    }

    private static int closestLarger(int number) {
        int closestNumber = 10;
        for (int i : numberTable) {
            if (i > number) {
                return i;
            }
        }
        return closestNumber;
    }
}