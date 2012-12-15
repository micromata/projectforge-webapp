/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.poll;

import org.apache.log4j.Logger;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.web.wicket.AbstractEditForm;

/**
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollEditForm extends AbstractEditForm<PollDO, PollEditPage>
{
  private static final long serialVersionUID = 1268981578238971117L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PollEditForm.class);

  @SpringBean(name = "accessChecker")
  protected AccessChecker accessChecker;

  /**
   * @param parentPage
   * @param data
   */
  public PollEditForm(final PollEditPage parentPage, final PollDO data)
  {
    super(parentPage, data);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#init()
   */
  @Override
  protected void init()
  {
    super.init();
    //    gridBuilder.newGrid8();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
