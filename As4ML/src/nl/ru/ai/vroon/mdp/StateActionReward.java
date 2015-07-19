package nl.ru.ai.vroon.mdp;

public class StateActionReward {
	Action	action;
	double	reward;
	
	public StateActionReward(Action action, double reward) {
		this.action = action;
		this.reward = reward;
	}
	
	public Action getAction() {
		return this.action;
	}
	
	public double getReward() {
		return this.reward;
	}
	
	public void setAction(Action action) {
		this.action = action;
	}
	
	public void setReward(double reward) {
		this.reward = reward;
	}
	
}
