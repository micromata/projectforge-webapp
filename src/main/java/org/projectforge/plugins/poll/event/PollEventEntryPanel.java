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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.joda.time.DateTime;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;
import org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public abstract class PollEventEntryPanel extends Panel
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

    final String pattern = DateFormats.getFormatString(DateFormatType.TIMESTAMP_MINUTES);
    add(new Label("startDate", "Start: " + DateFormatUtils.format(start.getMillis(), pattern)));
    add(new Label("endDate", "Ende: " + DateFormatUtils.format(end.getMillis(), pattern)));

    final AjaxIconButtonPanel iconButton = new AjaxIconButtonPanel("delete", IconType.MINUS_THICK){
      private static final long serialVersionUID = -2464985733387718199L;
      /**
       * @see org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onSubmit(final AjaxRequestTarget target)
      {
        onDeleteClick(target);
      }
    };
    add(iconButton);
  }

  /**
   * @param target
   */
  protected abstract void onDeleteClick(AjaxRequestTarget target);
}
