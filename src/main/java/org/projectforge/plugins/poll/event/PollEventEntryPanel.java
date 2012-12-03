/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.event;

import net.ftlines.wicket.fullcalendar.callback.SelectedRange;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
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
  public PollEventEntryPanel(String id, IModel<SelectedRange> model)
  {
    super(id, model);

    SelectedRange range = model.getObject();
    DateTime start = range.getStart();
    DateTime end = range.getEnd();

    add(new Label("startDate", "Start: " + DateFormatUtils.format(start.getMillis(), "dd.MM.yyyy HH:mm")));
    add(new Label("endDate", "Ende: " + DateFormatUtils.format(end.getMillis(), "dd.MM.yyyy HH:mm")));

    IconButtonPanel iconButton = new IconButtonPanel("delete", IconType.MINUS_THICK);
    add(iconButton);
  }
}
