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

import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.registry.Registry;
import org.projectforge.user.GroupDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserRights;
import org.projectforge.web.common.MultiChoiceListHelper;
import org.projectforge.web.user.GroupsComparator;
import org.projectforge.web.user.GroupsProvider;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.bootstrap.GridType;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

import com.vaynberg.wicket.select2.Select2MultiChoice;

/**
 * @author Billy Duong (b.duong@micromata.de)
 * 
 */
public class SkillEditForm extends AbstractEditForm<SkillDO, SkillEditPage>
{
  private static final long serialVersionUID = 7795854215943696332L;

  private static final Logger log = Logger.getLogger(SkillEditForm.class);

  @SpringBean(name = "skillDao")
  private SkillDao skillDao;

  MultiChoiceListHelper<GroupDO> fullAccessGroupsListHelper, readOnlyAccessGroupsListHelper;
  private Model <String> labelFullModel, labelReadOnlyModel;
  private SkillRight skillRight;

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

    skillRight = (SkillRight) UserRights.instance().getRight(SkillDao.USER_RIGHT_ID);

    gridBuilder.newSplitPanel(GridSize.COL50, GridType.CONTAINER);
    {
      // Parent
      final FieldsetPanel fs = gridBuilder.newFieldset(SkillDO.class, "parent");
      final SkillTextSelectPanel parentSelectPanel = new SkillTextSelectPanel(fs.newChildId(), new PropertyModel<SkillDO>(data, "parent"), parentPage,
          "parentId") {
        private static final long serialVersionUID = 8355684523726816059L;

        private void setLabels(final SkillDO skill) {
          if (skill != null && skill.getParent() != null) {
            labelFullModel.setObject(getGroupnames(skillRight.getFullAccessGroupIds(skill.getParent())));
            labelReadOnlyModel.setObject(getGroupnames(skillRight.getReadOnlyAccessGroupIds(skill.getParent())));
          } else {
            labelFullModel.setObject("");
            labelReadOnlyModel.setObject("");
          }
        }

        /**
         * @see org.projectforge.plugins.skillmatrix.SkillSelectPanel#onModelSelected(org.apache.wicket.ajax.AjaxRequestTarget, org.projectforge.plugins.skillmatrix.SkillDO)
         */
        @Override
        protected void onModelSelected(final AjaxRequestTarget target, final SkillDO skillDo)
        {
          super.onModelSelected(target, skillDo);
          setLabels(skillDo);
        }
        /**
         * @see org.projectforge.plugins.skillmatrix.SkillSelectPanel#onBeforeRender()
         */
        @Override
        protected void onBeforeRender()
        {
          super.onBeforeRender();
          final SkillDO skillDo = skillDao.getOrLoad(this.getCurrentSkillId());
          setLabels(skillDo);
        }
      };
      parentSelectPanel.init();
      fs.add(parentSelectPanel);
      fs.getFieldset().setOutputMarkupId(true);
      if (getSkillTree().isRootNode(data) == true) {
        fs.setVisible(false);
      } else {
        parentSelectPanel.setRequired(true);
      }
    }

    {
      // Title of skill
      final FieldsetPanel fs = gridBuilder.newFieldset(SkillDO.class, "title");
      final RequiredMaxLengthTextField titleField = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
          "title"));
      WicketUtils.setFocus(titleField);
      fs.add(titleField);
    }

    gridBuilder.newSplitPanel(GridSize.COL50, GridType.CONTAINER);
    {
      // Full access groups
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.fullAccess"), getString("plugins.teamcal.access.groups"));
      final GroupsProvider groupsProvider = new GroupsProvider();
      final Collection<GroupDO> fullAccessGroups = new GroupsProvider().getSortedGroups(getData().getFullAccessGroupIds());
      fullAccessGroupsListHelper = new MultiChoiceListHelper<GroupDO>().setComparator(new GroupsComparator()).setFullList(
          groupsProvider.getSortedGroups());
      if (fullAccessGroups != null) {
        for (final GroupDO group : fullAccessGroups) {
          fullAccessGroupsListHelper.addOriginalAssignedItem(group).assignItem(group);
        }
      }
      final Select2MultiChoice<GroupDO> groups = new Select2MultiChoice<GroupDO>(fs.getSelect2MultiChoiceId(),
          new PropertyModel<Collection<GroupDO>>(this.fullAccessGroupsListHelper, "assignedItems"), groupsProvider);
      fs.add(groups);

      final FieldsetPanel fs2 = gridBuilder.newFieldset("", getString("plugins.skillmatrix.skill.inherited")).setLabelFor(groups);
      fs2.setOutputMarkupId(true);
      labelFullModel = new Model<String>("");
      final Label labelFull = new Label (fs2.newChildId(), labelFullModel);
      labelFull.setOutputMarkupId(true);
      fs2.add(labelFull);
      fs2.getFieldset().setOutputMarkupId(true);
      if (getData().getParent() != null) {
        labelFullModel.setObject(getGroupnames( skillRight.getFullAccessGroupIds(getData().getParent())));
      }
    }
    {
      // Read-only access groups
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.readonlyAccess"), getString("plugins.teamcal.access.groups"));
      final GroupsProvider groupsProvider = new GroupsProvider();
      final Collection<GroupDO> readOnlyAccessGroups = new GroupsProvider().getSortedGroups(getData().getReadOnlyAccessGroupIds());
      readOnlyAccessGroupsListHelper = new MultiChoiceListHelper<GroupDO>().setComparator(new GroupsComparator()).setFullList(
          groupsProvider.getSortedGroups());
      if (readOnlyAccessGroups != null) {
        for (final GroupDO group : readOnlyAccessGroups) {
          readOnlyAccessGroupsListHelper.addOriginalAssignedItem(group).assignItem(group);
        }
      }
      final Select2MultiChoice<GroupDO> groups = new Select2MultiChoice<GroupDO>(fs.getSelect2MultiChoiceId(),
          new PropertyModel<Collection<GroupDO>>(this.readOnlyAccessGroupsListHelper, "assignedItems"), groupsProvider);
      fs.add(groups);

      final FieldsetPanel fs2 = gridBuilder.newFieldset("", getString("plugins.skillmatrix.skill.inherited")).setLabelFor(groups);
      fs2.setOutputMarkupId(true);
      labelReadOnlyModel = new Model<String>("");
      final Label labelReadOnly = new Label (fs2.newChildId(), labelReadOnlyModel);
      labelReadOnly.setOutputMarkupId(true);
      fs2.add(labelReadOnly);
      fs2.getFieldset().setOutputMarkupId(true);
      if (getData().getParent() != null) {
        labelReadOnlyModel.setObject(getGroupnames( skillRight.getReadOnlyAccessGroupIds(getData().getParent())));
      }
    }

    gridBuilder.newGridPanel();
    {
      // Description
      final FieldsetPanel fs = gridBuilder.newFieldset(SkillDO.class, "description");
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description"))).setAutogrow();
    }
    {
      // Comment
      final FieldsetPanel fs = gridBuilder.newFieldset(SkillDO.class, "comment");
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "comment"))).setAutogrow();
    }
    {
      // Rateable
      gridBuilder.newFieldset(SkillDO.class, "rateable").addCheckBox(new PropertyModel<Boolean>(data, "rateable"), null);
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

  public SkillTree getSkillTree()
  {
    return skillDao.getSkillTree();
  }

  private String getGroupnames(final Integer[] ids) {
    String s ="";
    final UserGroupCache userGroupCache = Registry.instance().getUserGroupCache();
    for (final Integer id : ids) {
      s += userGroupCache.getGroupname(id) + " ";
    }
    return s;
  }
}
