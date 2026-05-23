package com.example.swift_app.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.swift_app.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class InsightsFragment extends Fragment {

    private LineChart spendingChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);

        spendingChart = view.findViewById(R.id.spendingChart);
        setupChart();
        loadMockData();

        return view;
    }

    private void setupChart() {
        spendingChart.getDescription().setEnabled(false);
        spendingChart.setDrawGridBackground(false);
        spendingChart.getLegend().setEnabled(false);
        spendingChart.setTouchEnabled(true);
        spendingChart.setScaleEnabled(true);
        spendingChart.setPinchZoom(false);

        XAxis xAxis = spendingChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#8892B0"));
        xAxis.setDrawGridLines(false);

        spendingChart.getAxisLeft().setTextColor(Color.parseColor("#8892B0"));
        spendingChart.getAxisLeft().setDrawGridLines(true);
        spendingChart.getAxisLeft().setGridColor(Color.parseColor("#1F2937"));
        spendingChart.getAxisRight().setEnabled(false);
    }

    private void loadMockData() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 1200));
        entries.add(new Entry(1, 800));
        entries.add(new Entry(2, 1100));
        entries.add(new Entry(3, 900));
        entries.add(new Entry(4, 1500));
        entries.add(new Entry(5, 700));
        entries.add(new Entry(6, 1300));

        LineDataSet dataSet = new LineDataSet(entries, "Weekly Spending");
        dataSet.setColor(Color.parseColor("#00C9A7"));
        dataSet.setCircleColor(Color.parseColor("#00C9A7"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(Color.TRANSPARENT);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#00C9A7"));
        dataSet.setFillAlpha(30);

        LineData lineData = new LineData(dataSet);
        spendingChart.setData(lineData);
        spendingChart.animateX(1500);
        spendingChart.invalidate();
    }
}
