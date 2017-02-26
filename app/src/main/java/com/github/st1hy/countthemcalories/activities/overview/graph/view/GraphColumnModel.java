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

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FLAG_NONE, FLAG_COLUMN, FLAG_LINE, FLAG_ALL})
    public @interface VisibilityFlags {
    }

    public static final int FLAG_NONE = 0;
    public static final int FLAG_COLUMN = 1;
    public static final int FLAG_LINE = 2;
    public static final int FLAG_ALL = 3;

    final Paint columnColor;
    final RectF columnSize;
    float[] legendVector = EMPTY_POINTS;
    float[] lineVector = EMPTY_POINTS;
    final Paint linePaint;
    final Paint legendLinePaint;

    private boolean columnSizeDirty;
    private boolean lineVectorDirty;

    private boolean legendDirty;
    private float[] linePoints = EMPTY_POINTS;
    private float progress;
    private final float legendLineWidthHalf;
    private final float legendSeparatorSize;

    private final Orientation orientation;
    private int flags;
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
        legendSeparatorSize = a.getDimension(R.styleable.GraphColumn_gc_legend_separator_size, 10f);
        a.recycle();
        columnColor = new Paint();
        columnColor.setColor(color);
        progress = gc_progress;
        columnSize = new RectF();
        orientation = Orientation.valueOf(gc_orientation);
        flags = gc_mode;
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(gc_lineWidth);
        linePaint.setColor(gc_lineColor);
        legendLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendLinePaint.setStyle(Paint.Style.STROKE);
        legendLinePaint.setStrokeWidth(gc_legend_line_width);
        legendLinePaint.setColor(gc_legend_line_color);
        legendVector = new float[8];
        legendLineWidthHalf = gc_legend_line_width / 2f;
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
        legendDirty = true;
    }

    public void setProgress(float progress) {
        if (progress < 0f) progress = 0f;
        else if (progress > 1f) progress = 1f;
        this.progress = progress;
        columnSizeDirty = true;
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

    @SuppressWarnings("UnnecessaryLocalVariable") //readability
    void setLegendBounds(float width, float height) {
        if (!legendDirty) return;
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
        legendDirty = false;
    }

    boolean showColumn() {
        return (flags & FLAG_COLUMN) > 0;
    }

    boolean showLine() {
        return (flags & FLAG_LINE) > 0;
    }
}
