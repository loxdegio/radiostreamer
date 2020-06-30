package it.loxdegio.radiostreamer.services;

import org.springframework.stereotype.Service;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.State;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

@Service
public class PlayerService {

	private EmbeddedMediaPlayer mediaPlayer;

	public PlayerService() {
		MediaPlayerFactory factory = new MediaPlayerFactory();
		mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
	}

	public void play(final String streamURI) {
		mediaPlayer.media().play(streamURI);
	}

	public void stop() {
		if (mediaPlayer.status().isPlaying()) {
			mediaPlayer.controls().pause();
			mediaPlayer.controls().stop();
		}
	}

	public void pause() {
		if (mediaPlayer.status().isPlaying())
			mediaPlayer.controls().pause();
	}

	public void resume() {
		if (mediaPlayer.status().state() == State.PAUSED)
			mediaPlayer.controls().start();
	}

}
