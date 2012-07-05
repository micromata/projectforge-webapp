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
import org.apache.wicket.model.PropertyModel;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 * 
 */
public class SkillRatingEditForm extends AbstractEditForm<SkillRatingDO, SkillRatingEditPage>
{
  private static final long serialVersionUID = -4997909992117525036L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SkillRatingEditForm.class);

  /**
   * @param parentPage
   * @param data
   */
  public SkillRatingEditForm(final SkillRatingEditPage parentPage, final SkillRatingDO data)
  {
    super(parentPage, data);
    data.setUser(PFUserContext.getUser());
  }

  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newGrid16();
    {
      // User
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skillrating.user"));
      final DivTextPanel username = new DivTextPanel(fs.newChildId(), data.getUser().getUsername());
      username.setStrong();
      fs.add(username);
    }
    {
      // Skill
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skillrating.skill"));
      fs.add(new PFAutoCompleteMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "skill")) {
        private static final long serialVersionUID = 7398144813346052567L;

        @Override
        protected List<String> getChoices(final String input)
        {
          return getBaseDao().getAutocompletion("skill", input);
        }
      });
    }
    {
      // SkillRating
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.skillrating.rating"));
      final LabelValueChoiceRenderer<SkillRating> ratingChoiceRenderer = new LabelValueChoiceRenderer<SkillRating>(this, SkillRating.values());
      fs.addDropDownChoice(new PropertyModel<SkillRating>(data,"skillRating"), ratingChoiceRenderer.getValues(), ratingChoiceRenderer);
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
