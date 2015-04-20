package com.fqueue.form;

import java.beans.PropertyChangeEvent;

import javax.swing.JFrame;


public class QMainPanel extends CallDisplayScreen  { 
	public JFrame frameParent;

	public QMainPanel (JFrame frameParent) {
		super();
		this.frameParent = frameParent;
	}

	public void propertyChange_(final PropertyChangeEvent e) {
		super.propertyChange(e);
	}

}