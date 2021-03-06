package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class SimpleAction extends Action {

    private final Channel channel;

    public SimpleAction(Channel channel) {
        super();
        this.channel = channel;
    }

    @Override
    public String getLabel() {
        return channel.getStringValue();
    }

    @Override
    public Action instantiate(Map<Parameter, Value> parameters) {
        final Channel newChannel = channel.instantiate(parameters);
        if (channel.equals(newChannel))
            return this;

        return new SimpleAction(newChannel);
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        // this action cannot synchronize
        return null;
    }

    @Override
    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return channel.hashCode(parameterOccurences);
    }

    @Override
    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SimpleAction other = (SimpleAction) obj;
        if (!channel.equals(other.channel, parameterOccurences))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return channel.toString();
    }

	@Override
	protected Action copySubAction() {
		return new SimpleAction(channel);
	}

}
