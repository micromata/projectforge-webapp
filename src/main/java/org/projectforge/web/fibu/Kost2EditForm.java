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

package org.projectforge.web.fibu;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.kost.Kost2ArtDao;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.KostentraegerStatus;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMinMaxNumberField;
import org.projectforge.web.wicket.converter.IntegerConverter;


public class Kost2EditForm extends AbstractEditForm<Kost2DO, Kost2EditPage>
{
  private static final long serialVersionUID = -2138017238114715368L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Kost2EditForm.class);

  @SpringBean(name = "kost2ArtDao")
  private Kost2ArtDao kost2ArtDao;

  protected TextField<Integer> nummernkreisField;

  protected TextField<Integer> bereichField;

  protected TextField<Integer> teilbereichField;

  protected TextField<Integer> kost2ArtField;

  public Kost2EditForm(Kost2EditPage parentPage, Kost2DO data)
  {
    super(parentPage, data);
    this.colspan = 6;
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    final ProjektSelectPanel projektSelectPanel = new ProjektSelectPanel("projekt", new PropertyModel<ProjektDO>(data, "projekt"),
        parentPage, "projektId");
    add(projektSelectPanel);
    projektSelectPanel.init();

    nummernkreisField = new RequiredMinMaxNumberField<Integer>("nummernkreis", new PropertyModel<Integer>(data, "nummernkreis"), 0, 9);
    nummernkreisField.add(new FocusOnLoadBehavior());
    add(nummernkreisField);
    bereichField = new RequiredMinMaxNumberField<Integer>("bereich", new PropertyModel<Integer>(data, "bereich"), 0, 999) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(3);
      }
    };
    add(bereichField);
    teilbereichField = new RequiredMinMaxNumberField<Integer>("teilbereich", new PropertyModel<Integer>(data, "teilbereich"), 0, 99) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(2);
      }
    };
    add(teilbereichField);
    kost2ArtField = new RequiredMinMaxNumberField<Integer>("kost2ArtId", new PropertyModel<Integer>(data, "kost2Art.id"), 0, 99) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(2);
      }
    };
    kost2ArtField.setRequired(true);
    kost2ArtField.add(new AbstractValidator<Integer>() {
      @Override
      protected void onValidate(IValidatable<Integer> validatable)
      {
        final Integer value = validatable.getValue();
        if (value == null) {
          return;
        }
        if (kost2ArtDao.getById(value) == null) { // Kost2 available but not selected.
          error(validatable);
        }
      }

      @Override
      protected String resourceKey()
      {
        return "fibu.kost2art.error.notFound";
      }
    });
    add(kost2ArtField);
    add(new MinMaxNumberField<BigDecimal>("workFraction", new PropertyModel<BigDecimal>(data, "workFraction"), BigDecimal.ZERO,
        BigDecimal.ONE));
    add(new MaxLengthTextField("description", new PropertyModel<String>(data, "description")));
    add(new MaxLengthTextArea("comment", new PropertyModel<String>(data, "comment")));
    // DropDownChoice status
    final LabelValueChoiceRenderer<KostentraegerStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<KostentraegerStatus>(this,
        KostentraegerStatus.values());
    @SuppressWarnings("unchecked")
    final DropDownChoice statusChoice = new DropDownChoice("kostentraegerStatus", new PropertyModel(data, "kostentraegerStatus"),
        statusChoiceRenderer.getValues(), statusChoiceRenderer);
    statusChoice.setNullValid(false);
    statusChoice.setRequired(true);
    add(statusChoice);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
