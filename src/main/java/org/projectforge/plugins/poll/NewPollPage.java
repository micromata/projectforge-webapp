/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.poll.event.PollEventEditPage;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class NewPollPage extends AbstractSecuredPage
{
  private static final long serialVersionUID = -3852729293168721111L;

  @SpringBean(name = "pollDao")
  private PollDao pollDao;

  private GridBuilder gridBuilder;

  private final IModel<PollDO> pollDoModel;

  /**
   * @param parameters
   */
  public NewPollPage(PageParameters parameters)
  {
    super(parameters);
    pollDoModel = new Model<PollDO>(new PollDO());
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();

    final RepeatingView flowform = new RepeatingView("flowform");
    // body.add(flowform);
    gridBuilder = new GridBuilder(flowform, getMySession());

    gridBuilder.newGrid8();

    final FieldsetPanel fsTitle = gridBuilder.newFieldset(getString("plugins.poll.new.title"), true);
    MaxLengthTextField titleField = new MaxLengthTextField(fsTitle.getTextFieldId(), new PropertyModel<String>(pollDoModel, "title"));
    titleField.setRequired(true);
    fsTitle.add(titleField);

    final FieldsetPanel fsLocation = gridBuilder.newFieldset(getString("plugins.poll.new.location"), true);
    final PFAutoCompleteMaxLengthTextField locationTextField = new PFAutoCompleteMaxLengthTextField(fsLocation.getTextFieldId(),
        new PropertyModel<String>(pollDoModel, "location")) {
      private static final long serialVersionUID = 2008897410054999896L;

      @Override
      protected List<String> getChoices(final String input)
      {
        return pollDao.getAutocompletion("location", input);
      }
    };
    fsLocation.add(locationTextField);

    final FieldsetPanel fsDescription = gridBuilder.newFieldset(getString("plugins.poll.new.description"), true);
    MaxLengthTextArea descriptionField = new MaxLengthTextArea(fsDescription.getTextAreaId(), new PropertyModel<String>(pollDoModel,
        "description"));
    fsDescription.add(descriptionField);

    // Cancel button
    AjaxButton cancel = new AjaxButton("button") {

      private static final long serialVersionUID = -2517605954536886155L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
      {
        setResponsePage(CalendarPage.class);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form< ? > form)
      {
        error("asdf");
      }
    };
    SingleButtonPanel cancelPanel = new SingleButtonPanel("cancel", cancel, getString("cancel"), SingleButtonPanel.CANCEL);

    // Confirm button
    Button confirm = new Button(SingleButtonPanel.WICKET_ID) {
      static final long serialVersionUID = -7779593314951993472L;

      @Override
      public final void onSubmit()
      {
        setResponsePage(new PollEventEditPage(getPageParameters(), pollDoModel));
      }
    };
    SingleButtonPanel confirmPanel = new SingleButtonPanel("confirm", confirm, getString("plugins.poll.new.continue"),
        SingleButtonPanel.DEFAULT_SUBMIT);

    Form<String> form = new Form<String>("pollForm");
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

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("plugins.poll.title");
  }
}
