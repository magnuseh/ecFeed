/*******************************************************************************
 * Copyright (c) 2013 Testify AS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)gmail.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.plugin.editor.modeleditor;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.Section;

import com.testify.ecfeed.model.ExpectedValueCategoryNode;

public class ExpectedValueDetailsPage extends GenericNodeDetailsPage {

	private Section fMainSection;
	private Composite fMainComposite;
	private ExpectedValueCategoryNode fSelectedCategory;
	private Composite fDefaultValueComposite;
	private Text fDefaultValueText;

	public ExpectedValueDetailsPage(ModelMasterDetailsBlock parentBlock) {
		super(parentBlock);
	}

	public void createContents(Composite parent) {
		parent.setLayout(new FillLayout());
		
		fMainSection = getToolkit().createSection(parent, Section.TITLE_BAR);
		getToolkit().paintBordersFor(fMainSection);

		fMainComposite = new Composite(fMainSection, SWT.NONE);
		fToolkit.adapt(fMainComposite);
		fToolkit.paintBordersFor(fMainComposite);
		fMainSection.setClient(fMainComposite);
		fMainComposite.setLayout(new GridLayout(1, false));
		
		fDefaultValueComposite = new Composite(fMainComposite, SWT.NONE);
		fDefaultValueComposite.setLayout(new GridLayout(3, false));
		fDefaultValueComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label label = new Label(fDefaultValueComposite, SWT.NONE);
		label.setText("Defaut value");
		
		fDefaultValueText = fToolkit.createText(fDefaultValueComposite, null, SWT.RIGHT);
		fToolkit.paintBordersFor(fDefaultValueComposite);
		fDefaultValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fDefaultValueText.addListener(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR){
					changeDefaultExpectedValue();
				}
			}
		});

		Button changeButton = createButton(fDefaultValueComposite, "Change", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				changeDefaultExpectedValue();
			}
		});
		changeButton.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				changeDefaultExpectedValue();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				fDefaultValueText.selectAll();
			}
		});
	}

	private void changeDefaultExpectedValue() {
		String valueString = fDefaultValueText.getText();
		if(fSelectedCategory.validatePartitionStringValue(valueString)){
			fSelectedCategory.setDefaultValue(fSelectedCategory.getPartitionValueFromString(valueString));
		}
		updateModel(fSelectedCategory);
	}

	public void selectionChanged(IFormPart part, ISelection selection) {
		super.selectionChanged(part, selection);
		fSelectedCategory = (ExpectedValueCategoryNode)fSelectedNode;
		refresh();
	}
	
	public void refresh(){
		fMainSection.setText(fSelectedCategory.toString());
		fDefaultValueText.setText(fSelectedCategory.getDefaultValue().toString());
	}
}
