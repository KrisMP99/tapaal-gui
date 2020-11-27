package dk.aau.cs.model.tapn;

import java.util.*;

import dk.aau.cs.gui.undo.Colored.*;
import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Expressions.*;
import pipe.gui.MessengerImpl;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.event.ConstantChangedEvent;
import dk.aau.cs.model.tapn.event.ConstantEvent;
import dk.aau.cs.model.tapn.event.ConstantsListener;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.StringComparator;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.ConstantsPane;

public class TimedArcPetriNetNetwork {
	private final List<TimedArcPetriNet> tapns = new ArrayList<TimedArcPetriNet>();
	private final List<SharedPlace> sharedPlaces = new ArrayList<SharedPlace>();
	private final List<SharedTransition> sharedTransitions = new ArrayList<SharedTransition>();
	
	private NetworkMarking currentMarking = new NetworkMarking();
	private final ConstantStore constants;

    private List<ColorType> colorTypes = new ArrayList<ColorType>();
    private List<Variable> variables = new ArrayList<Variable>();
	private int defaultBound = 3;
	
	private final List<ConstantsListener> constantsListeners = new ArrayList<ConstantsListener>();
	
	private boolean paintNet = true;
	
	public TimedArcPetriNetNetwork() {
		this(new ConstantStore(), List.of(ColorType.COLORTYPE_DOT));
	}
	
	public TimedArcPetriNetNetwork(ConstantStore constants, List<ColorType> colorTypes){
		this.constants = constants;
		this.colorTypes.addAll(colorTypes);
        buildConstraints();
	}
	
	public void addConstantsListener(ConstantsListener listener){
		constantsListeners.add(listener);
	}
	
	public void removeConstantsListener(ConstantsListener listener){
		constantsListeners.remove(listener);
	}

	public void add(TimedArcPetriNet tapn) {
		Require.that(tapn != null, "tapn must be non-null");

		tapn.setParentNetwork(this);
		tapns.add(tapn);
		LocalTimedMarking marking = tapn.marking() instanceof LocalTimedMarking ? (LocalTimedMarking)tapn.marking() : new LocalTimedMarking();
		currentMarking.addMarking(tapn, marking);
		tapn.setMarking(currentMarking);
	}
	public void add(SharedTransition sharedTransition){
		add(sharedTransition, false);
	}

	
	public void add(SharedTransition sharedTransition, boolean multiAdd){
		Require.that(sharedTransition != null, "sharedTransition must not be null");
		if(!multiAdd) {
			Require.that(!isNameUsed(sharedTransition.name()), "There is already a transition or place with that name");
		}
		
		sharedTransition.setNetwork(this);
		if(!(sharedTransitions.contains(sharedTransition)))
			sharedTransitions.add(sharedTransition);
	}
	
	public void add(SharedPlace sharedPlace) {
		add(sharedPlace, false);
	}
	
	public void add(SharedPlace sharedPlace, boolean multiremove) {
		Require.that(sharedPlace != null, "sharedPlace must not be null");
		if(!multiremove) {
			Require.that(!isNameUsed(sharedPlace.name()), "There is already a transition or place with that name");
		}
		sharedPlace.setNetwork(this);
		sharedPlace.setCurrentMarking(currentMarking);
		if(!(sharedPlaces.contains(sharedPlace)))
			sharedPlaces.add(sharedPlace);
	}

	public boolean isNameUsedForShared(String name){
		for(SharedTransition transition : sharedTransitions){
			if(transition.name().equalsIgnoreCase(name)) {
				return true;
			}
		}
		
		for(SharedPlace place : sharedPlaces){
			if(place.name().equalsIgnoreCase(name)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isNameUsedInTemplates(String name){
		for(TimedArcPetriNet net : tapns){
			if(net.isNameUsed(name)) return true;
		}
		return false;
	}
	
	public boolean isNameUsedForPlacesOnly(String name) {
		for(TimedArcPetriNet net : tapns){
			for(TimedTransition transition : net.transitions()) {
				if(name.equalsIgnoreCase(transition.name()))
					return false;
			}
		}
		return true;
	}
	public boolean isNameUsedForTransitionsOnly(String name) {
		for(TimedArcPetriNet net : tapns){
			for(TimedPlace place : net.places()) {
				if(name.equalsIgnoreCase(place.name()))
					return false;
			}
		}
		return true;
	}
		
	public boolean isNameUsed(String name) {
		return isNameUsedForShared(name) || isNameUsedInTemplates(name);
	}

	public void remove(TimedArcPetriNet tapn) {
		if (tapn != null) {
			tapn.setParentNetwork(null);
			tapns.remove(tapn);
			currentMarking.removeMarkingFor(tapn);
		}
	}
	
	public void remove(SharedPlace sharedPlace) {
		if (sharedPlace != null) {
			sharedPlace.setNetwork(null);
			sharedPlaces.remove(sharedPlace);
		}
	}
	
	public void remove(SharedTransition sharedTransition) {
		if (sharedTransition != null) {
			sharedTransition.setNetwork(null);
			sharedTransitions.remove(sharedTransition);
			sharedTransition.delete();
		}
	}
	
	public List<TimedArcPetriNet> activeTemplates() {
		List<TimedArcPetriNet> activeTemplates = new ArrayList<TimedArcPetriNet>();
		for(TimedArcPetriNet t : tapns) {
			if(t.isActive())
				activeTemplates.add(t);
		}
		
		return activeTemplates;
	}

	public List<TimedArcPetriNet> allTemplates() {
		return tapns;
	}

	public boolean hasTAPNCalled(String newName) {
		for (TimedArcPetriNet tapn : tapns)
			if (tapn.name().equalsIgnoreCase(newName))
				return true;
		return false;
	}

	public NetworkMarking marking() {
		return currentMarking;
	}

	public void setMarking(NetworkMarking marking) {
		currentMarking = marking;
		for (TimedArcPetriNet tapn : tapns) {
			tapn.setMarking(currentMarking);
		}
	}

	public boolean isConstantNameUsed(String newName) {
		return constants.containsConstantByName(newName);
	}

	public void buildConstraints() {
		constants.buildConstraints(this);
	}

	// TODO: Command is a GUI concern. This should not know anything about it
	public Command addConstant(String name, int val) {
		Command cmd = constants.addConstant(name, val); 
		Constant constant = constants.getConstantByName(name);
		fireConstantAdded(constant);
		return cmd;
	}

	private void fireConstantAdded(Constant constant) {
		for(ConstantsListener listener : constantsListeners){
			listener.constantAdded(new ConstantEvent(constant, constants.getIndexOf(constant)));
		}
	}


	public Command removeConstant(String name) {
		Constant constant = constants.getConstantByName(name);
		int index = constants.getIndexOf(constant);
		Command cmd = constants.removeConstant(name);
		for(ConstantsListener listener : constantsListeners){
			listener.constantRemoved(new ConstantEvent(constant, index));
		}
		return cmd;
	}

	public Command updateConstant(String oldName, Constant constant) {
		Constant old = constants.getConstantByName(oldName);
		int index = constants.getIndexOf(old);
		Command edit = constants.updateConstant(oldName, constant, this);

		if (edit != null) {
			updateGuardsAndWeightsWithNewConstant(oldName, constant);
			for(ConstantsListener listener : constantsListeners){
				listener.constantChanged(new ConstantChangedEvent(old, constant, index));
			}
		}

		return edit;
	}

	public void updateGuardsAndWeightsWithNewConstant(String oldName, Constant newConstant) {
		for (TimedArcPetriNet tapn : allTemplates()) {
			for (TimedPlace place : tapn.places()) {
				updatePlaceInvariant(oldName, newConstant, place);
			}

			for (TimedInputArc inputArc : tapn.inputArcs()) {
				updateTimeIntervalAndWeight(oldName, newConstant, inputArc.interval(), inputArc.getWeight());
			}

			for (TransportArc transArc : tapn.transportArcs()) {
				updateTimeIntervalAndWeight(oldName, newConstant, transArc.interval(), transArc.getWeight());
			}

			for (TimedInhibitorArc inhibArc : tapn.inhibitorArcs()) {
				updateTimeIntervalAndWeight(oldName, newConstant, inhibArc.interval(), inhibArc.getWeight());
			}
			
			for (TimedOutputArc outputArc : tapn.outputArcs()) {
				updateWeight(oldName, newConstant, outputArc.getWeight());
			}
		}

	}

	private void updatePlaceInvariant(String oldName, Constant newConstant, TimedPlace place) {
		updateBound(oldName, newConstant, place.invariant().upperBound());
	}

	private void updateTimeIntervalAndWeight(String oldName, Constant newConstant, TimeInterval interval, Weight weight) {
		updateBound(oldName, newConstant, interval.lowerBound());
		updateBound(oldName, newConstant, interval.upperBound());
		updateWeight(oldName, newConstant, weight);
	}

	private void updateBound(String oldName, Constant newConstant, Bound bound) {
		if (bound instanceof ConstantBound) {
			ConstantBound cb = (ConstantBound) bound;

			if (cb.name().equals(oldName)) {
				cb.setConstant(newConstant);
			}
		}
	}
	
	private void updateWeight(String oldName, Constant newConstant, Weight weight) {
		if(weight instanceof ConstantWeight){
			ConstantWeight cw = (ConstantWeight) weight;
			
			if(cw.constant().name().equals(oldName)){
				cw.setConstant(newConstant);
			}
		}
		
	}

	public Collection<Constant> constants() {
		return constants.getConstants();
	}

	public Set<String> getConstantNames() {
		return constants.getConstantNames();
	}

	public int getConstantValue(String name) {
		return constants.getConstantByName(name).value();
	}

	public int getLargestConstantValue() {
		return constants.getLargestConstantValue();
	}

	public void setConstants(Iterable<Constant> constants) {
		for (Constant c : constants) {
			this.constants.add(c);
			fireConstantAdded(c);			
		}
	}

	public Constant getConstant(String constantName) {
		return constants.getConstantByName(constantName);
	}
	
	public Constant getConstant(int index){
		return constants.getConstantByIndex(index);
	}

	public TimedArcPetriNet getTAPNByName(String name) {
		for (TimedArcPetriNet tapn : tapns) {
			if (tapn.name().equals(name))
				return tapn;
		}
		return null;
	}

	
	
	public int numberOfSharedPlaces() {
		return sharedPlaces.size();
	}
	
	public int numberOfSharedTransitions() {
		return sharedTransitions.size();
	}

	public SharedPlace getSharedPlaceByIndex(int index) {
		return sharedPlaces.get(index);
	}

	public Object getSharedTransitionByIndex(int index) {
		return sharedTransitions.get(index);
	}

	public Collection<SharedTransition> sharedTransitions() {
		return sharedTransitions;
	}

	public Collection<SharedPlace> sharedPlaces() {
		return sharedPlaces;
	}

	public SharedTransition getSharedTransitionByName(String name) {
		for(SharedTransition t : sharedTransitions){
			if(t.name().equalsIgnoreCase(name)) return t;
		}
		return null;
	}

	public TimedPlace getSharedPlaceByName(String name) {
		for(SharedPlace place : sharedPlaces){
			if(place.name().equalsIgnoreCase(name)) return place;
		}
		return null;
	}

	public boolean hasInhibitorArcs() {
		for(TimedArcPetriNet tapn : tapns) {
			if(tapn.isActive() && tapn.hasInhibitorArcs())
				return true;
		}
		return false;
	}

	public void swapTemplates(int currentIndex, int newIndex) {
		TimedArcPetriNet temp = tapns.get(currentIndex);
		tapns.set(currentIndex, tapns.get(newIndex));
		tapns.set(newIndex, temp);
	}
	
	public TimedArcPetriNet[] sortTemplates() {
		TimedArcPetriNet[] oldOrder = tapns.toArray(new TimedArcPetriNet[0]);
		tapns.sort(new StringComparator());
		return oldOrder;
	}
	
	public void undoSort(TimedArcPetriNet[] tapns) {
		this.tapns.clear();
		this.tapns.addAll(Arrays.asList(tapns));
	}

	public void swapConstants(int currentIndex, int newIndex) {
		constants.swapConstants(currentIndex, newIndex);
	}
	
	public Constant[] sortConstants() {
		return constants.sortConstants();
	}
	
	public void undoSort(Constant[] oldOrder) {
		constants.undoSort(oldOrder);
	}

	public void swapSharedPlaces(int currentIndex, int newIndex) {
		SharedPlace temp = sharedPlaces.get(currentIndex);
		sharedPlaces.set(currentIndex, sharedPlaces.get(newIndex));
		sharedPlaces.set(newIndex, temp);
	}
	
	public SharedPlace[] sortSharedPlaces() {
		SharedPlace[] oldOrder = sharedPlaces.toArray(new SharedPlace[0]);
		sharedPlaces.sort(new StringComparator());
		return oldOrder;
	}
	
	public void undoSort(SharedPlace[] oldOrder) {
		sharedPlaces.clear();
		sharedPlaces.addAll(Arrays.asList(oldOrder));
	}

	public void swapSharedTransitions(int currentIndex, int newIndex) {
		SharedTransition temp = sharedTransitions.get(currentIndex);
		sharedTransitions.set(currentIndex, sharedTransitions.get(newIndex));
		sharedTransitions.set(newIndex, temp);
	}
	
	public SharedTransition[] sortSharedTransitions() {
		SharedTransition[] oldOrder = sharedTransitions.toArray(new SharedTransition[0]); 
		sharedTransitions.sort(new StringComparator());
		return oldOrder;
	}
	
	public void undoSort(SharedTransition[] oldOrder) {
		sharedTransitions.clear();
		sharedTransitions.addAll(Arrays.asList(oldOrder));
	}

	//TODO: maybe should be more extensive than this
    //E.g. check expressions, invariants, intervals and tokens
	public boolean hasColors(){
	    return colorTypes.size() > 1 || variables.size() > 0;
    }
	public boolean isUntimed(){
		for(TimedArcPetriNet t : tapns){
			if(!t.isUntimed()){
				return false;
			}
		}
		return true;
	}

	public boolean hasWeights() {
		for(TimedArcPetriNet t : tapns){
			if(t.isActive() && t.hasWeights()){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasUrgentTransitions() {
		for(TimedArcPetriNet t : tapns){
			if(t.isActive() && t.hasUrgentTransitions()){
				return true;
			}
		}
		return false;
	}

    public boolean hasUncontrollableTransitions() {
        for(TimedArcPetriNet t : tapns){
            if(t.isActive() && t.hasUncontrollableTransitions()){
                return true;
            }
        }
        return false;
    }
	
	public boolean hasInvariants() {
		for(TimedArcPetriNet t : tapns){
			if(t.isActive()){
				for(TimedPlace p : t.places()){
					if(!p.invariant().upperBound().equals(Bound.Infinity)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean isNonStrict(){
		for(TimedArcPetriNet t : tapns){
			if(t.isActive() && !t.isNonStrict()){
				return false;
			}
		}
		return true;
	}
	
	public boolean isDegree2(){
		ITAPNComposer composer = new TAPNComposer(new MessengerImpl(), false);
		Tuple<TimedArcPetriNet,NameMapping> composedModel = composer.transformModel(this);

		return composedModel.value1().isDegree2();
	}

    public int getHighestNetDegree(){
        ITAPNComposer composer = new TAPNComposer(new MessengerImpl(), false);
        Tuple<TimedArcPetriNet,NameMapping> composedModel = composer.transformModel(this);

        return composedModel.value1().getHighestNetDegree();
    }

	public boolean isSharedPlaceUsedInTemplates(SharedPlace place) {
		for(TimedArcPetriNet tapn : this.activeTemplates()){
			for(TimedPlace timedPlace : tapn.places()){
				if(timedPlace.equals(place)) return true;
			}
		}
		return false;
	}
	
	/**
	 * Finds the biggest constant in the active part of the network
	 * @return The biggest constant in the active part of the network or -1 if there are no constants in the net
	 */
	public int biggestConstantInActiveNet(){
		int biggestConstant = -1;
		for(TimedArcPetriNet tapn : this.activeTemplates()){
			int tmp = tapn.getBiggestConstant();
			if(tmp > biggestConstant){
				biggestConstant = tmp;
			}
		}
		return biggestConstant;
	}
	
	/**
	 * Finds the biggest constant which is associated with an enabled transition in the active part of the network
	 * @return The biggest constant which is associated with an enabled transition in the active part of the net or -1 if there are no such constants 
	 */
	public int biggestContantInActiveNetEnabledTransitions(){
		int biggestConstant = -1;
		for(TimedArcPetriNet tapn : this.activeTemplates()){
			int tmp = tapn.getBiggestConstantEnabledTransitions();
			if(tmp > biggestConstant){
				biggestConstant = tmp;
			}
		}
		return biggestConstant;
	}
	
	public TimedArcPetriNetNetwork copy(){
		TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
		
		for(SharedPlace p : sharedPlaces){
			network.add(new SharedPlace(p.name(), p.invariant().copy()));
            
			/* Copy markings for shared places */
			for(TimedToken token : currentMarking.getTokensFor(p)){
				network.currentMarking.add(token.clone());
			}
		}
		
		for(SharedTransition t : sharedTransitions){
			network.add(new SharedTransition(t.name()));	// TODO This is okay for now
		}
		
		for(Constant c : constants()){
			network.addConstant(c.name(), c.value());
		}
		
		for(TimedArcPetriNet t : tapns){
			TimedArcPetriNet new_t = t.copy();
			network.add(new_t);
			for(TimedTransition trans : new_t.transitions()){
				if(trans.isShared()){
					network.getSharedTransitionByName(trans.name()).makeShared(trans);
				}
			}
		}
		
		network.setDefaultBound(getDefaultBound());
		
		return network;
	}

	public int getDefaultBound() {
		return defaultBound;
	}
	
	public void setDefaultBound(int defaultBound) {
		this.defaultBound = defaultBound;
	}

	public boolean paintNet() {
		return paintNet;
	}
	
	public void setPaintNet(boolean paintNet){
		this.paintNet = paintNet;
	}

	//For colors

    public List<ColorType> colorTypes() { return colorTypes;}
    public void setColorTypes(List<ColorType> cts) { colorTypes = cts;}


    public List<Variable> variables() {return variables;}
    public void setVariables(List<Variable> newVariables) { variables = newVariables;}


    public void updateColorType(String oldName, ColorType colorType, ConstantsPane.ColorTypesListModel colorTypesListModel, UndoManager undoManager) {
        Integer index = getColorTypeIndex(oldName);
        ColorType oldColorType = getColorTypeByIndex(index);

        Command command = new UpdateColorTypeCommand(this, oldColorType, colorType, index, colorTypesListModel);
        command.redo();
        undoManager.addEdit(command);
        updateColorType(colorType, oldColorType, undoManager);
    }

    private void updateColorType(ColorType colorType, ColorType oldColorType, UndoManager undoManager){
        updateProductTypes(oldColorType, colorType, undoManager);
        for (TimedArcPetriNet tapn : tapns) {
            updateColorTypeOnPlaces(oldColorType, colorType, tapn.places(), undoManager);
            updateColorTypeOnArcs(oldColorType,colorType,tapn,undoManager);
            updateColorTypeOnTransitions(tapn.transitions(), colorType, oldColorType, undoManager);
        }
        updateColorTypeOnVariables(oldColorType, colorType, undoManager);
    }

    private void updateProductTypes(ColorType oldColorType, ColorType colorType, UndoManager undoManager){
        for(ColorType ct : colorTypes){
            if(ct instanceof ProductType){
                Command command = new UpdatePTColorTypeCommand(oldColorType, colorType, (ProductType)ct);
                command.redo();
                undoManager.addEdit(command);
            }
        }
    }
    private void updateColorTypeOnTransitions(List<TimedTransition> transitions, ColorType colorType, ColorType oldColorType, UndoManager undoManager){
	    for(TimedTransition transition : transitions){
            Command command = new UpdateTransitionColorsCommand(transition, oldColorType, colorType);
            command.redo();
            undoManager.addEdit(command);
        }
    }

    private void updateColorTypeOnArcs(ColorType oldColorType, ColorType colorType, TimedArcPetriNet tapn, UndoManager undoManager){
        ArrayList<Variable> variablesToRemove = new ArrayList<>();
	    for(Variable var : variables){
            if(!var.getColorType().equals(colorType) ){
                if (!var.getColorType().getId().equals(colorType.getId())) {
                    variablesToRemove.add(var);
                }
            }
        }
	    for(TimedInputArc arc : tapn.inputArcs()) {
            Command command = new UpdateInputArcColorTypeCommand(oldColorType, colorType, arc, variablesToRemove);
            command.redo();
            undoManager.addEdit(command);
        }
        for(TimedOutputArc arc : tapn.outputArcs()){
            Command command = new UpdateOutputArcColorTypeCommand(oldColorType, colorType, arc, variablesToRemove);
            command.redo();
            undoManager.addEdit(command);
        }
        for(TransportArc arc : tapn.transportArcs()){
            Command command = new UpdateTransportArcColorTypeCommand(oldColorType, colorType, arc, variablesToRemove);
            command.redo();
            undoManager.addEdit(command);
        }
    }
    public void updateColorTypeOnVariables(ColorType oldColorType, ColorType colorType, UndoManager undoManager){
        if (oldColorType.getId().equals(colorType.getId())) {
            //If it is the same color type that has just had some colors removed, the variables should be kept
            return;
        }
        Command command = new RemoveVariablesForColorTypeCommand(oldColorType, colorType , variables);
        command.redo();
        undoManager.addEdit(command);

    }

    public void updateColorTypeOnPlaces(ColorType oldColorType, ColorType colorType, List<TimedPlace> places, UndoManager undoManager){
        for(TimedPlace place : places){
            if(place.getColorType().equals(oldColorType)){
                Command command = new UpdateColorTypeForPlaceCommand(place, oldColorType, colorType);
                command.redo();
                undoManager.addEdit(command);
            }
        }
    }
    public void updateVariable(String oldName, Variable variable) {
        Integer index = getVariableIndex(oldName);
        Variable oldVar = getVariableByIndex(index);
        VariableExpression oldVarExpr = new VariableExpression(oldVar);
        VariableExpression newVarExpr = new VariableExpression(variable);
        updateVariable(newVarExpr, oldVarExpr);
        if (index != null) {
            variables.set(index, variable);
        }
    }

    private void updateVariable(ColorExpression newExpr, VariableExpression oldExpression){
        for (TimedArcPetriNet tapn : tapns) {
            int i = 0;
            for (TimedTransition transition : tapn.transitions()) {
                Expression expr = transition.getGuard();
                if (expr != null) {
                    expr.replace(oldExpression, newExpr, true);
                    transition.setGuard((GuardExpression) expr);
                    tapn.replace(transition, i);

                }
                for(TimedInputArc arc : transition.getInputArcs()){
                    Expression arcexpr = arc.getArcExpression();
                    arcexpr.replace(oldExpression, newExpr, true);
                    arc.setExpression((ArcExpression)arcexpr);
                }
                for(TimedOutputArc arc : transition.getOutputArcs()){
                    Expression arcexpr = arc.getExpression();
                    arcexpr.replace(oldExpression, newExpr, true);
                    arc.setExpression((ArcExpression)arcexpr);
                }
                for(TransportArc arc : transition.getTransportArcsGoingThrough()){
                    Expression arcexpr = arc.getInputExpression();
                    arcexpr.replace(oldExpression, newExpr, true);
                    arc.setInputExpression((ArcExpression)arcexpr);
                    arcexpr = arc.getOutputExpression();
                    arcexpr.replace(oldExpression, newExpr, true);
                    arc.setOutputExpression((ArcExpression)arcexpr);
                }
                i++;
            }
        }
    }
    public Integer getColorTypeIndex(String name) {
        for (int i = 0; i < colorTypes.size(); i++) {
            if (colorTypes.get(i).getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return null;
    }
    public ColorType getColorTypeByName(String name) {
        for (ColorType element : colorTypes) {
            if (element.getName().equalsIgnoreCase(name)) {
                return element;
            }
        }
        return null;
    }

    public Integer getVariableIndex(String name) {
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return null;
    }
    public boolean isNameUsedForColorType(String name) {
        for (ColorType element : colorTypes) {
            if (element.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNameUsedForVariable(String name) {
        for (Variable element : variables) {
            if (element.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    public Variable getVariableByName(String name){
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).getName().equalsIgnoreCase(name)) {
                return variables.get(i);
            }
        }
        return null;
    }
    public Color getColorByName(String name){
        for (ColorType element : colorTypes) {
            if(element.getColorByName(name) != null){
                return element.getColorByName(name);
            }
        }
        return null;
    }
    public Variable getVariableByIndex(int index) {
        return variables.get(index);
    }
    public int numberOfColorTypes() {
        return colorTypes.size();
    }

    public ColorType getColorTypeByIndex(int index) {
        return colorTypes.get(index);
    }

    public int numberOfVariables() {
        return variables.size();
    }
    public void add(ColorType colorType) {
        if (colorType.equals(ColorType.COLORTYPE_DOT) && isNameUsedForColorType(ColorType.COLORTYPE_DOT.getName()))
            return;

        Require.that(colorType != null, "colorType must not be null");
        Require.that(!isNameUsedForColorType(colorType.getName()), "There is already a color type with that name"); //TODO:: When using load, a nullpointer exception is thrown here
        colorTypes.add(colorType);
    }

    public void add(Variable variable) {
        Require.that(variable != null, "variable must not be null");
        Require.that(!isNameUsedForVariable(variable.getName()), "There is already a variable with that name");

        variables.add(variable);
    }

    public void remove(ColorType colorType, ConstantsPane.ColorTypesListModel colorTypesListModel, UndoManager undoManager) {
        List<ColorType> toRemove = collectColorTypesToRemoveAndUpdateTodot(colorType);
        for(ColorType ct : toRemove){
            Command command = new RemoveColorTypeFromNetworkCommand(ct, this, colorTypesListModel);
            command.redo();
            undoManager.addEdit(command);
            updateColorType(ColorType.COLORTYPE_DOT, ct, undoManager);
        }
        /*
	    if (colorType != null) {
            colorTypes.removeAll(toRemove);
        }
         */
    }

    private List<ColorType> collectColorTypesToRemoveAndUpdateTodot(ColorType colorType){
        List<ColorType> toRemove = new ArrayList<>();
        toRemove.add(colorType);
        for(ColorType ct : colorTypes){
            if(ct instanceof ProductType && ((ProductType) ct).contains(colorType)){
                toRemove.addAll(collectColorTypesToRemoveAndUpdateTodot(ct));
            }
        }
        return toRemove;
    }

    public void remove(Variable variable) {
        VariableExpression oldVarExpr = new VariableExpression(variable);
        UserOperatorExpression newExpr = new UserOperatorExpression(variable.getColorType().getFirstColor());
        updateVariable(newExpr,oldVarExpr);

	    if (variable != null) {
            variables.remove(variable);
        }
    }
    //TODO: Refactor the all these functions
    public ColorType[] sortColorTypes() {
        ColorType[] oldorder = colorTypes.toArray(new ColorType[0]);
        Collections.sort(colorTypes, new StringComparator());
        return oldorder;
    }
    public void undoSort(ColorType[] oldorder) {
        colorTypes.clear();
        for (ColorType element: oldorder) {
            colorTypes.add(element);
        }
    }
    public void swapColorTypes(int currentIndex, int newIndex) {
        ColorType temp = colorTypes.get(currentIndex);
        colorTypes.set(currentIndex, colorTypes.get(newIndex));
        colorTypes.set(newIndex, temp);
    }

    public void swapVariables(int currentIndex, int newIndex) {
        Variable temp = variables.get(currentIndex);
        variables.set(currentIndex, variables.get(newIndex));
        variables.set(newIndex, temp);
    }

    public Variable[] sortVariables() {
        Variable[] oldOrder = variables.toArray(new Variable[0]);
        Collections.sort(variables, new StringComparator());
        return oldOrder;
    }

    public void undoSort(Variable[] oldOrder) {
        variables.clear();
        for (Variable element: oldOrder) {
            variables.add(element);
        }
    }

    public ExpressionContext getContext(){
	    HashMap<String, ColorType> hashMap = new HashMap<>();
	    for(ColorType colorType : colorTypes){
	        hashMap.put(colorType.getName(), colorType);
        }
	    return new ExpressionContext(new HashMap<String, Color>(), hashMap);
    }


}
