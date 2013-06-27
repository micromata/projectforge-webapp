/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WicketUtils;

/**
 * @author Billy Duong (b.duong@micromata.de)
 * 
 */
public class SkillTreePage extends AbstractSecuredPage
{

  private static final long serialVersionUID = -3902220283833390881L;

  public static final String USER_PREFS_KEY_OPEN_SKILLS = "openSkills";

  public static final String I18N_KEY_SKILLTREE_INFO = "plugins.skillmatrix.skilltree.info";

  private SkillTreeForm form;

  private ISelectCallerPage caller;

  private String selectProperty;

  private SkillListPage skillListPage;

  /**
   * @param parameters
   */
  public SkillTreePage(final PageParameters parameters)
  {
    super(parameters);
    init();
  }

  /**
   * Called if the user clicks on button "tree view".
   * @param skillListPage
   * @param parameters
   */
  public SkillTreePage(final SkillListPage skillListPage, final PageParameters parameters)
  {
    super(parameters);
    this.skillListPage = skillListPage;
    init();
  }

  public SkillTreePage(final ISelectCallerPage caller, final String selectProperty) {
    super(new PageParameters());
    this.caller = caller;
    this.selectProperty = selectProperty;
    init();
  }

  private void init()
  {
    form = new SkillTreeForm(this);
    body.add(form);
    form.init();
    final SkillTreeBuilder skillTreeBuilder = new SkillTreeBuilder().setCaller(caller).setSelectProperty(selectProperty);
    form.add(skillTreeBuilder.createTree("tree", this, form.getSearchFilter()));

    body.add(new Label("info", new Model<String>(getString(I18N_KEY_SKILLTREE_INFO))));
  }

  public void refresh()
  {
    form.getSearchFilter().resetMatch();
  }

  /**
   * @return true, if this page is called for selection by a caller otherwise false.
   */
  public boolean isSelectMode()
  {
    return this.caller != null;
  }

  protected void onSearchSubmit()
  {
    refresh();
  }


  protected void onResetSubmit()
  {
    form.getSearchFilter().reset();
    refresh();
    form.clearInput();
  }

  protected void onListViewSubmit() {
    if (skillListPage != null) {
      setResponsePage(skillListPage);
    } else {
      setResponsePage(new SkillListPage(this, getPageParameters()));
    }
  }

  protected void onCancelSubmit()
  {
    if (isSelectMode() == true) {
      WicketUtils.setResponsePage(this, caller);
      caller.cancelSelection(selectProperty);
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    // TODO I18N KEY
    return "Test";
  }

}
