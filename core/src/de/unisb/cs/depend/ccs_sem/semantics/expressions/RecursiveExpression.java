package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class RecursiveExpression extends Expression {

    private final ProcessVariable referencedProcessVariable;
    private final List<Value> parameterValues;
    private Expression instantiatedExpression = null;

    public RecursiveExpression(ProcessVariable referencedProcessVariable, List<Value> parameters) {
        super();
        this.referencedProcessVariable = referencedProcessVariable;
        this.parameterValues = parameters;
    }

    /**
     * Note: The returned list must not be changed!
     */
    public List<Value> getParameters() {
        return parameterValues;
    }

    public ProcessVariable getReferencedProcessVariable() {
        return referencedProcessVariable;
    }

    /**
     * Creates the instantiated {@link Expression} of this {@link RecursiveExpression},
     * i.e. the {@link Expression} of the referenced {@link ProcessVariable},
     * instantiated by the parameters of this {@link RecursiveExpression}.
     * If the parameters are not in the valid {@link Range} of the
     * {@link ProcessVariable}'s {@link Parameter}s, an {@link ErrorExpression} is
     * generated.
     *
     * @return the generated {@link Expression}
     */
    public Expression getInstantiatedExpression() {
        if (instantiatedExpression == null) {
            // if all parameters are fully instantiated, check if the parameters
            // are in the correct range. if not, we just do no tests, they are
            // done later, when the expression is further instantiated
            boolean readyForCheck = true;
            for (final Value value: parameterValues)
                if (!(value instanceof ConstantValue)) {
                    readyForCheck = false;
                    break;
                }

            final boolean rangesOK = readyForCheck
                ? referencedProcessVariable.checkRanges(parameterValues) : true;
            instantiatedExpression = rangesOK
                ? referencedProcessVariable.instantiate(parameterValues)
                : ErrorExpression.get();
        }

        return instantiatedExpression;
    }

    @Override
    public Collection<Expression> getChildren() {
        return Collections.singleton(getInstantiatedExpression());
    }

    @Override
    public Collection<Expression> getSubTerms() {
        return Collections.singleton(referencedProcessVariable.getValue());
    }

    @Override
    protected List<Transition> evaluate0() {
        return getInstantiatedExpression().getTransitions();
    }

    @Override
    public Expression replaceRecursion(List<ProcessVariable> processVariables) {
        // nothing to do here
        return this;
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> params) {
        final List<Value> newParameters = new ArrayList<Value>(parameterValues.size());
        boolean changed = false;
        for (final Value param: parameterValues) {
            final Value newParam = param.instantiate(params);
            if (!changed && !newParam.equals(param))
                changed = true;
            newParameters.add(newParam);
        }

        if (!changed)
            return this;

        return ExpressionRepository.getExpression(new RecursiveExpression(referencedProcessVariable, newParameters));
    }

    @Override
    public Set<Action> getAlphabet(Set<ProcessVariable> alreadyIncluded) {
        if (alreadyIncluded.contains(referencedProcessVariable))
            // no Collections.emptySet() here because it could be modified by the caller
            return new HashSet<Action>();
        alreadyIncluded.add(referencedProcessVariable);
        final Set<Action> alphabet = referencedProcessVariable.getValue().getAlphabet(alreadyIncluded);
        // we have to remove it afterwards, so that other branches evaluate the full alphabet
        alreadyIncluded.remove(referencedProcessVariable);
        return alphabet;
    }

    @Override
    protected boolean isError0() {
        return getInstantiatedExpression().isError();
    }

    @Override
    public String toString() {
        if (parameterValues.size() == 0)
            return referencedProcessVariable.getName();

        return referencedProcessVariable.getName() + parameterValues;
    }

    @Override
    protected int hashCode0() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + referencedProcessVariable.hashCode();
        result = PRIME * result + parameterValues.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final RecursiveExpression other = (RecursiveExpression) obj;
        // hashCode is cached, so we compare it first (it's cheap)
        if (hashCode() != other.hashCode())
            return false;
        if (!referencedProcessVariable.equals(other.referencedProcessVariable))
            return false;
        if (!parameterValues.equals(other.parameterValues))
            return false;
        return true;
    }

}
