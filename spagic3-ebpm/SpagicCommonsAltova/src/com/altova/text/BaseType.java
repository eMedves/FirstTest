////////////////////////////////////////////////////////////////////////
//
// BaseType.java
//
// This file was generated by MapForce 2007sp1.
//
// YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
// OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
//
// Refer to the MapForce Documentation for further details.
// http://www.altova.com/mapforce
//
////////////////////////////////////////////////////////////////////////

package com.altova.text;

public class BaseType {
	protected ITextNode m_Node = NullTextNode.getInstance();

	public ITextNode getNode() {
		return m_Node;
	}

	public BaseType(ITextNode node) {
		m_Node = node;
	}

	public static String MakeDecimal(ITextNode node) {
		RootTextNode root = (RootTextNode) node.getRoot();
		return root.getValueOfNodeAsDecimalString(node);
	}
}