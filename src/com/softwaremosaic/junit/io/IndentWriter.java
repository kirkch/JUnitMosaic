package com.softwaremosaic.junit.io;


/**
 * Enhances an existing Writer with automatic prefixing of each line with an indentation string.
 * The level of indentation may be varied as required.
 */
public class IndentWriter {
    private static final String NEWLINE = System.getProperty("line.separator");

    private final StringBuilder buf;
    private final String        indentText;

    private int     indentationLevel      = 0;
    private boolean indentBeforeNextWrite = true;


    public IndentWriter() {
        this( new StringBuilder(), "  " );
    }

    public IndentWriter( StringBuilder buf, String indentText ) {
        this.indentText = indentText;
        this.buf        = buf;
    }

    public int incIndent() {
        return ++indentationLevel;
    }

    public int decIndent() {
        indentationLevel -= 1;

        return indentationLevel;
    }

    public void print( char c ) {
        append( c );
    }

    public void print( String txt ) {
        append(txt);
    }

    public void println( char c ) {
        print(c);
        newLine();
    }

    public void println( String txt ) {
        print(txt);
        newLine();
    }

    public void newLine() {
        buf.append(NEWLINE);

        indentBeforeNextWrite = true;
    }

    public void append( CharSequence csq ) {
        if ( csq.equals(NEWLINE) ) {
            newLine();
        } else if ( csq.length() != 0 ) {
            prefixOutputIffRequired();

            buf.append(csq);

            indentBeforeNextWrite = csq.toString().endsWith(NEWLINE);
        }
    }

    public void append( char c ) {
        prefixOutputIffRequired();

        buf.append( c );
    }

    public String toString() {
        return buf.toString();
    }

    private void prefixOutputIffRequired() {
        if ( indentBeforeNextWrite ) {
            for ( int i=0; i<indentationLevel; i++ ) {
                buf.append( indentText );
            }

            indentBeforeNextWrite = false;
        }
    }

}
