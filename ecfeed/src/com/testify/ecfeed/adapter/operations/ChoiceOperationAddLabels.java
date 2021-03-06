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

package com.testify.ecfeed.adapter.operations;

import java.util.Collection;

import com.testify.ecfeed.model.ChoiceNode;

public class ChoiceOperationAddLabels extends BulkOperation {
	public ChoiceOperationAddLabels(ChoiceNode target, Collection<String> labels) {
		super(OperationNames.ADD_PARTITION_LABELS, false);
		for(String label : labels){
			if(target.getInheritedLabels().contains(label) == false){
				addOperation(new ChoiceOperationAddLabel(target, label));
			}
		}
	}
}
