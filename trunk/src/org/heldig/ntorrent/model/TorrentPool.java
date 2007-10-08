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

package org.heldig.ntorrent.model;


import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.heldig.ntorrent.Controller;
import org.heldig.ntorrent.io.Rpc;
import org.heldig.ntorrent.io.xmlrpc.XmlRpc;
import org.heldig.ntorrent.io.xmlrpc.XmlRpcCallback;
import org.heldig.ntorrent.model.units.Bit;

/**
 * @author   netbrain
 */
public class TorrentPool extends XmlRpcCallback{
	TorrentSet torrents = new TorrentSet();
	private TorrentSet viewset = new TorrentSet();
	String view = "main";
	Rpc rpc;
	TorrentJTableModel table;
	Bit rateUp = new Bit(0);
	Bit rateDown = new Bit(0);
	Controller C;
	
	TorrentPool(){}

	public TorrentPool(Controller c) throws XmlRpcException{
		rpc = c.IO.getRpc();
		table = c.GC.getTorrentTableModel();
		C = c;
	}	
	
	
	/**
	 * @return
	 */
	public String getView() {
		return view;
	}
	
	/**
	 * @return
	 */
	public TorrentJTableModel getTable() {
		return table;
	}
	
	//From viewset
	public int size(){ return viewset.size(); }
	public TorrentInfo get(int index){ return viewset.get(index);	}

	/**
	 * @param  v
	 */
	public void setView(String v){
		view = v;
		if(v.equalsIgnoreCase("main"))
			viewset = torrents;
		else
			viewset = new TorrentSet();
		
		//interrupt thread
		C.TC.getMainContentThread().interrupt();
	}
	
	/**
	 * @return
	 */
	public Bit getRateDown() {
		return rateDown;
	}
	
	/**
	 * @return
	 */
	public Bit getRateUp() {
		return rateUp;
	}

	private String[] getHash(int[] i){
		int x = 0;
		String[] hashlist = new String[i.length];
		for(int index : i){
			hashlist[x++] = get(index).getHash();
		}
		return hashlist;
	}
	
	public void checkHash(int[] i){
		rpc.fileCommand(getHash(i),"d.check_hash");
	}
	public void close(int[] i){
		rpc.fileCommand(getHash(i), "d.close");
	}
	public void erase(int[] i){
		rpc.fileCommand(getHash(i), "d.erase");
	}
	public void open(int[] i){
		rpc.fileCommand(getHash(i), "d.open");
	}
	public void start(int[] i){
		rpc.fileCommand(getHash(i), "d.start");
	}
	public void stop(int[] i){
		rpc.fileCommand(getHash(i), "d.stop");
	}
	
	public void setPriority(int[] i, int pri){
		rpc.setTorrentPriority(getHash(i), pri);
	}
	
	public void stopAll(){
		String[] s = new String[torrents.size()];
		torrents.getHashSet().toArray(s);
		rpc.fileCommand(s, "d.stop");
	}
	
	public void startAll(){
		String[] s = new String[torrents.size()];
		torrents.getHashSet().toArray(s);
		rpc.fileCommand(s, "d.start");
	}

	private void removeOutdated() {
		for(int x = 0; x < viewset.size(); x++){
			TorrentInfo tf = viewset.get(x);
			if(tf.isOutOfDate()){
				viewset.remove(x);
				table.fireTableRowsDeleted(x, x);
			}
		}
	}

	@Override
	public void handleResult(XmlRpcRequest pRequest, Object pResult) {
		rateUp.setValue(0);
		rateDown.setValue(0);
		Object[] obj = (Object[])pResult;
		int viewSize = viewset.size();
		boolean fullUpdate = true;
		if(pRequest.getParameterCount() == XmlRpc.variable.length)
			fullUpdate = false;
		
		for(int x = 0; x < obj.length; x++){
			Object[] raw = (Object[])obj[x];
			TorrentInfo tf = torrents.get((String)raw[0]);
			if(tf == null && fullUpdate){
				tf = new TorrentInfo((String)raw[0]);
				tf.initialize(raw);
				torrents.add(tf);
				table.fireTableRowsInserted(x, x);
			}else if(tf == null && !fullUpdate){
				C.TC.getMainContentThread().interrupt();
				break;
			}
	
			viewset.add(tf);
			tf.update(raw);
			
			
			rateUp.appendValue(tf.getRateUp());
			rateDown.appendValue(tf.getRateDown());
			
		}
		if(viewSize == 0)
			table.fireTableDataChanged();
		else
			table.fireTableRowsUpdated(0, obj.length);
		removeOutdated();
	}	
}