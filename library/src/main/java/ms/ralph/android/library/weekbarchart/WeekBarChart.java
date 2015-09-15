//
// Copyright 2015 Ralph
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package ms.ralph.android.library.weekbarchart;

import com.appyvet.rangebar.RangeBar;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.utils.ValueFormatter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Calendar;

public class WeekBarChart extends LinearLayout {

    private BarChart chart;

    private RangeBar bar;

    private ArrayList<String> xLabel;

    private ArrayList<Integer> colors;

    private int prevStart = -1;

    private int prevEnd = -1;

    private int colorMain;

    private int colorSelected;

    private int days;

    private int colorWeekday;

    private int colorSaturday;

    private int colorSunday;

    private String xLabelFormat;

    private float xLabelTextSize;

    private float valueLabelTextSize;

    public WeekBarChart(Context context) {
        this(context, null);
    }

    public WeekBarChart(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.weekBarChartStyle);
    }

    public WeekBarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WeekBarChart(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        View root = inflate(context, R.layout.view_weekbarchart, this);
        if (isInEditMode()) {
            return;
        }

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WeekBarChart, defStyleAttr, defStyleRes);
        colorMain = ta.getColor(R.styleable.WeekBarChart_main_color, Color.argb(120, 250, 185, 55));
        colorSelected = ta.getColor(R.styleable.WeekBarChart_selected_color, Color.rgb(250, 185, 55));
        days = ta.getInteger(R.styleable.WeekBarChart_days, 8);
        colorWeekday = ta.getColor(R.styleable.WeekBarChart_weekday_color, Color.rgb(64, 64, 64));
        colorSaturday = ta.getColor(R.styleable.WeekBarChart_weekday_color, Color.rgb(81, 160, 214));
        colorSunday = ta.getColor(R.styleable.WeekBarChart_weekday_color, Color.rgb(242, 124, 85));
        xLabelFormat = ta.getString(R.styleable.WeekBarChart_x_label_format);
        valueLabelTextSize = ta.getDimension(R.styleable.WeekBarChart_value_label_text_size, 15.0f);
        xLabelTextSize = ta.getDimension(R.styleable.WeekBarChart_x_label_text_size, 10.0f);
        if (xLabelFormat == null) {
            xLabelFormat = "M/d";
        }
        ta.recycle();

        chart = (BarChart) root.findViewById(R.id.wbc_chart);
        bar = (RangeBar) root.findViewById(R.id.wbc_bar);

        colors = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            colors.add(colorSelected);
        }
        xLabel = new ArrayList<>();

        // BarChart
        initBarChart();

        // RangeBar
        initRangeBar();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void initBarChart() {
        chart.setDrawValueAboveBar(true);
        chart.setDrawMarkerViews(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDescription("");
        chart.getLegend().setEnabled(false);
        chart.setGridBackgroundColor(Color.argb(0, 0, 0, 0));

        ArrayList<Integer> xColor = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -1 * (days - 1 - i));
            xLabel.add(DateFormat.format(xLabelFormat, cal).toString());
            switch (cal.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                case Calendar.TUESDAY:
                case Calendar.WEDNESDAY:
                case Calendar.THURSDAY:
                case Calendar.FRIDAY:
                    xColor.add(colorWeekday);
                    break;
                case Calendar.SATURDAY:
                    xColor.add(colorSaturday);
                    break;
                case Calendar.SUNDAY:
                    xColor.add(colorSunday);
                    break;
            }
        }
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColors(xColor);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setLabelsToSkip(0);
        xAxis.setTextSize(xLabelTextSize);
        xAxis.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setEnabled(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setEnabled(false);

        chart.setOnTouchListener(new ChartTouchListener<BarChart>(chart) {
            private int start;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Highlight highlight = chart.getHighlightByTouchPoint(event.getX(), event.getY());
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        start = highlight.getXIndex();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_MOVE:
                        int startTemp = start;
                        int end = highlight.getXIndex();
                        if (startTemp > end) {
                            startTemp = startTemp + end;
                            end = startTemp - end;
                            startTemp = startTemp - end;
                        }
                        changeRange(startTemp, end, true);
                        break;
                }
                return true;
            }
        });
    }

    private void initRangeBar() {
        bar.setTickStart(0);
        bar.setTickEnd(days - 1);
        bar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int start, int end, String s, String s1) {
                changeRange(start, end, false);
            }
        });
        bar.setTickColor(Color.argb(0, 0, 0, 0));
        bar.setBarColor(colorMain);
        bar.setPinColor(colorSelected);
        bar.setSelectorColor(colorSelected);
        bar.setConnectingLineColor(colorSelected);
    }

    private void changeRange(int start, int end, boolean actionByChart) {
        if (prevStart == start && prevEnd == end) {
            return;
        }
        start = Math.min(Math.max(start, 0), days - 1);
        end = Math.max(Math.min(end, days - 1), 0);

        prevStart = start;
        prevEnd = end;

        for (int i = 0; i < start; i++) {
            colors.set(i, colorMain);
        }
        for (int i = start; i <= end; i++) {
            colors.set(i, colorSelected);
        }
        for (int i = end + 1; i < days; i++) {
            colors.set(i, colorMain);
        }
        chart.invalidate();
        if (actionByChart) {
            bar.setRangePinsByIndices(start, end);
        }
    }

    public void setRange(int start, int end) {
        changeRange(start, end, true);
    }

    public void setData(int[] rawData) {
        if (rawData.length != days) {
            throw new RuntimeException("Array size must be same with days. Array : " + rawData.length + ", Days : " + days);
        }
        ArrayList<BarEntry> data = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            data.add(new BarEntry(rawData[i], i));
        }
        BarDataSet set = new BarDataSet(data, null);
        set.setStackLabels(null);
        set.setColors(colors);
        set.setValueTextColors(colors);

        BarData barData = new BarData(xLabel, set);
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        barData.setValueTextSize(valueLabelTextSize);
        barData.setDrawValues(true);
        chart.setData(barData);
    }
}