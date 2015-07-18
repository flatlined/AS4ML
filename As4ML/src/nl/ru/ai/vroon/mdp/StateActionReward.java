package nl.ru.ai.vroon.mdp;

public class StateActionReward {
	int		beginX, beginY, endX, endY;
	Action	action;
	double	reward;
	
	public StateActionReward(int beginX, int beginY, int endX, int endY, Action action, double reward) {
		this.beginX = beginX;
		this.beginY = beginY;
		this.endX = endX;
		this.action = action;
		this.reward = reward;
	}
	
	public int getBeginX() {
		return this.beginX;
	}
	
	public void setBeginX(int beginX) {
		this.beginX = beginX;
	}
	
	public int getBeginY() {
		return this.beginY;
	}
	
	public void setBeginY(int beginY) {
		this.beginY = beginY;
	}
	
	public int getEndX() {
		return this.endX;
	}
	
	public void setEndX(int endX) {
		this.endX = endX;
	}
	
	public int getEndY() {
		return this.endY;
	}
	
	public void setEndY(int endY) {
		this.endY = endY;
	}
	
	public Action getAction() {
		return this.action;
	}
	
	public void setAction(Action action) {
		this.action = action;
	}
	
	public double getReward() {
		return this.reward;
	}
	
	public void setReward(double reward) {
		this.reward = reward;
	}
	
}
