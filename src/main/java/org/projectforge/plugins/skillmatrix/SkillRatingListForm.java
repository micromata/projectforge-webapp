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
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.DropDownChoicePanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 * 
 */
public class SkillRatingListForm extends AbstractListForm<SkillRatingFilter, SkillRatingListPage>
{
  private static final long serialVersionUID = 5333752125044497290L;

  private static final Logger log = Logger.getLogger(SkillRatingListForm.class);

  /**
   * @param parentPage
   */
  public SkillRatingListForm(final SkillRatingListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected void init() {
    super.init();
    {
      // Required experience
      gridBuilder.newColumnPanel(DivType.COL_60);
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.skillmatrix.search.reqiuredExperience")).setNoLabelFor();
      fs.getFieldset().setOutputMarkupId(true);
      final LabelValueChoiceRenderer<SkillRating> ratingChoiceRenderer = new LabelValueChoiceRenderer<SkillRating>(this,
          SkillRating.values());
      final DropDownChoicePanel<SkillRating> skillChoice = new DropDownChoicePanel<SkillRating>(fs.newChildId(),
          new PropertyModel<SkillRating>(getSearchFilter(), "skillRating"), ratingChoiceRenderer.getValues(), ratingChoiceRenderer);
      skillChoice.setNullValid(true);
      fs.add(skillChoice);
    }
    {
      // DropDownChoice page size
      gridBuilder.newColumnPanel(DivType.COL_40);
      addPageSizeFieldset();
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#newSearchFilterInstance()
   */
  @Override
  protected SkillRatingFilter newSearchFilterInstance()
  {
    return new SkillRatingFilter();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

}
