/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.utils;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.core.I18nEnum;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;

public class WicketUtil
{
  /**
   * Creates a wicket dropdown menu for I18nEnum.
   * @param <E>
   * @param parent parent component
   * @param wicketId the wicket id
   * @param propertyName property name of the model object
   * @param modelObject A model object holding the property that is used to set/get the field value
   * @param values all possible I18nEnum values
   * @return A DropDownChoice component
   */
  public static <E> DropDownChoice<E> getNewDropDownChoice(final Component parent, final String wicketId, final String propertyName, final Object modelObject, final I18nEnum[] values)
  {
    final LabelValueChoiceRenderer<E> choiceRenderer = new LabelValueChoiceRenderer<E>(parent, values);
    final DropDownChoice<E> dropDownChoice = new DropDownChoice<E>(wicketId, new PropertyModel<E>(
        modelObject, propertyName), choiceRenderer.getValues(), choiceRenderer);
    return dropDownChoice;
  }


}
