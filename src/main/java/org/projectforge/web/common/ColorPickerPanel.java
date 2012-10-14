/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.common;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class ColorPickerPanel extends Panel
{
  private static final long serialVersionUID = 2327758640305381880L;

  private String selectedColor;

  public static final String[] COLORS = { "#A8735B", "#CA6D62", "#EF4013", "#F25B33", "#F87824", "#FAAF26", "#5DD689", "#3AA75C",
    "#87D117", "#B8DC56", "#FBEA71", "#F8D24D", "#A6E0E8", "#A6E0E8", "#A2C5EA", "#4C84EE", "#989BFF", "#B599FF", "#C2C2C2", "#C9BDBF",
    "#C9A6AC", "#EF92B4", "#C674EE", "#9F79E9"};

  /**
   * @param id
   */
  public ColorPickerPanel(final String id)
  {
    this(id, COLORS[0]);
  }

  /**
   * @param string
   * @param color
   */
  public ColorPickerPanel(final String id, final String color)
  {
    super(id);
    this.selectedColor = color;
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    final WebMarkupContainer preview = new WebMarkupContainer("preview") {
      private static final long serialVersionUID = 1L;

      /**
       * @see org.apache.wicket.Component#onBeforeRender()
       */
      @Override
      protected void onBeforeRender()
      {
        super.onBeforeRender();
        // update color please
        add(new AttributeModifier("style", Model.of("background-color: " + selectedColor)));
      }
    };
    preview.setOutputMarkupId(true);
    add(preview);

    final Form<Void> colorForm = new Form<Void>("colorForm");
    add(colorForm);
    final HiddenField<String> colorField = new HiddenField<String>("color", new PropertyModel<String>(this, "selectedColor"));
    colorField.add(new AjaxFormComponentUpdatingBehavior("onChange") {
      private static final long serialVersionUID = 1L;

      /**
       * @see org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior#onUpdate(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        onColorUpdate(selectedColor);
        target.add(preview);
      }
    });
    colorForm.add(colorField);
    final RepeatingView repeater = new RepeatingView("colorRepeater");
    add(repeater);
    for (final String color : COLORS) {
      final WebMarkupContainer colorSpan = new WebMarkupContainer(repeater.newChildId());
      repeater.add(colorSpan);
      colorSpan.add(new AttributeModifier("style", Model.of("background-color: " + color)));
    }
  }

  /**
   * Hook method
   * 
   * @param selectedColor2
   */
  protected void onColorUpdate(final String selectedColor)
  {

  }

  /**
   * @return the selectedColor
   */
  public String getSelectedColor()
  {
    return selectedColor;
  }

  /**
   * @param selectedColor the selectedColor to set
   * @return this for chaining.
   */
  public ColorPickerPanel setSelectedColor(final String selectedColor)
  {
    this.selectedColor = selectedColor;
    return this;
  }

}
