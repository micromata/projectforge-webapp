package de.micromata.wicket.ajax;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;

/**
 * Call back interface for micromata wicket ajax form submitting handlings.
 * 
 * @author <a href="mailto:j.unterstein@micromata.de">Johannes Unterstein</a>
 * 
 */
public interface AjaxFormSubmitCallback extends AjaxCallback
{

  void onError(AjaxRequestTarget target, Form< ? > form);
}
