package com.fqueue.form;
// Fig 21.6: MediaPanel.java
// A JPanel the plays media from a URL
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.net.URL;

import javax.media.CannotRealizeException;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.GainControl;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;
import javax.swing.JPanel;

import com.fqueue.common.DataManager;

public class MediaPanel extends JPanel
{
   public MediaPanel( URL mediaURL )
   {
      setLayout( new BorderLayout() ); // use a BorderLayout
      DataManager man = new DataManager();
      // Use lightweight components for Swing compatibility
      Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, Boolean.TRUE);
      
      try
      {
          // create a player to play the media specified in the URL
         final Player mediaPlayer = Manager.createRealizedPlayer( mediaURL );
         
         // get the components for the video and the playback controls
         Component video = mediaPlayer.getVisualComponent();
         
         Component controls = mediaPlayer.getControlPanelComponent();
         
         if ( video != null ) 
            add( video, BorderLayout.CENTER ); 
//         if ( controls != null ) 
//            add( controls, BorderLayout.SOUTH ); 
         
         GainControl ctrl;
         
         ctrl = mediaPlayer.getGainControl();
         
         if(man.getParameter(man.muteCode).equalsIgnoreCase("on"))
        	 ctrl.setMute(false);
         else
        	 ctrl.setMute(true);
         
         mediaPlayer.addControllerListener(new ControllerListener() {
        	 public void controllerUpdate(ControllerEvent event) {
        		 if (event instanceof EndOfMediaEvent) {
        			 mediaPlayer.setMediaTime(new Time(0)); 
        			 mediaPlayer.start();      			 
        		 }
        	 }
         });
         mediaPlayer.start(); 
         
         
         
      } 
      catch ( NoPlayerException noPlayerException )
      {
         System.err.println( "No media player found" );
      } 
      catch ( CannotRealizeException cannotRealizeException )
      {
         System.err.println( "Could not realize media player" );
      }
      catch ( IOException iOException )
      {
         System.err.println( "Error reading from the source" );
      } 
   } 
   

	
	/*class SpellThread extends Thread{
		public void run(){
			while (true){
				URL url = new URL("file:/D:/bailey.mpg");
				MediaPanel mediaPanel = null;
				List listMovie = new ArrayList();
				listMovie.add(url);
				int i=0;
				while (true){
					mediaPanel = new MediaPanel((URL) listMovie.get(i));
					
					if (i<listMovie.size()-1)
						i++;
					else
						i=0;
				}
			}
		}
	}*/

} 
