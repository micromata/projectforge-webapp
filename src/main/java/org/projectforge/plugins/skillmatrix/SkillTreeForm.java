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
import org.projectforge.web.wicket.AbstractForm;
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

  /**
   * @param parentPage
   */
  public SkillTreeForm(final SkillTreePage parentPage)
  {
    super(parentPage);
    init();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractForm#init()
   */
  @Override
  protected void init()
  {
    super.init();

    // actionButtons = new MyComponentsRepeater<Component>("actionButtons");
    // add(actionButtons.getRepeatingView());
    // {
    // @SuppressWarnings("serial")
    // final Button skillListButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("listView")) {
    // @Override
    // public void onSubmit()
    // {
    // getParentPage().onListViewSubmit();
    // }
    // };
    // final SingleButtonPanel skillListButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), skillListButton, "List View",
    // SingleButtonPanel.NORMAL);
    // actionButtons.add(skillListButtonPanel);
    // }

  }

  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
    actionButtons.render();
  }

}
