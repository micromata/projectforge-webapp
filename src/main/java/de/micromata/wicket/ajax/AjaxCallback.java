package de.micromata.wicket.ajax;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * Call back interface for micromata wicket ajax handlings.
 * 
 * @author <a href="mailto:j.unterstein@micromata.de">Johannes Unterstein</a>
 * 
 */
public interface AjaxCallback extends Serializable
{

  void callback(AjaxRequestTarget target);
}
