package de.unisb.cs.depend.ccs_sem.semantics.types.ranges;

import java.util.Collection;

import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


/**
 * This interface represents a parameter range.
 *
 * @author Clemens Hammacher
 */
public interface Range {

    /**
     * @return a collection (list, set, ...) of all values that are allowed in
     * this range. At the time this method is called, there should be no more
     * parameters in the range, so it returns just ConstantValues.
     */
    Collection<ConstantValue> getPossibleValues();

    /**
     * Build a new Range that is the subtraction of this and the other Range.
     * @param otherRange the Range to subtract from this one
     * @return the new built Range
     */
    Range subtract(Range otherRange);

    /**
     * Build a new Range that is the addition of this and the other Range.
     * @param otherRange the Range to add to this one
     * @return the new built Range
     */
    Range add(Range otherRange);

    /**
     * Checks whether this Range contains a specific Value.
     * @param value the Value to check
     * @return <code>true</code>, if the given Value is within this Range
     */
    boolean contains(Value value);

    /**
     * Checks whether this Range restricts the possible values to a finite set.
     * @return <code>true</code>, if this Range restricts the possible values
     *         to a finite set
     */
    boolean isRangeRestricted();

}
