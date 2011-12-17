import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;

public class ChartTester extends ApplicationFrame {

    public ChartTester(String title) {
        super(title);
    }

    public static void main(String[] args) {
        final ChartTester chart = new ChartTester("Tweets");
        chart.showChart();
        chart.pack();
        chart.setVisible(true);
    }

    private void showChart() {
        final NumberAxis xAxis = new NumberAxis("Longitude");
        final NumberAxis yAxis = new NumberAxis("Latitude");

        float[][] data = new float[2][10];
        data[0][0] = 0f;
        data[1][0] = 0f;
        data[0][1] = 1f;
        data[1][1] = 1f;
        data[0][2] = -1f;
        data[1][2] = -1f;

        FastScatterPlot plot = new FastScatterPlot(data, xAxis, yAxis);
        plot.setRangeGridlinesVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.markerChanged(new MarkerChangeEvent(new ValueMarker(1.0f, Color.BLUE, new BasicStroke(10.0f))));

        JFreeChart chart = new JFreeChart("Tweets", plot);

        final ChartPanel panel = new ChartPanel(chart, true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));

        panel.setMinimumDrawHeight(10);
        panel.setMaximumDrawHeight(2000);
        panel.setMinimumDrawWidth(20);
        panel.setMaximumDrawWidth(2000);

        setContentPane(panel);
    }

}
