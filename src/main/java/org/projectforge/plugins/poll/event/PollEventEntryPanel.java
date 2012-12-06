/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.event;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.joda.time.DateTime;
import org.projectforge.web.wicket.flowlayout.IconButtonPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollEventEntryPanel extends Panel
{
  private static final long serialVersionUID = 8389532050901086582L;

  /**
   * @param id
   * @param model
   */
  public PollEventEntryPanel(final String id, final PollEventDO poll)
  {
    super(id);

    final DateTime start = poll.getStartDate();
    final DateTime end = poll.getEndDate();

    add(new Label("startDate", "Start: " + DateFormatUtils.format(start.getMillis(), "dd.MM.yyyy HH:mm")));  // TODO Max i18n
    add(new Label("endDate", "Ende: " + DateFormatUtils.format(end.getMillis(), "dd.MM.yyyy HH:mm"))); // TODO Max i18n!!

    final IconButtonPanel iconButton = new IconButtonPanel("delete", IconType.MINUS_THICK);
    add(iconButton);
  }
}
