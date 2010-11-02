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

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.converter.MyDateConverter;


/**
 * Panel for date selection. Works for java.util.Date and java.sql.Date. For java.sql.Date don't forget to call the constructor with
 * targetType java.sql.Date.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DatePanel extends FormComponentPanel<Date>
{
  private static final long serialVersionUID = 3785639935585959803L;

  protected Date date;

  protected DateTextField dateField;

  protected boolean modelMarkedAsChanged;

  /**
   * @param id
   * @param model
   * @param caller If set then CalendarPage will be available via second calendar icon.
   * @param targetType java.util.Date is default. If your model stores the date of type java.sql.Date then java.sql.Date.class should be
   *          given as targetType for correct converting.
   */
  @SuppressWarnings("serial")
  public DatePanel(final String id, final IModel<Date> model, final DatePanelSettings settings)
  {
    super(id, model);
    setType(settings.targetType);
    final MyDateConverter dateConverter = new MyDateConverter(settings.targetType, "S-");
    dateField = new DateTextField("dateField", new PropertyModel<Date>(this, "date"), dateConverter);
    dateField.add(new SimpleAttributeModifier("size", "10"));
    add(dateField);
    /*
     * @SuppressWarnings("serial") final DatePicker datePicker = new DatePicker() { @Override protected boolean enableMonthYearSelection() {
     * return true; } }; dateField.add(datePicker);
     */
    final SubmitLink selectDateButton = new SubmitLink("selectDate") {
      public void onSubmit()
      {
        if (settings.callerPage != null) {
          dateField.validate(); // Force to update model from user input.
          String selectProperty = id;
          if (StringUtils.isNotEmpty(settings.selectProperty) == true) {
            selectProperty = settings.selectProperty;
          }
          final CalendarPage calendarPage = new CalendarPage(settings.callerPage, selectProperty, dateField.getConvertedInput());
          calendarPage.setSelectPeriodMode(settings.selectPeriodMode);
          calendarPage.setSelectStartStopTime(settings.selectStartStopTime);
          calendarPage.setTargetType(settings.targetType);
          calendarPage.init();
          setResponsePage(calendarPage);
        }
      };
    };
    selectDateButton.setDefaultFormProcessing(false);
    if (settings.callerPage == null) {
      selectDateButton.setVisible(false);
    }
    add(selectDateButton);
    String i18nKey;
    if (settings.tooltipI18nKey != null) {
      i18nKey = settings.tooltipI18nKey;
    } else if (settings.selectPeriodMode == true) {
      i18nKey = "tooltip.selectDateOrPeriod";
    } else {
      i18nKey = "tooltip.selectDate";
    }
    selectDateButton.add(new TooltipImage("selectDateImage", getResponse(), WebConstants.IMAGE_DATE_SELECT, PFUserContext
        .getLocalizedString(i18nKey)));
  }

  public void setFocus()
  {
    dateField.add(new FocusOnLoadBehavior());
  }

  /**
   * Work around: If you change the model call this method, so onBeforeRender calls DateField.modelChanged() for updating the form text
   * field.
   */
  public void markModelAsChanged()
  {
    modelMarkedAsChanged = true;
  }

  @Override
  public void validate()
  {
    dateField.validate();
    super.validate();
  }

  @Override
  protected void onBeforeRender()
  {
    date = (Date) getDefaultModelObject();
    if (modelMarkedAsChanged == true) {
      dateField.modelChanged();
      modelMarkedAsChanged = false;
    }
    dateField.setRequired(isRequired());
    super.onBeforeRender();
  }

  @Override
  public void updateModel()
  {
    if (modelMarkedAsChanged == true) {
      // Work-around: update model only if not marked as changed. Prevent overwriting the model by the user's input.
      modelMarkedAsChanged = false;
    } else {
      super.updateModel();
    }
  }

  @Override
  protected void convertInput()
  {
    setConvertedInput(dateField.getConvertedInput());
  }

  @Override
  public String getInput()
  {
    return dateField.getInput();
  }
}
