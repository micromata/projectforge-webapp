package de.micromata.wicket.ajax;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;

/**
 * Own {@link AbstractDefaultAjaxBehavior}, just to make getCallbackScript public.<br/>
 * 
 * 
 * @author <a href="mailto:j.unterstein@micromata.de">Johannes Unterstein</a>
 * @see Wicket 6.0.0
 */
public abstract class MDefaultAjaxBehavior extends AbstractDefaultAjaxBehavior
{

  private static final long serialVersionUID = 5092956245673681067L;

  @Override
  public CharSequence getCallbackScript()
  {
    return super.getCallbackScript();
  }
}