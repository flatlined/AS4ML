package nl.ru.ai.vroon.mdp;

import java.text.DecimalFormat;
import java.util.Random;

public class Qlearning {
	final DecimalFormat df = new DecimalFormat("#.##");
	
	// path finding
	final double	alpha	= 0.1;
	final double	gamma	= 0.9;
	
	// states A,B,C,D,E,F
	// e.g. from A we can go to B or D
	// from C we can only go to C
	// C is goal state, reward 100 when B->C or F->C
	//
	// _______
	// |A|B|C|
	// |_____|
	// |D|E|F|
	// |_____|
	//
	
	final int	stateA	= 0;
	final int	stateB	= 1;
	final int	stateC	= 2;
	final int	stateD	= 3;
	final int	stateE	= 4;
	final int	stateF	= 5;
	
	final int	statesCount	= 6;
	final int[]	states		= new int[] { this.stateA, this.stateB, this.stateC, this.stateD, this.stateE, this.stateF };
	
	// Q(s,a)= Q(s,a) + alpha * (R(s,a) + gamma * Max(next state, all actions) - Q(s,a))
	// reward lookup
	int[][]		R	= new int[this.statesCount][this.statesCount];
	// Q learning
	double[][]	Q	= new double[this.statesCount][this.statesCount];
	
	int[]	actionsFromA	= new int[] { this.stateB, this.stateD };
	int[]	actionsFromB	= new int[] { this.stateA, this.stateC, this.stateE };
	int[]	actionsFromC	= new int[] { this.stateC };
	int[]	actionsFromD	= new int[] { this.stateA, this.stateE };
	int[]	actionsFromE	= new int[] { this.stateB, this.stateD, this.stateF };
	int[]	actionsFromF	= new int[] { this.stateC, this.stateE };
	int[][]	actions			= new int[][] { this.actionsFromA, this.actionsFromB, this.actionsFromC, this.actionsFromD, this.actionsFromE,
			this.actionsFromF };
			
	String[] stateNames = new String[] { "A", "B", "C", "D", "E", "F" };
	
	public Qlearning() {
		this.init();
	}
	
	public void init() {
		this.R[this.stateB][this.stateC] = 100; // from b to c
		this.R[this.stateF][this.stateC] = 100; // from f to c
	}
	
	public static void main(String[] args) {
		long BEGIN = System.currentTimeMillis();
		
		Qlearning obj = new Qlearning();
		
		obj.run();
		obj.printResult();
		obj.showPolicy();
		
		long END = System.currentTimeMillis();
		System.out.println("Time: " + ( (END - BEGIN) / 1000.0) + " sec.");
	}
	
	void run() {
		/*
		 * 1. Set parameter , and environment reward matrix R
		 * 2. Initialize matrix Q as zero matrix
		 * 3. For each episode: Select random initial state
		 * Do while not reach goal state o
		 * Select one among all possible actions for the current state o
		 * Using this possible action, consider to go to the next state o
		 * Get maximum Q value of this next state based on all possible actions o
		 * Compute o Set the next state as the current state
		 */
		
		// For each episode
		Random rand = new Random();
		for (int i = 0; i < 1000; i++ ) { // train episodes
			// Select random initial state
			int state = rand.nextInt(this.statesCount);
			while (state != this.stateC) // goal state
			{
				// Select one among all possible actions for the current state
				int[] actionsFromState = this.actions[state];
				
				// Selection strategy is random in this example
				int index = rand.nextInt(actionsFromState.length);
				int action = actionsFromState[index];
				
				// Action outcome is set to deterministic in this example
				// Transition probability is 1
				int nextState = action; // data structure
				
				// Using this possible action, consider to go to the next state
				double q = this.Q(state, action);
				double maxQ = this.maxQ(nextState);
				int r = this.R(state, action);
				
				double value = q + (this.alpha * ( (r + (this.gamma * maxQ)) - q));
				this.setQ(state, action, value);
				
				// Set the next state as the current state
				state = nextState;
			}
		}
	}
	
	double maxQ(int s) {
		int[] actionsFromState = this.actions[s];
		double maxValue = Double.MIN_VALUE;
		for (int i = 0; i < actionsFromState.length; i++ ) {
			int nextState = actionsFromState[i];
			double value = this.Q[s][nextState];
			
			if (value > maxValue) {
				maxValue = value;
			}
		}
		return maxValue;
	}
	
	// get policy from state
	int policy(int state) {
		int[] actionsFromState = this.actions[state];
		double maxValue = Double.MIN_VALUE;
		int policyGotoState = state; // default goto self if not found
		for (int i = 0; i < actionsFromState.length; i++ ) {
			int nextState = actionsFromState[i];
			double value = this.Q[state][nextState];
			
			if (value > maxValue) {
				maxValue = value;
				policyGotoState = nextState;
			}
		}
		return policyGotoState;
	}
	
	double Q(int s, int a) {
		return this.Q[s][a];
	}
	
	void setQ(int s, int a, double value) {
		this.Q[s][a] = value;
	}
	
	int R(int s, int a) {
		return this.R[s][a];
	}
	
	void printResult() {
		System.out.println("Print result");
		for (int i = 0; i < this.Q.length; i++ ) {
			System.out.print("out from " + this.stateNames[i] + ":  ");
			for (int j = 0; j < this.Q[i].length; j++ ) {
				System.out.print(this.df.format(this.Q[i][j]) + " ");
			}
			System.out.println();
		}
	}
	
	// policy is maxQ(states)
	void showPolicy() {
		System.out.println("\nshowPolicy");
		for (int i = 0; i < this.states.length; i++ ) {
			int from = this.states[i];
			int to = this.policy(from);
			System.out.println("from " + this.stateNames[from] + " goto " + this.stateNames[to]);
		}
	}
}