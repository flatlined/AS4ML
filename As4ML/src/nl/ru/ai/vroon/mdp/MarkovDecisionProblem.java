package nl.ru.ai.vroon.mdp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

import javax.swing.JFrame;

/**
 * Basic class that contains and displays a Markov Decision Problem with grid
 * positions in a landscape as states.
 * Most stuff can be set manually to create the MDP desired.
 * Also contains and updates an agent that can roam around in the MDP.
 */
public class MarkovDecisionProblem {
	private static Random						rand		= new Random();
	// FIELDS //
	private DrawFrame							frame		= null;
	private ArrayList<ArrayList<FieldPlusVal>>	landscape	= new ArrayList<ArrayList<FieldPlusVal>>();
	
	private int		width, height;
	private int		xPosition, yPosition;
	private int		initXPos, initYPos;
	private int		waittime;
	private int		actionsCounter;
	// probabilities:
	private double	pPerform;
	private double	pSidestep;
	private double	pBackstep;
	private double	pNoStep;
	// rewards, discount, etc
	private double	posReward;
	private double	negReward;
	private double	noReward;
	private double	gamma;
	private double	alpha;
	private double	minChange;
	private double	certainty	= 0.3;
	
	private boolean		showProgress;
	private boolean		isDeterministic;
	private boolean		terminated;
	LinkedList<Integer>	totalSteps	= new LinkedList<Integer>();
	
	public MarkovDecisionProblem() {
		this.defaultSettings();
		
		this.width = 4;
		this.height = 3;
		
		// Make and fill the fields:
		// landscape = new Field[width][height];
		this.initField();
		this.setField(1, 1, Field.OBSTACLE);
		this.setField(3, 1, Field.NEGREWARD);
		this.setField(3, 2, Field.REWARD);
		
		// for (int i = 0; i < height; i++ )
		// for (int j = 0; j < width; j++ )
		// landscape[j][i] = Field.EMPTY;
		// setField(1, 1, Field.OBSTACLE);
		// setField(3, 1, Field.NEGREWARD);
		// setField(3, 2, Field.REWARD);
		
		this.setStateValues();
		
		// Draw yourself:
		this.pDrawMDP();
	}
	
	public MarkovDecisionProblem(int width, int height) {
		this.defaultSettings();
		
		this.width = width;
		this.height = height;
		
		// Make and fill the fields:
		this.initField();
		// landscape = new Field[this.width][this.height];
		// for (int i = 0; i < this.height; i++ )
		// for (int j = 0; j < this.width; j++ )
		// landscape[j][i] = Field.EMPTY;
		//
		this.setField(0, 3, Field.REWARD);
		// this.setStateValues();
		
		this.pDrawMDP();
	}
	
	public Action pickAction(FieldPlusVal theOne) {
		if (rand.nextDouble() < this.certainty) {
			return theOne.getActionPolicy();
		}
		else {
			int index = rand.nextInt(3);
			if (theOne.getActionPolicy().equals(Action.LEFT)) {
				if (index == 0) {
					return Action.RIGHT;
				}
				else if (index == 1) {
					return Action.UP;
				}
				else {
					return Action.DOWN;
				}
			}
			else if (theOne.getActionPolicy().equals(Action.RIGHT)) {
				if (index == 0) {
					return Action.LEFT;
				}
				else if (index == 1) {
					return Action.UP;
				}
				else {
					return Action.DOWN;
				}
			}
			else if (theOne.getActionPolicy().equals(Action.UP)) {
				if (index == 0) {
					return Action.LEFT;
				}
				else if (index == 1) {
					return Action.RIGHT;
				}
				else {
					return Action.DOWN;
				}
			}
			else {
				if (index == 0) {
					return Action.LEFT;
				}
				else if (index == 1) {
					return Action.RIGHT;
				}
				else {
					return Action.UP;
				}
			}
			
		}
	}
	
	public void bestAction() {
		FieldPlusVal curFPV = this.landscape.get(this.xPosition).get(this.yPosition);
		while ( !curFPV.getMyField().equals(Field.REWARD) && ! (curFPV.getMyField().equals(Field.NEGREWARD))) {
			System.out.println(curFPV.getX() + "/" + curFPV.getY() + ":" + curFPV.getActionPolicy());
			this.performAction(curFPV.getActionPolicy());
			curFPV = this.landscape.get(this.xPosition).get(this.yPosition);
		}
	}
	
	public void Qlearn() {
		
		FieldPlusVal curFPV = this.landscape.get(this.xPosition).get(this.yPosition);
		while ( !curFPV.getMyField().equals(Field.REWARD) && ! (curFPV.getMyField().equals(Field.NEGREWARD))) {
			FieldPlusVal oldFPV = this.landscape.get(this.xPosition).get(this.yPosition);
			StateActionReward performed = this.performAction(this.pickAction(curFPV));
			curFPV = this.landscape.get(this.xPosition).get(this.yPosition);
			double curQ; // current Q value for current state/action
			double newQ;
			// ^ calculate the new Q value below: newQ(s,a)= oldQ(s,a) + alpha * (R(s,a) + gamma * Max(next state, all actions) - oldQ(s,a))
			if (performed.getAction().equals(Action.LEFT)) {
				curQ = oldFPV.getActionLeft();
				newQ = curQ + (this.alpha * ( (performed.getReward() + (this.gamma * curFPV.getMyVal())) - curQ));
				
				oldFPV.setActionLeft(newQ);
			}
			else if (performed.getAction().equals(Action.RIGHT)) {
				curQ = oldFPV.getActionRight();
				newQ = curQ + (this.alpha * ( (performed.getReward() + (this.gamma * curFPV.getMyVal())) - curQ));
				oldFPV.setActionRight(newQ);
			}
			else if (performed.getAction().equals(Action.UP)) {
				curQ = oldFPV.getActionUp();
				newQ = curQ + (this.alpha * ( (performed.getReward() + (this.gamma * curFPV.getMyVal())) - curQ));
				oldFPV.setActionUp(newQ);
			}
			else {
				curQ = oldFPV.getActionDown();
				newQ = curQ + (this.alpha * ( (performed.getReward() + (this.gamma * curFPV.getMyVal())) - curQ));
				oldFPV.setActionDown(newQ);
			}
			
			curFPV.setActionPolicy();
			
		}
		// takes the average of the past 3 runs. Unfortunately including the current one
		this.totalSteps.add(this.actionsCounter);
		while (this.totalSteps.size() > 3) {
			this.totalSteps.removeFirst();
		}
		int averageSteps = 0;
		
		for (int i = 0; i < this.totalSteps.size(); i++ ) {
			int nextInt = this.totalSteps.get(i);
			averageSteps += nextInt;
		}
		averageSteps /= this.totalSteps.size();
		if ( (this.certainty < 0.9) && (this.actionsCounter < averageSteps)) {
			this.certainty += 0.0025;
			for (int i = 0; i < this.totalSteps.size(); i++ ) {
			}
		}
		
		// Thread.currentThread();
		// try {
		// Thread.sleep(800);
		// }
		// catch (InterruptedException e) {
		// e.printStackTrace();
		// }
	}
	
	public void calcQ(ArrayList<ArrayList<FieldPlusVal>> copyScape, FieldPlusVal thisOne) {
		if ( !thisOne.getMyField().equals(Field.EMPTY)) {
			thisOne.setActionUp(0);
			thisOne.setActionDown(0);
			thisOne.setActionLeft(0);
			thisOne.setActionRight(0);
		}
		else {
			thisOne.setActionLeft(this.calcQLeft(copyScape, thisOne));
			thisOne.setActionRight(this.calcQRight(copyScape, thisOne));
			thisOne.setActionUp(this.calcQUp(copyScape, thisOne));
			thisOne.setActionDown(this.calcQDown(copyScape, thisOne));
		}
		
	}
	
	public void setStateValues() {
		// for each position:
		// look up/down/left/right, update respective actionval (Q(s,a)
		// Q^k(s,a) = sum_over_s'(T(s,a,s')(R(s,a,s')+lambda(V^k(s'))))
		// update state val V(s)
		// V^k(s) = max_a(Q^k(s,a))
		double change = 0;
		double maxChange = 0;
		int count = 0;
		do {
			maxChange = 0;
			// ArrayList<ArrayList<FieldPlusVal>> copyScape = new ArrayList<ArrayList<FieldPlusVal>>(this.landscape);
			
			ArrayList<ArrayList<FieldPlusVal>> copyScape = new ArrayList<ArrayList<FieldPlusVal>>();
			for (ArrayList<FieldPlusVal> a : this.landscape) {
				ArrayList<FieldPlusVal> temp = new ArrayList<FieldPlusVal>();
				for (FieldPlusVal f : a) {
					FieldPlusVal tempField = new FieldPlusVal(f.getMyField(), f.getMyVal(), f.getActionUp(), f.getActionDown(), f.getActionLeft(),
							f.getActionRight(), f.getX(), f.getY());
					temp.add(tempField);
				}
				copyScape.add(temp);
			}
			
			for (int x = 0; x < this.width; x++ ) {
				for (int y = 0; y < this.height; y++ ) {
					FieldPlusVal theOne = this.landscape.get(x).get(y);
					this.calcQ(copyScape, theOne);
					System.out.println(theOne.toString());
					if (theOne.getMyField() == Field.EMPTY) {
						change = theOne.setActionPolicy();
						maxChange = Math.max(change, maxChange);
					}
					else if (theOne.getMyField().equals(Field.REWARD)) {
						theOne.setMyVal(this.posReward);
					}
					else if (theOne.getMyField().equals(Field.NEGREWARD)) {
						theOne.setMyVal(this.negReward);
					}
					else if (theOne.getMyField().equals(Field.OBSTACLE) || theOne.getMyField().equals(Field.OUTOFBOUNDS)) {
						theOne.setMyVal(0);
					}
					
				}
			}
			// for (int x = 0; x < this.width; x++ ) {
			// for (int y = 0; y < this.height; y++ ) {
			// change = 0;
			// FieldPlusVal theOne = this.landscape.get(x).get(y);
			// double oldVal = theOne.getMyVal();
			// if (theOne.getMyField().equals(Field.REWARD)) {
			// theOne.setMyVal(this.posReward);
			// }
			// else if (theOne.getMyField().equals(Field.NEGREWARD)) {
			// theOne.setMyVal(this.negReward);
			// }
			// else if (theOne.getMyField().equals(Field.OBSTACLE) || theOne.getMyField().equals(Field.OUTOFBOUNDS)) {
			// theOne.setMyVal(0);
			// }
			// else {
			// theOne.updatemyVal();
			// }
			// change = Math.abs(theOne.getMyVal() - oldVal);
			// maxChange = Math.max(change, maxChange);
			// }
			// }
			count++ ;
			System.out.println("\ncount: " + count + "\n");
		} while ( (maxChange >= this.minChange) && (count < 10));
		
	}
	
	public void defaultSettings() {
		
		this.initXPos = 7;
		this.initYPos = 6;
		this.waittime = 100;
		this.actionsCounter = 0;
		
		this.xPosition = this.initXPos;
		this.yPosition = this.initYPos;
		
		this.posReward = 1;
		this.negReward = -1;
		this.noReward = -0.05;
		this.gamma = 0.5;
		this.alpha = 0.1;
		this.minChange = 0.001;
		
		this.actionsCounter = 0;
		this.showProgress = true;
		this.isDeterministic = false;
		this.terminated = false;
		
		if (this.isDeterministic) {
			this.pPerform = 1;
			this.pSidestep = 0;
			this.pBackstep = 0;
			this.pNoStep = 0;
		}
		else {
			this.pPerform = 0.8;
			this.pSidestep = 0.2;
			this.pBackstep = 0;
			this.pNoStep = 0;
		}
	}
	
	public double calcQLeft(ArrayList<ArrayList<FieldPlusVal>> copyScape, FieldPlusVal thisOne) {
		double returnVal = 0;
		int x = thisOne.getX();
		int y = thisOne.getY();
		if (x >= 0) {
			if ( (x == 0) || (copyScape.get(x - 1).get(y).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x - 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x - 1).get(y).getMyVal())));
			}
			
		}
		if (y >= 0) {
			if ( (y == 0) || (copyScape.get(x).get(y - 1).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x).get(y - 1).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += ( (this.pSidestep / 2)
						* (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y - 1).getMyVal())));
			}
			
		}
		if (y <= (this.height - 1)) {
			if ( (y == (this.height - 1)) || (copyScape.get(x).get(y + 1).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x).get(y + 1).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += ( (this.pSidestep / 2)
						* (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y + 1).getMyVal())));
			}
			
		}
		if (x <= (this.width - 1)) {
			if ( (x == (this.width - 1)) || (copyScape.get(x + 1).get(y).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x + 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x + 1).get(y).getMyVal())));
			}
			
		}
		if (true) {
			returnVal += (this.pNoStep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			
		}
		return returnVal;
	}
	
	public double calcQRight(ArrayList<ArrayList<FieldPlusVal>> copyScape, FieldPlusVal thisOne) {
		double returnVal = 0;
		int x = thisOne.getX();
		int y = thisOne.getY();
		if (x <= (this.width - 1)) {
			if ( (x == (this.width - 1)) || (copyScape.get(x + 1).get(y).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x + 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x + 1).get(y).getMyVal())));
			}
			
		}
		if (y >= 0) {
			if ( (y == 0) || (copyScape.get(x).get(y - 1).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x).get(y - 1).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += ( (this.pSidestep / 2)
						* (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y - 1).getMyVal())));
			}
			
		}
		if (y <= (this.height - 1)) {
			if ( (y == (this.height - 1)) || (copyScape.get(x).get(y + 1).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x).get(y + 1).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += ( (this.pSidestep / 2)
						* (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y + 1).getMyVal())));
			}
			
		}
		if (x >= 0) {
			if ( (x == 0) || (copyScape.get(x - 1).get(y).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x - 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x - 1).get(y).getMyVal())));
			}
			
		}
		if (true) {
			returnVal += (this.pNoStep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			
		}
		return returnVal;
	}
	
	public double calcQUp(ArrayList<ArrayList<FieldPlusVal>> copyScape, FieldPlusVal thisOne) {
		double returnVal = 0;
		int x = thisOne.getX();
		int y = thisOne.getY();
		if (y <= (this.height - 1)) {
			if ( (y == (this.height - 1)) || (copyScape.get(x).get(y + 1).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x).get(y + 1).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				double reward = this.getReward(copyScape.get(x).get(y));
				double V = copyScape.get(x).get(y + 1).getMyVal();
				returnVal += (this.pPerform * (reward + (this.gamma * V)));
				// System.out.println("pPer:" + this.pPerform + " rew:" + reward + " dcFct:" + this.discountFact + " V':" + V);
			}
			
		}
		if (x >= 0) {
			if ( (x == 0) || (copyScape.get(x - 1).get(y).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x - 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += ( (this.pSidestep / 2)
						* (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x - 1).get(y).getMyVal())));
			}
			
		}
		if (x <= (this.width - 1)) {
			if ( (x == (this.width - 1)) || (copyScape.get(x + 1).get(y).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x + 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += ( (this.pSidestep / 2)
						* (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x + 1).get(y).getMyVal())));
			}
			
		}
		if (y >= 0) {
			if ( (y == 0) || (copyScape.get(x).get(y - 1).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x).get(y - 1).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y - 1).getMyVal())));
			}
			
		}
		if (true) {
			
			returnVal += (this.pNoStep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			
		}
		return returnVal;
	}
	
	public double calcQDown(ArrayList<ArrayList<FieldPlusVal>> copyScape, FieldPlusVal thisOne) {
		double returnVal = 0;
		int x = thisOne.getX();
		int y = thisOne.getY();
		if (y >= 0) {
			if ( (y == 0) || (copyScape.get(x).get(y - 1).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x).get(y - 1).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				double reward = this.getReward(copyScape.get(x).get(y));
				double V = copyScape.get(x).get(y - 1).getMyVal();
				returnVal += (this.pPerform * (reward + (this.gamma * V)));
				// System.out.println("pPer:" + this.pPerform + " rew:" + reward + " dcFct:" + this.discountFact + " V':" + V);
			}
			
		}
		if (x >= 0) {
			if ( (x == 0) || (copyScape.get(x - 1).get(y).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x - 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += ( (this.pSidestep / 2)
						* (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x - 1).get(y).getMyVal())));
			}
			
		}
		if (x <= (this.width - 1)) {
			if ( (x == (this.width - 1)) || (copyScape.get(x + 1).get(y).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x + 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += ( (this.pSidestep / 2)
						* (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x + 1).get(y).getMyVal())));
			}
			
		}
		if (y <= (this.height - 1)) {
			if ( (y == (this.height - 1)) || (copyScape.get(x).get(y + 1).getMyField() == Field.OBSTACLE)
					|| (copyScape.get(x).get(y + 1).getMyField() == Field.OUTOFBOUNDS)) {
				returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			}
			else {
				returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y + 1).getMyVal())));
			}
			
		}
		if (true) {
			returnVal += (this.pNoStep * (this.getReward(copyScape.get(x).get(y)) + (this.gamma * copyScape.get(x).get(y).getMyVal())));
			
		}
		return returnVal;
	}
	
	private void doAction(Action action) {
		switch (action) {
			case UP:
				this.moveUp();
				break;
			case DOWN:
				this.moveDown();
				break;
			case LEFT:
				this.moveLeft();
				break;
			case RIGHT:
				this.moveRight();
				break;
		}
	}
	
	public int getActionsCounter() {
		return this.actionsCounter;
	}
	
	public FieldPlusVal getFieldPlusVal(int xpos, int ypos) {
		if ( (xpos >= 0) && (xpos < this.width) && (ypos >= 0) && (ypos < this.height)) {
			// return landscape[xpos][ypos];
			return this.landscape.get(xpos).get(ypos);
		}
		else {
			System.err.println("ERROR:MDP:getField:you request a field that does not exist!");
			
			FieldPlusVal errorField = new FieldPlusVal(Field.OUTOFBOUNDS, 0, 0, 0, 0, 0, 0, 0);
			return errorField;
		}
	}
	
	public double getReward() {
		// If we are terminated, no rewards can be gained anymore (i.e. every
		// action is futile):
		if (this.terminated) {
			return 0;
		}
		
		switch (this.landscape.get(this.xPosition).get(this.yPosition).getMyField()) {
			case EMPTY:
				return this.noReward;
			case REWARD:
				this.terminated = true;
				return this.posReward;
			case NEGREWARD:
				this.terminated = true;
				return this.negReward;
			default:
				// If something went wrong:
				System.err.println("ERROR: MDP: getReward(): agent is not in an empty, reward or negreward field...");
				return 0;
		}
		
	}
	
	public double getReward(FieldPlusVal FPV) {
		switch (FPV.getMyField()) {
			case EMPTY:
				return this.noReward;
			case REWARD:
				return this.posReward;
			case NEGREWARD:
				return this.negReward;
			default:
				return 0;
		}
		
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getStateXPosition() {
		return this.xPosition;
	}
	
	public int getStateYPostion() {
		return this.yPosition;
	}
	
	public void initField() {
		for (int i = 0; i < this.width; i++ ) {
			ArrayList<FieldPlusVal> temp = new ArrayList<>();
			this.landscape.add(temp);
			for (int j = 0; j < this.height; j++ ) {
				FieldPlusVal myFieldPlusVal = new FieldPlusVal(Field.EMPTY, 0.0, 0, 0, 0, 0, i, j);
				temp.add(myFieldPlusVal);
			}
		}
	}
	
	private void moveDown() {
		if ( (this.yPosition > 0) && (this.landscape.get(this.xPosition).get(this.yPosition - 1).getMyField() != Field.OBSTACLE)) {
			this.yPosition-- ;
		}
		
	}
	
	private void moveLeft() {
		if ( (this.xPosition > 0) && (this.landscape.get(this.xPosition - 1).get(this.yPosition).getMyField() != Field.OBSTACLE)) {
			this.xPosition-- ;
		}
		
	}
	
	private void moveRight() {
		if ( (this.xPosition < (this.width - 1)) && (this.landscape.get(this.xPosition + 1).get(this.yPosition).getMyField() != Field.OBSTACLE)) {
			this.xPosition++ ;
		}
		
	}
	
	private void moveUp() {
		if ( (this.yPosition < (this.height - 1)) && (this.landscape.get(this.xPosition).get(this.yPosition + 1).getMyField() != Field.OBSTACLE)) {
			this.yPosition++ ;
		}
		
	}
	
	private void pDrawMDP() {
		if (this.showProgress) {
			this.drawMDP();
		}
	}
	
	public StateActionReward performAction(Action action) {
		
		int beginX = this.xPosition;
		int beginY = this.yPosition;
		
		if (this.isDeterministic) {
			this.doAction(action);
		}
		else {
			double prob = MarkovDecisionProblem.rand.nextDouble();
			if (prob < this.pPerform) {
				this.doAction(action);
			}
			else if (prob < (this.pPerform + (this.pSidestep / 2))) {
				this.doAction(Action.previousAction(action));
			}
			else if (prob < (this.pPerform + this.pSidestep)) {
				this.doAction(Action.nextAction(action));
			}
			else if (prob < (this.pPerform + this.pSidestep + this.pBackstep)) {
				this.doAction(Action.backAction(action));
				// else: do nothing (i.e. stay where you are)
			}
		}
		this.actionsCounter++ ;
		this.pDrawMDP();
		int endX = this.xPosition;
		int endY = this.yPosition;
		StateActionReward result = new StateActionReward(beginX, beginY, endX, endY, action, this.getReward());
		// return this.getReward();
		return result;
	}
	
	public void restart() {
		System.out.println(this.actionsCounter + " . " + this.certainty);
		this.defaultSettings();
		this.pDrawMDP();
	}
	
	public void setField(int xpos, int ypos, Field field) {
		if ( (xpos >= 0) && (xpos < this.width) && (ypos >= 0) && (ypos < this.height)) {
			// landscape[xpos][ypos] = field;
			this.landscape.get(xpos).get(ypos).setMyField(field);
			if (field == Field.NEGREWARD) {
				this.landscape.get(xpos).get(ypos).setMyVal(this.negReward);
			}
			else if (field == Field.REWARD) {
				this.landscape.get(xpos).get(ypos).setMyVal(this.posReward);
			}
		}
		// this.pDrawMDP();
	}
	
	public void drawMDP() {
		// (1) sleep
		if (this.showProgress) {
			Thread.currentThread();
			// try {
			// Thread.sleep(25);
			// }
			// catch (InterruptedException e) {
			// e.printStackTrace();
			// }
		}
		
		// (2) repaint
		if (this.frame == null) {
			this.frame = new DrawFrame(this);
			this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.frame.setVisible(true);
		}
		else {
			this.frame.drawContent();
			this.frame.repaint();
		}
	}
	
}
