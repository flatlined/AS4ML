package nl.ru.ai.vroon.mdp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JFrame;

public class MarkovDecisionProblem {
	private static Random						rand		= new Random();
	private DrawFrame							frame		= null;
	private ArrayList<ArrayList<FieldPlusVal>>	landscape	= new ArrayList<ArrayList<FieldPlusVal>>();	// all the fields, coupled with required information
	
	private int			width, height;			// width and height of playing field
	private int			xPosition, yPosition;	// current X and Y position
	private int			initXPos, initYPos;		// initial X and Y position
	private int			waitTime;				// delay between steps
	private int			actionsCounter;			// number of steps taken in this run probabilities:
	private double		pPerform;				// change action succeeds
	private double		pSidestep;				// chance we take a right or left turn instead of intended action
	private double		pBackstep;				// chance we take a step back instead of intended action
	private double		pNoStep;				// chance we stand still rewards, discount, etc
	private double		posReward;				// reward on positive endnode
	private double		negReward;				// reward on negative endnode
	private double		noReward;				// reward/penalty for normal nodes
	private double		gamma;					// discount factor
	private double		alpha;					// weighs importance of old/new states in Q learning
	private double		minChange;				// change required to continue
	private double		certainty;				// likely it is we take the best option in Q learning
	private boolean		knowItAll;				// do we know the map, or need Q learning?
	private boolean		showProgress;			// do we want a gui or not?
	private boolean		isDeterministic;		// do we always take the steps we attempt to take?
	private boolean		terminated;				// are we done yet?
	private boolean		randomStart;			// predetermined start or random?
	LinkedList<Integer>	totalSteps;				// keeps track of the number of steps taken in the last three runs
	
	public MarkovDecisionProblem(boolean knowItAll, boolean isDeterministic, boolean randomStart) {
		this.defaultSettings(8, 8, 10, 10, 250, knowItAll, isDeterministic, randomStart);
		this.setField(1, 1, Field.OBSTACLE);
		this.setField(3, 1, Field.NEGREWARD);
		this.setField(3, 2, Field.REWARD);
		
		if (knowItAll) {// can we use value iteration
			this.setStateValues();
		}
		this.pDrawMDP();
	}
	
	public MarkovDecisionProblem(int width, int height, int waitTime, boolean knowItAll, boolean isDeterministic, boolean randomStart) {
		this.defaultSettings(7, 6, width, height, waitTime, knowItAll, isDeterministic, randomStart);
		this.setField(0, 0, Field.REWARD);
		
		if (knowItAll) {// can we use value iteration
			this.setStateValues();
		}
		this.pDrawMDP();
	}
	
	public void bestAction() {
		FieldPlusVal curFPV = this.landscape.get(this.xPosition).get(this.yPosition);
		while ( !curFPV.getMyField().equals(Field.REWARD) && ! (curFPV.getMyField().equals(Field.NEGREWARD))) {
			System.out.println(curFPV.getX() + "/" + curFPV.getY() + ":" + curFPV.getActionPolicy());
			this.performAction(curFPV.getActionPolicy());
			curFPV = this.landscape.get(this.xPosition).get(this.yPosition);
		}
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
	
	public void defaultSettings(int x, int y, int width, int height, int waitTime, boolean knowItAll, boolean isDeterministic, boolean randomStart) {
		this.totalSteps = new LinkedList<Integer>();
		this.randomStart = randomStart;
		this.knowItAll = knowItAll;
		this.isDeterministic = isDeterministic;
		this.showProgress = true;
		this.terminated = false;
		
		this.waitTime = waitTime;
		this.actionsCounter = 0;
		
		this.posReward = 1;
		this.negReward = -1;
		this.noReward = -0.05;
		
		this.gamma = 0.5;
		this.alpha = 0.1;
		this.minChange = 0.001;
		this.certainty = 0.3;
		
		this.width = width;
		this.height = height;
		if (randomStart) {
			this.initXPos = MarkovDecisionProblem.rand.nextInt(width);
			this.initYPos = MarkovDecisionProblem.rand.nextInt(height);
		}
		else {
			this.initXPos = x;
			this.initYPos = y;
		}
		this.xPosition = this.initXPos;
		this.yPosition = this.initYPos;
		
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
		this.initField();
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
	
	public void drawMDP() {
		// (1) sleep
		if (this.showProgress) {
			Thread.currentThread();
			try {
				Thread.sleep(this.waitTime);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
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
	
	public int getActionsCounter() {
		return this.actionsCounter;
	}
	
	public FieldPlusVal getFieldPlusVal(int xpos, int ypos) {
		if ( (xpos >= 0) && (xpos < this.width) && (ypos >= 0) && (ypos < this.height)) {
			return this.landscape.get(xpos).get(ypos);
		}
		else {
			System.err.println("ERROR:MDP:getField:you request a field that does not exist!");
			FieldPlusVal errorField = new FieldPlusVal(Field.OUTOFBOUNDS, 0, 0, 0, 0, 0, 0, 0);
			return errorField;
		}
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public double getReward() {
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
				// this should only happen if the agent bumps into a wall by misstep
				return this.noReward;
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
	
	public int getStateXPosition() {
		return this.xPosition;
	}
	
	public int getStateYPostion() {
		return this.yPosition;
	}
	
	public int getWidth() {
		return this.width;
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
			}
		}
		this.actionsCounter++ ;
		this.pDrawMDP();
		StateActionReward result = new StateActionReward(action, this.getReward());
		return result;
	}
	
	public Action pickAction(FieldPlusVal theOne) {
		if (MarkovDecisionProblem.rand.nextDouble() < this.certainty) {
			return theOne.getActionPolicy();
		}
		else {
			int index = MarkovDecisionProblem.rand.nextInt(3);
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
	
	public void Qlearn() {
		FieldPlusVal curFPV = this.landscape.get(this.xPosition).get(this.yPosition);
		while ( !curFPV.getMyField().equals(Field.REWARD) && ! (curFPV.getMyField().equals(Field.NEGREWARD))) {
			FieldPlusVal oldFPV = this.landscape.get(this.xPosition).get(this.yPosition);
			StateActionReward performed = this.performAction(this.pickAction(curFPV));
			curFPV = this.landscape.get(this.xPosition).get(this.yPosition);
			double curQ;// current Q value for current state/action
			double newQ;// new Q value calculated below: newQ(s,a)= oldQ(s,a) + alpha * (R(s,a) + gamma * Max(next state, all actions) - oldQ(s,a))
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
		// add steps taken in current run to the list. keep only the 4 most recent
		this.totalSteps.add(this.actionsCounter);
		while (this.totalSteps.size() > 4) {
			this.totalSteps.removeFirst();
		}
		// take the average of the previous 3 runs
		int averageSteps = 0;
		for (int i = 1; i < this.totalSteps.size(); i++ ) {
			int nextInt = this.totalSteps.get(i);
			averageSteps += nextInt;
		}
		averageSteps /= this.totalSteps.size();
		// if the last run took less steps than the average of the three before, the agent becomes more secure, and increases his certainty factor.
		// this modifies exploration vs exploitation in favor of exploitation. this could be improved by making it depend on the score, but this works almost as well.
		if ( (this.certainty < 0.9) && (this.actionsCounter < averageSteps)) {
			this.certainty += 0.0025;
			for (int i = 0; i < this.totalSteps.size(); i++ ) {
			}
		}
	}
	
	public void resetSettings(int x, int y, boolean knowItAll, boolean isDeterministic, boolean randomStart) {
		this.randomStart = randomStart;
		this.knowItAll = knowItAll;
		this.isDeterministic = isDeterministic;
		this.terminated = false;
		this.actionsCounter = 0;
		if (randomStart) {
			this.initXPos = MarkovDecisionProblem.rand.nextInt(this.width);
			this.initYPos = MarkovDecisionProblem.rand.nextInt(this.height);
		}
		else {
			this.initXPos = x;
			this.initYPos = y;
		}
		this.xPosition = this.initXPos;
		this.yPosition = this.initYPos;
		
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
	
	public void restart() {
		System.out.println(this.actionsCounter + " . " + this.certainty);
		this.resetSettings(7, 6, this.knowItAll, this.isDeterministic, this.randomStart);
		this.pDrawMDP();
	}
	
	public void runMe() {
		if (this.knowItAll) {
			this.bestAction();
		}
		else {
			this.Qlearn();
		}
	}
	
	public void setField(int xpos, int ypos, Field field) {
		if ( (xpos >= 0) && (xpos < this.width) && (ypos >= 0) && (ypos < this.height)) {
			this.landscape.get(xpos).get(ypos).setMyField(field);
			if (field == Field.NEGREWARD) {
				this.landscape.get(xpos).get(ypos).setMyVal(this.negReward);
			}
			else if (field == Field.REWARD) {
				this.landscape.get(xpos).get(ypos).setMyVal(this.posReward);
			}
		}
	}
	
	public void setStateValues() {
		// for each position:
		// look up/down/left/right, update respective actionval (Q(s,a)
		// Q^k(s,a) = sum_over_s'(T(s,a,s')(R(s,a,s')+gamma(V^k(s'))))
		// update state val V(s)
		// V^k(s) = max_a(Q^k(s,a))
		double change = 0;
		double maxChange = 0;
		int count = 0;
		do {
			maxChange = 0;
			
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
			count++ ;
		} while ( (maxChange >= this.minChange) && (count < 100));
	}
	
}
