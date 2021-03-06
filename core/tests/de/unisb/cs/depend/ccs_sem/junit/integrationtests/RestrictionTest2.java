package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class RestrictionTest2 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "out!1.0 + a!2.0 + a!3.0 \\ {a}";
    }

    @Override
    protected boolean isMinimize() {
        return false;
    }

    @Override
    protected void addStates() {
        addState("out!1.0 + a!2.0 + a!3.0 \\ {a}");
        addState("0 \\ {a}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "out!1");
    }
}
