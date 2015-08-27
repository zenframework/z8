package org.zenframework.z8.pde.refactoring.wordIterator;

import java.text.BreakIterator;
import java.text.CharacterIterator;

public class WordBreakIterator extends BreakIterator {
    protected static abstract class Run {
        protected int length;

        public Run() {
            init();
        }

        protected boolean consume(char ch) {
            if(isValid(ch)) {
                length++;
                return true;
            }
            return false;
        }

        protected abstract boolean isValid(char ch);

        protected void init() {
            length = 0;
        }
    }

    static final class Whitespace extends Run {
        @Override
        protected boolean isValid(char ch) {
            return Character.isWhitespace(ch) && ch != '\n' && ch != '\r';
        }
    }

    static final class LineDelimiter extends Run {
        private char fState;
        private static final char INIT = '\0';
        private static final char EXIT = '\1';

        @Override
        protected void init() {
            super.init();
            fState = INIT;
        }

        @Override
        protected boolean consume(char ch) {
            if(!isValid(ch) || fState == EXIT)
                return false;
            if(fState == INIT) {
                fState = ch;
                length++;
                return true;
            }
            else if(fState != ch) {
                fState = EXIT;
                length++;
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        protected boolean isValid(char ch) {
            return ch == '\n' || ch == '\r';
        }
    }

    static final class Identifier extends Run {
        @Override
        protected boolean isValid(char ch) {
            return Character.isJavaIdentifierPart(ch);
        }
    }

    static final class CamelCaseIdentifier extends Run {
        private static final int S_INIT = 0;
        private static final int S_LOWER = 1;
        private static final int S_ONE_CAP = 2;
        private static final int S_ALL_CAPS = 3;
        private static final int S_EXIT = 4;
        private static final int S_EXIT_MINUS_ONE = 5;

        private static final int K_INVALID = 0;
        private static final int K_LOWER = 1;
        private static final int K_UPPER = 2;
        private static final int K_OTHER = 3;
        private int fState;
        private final static int[][] MATRIX = new int[][] { { S_EXIT, S_LOWER, S_ONE_CAP, S_LOWER }, // S_INIT
                { S_EXIT, S_LOWER, S_EXIT, S_LOWER }, // S_LOWER
                { S_EXIT, S_LOWER, S_ALL_CAPS, S_LOWER }, // S_ONE_CAP
                { S_EXIT, S_EXIT_MINUS_ONE, S_ALL_CAPS, S_LOWER }, // S_ALL_CAPS
        };

        @Override
        protected void init() {
            super.init();
            fState = S_INIT;
        }

        @Override
        protected boolean consume(char ch) {
            int kind = getKind(ch);
            fState = MATRIX[fState][kind];
            switch(fState) {
            case S_LOWER:
            case S_ONE_CAP:
            case S_ALL_CAPS:
                length++;
                return true;
            case S_EXIT:
                return false;
            case S_EXIT_MINUS_ONE:
                length--;
                return false;
            default:
                return false;
            }
        }

        private int getKind(char ch) {
            if(Character.isUpperCase(ch))
                return K_UPPER;
            if(Character.isLowerCase(ch))
                return K_LOWER;
            if(Character.isJavaIdentifierPart(ch)) // _, digits...
                return K_OTHER;
            return K_INVALID;
        }

        @Override
        protected boolean isValid(char ch) {
            return Character.isJavaIdentifierPart(ch);
        }
    }

    static final class Other extends Run {
        @Override
        protected boolean isValid(char ch) {
            return !Character.isWhitespace(ch) && !Character.isJavaIdentifierPart(ch);
        }
    }

    private static final Run WHITESPACE = new Whitespace();
    private static final Run DELIMITER = new LineDelimiter();
    private static final Run CAMELCASE = new CamelCaseIdentifier(); // new
                                                                    // Identifier();
    private static final Run OTHER = new Other();
    protected final BreakIterator fIterator;
    protected CharSequence fText;
    private int fIndex;

    public WordBreakIterator() {
        fIterator = BreakIterator.getWordInstance();
        fIndex = fIterator.current();
    }

    @Override
    public int current() {
        return fIndex;
    }

    @Override
    public int first() {
        fIndex = fIterator.first();
        return fIndex;
    }

    @Override
    public int following(int offset) {
        if(offset == getText().getEndIndex())
            return DONE;
        int next = fIterator.following(offset);
        if(next == DONE)
            return DONE;
        Run run = consumeRun(offset);
        return offset + run.length;
    }

    private Run consumeRun(int offset) {
        char ch = fText.charAt(offset);
        int length = fText.length();
        Run run = getRun(ch);
        while(run.consume(ch) && offset < length - 1) {
            offset++;
            ch = fText.charAt(offset);
        }
        return run;
    }

    private Run getRun(char ch) {
        Run run;
        if(WHITESPACE.isValid(ch))
            run = WHITESPACE;
        else if(DELIMITER.isValid(ch))
            run = DELIMITER;
        else if(CAMELCASE.isValid(ch))
            run = CAMELCASE;
        else if(OTHER.isValid(ch))
            run = OTHER;
        else {
            return null;
        }
        run.init();
        return run;
    }

    @Override
    public CharacterIterator getText() {
        return fIterator.getText();
    }

    @Override
    public boolean isBoundary(int offset) {
        if(offset == getText().getBeginIndex())
            return true;
        else
            return following(offset - 1) == offset;
    }

    @Override
    public int last() {
        fIndex = fIterator.last();
        return fIndex;
    }

    @Override
    public int next() {
        fIndex = following(fIndex);
        return fIndex;
    }

    @Override
    public int next(int n) {
        return fIterator.next(n);
    }

    @Override
    public int preceding(int offset) {
        if(offset == getText().getBeginIndex())
            return DONE;
        if(isBoundary(offset - 1))
            return offset - 1;
        int previous = offset - 1;
        do {
            previous = fIterator.preceding(previous);
        } while(!isBoundary(previous));
        int last = DONE;
        while(previous < offset) {
            last = previous;
            previous = following(previous);
        }
        return last;
    }

    @Override
    public int previous() {
        fIndex = preceding(fIndex);
        return fIndex;
    }

    @Override
    public void setText(String newText) {
        setText((CharSequence)newText);
    }

    public void setText(CharSequence newText) {
        fText = newText;
        fIterator.setText(new SequenceCharacterIterator(newText));
        first();
    }

    @Override
    public void setText(CharacterIterator newText) {
        if(newText instanceof CharSequence) {
            fText = (CharSequence)newText;
            fIterator.setText(newText);
            first();
        }
        else {
            throw new UnsupportedOperationException("CharacterIterator not supported");
        }
    }
}
