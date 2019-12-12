package cn.qqtheme.framework.picker;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import cn.qqtheme.framework.util.DateUtils;
import cn.qqtheme.framework.util.LogUtils;
import cn.qqtheme.framework.widget.WheelView;

/**
 * 日期时间选择器，可同时选中日期及时间，另见{@link DatePicker}和{@link TimePicker}
 * <p/>
 * Created by Dong on 2016/5/13.
 * Refactored by 李玉江 on 2016/12/31.
 */
public class DateTimePicker extends WheelPicker {
    /**
     * 不显示
     */
    public static final int NONE = -1;
    /**
     * 年月日
     */
    public static final int YEAR_MONTH_DAY = 0;
    /**
     * 年月
     */
    public static final int YEAR_MONTH = 1;
    /**
     * 月日
     */
    public static final int MONTH_DAY = 2;

    /**
     * 24小时
     */
    public static final int HOUR_24 = 3;
    /**
     * @deprecated use {@link #HOUR_24} instead
     */
    @Deprecated
    public static final int HOUR_OF_DAY = 3;
    /**
     * 12小时
     */
    public static final int HOUR_12 = 4;
    /**
     * @deprecated use {@link #HOUR_12} instead
     */
    @Deprecated
    public static final int HOUR = 4;

    private ArrayList<String> years = new ArrayList<>();
    private ArrayList<String> months = new ArrayList<>();
    private ArrayList<String> days = new ArrayList<>();
    private ArrayList<String> hours = new ArrayList<>();
    private ArrayList<String> minutes = new ArrayList<>();
    private String yearLabel = "年", monthLabel = "月", dayLabel = "日";
    private String hourLabel = "时", minuteLabel = "分";
    private int selectedYearIndex = 0, selectedMonthIndex = 0, selectedDayIndex = 0;
    private String selectedHour = "", selectedMinute = "";
    private OnWheelListener onWheelListener;
    private OnDateTimePickListener onDateTimePickListener;
    private int dateMode = YEAR_MONTH_DAY, timeMode = HOUR_24;
    private int startYear = 2010, startMonth = 1, startDay = 1;
    private int endYear = 2020, endMonth = 12, endDay = 31;
    private int startHour, startMinute = 0;
    private int endHour, endMinute = 59;
    private int textSize = WheelView.TEXT_SIZE;
    private boolean resetWhileWheel = true;

    @IntDef(value = {NONE, YEAR_MONTH_DAY, YEAR_MONTH, MONTH_DAY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DateMode {
    }

    @IntDef(value = {NONE, HOUR_24, HOUR_12})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TimeMode {
    }

    /**
     * @see #HOUR_24
     * @see #HOUR_12
     */
    public DateTimePicker(Activity activity, @TimeMode int timeMode) {
        this(activity, YEAR_MONTH_DAY, timeMode);
    }

    public DateTimePicker(Activity activity, @DateMode int dateMode, @TimeMode int timeMode) {
        super(activity);
        if (dateMode == NONE && timeMode == NONE) {
            throw new IllegalArgumentException("The modes are NONE at the same time");
        }
        if (dateMode == YEAR_MONTH_DAY && timeMode != NONE) {
            if (screenWidthPixels < 720) {
                textSize = 14;//年月日时分，比较宽，设置字体小一点才能显示完整
            } else if (screenWidthPixels < 480) {
                textSize = 12;
            }
        }
        this.dateMode = dateMode;
        //根据时间模式初始化小时范围
        if (timeMode == HOUR_12) {
            startHour = 1;
            endHour = 12;
        } else {
            startHour = 0;
            endHour = 23;
        }
        this.timeMode = timeMode;
    }

    /**
     * 滚动时是否重置下一级的索引
     */
    public void setResetWhileWheel(boolean resetWhileWheel) {
        this.resetWhileWheel = resetWhileWheel;
    }

    /**
     * 设置年份范围
     *
     * @deprecated use {@link #setDateRangeStart(int, int, int)}
     * and {@link #setDateRangeEnd(int, int, int)} or  instead
     */
    @Deprecated
    public void setRange(int startYear, int endYear) {
        if (dateMode == NONE) {
            throw new IllegalArgumentException("Date mode invalid");
        }
        this.startYear = startYear;
        this.endYear = endYear;
        initYearData();
    }

    /**
     * 设置范围：开始的年月日
     */
    public void setDateRangeStart(int startYear, int startMonth, int startDay) {
        if (dateMode == NONE) {
            throw new IllegalArgumentException("Date mode invalid");
        }
        this.startYear = startYear;
        this.startMonth = startMonth;
        this.startDay = startDay;
        initYearData();
    }

    /**
     * 设置范围：结束的年月日
     */
    public void setDateRangeEnd(int endYear, int endMonth, int endDay) {
        if (dateMode == NONE) {
            throw new IllegalArgumentException("Date mode invalid");
        }
        this.endYear = endYear;
        this.endMonth = endMonth;
        this.endDay = endDay;
        initYearData();
    }

    /**
     * 设置范围：开始的年月日
     */
    public void setDateRangeStart(int startYearOrMonth, int startMonthOrDay) {
        if (dateMode == NONE) {
            throw new IllegalArgumentException("Date mode invalid");
        }
        if (dateMode == YEAR_MONTH_DAY) {
            throw new IllegalArgumentException("Not support year/month/day mode");
        }
        if (dateMode == YEAR_MONTH) {
            this.startYear = startYearOrMonth;
            this.startMonth = startMonthOrDay;
        } else if (dateMode == MONTH_DAY) {
            int year = Calendar.getInstance(Locale.CHINA).get(Calendar.YEAR);
            startYear = endYear = year;
            this.startMonth = startYearOrMonth;
            this.startDay = startMonthOrDay;
        }
        initYearData();
    }

    /**
     * 设置范围：结束的年月日
     */
    public void setDateRangeEnd(int endYearOrMonth, int endMonthOrDay) {
        if (dateMode == NONE) {
            throw new IllegalArgumentException("Date mode invalid");
        }
        if (dateMode == YEAR_MONTH_DAY) {
            throw new IllegalArgumentException("Not support year/month/day mode");
        }
        if (dateMode == YEAR_MONTH) {
            this.endYear = endYearOrMonth;
            this.endMonth = endMonthOrDay;
        } else if (dateMode == MONTH_DAY) {
            this.endMonth = endYearOrMonth;
            this.endDay = endMonthOrDay;
        }
        initYearData();
    }

    /**
     * 设置范围：开始的时分
     */
    public void setTimeRangeStart(int startHour, int startMinute) {
        if (timeMode == NONE) {
            throw new IllegalArgumentException("Time mode invalid");
        }
        boolean illegal = false;
        if (startHour < 0 || startMinute < 0 || startMinute > 59) {
            illegal = true;
        }
        if (timeMode == HOUR_12 && (startHour == 0 || startHour > 12)) {
            illegal = true;
        }
        if (timeMode == HOUR_24 && startHour >= 24) {
            illegal = true;
        }
        if (illegal) {
            throw new IllegalArgumentException("Time out of range");
        }
        this.startHour = startHour;
        this.startMinute = startMinute;
        initHourData();
    }

    /**
     * 设置范围：结束的时分
     */
    public void setTimeRangeEnd(int endHour, int endMinute) {
        if (timeMode == NONE) {
            throw new IllegalArgumentException("Time mode invalid");
        }
        boolean illegal = false;
        if (endHour < 0 || endMinute < 0 || endMinute > 59) {
            illegal = true;
        }
        if (timeMode == HOUR_12 && (endHour == 0 || endHour > 12)) {
            illegal = true;
        }
        if (timeMode == HOUR_24 && endHour >= 24) {
            illegal = true;
        }
        if (illegal) {
            throw new IllegalArgumentException("Time out of range");
        }
        this.endHour = endHour;
        this.endMinute = endMinute;
        initHourData();
    }

    /**
     * 设置年月日时分的显示单位
     */
    public void setLabel(String yearLabel, String monthLabel, String dayLabel, String hourLabel, String minuteLabel) {
        this.yearLabel = yearLabel;
        this.monthLabel = monthLabel;
        this.dayLabel = dayLabel;
        this.hourLabel = hourLabel;
        this.minuteLabel = minuteLabel;
    }

    /**
     * 设置默认选中的年月日时分
     */
    public void setSelectedItem(int year, int month, int day, int hour, int minute) {
        if (dateMode != YEAR_MONTH_DAY) {
            throw new IllegalArgumentException("Date mode invalid");
        }
        LogUtils.verbose(this, "change months and days while set selected");
        changeMonthData(year);
        changeDayData(year, month);
        selectedYearIndex = findItemIndex(years, year);
        selectedMonthIndex = findItemIndex(months, month);
        selectedDayIndex = findItemIndex(days, day);
        if (timeMode != NONE) {
            selectedHour = DateUtils.fillZero(hour);
            selectedMinute = DateUtils.fillZero(minute);
        }
    }

    /**
     * 设置默认选中的年月时分或者月日时分
     */
    public void setSelectedItem(int yearOrMonth, int monthOrDay, int hour, int minute) {
        if (dateMode == YEAR_MONTH_DAY) {
            throw new IllegalArgumentException("Date mode invalid");
        }
        if (dateMode == MONTH_DAY) {
            LogUtils.verbose(this, "change months and days while set selected");
            int year = Calendar.getInstance(Locale.CHINA).get(Calendar.YEAR);
            startYear = endYear = year;
            changeMonthData(year);
            changeDayData(year, yearOrMonth);
            selectedMonthIndex = findItemIndex(months, yearOrMonth);
            selectedDayIndex = findItemIndex(days, monthOrDay);
        } else if (dateMode == YEAR_MONTH) {
            LogUtils.verbose(this, "change months while set selected");
            changeMonthData(yearOrMonth);
            selectedYearIndex = findItemIndex(years, yearOrMonth);
            selectedMonthIndex = findItemIndex(months, monthOrDay);
        }
        if (timeMode != NONE) {
            selectedHour = DateUtils.fillZero(hour);
            selectedMinute = DateUtils.fillZero(minute);
        }
    }

    public void setOnWheelListener(OnWheelListener onWheelListener) {
        this.onWheelListener = onWheelListener;
    }

    public void setOnDateTimePickListener(OnDateTimePickListener listener) {
        this.onDateTimePickListener = listener;
    }

    public String getSelectedYear() {
        if (dateMode == YEAR_MONTH_DAY || dateMode == YEAR_MONTH) {
            if (years.size() <= selectedYearIndex) {
                selectedYearIndex = years.size() - 1;
            }
            return years.get(selectedYearIndex);
        }
        return "";
    }

    public String getSelectedMonth() {
        if (dateMode != NONE) {
            if (months.size() <= selectedMonthIndex) {
                selectedMonthIndex = months.size() - 1;
            }
            return months.get(selectedMonthIndex);
        }
        return "";
    }

    public String getSelectedDay() {
        if (dateMode == YEAR_MONTH_DAY || dateMode == MONTH_DAY) {
            if (days.size() <= selectedDayIndex) {
                selectedDayIndex = days.size() - 1;
            }
            return days.get(selectedDayIndex);
        }
        return "";
    }

    public String getSelectedHour() {
        if (timeMode != NONE) {
            return selectedHour;
        }
        return "";
    }

    public String getSelectedMinute() {
        if (timeMode != NONE) {
            return selectedMinute;
        }
        return "";
    }

    @NonNull
    @Override
    protected View makeCenterView() {
        // 如果未设置默认项，则需要在此初始化数据
        if ((dateMode == YEAR_MONTH_DAY || dateMode == YEAR_MONTH) && years.size() == 0) {
            LogUtils.verbose(this, "init years before make view");
            initYearData();
        }
        if (dateMode != NONE && months.size() == 0) {
            LogUtils.verbose(this, "init months before make view");
            int selectedYear = DateUtils.trimZero(getSelectedYear());
            changeMonthData(selectedYear);
        }
        if ((dateMode == YEAR_MONTH_DAY || dateMode == MONTH_DAY) && days.size() == 0) {
            LogUtils.verbose(this, "init days before make view");
            int selectedYear;
            if (dateMode == YEAR_MONTH_DAY) {
                selectedYear = DateUtils.trimZero(getSelectedYear());
            } else {
                selectedYear = Calendar.getInstance(Locale.CHINA).get(Calendar.YEAR);
            }
            int selectedMonth = DateUtils.trimZero(getSelectedMonth());
            changeDayData(selectedYear, selectedMonth);
        }
        if (timeMode != NONE && hours.size() == 0) {
            LogUtils.verbose(this, "init hours before make view");
            initHourData();
        }
        if (timeMode != NONE && minutes.size() == 0) {
            LogUtils.verbose(this, "init minutes before make view");
            changeMinuteData(DateUtils.trimZero(selectedHour));
        }

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);

        final WheelView yearView = createWheelView();
        final WheelView monthView = createWheelView();
        final WheelView dayView = createWheelView();
        final WheelView hourView = createWheelView();
        final WheelView minuteView = createWheelView();

        if (dateMode == YEAR_MONTH_DAY || dateMode == YEAR_MONTH) {
            yearView.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1.0f));
            yearView.setItems(years, selectedYearIndex);
            yearView.setOnItemSelectListener(new WheelView.OnItemSelectListener() {
                @Override
                public void onSelected(int index) {
                    selectedYearIndex = index;
                    String selectedYearStr = years.get(selectedYearIndex);
                    if (onWheelListener != null) {
                        onWheelListener.onYearWheeled(selectedYearIndex, selectedYearStr);
                    }
                    LogUtils.verbose(this, "change months after year wheeled");
                    if (resetWhileWheel) {
                        selectedMonthIndex = 0;//重置月份索引
                        selectedDayIndex = 0;//重置日子索引
                    }
                    //需要根据年份及月份动态计算天数
                    int selectedYear = DateUtils.trimZero(selectedYearStr);
                    changeMonthData(selectedYear);
                    monthView.setItems(months, selectedMonthIndex);
                    if (onWheelListener != null) {
                        onWheelListener.onMonthWheeled(selectedMonthIndex, months.get(selectedMonthIndex));
                    }
                    changeDayData(selectedYear, DateUtils.trimZero(months.get(selectedMonthIndex)));
                    dayView.setItems(days, selectedDayIndex);
                    if (onWheelListener != null) {
                        onWheelListener.onDayWheeled(selectedDayIndex, days.get(selectedDayIndex));
                    }
                }
            });
            layout.addView(yearView);
            if (!TextUtils.isEmpty(yearLabel)) {
                TextView labelView = createLabelView();
                labelView.setTextSize(textSize);
                labelView.setText(yearLabel);
                layout.addView(labelView);
            }
        }

        if (dateMode != NONE) {
            monthView.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1.0f));
            monthView.setItems(months, selectedMonthIndex);
            monthView.setOnItemSelectListener(new WheelView.OnItemSelectListener() {
                @Override
                public void onSelected(int index) {
                    selectedMonthIndex = index;
                    String selectedMonthStr = months.get(selectedMonthIndex);
                    if (onWheelListener != null) {
                        onWheelListener.onMonthWheeled(selectedMonthIndex, selectedMonthStr);
                    }
                    if (dateMode == YEAR_MONTH_DAY || dateMode == MONTH_DAY) {
                        LogUtils.verbose(this, "change days after month wheeled");
                        if (resetWhileWheel) {
                            selectedDayIndex = 0;//重置日子索引
                        }
                        int selectedYear;
                        if (dateMode == YEAR_MONTH_DAY) {
                            selectedYear = DateUtils.trimZero(getSelectedYear());
                        } else {
                            selectedYear = Calendar.getInstance(Locale.CHINA).get(Calendar.YEAR);
                        }
                        changeDayData(selectedYear, DateUtils.trimZero(selectedMonthStr));
                        dayView.setItems(days, selectedDayIndex);
                        if (onWheelListener != null) {
                            onWheelListener.onDayWheeled(selectedDayIndex, days.get(selectedDayIndex));
                        }
                    }
                }
            });
            layout.addView(monthView);
            if (!TextUtils.isEmpty(monthLabel)) {
                TextView labelView = createLabelView();
                labelView.setTextSize(textSize);
                labelView.setText(monthLabel);
                layout.addView(labelView);
            }
        }

        if (dateMode == YEAR_MONTH_DAY || dateMode == MONTH_DAY) {
            dayView.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1.0f));
            dayView.setItems(days, selectedDayIndex);
            dayView.setOnItemSelectListener(new WheelView.OnItemSelectListener() {
                @Override
                public void onSelected(int index) {
                    selectedDayIndex = index;
                    if (onWheelListener != null) {
                        onWheelListener.onDayWheeled(selectedDayIndex, days.get(selectedDayIndex));
                    }
                }
            });
            layout.addView(dayView);
            if (!TextUtils.isEmpty(dayLabel)) {
                TextView labelView = createLabelView();
                labelView.setTextSize(textSize);
                labelView.setText(dayLabel);
                layout.addView(labelView);
            }
        }

        if (timeMode != NONE) {
            hourView.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1.0f));
            hourView.setItems(hours, selectedHour);
            hourView.setOnItemSelectListener(new WheelView.OnItemSelectListener() {
                @Override
                public void onSelected(int index) {
                    selectedHour = hours.get(index);
                    if (onWheelListener != null) {
                        onWheelListener.onHourWheeled(index, selectedHour);
                    }
                    LogUtils.verbose(this, "change minutes after hour wheeled");
                    changeMinuteData(DateUtils.trimZero(selectedHour));
                    minuteView.setItems(minutes, selectedMinute);
                }
            });
            layout.addView(hourView);
            if (!TextUtils.isEmpty(hourLabel)) {
                TextView labelView = createLabelView();
                labelView.setTextSize(textSize);
                labelView.setText(hourLabel);
                layout.addView(labelView);
            }

            minuteView.setLayoutParams(new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1.0f));
            minuteView.setItems(minutes, selectedMinute);
            minuteView.setOnItemSelectListener(new WheelView.OnItemSelectListener() {
                @Override
                public void onSelected(int index) {
                    selectedMinute = minutes.get(index);
                    if (onWheelListener != null) {
                        onWheelListener.onMinuteWheeled(index, selectedMinute);
                    }
                }
            });
            layout.addView(minuteView);
            if (!TextUtils.isEmpty(minuteLabel)) {
                TextView labelView = createLabelView();
                labelView.setTextSize(textSize);
                labelView.setText(minuteLabel);
                layout.addView(labelView);
            }
        }

        return layout;
    }

    @Override
    protected void onSubmit() {
        if (onDateTimePickListener == null) {
            return;
        }
        String year = getSelectedYear();
        String month = getSelectedMonth();
        String day = getSelectedDay();
        String hour = getSelectedHour();
        String minute = getSelectedMinute();
        switch (dateMode) {
            case YEAR_MONTH_DAY:
                ((OnYearMonthDayTimePickListener) onDateTimePickListener).onDateTimePicked(year, month, day, hour, minute);
                break;
            case YEAR_MONTH:
                ((OnYearMonthTimePickListener) onDateTimePickListener).onDateTimePicked(year, month, hour, minute);
                break;
            case MONTH_DAY:
                ((OnMonthDayTimePickListener) onDateTimePickListener).onDateTimePicked(month, day, hour, minute);
                break;
            case NONE:
                ((OnTimePickListener) onDateTimePickListener).onDateTimePicked(hour, minute);
                break;
        }
    }

    private int findItemIndex(ArrayList<String> items, int item) {
        //折半查找有序元素的索引
        int index = Collections.binarySearch(items, item, new Comparator<Object>() {
            @Override
            public int compare(Object lhs, Object rhs) {
                String lhsStr = lhs.toString();
                String rhsStr = rhs.toString();
                lhsStr = lhsStr.startsWith("0") ? lhsStr.substring(1) : lhsStr;
                rhsStr = rhsStr.startsWith("0") ? rhsStr.substring(1) : rhsStr;
                try {
                    return Integer.parseInt(lhsStr) - Integer.parseInt(rhsStr);
                } catch (java.lang.NumberFormatException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        if (index < 0) {
            throw new IllegalArgumentException("Item[" + item + "] out of range");
        }
        return index;
    }

    private void initYearData() {
        years.clear();
        if (startYear == endYear) {
            years.add(String.valueOf(startYear));
        } else if (startYear < endYear) {
            //年份正序
            for (int i = startYear; i <= endYear; i++) {
                years.add(String.valueOf(i));
            }
        } else {
            //年份逆序
            for (int i = startYear; i >= endYear; i--) {
                years.add(String.valueOf(i));
            }
        }
        if (!resetWhileWheel) {
            if (dateMode == YEAR_MONTH_DAY || dateMode == YEAR_MONTH) {
                int index = years.indexOf(DateUtils.fillZero(Calendar.getInstance().get(Calendar.YEAR)));
                if (index == -1) {
                    //当前设置的年份不在指定范围，则默认选中范围开始的年
                    selectedYearIndex = 0;
                } else {
                    selectedYearIndex = index;
                }
            }
        }
    }

    private void changeMonthData(int selectedYear) {
        String preSelectMonth = "";
        if (!resetWhileWheel) {
            if (months.size() > selectedMonthIndex) {
                preSelectMonth = months.get(selectedMonthIndex);
            } else {
                preSelectMonth = DateUtils.fillZero(Calendar.getInstance().get(Calendar.MONTH) + 1);
            }
            LogUtils.verbose(this, "preSelectMonth=" + preSelectMonth);
        }
        months.clear();
        if (startMonth < 1 || endMonth < 1 || startMonth > 12 || endMonth > 12) {
            throw new IllegalArgumentException("Month out of range [1-12]");
        }
        if (startYear == endYear) {
            if (startMonth > endMonth) {
                for (int i = endMonth; i >= startMonth; i--) {
                    months.add(DateUtils.fillZero(i));
                }
            } else {
                for (int i = startMonth; i <= endMonth; i++) {
                    months.add(DateUtils.fillZero(i));
                }
            }
        } else if (selectedYear == startYear) {
            for (int i = startMonth; i <= 12; i++) {
                months.add(DateUtils.fillZero(i));
            }
        } else if (selectedYear == endYear) {
            for (int i = 1; i <= endMonth; i++) {
                months.add(DateUtils.fillZero(i));
            }
        } else {
            for (int i = 1; i <= 12; i++) {
                months.add(DateUtils.fillZero(i));
            }
        }
        if (!resetWhileWheel) {
            //当前设置的月份不在指定范围，则默认选中范围开始的月份
            int preSelectMonthIndex = months.indexOf(preSelectMonth);
            selectedMonthIndex = preSelectMonthIndex == -1 ? 0 : preSelectMonthIndex;
        }
    }

    private void changeDayData(int selectedYear, int selectedMonth) {
        int maxDays = DateUtils.calculateDaysInMonth(selectedYear, selectedMonth);
        String preSelectDay = "";
        if (!resetWhileWheel) {
            if (selectedDayIndex >= maxDays) {
                //如果之前选择的日是之前年月的最大日，则日自动为该年月的最大日
                selectedDayIndex = maxDays - 1;
            }
            if (days.size() > selectedDayIndex) {
                //年或月变动时，保持之前选择的日不动
                preSelectDay = days.get(selectedDayIndex);
            } else {
                preSelectDay = DateUtils.fillZero(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            }
            LogUtils.verbose(this, "maxDays=" + maxDays + ", preSelectDay=" + preSelectDay);
        }
        days.clear();
        if (selectedYear == startYear && selectedMonth == startMonth
                && selectedYear == endYear && selectedMonth == endMonth) {
            //开始年月及结束年月相同情况
            for (int i = startDay; i <= endDay; i++) {
                days.add(DateUtils.fillZero(i));
            }
        } else if (selectedYear == startYear && selectedMonth == startMonth) {
            //开始年月相同情况
            for (int i = startDay; i <= maxDays; i++) {
                days.add(DateUtils.fillZero(i));
            }
        } else if (selectedYear == endYear && selectedMonth == endMonth) {
            //结束年月相同情况
            for (int i = 1; i <= endDay; i++) {
                days.add(DateUtils.fillZero(i));
            }
        } else {
            for (int i = 1; i <= maxDays; i++) {
                days.add(DateUtils.fillZero(i));
            }
        }
        if (!resetWhileWheel) {
            //当前设置的日子不在指定范围，则默认选中范围开始的日子
            int preSelectDayIndex = days.indexOf(preSelectDay);
            selectedDayIndex = preSelectDayIndex == -1 ? 0 : preSelectDayIndex;
        }
    }

    private void initHourData() {
        hours.clear();
        int currentHour = 0;
        if (!resetWhileWheel) {
            if (timeMode == HOUR_24) {
                currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            } else {
                currentHour = Calendar.getInstance().get(Calendar.HOUR);
            }
        }
        for (int i = startHour; i <= endHour; i++) {
            String hour = DateUtils.fillZero(i);
            if (!resetWhileWheel) {
                if (i == currentHour) {
                    selectedHour = hour;
                }
            }
            hours.add(hour);
        }
        if (hours.indexOf(selectedHour) == -1) {
            //当前设置的小时不在指定范围，则默认选中范围开始的小时
            selectedHour = hours.get(0);
        }
        if (!resetWhileWheel) {
            selectedMinute = DateUtils.fillZero(Calendar.getInstance().get(Calendar.MINUTE));
        }
    }

    private void changeMinuteData(int selectedHour) {
        minutes.clear();
        if (startHour == endHour) {
            if (startMinute > endMinute) {
                int temp = startMinute;
                startMinute = endMinute;
                endMinute = temp;
            }
            for (int i = startMinute; i <= endMinute; i++) {
                minutes.add(DateUtils.fillZero(i));
            }
        } else if (selectedHour == startHour) {
            for (int i = startMinute; i <= 59; i++) {
                minutes.add(DateUtils.fillZero(i));
            }
        } else if (selectedHour == endHour) {
            for (int i = 0; i <= endMinute; i++) {
                minutes.add(DateUtils.fillZero(i));
            }
        } else {
            for (int i = 0; i <= 59; i++) {
                minutes.add(DateUtils.fillZero(i));
            }
        }
        if (minutes.indexOf(selectedMinute) == -1) {
            //当前设置的分钟不在指定范围，则默认选中范围开始的分钟
            selectedMinute = minutes.get(0);
        }
    }

    public interface OnWheelListener {

        void onYearWheeled(int index, String year);

        void onMonthWheeled(int index, String month);

        void onDayWheeled(int index, String day);

        void onHourWheeled(int index, String hour);

        void onMinuteWheeled(int index, String minute);

    }

    protected interface OnDateTimePickListener {

    }

    public interface OnYearMonthDayTimePickListener extends OnDateTimePickListener {

        void onDateTimePicked(String year, String month, String day, String hour, String minute);

    }

    public interface OnYearMonthTimePickListener extends OnDateTimePickListener {

        void onDateTimePicked(String year, String month, String hour, String minute);

    }

    /**
     * @deprecated use {@link OnYearMonthTimePickListener} instead
     */
    @Deprecated
    public interface OnYearMonthPickListener extends OnYearMonthTimePickListener {

    }

    public interface OnMonthDayTimePickListener extends OnDateTimePickListener {

        void onDateTimePicked(String month, String day, String hour, String minute);
    }

    /**
     * @deprecated use {@link OnMonthDayTimePickListener} instead
     */
    @Deprecated
    public interface OnMonthDayPickListener extends OnMonthDayTimePickListener {

    }

    public interface OnTimePickListener extends OnDateTimePickListener {

        void onDateTimePicked(String hour, String minute);
    }

}
