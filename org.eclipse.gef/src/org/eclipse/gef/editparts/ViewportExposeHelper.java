package org.eclipse.gef.editparts;
/*
 * Licensed Material - Property of IBM
 * (C) Copyright IBM Corp. 2001, 2002 - All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 */

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;

import org.eclipse.gef.*;
import org.eclipse.gef.EditPart;

/**
 * An implementation of <code>ExposeHelper</code> for use with {@link
 * org.eclipse.draw2d.Viewport} figures.
 * @author hudsonr
 * @since 2.0 */
public class ViewportExposeHelper
	implements ExposeHelper
{

private GraphicalEditPart owner;
private int minimumFrameCount = 3;
private int maximumFrameCount = 8;

/**
 * Constructs a new ViewportExposeHelper on the specified GraphicalEditPart. The
 * GraphicalEditPart must have a <code>Viewport</code> somewhere between its
 * <i>contentsPane</i> and its <i>figure</i> inclusively.
 * @param owner the GraphicalEditPart that owns the Viewport */
public ViewportExposeHelper(GraphicalEditPart owner) {
	this.owner = owner;
}

/**
 * Exposes the descendant EditPart by smoothly scrolling the <code>Viewport</code>. The
 * smoothness is determined by the minimum and maximum frame count, and the overall
 * amount being scrolled.
 * @see org.eclipse.gef.ExposeHelper#exposeDescendant(EditPart) */
public void exposeDescendant(EditPart part) {
	Viewport port = findViewport(owner);
	if (port == null)
		return;
	IFigure target = ((GraphicalEditPart)part).getFigure();

/* All calculations are done relative to the contents of the viewport. The expose margin
 * is added in absolute coordinates, and then taken back to relative viewport coordinates.
 */
	Rectangle exposeRegion = target.getBounds().getCopy();
	target.translateToAbsolute(exposeRegion);
//	exposeRegion.expand(5,5);
	port.getContents().translateToRelative(exposeRegion);

	Point offset = port.getContents().getBounds().getLocation();
//By substracting the offset, the region is now the difference from the contents origin.
	exposeRegion.translate(offset.negate());
	exposeRegion.translate(
		port.getHorizontalRangeModel().getMinimum(),
		port.getVerticalRangeModel().getMinimum());

	Dimension viewportSize = port.getClientArea().getSize();
	Point topLeft = exposeRegion.getTopLeft();
	Point bottomRight = exposeRegion.
		getBottomRight().
		translate(viewportSize.getNegated());

	Point finalLocation = Point.min(topLeft,
		Point.max(bottomRight, port.getViewLocation()));
	Point startLocation = port.getViewLocation();

	int dx = finalLocation.x - startLocation.x;
	int dy = finalLocation.y - startLocation.y;

	int FRAMES = (Math.abs(dx) + Math.abs(dy)) / 15;
	FRAMES = Math.max(FRAMES, getMinimumFrameCount());
	FRAMES = Math.min(FRAMES, getMaximumFrameCount());

	int stepX = Math.min((dx / FRAMES), (viewportSize.width / 3));
	int stepY = Math.min((dy / FRAMES), (viewportSize.height / 3));

	for (int i = 1; i < FRAMES; i++) {
		port.setViewLocation(startLocation.x + stepX * i, startLocation.y + stepY * i);
		port.getUpdateManager().performUpdate();
	}
	port.setViewLocation(finalLocation);
}

private Viewport findViewport(GraphicalEditPart part) {
	IFigure figure = null;
	Viewport port = null;
	do {
		if (figure == null)
			figure = part.getContentPane();
		else
			figure = figure.getParent();
		if (figure instanceof Viewport) {
			port = (Viewport) figure;
			break;
		}
	} while (figure != part.getFigure() && figure != null);
	return port;
}

/**
 * Returns the maximumFrameCount.
 * @return int
 */
public int getMaximumFrameCount() {
	return maximumFrameCount;
}

/**
 * Returns the minimumFrameCount.
 * @return int
 */
public int getMinimumFrameCount() {
	return minimumFrameCount;
}

/**
 * Sets the maximumFrameCount.
 * @param maximumFrameCount The maximumFrameCount to set
 */
public void setMaximumFrameCount(int maximumFrameCount) {
	this.maximumFrameCount = maximumFrameCount;
}

/**
 * Sets the minimumFrameCount.
 * @param minimumFrameCount The minimumFrameCount to set
 */
public void setMinimumFrameCount(int minimumFrameCount) {
	this.minimumFrameCount = minimumFrameCount;
	if (getMaximumFrameCount() < minimumFrameCount)
		setMaximumFrameCount(minimumFrameCount);
}

}
