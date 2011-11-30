/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.orga;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.ConfigXml;
import org.projectforge.orga.ContractDO;
import org.projectforge.orga.ContractStatus;
import org.projectforge.orga.ContractType;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.JiraIssuesPanel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.TooltipImage;

public class ContractEditForm extends AbstractEditForm<ContractDO, ContractEditPage>
{
  private static final long serialVersionUID = -2138017238114715368L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ContractEditForm.class);

  protected DatePanel datePanel, validFromDatePanel, validUntilDatePanel, dueDatePanel, resubmissionDatePanel, signingDatePanel;

  public ContractEditForm(final ContractEditPage parentPage, final ContractDO data)
  {
    super(parentPage, data);
    this.colspan = 4;
  }

  @SuppressWarnings("serial")
  private PFAutoCompleteTextField<String> addAutocompleteTextField(final String property)
  {
    final PFAutoCompleteTextField<String> textField = (new PFAutoCompleteMaxLengthTextField(property, new PropertyModel<String>(data,
        property)) {
      @Override
      protected List<String> getChoices(final String input)
      {
        return parentPage.getBaseDao().getAutocompletion(property, input);
      }
    }.withMatchContains(true).withMinChars(2));
    add(textField);
    return textField;
  }

  @Override
  protected void init()
  {
    super.init();
    add(new MinMaxNumberField<Integer>("number", new PropertyModel<Integer>(data, "number"), 0, 99999999));
    final TooltipImage nummerHelpImage = new TooltipImage("numberHelp", getResponse(), WebConstants.IMAGE_HELP,
        getString("fibu.tooltip.nummerWirdAutomatischVergeben"));
    if (NumberHelper.greaterZero(getData().getNumber()) == true) {
      nummerHelpImage.setVisible(false); // Show only if number is not already given.
    }
    add(nummerHelpImage);
    datePanel = new DatePanel("date", new PropertyModel<Date>(data, "date"), DatePanelSettings.get().withCallerPage(parentPage)
        .withTargetType(java.sql.Date.class));
    datePanel.setRequired(true);
    add(datePanel);
    validFromDatePanel = new DatePanel("validFrom", new PropertyModel<Date>(data, "validFrom"), DatePanelSettings.get().withCallerPage(
        parentPage).withTargetType(java.sql.Date.class));
    add(validFromDatePanel);
    validUntilDatePanel = new DatePanel("validUntil", new PropertyModel<Date>(data, "validUntil"), DatePanelSettings.get().withCallerPage(
        parentPage).withTargetType(java.sql.Date.class));
    add(validUntilDatePanel);
    addAutocompleteTextField("title").setRequired(true);
    addAutocompleteTextField("coContractorA");
    addAutocompleteTextField("contractPersonA");
    addAutocompleteTextField("signerA");
    addAutocompleteTextField("coContractorB");
    addAutocompleteTextField("contractPersonB");
    addAutocompleteTextField("signerB");
    signingDatePanel = new DatePanel("signingDate", new PropertyModel<Date>(data, "signingDate"), DatePanelSettings.get().withCallerPage(
        parentPage).withTargetType(java.sql.Date.class));
    add(signingDatePanel);

    // DropDownChoice type
    final List<ContractType> contractTypes = ConfigXml.getInstance().getContractTypes();
    final LabelValueChoiceRenderer<ContractType> typeChoiceRenderer = new LabelValueChoiceRenderer<ContractType>(contractTypes);
    @SuppressWarnings("unchecked")
    final DropDownChoice typeChoice = new DropDownChoice("type", new PropertyModel(data, "type"), typeChoiceRenderer.getValues(),
        typeChoiceRenderer);
    typeChoice.setNullValid(false);
    add(typeChoice);

    // DropDownChoice status
    final LabelValueChoiceRenderer<ContractStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<ContractStatus>(this, ContractStatus
        .values());
    @SuppressWarnings("unchecked")
    final DropDownChoice statusChoice = new DropDownChoice("status", new PropertyModel(data, "status"), statusChoiceRenderer.getValues(),
        statusChoiceRenderer);
    statusChoice.setNullValid(false);
    statusChoice.setRequired(true);
    add(statusChoice);

    add(new MaxLengthTextArea("text", new PropertyModel<String>(data, "text")));
    add(WicketUtils.getJIRASupportTooltipImage("jiraSupportTooltipImage", getResponse(), this));
    add(new JiraIssuesPanel("jiraIssues", data.getText()));
    add(new MaxLengthTextField("reference", new PropertyModel<String>(data, "reference")));
    add(new MaxLengthTextField("filing", new PropertyModel<String>(data, "filing")));
    resubmissionDatePanel = new DatePanel("resubmissionOnDate", new PropertyModel<Date>(data, "resubmissionOnDate"), DatePanelSettings
        .get().withCallerPage(parentPage).withTargetType(java.sql.Date.class));
    add(resubmissionDatePanel);
    dueDatePanel = new DatePanel("dueDate", new PropertyModel<Date>(data, "dueDate"), DatePanelSettings.get().withCallerPage(parentPage)
        .withTargetType(java.sql.Date.class));
    add(dueDatePanel);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
