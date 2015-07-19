package nl.ru.ai.vroon.mdp;

import java.awt.Container;

import javax.swing.JFrame;

public class DrawFrame extends JFrame {
	
	private static final long		serialVersionUID	= 1L;
	private int						width				= 750;
	private int						height				= 800;
	private MarkovDecisionProblem	mdp;
	
	public DrawFrame(MarkovDecisionProblem mdp) {
		this.mdp = mdp;
		this.width = mdp.getWidth() * 50;
		this.height = mdp.getHeight() * 50;
		this.setSize(this.width + 20, this.height + 100);
		this.setTitle("MDP Visualization");
		
		this.drawContent();
	}
	
	public void drawContent() {
		DrawPanel panel = new DrawPanel(this.mdp, this.width, this.height);
		Container contentPane = this.getContentPane();
		contentPane.add(panel);
	}
	
}
