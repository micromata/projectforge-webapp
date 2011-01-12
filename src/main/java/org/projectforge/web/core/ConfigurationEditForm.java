/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.core;

import java.math.BigDecimal;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.core.ConfigurationType;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.TimeZonePanel;
import org.projectforge.web.wicket.converter.BigDecimalPercentConverter;

public class ConfigurationEditForm extends AbstractEditForm<ConfigurationDO, ConfigurationEditPage>
{
  public ConfigurationEditForm(ConfigurationEditPage parentPage, ConfigurationDO data)
  {
    super(parentPage, data);
    this.colspan = 2;
  }

  private static final long serialVersionUID = 6156899763199729949L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConfigurationEditForm.class);

  private TaskDO task;

  @SpringBean(name = "taskDao")
  private TaskDao taskDao;

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    add(new Label("parameter", getString(data.getI18nKey())));
    add(new Label("description", new Model<String>(getString("administration.configuration.param." + data.getParameter() + ".description"))));
    final Component textareaField = new MaxLengthTextArea("textValue", new PropertyModel<String>(data, "stringValue"));
    textareaField.setVisible(false);
    add(textareaField);
    final Component valueField;
    Component panel = null;
    if (data.getConfigurationType() == ConfigurationType.INTEGER) {
      valueField = new TextField<Integer>("value", new PropertyModel<Integer>(data, "intValue"));
    } else if (data.getConfigurationType() == ConfigurationType.PERCENT) {
      valueField = new MinMaxNumberField<BigDecimal>("value", new PropertyModel<BigDecimal>(data, "floatValue"), BigDecimal.ZERO,
          NumberHelper.HUNDRED) {
        @Override
        public IConverter getConverter(Class< ? > type)
        {
          return new BigDecimalPercentConverter(true);
        }
      };
    } else if (data.getConfigurationType() == ConfigurationType.STRING) {
      valueField = new MaxLengthTextField("value", new PropertyModel<String>(data, "stringValue"));
      valueField.add(new SimpleAttributeModifier("class", WebConstants.CSS_INPUT_STDTEXT));
    } else if (data.getConfigurationType() == ConfigurationType.TEXT) {
      textareaField.setVisible(true);
      valueField = createInvisibleDummyComponent("value");
    } else if (data.getConfigurationType() == ConfigurationType.TIME_ZONE) {
      panel = new TimeZonePanel("panel", new PropertyModel<TimeZone>(data, "timeZone"));
      add(panel);
      valueField = createInvisibleDummyComponent("value");
    } else if (data.getConfigurationType() == ConfigurationType.TASK) {
      if (data.getTaskId() != null) {
        this.task = taskDao.getById(data.getTaskId());
      }
      final TaskSelectPanel taskSelectPanel = new TaskSelectPanel("panel", new PropertyModel<TaskDO>(this, "task"), parentPage, "taskId");
      panel = taskSelectPanel;
      add(panel);
      taskSelectPanel.init();
      valueField = createInvisibleDummyComponent("value");
    } else {
      throw new UnsupportedOperationException("Parameter of type '" + data.getConfigurationType() + "' not supported.");
    }
    add(valueField);
    if (panel == null) {
      panel = createInvisibleDummyComponent("panel");
      add(panel);
    }
    final Label previewLabel = new Label("previewLabel", new Model<String>() {
      @Override
      public String getObject()
      {
        return getData().getStringValue();
      }
    });
    previewLabel.setEscapeModelStrings(false);
    if (data.getConfigurationType() != ConfigurationType.TEXT) {
      previewLabel.setVisible(false);
    }
    add(previewLabel);
  }

  @SuppressWarnings("serial")
  @Override
  protected void addButtonPanel()
  {
    final Fragment buttonFragment = new Fragment("buttonPanel", "buttonFragment", this);
    buttonFragment.setRenderBodyOnly(true);
    buttonCell.add(buttonFragment);
    final SingleButtonPanel previewButtonPanel = new SingleButtonPanel("preview", new Button("button", new Model<String>(
        getString("preview"))) {
      @Override
      public final void onSubmit()
      {
      }
    });
    buttonFragment.add(previewButtonPanel);
    if (getData().getConfigurationType() != ConfigurationType.TEXT) {
      previewButtonPanel.setVisible(false);
    }
  }

  public TaskDO getTask()
  {
    return task;
  }

  public void setTask(final TaskDO task)
  {
    this.task = task;
    if (task != null) {
      data.setTaskId(task.getId());
    } else {
      data.setTaskId(null);
    }
  }

  public void setTask(final Integer taskId)
  {
    if (taskId != null) {
      setTask(taskDao.getById(taskId));
    } else {
      setTask((TaskDO) null);
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
