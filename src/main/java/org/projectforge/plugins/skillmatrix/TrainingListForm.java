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

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.projectforge.web.wicket.AbstractListForm;

/**
 * The list formular for the list view.
 * @author Werner Feder (werner.feder@t-online.de)
 */
public class TrainingListForm extends AbstractListForm<TrainingFilter, TrainingListPage> implements Serializable
{

  private static final long serialVersionUID = 1284459106693285166L;
  private static final Logger log = Logger.getLogger(TrainingListForm.class);

  /**
   * @param parentPage
   */
  public TrainingListForm(final TrainingListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected void init()
  {
    super.init();
    {
      gridBuilder.newGridPanelId();
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#newSearchFilterInstance()
   */
  @Override
  protected TrainingFilter newSearchFilterInstance()
  {
    return new TrainingFilter();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

}
