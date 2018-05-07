package com.wytings.progress.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.wytings.progress.R;
import com.wytings.progress.widget.helper.GraphTextHelper;
import com.wytings.progress.widget.helper.TextAxisType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by rex.wei on 2018/04/13 19:56.
 *
 * @author rex.wei@yff.com
 */
public class TimeProgressView extends View {

    private static final int POSITION_LEFT = 1;
    private static final int POSITION_MIDDLE = 2;
    private static final int POSITION_RIGHT = 3;

    final TextPaint paint;
    final float singleWidth;
    final RectF canvasRectF = new RectF();
    final RectF tempRectF = new RectF();
    final List<TimeData> dataList = new ArrayList<>();
    final GraphTextHelper textHelper;
    long currentTimestamp = System.currentTimeMillis();
    int descTextSize, timeTextSize, dotTopPadding, dotBottomPadding, dotHorizontalPadding, dotRadius, lineHeight;
    int descBrightColor, descDarkColor, timeBrightColor, timeDarkColor, lineBrightColor, lineDarkColor, dotBrightColor, dotDarkColor;

    public TimeProgressView(Context context) {
        this(context, null);
    }

    public TimeProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        descTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, context.getResources().getDisplayMetrics());
        timeTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, context.getResources().getDisplayMetrics());
        dotTopPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
        dotBottomPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
        dotHorizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
        dotRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
        lineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());

        descBrightColor = ContextCompat.getColor(context, R.color.white_color_100);
        descDarkColor = ContextCompat.getColor(context, R.color.white_color_30);
        timeBrightColor = ContextCompat.getColor(context, R.color.white_color_50);
        timeDarkColor = ContextCompat.getColor(context, R.color.white_color_30);
        lineBrightColor = ContextCompat.getColor(context, R.color.time_progress_bright);
        lineDarkColor = ContextCompat.getColor(context, R.color.white_color_10);
        dotBrightColor = ContextCompat.getColor(context, R.color.blue);
        dotDarkColor = ContextCompat.getColor(context, R.color.time_progress_dot_dark);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeProgressView);
        descTextSize = typedArray.getDimensionPixelSize(R.styleable.TimeProgressView_time_progress_desc_text_size, descTextSize);
        timeTextSize = typedArray.getDimensionPixelSize(R.styleable.TimeProgressView_time_progress_time_text_size, timeTextSize);
        dotTopPadding = typedArray.getDimensionPixelSize(R.styleable.TimeProgressView_time_progress_dot_top_padding, dotTopPadding);
        dotBottomPadding = typedArray.getDimensionPixelSize(R.styleable.TimeProgressView_time_progress_dot_bottom_padding, dotBottomPadding);
        dotHorizontalPadding = typedArray.getDimensionPixelSize(R.styleable.TimeProgressView_time_progress_dot_horizontal_padding, dotHorizontalPadding);
        dotRadius = typedArray.getDimensionPixelSize(R.styleable.TimeProgressView_time_progress_dot_radius, dotRadius);
        lineHeight = typedArray.getDimensionPixelSize(R.styleable.TimeProgressView_time_progress_line_height, lineHeight);

        descBrightColor = typedArray.getColor(R.styleable.TimeProgressView_time_progress_desc_bright_color, descBrightColor);
        descDarkColor = typedArray.getColor(R.styleable.TimeProgressView_time_progress_desc_dark_color, descDarkColor);
        timeBrightColor = typedArray.getColor(R.styleable.TimeProgressView_time_progress_time_bright_color, timeBrightColor);
        timeDarkColor = typedArray.getColor(R.styleable.TimeProgressView_time_progress_time_dark_color, timeDarkColor);
        lineBrightColor = typedArray.getColor(R.styleable.TimeProgressView_time_progress_line_bright_color, lineBrightColor);
        lineDarkColor = typedArray.getColor(R.styleable.TimeProgressView_time_progress_line_dark_color, lineDarkColor);
        dotBrightColor = typedArray.getColor(R.styleable.TimeProgressView_time_progress_dot_bright_color, dotBrightColor);
        dotDarkColor = typedArray.getColor(R.styleable.TimeProgressView_time_progress_dot_dark_color, dotDarkColor);
        typedArray.recycle();

        if (isInEditMode()) {
            dataList.addAll(getTestData());
        }

        textHelper = new GraphTextHelper(context);
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(timeTextSize);
        singleWidth = paint.measureText("yyyy-MM-dd");
    }

    public void setDataList(String currentTime, List<TimeData> list) {
        this.dataList.clear();
        this.dataList.addAll(list);
        this.currentTimestamp = formatDate(currentTime);
    }

    public void setCurrentTime(String currentTime) {
        this.currentTimestamp = formatDate(currentTime);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int specMode = MeasureSpec.getMode(heightMeasureSpec);
        final int height;
        if (specMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = getPaddingTop() + descTextSize + dotTopPadding + dotRadius * 2 + dotBottomPadding + timeTextSize + getPaddingBottom();
        }
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        final int systemDefaultMiniHeight = super.getSuggestedMinimumHeight();
        final int userDefaultMiniHeight = descTextSize + dotTopPadding + dotRadius * 2 + dotBottomPadding + timeTextSize;
        return Math.max(systemDefaultMiniHeight, userDefaultMiniHeight);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        final int systemDefaultMiniWidth = super.getSuggestedMinimumWidth();
        final int userDefaultMiniWidth = (int) (singleWidth * dataList.size() + 0.5f);
        return Math.max(systemDefaultMiniWidth, userDefaultMiniWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final int leftPadding = getPaddingLeft();
        final int rightPadding = getPaddingRight();
        final int topPadding = getPaddingTop();
        final int bottomPadding = getPaddingBottom();
        canvasRectF.set(leftPadding, topPadding, w - rightPadding, h - bottomPadding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataList.isEmpty()) {
            return;
        }
        drawTimeTextBlock(canvas);
        drawTimeLine(canvas);
    }

    private void drawTimeLine(Canvas canvas) {
        if (dataList.size() < 2) {
            return;
        }

        final int centerY = lineCenterY();
        final int halfLineHeight = lineHeight / 2;
        for (int i = 0; i < dataList.size() - 1; i++) {
            TimeData data = dataList.get(i);
            TimeData next = dataList.get(i + 1);

            tempRectF.left = data.dotCenterX + dotRadius + dotHorizontalPadding;
            tempRectF.top = centerY - halfLineHeight;
            tempRectF.right = next.dotCenterX - dotRadius - dotHorizontalPadding;
            tempRectF.bottom = centerY + halfLineHeight;


            if (currentTimestamp < data.timestamp) {
                paint.setColor(lineDarkColor);
                canvas.drawRect(tempRectF, paint);
            } else if (data.timestamp <= currentTimestamp && currentTimestamp < next.timestamp) {
                paint.setColor(lineDarkColor);
                canvas.drawRect(tempRectF, paint);

                float percent = 1.0f * (currentTimestamp - data.timestamp) / (next.timestamp - data.timestamp);
                tempRectF.right = (int) (tempRectF.left + tempRectF.width() * percent);

                paint.setColor(lineBrightColor);
                canvas.drawRect(tempRectF, paint);
            } else {
                paint.setColor(lineBrightColor);
                canvas.drawRect(tempRectF, paint);
            }

        }

    }

    private void drawTimeTextBlock(Canvas canvas) {
        float width = canvasRectF.width();
        int size = dataList.size();

        if (size == 1) {
            drawSingleTimeBlock(dataList.get(0), canvasRectF.centerX(), POSITION_MIDDLE, canvas);
            return;
        }

        float interval = (width - singleWidth * size) / (size - 1);
        for (int i = 0; i < size; i++) {
            final TimeData data = dataList.get(i);
            final int positionType;
            final float anchorX;
            if (i == 0) {
                positionType = POSITION_LEFT;
                anchorX = canvasRectF.left;
            } else if (i == size - 1) {
                positionType = POSITION_RIGHT;
                anchorX = canvasRectF.right;
            } else {
                positionType = POSITION_MIDDLE;
                anchorX = canvasRectF.left + (singleWidth + interval) * i + singleWidth / 2;
            }
            drawSingleTimeBlock(data, anchorX, positionType, canvas);
        }
    }

    private void drawSingleTimeBlock(TimeData data, float anchorX, int positionType, Canvas canvas) {
        final boolean isBright = currentTimestamp >= data.timestamp;
        final int timeColor = isBright ? timeBrightColor : timeDarkColor;
        final int descColor = isBright ? descBrightColor : descDarkColor;
        float dotCenterX = anchorX;
        switch (positionType) {
            case POSITION_LEFT:
                dotCenterX += dotRadius;
                textHelper.drawText(canvas, data.desc, descTextSize, descColor, anchorX, canvasRectF.top, TextAxisType.LEFT_TOP);
                textHelper.drawText(canvas, data.timeString, timeTextSize, timeColor, anchorX, canvasRectF.bottom, TextAxisType.LEFT_BOTTOM);
                break;

            case POSITION_RIGHT:
                dotCenterX -= dotRadius;
                textHelper.drawText(canvas, data.desc, descTextSize, descColor, anchorX, canvasRectF.top, TextAxisType.RIGHT_TOP);
                textHelper.drawText(canvas, data.timeString, timeTextSize, timeColor, anchorX, canvasRectF.bottom, TextAxisType.RIGHT_BOTTOM);
                break;

            case POSITION_MIDDLE:
                textHelper.drawText(canvas, data.desc, descTextSize, descColor, anchorX, canvasRectF.top, TextAxisType.CENTER_TOP);
                textHelper.drawText(canvas, data.timeString, timeTextSize, timeColor, anchorX, canvasRectF.bottom, TextAxisType.CENTER_BOTTOM);
                break;
        }

        paint.setColor(isBright ? dotBrightColor : dotDarkColor);
        canvas.drawCircle(dotCenterX, lineCenterY(), dotRadius, paint);
        data.dotCenterX = dotCenterX;
    }

    private int lineCenterY() {
        return (int) (canvasRectF.top + descTextSize + dotTopPadding + dotRadius);
    }

    Collection<? extends TimeData> getTestData() {
        List<TimeData> list = new LinkedList<>();
        list.add(new TimeData("2018-01-27", "WakeWake"));
        list.add(new TimeData("2018-02-27", "BedBed"));
        list.add(new TimeData("2018-03-27", "WashWash"));
        list.add(new TimeData("2018-04-27", "OutOut"));
        return list;
    }


    private static long formatDate(String times) {
        long dateTime = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = format.parse(times);
            dateTime = date.getTime();
        } catch (ParseException e) {
            Log.e("TimeProgressView", "fail to format date, error = " + e);
        }
        return dateTime;
    }

    public static class TimeData {
        final long timestamp;
        final String timeString;
        final String desc;
        float dotCenterX;

        public TimeData(String timeString, String desc) {
            this.timeString = timeString;
            this.desc = desc;
            this.timestamp = formatDate(timeString);
        }
    }

}