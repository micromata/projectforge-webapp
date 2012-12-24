/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

import java.io.StringReader;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.projectforge.web.wicket.components.DropFileContainer;

/**
 * Adaption of {@link DropFileContainer} for dropped {@link Calendar} files.
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public abstract class DropIcsPanel extends DropFileContainer
{
  private static final long serialVersionUID = 1094945928912822172L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DropIcsPanel.class);

  /**
   * @param id
   */
  public DropIcsPanel(final String id)
  {
    super(id);
  }

  /**
   * @see org.projectforge.web.wicket.components.DropFileContainer#onStringImport(org.apache.wicket.ajax.AjaxRequestTarget,
   *      java.lang.String)
   */
  @Override
  protected void onStringImport(final AjaxRequestTarget target, final String string)
  {

    try {
      final CalendarBuilder builder = new CalendarBuilder();
      final Calendar calendar = builder.build(new StringReader(string));
      onIcsImport(target, calendar);
    } catch (final Exception ex) {
      // TODO ju: handle exception
      log.fatal("unable to import dropped calendar", ex);
    }
  }

  protected abstract void onIcsImport(final AjaxRequestTarget target, final Calendar calendar);
}
