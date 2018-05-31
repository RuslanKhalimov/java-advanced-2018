package ru.ifmo.rain.khalimov.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> array;
    private final Comparator<? super T> comp;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Comparator<? super T> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(Collection<T> collection) {
        this(collection, null);
    }

    private ArraySet(List<T> list, Comparator<? super T> comparator) {
        array = list;
        comp = comparator;
    }

    public ArraySet(Collection<T> collection, Comparator<? super T> comparator) {
        comp = comparator;
        List<T> list = new ArrayList<>(collection);
        list.sort(comparator);
        Iterator<T> iterator = list.iterator();
        array = new ArrayList<>();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (size() == 0 || comparator == null && !last().equals(next) || comparator != null && comparator.compare(last(), next) != 0) {
                array.add(next);
            }
        }
    }

    @Override
    public Comparator<? super T> comparator() {
        return comp;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object element) throws ClassCastException, NullPointerException {
        if (element == null) {
            throw new NullPointerException();
        }
        return Collections.binarySearch(array, (T) element, comp) >= 0;
    }

    @Override
    public T first() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return getElementByIndex(0);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) throws NullPointerException {
        if (isEmpty()) {
            return new ArraySet<>(comp);
        }
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public SortedSet<T> headSet(T toElement) throws NullPointerException {
        return headSet(toElement, false);
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(array).iterator();
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new DescendingIterator<>(Collections.unmodifiableList(array).listIterator(size()));
    }

    @Override
    public T last() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return getElementByIndex(array.size() - 1);
    }

    @Override
    public int size() {
        return array.size();
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) throws NullPointerException {
        if (isEmpty()) {
            return new ArraySet<>(comp);
        }
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) throws NullPointerException {
        return tailSet(fromElement, true);
    }

    private int searchIndex(T element, int shiftIfNotContains, int shiftIfContains) throws NullPointerException {
        if (element == null) {
            throw new NullPointerException();
        }
        int i = Collections.binarySearch(array, element, comp);
        if (i < 0) {
            i = -i - 1 + shiftIfNotContains;
        }
        else {
            i += shiftIfContains;
        }
        return i;
    }

    private T getElementByIndex(int index) {
        if (index < 0 || index >= size()) {
            return null;
        }
        return array.get(index);
    }

    @Override
    public T ceiling(T element) throws NullPointerException {
        return getElementByIndex(searchIndex(element, 0, 0));
    }

    @Override
    public T floor(T element) throws NullPointerException {
        return getElementByIndex(searchIndex(element, -1, 0));
    }

    @Override
    public T lower(T element) throws NullPointerException {
        return getElementByIndex(searchIndex(element, -1, -1));
    }

    @Override
    public T higher(T element) throws NullPointerException {
        return getElementByIndex(searchIndex(element, 0, 1));
    }

    @Override
    public T pollFirst() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        List<T> reversedArray = new ArrayList<>(array);
        Collections.reverse(reversedArray);
        return new ArraySet<>(reversedArray, comp.reversed());
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) throws NullPointerException, IllegalArgumentException {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) throws NullPointerException, IllegalArgumentException {
        int from = searchIndex(fromElement, 0, fromInclusive ? 0 : 1);
        int to = searchIndex(toElement, 0, toInclusive ? 1 : 0);
        if (from > to) {
            return new ArraySet<>(comp);
        }
        return new ArraySet<>(array.subList(from, to), comp);
    }

    private class DescendingIterator<S> implements Iterator<S> {
        private final ListIterator<S> iterator;

        DescendingIterator(ListIterator<S> iterator) {
            this.iterator = iterator;
        }

        public S next() {
            return iterator.previous();
        }

        public boolean hasNext() {
            return iterator.hasPrevious();
        }
    }
}
