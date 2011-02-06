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
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WicketUtils;

public class DocumentationPage extends AbstractSecuredPage
{
  public DocumentationPage(final PageParameters parameters)
  {
    super(parameters);
    final Locale locale = PFUserContext.getLocale();
    final boolean isGerman = locale != null && locale.toString().startsWith("de") == true;
    addDocLink("newsLink", "doc/News.html");
    addDocLink("tutorialLink", "doc/ProjectForge.html");
    addDocLink("handbookLink", "doc/Handbuch.html");
    if (isGerman == true) {
      addDocLink("faqLink", "doc/FAQ_de.html");
    } else {
      addDocLink("faqLink", "doc/FAQ.html");
    }
    addDocLink("licenseLink", "LICENSE.txt");

    addDocLink("adminLogbuchLink", "doc/AdminLogbuch.html");
    addDocLink("adminGuideLink", "doc/AdministrationGuide.html");
    addDocLink("developerGuideLink", "doc/DeveloperGuide.html");
    addDocLink("projectDocLink", "site/index.html");
    addDocLink("javaDocLink", "site/apidocs/index.html");
  }

  private void addDocLink(final String id, final String url)
  {
    final WebMarkupContainer linkContainer = new WebMarkupContainer(id);
    linkContainer.add(new SimpleAttributeModifier("onclick", "javascript:openDoc('"
        + WicketUtils.getUrl(getResponse(), "/secure/" + url, true)
        + "');"));
    linkContainer.add(new SimpleAttributeModifier("onmouseover", "style.cursor='pointer'"));
    body.add(linkContainer);
  }

  @Override
  protected String getTitle()
  {
    return getString("doc.title");
  }

}
