package org.coderoller.springlayoutsample.test;

import org.coderoller.springlayout.LayoutMath;

import junit.framework.TestCase;

public class LayoutMathTest extends TestCase {
    LayoutMath mTestMath;
    
    public void setUp() throws Exception {
        super.setUp();
        mTestMath = new LayoutMath();
    }
    
    public void testVariableCreation() {
        LayoutMath.Value v = mTestMath.variable(1);
        assertEquals(0, v.mRetainCount);
        assertEquals(0, mTestMath.getVariablePoolSize());
        v.mRetainCount++;
        v.release();
        assertEquals(0, v.mRetainCount);
        assertEquals(1, mTestMath.getVariablePoolSize());
        v = mTestMath.variable(1);
        assertEquals(0, v.mRetainCount);
        assertEquals(0, mTestMath.getVariablePoolSize());
    }
    
    public void testValueWrapperCreation() {
        LayoutMath.ValueWrapper v = mTestMath.wrap();
        assertEquals(1, v.mRetainCount);
        assertEquals(0, mTestMath.getValueWrapperPoolSize());
        v.release();
        assertEquals(0, v.mRetainCount);
        assertEquals(1, mTestMath.getValueWrapperPoolSize());
        v = mTestMath.wrap();
        assertEquals(1, v.mRetainCount);
        assertEquals(0, mTestMath.getValueWrapperPoolSize());
    }
    
    public void testBinaryOperationValueCreation() {
        LayoutMath.BinaryOperationValue v = mTestMath.binaryOperation('+', mTestMath.unknown(), mTestMath.unknown());
        assertEquals(0, v.mRetainCount);
        assertEquals(0, mTestMath.getBinaryOperationPoolSize());
        v.mRetainCount++;
        v.release();
        assertEquals(0, v.mRetainCount);
        assertEquals(1, mTestMath.getBinaryOperationPoolSize());
        v = mTestMath.binaryOperation('+', mTestMath.unknown(), mTestMath.unknown());
        assertEquals(0, v.mRetainCount);
        assertEquals(0, mTestMath.getBinaryOperationPoolSize());
    }
    
    public void testWrapperWrapping() {
        LayoutMath.Value v = mTestMath.variable(10);
        LayoutMath.ValueWrapper wrapper1 = mTestMath.wrap();
        LayoutMath.ValueWrapper wrapper2 = mTestMath.wrap();
        wrapper1.setValueObject(v);
        wrapper2.setValueObject(wrapper1);
        wrapper1.release();
        wrapper2.release();
        v.release();
        assertEquals(1, mTestMath.getVariablePoolSize());
        assertEquals(2, mTestMath.getValueWrapperPoolSize());
    }
    
    public void testValueWrapping() {
        LayoutMath.Value v = mTestMath.variable(10);
        LayoutMath.ValueWrapper wrapper = mTestMath.wrap();
        wrapper.setValueObject(v);
        wrapper.release();
        v.release();
        assertEquals(1, mTestMath.getVariablePoolSize());
        assertEquals(1, mTestMath.getValueWrapperPoolSize());
    }
    
    public void testSimpleBinaryOperations() {
        LayoutMath.Value x1 = mTestMath.variable(10);
        LayoutMath.Value x2 = mTestMath.variable(10);
        LayoutMath.Value w = mTestMath.variable(64);
        LayoutMath.Value y = mTestMath.wrap(x1.subtract(x2).subtract(w));
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
        LayoutMath.ValueWrapper wrapper = mTestMath.wrap();
        LayoutMath.Value sum = x2.subtract(x1).subtract(w1).subtract(w2);
        wrapper.setValueObject(sum);
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
        LayoutMath.ValueWrapper wrapper = mTestMath.wrap();
        wrapper.setValueObject(sum);
        wrapper.release();
        sum.release();
        w1.release();
        w2.release();
        x1.release();
        x2.release();
        assertEquals(2, mTestMath.getVariablePoolSize());
        assertEquals(3, mTestMath.getValueWrapperPoolSize());
        assertEquals(1, mTestMath.getBinaryOperationPoolSize());
    }

    public void tearDown() throws Exception {
        super.tearDown();
        mTestMath = null;
    }
}
