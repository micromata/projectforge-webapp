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

package org.projectforge.web.wicket.components;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;
import org.projectforge.web.I18nCore;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DatePickerUtils
{
  public static void renderHead(final IHeaderResponse response, final Locale locale, final String id, final boolean autosubmit)
  {
    final String datePickerLocalizationFile = I18nCore.getDatePickerLocalizationFile(locale);
    if (datePickerLocalizationFile != null) {
      response.renderJavaScriptReference(datePickerLocalizationFile);
    }
    final StringBuffer buf = new StringBuffer();
    final String loc = I18nCore.getDatePickerLocale(locale);
    buf.append("$(function() {\n");
    if (loc != null) {
      buf.append("  $.datepicker.setDefaults($.datepicker.regional['").append(loc).append("']);\n");
    }
    buf.append("  $.datepicker.setDefaults({\n");
    buf.append("    showAnim : 'fadeIn',\n");
    // if (isGerman == true) {
    // buf.append("    showWeek : true\n");
    // }
    buf.append("  });\n");
    buf.append("});");
    response.renderJavaScript(buf, "datepicker");
    response.renderJavaScript(getDatePickerInitJavaScript(id, autosubmit), null);
  }

  public static String getDatePickerInitJavaScript(final String id, final boolean autosubmit)
  {
    final StringBuffer buf = new StringBuffer();
    buf.append("$(function() { $('#").append(id).append("')");
    String dateFormat = DateFormats.getFormatString(DateFormatType.DATE);
    if (StringUtils.isNotBlank(dateFormat) == true) {
      dateFormat = dateFormat.toLowerCase().replace("yy", "y"); // Date format conversion for DatePicker of jquery ui.
      buf.append(".datepicker({ dateFormat : '");
      buf.append(dateFormat);
      buf.append("', showButtonPanel: true");
    } else {
      buf.append(".datepicker({ showButtonPanel: true");
    }
    if (autosubmit == true) {
      buf.append(", onSelect: function (dateText, inst) { $(this).parents('form').submit(); }");
    }
    buf.append(" });\n");
    buf.append("});");
    return buf.toString();
  }
}
