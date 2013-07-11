/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A simple component with an image link and a variety of constructors for every purpose.
 * @author Sweeps <pf@byte-storm.com>
 */
public class ImageLinkPanel extends Panel
{
  private static final long serialVersionUID = -6814329972260829872L;

  private final Link<Void> link;

  private Image image;

  private Label linkLabel;

  private ImageLinkPanel(final String id)
  {
    super(id);
    link = getNewLink();
    linkLabel = new Label("linkText");
    link.add(linkLabel);
    add(link);
  }

  private <C extends Page> ImageLinkPanel(final String id, final Class<C> pageClass)
  {
    super(id);
    link = getNewBookmarkableLink(pageClass);
    linkLabel = new Label("linkText");
    link.add(linkLabel);
    add(link);
  }

  private <C extends Page> Link<Void> getNewBookmarkableLink(final Class<C> pageClass)
  {
    return new BookmarkablePageLink<Void>("link", pageClass);
  }

  protected Link<Void> getNewLink()
  {
    return new Link<Void>("link") {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClick()
      {
        ImageLinkPanel.this.onClick();
      };
    };
  }

  /**
   * Constructor for displaying an image as link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   */
  public ImageLinkPanel(final String id, final ResourceReference imageResource) {
    this(id);
    image = createImage(imageResource);
    link.add(image);
  }

  public Image getImage()
  {
    return image;
  }

  /**
   * Constructor for displaying an image plus link text as link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param linkText A text string that is displayed next to the image
   */
  public ImageLinkPanel(final String id, final ResourceReference imageResource, final String linkText) {
    this(id, imageResource);
    setLinkText(linkText);
  }

  /**
   * Constructor for displaying an image as bookmarkable link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param pageClass Page class of the link target
   */
  public <C extends Page> ImageLinkPanel(final String id, final ResourceReference imageResource, final Class<C> pageClass) {
    this(id, pageClass);
    image = createImage(imageResource);
    link.add(image);
  }

  /**
   * Constructor for displaying an image plus link text as bookmarkable link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param pageClass Page class of the link target
   * @param linkText A text string that is displayed next to the image
   */
  public <C extends Page> ImageLinkPanel(final String id, final ResourceReference imageResource, final Class<C> pageClass, final String linkText) {
    this(id, imageResource, pageClass);
    setLinkText(linkText);
  }

  /**
   * Constructor for displaying an image of the given dimensions as link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param width Image width (-1 = ignore)
   * @param height Image height (-1 = ignore)
   */
  public ImageLinkPanel(final String id, final ResourceReference imageResource, final int width, final int height) {
    this(id, imageResource);
    setDimensions(width, height);
  }

  /**
   * Constructor for displaying an image of the given dimensions and link text as link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param width Image width (-1 = ignore)
   * @param height Image height (-1 = ignore)
   * @param linkText A text string that is displayed next to the image
   */
  public ImageLinkPanel(final String id, final ResourceReference imageResource, final int width, final int height, final String linkText) {
    this(id, imageResource, width, height);
    setLinkText(linkText);
  }

  /**
   * Constructor for displaying an image of the given dimensions as bookmarkable link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param pageClass Page class of the link target
   * @param width Image width (-1 = ignore)
   * @param height Image height (-1 = ignore)
   */
  public <C extends Page> ImageLinkPanel(final String id, final ResourceReference imageResource, final Class<C> pageClass, final int width, final int height) {
    this(id, imageResource, pageClass);
    setDimensions(width, height);
  }

  /**
   * Constructor for displaying an image of the given dimensions plus link text as bookmarkable link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param pageClass Page class of the link target
   * @param width Image width (-1 = ignore)
   * @param height Image height (-1 = ignore)
   * @param linkText A text string that is displayed next to the image
   */
  public <C extends Page> ImageLinkPanel(final String id, final ResourceReference imageResource, final Class<C> pageClass, final int width, final int height, final String linkText) {
    this(id, imageResource, pageClass, width, height);
    setLinkText(linkText);
  }

  /**
   * Constructor for displaying an image with title and alternative text as link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param title Image title, e.g. for displaying on mouse-over (may be null)
   * @param altText Alternative text for displaying if the image cannot be displayed (may be null)
   */
  public ImageLinkPanel(final String id, final ResourceReference imageResource, final String title, final String altText) {
    this(id, imageResource);
    setTitle(title);
    setAltText(altText);
  }

  /**
   * Constructor for displaying an image with title, alternative text and link text as link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param title Image title, e.g. for displaying on mouse-over (may be null)
   * @param altText Alternative text for displaying if the image cannot be displayed (may be null)
   * @param linkText A text string that is displayed next to the image
   */
  public ImageLinkPanel(final String id, final ResourceReference imageResource, final String title, final String altText, final String linkText) {
    this(id, imageResource, title, altText);
    setLinkText(linkText);
  }

  /**
   * Constructor for displaying an image with title and alternative text as bookmarkable link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param pageClass Page class of the link target
   * @param title Image title, e.g. for displaying on mouse-over (may be null)
   * @param altText Alternative text for displaying if the image cannot be displayed (may be null)
   */
  public <C extends Page> ImageLinkPanel(final String id, final ResourceReference imageResource, final Class<C> pageClass, final String title, final String altText) {
    this(id, imageResource, pageClass);
    setTitle(title);
    setAltText(altText);
  }

  /**
   * Constructor for displaying an image with title, alternative text and link text as bookmarkable link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param pageClass Page class of the link target
   * @param title Image title, e.g. for displaying on mouse-over (may be null)
   * @param altText Alternative text for displaying if the image cannot be displayed (may be null)
   * @param linkText A text string that is displayed next to the image
   */
  public <C extends Page> ImageLinkPanel(final String id, final ResourceReference imageResource, final Class<C> pageClass, final String title, final String altText, final String linkText) {
    this(id, imageResource, pageClass, title, altText);
    setLinkText(linkText);
  }

  /**
   * Constructor for displaying an image of the given dimensions, title and alternative text as link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param width Image width (-1 = ignore)
   * @param height Image height (-1 = ignore)
   * @param title Image title, e.g. for displaying on mouse-over (may be null)
   * @param altText Alternative text for displaying if the image cannot be displayed (may be null)
   */
  public ImageLinkPanel(final String id, final ResourceReference imageResource, final int width, final int height, final String title, final String altText) {
    this(id, imageResource);
    setDimensions(width, height);
    setTitle(title);
    setAltText(altText);
  }

  /**
   * Constructor for displaying an image of the given dimensions, title, alternative text and link text as link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param width Image width (-1 = ignore)
   * @param height Image height (-1 = ignore)
   * @param title Image title, e.g. for displaying on mouse-over (may be null)
   * @param altText Alternative text for displaying if the image cannot be displayed (may be null)
   * @param linkText A text string that is displayed next to the image
   */
  public ImageLinkPanel(final String id, final ResourceReference imageResource, final int width, final int height, final String title, final String altText, final String linkText) {
    this(id, imageResource, width, height, title, altText);
    setLinkText(linkText);
  }

  /**
   * Constructor for displaying an image of the given dimensions, title and alternative text as bookmarkable link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param pageClass Page class of the link target
   * @param width Image width (-1 = ignore)
   * @param height Image height (-1 = ignore)
   * @param title Image title, e.g. for displaying on mouse-over (may be null)
   * @param altText Alternative text for displaying if the image cannot be displayed (may be null)
   */
  public <C extends Page> ImageLinkPanel(final String id, final ResourceReference imageResource, final Class<C> pageClass, final int width, final int height, final String title, final String altText) {
    this(id, imageResource, pageClass);
    setDimensions(width, height);
    setTitle(title);
    setAltText(altText);
  }

  /**
   * Constructor for displaying an image of the given dimensions, title, alternative text and link text as bookmarkable link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   * @param pageClass Page class of the link target
   * @param width Image width (-1 = ignore)
   * @param height Image height (-1 = ignore)
   * @param title Image title, e.g. for displaying on mouse-over (may be null)
   * @param altText Alternative text for displaying if the image cannot be displayed (may be null)
   * @param linkText A text string that is displayed next to the image
   */
  public <C extends Page> ImageLinkPanel(final String id, final ResourceReference imageResource, final Class<C> pageClass, final int width, final int height, final String title, final String altText, final String linkText) {
    this(id, imageResource, pageClass, width, height, title, altText);
    setLinkText(linkText);
  }

  private Image createImage(final ResourceReference imageResource)
  {
    return new Image("image", imageResource) {
      /***/
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean shouldAddAntiCacheParameter()
      {
        return false;
      }
    };
  }

  private void setAltText(final String altText)
  {
    if (altText != null)
      image.add(AttributeModifier.replace("alt", altText));
  }

  private void setTitle(final String title)
  {
    if (title != null)
      image.add(AttributeModifier.replace("title", title));
  }

  private void setDimensions(final int width, final int height)
  {
    if (width >= 0)
      image.add(AttributeModifier.replace("width", width));
    if (height >= 0)
      image.add(AttributeModifier.replace("height", height));
  }

  private void setLinkText(final String linkText)
  {
    linkLabel.setDefaultModel(new Model<String>(linkText));
  }

  public void onClick()
  {
  }

}
