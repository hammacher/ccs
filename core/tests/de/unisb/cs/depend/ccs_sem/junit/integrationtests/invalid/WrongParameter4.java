package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class WrongParameter4 extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[ch] = ch!4711; X[1]";
    }

    @Override
    protected int getExpectedParsingErrors() {
        return 1;
    }

    @Override
    protected boolean expectParsedProgram() {
        return false;
    }

}
