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

package org.projectforge.web.scripting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.scripting.ScriptDO;
import org.projectforge.scripting.ScriptParameter;
import org.projectforge.scripting.ScriptParameterType;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.calendar.QuickSelectPanel;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.SingleButtonPanel;


public class ScriptExecuteForm extends AbstractForm<ScriptDO, ScriptExecutePage>
{
  private static final long serialVersionUID = -8371629527384652778L;

  @SpringBean(name = "taskDao")
  private TaskDao taskDao;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  protected ScriptDO data;

  protected transient GroovyResult groovyResult;

  protected List<ScriptParameter> scriptParameters;

  protected RepeatingView parametersView;

  protected DatePanel[] datePanel1 = new DatePanel[5];

  protected DatePanel[] datePanel2 = new DatePanel[5];

  protected QuickSelectPanel[] quickSelectPanel = new QuickSelectPanel[5];

  protected boolean refresh;

  protected RecentScriptCalls recentScriptCalls;

  public ScriptExecuteForm(final ScriptExecutePage parentPage, final ScriptDO data)
  {
    super(parentPage);
    this.data = data;
    loadParameters();
  }

  private void loadParameters()
  {
    scriptParameters = new ArrayList<ScriptParameter>();
    addParameter(data.getParameter1Name(), data.getParameter1Type());
    addParameter(data.getParameter2Name(), data.getParameter2Type());
    addParameter(data.getParameter3Name(), data.getParameter3Type());
    addParameter(data.getParameter4Name(), data.getParameter4Type());
    addParameter(data.getParameter5Name(), data.getParameter5Type());
  }

  private void addParameter(final String parameterName, final ScriptParameterType type)
  {
    if (StringUtils.isNotBlank(parameterName) == true && type != null) {
      scriptParameters.add(new ScriptParameter(parameterName, type));
    }
  }

  private void prefillParameters()
  {
    RecentScriptCalls recents = parentPage.getRecentScriptCalls();
    final ScriptCallData scriptCallData = recents.getScriptCallData(data.getName());
    if (scriptCallData != null && scriptCallData.getScriptParameter() != null) {
      for (final ScriptParameter recentParameter : scriptCallData.getScriptParameter()) {
        for (final ScriptParameter parameter : scriptParameters) {
          if (StringUtils.equals(parameter.getParameterName(), recentParameter.getParameterName()) == true) {
            if (parameter.getType() == recentParameter.getType()) {
              // Copy only if type matches
              if (parameter.getType() == ScriptParameterType.TASK) {
                final TaskDO task = taskDao.getById(recentParameter.getIntValue());
                parameter.setTask(task);
              } else if (parameter.getType() == ScriptParameterType.USER) {
                final PFUserDO user = userDao.getById(recentParameter.getIntValue());
                parameter.setUser(user);
              } else {
                parameter.setValue(recentParameter.getValue());
              }
            }
            break;
          } // if parameterNames are equal.
        } // for script parameters
      } // for recent parameters.
    } // if scriptCallData is given
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    prefillParameters();
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    add(new Label("name", data.getName()));
    add(new Label("description", data.getDescription()));
    final Button backButton = new Button("button", new Model<String>(getString("back"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.cancel();
      }
    };
    backButton.add(WebConstants.BUTTON_CLASS_CANCEL);
    backButton.setDefaultFormProcessing(false);
    final SingleButtonPanel backButtonPanel = new SingleButtonPanel("back", backButton);
    add(backButtonPanel);
    final Button executeButton = new Button("button", new Model<String>(getString("execute"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.execute();
      }
    };
    executeButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    setDefaultButton(executeButton);
    final SingleButtonPanel executeButtonPanel = new SingleButtonPanel("execute", executeButton);
    add(executeButtonPanel);
    final MarkupContainer resultRow = new WebMarkupContainer("scriptResultRow") {
      @Override
      public boolean isVisible()
      {
        return groovyResult != null;
      }
    };
    add(resultRow);
    final Label scriptResultLabel = new Label("scriptResult", new Model<String>() {
      @Override
      public String getObject()
      {
        return groovyResult != null ? groovyResult.getResultAsHtmlString() : "";
      }
    });
    scriptResultLabel.setEscapeModelStrings(false);
    resultRow.add(scriptResultLabel);
    refreshParametersView();
  }

  @Override
  public void onBeforeRender()
  {
    if (refresh == true) {
      data = parentPage.loadScript();
      loadParameters();
      prefillParameters();
      refreshParametersView();
      parentPage.refreshSourceCode();
    }
    super.onBeforeRender();
  }

  protected void refreshParametersView()
  {
    if (parametersView != null) {
      remove(parametersView);
    }
    parametersView = new RepeatingView("parameters");
    add(parametersView);
    int index = 0;
    boolean focusSet = false;
    for (final ScriptParameter parameter : scriptParameters) {
      WebMarkupContainer item = new WebMarkupContainer(parametersView.newChildId());
      parametersView.add(item);
      final Label parameterNameLabel = new Label("name", StringUtils.capitalize(parameter.getParameterName()));
      item.add(parameterNameLabel);
      TextField< ? > parameterValueField = null;
      WebMarkupContainer panel1 = null;
      WebMarkupContainer panel2 = null;
      WebMarkupContainer panel3 = null;
      if (parameter.getType() == ScriptParameterType.INTEGER) {
        parameterValueField = new TextField<Integer>("value", new PropertyModel<Integer>(parameter, "intValue"));
      } else if (parameter.getType() == ScriptParameterType.STRING) {
        parameterValueField = new TextField<String>("value", new PropertyModel<String>(parameter, "stringValue"));
      } else if (parameter.getType() == ScriptParameterType.DECIMAL) {
        parameterValueField = new TextField<BigDecimal>("value", new PropertyModel<BigDecimal>(parameter, "decimalValue"));
      } else if (parameter.getType() == ScriptParameterType.DATE || parameter.getType() == ScriptParameterType.TIME_PERIOD) {
        final String property = parameter.getType() == ScriptParameterType.TIME_PERIOD ? "timePeriodValue.fromDate" : "dateValue";
        datePanel1[index] = new DatePanel("panel", new PropertyModel<Date>(parameter, property), DatePanelSettings.get().withCallerPage(
            parentPage).withSelectProperty("date:" + index).withSelectPeriodMode(true));
        item.add(datePanel1[index]);
        panel1 = datePanel1[index];
        if (parameter.getType() == ScriptParameterType.TIME_PERIOD) {
          datePanel2[index] = new DatePanel("panel2", new PropertyModel<Date>(parameter, "timePeriodValue.toDate"), DatePanelSettings.get()
              .withCallerPage(parentPage).withSelectProperty("date2:" + index).withSelectPeriodMode(true));
          item.add(datePanel2[index]);
          panel2 = datePanel2[index];
          quickSelectPanel[index] = new QuickSelectPanel("panel3", parentPage, "quickSelect:" + index, datePanel1[index]);
          item.add(quickSelectPanel[index]);
          quickSelectPanel[index].init();
          panel3 = quickSelectPanel[index];
        }
      } else if (parameter.getType() == ScriptParameterType.TASK) {
        final TaskSelectPanel taskSelectPanel = new TaskSelectPanel("panel", new PropertyModel<TaskDO>(parameter, "task"), parentPage,
            "taskId:" + index);
        item.add(taskSelectPanel);
        taskSelectPanel.init();
        taskSelectPanel.setRequired(true);
        panel1 = taskSelectPanel;
      } else if (parameter.getType() == ScriptParameterType.USER) {
        final UserSelectPanel userSelectPanel = new UserSelectPanel("panel", new PropertyModel<PFUserDO>(parameter, "user"), parentPage,
            "userId:" + index);
        item.add(userSelectPanel);
        userSelectPanel.init();
        userSelectPanel.setRequired(true);
        panel1 = userSelectPanel;
      } else {
        throw new UnsupportedOperationException("Parameter type: " + parameter.getType() + " not supported.");
      }
      if (focusSet == false) {
        if (parameterValueField != null) {
          parameterValueField.add(new FocusOnLoadBehavior());
          focusSet = true;
        } else if (panel1 instanceof DatePanel) {
          ((DatePanel)panel1).setFocus();
          focusSet = true;
        }
      }
      if (parameterValueField == null) {
        parameterValueField = new TextField<String>("value");
        parameterValueField.setVisible(false);
      }
      item.add(parameterValueField);
      if (panel1 == null) {
        panel1 = new WebMarkupContainer("panel");
        panel1.setVisible(false);
        item.add(panel1);
      }
      if (panel2 == null) {
        panel2 = new WebMarkupContainer("panel2");
        panel2.setVisible(false);
        item.add(panel2);
      }
      if (panel3 == null) {
        panel3 = new WebMarkupContainer("panel3");
        panel3.setVisible(false);
        item.add(panel3);
      }
      index++;
    }
    refresh = false;
  }

  protected void setScriptResult(GroovyResult result)
  {
    this.groovyResult = result;
  }
}
