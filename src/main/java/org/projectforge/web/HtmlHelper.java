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

package org.projectforge.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.projectforge.web.core.LocalizerAndUrlBuilder;
import org.projectforge.web.core.PageContextLocalizerAndUrlBuilder;

public class HtmlHelper
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HtmlHelper.class);

  public static final int TAB_WIDTH = 8;

  public static final String IMAGE_INFO_ICON = "/images/information.png";

  /**
   * Only xml characters will be escaped (for compatibility with fop rendering engine).
   * @return
   * @see StringEscapeUtils#escapeXml(String)
   */
  public static final String escapeXml(String str)
  {
    return StringEscapeUtils.escapeXml(str);
  }

  /**
   * Only xml characters will be escaped (for compatibility with fop rendering engine).
   * @param str The string to convert.
   * @param createLineBreaks If true then new lines will be replaced by newlines and &lt;br/&gt;
   * @return
   * @see StringEscapeUtils#escapeHtml(String)
   */
  public static final String escapeHtml(final String str, final boolean createLineBreaks)
  {
    if (str == null) {
      return null;
    }
    final String result = StringEscapeUtils.escapeHtml(str);
    if (createLineBreaks == false) {
      return result;
    } else {
      if (result.contains("\r\n") == true) {
        return StringUtils.replace(result, "\r\n", "<br/>\r\n");
      } else {
        return StringUtils.replace(result, "\n", "<br/>\n");
      }
    }
  }

  /**
   * Returns ' &lt;attribute&gt;="&lt;value&gt;"', e. g. ' width="120px"'.
   * @param attribute
   * @param value
   * @return
   */
  public String attribute(String attribute, String value)
  {
    StringBuffer buf = new StringBuffer();
    return attribute(buf, attribute, value).toString();
  }

  /**
   * Returns ' &lt;attribute&gt;="&lt;value&gt;"', e. g. ' width="120px"'.
   * @param buf
   * @param attribute
   * @param value
   * @return
   */
  public StringBuffer attribute(StringBuffer buf, String attribute, String value)
  {
    return buf.append(" ").append(attribute).append("=\"").append(value).append("\"");
  }

  /**
   * Returns " &lt;attribute&gt;='&lt;value&gt;'", e. g. " width='120px'".
   * @param attribute
   * @param value
   * @return
   */
  public String attributeSQ(String attribute, String value)
  {
    StringBuffer buf = new StringBuffer();
    return attributeSQ(buf, attribute, value).toString();
  }

  /**
   * Returns " &lt;attribute&gt;='&lt;value&gt;'", e. g. " width='120px'".
   * @param buf
   * @param attribute
   * @param value
   * @return
   */
  public StringBuffer attributeSQ(StringBuffer buf, String attribute, String value)
  {
    return buf.append(" ").append(attribute).append("='").append(value).append("'");
  }

  public String encodeUrl(String url)
  {
    try {
      return URLEncoder.encode(url, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      log.warn(ex);
      return url;
    }
  }

  public String getImageTag(PageContext pageContext, String src)
  {
    StringBuffer buf = new StringBuffer();
    appendImageTag(pageContext, buf, src);
    return buf.toString();
  }

  public HtmlHelper appendImageTag(PageContext pageContext, StringBuffer buf, String src)
  {
    return appendImageTag(pageContext, buf, src, null, null, null, null);
  }

  public HtmlHelper appendImageTag(LocalizerAndUrlBuilder locUrlBuilder, StringBuffer buf, String src, int width, int height)
  {
    return appendImageTag(locUrlBuilder, buf, src, String.valueOf(width), String.valueOf(height), null, null);
  }

  public HtmlHelper appendImageTag(PageContext pageContext, StringBuffer buf, String src, String width, String height)
  {
    return appendImageTag(pageContext, buf, src, width, height, null, null);
  }

  public HtmlHelper appendImageTag(PageContext pageContext, StringBuffer buf, String src, String tooltip)
  {
    return appendImageTag(pageContext, buf, src, null, null, tooltip, null);
  }

  public HtmlHelper appendImageTag(PageContext pageContext, StringBuffer buf, String src, String width, String height, String tooltip)
  {
    return appendImageTag(pageContext, buf, src, width, height, tooltip, null);
  }

  /**
   * For the source the URL will be build via buildUrl();
   * @param buf
   * @param src
   * @param width If less than zero, than this attribute will be ignored.
   * @param height If less than zero, than this attribute will be ignored.
   * @param tooltip If null, than this attribute will be ignored.
   * @param align If null, than this attribute will be ignored.
   */
  public HtmlHelper appendImageTag(PageContext pageContext, StringBuffer buf, String src, String width, String height, String tooltip,
      HtmlAlignment align)
  {
    return appendImageTag(new PageContextLocalizerAndUrlBuilder(pageContext), buf, src, width, height, tooltip, align);
  }

  public HtmlHelper appendImageTag(LocalizerAndUrlBuilder locUrlBuilder, StringBuffer buf, String src)
  {
    return appendImageTag(locUrlBuilder, buf, src, null, null, null, null);
  }

  public HtmlHelper appendImageTag(LocalizerAndUrlBuilder locUrlBuilder, StringBuffer buf, String src, String tooltip)
  {
    return appendImageTag(locUrlBuilder, buf, src, null, null, tooltip, null);
  }

  /**
   * For the source the URL will be build via buildUrl();
   * @param buf
   * @param src
   * @param width If less than zero, than this attribute will be ignored.
   * @param height If less than zero, than this attribute will be ignored.
   * @param tooltip If null, than this attribute will be ignored.
   * @param align If null, than this attribute will be ignored.
   */
  public HtmlHelper appendImageTag(LocalizerAndUrlBuilder locUrlBuilder, StringBuffer buf, String src, String width, String height,
      String tooltip, HtmlAlignment align)
  {

    HtmlTagBuilder tag = new HtmlTagBuilder(buf, "img");
    tag.addAttribute("src", locUrlBuilder.buildUrl(src));
    addTooltip(tag, tooltip);
    tag.addAttribute("width", width);
    tag.addAttribute("height", height);
    tag.addAttribute("border", "0");
    if (align != null) {
      tag.addAttribute("align", align.getString());
    }
    tag.finishEmptyTag();
    return this;
  }

  protected void addTooltip(HtmlTagBuilder tag, String tooltip)
  {
    tag.addAttribute("alt", tooltip);
    tag.addAttribute("title", tooltip);
  }

  /**
   * Creates anchor: &lt;a href="${buildUrl(href)}"&gt;
   * @param pageContext
   * @param buf
   * @param href Will be modified via buildUrl.
   * @return
   */
  public HtmlHelper appendAncorStartTag(PageContext pageContext, StringBuffer buf, String href)
  {
    return appendAncorStartTag(new PageContextLocalizerAndUrlBuilder(pageContext), buf, href);
  }

  /**
   * Creates anchor: &lt;a href="${buildUrl(href)}"&gt;
   * @param pageContext
   * @param buf
   * @param href Will be modified via buildUrl.
   * @return
   */
  public HtmlHelper appendAncorStartTag(LocalizerAndUrlBuilder locUrlBuilder, StringBuffer buf, String href)
  {
    HtmlTagBuilder tag = new HtmlTagBuilder(buf, "a");
    tag.addAttribute("href", locUrlBuilder.buildUrl(href));
    tag.finishStartTag();
    return this;
  }

  /**
   * Creates anchor: &lt;a href="#" onclick='javascript:${method}("${params}")'&gt;
   * @param buf
   * @param params
   * @return
   */
  public HtmlHelper appendAncorOnClickSubmitEventStartTag(StringBuffer buf, String method, String... params)
  {
    Validate.notNull(params);
    HtmlTagBuilder tag = new HtmlTagBuilder(buf, "a");
    tag.addAttribute("href", "#");
    if (params.length == 1) {
      // Standard code in over 90%, so avoid creation of new StringBuffer:
      tag.addAttribute("onclick", "javascript:" + method + "('" + params[0] + "')");
    } else {
      StringBuffer s = new StringBuffer();
      for (int i = 0; i < params.length; i++) {
        s.append(params[i]);
        if (i < params.length - 1) {
          s.append("', '");
        }
      }
      tag.addAttribute("onclick", "javascript:" + method + "('" + s.toString() + "')");
    }
    tag.finishStartTag();
    return this;
  }

  public HtmlHelper appendAncorEndTag(StringBuffer buf)
  {
    buf.append("</a>");
    return this;
  }

  public String getInfoImage()
  {
    return IMAGE_INFO_ICON;
  }

  /**
   * Replaces the new lines of the given string by &lt;br/&gt; and returns the result. Later the Wiki notation should be supported.
   * @param str
   * @param escapeChars If true then the html characters of the given string will be quoted before.
   * @return
   * @see StringEscapeUtils#escapeXml(String)
   */
  public static String formatText(String str, boolean escapeChars)
  {
    if (StringUtils.isEmpty(str) == true) {
      return "";
    }
    String s = str;
    if (escapeChars == true) {
      s = escapeXml(str);
    }
    StringBuffer buf = new StringBuffer();
    boolean doubleSpace = false;
    int col = 0;
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      if (ch == '\n') {
        buf.append("<br/>");
        col = 0;
      } else if (ch == '\r') {
        // Do nothing
      } else if (ch == ' ') {
        if (doubleSpace == true) {
          buf.append("&nbsp;");
        } else {
          buf.append(' ');
        }
      } else if (ch == '\t') {
        do {
          buf.append("&nbsp;");
          ++col;
        } while (col % TAB_WIDTH > 0);
      } else {
        buf.append(ch);
        ++col;
      }
      if (Character.isWhitespace(ch) == true) {
        doubleSpace = true;
      } else {
        doubleSpace = false;
      }
    }
    return buf.toString();
  }

  public String formatXSLFOText(String str, boolean escapeChars)
  {
    String s = str;
    if (escapeChars == true) {
      s = escapeXml(str);
    }
    return StringUtils.replace(s, "\n", "<br/>");
  }
}
