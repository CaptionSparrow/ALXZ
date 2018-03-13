import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

public class AcLink {

	public static void main(String[] args) throws InterruptedException, UnsupportedAudioFileException, IOException {
		/* Project 1 Part 1 CK 1 */
		// Node1 node = new Node1();
		// node.record(10);
		// node.play();
		/* --------------------- */
		
		/* Project 1 Part 1 CK 2 */
		Node1 node = new Node1();
		node.recordPreSound("audio/ha02_06.wav", 10);
		node.play();
		/* --------------------- */
		
		/* Project 1 Part 2 CK 1 */
		/* --------------------- */
		
		/* Project 1 Part 3 CK 1 */
		/* --------------------- */
		
		/* Project 1 Part 4 CK 1 */
		/* --------------------- */
	}

}
