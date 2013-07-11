/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;

/**
 * Text field component with date selector based on joda time's
 * DateTime class.
 * @author Sweeps <pf@byte-storm.com>
 */
public class ChimneyJodaDateTimeField extends DateTextField{
  private static final long serialVersionUID = 8520421267633911586L;

  public ChimneyJodaDateTimeField(final String id)
  {
    super(id);
    addDatePicker();
  }

  private void addDatePicker()
  {
    final DatePicker dp = new DatePicker(){
      private static final long serialVersionUID = 5505056082509106462L;

      @Override
      protected String getDatePattern()
      {

        return DateFormats.getFormatString(DateFormatType.DATE);
        //return ChimneyJodaDateConverter.DATE_TIME_PATTERN;
      }

    };
    dp.setShowOnFieldClick(true);
    dp.setAutoHide(true);
    add(dp);
  }

  @Override
  public <C> IConverter<C> getConverter(final Class<C> c)
  {
    return new ChimneyJodaDateConverter<C>();
  }
}
