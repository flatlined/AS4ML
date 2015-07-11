package nl.ru.ai.vroon.mdp;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

/**
 * Basic class that contains and displays a Markov Decision Problem with grid
 * positions in a landscape as states.
 * Most stuff can be set manually to create the MDP desired.
 * Also contains and updates an agent that can roam around in the MDP.
 */
public class MarkovDecisionProblem {
	// FIELDS //
	private DrawFrame							frame		= null;
	private ArrayList<ArrayList<FieldPlusVal>>	landscape	= new ArrayList<ArrayList<FieldPlusVal>>();
	private static Random						rand		= new Random();

	private int									width, height;
	private int									xPosition, yPosition;
	private int									initXPos, initYPos;
	private int									waittime;
	private int									actionsCounter;
	// probabilities:
	private double								pPerform;
	private double								pSidestep;
	private double								pBackstep;
	private double								pNoStep;
	// rewards, discount, etc
	private double								posReward;
	private double								negReward;
	private double								noReward;
	private double								discountFact;
	private double								minChange;

	private boolean								showProgress;
	private boolean								isDeterministic;
	private boolean								terminated;

	// FUNCTIONS //

	/**
	 * Constructor. Constructs a basic MDP (the one described in Chapter 17 of Russell & Norvig)
	 */
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

	/**
	 * Constructs a basic MDP with the given width and height.
	 * All fields are set to Field.EMPTY.
	 * All other settings are the same as in the MDP described in Chapter 17 of
	 * Russell & Norvig
	 *
	 * @param width
	 * @param height
	 */
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
		this.setStateValues();

		this.pDrawMDP();
	}

	public void bestAction() {
		FieldPlusVal curFPV = this.landscape.get(this.xPosition).get(this.yPosition);
		while (curFPV.getMyField() == Field.EMPTY) {
			System.out.println(curFPV.getActionPolicy());
			this.performAction(curFPV.getActionPolicy());
			curFPV = this.landscape.get(this.xPosition).get(this.yPosition);
		}
	}

	public void calcQ(ArrayList<ArrayList<FieldPlusVal>> copyScape, FieldPlusVal thisOne, int x, int y) {
		// System.out.println("L:" + thisOne.getActionLeft() + "R:" + thisOne.getActionRight() + "U:" + thisOne.getActionUp() + "D:"
		// + thisOne.getActionDown());
		if ( !thisOne.getMyField().equals(Field.EMPTY)) {
			thisOne.setActionUp(0);
			thisOne.setActionDown(0);
			thisOne.setActionLeft(0);
			thisOne.setActionRight(0);
		}
		else {
			double returnVal = 0;
			// for action=left
			if (x >= 0) {
				if ( (x == 0) || (copyScape.get(x - 1).get(y).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x - 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x)
							.get(y).getMyVal())));
				}
				else {
					returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x - 1)
							.get(y).getMyVal())));
				}

			}
			if (y >= 0) {
				if ( (y == 0) || (copyScape.get(x).get(y - 1).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x).get(y - 1).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y).getMyVal())));
				}
				else {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y - 1).getMyVal())));
				}

			}
			if (y <= (this.height - 1)) {
				if ( (y == (this.height - 1)) || (copyScape.get(x).get(y + 1).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x).get(y + 1).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y).getMyVal())));
				}
				else {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y + 1).getMyVal())));
				}

			}
			if (x <= (this.width - 1)) {
				if ( (x == (this.width - 1)) || (copyScape.get(x + 1).get(y).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x + 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x)
							.get(y).getMyVal())));
				}
				else {
					returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x + 1)
							.get(y).getMyVal())));
				}

			}
			if (true) {
				returnVal += (this.pNoStep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x).get(y)
						.getMyVal())));

			}
			thisOne.setActionLeft(returnVal);
			returnVal = 0;
			// for action=right
			if (x <= (this.width - 1)) {
				if ( (x == (this.width - 1)) || (copyScape.get(x + 1).get(y).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x + 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x)
							.get(y).getMyVal())));
				}
				else {
					returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x + 1)
							.get(y).getMyVal())));
				}

			}
			if (y >= 0) {
				if ( (y == 0) || (copyScape.get(x).get(y - 1).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x).get(y - 1).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y).getMyVal())));
				}
				else {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y - 1).getMyVal())));
				}

			}
			if (y <= (this.height - 1)) {
				if ( (y == (this.height - 1)) || (copyScape.get(x).get(y + 1).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x).get(y + 1).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y).getMyVal())));
				}
				else {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y + 1).getMyVal())));
				}

			}
			if (x >= 0) {
				if ( (x == 0) || (copyScape.get(x - 1).get(y).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x - 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x)
							.get(y).getMyVal())));
				}
				else {
					returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x - 1)
							.get(y).getMyVal())));
				}

			}
			if (true) {
				returnVal += (this.pNoStep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x).get(y)
						.getMyVal())));

			}
			thisOne.setActionRight(returnVal);
			returnVal = 0;
			// for action=up
			if (y <= (this.height - 1)) {
				if ( (y == (this.height - 1)) || (copyScape.get(x).get(y + 1).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x).get(y + 1).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x)
							.get(y).getMyVal())));
				}
				else {
					double reward = this.getReward(copyScape.get(x).get(y).getMyField());
					double V = copyScape.get(x).get(y + 1).getMyVal();
					returnVal += (this.pPerform * (reward + (this.discountFact * V)));
					// System.out.println("pPer:" + this.pPerform + " rew:" + reward + " dcFct:" + this.discountFact + " V':" + V);
				}

			}
			if (x >= 0) {
				if ( (x == 0) || (copyScape.get(x - 1).get(y).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x - 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y).getMyVal())));
				}
				else {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x - 1).get(y).getMyVal())));
				}

			}
			if (x <= (this.width - 1)) {
				if ( (x == (this.width - 1)) || (copyScape.get(x + 1).get(y).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x + 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y).getMyVal())));
				}
				else {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x + 1).get(y).getMyVal())));
				}

			}
			if (y >= 0) {
				if ( (y == 0) || (copyScape.get(x).get(y - 1).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x).get(y - 1).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x)
							.get(y).getMyVal())));
				}
				else {
					returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x)
							.get(y - 1).getMyVal())));
				}

			}
			if (true) {

				returnVal += (this.pNoStep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x).get(y)
						.getMyVal())));

			}
			thisOne.setActionUp(returnVal);
			returnVal = 0;
			// for action=down
			if (y >= 0) {
				if ( (y == 0) || (copyScape.get(x).get(y - 1).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x).get(y - 1).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += (this.pPerform * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x)
							.get(y).getMyVal())));
				}
				else {
					double reward = this.getReward(copyScape.get(x).get(y).getMyField());
					double V = copyScape.get(x).get(y - 1).getMyVal();
					returnVal += (this.pPerform * (reward + (this.discountFact * V)));
					// System.out.println("pPer:" + this.pPerform + " rew:" + reward + " dcFct:" + this.discountFact + " V':" + V);
				}

			}
			if (x >= 0) {
				if ( (x == 0) || (copyScape.get(x - 1).get(y).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x - 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y).getMyVal())));
				}
				else {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x - 1).get(y).getMyVal())));
				}

			}
			if (x <= (this.width - 1)) {
				if ( (x == (this.width - 1)) || (copyScape.get(x + 1).get(y).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x + 1).get(y).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x).get(y).getMyVal())));
				}
				else {
					returnVal += ( (this.pSidestep / 2) * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape
							.get(x + 1).get(y).getMyVal())));
				}

			}
			if (y <= (this.height - 1)) {
				if ( (y == (this.height - 1)) || (copyScape.get(x).get(y + 1).getMyField() == Field.OBSTACLE)
						|| (copyScape.get(x).get(y + 1).getMyField() == Field.OUTOFBOUNDS)) {
					returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x)
							.get(y).getMyVal())));
				}
				else {
					returnVal += (this.pBackstep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x)
							.get(y + 1).getMyVal())));
				}

			}
			if (true) {
				returnVal += (this.pNoStep * (this.getReward(copyScape.get(x).get(y).getMyField()) + (this.discountFact * copyScape.get(x).get(y)
						.getMyVal())));

			}
			thisOne.setActionDown(returnVal);
		}

	}

	/**
	 * Sets most parameters (except for the landscape, its width and height) to
	 * their default value
	 */
	public void defaultSettings() {

		this.initXPos = 7;
		this.initYPos = 6;
		this.waittime = 500;
		this.actionsCounter = 0;

		this.xPosition = this.initXPos;
		this.yPosition = this.initYPos;

		this.posReward = 1;
		this.negReward = -1;
		this.noReward = -0.05;
		this.discountFact = 0.5;
		this.minChange = 0.001;

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

	/**
	 * Executes the given action as is
	 * (i.e. translates Action to an actual function being performed)
	 *
	 * @param action
	 */
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

	/**
	 * Draws this MDP. If showProgress is set to true called by MDP every time
	 * something changes.
	 * In that case also waits the waittime.
	 */
	public void drawMDP() {
		// (1) sleep
		if (this.showProgress) {
			Thread.currentThread();
			try {
				Thread.sleep(this.waittime);
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

	/**
	 * Returns the number of actions that has been performed since the last
	 * (re)start.
	 *
	 * @return
	 */
	public int getActionsCounter() {
		return this.actionsCounter;
	}

	/**
	 * Returns the field with the given x and y coordinates
	 *
	 * @param xpos
	 *            , should fall within the landscape
	 * @param ypos
	 *            , should fall within the landscape
	 * @return
	 */
	public Field getField(int xpos, int ypos) {
		if ( (xpos >= 0) && (xpos < this.width) && (ypos >= 0) && (ypos < this.height)) {
			// return landscape[xpos][ypos];
			return this.landscape.get(xpos).get(ypos).getMyField();
		}
		else {
			System.err.println("ERROR:MDP:getField:you request a field that does not exist!");
			return Field.OUTOFBOUNDS;
		}
	}

	public FieldPlusVal getFieldPlusVal(int xpos, int ypos) {
		if ( (xpos >= 0) && (xpos < this.width) && (ypos >= 0) && (ypos < this.height)) {
			// return landscape[xpos][ypos];
			return this.landscape.get(xpos).get(ypos);
		}
		else {
			System.err.println("ERROR:MDP:getField:you request a field that does not exist!");

			FieldPlusVal errorField = new FieldPlusVal(Field.OUTOFBOUNDS, 0, 0, 0, 0, 0);
			return errorField;
		}
	}

	/**
	 * Returns the height of the landscape
	 *
	 * @return
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Returns the reward the field in which the agent currently is yields
	 *
	 * @return a double (can be negative)
	 */
	public double getReward() {
		// If we are terminated, no rewards can be gained anymore (i.e. every
		// action is futile):
		if (this.terminated) {
			return 0;
		}

		// switch (landscape[xPosition][yPosition]) {
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

	/**
	 * Returns the reward for a given field
	 *
	 * @return a double (can be negative)
	 */
	public double getReward(Field field) {
		switch (field) {
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

	/**
	 * Returns the x-position of the current state
	 *
	 * @return a number between 0 and width
	 */
	public int getStateXPosition() {
		return this.xPosition;
	}

	/**
	 * Returns the y-position of the current state
	 *
	 * @return a number between 1 and height
	 */
	public int getStateYPostion() {
		return this.yPosition;
	}

	/**
	 * Returns the width of the landscape
	 *
	 * @return
	 */
	public int getWidth() {
		return this.width;
	}

	// ///////////////////////////////////////////////////////
	// / SETTERS
	// ///////////////////////////////////////////////////////

	public void initField() {
		for (int i = 0; i < this.width; i++ ) {
			ArrayList<FieldPlusVal> temp = new ArrayList<>();
			this.landscape.add(temp);
			for (int j = 0; j < this.height; j++ ) {
				FieldPlusVal myFieldPlusVal = new FieldPlusVal(Field.EMPTY, 0.0, 0, 0, 0, 0);
				temp.add(myFieldPlusVal);
			}
		}
	}

	/**
	 * Returns if this MDP is deterministic or stochastic
	 *
	 * @return
	 */
	public boolean isDeterministic() {
		return this.isDeterministic;
	}

	/**
	 * sets the default state for the agent (used in restart() )
	 *
	 * @param xpos
	 * @param ypos
	 */
	// public void setInitialState(int xpos, int ypos) {
	// this.initXPos = xpos;
	// this.initYPos = ypos;
	// }

	/**
	 * Returns if the MDP has been terminated (i.e. a final state has been
	 * reached)
	 *
	 * @return
	 */
	public boolean isTerminated() {
		return this.terminated;
	}

	/**
	 * Moves the agent down (if possible).
	 */
	private void moveDown() {
		// if (yPosition > 0 && landscape[xPosition][yPosition - 1] != Field.OBSTACLE)
		if ( (this.yPosition > 0) && (this.landscape.get(this.xPosition).get(this.yPosition - 1).getMyField() != Field.OBSTACLE)) {
			this.yPosition-- ;
		}

	}

	/**
	 * Moves the agent left (if possible).
	 */
	private void moveLeft() {
		// if (xPosition > 0 && landscape[xPosition - 1][yPosition] != Field.OBSTACLE)
		if ( (this.xPosition > 0) && (this.landscape.get(this.xPosition - 1).get(this.yPosition).getMyField() != Field.OBSTACLE)) {
			this.xPosition-- ;
		}

	}

	/**
	 * Moves the agent right (if possible).
	 */
	private void moveRight() {
		// if (xPosition < (width - 1) && landscape[xPosition + 1][yPosition] != Field.OBSTACLE)
		if ( (this.xPosition < (this.width - 1)) && (this.landscape.get(this.xPosition + 1).get(this.yPosition).getMyField() != Field.OBSTACLE)) {
			this.xPosition++ ;
		}

	}

	/**
	 * Moves the agent up (if possible).
	 */
	private void moveUp() {
		// if (yPosition < (height - 1) && landscape[xPosition][yPosition + 1] != Field.OBSTACLE)
		if ( (this.yPosition < (this.height - 1)) && (this.landscape.get(this.xPosition).get(this.yPosition + 1).getMyField() != Field.OBSTACLE)) {
			this.yPosition++ ;
		}

	}

	// ///////////////////////////////////////////////////////
	// / GETTERS
	// ///////////////////////////////////////////////////////

	/**
	 * Private method used to have this MDP draw itself only if it should show
	 * its progress.
	 */
	private void pDrawMDP() {
		if (this.showProgress) {
			this.drawMDP();
		}
	}

	/**
	 * Performs the given action and returns the reward that action yielded.
	 * However, keep in mind that, if this MDP is non-deterministic, the given
	 * action
	 * need not be executed - another action could be executed as well.
	 *
	 * @param action
	 *            , the Action that is _intended_ to be executed
	 * @return the reward the agent gains at its new state
	 */
	public double performAction(Action action) {
		// If we are working deterministically, the action is performed
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
		return this.getReward();
	}

	/**
	 * sets the agent back to its default state and sets terminated to false.
	 */
	public void restart() {
		System.out.println(this.actionsCounter);
		// this.terminated = false;
		// this.xPosition = this.initXPos;
		// this.yPosition = this.initYPos;
		// this.actionsCounter = 0;
		this.defaultSettings();
		this.pDrawMDP();
	}

	/**
	 * makes this MDP deterministic or stochastically
	 */
	public void setDeterministic(boolean isDeterministic) {
		this.isDeterministic = isDeterministic;
	}

	/**
	 * Sets the field with the given x and y coordinate to the given field.
	 * Updates the visual display.
	 *
	 * @param xpos
	 * @param ypos
	 * @param field
	 */
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
		this.pDrawMDP();
	}

	/**
	 * Setter to set the reward given when a Field.NEGREWARD is reached
	 *
	 * @param posReward
	 */
	public void setNegReward(double negReward) {
		this.negReward = negReward;
	}

	/**
	 * Setter to set the reward given when a Field.EMPTY is reached
	 *
	 * @param posReward
	 */
	public void setNoReward(double noReward) {
		this.noReward = noReward;
	}

	/**
	 * Setter to set the reward given when a Field.REWARD is reached
	 *
	 * @param posReward
	 */
	public void setPosReward(double posReward) {
		this.posReward = posReward;
	}

	/**
	 * Setter to set the probabilities for all (mis)interpretations of a
	 * to-be-performed action.
	 * The given probabilities should add up to 1.
	 *
	 * @param pPerform
	 *            , the probability an action is performed as is (e.g. UP is
	 *            executed as UP)
	 * @param pSidestep
	 *            , the probability a sidestep is performed (e.g. UP is executed
	 *            as LEFT or RIGHT)
	 * @param pBackstep
	 *            , the probability a backstep is performed (e.g. UP is executed
	 *            as DOWN)
	 * @param pNoStep
	 *            , the probability an action is not performed at all (e.g. UP
	 *            is not executed)
	 */
	public void setProbsStep(double pPerform, double pSidestep, double pBackstep, double pNoStep) {
		double total = pPerform + pSidestep + pBackstep + pNoStep;
		if (total == 1.0) {
			System.err.println("ERROR: MDP: setProbsStep: given probabilities do not add up to 1. I will normalize to compensate.");
		}
		this.pPerform = pPerform / total;
		this.pSidestep = pSidestep / total;
		this.pBackstep = pBackstep / total;
		this.pNoStep = pNoStep / total;
	}

	// ///////////////////////////////////////////////////////
	// / DISPLAY STUFF
	// ///////////////////////////////////////////////////////

	/**
	 * Setter to enable/disable the showing of the progress on the display
	 *
	 * @param show
	 */
	public void setShowProgress(boolean show) {
		this.showProgress = show;
	}

	/**
	 * Moves the agent to the given state (x and y coordinate)
	 *
	 * @param xpos
	 * @param ypos
	 */
	public void setState(int xpos, int ypos) {
		this.xPosition = xpos;
		this.yPosition = ypos;
		this.pDrawMDP();
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
							f.getActionRight());
					temp.add(tempField);
				}
				copyScape.add(temp);
			}

			for (int x = 0; x < this.width; x++ ) {
				for (int y = 0; y < this.height; y++ ) {
					FieldPlusVal theOne = this.landscape.get(x).get(y);
					this.calcQ(copyScape, theOne, x, y);
					System.out.println("x/y:" + x + "/" + y + theOne.toString());
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

	/**
	 * Setter to set the speed with which the display is updated at maximum
	 *
	 * @param waittime
	 *            in ms
	 */
	public void setWaittime(int waittime) {
		if (waittime >= 0) {
			this.waittime = waittime;
		}
		else {
			System.err.println("ERROR:MDP:setWaittime: no negative waittime alowed.");
		}
	}

	// actions not necessary, can try any in action thing.
	public void tryAction(Field curState, Object knowledge) {
		// if (walkinwall, penalty, etc)

	}
}
