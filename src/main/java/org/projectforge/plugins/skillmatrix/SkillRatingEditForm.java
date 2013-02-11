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

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.DropDownChoicePanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 * 
 */
public class SkillRatingEditForm extends AbstractEditForm<SkillRatingDO, SkillRatingEditPage>
{
  private static final long serialVersionUID = -4997909992117525036L;

  private static final Logger log = Logger.getLogger(SkillRatingEditForm.class);

  @SpringBean(name = "skillDao")
  private SkillDao skillDao;

  @SpringBean(name = "skillRatingDao")
  private SkillRatingDao skillRatingDao;

  // For AjaxRequest in skill and skill rating
  private FieldsetPanel fs;

  private final FormComponent< ? >[] dependentFormComponents = new FormComponent[2];

  /**
   * @param parentPage
   * @param data
   */
  public SkillRatingEditForm(final SkillRatingEditPage parentPage, final SkillRatingDO data)
  {
    super(parentPage, data);
    data.setUser(PFUserContext.getUser());
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();

    add(new IFormValidator() {

      @Override
      public FormComponent< ? >[] getDependentFormComponents()
      {
        return dependentFormComponents;
      }

      @SuppressWarnings("unchecked")
      @Override
      public void validate(final Form< ? > form)
      {
        final PFAutoCompleteTextField<SkillDO> skillTextField = (PFAutoCompleteTextField<SkillDO>) dependentFormComponents[0];
        final DropDownChoice<SkillRating> skillRatingDropDown = (DropDownChoice<SkillRating>) dependentFormComponents[1];
        if (skillTextField.getConvertedInput().isRateable() == true && skillRatingDropDown.getConvertedInput() == null) {
          error(getString("plugins.skillmatrix.error.rateableSkillWithNullRating"));
        } else if (skillTextField.getConvertedInput().isRateable() == false && skillRatingDropDown.getConvertedInput() != null) {
          error(getString("plugins.skillmatrix.error.unrateableSkillWithRating"));
        }

        final SkillRatingFilter filter = new SkillRatingFilter();
        filter.setSearchFields("user.username");
        filter.setSearchString(data.getUser().getUsername());
        final List<SkillRatingDO> list = skillRatingDao.getList(filter);
        for (final SkillRatingDO skillRatingDO : list) {
          final boolean isDeleted = skillRatingDO.getSkill().isDeleted();
          final boolean sameSkill = skillRatingDO.getSkill().getTitle() == skillTextField.getConvertedInput().getTitle();
          final boolean sameSkillRating = skillRatingDO.getCreated().equals(data.getCreated());
          if (isDeleted == false && sameSkill && sameSkillRating == false) {
            error(getString("plugins.skillmatrix.error.skillRatingExistsAlready"));
            break;
          }
        }
      }

    });

    gridBuilder.newGridPanel();
    {
      // User
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skillrating.user")).supressLabelForWarning();
      final DivTextPanel username = new DivTextPanel(fs.newChildId(), data.getUser().getUsername());
      username.setStrong();
      fs.add(username);
    }
    {
      // Skill, look at UserSelectPanel for fine tuning ( getConverter() )
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skillrating.skill"));
      final PFAutoCompleteTextField<SkillDO> autoCompleteTextField = new PFAutoCompleteTextField<SkillDO>(fs.getTextFieldId(),
          new PropertyModel<SkillDO>(data, "skill")) {
        @Override
        protected List<SkillDO> getChoices(final String input)
        {
          final BaseSearchFilter filter = new BaseSearchFilter();
          filter.setSearchFields("title");
          filter.setSearchString(input);
          final List<SkillDO> list = skillDao.getList(filter);
          return list;
        }

        @Override
        protected String formatLabel(final SkillDO skill)
        {
          if (skill == null) {
            return "";
          }
          return skill.getTitle();
        }

        @Override
        protected String formatValue(final SkillDO skill)
        {
          if (skill == null) {
            return "";
          }
          return skill.getTitle();
        }

        @SuppressWarnings({ "unchecked", "rawtypes"})
        @Override
        public <C> IConverter<C> getConverter(final Class<C> type)
        {
          return new IConverter() {
            @Override
            public Object convertToObject(final String value, final Locale locale)
            {
              if (StringUtils.isEmpty(value) == true) {
                getModel().setObject(null);
                return null;
              }
              final SkillDO skill = skillDao.getSkillTree().getSkill(value);
              if (skill == null) {
                error(getString("plugins.skillmatrix.error.skillNotFound"));
              }
              getModel().setObject(skill);
              final AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
              if (target != null) {
                target.add(SkillRatingEditForm.this.fs.getFieldset());
              }
              return skill;
            }

            @Override
            public String convertToString(final Object value, final Locale locale)
            {
              if (value == null) {
                return "";
              }
              final SkillDO skill = (SkillDO) value;
              return skill.getTitle();
            }
          };
        }

      };
      autoCompleteTextField.withLabelValue(true).withMatchContains(true).withMinChars(2).withAutoSubmit(false).withWidth(400)
      .setRequired(true);
      autoCompleteTextField.add(new AjaxFormComponentUpdatingBehavior("onChange") {

        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          // AjaxRequestTarget needs this.
        }
      });
      fs.add(autoCompleteTextField);
      dependentFormComponents[0] = autoCompleteTextField;
    }
    {
      // Skill rating
      fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skillrating.rating"));
      fs.getFieldset().setOutputMarkupId(true);
      final LabelValueChoiceRenderer<SkillRating> ratingChoiceRenderer = new LabelValueChoiceRenderer<SkillRating>(this,
          SkillRating.values());
      final DropDownChoicePanel<SkillRating> skillChoice = new DropDownChoicePanel<SkillRating>(fs.newChildId(),
          new PropertyModel<SkillRating>(data, "skillRating"), ratingChoiceRenderer.getValues(), ratingChoiceRenderer) {
        @Override
        public boolean isVisible()
        {
          if (data == null || data.getSkill() == null || !data.getSkill().isRateable()) {
            return false;
          } else {
            return true;
          }
        }
      };
      fs.add(skillChoice);
      dependentFormComponents[1] = skillChoice.getDropDownChoice();
    }
    {
      // Since year
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skillrating.sinceyear"));
      fs.add(new MinMaxNumberField<Integer>(fs.getTextFieldId(), new PropertyModel<Integer>(data, "sinceYear"), 0, 9000));
    }
    {
      // Certificates
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skillrating.certificates"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "certificates")));
    }
    {
      // Training courses
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skillrating.trainingcourses"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "trainingCourses")));
    }
    {
      // Description
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skillrating.description"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description"))).setAutogrow();
    }
    {
      // Comment
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skillrating.comment"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "comment"))).setAutogrow();
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
