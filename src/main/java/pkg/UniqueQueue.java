package pkg;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * @author Brian Chipman
 *
 * This is a quickly implemented FIFO queue which contains only unique elements.
 */
public class UniqueQueue<T> implements Queue<T> {

   private final Queue<T> queue = new LinkedList<>();

   private final Set<T> set = new HashSet<>();

   @Override
   public int size() {
      return queue.size();
   }

   @Override
   public boolean isEmpty() {
      return queue.isEmpty();
   }

   @Override
   public boolean contains(final Object o) {
      return set.contains(o);
   }

   @Override
   public Iterator<T> iterator() {
      return queue.iterator();
   }

   @Override
   public Object[] toArray() {
      return queue.toArray();
   }

   @Override
   public <T1> T1[] toArray(final T1[] a) {
      return queue.toArray(a);
   }

   @Override
   public boolean add(final T t) {
      if (set.add(t)) {
         queue.add(t);
         return true;
      }
      return false;
   }

   @Override
   public boolean remove(final Object o) {
      set.remove(o);
      return queue.remove(o);
   }

   @Override
   public boolean containsAll(final Collection<?> c) {
      return set.containsAll(c);
   }

   @Override
   public boolean addAll(final Collection<? extends T> c) {
      boolean changed = false;
      for (final T t : c) {
         if (add(t)) {
            changed = true;
         }
      }
      return changed;
   }

   @Override
   public boolean removeAll(final Collection<?> c) {
      set.removeAll(c);
      return queue.removeAll(c);
   }

   @Override
   public boolean retainAll(final Collection<?> c) {
      set.retainAll(c);
      return queue.retainAll(c);
   }

   @Override
   public void clear() {
      set.clear();
      queue.clear();
   }

   @Override
   public boolean offer(final T t) {
      return add(t);
   }

   @Override
   public T remove() {
      T t = queue.remove();
      set.remove(t);
      return t;
   }

   @Override
   public T poll() {
      T t = queue.poll();
      set.remove(t);
      return t;
   }

   @Override
   public T element() {
      return queue.element();
   }

   @Override
   public T peek() {
      return queue.peek();
   }
}
