import javax.sound.sampled.*;
import java.util.concurrent.*;
import java.io.*;

public class Node1 {
	private boolean recordTrigger;
	protected ByteArrayOutputStream bOut;
	
	private AudioFormat getAudioFormat() {
		float fs = 8000; // Sample Rate
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
					} catch (IOException e) {
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
			System.err.println("Line not available");
			System.exit(-2);
		}
	}
	
	public void record(int time) throws InterruptedException {
		recordAudio();
		TimeUnit.SECONDS.sleep(time);
		recordTrigger = false;
	}	
}
