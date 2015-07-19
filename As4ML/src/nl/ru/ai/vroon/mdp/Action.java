package nl.ru.ai.vroon.mdp;

public enum Action {
	UP, DOWN, LEFT, RIGHT;
	
	public static Action backAction(Action in) {
		switch (in) {
			case UP:
				return DOWN;
			case DOWN:
				return UP;
			case LEFT:
				return RIGHT;
			case RIGHT:
				return LEFT;
		}
		return null;
	}
	
	public static Action nextAction(Action in) {
		switch (in) {
			case UP:
				return RIGHT;
			case DOWN:
				return LEFT;
			case LEFT:
				return UP;
			case RIGHT:
				return DOWN;
		}
		return null;
	}
	
	public static Action previousAction(Action in) {
		switch (in) {
			case UP:
				return LEFT;
			case DOWN:
				return RIGHT;
			case LEFT:
				return DOWN;
			case RIGHT:
				return UP;
		}
		return null;
	}
}
