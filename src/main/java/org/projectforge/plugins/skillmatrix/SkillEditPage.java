/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.skillmatrix;

import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.wicket.AbstractEditPage;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 *
 */
public class SkillEditPage extends AbstractEditPage<SkillDO, SkillEditForm, SkillDao>
{
  private static final long serialVersionUID = 4317454400876214258L;

  private static final Logger log = Logger.getLogger(SkillEditPage.class);

  @SpringBean(name = "skillDao")
  private SkillDao skillDao;

  /**
   * @param parameters
   * @param i18nPrefix
   */
  public SkillEditPage(final PageParameters parameters)
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
  protected SkillEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final SkillDO data)
  {
    return new SkillEditForm(this, data);
  }


}
