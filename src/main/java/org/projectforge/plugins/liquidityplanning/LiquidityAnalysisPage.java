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

package org.projectforge.plugins.liquidityplanning;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jfree.chart.JFreeChart;
import org.projectforge.web.wicket.AbstractStandardFormPage;
import org.projectforge.web.wicket.JFreeChartImage;
import org.projectforge.web.wicket.bootstrap.GridBuilder;
import org.projectforge.web.wicket.flowlayout.ImagePanel;

public class LiquidityAnalysisPage extends AbstractStandardFormPage
{
  private static final long serialVersionUID = 6510134821712582764L;

  private static final int IMAGE_WIDTH = 800;

  private static final int IMAGE_HEIGHT = 400;

  @SpringBean(name = "liquidityEntryDao")
  private LiquidityEntryDao liquidityEntryDao;

  private LiquidityAnalysis analysis;

  private final GridBuilder gridBuilder;

  private ImagePanel chartImage;

  public LiquidityAnalysisPage(final PageParameters parameters)
  {
    super(parameters);
    final LiquidityAnalysisForm form = new LiquidityAnalysisForm(this);
    body.add(form);
    form.init();
    gridBuilder = new GridBuilder(body, "flowgrid");
  }

  /**
   * @see org.projectforge.web.wicket.AbstractSecuredPage#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    chartImage = new ImagePanel(gridBuilder.getPanel().newChildId());
    gridBuilder.getPanel().add(chartImage);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#onBeforeRender()
   */
  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    final LiquidityChartBuilder chartBuilder = new LiquidityChartBuilder();
    final JFreeChart chart = chartBuilder.create(analysis, 100);
    final JFreeChartImage image = new JFreeChartImage(ImagePanel.IMAGE_ID, chart, IMAGE_WIDTH, IMAGE_HEIGHT);
    image.add(AttributeModifier.replace("width", String.valueOf(IMAGE_WIDTH)));
    image.add(AttributeModifier.replace("height", String.valueOf(IMAGE_HEIGHT)));
    chartImage.replaceImage(image);
  }

  /**
   * @param analysis the analysis to set
   * @return this for chaining.
   */
  public LiquidityAnalysisPage setAnalysis(final LiquidityAnalysis analysis)
  {
    this.analysis = analysis;
    return this;
  }

  @Override
  protected String getTitle()
  {
    return getString("plugins.liquidityplanning.analysis");
  }
}
