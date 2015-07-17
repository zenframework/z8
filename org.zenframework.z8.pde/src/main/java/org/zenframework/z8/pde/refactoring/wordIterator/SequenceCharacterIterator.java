package org.zenframework.z8.pde.refactoring.wordIterator;

import java.text.CharacterIterator;

public class SequenceCharacterIterator implements CharacterIterator {
    private int fIndex = -1;
    private final CharSequence fSequence;
    private final int fFirst;
    private final int fLast;

    private void invariant() {}

    public SequenceCharacterIterator(CharSequence sequence) {
        this(sequence, 0);
    }

    public SequenceCharacterIterator(CharSequence sequence, int first) throws IllegalArgumentException {
        this(sequence, first, sequence.length());
    }

    public SequenceCharacterIterator(CharSequence sequence, int first, int last) throws IllegalArgumentException {
        if(sequence == null)
            throw new NullPointerException();
        if(first < 0 || first > last)
            throw new IllegalArgumentException();
        if(last > sequence.length())
            throw new IllegalArgumentException();
        fSequence = sequence;
        fFirst = first;
        fLast = last;
        fIndex = first;
        invariant();
    }

    @Override
    public char first() {
        return setIndex(getBeginIndex());
    }

    @Override
    public char last() {
        if(fFirst == fLast)
            return setIndex(getEndIndex());
        else
            return setIndex(getEndIndex() - 1);
    }

    @Override
    public char current() {
        if(fIndex >= fFirst && fIndex < fLast)
            return fSequence.charAt(fIndex);
        else
            return DONE;
    }

    @Override
    public char next() {
        return setIndex(Math.min(fIndex + 1, getEndIndex()));
    }

    @Override
    public char previous() {
        if(fIndex > getBeginIndex()) {
            return setIndex(fIndex - 1);
        }
        else {
            return DONE;
        }
    }

    @Override
    public char setIndex(int position) {
        if(position >= getBeginIndex() && position <= getEndIndex())
            fIndex = position;
        else
            throw new IllegalArgumentException();
        invariant();
        return current();
    }

    @Override
    public int getBeginIndex() {
        return fFirst;
    }

    @Override
    public int getEndIndex() {
        return fLast;
    }

    @Override
    public int getIndex() {
        return fIndex;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch(CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
