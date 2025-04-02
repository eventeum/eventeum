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

package net.consensys.eventeum.dto.event.parameter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class ParameterTypeIdResolver extends TypeIdResolverBase {

  private JavaType superType;

  @Override
  public void init(JavaType baseType) {
    superType = baseType;
  }

  @Override
  public String idFromValue(Object value) {
    return idFromValueAndType(value, value.getClass());
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> suggestedType) {
    final EventParameter<?> parameter = (EventParameter) value;
    return parameter.getType();
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) {
    Class<?> subType = null;

    if (id.endsWith("[]")) {
      subType = ArrayParameter.class;
    } else if (id.startsWith("byte") || id.equals("string") || id.equals("address")) {
      subType = StringParameter.class;
    } else if (id.startsWith("uint") || id.startsWith("int") || id.startsWith("bool")) {
      subType = NumberParameter.class;
    }
    return context.constructSpecializedType(superType, subType);
  }

  @Override
  public JsonTypeInfo.Id getMechanism() {
    return JsonTypeInfo.Id.NAME;
  }
}
