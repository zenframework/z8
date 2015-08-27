package org.zenframework.z8.pde.refactoring.wordIterator;

import java.text.BreakIterator;
import java.text.CharacterIterator;

public class WordIterator extends BreakIterator {
    private WordBreakIterator fIterator;

    private int fIndex;

    public WordIterator() {
        fIterator = new WordBreakIterator();
        first();
    }

    @Override
    public int first() {
        fIndex = fIterator.first();
        return fIndex;
    }

    @Override
    public int last() {
        fIndex = fIterator.last();
        return fIndex;
    }

    @Override
    public int next(int n) {
        int next = 0;
        while(--n > 0 && next != DONE) {
            next = next();
        }
        return next;
    }

    @Override
    public int next() {
        fIndex = following(fIndex);
        return fIndex;
    }

    @Override
    public int previous() {
        fIndex = preceding(fIndex);
        return fIndex;
    }

    @Override
    public int preceding(int offset) {
        int first = fIterator.preceding(offset);
        if(isWhitespace(first, offset)) {
            int second = fIterator.preceding(first);
            if(second != DONE && !isDelimiter(second, first))
                return second;
        }
        return first;
    }

    @Override
    public int following(int offset) {
        int first = fIterator.following(offset);
        if(eatFollowingWhitespace(offset, first)) {
            int second = fIterator.following(first);
            if(isWhitespace(first, second))
                return second;
        }
        return first;
    }

    private boolean eatFollowingWhitespace(int offset, int exclusiveEnd) {
        if(exclusiveEnd == DONE || offset == DONE)
            return false;
        if(isWhitespace(offset, exclusiveEnd))
            return false;
        if(isDelimiter(offset, exclusiveEnd))
            return false;
        return true;
    }

    private boolean isDelimiter(int offset, int exclusiveEnd) {
        if(exclusiveEnd == DONE || offset == DONE)
            return false;
        CharSequence seq = fIterator.fText;
        while(offset < exclusiveEnd) {
            char ch = seq.charAt(offset);
            if(ch != '\n' && ch != '\r')
                return false;
            offset++;
        }
        return true;
    }

    private boolean isWhitespace(int offset, int exclusiveEnd) {
        if(exclusiveEnd == DONE || offset == DONE)
            return false;
        CharSequence seq = fIterator.fText;
        while(offset < exclusiveEnd) {
            char ch = seq.charAt(offset);
            if(!Character.isWhitespace(ch))
                return false;
            if(ch == '\n' || ch == '\r')
                return false;
            offset++;
        }
        return true;
    }

    @Override
    public int current() {
        return fIndex;
    }

    @Override
    public CharacterIterator getText() {
        return fIterator.getText();
    }

    public void setText(CharSequence newText) {
        fIterator.setText(newText);
        first();
    }

    @Override
    public void setText(CharacterIterator newText) {
        fIterator.setText(newText);
        first();
    }

    @Override
    public void setText(String newText) {
        setText((CharSequence)newText);
    }
}
