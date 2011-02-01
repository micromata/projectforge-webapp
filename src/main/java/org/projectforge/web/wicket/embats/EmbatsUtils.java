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

package org.projectforge.web.wicket.embats;

import org.projectforge.web.UserAgentBrowser;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class EmbatsUtils
{
  /**
   * @param browser
   * @param version The browser's version.
   * @return true, if the given browser supports embats, otherwise false.
   */
  public static boolean isEmbatsSupported(final UserAgentBrowser browser, final String version)
  {
    if (version == null) {
      return false;
    }
    // http://webfonts.info/wiki/index.php?title=%40font-face_browser_support
    if (browser == UserAgentBrowser.IE) {
      return version.compareTo("4") >= 0;
    } else if (browser == UserAgentBrowser.FIREFOX) {
      return version.compareTo("3.5") >= 0;
    } else if (browser == UserAgentBrowser.SAFARI) {
      return version.compareTo("5.1") >= 0;
    } else if (browser == UserAgentBrowser.CHROME) {
      return version.compareTo("4") >= 0;
    } else if (browser == UserAgentBrowser.OPERA) {
      return version.compareTo("10") >= 0;
    }
    return false;
  }
}
