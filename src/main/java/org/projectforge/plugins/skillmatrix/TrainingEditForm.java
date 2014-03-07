/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.skillmatrix;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 * 
 */
@SuppressWarnings("deprecation")
public class TrainingEditForm extends AbstractEditForm<TrainingDO, TrainingEditPage>
{

  private static final long serialVersionUID = 359682752123823685L;

  private static final Logger log = Logger.getLogger(TrainingEditForm.class);

  @SpringBean(name = "trainingDao")
  private TrainingDao trainingDao;

  @SpringBean(name = "skillDao")
  private SkillDao skillDao;

  private final FormComponent< ? >[] dependentFormComponents = new FormComponent[1];

  private TextField<String> valuesRating;

  private TextField<String> valuesCertificate;

  /**
   * @param parentPage
   * @param data
   */
  public TrainingEditForm(final TrainingEditPage parentPage, final TrainingDO data)
  {
    super(parentPage, data);
  }

  @SuppressWarnings("serial")
  @Override
  public void init()
  {
    super.init();

    gridBuilder.newGridPanel();

    { // Title of skill
      final FieldsetPanel fs = gridBuilder.newFieldset(TrainingDO.class, "title");
      final RequiredMaxLengthTextField titleField = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
          "title"));
      WicketUtils.setFocus(titleField);
      fs.add(titleField);
      dependentFormComponents[0] = titleField;
    }

    { // Skill
      final FieldsetPanel fs = gridBuilder.newFieldset(TrainingDO.class, "skill");
      final SkillSelectPanel parentSelectPanel = new SkillSelectPanel(fs, new PropertyModel<SkillDO>(data, "skill"), parentPage, "skillId");
      fs.add(parentSelectPanel);
      fs.getFieldset().setOutputMarkupId(true);
      parentSelectPanel.init();
    }

    gridBuilder.newGridPanel();

    { // Descritption
      final FieldsetPanel fs = gridBuilder.newFieldset(TrainingDO.class, "description");
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description"))).setAutogrow();
    }

    { // startDate
      final FieldsetPanel fs = gridBuilder.newFieldset(TrainingDO.class, "startDate");
      fs.add(new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "startDate"), DatePanelSettings.get().withTargetType(
          java.sql.Date.class)));
    }

    { // EndDate
      final FieldsetPanel fs = gridBuilder.newFieldset(TrainingDO.class, "endDate");
      fs.add(new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "endDate"), DatePanelSettings.get().withTargetType(
          java.sql.Date.class)));

    }

    { // Rating
      final FieldsetPanel fs = gridBuilder.newFieldset(TrainingDO.class, "rating");
      valuesRating = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "rating"));
      fs.addHelpIcon(getString("plugins.marketing.addressCampaign.values.format"));
      fs.add(valuesRating);
      fs.addAlertIcon(getString("plugins.skillmatrix.skilltraining.edit.warning.doNotChangeValues"));
      valuesRating.setRequired(true);
      valuesRating.add(new AbstractValidator<String>() {
        @Override
        protected void onValidate(final IValidatable<String> validatable)
        {
          if (TrainingDO.getValuesArray(validatable.getValue()) == null) {
            valuesRating.error(getString("plugins.skillmatrix.skilltraining.values.invalidFormat"));
          }
        }
      });
    }

    { // Certificate
      final FieldsetPanel fs = gridBuilder.newFieldset(TrainingDO.class, "certificate");
      valuesCertificate = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "certificate"));
      fs.addHelpIcon(getString("plugins.marketing.addressCampaign.values.format"));
      fs.add(valuesCertificate);
      fs.addAlertIcon(getString("plugins.skillmatrix.skilltraining.edit.warning.doNotChangeValues"));
      valuesCertificate.setRequired(true);
      valuesCertificate.add(new AbstractValidator<String>() {
        @Override
        protected void onValidate(final IValidatable<String> validatable)
        {
          if (TrainingDO.getValuesArray(validatable.getValue()) == null) {
            valuesCertificate.error(getString("plugins.skillmatrix.skilltraining.values.invalidFormat"));
          }
        }
      });
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

}
