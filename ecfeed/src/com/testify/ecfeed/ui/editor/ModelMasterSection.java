/*******************************************************************************
 * Copyright (c) 2013 Testify AS.                                                   
 * All rights reserved. This program and the accompanying materials                 
 * are made available under the terms of the Eclipse Public License v1.0            
 * which accompanies this distribution, and is available at                         
 * http://www.eclipse.org/legal/epl-v10.html                                        
 *                                                                                  
 * Contributors:                                                                    
 *     Patryk Chamuczynski (p.chamuczynski(at)radytek.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.testify.ecfeed.model.IGenericNode;
import com.testify.ecfeed.model.IModelWrapper;
import com.testify.ecfeed.model.RootNode;

public class ModelMasterSection extends TreeViewerSection{
	private static final int STYLE = Section.EXPANDED | Section.TITLE_BAR;
	private static final int AUTO_EXPAND_LEVEL = 3;

	private List<IModelSelectionListener> fModelSelectionListeners;
	private Button fMoveUpButton;
	private Button fMoveDownButton;
	private RootNode fModel;
	
	private static class DummyUpdateListener implements IModelUpdateListener{
		@Override
		public void modelUpdated(AbstractFormPart source) {
		}
	}

	private class MoveUpAdapter extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e){
			moveSelectedItem(true);
		}
	}

	private class MoveDownAdapter extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e){
			moveSelectedItem(false);
		}
	}
	
	private class ModelSelectionListener implements ISelectionChangedListener{
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection)event.getSelection();
			IGenericNode selectedNode = (IGenericNode)selection.getFirstElement();
			enableSortButtons(selectedNode);
			notifyModelSelectionListeners(selection);
		}

		private void enableSortButtons(IGenericNode selectedElement) {
			boolean enabled = true;
			if (selectedElement instanceof RootNode) {
				enabled = false;
			}
			fMoveUpButton.setEnabled(enabled);
			fMoveDownButton.setEnabled(enabled);
		}
	}

	public ModelMasterSection(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, STYLE, new DummyUpdateListener());
		fModelSelectionListeners = new ArrayList<IModelSelectionListener>();
	}
	
	public void addModelSelectionChangedListener(IModelSelectionListener listener){
		fModelSelectionListeners.add(listener);
	}
	
	@Override
	protected void createContent(){
		super.createContent();
		getSection().setText("Structure");
		fMoveUpButton = addButton("Move Up", new MoveUpAdapter());
		fMoveDownButton = addButton("Move Down", new MoveDownAdapter());
		getTreeViewer().setAutoExpandLevel(AUTO_EXPAND_LEVEL);
		addSelectionChangedListener(new ModelSelectionListener());
	}

	private void notifyModelSelectionListeners(ISelection newSelection) {
		for(IModelSelectionListener listener : fModelSelectionListeners){
			listener.modelSelectionChanged(newSelection);
		}
	}

	private void moveSelectedItem(boolean moveUp) {
		if(selectedNode() != null && selectedNode().getParent() != null){
			selectedNode().getParent().moveChild(selectedNode(), moveUp);
			markDirty();
			refresh();
		}
	}

	private IGenericNode selectedNode() {
		Object selectedElement = getSelectedElement();
		if(selectedElement instanceof IGenericNode){
			return (IGenericNode)selectedElement;
		}
		return null;
	}
	
	public void setInput(IModelWrapper wrapper){
		super.setInput(wrapper);
	}
	
	public void setModel(RootNode model){
		fModel = model;
		setInput(new IModelWrapper() {
			@Override
			public RootNode getModel() {
				return fModel;
			}
		});
	}
	
	public RootNode getModel(){
		return fModel;
	}

	@Override
	protected IContentProvider viewerContentProvider() {
		return new ModelContentProvider();
	}
	
	@Override 
	protected IBaseLabelProvider viewerLabelProvider(){
		return new ModelLabelProvider();
	}

}
