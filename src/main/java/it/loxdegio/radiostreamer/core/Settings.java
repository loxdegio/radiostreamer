package it.loxdegio.radiostreamer.core;

import java.awt.GridLayout;
import java.awt.LayoutManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.loxdegio.radiostreamer.gui.RadiostreamerGui;

@Configuration("classpath:application.properties")
public class Settings {

	@Bean("gui")
	public RadiostreamerGui getRadiostreamerGui() throws Exception {
		return new RadiostreamerGui(getRsGridLayout());
	}
	
	
	@Bean("rsGridLayout")
	public LayoutManager getRsGridLayout() {
		GridLayout layout = new GridLayout(0, 5);
		layout.setHgap(5);
		layout.setVgap(5);
		return layout;
	}
}
