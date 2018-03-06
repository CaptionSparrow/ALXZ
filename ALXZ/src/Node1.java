import javax.sound.sampled.*;
import java.util.concurrent.*;
import java.io.*;

public class Node1 {
	private boolean recordTrigger;
	protected ByteArrayOutputStream bOut;
	protected ByteArrayInputStream bIn;
	
	private AudioFormat getAudioFormat() {
		float fs = 44100; // Sample Rate
	    int sampleSize = 8; // Sample size in bits
	    int channels = 1;
	    boolean signed = true;
	    boolean bigEndian = true;
	    return new AudioFormat(fs, sampleSize, channels, signed, bigEndian);
	}
	
	private void recordAudio() {
		try {
			final AudioFormat format = getAudioFormat();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start(); // Get audio from the microphone
			
			/* Establish a thread method for recording audio */
			Runnable recorder = new Runnable() {
				int size = (int)format.getSampleRate() * format.getFrameSize();
				byte buffer[] = new byte[size];
				
				public void run() {
					bOut = new ByteArrayOutputStream();
					recordTrigger = true;
					try {
						while (recordTrigger) {
							// Read bytes from line to buffer
							int nBytes = line.read(buffer, 0, buffer.length);
							// Write bytes from buffer to output stream
				            if (nBytes > 0) {
				                bOut.write(buffer, 0, nBytes);
				            }
						}
						bOut.close();
					}
					catch (IOException e) {
						System.err.println("IO exception when recording");
						System.exit(-1);
					}
				}
			};
			
			/* Construct a thread for recording audio */
			Thread recordThread = new Thread(recorder);
			recordThread.start();
		}
		catch (LineUnavailableException e) {
			System.err.println("Line not available when recording");
			System.exit(-2);
		}
	}
	
	private void playAudio() {
		try {
			final AudioFormat format = getAudioFormat();
			byte audioArray[] = bOut.toByteArray();
			bIn = new ByteArrayInputStream(audioArray);
			final AudioInputStream aIn = new AudioInputStream(bIn, format, audioArray.length / format.getFrameSize());
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start(); // Output audio to sounder
			
			/* Establish a thread method for playing audio */
			Runnable player = new Runnable() {
				int size = (int)format.getSampleRate() * format.getFrameSize();
				byte buffer[] = new byte[size];
				
				public void run() {
					try {
						// Read bytes from input stream to buffer
						int nBytes = aIn.read(buffer, 0, buffer.length);
						while (nBytes != -1) {
							if (nBytes > 0) {
								// Write bytes from buffer to line
								line.write(buffer, 0, nBytes);
							}
							// Read bytes from input stream to buffer
							nBytes = aIn.read(buffer, 0, buffer.length);
						}
						// Close the line after the playing is end
						line.drain();
						line.close();
					}
					catch (IOException e) {
						System.err.println("IO exception when playing");
						System.exit(-3);
					}
				}
			};
			
			/* Construct a thread for playing audio */
			Thread playThread = new Thread(player);
		    playThread.start();
		}
		catch (LineUnavailableException e) {
			System.err.println("Line not available when playing");
			System.exit(-4);
		}
	}
	
	public void record(int time) throws InterruptedException {
		System.out.println("Now recording");
		recordAudio();
		TimeUnit.SECONDS.sleep(time);
		recordTrigger = false;
		System.out.println("End recording");
	}
	
	public void play() {
		System.out.println("Now playing");
		playAudio();
	}
}
