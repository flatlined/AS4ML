package nl.ru.ai.vroon.mdp;

/**
 * This main is for testing purposes (and to show you how to use the MDP class).
 *
 * @author Jered Vroon
 */
public class Main {
	
	/**
	 * @param args
	 *            , not used
	 */
	public static void main(String[] args) {
		// MarkovDecisionProblem mdp = new MarkovDecisionProblem();
		// // mdp.setInitialState(0, 0);
		// for (int i = 0; i < 5; i++ ) {
		// mdp.bestAction();
		// // mdp.performAction(Action.UP);
		// mdp.restart();
		// }
		
		MarkovDecisionProblem mdp2 = new MarkovDecisionProblem(10, 10);
		for (int i = 0; i < 1000; i++ ) {
			// mdp2.bestAction();
			mdp2.Qlearn();
			// mdp2.performAction(Action.UP);
			mdp2.restart();
		}
		
	}
}
