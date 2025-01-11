package com.sakibavdibasicipia.example.view;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.sakibavdibasicipia.example.config.DatabaseConfig;
import org.bson.Document;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import com.mongodb.client.MongoDatabase;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SalaryChart {

    private String title;
    private String[] months;
    private double[] salaries;

    public SalaryChart(String title, String[] months, double[] salaries) {
        this.title = title;
        this.months = months;
        this.salaries = salaries;
    }

    public void display() {
        int highestSalary = getHighestSalaryFromDatabase();
        JFreeChart lineChart = createChart(createDataset(), highestSalary);
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        JFrame chartFrame = new JFrame(title);
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.getContentPane().add(chartPanel);
        chartFrame.pack();
        chartFrame.setVisible(true);
    }

    private DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < months.length; i++) {
            dataset.addValue(salaries[i], "Plata", months[i]);
        }

        return dataset;
    }

    private JFreeChart createChart(DefaultCategoryDataset dataset, int highestSalary) {
        JFreeChart chart = ChartFactory.createLineChart(
                title,
                "Mjesec",
                "Plata",
                dataset
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangePannable(true);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(0, highestSalary + (highestSalary * 0.1));

        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(3f));  // Thicker line
        renderer.setSeriesShapesVisible(0, true);  // Show dots at data points
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));  // Dot size

        plot.setRenderer(renderer);

        return chart;
    }

    private int getHighestSalaryFromDatabase() {
        AggregateIterable<Document> result = DatabaseConfig.getDatabase().getCollection("salaries").aggregate(Arrays.asList(
                Aggregates.group(null, Accumulators.max("maxSalary", "$salary"))
        ));

        Document doc = result.first();
        if (doc != null) {
            return doc.getInteger("maxSalary");
        }
        return 0;
    }

}
