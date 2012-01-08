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

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.projectforge.common.DateHelper;


/**
 * Displays from and to date as UTC time stamp. Use-ful for checking the correctness of the time zone of any date object in the UI.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class DatesAsUTCLabel extends Label
{
  private static final long serialVersionUID = 3910588105442026807L;

  @SuppressWarnings("serial")
  public DatesAsUTCLabel(final String id)
  {
    super(id);
    setDefaultModel(new Model<String>() {
      @Override
      public String getObject()
      {
        StringBuffer buf = new StringBuffer();
        if (getStartTime() != null) {
          buf.append(DateHelper.TECHNICAL_ISO_UTC.get().format(getStartTime()));
          if (getStopTime() != null) {
            buf.append(" - ");
          }
        }
        if (getStopTime() != null) {
          buf.append(DateHelper.TECHNICAL_ISO_UTC.get().format(getStopTime()));
        }
        return buf.toString();
      }
    });
  }

  public Date getStartTime()
  {
    return null;
  }

  public Date getStopTime()
  {
    return null;
  }

}
