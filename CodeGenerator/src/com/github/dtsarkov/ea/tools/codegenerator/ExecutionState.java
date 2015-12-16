package com.github.dtsarkov.ea.tools.codegenerator;

public class ExecutionState {
    
    private boolean isBranchProcessed;;
    private boolean processBranch;
    
    public ExecutionState() {
        resetState();
    }
    public ExecutionState(ExecutionState state) {
    	isBranchProcessed 	= state.isBranchProcessed;
    	processBranch		= state.processBranch; 
    }
    
    public void setProcessBranch(boolean processBranch) {
            this.processBranch = processBranch;
    }
    
    public boolean canProcessBranch() {
        return processBranch && !isBranchProcessed;
    }
    
    public void setBranchProcessed() {
        isBranchProcessed   = true;
        processBranch       = false;
    }
    
    public boolean isBranchProcessed() {
    	return isBranchProcessed;
    }
    
    public void resetState() {
        isBranchProcessed   = false;
        processBranch       = true;
    }

    @Override 
    public String toString() {
        return String.format(
            "canProcessBranch = %s, isBranchProcessed = %s"
            , canProcessBranch()
            , isBranchProcessed
        );    
    }
}