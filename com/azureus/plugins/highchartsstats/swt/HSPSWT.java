/*
 * Created on 05-Sep-2005
 * Created by Paul Gardner
 * Copyright (C) 2005 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * AELITIS, SARL au capital de 30,000 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 *
 */

package com.azureus.plugins.highchartsstats.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import com.biglybt.ui.swt.pif.UISWTInstance;
import com.biglybt.ui.swt.pif.UISWTView;
import com.biglybt.ui.swt.pif.UISWTViewEvent;
import com.biglybt.ui.swt.pif.UISWTViewEventListener;

import com.azureus.plugins.highchartsstats.HighchartsStatsPlugin;

public class 
HSPSWT 
{
	private HighchartsStatsPlugin	plugin;
	private UISWTInstance			swt;
		
	public
	HSPSWT(
		HighchartsStatsPlugin	_plugin,
		UISWTInstance			_swt )
	{
		plugin	= _plugin;
		swt		= _swt;
		
		swt.addView(
			UISWTInstance.VIEW_MAIN,
			"highchartsstats",
			new UISWTViewEventListener()
			{
				private Browser		browser;

				@Override
				public boolean
				eventOccurred(
					UISWTViewEvent event )
				{
					switch( event.getType()){
					
						case UISWTViewEvent.TYPE_CREATE:{
						
							event.getView().setControlType( UISWTView.CONTROLTYPE_SWT );
						
							break;
						}
						case UISWTViewEvent.TYPE_INITIALIZE:{
													
							Composite c = (Composite)event.getData();
							
							browser = new Browser( c, SWT.NULL );
							
							browser.setUrl( plugin.getLocalURL());
							
							break;
						}
						case UISWTViewEvent.TYPE_FOCUSGAINED:{
									
							break;
						}
						case UISWTViewEvent.TYPE_DESTROY:{
						
							if ( browser != null ){
								
								try{
									browser.dispose();
									
								}catch( Throwable e ){
									
									// some kinds of SWT glitch is causing this to fail on closedown
									// java.lang.NullPointerException
									// at org.eclipse.swt.widgets.TreeItem.getBounds(TreeItem.java:431)
								}
								
								browser = null;
							}
													
							break;
						}
					}
					
					return( true );
				}
			});
	}

	public void
	destroy()
	{
		swt.removeViews( UISWTInstance.VIEW_MAIN, "highchartsstats" );
	}
}
