package com.testify.ecfeed.ui.editor;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.ExpectedCategoryNode;
import com.testify.ecfeed.model.IGenericNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.PartitionedCategoryNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.TestCaseNode;

public class MenuPasteOperation extends MenuOperation{
	protected IGenericNode fSource;
	protected IGenericNode fTarget;
	protected ModelMasterSection fModel;
	protected final String DIALOG_OPERATION_FAILED_TITLE = "Paste failed";
	protected final String DIALOG_OPERATION_FAILED_MESSAGE = "Clipboard content doesn't match here.";

	public MenuPasteOperation(String name, IGenericNode target, IGenericNode source, ModelMasterSection model){
		super(name);
		fSource = source;
		fTarget = target;
		fModel = model;
	}

	@Override
	public void operate(){
		if(createPastable(true) == null){
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), DIALOG_OPERATION_FAILED_TITLE,
					DIALOG_OPERATION_FAILED_MESSAGE);
		}
	}
	/*
	 * since we must do all the checks again while checking if operation is
	 * possible and then again while pasting I think it is worth sacrificing
	 * dozen of miliseconds per menu selection for the sake of code quality. the
	 * function
	 */
	public Object createPastable(boolean paste){
		Object pastable = null;

		if(fSource != null && fTarget != null){
			if(fTarget instanceof PartitionNode){
				PartitionNode target = (PartitionNode)fTarget;
				if(fSource instanceof PartitionNode){
					PartitionNode source = (PartitionNode)fSource;
					if(target.getCategory().getType().equals(source.getCategory().getType())){
						while(target.getPartition(source.getName()) != null){
							source.setName(source.getName() + "1");
						}
						pastable = source;
						if(paste){
							target.addPartition(source);
						}
					}
				}
			} else if(fTarget instanceof PartitionedCategoryNode){
				PartitionedCategoryNode target = (PartitionedCategoryNode)fTarget;
				if(fSource instanceof PartitionNode){
					PartitionNode source = (PartitionNode)fSource;
					if(target.getType().equals(source.getCategory().getType())){
						while(target.getPartitionNames().contains(source.getName())){
							source.setName(source.getName() + "1");
						}
						pastable = source;
						if(paste){
							target.addPartition(source);
						}
					}
				}
			} else if(fTarget instanceof MethodNode){
				MethodNode target = (MethodNode)fTarget;
				if(fSource instanceof PartitionedCategoryNode){
					PartitionedCategoryNode source = (PartitionedCategoryNode)fSource;
					while(target.getCategoriesNames().contains(source.getName())){
						source.setName(source.getName() + "1");
					}
					pastable = source;
					if(paste){
						target.addCategory(source);
					}
				} else if(fSource instanceof ExpectedCategoryNode){
					ExpectedCategoryNode source = (ExpectedCategoryNode)fSource;
					while(target.getCategoriesNames().contains(source.getName())){
						source.setName(source.getName() + "1");
					}
					pastable = source;
					if(paste){
						target.addCategory(source);
					}
				} else if(fSource instanceof ConstraintNode){
					ConstraintNode source = (ConstraintNode)fSource;
				    target.addConstraint(source);
				    if(!source.updateReferences()){
				    	target.removeConstraint(source);
				    	return null;
				    }
				    if(!paste){
				    	target.removeConstraint(source);
				    	return source;
				    }
					pastable = source;
				}
				else if(fSource instanceof TestCaseNode){
					TestCaseNode source = (TestCaseNode)fSource.getCopy();
					target.addTestCase(source);
					if(!source.updateReferences()){
						target.removeTestCase(source);
						return null;
					}
					if(!paste){
						target.removeTestCase(source);
						return source;
					}
					pastable = source;
				}

			} else if(fTarget instanceof ClassNode){
				ClassNode target = (ClassNode)fTarget;
				if(fSource instanceof MethodNode){
					MethodNode source = (MethodNode)fSource;
					while(target.getMethod(source.getName(), source.getCategoriesTypes()) != null){
						source.setName(source.getName() + "1");
					}
					pastable = source;
					if(paste){
						target.addMethod(source);
					}
				}
			} else if(fTarget instanceof RootNode){
				RootNode target = (RootNode)fTarget;
				if(fSource instanceof ClassNode){
					ClassNode source = (ClassNode)fSource;
					while(target.getClassModel(source.getName()) != null){
						source.setName(source.getName() + "1");
					}
					pastable = source;
					if(paste){
						target.addClass(source);
					}
				}
			}
		}
		if(paste){
			fModel.markDirty();
			fModel.refresh();
		}
		return pastable;
	}

	@Override
	public boolean isEnabled(){
		if(createPastable(false) != null)
			return true;
		return false;
	}
}
	
	// @Override
	// public boolean isEnabled(){
	// if(fSource != null && fTarget != null){
	// if(fTarget instanceof PartitionNode){
	// // partition
	// PartitionNode target = (PartitionNode)fTarget;
	// if(fSource instanceof PartitionNode){
	// PartitionNode source = (PartitionNode)fSource;
	// if(target.getCategory().getType().equals(source.getCategory().getType()))
	// return true;
	// }
	// }
	// else if(fTarget instanceof PartitionedCategoryNode){
	// PartitionedCategoryNode target = (PartitionedCategoryNode)fTarget;
	// if(fSource instanceof PartitionNode){
	// PartitionNode source = (PartitionNode)fSource;
	// if(target.getType().equals(source.getCategory().getType()))
	// return true;
	// }
	// }
	// else if(fTarget instanceof ExpectedCategoryNode){
	// return false;
	// }
	// else if(fTarget instanceof MethodNode){
	// if(fSource instanceof PartitionedCategoryNode){
	// return true;
	// }
	// else if(fSource instanceof ExpectedCategoryNode){
	// return true;
	// }
	// //add checking if adaptation is possible
	// else if(fSource instanceof ConstraintNode){
	// return true;
	// }
	// //add checking if adaptation is possible AND ask about TestSUITE node...
	// else if(fSource instanceof TestCaseNode){
	// return true;
	// }
	// }
	// else if(fTarget instanceof ClassNode){
	// if(fSource instanceof MethodNode){
	// return true;
	// }
	// }
	// else if(fTarget instanceof RootNode){
	// if(fSource instanceof ClassNode){
	// return true;
	// }
	// }
	// }
	// return false;
	// }
	
//	if(fSource != null && fTarget != null){
//		if(fTarget instanceof PartitionNode){
//			PartitionNode target = (PartitionNode)fTarget;
//			if(fSource instanceof PartitionNode){
//				PartitionNode source = (PartitionNode)fSource;
//				if(target.getCategory().getType().equals(source.getCategory().getType()))
//					while(target.getPartition(source.getName()) != null){
//						source.setName(source.getName() + "1");
//					}
//				target.addPartition(source);
//			}
//		} else if(fTarget instanceof PartitionedCategoryNode){
//			PartitionedCategoryNode target = (PartitionedCategoryNode)fTarget;
//			if(fSource instanceof PartitionNode){
//				PartitionNode source = (PartitionNode)fSource;
//				if(target.getType().equals(source.getCategory().getType()))
//					while(target.getPartitionNames().contains(source.getName())){
//						source.setName(source.getName() + "1");
//					}
//				target.addPartition(source);
//			}
//		} else if(fTarget instanceof MethodNode){
//			MethodNode target = (MethodNode)fTarget;
//			if(fSource instanceof PartitionedCategoryNode){
//				PartitionedCategoryNode source = (PartitionedCategoryNode)fSource;
//				while(target.getCategoriesNames().contains(source.getName())){
//					source.setName(source.getName() + "1");
//				}
//				target.addCategory(source);
//			} else if(fSource instanceof ExpectedCategoryNode){
//				ExpectedCategoryNode source = (ExpectedCategoryNode)fSource;
//				while(target.getCategoriesNames().contains(source.getName())){
//					source.setName(source.getName() + "1");
//				}
//				target.addCategory(source);
//			} else if(fSource instanceof ConstraintNode){
//
//			}
//			// add checking if adaptation is possible AND ask about
//			// TestSUITE node...
//			else if(fSource instanceof TestCaseNode){
//				target.addTestCase((TestCaseNode)fSource.getCopy());
//
//			}
//
//		} else if(fTarget instanceof ClassNode){
//			ClassNode target = (ClassNode)fTarget;
//			if(fSource instanceof MethodNode){
//				MethodNode source = (MethodNode)fSource;
//				while(target.getMethod(source.getName(), source.getCategoriesTypes()) != null){
//					source.setName(source.getName() + "1");
//				}
//				target.addMethod(source);
//			}
//		} else if(fTarget instanceof RootNode){
//			RootNode target = (RootNode)fTarget;
//			if(fSource instanceof ClassNode){
//				ClassNode source = (ClassNode)fSource;
//				while(target.getClassModel(source.getName()) != null){
//					source.setName(source.getName() + "1");
//				}
//				target.addClass(source);
//			}
//		}
//		fModel.markDirty();
//		fModel.refresh();
//	}