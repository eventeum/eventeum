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

package net.consensys.eventeum.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * An async task service that utilises a single thread executor
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component("asyncTaskService")
public class SingleThreadedAsyncTaskService implements AsyncTaskService {

    private Map<String, ExecutorService> executorServices = new HashMap<>();

    @Override
    public void execute(String executorName, Runnable task) {
        getOrCreateExecutor(executorName).execute(task);
    }

    @Override
    public <T> Future<T> submit(String executorName, Callable<T> task) {
        return getOrCreateExecutor(executorName).submit(task);
    }

    @Override
    public CompletableFuture<Void> executeWithCompletableFuture(String executorName, Runnable task) {
        return CompletableFuture.runAsync(task, getOrCreateExecutor(executorName));
    }

    private ExecutorService getOrCreateExecutor(String executorName) {
        if (!executorServices.containsKey(executorName)) {
            executorServices.put(executorName, buildExecutor(executorName));
        }

        return executorServices.get(executorName);
    }

    protected ExecutorService buildExecutor(String executorName) {
        return Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder().setNameFormat(executorName + "-%d").build());
    }
}
