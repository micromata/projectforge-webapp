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
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
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
  private List<ReminderActionType> reminderActionTypeList;
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
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    if (data.getId() == null) {
      data.setReminderActionType(ReminderActionType.NONE);
    }
    final boolean reminderOptionVisibility = data.getReminderActionType() != ReminderActionType.NONE;

    final MinMaxNumberField<Integer> reminderDuration = new MinMaxNumberField<Integer>(reminderPanel.getTextFieldId(), new PropertyModel<Integer>(data, "reminderDuration"), 0, DURATION_MAX);
    reminderDuration.setMaxLength(300);
    reminderDuration.setVisible(reminderOptionVisibility);
    reminderDuration.setRequired(reminderOptionVisibility);
    reminderDuration.setOutputMarkupId(true);
    reminderDuration.setOutputMarkupPlaceholderTag(true);

    // reminder duration dropdown
    final IChoiceRenderer<AlarmReminderType> reminderDurationTypeRenderer = new IChoiceRenderer<AlarmReminderType>(){
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      @Override
      public Object getDisplayValue(final AlarmReminderType object)
      {
        return getString(object.getI18nKey());
      }

      @Override
      public String getIdValue(final AlarmReminderType object, final int index)
      {
        return object.name();
      }
    };

    reminderDurationTypeList = new ArrayList<AlarmReminderType>();
    for (final AlarmReminderType type : AlarmReminderType.values()) {
      reminderDurationTypeList.add(type);
    }

    final IModel<List<AlarmReminderType>> reminderDurationChoicesModel = new PropertyModel<List<AlarmReminderType>>(this, "reminderDurationTypeList");
    final IModel<AlarmReminderType> reminderDurationActiveModel = new PropertyModel<AlarmReminderType>(data, "reminderDurationType");
    final DropDownChoicePanel<AlarmReminderType> reminderDurationTypeChoice = new DropDownChoicePanel<AlarmReminderType>(reminderPanel.newChildId(), reminderDurationActiveModel,
        reminderDurationChoicesModel, reminderDurationTypeRenderer, false);
    reminderDurationTypeChoice.getDropDownChoice().setVisible(reminderOptionVisibility);
    reminderDurationTypeChoice.setRequired(reminderOptionVisibility);
    reminderDurationTypeChoice.getDropDownChoice().setOutputMarkupId(true);
    reminderDurationTypeChoice.getDropDownChoice().setOutputMarkupPlaceholderTag(true);

    // reminder action drop down
    final IChoiceRenderer<ReminderActionType> reminderActionTypeRenderer = new IChoiceRenderer<ReminderActionType>(){
      private static final long serialVersionUID = 1L;

      @Override
      public Object getDisplayValue(final ReminderActionType object)
      {
        return getString(object.getI18nKey());
      }

      @Override
      public String getIdValue(final ReminderActionType object, final int index)
      {
        return object.name();
      }
    };

    reminderActionTypeList = new ArrayList<ReminderActionType>();
    for (final ReminderActionType type : ReminderActionType.values()) {
      reminderActionTypeList.add(type);
    }

    final IModel<List<ReminderActionType>> reminderActionTypeChoiceModel = new PropertyModel<List<ReminderActionType>>(this, "reminderActionTypeList");
    final IModel<ReminderActionType> reminderActionActiveModel = new PropertyModel<ReminderActionType>(data, "reminderActionType");
    final DropDownChoicePanel<ReminderActionType> reminderActionTypeChoice = new DropDownChoicePanel<ReminderActionType>(reminderPanel.getDropDownChoiceId(), reminderActionActiveModel,
        reminderActionTypeChoiceModel, reminderActionTypeRenderer, false);
    reminderActionTypeChoice.getDropDownChoice().add(new AjaxFormComponentUpdatingBehavior("onChange"){

      private static final long serialVersionUID = 1L;

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

  /**
   * @see org.apache.wicket.Component#onRender()
   */
  @Override
  protected void onRender()
  {

  }
}