package com.github.dtsarkov.ea.tools.codegenerator;

public class ExecutionState {
	private boolean processBranch 	  = true;
	private boolean isBranchProcessed  = false;
	
	public boolean canProcessBranch() {
		return processBranch;
	}
	public void setProcessBranch(boolean processBranch) {
		this.processBranch = processBranch;
	}
	
	public boolean isBranchProcessed() {
		return isBranchProcessed;
	}
	public void setBranchProcessed(boolean isBranchProcessed) {
		this.isBranchProcessed 	= isBranchProcessed;
		this.processBranch 		= !isBranchProcessed;
	}
	@Override
	public String toString() {
		return String.format(
				"isBranchProcessed = %s, processBranch = %s"
				,isBranchProcessed
				,processBranch
		);
	}

	
}
