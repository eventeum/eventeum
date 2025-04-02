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

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * An service that should execute a task in an Asynchronous manner.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface AsyncTaskService {

  void execute(String executorName, Runnable task);

  CompletableFuture<Void> executeWithCompletableFuture(String executorName, Runnable task);

  <T> Future<T> submit(String executorName, Callable<T> task);
}
