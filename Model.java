package v1;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Model extends java.util.Observable implements Observer {

	private ArrayList<ChessPiece> piecesOnBoard;
	private ArrayList<ChessPiece> piecesOffBoard;
	private boolean[][] highlightSquares;
	private boolean areMovesHighlighted;
	private ChessPlayer player1;
	private ChessPlayer player2;
	private boolean isPlayerTurn;
	private ArrayList<ChessMove> log = new ArrayList<ChessMove>();

	public Model(String player1Name, String player2Name) {
		isPlayerTurn = true;
		highlightSquares = new boolean[8][8];
		piecesOnBoard = new ArrayList<ChessPiece>();
		piecesOffBoard = new ArrayList<ChessPiece>();
		this.player1 = new ChessPlayer(player1Name);
		this.player2 = new ChessPlayer(player2Name);
		this.player1.addObserver(this);
		this.player2.addObserver(this);

		ArrayList<ChessPiece> pieces = new ArrayList<ChessPiece>();

		pieces.add(new Rook(player1, this, new ChessPosition(0, 0)));
		pieces.add(new Knight(player1, this, new ChessPosition(1, 0)));
		pieces.add(new Bishop(player1, this, new ChessPosition(2, 0)));
		pieces.add(new Queen(player1, this, new ChessPosition(3, 0)));
		pieces.add(new King(player1, this, new ChessPosition(4, 0)));
		pieces.add(new Bishop(player1, this, new ChessPosition(5, 0)));
		pieces.add(new Knight(player1, this, new ChessPosition(6, 0)));
		pieces.add(new Rook(player1, this, new ChessPosition(7, 0)));

		for (int i = 0; i < 8; i++) {
			pieces.add(new Pawn(player1, this, new ChessPosition(i, 1)));
		}

		pieces.add(new Rook(player2, this, new ChessPosition(0, 7)));
		pieces.add(new Knight(player2, this, new ChessPosition(1, 7)));
		pieces.add(new Bishop(player2, this, new ChessPosition(2, 7)));
		pieces.add(new Queen(player2, this, new ChessPosition(3, 7)));
		pieces.add(new King(player2, this, new ChessPosition(4, 7)));
		pieces.add(new Bishop(player2, this, new ChessPosition(5, 7)));
		pieces.add(new Knight(player2, this, new ChessPosition(6, 7)));
		pieces.add(new Rook(player2, this, new ChessPosition(7, 7)));

		for (int i = 0; i < 8; i++) {
			pieces.add(new Pawn(player2, this, new ChessPosition(i, 6)));
		}

		for (ChessPiece piece : pieces) {
			setPieceOnBoard(piece);
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg0 instanceof ChessPlayer) {
			setChanged();
			notifyObservers(arg1);
		} else {
			setChanged();
			notifyObservers(getLog());
		}
	}

	public ArrayList<ChessPiece> getPiecesOnBoard() {
		return piecesOnBoard;
	}

	public ArrayList<ChessPiece> getPiecesOnBoard(ChessPlayer player) {
		// returns enemy pieces
		ArrayList<ChessPiece> pieces = new ArrayList<ChessPiece>();
		for (ChessPiece piece : piecesOnBoard) {
			if (!piece.getOwner().equals(player)) {
				pieces.add(piece);
			}
		}
		return pieces;
	}

	public ChessPiece getPieceOnBoard(ChessPosition position) {
		ChessPiece pieceAtPosition = null;
		for (ChessPiece piece : piecesOnBoard) {
			if (piece.getPosition().equals(position)) {
				pieceAtPosition = piece;
				break;
			}
		}
		return pieceAtPosition;
	}

	public void setPieceOnBoard(ChessPiece piece) {
		if (piece != null) {
			piecesOnBoard.add(piece);
		}
	}

	public void removePieceFromBoard(ChessPiece piece) {
		if (piece != null) {
			piecesOnBoard.remove(piece);
		}
	}

	public void removePieceFromBoard(ChessPosition position) {
		ChessPiece piece = getPieceOnBoard(position);
		if (piece == null) {
			return;
		} else {
			piecesOnBoard.remove(piece);
		}
	}

	public ArrayList<ChessPiece> getPiecesOffBoard() {
		return piecesOffBoard;
	}

	public void setPieceOffBoard(ChessPiece piece) {
		piecesOffBoard.add(piece);
	}

	public void removePieceFromOffBoard(ChessPiece piece) {
		piecesOffBoard.remove(piece);
	}

	private King getKing(ChessPlayer owner) {
		King king = null;
		for (ChessPiece piece : piecesOnBoard) {
			if (piece.isKing() && piece.getOwner().equals(owner)) {
				king = (King) piece;
				break;
			}
		}
		return king;
	}

	public ArrayList<ChessMove> getLog() {
		return log;
	}

	public ChessMove getLog(int n) {
		ChessMove move = null;
		if (n < 0) {
			move = log.get(log.size() - 1);
		}
		return move;
	}

	public int getLogSize() {
		return log.size();
	}

	private ArrayList<ChessPosition> getLegalDestinationsOnBoard(
			ChessPiece piece) {
		ChessPosition position = piece.getPosition();
		ArrayList<ChessPosition> possibleLegalDestinations = piece
				.getLegalDestinations();
		ArrayList<ChessPosition> legalDestinations = new ArrayList<ChessPosition>();
		for (ChessPosition destination : possibleLegalDestinations) {
			try {
				if (position.equals(destination)) {
					// do not add this move to the legal moves list
				} else if (piece.isPathBlocked(destination)) {
					// do not add this move
				} else if (piece.isKing()
						&& Math.abs(position.getX() - destination.getX()) == 2) {
					// Castling case
					King king = (King) piece;
					// check if the king can castle
					ChessPosition corner1 = new ChessPosition(0,
							position.getY());
					ChessPiece rook1 = this.getPieceOnBoard(corner1);
					ChessPosition corner2 = new ChessPosition(7,
							position.getY());
					ChessPiece rook2 = this.getPieceOnBoard(corner2);
					if (rook1 != null
							&& rook1.isRook()
							&& !((Rook) rook1).isMoved()
							&& !king.isPathBlocked(new ChessPosition(0,
									position.getY()))) {
						ChessPosition castle1 = new ChessPosition(2,
								position.getY());
						legalDestinations.add(castle1);
					}
					if (rook2 != null
							&& rook2.isRook()
							&& !((Rook) rook2).isMoved()
							&& !king.isPathBlocked(new ChessPosition(7,
									position.getY()))) {
						ChessPosition castle2 = new ChessPosition(6,
								position.getY());
						legalDestinations.add(castle2);
					}
				} else if (piece.getOwner() != (getPieceOnBoard(destination)
						.getOwner())) {
					// You can't take your own pieces.
					legalDestinations.add(destination);
					// if getOwner == null, this throws a null pointer
					// exception
				}
			} catch (NullPointerException e) {
				// if there is no piece belonging to anyone at the
				// test position this will execute
				legalDestinations.add(destination);
			}
		}
		return legalDestinations;
	}

	private ArrayList<ChessMove> getLegalMoves(ChessPiece piece) {
		ChessPosition from = piece.getPosition();
		ArrayList<ChessPosition> legalDestinations = getLegalDestinationsOnBoard(piece);
		ArrayList<ChessMove> legalMoves = new ArrayList<ChessMove>();
		for (ChessPosition destination : legalDestinations) {
			if (piece.isKing()
					&& Math.abs(from.getX() - destination.getX()) == 2) {
				// this is a castling move;
				if ((destination.getX() - from.getX()) < 0) {
					legalMoves.add(new ChessMove(piece, from,
							ChessMove.MoveType.LONG_CASTLE, destination,
							getPieceOnBoard(destination)));
				} else {
					legalMoves.add(new ChessMove(piece, from,
							ChessMove.MoveType.SHORT_CASTLE, destination,
							getPieceOnBoard(destination)));
				}
			} else if (piece.isPawn()
					&& Math.abs(from.getX() - destination.getX()) == 1
					&& getPieceOnBoard(destination) == null) {
				// this is en passant
				if (piece.getOwner().equals(getPlayer1())) {
					legalMoves.add(new ChessMove(piece, from,
							ChessMove.MoveType.EN_PASSANT, destination,
							getPieceOnBoard(new ChessPosition(destination
									.getX(), destination.getY() - 1))));
				} else {
					legalMoves.add(new ChessMove(piece, from,
							ChessMove.MoveType.EN_PASSANT, destination,
							getPieceOnBoard(new ChessPosition(destination
									.getX(), destination.getY() + 1))));
				}

			} else {
				legalMoves.add(new ChessMove(piece, from, destination,
						getPieceOnBoard(destination)));
			}
		}
		return legalMoves;
	}

	private ArrayList<ChessMove> getChecks(ArrayList<ChessMove> moves) {
		ArrayList<ChessMove> checks = new ArrayList<ChessMove>();
		for (ChessMove move : moves) {
			// set up the conditions
			ChessPlayer owner = move.getPiece().getOwner();
			King king = getKing(owner);
			removePieceFromBoard(move.getCaptured());
			move.getPiece().setPosition(move.getTo());
			// get the enemy pieces on the new board
			ArrayList<ChessPiece> enemyPieces = getPiecesOnBoard(owner);
			// test check
			for (ChessPiece enemyPiece : enemyPieces) {
				ArrayList<ChessMove> enemyMoves = getLegalMoves(enemyPiece);
				for (ChessMove enemyMove : enemyMoves) {
					if (enemyMove.getCaptured() != null
							&& enemyMove.getCaptured().equals(king)) {
						// this is a check, the original move puts the king into
						// check
						checks.add(move);
					}
				}
			}
			// restore the board
			setPieceOnBoard(move.getCaptured());
			move.getPiece().setPosition(move.getFrom());
		}
		return checks;
	}

	public ArrayList<ChessMove> getUnCheckedMoves(ChessPiece piece) {
		ArrayList<ChessMove> movesWithChecks = getLegalMoves(piece);
		ArrayList<ChessMove> checks = getChecks(movesWithChecks);
		movesWithChecks.removeAll(checks);
		return movesWithChecks; // return moves without checks
	}

	public ArrayList<ChessMove> getTurnEnforcedMoves(ChessPiece piece) {
		ArrayList<ChessMove> turnEnforcedMoves = new ArrayList<ChessMove>();
		if (isPlayerTurn() && piece.getOwner().equals(getPlayer1())) {
			turnEnforcedMoves = getUnCheckedMoves(piece);
		} else if (!isPlayerTurn() && piece.getOwner().equals(getPlayer2())) {
			turnEnforcedMoves = getUnCheckedMoves(piece);
		}
		return turnEnforcedMoves;
	}

	public void move(ChessMove move) throws IllegalMove {
		ArrayList<ChessMove> turnEnforcedMoves = getTurnEnforcedMoves(move
				.getPiece());
		for (ChessMove unCheckedMove : turnEnforcedMoves) {
			ChessPosition to = unCheckedMove.getTo();
			ChessPiece capture = unCheckedMove.getCaptured();
			if (move.getTo().equals(to)) {
				ChessPiece piece = unCheckedMove.getPiece();
				if (piece.isKing()) {
					King king = (King) piece;
					try {
						removePieceFromBoard(capture);
						if (capture != null) {
							setPieceOffBoard(capture);
						}
					} catch (NullPointerException e) {
						// no captured piece
					}
					king.setPosition(to);
					// castling
					Rook rook;
					ChessMove rookMove;
					if (unCheckedMove.getMoveType().equals(
							ChessMove.MoveType.LONG_CASTLE)) {
						rook = (Rook) getPieceOnBoard(new ChessPosition(0, king
								.getPosition().getY()));
						rookMove = new ChessMove(
								rook,
								new ChessPosition(0, king.getPosition().getY()),
								ChessMove.MoveType.LONG_CASTLE,
								new ChessPosition(3, king.getPosition().getY()),
								null);
						rook.setPosition(rookMove.getTo());
						rook.setFirstMove(rookMove);
					} else if (unCheckedMove.getMoveType().equals(
							ChessMove.MoveType.SHORT_CASTLE)) {
						rook = (Rook) getPieceOnBoard(new ChessPosition(7, king
								.getPosition().getY()));
						rookMove = new ChessMove(
								rook,
								new ChessPosition(7, king.getPosition().getY()),
								ChessMove.MoveType.SHORT_CASTLE,
								new ChessPosition(5, king.getPosition().getY()),
								null);
						rook.setPosition(rookMove.getTo());
						rook.setFirstMove(rookMove);
					}
					if (king.getFirstMove() == null) {
						king.setFirstMove(unCheckedMove);
					}
					log.add(unCheckedMove);
					invertPlayerTurn();
					setChanged();
					notifyObservers();
					return;
				} else if (piece.isRook()) {
					if (((Rook) piece).getFirstMove() == null) {
						((Rook) piece).setFirstMove(unCheckedMove);
					}
				} else if (piece.isPawn()) {
					if (((Pawn) piece).getFirstMove() == null) {
						((Pawn) piece).setFirstMove(unCheckedMove);
					}
				}
				try {
					removePieceFromBoard(capture);
					if (capture != null) {
						setPieceOffBoard(capture);
					}
				} catch (NullPointerException e) {
					// no captured piece
				}
				piece.setPosition(to);
				invertPlayerTurn();
				if (piece.isPawn() && ((Pawn) piece).isPromotionCondition()) {
					removePieceFromBoard(piece);
					setPieceOnBoard(new Queen(piece.getOwner(), this, to));
					unCheckedMove.setMoveType(ChessMove.MoveType.PROMOTED);
				}
				log.add(unCheckedMove);

				setChanged();
				notifyObservers();
				return;
			}
		}
		throw new IllegalMove(move.getPiece(), move.getFrom(), move.getTo());
	}

	public void getHighlightPositions(ChessPiece piece) {
		ArrayList<ChessMove> turnEnforced = getTurnEnforcedMoves(piece);
		if (areMovesHighlighted) {
			for (ChessMove move : turnEnforced) {
				ChessPosition pos = move.getTo();
				highlightSquares[pos.getX()][pos.getY()] = true;
			}
		}
		setChanged();
		notifyObservers(highlightSquares);
	}

	public void resetHighlightedPositions() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				highlightSquares[j][i] = false;
			}
		}
		setChanged();
		notifyObservers(highlightSquares);
	}

	public void undo() {
		ChessMove lastMove = getLog(-1);
		ChessPosition destination = lastMove.getTo();
		// undo castling
		ChessPosition rookPosition;
		Rook rook;
		ChessPosition currentPosition = destination;
		if (lastMove.getMoveType().equals(ChessMove.MoveType.LONG_CASTLE)) {
			rookPosition = new ChessPosition(3, currentPosition.getY());
			rook = (Rook) getPieceOnBoard(rookPosition);
			// reset the Rook, king is done below
			rook.setPosition(new ChessPosition(0, currentPosition.getY()));
			rook.setFirstMove(null);
		} else if (lastMove.getMoveType().equals(
				ChessMove.MoveType.SHORT_CASTLE)) {
			rookPosition = new ChessPosition(5, currentPosition.getY());
			rook = (Rook) getPieceOnBoard(rookPosition);
			rook.setPosition(new ChessPosition(7, currentPosition.getY()));
			rook.setFirstMove(null);
		} else if (lastMove.getMoveType().equals(ChessMove.MoveType.PROMOTED)) {
			removePieceFromBoard(destination);
			setPieceOnBoard(lastMove.getPiece());
		}
		// handle piece specific undo actions
		King king;
		ChessPiece piece = lastMove.getPiece();
		if (piece.isKing()) {
			king = (King) lastMove.getPiece();
			if (king.getFirstMove().equals(lastMove)) {
				king.setFirstMove(null);
			}
		} else if (piece.isRook()) {
			rook = (Rook) lastMove.getPiece();
			// if this is the rook's first move
			if (rook.getFirstMove().equals(lastMove)) {
				rook.setFirstMove(null);
			}
		} else if (lastMove.getPiece().isPawn()) {
			Pawn pawn = (Pawn) lastMove.getPiece();
			// if this is the pawn's first move
			if (pawn.getFirstMove().equals(lastMove)) {
				pawn.setFirstMove(null);
			}
		}
		lastMove.getPiece().setPosition(lastMove.getFrom());
		if (lastMove.getCaptured() != null) {
			setPieceOnBoard(lastMove.getCaptured());
			removePieceFromOffBoard(lastMove.getCaptured());
		}
		getLog().remove(log.size() - 1);
		invertPlayerTurn();
		setChanged();
		notifyObservers();
	}

	public ChessPlayer getPlayer1() {
		return player1;
	}

	public ChessPlayer getPlayer2() {
		return player2;
	}

	public boolean isPlayerTurn() {
		return isPlayerTurn;
	}

	public void invertPlayerTurn() {
		this.isPlayerTurn = !isPlayerTurn;
	}

	public void setHighlightMoves(boolean b) {
		this.areMovesHighlighted = b;
	}
}

class ChessPlayer extends Observable {
	private String name;

	public ChessPlayer(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		setChanged();
		notifyObservers(this);
	}
}

class ChessMove {
	public enum MoveType {
		MOVE, LONG_CASTLE, SHORT_CASTLE, EN_PASSANT, PROMOTED
	};

	private MoveType type;
	private ChessPiece piece;
	private ChessPosition from;
	private ChessPosition to;
	private ChessPiece captured;

	public ChessMove(ChessPiece piece, ChessPosition from, MoveType type,
			ChessPosition to, ChessPiece captured) {
		this.piece = piece;
		this.from = from;
		this.type = type;
		this.to = to;
		this.captured = captured;
	}

	public ChessMove(ChessPiece piece, ChessPosition from, ChessPosition to,
			ChessPiece captured) {
		this(piece, from, ChessMove.MoveType.MOVE, to, captured);
	}

	public ChessMove(ChessPiece piece, ChessPosition from, ChessPosition to) {
		this(piece, from, ChessMove.MoveType.MOVE, to, null);
	}

	public ChessPiece getPiece() {
		return piece;
	}

	public ChessPosition getFrom() {
		return from;
	}

	public ChessPosition getTo() {
		return to;
	}

	public ChessPiece getCaptured() {
		return captured;
	}

	public boolean pieceWasCaptured() {
		return (captured != null);
	}

	@Override
	public String toString() {
		String moveType;
		switch (type) {
		case LONG_CASTLE:
			moveType = " castled long ";
			break;
		case SHORT_CASTLE:
			moveType = " castled short ";
			break;
		case EN_PASSANT:
			// the French doesn't really fit well with the structure we
			// created...
			moveType = " en passant to ";
			break;
		case PROMOTED:
			moveType = " promoted and moved to ";
			break;
		default:
			moveType = " moved from ";
		}

		String result = piece.getOwner().getName() + "'s " + piece.toString()
				+ moveType + from.toString() + " to " + to.toString();

		if (pieceWasCaptured()) {
			result += " capturing " + captured.getOwner().getName() + "'s "
					+ captured.toString();
		}
		return result;
	}

	public MoveType getMoveType() {
		return type;
	}

	public void setMoveType(MoveType type) {
		this.type = type;
	}
}

class ChessPosition {
	private int x;
	private int y;

	public ChessPosition(int x, int y) {
		if ((x < 0) || (x > 7)) {
			throw new IllegalArgumentException(
					"x value of chess position out of range: " + x);
		}

		if ((y < 0) || (y > 7)) {
			throw new IllegalArgumentException(
					"y value of chess position out of range: " + y);
		}

		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	@Override
	public boolean equals(Object o) {
		ChessPosition p = (ChessPosition) o;
		return ((p.getX() == x) && (p.getY() == y));
	}
}

abstract class ChessPiece extends java.util.Observable {
	protected ChessPlayer owner;
	protected Model game;
	protected ChessPosition position;
	protected char mark;

	protected ChessPiece(ChessPlayer owner, Model game,
			ChessPosition init_position) {
		this.owner = owner;
		this.game = game;
		this.position = init_position;
		addObserver(game);

	}

	public abstract ArrayList<ChessPosition> getLegalDestinations();

	protected boolean isPathBlocked(ChessPosition destination) {
		int xChange = destination.getX() - getPosition().getX();
		int yChange = destination.getY() - getPosition().getY();
		int testX = getPosition().getX();
		int testY = getPosition().getY();
		for (int i = 0; i < Math.max(Math.abs(xChange), Math.abs(yChange)) - 1; i++) {
			// Loop over the spaces that the piece will travel, excluding the
			// last space
			try {
				// Add or subtract one (depending on the direction of xChange)
				testX += xChange / Math.abs(xChange);
			} catch (ArithmeticException e) {
				// If xChange is zero, leave testX as it is.
			}
			try {
				// Add or subtract one (depending on the direction of yChange)
				testY += yChange / Math.abs(yChange);
			} catch (ArithmeticException e) {
				// If yChange is zero, leave testY as it is.
			}
			if (game.getPieceOnBoard(new ChessPosition(testX, testY)) != null) {
				return true;
			}
		}
		return false;
	}

	public boolean isRook() {
		return false;
	}

	public boolean isKnight() {
		return false;
	}

	public boolean isBishop() {
		return false;
	}

	public boolean isKing() {
		return false;
	}

	public boolean isQueen() {
		return false;
	}

	public boolean isPawn() {
		return false;
	}

	public ChessPlayer getOwner() {
		return owner;
	}

	public ChessPosition getPosition() {
		return position;
	}

	protected void setPosition(ChessPosition new_position) {
		position = new_position;
	}

	public char getMark() {
		return mark;
	}
}

class Rook extends ChessPiece {
	private ChessMove firstMove = null; // for castling purposes

	public Rook(ChessPlayer owner, Model game, ChessPosition init_position) {
		super(owner, game, init_position);
		if (owner == game.getPlayer1()) {
			mark = '\u2656';
		} else {
			mark = '\u265C';
		}
	}

	@Override
	public boolean isRook() {
		return true;
	}

	public boolean isMoved() {
		if (firstMove == null) {
			return false;
		}
		return true;
	}

	public void setFirstMove(ChessMove move) {
		this.firstMove = move;
	}

	public ChessMove getFirstMove() {
		return firstMove;
	}

	@Override
	public String toString() {
		return "rook";
	}

	@Override
	public ArrayList<ChessPosition> getLegalDestinations() {
		ArrayList<ChessPosition> destinations = new ArrayList<ChessPosition>();
		int x = position.getX();
		int y = position.getY();
		for (int i = 0; i < 8; i++) {
			if (i != x) {
				ChessPosition destination = new ChessPosition(i, y);
				destinations.add(destination);
			}
			if (i != y) {
				ChessPosition destination = new ChessPosition(x, i);
				destinations.add(destination);
			}
		}
		return destinations;
	}
}

class Bishop extends ChessPiece {
	public Bishop(ChessPlayer owner, Model game, ChessPosition init_position) {
		super(owner, game, init_position);
		if (owner == game.getPlayer1()) {
			mark = '\u2657';
		} else {
			mark = '\u265D';
		}
	}

	@Override
	public boolean isBishop() {
		return true;
	}

	@Override
	public String toString() {
		return "bishop";
	}

	@Override
	public ArrayList<ChessPosition> getLegalDestinations() {
		ArrayList<ChessPosition> destinations = new ArrayList<ChessPosition>();
		int x = position.getX();
		int y = position.getY();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int xChange = Math.abs(i - x);
				int yChange = Math.abs(j - y);
				if (xChange == yChange) {
					ChessPosition destination = new ChessPosition(i, j);
					destinations.add(destination);
				}
			}
		}
		return destinations;
	}
}

class Knight extends ChessPiece {
	public Knight(ChessPlayer owner, Model game, ChessPosition init_position) {
		super(owner, game, init_position);
		if (owner == game.getPlayer1()) {
			mark = '\u2658';
		} else {
			mark = '\u265E';
		}
	}

	@Override
	protected boolean isPathBlocked(ChessPosition destination) {
		return false;
	}

	@Override
	public boolean isKnight() {
		return true;
	}

	@Override
	public String toString() {
		return "knight";
	}

	@Override
	public ArrayList<ChessPosition> getLegalDestinations() {
		ArrayList<ChessPosition> destinations = new ArrayList<ChessPosition>();
		int[] xChanges = { -2, -1, -2, -1, 2, 1, 2, 1 };
		int[] yChanges = { -1, -2, 1, 2, -1, -2, 1, 2 };
		int x = position.getX();
		int y = position.getY();
		for (int i = 0; i < 8; i++) {
			try {
				ChessPosition destination = new ChessPosition(x + xChanges[i],
						y + yChanges[i]);
				destinations.add(destination);
			} catch (IllegalArgumentException e) {
				// if the piece is on the edge of the board, this will happen,
				// do not add this move to the list of possible moves.
			}
		}
		return destinations;
	}
}

class Queen extends ChessPiece {
	public Queen(ChessPlayer owner, Model game, ChessPosition init_position) {
		super(owner, game, init_position);
		if (owner == game.getPlayer1()) {
			mark = '\u2655';
		} else {
			mark = '\u265B';
		}
	}

	@Override
	public boolean isQueen() {
		return true;
	}

	@Override
	public String toString() {
		return "queen";
	}

	@Override
	public ArrayList<ChessPosition> getLegalDestinations() {
		ArrayList<ChessPosition> destinations = new ArrayList<ChessPosition>();
		int x = position.getX();
		int y = position.getY();
		// horizontal moves
		for (int i = 0; i < 8; i++) {
			if (i != x) {
				ChessPosition destination = new ChessPosition(i, y);
				destinations.add(destination);
			}
			if (i != y) {
				ChessPosition destination = new ChessPosition(x, i);
				destinations.add(destination);
			}
		}
		// diagonal moves
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int xChange = Math.abs(i - x);
				int yChange = Math.abs(j - y);
				if (xChange == yChange) {
					ChessPosition destination = new ChessPosition(i, j);
					destinations.add(destination);
				}
			}
		}
		return destinations;
	}
}

class King extends ChessPiece {
	private ChessMove firstMove = null; // For castling purposes

	public King(ChessPlayer owner, Model game, ChessPosition init_position) {
		super(owner, game, init_position);
		if (owner == game.getPlayer1()) {
			mark = '\u2654';
		} else {
			mark = '\u265A';
		}
	}

	@Override
	public boolean isKing() {
		return true;
	}

	public boolean isMoved() {
		if (firstMove == null) {
			return false;
		}
		return true;
	}

	public void setFirstMove(ChessMove move) {
		this.firstMove = move;
	}

	public ChessMove getFirstMove() {
		return firstMove;
	}

	@Override
	public String toString() {
		return "king";
	}

	@Override
	public ArrayList<ChessPosition> getLegalDestinations() {
		ArrayList<ChessPosition> destinations = new ArrayList<ChessPosition>();
		int x = position.getX();
		int y = position.getY();
		if (getFirstMove() == null) {
			// castling. Possibly legal; must be verified in game method
			destinations.add(new ChessPosition(2, y));
			destinations.add(new ChessPosition(6, y));
		}
		// legal destinations
		int[] xChanges = { -1, -1, 0, 1, 1, 1, 0, -1 };
		int[] yChanges = { 0, 1, 1, 1, 0, -1, -1, -1 };
		for (int i = 0; i < 8; i++) {
			try {
				ChessPosition destination = new ChessPosition(x + xChanges[i],
						y + yChanges[i]);
				destinations.add(destination);
			} catch (IllegalArgumentException e) {
				// if the piece is on the edge of the board, this will happen,
				// do not add this move to the list of possible moves.
			}
		}
		return destinations;
	}
}

class Pawn extends ChessPiece {
	private ChessMove firstMove; // to only allow forward movement

	public Pawn(ChessPlayer owner, Model game, ChessPosition init_position) {
		super(owner, game, init_position);
		if (owner == game.getPlayer1()) {
			mark = '\u2659';
		} else {
			mark = '\u265F';
		}
		firstMove = null;
	}

	@Override
	public boolean isPawn() {
		return true;
	}

	public ChessMove getFirstMove() {
		return firstMove;
	}

	public void setFirstMove(ChessMove firstMove) {
		this.firstMove = firstMove;
	}

	@Override
	public String toString() {
		return "pawn";
	}

	public boolean isPromotionCondition() {
		int y = position.getY();
		if (firstMove != null && (y == 7 || y == 0)) {
			// this could display a dialog or something, but for now let's just
			// replace the pawn with a queen
			return true;
		} else {
			return false;
		}
	}

	@Override
	public ArrayList<ChessPosition> getLegalDestinations() {
		ArrayList<ChessPosition> destinations = new ArrayList<ChessPosition>();
		int x = position.getX();
		int y = position.getY();
		// first move case
		if (getFirstMove() == null) {
			if (y == 1) {
				ChessPosition destination = new ChessPosition(x, 3);
				if (game.getPieceOnBoard(destination) == null) {
					destinations.add(destination);
				}
				for (int i = -1; i < 2; i++) {
					try {
						destination = new ChessPosition(x + i, y + 1);
						if (i == 0) {
							if (game.getPieceOnBoard(destination) == null) {
								destinations.add(destination);
							}
						} else {
							if (game.getPieceOnBoard(destination) != null) {
								destinations.add(destination);
							}
						}
					} catch (IllegalArgumentException e) {
						// if the piece is on the edge of the board, this will
						// happen,
						// do not add this move to the list of possible moves.
					}
				}
			} else {
				ChessPosition destination = new ChessPosition(x, 4);
				if (game.getPieceOnBoard(destination) == null) {
					destinations.add(destination);
				}
				for (int i = -1; i < 2; i++) {
					try {
						destination = new ChessPosition(x + i, y - 1);
						if (i == 0) {
							if (game.getPieceOnBoard(destination) == null) {
								destinations.add(destination);
							}
						} else {
							if (game.getPieceOnBoard(destination) != null) {
								destinations.add(destination);
							}
						}
					} catch (IllegalArgumentException e) {
						// if the piece is on the edge of the board, this will
						// happen,
						// do not add this move to the list of possible moves.
					}
				}
			}
		} else {
			// normal moves
			ChessPosition destination;
			for (int i = -1; i < 2; i++) {
				try {
					if (getFirstMove().getFrom().getY() == 1) {
						destination = new ChessPosition(x + i, y + 1);
					} else {
						destination = new ChessPosition(x + i, y - 1);
					}
					if (i == 0) {
						if (game.getPieceOnBoard(destination) == null) {
							destinations.add(destination);
						}
					} else {
						if (game.getPieceOnBoard(destination) != null) {
							destinations.add(destination);
						}
					}
				} catch (IllegalArgumentException e) {
					// if the piece is on the edge of the board, this will
					// happen,
					// do not add this move to the list of possible moves.
				}
			}
		}
		// en passant
		if (game.getLogSize() > 0) {
			ChessMove lastMove = game.getLog(-1);
			ChessPosition destination = lastMove.getTo();
			if (lastMove.getPiece().isPawn()
					&& ((Pawn) lastMove.getPiece()).getFirstMove().equals(
							lastMove)) {
				if (destination.getX() == x + 1 || destination.getX() == x - 1) {
					if (destination.getY() == y) {
						// then this is a legal en Passant;
						if (lastMove.getPiece().getOwner() == game.getPlayer1()) {
							destinations.add(new ChessPosition(destination
									.getX(), destination.getY() - 1));
						} else {
							destinations.add(new ChessPosition(destination
									.getX(), destination.getY() + 1));
						}

					}
				}
			}
		}

		return destinations;
	}
}
