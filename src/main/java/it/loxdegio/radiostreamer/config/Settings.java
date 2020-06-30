package it.loxdegio.radiostreamer.config;

import java.awt.GridLayout;
import java.awt.LayoutManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.loxdegio.radiostreamer.gui.RadiostreamerControls;
import it.loxdegio.radiostreamer.gui.RadiostreamerGui;

@Configuration("classpath:application.properties")
public class Settings {

	@Bean("gui")
	public RadiostreamerGui getRadiostreamerGui() throws Exception {
		return new RadiostreamerGui(getRsGridLayout());
	}
	
	@Bean("controls")
	public RadiostreamerControls getRadiostreamerControls() throws Exception {
		return new RadiostreamerControls(getControlsGridLayout());
	}
	
	
	@Bean("rsGridLayout")
	public LayoutManager getRsGridLayout() {
		GridLayout layout = new GridLayout(0, 5);
		layout.setHgap(5);
		layout.setVgap(5);
		return layout;
	}
	
	@Bean("controlsGridLayout")
	public LayoutManager getControlsGridLayout() {
		GridLayout layout = new GridLayout(0, 2);
		layout.setHgap(5);
		layout.setVgap(5);
		return layout;
	}
}
