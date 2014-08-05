package v1;

public abstract class ChessException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5151904774063632351L;

	protected ChessException(String message) {
		super(message);
	}
}

class IllegalMove extends ChessException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -405583683298031701L;

	public IllegalMove(ChessPiece p, ChessPosition from, ChessPosition to) {
		super("Illegal move: Piece " + p.toString() + " can not move from " + from.toString() + " to " + to.toString());
	}
	
	@Override
	public String toString() {
		return this.getMessage();
	}
}