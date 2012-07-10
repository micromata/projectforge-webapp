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
public class SkillRatingEditPage extends AbstractEditPage<SkillRatingDO, SkillRatingEditForm, SkillRatingDao>
{
  private static final long serialVersionUID = 1403978551875901644L;

  private static final Logger log = Logger.getLogger(SkillRatingEditPage.class);

  @SpringBean(name = "skillRatingDao")
  private SkillRatingDao skillRatingDao;

  /**
   * @param parameters
   */
  public SkillRatingEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.skillmatrix");
    init();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected SkillRatingDao getBaseDao()
  {
    return skillRatingDao;
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
  protected SkillRatingEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final SkillRatingDO data)
  {
    return new SkillRatingEditForm(this, data);
  }

}
