package com.puppycrawl.tools.checkstyle.checks.log;
import com.puppycrawl.tools.checkstyle.api.*;

public class MethodLimitCheck  extends AbstractCheck{
	private static final int DEFAULT_MAX = 30;
	private int max = DEFAULT_MAX;
	
	@Override
	public int [] getDefaultTokens() {
		return new int[] {TokenTypes.CLASS_DEF,TokenTypes.INTERFACE_DEF};
	}
	
	@Override
	public void visitToken(DetailAST ast){
		DetailAST objBlock =ast.findFirstToken(TokenTypes.OBJBLOCK);
		
		int methodDefs = objBlock.getChildCount(TokenTypes.METHOD_DEF);
		
		if(methodDefs > this.max) {
			String message = "to many method ,only "+this.max+"are allowed";
			log(ast.getLineNo(),message);
		}
		
	}

	@Override
	public int[] getAcceptableTokens() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getRequiredTokens() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// make somthing could property
	public void setMax(int limit) {
		max = limit;
	}


}
