package it.loxdegio.radiostreamer.gui;

import javax.swing.*;

class RadiostreamerButton extends JButton {

	private static final long serialVersionUID = 5599487727100237442L;
	private String uri;

	public RadiostreamerButton(String uri, String station) {
		super(station);
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
