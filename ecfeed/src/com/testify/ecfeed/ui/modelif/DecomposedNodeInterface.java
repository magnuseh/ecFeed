package com.testify.ecfeed.ui.modelif;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.testify.ecfeed.adapter.IModelOperation;
import com.testify.ecfeed.adapter.operations.GenericOperationAddPartition;
import com.testify.ecfeed.adapter.operations.GenericOperationRemovePartition;
import com.testify.ecfeed.model.ChoiceNode;
import com.testify.ecfeed.model.DecomposedNode;
import com.testify.ecfeed.ui.common.Constants;
import com.testify.ecfeed.ui.common.EclipseModelBuilder;
import com.testify.ecfeed.ui.common.EclipseTypeAdapterProvider;
import com.testify.ecfeed.ui.common.Messages;

public class DecomposedNodeInterface extends GenericNodeInterface {

	private DecomposedNode fTarget;

	public DecomposedNodeInterface(IModelUpdateContext updateContext){
		super(updateContext);
	}

	public void setTarget(DecomposedNode target){
		super.setTarget(target);
		fTarget = target;
	}

	public ChoiceNode addNewPartition() {
		String name = generatePartitionName();
		String value = generateNewPartitionValue();
		ChoiceNode newPartition = new ChoiceNode(name, value);
		if(addPartition(newPartition)){
			return newPartition;
		}
		return null;
	}

	public boolean addPartition(ChoiceNode newPartition) {
		IModelOperation operation = new GenericOperationAddPartition(fTarget, newPartition, new EclipseTypeAdapterProvider(), fTarget.getPartitions().size(), true);
		return execute(operation, Messages.DIALOG_ADD_CHOICE_PROBLEM_TITLE);
	}

	public boolean removePartition(ChoiceNode partition) {
		IModelOperation operation = new GenericOperationRemovePartition(fTarget, partition, true);
		return execute(operation, Messages.DIALOG_REMOVE_CHOICE_TITLE);
	}

	public boolean removePartitions(Collection<ChoiceNode> partitions) {
		boolean displayWarning = false;
		for(ChoiceNode p : partitions){
			if(fTarget.getParameter().getMethod().mentioningConstraints(p).size() > 0 || fTarget.getParameter().getMethod().mentioningTestCases(p).size() > 0){
				displayWarning = true;
			}
		}
		if(displayWarning){
			if(MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
					Messages.DIALOG_REMOVE_CHOICE_WARNING_TITLE,
					Messages.DIALOG_REMOVE_CHOICE_WARNING_MESSAGE) == false){
				return false;
			}
		}
		return removeChildren(partitions, Messages.DIALOG_REMOVE_CHOICES_PROBLEM_TITLE);
	}

	protected String generateNewPartitionValue() {
		EclipseModelBuilder builder = new EclipseModelBuilder();
		String type = fTarget.getParameter().getType();
		String value = builder.getDefaultExpectedValue(type);
		if(isPrimitive() == false && builder.getSpecialValues(type).size() == 0){
			int i = 0;
			while(fTarget.getLeafPartitionValues().contains(value)){
				value = builder.getDefaultExpectedValue(type) + i++;
			}
		}
		return value;
	}

	public boolean isPrimitive() {
		return ParameterInterface.isPrimitive(fTarget.getParameter().getType());
	}

	public boolean isUserType() {
		return !isPrimitive();
	}

	public List<String> getSpecialValues() {
		return new EclipseModelBuilder().getSpecialValues(fTarget.getParameter().getType());
	}

	public boolean hasLimitedValuesSet() {
		return !isPrimitive() || isBoolean();
	}

	public  boolean isBoolean() {
		return ParameterInterface.isBoolean(fTarget.getParameter().getType());
	}

	protected String generatePartitionName(){
		String name = Constants.DEFAULT_NEW_PARTITION_NAME;
		int i = 0;
		while(fTarget.getPartitionNames().contains(name)){
			name = Constants.DEFAULT_NEW_PARTITION_NAME + i++;
		}
		return name;
	}

}