/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.web.wicket.flowlayout;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;

/**
 * Represents a entry of a group panel. This can be a label, text field or other form components.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class AbstractFieldsetPanel<T extends AbstractFieldsetPanel< ? >> extends Panel
{
  private static final long serialVersionUID = -4215154959282166107L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractFieldsetPanel.class);

  protected WebMarkupContainer fieldset;

  protected WebMarkupContainer label;

  protected boolean labelFor, childAdded;

  protected boolean multipleChildren;

  protected String labelText;

  protected RepeatingView repeater;

  /**
   * Adds this FieldsetPanel to the parent panel.
   * @param parent
   * @param label
   */
  protected AbstractFieldsetPanel(final String id)
  {
    super(id);
  }

  public T setUnit(final String unit)
  {
    this.labelText = WicketUtils.getLabelWithUnit(labelText, unit);
    return getThis();
  }

  /**
   * @return the labelText
   */
  public String getLabel()
  {
    return labelText;
  }

  /**
   * Sets the background color of this whole fieldset to red.
   * @return this for chaining.
   */
  public T setWarningBackground()
  {
    fieldset.add(AttributeModifier.replace("style", WebConstants.CSS_BACKGROUND_COLOR_RED));
    return getThis();
  }

  public T setLabelFor(final Component component)
  {
    if (component instanceof ComponentWrapperPanel) {
      this.label.add(AttributeModifier.replace("for", ((ComponentWrapperPanel) component).getComponentOutputId()));
    } else {
      this.label.add(AttributeModifier.replace("for", component.getOutputMarkupId()));
    }
    labelFor = true;
    return getThis();
  }

  /**
   * Declares that there is no validation field which the label should set for. This has no other meaning and effect than not to display the
   * development warning "No label set for field...'.
   * @return
   */
  public T setNoLabelFor()
  {
    labelFor = true;
    return getThis();
  }

  public Component superAdd(final Component... childs)
  {
    return super.add(childs);
  }

  /**
   * @see org.apache.wicket.MarkupContainer#add(org.apache.wicket.Component[])
   */
  @Override
  public MarkupContainer add(final Component... childs)
  {
    if (repeater == null) {
      if (childAdded == true) {
        throw new IllegalArgumentException("You can't add multiple children, please call constructor with multipleChildren=true.");
      }
      childAdded = true;
      checkLabelFor(childs);
      return addChild(childs);
    } else {
      childAdded = true;
      checkLabelFor(childs);
      for (final Component component : childs) {
        modifyAddedChild(component);
      }
      return repeater.add(childs);
    }
  }

  /**
   * @param textField
   * @return The created InputPanel.
   * @see InputPanel#InputPanel(String, Component)
   */
  public InputPanel add(final TextField< ? > textField)
  {
    final InputPanel input = new InputPanel(newChildId(), textField);
    if (textField.getLabel() == null) {
      textField.setLabel(new Model<String>(labelText));
    }
    add(input);
    return input;
  }

  /**
   * @param textField
   * @return The created InputPanel.
   * @see InputPanel#InputPanel(String, Component)
   */
  public InputPanel add(final TextField< ? > textField, final FieldProperties< ? > fieldProperties)
  {
    final InputPanel input = add(textField);
    if (fieldProperties.getFieldType() != null) {
      setFieldType(input, fieldProperties.getFieldType());
    }
    return input;
  }

  protected InputPanel setFieldType(final InputPanel input, final FieldType fieldType)
  {
    input.setFieldType(fieldType);
    return input;
  }

  /**
   * @param passwordField
   * @return The created PasswordPanel.
   * @see PasswordPanel#PasswordPanel(String, Component)
   */
  public PasswordPanel add(final PasswordTextField passwordField)
  {
    final PasswordPanel passwordInput = new PasswordPanel(newChildId(), passwordField);
    if (passwordField.getLabel() == null) {
      passwordField.setLabel(new Model<String>(labelText));
    }
    add(passwordInput);
    return passwordInput;
  }

  /**
   * @return The Wicket id of the embedded text field of InputPanel
   */
  public final String getTextFieldId()
  {
    return InputPanel.WICKET_ID;
  }

  /**
   * @param textArea
   * @return The created InputPanel.
   * @see TextAreaPanel#TextAreaPanel(String, Component)
   */
  public TextAreaPanel add(final TextArea< ? > textArea)
  {
    final TextAreaPanel panel = new TextAreaPanel(newChildId(), textArea);
    if (textArea.getLabel() == null) {
      textArea.setLabel(new Model<String>(labelText));
    }
    add(panel);
    return panel;
  }

  /**
   * @return The Wicket id of the embedded text field of TextAreaPanel
   */
  public final String getTextAreaId()
  {
    return TextAreaPanel.WICKET_ID;
  }

  /**
   * @param id
   * @param label
   * @param model
   * @param values
   * @param renderer
   * @return The created DropDownChoicePanel.
   * @see DropDownChoicePanel#DropDownChoicePanel(String, String, IModel, List, IChoiceRenderer)
   */
  public <C> DropDownChoicePanel<C> addDropDownChoice(final IModel<C> model, final List< ? extends C> values,
      final IChoiceRenderer<C> renderer)
      {
    return addDropDownChoice(model, values, renderer, false);
      }

  /**
   * @param id
   * @param label
   * @param model
   * @param values
   * @param renderer
   * @param submitOnChange.
   * @return The created DropDownChoicePanel.
   * @see DropDownChoicePanel#DropDownChoicePanel(String, String, IModel, List, IChoiceRenderer, boolean))
   */
  public <C> DropDownChoicePanel<C> addDropDownChoice(final IModel<C> model, final List< ? extends C> values,
      final IChoiceRenderer<C> renderer, final boolean submitOnChange)
      {
    final DropDownChoicePanel<C> dropDownChoicePanel = new DropDownChoicePanel<C>(newChildId(), model, values, renderer, submitOnChange);
    dropDownChoicePanel.getDropDownChoice().setLabel(new Model<String>(getLabel()));
    add(dropDownChoicePanel);
    return dropDownChoicePanel;
      }

  /**
   * @param id
   * @param label
   * @param dropDownChoice
   * @return The created DropDownChoicePanel.
   * @see DropDownChoicePanel#DropDownChoicePanel(String, String, DropDownChoice)
   */
  public <C> DropDownChoicePanel<C> add(final DropDownChoice<C> dropDownChoice)
  {
    dropDownChoice.setLabel(new Model<String>(getLabel()));
    return add(dropDownChoice, false);
  }

  /**
   * @param id
   * @param label
   * @param dropDownChoice
   * @return The created DropDownChoicePanel.
   * @see DropDownChoicePanel#DropDownChoicePanel(String, String, DropDownChoice, boolean)
   */
  public <C> DropDownChoicePanel<C> add(final DropDownChoice<C> dropDownChoice, final boolean submitOnChange)
  {

    final DropDownChoicePanel<C> dropDownChoicePanel = new DropDownChoicePanel<C>(newChildId(), dropDownChoice, submitOnChange);
    dropDownChoicePanel.getDropDownChoice().setLabel(new Model<String>(getLabel()));
    add(dropDownChoicePanel);
    return dropDownChoicePanel;
  }

  /**
   * @return The Wicket id of the embedded text fiel of {@link DropDownChoicePanel}.
   */
  public String getDropDownChoiceId()
  {
    return DropDownChoicePanel.WICKET_ID;
  }

  public abstract String newChildId();

  protected void modifyAddedChild(final Component child)
  {

  }

  protected abstract MarkupContainer addChild(Component... childs);

  private void checkLabelFor(final Component... components)
  {
    if (labelFor == true) {
      return;
    }
    final Component component = components[0];
    if (component instanceof ComponentWrapperPanel) {
      this.label.add(AttributeModifier.replace("for", ((ComponentWrapperPanel) component).getComponentOutputId()));
      labelFor = true;
    }
    for (final Component comp : components) {
      if (comp instanceof FormComponent) {
        ((FormComponent< ? >) comp).setLabel(new Model<String>(getLabel()));
      }
    }
  }

  /**
   * @see org.apache.wicket.Component#onBeforeRender()
   */
  @Override
  protected void onBeforeRender()
  {
    if (labelFor == false && WebConfiguration.isDevelopmentMode() == true) {
      log.warn("No label set for field '" + labelText + "'. Please call setLabelFor(component) for this fieldset.");
    }
    super.onBeforeRender();
    if (childAdded == false) {
      childAdded = true;
      addInvisibleChild();
    }
  }

  protected abstract void addInvisibleChild();

  protected abstract T getThis();
}
