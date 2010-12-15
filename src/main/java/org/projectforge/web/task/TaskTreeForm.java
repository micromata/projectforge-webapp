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

package org.projectforge.web.task;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.TooltipImage;


public class TaskTreeForm extends AbstractForm<TaskFilter, TaskTreePage>
{
  private static final long serialVersionUID = -203572415793301622L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskTreeForm.class);

  private TaskFilter searchFilter;

  private SingleButtonPanel cancelButtonPanel;

  private SingleButtonPanel resetButtonPanel;
  
  private SingleButtonPanel listViewButtonPanel;

  private SingleButtonPanel searchButtonPanel;

  private class RefreshCheckBox extends CheckBox
  {
    private static final long serialVersionUID = -6304659091210189380L;

    RefreshCheckBox(final String componentId, final String property)
    {
      super(componentId, new PropertyModel<Boolean>(getSearchFilter(), property));
    }

    @Override
    public void onSelectionChanged()
    {
      super.onSelectionChanged();
      parentPage.refresh();
    }

    @Override
    protected boolean wantOnSelectionChangedNotifications()
    {
      return true;
    }
  }

  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    add(new RefreshCheckBox("notOpenedCheckBox", "notOpened"));
    add(new RefreshCheckBox("openedCheckBox", "opened"));
    add(new RefreshCheckBox("closedCheckBox", "closed"));
    add(new RefreshCheckBox("deletedCheckBox", "deleted"));
    add(new RefreshCheckBox("ajaxSupportCheckBox", "ajaxSupport"));
    setModel(new CompoundPropertyModel<TaskFilter>(searchFilter));
    final Component searchField = new TextField<String>("searchString", new PropertyModel<String>(searchFilter, "searchString"));
    searchField.add(new FocusOnLoadBehavior());
    WicketUtils.addTooltip(searchField, getString("search.string.info.title"), getParentPage().getSearchToolTip());
    add(searchField);
    final ExternalLink handbuchVolltextsucheLink = new ExternalLink("handbuchVolltextsucheLink",
        getUrl("/secure/doc/Handbuch.html#label_volltextsuche"));
    add(handbuchVolltextsucheLink);
    handbuchVolltextsucheLink.add(new TooltipImage("fulltextSearchTooltipImage", getResponse(), WebConstants.IMAGE_HELP,
        getString("tooltip.lucene.link")));
    add(new TooltipImage("ajaxSupportTooltipImage", getResponse(), WebConstants.IMAGE_HELP, getString("task.tree.tooltip.ajaxSupport")));
    final Button searchButton = new Button("button", new Model<String>(getString("search"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onSearchSubmit();
      }
    };
    
    searchButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    
    searchButtonPanel = new SingleButtonPanel("search", searchButton);
    add(searchButtonPanel);
    final Button resetButton = new Button("button", new Model<String>(getString("reset"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onResetSubmit();
      }
    };
    resetButton.add(WebConstants.BUTTON_CLASS_RESET);
    resetButton.setDefaultFormProcessing(false);
    
    resetButtonPanel = new SingleButtonPanel("reset", resetButton);
    add(resetButtonPanel);
    final Button listViewButton = new Button("button", new Model<String>(getString("listView"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onListViewSubmit();
      }
    };
    
    listViewButton.add(WebConstants.BUTTON_CLASS_NOBUTTON);
    listViewButtonPanel = new SingleButtonPanel("listView", listViewButton);
    
    
    final Button cancelButton = new Button("button", new Model<String>(getString("cancel"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onCancelSubmit();
      }
    };
    cancelButton.setDefaultFormProcessing(false);
    cancelButton.add(new SimpleAttributeModifier("class", "cancel"));
    cancelButtonPanel = new SingleButtonPanel("cancel", cancelButton);
    add(cancelButtonPanel);
    add(listViewButtonPanel);
    setDefaultButton(searchButton);
    setComponentsVisibility();
  }

  public TaskTreeForm(TaskTreePage parentPage)
  {
    super(parentPage);
  }

  protected void setComponentsVisibility()
  {
    if (parentPage.isSelectMode() == false) {
      // Show cancel button only in select mode.
      cancelButtonPanel.setVisible(false);
    }
    searchButtonPanel.setVisible(true);
    resetButtonPanel.setVisible(true);
  }

  public TaskFilter getSearchFilter()
  {
    if (this.searchFilter == null) {
      final Object filter = getParentPage().getUserPrefEntry(TaskListForm.class.getName() + ":Filter");
      if (filter != null) {
        try {
          this.searchFilter = (TaskFilter) filter;
        } catch (ClassCastException ex) {
          // Probably a new software release results in an incompability of old and new filter format.
          log.info("Could not restore filter from user prefs: (old) filter type "
              + filter.getClass().getName()
              + " is not assignable to (new) filter type TaskFilter (OK, probably new software release).");
        }
      }
    }
    if (this.searchFilter == null) {
      this.searchFilter = new TaskFilter();
      getParentPage().putUserPrefEntry(TaskListForm.class.getName() + ":Filter", this.searchFilter, true);
    }
    return this.searchFilter;
  }
}
