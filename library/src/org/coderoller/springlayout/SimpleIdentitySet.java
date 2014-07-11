package org.coderoller.springlayout;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class SimpleIdentitySet<T> implements Set<T> {
    private Object[] mContent = new Object[16];
    private int mSize = 0;

    private void resize(int newLen) {
        if (mContent.length < newLen) {
            Object[] oldContent = mContent;
            mContent = new ViewConstraints[newLen];
            System.arraycopy(oldContent, 0, mContent, 0, oldContent.length);
        }
    }

    @Override
    public boolean add(T object) {
        if (!contains(object)) {
            if (mSize >= mContent.length) {
                resize(mContent.length * 2);
            }
            mContent[mSize] = object;
            mSize++;
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public T get(int i) {
        return (T) mContent[i];
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        int oldSize = mSize;
        for (T object : collection) {
            add(object);
        }
        return oldSize != mSize;
    }

    @Override
    public void clear() {
        for (int i = 0; i < mSize; i++) {
            mContent[i] = null;
        }
        mSize = 0;
    }

    @Override
    public boolean contains(Object object) {
        for (int i = 0; i < mSize; i++) {
            if (mContent[i] == object) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object object : collection) {
            if (!contains(object)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return mSize == 0;
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return mSize;
    }

    @Override
    public Object[] toArray() {
        return mContent;
    }

    @Override
    public <U> U[] toArray(U[] array) {
        throw new UnsupportedOperationException();
    }
}
