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

package org.projectforge.web;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.web.wicket.AbstractStandardFormPage;
import org.projectforge.web.wicket.MySession;

public class LayoutSettingsPage extends AbstractStandardFormPage
{
  private static final String USER_PREF_BROWSER_SCREEN_WIDTH_KEY = "browserScreenWidth";

  private static final long serialVersionUID = -5855547280552503160L;

  private final LayoutSettingsForm form;

  String result;

  public static String getBrowserScreenWidthUserPrefKey(final MySession mySession)
  {
    final UserAgentDevice device = mySession.getUserAgentDevice();
    if (device != UserAgentDevice.UNKNOWN) {
      return USER_PREF_BROWSER_SCREEN_WIDTH_KEY + "." + device;
    }
    final UserAgentOS os = mySession.getUserAgentOS();
    if (os != UserAgentOS.UNKNOWN) {
      return USER_PREF_BROWSER_SCREEN_WIDTH_KEY + "." + os;
    }
    return USER_PREF_BROWSER_SCREEN_WIDTH_KEY;
  }

  public LayoutSettingsPage(final PageParameters parameters)
  {
    super(parameters);
    form = new LayoutSettingsForm(this);
    body.add(form);
    form.init();
  }

  @Override
  protected String getTitle()
  {
    return getString("layout.settings.title");
  }
}
