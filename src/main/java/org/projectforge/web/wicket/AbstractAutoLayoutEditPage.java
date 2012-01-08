/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.web.wicket;

import org.apache.wicket.PageParameters;
import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.BaseDao;

public abstract class AbstractAutoLayoutEditPage<O extends AbstractBaseDO< ? >, F extends AbstractEditForm<O, ? >, D extends BaseDao<O>>
    extends AbstractEditPage<O, F, D>
{
  private static final long serialVersionUID = 1359786142324813669L;

  public AbstractAutoLayoutEditPage(final PageParameters parameters, final String i18nPrefix)
  {
    super(parameters, i18nPrefix);
  }

  public F getForm()
  {
    return form;
  }
}
