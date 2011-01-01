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

package org.projectforge.web.fibu;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.common.NumberHelper;
import org.projectforge.fibu.AuftragsPositionDO;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.RechnungStatus;
import org.projectforge.fibu.RechnungTyp;
import org.projectforge.fibu.RechnungsPositionDO;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.PresizedImage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.TooltipImage;


public class RechnungEditForm extends AbstractRechnungEditForm<RechnungDO, RechnungsPositionDO, RechnungEditPage>
{
  private static final long serialVersionUID = -6018131069720611834L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RechnungEditForm.class);

  private DropDownChoice<RechnungStatus> statusChoice;

  public RechnungEditForm(RechnungEditPage parentPage, RechnungDO data)
  {
    super(parentPage, data);
  }

  @Override
  protected void onInit()
  {
    add(new MinMaxNumberField<Integer>("nummer", new PropertyModel<Integer>(data, "nummer"), 0, 99999999));
    final TooltipImage nummerHelpImage = new TooltipImage("nummerHelp", getResponse(), WebConstants.IMAGE_HELP,
        getString("fibu.tooltip.nummerWirdAutomatischVergeben"));
    if (NumberHelper.greaterZero(getData().getNummer()) == true) {
      nummerHelpImage.setVisible(false); // Show only if number is not already given.
    }
    add(nummerHelpImage);
    final TooltipImage kundeHelpImage = new TooltipImage("kundeHelp", getResponse(), WebConstants.IMAGE_HELP,
        getString("fibu.rechnung.hint.kannVonProjektKundenAbweichen"));
    add(kundeHelpImage);
    // DropDownChoice status
    final LabelValueChoiceRenderer<RechnungStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<RechnungStatus>(this, RechnungStatus
        .values());
    statusChoice = new DropDownChoice<RechnungStatus>("status", new PropertyModel<RechnungStatus>(data, "status"), statusChoiceRenderer
        .getValues(), statusChoiceRenderer);
    statusChoice.setNullValid(false);
    statusChoice.setRequired(true);
    add(statusChoice);
    // DropDownChoice type
    final LabelValueChoiceRenderer<RechnungTyp> typeChoiceRenderer = new LabelValueChoiceRenderer<RechnungTyp>(this, RechnungTyp.values());
    final DropDownChoice<RechnungTyp> typeChoice = new DropDownChoice<RechnungTyp>("typ", new PropertyModel<RechnungTyp>(data, "typ"),
        typeChoiceRenderer.getValues(), typeChoiceRenderer);
    typeChoice.setNullValid(false);
    typeChoice.setRequired(true);
    add(typeChoice);
    final ProjektSelectPanel projektSelectPanel = new ProjektSelectPanel("selectProjekt", new PropertyModel<ProjektDO>(data, "projekt"),
        parentPage, "projektId");
    add(projektSelectPanel);
    projektSelectPanel.init();
    final CustomerSelectPanel kundeSelectPanel = new CustomerSelectPanel("selectKunde", new PropertyModel<KundeDO>(data, "kunde"),
        new PropertyModel<String>(data, "kundeText"), parentPage, "kundeId");
    add(kundeSelectPanel);
    kundeSelectPanel.init();
  }

  @Override
  protected void afterInit()
  {
    datumPanel.setRequired(true);
  }

  @SuppressWarnings("serial")
  @Override
  protected void onRenderPosition(final WebMarkupContainer item, final RechnungsPositionDO position)
  {
    item.add(new AuftragsPositionFormComponent("orderPosition", new PropertyModel<AuftragsPositionDO>(position,
    "auftragsPosition"), false));

    final Link<String> orderLink = new Link<String>("orderLink") {
      @Override
      public void onClick()
      {
        if (position.getAuftragsPosition() != null) {
          final PageParameters parameters = new PageParameters();
          parameters.put(AbstractEditPage.PARAMETER_KEY_ID, position.getAuftragsPosition().getAuftrag().getId());
          final AuftragEditPage auftragEditPage = new AuftragEditPage(parameters);
          auftragEditPage.setReturnToPage(getParentPage());
          setResponsePage(auftragEditPage);
        }
      }
    };
    item.add(orderLink);
    if (position.getAuftragsPosition() == null) {
      orderLink.setVisible(false);
    }
    orderLink.add(new PresizedImage("linkImage", getResponse(), WebConstants.IMAGE_FIND));
  }

  @Override
  protected void cloneRechnung()
  {
    parentPage.cloneRechnung();
  }

  @Override
  protected void validation()
  {
    super.validation();

    final RechnungStatus status = statusChoice.getConvertedInput();
    final BigDecimal zahlBetrag = zahlBetragField.getConvertedInput();
    final Integer projektId = getData().getProjektId();
    final String kundeText = getData().getKundeText();
    boolean zahlBetragExists = (zahlBetrag != null && zahlBetrag.compareTo(BigDecimal.ZERO) != 0);
    if (status == RechnungStatus.BEZAHLT && zahlBetragExists == false) {
      addError("fibu.rechnung.error.statusBezahltErfordertZahlBetrag");
    }
    if (projektId == null && StringUtils.isBlank(kundeText) == true) {
      addError("fibu.rechnung.error.kundeTextOderProjektRequired");
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected RechnungsPositionDO newPositionInstance()
  {
    return new RechnungsPositionDO();
  }
}
