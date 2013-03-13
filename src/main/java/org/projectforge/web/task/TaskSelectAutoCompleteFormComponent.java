/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskNode;
import org.projectforge.task.TaskTree;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;

/**
 * @author TODO
 * 
 */
public abstract class TaskSelectAutoCompleteFormComponent extends PFAutoCompleteTextField<TaskDO>
{

  private static final long serialVersionUID = 2278347191215880396L;

  @SpringBean(name = "taskDao")
  private TaskDao taskDao;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  private TaskDO taskDo;

  /**
   * @param id
   */
  public TaskSelectAutoCompleteFormComponent(final String id)
  {
    super(id, null);
    setModel(new PropertyModel<TaskDO>(this, "taskDo"));
    getSettings().withLabelValue(true).withMatchContains(true).withMinChars(2).withAutoSubmit(false);
    add(AttributeModifier.append("onkeypress", "if ( event.which == 13 ) { return false; }"));
    add(new AjaxFormComponentUpdatingBehavior("onChange") {
      private static final long serialVersionUID = 3681828654557441560L;

      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        // just update the model
      }
    });
  }

  /**
   * @see org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField#getChoices(java.lang.String)
   */
  @Override
  protected List<TaskDO> getChoices(final String input)
  {
    final BaseSearchFilter filter = new BaseSearchFilter();
    filter.setSearchFields("title", "taskpath");
    filter.setSearchString(input);
    final List<TaskDO> list = taskDao.getList(filter);
    final List<TaskDO> choices = new ArrayList<TaskDO>();
    // removing nodes without kost2 list.
    for (final TaskDO t : list) {
      if (taskTree.getKost2List(t.getId()) != null) {
        choices.add(t);
      }
    }
    return choices;
  }

  @Override
  protected String formatValue(final TaskDO value)
  {
    if (value == null) {
      return "";
    }
    return "" + value.getId();
  }

  @Override
  protected String formatLabel(final TaskDO value)
  {
    if (value == null) {
      return "";
    }

    return createPath(value.getId()) + value.getTitle();
  }

  /**
   * create path to root
   * 
   * @return
   */
  private String createPath(final Integer taskId)
  {
    String path = "";
    final List<TaskNode> nodeList = taskTree.getPathToRoot(taskId);
    for (final TaskNode node : nodeList) {
      if (node.getId() != taskId) {
        path += node.getTask().getTitle() + " | ";
      }
    }
    return path;
  }

  protected void notifyChildren()
  {
    final AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
    if (target != null) {
      onModelSelected(target, taskDo);
    }
  }

  /**
   * Hook method which is called when the model is changed with a valid durin an ajax call
   * 
   * @param target
   * @param taskDo
   */
  protected abstract void onModelSelected(final AjaxRequestTarget target, TaskDO taskDo);

  @SuppressWarnings({ "unchecked", "rawtypes"})
  @Override
  public <C> IConverter<C> getConverter(final Class<C> type)
  {
    return new IConverter() {
      private static final long serialVersionUID = -7729322118285105516L;

      @Override
      public Object convertToObject(final String value, final Locale locale)
      {
        if (StringUtils.isEmpty(value) == true) {
          getModel().setObject(null);
          notifyChildren();
          return null;
        }
        try {
          final TaskDO task = taskTree.getTaskById(Integer.valueOf(value));
          if (task == null
              || taskTree.getKost2List(task.getId()) == null
              ) {
            error(getString("timesheet.error.invalidTaskId"));
            return null;
          }
          getModel().setObject(task);
          notifyChildren();
          return task;
        } catch (final NumberFormatException e) {
          // just ignore the NumberFormatException, because this could happen during wrong inputs
          return null;
        }
      }

      @Override
      public String convertToString(final Object value, final Locale locale)
      {
        if (value == null) {
          return "";
        }
        final TaskDO task = (TaskDO) value;
        return task.getTitle();
      }
    };
  }
}
