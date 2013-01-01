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

package org.projectforge.core;

import java.util.Locale;

import org.hibernate.Session;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.HistoryUserRetriever;
import de.micromata.hibernate.history.delta.PropertyDelta;
import de.micromata.hibernate.history.web.HistoryTable;
import de.micromata.hibernate.history.web.HistoryTag;

/**
 * @author wolle
 * 
 */
public class HistoryUser implements HistoryUserRetriever
{
  public static final String RESOURCE_BUNDLE_NAME = "Printing";

  public HistoryUser()
  {
    HistoryTag.setDefaultBundle(RESOURCE_BUNDLE_NAME);
    // Hook zum Registrieren des Defaultformatters
    HistoryTable.setDefaultFormat(new PrintingHistoryFormatter(RESOURCE_BUNDLE_NAME));
    HistoryTable.registerFormatter(PFUserDO.class, "lastLogin", new PrintingHistoryFormatter(RESOURCE_BUNDLE_NAME) {
      @Override
      public boolean isVisible(Session session, Locale locale, Object changed, HistoryEntry historyEntry, PropertyDelta delta)
      {        
        return false;
      }
    });
    
    // Formatierung der History-Tabelle
    HistoryTable.registerFormatter(PFUserDO.class, "password", new PrintingHistoryFormatter(RESOURCE_BUNDLE_NAME) {
      @Override
      public String asString(Session session, final Locale locale, String className, String property, Object value)
      {
        return "*****"; // escapeHtml wird nicht benötigt.
      }
    });
  }

  /**
   * get the principal from the ThreadLocal
   * 
   * @see org.hibernate.HistoryUserRetriever#getPrincipal()
   */
  public String getPrincipal()
  {
    final PFUserDO user = PFUserContext.getUser();
    if (user == null) {
      return null;
    }
    /*
    if (RunAsAdapter.isRealUser() == true) {
      return user.getPk().toString();
    }
    PFUserDO realUser = RunAsAdapter.getRealUser() instanceof PFUserDO ? (PFUserDO) RunAsAdapter.getRealUser() : null;
    if (realUser != null) {
      return realUser.getPk() + "," + user.getPk();
    }*/
    return user.getId().toString();
  }
}
