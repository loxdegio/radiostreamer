package it.loxdegio.radiostreamer.gui;

import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.springframework.beans.factory.annotation.Autowired;

import it.loxdegio.radiostreamer.services.PlayerService;

public class RadiostreamerControls extends JPanel {

	private static final long serialVersionUID = 5355086284817761267L;

	@Autowired
	private PlayerService playerService;
	
	public RadiostreamerControls(LayoutManager rsGridLayout) throws Exception {
			super(rsGridLayout);
			
			add(getPlayButton());
			
			add(getPauseButton());
	}
	
	private JButton getPlayButton() {
		JButton play = new JButton("Play!");
		play.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				playerService.resume();
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		return play;
	}
	
	private JButton getPauseButton() {
		JButton pause = new JButton("Pause...");
		pause.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				playerService.pause();
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		return pause;
	}

}
