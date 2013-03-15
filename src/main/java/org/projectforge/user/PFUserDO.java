/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.user;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.joda.time.DateTimeZone;
import org.projectforge.common.ReflectionToString;
import org.projectforge.common.TimeNotation;
import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.AbstractHistorizableBaseDO;
import org.projectforge.core.BaseDO;
import org.projectforge.core.Configuration;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.ModificationStatus;
import org.projectforge.core.ShortDisplayNameCapable;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PF_USER", uniqueConstraints = { @UniqueConstraint(columnNames = { "username"})})
public class PFUserDO extends DefaultBaseDO implements ShortDisplayNameCapable
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PFUserDO.class);

  private static final long serialVersionUID = 6680346054753032534L;

  private static final String NOPASSWORD = "--- none ---";

  static {
    AbstractHistorizableBaseDO.putNonHistorizableProperty(PFUserDO.class, "loginFailures", "lastLogin", "stayLoggedInKey");
  }

  private transient Map<String, Object> attributeMap;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String username;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String jiraUsername;

  private String password;

  private boolean localUser;

  private boolean restrictedUser;

  private boolean deactivated;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String firstname;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String lastname;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String email;

  private String stayLoggedInKey;

  private String authenticationToken;

  private Timestamp lastLogin;

  private int loginFailures;

  private Locale locale;

  private TimeZone timeZone;

  private Locale clientLocale;

  private String dateFormat;

  private String excelDateFormat;

  private Integer firstDayOfWeek;

  private TimeNotation timeNotation;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String organization;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String personalPhoneIdentifiers;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String personalMebMobileNumbers;

  private Set<UserRightDO> rights = new HashSet<UserRightDO>();

  private boolean hrPlanning = true;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String ldapValues;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String sshPublicKey;

  @Transient
  public String getShortDisplayName()
  {
    return getUsername();
  }

  @Column(length = 255)
  public String getOrganization()
  {
    return organization;
  }

  public PFUserDO setOrganization(final String organization)
  {
    this.organization = organization;
    return this;
  }

  /**
   * @return For example "Europe/Berlin" if time zone is given otherwise empty string.
   */
  @Transient
  public String getTimeZoneDisplayName()
  {
    if (timeZone == null) {
      return "";
    }
    return timeZone.getDisplayName();
  }

  /**
   * @return For example "Europe/Berlin" if time zone is given otherwise empty string.
   */
  @Column(name = "time_zone")
  public String getTimeZone()
  {
    if (timeZone == null) {
      return "";
    }
    return timeZone.getID();
  }

  public void setTimeZone(final String timeZoneId)
  {
    if (StringUtils.isNotBlank(timeZoneId) == true) {
      setTimeZone(TimeZone.getTimeZone(timeZoneId));
    }
  }

  public void setTimeZone(final TimeZone timeZone)
  {
    this.timeZone = timeZone;
  }

  @Transient
  public TimeZone getTimeZoneObject()
  {
    if (timeZone != null) {
      return this.timeZone;
    } else {
      return Configuration.getInstance().getDefaultTimeZone();
    }
  }

  public void setTimeZoneObject(final TimeZone timeZone)
  {
    this.timeZone = timeZone;
  }

  @Transient
  public DateTimeZone getDateTimeZone()
  {
    final TimeZone timeZone = getTimeZoneObject();
    return DateTimeZone.forID(timeZone.getID());
  }

  @Column
  public Locale getLocale()
  {
    return locale;
  }

  public void setLocale(final Locale locale)
  {
    this.locale = locale;
  }

  /**
   * Default date format for the user. Examples:
   * <ul>
   * <li>yyyy-MM-dd: 2011-02-21, ISO format.</li>
   * <li>dd.MM.yyyy: 21.02.2011, German format (day of month first)</li>
   * <li>dd/MM/yyyy: 21/02/2011, British and French format (day of month first)</li>
   * <li>MM/dd/yyyy: 02/21/2011, American format (month first)</li>
   * </ul>
   * @return
   */
  @Column(name = "date_format", length = 20)
  public String getDateFormat()
  {
    return dateFormat;
  }

  public void setDateFormat(final String dateFormat)
  {
    this.dateFormat = dateFormat;
  }

  /**
   * Default excel date format for the user. Examples:
   * <ul>
   * <li>DD.MM.YYYY: 21.02.2011, German format (day of month first)</li>
   * <li>DD/MM/YYYY: 21/02/2011, British and French format (day of month first)</li>
   * <li>MM/DD/YYYY: 02/21/2011, American format (month first)</li>
   * </ul>
   * @return
   */
  @Column(name = "excel_date_format", length = 20)
  public String getExcelDateFormat()
  {
    return excelDateFormat;
  }

  public void setExcelDateFormat(final String excelDateFormat)
  {
    this.excelDateFormat = excelDateFormat;
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "time_notation", length = 6)
  public TimeNotation getTimeNotation()
  {
    return timeNotation;
  }

  /**
   * 0 - sunday, 1 - monday etc.
   * @return the firstDayOfWeek
   */
  @Column(name = "first_day_of_week")
  public Integer getFirstDayOfWeek()
  {
    return firstDayOfWeek;
  }

  /**
   * @param firstDayOfWeek the firstDayOfWeek to set
   * @return this for chaining.
   */
  public PFUserDO setFirstDayOfWeek(final Integer firstDayOfWeek)
  {
    this.firstDayOfWeek = firstDayOfWeek;
    return this;
  }

  public void setTimeNotation(final TimeNotation timeNotation)
  {
    this.timeNotation = timeNotation;
  }

  /**
   * Eine kommaseparierte Liste mit den Kennungen des/der Telefon(e) des Mitarbeiters an der unterstützten Telefonanlage, &zB; zur
   * Direktwahl aus ProjectForge heraus.
   */
  @Column(name = "personal_phone_identifiers", length = 255)
  public String getPersonalPhoneIdentifiers()
  {
    return personalPhoneIdentifiers;
  }

  public void setPersonalPhoneIdentifiers(final String personalPhoneIdentifiers)
  {
    this.personalPhoneIdentifiers = personalPhoneIdentifiers;
  }

  /**
   * A comma separated list of all personal mobile numbers from which SMS can be send. Those SMS will be assigned to this user. <br/>
   * This is a feature from the Mobile Enterprise Blogging.
   */
  @Column(name = "personal_meb_identifiers", length = 255)
  public String getPersonalMebMobileNumbers()
  {
    return personalMebMobileNumbers;
  }

  public void setPersonalMebMobileNumbers(final String personalMebMobileNumbers)
  {
    this.personalMebMobileNumbers = personalMebMobileNumbers;
  }

  /**
   * Returns string containing all fields (except the password) of given user object (via ReflectionToStringBuilder).
   * @param user
   * @return
   */
  @Override
  public String toString()
  {
    return (new ReflectionToString(this) {
      @Override
      protected boolean accept(final java.lang.reflect.Field f)
      {
        return super.accept(f)
            && !"password".equals(f.getName())
            && !"stayLoggedInKey".equals(f.getName())
            && !"authenticationToken".equals(f.getName());
      }
    }).toString();
  }

  @Transient
  public String getUserDisplayname()
  {
    final String str = getFullname();
    if (StringUtils.isNotBlank(str) == true) {
      return str + " (" + getUsername() + ")";
    }
    return getUsername();
  }

  @Override
  public boolean equals(final Object o)
  {
    if (o instanceof PFUserDO) {
      final PFUserDO other = (PFUserDO) o;
      if (ObjectUtils.equals(this.getUsername(), other.getUsername()) == false)
        return false;
      return true;
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    return getUsername() == null ? 0 : getUsername().hashCode();
  }

  @Override
  public ModificationStatus copyValuesFrom(final BaseDO< ? extends Serializable> src, String... ignoreFields)
  {
    ignoreFields = (String[]) ArrayUtils.add(ignoreFields, "password"); // NPE save considering ignoreFields
    final PFUserDO user = (PFUserDO) src;
    ModificationStatus modificationStatus = AbstractBaseDO.copyValues(user, this, ignoreFields);
    if (user.getPassword() != null) {
      setPassword(user.getPassword());
      checkAndFixPassword();
      if (getPassword() != null) {
        modificationStatus = ModificationStatus.MAJOR;
      }
    }
    return modificationStatus;
  }

  /**
   * If password is not given as "SHA{..." then it will be set to null due to security reasons.
   */
  protected void checkAndFixPassword()
  {
    if (StringUtils.isNotEmpty(getPassword()) == true
        && getPassword().startsWith("SHA{") == false
        && getPassword().equals(NOPASSWORD) == false) {
      setPassword(null);
      log.error("Password for user '" + getUsername() + "' is not given SHA encrypted. Ignoring it.");
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked"})
  @Override
  @Transient
  public Set getHistorizableAttributes()
  {
    return null;
  }

  /**
   * The locale given from the client (e. g. from the browser by the http request). This locale is needed by PFUserContext for getting the
   * browser locale if the user's locale is null and the request's locale is not available.
   * @return
   */
  @Transient
  public Locale getClientLocale()
  {
    return clientLocale;
  }

  public PFUserDO setClientLocale(final Locale clientLocale)
  {
    this.clientLocale = clientLocale;
    return this;
  }

  /**
   * Do nothing.
   * @see org.projectforge.core.ExtendedBaseDO#recalculate()
   */
  @Override
  public void recalculate()
  {
  }

  @Override
  public Object getAttribute(final String key)
  {
    if (attributeMap == null) {
      return null;
    }
    return attributeMap.get(key);
  }

  @Override
  public void setAttribute(final String key, final Object value)
  {
    synchronized (this) {
      if (attributeMap == null) {
        attributeMap = new HashMap<String, Object>();
      }
      attributeMap.put(key, value);
    }
  }

  public void removeAttribute(final String key)
  {
    if (attributeMap == null) {
      return;
    }
    attributeMap.remove(key);
  }

  /**
   * @return Returns the username.
   */
  @Column(length = 255, unique = true, nullable = false)
  public String getUsername()
  {
    return username;
  }

  /**
   * @param username The username to set.
   * @return this for chaining.
   */
  public PFUserDO setUsername(final String username)
  {
    this.username = username;
    return this;
  }

  /**
   * Die E-Mail Adresse des Benutzers, falls vorhanden.
   * @return Returns the email.
   */
  @Column(length = 255)
  public String getEmail()
  {
    return email;
  }

  /**
   * @param email The email to set.
   * @return this for chaining.
   */
  public PFUserDO setEmail(final String email)
  {
    Validate.isTrue(email == null || email.length() <= 255, email);
    this.email = email;
    return this;
  }

  /**
   * Key stored in the cookies for the functionality of stay logged in.
   */
  @Column(name = "stay_logged_in_key", length = 255)
  public String getStayLoggedInKey()
  {
    return stayLoggedInKey;
  }

  public void setStayLoggedInKey(final String stayLoggedInKey)
  {
    this.stayLoggedInKey = stayLoggedInKey;
  }

  /**
   * The authentication token is usable for download links of the user (without further login). This is used e. g. for ics download links of
   * the team calendars.
   * @return the authenticationToken
   */
  @Column(name = "authentication_token", length = 100)
  public String getAuthenticationToken()
  {
    return authenticationToken;
  }

  /**
   * @param authenticationToken the authenticationToken to set
   * @return this for chaining.
   */
  public PFUserDO setAuthenticationToken(final String authenticationToken)
  {
    this.authenticationToken = authenticationToken;
    return this;
  }

  /**
   * Der Vorname des Benutzer.
   * @return Returns the firstname.
   */
  @Column(length = 255)
  public String getFirstname()
  {
    return firstname;
  }

  /**
   * @param firstname The firstname to set.
   * @return this for chaining.
   */
  public PFUserDO setFirstname(final String firstname)
  {
    Validate.isTrue(firstname == null || firstname.length() <= 255, firstname);
    this.firstname = firstname;
    return this;
  }

  /**
   * Gibt den Vor- und Nachnamen zurück, falls gegeben. Vor- und Nachname sind durch ein Leerzeichen getrennt.
   * @return String
   */
  @Transient
  public String getFullname()
  {
    final StringBuffer name = new StringBuffer();
    if (this.firstname != null) {
      name.append(this.firstname);
      name.append(" ");
    }
    if (this.lastname != null) {
      name.append(this.lastname);
    }

    return name.toString();
  }

  /**
   * Zeitstempel des letzten erfolgreichen Logins.
   * @return Returns the lastLogin.
   */
  public Timestamp getLastLogin()
  {
    return lastLogin;
  }

  /**
   * @return Returns the lastname.
   */
  @Column(length = 255)
  public String getLastname()
  {
    return lastname;
  }

  /**
   * @return Returns the description.
   */
  @Column(length = 255)
  public String getDescription()
  {
    return description;
  }

  /**
   * @param description The description to set.
   * @return this for chaining.
   */
  public PFUserDO setDescription(final String description)
  {
    Validate.isTrue(description == null || description.length() <= 255, description);
    this.description = description;
    return this;
  }

  /**
   * Die Anzahl der erfolglosen Logins. Dieser Wert wird bei dem nächsten erfolgreichen Login auf 0 zurück gesetzt.
   * @return Returns the loginFailures.
   */
  public int getLoginFailures()
  {
    return loginFailures;
  }

  /**
   * JIRA user name (if differ from the ProjectForge's user name) is used e. g. in MEB for creating new issues.
   */
  @Column(name = "jira_username", length = 100)
  public String getJiraUsername()
  {
    return jiraUsername;
  }

  public void setJiraUsername(final String jiraUsername)
  {
    this.jiraUsername = jiraUsername;
  }

  /**
   * @return The JIRA user name or if not given the user name (assuming that the JIRA user name is same as ProjectForge user name).
   */
  @Transient
  public String getJiraUsernameOrUsername()
  {
    if (StringUtils.isNotEmpty(jiraUsername) == true) {
      return this.jiraUsername;
    } else {
      return this.username;
    }
  }

  /**
   * Encoded password of the user (SHA-1).
   * @return Returns the password.
   */
  @Column(length = 50)
  public String getPassword()
  {
    return password;
  }

  /**
   * @param password The password to set.
   * @return this for chaining.
   */
  public PFUserDO setPassword(final String password)
  {
    this.password = password;
    return this;
  }

  /**
   * @param password The password to set.
   * @return this for chaining.
   */
  public PFUserDO setNoPassword()
  {
    this.password = NOPASSWORD;
    return this;
  }

  /**
   * @param lastLogin The lastLogin to set.
   */
  public void setLastLogin(final Timestamp lastLogin)
  {
    this.lastLogin = lastLogin;
  }

  /**
   * @param lastname The lastname to set.
   * @return this for chaining.
   */
  public PFUserDO setLastname(final String lastname)
  {
    this.lastname = lastname;
    return this;
  }

  /**
   * @param loginFailures The loginFailures to set.
   */
  public void setLoginFailures(final int loginFailures)
  {
    this.loginFailures = loginFailures;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "user")
  public Set<UserRightDO> getRights()
  {
    return this.rights;
  }

  public void setRights(final Set<UserRightDO> rights)
  {
    this.rights = rights;
  }

  public PFUserDO addRight(final UserRightDO right)
  {
    if (this.rights == null) {
      setRights(new HashSet<UserRightDO>());
    }
    this.rights.add(right);
    right.setUser(this);
    return this;
  }

  @Transient
  public UserRightDO getRight(final UserRightId rightId)
  {
    if (this.rights == null) {
      return null;
    }
    for (final UserRightDO right : this.rights) {
      if (right.getRightId().equals(rightId) == true) {
        return right;
      }
    }
    return null;
  }

  /**
   * If true (default) then the user is highlighted in the human resource planning page if not planned for the actual week.
   * @return the hrPlanning
   */
  @Column(name = "hr_planning", nullable = false)
  public boolean isHrPlanning()
  {
    return hrPlanning;
  }

  /**
   * @param hrPlanning the hrPlanning to set
   * @return this for chaining.
   */
  public PFUserDO setHrPlanning(final boolean hrPlanning)
  {
    this.hrPlanning = hrPlanning;
    return this;
  }

  /**
   * A local user will not be synchronized with any external user management system.
   * @return the localUser
   */
  @Column(name = "local_user", nullable = false)
  public boolean isLocalUser()
  {
    return localUser;
  }

  /**
   * @param localUser the localUser to set
   * @return this for chaining.
   */
  public PFUserDO setLocalUser(final boolean localUser)
  {
    this.localUser = localUser;
    return this;
  }

  /**
   * A restricted user has only the ability to log-in and to change his password. This is useful if ProjectForge runs in master mode for
   * managing an external LDAP system. Then this user is a LDAP user but has no other functionality than change password in the ProjectForge
   * system itself.
   * @return the restrictedUser
   */
  @Column(name = "restricted_user", nullable = false)
  public boolean isRestrictedUser()
  {
    return restrictedUser;
  }

  /**
   * @param restrictedUser the restrictedUser to set
   * @return this for chaining.
   */
  public PFUserDO setRestrictedUser(final boolean restrictedUser)
  {
    this.restrictedUser = restrictedUser;
    return this;
  }

  /**
   * A deactivated user has no more system access.
   * @return the deactivated
   */
  @Column(nullable = false)
  public boolean isDeactivated()
  {
    return deactivated;
  }

  /**
   * @param deactivated the deactivated to set
   * @return this for chaining.
   */
  public PFUserDO setDeactivated(final boolean deactivated)
  {
    this.deactivated = deactivated;
    return this;
  }

  /**
   * LDAP values as key-value-pairs, e. g. gidNumber=1000,uidNumber=1001,homeDirectory="/home/kai",shell="/bin/bash". For handling of string
   * values see {@link org.apache.commons.csv.writer.CSVWriter}. This field is handled by the ldap package and has no further effect in
   * ProjectForge's core package.
   * @return the ldapValues
   */
  @Column(name = "ldap_values", length = 4000)
  public String getLdapValues()
  {
    return ldapValues;
  }

  /**
   * @param ldapValues the ldapValues to set
   * @return this for chaining.
   */
  public PFUserDO setLdapValues(final String ldapValues)
  {
    this.ldapValues = ldapValues;
    return this;
  }

  /**
   * @return the sshPublicKey
   */
  @Column(name = "ssh_public_key", length = 1000)
  public String getSshPublicKey()
  {
    return sshPublicKey;
  }

  /**
   * @param sshPublicKey the sshPublicKey to set
   * @return this for chaining.
   */
  public PFUserDO setSshPublicKey(final String sshPublicKey)
  {
    this.sshPublicKey = sshPublicKey;
    return this;
  }

  /**
   * @return true only and only if the user isn't either deleted nor deactivated, otherwise false.
   */
  @Transient
  public boolean hasSystemAccess()
  {
    return isDeleted() == false && isDeactivated() == false;
  }

  @Transient
  public String getDisplayUsername()
  {
    return getShortDisplayName();
  }
}
