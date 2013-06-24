/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSecuredPage;

/**
 * @author Billy Duong (b.duong@micromata.de)
 * 
 */
public class SkillTreePage extends AbstractSecuredPage
{

  private static final long serialVersionUID = -3902220283833390881L;

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
  }

  protected void onListViewSubmit() {
    if (skillListPage != null) {
      setResponsePage(skillListPage);
    } else {
      setResponsePage(new SkillListPage(this, getPageParameters()));
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
