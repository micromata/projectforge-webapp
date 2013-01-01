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

package org.projectforge.export;

import org.jfree.chart.JFreeChart;

public class ExportJFreeChart
{
  private JFreeChart jFreeChart;

  private int width;

  private int height;
  
  private JFreeChartImageType imageType = JFreeChartImageType.JPEG;

  public ExportJFreeChart(final JFreeChart jFreeChart, final int width, final int height)
  {
    this.jFreeChart = jFreeChart;
    this.width = width;
    this.height = height;
  }
  
  public JFreeChart getJFreeChart()
  {
    return jFreeChart;
  }
  
  public int getWidth()
  {
    return width;
  }
  
  public int getHeight()
  {
    return height;
  }
  
  public JFreeChartImageType getImageType()
  {
    return imageType;
  }
  
  public ExportJFreeChart setImageType(JFreeChartImageType imageType)
  {
    this.imageType = imageType;
    return this;
  }
}
