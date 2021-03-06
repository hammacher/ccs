package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.Map;

import org.omg.CORBA.UNKNOWN;

import de.unisb.cs.depend.ccs_sem.exceptions.ArithmeticError;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.IntervalRange;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.SetRange;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.BooleanValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConditionalValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstString;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.IntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;
import de.unisb.cs.depend.ccs_sem.utils.WeakIdentityHashMap;


/**
 * Represents a Parameter of a recursion variable.
 *
 * @author Clemens Hammacher
 */
public class Parameter {

    public static enum Type {
        UNKNOWN("Unused"),
        CHANNEL("Channel"),
        VALUE("Value"),
        STRINGVALUE("String value"),
        BOOLEANVALUE("Boolean value"),
        INTEGERVALUE("Integer value"),
        STRING("String (value/channel)");

        private String desc;

        private Type(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }
    }

    // the type is determined while parsing and UnknownRecursiveExpression.replaceRecursion()
    private Type type = Type.UNKNOWN;
    private final String name;
    private Map<Parameter, Object> connectedParameters = null;
    private final Range range;

    public Parameter(String name) {
        this(name, null);
    }

    public Parameter(String name, Range range) {
        this.name = name;
        this.range = range;
    }

    @Override
    public String toString() {
        if (range == null)
            return name;

        if (range instanceof IntervalRange || range instanceof SetRange)
            return name + ":" + range;

        return name + ":(" + range + ")";
    }

    public String getName() {
        return name;
    }

    public Range getRange() {
        return range;
    }

    /**
     * Tries to match this parameter with the given Value.
     * If this Parameter has UNKNOWN type, then the type is set according to the
     * value.
     * If the type is different from UNKNOWN and the type of the value does
     * not suit, the type is left unchanged, and a ParseException is thrown.
     *
     * @param value the Value to match this Parameter with
     * @param instantiation only used for output: do you try to instantiate the
     *                      parameter, or just change/specialize it
     * @throws ParseException if this parameter cannot be instantiated with the given value
     */
    public void match(Value value, boolean instantiation) throws ParseException {
        if (value instanceof ParameterReference) {
            final ParameterReference paramValue = (ParameterReference) value;
            final Type otherType = paramValue.getParam().type;
            addConnectedParameter(paramValue.getParam());
            paramValue.getParam().addConnectedParameter(this);
            switch (otherType) {
            case UNKNOWN:
                break;
            case CHANNEL:
            case VALUE:
            case BOOLEANVALUE:
            case INTEGERVALUE:
            case STRINGVALUE:
            case STRING:
                setType(otherType, instantiation);
                break;
            default:
                assert false;
                break;
            }
        } else if (value instanceof Channel) {
            setType(Type.CHANNEL, instantiation);
        } else if (value instanceof BooleanValue) {
            setType(Type.BOOLEANVALUE, instantiation);
        } else if (value instanceof IntegerValue) {
            setType(Type.INTEGERVALUE, instantiation);
        } else if (value instanceof ConstString) {
            setType(Type.STRING, instantiation);
        } else if (value instanceof ConditionalValue) {
            final ConditionalValue cond = (ConditionalValue) value;
            match(cond.getThenValue(), instantiation);
            match(cond.getElseValue(), instantiation);
        } else {
            // we should never get to here
            assert false;
        }
    }

    /**
     * Tries to set a new type, which has suit the old one.
     * Otherwise a ParseException is thrown.
     * @param newType the new type to set this parameter to
     * @param instantiation only used for output: do you try to instantiate the
     *                      parameter, or just change/specialize it
     * @throws ParseException if the old type and the new type don't fit together
     */
    @SuppressWarnings("fallthrough")
    public void setType(Type newType, boolean instantiation) throws ParseException {
        assert newType != Type.UNKNOWN;
        if (type == newType)
            return;

        boolean error = false;
        switch (type) {
        case UNKNOWN:
            // we accept every type
            break;
        case CHANNEL:
            if (newType == Type.STRING)
                // do not change
                return;
            error = true;
            break;
        case VALUE:
            switch (newType) {
            case STRING:
                newType = Type.STRINGVALUE;
                break; // inner switch!
            case BOOLEANVALUE:
            case INTEGERVALUE:
            case STRINGVALUE:
                // accept
                break; // inner switch!

            default:
                error = true;
            }
            break;

        case STRINGVALUE:
            if (newType == Type.STRING)
                return;
            // fall through here!
        case BOOLEANVALUE:
        case INTEGERVALUE:
            error |= newType != Type.VALUE;
            break;
        case STRING:
            error |= newType != Type.CHANNEL && newType != Type.STRINGVALUE;
            break;
        default:
            assert false;
        }

        if (error)
            throw new ParseException("Parameter \"" + name + "\" already has type \""
                + type + "\", cannot be " + (instantiation ? "instantiated with" : "changed to")
                + " \"" + newType + "\"", -1, -1);

        type = newType;
        if (connectedParameters != null) {
            // work on a copy of connectedParameters, otherwise we would get a loop
            final Map<Parameter, Object> parametersToSetType = connectedParameters;
            connectedParameters = null;
            try {
                for (final Parameter otherParam: parametersToSetType.keySet())
                    otherParam.setType(newType, instantiation);
            } finally {
                connectedParameters = parametersToSetType;
            }
        }
    }

    private void addConnectedParameter(Parameter param) {
        if (param == this)
            return;
        if (connectedParameters == null)
            connectedParameters = new WeakIdentityHashMap<Parameter, Object>(4);
        connectedParameters.put(param, null);
    }

    public Parameter instantiate(Map<Parameter, Value> parameters) throws ArithmeticError {
        if (range == null)
            return this;

        final Range newRange = range.instantiate(parameters);
        if (range.equals(newRange))
            return this;
        return new Parameter(name, newRange);
    }

    public Type getType() {
        return type;
    }

    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + (range == null ? 0 : range.hashCode(parameterOccurences));
        //result = prime * result + (type == null ? 0 : type.hashCode());

        // check if we have a recursion
        final ParameterOrProcessEqualsWrapper myWrapper = new ParameterOrProcessEqualsWrapper(this);
        Integer myNum = parameterOccurences.get(myWrapper);
        if (myNum != null)
            result = prime * result + myNum;

        myNum = parameterOccurences.size() + 1;
        parameterOccurences.put(myWrapper, myNum);

        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + (range == null ? 0 : range.hashCode());
        //result = prime * result + (type == null ? 0 : type.hashCode());
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
        final Parameter other = (Parameter) obj;
        if (!type.equals(other.type) && 
        		!(type == Type.UNKNOWN || other.type == Type.UNKNOWN) )
            return false;
        if (!name.equals(other.name))
            return false;
        if (range == null) {
            if (other.range != null)
                return false;
        } else if (!range.equals(other.range))
            return false;
        return true;
    }

    public boolean equals(Object obj, Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Parameter other = (Parameter) obj;
        if (!type.equals(other.type) && 
        		!(type == Type.UNKNOWN || other.type == Type.UNKNOWN) )
            return false;
        if (!name.equals(other.name))
            return false;
        if (range == null) {
            if (other.range != null)
                return false;
        } else if (!range.equals(other.range, parameterOccurences))
            return false;

        // ok, now the difficulty...
        final ParameterOrProcessEqualsWrapper myWrapper = new ParameterOrProcessEqualsWrapper(this);
        final ParameterOrProcessEqualsWrapper otherWrapper = new ParameterOrProcessEqualsWrapper(other);
        Integer myNum = parameterOccurences.get(myWrapper);
        final Integer otherNum = parameterOccurences.get(otherWrapper);
        if (myNum != null)
            return myNum.equals(otherNum);
        if (otherNum != null)
            return false;

        myNum = parameterOccurences.size()+1;
        parameterOccurences.put(myWrapper, myNum);
        parameterOccurences.put(otherWrapper, myNum);

        return true;
    }

}
