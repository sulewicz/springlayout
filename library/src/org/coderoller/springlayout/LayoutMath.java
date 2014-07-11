package org.coderoller.springlayout;

/**
 * Helper class used for lazy evaluation of layout constraints.
 * 
 * @author sulewicz
 * 
 */
public class LayoutMath {
    final UnknownValue UNKNOWN_VALUE = new UnknownValue();

    Variable mVariablePool;
    ValueWrapper mValueWrapperPool;
    BinaryOperationValue mBinaryOperationPool;

    public int getVariablePoolSize() {
        int size = 0;
        for (Variable v = mVariablePool; v != null; v = v.mPoolNext) {
            size++;
        }
        return size;
    }

    public int getValueWrapperPoolSize() {
        int size = 0;
        for (ValueWrapper v = mValueWrapperPool; v != null; v = v.mPoolNext) {
            size++;
        }
        return size;
    }

    public int getBinaryOperationPoolSize() {
        int size = 0;
        for (BinaryOperationValue v = mBinaryOperationPool; v != null; v = v.mPoolNext) {
            size++;
        }
        return size;
    }

    /**
     * @return Empty ValueWrapper.
     */
    public ValueWrapper wrap() {
        return wrap(UNKNOWN_VALUE);
    }

    /**
     * @param value
     *            Value to be wrapped.
     * @return Empty ValueWrapper.
     */
    public ValueWrapper wrap(Value val) {
        ValueWrapper ret;
        if (mValueWrapperPool != null) {
            ret = mValueWrapperPool;
            mValueWrapperPool = mValueWrapperPool.mPoolNext;
        } else {
            ret = new ValueWrapper();
        }
        ret.setValueObject(val);
        return ret;
    }

    /**
     * @return Unknown value object.
     */
    public UnknownValue unknown() {
        return UNKNOWN_VALUE;
    }

    /**
     * @return Variable object.
     */
    public Variable variable() {
        return variable(0);
    }

    /**
     * @param value
     *            Value to be stored in variable.
     * @return Variable object with given integer.
     */
    public Variable variable(int value) {
        Variable ret;
        if (mVariablePool != null) {
            ret = mVariablePool;
            ret.mValue = value;
            mVariablePool = mVariablePool.mPoolNext;
        } else {
            ret = new Variable(value);
        }
        return ret;
    }

    public BinaryOperationValue binaryOperation(char op, Value v1, Value v2) {
        BinaryOperationValue ret;
        if (mBinaryOperationPool != null) {
            ret = mBinaryOperationPool;
            mBinaryOperationPool.setOperation(op, v1, v2);
            mBinaryOperationPool = mBinaryOperationPool.mPoolNext;
        } else {
            ret = new BinaryOperationValue(op, v1, v2);
        }
        return ret;
    }

    public abstract class Value {
        public final int INVALID = Integer.MIN_VALUE;
        protected int mValueCache = INVALID;
        public int mRetainCount;

        final int getValue() {
            return (mValueCache == INVALID) ? (mValueCache = getValueImpl()) : mValueCache;
        }

        public abstract Value retain();

        abstract int getValueImpl();

        abstract void releaseImpl();

        abstract void addToPool();

        abstract void invalidate();

        public void release() {
            if (mRetainCount > 0) {
                mRetainCount--;
                if (mRetainCount == 0) {
                    invalidate();
                    releaseImpl();
                    addToPool();
                }
            }
        }

        public BinaryOperationValue add(Value value) {
            return binaryOperation('+', this, value);
        }

        public BinaryOperationValue subtract(Value value) {
            return binaryOperation('-', this, value);
        }

        public BinaryOperationValue multiply(Value factor) {
            return binaryOperation('*', this, factor);
        }

        public BinaryOperationValue divide(Value denominator) {
            return binaryOperation('/', this, denominator);
        }

        public BinaryOperationValue min(Value other) {
            return binaryOperation('m', this, other);
        }

        public BinaryOperationValue max(Value other) {
            return binaryOperation('M', this, other);
        }
    }

    public class Variable extends Value {
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
        public Variable retain() {
            mRetainCount++;
            return this;
        }

        @Override
        void addToPool() {
            mPoolNext = mVariablePool;
            mVariablePool = this;
        }
    }

    public class ValueWrapper extends Value {
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
                mValue = value.getValueObject().retain();
            } else {
                mValue = value.retain();
            }
        }

        public void setValueObject(Value value) {
            invalidate();
            if (mValue != null) {
                mValue.release();
            }
            mValue = value.retain();
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
        public ValueWrapper retain() {
            mRetainCount++;
            return this;
        }

        @Override
        void addToPool() {
            mPoolNext = mValueWrapperPool;
            mValueWrapperPool = this;
        }
    }

    public class UnknownValue extends Value {
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

        @Override
        public Value retain() {
            return this;
        }
    }

    public class BinaryOperationValue extends Value {
        char mOp;
        Value mV1, mV2;
        protected BinaryOperationValue mPoolNext;

        private BinaryOperationValue(char op, Value v1, Value v2) {
            setOperation(op, v1, v2);
        }

        void setOperation(char op, Value v1, Value v2) {
            mOp = op;
            mV1 = v1.retain();
            mV2 = v2.retain();
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
            case 'm':
                return Math.min(mV1.getValue(), mV2.getValue());
            case 'M':
                return Math.max(mV1.getValue(), mV2.getValue());
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
        public BinaryOperationValue retain() {
            mRetainCount++;
            return this;
        }

        @Override
        void addToPool() {
            mPoolNext = mBinaryOperationPool;
            mBinaryOperationPool = this;
        }
    }
}
