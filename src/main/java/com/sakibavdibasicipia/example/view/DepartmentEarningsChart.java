package com.sakibavdibasicipia.example.view;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.sakibavdibasicipia.example.config.DatabaseConfig;
import org.bson.Document;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DepartmentEarningsChart {

    public static void displayBarChart(MongoDatabase database) {
        MongoCollection<Document> departmentsCollection = database.getCollection("departments");
        MongoCollection<Document> salariesCollection = database.getCollection("salaries");

        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonth = currentDate.minusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String previousMonthStr = previousMonth.format(formatter);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<Document> departments = departmentsCollection.find().into(new ArrayList<>());
        for (Document department : departments) {
            String departmentName = department.getString("name");
            Document result = salariesCollection.aggregate(Arrays.asList(
                    Aggregates.match(new Document("month", previousMonthStr)),
                    Aggregates.lookup("users", "employee_id", "_id", "user_details"),
                    Aggregates.unwind("$user_details"),
                    Aggregates.match(new Document("user_details.department_id", department.getObjectId("_id"))),
                    Aggregates.group(null, Accumulators.sum("total_salary", "$salary"))
            )).first();
            dataset.addValue(result.getInteger("total_salary"), "Zarada", departmentName);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Ukupna zarada po odjelu",
                "Odjel",
                "Ukupna zarada",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 74, 173));
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setMaximumBarWidth(0.3);
        renderer.setItemMargin(0.1);

        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setBackgroundPaint(Color.WHITE);

        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        JFrame chartFrame = new JFrame("Komparacija ukupne zarade po odjelu");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.getContentPane().add(chartPanel);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(null);
        chartFrame.setVisible(true);
    }
}
