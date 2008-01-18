package de.unisb.cs.depend.ccs_sem.lexer.tokens;



public class Dot extends AbstractToken {

    public Dot(int position) {
        super(position, position);
    }

    @Override
    public String toString() {
        return ".";
    }

}
