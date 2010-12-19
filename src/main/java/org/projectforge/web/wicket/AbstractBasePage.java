/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.Version;
import org.projectforge.common.DateHelper;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.LoginPage;
import org.projectforge.web.Menu;
import org.projectforge.web.MenuBuilder;
import org.projectforge.web.core.LogoServlet;
import org.projectforge.web.core.MenuPanel;
import org.projectforge.web.wicket.components.TooltipImage;

/**
 * Do only derive from this page, if no login is required!
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class AbstractBasePage extends WebPage
{
  protected WebMarkupContainer body;

  protected boolean alreadySubmitted = false;

  @SpringBean(name = "menuBuilder")
  private MenuBuilder menuBuilder;

  @SpringBean(name = "userXmlPreferencesCache")
  private UserXmlPreferencesCache userXmlPreferencesCache;

  /**
   * Url with no or minimal set of parameters.
   */
  protected String bookmarkableUrl;

  /**
   * Url with set of parameters (if supported, otherwise null).
   */
  protected String extendedBookmarkableUrl;

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
  public AbstractBasePage(final PageParameters parameters)
  {
    super(parameters);
    final Menu menu = menuBuilder.getMenu(getUser());
    menu.setSelectedMenu(this);
    add(new Label("windowTitle", new Model<String>() {
      @Override
      public String getObject()
      {
        return getWindowTitle();
      }
    }));
    add(CSSPackageResource.getHeaderContribution("styles/main.css"));
    add(CSSPackageResource.getHeaderContribution("styles/calendar.css"));
    add(CSSPackageResource.getHeaderContribution("styles/projectforge.css"));
    add(JavascriptPackageResource.getHeaderContribution("scripts/jquery-1.4.2.min.js"));
    add(JavascriptPackageResource.getHeaderContribution("scripts/jquery.tooltip.min.js"));
    add(JavascriptPackageResource.getHeaderContribution("scripts/projectforge.js"));
    add(WicketUtils.headerContributorForFavicon(getUrl("/favicon.ico")));
    body = new WebMarkupContainer("body") {
      protected void onComponentTag(ComponentTag tag)
      {
        onBodyTag(tag);
      }
    };
    add(body);
    body.add(new Label("title", "<not yet visible>").setVisible(false));
    final WebMarkupContainer navigationContainer = new WebMarkupContainer("navigation");
    body.add(navigationContainer);
    final Label developmentsystemLabel = new Label("developmentsystem", "Developmentsystem!");
    navigationContainer.add(developmentsystemLabel);
    if (getWicketApplication().isDevelopmentSystem() == true) {
      navigationContainer.add(new SimpleAttributeModifier("style", WebConstants.CSS_BACKGROUND_COLOR_RED));
    } else {
      developmentsystemLabel.setVisible(false);
    }
    navigationContainer.add(new ContextImage("logoImage", LogoServlet.URL));
    final Model<String> loggedInLabelModel = new Model<String>() {
      public String getObject()
      {
        final PFUserDO user = getUser();
        if (user == null) {
          return getString("notLoggedIn");
        }
        return getString("loggedInUserInfo") + " <strong>" + escapeHtml(user.getUserDisplayname()) + "</strong> |";
      }
    };
    navigationContainer.add(new Label("loggedInLabel", loggedInLabelModel).setEscapeModelStrings(false).setRenderBodyOnly(false));
    final PageParameters params = new PageParameters();
    params.add(LoginPage.REQUEST_PARAM_LOGOUT, "true");
    final Link<String> logoutLink = new Link<String>("logoutLink") {
      public void onClick()
      {
        logout();
        setResponsePage(LoginPage.class);
      };
    };
    navigationContainer.add(logoutLink);
    if (getUser() == null) {
      logoutLink.setVisible(false);
    }
    logoutLink.add(new Label("logoutLabel", getString("menu.logout")).setRenderBodyOnly(true));
    final ExternalLink newsLink = new ExternalLink("newsLink", getUrl("/secure/doc/News.html"));
    navigationContainer.add(newsLink);
    newsLink.add(new Label("versionLabel", "V.&nbsp;" + getAppVersion() + ",&nbsp;" + getAppReleaseTimestamp())
        .setEscapeModelStrings(false));
    @SuppressWarnings("unchecked")
    final Link< ? > sendFeedbackLink = new Link("sendFeedback") {
      public void onClick()
      {
        setResponsePage(FeedbackPage.class);
      };
    };
    navigationContainer.add(sendFeedbackLink);

    final MenuPanel menuPanel = new MenuPanel("mainMenu");
    navigationContainer.add(menuPanel);
    menuPanel.init();

    final TooltipImage starImage = new TooltipImage("image", getResponse(), WebConstants.IMAGE_STAR_PLUS, new Model<String>() {
      @Override
      public String getObject()
      {
        return getString("tooltip.directPageLink") + ": " + bookmarkableUrl;
      }
    });
    starImage.setVisible(isBookmarkLinkIconVisible());
    body.add(starImage);
    final Label bookmarkLabel = new Label("bookmark", new Model<String>() {
      @Override
      public String getObject()
      {
        final StringBuffer buf = new StringBuffer();
        buf.append(getString("tooltip.directPageLink")).append(":<br/><b>").append(bookmarkableUrl).append("</b>");
        if (extendedBookmarkableUrl != null) {
          buf.append("<br/>").append(getString("tooltip.directPageExtendedLink")).append(":<br/>").append(extendedBookmarkableUrl);
        }
        return buf.toString();
      }
    });
    bookmarkLabel.setEscapeModelStrings(false);
    body.add(bookmarkLabel);
    if (isBookmarkable() == false) {
      starImage.setVisible(false);
      bookmarkLabel.setVisible(false);
    }
  }

  /**
   * Overload this method for pages for which the link icon shouldn't be visible (e. g. ImageCropperPage).
   * @return True, if the bookmark link icon should be visible.
   */
  protected boolean isBookmarkLinkIconVisible()
  {
    return true;
  }

  private String getBookmarkableUrl(final PageParameters parameters)
  {
    String relativeUrl = (String) urlFor(this.getClass(), parameters);
    final int pos = relativeUrl.lastIndexOf("../"); // Hotfix: Wicket prepends some times a lot of "../" strings.
    if (pos > 0) {
      relativeUrl = relativeUrl.substring(pos + 3);
    }
    if (relativeUrl.indexOf("/wicket:pageMapName") > 0) {
      // Remove this wicket parameter:
      relativeUrl = relativeUrl.replaceAll("/wicket:pageMapName/wicket-[0-9]+", "");
    }
    return WicketUtils.getAbsolutePageUrl(getRequest(), relativeUrl, true);
  }

  private String getBookmarkableUrl()
  {
    final PageParameters parameters = getBookmarkRequiredPageParameters();
    if (parameters != null) {
      return getBookmarkableUrl(parameters);
    } else {
      return getBookmarkableUrl(new PageParameters());
    }
  }

  private String getBookmarkableExtendedUrl()
  {
    final PageParameters extendedParameters = getBookmarkPageExtendedParameters();
    if (extendedParameters == null) {
      return null;
    }
    return getBookmarkableUrl(extendedParameters);
  }

  /**
   * Overwrite this method if you want to add additional page parameters for your bookmarks (extended direct link).
   * @return null at default.
   */
  protected PageParameters getBookmarkPageExtendedParameters()
  {
    return null;
  }

  /**
   * Overwrite this method if you want to add required page parameters for your bookmarks (basic direct link).
   * @return null at default.
   */
  protected PageParameters getBookmarkRequiredPageParameters()
  {
    return null;
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    this.bookmarkableUrl = getBookmarkableUrl();
    this.extendedBookmarkableUrl = getBookmarkableExtendedUrl();
    alreadySubmitted = false;
  }

  /**
   * Get current time considering the users locale.
   * @see DateHelper#getCalendar()
   */
  public Date now()
  {
    Calendar cal = DateHelper.getCalendar();
    return cal.getTime();
  }

  /**
   * Gets the version of this Application.
   * @see Version#NUMBER
   */
  public final String getAppVersion()
  {
    return Version.NUMBER;
  }

  /**
   * Gets the release date of this Application.
   * @see Version#RELEASE_DATE
   */
  public final String getAppReleaseDate()
  {
    return Version.RELEASE_DATE;
  }

  /**
   * Gets the release date of this Application.
   * @see Version#RELEASE_DATE
   */
  public final String getAppReleaseTimestamp()
  {
    return Version.RELEASE_TIMESTAMP;
  }

  public MySession getMySession()
  {
    return (MySession) getSession();
  }

  /**
   * Includes session id (encode URL) at default.
   * @see #getUrl(String, boolean)
   */
  public String getUrl(String path)
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
  protected void redirectToUrl(String url)
  {
    getRequestCycle().setRequestTarget(new RedirectRequestTarget(getUrl(url)));
  }

  protected abstract String getTitle();

  /**
   * Security. Implement this method if you are really sure that you want to implement an unsecure page (meaning this page is available
   * without any authorization, it's therefore public)!
   */
  protected abstract void thisIsAnUnsecuredPage();

  protected String getWindowTitle()
  {
    return Version.APP_ID + " - " + getTitle();
  }

  /**
   * If your page need to manipulate the body tag overwrite this method, e. g.: tag.put("onload", "...");
   * @return
   */
  protected void onBodyTag(ComponentTag bodyTag)
  {
  }

  protected WicketApplication getWicketApplication()
  {
    return (WicketApplication) getApplication();
  }

  /**
   * @see StringEscapeUtils#escapeHtml(String)
   */
  protected String escapeHtml(String str)
  {
    return StringEscapeUtils.escapeHtml(str);
  }

  /**
   * Always returns null for unsecured page.
   * @return null
   * @see AbstractSecuredPage#getUser()
   */
  protected PFUserDO getUser()
  {
    return null;
  }

  protected String getLocalizedMessage(String key, Object... params)
  {
    if (params == null) {
      return getString(key);
    }
    return MessageFormat.format(getString(key), params);
  }

  /**
   * Logs the user out by invalidating the session.
   * @see LoginPage#logout(MySession, WebRequest, WebResponse, UserXmlPreferencesCache, MenuBuilder)
   */
  private void logout()
  {
    LoginPage
        .logout((MySession) getSession(), (WebRequest) getRequest(), (WebResponse) getResponse(), userXmlPreferencesCache, menuBuilder);
  }
}
