/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import org.apache.log4j.Logger;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 * 
 */
public class SkillMatrixEditForm extends AbstractEditForm<SkillDO, SkillMatrixEditPage>
{
  private static final long serialVersionUID = -4997909992117525036L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SkillMatrixEditForm.class);

  /**
   * @param parentPage
   * @param data
   */
  public SkillMatrixEditForm(final SkillMatrixEditPage parentPage, final SkillDO data)
  {
    super(parentPage, data);
  }

  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newGrid16();

    // User
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.user"));
      // TODO this should be a disabled textfield or label, which shows the user's name
      fs.add(new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,"owner"))).setEnabled(false);
    }
    // Skill
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skill"));
      // TODO change to PFAutoCompleteTextField
      fs.add(new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,"skill")));
    }
    // Experience
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.experience"));
      // TODO change to PFAutoCompleteTextField
      fs.add(new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,"experience")));
    }
    // Comment
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.comment"));
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
