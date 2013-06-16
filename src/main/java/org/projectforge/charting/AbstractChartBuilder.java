/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.charting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.projectforge.user.PFUserContext;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class AbstractChartBuilder
{
  private Shape timeseriesShape = new Ellipse2D.Float(-3, -3, 6, 6);

  private Stroke timeseriesStroke = new BasicStroke(3.0f);

  private AbstractXYItemRenderer renderer;

  private JFreeChart chart;

  /**
   * @param dataset
   * @param showAxisValues
   * @param valueAxisUnitKey
   * @return
   */
  public AbstractChartBuilder prepare(final TimeSeriesCollection dataset)
  {
    chart = ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, true, true, false);
    if (renderer == null) {
      renderer = new XYLineAndShapeRenderer(true, true);
    }
    if (timeseriesShape != null) {
      for (int i = 0; i < dataset.getSeries().size(); i++) {
        renderer.setSeriesShape(i, timeseriesShape);
      }
    } else {
      final Shape none = new Rectangle();
      for (int i = 0; i < dataset.getSeries().size(); i++) {
        renderer.setSeriesShape(i, none);
      }
    }
    for (int i = 0; i < dataset.getSeries().size(); i++) {
      renderer.setSeriesStroke(i, timeseriesStroke);
      renderer.setSeriesVisibleInLegend(i, false);
    }
    return this;
  }

  public JFreeChart create(final boolean showAxisValues, final String valueAxisUnitKey)
  {
    final XYPlot plot = chart.getXYPlot();
    plot.setRenderer(renderer);
    plot.setBackgroundPaint(Color.white);
    plot.setDomainGridlinePaint(Color.lightGray);
    plot.setRangeGridlinePaint(Color.lightGray);
    final DateAxis xAxis = new DateAxis();
    xAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
    xAxis.setLowerMargin(0.0);
    xAxis.setUpperMargin(0.0);
    xAxis.setVisible(showAxisValues);
    plot.setDomainAxis(xAxis);
    final NumberAxis yAxis;
    if (showAxisValues == true && valueAxisUnitKey != null) {
      yAxis = new NumberAxis(PFUserContext.getLocalizedString(valueAxisUnitKey));
    } else {
      yAxis = new NumberAxis();
    }
    yAxis.setVisible(showAxisValues);
    plot.setRangeAxis(yAxis);
    plot.setOutlineVisible(false);
    return chart;
  }

  public Color getRedFill()
  {
    return new Color(238, 176, 176);
  }

  public Color getGreenFill()
  {
    return new Color(135, 206, 112);
  }

  public Color getRedMarker()
  {
    return new Color(222, 23, 33);
  }

  public Color getGreenMarker()
  {
    return new Color(64, 169, 59);
  }

  /**
   * @param timeseriesShape the timeseriesShape to set
   * @return this for chaining.
   */
  public AbstractChartBuilder setTimeseriesShape(final Shape timeseriesShape)
  {
    this.timeseriesShape = timeseriesShape;
    return this;
  }

  /**
   * @param timeseriesStroke the timeseriesStroke to set
   * @return this for chaining.
   */
  public AbstractChartBuilder setTimeseriesStroke(final Stroke timeseriesStroke)
  {
    this.timeseriesStroke = timeseriesStroke;
    return this;
  }

  /**
   * @param xyDifferenceRenderer the xyDifferenceRenderer to set
   * @return this for chaining.
   */
  public AbstractChartBuilder setXyDifferenceRenderer(final Color positivePaint, final Color negativePaint, final boolean shapes)
  {
    this.renderer = new XYDifferenceRenderer(positivePaint, negativePaint, shapes);
    return this;
  }

  /**
   * @param xyDifferenceRenderer the xyDifferenceRenderer to set
   * @return this for chaining.
   */
  public AbstractChartBuilder setXYLineAndShapeRenderer(final boolean lines, final boolean shapes)
  {
    this.renderer = new XYLineAndShapeRenderer(lines, shapes);
    return this;
  }

  /**
   * @return the renderer
   */
  public AbstractXYItemRenderer getRenderer()
  {
    return renderer;
  }
}
