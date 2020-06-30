package it.loxdegio.radiostreamer.core;

import java.awt.GridLayout;

import javax.annotation.PostConstruct;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.loxdegio.radiostreamer.gui.RadiostreamerControls;
import it.loxdegio.radiostreamer.gui.RadiostreamerGui;
import it.loxdegio.radiostreamer.services.WebDriverService;

@Component
public class Radiostreamer extends JFrame{

	private static final long serialVersionUID = 316549591825631163L;
	
	@Autowired
	private RadiostreamerGui gui;
	
	@Autowired
	private RadiostreamerControls controls;
	
	@Autowired
	private WebDriverService driverService;
	
	@PostConstruct
	public void postConstruct() throws Exception {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        driverService.closeBrowser();
		    }
		});
		GridLayout layout = new GridLayout(2, 1);
		layout.setHgap(5);
		layout.setVgap(5);
		JPanel generalPanel = new JPanel(layout);
		generalPanel.add(gui);
		generalPanel.add(controls);
		setContentPane(generalPanel);
		pack();
	}
	

}
