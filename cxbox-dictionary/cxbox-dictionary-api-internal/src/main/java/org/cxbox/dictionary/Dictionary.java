/*
 * © OOO "SI IKS LAB", 2022-2024
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

package org.cxbox.dictionary;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import lombok.NonNull;
import lombok.SneakyThrows;


/**
 * Dictionary - Enum style way to work with configurable (through database or any external data source) enumerations
 *
 *
 * <br>
 * <br>
 * A) Les's compare Enum to Dictionary in typical REST API + JPA microservice <code>(out-side of cxbox usage)</code>
 * <br>
 * <br>
 * A.1) Enum:
 * <pre>{@code
 * public enum DocumentType {
 *  REFERENCE("Reference"),
 *  POLICY("Policy"),
 *  LEGAL("Legal");
 *
 *  @JsonValue
 *  private final String value;
 * }
 * }</pre>
 * <pre>{@code
 * public class DocumentDTO {
 *  private DocumentType type;
 * }}</pre>
 * <pre>{@code
 * @Entity
 * public class Document {
 *  @Id
 *  private String id;
 *
 *  @Column
 *  @Enumerated(EnumType.STRING)
 *  private DocumentType type;
 * }}</pre>
 * <pre>{@code
 * public class DocumentMapper {
 *  DocumentDTO toDto(Document entity) {
 *   //other mappings
 *   dto.setType(entity.getType);
 *   return dto;
 *  }
 *
 *  Document toEntity(DocumentDTO dto) {
 *    //other mappings
 *    entity.setType(dto.getType);
 *    return dto;
 *   }
 * }}</pre>
 * <br>
 * <br>
 * A.2) Dictionary:
 * <pre>{@code
 * public record DocumentType(String key) implements Dictionary {
 *  //constants only for options, that are directly referenced in business logic, because we do not know all options (they are configurable)
 *  public static final ClientImportance REFERENCE = new ClientImportance("REFERENCE");
 * }}</pre>
 * <pre>{@code
 * public class DocumentDTO {
 *  private DocumentType type;
 * }}</pre>
 * <pre>{@code
 * @Entity
 * public class Document {
 *  @Id
 *  private String id;
 *
 *  @Column
 *  @Type(DictionaryType.class)
 *  private DocumentType type;
 * }}</pre>
 * <pre>{@code
 * public class DocumentMapper {
 *  DocumentDTO toDto(Document entity) {
 *   //other mappings
 *   dto.setType(entity.getType);
 *   return dto;
 *  }
 *
 *  Document toEntity(DocumentDTO dto) {
 *    //other mappings
 *    entity.setType(dto.getType);
 *    return dto;
 *   }
 * }}</pre>
 * <br> So, dictionary usage is quite similar to enum. It also has advantages of strongly typed code, that is checked at compile-time (comparing to using String)
 * <br>
 * <br>
 * Provide following <code>project level</code> configurations to use dictionary:
 * <br>
 * A.2.1 <code>hibernate</code>: if you microservice uses hibernate, then add dependency, that will allow you to use {@link org.cxbox.dictionary.hibernate.DictionaryType} under entity column, e.g. <code>@Type(DictionaryType.class)</code>:
 * <br>
 * <pre>{@code
 * <dependency>
 *   <groupId>org.cxbox</groupId>
 *   <artifactId>cxbox-dictionary-hibernate</artifactId>
 * </dependency>
 * }</pre>
 * Also, you can turn on auto type registration setting org.cxbox.dictionary.enable_types_contributor=true in hibernate properties, e.g. in spring:
 * <pre>{@code
 * <dependency>
 *   <groupId>org.cxbox</groupId>
 *   <artifactId>cxbox-dictionary-api</artifactId>
 * </dependency>
 * }</pre>
 * <pre>{@code
 * spring:
 *  jpa:
 *    properties:
 *      org:
 *        cxbox:
 *          dictionary:
 *            enable_types_contributor: true
 * }</pre>
 * then <code>@Type(DictionaryType.class)</code> can be <code>optionally</code> skipped under column, because system will register type for all Dictionary implementations. Note! enable_types_contributor feature is experimental - please, use <code>@Type(DictionaryType.class)</code> under entity column as fallback variant
 * <br>
 * <br>
 * A.2.2 <code>jackson</code>: if you microservice uses jackson, then add dependency, that will allow you to serialize Dictionary just like enum (e.g. <code>{type: $serialized_value}</code> instead of <code>{type: {key: $key_value}}</code>)
 * <pre>{@code
 * <dependency>
 *   <groupId>org.cxbox</groupId>
 *   <artifactId>cxbox-dictionary-jackson</artifactId>
 * </dependency>
 * }</pre>
 * <br>
 * then configure your ObjectMapper
 * <br>
 * A.2.2.1 if you want keys to be used as serialized value (e.g. <code>{type: "REFERENCE"}</code> instead of <code>{type: {key: "REFERENCE"}}</code> from example in the start of this java doc)
 * <pre>{@code
 *  return Jackson2ObjectMapperBuilder
 *   .json()
 *   .handlerInstantiator(handlerInstantiator)
 *   .modules(buildDictionaryModule(Optional.empty(), false))
 *   .build();
 * }</pre>
 *
 * A.2.2.2 else, if you want values to be used as serialized value (e.g. <code>{type: "Reference"}</code> instead of <code>{type: {key: "REFERENCE"}}</code> from example in the start of this java doc)
 * <pre>{@code
 * return Jackson2ObjectMapperBuilder
 *   .json()
 *   .handlerInstantiator(handlerInstantiator)
 *   .modules(buildDictionaryModule(dictionaryProvider, true))
 *   .build();
 * }</pre>
 * in this case you need to configure value provider, that will be used by Jackson during key <-> value conversion
 * <pre>{@code
 *  public DictionaryProvider dictionaryProvider() {
 *    return new DictionaryProvider() {
 *     public Dictionary lookupName(DictionaryValue value, Class<Dictionary> type) {
 *       var dictionaryType = Dictionary.of(type, "").getDictionaryType();
 *       //Caching STRONGLY recommended here
 *       var key = //..fetch key by value and type from you data source.
 *       return Dictionary.of(type, key);
 *     }
 *     public List<Dictionary> getAll(Class<Dictionary> dictionaryType) {
 *       var dictionaryType = Dictionary.of(type, "").getDictionaryType();
 *       //Caching STRONGLY recommended here
 *       var dictionaryTypeOptions = //..fetch all keys by type from you data source
 *       return dictionaryTypeOptions
 *         .stream()
 *         .map(e -> Dictionary.of(dictionaryType, e.getKey()))
 *         .toList();
 *     }
 *     public DictionaryValue get(Dictionary dictionary) {
 *       var type = dictionary.getDictionaryType();
 *       var key = dictionary.key()
 *       //Caching STRONGLY recommended here
 *       return //..fetch dictionary value by key and type from you data source
 *     }
 *    };
 *  }
 * }</pre>
 * <br>
 * ---------------------inside cxbox usage---------------------
 * <br>
 * B) Les's compare Enum to Dictionary in cxbox to UI interaction <code>(inside cxbox usage)</code>
 * <br>
 * B.1) <code>enum</code> same as A.1
 * <br>
 * B.2) <code>dictionary</code>
 * <br>
 * B.2.1) <code>hibernate</code>: same as A.2, but dependency already included in
 * <pre>{@code
 * <dependency>
 *   <groupId>org.cxbox</groupId>
 *   <artifactId>cxbox-dictionary-all</artifactId>
 * </dependency>
 * }</pre>
 * <br>
 * B.2.2) <code>jackson</code>:dependency already included in <code>cxbox-dictionary-all</code>
 * <br>
 * B.2.2.1) <code>serializing/deserializing as key</code> that is described in A.2.2.1) is not used
 * <br>
 * B.2.2.2) <code>serializing/deserializing as value</code> that is described in A.2.2.2) is used, but
 * Jackson2ObjectMapperBuilder is already configured in {@link org.cxbox.core.config.JacksonConfig} and DictionaryProvider implementation will be:
 * <pre>{@code
 * @Configuration
 * public class DictionaryConfig {
 *  @Bean
 *  public DictionaryProvider dictionaryProvider() {
 *    return new DictionaryProvider() {
 *      @Override
 *      public <T extends Dictionary> T lookupName(@NonNull Class<T> type, @NonNull DictionaryValue value) {
 *        var dictTmp = Dictionary.of(type, "");
 *        var lov = DictionaryCache.dictionary().lookupName(value.getValue(), dictTmp.getDictionaryType());
 *        return Dictionary.of(type, lov.getKey());
 *      }
 *      @Override
 *      public <T extends Dictionary> SimpleDictionary lookupValue(@NonNull T dictionary) {
 *        return DictionaryCache.dictionary().get(dictionary.getDictionaryType(), dictionary.key());
 *      }
 *      @Override
 *      public <T extends Dictionary> Collection<T> getAll(@NonNull Class<T> dictionaryType) {
 *        return DictionaryCache.dictionary().getAll(Dictionary.of(dictionaryType, "").getDictionaryType())
 *          .stream()
 *          .map(e -> Dictionary.of(dictionaryType, e.getKey()))
 *          .toList();
 *      }
 *     };
 *  }
 * }
 *}</pre>
 * that uses db tables DICTIONARY, DICTIONARY_ITEM, DICTIONARY_ITEM_TR to store dictionaries, that one can populate with UI or csv+liquibase.
 * <br>
 * in single language environment csv can look like this:
 * <pre>{@code
 * _____________________________________________________________________________________
 * | TYPE          | KEY       | VALUE     | DISPLAY_ORDER | DESCRIPTION | ACTIVE | ID |
 * _____________________________________________________________________________________
 * | DOCUMENT_TYPE | REFERENCE | Reference | 1             | null        | null | null |
 * | DOCUMENT_TYPE | POLICY    | Policy    | 2             | null        | null | null |
 * | DOCUMENT_TYPE | LEGAL     | Legal     | 3             | null        | null | null |
 * _____________________________________________________________________________________
 *}</pre>
 */
public interface Dictionary extends Serializable {

	/**
	 * Dictionary class implementations MUST have constructor(String key)
	 *
	 * @param clazz - dictionary class
	 * @param key - dictionary key
	 * @return dictionary instance
	 */
	@SneakyThrows
	@NonNull
	static <T extends Dictionary> T of(@NonNull Class<T> clazz, @NonNull String key) {
		try {
			return clazz.getDeclaredConstructor(String.class).newInstance(key);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalStateException(
					"constructor(String key) was not found, but MUST exist in " + clazz.getSimpleName(), e);
		}
	}

	@NonNull
	String key();

	/**
	 * By default, dictionary type is a "simple Class name" converted to SCREAMING_SNAKE_CASE.
	 * <br>
	 * Example: `COUNTRY_REGIONS`  <=> 'public record CountryRegions implements Dict ...'
	 * <br>
	 * <br>
	 * In exclusive situations one can @Override convention, but realization must not depend on any non-static fields, because getDictType can be called before all fields are instantiated (e.g. EMPTY key)!
	 *
	 * @return dictionary type
	 */
	@NonNull
	default String getDictionaryType() {
		return this.getClass().getSimpleName().replaceAll("([A-Z])", "_$1").replaceFirst("_", "").toUpperCase();
	}

}
