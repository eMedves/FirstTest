package org.spagic3.ui.serviceeditor.expression;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.janino.ExpressionEvaluator;
import org.spagic3.ui.serviceeditor.model.IServiceModel;


/**
 * 
 * @author zoppello
 *
 */
public class ScrappyEvaluator {
	
	private String janinoExpr = null;
	
	public ScrappyEvaluator(String expr){
		janinoExpr = resolve(expr);
	}
	
	public boolean eval(IServiceModel serviceModel){
		try {
			ExpressionEvaluator ee = new ExpressionEvaluator(
				janinoExpr,
				boolean.class,
				new String[] {"model"},
				new Class[] {IServiceModel.class});
			 
			return (Boolean)ee.evaluate(new Object[]{serviceModel});
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public String[] getTokens(String expr) {
		List<String> tokenList = new ArrayList<String>();
		int level=0;
		
		StringBuffer sb = new StringBuffer();
		
		char c;
		for (int i=0; i < expr.length(); i++){
			 c = expr.charAt(i);
			 if ((c == ',') && (level == 0)){
				 tokenList.add(sb.toString());
				 sb = new StringBuffer();
			 }else{
				 sb.append(c);
				 if (c == '(')
					 level++;
				 else if (c == ')')
					 level--;
			 }
		}
		if (level == 0)
			tokenList.add(sb.toString());
		String[] tokList = new String[tokenList.size()];
		tokList = tokenList.toArray(tokList);
		return tokList;
	}
	
	
	public String resolve(String expr) {
		if ((expr.startsWith("and")) || (expr.startsWith("or"))) {
			String predicate =  expr.substring(0,expr.indexOf("("));
			String predicateArgument = expr.substring(expr.indexOf("(") + 1, expr.lastIndexOf(")"));
			String[] tokens = getTokens(predicateArgument);
			StringBuffer sb = new StringBuffer();
			sb.append("( ");
			for (int i=0; i < tokens.length; i++){
				sb.append(resolve(tokens[i]));
				if (i < (tokens.length - 1) )
					sb.append( predicate.equalsIgnoreCase("and") ? " && " : " || ");
			}
			sb.append(" ) ");
			return sb.toString();
		} else {
			return predicateResolve(expr);
		}
	}
	
	public String predicateResolve(String predicate) {
		if (predicate.equalsIgnoreCase("true()")){
			return new String(" ( 1 == 1 ) ");
		}else if (predicate.equalsIgnoreCase("false()")){
			return new String(" ( 1 == 2 ) ");
		}else if (predicate.startsWith("true(")){
			String pName = predicate.substring(predicate.indexOf("(")+1, predicate.lastIndexOf(")"));
			return new String(" (  \"true\".equals(model.get(\""+pName+"\")) ) ");
		}else if (predicate.startsWith("false(")){
			String pName = predicate.substring(predicate.indexOf("(")+1, predicate.lastIndexOf(")"));
			return new String(" ( \"false\".equals(model.get(\""+pName+"\")) ) ");
		}else if (predicate.startsWith("is(")){
			String pName = predicate.substring(predicate.indexOf("(")+1, predicate.indexOf(","));
			String pValue = predicate.substring(predicate.indexOf(",")+1, predicate.lastIndexOf(")"));
			return new String(" ( \""+pValue+"\".equals(model.get(\""+pName+"\")) ) ");
		}else if (predicate.startsWith("oneOf(")){
			String pName = predicate.substring(predicate.indexOf("(")+1, predicate.indexOf(","));
			String candidateTokens = predicate.substring(predicate.indexOf("[")+1, predicate.indexOf("]"));
			String[] cTokens = candidateTokens.split(";");
			
			StringBuffer janinoBuffer = new StringBuffer(); 
			janinoBuffer.append("( ");
			for (int i=0; i < cTokens.length; i++){
				
				janinoBuffer.append("( \""+cTokens[i]+"\".equals(model.get(\""+pName+"\")) )");
				if (i < (cTokens.length - 1) ){
					janinoBuffer.append ( " || ");
				}
			}
			janinoBuffer.append(" ) ");
			return janinoBuffer.toString();
		}else{
			return predicate;
		}
	}

}
