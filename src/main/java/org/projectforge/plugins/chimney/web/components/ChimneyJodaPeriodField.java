/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.joda.time.Period;

/**
 * Wicket text field with conversion from and to Joda Period objects
 * @author Sweeps <pf@byte-storm.com>
 */
public class ChimneyJodaPeriodField extends TextField<Period>{
  public ChimneyJodaPeriodField(final String id, final IModel<Period> model)
  {
    super(id, model);
  }

  private static final long serialVersionUID = 8520421267633911586L;

  public ChimneyJodaPeriodField(final String id)
  {
    super(id, Period.class);
  }

  @Override
  public <C> IConverter<C> getConverter(final Class<C> c)
  {
    return new ChimneyJodaPeriodConverter<C>();
  }
}
