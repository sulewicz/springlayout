package org.coderoller.springlayout;

/**
 * Helper class used for lazy evaluation of layout constraints.
 * 
 * @author sulewicz
 * 
 */
public class LayoutMath {
    final Value UNKNOWN_VALUE = new UnknownValue();
    final Value ZERO = new Constant(0);
    final Value ONE = new Constant(1);
    final Value TWO = new Constant(2);
    final Value HUNDRED = new Constant(100);
    final Value MINUS_ONE = new Constant(-1);

    /**
     * @return Empty ValueWrapper.
     */
    ValueWrapper wrap() {
        return new ValueWrapper();
    }

    /**
     * @param value
     *            Constant value to wrap.
     * @return ValueWrapper with given constant.
     */
    ValueWrapper wrap(int value) {
        return new ValueWrapper(variable(value));
    }

    /**
     * @param value
     *            Value object to wrap.
     * @return ValueWrapper with given Value object.
     */
    ValueWrapper wrap(Value value) {
        return new ValueWrapper(value);
    }

    /**
     * @return Unknown value object.
     */
    UnknownValue unknown() {
        return new UnknownValue();
    }

    /**
     * @param value
     *            Value to be stored in constant.
     * @return Constant object with given integer.
     */
    Constant constant(int value) {
        return new Constant(value);
    }
    
    /**
     * @return Variable object.
     */
    Variable variable() {
        return new Variable();
    }
    
    /**
     * @param value
     *            Value to be stored in variable.
     * @return Variable object with given integer.
     */
    Value variable(int value) {
        return new Variable(value);
    }

    abstract class Value {
        public final int INVALID = Integer.MIN_VALUE;
        protected int mValueCache = INVALID;

        final int getValue() {
            return (mValueCache == INVALID) ? (mValueCache = getValueImpl()) : mValueCache;
        }

        abstract int getValueImpl();

        abstract void invalidate();

        Value add(Value value) {
            return new BinaryOperationValue('+', this, value);
        }

        Value subtract(Value value) {
            return new BinaryOperationValue('-', this, value);
        }

        Value multiply(Value factor) {
            return new BinaryOperationValue('*', this, factor);
        }

        Value divide(Value denominator) {
            return new BinaryOperationValue('/', this, denominator);
        }
    }
    
    class Variable extends Value {
        private int mValue;

        Variable() {
            this(0);
        }

        Variable(int value) {
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
    }

    class ValueWrapper extends Value {
        private Value mValue;

        ValueWrapper() {
            this(UNKNOWN_VALUE);
        }

        ValueWrapper(Value value) {
            mValue = value;
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
            if (value.getValueObject() instanceof ValueWrapper) {
                // To avoid having depeer than one-level wrappers
                mValue = value.getValueObject();
            } else {
                mValue = value;
            }
        }

        void setValueObject(Value value) {
            invalidate();
            mValue = value;
        }

        Value getValueObject() {
            return mValue;
        }

        @Override
        public String toString() {
            return mValue.toString();
        }
    }

    class Constant extends Value {
        private final int mValue;

        Constant(int value) {
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
    }

    class UnknownValue extends Value {
        UnknownValue() {
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
    }

    class BinaryOperationValue extends Value {
        final char mOp;
        final Value mV1, mV2;

        BinaryOperationValue(char op, Value v1, Value v2) {
            mOp = op;
            mV1 = v1;
            mV2 = v2;
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
    }
}
