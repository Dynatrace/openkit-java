/**
 * Copyright 2018-2019 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * SynchronizedQueue is an implementation of a data structure that fulfills the following requirements:
 * - has to be thread-safe for access from multiple threads
 * - should be non-blocking for performance reasons
 * - random-delete has to be possible
 * - first-in, first-out
 * - shallow copies should be possible
 * - should be easy to clear
 * <p>
 * As there was no real fit in the Java 6 JDK data structures, this is a simple self-made implementation.
 * It's for sure not the best-performing implementation and it could make sense to introduce upper bounds, but it works well enough.
 *
 * @param <T> type of items in the queue instance
 */
public class SynchronizedQueue<T> {

    // use a linked list as basic data structure
    private LinkedList<T> list;

    public SynchronizedQueue() {
        this.list = new LinkedList<T>();
    }

    // put an item into the queue (at the end)
    public boolean put(T entry) {
        synchronized (list) {
            return list.add(entry);
        }
    }

    // get an item from the queue i.e. removes it (from the beginning)
    public T get() {
        synchronized (list) {
            return list.removeFirst();
        }
    }

    // remove specific item from the queue
    public boolean remove(T entry) {
        synchronized (list) {
            return list.remove(entry);
        }
    }

    // clear queue
    public void clear() {
        synchronized (list) {
            list.clear();
        }
    }

    // check if queue is empty
    public boolean isEmpty() {
        synchronized (list) {
            return list.isEmpty();
        }
    }

    // return shallow-copy of all the items in the queue
    public ArrayList<T> toArrayList() {
        synchronized (list) {
            return new ArrayList<T>(list);
        }
    }

}
