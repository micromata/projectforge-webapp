/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.projectforge.AppVersion;
import org.projectforge.Version;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.UserAgentBrowser;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.doc.DocumentationPage;

/**
 * Do only derive from this page, if no login is required!
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class AbstractUnsecureBasePage extends WebPage
{
  private static final long serialVersionUID = 7396310612549535899L;

  private static PackageTextTemplate jsTemplate;

  protected WebMarkupContainer body, html;

  protected boolean alreadySubmitted = false;

  /**
   * Convenience method for creating a component which is in the mark-up file but should not be visible.
   * @param wicketId
   * @return
   */
  public static Label createInvisibleDummyComponent(final String wicketId)
  {
    final Label dummyLabel = new Label(wicketId);
    dummyLabel.setVisible(false);
    return dummyLabel;
  }

  /**
   * Constructor that is invoked when page is invoked without a session.
   * 
   * @param parameters Page parameters
   */
  @SuppressWarnings("serial")
  public AbstractUnsecureBasePage(final PageParameters parameters)
  {
    super(parameters);

    final MySession session = ((MySession) getSession());
    html = new TransparentWebMarkupContainer("html");
    add(html);
    if (session.getUserAgentBrowser() == UserAgentBrowser.IE) {
      final Version version = session.getUserAgentBrowserVersion();
      if (version != null) {
        final int major = version.getMajorRelease();
        if (major < 7) {
          html.add(AttributeModifier.append("class", "no-js ie6"));
        } else if (major == 7) {
          html.add(AttributeModifier.append("class", "no-js ie7"));
        } else if (major == 8) {
          html.add(AttributeModifier.append("class", "no-js ie8"));
        } else if (major == 9) {
          html.add(AttributeModifier.append("class", "no-js ie9"));
        }
      }
    }
    add(new Label("windowTitle", new Model<String>() {
      @Override
      public String getObject()
      {
        return getWindowTitle();
      }
    }));

    body = new WebMarkupContainer("body") {
      @Override
      protected void onComponentTag(final ComponentTag tag)
      {
        onBodyTag(tag);
      }
    };
    add(body);
    final WebMarkupContainer developmentSystem = new WebMarkupContainer("developmentSystem");
    body.add(developmentSystem);
    if (WebConfiguration.isDevelopmentMode() == false) {
      developmentSystem.setVisible(false);
    }
    final PFUserDO user = PFUserContext.getUser();
    AbstractLink link;
    if (user == null) {
      body.add(new Label("username", getString("notLoggedIn")).setEscapeModelStrings(false));
      link = new ExternalLink("footerNewsLink", "http://www.projectforge.org/pf-en/News");
      body.add(link);
    } else {
      link = DocumentationPage.addNewsLink(body, "footerNewsLink");
      body.add(new Label("username", getString("loggedInUserInfo")
          + " <span class=\"username\">"
          + escapeHtml(user.getUserDisplayname())
          + "</span>").setEscapeModelStrings(false));
    }
    link.add(new Label("version", "Version " + AppVersion.VERSION.toString() + ", " + AppVersion.RELEASE_DATE).setRenderBodyOnly(true));
  }

  @Override
  public void renderHead(final IHeaderResponse response)
  {
    super.renderHead(response);
    WicketRenderHeadUtils.renderMainJavaScriptIncludes(response);
    response.render(StringHeaderItem.forString(WicketUtils.getCssForFavicon(getUrl("/favicon.ico"))));
    response.render(CssHeaderItem.forUrl("styles/adminica-2.2/main.css"));
    // response.render(CssHeaderItem.forUrl("styles/adminica-2.2/grid.css"));
    response.render(CssHeaderItem.forUrl("styles/adminica-2.2/mobile.css"));
    // response.render(CssHeaderItem.forUrl("styles/adminica-2.2/themes/switcher.css"));
    // response.render(CssHeaderItem.forUrl("styles/adminica-2.2/colours.css"));
    // response.render(CssHeaderItem.forUrl("styles/adminica-2.2/themes/theme_base.css"));
    // response.render(CssHeaderItem.forUrl("styles/adminica-2.2/themes/skin_light.css"));
    // response.render(CssHeaderItem.forUrl("styles/adminica-2.2/themes/bg_noise_zero.css"));
    response.render(CssHeaderItem.forUrl("styles/adminica-2.2/themes/nav_top.css"));
    response.render(CssHeaderItem.forUrl("styles/adminica-2.2/adminica-patch.css"));
    response.render(CssHeaderItem.forUrl("include/bootstrap/css/bootstrap.min.css"));
    response.render(CssHeaderItem.forUrl("styles/projectforge.css"));

    // if (WebConfiguration.isDevelopmentMode() == true) {
    response.render(JavaScriptHeaderItem.forUrl("scripts/adminica-2.2/prefixfree/prefixfree.js"));
    response.render(JavaScriptHeaderItem.forUrl("scripts/adminica-2.2/adminica_ui.js"));
    response.render(JavaScriptHeaderItem.forUrl("scripts/adminica-2.2/adminica_mobile.js"));
    response.render(JavaScriptHeaderItem.forUrl("scripts/adminica-2.2/adminica_load.js"));
    // } else {
    // response.render(JavaScriptHeaderItem.forUrl("scripts/adminica-2.2/prefixfree/prefixfree-min.js"));
    // response.render(JavaScriptHeaderItem.forUrl("scripts/adminica-2.2/bootstrap/bootstrap.min.js"));
    // response.render(JavaScriptHeaderItem.forUrl("scripts/adminica-2.2/adminica_ui.js")); // modified (can't use compressed version).
    // response.render(JavaScriptHeaderItem.forUrl("scripts/adminica-2.2/adminica_mobile-min.js"));
    // response.render(JavaScriptHeaderItem.forUrl("scripts/adminica-2.2/adminica_load-min.js"));
    // }
    response.render(JavaScriptHeaderItem.forUrl("scripts/projectforge.js"));
    initializeContextMenu(response);
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    alreadySubmitted = false;
  }

  /**
   * Gets the version of this Application.
   * @see AppVersion#NUMBER
   */
  public final String getAppVersion()
  {
    return AppVersion.NUMBER;
  }

  /**
   * Gets the release date of this Application.
   * @see AppVersion#RELEASE_DATE
   */
  public final String getAppReleaseDate()
  {
    return AppVersion.RELEASE_DATE;
  }

  /**
   * Gets the release date of this Application.
   * @see AppVersion#RELEASE_DATE
   */
  public final String getAppReleaseTimestamp()
  {
    return AppVersion.RELEASE_TIMESTAMP;
  }

  /**
   * Includes session id (encode URL) at default.
   * @see #getUrl(String, boolean)
   */
  public String getUrl(final String path)
  {
    return getUrl(path, true);
  }

  /**
   * @see WicketUtils#getImageUrl(org.apache.wicket.Response, String)
   */
  public String getImageUrl(final String subpath)
  {
    return WicketUtils.getImageUrl(getResponse(), subpath);
  }

  /**
   * @see WicketUtils#getUrl(org.apache.wicket.Response, String, boolean)
   */
  public String getUrl(final String path, final boolean encodeUrl)
  {
    return WicketUtils.getUrl(getResponse(), path, encodeUrl);
  }

  /**
   * @param url
   * @see #getUrl(String)
   */
  protected void redirectToUrl(final String url)
  {
    getRequestCycle().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(getUrl(url)));
  }

  protected abstract String getTitle();

  /**
   * Security. Implement this method if you are really sure that you want to implement an unsecure page (meaning this page is available
   * without any authorization, it's therefore public)!
   */
  protected abstract void thisIsAnUnsecuredPage();

  protected String getWindowTitle()
  {
    return AppVersion.APP_ID + " - " + getTitle();
  }

  /**
   * If your page need to manipulate the body tag overwrite this method, e. g.: tag.put("onload", "...");
   * @return
   */
  protected void onBodyTag(final ComponentTag bodyTag)
  {
  }

  protected WicketApplicationInterface getWicketApplication()
  {
    return (WicketApplicationInterface) getApplication();
  }

  /**
   * @see StringEscapeUtils#escapeHtml(String)
   */
  protected String escapeHtml(final String str)
  {
    return StringEscapeUtils.escapeHtml(str);
  }

  public MySession getMySession()
  {
    return (MySession) getSession();
  }

  /**
   * Always returns null for unsecured page, otherwise the logged-in user.
   * @return null
   * @see AbstractSecuredPage#getUser()
   */
  protected PFUserDO getUser()
  {
    return null;
  }

  /**
   * Always returns null for unsecured page, otherwise the id of the logged-in user.
   * @return null
   * @see AbstractSecuredPage#getUser()
   */
  protected Integer getUserId()
  {
    return null;
  }

  public String getLocalizedMessage(final String key, final Object... params)
  {
    if (params == null) {
      return getString(key);
    }
    return MessageFormat.format(getString(key), params);
  }

  private void initializeContextMenu(final IHeaderResponse response)
  {

    // context menu
    final Map<String, String> i18nKeyMap = new HashMap<String, String>();
    i18nKeyMap.put("newTab", getString("contextMenu.newTab"));
    i18nKeyMap.put("cancel", getString("contextMenu.cancel"));
    response.render(OnDomReadyHeaderItem.forScript(getJstemplate().asString(i18nKeyMap)));
  }

  /**
   * @return the jstemplate
   */
  private static PackageTextTemplate getJstemplate()
  {
    if (jsTemplate == null) {
      jsTemplate = new PackageTextTemplate(AbstractUnsecureBasePage.class, "ContextMenu.js.template");
    }
    return jsTemplate;
  }
}
