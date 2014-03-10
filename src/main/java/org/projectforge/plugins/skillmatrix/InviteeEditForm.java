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


import org.apache.log4j.Logger;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.Hibernate;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * This is the edit formular page.
 * @author Werner Feder (werner.feder@t-online.de)
 */
public class InviteeEditForm extends AbstractEditForm<InviteeDO, InviteeEditPage>
{

  private static final long serialVersionUID = 6814668114853472909L;

  private static final Logger log = Logger.getLogger(InviteeEditForm.class);

  private static String[] defaultRatingArray            = {"hoch", "mittel", "niedrig"};
  private static String[] defaultCertificateArray       = {"Note 1", "Note 2", "Note 3", "Note 4"};

  private LabelValueChoiceRenderer<String> ratingChoiceRenderer;
  private LabelValueChoiceRenderer<String> certificateChoiceRenderer;

  @SpringBean(name = "inviteeDao")
  private InviteeDao inviteeDao;

  @SpringBean(name = "skillDao")
  private SkillDao skillDao;

  /**
   * @param parentPage
   * @param data
   */
  public InviteeEditForm(final InviteeEditPage parentPage, final InviteeDO data)
  {
    super(parentPage, data);
  }


  @Override
  public void init()
  {
    super.init();

    gridBuilder.newSplitPanel(GridSize.COL50);

    { // Training
      final FieldsetPanel fs = gridBuilder.newFieldset(InviteeDO.class, "training");
      TrainingDO training = data.getTraining();
      if (Hibernate.isInitialized(training) == false) {
        training = inviteeDao.getTraingDao().getOrLoad(training.getId());
        data.setTraining(training);
      }
      final TrainingSelectPanel trainingSelectPanel = new TrainingSelectPanel(fs.newChildId(), new PropertyModel<TrainingDO>(data, "training"),
          parentPage, "trainingId");
      fs.add(trainingSelectPanel);
      trainingSelectPanel.setRequired(true);
      trainingSelectPanel.init();
      trainingSelectPanel.setFocus();
    }

    { // Invitee
      final FieldsetPanel fs = gridBuilder.newFieldset(InviteeDO.class, "person");
      PFUserDO person = data.getPerson();
      if (Hibernate.isInitialized(person) == false) {
        person = inviteeDao.getUserDao().getOrLoad(person.getId());
        data.setPerson(person);
      }
      final UserSelectPanel inviteeSelectPanel = new UserSelectPanel(fs.newChildId(), new PropertyModel<PFUserDO>(data, "person"),
          parentPage, "personId");
      fs.add(inviteeSelectPanel);
      inviteeSelectPanel.setRequired(false);
      inviteeSelectPanel.init();
    }

    { // Rating
      final FieldsetPanel fs = gridBuilder.newFieldset(InviteeDO.class, "rating");
      final TrainingDO training = data.getTraining();
      if (training != null && training.getRatingArray() != null) {
        ratingChoiceRenderer = new LabelValueChoiceRenderer<String>(training.getRatingArray());
      } else {
        ratingChoiceRenderer = new LabelValueChoiceRenderer<String>(defaultRatingArray);
      }
      fs.addDropDownChoice(new PropertyModel<String>(data, "rating"), ratingChoiceRenderer.getValues(), ratingChoiceRenderer).setNullValid(
          true);
    }

    { // Certificate
      final FieldsetPanel fs = gridBuilder.newFieldset(InviteeDO.class, "certificate");
      final TrainingDO training = data.getTraining();
      if (training != null && training.getCertificateArray() != null) {
        certificateChoiceRenderer = new LabelValueChoiceRenderer<String>(training.getCertificateArray());
      } else {
        certificateChoiceRenderer = new LabelValueChoiceRenderer<String>(defaultCertificateArray);
      }
      fs.addDropDownChoice(new PropertyModel<String>(data, "certificate"), certificateChoiceRenderer.getValues(), certificateChoiceRenderer).setNullValid(
          true);
    }

    { // Description
      final FieldsetPanel fs = gridBuilder.newFieldset(InviteeDO.class, "description");
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description"))).setAutogrow();
    }

    { // Successfully
      final FieldsetPanel fs = gridBuilder.newFieldset(InviteeDO.class, "successfully");
      fs.addCheckBox(new PropertyModel<Boolean>(data, "successfully"), null);
    }

  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#onBeforeRender()
   */
  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
    final TrainingDO training = data.getTraining();
    certificateChoiceRenderer.clear();
    ratingChoiceRenderer.clear();
    if (training != null && training.getCertificateArray() != null && training.getRatingArray() != null) {
      certificateChoiceRenderer.setValueArray(training.getCertificateArray());
      ratingChoiceRenderer.setValueArray(training.getRatingArray());
    } else {
      certificateChoiceRenderer.setValueArray(defaultCertificateArray);
      ratingChoiceRenderer.setValueArray(defaultRatingArray);
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
