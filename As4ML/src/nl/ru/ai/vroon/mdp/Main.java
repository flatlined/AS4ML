package nl.ru.ai.vroon.mdp;

public class Main {
	
	public static void main(String[] args) {
		
		MarkovDecisionProblem mdp2 = new MarkovDecisionProblem(5, 5, 10, true, true, true);
		for (int i = 0; i < 1000; i++ ) {
			mdp2.runMe();
			mdp2.restart();
		}
		
	}
}
