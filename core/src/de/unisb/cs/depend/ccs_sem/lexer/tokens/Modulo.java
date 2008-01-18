package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Modulo extends AbstractToken {

    public Modulo(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "%";
    }

}
