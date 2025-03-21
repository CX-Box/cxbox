/*
 * © OOO "SI IKS LAB", 2022-2025
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

package org.cxbox.core.dto.multivalue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Test class for MultivalueField.
 * Tests constructor creation, toMultivalueField creation, and serialization/deserialization.
 */
class MultivalueFieldTest {

	/**
	 * Test JDK serialization/deserialization of MultivalueField created using its constructor.
	 */
	@Test
	void whenMultivalueFieldCreatedWithConstructor_thenSerializationDeserializationWorks() throws Exception {
		// Create a list of MultivalueFieldSingleValue objects and MultivalueField
		List<MultivalueFieldSingleValue> values = new ArrayList<>();
		values.add(new MultivalueFieldSingleValue("1", "Value 1"));
		values.add(new MultivalueFieldSingleValue("2", "Value 2"));
		MultivalueField originalField = new MultivalueField(values);

		// Verify the field was created correctly
		assertNotNull(originalField);
		assertEquals(2, originalField.getValues().size());
		assertEquals("1", originalField.getValues().get(0).getId());
		assertEquals("Value 1", originalField.getValues().get(0).getValue());
		assertEquals("2", originalField.getValues().get(1).getId());
		assertEquals("Value 2", originalField.getValues().get(1).getValue());

		// Verify that MultivalueField implements Serializable
		assertTrue(originalField instanceof Serializable);

		// Serialize and deserialize the field
		MultivalueField deserializedField = serializeAndDeserialize(originalField);

		// Verify the deserialized field matches the original
		verifyDeserializedField(originalField, deserializedField);
	}

	/**
	 * Test JDK serialization/deserialization of MultivalueField created using the toMultivalueField collector.
	 */
	@Test
	void whenMultivalueFieldCreatedWithToMultivalueField_thenSerializationDeserializationWorks() throws Exception {
		// Create test data
		List<TestData> testDataList = Arrays.asList(
				new TestData("1", "Value 1"),
				new TestData("2", "Value 2")
		);

		// Create MultivalueField using toMultivalueField
		MultivalueField originalField = testDataList.stream()
				.collect(MultivalueField.toMultivalueField(
						TestData::id,
						TestData::value
				));

		// Verify the field was created correctly
		assertNotNull(originalField);
		assertEquals(2, originalField.getValues().size());
		assertEquals("1", originalField.getValues().get(0).getId());
		assertEquals("Value 1", originalField.getValues().get(0).getValue());
		assertEquals("2", originalField.getValues().get(1).getId());
		assertEquals("Value 2", originalField.getValues().get(1).getValue());

		// Verify that MultivalueField implements Serializable
		assertTrue(originalField instanceof Serializable);

		// Serialize and deserialize the field
		MultivalueField deserializedField = serializeAndDeserialize(originalField);

		// Verify the deserialized field matches the original
		verifyDeserializedField(originalField, deserializedField);
	}

	/**
	 * Test JDK serialization and deserialization of MultivalueField with options.
	 */
	@Test
	void whenMultivalueFieldHasOptions_thenSerializationDeserializationPreservesOptions() throws Exception {
		// Create a MultivalueField with options
		List<MultivalueFieldSingleValue> values = new ArrayList<>();
		values.add(new MultivalueFieldSingleValue("1", "Value 1"));
		values.add(new MultivalueFieldSingleValue("2", "Value 2"));
		values.get(0).addOption(MultivalueOptionType.HINT, "Hint 1");
		MultivalueField originalField = new MultivalueField(values);

		// Serialize and deserialize the field
		MultivalueField deserializedField = serializeAndDeserialize(originalField);

		// Verify the deserialized field matches the original, including options
		verifyDeserializedFieldWithOptions(originalField, deserializedField);
	}

	/**
	 * Serializes and deserializes a MultivalueField.
	 */
	private MultivalueField serializeAndDeserialize(MultivalueField originalField) throws Exception {
		// Serialize the field
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(originalField);
		oos.close();

		// Deserialize the field
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		MultivalueField deserializedField = (MultivalueField) ois.readObject();
		ois.close();

		return deserializedField;
	}

	/**
	 * Verifies that a deserialized MultivalueField matches the original.
	 */
	private void verifyDeserializedField(MultivalueField originalField, MultivalueField deserializedField) {
		assertNotNull(deserializedField);
		assertEquals(originalField.getValues().size(), deserializedField.getValues().size());
		assertEquals(originalField.getValues().get(0).getId(), deserializedField.getValues().get(0).getId());
		assertEquals(originalField.getValues().get(0).getValue(), deserializedField.getValues().get(0).getValue());
		assertEquals(originalField.getValues().get(1).getId(), deserializedField.getValues().get(1).getId());
		assertEquals(originalField.getValues().get(1).getValue(), deserializedField.getValues().get(1).getValue());
	}

	/**
	 * Verifies that a deserialized MultivalueField with options matches the original.
	 */
	private void verifyDeserializedFieldWithOptions(MultivalueField originalField, MultivalueField deserializedField) {
		verifyDeserializedField(originalField, deserializedField);
		assertEquals(
				originalField.getValues().get(0).getOptions().get(MultivalueOptionType.HINT.getValue()),
				deserializedField.getValues().get(0).getOptions().get(MultivalueOptionType.HINT.getValue())
		);
	}

	/**
	 * Helper class for testing toMultivalueField.
	 */
	private record TestData(String id, String value) {

	}

}
