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

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.wicket.AbstractStandardFormPage;
import org.projectforge.web.wicket.bootstrap.GridBuilder;

public class LiquidityAnalysisPage extends AbstractStandardFormPage
{
  private static final long serialVersionUID = 6510134821712582764L;

  private static final int IMAGE_WIDTH = 500;

  private static final int IMAGE_HEIGHT = 400;

  @SpringBean(name = "liquidityEntryDao")
  private LiquidityEntryDao liquidityEntryDao;

  private LiquidityAnalysis analysis;

  public LiquidityAnalysisPage(final PageParameters parameters)
  {
    super(parameters);
    final LiquidityAnalysisForm form = new LiquidityAnalysisForm(this);
    body.add(form);
    form.init();
    final GridBuilder gridBuilder = new GridBuilder(body, "flowgrid");
    // final Shape shape = new Ellipse2D.Float(-3, -3, 6, 6);
    // // final Shape shape = null;
    // final Stroke stroke = new BasicStroke(3.0f);
    // // final Stroke stroke = new BasicStroke(1.0f);
    // final EmployeeDO employee = employeeDao.getByUserId(PFUserContext.getUserId());
    // double workingHoursPerDay = 8;
    // if (employee != null && NumberHelper.greaterZero(employee.getWochenstunden()) == true) {
    // workingHoursPerDay = employee.getWochenstunden() / 5;
    // }
    // final TimesheetDisciplineChartBuilder chartBuilder = new TimesheetDisciplineChartBuilder();
    // final JFreeChart chart1 = chartBuilder.create(timesheetDao, getUser().getId(), workingHoursPerDay, LAST_N_DAYS, shape, stroke, true);
    // final JFreeChartImage image = new JFreeChartImage("timesheetStatisticsImage1", chart1, IMAGE_WIDTH, IMAGE_HEIGHT);
    // image.add(AttributeModifier.replace("width", String.valueOf(IMAGE_WIDTH)));
    // image.add(AttributeModifier.replace("height", String.valueOf(IMAGE_HEIGHT)));
    // body.add(image);
    // body.add(timesheetDisciplineChart1Legend);
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
