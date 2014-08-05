package chess;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

@SuppressWarnings("serial")
public class View extends JPanel implements MouseListener, Observer {

	private final ArrayList<ViewListener> viewListeners = new ArrayList<ViewListener>();
	private GameInfoPanel infoPanel;
	private JPanel mainPanel;
	private GameBoardWidget boardPanel;
	private PlayerLabelWidget playerOne;
	private PlayerLabelWidget playerTwo;

	private final Model model;

	public View(Model model) {
		this.model = model;
		init(model);
		boardPanel.update(model);
	}

	public void init(Model model) {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		playerTwo = new PlayerLabelWidget(model.getPlayer2());
		boardPanel = new GameBoardWidget(this);
		playerOne = new PlayerLabelWidget(model.getPlayer1());

		mainPanel.add(playerTwo, BorderLayout.NORTH);
		mainPanel.add(boardPanel, BorderLayout.CENTER);
		mainPanel.add(playerOne, BorderLayout.SOUTH);

		infoPanel = new GameInfoPanel(model, this);

		// infoPanel constraints
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.weightx = 0;
		c.weighty = 1;
		c.gridy = 0;
		c.gridx = 0;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(infoPanel, c);

		// mainPanel constraints
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridx = 1;
		gridBag.setConstraints(mainPanel, c);
		this.setLayout(gridBag);
		this.add(infoPanel);
		this.add(mainPanel);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof String) {
			infoPanel.updateText((String) arg);
		} else if (arg instanceof ChessPlayer) {
			ChessPlayer player = (ChessPlayer) arg;
			if (model.getPlayer1().equals(player)) {
				playerOne.updateLabel((ChessPlayer) arg);
			} else {
				playerTwo.updateLabel(player);
			}
			infoPanel.updatePlayerTurn(player);
			// update these to change the player name toolTips
			infoPanel.updateAll(model);
			boardPanel.update(model);
		} else if (arg instanceof boolean[][]) {
			boardPanel.update((boolean[][]) arg);
		} else {
			infoPanel.updateAll(model);
			boardPanel.update(model);
		}
	}

	public void addViewListener(ViewListener l) {
		viewListeners.add(l);
	}

	public void removeViewListener(ViewListener l) {
		viewListeners.remove(l);
	}

	public ArrayList<ViewListener> getViewListeners() {
		return viewListeners;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		notifyViewListeners(new MouseMoveEvent(e, null,
				MouseMoveEvent.Type.MOUSE_ENTERED));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		notifyViewListeners(new MouseMoveEvent(e, null,
				MouseMoveEvent.Type.MOUSE_EXITED));
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		ChessPosition position = ((GameSquareWidget) e.getSource())
				.getPosition();
		notifyViewListeners(new MouseMoveEvent(e, position,
				MouseMoveEvent.Type.MOUSE_RELEASED));
	}

	public void notifyViewListeners(ViewEvent e) {
		for (ViewListener l : viewListeners) {
			l.handleChessViewEvent(e);
		}
	}

}

class GameBoardWidget extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4008239470376384410L;
	private final GameSquareWidget[][] squares = new GameSquareWidget[8][8];
	private final GridBagLayout gridbag = new GridBagLayout();
	private final GridBagConstraints c = new GridBagConstraints();

	public GameBoardWidget(MouseListener l) {
		this.setLayout(gridbag);
		this.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(10,
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 20.0f)));
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridy = 0;
		c.gridx = 1;
		// set these as the constraints for the panel
		gridbag.setConstraints(this, c);
		// common constraints below
		c.gridheight = 1;
		c.gridwidth = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new Insets(0, 0, 0, 0);
		for (int j = 0; j < 8; j++) {
			// Number the rows
			JLabel rowLabel = new JLabel(" " + String.valueOf(Math.abs(8 - j))
					+ " ");
			rowLabel.setForeground(Color.WHITE);
			rowLabel.setOpaque(true);
			rowLabel.setBackground(Color.DARK_GRAY);
			c.gridx = 0;
			c.gridy = j;
			c.weightx = 0;
			c.weighty = 0;
			this.add(rowLabel, c);
			for (int i = 0; i < 8; i++) {
				// Build the game board squares
				squares[i][j] = new GameSquareWidget(new ChessPosition(i, j));
				c.fill = GridBagConstraints.BOTH;
				c.gridx = i + 1;
				c.gridy = Math.abs(j - 7);
				c.weightx = 1;
				c.weighty = 1;
				squares[i][j].addMouseListener(l);
				this.add(squares[i][j], c);
			}
		}
		// Letter indices for the columns
		for (int i = 0; i < 8; i++) {
			if (i == 0) {
				JLabel corner = new JLabel(" ");
				corner.setOpaque(true);
				corner.setBackground(Color.DARK_GRAY);
				c.gridx = 0;
				c.gridy = 8;
				c.weightx = 0;
				c.weighty = 0;
				this.add(corner, c);
			}
			JLabel rowLabel = new JLabel(" " + (char) ('a' + i) + " ",
					JLabel.CENTER);
			rowLabel.setForeground(Color.WHITE);
			rowLabel.setOpaque(true);
			rowLabel.setBackground(Color.DARK_GRAY);
			c.gridx = i + 1;
			c.gridy = 8;
			c.weightx = 0;
			c.weighty = 0;
			this.add(rowLabel, c);
		}
		update();
	}

	public void update(boolean[][] highlights) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (highlights[i][j]) {
					squares[i][j].setBackground(Color.YELLOW);
				} else {
					if (i % 2 == 1 && j % 2 == 0) {
						squares[i][j].setBackground(Color.WHITE);
					} else if (i % 2 == 0 && j % 2 == 1) {
						squares[i][j].setBackground(Color.WHITE);
					} else {
						squares[i][j].setBackground(Color.GRAY);
					}
				}
			}
		}
	}

	private void update() {
		boolean[][] highlights = new boolean[8][8];
		update(highlights);
	}

	public void update(Model model) {
		for (int j = 0; j < 8; j++) {
			for (int i = 0; i < 8; i++) {
				ChessPiece pieceRef = model.getPieceOnBoard(new ChessPosition(
						i, j));
				JButton squareRef = squares[i][j];
				if (pieceRef != null) {
					squareRef.setText(Character.toString(pieceRef.getMark()));
					squareRef.setFont(new Font("Dialog", 1, 40));
					squareRef.setToolTipText(pieceRef.getOwner().getName()
							+ "'s " + pieceRef.toString());
				} else {
					squareRef.setText("");
					squareRef.setToolTipText("");
				}
			}
		}
	}
}

class CapturedPiecesWidget extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2882742724478041909L;
	private final GameSquareWidget[][] squares = new GameSquareWidget[4][8];
	private final GridBagLayout gridbag = new GridBagLayout();
	private final GridBagConstraints c = new GridBagConstraints();

	public CapturedPiecesWidget() {
		this.setLayout(gridbag);
		c.fill = GridBagConstraints.VERTICAL;
		c.weightx = 0;
		c.weighty = 1;
		c.gridy = 0;
		c.gridx = 2;
		// set these as the constraints for the panel
		gridbag.setConstraints(this, c);
		// common constraints below
		c.gridheight = 1;
		c.gridwidth = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new Insets(0, 0, 0, 0);
		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 8; i++) {
				// Build the squares
				squares[j][i] = new GameSquareWidget();
				c.fill = GridBagConstraints.BOTH;
				c.gridx = i + 1;
				c.gridy = Math.abs(j - 7);
				c.weightx = 1;
				c.weighty = 1;
				this.add(squares[j][i], c);
			}
		}
	}

	public void update(Model model) {
		ArrayList<ChessPiece> pieces = model.getPiecesOffBoard();
		int index = 0;
		for (GameSquareWidget[] rows : squares) {
			for (GameSquareWidget square : rows) {
				try {
					ChessPiece piece = pieces.get(index);
					square.setText(Character.toString(piece.getMark()));
					square.setFont(new Font("Dialog", 1, 20));
					square.setToolTipText(piece.getOwner().getName() + "'s "
							+ piece.toString());
				} catch (IndexOutOfBoundsException e) {
					square.setText("");
					square.setToolTipText("");
				} finally {
					index++;
				}
			}
		}
	}
}

class GameSquareWidget extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4814385078710542011L;
	private final ChessPosition location;

	public GameSquareWidget(ChessPosition location) {
		this.location = location;
		this.setPreferredSize(new Dimension(50, 50));
		this.setBorder(BorderFactory.createEtchedBorder());
		this.setFocusable(false);
	}

	public GameSquareWidget() {
		// for the captured pieces panel
		this(new ChessPosition(0, 0));
		this.setPreferredSize(new Dimension(25, 25));
		this.setBackground(Color.GRAY);
		this.setOpaque(false);
		this.setBorder(null);
	}

	public ChessPosition getPosition() {
		return location;
	}
}

class PlayerLabelWidget extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6970905918322197153L;
	private final GridBagLayout gridbag = new GridBagLayout();
	private final GridBagConstraints c = new GridBagConstraints();
	private final JLabel playerLabel;

	public PlayerLabelWidget(ChessPlayer player) {
		playerLabel = new JLabel();
		updateLabel(player);
		this.add(playerLabel);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridy = 0;
		c.gridx = 1;
		gridbag.setConstraints(this, c);
	}

	public void updateLabel(ChessPlayer player) {
		playerLabel.setText(player.getName());
		this.validate();
		this.repaint();
	}
}

class GameInfoPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8939647084387470879L;
	private final GridBagLayout gridbag = new GridBagLayout();
	private final GridBagConstraints c = new GridBagConstraints();
	private ChessClockWidget chessClock;
	private JLabel playerTurn;
	private JTextField inputField;
	private JButton undoButton;
	private JButton logButton;
	private JButton highlightButton;
	private JButton nameButton;
	private JButton helpButton;
	private CapturedPiecesWidget captures;
	private final View view;

	public GameInfoPanel(Model game, View view) {
		this.view = view;
		init(game);
	}

	public void updatePlayerTurn(ChessPlayer player) {
		playerTurn.setText(player.getName() + "'s turn.");
		playerTurn.validate();
		playerTurn.repaint();
	}

	public void updatePlayerTurn(Model model) {
		playerTurn.setText(getPlayerTurn(model));
		playerTurn.validate();
		playerTurn.repaint();
	}

	public void updateText(String text) {
		inputField.setText(text);
		inputField.requestFocus();
		inputField.selectAll();
	}

	public void updateAll(Model model) {
		captures.update(model);
		chessClock.update(model);
		updatePlayerTurn(model);
	}

	private void init(Model model) {
		this.setLayout(gridbag);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.weighty = 1;
		c.gridy = 0;
		c.gridx = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new Insets(0, 0, 0, 0);

		playerTurn = new JLabel(getPlayerTurn(model));
		playerTurn.setToolTipText("It is this player's turn");
		c.gridy = 0;
		this.add(playerTurn, c);
		chessClock = new ChessClockWidget();
		c.gridy = 1;
		this.add(chessClock, c);

		JLabel waterMark = new JLabel("\u3020", JLabel.CENTER);
		waterMark.setFont(new Font("Dialog", 1, 160));
		waterMark.setForeground(Color.LIGHT_GRAY);
		waterMark
				.setToolTipText("This is Chad (CHess Associate Director). He takes up space.");
		c.gridy = 2;
		this.add(waterMark, c);

		captures = new CapturedPiecesWidget();
		c.gridy = 3;
		this.add(captures, c);

		inputField = new MoveInput(view);
		c.gridy = 4;
		this.add(inputField, c);
		undoButton = new UndoButton(view);
		c.gridy = 5;
		this.add(undoButton, c);
		logButton = new LogButton(view);
		c.gridy = 6;
		this.add(logButton, c);
		highlightButton = new HighlightButton(view);
		c.gridy = 7;
		this.add(highlightButton, c);
		nameButton = new ChangeNamesButton(view);
		c.gridy = 8;
		this.add(nameButton, c);
		helpButton = new HelpButton(view);
		c.gridy = 9;
		this.add(helpButton, c);
	}

	private String getPlayerTurn(Model model) {
		String turn;
		if (model.isPlayerTurn()) {
			turn = model.getPlayer1().getName();
		} else {
			turn = model.getPlayer2().getName();
		}
		return turn + "'s turn";
	}

}

class ChessClockWidget extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2910956338423285089L;
	private double elapsed;
	private final JLabel clock;
	private ChessTimer timer;

	public ChessClockWidget() {
		this.setLayout(new BorderLayout());
		elapsed = 0.0;
		clock = new JLabel("Chess clock:");
		this.add(clock, BorderLayout.WEST);
		timer = new ChessTimer(this);
		timer.start();
	}

	public void updateElapsed(double delta) {
		elapsed += delta;
		clock.setText("Chess clock: " + Math.round(elapsed * 10) / 10.0);
	}

	public void update(Object arg) {
		timer.halt();
		try {
			timer.join();
		} catch (InterruptedException e1) {
		}
		elapsed = 0;
		timer = new ChessTimer(this);
		timer.start();
	}
}

class ChessTimer extends Thread {
	private boolean done;
	private final ChessClockWidget clock;

	public ChessTimer(ChessClockWidget clock) {
		this.clock = clock;
		done = false;
	}

	public void halt() {
		done = true;
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();
		while (!done) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			long end = System.currentTimeMillis();
			final double delta = ((end - start) / 1000.0);

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					clock.updateElapsed(delta);
				}
			});
			start = end;
		}
		long end = System.currentTimeMillis();
		final double change = ((end - start) / 1000.0);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				clock.updateElapsed(change);
			}
		});
		start = end;
	}
}

class MoveInput extends JTextField implements KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2504643074954351218L;
	private final View view;

	public MoveInput(View view) {
		super("Type a command");
		this.view = view;
		this.selectAll();
		this.addKeyListener(this);
		this.setToolTipText("Type a command");
	}

	@Override
	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			String input = this.getText();
			view.notifyViewListeners(new InputEvent(input, event));
		} else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.setText("Type a command");
			this.selectAll();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}
}

class UndoButton extends JButton implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -923958918462053114L;
	private final View view;

	public UndoButton(View view) {
		this.view = view;
		this.setText("Undo");
		this.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		view.notifyViewListeners(new UndoEvent());
	}
}

class ChangeNamesButton extends JButton implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -911467316404062410L;
	private final View view;

	public ChangeNamesButton(View view) {
		this.view = view;
		this.setText("Change Names");
		this.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		view.notifyViewListeners(new NamesWindowEvent());
	}
}

class LogButton extends JButton implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7157270291503966602L;
	private final View view;

	public LogButton(View view) {
		this.view = view;
		this.setText("Show log");
		this.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		view.notifyViewListeners(new ShowLogEvent());
	}
}

class HighlightButton extends JButton implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 514205102171291926L;
	private final View view;
	private boolean enabled;

	public HighlightButton(View view) {
		this.view = view;
		this.enabled = false;
		this.setText("Show legal moves");
		this.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (!enabled) {
			view.notifyViewListeners(new HighlightEvent(true));
			this.setText("Hide legal moves");
			enabled = true;
		} else {
			view.notifyViewListeners(new HighlightEvent(false));
			this.setText("Show legal moves");
			enabled = false;
		}
	}
}

class HelpButton extends JButton implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4307690287397664315L;
	private final View view;

	public HelpButton(View view) {
		this.view = view;
		this.setText("Help");
		this.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		view.notifyViewListeners(new HelpEvent());
	}
}

class ChessHelpWindow extends JFrame implements HyperlinkListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 103819169517240173L;
	private static ChessHelpWindow oneHelp;
	private JPanel helpPanel;
	private JEditorPane textPane;

	private ChessHelpWindow() {
		// private constructor for the factory pattern
		ChessHelpWindow.oneHelp = this;
		init();
	}

	public static void getChessHelpWindow() {
		// there should only ever be one ChessHelpWindow
		if (oneHelp == null) {
			new ChessHelpWindow();
		}
		oneHelp.pack();
		oneHelp.setVisible(true);
	}

	private void init() {
		this.setTitle("Help");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setLocation(650, 410);
		this.setPreferredSize(new Dimension(375, 300));

		helpPanel = new JPanel();
		helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.PAGE_AXIS));
		JScrollPane scrollPane = new JScrollPane(helpPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		try {
			// Use a JEditorPane because they can hold html files
			textPane = new JEditorPane(Paths.get("src\\ChessHelpSource.html")
					.toUri().toURL());

			textPane.setEditable(false);
			textPane.addHyperlinkListener(this);
			helpPanel.add(textPane);
		} catch (IOException e) {
			e.printStackTrace();
			helpPanel.add(new JLabel("ChessHelpSource.html file is missing."));
			helpPanel
					.add(new JLabel("File should be located in src directory."));
		}

		this.add(scrollPane);
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent arg0) {
		// This updates the textPane when a link is clicked
		if (arg0.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				textPane.setPage(arg0.getURL());
			} catch (IOException ioe) {
				// This shouldn't happen with the help content
			}
		}
	}
}

class ChessLogWindow extends JFrame implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8225812879815018408L;
	private static ChessLogWindow oneLog = null;
	private JPanel logPanel;

	private ChessLogWindow(Model model) {
		ChessLogWindow.oneLog = this;
		model.addObserver(this);
		init(model);
	}

	public static void getChessLog(Model model) {
		if (oneLog == null) {
			oneLog = new ChessLogWindow(model);
		}
		oneLog.pack();
		oneLog.setVisible(true);
	}

	private void buildLogPanel(Model model) {
		int index = 0;
		for (ChessMove l : model.getLog()) {
			JLabel move_label = new JLabel(l.toString());
			logPanel.add(move_label, index);
			index++;
		}
	}

	@Override
	public void update(Observable model, Object arg1) {
		logPanel.removeAll();
		buildLogPanel((Model) model);
		logPanel.validate();
		logPanel.repaint();
	}

	private void init(Model model) {
		this.setTitle("Move log");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setLocation(650, 0);
		this.setPreferredSize(new Dimension(375, 400));

		logPanel = new JPanel();
		logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.PAGE_AXIS));
		buildLogPanel(model);
		this.add(logPanel);
	}

}

class ChangeNameDialog extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3439654734533824249L;
	private final View view;
	private JTextField player1;
	private JTextField player2;

	private ChangeNameDialog(Model model, View view) {
		super(null, "Change Player Names", ModalityType.APPLICATION_MODAL);
		this.view = view;
		init(model);
	}

	public static ChangeNameDialog getChangeNameDialog(Model model, View view) {
		// Since this is a dialog, it is impossible to construct two at the same
		// time, so we do not need to check if an instance of this already
		// exists
		return new ChangeNameDialog(model, view);
	}

	private void init(Model model) {
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setPreferredSize(new Dimension(200, 125));

		JPanel namePanel = new JPanel(new GridLayout(0, 1));
		player1 = new JTextField(model.getPlayer1().getName());
		player2 = new JTextField(model.getPlayer2().getName());

		namePanel.add(new JLabel("Player One:"));
		namePanel.add(player1);
		namePanel.add(new JLabel("Player Two:"));
		namePanel.add(player2);
		mainPanel.add(namePanel, BorderLayout.NORTH);

		player1.selectAll();
		player1.requestFocusInWindow();
		player2.selectAll();

		JPanel buttons = new JPanel(new GridLayout(1, 0));

		JButton ok = new JButton("OK");
		ok.setActionCommand("ok");
		ok.addActionListener(this);

		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);

		buttons.add(ok);
		buttons.add(cancel);
		mainPanel.add(buttons, BorderLayout.SOUTH);
		this.getContentPane().add(mainPanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand() == "ok") {
			view.notifyViewListeners(new ChangeNamesEvent(player1.getText(),
					player2.getText()));
		}
		this.dispose();
	}
}