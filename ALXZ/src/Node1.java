import javax.sound.sampled.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.*;

public class Node1 {
	private boolean recordTrigger;
	private boolean playerTrigger;
	private boolean generateTrigger;
	protected ByteArrayOutputStream bOut;
	protected ByteArrayInputStream bIn;
	
	private AudioFormat getAudioFormat() {
		float fs = 44100; // Sample Rate
	    int sampleSize = 16; // Sample size in bits
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

	private void playFile(String path) throws UnsupportedAudioFileException, IOException {
		try {
			File file = new File(path);
			final AudioInputStream preSound = AudioSystem.getAudioInputStream(file);
			final AudioFormat format = preSound.getFormat();
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start(); // Output audio to sounder
			
			/* Establish a thread method for playing audio */
			Runnable sounder = new Runnable() {
				int size = (int)format.getSampleRate() * format.getFrameSize();
				byte buffer[] = new byte[size];
				
				public void run() {
					playerTrigger = true;
					try {
						// Read bytes from input stream to buffer
						int nBytes = preSound.read(buffer, 0, buffer.length);
						while (nBytes != -1 && playerTrigger) {
							if (nBytes > 0) {
								// Write bytes from buffer to line
								line.write(buffer, 0, nBytes);
							}
							// Read bytes from input stream to buffer
							nBytes = preSound.read(buffer, 0, buffer.length);
						}
						// Close the line after the playing is end
						line.drain();
						line.close();
					}
					catch (IOException e) {
						System.err.println("IO exception when playing");
						System.exit(-5);
					}
				}
			};
			
			/* Construct a thread for playing audio */
			Thread soundThread = new Thread(sounder);
		    soundThread.start();
		}
		catch (LineUnavailableException e) {
			System.err.println("Line not available when playing");
			System.exit(-6);
		}
	}
	
	private void playSine(double fReq1, double fReq2) {
		try {
			final AudioFormat format = getAudioFormat();
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start(); // Output audio to sounder
			
			/* Establish a thread method for playing audio */
			Runnable playsine = new Runnable() {
				ByteBuffer buf = ByteBuffer.allocate(line.getBufferSize());
				
				public void run() {
					generateTrigger = true;
					double fStep1 = fReq1 / 44100;
					double fStep2 = fReq2 / 44100;
					double fPosition1 = 0;
					double fPosition2 = 0;
					while (generateTrigger) {
				         buf.clear();
				         int ctSamples = line.available() / 2;   
				         for (int i=0; i < ctSamples; i++) {
				            buf.putShort((short)(Short.MAX_VALUE * Math.sin(2*Math.PI *fPosition1)+Short.MAX_VALUE * Math.sin(2*Math.PI *fPosition2)));
				            fPosition1 += fStep1;
				            fPosition2 += fStep2;
				         }

				         line.write(buf.array(), 0, buf.position());             

				         //Wait until the buffer is at least half empty  before we add more
				         while (line.getBufferSize()/2 < line.available())
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {
								System.err.println("Interrupt exception when playing");
								System.exit(-7);
							}                                             
				      }

				      line.drain();                                         
				      line.close();
				}
			};
			
			/* Construct a thread for playing audio */
			Thread sineThread = new Thread(playsine);
		    sineThread.start();
		}
		catch (LineUnavailableException e) {
			System.err.println("Line not available when playing");
			System.exit(-8);
		}
	}
	
	public void record(int time) throws InterruptedException {
		System.out.println("Now recording");
		recordAudio();
		TimeUnit.SECONDS.sleep(time);
		recordTrigger = false;
		System.out.println("End recording");
	}
	
	public void play() throws InterruptedException {
		System.out.println("Now playing the record audio");
		TimeUnit.SECONDS.sleep(1);
		playAudio();
	}
	
	public void recordPreSound(String path, int time) throws UnsupportedAudioFileException, IOException, InterruptedException {
		System.out.println("Playing the predefined sound");
		playFile(path);
		System.out.println("Recording the predefined sound");
		recordAudio();
		TimeUnit.SECONDS.sleep(time);
		playerTrigger = false;
		recordTrigger = false;
		System.out.println("End recording");
	}
	
	public void playFunction(double fReq1, double fReq2, int time) throws InterruptedException {
		System.out.println("Now playing the sine funtion");
		playSine(fReq1, fReq2);
		TimeUnit.SECONDS.sleep(time);
		generateTrigger = false;
		System.out.println("End playing");
	}
}
