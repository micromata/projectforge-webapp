/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.plugins.marketing;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.SingleButtonPanel;


public class AddressCampaignValueMassUpdateForm extends AbstractForm<AddressCampaignValueDO, AddressCampaignValueMassUpdatePage>
{
  private static final long serialVersionUID = -6785832818308468337L;

  protected AddressCampaignValueDO data;

  public AddressCampaignValueMassUpdateForm(final AddressCampaignValueMassUpdatePage parentPage, final AddressCampaignDO addressCampaign)
  {
    super(parentPage);
    data = new AddressCampaignValueDO();
    data.setAddressCampaign(addressCampaign);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    final LabelValueChoiceRenderer<String> valueChoiceRenderer = new LabelValueChoiceRenderer<String>(data.getAddressCampaign().getValuesArray());
    @SuppressWarnings("unchecked")
    final DropDownChoice valueChoice = new DropDownChoice("value", new PropertyModel(data, "value"), valueChoiceRenderer.getValues(),
        valueChoiceRenderer);
    valueChoice.setNullValid(false);
    add(valueChoice);

    add(new MaxLengthTextArea("comment", new PropertyModel<String>(data, "comment")));
    final Button cancelButton = new Button("button", new Model<String>(getString("cancel"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onCancelSubmit();
      }
    };
    cancelButton.add(WebConstants.BUTTON_CLASS_CANCEL);
    cancelButton.setDefaultFormProcessing(false);
    final SingleButtonPanel cancelButtonPanel = new SingleButtonPanel("cancel", cancelButton);
    add(cancelButtonPanel);
    final Button updateAllButton = new Button("button", new Model<String>(getString("updateAll"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onUpdateAllSubmit();
      }
    };
    updateAllButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    updateAllButton.add(new SimpleAttributeModifier("onclick", "return showUpdateQuestionDialog()"));
    setDefaultButton(updateAllButton);
    final SingleButtonPanel updateAllButtonPanel = new SingleButtonPanel("updateAll", updateAllButton);
    add(updateAllButtonPanel);
  }
}
