/*******************************************************************************
 * Copyright (c) 2015 Testify AS..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)radytek.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.ui.modelif;

import java.util.List;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.ui.forms.AbstractFormPart;

import com.testify.ecfeed.adapter.ModelOperationManager;

public interface IModelUpdateContext {
	public ModelOperationManager getOperationManager();
	public AbstractFormPart getSourceForm();
	public List<IModelUpdateListener> getUpdateListeners();
	public IUndoContext getUndoContext();
}
