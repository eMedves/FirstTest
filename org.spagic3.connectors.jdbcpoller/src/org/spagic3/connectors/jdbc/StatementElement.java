/**

    Copyright 2007, 2008 Engineering Ingegneria Informatica S.p.A.

    This file is part of Spagic.

    Spagic is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    any later version.

    Spagic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
**/
package org.spagic3.connectors.jdbc;

public class StatementElement {
	
	private String name;
	private String sql;
	private StatementElement[] subQueries;
	
	public StatementElement() {
		super();
	}

	public StatementElement(String name, String sql) {
		super();
		this.name = name;
		this.sql = sql;
	}

	public StatementElement(String name, String sql, StatementElement[] subQueries) {
		super();
		this.name = name;
		this.sql = sql;
		this.subQueries = subQueries;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getSql() {
		return sql;
	}


	public void setSql(String sql) {
		this.sql = sql;
	}


	public StatementElement[] getSubQueries() {
		return subQueries;
	}


	public void setSubQueries(StatementElement[] subQueries) {
		this.subQueries = subQueries;
	}

	
}
