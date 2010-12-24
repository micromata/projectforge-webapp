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

package org.projectforge.web.mobile;

import java.text.MessageFormat;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.projectforge.Version;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.WicketApplication;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.ImageBookmarkablePageLinkPanel;
import org.projectforge.web.wicket.components.MyRepeatingView;

/**
 * Do only derive from this page, if no login is required!
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class AbstractMobilePage extends WebPage
{
  protected final static String RIGHT_BUTTON_ID = "rightButton";

  protected RepeatingView leftNavigationRepeater;

  protected WebMarkupContainer leftNavigationContainer, rightButtonContainer;

  // iWebKit doesn't work completely with wicket tags such as wicket:panel etc.
  private static boolean stripTags;

  static {
    stripTags = Application.get().getMarkupSettings().getStripWicketTags();
  }

  public AbstractMobilePage()
  {
    this(new PageParameters());
  }

  /**
   * Constructor that is invoked when page is invoked without a session.
   * 
   * @param parameters Page parameters
   */
  @SuppressWarnings("serial")
  public AbstractMobilePage(final PageParameters parameters)
  {
    super(parameters);
    add(CSSPackageResource.getHeaderContribution("mobile/css/style.css"));
    add(JavascriptPackageResource.getHeaderContribution("mobile/javascript/functions.js"));
    add(WicketUtils.headerContributorForFavicon(getUrl("/favicon.ico")));
    add(leftNavigationContainer = new WebMarkupContainer("leftNavigation"));
    leftNavigationContainer.add(leftNavigationRepeater = new MyRepeatingView("leftNavigationRepeater"));
    leftNavigationRepeater.add(new ImageBookmarkablePageLinkPanel(leftNavigationRepeater.newChildId(), MenuMobilePage.class, getResponse(),
        MobileWebConstants.IMAGE_HOME));
    add(new Label("windowTitle", new Model<String>() {
      @Override
      public String getObject()
      {
        return getWindowTitle();
      }
    }));
    addRightButton();
    final Model<String> loggedInLabelModel = new Model<String>() {
      public String getObject()
      {
        final PFUserDO user = getUser();
        if (user == null) {
          return getString("notLoggedIn");
        }
        return "<strong>" + escapeHtml(user.getFullname()) + "</strong> |";
      }
    };
    add(new Label("loggedInLabel", loggedInLabelModel).setEscapeModelStrings(false).setRenderBodyOnly(false).setVisible(getUser() != null));
    if (getWicketApplication().isDevelopmentSystem() == true) {
      // navigationContainer.add(new SimpleAttributeModifier("style", WebConstants.CSS_BACKGROUND_COLOR_RED));
    } else {
    }
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    if (stripTags == false) {
      Application.get().getMarkupSettings().setStripWicketTags(true);
    }
  }

  @Override
  protected void onAfterRender()
  {
    super.onAfterRender();
    if (stripTags == false) {
      Application.get().getMarkupSettings().setStripWicketTags(false);
    }
  }

  protected void addLeftnavComponent(final Component component)
  {
    leftNavigationRepeater.add(component);
    leftNavigationContainer.setVisible(true);
  }

  public MySession getMySession()
  {
    return (MySession) getSession();
  }

  protected WicketApplication getWicketApplication()
  {
    return (WicketApplication) getApplication();
  }

  protected abstract String getTitle();

  /**
   * Security. Implement this method if you are really sure that you want to implement an unsecure page (meaning this page is available
   * without any authorization, it's therefore public)!
   */
  protected abstract void thisIsAnUnsecuredPage();

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

  protected String getWindowTitle()
  {
    return Version.APP_ID + " - " + getTitle();
  }

  /**
   * Adds invisible component as default.
   */
  protected void addRightButton()
  {
    add(new Label(RIGHT_BUTTON_ID, "[invisible]").setVisible(false));
  }
}
