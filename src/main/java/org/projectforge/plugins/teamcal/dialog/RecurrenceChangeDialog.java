/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.dialog;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventDao;
import org.projectforge.web.dialog.PFDialog;
import org.projectforge.web.wicket.components.SingleButtonPanel;

import de.micromata.wicket.ajax.AjaxCallback;

/**
 * Dialog which appears, when a user tries to modify an recurrent event
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class RecurrenceChangeDialog extends PFDialog
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecurrenceChangeDialog.class);

  private static final long serialVersionUID = 7266725860088619248L;

  private TeamEventDO eventDo;

  @SpringBean
  private TeamEventDao teamEventDao;

  /**
   * @param id
   * @param titleModel
   */
  public RecurrenceChangeDialog(final String id, final IModel<String> titleModel)
  {
    super(id, titleModel);
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    // add future only change callback
    final AjaxCallback cancelCallback = new AjaxCallback() {
      private static final long serialVersionUID = 7852511931690947544L;

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        close(target);
      }
    };
    appendNewAjaxActionButton(cancelCallback, getString("cancel"), SingleButtonPanel.CANCEL);
    // add future only change callback
    final AjaxCallback futureCallback = new AjaxCallback() {
      private static final long serialVersionUID = 7852511931690947544L;

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        onChangeFutureOnlyEventsSelected(target, eventDo);
      }
    };
    appendNewAjaxActionButton(futureCallback, getString("plugins.teamcal.event.recurrence.change.future"), SingleButtonPanel.DEFAULT_SUBMIT);

    // add future only change callback
    final AjaxCallback singleCallback = new AjaxCallback() {
      private static final long serialVersionUID = 7852511931690947544L;

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        onChangeSingleEventSelected(target, eventDo);
      }
    };
    appendNewAjaxActionButton(singleCallback, getString("plugins.teamcal.event.recurrence.change.single"), SingleButtonPanel.DEFAULT_SUBMIT);
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#getDialogContent(java.lang.String)
   */
  @Override
  protected Component getDialogContent(final String wicketId)
  {
    return new Label(wicketId, new ResourceModel("plugins.teamcal.event.recurrence.change.content"));
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#open(org.apache.wicket.ajax.AjaxRequestTarget)
   */
  @Override
  public void open(final AjaxRequestTarget target)
  {
    log.error("Dear developer, please use open(target, eventDo).");
    throw new UnsupportedOperationException();
  }

  public void open(final AjaxRequestTarget target, final TeamEventDO eventDo)
  {
    this.eventDo = eventDo;
    super.open(target);
  }

  protected void onChangeFutureOnlyEventsSelected(final AjaxRequestTarget target, final TeamEventDO event)
  {
    // TODO kai: implement change of all future events here
  }

  protected void onChangeSingleEventSelected(final AjaxRequestTarget target, final TeamEventDO event)
  {
    // TODO kai: implement change of single event here
  }

}
