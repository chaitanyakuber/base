/*
 * #%L
 * pool-status-servlet
 * %%
 * Copyright (C) 2015 Wotif Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.wotifgroup.poolstatusservlet;

public class PoolStatus {

    public enum State {
        ALIVE("alive"), DEAD("dead");

        private final String label;

        private State(final String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

    }

    private volatile State state = State.DEAD;

    public State enter() {
        return getAndSetState(State.ALIVE);
    }

    public State exit() {
        return getAndSetState(State.DEAD);
    }

    public State state() {
        return state;
    }

    public synchronized void awaitEntry() throws InterruptedException {
        while (state != State.ALIVE) {
            wait();
        }
    }

    private synchronized State getAndSetState(State newState) {
        State oldState = state;
        if (oldState != newState) {
            state = newState;
            notifyAll();
        }
        return oldState;
    }

}
