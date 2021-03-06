package de.unisb.cs.depend.ccs_sem.semantics.types.values;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ArithmeticError;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;


public class NotValue extends AbstractValue implements BooleanValue {

    // the type is checked by the parser
    private final Value negatedValue;

    private NotValue(Value negatedValue) {
        super();
        this.negatedValue = negatedValue;
    }

    public Value getNegatedValue() {
        return negatedValue;
    }

    public static Value create(Value negatedValue) {
        if (negatedValue instanceof ConstBooleanValue)
            return ConstBooleanValue.get(!((ConstBooleanValue)negatedValue).getValue());
        if (negatedValue instanceof NotValue)
            return ((NotValue)negatedValue).getNegatedValue();
        return new NotValue(negatedValue);
    }

    @Override
    public Value instantiate(Map<Parameter, Value> parameters) throws ArithmeticError {
        final Value newNegatedValue = negatedValue.instantiate(parameters);
        if (negatedValue.equals(newNegatedValue))
            return this;
        return create(newNegatedValue);
    }

    public String getStringValue() {
        final boolean needParenthesis = !(negatedValue instanceof ParameterReference
                || negatedValue instanceof ConstBooleanValue);
        return needParenthesis ? "!(" + negatedValue + ")" : "!" + negatedValue;
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return 13*31 + negatedValue.hashCode(parameterOccurences);
    }

    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NotValue other = (NotValue) obj;
        if (!negatedValue.equals(other.negatedValue, parameterOccurences))
            return false;
        return true;
    }

}
