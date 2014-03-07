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
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 * 
 */
public class InviteeEditPage extends AbstractEditPage<InviteeDO, InviteeEditForm, InviteeDao> implements ISelectCallerPage
{

  private static final long serialVersionUID = 6051980938679738200L;

  private static final Logger log = Logger.getLogger(InviteeEditPage.class);

  public static final String I18N_KEY_PREFIX = "plugins.skillmatrix.skilltraining";

  @SpringBean(name = "inviteeDao")
  private InviteeDao inviteeDao;

  /**
   * @param parameters
   */
  public InviteeEditPage(final PageParameters parameters)
  {
    super(parameters, I18N_KEY_PREFIX);
    init();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected InviteeDao getBaseDao()
  {
    return inviteeDao;
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
   * @see org.projectforge.web.wicket.AbstractEditPage#newEditForm(org.projectforge.web.wicket.AbstractEditPage,
   *      org.projectforge.core.AbstractBaseDO)
   */
  @Override
  protected InviteeEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final InviteeDO data)
  {
    return new InviteeEditForm(this, data);
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Object)
   */
  @Override
  public void select(final String property, final Object selectedValue)
  {
    //    if ("skillId".equals(property) == true) {
    //      trainingDao.setSkill(getData(), (Integer) selectedValue);
    //    } else {
    //      log.error("Property '" + property + "' not supported for selection.");
    //    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  @Override
  public void unselect(final String property)
  {
    //    if ("skillId".equals(property) == true) {
    //      trainingDao.setSkill(getData(), null);
    //    } else {
    //      log.error("Property '" + property + "' not supported for selection.");
    //    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  @Override
  public void cancelSelection(final String property)
  {
    // Do nothing
  }
}
