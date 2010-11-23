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

package org.projectforge.web.core;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;

import org.projectforge.Version;
import org.projectforge.access.AccessChecker;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.Menu;
import org.projectforge.web.MenuBuilder;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Base action stripes bean should be implemented by all action beans. Supports i18n and autowiring members via spring.
 */
@Deprecated
public abstract class BaseActionBean implements ActionBean
{
  public static final String USER_PREF_MENU_KEY = "menu";

  protected AccessChecker accessChecker;

  private static Boolean developmentModus;

  protected String eventKey;

  protected String selectedValue;

  /**
   * @see #getAjaxAutocompleteValue()
   */
  public static final String AJAX_TEXT_PARAMETER = "q";

  public static Resolution getJsonResolution(final String content)
  {
    return new Resolution() {
      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
      {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "utf-8"));
        pw.print(content);
        pw.flush();
      }
    };
  }

  public static ResourceBundle getResourceBundle(final Locale locale)
  {
    return PFUserContext.getResourceBundle(locale);
  }

  public String getBundleName()
  {
    return PFUserContext.BUNDLE_NAME;
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

  private BaseActionBeanContext context;

  private MenuBuilder menuBuilder;

  public void setMenuBuilder(MenuBuilder menuBuilder)
  {
    this.menuBuilder = menuBuilder;
  }

  /**
   * Needed for forwarding to input jsp.
   * @return
   */
  protected String getJspUrl()
  {
    return getClass().getAnnotation(BaseAction.class).jspUrl();
  }

  /**
   * Here's how it is. Someone (quite possible the Stripes Dispatcher) needed to get the source page resolution. But no source page was
   * supplied in the request, and unless you override ActionBeanContext.getSourcePageResolution() you're going to need that value. When you
   * use a <stripes:form> tag a hidden field called '_sourcePage' is included. If you write your own forms or links that could generate
   * validation errors, you must include a value for this parameter. This can be done by calling request.getServletPath().
   * @return
   */
  public String getSourcePage()
  {
    return context.getRequest().getServletPath();
  }

  public BaseActionBeanContext getContext()
  {
    return context;
  }

  /**
   * This request parameter will be posted by the jquery autocomplete and contains the text the user has typed in.
   * @see #AJAX_TEXT_PARAMETER
   */
  public String getAjaxAutocompleteValue()
  {
    return context.getRequest().getParameter(AJAX_TEXT_PARAMETER);
  }

  /**
   * Autowires the spring objects.
   */
  public void setContext(final ActionBeanContext context)
  {
    this.context = (BaseActionBeanContext) context;
    HttpServletRequest request = getContext().getRequest();
    HttpSession session = request.getSession();
    WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {}, webApplicationContext);
    ctx.getBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    ctx.getBeanFactory().autowireBeanProperties(context, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
  }

  /**
   * The action url is needed inside flows (e. g. for selecting task, date etc.)
   * @return
   */
  protected String getActionUrl()
  {
    return getClass().getAnnotation(UrlBinding.class).value();
  }

  public String getLocalizedMessage(String messageKey, Object... params)
  {
    return PFUserContext.getLocalizedMessage(messageKey, params);
  }

  public String getLocalizedString(String key)
  {
    return PFUserContext.getLocalizedString(key);
  }

  public void addError(String field, String messageKey, Object... params)
  {
    getErrors().add(field, new I18nError(this, messageKey, params));
  }

  public void addGlobalError(String messageKey, Object... params)
  {
    // Shift the
    getErrors().addGlobalError(new I18nError(this, messageKey, params));
  }

  public boolean isDevelopmentSystem()
  {
    if (developmentModus == null) {
      String value = getContext().getServletContext().getInitParameter("development");
      developmentModus = "true".equals(value);
    }
    return developmentModus;
  }

  protected ValidationErrors getErrors()
  {
    ValidationErrors errors = getContext().getValidationErrors();
    if (errors == null) {
      errors = new ValidationErrors();
      getContext().setValidationErrors(errors);
    }
    return errors;
  }

  protected boolean hasErrors()
  {
    ValidationErrors errors = getContext().getValidationErrors();
    if (errors != null && errors.isEmpty() == false) {
      // This form has validation errors.
      return true;
    }
    return false;
  }

  public Menu getMenu()
  {
    Menu menu = (Menu) getContext().getEntry(USER_PREF_MENU_KEY);
    if (menu == null) {
      menu = rebuildMenu();
    }
    return menu;
  }

  protected Menu rebuildMenu()
  {
    final PFUserDO user = getContext().getUser();
    if (user != null) {
      menuBuilder.expireMenu(user.getId());
    }
    Menu menu = menuBuilder.buildDTreeMenu(user);
    getContext().putEntry(USER_PREF_MENU_KEY, menu, false); // Stripes menu
    getContext().removeEntry(OldMenuPanel.USER_PREF_MENU_KEY); // Wicket menu
    return menu;
  }

  public void setAccessChecker(AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  protected Resolution getDownloadResolution(final String fileName, final byte[] contents)
  {
    return new Resolution() {
      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
      {
        try {
          ResponseUtils.streamToOut(fileName, contents, response, getContext().getServletContext(), true);
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    };
  }

  /**
   * For own events. Please do not forget
   * 
   * <pre>
   *        &lt;stripes:hidden name=&quot;eventKey&quot; /&gt;
   * </pre>
   * 
   * in your jsp. Events can be fired for example via JavaScript:
   * 
   * <pre>
   *        &lt;script type=&quot;text/javascript&quot;&gt;
   *          function submitEvent (event) {
   *            document.getElementsByName(&quot;eventKey&quot;)[0].value = event;
   *            return document.forms[0].submit();
   *          }
   *        &lt;/script&gt;
   * </pre>
   * 
   * <pre>
   *        &lt;a onclick=&quot;submitEvent('select.taskId')&quot;
   *              href=&quot;#&quot;&gt; ${node.taskName} &lt;/a&gt;
   * </pre>
   * 
   * The select events will be handled by this action bean automatically. Please use other event names for you own events.
   * @return
   */
  @Validate
  public String getEventKey()
  {
    return eventKey;
  }

  public void setEventKey(String event)
  {
    this.eventKey = event;
  }

  /**
   * The property selectedValue holds the value of the selected item in selection mode. This value will be set via JavaScript function
   * submitEvent(eventKey, selectedValue)
   * @return
   */
  @Validate
  public String getSelectedValue()
  {
    return selectedValue;
  }

  public void setSelectedValue(String selectedValue)
  {
    this.selectedValue = selectedValue;
  }
}
