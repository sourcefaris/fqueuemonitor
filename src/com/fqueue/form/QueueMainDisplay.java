package com.fqueue.form;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import com.fqueue.QueueCentral;
import com.fqueue.common.KillApp;
import com.rubean.rcms.ui.RubeanUI;

public class QueueMainDisplay{
	
	private static boolean isRunningQDisplay(){
		boolean enable=false;
		try {
			File file = new File(System.getProperty("queue.dir")+"lock_qdisplay");
			file.isHidden();
			long l = file.lastModified();
			if (l != 0L) {
				long l1 = System.currentTimeMillis() - l;
				if (l1 < 4000L)
					enable= true;
			} else {
				enable= false;
			}
		} catch (Exception e) {
		}
		return enable;
	}
	private void initStartLock() {
        final File file = new File(System.getProperty("queue.dir")+"lock_qdisplay");
	    try {
	         long l = file.lastModified();
	         Timer timer=new Timer(4000, new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					file.delete();
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
	         timer.start();
	         if(l != 0L)
	         {
	             long l1 = System.currentTimeMillis() - l;
	             if(l1 < 4000L){
	             	JOptionPane.showMessageDialog(null,
	    					"Aplikasi Queue Display sudah dijalankan",
	    					"Warning", JOptionPane.ERROR_MESSAGE);
	    							System.exit(0);}
	         }
	     }
	     catch(Exception ioexception)
	     {
	         ioexception.printStackTrace();
	     }
	 }
	public static void main(String[] args) {		
		int x = 0;
		int y = 0;
		
		if ( args.length == 0 ){
			x = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
			y = 0;
		}
		else{
			x = Integer.valueOf(args[0]).intValue();
			y = Integer.valueOf(args[1]).intValue();
		}
		
		if(isRunningQDisplay()==false){
			new QueueMainDisplay(x,y);
		}
		else if(isRunningQDisplay()){
			JOptionPane.showMessageDialog(null,
					"Aplikasi Queue Display sudah dijalankan",
					"Warning", JOptionPane.ERROR_MESSAGE);
							System.exit(0);
		}
	}
	
	public QueueMainDisplay(int x, int y){
		File file = new File(System.getProperty("queue.dir"));
		if(!file.exists()){
			file.mkdir();
		}
		initStartLock();
		try {
			RubeanUI.setRubeanUI();
			QueueCentral.getInstance().init();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		final JFrame frame = new JFrame();
		final JFrame frame2 = new JFrame();
		final CallDisplayScreen mainForm = new QMainPanel(frame);
		
		mainForm.init();
		
		frame.getContentPane().add(mainForm);
		
		double d = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		double d1 = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		Dimension dim=new Dimension();
		dim.setSize(d,d1);
		frame.setSize(dim);
		frame.setLocation(x,y);
		frame.setUndecorated(true);
		frame.setTitle("Queuing Display");
		frame.getContentPane().setBackground(Color.white);   
		frame.setBackground(Color.white);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame2.setLocation(0, 0);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowevent) {
				if (JOptionPane.showConfirmDialog(frame2, "Close Application?", "Exit Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					try {
						System.exit(0);
					} catch (Exception e) {
						System.exit(1);
					}
				}
			}
		});
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
		new KillApp("display");
	}
}


