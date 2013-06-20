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
import org.projectforge.web.wicket.AbstractSecuredPage;

/**
 * @author Billy Duong (b.duong@micromata.de)
 *
 */
public class SkillTreePage extends AbstractSecuredPage
{

  private static final long serialVersionUID = -3902220283833390881L;

  private SkillTreeForm form;

  /**
   * @param parameters
   */
  public SkillTreePage(final PageParameters parameters)
  {
    super(parameters);
    init();
  }

  private void init() {
    // TODO Setup components.
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
