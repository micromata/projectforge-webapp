/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.wicket.AbstractEditPage;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 *
 */
public class SkillMatrixEditPage extends AbstractEditPage<SkillDO, SkillMatrixEditForm, SkillDao>
{
  private static final long serialVersionUID = 1403978551875901644L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SkillMatrixEditPage.class);

  @SpringBean(name = "skillDao")
  private SkillDao skillDao;

  /**
   * @param parameters
   */
  public SkillMatrixEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.skillmatrix");
    init();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected SkillDao getBaseDao()
  {
    return skillDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#newEditForm(org.projectforge.web.wicket.AbstractEditPage, org.projectforge.core.AbstractBaseDO)
   */
  @Override
  protected SkillMatrixEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final SkillDO data)
  {
    return new SkillMatrixEditForm(this, data);
  }

}
