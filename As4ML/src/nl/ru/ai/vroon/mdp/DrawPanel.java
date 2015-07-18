package nl.ru.ai.vroon.mdp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/**
 * Creates visual content in accordance with the given MDP
 *
 * @author Sjoerd Lagarde + some adaptations by Jered Vroon
 */
public class DrawPanel extends JPanel {
	
	private static final long		serialVersionUID	= 1L;
	private int						screenWidth;
	private int						screenHeight;
	private MarkovDecisionProblem	mdp;
	
	/**
	 * Constructor
	 *
	 * @param mdp
	 * @param screenWidth
	 * @param screenHeight
	 */
	public DrawPanel(MarkovDecisionProblem mdp, int screenWidth, int screenHeight) {
		this.mdp = mdp;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		this.setBackground(new Color(255, 255, 255)); 	// White background
		super.paintComponent(g);
		
		int stepSizeX = this.screenWidth / this.mdp.getWidth();
		int stepSizeY = this.screenHeight / this.mdp.getHeight();
		
		Graphics2D g2 = (Graphics2D) g;
		for (int i = 0; i < this.mdp.getWidth(); i++ ) {
			for (int j = 0; j < this.mdp.getHeight(); j++ ) {
				Field f = this.mdp.getFieldPlusVal(i, j).getMyField();
				
				g2.setPaint(Color.WHITE);
				if (f.equals(Field.REWARD)) {
					g2.setPaint(Color.GREEN);
				}
				else if (f.equals(Field.NEGREWARD)) {
					g2.setPaint(Color.RED);
				}
				else if (f.equals(Field.OBSTACLE)) {
					g2.setPaint(Color.GRAY);
				}
				g2.fillRect(stepSizeX * i, this.screenHeight - (stepSizeY * (j + 1)), stepSizeX, stepSizeY);
				
				if ( (this.mdp.getStateXPosition() == i) && (this.mdp.getStateYPostion() == j)) {
					g2.setPaint(Color.BLUE);
					g2.fillOval( (stepSizeX * i) + (stepSizeX / 4), (this.screenHeight - (stepSizeY * (j + 1))) + (stepSizeY / 4), stepSizeX / 2,
							stepSizeY / 2);
				}
				
				g2.setPaint(Color.BLACK);
				g2.drawRect(stepSizeX * i, this.screenHeight - (stepSizeY * (j + 1)), stepSizeX, stepSizeY);
			}
		}
		g2.drawString("Reward: \t\t" + this.mdp.getReward(), 30, this.screenHeight + 25);
		g2.drawString("#Actions: \t\t" + this.mdp.getActionsCounter(), 30, this.screenHeight + 40);
	}
	
}
