/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.event;

import java.util.Collection;
import java.util.List;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

/**
 * <b>ATTENTION</b> Just use this for disabled selections!
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class PollEventDisabledChoiceProvider extends TextChoiceProvider<PollEventDO>
{
  private static final long serialVersionUID = 6025289092378986901L;

  private final List<PollEventDO> attendees;

  /**
   * 
   */
  public PollEventDisabledChoiceProvider(final List<PollEventDO> attendees)
  {
    this.attendees = attendees;
  }

  /**
   * @see com.vaynberg.wicket.select2.TextChoiceProvider#getDisplayText(java.lang.Object)
   */
  @Override
  protected String getDisplayText(final PollEventDO choice)
  {
    return choice == null ? "" : choice.toString();
  }

  /**
   * @see com.vaynberg.wicket.select2.TextChoiceProvider#getId(java.lang.Object)
   */
  @Override
  protected String getId(final PollEventDO choice)
  {
    return "" + choice.toString();
  }

  /**
   * @see com.vaynberg.wicket.select2.ChoiceProvider#query(java.lang.String, int, com.vaynberg.wicket.select2.Response)
   */
  @Override
  public void query(final String term, final int page, final Response<PollEventDO> response)
  {
    // just do nothing
  }

  /**
   * @see com.vaynberg.wicket.select2.ChoiceProvider#toChoices(java.util.Collection)
   */
  @Override
  public Collection<PollEventDO> toChoices(final Collection<String> ids)
  {
    return attendees;
  }

}
