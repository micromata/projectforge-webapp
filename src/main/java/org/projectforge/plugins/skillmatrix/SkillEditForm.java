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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 * 
 */
public class SkillEditForm extends AbstractEditForm<SkillDO, SkillEditPage>
{
  private static final long serialVersionUID = 7795854215943696332L;

  private static final Logger log = Logger.getLogger(SkillEditForm.class);

  @SpringBean(name = "skillDao")
  private SkillDao skillDao;

  private final FormComponent< ? >[] dependentFormComponents = new FormComponent[1];

  /**
   * @param parentPage
   * @param data
   */
  public SkillEditForm(final SkillEditPage parentPage, final SkillDO data)
  {
    super(parentPage, data);
  }

  @Override
  @SuppressWarnings("serial")
  public void init()
  {
    super.init();

    add(new IFormValidator() {

      @Override
      public FormComponent< ? >[] getDependentFormComponents()
      {
        return dependentFormComponents;
      }

      @Override
      public void validate(final Form< ? > form)
      {
        final RequiredMaxLengthTextField skillTextField = (RequiredMaxLengthTextField) dependentFormComponents[0];
        final SkillTree skillTree = skillDao.getSkillTree();
        if(skillTree.getSkill(skillTextField.getConvertedInput()) != null) {
          error(getString("plugins.skillmatrix.error.skillExistsAlready"));
        }
      }
    });

    gridBuilder.newGridPanel();

    {
      // Title of skill
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skill.title"));
      final RequiredMaxLengthTextField skillTextField = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "title"));
      fs.add(skillTextField);
      dependentFormComponents[0] = skillTextField;
    }
    {
      // Parent, look at UserSelectPanel for fine tuning
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skill.parent"));
      final PFAutoCompleteTextField<SkillDO> autoCompleteTextField = new PFAutoCompleteTextField<SkillDO>(fs.getTextFieldId(),
          new PropertyModel<SkillDO>(data, "parent")) {
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
      autoCompleteTextField.withLabelValue(true).withMatchContains(true).withMinChars(2).withAutoSubmit(false).withWidth(400);
      fs.add(autoCompleteTextField);
    }

    {
      // Descritption
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skill.description"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description"))).setAutogrow();
    }
    {
      // Comment
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skill.comment"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "comment"))).setAutogrow();
    }
    {
      // Rateable
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skill.options"));
      final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
      checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(data, "rateable"),
          getString("plugins.skillmatrix.skill.rateable")));
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
