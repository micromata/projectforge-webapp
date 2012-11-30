/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll;

import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public abstract class PollBasePage extends AbstractSecuredPage
{
  private static final long serialVersionUID = -1876054200928030613L;

  protected GridBuilder gridBuilder;

  protected Form<String> form;

  /**
   * @param parameters
   */
  public PollBasePage(PageParameters parameters)
  {
    super(parameters);
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();

    final RepeatingView flowform = new RepeatingView("flowform");
    gridBuilder = new GridBuilder(flowform, getMySession());

    // Cancel button
    Button cancel = new Button(SingleButtonPanel.WICKET_ID) {
      static final long serialVersionUID = -7779593314951993472L;

      @Override
      public final void onSubmit()
      {
        PollBasePage.this.onCancel();
      }
    };
    SingleButtonPanel cancelPanel = new SingleButtonPanel("cancel", cancel, getString("cancel"), SingleButtonPanel.CANCEL);

    // Confirm button
    Button confirm = new Button(SingleButtonPanel.WICKET_ID) {
      static final long serialVersionUID = -7779593314951993472L;

      @Override
      public final void onSubmit()
      {
        PollBasePage.this.onConfirm();
      }
    };
    SingleButtonPanel confirmPanel = new SingleButtonPanel("confirm", confirm, getString("plugins.poll.new.continue"),
        SingleButtonPanel.DEFAULT_SUBMIT);

    form = new Form<String>("pollForm");
    body.add(form);
    form.add(cancelPanel);
    form.add(confirmPanel);
    form.add(flowform);

    final ContainerFeedbackMessageFilter containerFeedbackMessageFilter = new ContainerFeedbackMessageFilter(this);
    final WebMarkupContainer feedbackContainer = new WebMarkupContainer("feedbackContainer") {
      private static final long serialVersionUID = -2676548030393266940L;

      /**
       * @see org.apache.wicket.Component#isVisible()
       */
      @Override
      public boolean isVisible()
      {
        return MySession.get().getFeedbackMessages().hasMessage(containerFeedbackMessageFilter);
      }
    };
    form.add(feedbackContainer);
    feedbackContainer.add(new FeedbackPanel("feedback", containerFeedbackMessageFilter));
  }

  protected abstract void onConfirm();

  protected abstract void onCancel();

  protected String setCancelButtonTitle(String title)
  {
    return title;
  }

  protected String setConfirmButtonTitle(String title)
  {
    return title;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return null;
  }

}
