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
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.core.I18nEnum;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.flowlayout.DropDownChoicePanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * Component renders DropDown to select reminder action, TextField to set duration
 * and DropDown to select duration type.
 * 
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamEventReminderComponent extends Component
{

  private static final long serialVersionUID = -7384630904654370695L;
  private static final Integer DURATION_MAX = 1000;
  private final TeamEventDO data;
  @SuppressWarnings("unused") // used by wicked
  private List<ReminderActionType> reminderActionTypeList;
  @SuppressWarnings("unused") // used by wicked
  private List<AlarmReminderType> reminderDurationTypeList;
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
  @SuppressWarnings("unchecked")
  @Override
  protected void onInitialize()
  {
    super.onInitialize();

    if (data.getId() == null) {
      data.setReminderActionType(ReminderActionType.NONE);
    }
    final boolean reminderOptionVisibility = data.getReminderActionType() != ReminderActionType.NONE;

    // ### unchecked
    reminderDurationTypeList = (List<AlarmReminderType>) getTypeList(AlarmReminderType.class);
    reminderActionTypeList = (List<ReminderActionType>) getTypeList(ReminderActionType.class);
    final IChoiceRenderer<AlarmReminderType> reminderDurationTypeRenderer = (IChoiceRenderer<AlarmReminderType>) getChoiceRenderer(AlarmReminderType.class); //
    final IChoiceRenderer<ReminderActionType> reminderActionTypeRenderer = (IChoiceRenderer<ReminderActionType>) getChoiceRenderer(ReminderActionType.class); //
    // ###

    final MinMaxNumberField<Integer> reminderDuration = new MinMaxNumberField<Integer>(reminderPanel.getTextFieldId(), new PropertyModel<Integer>(data, "reminderDuration"), 0, DURATION_MAX);
    reminderDuration.setMaxLength(300);
    setComponentProperties(reminderDuration, reminderOptionVisibility, true);

    // reminder duration dropDown
    final IModel<List<AlarmReminderType>> reminderDurationChoicesModel = new PropertyModel<List<AlarmReminderType>>(this, "reminderDurationTypeList");
    final IModel<AlarmReminderType> reminderDurationActiveModel = new PropertyModel<AlarmReminderType>(data, "reminderDurationType");
    final DropDownChoicePanel<AlarmReminderType> reminderDurationTypeChoice = new DropDownChoicePanel<AlarmReminderType>(reminderPanel.newChildId(), reminderDurationActiveModel,
        reminderDurationChoicesModel, reminderDurationTypeRenderer, false);
    setComponentProperties(reminderDurationTypeChoice.getDropDownChoice(), reminderOptionVisibility, true);

    // reminder action dropDown
    final IModel<List<ReminderActionType>> reminderActionTypeChoiceModel = new PropertyModel<List<ReminderActionType>>(this, "reminderActionTypeList");
    final IModel<ReminderActionType> reminderActionActiveModel = new PropertyModel<ReminderActionType>(data, "reminderActionType");
    final DropDownChoicePanel<ReminderActionType> reminderActionTypeChoice = new DropDownChoicePanel<ReminderActionType>(reminderPanel.getDropDownChoiceId(), reminderActionActiveModel,
        reminderActionTypeChoiceModel, reminderActionTypeRenderer, false);
    reminderActionTypeChoice.getDropDownChoice().add(new AjaxFormComponentUpdatingBehavior("onChange"){

      private static final long serialVersionUID = -6018962442041909705L;

      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        if (data.getReminderActionType() != null) {
          boolean isVisibel = false;
          if (data.getReminderActionType().equals(ReminderActionType.MESSAGE) || data.getReminderActionType().equals(ReminderActionType.MESSAGE_SOUND)) {
            isVisibel = true;
          } else if (data.getReminderActionType().equals(ReminderActionType.NONE)) {
            isVisibel = false;
          }
          reminderDuration.setVisible(isVisibel);
          reminderDurationTypeChoice.getDropDownChoice().setVisible(isVisibel);
          reminderDurationTypeChoice.setRequired(isVisibel);
          target.add(reminderDurationTypeChoice.getDropDownChoice(), reminderDuration);
        }
      }

    });
    reminderPanel.add(reminderActionTypeChoice);
    reminderPanel.add(reminderDuration);
    reminderPanel.add(reminderDurationTypeChoice);
  }

  private void setComponentProperties(final FormComponent<?> comp, final boolean visible, final boolean markUp) {
    comp.setVisible(visible);
    comp.setRequired(visible);
    comp.setOutputMarkupId(markUp);
    comp.setOutputMarkupPlaceholderTag(markUp);
  }

  private List< ? extends I18nEnum> getTypeList(final Class< ? extends I18nEnum> obj) {
    final List<I18nEnum> list = new ArrayList<I18nEnum>();
    for (final I18nEnum type : obj.getEnumConstants()) {
      list.add(type);
    }
    return list;
  }

  private IChoiceRenderer< ? extends I18nEnum> getChoiceRenderer(final Class< ? extends I18nEnum> c) {
    final IChoiceRenderer<I18nEnum> reminderActionTypeRenderer = new IChoiceRenderer<I18nEnum>(){

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