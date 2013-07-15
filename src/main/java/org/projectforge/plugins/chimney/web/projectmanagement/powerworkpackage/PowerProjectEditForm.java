/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.hibernate.Hibernate;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.gantt.GanttScheduledActivity;
import org.projectforge.plugins.chimney.gantt.IScheduler;
import org.projectforge.plugins.chimney.utils.date.InconsistentFixedDatesException;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.ProjectDao;
import org.projectforge.plugins.chimney.web.JodaDateModel;
import org.projectforge.plugins.chimney.web.components.I18nEnumReadonlyModel;
import org.projectforge.task.TaskStatus;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.JodaDatePanel;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.InputPanel;
import org.projectforge.web.wicket.flowlayout.TextAreaPanel;

public class PowerProjectEditForm extends AbstractEditForm<ProjectDO, PowerProjectEditPage>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PowerProjectEditForm.class);

  private static final long serialVersionUID = -5898477856544440400L;

  @SpringBean
  private WbsActivityDao wbsActivityDao;

  @SpringBean
  private ProjectDao projectDao;

  @SpringBean
  private IScheduler scheduler;

  @SpringBean
  private UserGroupCache userGroupCache;

  private WbsActivityDO activity;

  public PowerProjectEditForm(final PowerProjectEditPage parentPage, final ProjectDO data)
  {
    super(parentPage, data);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newGridPanel();
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.chimney.editwbsnode.title"));
      final RequiredMaxLengthTextField field = new RequiredMaxLengthTextField(InputPanel.WICKET_ID,
          new PropertyModel<String>(data, "title"));
      fs.add(field);
      field.add(StringValidator.minimumLength(3));
      field.add(new IValidator<String>() {
        @Override
        public void validate(final IValidatable<String> validatable)
        {
          data.setTitle(validatable.getValue());
          if (StringUtils.isNotEmpty(data.getTitle()) == true && projectDao.doesTitleAlreadyExist(data) == true) {
            field.error(getString("plugins.chimney.errors.titleDoesAlreadyExist"));
          }
        }
      });
      WicketUtils.setFocus(field);
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.chimney.wbs.wbscode"));
      final RequiredMaxLengthTextField field = new RequiredMaxLengthTextField(InputPanel.WICKET_ID, new PropertyModel<String>(data,
          "wbsCode"));
      fs.add(field);
    }
    { // Responsible user:
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.chimney.editwbsnode.responsible"));
      PFUserDO responsibleUser = data.getResponsibleUser();
      if (Hibernate.isInitialized(responsibleUser) == false) {
        responsibleUser = userGroupCache.getUser(responsibleUser.getId());
        data.setResponsibleUser(responsibleUser);
      }
      final UserSelectPanel responsibleUserSelectPanel = new UserSelectPanel(fs.newChildId(), new PropertyModel<PFUserDO>(data,
          "responsibleUser"), parentPage, "responsibleUserId");
      fs.add(responsibleUserSelectPanel);
      responsibleUserSelectPanel.init();
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.chimney.editwbsnode.status"));
      fs.add(new Label(fs.newChildId(), new I18nEnumReadonlyModel(new PropertyModel<TaskStatus>(data, "status"), this)));
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.chimney.editwbsnode.shortdesc"));
      fs.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(data, "shortDescription")));
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.chimney.powerproject.schedulechoice"));
      fs.add(new JodaDatePanel(fs.newChildId(), getBeginDateModel()));
    }
    if (isNew() == false) {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.chimney.editresourceassignment.endDate"));
      String end = "";
      try {
        scheduler.schedule(getActivity());
        final GanttScheduledActivity result = scheduler.getResult(getActivity());
        end = DateTimeFormatter.instance().getFormattedDate(result.getEnd());
      } catch (final Exception e) {
        log.error(e.getMessage(), e);
      }
      fs.add(new Label(fs.newChildId(), new I18nEnumReadonlyModel(new PropertyModel<TaskStatus>(data, "status"), this)));
      add(new Label("end", end));

    }

  }

  WbsActivityDO getActivity()
  {
    if (activity == null) {
      if (isNew() == true) {
        this.activity = new WbsActivityDO();
      } else {
        this.activity = wbsActivityDao.getByOrCreateFor(data);
      }
    }
    return activity;
  }

  protected IModel<DateMidnight> getBeginDateModel()
  {
    return new JodaDateModel<WbsActivityDO>(Model.of(getActivity())) {
      private static final long serialVersionUID = 1L;

      @Override
      protected DateTime getDateTime()
      {
        return getModelObject().getFixedBeginDate();
      }

      @Override
      protected void setDateTime(final DateTime dateTime)
      {
        try {
          getModelObject().setFixedBeginDate(dateTime);
        } catch (final InconsistentFixedDatesException ex) {
          error(getString("plugins.chimney.errors.beginenddateinconsistent"));
        }
      }
    };
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  // @Override
  // public void renderHead(final IHeaderResponse response)
  // {
  // super.renderHead(response);
  // response.render(JavaScriptHeaderItem.forScript(
  // "  $(document).ready(function() { $(\"#progressSlider\").slider({ max: 100, min: 0, value: "
  // + data.getProgress()
  // + ", "
  // + "slide: function(event, ui) {  $(\"#p_progress\").val ( ui.value ); } }); });", "progresssliderscript"));
  // }
}
