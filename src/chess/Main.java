package chess;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
public class Main {

	public static void main(String[] args) {
		String p1 = "P1";
		String p2 = "P2";

		Model game = new Model(p1, p2);
		View view = new View(game);
		@SuppressWarnings("unused")
		Controller ctrl = new Controller(game, view);
				
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// don't worry about it
		}
		JFrame main_frame = new JFrame();
		main_frame.setTitle("Chess Game");
		main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main_frame.setContentPane(view);
		main_frame.pack();
		main_frame.setVisible(true);
		main_frame.setMinimumSize(main_frame.getSize());
	}
}
