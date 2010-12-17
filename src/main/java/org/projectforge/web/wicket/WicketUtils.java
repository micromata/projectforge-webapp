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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.projectforge.calendar.DayHolder;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.common.BeanHelper;
import org.projectforge.common.ClassHelper;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.core.BaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.core.Configuration;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.TooltipImage;

public class WicketUtils
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WicketUtils.class);

  private static String APPLICATION_CONTEXT = "/ProjectForge";

  public static final String WICKET_APPLICATION_PATH = "wa/";

  public static String getContextPath()
  {
    return APPLICATION_CONTEXT;
  }

  static void setContextPath(final String contextPath)
  {
    APPLICATION_CONTEXT = contextPath;
  }

  /**
   * Renders &lt;link type="image/x-icon" rel="shortcut icon" href="favicon.ico" /&gt;
   * @param favicon The favicon file, e. g. "/ProjectForge/favicon.ico".
   */
  public static HeaderContributor headerContributorForFavicon(final String favicon)
  {
    return new HeaderContributor(new IHeaderContributor() {
      private static final long serialVersionUID = 1L;

      public void renderHead(final IHeaderResponse response)
      {
        response.renderString("<link type=\"image/x-icon\" rel=\"shortcut icon\" href=\"" + favicon + "\" />");
      }
    });
  }

  /**
   * Prepends APPLICATION_CONTEXT if url starts with '/', otherwise url is returned unchanged.
   * @param url
   */
  public static final String getAbsoluteUrl(final String url)
  {
    if (url.startsWith("/") == true) {
      return APPLICATION_CONTEXT + url;
    }
    return url;
  }

  /**
   * Get the url for the given path (without image path). Later, the path of the images is changeable.
   * @param response Needed to encode url.
   * @param subpath
   * @return
   */
  public static String getImageUrl(final Response response, final String subpath)
  {
    return getUrl(response, "/images/" + subpath, true);
  }

  /**
   * Should be c:url equivalent, but isn't yet (works for now).
   * @param response Needed to encode url.
   * @param path
   * @param encodeUrl
   * @return path itself if not starts with '/' otherwise "/ProjectForge" + path with session id and params.
   */
  public static String getUrl(final Response response, final String path, final boolean encodeUrl)
  {
    if (path == null) {
      return null;
    }
    if (path.charAt(0) != '/') {
      // Do not touch relative path:
      return path;
    }
    if (encodeUrl == true) {
      return response.encodeURL(APPLICATION_CONTEXT + path).toString();
    }
    return APPLICATION_CONTEXT + path;
  }

  /**
   * Works for Wicket and non Wicket calling pages. For non Wicket callers the pageClass must be bookmarked in Wicket application.
   * @param pageClass
   * @param Optional list of params in tupel form: key, value, key, value...
   */
  public static String getBookmarkablePageUrl(final Class< ? extends Page> pageClass, final String... params)
  {
    final RequestCycle requestCylce = RequestCycle.get();
    if (requestCylce != null) {
      PageParameters pageParameter = getPageParameters(params);
      return requestCylce.urlFor(pageClass, pageParameter).toString();
    } else {
      // RequestCycle.get().urlFor(pageClass, pageParameter).toString() can't be used for non wicket requests!
      final String alias = WicketApplication.getBookmarkableMountPath(pageClass);
      if (alias == null) {
        log.error("Given page class is not mounted. Please mount class in WicketApplication: " + pageClass);
        return getDefaultPageUrl();
      }
      if (params == null) {
        return WICKET_APPLICATION_PATH + alias;
      }
      final StringBuffer buf = new StringBuffer();
      buf.append(WICKET_APPLICATION_PATH).append(alias);
      try {
        for (int i = 0; i < params.length; i += 2) {
          if (i == 0) {
            buf.append("?");
          } else {
            buf.append("&");
          }
          buf.append(URLEncoder.encode(params[i], "UTF-8")).append("=");
          if (i + 1 < params.length) {
            buf.append(URLEncoder.encode(params[i + 1], "UTF-8"));
          }
        }
      } catch (UnsupportedEncodingException ex) {
        log.error(ex.getMessage(), ex);
      }
      return buf.toString();
    }
  }

  /**
   * Tuples of parameters converted to Wicket parameters.
   * @param params
   * @return
   */
  public static PageParameters getPageParameters(final String[] params)
  {
    final PageParameters pageParameters = new PageParameters();
    if (params != null) {
      for (int i = 0; i < params.length; i += 2) {
        if (i + 1 < params.length) {
          pageParameters.add(params[i], params[i + 1]);
        } else {
          pageParameters.add(params[i], null);
        }
      }
    }
    return pageParameters;
  }

  /**
   * Gets absolute url for an edit page with the object id as parameter.
   */
  public static String getAbsoluteEditPageUrl(final Request request, final Class< ? extends Page> pageClass, Integer id)
  {
    return getAbsoluteEditPageUrl(request, WicketApplication.getBookmarkableMountPath(pageClass), id);
  }

  /**
   * Gets absolute url for an edit page with the object id as parameter. The alias' should be defined in WebConstants.
   * @param alias Name under which the page was mounted to (in WicketApplication).
   */
  public static String getAbsoluteEditPageUrl(final Request request, String alias, Integer id)
  {
    StringBuffer buf = buildAbsoluteUrl(request);
    buf.append(alias + "/id/" + id);
    return buf.toString();
  }

  public static String getAbsolutePageUrl(final Request request, String relativeUrl, boolean removeSessionId)
  {
    final HttpServletRequest httpServletRequest = ((ServletWebRequest) request).getHttpServletRequest();
    final StringBuffer buf = buildAbsoluteBaseUrl(request, httpServletRequest).append("/").append(WICKET_APPLICATION_PATH);
    if (removeSessionId == true) {
      relativeUrl = removeSessionId(relativeUrl);
    }
    if (buf.indexOf(relativeUrl) < 0) {
      // relative url is not yet part of result.
      buf.append(relativeUrl);
    }
    if (removeSessionId == true) {
      return removeSessionId(buf.toString());
    }
    return buf.toString();

  }

  /**
   * @return Default page of ProjectForge. Currently CalendarPage is the default page (e. g. to redirect after login if no forward url is
   *         specified).
   */
  public static String getDefaultPageUrl()
  {
    return getBookmarkablePageUrl(CalendarPage.class);
  }

  /**
   * Removes sessionId from url if exists.
   * @param url
   */
  public static String removeSessionId(final String url)
  {
    if (url == null) {
      return null;
    }
    return url.replaceFirst(";jsessionid=[a-zA-Z0-9]*", "");
  }

  /**
   * Removes sessionId from url if exists.
   * @param url
   */
  public static String removeSessionId(final CharSequence url)
  {
    if (url == null) {
      return null;
    }
    return removeSessionId((String) url);
  }

  /**
   * @return Default page of ProjectForge. Currently CalendarPage is the default page (e. g. to redirect after cancel if no other return
   *         page is specified).
   */
  public static Class< ? extends AbstractSecuredPage> getDefaultPage()
  {
    return CalendarPage.class;
  }

  /**
   * If value is null or value is default value then nothing is done. Otherwise the given value is added as page parameter under the given
   * key. Dates and TimePeriods are converted and can be gotten by {@link #getPageParameter(PageParameters, String, Class)}.
   * @param pageParameters
   * @param key
   * @param value
   * @see ClassHelper#isDefaultType(Class, Object)
   */
  public static void putPageParameter(final PageParameters pageParameters, final String key, final Object value)
  {
    if (value == null) {
      // Do not put null values to page parameters.
    } else if (ClassHelper.isDefaultType(value.getClass(), value)) {
      // Do not put default values to page parameters.
    } else if (value instanceof Date) {
      pageParameters.put(key, ((Date) value).getTime());
    } else if (value instanceof TimePeriod) {
      pageParameters.put(key, ((TimePeriod) value).getFromDate().getTime() + "-" + ((TimePeriod) value).getToDate().getTime());
    } else {
      pageParameters.put(key, value);
    }
  }

  public static void putPageParameters(final Object bean, final PageParameters pageParameters, final String prefix,
      final String[] bookmarkableProperties)
  {
    if (bookmarkableProperties == null) {
      return;
    }
    final String pre = prefix != null ? prefix + "." : "";
    for (final String propertyString : bookmarkableProperties) {
      final String[] propertyAndAlias = getPropertyAndAlias(propertyString);
      final Object value = BeanHelper.getProperty(bean, propertyAndAlias[0]);
      WicketUtils.putPageParameter(pageParameters, pre + propertyAndAlias[1], value);
    }
  }

  /**
   * @param pageParameters
   * @param key
   * @param objectType
   * @see #putPageParameter(PageParameters, String, Object)
   */
  public static Object getPageParameter(final PageParameters pageParameters, final String key, final Class< ? > objectType)
  {
    if (objectType.isAssignableFrom(Date.class) == true) {
      final Long longValue = pageParameters.getAsLong(key);
      if (longValue == null) {
        return null;
      }
      return new Date(longValue);
    } else if (objectType.isAssignableFrom(Boolean.class) == true) {
      return pageParameters.getAsBoolean(key);
    } else if (objectType.isPrimitive() == true) {
      if (Boolean.TYPE.equals(objectType)) {
        return pageParameters.getAsBoolean(key);
      }
    } else if (Enum.class.isAssignableFrom(objectType) == true) {
      final String sValue = pageParameters.getString(key);
      @SuppressWarnings("unchecked")
      final Enum< ? > en = Enum.valueOf((Class<Enum>) objectType, sValue);
      return en;
    } else if (objectType.isAssignableFrom(Integer.class) == true) {
      return pageParameters.getAsInteger(key);
    } else if (objectType.isAssignableFrom(String.class) == true) {
      return pageParameters.getString(key);
    } else if (objectType.isAssignableFrom(TimePeriod.class) == true) {
      final String sValue = pageParameters.getString(key);
      if (sValue == null) {
        return null;
      }
      final int pos = sValue.indexOf('-');
      if (pos < 0) {
        log.warn("PageParameter of type TimePeriod '" + objectType.getName() + "' in wrong format: " + sValue);
        return null;
      }
      final Long fromTime = NumberHelper.parseLong(sValue.substring(0, pos));
      final Long toTime = pos < sValue.length() - 1 ? NumberHelper.parseLong(sValue.substring(pos + 1)) : null;
      return new TimePeriod(fromTime != null ? new Date(fromTime) : null, toTime != null ? new Date(toTime) : null);
    } else {
      log.error("PageParameter of type '" + objectType.getName() + "' not yet supported.");
    }
    return null;
  }

  /**
   * At least one parameter should be given for setting the fill the bean with all book-markable properties (absent properties will be set
   * to zero).
   * @param bean
   * @param parameters
   * @param prefix
   * @param bookmarkableProperties
   */
  public static void evaluatePageParameters(final Object bean, final PageParameters parameters, final String prefix,
      final String[] bookmarkableProperties)
  {
    if (bookmarkableProperties == null) {
      return;
    }
    final String pre = prefix != null ? prefix + "." : "";
    // First check if any parameter is given:
    boolean useParameters = false;
    for (final String str : bookmarkableProperties) {
      final String[] propertyAndAlias = getPropertyAndAlias(str);
      final String property = propertyAndAlias[0];
      final String alias = propertyAndAlias[1];
      if (parameters.containsKey(pre + property) == true || parameters.containsKey(pre + alias) == true) {
        useParameters = true;
        break;
      }
    }
    if (useParameters == false) {
      // No book-markable parameters found.
      return;
    }
    for (final String str : bookmarkableProperties) {
      final String[] propertyAndAlias = getPropertyAndAlias(str);
      final String property = propertyAndAlias[0];
      final String alias = propertyAndAlias[1];
      String key = null;
      if (parameters.containsKey(pre + property) == true) {
        key = property;
      } else if (parameters.containsKey(pre + alias) == true) {
        key = alias;
      }
      if (bean instanceof ISelectCallerPage) {
        if (key == null) {
          ((ISelectCallerPage) bean).unselect(property);
        } else {
          ((ISelectCallerPage) bean).select(property, parameters.getString(pre + key));
        }
      } else {
        try {
          final Method method = BeanHelper.determineGetter(bean.getClass(), property);
          if (key == null) {
            BeanHelper.setProperty(bean, property, ClassHelper.getDefaultType(method.getReturnType()));
          } else {
            final Object value = WicketUtils.getPageParameter(parameters, pre + key, method.getReturnType());
            BeanHelper.setProperty(bean, property, value);
          }
        } catch (Exception ex) {
          log.warn("Property '" + key + "' not found. Ignoring URL parameter.");
        }
      }
    }
  }

  public static String[] getPropertyAndAlias(final String propertyString)
  {
    final int pos = propertyString.indexOf('|');
    final String property;
    final String alias;
    if (pos >= 0) {
      property = propertyString.substring(0, pos);
      alias = propertyString.substring(pos + 1);
    } else {
      // No alias given.
      property = propertyString;
      alias = propertyString;
    }
    return new String[] { property, alias};
  }

  private static StringBuffer buildAbsoluteUrl(final Request request)
  {
    final HttpServletRequest httpServletRequest = ((ServletWebRequest) request).getHttpServletRequest();
    final StringBuffer buf = buildAbsoluteBaseUrl(request, httpServletRequest);
    buf.append(httpServletRequest.getServletPath());
    return buf;
  }

  private static StringBuffer buildAbsoluteBaseUrl(final Request request, final HttpServletRequest httpServletRequest)
  {
    final StringBuffer buf = new StringBuffer();
    String scheme = httpServletRequest.getScheme();
    buf.append(scheme).append("://");
    buf.append(httpServletRequest.getServerName());
    int port = httpServletRequest.getServerPort();
    if ("https".equals(scheme) == true) {
      if (port != 443) {
        buf.append(":").append(port);
      }
    } else if ("http".equals(scheme) == true) {
      if (port != 80) {
        buf.append(":").append(port);
      }
    } else {
      log.warn("Oups, unkown protocol: " + scheme);
      buf.append(":").append(port);
    }
    buf.append(httpServletRequest.getContextPath());
    return buf;
  }

  /**
   * Adds onclick attribute with "javascript:rowClick(this);".
   * @param row Html tr element.
   */
  public static void addRowClick(final Component row)
  {
    row.add(new SimpleAttributeModifier("onclick", "javascript:rowClick(this);"));
  }

  /**
   * 
   * @return
   */
  public static ContextImage getInvisibleDummyImage(final String id, final Response response)
  {
    final ContextImage image = new ContextImage(id, WicketUtils.getImageUrl(response, WebConstants.IMAGE_SPACER));
    image.setVisible(false);
    return image;
  }

  /**
   * Uses "jiraSupportTooltipImage" as component id.
   * @param response
   * @param parent
   * @see #getJIRASupportTooltipImage(String, Response, Component)
   */
  public static TooltipImage getJIRASupportTooltipImage(final Response response, final Component parent)
  {
    return getJIRASupportTooltipImage("jiraSupportTooltipImage", response, parent);
  }

  /**
   * Component id is jiraSupportTooltipImage: &lt;img wicket:id="jiraSupportTooltipImage" /&gt;
   * @param response
   * @param parent Needed for localization (call getString()).
   * @return Image with tooltip. The image is not visible if JIRA is not configured in the config.xml.
   */
  public static TooltipImage getJIRASupportTooltipImage(final String componentId, final Response response, final Component parent)
  {
    final TooltipImage image = new TooltipImage(componentId, response, WebConstants.IMAGE_INFO, parent
        .getString("tooltip.jiraSupport.field"));
    if (isJIRAConfigured() == false) {
      image.setVisible(false);
    }
    return image;
  }

  public static final boolean isJIRAConfigured()
  {
    return Configuration.getInstance().isJIRAConfigured();
  }

  /**
   * Usage in markup: &lt;img wicket:id="addPositionImage" /&gt;
   * @param componentId
   * @param response
   * @param tooltip
   * @return
   */
  public static Component getAddRowImage(final String componentId, final Response response, final String tooltip)
  {
    return new TooltipImage(componentId, response, WebConstants.IMAGE_ADD, tooltip);
  }

  /**
   * Usage in markup: &lt;img wicket:id="deletePositionImage" /&gt;
   * @param parent Needed for i18n of tooltip.
   * @param componentId
   * @param response
   * @param obj If the obj has an id then a mark-as-deleted tool tip will be shown, otherwise a delete tool tip.
   * @return
   * @see #getMarkAsDeletedTooltipImage(MarkupContainer, String, Response, BaseDO)
   * @see #getDeleteTooltipImage(MarkupContainer, String, Response, BaseDO)
   */
  public static Component getDeleteRowImage(final MarkupContainer parent, final String componentId, final Response response,
      final BaseDO< ? > obj)
  {
    if (obj.getId() != null) {
      return getMarkAsDeletedTooltipImage(parent, componentId, response);
    } else {
      return getDeleteTooltipImage(parent, componentId, response);
    }
  }

  public static Component getDeleteTooltipImage(final MarkupContainer parent, final String componentId, final Response response)
  {
    return new TooltipImage(componentId, response, WebConstants.IMAGE_DELETE, parent.getString("tooltip.entry.markAsDeleted"));
  }

  public static Component getMarkAsDeletedTooltipImage(final MarkupContainer parent, final String componentId, final Response response)
  {
    return new TooltipImage(componentId, response, WebConstants.IMAGE_DELETE, parent.getString("tooltip.entry.delete"));
  }

  /**
   * Usage in markup: &lt;img wicket:id="deletePositionImage" /&gt;
   * @param parent Needed for i18n of tooltip.
   * @param componentId
   * @param response
   * @param obj If the obj has an id then a mark-as-deleted tool tip will be shown, otherwise a delete tool tip.
   * @return
   */
  public static Component getUndeleteRowImage(final MarkupContainer parent, final String componentId, final Response response)
  {
    return new TooltipImage(componentId, response, WebConstants.IMAGE_UNDELETE, parent.getString("tooltip.entry.undelete"));
  }

  /**
   * Add JavaScript function showDeleteEntryQuestionDialog(). Depending on BaseDao.isHistorizable() a delete or mark-as-deleted question
   * will be displayed. Usage in markup: &lt;script wicket:id="showDeleteEntryQuestionDialog"&gt;[...]&lt;/script&gt;
   * @param parent
   * @param dao
   */
  public static void addShowDeleteRowQuestionDialog(final MarkupContainer parent, final BaseDao< ? > dao)
  {
    final StringBuffer buf = new StringBuffer();
    buf.append("function showDeleteEntryQuestionDialog() {\n").append("  return window.confirm('");
    if (dao.isHistorizable() == true) {
      buf.append(parent.getString("question.markAsDeletedQuestion"));
    } else {
      buf.append(parent.getString("question.deleteQuestion"));
    }
    buf.append("');\n}\n");
    parent.add(new Label("showDeleteEntryQuestionDialog", buf.toString()).setEscapeModelStrings(false).add(
        new SimpleAttributeModifier("type", "text/javascript")));
  }

  /**
   * @param parent Only for i18n needed.
   * @param startTime Start time or null.
   * @param stopTime Stop time or null.
   * @return The weeks of year range for the given start an stop time.
   */
  public static String getCalendarWeeks(final MarkupContainer parent, final Date startTime, final Date stopTime)
  {
    int fromWeek = -1;
    int toWeek = -1;
    if (startTime != null) {
      fromWeek = DateHelper.getWeekOfYear(startTime);
    }
    if (stopTime != null) {
      toWeek = DateHelper.getWeekOfYear(stopTime);
    }
    if (fromWeek < 0 && toWeek < 0) {
      return "";
    }
    final StringBuffer buf = new StringBuffer();
    buf.append(parent.getString("calendar.weekOfYearShortLabel")).append(" ");
    if (fromWeek >= 0) {
      buf.append(StringHelper.format2DigitNumber(fromWeek));
      if (toWeek == -1) {
        buf.append("-");
      } else if (toWeek != fromWeek) {
        buf.append("-").append(StringHelper.format2DigitNumber(toWeek));
      }
    } else {
      buf.append("-").append(StringHelper.format2DigitNumber(toWeek));
    }
    return buf.toString();
  }

  /**
   * @param startTime Start time or null.
   * @param stopTime Stop time or null.
   */
  public static String getUTCDates(final Date startTime, final Date stopTime)
  {
    final StringBuffer buf = new StringBuffer();
    final DateHolder start = startTime != null ? new DateHolder(startTime) : null;
    final DateHolder stop = stopTime != null ? new DateHolder(stopTime) : null;
    if (start != null) {
      buf.append(DateHelper.TECHNICAL_ISO_UTC.get().format(start.getDate()));
      if (stop != null) {
        buf.append(" - ");
      }
    }
    if (stop != null) {
      buf.append(DateHelper.TECHNICAL_ISO_UTC.get().format(stop.getDate()));
    }
    return buf.toString();
  }

  public static LabelValueChoiceRenderer<Long> getDatumChoiceRenderer(final int lastNDays)
  {
    final LabelValueChoiceRenderer<Long> datumChoiceRenderer = new LabelValueChoiceRenderer<Long>();
    for (int i = 0; i > -lastNDays; i--) {
      final DayHolder day = new DayHolder();
      day.add(Calendar.DAY_OF_YEAR, i);
      datumChoiceRenderer.addValue(day.getSQLDate().getTime(), DateTimeFormatter.instance().getFormattedDate(day.getSQLDate(),
          DateTimeFormatter.I18N_KEY_LONG_DATE_FORMAT));
    }
    return datumChoiceRenderer;
  }

  /**
   * Should be called for every component for which a tooltip (title attribute) is set. Adds a style attribute for the given component
   * (font-style: italic;). <br/>
   * Will be ignored for instances of FormComponent.
   * @param component
   */
  public static Component setStyleHasTooltip(final Component component)
  {
    if (FormComponent.class.isAssignableFrom(component.getClass()) == true
        || ContextImage.class.isAssignableFrom(component.getClass()) == true) {
      return component;
    }
    component.add(new AttributeAppendModifier("class", " hastooltip")); // TODO: KAI humm, perhaps it's better to add that space per default?
    return component;
  }

  public static String getHighlightedRowCssStyle()
  {
    return "background-color: #ffcccc;";
  }

  /**
   * Current implementation of tool tip is the format &lt;title&gt; - &lt;text&gt;. The title will be shown in bold letters. Should be used
   * whenever a tool tip is shown. <br/>
   * Use addTooltip(...) methods.
   * @param title Can be null.
   * @param text Can be null.
   */
  public static String createTooltip(final String title, final String text)
  {
    if (StringUtils.isBlank(title) == true) {
      if (text == null) {
        return "";
      } else {
        return " - " + text;
      }
    } else {
      if (text == null) {
        return title;
      } else {
        return title + " - " + text;
      }
    }
  }

  /**
   * Adds a SimpleAttributeModifier("title", ...) to the given component.
   * @param component
   * @param title
   * @param text
   * @see #createTooltip(String, String)
   * @see #setStyleHasTooltip(Component)
   */
  public static Component addTooltip(final Component component, final String title, final String text)
  {
    return addTooltip(component, title, text, false);
  }

  /**
   * Adds a SimpleAttributeModifier("title", ...) to the given component.
   * @param component
   * @param text
   * @see #createTooltip(String, String)
   * @see #setStyleHasTooltip(Component)
   */
  public static Component addTooltip(final Component component, final String text)
  {
    return addTooltip(component, null, text, false);
  }

  /**
   * Adds a SimpleAttributeModifier("title", ...) to the given component. Does not modify the given tool tip text!
   * @param component
   * @param text
   * @see #setStyleHasTooltip(Component)
   */
  public static Component addTooltip(final Component component, final IModel<String> text)
  {
    component.add(new AttributeModifier("title", true, text));
    setStyleHasTooltip(component);
    return component;
  }

  /**
   * Adds a SimpleAttributeModifier("title", ...) to the given component.
   * @param component
   * @param text
   * @param suppressStyleChange If true then the style attribute will not be modified. Default is false.
   * @see #createTooltip(String, String)
   * @see #setStyleHasTooltip(Component)
   */
  public static Component addTooltip(final Component component, final String text, final boolean suppressStyleChange)
  {
    return addTooltip(component, null, text, suppressStyleChange);
  }

  /**
   * Adds a SimpleAttributeModifier("title", ...) to the given component.
   * @param component
   * @param title
   * @param text
   * @param suppressStyleChange If true then the style attribute will not be modified. Default is false.
   * @see #createTooltip(String, String)
   * @see #setStyleHasTooltip(Component)
   */
  public static Component addTooltip(final Component component, final String title, final String text, final boolean suppressStyleChange)
  {
    component.add(new SimpleAttributeModifier("title", createTooltip(title, text)));
    if (suppressStyleChange == false) {
      setStyleHasTooltip(component);
    }
    return component;
  }

  /**
   * Searchs the attribute behavior (SimpleAttributeModifier or AttibuteApendModifier) with the given attribute name and returns it if
   * found, otherwise null.
   * @param comp
   * @param name Name of attribute.
   */
  public static AbstractBehavior getAttributeModifier(final Component comp, final String name)
  {
    for (final IBehavior behavior : comp.getBehaviors()) {
      if (behavior instanceof AttributeAppendModifier && name.equals(((AttributeAppendModifier) behavior).getAttribute()) == true) {
        return (AttributeAppendModifier) behavior;
      } else if (behavior instanceof SimpleAttributeModifier && name.equals(((SimpleAttributeModifier) behavior).getAttribute()) == true) {
        return (SimpleAttributeModifier) behavior;
      }
    }
    return null;
  }

  /**
   * Calls {@link Component#setResponsePage(Page)}. If the responseItem is an instance of a Page then setResponse for this Page is called
   * otherwise setResponse is called via {@link Component#getPage()}.
   * @param component
   * @param responseItem Page or Component.
   */
  public static void setResponsePage(final Component component, final Component responseItem)
  {
    if (responseItem instanceof Page) {
      component.setResponsePage((Page) responseItem);
    } else {
      component.setResponsePage(responseItem.getPage());
    }
  }

  /**
   * Casts callerPage to Component and calls {@link #setResponsePage(Component, Component)}.
   * @param component
   * @param callerPage Must be an instance of Component (otherwise a ClassCastException is thrown).
   */
  public static void setResponsePage(final Component component, final ISelectCallerPage callerPage)
  {
    setResponsePage(component, (Component) callerPage);
  }
}
