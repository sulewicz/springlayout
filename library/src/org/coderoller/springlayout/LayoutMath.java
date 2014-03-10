package org.coderoller.springlayout;

/**
 * Helper class used for lazy evaluation of layout constraints.
 * 
 * @author sulewicz
 * 
 */
public class LayoutMath {
    final UnknownValue UNKNOWN_VALUE = new UnknownValue();
    final Value ZERO = new Constant(0);
    final Value ONE = new Constant(1);
    final Value TWO = new Constant(2);
    final Value HUNDRED = new Constant(100);
    final Value MINUS_ONE = new Constant(-1);
   
    Constant mConstantsPool;
    Variable mVariablePool;
    ValueWrapper mValueWrapperPool;
    BinaryOperationValue mBinaryOperationPool;
    
    /**
     * @return Empty ValueWrapper.
     */
    ValueWrapper wrap() {
        ValueWrapper ret;
        if (mValueWrapperPool != null) {
            ret = mValueWrapperPool;
            ret.mValue = UNKNOWN_VALUE;
            mValueWrapperPool = mValueWrapperPool.mPoolNext;
            ret.mPoolNext = null;
        } else {
            ret = new ValueWrapper();
        }
        return ret;
    }

    /**
     * @param value
     *            Constant value to wrap.
     * @return ValueWrapper with given constant.
     */
    ValueWrapper wrap(int value) {
        ValueWrapper ret = wrap();
        ret.setValueObject(variable(value));
        return ret;
    }

    /**
     * @param value
     *            Value object to wrap.
     * @return ValueWrapper with given Value object.
     */
    ValueWrapper wrap(Value value) {
        ValueWrapper ret = wrap();
        ret.setValueObject(value);
        return ret;
    }

    /**
     * @return Unknown value object.
     */
    UnknownValue unknown() {
        return UNKNOWN_VALUE;
    }

    /**
     * @param value
     *            Value to be stored in constant.
     * @return Constant object with given integer.
     */
    Constant constant(int value) {
        Constant ret;
        if (mConstantsPool != null) {
            ret = mConstantsPool;
            ret.mValue = value;
            mConstantsPool = mConstantsPool.mPoolNext;
            ret.mPoolNext = null;
        } else {
            ret = new Constant(value);
        }
        return ret;
    }
    
    /**
     * @return Variable object.
     */
    Variable variable() {
        return variable(0);
    }
    
    /**
     * @param value
     *            Value to be stored in variable.
     * @return Variable object with given integer.
     */
    Variable variable(int value) {
        Variable ret;
        if (mVariablePool != null) {
            ret = mVariablePool;
            ret.mValue = 0;
            mVariablePool = mVariablePool.mPoolNext;
            ret.mPoolNext = null;
        } else {
            ret = new Variable(value);
        }
        return ret;
    }
    
    BinaryOperationValue binaryOperation(char op, Value v1, Value v2) {
        BinaryOperationValue ret;
        if (mBinaryOperationPool != null) {
            ret = mBinaryOperationPool;
            mBinaryOperationPool.setOperation(op, v1, v2);
            mBinaryOperationPool = mBinaryOperationPool.mPoolNext;
            ret.mPoolNext = null;
        } else {
            ret = new BinaryOperationValue(op, v1, v2);
        }
        return ret;
    }

    abstract class Value {
        public final int INVALID = Integer.MIN_VALUE;
        protected int mValueCache = INVALID;
        protected int mRetainCount;

        final int getValue() {
            return (mValueCache == INVALID) ? (mValueCache = getValueImpl()) : mValueCache;
        }

        abstract int getValueImpl();
        abstract void releaseImpl();
        abstract void addToPool();

        abstract void invalidate();
        
        void release() {
            mRetainCount--;
            if (mRetainCount == 0) {
                invalidate();
                releaseImpl();
                addToPool();
            }
        }

        Value add(Value value) {
            return binaryOperation('+', this, value);
        }

        Value subtract(Value value) {
            return binaryOperation('-', this, value);
        }

        Value multiply(Value factor) {
            return binaryOperation('*', this, factor);
        }

        Value divide(Value denominator) {
            return binaryOperation('/', this, denominator);
        }
    }
    
    class Variable extends Value {
        private int mValue;
        protected Variable mPoolNext;

        private Variable() {
            this(0);
        }

        private Variable(int value) {
            mValue = value;
        }

        @Override
        int getValueImpl() {
            return mValue;
        }
        
        void setValue(int value) {
            mValueCache = INVALID;
            mValue = value;
        }

        @Override
        void invalidate() {
            mValueCache = INVALID;
        }
        
        @Override
        public String toString() {
            return String.valueOf(mValue);
        }

        @Override
        void releaseImpl() {
        }

        @Override
        void addToPool() {
            mPoolNext = mVariablePool;
            mVariablePool = this;
        }
    }

    class ValueWrapper extends Value {
        private Value mValue = UNKNOWN_VALUE;
        protected ValueWrapper mPoolNext;

        private ValueWrapper() {
        }

        @Override
        void invalidate() {
            if (mValueCache != INVALID) {
                mValueCache = INVALID;
                mValue.invalidate();
            }
        }

        @Override
        int getValueImpl() {
            return mValue.getValue();
        }

        void setValueObject(ValueWrapper value) {
            invalidate();
            if (mValue != null) {
                mValue.release();
            }
            if (value.getValueObject() instanceof ValueWrapper) {
                // To avoid having depeer than one-level wrappers
                mValue = value.getValueObject();
            } else {
                mValue = value;
            }
            mValue.mRetainCount++;
        }

        void setValueObject(Value value) {
            invalidate();
            if (mValue != null) {
                mValue.release();
            }
            mValue = value;
            mValue.mRetainCount++;
        }

        Value getValueObject() {
            return mValue;
        }

        @Override
        public String toString() {
            return mValue.toString();
        }

        @Override
        void releaseImpl() {
            mValue.release();
            mValue = UNKNOWN_VALUE;
        }

        @Override
        void addToPool() {
            mPoolNext = mValueWrapperPool;
            mValueWrapperPool = this;
        }
    }

    class Constant extends Value {
        private int mValue;
        protected Constant mPoolNext;

        private Constant(int value) {
            mValue = value;
        }

        @Override
        int getValueImpl() {
            return mValue;
        }

        @Override
        void invalidate() {
            mValueCache = INVALID;
        }

        @Override
        public String toString() {
            return String.valueOf(mValue);
        }

        @Override
        void releaseImpl() {
        }

        @Override
        void addToPool() {
            mPoolNext = mConstantsPool;
            mConstantsPool = this;
        }
    }

    class UnknownValue extends Value {
        private UnknownValue() {
        }

        @Override
        int getValueImpl() {
            throw new IllegalStateException("Exact value not known");
        }

        @Override
        void invalidate() {
        }

        @Override
        public String toString() {
            return "?";
        }

        @Override
        void releaseImpl() {
        }

        @Override
        void addToPool() {
        }
    }

    class BinaryOperationValue extends Value {
        char mOp;
        Value mV1, mV2;
        protected BinaryOperationValue mPoolNext;

        private BinaryOperationValue(char op, Value v1, Value v2) {
            setOperation(op, v1, v2);
        }
        
        void setOperation(char op, Value v1, Value v2) {
            mOp = op;
            mV1 = v1;
            mV2 = v2;
            mV1.mRetainCount++;
            mV2.mRetainCount++;
        }

        @Override
        void invalidate() {
            if (mValueCache != INVALID) {
                mValueCache = INVALID;
                mV1.invalidate();
                mV2.invalidate();
            }
        }

        @Override
        int getValueImpl() {
            switch (mOp) {
            case '+':
                return mV1.getValue() + mV2.getValue();
            case '-':
                return mV1.getValue() - mV2.getValue();
            case '*':
                return mV1.getValue() * mV2.getValue();
            case '/':
                return mV1.getValue() / mV2.getValue();
            default:
                throw new IllegalArgumentException("Unknown operation: " + mOp);
            }
        }

        @Override
        public String toString() {
            return "( " + mV1.toString() + " " + mOp + " " + mV2.toString() + " )";
        }

        @Override
        void releaseImpl() {
            mV1.release();
            mV2.release();
            mV1 = UNKNOWN_VALUE;
            mV2 = UNKNOWN_VALUE;
        }

        @Override
        void addToPool() {
            mPoolNext = mBinaryOperationPool;
            mBinaryOperationPool = this;
        }
    }
}
