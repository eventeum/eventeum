/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.testutils;

import lombok.NoArgsConstructor;
import net.consensys.eventeum.service.AsyncTaskService;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@NoArgsConstructor
public class DummyAsyncTaskService implements AsyncTaskService {

    @Override
    public void execute(String executorName, Runnable task) {
        task.run();
    }

    @Override
    public CompletableFuture<Void> executeWithCompletableFuture(String executorName, Runnable task) {
        return null;
    }

    @Override
    public <T> Future<T> submit(String executorName, Callable<T> task) {
        try {
            return CompletableFuture.completedFuture(task.call());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}