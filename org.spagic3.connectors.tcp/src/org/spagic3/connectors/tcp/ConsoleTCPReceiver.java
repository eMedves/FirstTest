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
package org.spagic3.connectors.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleTCPReceiver implements TCPOutInReceiver {
	private static final Logger log = LoggerFactory.getLogger(ConsoleTCPReceiver.class);

	public void init() {

	}

	public void messageReceived(Object message) {
		log.info("Message Received " + message);
	}

}
