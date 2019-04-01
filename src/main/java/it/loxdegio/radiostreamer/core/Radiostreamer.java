package it.loxdegio.radiostreamer.core;

import java.awt.GridLayout;

import javax.swing.JFrame;

import it.loxdegio.radiostreamer.gui.RadiostreamerGui;

public class Radiostreamer extends JFrame{

	private static final long serialVersionUID = 316549591825631163L;
	
	private RadiostreamerGui gui;
	public Radiostreamer() throws Exception {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridLayout layout = new GridLayout(0, 5);
		layout.setHgap(5);
		layout.setVgap(5);
		gui = new RadiostreamerGui(layout);
		setContentPane(gui);
		pack();
	}

}
