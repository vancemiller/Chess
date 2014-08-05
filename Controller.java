package v1;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;

import javax.swing.BorderFactory;

public class Controller extends Observable implements ViewListener, Observer {

	private Model model;
	private View view;
	private Pattern move_pattern = Pattern
			.compile("\\s*(\\w)\\s*(\\d)\\s*(\\w)\\s*(\\d)");
	private Pattern legacy_move_pattern = Pattern
			.compile("move\\s*\\(\\s*(\\d)\\s*,\\s*(\\d)\\s*\\)\\s*->\\s*\\(\\s*(\\d)\\s*,\\s*(\\d)\\s*\\)");
	private boolean isFirstClick;
	private Object firstEventSource;
	private MouseEvent firstMouseEvent;
	private ChessPosition firstPosition;

	public Controller(Model model, View view) {
		this.model = model;
		this.view = view;
		isFirstClick = true;
		view.addViewListener(this);
		model.addObserver(this);
		addObserver(view);
	}

	@Override
	public void handleChessViewEvent(ViewEvent e) {
		if (e.isMouseMoveEvent()) {
			handleMouseEvent(e);
		} else if (e.isUndoEvent()) {
			handleUndoEvent();
		} else if (e.isInputEvent()) {
			handleInputEvent(e);
		} else if (e.isChangeNamesEvent()) {
			handleChangeNamesEvent(e);
		} else if (e.isShowLogEvent()) {
			handleLogEvent();
		} else if (e.isHighlightEvent()) {
			handleHighlightEvent(e);
		} else if (e.isHelpEvent()) {
			handleHelpEvent();
		} else if (e.isNamesWindowEvent()) {
			handleNamesWindowEvent();
		}
	}

	private void handleMouseEvent(ViewEvent e) {
		MouseMoveEvent moveEvent = ((MouseMoveEvent) e);
		MouseEvent mouseEvent = moveEvent.getMouseEvent();
		if (moveEvent.getType().equals(MouseMoveEvent.Type.MOUSE_ENTERED)) {
			if (isFirstClick) {
				mouseEvent.getComponent().setForeground(Color.ORANGE);
			} else if (mouseEvent.getSource() != firstEventSource) {
				((JButton) mouseEvent.getComponent()).setBorder(BorderFactory
						.createLineBorder(Color.ORANGE, 2, true));
			}
		} else if (moveEvent.getType().equals(MouseMoveEvent.Type.MOUSE_EXITED)) {
			if (mouseEvent.getSource() != firstEventSource) {
				mouseEvent.getComponent().setForeground(null);
				((JButton) mouseEvent.getComponent()).setBorder(BorderFactory
						.createEtchedBorder());
			}
		} else {
			ChessPosition position = ((MouseMoveEvent) e).getPosition();
			if (isFirstClick) {
				firstPosition = position;
				firstMouseEvent = mouseEvent;
				firstEventSource = mouseEvent.getSource();
				if (model.getPieceOnBoard(firstPosition) != null) {
					mouseEvent.getComponent().setForeground(Color.GREEN);
					isFirstClick = false;
					model.getHighlightPositions(model.getPieceOnBoard(position));
				}
			} else {
				model.resetHighlightedPositions();
				((GameSquareWidget) mouseEvent.getSource())
						.setBorder(BorderFactory.createEtchedBorder());
				firstMouseEvent.getComponent().setForeground(null);
				if (firstEventSource != mouseEvent.getSource()) {
					// if the user clicked a square that was not the first
					// square
					// attempt to move the piece, else reset the board
					ChessPosition first = firstPosition;
					ChessPosition second = position;
					setTextField("Type a command");
					handleAttemptedMove(new ChessMove(
							model.getPieceOnBoard(first), first, second));
				}
				firstMouseEvent = null;
				firstEventSource = null;
				isFirstClick = true;
			}
		}
	}

	private void handleAttemptedMove(ChessMove chessMove) {
		try {
			model.move(chessMove);
		} catch (IllegalMove e) {
			setTextField("Illegal Move");
			firstMouseEvent = null;
			firstEventSource = null;
			isFirstClick = true;
		}
	}

	private void handleUndoEvent() {
		if (!isFirstClick) {
			// undo the current piece selection
			model.resetHighlightedPositions();
			firstMouseEvent.getComponent().setForeground(null);
			firstMouseEvent = null;
			firstEventSource = null;
			isFirstClick = true;
			setTextField("Undid piece selection");
			return;
		}
		if (model.getLogSize() == 0) {
			setTextField("Nothing to undo");
			return;
		}
		// now, to undo the last move:
		model.undo();
		setTextField("Undone");
	}

	private void handleInputEvent(ViewEvent e) {
		String input = ((InputEvent) e).toString();
		Matcher matcher = move_pattern.matcher(input);
		Matcher matcher2 = legacy_move_pattern.matcher(input);
		try {
			if (matcher.matches()) {
				ChessPosition from = convert(input, true, matcher);
				ChessPosition to = convert(input, false, matcher);
				ChessPiece piece = model.getPieceOnBoard(from);
				setTextField(input);
				handleAttemptedMove(new ChessMove(piece, from, to));
				return;
			} else if (matcher2.matches()) {
				ChessPosition from = new ChessPosition(
						Integer.parseInt(matcher2.group(1)),
						Integer.parseInt(matcher2.group(2)));
				ChessPosition to = new ChessPosition(Integer.parseInt(matcher2
						.group(3)), Integer.parseInt(matcher2.group(4)));
				setTextField(input);
				handleAttemptedMove(new ChessMove(model.getPieceOnBoard(from),
						from, to));
				return;
			} else if (input.equalsIgnoreCase("undo")) {
				handleUndoEvent();
				// text field modification happens in undo event handler
				return;
			} else if (input.equalsIgnoreCase("quit")) {
				setTextField("Type 'please quit', please.");
			} else if (input.equalsIgnoreCase("please quit")) {
				System.exit(0);
			} else if (input.equalsIgnoreCase("log")) {
				handleLogEvent();
				return;
			} else if (input.equalsIgnoreCase("help")) {
				handleHelpEvent();
			} else if (input.equalsIgnoreCase("names")) {
				handleNamesWindowEvent();
			} else {
				setTextField("Invalid command");
			}
			// Exceptions thrown by the convert methods for illegal move input
		} catch (NullPointerException e2) {
			setTextField("Invalid index");
		} catch (IllegalArgumentException e3) {
			setTextField("Invalid input.  Try: a2 a4");
		}
	}

	private ChessPosition convert(String input, boolean isFrom, Matcher matcher) {
		char x;
		int y;
		if (isFrom) {
			x = matcher.group(1).charAt(0);
			y = Integer.parseInt(matcher.group(2)) - 1;
		} else {
			x = matcher.group(3).charAt(0);
			y = Integer.parseInt(matcher.group(4)) - 1;
		}
		switch (x) {
		case 'a':
			x = 0;
			break;
		case 'b':
			x = 1;
			break;
		case 'c':
			x = 2;
			break;
		case 'd':
			x = 3;
			break;
		case 'e':
			x = 4;
			break;
		case 'f':
			x = 5;
			break;
		case 'g':
			x = 6;
			break;
		case 'h':
			x = 7;
			break;
		default:
			// else, the character is not a valid index and points to a location
			// that does not exist.
			// null pointers are handled in handleInputEvent method
			throw new NullPointerException();
		}
		try {
			return new ChessPosition(x, y);
		} catch (IllegalArgumentException e) {
			throw new NullPointerException();
		} catch (NullPointerException e) {
			throw new NullPointerException();
		}
	}

	private void handleNamesWindowEvent() {
		ChangeNameDialog.getChangeNameDialog(model, view);
		setTextField("Type a command");
	}

	private void handleChangeNamesEvent(ViewEvent e) {
		ChangeNamesEvent event = (ChangeNamesEvent) e;
		model.getPlayer1().setName(event.getName1());
		model.getPlayer2().setName(event.getName2());
	}

	private void handleLogEvent() {
		ChessLogWindow.getChessLog(model);
		// Keep the focus on the inputField
		setTextField("Type a command");
	}

	private void handleHighlightEvent(ViewEvent e) {
		HighlightEvent event = (HighlightEvent) e;
		if (event.getToggle()) {
			model.setHighlightMoves(true);
		} else {
			model.setHighlightMoves(false);
		}
	}
	
	private void handleHelpEvent() {
		ChessHelpWindow.getChessHelpWindow();
		setTextField("Type a command");
	}

	private void setTextField(String text) {
		setChanged();
		notifyObservers(text);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// all changes in the model pass through here and are propagated up to
		// the view;
		setChanged();
		notifyObservers(arg1);
	}
}
