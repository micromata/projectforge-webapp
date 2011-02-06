/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.doc;

import java.util.Locale;

import org.apache.wicket.PageParameters;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class DocumentationPage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DocumentationPage.class);

  public DocumentationPage(final PageParameters parameters)
  {
    super(parameters);
    final Locale locale = PFUserContext.getLocale();
    final boolean isGerman = locale != null && locale.toString().startsWith("de") == true;
    // final Node doc = root.addSubMenu(user, MenuItemDef.DOCUMENTATION);
    // doc.addSubMenu(user, MenuItemDef.NEWS);
    // doc.addSubMenu(user, MenuItemDef.PROJECTFORGE_DOC);
    // doc.addSubMenu(user, MenuItemDef.USER_GUIDE);
    // if (isGerman == true) {
    // doc.addSubMenu(user, MenuItemDef.FAQ_DE);
    // } else {
    // doc.addSubMenu(user, MenuItemDef.FAQ);
    // }
    // doc.addSubMenu(user, MenuItemDef.LICENSE);
    // doc.addSubMenu(user, MenuItemDef.PROJECT_DOC);
    // doc.addSubMenu(user, MenuItemDef.ADMIN_LOGBUCH);
    // doc.addSubMenu(user, MenuItemDef.ADMIN_GUIDE);
    // doc.addSubMenu(user, MenuItemDef.DEVELOPER_GUIDE);
    // doc.addSubMenu(user, MenuItemDef.JAVA_DOC);
    // doc.addSubMenu(user, MenuItemDef.TEST_REPORTS);

  }

  @Override
  protected String getTitle()
  {
    return getString("doc.title");
  }

}
