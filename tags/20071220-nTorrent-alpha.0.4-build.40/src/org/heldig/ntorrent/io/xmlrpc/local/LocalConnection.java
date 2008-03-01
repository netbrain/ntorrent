/**
 *   nTorrent - A GUI client to administer a rtorrent process 
 *   over a network connection.
 *   
 *   Copyright (C) 2007  Kim Eik
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.heldig.ntorrent.io.xmlrpc.local;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.heldig.ntorrent.gui.profile.ClientProfile;
import org.heldig.ntorrent.io.RpcConnection;
import org.heldig.ntorrent.io.xmlrpc.CustomTypeFactory;
import org.heldig.ntorrent.io.xmlrpc.XmlRpcQueue;
import org.heldig.ntorrent.io.xmlrpc.XmlRpcSCGITransportFactory;

/**
 * @author Kim Eik
 *
 */
public class LocalConnection implements RpcConnection {
	
	private XmlRpcClientConfigImpl config;
	private XmlRpcQueue client;

	public LocalConnection(ClientProfile p) throws MalformedURLException {
		config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL("http://"+p.getHost()+":"+p.getSocketPort()));
		config.setEnabledForExtensions(true);
		config.setEnabledForExceptions(true);
		config.setBasicUserName(p.getUsername());
		config.setBasicPassword(p.getPassword());
		client = new XmlRpcQueue(config);
		client.setTransportFactory(new XmlRpcSCGITransportFactory(client));
		client.setTypeFactory(new CustomTypeFactory(null));
	}
	
	public XmlRpcQueue connect() throws XmlRpcException {
		client.setConfig(config);
	    Object[] params = {"apache"};
	    client.execute("xmlrpc_dialect", params);
		return client;
	}
}