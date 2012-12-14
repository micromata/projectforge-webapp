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

package org.projectforge.plugins.poll;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.poll.event.PollEventEditPage;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class NewPollPage extends PollBasePage
{
  private static final long serialVersionUID = -3852729293168721111L;

  @SpringBean(name = "pollDao")
  private PollDao pollDao;

  private final IModel<PollDO> pollDoModel;

  /**
   * @param parameters
   */
  public NewPollPage(PageParameters parameters)
  {
    super(parameters);
    pollDoModel = new Model<PollDO>(new PollDO());
    pollDoModel.getObject().setOwner(getUser());
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();

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

  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("plugins.poll.title");
  }

  /**
   * @see org.projectforge.plugins.poll.PollBasePage#onConfirm()
   */
  @Override
  protected void onConfirm()
  {
    setResponsePage(new PollEventEditPage(getPageParameters(), pollDoModel));
  }

  /**
   * @see org.projectforge.plugins.poll.PollBasePage#onCancel()
   */
  @Override
  protected void onCancel()
  {
    setResponsePage(CalendarPage.class);
  }
}
