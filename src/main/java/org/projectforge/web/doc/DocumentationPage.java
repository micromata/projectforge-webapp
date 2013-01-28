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

package org.projectforge.web.doc;

import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WicketUtils;

public class DocumentationPage extends AbstractSecuredPage
{
  private static final long serialVersionUID = 1680968273313948593L;

  /**
   * Adds BookmarkablePageLink with given id to the given parentContainer.
   * @param id id of the link (shouldn't bee "newsLink" in body, because it's already used by DocumentationPage).
   * @param parentContainer Page (normally body)
   */
  public static final AbstractLink addNewsLink(final WebMarkupContainer parentContainer, final String id)
  {
    final AbstractLink link = new ExternalLink(id, WicketUtils.getUrl(parentContainer.getRequestCycle(), "secure/doc/News.html", true));
    parentContainer.add(link);
    return link;
  }

  public DocumentationPage(final PageParameters parameters)
  {
    super(parameters);
    final Locale locale = PFUserContext.getLocale();
    final boolean isGerman = locale != null && locale.toString().startsWith("de") == true;
    addDocLink(body, "newsLink", "doc/News.html");
    addDocLink(body, "tutorialLink", "doc/ProjectForge.html");
    addDocLink(body, "handbookLink", "doc/Handbuch.html");
    if (isGerman == true) {
      addDocLink(body, "faqLink", "doc/FAQ_de.html");
    } else {
      addDocLink(body, "faqLink", "doc/FAQ.html");
    }
    addDocLink(body, "licenseLink", "LICENSE.txt");

    addDocLink(body, "adminLogbuchLink", "doc/AdminLogbuch.html");
    addDocLink(body, "adminGuideLink", "doc/AdministrationGuide.html");
    addDocLink(body, "developerGuideLink", "doc/DeveloperGuide.html");
    addDocLink(body, "projectDocLink", "site/index.html");
    addDocLink(body, "javaDocLink", "site/apidocs/index.html");
  }

  private static void addDocLink(final WebMarkupContainer parentContainer, final String id, final String url)
  {
    final WebMarkupContainer linkContainer = new WebMarkupContainer(id);
    linkContainer.add(AttributeModifier.replace("onclick",
        "javascript:openDoc('" + WicketUtils.getUrl(parentContainer.getRequestCycle(), "secure/" + url, true) + "');"));
    linkContainer.add(AttributeModifier.replace("onmouseover", "style.cursor='pointer'"));
    parentContainer.add(linkContainer);
  }

  @Override
  protected String getTitle()
  {
    return getString("doc.title");
  }

}
