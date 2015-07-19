package nl.ru.ai.vroon.mdp;

public class FieldPlusVal {
	
	Field	myField			= null;
	double	myVal			= 0;
	double	actionUp		= 0, actionDown = 0, actionLeft = 0, actionRight = 0;
	int		x, y;
	Action	actionPolicy	= Action.LEFT;
	
	public FieldPlusVal(Field field, double val, double up, double down, double left, double right, int x, int y) {
		this.myField = field;
		this.myVal = val;
		this.actionUp = up;
		this.actionDown = down;
		this.actionLeft = left;
		this.actionRight = right;
		this.x = x;
		this.y = y;
	}
	
	public double getActionDown() {
		return this.actionDown;
	}
	
	public double getActionLeft() {
		return this.actionLeft;
	}
	
	public Action getActionPolicy() {
		return this.actionPolicy;
	}
	
	public double getActionRight() {
		return this.actionRight;
	}
	
	public double getActionUp() {
		return this.actionUp;
	}
	
	public Field getMyField() {
		return this.myField;
	}
	
	public double getMyVal() {
		return this.myVal;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public double maxAct() {
		return Math.max(Math.max(this.actionLeft, this.actionRight), Math.max(this.actionUp, this.actionDown));
	}
	
	public void setActionDown(double actionDown) {
		this.actionDown = actionDown;
	}
	
	public void setActionLeft(double actionLeft) {
		this.actionLeft = actionLeft;
	}
	
	public double setActionPolicy() {
		double oldval = this.myVal;
		this.myVal = Math.max(Math.max(this.actionLeft, this.actionRight), Math.max(this.actionUp, this.actionDown));
		if (this.myVal == this.actionLeft) {
			this.actionPolicy = Action.LEFT;
		}
		else if (this.myVal == this.actionRight) {
			this.actionPolicy = Action.RIGHT;
		}
		else if (this.myVal == this.actionUp) {
			this.actionPolicy = Action.UP;
		}
		else {
			this.actionPolicy = Action.DOWN;
		}
		return Math.abs( (oldval - this.myVal));
	}
	
	public void setActionRight(double actionRight) {
		this.actionRight = actionRight;
	}
	
	public void setActionUp(double actionUp) {
		this.actionUp = actionUp;
	}
	
	public void setMyField(Field field) {
		this.myField = field;
	}
	
	public void setMyVal(double val) {
		this.myVal = val;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "x/y:" + this.x + "/" + this.y + " Val: " + this.myVal + " Up:" + this.actionUp + " Dn:" + this.actionDown + " Lt:" + this.actionLeft
				+ " Rt:" + this.actionRight + " AP:" + this.actionPolicy + "\n";
	}
	
	public void updatemyVal() {
		this.myVal = this.maxAct();
	}
	
}
