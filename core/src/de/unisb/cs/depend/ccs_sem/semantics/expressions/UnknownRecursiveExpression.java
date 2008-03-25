package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;
import de.unisb.cs.depend.ccs_sem.utils.Globals;


/**
 * Is later (by replaceRecursion()) replaced by a RecursiveExpression.
 *
 * @author Clemens Hammacher
 */
public class UnknownRecursiveExpression extends Expression {

    private final String name;
    private final List<Value> parameters;
    private int startPos;
    private int endPos;

    public UnknownRecursiveExpression(String name, List<Value> parameters, int startPos, int endPos) {
        super();
        this.name = name;
        this.parameters = parameters;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public UnknownRecursiveExpression(String name) {
        super();
        this.name = name;
        this.parameters = Collections.emptyList();
    }

    @Override
    public Collection<Expression> getChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<Transition> evaluate0() {
        throw new UnsupportedOperationException();
    }

    /**
     * Replaces this {@link UnknownRecursiveExpression} by either a {@link RecursiveExpression} or
     * throws a {@link ParseException}.
     */
    @Override
    public Expression replaceRecursion(List<ProcessVariable> processVariables) throws ParseException {
        for (final ProcessVariable proc: processVariables) {
            if (proc.getName().equals(name) && proc.getParamCount() == parameters.size()) {
                // check if parameters match
                // this possibly throws a ParseException
                try {
                    proc.checkMatch(parameters);
                } catch (final ParseException e) {
                    throw new ParseException(e.getMessage(), startPos, endPos);
                }
                final RecursiveExpression newExpr = new RecursiveExpression(proc, parameters);
                return ExpressionRepository.getExpression(newExpr);
            }
        }

        // search for possible matches
        final List<ProcessVariable> proposals = new ArrayList<ProcessVariable>();
        for (final ProcessVariable proc: processVariables)
            if (proc.getName().equalsIgnoreCase(name))
                proposals.add(proc);
        final StringBuilder sb = new StringBuilder("Unknown process variable ");
        sb.append(this);
        if (proposals.size() > 1) {
            sb.append(". Did you mean");
            if (proposals.size() == 1)
                sb.append(' ').append(proposals.get(0).getFullName()).append('?');
            else
                for (final ProcessVariable prop: proposals)
                    sb.append(Globals.getNewline()).append("  - ").append(prop.getFullName());
        }

        throw new ParseException(sb.toString(), startPos, endPos);
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> params) {
        assert false;
        return null;
    }

    @Override
    protected boolean isError0() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        if (parameters.size() == 0)
            return name;

        return name+parameters;
    }

    @Override
    protected int hashCode0() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + name.hashCode();
        result = PRIME * result + parameters.hashCode();
        result = PRIME * result + startPos;
        result = PRIME * result + endPos;
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
        final UnknownRecursiveExpression other = (UnknownRecursiveExpression) obj;
        // hashCode is cached, so we compare it first (it's cheap)
        if (hashCode() != other.hashCode())
            return false;
        if (startPos != other.startPos)
            return false;
        if (endPos != other.endPos)
            return false;
        if (!name.equals(other.name))
            return false;
        if (!parameters.equals(other.parameters))
            return false;
        return true;
    }

}
