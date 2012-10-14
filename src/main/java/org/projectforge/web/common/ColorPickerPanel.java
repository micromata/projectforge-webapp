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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.template.JavaScriptTemplate;
import org.apache.wicket.util.template.PackageTextTemplate;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class ColorPickerPanel extends Panel
{
  private static final long serialVersionUID = 2327758640305381880L;

  private String selectedColor;

  /**
   * @param id
   */
  public ColorPickerPanel(final String id)
  {
    this(id, "#FAAF26");
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
    final TextField<String> colorField = new TextField<String>("color", new PropertyModel<String>(this, "selectedColor"));
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
    // colorpicker js
    final JavaScriptTemplate jsTemplate = new JavaScriptTemplate(new PackageTextTemplate(ColorPickerPanel.class, "ColorPicker.js.template"));
    final String javaScript = jsTemplate.asString(new MicroMap<String, String>("markupId", colorField.getMarkupId()));
    add(new Label("template", javaScript).setEscapeModelStrings(false));
  }


  /**
   * Hook method
   * 
   * @param selectedColor
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
