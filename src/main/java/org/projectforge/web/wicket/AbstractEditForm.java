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

package org.projectforge.web.wicket;

import org.apache.log4j.Logger;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.web.scripting.ScriptEditForm;
import org.projectforge.web.timesheet.TimesheetEditForm;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public abstract class AbstractEditForm<O extends AbstractBaseDO< ? >, P extends AbstractEditPage< ? , ? , ? >> extends AbstractSecuredForm<O, P>
{
  private static final long serialVersionUID = -6707610179583359099L;

  protected O data;

  protected WebMarkupContainer buttonPanel;

  protected WebMarkupContainer buttonCell;

  protected WebMarkupContainer bottomRows;

  private Button cancelButton;

  private Button createButton;

  protected SingleButtonPanel createButtonPanel;

  private Button updateButton;

  protected SingleButtonPanel updateButtonPanel;

  protected SingleButtonPanel deleteButtonPanel;

  protected SingleButtonPanel markAsDeletedButtonPanel;

  private Button undeleteButton;

  protected SingleButtonPanel undeleteButtonPanel;

  /**
   * Change this value if the number of columns of your form table differ.
   */
  protected int colspan = 2;

  public AbstractEditForm(P parentPage, O data)
  {
    super(parentPage);
    this.data = data;
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    buttonCell = new WebMarkupContainer("buttonCell") {
      public boolean isTransparentResolver()
      {
        return true;
      }

      protected void onComponentTag(ComponentTag tag)
      {
        tag.put("colspan", colspan - 1);
      }
    };
    add(buttonCell);
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));

    addButtonPanel();

    updateButton = new Button("button", new Model<String>(getString("update"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.update();
      }
    };
    updateButtonPanel = new SingleButtonPanel("update", updateButton);
    add(updateButtonPanel);
    createButton = new Button("button", new Model<String>(getString("create"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.create();
      }
    };
    createButtonPanel = new SingleButtonPanel("create", createButton);
    add(createButtonPanel);
    cancelButton = new Button("button", new Model<String>(getString("cancel"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.cancel();
      }
    };
    cancelButton.setDefaultFormProcessing(false); // No validation of the
    final SingleButtonPanel cancelButtonPanel = new SingleButtonPanel("cancel", cancelButton);
    add(cancelButtonPanel);
    undeleteButton = new Button("button", new Model<String>(getString("undelete"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.undelete();
      }
    };
    undeleteButtonPanel = new SingleButtonPanel("undelete", undeleteButton);
    add(undeleteButtonPanel);
    final Button markAsDeletedButton = new Button("button", new Model<String>(getString("markAsDeleted"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.markAsDeleted();
      }
    };
    markAsDeletedButton.add(new SimpleAttributeModifier("onclick", "return showDeleteQuestionDialog();"));
    markAsDeletedButtonPanel = new SingleButtonPanel("markAsDeleted", markAsDeletedButton);
    add(markAsDeletedButtonPanel);
    final Button deleteButton = new Button("button", new Model<String>(getString("delete"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.delete();
      }
    };
    deleteButton.add(new SimpleAttributeModifier("onclick", "return showDeleteQuestionDialog();"));
    deleteButtonPanel = new SingleButtonPanel("delete", deleteButton);
    deleteButton.setDefaultFormProcessing(false);
    add(deleteButtonPanel);
    final Button resetButton = new Button("button", new Model<String>(getString("reset"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.reset();
      }
    };
    final SingleButtonPanel resetButtonPanel = new SingleButtonPanel("reset", resetButton);
    resetButtonPanel.setVisible(false);
    add(resetButtonPanel);
    updateButtonVisibility();

    addBottomRows();
  }

  /**
   * Sets the visibility of buttons update, create and markAsDeleted in dependency of the isNew() function. Currently used by
   * TimesheetEdit's clone function for redraw the buttons correctly after clone.
   */
  public void updateButtonVisibility()
  {
    @SuppressWarnings("unchecked")
    final BaseDao<O> baseDao = (BaseDao<O>) parentPage.getBaseDao();
    if (isNew() == true) {
      updateButtonPanel.setVisible(false);
      undeleteButtonPanel.setVisible(false);
      markAsDeletedButtonPanel.setVisible(false);
      deleteButtonPanel.setVisible(false);
      createButtonPanel.setVisible(baseDao.hasInsertAccess());
      if (createButtonPanel.isVisible() == true) {
        setDefaultButton(createButton);
      } else {
        setDefaultButton(cancelButton);
      }
    } else {
      createButtonPanel.setVisible(false);
      if (getData().isDeleted() == true) {
        undeleteButtonPanel.setVisible(baseDao.hasUpdateAccess(getData(), getData(), false));
        if (undeleteButtonPanel.isVisible() == true) {
          setDefaultButton(undeleteButton);
        }
        markAsDeletedButtonPanel.setVisible(false);
        deleteButtonPanel.setVisible(false);
        updateButtonPanel.setVisible(false);
      } else {
        undeleteButtonPanel.setVisible(false);
        if (parentPage.getBaseDao().isHistorizable() == true) {
          deleteButtonPanel.setVisible(false);
          markAsDeletedButtonPanel.setVisible(baseDao.hasDeleteAccess(getData(), getData(), false));
        } else {
          deleteButtonPanel.setVisible(baseDao.hasDeleteAccess(getData(), getData(), false));
          markAsDeletedButtonPanel.setVisible(false);
        }
        updateButtonPanel.setVisible(baseDao.hasUpdateAccess(getData(), getData(), false));
        if (updateButtonPanel.isVisible() == true) {
          setDefaultButton(updateButton);
        } else {
          setDefaultButton(cancelButton);
        }
      }
    }
  }

  /**
   * Override this method if you need additional buttons. Example in {@link TimesheetEditForm#addButtonPanel()}.
   */
  protected void addButtonPanel()
  {
    buttonPanel = new WebMarkupContainer("buttonPanel");
    buttonPanel.setVisible(false);
    buttonCell.add(buttonPanel);
  }

  /**
   * Override this for additional rows below the buttons. Example in {@link ScriptEditForm#addBottomRows()}.
   */
  protected void addBottomRows()
  {
    bottomRows = new WebMarkupContainer("bottomRows");
    bottomRows.setVisible(false);
    add(bottomRows);
  }

  /**
   * @return true, if id of data is null (id not yet exists).
   */
  public boolean isNew()
  {
    return data.getId() == null;
  }

  public O getData()
  {
    return this.data;
  }

  /** This class uses the logger of the extended class. */
  protected abstract Logger getLogger();
}
