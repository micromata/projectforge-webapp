/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.resourceworkload;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;

/**
 * This Renderer is for drawing an Warnsymbol in a JFreeChart, when a series has an higher Value then another Series.
 * Used at the moment to warn if the planned resource workload is higher than the maximum resource workload (availability of the resource)
 * @author Sweeps <pf@byte-storm.com>
 */
public class OverloadWarnXYAreaRenderer extends XYAreaRenderer
{

  /**
   * 
   */
  private static final long serialVersionUID = -3344109002262595600L;


  private final XYDataset compareDataset;
  private final int compareSeries;

  private Color warnSymbolOutlineColor          = Color.red;
  private Color warnSymbolInlineColor           = Color.yellow;
  private final Color warnSymbolExclamationMarkCOlor  = Color.black;
  // private static final Logger log = Logger.getLogger(OverloadWarnXYAreaRenderer.class);


  /**
   * If the series with index 'compareSeries' is at any x-point higher in y-value than 'compareDataset', an warn symbol is drawn above this series
   * @see XYAreaRenderer()
   * @param compareDataset
   * @param compareSeries
   */
  public OverloadWarnXYAreaRenderer(final XYDataset compareDataset, final int compareSeries)
  {
    super();
    this.compareDataset = compareDataset;
    this.compareSeries = compareSeries;
  }
  /**
   * If the series with index 'compareSeries' is at any x-point higher in y-value than 'compareDataset', an warn symbol is drawn above this series
   * @see XYAreaRenderer(int)
   * @param compareDataset
   * @param compareSeries
   * @param type
   */
  public OverloadWarnXYAreaRenderer(final XYDataset compareDataset, final int compareSeries, final int type)
  {
    super(type);
    this.compareDataset = compareDataset;
    this.compareSeries = compareSeries;
  }

  /**
   * If the series with index 'compareSeries' is at any x-point higher in y-value than 'compareDataset', an warn symbol is drawn above this series
   * @see XYAreaRenderer(int, XYToolTipGenerator, urlGenerator)
   * @param compareDataset
   * @param compareSeries
   * @param type
   * @param toolTipGenerator
   * @param urlGenerator
   */
  public OverloadWarnXYAreaRenderer(final XYDataset compareDataset, final int compareSeries, final int type, final XYToolTipGenerator toolTipGenerator, final XYURLGenerator urlGenerator)
  {
    super(type, toolTipGenerator, urlGenerator);
    this.compareDataset = compareDataset;
    this.compareSeries = compareSeries;
  }

  /**
   * overriide to draw an warn symbol above the series plot
   * @see org.jfree.chart.renderer.xy.XYAreaRenderer#drawItem(java.awt.Graphics2D, org.jfree.chart.renderer.xy.XYItemRendererState, java.awt.geom.Rectangle2D, org.jfree.chart.plot.PlotRenderingInfo, org.jfree.chart.plot.XYPlot, org.jfree.chart.axis.ValueAxis, org.jfree.chart.axis.ValueAxis, org.jfree.data.xy.XYDataset, int, int, org.jfree.chart.plot.CrosshairState, int)
   */
  @Override
  public void drawItem(final Graphics2D g2, final XYItemRendererState state,
      final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot,
      final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset,
      final int series, final int item, final CrosshairState crosshairState, final int pass) {

    super.drawItem(
        g2, state, dataArea, info, plot, domainAxis, rangeAxis,
        dataset, series, item, crosshairState, pass
    );

    //log.info("test "+item);
    if (!getItemVisible(series, item) || series != compareSeries) {
      return;
    }

    if (dataset.getYValue(series, item) <= compareDataset.getYValue(0, item)) {
      return;
    }

    // get the data point...
    final double x1 = dataset.getXValue(series, item);
    double y1 = dataset.getYValue(series, item);
    if (Double.isNaN(y1)) {
      y1 = 0.0;
    }

    final int itemCount = dataset.getItemCount(series);
    final double x2 = dataset.getXValue(series, Math.min(item+1, itemCount-1));
    double y2 = dataset.getYValue(series, Math.min(item+1, itemCount-1));
    if (Double.isNaN(y1)) {
      y2 = 0.0;
    }


    if (y1 != y2) return;

    final double transX1 = domainAxis.valueToJava2D(x1, dataArea,
        plot.getDomainAxisEdge());
    final double transY1 = rangeAxis.valueToJava2D(y1, dataArea,
        plot.getRangeAxisEdge());

    final double transX2 = domainAxis.valueToJava2D(x2, dataArea,
        plot.getDomainAxisEdge());
    final double transY2 = rangeAxis.valueToJava2D(y2, dataArea,
        plot.getRangeAxisEdge());

    double warnSymbolX, warnSymbolY, xSymbolFittingSize, ySymbolFittingSize, warnSymbolSize;
    warnSymbolX = 0.5f*(transX1+transX2);
    warnSymbolY = 0.5*(transY1+transY2);

    xSymbolFittingSize = Math.abs(transX1 - transX2);

    ySymbolFittingSize = Math.abs(
        rangeAxis.valueToJava2D(rangeAxis.getUpperBound()*rangeAxis.getUpperMargin()*0.8, dataArea, plot.getRangeAxisEdge())
        -
        rangeAxis.valueToJava2D(0.0, dataArea, plot.getRangeAxisEdge())
    );

    warnSymbolSize = 0.9*Math.min(xSymbolFittingSize, ySymbolFittingSize);
    // log.info("x1: "+x1+"; x2: "+x2);
    // log.info("y1: "+y1+"; y2: "+y2);

    drawWarnSymbol(g2, warnSymbolX, warnSymbolY-warnSymbolSize*0.6, warnSymbolSize, warnSymbolSize);
  }

  /**
   * Draws an Warnsymbol at the given position in the given size
   * @param g2
   * @param x
   * @param y
   * @param w
   * @param h
   */
  protected void drawWarnSymbol(final Graphics2D g2, final double x, final double y, final double w, final double h) {
    Polygon p;

    //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    //g2.setStroke(new BasicStroke (1, BasicStroke.CAP_ROUND , BasicStroke.JOIN_ROUND ));

    // draw outline triangle
    g2.setColor(Color.red);
    p = new Polygon();
    p.addPoint((int)(x), (int)(y-0.5*h));
    p.addPoint((int)(x-0.5*w), (int)(y+0.5*h));
    p.addPoint((int)(x+0.5*w), (int)(y+0.5*h));
    g2.fillPolygon(p);

    //g2.setColor(warnSymbolExclamationMarkCOlor);
    //g2.drawPolygon(p);

    // draw inline triangle
    g2.setColor(Color.yellow);
    p = new Polygon();
    p.addPoint((int)(x), (int)(y-0.3*h));
    p.addPoint((int)(x-0.35*w), (int)(y+0.4*h));
    p.addPoint((int)(x+0.35*w), (int)(y+0.4*h));
    g2.fillPolygon(p);

    //g2.setColor(warnSymbolExclamationMarkCOlor);
    //g2.drawPolygon(p);

    // draw exclamation mark
    g2.setColor(warnSymbolExclamationMarkCOlor);
    p = new Polygon();
    p.addPoint((int)(x-0.04*w), (int)(y-0.14*h));
    p.addPoint((int)(x+0.04*w), (int)(y-0.14*h));
    p.addPoint((int)(x+0.017*w), (int)(y+0.23*h));
    p.addPoint((int)(x-0.017*w), (int)(y+0.23*h));
    g2.fillPolygon(p);

    g2.fillOval((int)(x-0.06*w), (int)(y+0.25*h), (int)(0.12*w), (int)(0.12*h));

  }
  public Color getWarnSymbolOutlineColor()
  {
    return warnSymbolOutlineColor;
  }
  public void setWarnSymbolOutlineColor(final Color warnSymbolOutlineColor)
  {
    this.warnSymbolOutlineColor = warnSymbolOutlineColor;
  }
  public Color getWarnSymbolInlineColor()
  {
    return warnSymbolInlineColor;
  }
  public void setWarnSymbolInlineColor(final Color warnSymbolInlineColor)
  {
    this.warnSymbolInlineColor = warnSymbolInlineColor;
  }

}
