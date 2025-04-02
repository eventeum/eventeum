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

package net.consensys.eventeum.chain.converter;

import net.consensys.eventeum.dto.event.parameter.EventParameter;

/**
 * A converter that converts the input value of type T, into an EventParameter.
 *
 * @param <T> The input type
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface EventParameterConverter<T> {

  EventParameter convert(T toConvert);
}
