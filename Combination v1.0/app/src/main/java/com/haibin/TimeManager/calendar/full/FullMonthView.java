package com.haibin.TimeManager.calendar.full;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.MonthView;

import java.util.List;

public class FullMonthView extends MonthView {

    private Paint mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mSchemeBasicPaint = new Paint();

    public FullMonthView(Context context) {
        super(context);

        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(dipToPx(context, 0.5f));
        mRectPaint.setColor(0x88efefef);

        mSchemeBasicPaint.setAntiAlias(true);
        mSchemeBasicPaint.setStyle(Paint.Style.FILL);
        mSchemeBasicPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeBasicPaint.setFakeBoldText(true);

        //兼容硬件加速无效的代码
        setLayerType(View.LAYER_TYPE_SOFTWARE, mSchemeBasicPaint);
        //4.0以上硬件加速会导致无效
        mSelectedPaint.setMaskFilter(new BlurMaskFilter(50, BlurMaskFilter.Blur.SOLID));
    }
    /**
     * 绘制选中的日子
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param y         日历Card y起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme) {
        canvas.drawRect(x, y , x + mItemWidth, y + mItemHeight, mBorderPaint);
        return true;
    }
    /**
     * 绘制标记的事件日子
     *
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     * @param y        日历Card y起点坐标
     */
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y) {
        mSchemeBasicPaint.setColor(calendar.getSchemeColor());
        List<Calendar.Scheme> schemes = calendar.getSchemes();
        if (schemes == null || schemes.size() == 0) {
            return;
        }
        int space = dipToPx(getContext(), 2);
        int indexY = y + mItemHeight - 2 * space;
        int sw = dipToPx(getContext(), mItemWidth / 10);
        int sh = dipToPx(getContext(), 4);
        for (Calendar.Scheme scheme : schemes) {

            mSchemePaint.setColor(scheme.getShcemeColor());

            canvas.drawRect(x + mItemWidth - sw -  2 * space,

                    indexY - sh, x + mItemWidth - 2 * space, indexY, mSchemePaint);
            indexY = indexY - space -sh;
        }
    }

    /**
     * 绘制文本
     *
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param y          日历Card y起点坐标
     * @param hasScheme  是否是标记的日期
     * @param isSelected 是否选中
     */
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        canvas.drawRect(x, y, x + mItemWidth, y + mItemHeight, mRectPaint);
        int cx = x + mItemWidth / 2;
        int lx = x + mItemWidth / 10;
        int top = y - mItemHeight / 3;

        boolean isInRange = isInRange(calendar);

        if (isSelected) {

            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top,
                    mSelectTextPaint);
            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + top + mItemHeight / 8, mSelectedLunarTextPaint);

            if (hasScheme){
                String scheme = calendar.getScheme();
                String strs[] = scheme.split("\n");

                float currentHeight = mTextBaseLine + top + mItemHeight / 8 + mItemHeight /20;
                int offset = mItemHeight / 50 ;
                int len = strs.length;
                for(int i = 0 ; i < len; i++){
                    canvas.drawRect(x + mItemWidth/20, currentHeight ,
                            x + 19 * mItemWidth /20 , currentHeight + mItemHeight / 9 , mTodoBackgroundPaint);
                    canvas.drawText(strs[i].length() <= 4 ? strs[i] : strs[i].substring(0, 4), lx, currentHeight + offset*47/10, mTodoTextPaint);
                    currentHeight = offset*2 + currentHeight + mItemHeight / 10;
                }
            }


        } else if (hasScheme) {

            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top,
                    calendar.isCurrentMonth() && isInRange ? mSchemeTextPaint : mOtherMonthTextPaint);

            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + top + mItemHeight / 8, mCurMonthLunarTextPaint);


            String scheme = calendar.getScheme();
            String strs[] = scheme.split("\n");

            float currentHeight = mTextBaseLine + top + mItemHeight / 8 + mItemHeight /20;
            int offset = mItemHeight / 50 ;
            int len = strs.length;
            for(int i = 0 ; i < (3 < len ? 3 : len); i++){
                canvas.drawRect(x + mItemWidth/20, currentHeight ,
                        x + 19 * mItemWidth /20 , currentHeight + mItemHeight / 9 , mTodoBackgroundPaint);

                canvas.drawText(strs[i].length() <= 4 ? strs[i] : strs[i].substring(0, 4), lx, currentHeight + offset*47/10, mTodoTextPaint);
                currentHeight = offset*2 + currentHeight + mItemHeight / 10;
            }
        } else {


            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top,
                    calendar.isCurrentDay() ? mCurDayTextPaint :
                            calendar.isCurrentMonth() && isInRange ? mCurMonthTextPaint : mOtherMonthTextPaint);
            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + top + mItemHeight / 8,
                    calendar.isCurrentDay() && isInRange ? mCurDayLunarTextPaint :
                            calendar.isCurrentMonth() ? mCurMonthLunarTextPaint : mOtherMonthLunarTextPaint);


        }
    }

    /**
     * dp转px
     *
     * @param context context
     * @param dpValue dp
     * @return px
     */
    private static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
