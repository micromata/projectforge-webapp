/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DateTimePanel;
import org.projectforge.web.wicket.components.DateTimePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamEventEditForm extends AbstractEditForm<TeamEventDO, TeamEventEditPage>
{
  private static final long serialVersionUID = -8378262684943803495L;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  @SuppressWarnings("unused")
  private int stopHourOfDay, stopMinute;

  /**
   * @param parentPage
   * @param data
   */
  public TeamEventEditForm(final TeamEventEditPage parentPage, final TeamEventDO data)
  {
    super(parentPage, data);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#init()
   */
  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();

    gridBuilder.newGrid16();
    parentPage.preInit();

    {
      final FieldsetPanel set = gridBuilder.newFieldset("Teamcal");
      final List<TeamCalDO> list = teamCalDao.getFullAccessTeamCals(getUser(), data.getCalendar().getFullAccessGroup());
      final DropDownChoice<TeamCalDO> teamCalDrop = new DropDownChoice<TeamCalDO>(set.getDropDownChoiceId(),
          Model.of(data.getCalendar()), list, getLabeledList(list));
      teamCalDrop.setRequired(true);
      set.add(teamCalDrop);
    }
    {
      final FieldsetPanel set = gridBuilder.newFieldset("Subject");
      final MaxLengthTextField subjectField = new MaxLengthTextField(set.getTextFieldId(), new PropertyModel<String>(data, "subject"));
      subjectField.setRequired(true);
      set.add(subjectField);
    }
    {
      final FieldsetPanel set = gridBuilder.newFieldset("Note");
      final MaxLengthTextArea noteField = new MaxLengthTextArea(set.getTextAreaId(), new PropertyModel<String>(data, "note"));
      set.add(noteField);
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("timePeriod"), true);
      final DateTimePanel startDateTimePanel = new DateTimePanel(fs.newChildId(), new PropertyModel<Date>(data, "startDate"),
          (DateTimePanelSettings) DateTimePanelSettings.get().withSelectStartStopTime(true).withTargetType(java.sql.Timestamp.class)
          .withRequired(true), DatePrecision.MINUTE_15);
      fs.add(startDateTimePanel);
      WicketUtils.addTooltip(startDateTimePanel.getDateField(), new Model<String>() {
        @Override
        public String getObject()
        {
          final StringBuffer buf = new StringBuffer();
          if (data.getStartDate() != null) {
            buf.append(DateHelper.TECHNICAL_ISO_UTC.get().format(data.getStartDate()));
            if (data.getEndDate() != null) {
              buf.append(" - ");
            }
          }
          if (data.getEndDate() != null) {
            buf.append(DateHelper.TECHNICAL_ISO_UTC.get().format(data.getEndDate()));
          }
          return buf.toString();
        }
      });
      fs.add(new DivTextPanel(fs.newChildId(), getString("until")));
      // Stop time
      final DropDownChoice<Integer> stopHourOfDayDropDownChoice = new DropDownChoice<Integer>(fs.getDropDownChoiceId(),
          new PropertyModel<Integer>(this, "stopHourOfDay"), DateTimePanel.getHourOfDayRenderer().getValues(),
          DateTimePanel.getHourOfDayRenderer());
      stopHourOfDayDropDownChoice.setNullValid(false);
      stopHourOfDayDropDownChoice.setRequired(true);
      fs.add(stopHourOfDayDropDownChoice);
      final DropDownChoice<Integer> stopMinuteDropDownChoice = new DropDownChoice<Integer>(fs.getDropDownChoiceId(),
          new PropertyModel<Integer>(this, "stopMinute"), DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15).getValues(),
          DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15));
      stopMinuteDropDownChoice.setNullValid(false);
      stopMinuteDropDownChoice.setRequired(true);
      fs.add(stopMinuteDropDownChoice);
    }
  }

  private LabelValueChoiceRenderer<TeamCalDO> getLabeledList(final List<TeamCalDO> list) {
    final LabelValueChoiceRenderer<TeamCalDO> templateNamesChoiceRenderer = new LabelValueChoiceRenderer<TeamCalDO>();
    for (final TeamCalDO t : list) {
      templateNamesChoiceRenderer.addValue(t, t.getTitle());
    }
    return templateNamesChoiceRenderer;
  }

  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
    final DateHolder stopDateHolder = new DateHolder(data.getEndDate(), DatePrecision.MINUTE_15);
    stopHourOfDay = stopDateHolder.getHourOfDay();
    stopMinute = stopDateHolder.getMinute();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
