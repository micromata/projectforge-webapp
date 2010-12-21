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

package org.projectforge.web.fibu;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.fibu.EmployeeSalaryDO;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;

public class EmployeeSalaryEditForm extends AbstractEditForm<EmployeeSalaryDO, EmployeeSalaryEditPage>
{
  private static final long serialVersionUID = 8746545908106124484L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmployeeSalaryEditForm.class);

  DatePanel eintrittsDatePanel, austrittsDatePanel;

  public EmployeeSalaryEditForm(EmployeeSalaryEditPage parentPage, EmployeeSalaryDO data)
  {
    super(parentPage, data);
    this.colspan = 4;
  }

  @Override
  protected void init()
  {
    super.init();
    final UserSelectPanel userSelectPanel = new UserSelectPanel("user", new PropertyModel<PFUserDO>(data, "user"), parentPage, "userId");
    add(userSelectPanel.setRequired(true));
    userSelectPanel.init();
    add(new Kost1FormComponent("kost1", new PropertyModel<Kost1DO>(data, "kost1"), true));
    // DropDownChoice status
//    final LabelValueChoiceRenderer<EmployeeSalaryStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<EmployeeSalaryStatus>(this, EmployeeSalaryStatus
//        .values());
//    @SuppressWarnings("unchecked")
//    final DropDownChoice statusChoice = new DropDownChoice("status", new PropertyModel(data, "status"), statusChoiceRenderer.getValues(),
//        statusChoiceRenderer);
//    statusChoice.setNullValid(false).setRequired(true);
//    add(statusChoice);
    add(new MaxLengthTextField("division", new PropertyModel<String>(data, "abteilung")));
    add(new MaxLengthTextField("position", new PropertyModel<String>(data, "position")));
    add(new MinMaxNumberField<Integer>("wochenstunden", new PropertyModel<Integer>(data, "wochenstunden"), 0, 168));
    add(new MinMaxNumberField<Integer>("urlaubstage", new PropertyModel<Integer>(data, "urlaubstage"), 0, 366));
    add(eintrittsDatePanel = new DatePanel("eintrittsDatum", new PropertyModel<Date>(data, "eintrittsDatum"), new DatePanelSettings()));
    add(austrittsDatePanel = new DatePanel("austrittsDatum", new PropertyModel<Date>(data, "austrittsDatum"), new DatePanelSettings()));
    add(new MaxLengthTextArea("comment", new PropertyModel<String>(data, "comment")));
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
