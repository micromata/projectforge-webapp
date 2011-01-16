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

package org.projectforge.web.wicket.layout;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public interface IField
{
  /**
   * Sets the alignment of this field (if supported).
   * @return this for chaining.
   */
  public IField setAlignment(final LayoutAlignment aligment);

  /**
   * Sets the field as bold (if supported).
   * @return this for chaining.
   */
  public IField setStrong();

  /**
   * Sets the attribute style (if supported).
   * @return this for chaining.
   */
  public IField setCssStyle(final String cssStyle);

  /**
   * Sets the focus on this field (if supported).
   * @return this for chaining.
   */
  public IField setFocus();

  /**
   * Sets the field as required (if supported).
   * @return this for chaining.
   */
  public IField setRequired();

  /**
   * Sets a tool-tip for this field (if supported).
   * @return this for chaining.
   */
  public IField setTooltip(final String text);
}
