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
import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormPart;

import com.testify.ecfeed.gal.Constants;
import com.testify.ecfeed.gal.GalException;
import com.testify.ecfeed.gal.ModelOperationManager;
import com.testify.ecfeed.gal.javax.partition.PartitionAbstractionLayer;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.ui.common.Messages;
import com.testify.ecfeed.ui.common.PartitionNodeAbstractLayer;
import com.testify.ecfeed.utils.ModelUtils;

public class PartitionDetailsPage extends BasicDetailsPage {
	
	private PartitionChildrenViewer fPartitionChildren;
	private PartitionLabelsViewer fLabelsViewer;
	private Text fPartitionNameText;
	private Combo fPartitionValueCombo;
	private StackLayout fComboLayout;
	private Combo fBooleanValueCombo;
	private PartitionAbstractionLayer fPartitionAL;

	private class PartitionNameTextListener extends ApplyChangesSelectionAdapter implements Listener{
		@Override
		public void handleEvent(Event event) {
			if(event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR){
				try {
					fPartitionAL.setName(fPartitionNameText.getText());
					modelUpdated(null);
				} catch (GalException e) {
					MessageDialog.openError(Display.getCurrent().getActiveShell(),
							Messages.DIALOG_PARTITION_NAME_PROBLEM_TITLE,
							e.getMessage());
				}
				fPartitionNameText.setText(getSelectedPartition().getName());
			}
		}
	}
	
	private class ApplyChangesSelectionAdapter extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e){
			boolean update = false;
			if(PartitionNodeAbstractLayer.changePartitionName(getSelectedPartition(), fPartitionNameText.getText())){
				update = true;
			}
			if(PartitionNodeAbstractLayer.changePartitionValue(getSelectedPartition(), fPartitionValueCombo.getText())){
				update = true;
			}
			if(update){
				modelUpdated(null);
			}
			fPartitionNameText.setText(getSelectedPartition().getName());
			fPartitionValueCombo.setText(getSelectedPartition().getValueString());
		}
	}
	
	private class booleanValueComboSelectionAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e){
			if(PartitionNodeAbstractLayer.changePartitionValue(getSelectedPartition(), fBooleanValueCombo.getText())){
				modelUpdated(null);
			}
			fBooleanValueCombo.setText(getSelectedPartition().getValueString());
		}
	}
	
	private class valueComboSelectionAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e){
			if(PartitionNodeAbstractLayer.changePartitionValue(getSelectedPartition(), fPartitionValueCombo.getText())){
				modelUpdated(null);
			}
			fPartitionValueCombo.setText(getSelectedPartition().getValueString());
		}
	}
	
	private class valueTextListener implements Listener{
		@Override
		public void handleEvent(Event event){
			if(event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR){
				if(PartitionNodeAbstractLayer.changePartitionValue(getSelectedPartition(), fPartitionValueCombo.getText())){
					modelUpdated(null);
				}
				fPartitionValueCombo.setText(getSelectedPartition().getValueString());
			}
		}
	}
	
	public PartitionDetailsPage(ModelMasterSection masterSection, ModelOperationManager operationManager) {
		super(masterSection);
		fPartitionAL = new PartitionAbstractionLayer(operationManager);
	}
	
	@Override
	public void createContents(Composite parent){
		super.createContents(parent);

		createNameValueEdit(getMainComposite());
		addForm(fPartitionChildren = new PartitionChildrenViewer(this, getToolkit()));
		addForm(fLabelsViewer = new PartitionLabelsViewer(this, getToolkit()));
		
		getToolkit().paintBordersFor(getMainComposite());
	}
	
	@Override
	public void refresh(){
		PartitionNode selectedPartition = getSelectedPartition();
		
		String title = getSelectedPartition().toString();
		boolean implemented = ModelUtils.isPartitionImplemented(selectedPartition);
		if(implemented){
			title += " [implemented]";
		}
		getMainSection().setText(title);
		fPartitionChildren.setInput(selectedPartition);
		fLabelsViewer.setInput(selectedPartition);
		fPartitionNameText.setText(selectedPartition.getName());
		if(selectedPartition.isAbstract()){
			fPartitionValueCombo.setEnabled(false);
			fBooleanValueCombo.setEnabled(false);
			fPartitionValueCombo.setText("");
			fBooleanValueCombo.setText("");
		} else{
			if(selectedPartition.getCategory().getType().equals(Constants.TYPE_NAME_BOOLEAN)){
				fPartitionValueCombo.setVisible(false);
				fBooleanValueCombo.setVisible(true);
				fBooleanValueCombo.setEnabled(true);
				prepareDefaultValues(selectedPartition, fBooleanValueCombo);
				fBooleanValueCombo.setText(selectedPartition.getValueString());
				fComboLayout.topControl = fBooleanValueCombo;
			} else{
				fBooleanValueCombo.setVisible(false);
				fPartitionValueCombo.setEnabled(true);
				fPartitionValueCombo.setVisible(true);
				prepareDefaultValues(selectedPartition, fPartitionValueCombo);
				fPartitionValueCombo.setText(selectedPartition.getValueString());
				fComboLayout.topControl = fPartitionValueCombo;
			}
		}	
	}
	
	@Override
	public void selectionChanged(IFormPart part, ISelection selection){
		super.selectionChanged(part, selection);
		fPartitionAL.setTarget(getSelectedPartition());
	}
	
	private PartitionNode getSelectedPartition(){
		if(getSelectedElement() != null && getSelectedElement() instanceof PartitionNode) {
			return (PartitionNode)getSelectedElement();
		}
		return null;
	}
	
	private void createNameValueEdit(Composite parent) {
		Composite composite = getToolkit().createComposite(parent);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		createNameEdit(composite);
		createValueEdit(composite);
	}

	private void createNameEdit(Composite parent) {
		getToolkit().createLabel(parent, "Name");
		fPartitionNameText = getToolkit().createText(parent, "", SWT.NONE);
		fPartitionNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fPartitionNameText.addListener(SWT.KeyDown, new PartitionNameTextListener());
		
		Composite buttonComposite = getToolkit().createComposite(parent);
		buttonComposite.setLayout(new GridLayout(1, false));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 2));
		Button applyButton = getToolkit().createButton(buttonComposite, "Change", SWT.CENTER);
		applyButton.addSelectionListener(new ApplyChangesSelectionAdapter());
		
		getToolkit().paintBordersFor(parent);

	}

	private void createValueEdit(Composite parent) {
		getToolkit().createLabel(parent, "Value");
		Composite valueComposite = getToolkit().createComposite(parent);
		fComboLayout = new StackLayout();
		valueComposite.setLayout(fComboLayout);
		valueComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		fPartitionValueCombo = new Combo(valueComposite,SWT.DROP_DOWN);
		fPartitionValueCombo.setLayoutData(new GridData(SWT.FILL,  SWT.CENTER, true, false));
		fPartitionValueCombo.addListener(SWT.KeyDown, new valueTextListener());
		fPartitionValueCombo.addSelectionListener(new valueComboSelectionAdapter());
		// boolean value combo
		fBooleanValueCombo = new Combo(valueComposite,SWT.READ_ONLY);
		fBooleanValueCombo.setLayoutData(new GridData(SWT.FILL,  SWT.CENTER, true, false));
		fBooleanValueCombo.addSelectionListener(new booleanValueComboSelectionAdapter());	
		
		getToolkit().paintBordersFor(parent);	
	}
	
	private void prepareDefaultValues(PartitionNode node, Combo valueText){
		HashMap<String, String> values = ModelUtils.generatePredefinedValues(node.getCategory().getType());
		String [] items = new String[values.values().size()];
		items = values.values().toArray(items);
		ArrayList<String> newItems = new ArrayList<String>();

		valueText.setItems(items);
		for (int i = 0; i < items.length; ++i) {
			newItems.add(items[i]);
			if (items[i].equals(node.getValueString())) {
				return;
			}
		}
		newItems.add(node.getValueString());
		valueText.setItems(newItems.toArray(items));
	}
	
}
