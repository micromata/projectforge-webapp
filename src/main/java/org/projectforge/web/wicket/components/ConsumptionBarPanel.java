/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.wicket.components;

import java.math.BigDecimal;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.projectforge.common.NumberHelper;
import org.projectforge.web.timesheet.TimesheetListPage;
import org.projectforge.web.wicket.WicketUtils;


/**
 * Shows a div layer with a colored percentage bar.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ConsumptionBarPanel extends Panel
{
  private static final long serialVersionUID = -4328646802035960450L;

  /**
   * @param id
   * @param usage
   * @param maxValue
   * @param taskId
   * @param taskNodeFinished Depending on the task node is finished or not, the colors are different: E. g. a 95% used bar is green for
   *          finished tasks, for unfinished ones not.
   * @param unit
   * @param linkEnabled If true then the user can click on this bar for getting all time sheets behind this bar.
   */
  public ConsumptionBarPanel(final String id, final BigDecimal usage, BigDecimal maxValue, final Integer taskId,
      final boolean taskNodeFinished, final String unit, final boolean linkEnabled)
  {
    super(id);
    if (NumberHelper.isNotZero(maxValue) == false) {
      maxValue = null;
    }
    @SuppressWarnings( { "unchecked", "serial"})
    final Link< ? > showTimesheetsLink = new Link("sheets") {
      @Override
      public void onClick()
      {
        final PageParameters parameters = new PageParameters();
        parameters.put(TimesheetListPage.PARAMETER_KEY_STORE_FILTER, false);
        parameters.put(TimesheetListPage.PARAMETER_KEY_TASK_ID, taskId);
        parameters.put(TimesheetListPage.PARAMETER_KEY_START_TIME, null);
        parameters.put(TimesheetListPage.PARAMETER_KEY_STOP_TIME, null);
        parameters.put(TimesheetListPage.PARAMETER_KEY_USER_ID, null);
        final TimesheetListPage timesheetListPage = new TimesheetListPage(parameters);
        setResponsePage(timesheetListPage);
      }
    };
    showTimesheetsLink.setEnabled(linkEnabled);
    add(showTimesheetsLink);
    final WebMarkupContainer bar = new WebMarkupContainer("bar");
    final Label progressLabel = new Label("progress", new Model<String>(" "));
    final int percentage = maxValue != null ? usage.divide(maxValue, 2, BigDecimal.ROUND_HALF_UP).multiply(NumberHelper.HUNDRED).intValue()
        : 0;
    int width = percentage <= 100 ? percentage : 10000 / percentage;
    if (percentage <= 80 || (taskNodeFinished == true && percentage <= 100)) {
      if (percentage > 0) {
        bar.add(new SimpleAttributeModifier("class", "progress-done"));
      } else {
        bar.add(new SimpleAttributeModifier("class", "progress-none"));
        progressLabel.setVisible(false);
      }
    } else if (percentage <= 90) {
      bar.add(new SimpleAttributeModifier("class", "progress-80"));
    } else if (percentage <= 100) {
      bar.add(new SimpleAttributeModifier("class", "progress-90"));
    } else if (taskNodeFinished == true && percentage <= 110) {
      bar.add(new SimpleAttributeModifier("class", "progress-overbooked-min"));
    } else {
      bar.add(new SimpleAttributeModifier("class", "progress-overbooked"));
    }
    if (maxValue == null && (usage == null || usage.compareTo(BigDecimal.ZERO) == 0)) {
      bar.setVisible(false);
    }
    progressLabel.add(new SimpleAttributeModifier("style", "width: " + width + "%;"));
    final StringBuffer buf = new StringBuffer();
    buf.append(NumberHelper.getNumberFractionFormat(getLocale(), usage.scale()).format(usage));
    if (unit != null) {
      buf.append(unit);
    }
    if (maxValue != null) {
      buf.append("/");
      buf.append(NumberHelper.getNumberFractionFormat(getLocale(), maxValue.scale()).format(maxValue));
      buf.append(unit);
      buf.append(" (").append(percentage).append("%)");
    }
    WicketUtils.addTooltip(bar, buf.toString(), true);
    showTimesheetsLink.add(bar);
    bar.add(progressLabel);
  }
}
