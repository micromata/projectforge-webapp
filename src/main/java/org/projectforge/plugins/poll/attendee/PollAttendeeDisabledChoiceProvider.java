/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.attendee;

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
public class PollAttendeeDisabledChoiceProvider extends TextChoiceProvider<PollAttendeeDO>
{
  private static final long serialVersionUID = 6025289092378986901L;

  private final List<PollAttendeeDO> attendees;

  /**
   * 
   */
  public PollAttendeeDisabledChoiceProvider(final List<PollAttendeeDO> attendees)
  {
    this.attendees = attendees;
  }

  /**
   * @see com.vaynberg.wicket.select2.TextChoiceProvider#getDisplayText(java.lang.Object)
   */
  @Override
  protected String getDisplayText(final PollAttendeeDO choice)
  {
    return choice == null ? "" : choice.toString();
  }

  /**
   * @see com.vaynberg.wicket.select2.TextChoiceProvider#getId(java.lang.Object)
   */
  @Override
  protected String getId(final PollAttendeeDO choice)
  {
    return choice.getUser() == null ? choice.getEmail() : "" + choice.getUser().getId();
  }

  /**
   * @see com.vaynberg.wicket.select2.ChoiceProvider#query(java.lang.String, int, com.vaynberg.wicket.select2.Response)
   */
  @Override
  public void query(final String term, final int page, final Response<PollAttendeeDO> response)
  {
    // do nothing
  }

  /**
   * @see com.vaynberg.wicket.select2.ChoiceProvider#toChoices(java.util.Collection)
   */
  @Override
  public Collection<PollAttendeeDO> toChoices(final Collection<String> ids)
  {
    return attendees;
  }

}
