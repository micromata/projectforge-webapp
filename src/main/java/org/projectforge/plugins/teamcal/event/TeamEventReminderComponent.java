/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.I18nEnum;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.flowlayout.DropDownChoicePanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * Component renders DropDown to select reminder action, TextField to set duration and DropDown to select duration type.
 * 
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamEventReminderComponent extends Component
{
  private static final long serialVersionUID = -7384630904654370695L;

  private static final Integer DURATION_MAX = 1000;

  private final TeamEventDO data;

  @SuppressWarnings("unused")
  // used by wicked
  private List<ReminderActionType> reminderActionTypeList;

  @SuppressWarnings("unused")
  // used by wicked
  private List<ReminderDurationUnit> reminderDurationTypeList;

  private final FieldsetPanel reminderPanel;

  /**
   * @param id
   * @param model
   */
  public TeamEventReminderComponent(final String id, final IModel<TeamEventDO> model, final FieldsetPanel fieldSet)
  {
    super(id, model);
    data = model.getObject();
    this.reminderPanel = fieldSet;
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @SuppressWarnings({ "unchecked", "serial"})
  @Override
  protected void onInitialize()
  {
    super.onInitialize();

    final boolean reminderOptionVisibility = data.getReminderActionType() != null;

    // ### unchecked
    reminderDurationTypeList = (List<ReminderDurationUnit>) getTypeList(ReminderDurationUnit.class);
    reminderActionTypeList = (List<ReminderActionType>) getTypeList(ReminderActionType.class);
    final IChoiceRenderer<ReminderDurationUnit> reminderDurationTypeRenderer = (IChoiceRenderer<ReminderDurationUnit>) getChoiceRenderer(ReminderDurationUnit.class); //
    final IChoiceRenderer<ReminderActionType> reminderActionTypeRenderer = (IChoiceRenderer<ReminderActionType>) getChoiceRenderer(ReminderActionType.class); //
    // ###

    final MinMaxNumberField<Integer> reminderDuration = new MinMaxNumberField<Integer>(reminderPanel.getTextFieldId(),
        new PropertyModel<Integer>(data, "reminderDuration"), 0, DURATION_MAX);
    WicketUtils.setSize(reminderDuration, 3);
    if (data.getReminderActionType() == null) {
      // Pre-set default values if the user selects a reminder action:
      if (NumberHelper.greaterZero(data.getReminderDuration()) == false) {
        data.setReminderDuration(15);
      }
      if (data.getReminderDurationUnit() == null) {
        data.setReminderDurationUnit(ReminderDurationUnit.MINUTES);
      }
    }
    setComponentProperties(reminderDuration, reminderOptionVisibility, true);

    // reminder duration dropDown
    final IModel<List<ReminderDurationUnit>> reminderDurationChoicesModel = new PropertyModel<List<ReminderDurationUnit>>(this,
        "reminderDurationTypeList");
    final IModel<ReminderDurationUnit> reminderDurationActiveModel = new PropertyModel<ReminderDurationUnit>(data, "reminderDurationType");
    final DropDownChoicePanel<ReminderDurationUnit> reminderDurationTypeChoice = new DropDownChoicePanel<ReminderDurationUnit>(
        reminderPanel.newChildId(), reminderDurationActiveModel, reminderDurationChoicesModel, reminderDurationTypeRenderer, false);
    setComponentProperties(reminderDurationTypeChoice.getDropDownChoice(), reminderOptionVisibility, true);

    // reminder action dropDown
    final IModel<List<ReminderActionType>> reminderActionTypeChoiceModel = new PropertyModel<List<ReminderActionType>>(this,
        "reminderActionTypeList");
    final IModel<ReminderActionType> reminderActionActiveModel = new PropertyModel<ReminderActionType>(data, "reminderActionType");
    final DropDownChoicePanel<ReminderActionType> reminderActionTypeChoice = new DropDownChoicePanel<ReminderActionType>(
        reminderPanel.newChildId(), new DropDownChoice<ReminderActionType>(DropDownChoicePanel.WICKET_ID,
            reminderActionActiveModel, reminderActionTypeChoiceModel, reminderActionTypeRenderer) {
          /**
           * @see org.apache.wicket.markup.html.form.AbstractSingleSelectChoice#getNullKey()
           */
          @Override
          protected String getNullValidKey()
          {
            return "plugins.teamcal.event.reminder.NONE";
          }
        });
    reminderActionTypeChoice.setNullValid(true);
    reminderActionTypeChoice.getDropDownChoice().add(new AjaxFormComponentUpdatingBehavior("onChange") {

      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        final boolean isVisible = data.getReminderActionType() != null;
        if (isVisible == true) {
        }
        reminderDuration.setVisible(isVisible);
        reminderDurationTypeChoice.getDropDownChoice().setVisible(isVisible);
        reminderDurationTypeChoice.setRequired(isVisible);
        target.add(reminderDurationTypeChoice.getDropDownChoice(), reminderDuration);
      }

    });
    reminderPanel.add(reminderActionTypeChoice);
    reminderPanel.add(reminderDuration);
    reminderPanel.add(reminderDurationTypeChoice);
  }

  private void setComponentProperties(final FormComponent< ? > comp, final boolean visible, final boolean markUp)
  {
    comp.setVisible(visible);
    comp.setRequired(visible);
    comp.setOutputMarkupId(markUp);
    comp.setOutputMarkupPlaceholderTag(markUp);
  }

  private List< ? extends I18nEnum> getTypeList(final Class< ? extends I18nEnum> obj)
  {
    final List<I18nEnum> list = new ArrayList<I18nEnum>();
    for (final I18nEnum type : obj.getEnumConstants()) {
      list.add(type);
    }
    return list;
  }

  private IChoiceRenderer< ? extends I18nEnum> getChoiceRenderer(final Class< ? extends I18nEnum> c)
  {
    final IChoiceRenderer<I18nEnum> reminderActionTypeRenderer = new IChoiceRenderer<I18nEnum>() {

      private static final long serialVersionUID = -4264875398872979820L;

      @Override
      public Object getDisplayValue(final I18nEnum object)
      {
        return getString(object.getI18nKey());
      }

      @Override
      public String getIdValue(final I18nEnum object, final int index)
      {
        return object.toString();
      }
    };
    return reminderActionTypeRenderer;
  }

  /**
   * @see org.apache.wicket.Component#onRender()
   */
  @Override
  protected void onRender()
  {

  }
}