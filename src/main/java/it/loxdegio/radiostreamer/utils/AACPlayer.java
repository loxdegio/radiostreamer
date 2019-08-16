package it.loxdegio.radiostreamer.utils;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;

public class AACPlayer {

	private SourceDataLine line;

	private boolean playing;
	private boolean stop;

	public void readAACStream(URL aacStream, ArrayBlockingQueue<byte[]> buffer) throws AACException, IOException, LineUnavailableException {
		playing = true;
		byte[] b;
		ADTSDemultiplexer adts = new ADTSDemultiplexer(aacStream.openStream());
		Decoder dec = new Decoder(adts.getDecoderSpecificInfo());
		SampleBuffer buf = new SampleBuffer();
		while ((b = adts.readNextFrame()) != null && !stop) {
			dec.decodeFrame(b, buf);

			if (line == null) {
				final AudioFormat aufmt = new AudioFormat(buf.getSampleRate(), buf.getBitsPerSample(),
						buf.getChannels(), true, true);
				line = AudioSystem.getSourceDataLine(aufmt);
				line.open();
				line.start();
			}

			buffer.add(buf.getData());
		}
	}

	public void playAACStream(ArrayBlockingQueue<byte[]> buffer) {
		byte[] b;
		while ((b = buffer.remove()) != null)
			line.write(b, 0, b.length);
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

}
