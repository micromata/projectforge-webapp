/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
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

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SkillEditForm.class);

  @SpringBean(name = "skillDao")
  SkillDao skillDao;

  /**
   * @param parentPage
   * @param data
   */
  public SkillEditForm(final SkillEditPage parentPage, final SkillDO data)
  {
    super(parentPage, data);
  }

  @Override
  public void init()
  {
    super.init();
    gridBuilder.newGrid16();

    {
      // Title of skill
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skill.title"));
      fs.add(new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "title")));
    }
    {
      // Parent
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skill.parent"));
      final List<SkillDO> list = skillDao.getList(new BaseSearchFilter());
      final LabelValueChoiceRenderer<SkillDO> ratingChoiceRenderer = new LabelValueChoiceRenderer<SkillDO>(list);
      final DropDownChoice<SkillDO> dropDownChoice = new DropDownChoice<SkillDO>(fs.getDropDownChoiceId(), new PropertyModel<SkillDO>(data,
          "parent"), ratingChoiceRenderer.getValues(), ratingChoiceRenderer);
      dropDownChoice.setNullValid(true);
      fs.add(dropDownChoice);
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
