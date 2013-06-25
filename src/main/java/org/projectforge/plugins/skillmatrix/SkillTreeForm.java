/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.bootstrap.GridBuilder;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.InputPanel;
import org.projectforge.web.wicket.flowlayout.MyComponentsRepeater;

/**
 * @author Billy Duong (b.duong@micromata.de)
 * 
 */
public class SkillTreeForm extends AbstractForm<SkillFilter, SkillTreePage>
{

  private static final long serialVersionUID = 1227686732149287124L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SkillTreeForm.class);

  private MyComponentsRepeater<Component> actionButtons;

  protected GridBuilder gridBuilder;

  private SkillFilter searchFilter;

  /**
   * @param parentPage
   */
  public SkillTreeForm(final SkillTreePage parentPage)
  {
    super(parentPage);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractForm#init()
   */
  @Override
  protected void init()
  {
    super.init();

    gridBuilder = newGridBuilder(this, "flowform");
    {
      gridBuilder.newSplitPanel(GridSize.COL100);
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("searchFilter"));
      final TextField<String> searchField = new TextField<String>(InputPanel.WICKET_ID, new PropertyModel<String>(getSearchFilter(),
          "searchString"));
      searchField.add(WicketUtils.setFocus());
      fs.add(new InputPanel(fs.newChildId(), searchField));
      // fs.add(new IconPanel(fs.newIconChildId(), IconType.HELP, getString("tooltip.lucene.link")).setOnClickLocation(getRequestCycle(),
      // WebConstants.DOC_LINK_HANDBUCH_LUCENE, true), FieldSetIconPosition.TOP_RIGHT);
    }

    actionButtons = new MyComponentsRepeater<Component>("actionButtons");

    add(actionButtons.getRepeatingView());
    // {
    // @SuppressWarnings("serial")
    // final Button cancelButton = new Button("button", new Model<String>("cancel")) {
    // @Override
    // public final void onSubmit()
    // {
    // getParentPage().onCancelSubmit();
    // }
    // };
    // cancelButton.setDefaultFormProcessing(false);
    // final SingleButtonPanel cancelButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), cancelButton, getString("cancel"),
    // SingleButtonPanel.CANCEL);
    // actionButtons.add(cancelButtonPanel);
    // }
    {
      @SuppressWarnings("serial")
      final Button resetButton = new Button("button", new Model<String>("reset")) {

        @Override
        public final void onSubmit()
        {
          getParentPage().onResetSubmit();
        }
      };
      resetButton.setDefaultFormProcessing(false);
      final SingleButtonPanel resetButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), resetButton, getString("reset"),
          SingleButtonPanel.RESET);
      actionButtons.add(resetButtonPanel);
    }
    {
      @SuppressWarnings("serial")
      final Button skillListButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("listView")) {

        @Override
        public void onSubmit()
        {
          getParentPage().onListViewSubmit();
        }
      };
      final SingleButtonPanel skillListButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), skillListButton, "List View",
          SingleButtonPanel.NORMAL);
      actionButtons.add(skillListButtonPanel);
    }
    {
      @SuppressWarnings("serial")
      final Button searchButton = new Button("button", new Model<String>("search")) {

        @Override
        public final void onSubmit()
        {
          getParentPage().onSearchSubmit();
        }
      };
      final SingleButtonPanel searchButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), searchButton, getString("search"),
          SingleButtonPanel.DEFAULT_SUBMIT);
      actionButtons.add(searchButtonPanel);
      setDefaultButton(searchButton);
    }

  }

  public SkillFilter getSearchFilter()
  {
    if (this.searchFilter == null) {
      final Object filter = getParentPage().getUserPrefEntry(SkillListForm.class.getName() + ":Filter");
      if (filter != null) {
        try {
          this.searchFilter = (SkillFilter) filter;
        } catch (final ClassCastException ex) {
          // Probably a new software release results in an incompability of old and new filter format.
          log.info("Could not restore filter from user prefs: (old) filter type "
              + filter.getClass().getName()
              + " is not assignable to (new) filter type SkillFilter (OK, probably new software release).");
        }
      }
    }
    if (this.searchFilter == null) {
      this.searchFilter = new SkillFilter();
      getParentPage().putUserPrefEntry(SkillListForm.class.getName() + ":Filter", this.searchFilter, true);
    }
    return this.searchFilter;
  }

  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
    actionButtons.render();
  }

  // @SuppressWarnings("serial")
  // private class MyCheckBoxPanel extends CheckBoxPanel
  // {
  // public MyCheckBoxPanel(final String id, final IModel<Boolean> model, final String labelString)
  // {
  // super(id, model, labelString);
  // }
  //
  // @Override
  // protected boolean wantOnSelectionChangedNotifications()
  // {
  // return true;
  // }
  //
  // @Override
  // protected void onSelectionChanged(final Boolean newSelection)
  // {
  // // parentPage.refresh();
  // }
  // }

}
