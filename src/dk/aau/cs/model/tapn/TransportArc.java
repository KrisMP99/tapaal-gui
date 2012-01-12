package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import pipe.gui.Pipe;

import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.Require;

public class TransportArc extends TAPNElement {
	private TimedPlace source;
	private TimedTransition transition;
	private TimedPlace destination;

	private TimeInterval interval;

	public TransportArc(TimedPlace source, TimedTransition transition, TimedPlace destination, TimeInterval interval) {
		Require.that(source != null, "The source place cannot be null");
		Require.that(transition != null, "The associated transition cannot be null");
		Require.that(destination != null, "The destination place cannot be null");
		Require.that(!source.isShared() || !transition.isShared(), "You cannot draw an arc between a shared transition and shared place.");
		Require.that(!transition.isShared() || !destination.isShared(), "You cannot draw an arc between a shared transition and shared place.");
		
		this.source = source;
		this.transition = transition;
		this.destination = destination;
		setTimeInterval(interval);
	}

	public TimedPlace source() {
		return source;
	}

	public TimedTransition transition() {
		return transition;
	}

	public TimedPlace destination() {
		return destination;
	}

	public TimeInterval interval() {
		return interval;
	}

	public void setTimeInterval(TimeInterval interval) {
		Require.that(interval != null, "A transport arc must have an associated interval");

		this.interval = interval;
	}

	public boolean isEnabled() {
		return getElligibleTokens().size() > 0;
	}

	public boolean isEnabledBy(TimedToken token) {
		Require.that(source.equals(token.place()), "Token must be in the correct place");

		return interval.isIncluded(token.age()) && destination.invariant().isSatisfied(token.age());
	}
	
	public List<TimedToken> getElligibleTokens(){
		List<TimedToken> elligibleTokens = new ArrayList<TimedToken>();
		Iterable<TimedToken> tokens = source.tokens();
		for (TimedToken token : tokens) {
			if (isEnabledBy(token)) elligibleTokens.add(token);
		}
		return elligibleTokens;
	}
	
	

	@Override
	public void delete() {
		model().remove(this);
	}

	public TransportArc copy(TimedArcPetriNet tapn) {
		return new TransportArc(tapn.getPlaceByName(source.name()), 
								tapn.getTransitionByName(transition.name()), 
								tapn.getPlaceByName(destination.name()), 
								interval.copy());
	}

	// Should ONLY be called in relation to sharing/unsharing places
	public void setSource(TimedPlace place) {
		Require.that(place != null, "place cannot be null");
		source = place;		
	}
	
	// Should ONLY be called in relation to sharing/unsharing places
	public void setDestination(TimedPlace place) {
		Require.that(place != null, "place cannot be null");
		destination = place;		
	}
	
	public TimeInterval getDEnabledInterval(){
		TimeInterval result = null;
		BigDecimal iLow = new BigDecimal(interval.lowerBound().value(), new MathContext(Pipe.AGE_PRECISION));
		BigDecimal iHeigh = new BigDecimal(interval.upperBound().value(), new MathContext(Pipe.AGE_PRECISION));
		
		for(TimedToken token : source.tokens()){
			TimeInterval temp = null;
			if( token.age().compareTo(iHeigh) <= 0 || interval().upperBound().value() < 0){//token's age is smaller than the upper bound of the interval (or the intervals upperbound is infinite)
				BigDecimal newLower = iLow.subtract(token.age(), new MathContext(Pipe.AGE_PRECISION));
				if(newLower.compareTo(BigDecimal.ZERO) < 0){
					newLower = BigDecimal.ZERO;
				}
				
				if(interval.upperBound().value() >= 0){//not infinite
					BigDecimal newUpper = iHeigh.subtract(token.age(), new MathContext(Pipe.AGE_PRECISION));
					if(newUpper.compareTo(BigDecimal.ZERO) < 0){
						newUpper = BigDecimal.ZERO;
					}
					
					if(newUpper.compareTo(BigDecimal.ZERO) == 0 && interval.IsUpperBoundNonStrict()){
						temp = new TimeInterval(true, new IntBound(newLower.intValue()), new IntBound(newUpper.intValue()), true);
					} else if (newUpper.compareTo(newLower) == 0 && interval.IsLowerBoundNonStrict() && interval.IsUpperBoundNonStrict()){
						temp = new TimeInterval(true, new IntBound(newLower.intValue()), new IntBound(newUpper.intValue()), true);
					} else if (newLower.compareTo(newUpper) < 0){
						temp = new  TimeInterval(interval.IsLowerBoundNonStrict(), new IntBound(newLower.intValue()), new IntBound(newUpper.intValue()), interval.IsUpperBoundNonStrict());
					} else { //new bounds are wrong
						temp = null;
					}
				} else { //upper bound is inf
					temp = new TimeInterval(interval.IsLowerBoundNonStrict(), new IntBound(newLower.intValue()), interval.upperBound(), false);
				}
			}
			
			result = IntervalOperations.union(temp, result);
		}
		
		//TODO Consider invariants (both on destination and source)
		return result;
	}
	
	@Override
	public String toString() {
		return "From " + source.name() + " to " + destination.name() + " through " + transition.name() + " with interval " + interval().toString();
	}
}
