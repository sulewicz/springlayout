package org.coderoller.springlayoutsample.test;

import junit.framework.TestCase;

import org.coderoller.springlayout.LayoutMath;

public class LayoutMathTest extends TestCase {
    LayoutMath mTestMath;

    public void setUp() throws Exception {
        super.setUp();
        mTestMath = new LayoutMath();
    }

    public void testVariableCreation() {
        LayoutMath.Value v = mTestMath.variable(1).retain();
        assertEquals(1, v.mRetainCount);
        assertEquals(0, mTestMath.getVariablePoolSize());
        v.release();
        assertEquals(0, v.mRetainCount);
        assertEquals(1, mTestMath.getVariablePoolSize());
        v = mTestMath.variable(1).retain();
        assertEquals(1, v.mRetainCount);
        assertEquals(0, mTestMath.getVariablePoolSize());
    }

    public void testValueWrapperCreation() {
        LayoutMath.ValueWrapper v = mTestMath.wrap().retain();
        assertEquals(1, v.mRetainCount);
        assertEquals(0, mTestMath.getValueWrapperPoolSize());
        v.release();
        assertEquals(0, v.mRetainCount);
        assertEquals(1, mTestMath.getValueWrapperPoolSize());
        v = mTestMath.wrap().retain();
        assertEquals(1, v.mRetainCount);
        assertEquals(0, mTestMath.getValueWrapperPoolSize());
    }

    public void testBinaryOperationValueCreation() {
        LayoutMath.BinaryOperationValue v = mTestMath.binaryOperation('+', mTestMath.unknown(), mTestMath.unknown()).retain();
        assertEquals(1, v.mRetainCount);
        assertEquals(0, mTestMath.getBinaryOperationPoolSize());
        v.release();
        assertEquals(0, v.mRetainCount);
        assertEquals(1, mTestMath.getBinaryOperationPoolSize());
        v = mTestMath.binaryOperation('+', mTestMath.unknown(), mTestMath.unknown()).retain();
        assertEquals(1, v.mRetainCount);
        assertEquals(0, mTestMath.getBinaryOperationPoolSize());
    }

    public void testWrapperWrapping() {
        LayoutMath.Value v = mTestMath.variable(10);
        LayoutMath.ValueWrapper wrapper1 = mTestMath.wrap();
        LayoutMath.ValueWrapper wrapper2 = mTestMath.wrap().retain();
        wrapper1.setValueObject(v);
        wrapper2.setValueObject(wrapper1);
        wrapper2.release();
        assertEquals(1, mTestMath.getVariablePoolSize());
        assertEquals(2, mTestMath.getValueWrapperPoolSize());
    }

    public void testValueWrapping() {
        LayoutMath.Value v = mTestMath.variable(10);
        LayoutMath.ValueWrapper wrapper = mTestMath.wrap().retain();
        wrapper.setValueObject(v);
        wrapper.release();
        assertEquals(1, mTestMath.getVariablePoolSize());
        assertEquals(1, mTestMath.getValueWrapperPoolSize());
    }

    public void testSimpleBinaryOperations() {
        LayoutMath.Value x1 = mTestMath.variable(10);
        LayoutMath.Value x2 = mTestMath.variable(10);
        LayoutMath.Value w = mTestMath.variable(64);
        LayoutMath.Value y = mTestMath.wrap(x1.subtract(x2).subtract(w)).retain();
        y.release();
        assertEquals(3, mTestMath.getVariablePoolSize());
        assertEquals(2, mTestMath.getBinaryOperationPoolSize());
        assertEquals(1, mTestMath.getValueWrapperPoolSize());
    }

    public void testComplexBinaryOperations() {
        LayoutMath.Value x1 = mTestMath.variable(10);
        LayoutMath.Value x2 = mTestMath.variable(320);
        LayoutMath.Value w1 = mTestMath.variable(64);
        LayoutMath.Value w2 = mTestMath.variable(64);
        LayoutMath.ValueWrapper wrapper = mTestMath.wrap().retain();
        wrapper.setValueObject(x2.subtract(x1).subtract(w1).subtract(w2));
        wrapper.release();
        assertEquals(4, mTestMath.getVariablePoolSize());
        assertEquals(1, mTestMath.getValueWrapperPoolSize());
        assertEquals(3, mTestMath.getBinaryOperationPoolSize());
    }

    public void testOperationsOnWrapper() {
        LayoutMath.Value x1 = mTestMath.variable(10);
        LayoutMath.Value x2 = mTestMath.variable(320);
        LayoutMath.ValueWrapper w1 = mTestMath.wrap();
        w1.setValueObject(x1);
        LayoutMath.ValueWrapper w2 = mTestMath.wrap();
        w2.setValueObject(x2);
        LayoutMath.Value sum = w1.add(w2);
        LayoutMath.ValueWrapper wrapper = mTestMath.wrap(sum).retain();
        wrapper.release();
        assertEquals(2, mTestMath.getVariablePoolSize());
        assertEquals(3, mTestMath.getValueWrapperPoolSize());
        assertEquals(1, mTestMath.getBinaryOperationPoolSize());
    }

    public void tearDown() throws Exception {
        super.tearDown();
        mTestMath = null;
    }
}
