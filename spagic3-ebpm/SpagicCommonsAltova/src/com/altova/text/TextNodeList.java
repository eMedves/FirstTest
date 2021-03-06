////////////////////////////////////////////////////////////////////////
//
// TextNodeList.java
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.altova.text.TextNodeListIterator;

public class TextNodeList implements ITextNodeList {
	private List m_List = null;
	private Hashtable m_NamesToNodes = null;

	private TextNode m_Owner = null;

	void buildNamedNodes() {
		if (m_NamesToNodes != null)
			return;
			
		m_NamesToNodes = new Hashtable();

		for (int i=0; i< m_List.size(); i++)
			addToTable( (ITextNode) m_List.get(i));
	}
	
	private void addToTable(ITextNode rhs) {
		
		String name = rhs.getName();
		
		if (!m_NamesToNodes.containsKey(name))
			m_NamesToNodes.put(name, new ArrayList());
		ArrayList list = (ArrayList) m_NamesToNodes.get(name);
		list.add(rhs);
	}

	private void removeFromTable(ITextNode rhs) {
		String name = rhs.getName();
			
		ArrayList list = (ArrayList) m_NamesToNodes.get(name);
		list.remove(rhs);
	}

	public TextNodeList(TextNode owner) {
		m_Owner = owner;
	}

	public void add(ITextNode rhs) {
		if (null == rhs)
			return;
		
		if (m_List == null)
			m_List = new ArrayList();
		if (m_NamesToNodes != null)
			addToTable(rhs);
		
		m_List.add(rhs);
		rhs.setParent(m_Owner);
	}

	public void insertAt(ITextNode rhs, int index) {
		 if (m_List == null) 
                m_List = new ArrayList ();
		if (m_NamesToNodes != null)
			addToTable(rhs);
		 
		m_List.add(index, rhs);
		rhs.setParent(m_Owner);
	}

	public void removeAt(int index) {
		if (m_List == null)
                return;
		
		if (m_NamesToNodes != null)
			removeFromTable((ITextNode) m_List.get(index));
		m_List.remove(index);
	}

	public int size() {
		return m_List == null ? 0: m_List.size();
	}

	public ITextNode getAt(int index) {
		if (m_List == null || (0 > index) || (index >= m_List.size()))
			return NullTextNode.getInstance();
		return (TextNode) m_List.get(index);
	}

	public TextNodeListIterator iterator() {
		return new TextNodeListIterator(m_List.iterator());
	}

	public boolean contains(ITextNode rhs) {
		if (m_List == null)
                return false;
		
		for (int i = 0; i < m_List.size(); ++i) {
			ITextNode kid = this.getAt(i);
			if (kid == rhs)
				return true;
			if (kid.getChildren().contains(rhs))
				return true;
		}
		return false;
	}

	public TextNodeList filterByName(String name) {
		TextNodeList result = new TextNodeList(m_Owner);
		if (m_List == null)
			return result;

		buildNamedNodes();

		if (!m_NamesToNodes.containsKey(name))
			return result;
		ArrayList list = (ArrayList) m_NamesToNodes.get(name);
		result.m_List = new ArrayList();
		result.m_List.addAll(list);
		return result;
	}
	
	public ITextNode getFirstNodeByName(String name) {
		if (m_List == null)
                return null;
		
		 buildNamedNodes();
		
		if (!m_NamesToNodes.containsKey(name))
			return null;
		
		TextNodeList children = filterByName(name);
		return children.size() == 0 ? null : children.getAt(0);
	}
	
	public ITextNode getLastNodeByName(String name) {
		if (m_List == null)
                return null;
		
		 buildNamedNodes();
		
		if (!m_NamesToNodes.containsKey(name))
			return null;
		
		TextNodeList children = filterByName(name);
		return children.size() == 0 ? null : children.getAt(children.size()-1);
	}
	
	public void moveNode(ITextNode rhs, int index) {
		if (m_List == null)
                return;
		
		int nowIndex = m_List.indexOf(rhs);
		
		if (index != nowIndex)
		{
			m_List.remove(nowIndex);
			if (index > nowIndex)
				m_List.add(index-1, rhs);
			else
				m_List.add(index, rhs);
		}
	}
}
