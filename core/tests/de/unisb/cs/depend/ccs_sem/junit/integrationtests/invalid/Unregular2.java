package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import org.junit.Assert;
import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class Unregular2 {

    private static final String term = "X[a] = out!a | Y[true] | Y[false]; "
        + "Y[n] = uepsilon!n . when n (fertisch | X[n ? 17 : 71]); "
        + "X[0]";

    @Test
    public void checkForError() throws ParseException, LexException {
        final Program prog = new CCSParser().parse(term);
        Assert.assertFalse("Expression should not be regular.", prog.isRegular());
    }

}