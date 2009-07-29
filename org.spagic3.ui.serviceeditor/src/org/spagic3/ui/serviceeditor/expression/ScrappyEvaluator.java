package org.spagic3.ui.serviceeditor.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.janino.ExpressionEvaluator;
import org.spagic3.ui.serviceeditor.model.IServiceModel;
import org.spagic3.ui.serviceeditor.model.ServiceModel;


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
	
	public boolean eval(){
		try{
			 ExpressionEvaluator ee = new ExpressionEvaluator(
					 janinoExpr,
					 boolean.class,
					 new String[] {"model"},
					 new Class[] {IServiceModel.class});
			 
			 IServiceModel serviceModel = new ServiceModel();
			 serviceModel.addProperty("isSsl", "true");
			 serviceModel.addProperty("clientAuthentication", "ccc");
			 
			 return (Boolean)ee.evaluate(new Object[]{serviceModel});
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	//and(true(isSsl),oneOf(clientAuthentication,[OptionalClientAuthentication;MandatoryClientAuthentication]))
	
	public String[] getTokens(String expr){
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
	
	
	public String resolve(String expr){
		
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
	
	public String predicateResolve(String predicate){
		if (predicate.equalsIgnoreCase("true()")){
			return new String(" ( 1 == 1 ) ");
		}else if (predicate.equalsIgnoreCase("false()")){
			return new String(" ( 1 == 2 ) ");
		}else if (predicate.startsWith("true(")){
			String pName = predicate.substring(predicate.indexOf("(")+1, predicate.lastIndexOf(")"));
			return new String(" ( model.get(\""+pName+"\") == \"true\" )");
		}else if (predicate.startsWith("false(")){
			String pName = predicate.substring(predicate.indexOf("(")+1, predicate.lastIndexOf(")"));
			return new String(" ( model.get(\""+pName+"\") == \"false\" )");
		}else if (predicate.startsWith("is(")){
			String pName = predicate.substring(predicate.indexOf("(")+1, predicate.indexOf(","));
			String pValue = predicate.substring(predicate.indexOf(",")+1, predicate.lastIndexOf(")"));
			return new String(" ( model.get(\""+pName+"\") == \""+pValue+"\" )");
		}else if (predicate.startsWith("oneOf(")){
			String pName = predicate.substring(predicate.indexOf("(")+1, predicate.indexOf(","));
			String candidateTokens = predicate.substring(predicate.indexOf("[")+1, predicate.indexOf("]"));
			String[] cTokens = candidateTokens.split(";");
			
			StringBuffer janinoBuffer = new StringBuffer(); 
			janinoBuffer.append("( ");
			for (int i=0; i < cTokens.length; i++){
				
				janinoBuffer.append("( model.get(\""+pName+"\") == \""+cTokens[i]+"\" )");
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
	
	/*
	public static void main(String[] args) {
		ScrappyEvaluator evaluatorTest = new ScrappyEvaluator(ss);
		String resolved = evaluatorTest.resolve("and(true(isSsl),oneOf(clientAuthentication,[OptionalClientAuthentication;MandatoryClientAuthentication]))");
		System.out.println(resolved);
		
		try{
			 ExpressionEvaluator ee = new ExpressionEvaluator(
					 resolved,
					 boolean.class,
					 new String[] {"model"},
					 new Class[] {IServiceModel.class});
			 
			 IServiceModel serviceModel = new ServiceModel();
			 serviceModel.addProperty("isSsl", "true");
			 serviceModel.addProperty("clientAuthentication", "ccc");
			 
			 System.out.println(ee.evaluate(new Object[]{serviceModel}));
		}catch (Exception e) {
			e.printStackTrace();
		}
		/*
		try{
			
			String expr = "isSsl==\"true\" && ( isClientAuthentication ==\"Optional\" || isClientAuthentication==\"Mandatory\")";
			// Compile the expression once; relatively slow.
			
		    ExpressionEvaluator ee = new ExpressionEvaluator(
		          expr,                     							// expression
		          boolean.class,                            			// expressionType
		          new String[] { "isSsl", "isClientAuthentication", "pippo", "pelliccio" },           // parameterNames
		          new Class[] { String.class, String.class, String.class, String.class   });	
			Boolean flag = (Boolean)ee.evaluate(new Object[]{"true", "Mandatory", "Ciccio", "Pelliccio"});
			System.out.println(flag);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/


}
