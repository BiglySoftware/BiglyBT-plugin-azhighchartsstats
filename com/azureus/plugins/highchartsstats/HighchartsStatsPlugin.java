/*
 * Created on Jun 8, 2012
 * Created by Paul Gardner
 * 
 * Copyright 2012 Vuze, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */



package com.azureus.plugins.highchartsstats;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Properties;

import com.biglybt.core.util.Debug;
import com.biglybt.pif.PluginException;
import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.UnloadablePlugin;
import com.biglybt.pif.download.DownloadManagerStats;
import com.biglybt.pif.tracker.web.TrackerWebPageRequest;
import com.biglybt.pif.tracker.web.TrackerWebPageResponse;
import com.biglybt.pif.ui.UIInstance;
import com.biglybt.pif.ui.UIManagerListener;
import com.biglybt.pif.ui.model.BasicPluginConfigModel;
import com.biglybt.ui.swt.pif.UISWTInstance;
import com.biglybt.ui.webplugin.WebPlugin;

import com.biglybt.core.pairing.PairingManager;
import com.biglybt.core.pairing.PairingManagerFactory;
import com.azureus.plugins.highchartsstats.swt.HSPSWT;

public class 
HighchartsStatsPlugin 
	extends WebPlugin
	implements UnloadablePlugin
{
    public static final int DEFAULT_PORT    = 9092;

    private static Properties defaults = new Properties();

    static{

        defaults.put( WebPlugin.PR_DISABLABLE, new Boolean( true ));
        defaults.put( WebPlugin.PR_ENABLE, new Boolean( true ));
        defaults.put( WebPlugin.PR_PORT, new Integer( DEFAULT_PORT ));
        defaults.put( WebPlugin.PR_ROOT_DIR, "web" );
        defaults.put( WebPlugin.PR_ENABLE_KEEP_ALIVE, new Boolean(true));
        defaults.put( WebPlugin.PR_HIDE_RESOURCE_CONFIG, new Boolean(true));
        defaults.put( WebPlugin.PR_PAIRING_SID, "highchartsstats" );
    }

    private HSPSWT	swt_ui;
    
    public
    HighchartsStatsPlugin()
    {
    	super( defaults );
    }
    
	@Override
	public void
	initialize(
		PluginInterface _plugin_interface )
	
		throws PluginException
	{	
		super.initialize( _plugin_interface );
		
		plugin_interface	= _plugin_interface;
		
		plugin_interface.getUtilities().getLocaleUtilities().integrateLocalisedMessageBundle( 
				"com.azureus.plugins.highchartsstats.internat.Messages" );
				
		BasicPluginConfigModel	config = getConfigModel();
			
		config.addLabelParameter2( "highchartsstats.blank" );

		config.addHyperlinkParameter2( "highchartsstats.openui", getBaseURL());
		
		config.addLabelParameter2( "highchartsstats.blank" );
		
		plugin_interface.getUIManager().addUIListener(
				new UIManagerListener()
				{
					@Override
					public void
					UIAttached(
						UIInstance		instance )
					{
						if ( instance instanceof UISWTInstance ){
							
							UISWTInstance	swt = (UISWTInstance)instance;
							
							swt_ui = new HSPSWT( HighchartsStatsPlugin.this, swt );
						}
					}
					
					@Override
					public void
					UIDetached(
						UIInstance		instance )
					{
						
					}
				});
	}
	
	@Override
	public void
	unload() 
		
		throws PluginException 
	{	
		if ( swt_ui != null ){
			
			swt_ui.destroy();
			
			swt_ui = null;
		}
	}
	
	protected String
	getBaseURL()
	{
		int port = plugin_interface.getPluginconfig().getPluginIntParameter( WebPlugin.CONFIG_PORT, CONFIG_PORT_DEFAULT );

		return( "http://127.0.0.1:" + port + "/" );
	}
	
	public String
	getLocalURL()
	{
		PairingManager pm = PairingManagerFactory.getSingleton();
		
		String	res = getBaseURL();
		
		if ( pm.isEnabled()){
			
			String ac = pm.peekAccessCode();
			
			if ( ac != null ){
				
				res += "?vuze_pairing_ac=" + ac;
			}
		}
		
		return( res );
	}
	
	@Override
	public boolean
	generateSupport(
		TrackerWebPageRequest		request,
		TrackerWebPageResponse		response )
	
		throws IOException
	{
		try{
			URL url = request.getAbsoluteURL();
				
			String	url_path = url.getPath();
			
			if ( url_path.equals( "/" )){
				
				PrintWriter pw =new PrintWriter( new OutputStreamWriter( response.getOutputStream(), "UTF-8" ));
				
				pw.println( "<HTML>" );
				pw.println( "<HEAD><TITLE>Azureus Highcharts Stats</TITLE></HEAD>" );
				pw.println( "<BODY>" );
				pw.println( "<UL>" );
				pw.println( "<li><a href=\"charts/updown.html\">Up Down</a> ") ;
				pw.println( "</UL>" );
				pw.println( "</BODY></HTML>" );
				pw.flush();
				
				response.setContentType( "text/html; charset=UTF-8" );
				
				response.setGZIP( true );
				
				return( true );
				
			}else if ( url_path.equals( "/charts/updown_latest.dat" )){
				
				PrintWriter pw =new PrintWriter( new OutputStreamWriter( response.getOutputStream(), "UTF-8" ));
				
				DownloadManagerStats stats = plugin_interface.getDownloadManager().getStats();
				
				int send	= stats.getDataSendRate() + stats.getProtocolSendRate();
				int rec 	= stats.getDataReceiveRate() + stats.getProtocolReceiveRate();
				
				pw.println( send + "," + rec );
				
				pw.flush();
				
				response.setContentType( "text/html; charset=UTF-8" );
				
				response.setGZIP( true );
				
				return( true );
				
			}else{
				
				return( super.generateSupport(request, response));
			}
		}catch( Throwable e ){
							
			log( "Processing failed", e );
			
			throw( new IOException( "Processing failed: " + Debug.getNestedExceptionMessage( e )));
		}
	}
}
