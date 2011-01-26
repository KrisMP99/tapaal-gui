package pipe.dataLayer.colors;

import java.util.ArrayList;
import java.util.List;


public class ColoredInterval {
	private boolean lowerIncluded = true;
	private IntervalBound lower = new IntervalBound(false);
	private IntervalBound upper = new IntervalBound(true);
	private boolean upperIncluded = false;
	private boolean displayValues = false;
	
	public ColoredInterval(){
		
	};
	
	public ColoredInterval(String lowerParenthesis, IntervalBound lower, IntervalBound upper, String upperParenthesis){
		lowerIncluded = !lowerParenthesis.equals("("); // to make it default to [
		upperIncluded = upper.goesToInfinity() ? false : upperParenthesis.equals("]"); // to make it default to )
		this.lower = lower;
		this.upper = upper;		
	}
	
	public ColoredInterval(String interval) {
		this(interval.substring(0,1),
				new IntervalBound(interval.substring(1,interval.indexOf(",")).trim()),
				new IntervalBound(interval.substring(interval.indexOf(",")+1, interval.length()-1).trim()),
				interval.substring(interval.length()-1));
		
	}

	public IntervalBound getLower(){
		return lower;
	}
	
	public IntervalBound getUpper(){
		return upper;
	}
	
	public boolean goesToInfinity(){
		return upper.goesToInfinity();
	}
	
	public String getOpenParenthesis(){
		return lowerIncluded ? "[" : "(";
	}
	
	public String getCloseParenthesis(){
		return upperIncluded ? "]" : ")";
	}
		
	@Override
	public String toString() {
		if(displayValues) return toStringWithoutConstants();
		
		String format = "%1$s%2$s, %3$s%4$s";
		
		return String.format(format, getOpenParenthesis(), lower, upper, getCloseParenthesis());
	}

	public boolean contains(ColoredToken token) {
		// check if age fits one of the end points
		if(lower.equals(token) && !lowerIncluded) return false;
		if(upper.equals(token) && !upperIncluded) return false;
		
		return lower.isLessThanOrEqual(token) && upper.isGreaterThanOrEqual(token);
	}

	public List<String> getUsedConstants() {
		List<String> list = new ArrayList<String>();
		
		list.addAll(lower.getUsedConstants());
		list.addAll(upper.getUsedConstants());
		
		return list;
	}

	public String toStringWithoutConstants() {
String format = "%1$s%2$s, %3$s%4$s";
		
		return String.format(format, getOpenParenthesis(), lower.toStringWithoutConstants(), 
				upper.toStringWithoutConstants(), getCloseParenthesis());
	}

	public void displayValues(boolean showValues) {
		this.displayValues = showValues;		
	}

	public void updateConstantName(String oldName, String newName) {
		lower.updateConstantName(oldName, newName);
		upper.updateConstantName(oldName, newName);
	}
}