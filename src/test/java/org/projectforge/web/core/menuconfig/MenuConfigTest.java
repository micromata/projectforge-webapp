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

package org.projectforge.web.core.menuconfig;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;
import org.projectforge.web.FavoritesMenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MenuConfigTest
{
  @Test
  public void jsonTest()
  {
    final String json = "[{'content':'Administration','children':[{'ref':'ACCESS_LIST','content':'Zugriffsverwaltung'},{'ref':'GROUP_LIST','content':'Gruppen'},{'ref':'USER_LIST','content':'Benutzer'},{'ref':'SYSTEM','content':'System'},{'ref':'licenseManagement','content':'Lizenzen'}]},{'content':'Orga','children':[{'ref':'INBOX_LIST','content':'Posteingang'},{'ref':'OUTBOX_LIST','content':'Postausgang'},{'ref':'CONTRACTS','content':'Verträge'},{'ref':'BOOK_LIST','content':'Bücher'}]},{'content':'FiBu1','children':[{'ref':'OUTGOING_INVOICE_LIST','content':'Debitorenrechnungen'},{'ref':'INCOMING_INVOICE_LIST','content':'Kreditorenrechnungen'},{'ref':'ORDER_LIST','content':'Auftragsbuch1'}]},{'content':'Kost','children':[{'ref':'CUSTOMER_LIST','content':'Kunden'},{'ref':'PROJECT_LIST','content':'Projekte'}]},{'ref':'INCOMING_INVOICE_LIST','content':'Kreditorenrechnungen'},{'content':'Projektmanagement5','children':[{'ref':'MEB','content':'MEB5'},{'ref':'HR_VIEW','content':'Personalplanung'},{'ref':'MONTHLY_EMPLOYEE_REPORT','content':'Monatsbericht'},{'ref':'TIMESHEET_LIST','content':'Zeitberichte'}]},{'content':'Adressen','children':[{'ref':'ADDRESS_LIST','content':'Adressen'},{'ref':'addressCampaignValues','content':'Adressen für Kampagnen'},{'ref':'PHONE_CALL','content':'Direktwahl'}]},{'ref':'TASK_TREE','content':'Aufgaben'},{'ref':'CALENDAR','content':'Kalender'},{'ref':'toDo','content':'ToDo4'}]"
        .replace('\'', '"');
    final Gson gson = new Gson();
    final Type collectionType = new TypeToken<Collection<FavoritesMenuItem>>() {
    }.getType();
    final Collection<FavoritesMenuItem> col = gson.fromJson(json, collectionType);
    Assert.assertEquals(10, col.size());
    final Iterator<FavoritesMenuItem> it = col.iterator();
    final FavoritesMenuItem item = it.next();
    Assert.assertEquals("Administration", item.getContent());
    Assert.assertNull(gson.fromJson("", collectionType));
  }
}
