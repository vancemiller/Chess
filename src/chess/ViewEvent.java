package chess;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

abstract public class ViewEvent {

	public boolean isMoveEvent() {
		return false;
	}

	public boolean isMouseMoveEvent() {
		return false;
	}

	public boolean isInputEvent() {
		return false;
	}

	public boolean isUndoEvent() {
		return false;
	}

	public boolean isNamesWindowEvent() {
		return false;
	}

	public boolean isChangeNamesEvent() {
		return false;
	}

	public boolean isShowLogEvent() {
		return false;
	}
	
	public boolean isHighlightEvent() {
		return false;
	}

	public boolean isHelpEvent() {
		return false;
	}
}

class MoveEvent extends ViewEvent {
	private ChessMove move;

	public MoveEvent(ChessMove move) {
		this.move = move;
	}

	public ChessMove getMove() {
		return move;
	}

	public String getString() {
		return move.toString();
	}

	@Override
	public boolean isMoveEvent() {
		return true;
	}

}

class InputEvent extends ViewEvent {
	private String input;
	private KeyEvent event;

	public InputEvent(String input, KeyEvent event) {
		this.input = input;
		this.event = event;
	}

	@Override
	public String toString() {
		return input;
	}

	@Override
	public boolean isInputEvent() {
		return true;
	}

	public KeyEvent getKeyEvent() {
		return event;
	}
}

class UndoEvent extends ViewEvent {
	public UndoEvent() {
	}

	@Override
	public boolean isUndoEvent() {
		return true;
	}

}

class NamesWindowEvent extends ViewEvent {
	public NamesWindowEvent() {

	}

	@Override
	public boolean isNamesWindowEvent() {
		return true;
	}
}

class ChangeNamesEvent extends ViewEvent {
	private String name1;
	private String name2;
	
	public ChangeNamesEvent(String name1, String name2) {
		this.name1 = name1;
		this.name2 = name2;
	}

	@Override
	public boolean isChangeNamesEvent() {
		return true;
	}

	public String getName1() {
		return name1;
	}

	public String getName2() {
		return name2;
	}
}

class ShowLogEvent extends ViewEvent {
	public ShowLogEvent() {
	}

	@Override
	public boolean isShowLogEvent() {
		return true;
	}
}

class HighlightEvent extends ViewEvent {
	private boolean toggle;
	
	public HighlightEvent(boolean toggle) {
		this.toggle = toggle;
	}
	
	public boolean getToggle() {
		return toggle;
	}
	
	@Override
	public boolean isHighlightEvent() {
		return true;
	}
}

class MouseMoveEvent extends ViewEvent {
	private MouseEvent event;
	private ChessPosition position;
	public enum Type {MOUSE_ENTERED,MOUSE_EXITED,MOUSE_RELEASED}
	private Type type;

	public MouseMoveEvent(MouseEvent e, ChessPosition position, Type type) {
		this.event = e;
		this.position = position;
		this.type = type;
	}

	@Override
	public boolean isMouseMoveEvent() {
		return true;
	}

	public MouseEvent getMouseEvent() {
		return event;
	}

	public ChessPosition getPosition() {
		return position;
	}

	public Type getType() {
		return type;
	}

}

class HelpEvent extends ViewEvent {
	public HelpEvent() {
	}

	@Override
	public boolean isHelpEvent() {
		return true;
	}
}