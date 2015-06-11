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

import org.junit.Test;
import static org.fest.assertions.api.Assertions.assertThat;

public class PoolStatusTest {

    @Test
    public void setAliveIfDead() {
        final PoolStatus status = new PoolStatus();

        assertThat(PoolStatus.State.DEAD)
                .isEqualTo(status.state());
        assertThat(PoolStatus.State.DEAD)
                .isEqualTo(status.enter());
        assertThat(PoolStatus.State.ALIVE)
                .isEqualTo(status.state());
    }

    @Test
    public void setDeadIfDead() {
        final PoolStatus status = new PoolStatus();

        assertThat(PoolStatus.State.DEAD)
                .isEqualTo(status.state());
        assertThat(PoolStatus.State.DEAD)
                .isEqualTo(status.exit());
        assertThat(PoolStatus.State.DEAD)
                .isEqualTo(status.state());
    }

    @Test
    public void setAliveIfAlive() {
        final PoolStatus status = new PoolStatus();

        assertThat(PoolStatus.State.DEAD)
                .isEqualTo(status.state());
        assertThat(PoolStatus.State.DEAD)
                .isEqualTo(status.enter());
        assertThat(PoolStatus.State.ALIVE)
                .isEqualTo(status.state());
        assertThat(PoolStatus.State.ALIVE)
                .isEqualTo(status.enter());
        assertThat(PoolStatus.State.ALIVE)
                .isEqualTo(status.state());
    }

    @Test
    public void setDeadIfAlive() {
        final PoolStatus status = new PoolStatus();

        assertThat(PoolStatus.State.DEAD)
                .isEqualTo(status.state());
        assertThat(PoolStatus.State.DEAD)
                .isEqualTo(status.enter());
        assertThat(PoolStatus.State.ALIVE)
                .isEqualTo(status.state());
        assertThat(PoolStatus.State.ALIVE)
                .isEqualTo(status.exit());
        assertThat(PoolStatus.State.DEAD)
                .isEqualTo(status.state());
    }

    @Test
    public void awaitEntry() throws Exception {
        final PoolStatus status = new PoolStatus();

        Thread[] waitThreads = new Thread[3];
        final long[] waitMillis = new long[waitThreads.length];
        for (int i = 0; i < waitThreads.length; i++) {
            Thread waitThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        long start = System.currentTimeMillis();
                        status.awaitEntry();
                        waitMillis[Integer.valueOf(Thread.currentThread().getName())] = System.currentTimeMillis() - start;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, String.valueOf(i));
            waitThreads[i] = waitThread;
            waitThread.start();
        }

        // make sure all waiters have started their timers
        for (Thread waitThread : waitThreads) {
            while (waitThread.getState() != Thread.State.WAITING) {
                Thread.sleep(50);
            }
        }

        // make waiters wait for a while
        long minWaitPeriod = 200;
        Thread.sleep(minWaitPeriod);
        status.enter();

        for (int i = 0; i < waitThreads.length; i++) {
            // allow waiter to finish, and ensure visibility of waitMillis result
            waitThreads[i].join();
            // check that it waited for the state change
            assertThat(waitMillis[i]).isGreaterThanOrEqualTo(minWaitPeriod);
        }
    }

}
