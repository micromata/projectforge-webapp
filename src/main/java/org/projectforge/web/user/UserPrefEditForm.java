/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.user;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.core.I18nEnum;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserPrefArea;
import org.projectforge.user.UserPrefAreaRegistry;
import org.projectforge.user.UserPrefDO;
import org.projectforge.user.UserPrefDao;
import org.projectforge.user.UserPrefEntryDO;
import org.projectforge.web.fibu.CustomerSelectPanel;
import org.projectforge.web.fibu.Kost2DropDownChoice;
import org.projectforge.web.fibu.ProjektSelectPanel;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.TooltipImage;

public class UserPrefEditForm extends AbstractEditForm<UserPrefDO, UserPrefEditPage>
{
  private static final long serialVersionUID = 6647201995353615498L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserPrefEditForm.class);

  @SpringBean(name = "userPrefDao")
  private UserPrefDao userPrefDao;

  private Label areaLabel;

  private DropDownChoice<UserPrefArea> areaDropDownChoice;

  private RepeatingView parametersRepeater;

  protected Map<String, Component> dependentsMap = new HashMap<String, Component>();

  /**
   * @param parent Needed for i18n
   * @param bean is used for creating a PropertyModel.
   * @param propertyName is used as property name of the property model.
   * @return
   */
  public static DropDownChoice<UserPrefArea> createAreaDropdownChoice(final Component parent, final String id, final Object bean,
      final String propertyName, final boolean nullValid)
  {
    // DropDownChoice area
    final LabelValueChoiceRenderer<UserPrefArea> areaChoiceRenderer = createAreaChoiceRenderer(parent);
    final DropDownChoice<UserPrefArea> areaDropDownChoice = new DropDownChoice<UserPrefArea>(id, new PropertyModel<UserPrefArea>(bean,
        propertyName), areaChoiceRenderer.getValues(), areaChoiceRenderer);
    areaDropDownChoice.setNullValid(nullValid);
    return areaDropDownChoice;
  }

  public static LabelValueChoiceRenderer<UserPrefArea> createAreaChoiceRenderer(final Component parent)
  {
    // DropDownChoice area
    final LabelValueChoiceRenderer<UserPrefArea> areaChoiceRenderer = new LabelValueChoiceRenderer<UserPrefArea>();
    for (final UserPrefArea area : UserPrefAreaRegistry.instance().getOrderedEntries(PFUserContext.getLocale())) {
      areaChoiceRenderer.addValue(area, parent.getString("user.pref.area." + area.getKey()));
    }
    return areaChoiceRenderer;
  }

  public UserPrefEditForm(UserPrefEditPage parentPage, UserPrefDO data)
  {
    super(parentPage, data);
    this.colspan = 2;
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    parametersRepeater = new RepeatingView("parameters");
    add(parametersRepeater);
    data.setUser(PFUserContext.getUser());
    areaLabel = new Label("areaLabel", new Model<String>() {
      @Override
      public String getObject()
      {
        if (data.getArea() != null) {
          return getString(data.getArea().getI18nKey());
        } else {
          return "";
        }
      }
    });
    add(areaLabel);

    final LabelValueChoiceRenderer<UserPrefArea> areaChoiceRenderer = createAreaChoiceRenderer(this);
    areaDropDownChoice = new DropDownChoice<UserPrefArea>("areaChoice", new PropertyModel<UserPrefArea>(data, "area"), areaChoiceRenderer
        .getValues(), areaChoiceRenderer) {
      @Override
      protected boolean wantOnSelectionChangedNotifications()
      {
        return true;
      }

      @Override
      protected void onSelectionChanged(final UserPrefArea newSelection)
      {
        if (newSelection != null && parametersRepeater.isVisible() == false) {
          // Area is now given, so update area label:
          areaLabel.setVisible(true);
          areaDropDownChoice.setVisible(false);
          // and create repeater childs:
          createParameterRepeaterChilds();
        }
      }
    };
    areaDropDownChoice.setNullValid(true);
    areaDropDownChoice.setRequired(true);

    if (isNew() == false || data.getArea() != null) {
      areaDropDownChoice.setVisible(false);
    }
    if (data.getArea() == null) {
      parametersRepeater.setVisible(false);
    } else {
      createParameterRepeaterChilds();
    }
    add(areaDropDownChoice);
    if (data.getUser() == null) {
      data.setUser(getUser());
    }
    add(new Label("user", data.getUser().getFullname()));

    final RequiredMaxLengthTextField nameField = new RequiredMaxLengthTextField("name", new PropertyModel<String>(data, "name"));
    nameField.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable)
      {
        if (data.getArea() == null) {
          return;
        }
        final String value = validatable.getValue();
        if (parentPage.userPrefDao.doesParameterNameAlreadyExist(data.getId(), data.getUser(), data.getArea(), value)) {
          error(validatable);
        }
      }

      @Override
      protected String resourceKey()
      {
        return "user.pref.error.nameDoesAlreadyExist";
      }
    });

    add(nameField);
    nameField.add(new FocusOnLoadBehavior());
  }

  @SuppressWarnings("serial")
  private void createParameterRepeaterChilds()
  {
    if (data.getArea() == null) {
      log.warn("Could not create ParameterRepeater because UserPrefArea is not given.");
      return;
    }
    if (isNew() == true && data.getUserPrefEntries() == null) {
      parentPage.userPrefDao.addUserPrefParameters(data, data.getArea());
    }
    if (data.getUserPrefEntries() != null) {
      for (final UserPrefEntryDO param : data.getSortedUserPrefEntries()) {
        final WebMarkupContainer item = new WebMarkupContainer(parametersRepeater.newChildId());
        parametersRepeater.add(item);
        final Label label = new Label("label", param.getI18nKey() != null ? getString(param.getI18nKey()) : param.getParameter());
        item.add(label.setRenderBodyOnly(true));
        if (StringUtils.isNotEmpty(param.getTooltipI18nKey()) == true) {
          item.add(new TooltipImage("parameterHelpImage", getResponse(), WebConstants.IMAGE_HELP, getString(param.getTooltipI18nKey())));
        } else {
          item.add(new TooltipImage("parameterHelpImage", getResponse(), WebConstants.IMAGE_HELP, "").setVisible(false));
        }
        TextField<String> textField = null;
        TextArea<String> textArea = null;
        DropDownChoice< ? > valueChoice = null;
        Component valueField = null;
        parentPage.userPrefDao.updateParameterValueObject(param);
        if (PFUserDO.class.isAssignableFrom(param.getType()) == true) {
          final UserSelectPanel userSelectPanel = new UserSelectPanel("valueField", new UserPrefPropertyModel<PFUserDO>(userPrefDao, param,
              "valueAsObject"), parentPage, param.getParameter());
          if (data.getArea() == UserPrefArea.USER_FAVORITE) {
            userSelectPanel.setShowFavorites(false);
          }
          valueField = userSelectPanel;
        } else if (TaskDO.class.isAssignableFrom(param.getType()) == true) {
          final TaskSelectPanel taskSelectPanel = new TaskSelectPanel("valueField", new UserPrefPropertyModel<TaskDO>(userPrefDao, param,
              "valueAsObject"), parentPage, param.getParameter());
          if (data.getArea() == UserPrefArea.TASK_FAVORITE) {
            taskSelectPanel.setShowFavorites(false);
          }
          valueField = taskSelectPanel;
        } else if (Kost2DO.class.isAssignableFrom(param.getType()) == true) {
          final UserPrefEntryDO taskParam = data.getUserPrefEntry(param.getDependsOn());
          Integer taskId = null;
          if (taskParam == null) {
            log.error("Annotation for Kost2DO types should have a valid dependsOn annotation. Task param not found for: " + param);
          } else {
            final TaskDO task = (TaskDO) taskParam.getValueAsObject();
            if (task != null) {
              taskId = task.getId();
            }
          }
          final Kost2DropDownChoice kost2DropDownChoice = new Kost2DropDownChoice("valueChoice", (Kost2DO) param.getValueAsObject(), taskId) {
            @Override
            protected void setKost2Id(Integer kost2Id)
            {
              param.setValue(String.valueOf(kost2Id));
            }
          };
          valueChoice = kost2DropDownChoice;
          dependentsMap.put(param.getParameter(), kost2DropDownChoice);
        } else if (ProjektDO.class.isAssignableFrom(param.getType()) == true) {
          final ProjektSelectPanel projektSelectPanel = new ProjektSelectPanel("valueField", new UserPrefPropertyModel<ProjektDO>(
              userPrefDao, param, "valueAsObject"), parentPage, param.getParameter());
          if (data.getArea() == UserPrefArea.PROJEKT_FAVORITE) {
            projektSelectPanel.setShowFavorites(false);
          }
          valueField = projektSelectPanel;
        } else if (KundeDO.class.isAssignableFrom(param.getType()) == true) {
          final CustomerSelectPanel kundeSelectPanel = new CustomerSelectPanel("valueField", new UserPrefPropertyModel<KundeDO>(
              userPrefDao, param, "valueAsObject"), null, parentPage, param.getParameter());
          if (data.getArea() == UserPrefArea.KUNDE_FAVORITE) {
            kundeSelectPanel.setShowFavorites(false);
          }
          valueField = kundeSelectPanel;
        } else if (param.isMultiline() == true) {
          int maxLength = param.getMaxLength();
          if (maxLength <= 0 || UserPrefEntryDO.MAX_STRING_VALUE_LENGTH < maxLength) {
            maxLength = UserPrefEntryDO.MAX_STRING_VALUE_LENGTH;
          }
          textArea = new MaxLengthTextArea("textArea", new PropertyModel<String>(param, "value"), maxLength);
        } else if (I18nEnum.class.isAssignableFrom(param.getType()) == true) {
          final LabelValueChoiceRenderer<I18nEnum> choiceRenderer = new LabelValueChoiceRenderer<I18nEnum>(this, (I18nEnum[]) param
              .getType().getEnumConstants());
          final DropDownChoice<I18nEnum> choice = new DropDownChoice<I18nEnum>("valueChoice", new UserPrefPropertyModel<I18nEnum>(
              userPrefDao, param, "valueAsObject"), choiceRenderer.getValues(), choiceRenderer);
          choice.setNullValid(true);
          valueChoice = choice;
        } else {
          Integer maxLength = param.getMaxLength();
          if (maxLength == null || maxLength <= 0 || UserPrefEntryDO.MAX_STRING_VALUE_LENGTH < maxLength) {
            maxLength = UserPrefEntryDO.MAX_STRING_VALUE_LENGTH;
          }
          textField = new MaxLengthTextField("textField", new PropertyModel<String>(param, "value"), maxLength);
          textField.setRequired(param.isRequired());
        }
        if (valueField == null) {
          valueField = new Label("valueField");
          valueField.setVisible(false);
        }
        item.add(valueField);
        if (valueChoice == null) {
          valueChoice = new DropDownChoice<Integer>("valueChoice");
          valueChoice.setVisible(false);
        }
        item.add(valueChoice);
        if (textField == null) {
          textField = new TextField<String>("textField");
          textField.setVisible(false);
        }
        item.add(textField);
        if (textArea == null) {
          textArea = new TextArea<String>("textArea");
          textArea.setVisible(false);
        }
        item.add(textArea);
        if (valueField instanceof AbstractSelectPanel< ? >) {
          ((AbstractSelectPanel< ? >) valueField).setRequired(param.isRequired());
          ((AbstractSelectPanel< ? >) valueField).init();
        }
      }
    }
    parametersRepeater.setVisible(true);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  void setData(final UserPrefDO data)
  {
    this.data = data;
  }

  private class UserPrefPropertyModel<T> extends PropertyModel<T>
  {
    private static final long serialVersionUID = 6644505091461853375L;

    private UserPrefDao userPrefDao;

    private UserPrefEntryDO userPrefEntry;

    public UserPrefPropertyModel(final UserPrefDao userPrefDao, final UserPrefEntryDO userPrefEntry, final String expression)
    {
      super(userPrefEntry, expression);
      this.userPrefDao = userPrefDao;
      this.userPrefEntry = userPrefEntry;
    }

    @Override
    public void setObject(final T object)
    {
      super.setObject(object);
      userPrefDao.setValueObject(userPrefEntry, object);
    };
  }
}
