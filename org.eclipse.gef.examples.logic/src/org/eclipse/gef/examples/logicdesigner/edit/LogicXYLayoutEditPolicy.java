
/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.gef.examples.logicdesigner.edit;

import java.util.Iterator;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.rulers.RulerProvider;

import org.eclipse.gef.examples.logicdesigner.LogicMessages;
import org.eclipse.gef.examples.logicdesigner.model.*;
import org.eclipse.gef.examples.logicdesigner.model.commands.*;

public class LogicXYLayoutEditPolicy
	extends org.eclipse.gef.editpolicies.XYLayoutEditPolicy
{

protected Command createAddCommand(EditPart childEditPart, Object constraint) {
	LogicSubpart part = (LogicSubpart)childEditPart.getModel();
	Rectangle rect = (Rectangle)constraint;

	AddCommand add = new AddCommand();
	add.setParent((LogicDiagram)getHost().getModel());
	add.setChild(part);
	add.setLabel(LogicMessages.LogicXYLayoutEditPolicy_AddCommandLabelText);
	add.setDebugLabel("LogicXYEP add subpart");//$NON-NLS-1$

	SetConstraintCommand setConstraint = new SetConstraintCommand();
	setConstraint.setLocation(rect);
	setConstraint.setPart(part);
	setConstraint.setLabel(LogicMessages.LogicXYLayoutEditPolicy_AddCommandLabelText);
	setConstraint.setDebugLabel("LogicXYEP setConstraint");//$NON-NLS-1$

	return add.chain(setConstraint);
}

/**
 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart, java.lang.Object)
 */
protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
	return null;
}

protected Command createChangeConstraintCommand(ChangeBoundsRequest request, 
                                                EditPart child, Object constraint) {
	SetConstraintCommand cmd = new SetConstraintCommand();
	LogicSubpart part = (LogicSubpart)child.getModel();
	cmd.setPart(part);
	cmd.setLocation((Rectangle)constraint);
	Command result = cmd;

	if ((request.getResizeDirection() & PositionConstants.NORTH_SOUTH) != 0) {
		Integer guidePos = (Integer)request.getExtendedData()
				.get(SnapToGuides.PROPERTY_HORIZONTAL_GUIDE);
		if (guidePos != null) {
			int hAlignment = ((Integer)request.getExtendedData()
					.get(SnapToGuides.PROPERTY_HORIZONTAL_ANCHOR)).intValue();
			ChangeGuideCommand cgm = new ChangeGuideCommand(part, true);
			cgm.setNewGuide(findGuideAt(guidePos.intValue(), true), hAlignment);
			result = result.chain(cgm);
		} else if (part.getHorizontalGuide() != null) {
			// SnapToGuides didn't provide a horizontal guide, but this part is attached
			// to a horizontal guide.  Now we check to see if the part is attached to
			// the guide along the edge being resized.  If that is the case, we need to
			// detach the part from the guide; otherwise, we leave it alone.
			int alignment = part.getHorizontalGuide().getAlignment(part);
			int edgeBeingResized = 0;
			if ((request.getResizeDirection() & PositionConstants.NORTH) != 0)
				edgeBeingResized = -1;
			else
				edgeBeingResized = 1;
			if (alignment == edgeBeingResized) {
				ChangeGuideCommand cgm = new ChangeGuideCommand(part, true);
				result = result.chain(cgm);
			}
		}
	}
	
	if ((request.getResizeDirection() & PositionConstants.EAST_WEST) != 0) {
		Integer guidePos = (Integer)request.getExtendedData()
				.get(SnapToGuides.PROPERTY_VERTICAL_GUIDE);
		if (guidePos != null) {
			int vAlignment = ((Integer)request.getExtendedData()
					.get(SnapToGuides.PROPERTY_VERTICAL_ANCHOR)).intValue();
			ChangeGuideCommand cgm = new ChangeGuideCommand(part, false);
			cgm.setNewGuide(findGuideAt(guidePos.intValue(), false), vAlignment);
			result = result.chain(cgm);
		} else if (part.getVerticalGuide() != null) {
			int alignment = part.getVerticalGuide().getAlignment(part);
			int edgeBeingResized = 0;
			if ((request.getResizeDirection() & PositionConstants.WEST) != 0)
				edgeBeingResized = -1;
			else
				edgeBeingResized = 1;
			if (alignment == edgeBeingResized) {
				ChangeGuideCommand cgm = new ChangeGuideCommand(part, false);
				result = result.chain(cgm);
			}
		}
	}
	
	if (request.getType().equals(REQ_MOVE_CHILDREN)
			|| request.getType().equals(REQ_ALIGN_CHILDREN)) {
		Integer guidePos = (Integer)request.getExtendedData()
				.get(SnapToGuides.PROPERTY_HORIZONTAL_GUIDE);
		ChangeGuideCommand cgm = new ChangeGuideCommand(part, true);
		if (guidePos != null) {
			int hAlignment = ((Integer)request.getExtendedData()
					.get(SnapToGuides.PROPERTY_HORIZONTAL_ANCHOR)).intValue();
			cgm.setNewGuide(findGuideAt(guidePos.intValue(), true), hAlignment);
		}
		result = result.chain(cgm);
		
		guidePos = (Integer)request.getExtendedData()
				.get(SnapToGuides.PROPERTY_VERTICAL_GUIDE);
		cgm = new ChangeGuideCommand(part, false);
		if (guidePos != null) {
			int vAlignment = ((Integer)request.getExtendedData()
					.get(SnapToGuides.PROPERTY_VERTICAL_ANCHOR)).intValue();
			cgm.setNewGuide(findGuideAt(guidePos.intValue(), false), vAlignment);
		}
		result = result.chain(cgm);
	}

	return result;
}

protected EditPolicy createChildEditPolicy(EditPart child) {
	if (child instanceof LEDEditPart ||
	    child instanceof OutputEditPart ) {
		//|| child instanceof LogicLabelEditPart) {
		return new NonResizableEditPolicy();
	} else if (child instanceof LogicLabelEditPart) {
		ResizableEditPolicy policy = new ResizableEditPolicy();
		policy.setResizeDirections(PositionConstants.EAST | PositionConstants.WEST);
		return policy;
	}
	
	//return new LogicResizableEditPolicy();
	return super.createChildEditPolicy(child);
}

protected LogicGuide findGuideAt(int pos, boolean horizontal) {
	RulerProvider provider = ((RulerProvider)getHost().getViewer().getProperty(
			horizontal ? RulerProvider.PROPERTY_VERTICAL_RULER 
			: RulerProvider.PROPERTY_HORIZONTAL_RULER));
	return (LogicGuide)provider.getGuideAt(pos);
}

/**
 * Override to return the <code>Command</code> to perform an {@link
 * RequestConstants#REQ_CLONE CLONE}. By default, <code>null</code> is
 * returned.
 * @param request the Clone Request
 * @return A command to perform the Clone.
 */
protected Command getCloneCommand(ChangeBoundsRequest request) {
	CloneCommand clone = new CloneCommand();
	
	clone.setParent((LogicDiagram)getHost().getModel());
	
	Iterator i = request.getEditParts().iterator();
	GraphicalEditPart currPart = null;
	
	while (i.hasNext()) {
		currPart = (GraphicalEditPart)i.next();
		clone.addPart((LogicSubpart)currPart.getModel(), (Rectangle)getConstraintForClone(currPart, request));
	}
	
	return clone;
}


protected Command getCreateCommand(CreateRequest request) {
	CreateCommand create = new CreateCommand();
	create.setParent((LogicDiagram)getHost().getModel());
	LogicSubpart newPart = (LogicSubpart)request.getNewObject();
	create.setChild(newPart);
	Rectangle constraint = (Rectangle)getConstraintFor(request);
	create.setLocation(constraint);
	create.setLabel(LogicMessages.LogicXYLayoutEditPolicy_CreateCommandLabelText);
	Command result = create;
	
	// Attach to horizontal guide, if one is given
	Integer guidePos = (Integer)request.getExtendedData()
			.get(SnapToGuides.PROPERTY_HORIZONTAL_GUIDE);
	if (guidePos != null) {
		int hAlignment = ((Integer)request.getExtendedData()
				.get(SnapToGuides.PROPERTY_HORIZONTAL_ANCHOR)).intValue();
		ChangeGuideCommand cgm = new ChangeGuideCommand(newPart, true);
		cgm.setNewGuide(findGuideAt(guidePos.intValue(), true), hAlignment);
		result = result.chain(cgm);
	}

	// Attach to vertical guide, if one is given
	guidePos = (Integer)request.getExtendedData()
			.get(SnapToGuides.PROPERTY_VERTICAL_GUIDE);
	if (guidePos != null) {
		int vAlignment = ((Integer)request.getExtendedData()
				.get(SnapToGuides.PROPERTY_VERTICAL_ANCHOR)).intValue();
		ChangeGuideCommand cgm = new ChangeGuideCommand(newPart, false);
		cgm.setNewGuide(findGuideAt(guidePos.intValue(), false), vAlignment);
		result = result.chain(cgm);
	}
	
	return result;
}

protected Command getDeleteDependantCommand(Request request) {
	return null;
}

protected Command getOrphanChildrenCommand(Request request) {
	return null;
}

}