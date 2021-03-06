package de.unisb.cs.depend.ccs_sem.junit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.evaluators.Evaluator;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.parser.IParsingProblemListener;
import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ExpressionRepository;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RecursiveExpression.RecursiveExpressionAlphabetWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.AbstractValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;
import de.unisb.cs.depend.ccs_sem.utils.Bisimulation;
import de.unisb.cs.depend.ccs_sem.utils.Globals;
import de.unisb.cs.depend.ccs_sem.utils.StateNumerator;
import de.unisb.cs.depend.ccs_sem.utils.TransitionCounter;
import de.unisb.cs.depend.ccs_sem.utils.UniqueQueue;
import de.unisb.cs.depend.ccs_sem.utils.Bisimulation.Partition;


/**
 * This is a JUnit4 testcase that checks for any CCS expression if it creates
 * the correct transition system.
 *
 * @author Clemens Hammacher
 */
public abstract class IntegrationTest implements IParsingProblemListener {


    private List<String> states;
    private List<List<SimpleTrans>> transitions;
    private Program program;
    private List<ParsingProblem> parsingWarnings;
    private List<ParsingProblem> parsingErrors;
    private List<ParsingProblem> evaluationWarnings;
    private List<ParsingProblem> evaluationErrors;
    private boolean evaluating;

    // may be overridden to use another evaluator
    protected Evaluator getEvaluator() {
        return Globals.getDefaultEvaluator();
    }

    @Before
    public void initialize() {
        ExpressionRepository.reset();
        states = new ArrayList<String>();
        transitions = new ArrayList<List<SimpleTrans>>();

        addStates();
        addTransitions();

        // evaluate the expression
        final String expressionString = getExpressionString();
        final CCSParser parser = new CCSParser();
        parsingWarnings = new ArrayList<ParsingProblem>();
        parsingErrors = new ArrayList<ParsingProblem>();
        evaluationWarnings = new ArrayList<ParsingProblem>();
        evaluationErrors = new ArrayList<ParsingProblem>();
        evaluating = false;
        parser.addProblemListener(this);
        program = parser.parse(expressionString);

        if (states.size() == 0)
            fail("This testcase contains no nodes.");
    }

    private void evaluate() {
        if (program == null)
            Assert.fail("Program could not be parsed");
        evaluating = true;
        try {
            program.evaluate(getEvaluator());
        } catch (final InterruptedException e) {
            Assert.fail("Interrupted while evaluating.");
        }

        try {
            if (isMinimize())
                program.minimizeTransitions();
        } catch (final InterruptedException e) {
            Assert.fail("Interrupted while minimizing.");
        }

    }

    @After
    public void cleanUp() {
        ExpressionRepository.reset();
        program = null;
        states = null;
        transitions = null;
    }

    @Test
    public void checkErrorsAndWarnings() {
        if (program != null)
            evaluate();
        final StringBuilder sb = new StringBuilder();
        final String newLine = Globals.getNewline();
        if (parsingErrors.size() != getExpectedParsingErrors()) {
            sb.append("Number of parsing errors differs from expected number.").append(newLine);
            sb.append("Expected ").append(getExpectedParsingErrors());
            sb.append(", got ").append(parsingErrors.size()).append(newLine);
            for (final ParsingProblem problem: parsingErrors) {
                sb.append("    --> ").append(problem).append(newLine);
            }
        }
        if (parsingWarnings.size() != getExpectedParsingWarnings()) {
            if (sb.length() > 0)
                sb.append(newLine);
            sb.append("Number of parsing warnings differs from expected number.").append(newLine);
            sb.append("Expected ").append(getExpectedParsingWarnings());
            sb.append(", got ").append(parsingWarnings.size()).append(newLine);
            for (final ParsingProblem problem: parsingWarnings) {
                sb.append("    --> ").append(problem).append(newLine);
            }
        }
        if (evaluationErrors.size() != getExpectedEvaluationErrors()) {
            if (sb.length() > 0)
                sb.append(newLine);
            sb.append("Number of evaluation errors differs from expected number.").append(newLine);
            sb.append("Expected ").append(getExpectedEvaluationErrors());
            sb.append(", got ").append(evaluationErrors.size()).append(newLine);
            for (final ParsingProblem problem: evaluationErrors) {
                sb.append("    --> ").append(problem).append(newLine);
            }
        }
        if (evaluationWarnings.size() != getExpectedEvaluationWarnings()) {
            if (sb.length() > 0)
                sb.append(newLine);
            sb.append("Number of evaluation warnings differs from expected number.").append(newLine);
            sb.append("Expected ").append(getExpectedEvaluationWarnings());
            sb.append(", got ").append(evaluationWarnings.size()).append(newLine);
            for (final ParsingProblem problem: evaluationWarnings) {
                sb.append("    --> ").append(problem).append(newLine);
            }
        }
        if (sb.length() > 0) {
            fail(sb.toString());
        }
    }

    @Test
    public void checkStateNumber() {
        evaluate();
        final int found = StateNumerator.numerateStates(program.getExpression()).size();

        if (found != states.size())
            fail("The number of states does not match. Expected "
                + states.size() + ", found " + found);
    }

    @Test
    public void checkTransitionNumber() {
        evaluate();
        final int found = TransitionCounter.countTransitions(program.getExpression());
        int expected = 0;
        for (final List<SimpleTrans> trans: transitions)
            expected += trans.size();

        if (found != expected)
            fail("The number of transitions does not match. Expected "
                + expected + ", found " + found);
    }

    @Test
    public void checkWeakBisimilarity() throws InterruptedException {
        checkBisimilarity(false);
    }

    @Test
    public void checkStrongBisimilarity() throws InterruptedException {
        checkBisimilarity(true);
    }

    private void checkBisimilarity(boolean strong) throws InterruptedException {
        evaluate();
        final Expression expression = program.getExpression();
        final RebuiltExpression got = RebuiltExpression.create(expression);
        final RebuiltExpression expected = RebuiltExpression.create(states, transitions);
        final List<Expression> exprList = new ArrayList<Expression>(2);
        exprList.add(got);
        exprList.add(expected);
        final Map<Expression, Partition> partitions = Bisimulation.computePartitions(exprList, strong);
        if (!partitions.get(got).equals(partitions.get(expected)))
            fail("The transition system is not "
                + (strong ? "strong" : "weak") + " bisimilar to the expected one.");
    }

    @Test
    public void checkStatesExplicitely() {
        evaluate();
        final Expression expression = program.getExpression();

        // the queue of expressions to check
        final Queue<Integer> queue = new UniqueQueue<Integer>();
        queue.add(0);

        // mapping from stateNo to expression in the program
        final List<Expression> generatedExpr = new ArrayList<Expression>(states.size());
        generatedExpr.add(expression);

        // first check if the starting state is the same
        assertEquals("The starting states are different",
            states.get(0), expression.toString());

        while (!queue.isEmpty()) {
            final int stateNo = queue.poll();
            final List<SimpleTrans> expectedTransitions = transitions.get(stateNo);
            final Expression foundExpr = generatedExpr.get(stateNo);
            final List<Transition> foundTransitions = foundExpr.getTransitions();

            // now compare outTrans with the outgoing transitions of expr
            if (expectedTransitions.size() != foundTransitions.size())
                failAtState(stateNo, foundExpr,
                    "Number of outgoing transitions does not match");

            outer:
            for (final Transition trans: foundTransitions) {
                final String transLabel = trans.getAction().getLabel();
                final String targetLabel = trans.getTarget().toString();
                for (final SimpleTrans sTrans: expectedTransitions) {
                    boolean isError = false;
                    String label = states.get(sTrans.endNodeNo);
                    if (label.startsWith("error_")) {
                        isError = true;
                        label = label.substring(6);
                    } else if ("ERROR".equals(label)) {
                        isError = true;
                    }
                    if (sTrans.label.equals(transLabel) &&
                            label.equals(targetLabel) &&
                            isError == trans.getTarget().isError()) {
                        // found a matching transition
                        while (generatedExpr.size() <= sTrans.endNodeNo)
                            generatedExpr.add(null);
                        generatedExpr.set(sTrans.endNodeNo, trans.getTarget());
                        queue.add(sTrans.endNodeNo);
                        continue outer;
                    }
                }
                failAtState(stateNo, foundExpr, "Transition \""
                    + foundExpr.toString() + "\" --\"" + transLabel
                    + "\"-> \"" + targetLabel + "\" shouldn't be there");
            }
        }
    }

    @Test
    public void checkProgramOutputAndReconstruction() {
        evaluate();
        final String parsedExpressionString = program.toString(true);
        final Program reparsed = new CCSParser().parse(parsedExpressionString);
        if (reparsed == null)
            fail("Program could not be reparsed!");
        if (!program.getMainExpression().equals(reparsed.getMainExpression())) {
            final String nl = Globals.getNewline();
            final StringBuilder sb = new StringBuilder("Reparsed program differs from original.");
            sb.append(nl).append(" --> Original:").append(nl);
            sb.append(program);
            sb.append(nl).append(nl).append(" --> Reparsed:").append(nl);
            sb.append(reparsed);
            fail(sb.toString());
        }
    }

    private void failAtState(int stateNo, Expression foundExpr, String message) {
        final StringBuilder sb = new StringBuilder();
        sb.append(message).append(" at state \"").append(states.get(stateNo));
        sb.append('"').append(Globals.getNewline());
        sb.append("Expected Transitions:").append(Globals.getNewline());
        final List<SimpleTrans> expectedTrans = transitions.get(stateNo);
        if (expectedTrans.isEmpty())
            sb.append("    (none)").append(Globals.getNewline());
        else
            for (final SimpleTrans trans: expectedTrans) {
                sb.append("    - \"").append(trans.label);
                boolean isError = false;
                String label = states.get(trans.endNodeNo);
                if (label.startsWith("error_")) {
                    isError = true;
                    label = label.substring(6);
                } else if ("ERROR".equals(label)) {
                    isError = true;
                }
                sb.append(isError ? "\" to error state \"" : "\" to state \"");
                sb.append(label).append('"').append(Globals.getNewline());
            }
        sb.append("Found Transitions:").append(Globals.getNewline());
        if (foundExpr.getTransitions().isEmpty())
            sb.append("    (none)").append(Globals.getNewline());
        else
            for (final Transition trans: foundExpr.getTransitions()) {
                sb.append("    - \"").append(trans.getAction().getLabel());
                sb.append(trans.getTarget().isError()
                    ? "\" to error state \"" : "\" to state \"");
                sb.append(trans.getTarget()).append('"');
                sb.append(Globals.getNewline());
            }
        fail(sb.toString());
    }

    // can be overwritten to set whether the lts should be minimized
    protected boolean isMinimize() {
        return false;
    }

    protected int getExpectedParsingWarnings() {
        return 0;
    }

    protected int getExpectedParsingErrors() {
        return 0;
    }

    protected int getExpectedEvaluationWarnings() {
        return 0;
    }

    protected int getExpectedEvaluationErrors() {
        return 0;
    }

    protected String decode(String str) {
        final StringBuilder sb = new StringBuilder(str.length() * 3 / 2);

        final char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];

            if (c == '%') {
                int k = 0;
                while (++i < chars.length && (c = chars[i]) >= '0' && c <= '9')
                    k = 10*k + c - '0';
                --i;
                sb.append((char)k);
            } else
                sb.append(c);
        }

        return sb.toString();
    }

    protected void addState(String label) {
        states.add(label);
        transitions.add(new ArrayList<SimpleTrans>(3));
    }

    protected void addTransition(int startNodeNo, int endNodeNo, String label) {
        if (startNodeNo >= states.size())
            fail("Error in the testcase itself. Node " + startNodeNo
                + " is greater/equal to the number of nodes (" + states.size() + ")");
        if (endNodeNo >= states.size())
            fail("Error in the testcase itself. Node " + endNodeNo
                + " is greater/equal to the number of nodes (" + states.size() + ")");
        transitions.get(startNodeNo).add(new SimpleTrans(label, endNodeNo));
    }

    public void reportParsingProblem(ParsingProblem problem) {
        if (evaluating && problem.getType() == ParsingProblem.ERROR)
            evaluationErrors.add(problem);
        else if (!evaluating && problem.getType() == ParsingProblem.ERROR)
            parsingErrors.add(problem);
        else if (evaluating && problem.getType() == ParsingProblem.WARNING)
            evaluationWarnings.add(problem);
        else if (!evaluating && problem.getType() == ParsingProblem.WARNING)
            parsingWarnings.add(problem);
    }


    // the methods to be implemented by subclasses:

    protected abstract String getExpressionString();

    protected abstract void addStates();

    protected abstract void addTransitions();

    private static class SimpleTrans {
        public String label;
        int endNodeNo;

        public SimpleTrans(String label, int endNodeNo) {
            this.label = label;
            this.endNodeNo = endNodeNo;
        }
    }

    private static class RebuiltExpression extends Expression {

        private final List<Transition> transitions = new ArrayList<Transition>(2);
        private final String label;
        private final boolean isError;

        public RebuiltExpression(String label, boolean error) {
            this.isError = error;
            this.label = label;
        }

        public static RebuiltExpression create(Expression expression) {
            return create(expression, new HashMap<Expression, RebuiltExpression>());
        }

        private static RebuiltExpression create(Expression expression,
                Map<Expression,RebuiltExpression> map) {
            RebuiltExpression rebuilt = map.get(expression);
            if (rebuilt != null)
                return rebuilt;
            rebuilt = new RebuiltExpression(expression.toString(), expression.isError());
            map.put(expression, rebuilt);
            for (final Transition trans: expression.getTransitions())
                rebuilt.addTransition(new RebuiltTransition(
                    trans.getAction().getLabel(), create(trans.getTarget(), map)));
            rebuilt.evaluate();
            return rebuilt;
        }

        private void addTransition(RebuiltTransition rebuiltTransition) {
            transitions.add(rebuiltTransition);
        }

        public static RebuiltExpression create(List<String> states,
                List<List<SimpleTrans>> transitions) {
            final List<RebuiltExpression> createdExpressions =
                new ArrayList<RebuiltExpression>(states.size());

            // create all expressions
            for (String stateLabel: states) {
                boolean isError = false;
                if (stateLabel.startsWith("error_")) {
                    isError = true;
                    stateLabel = stateLabel.substring(6);
                } else if ("ERROR".equals(stateLabel)) {
                    isError = true;
                }
                createdExpressions.add(new RebuiltExpression(stateLabel, isError));
            }

            // then, create the transitions
            for (int i = 0; i < states.size(); ++i) {
                final RebuiltExpression expression = createdExpressions.get(i);
                final List<SimpleTrans> myTransitions = transitions.get(i);
                for (final SimpleTrans st: myTransitions)
                    expression.addTransition(new RebuiltTransition(st.label, createdExpressions.get(st.endNodeNo)));
                expression.evaluate();
            }

            return createdExpressions.get(0);
        }

        @Override
        protected List<Transition> evaluate0() {
            return transitions;
        }

        @Override
        public Collection<Expression> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public int hashCode(
                Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(Object obj,
                Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
            return this == obj;
        }

        @Override
        public Expression instantiate(Map<Parameter, Value> parameters) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression replaceRecursion(List<ProcessVariable> processVariables) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<Action, Action> getAlphabet(Set<RecursiveExpressionAlphabetWrapper> alreadyIncluded) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        protected boolean isError0() {
            return isError;
        }

    }

    private static class RebuiltTransition extends Transition {

        public RebuiltTransition(String label, Expression target) {
            super(new RebuiltAction(label), target);
        }

    }

    public static class RebuiltAction extends Action {

        private final String label;
        private final RebuiltChannel channel;

        public RebuiltAction(String label) {
            this.label = label;
            channel = new RebuiltChannel(label);
        }

        @Override
        public boolean equals(
                Object obj,
                Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
            if (obj instanceof RebuiltAction)
                return label.equals(((RebuiltAction)obj).label);
            return false;
        }

        @Override
        public Channel getChannel() {
            return channel;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public Value getValue() {
            return null;
        }

        @Override
        public int hashCode(
                Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
            return label.hashCode();
        }

        @Override
        public Action instantiate(Map<Parameter, Value> parameters) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression synchronizeWith(Action otherAction, Expression target) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return label;
        }

		@Override
		protected Action copySubAction() {
			return null;
		}

    }


    private static class RebuiltChannel extends AbstractValue implements Channel {

        private final String label;

        public RebuiltChannel(String label) {
            this.label = label;
        }

        public boolean sameChannel(Channel other) {
            if (other instanceof RebuiltChannel)
                return label.equals(((RebuiltChannel)other).label);
            return false;
        }

        public boolean equals(
                Object obj,
                Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
            if (obj instanceof RebuiltChannel)
                return label.equals(((RebuiltChannel)obj).label);
            return false;
        }

        public String getStringValue() {
            return label;
        }

        public int hashCode(
                Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
            return label.hashCode();
        }

        @Override
        public Channel instantiate(Map<Parameter, Value> parameters) {
            throw new UnsupportedOperationException();
        }

    }

}
