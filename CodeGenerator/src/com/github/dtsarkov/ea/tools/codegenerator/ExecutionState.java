package com.github.dtsarkov.ea.tools.codegenerator;

public class ExecutionState {
    
    private boolean isBranchProcessed;;
    private boolean processBranch;
    
    public ExecutionState() {
        resetState();
    }
    
    public void setProcessBranch(boolean processBranch) {
            this.processBranch = processBranch;
    }
    
    public boolean canProcessBranch() {
        return processBranch;
    }
    
    public void setBranchProcessed(boolean newState ) {
        isBranchProcessed   = newState;
        processBranch       = !newState;
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