/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.AppVersion;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.WicketApplicationInterface;
import org.projectforge.web.wicket.WicketUtils;

/**
 * Do only derive from this page, if no login is required!
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class AbstractMobilePage extends WebPage
{
  private static final long serialVersionUID = -6221091194614601467L;

  protected final static String TOP_RIGHT_BUTTON_ID = "topRightButton";

  protected final static String TOP_CENTER_ID = "topCenter";

  protected boolean alreadySubmitted = false;

  protected WebMarkupContainer headerContainer;

  protected WebMarkupContainer rightButtonContainer;

  protected WebMarkupContainer pageContainer;

  // iWebKit doesn't work completely with wicket tags such as wicket:panel etc.
  private static Boolean stripTags;

  private boolean rightButtonRendered;

  public AbstractMobilePage()
  {
    this(new PageParameters());
  }

  protected void setNoBackButton()
  {
    headerContainer.add(AttributeModifier.replace("data-nobackbtn", "true"));
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
    if (stripTags == null) {
      stripTags = Application.get().getMarkupSettings().getStripWicketTags();
    }
    add(pageContainer = new WebMarkupContainer("page"));
    pageContainer.add(headerContainer = new WebMarkupContainer("header"));
    headerContainer.add(getTopCenter());
    add(new Label("windowTitle", new Model<String>() {
      @Override
      public String getObject()
      {
        return getWindowTitle();
      }
    }));
    final Model<String> loggedInLabelModel = new Model<String>() {
      @Override
      public String getObject()
      {
        return "<strong>" + escapeHtml(AppVersion.APP_TITLE) + "</strong>";
      }
    };
    pageContainer.add(new Label("loggedInLabel", loggedInLabelModel).setEscapeModelStrings(false).setRenderBodyOnly(false)
        .setVisible(getUser() != null));
    if (getWicketApplication().isDevelopmentSystem() == true) {
      // navigationContainer.add(AttributeModifier.replace("style", WebConstants.CSS_BACKGROUND_COLOR_RED));
    } else {
    }
    pageContainer.add(AttributeModifier.append("data-title", new Model<String>() {
      @Override
      public String getObject()
      {
        return getTitle();
      }
    }));
  }

  @Override
  public void renderHead(final IHeaderResponse response)
  {
    super.renderHead(response);
    response.renderString(WicketUtils.getCssForFavicon(getUrl("/favicon.ico")));
    // add(CSSPackageResource.getHeaderContribution("mobile/css/iWebKit.css"));
    response.renderCSSReference("mobile/jquery.mobile/jquery.mobile-1.0.1.min.css");
    // add(CSSPackageResource.getHeaderContribution("mobile/css/projectforge.css"));
    response.renderCSSReference("mobile/projectforge.css");
    response.renderJavaScriptReference("scripts/jquery/1.7.1/jquery.min.js");
    // response.renderJavaScriptReference("mobile/jquery.mobile/myconfig.js");
    response.renderJavaScriptReference("mobile/jquery.mobile/jquery.mobile-1.0.1.min.js");
  }

  /**
   * @return Home link as default.
   */
  protected Component getTopCenter()
  {
    return MenuMobilePage.getHomeLink(this, TOP_CENTER_ID);
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    if (rightButtonRendered == false) {
      rightButtonRendered = true;
      addTopRightButton();
    }
    if (stripTags == false) {
      Application.get().getMarkupSettings().setStripWicketTags(true);
    }
    alreadySubmitted = false;
  }

  @Override
  protected void onAfterRender()
  {
    super.onAfterRender();
    if (stripTags == false) {
      Application.get().getMarkupSettings().setStripWicketTags(false);
    }
  }

  public MySession getMySession()
  {
    return (MySession) getSession();
  }

  protected WicketApplicationInterface getWicketApplication()
  {
    return (WicketApplicationInterface) getApplication();
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
  protected String escapeHtml(final String str)
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

  protected String getLocalizedMessage(final String key, final Object... params)
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

  protected String getWindowTitle()
  {
    return AppVersion.APP_ID;
  }

  /**
   * Adds invisible component as default.
   */
  protected void addTopRightButton()
  {
    headerContainer.add(new Label(TOP_RIGHT_BUTTON_ID, "[invisible]").setVisible(false));
  }
}
