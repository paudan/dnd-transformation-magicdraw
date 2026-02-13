package org.ktu.dndtransformations.tests;

/**
 * Exception, casually thrown if parameters for tests are not set properly
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, Kaunas University of Technology, 2014-2015
 */
public class TestParameterException extends Exception {

    /**
     * Creates a new instance of <code>TestParameterException</code> without detail message.
     */
    public TestParameterException() {
    }

    /**
     * Constructs an instance of <code>TestParameterException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TestParameterException(String msg) {
        super(msg);
    }
}
