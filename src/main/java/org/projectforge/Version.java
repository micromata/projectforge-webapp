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

package org.projectforge;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a version number (major-release, minor-release, patch-level and build-number).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class Version implements Comparable<Version>, Serializable
{
  private static final long serialVersionUID = 1446772593211999270L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Version.class);

  private int majorRelease, minorRelease, patchLevel, buildNumber, betaVersion = -1;

  private String asString;

  /**
   * Supported formats: "#" ("3"), "#.#" ("3.5"), "#.#.#" ("3.5.4") or "#.#.#.#" ("3.5.4.2"). Append b# for marking version as beta version.
   * @param version
   */
  public Version(final String version)
  {
    if (version == null) {
      return;
    }
    final int betaPos = version.indexOf('b');
    final String str = betaPos >= 0 ? version.substring(0, betaPos) : version;
    final String[] sa = StringUtils.split(str, ".");
    if (sa.length > 0) {
      majorRelease = parseInt(version, sa[0]);
      if (sa.length > 1) {
        minorRelease = parseInt(version, sa[1]);
        if (sa.length > 2) {
          patchLevel = parseInt(version, sa[2]);
          if (sa.length > 3) {
            buildNumber = parseInt(version, sa[3]);
          }
        }
      }
    }
    if (betaPos >= 0) {
      if (betaPos < version.length()) {
        betaVersion = parseInt(version, version.substring(betaPos + 1));
      } else {
        betaVersion = 0;
      }
    }
    asString();
  }

  public Version(final int majorRelease, final int minorRelease, final int patchLevel)
  {
    this(majorRelease, minorRelease, patchLevel, 0);
  }

  public Version(final int majorRelease, final int minorRelease, final int patchLevel, final int buildNumber)
  {
    this.majorRelease = majorRelease;
    this.minorRelease = minorRelease;
    this.patchLevel = patchLevel;
    this.buildNumber = buildNumber;
    asString();
  }

  public int getMajorRelease()
  {
    return majorRelease;
  }

  public int getMinorRelease()
  {
    return minorRelease;
  }

  public int getPatchLevel()
  {
    return patchLevel;
  }

  public int getBuildNumber()
  {
    return buildNumber;
  }

  /**
   * @param betaVersion
   * @return this for chaining.
   */
  public Version setBetaVersion(int betaVersion)
  {
    this.betaVersion = betaVersion;
    return this;
  }

  public int getBetaVersion()
  {
    return betaVersion;
  }

  public boolean isBeta()
  {
    return betaVersion >= 0;
  }

  @Override
  public int compareTo(final Version o)
  {
    int compare = compare(this.majorRelease, o.majorRelease);
    if (compare != 0) {
      return compare;
    }
    compare = compare(this.minorRelease, o.minorRelease);
    if (compare != 0) {
      return compare;
    }
    compare = compare(this.patchLevel, o.patchLevel);
    if (compare != 0) {
      return compare;
    }
    return compare(this.buildNumber, o.buildNumber);
  }

  /**
   * @return Version as string: "#.#" ("3.0"), "#.#.#" ("3.5.4"), "#.#.#.#" ("3.5.4.2") or "#.*.#b#" ("3.5.4.2b2").
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return asString;
  }

  private void asString()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(majorRelease);
    sb.append('.').append(minorRelease);
    if (patchLevel != 0 || buildNumber != 0) {
      sb.append('.').append(patchLevel);
      if (buildNumber != 0) {
        sb.append('.').append(buildNumber);
      }
    }
    if (betaVersion >= 0) {
      sb.append('b').append(betaVersion);
    }
    asString = sb.toString();
  }

  private int parseInt(final String version, final String str)
  {
    try {
      return new Integer(str);
    } catch (NumberFormatException ex) {
      log.error("Can't parse version string '" + version + "'. '" + str + "'isn't a number");
    }
    return 0;
  }

  private int compare(final int i1, final int i2)
  {
    if (i1 < i2) {
      return -1;
    } else if (i1 > i2) {
      return 1;
    } else {
      return 0;
    }
  }
}
