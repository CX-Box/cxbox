/*



package org.cxbox.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.cxbox.core.config.JacksonConfig;
import org.cxbox.core.dto.multivalue.MultivalueField;
import org.cxbox.core.ui.model.json.field.FieldMeta;
import org.cxbox.core.ui.model.json.field.subtypes.DictionaryFieldMeta;
import org.cxbox.core.ui.model.json.field.subtypes.DiffTextFieldMeta;
import org.cxbox.core.ui.model.json.field.subtypes.EmptyFieldMeta;
import org.cxbox.core.ui.model.json.field.subtypes.HiddenFieldMeta;
import org.cxbox.core.ui.model.json.field.subtypes.InlinePickListFieldMeta;
import org.cxbox.core.ui.model.json.field.subtypes.InputFieldMeta;
import org.cxbox.core.ui.model.json.field.subtypes.NumberFieldMeta;
import org.cxbox.core.ui.model.json.field.subtypes.TextFieldMeta;
import org.cxbox.core.ui.model.json.field.subtypes.UploadFileFieldMeta;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.core.util.SpringBeanUtils;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@DirtiesContext
@SpringJUnitConfig({
		JacksonConfig.class,
		SpringBeanUtils.class
})
public class ObjectMapperTest {

	@Test
	void testFieldMetaBase() throws Exception {
		String fields = IOUtils.toString(
				getClass().getResourceAsStream("FieldMetaBase.json"),
				Charset.defaultCharset()
		);
		final FieldMeta field = JsonUtils.readValue(FieldMeta.class, fields);
		assertThat(field).isInstanceOf(InputFieldMeta.class);
		InputFieldMeta inputFieldMeta = (InputFieldMeta) field;
		assertThat(inputFieldMeta.getIsValue()).isNotNull();
		assertThat(inputFieldMeta.getIsCol()).isNotNull();
		assertThat(inputFieldMeta.getIsRow()).isNotNull();
		assertThat(inputFieldMeta.getLabel()).isNotNull();
		assertThat(inputFieldMeta.getXDefault()).isNull();
		assertThat(inputFieldMeta.getYDefault()).isNull();
		assertThat(inputFieldMeta.getFixedAxis()).isNotNull();
		assertThat(inputFieldMeta.getRequired()).isNotNull();
		assertThat(inputFieldMeta.getPermanent()).isNotNull();
		assertThat(inputFieldMeta.getDrillDownTypeKey()).isNotNull();
		assertThat(inputFieldMeta.getBgColor()).isNotNull();
		assertThat(inputFieldMeta.getBgColorKey()).isNotNull();
		assertThat(inputFieldMeta.getIconParamsKey()).isNotNull();
		assertThat(inputFieldMeta.getIconType()).isNotNull();
		assertThat(inputFieldMeta.getIconColor()).isNotNull();
		assertThat(inputFieldMeta.getIconTypeKey()).isNotNull();
		assertThat(inputFieldMeta.getGroupName()).isNotNull();
		assertThat(inputFieldMeta.getWidth()).isNotNull();
		assertThat(inputFieldMeta.getHintTitle()).isNotNull();
		assertThat(inputFieldMeta.getHintText()).isNotNull();
		assertThat(inputFieldMeta.getMaxInput()).isNotNull();
		assertThat(inputFieldMeta.getDrillDown()).isNotNull();
		assertThat(inputFieldMeta.getDrillDownKey()).isNotNull();
		assertThat(inputFieldMeta.getIconColorKey()).isNotNull();
	}

	@Test
	void testMultiValue() throws Exception {
		String multiValueString = IOUtils.toString(
				getClass().getResourceAsStream("MultiValue.json"),
				Charset.defaultCharset()
		);
		final MultivalueField field = JsonUtils.readValue(MultivalueField.class, multiValueString);
		assertThat(field).isInstanceOf(MultivalueField.class);
		assertThat(field.getValues()).hasSize(3);
		assertThat(field.getValues().get(0).getOptions()).isNotEmpty();
	}

	@Test
	void testInputField() throws Exception {
		String fields = IOUtils.toString(
				getClass().getResourceAsStream("InputField.json"),
				Charset.defaultCharset()
		);
		final FieldMeta field = JsonUtils.readValue(FieldMeta.class, fields);
		assertThat(field).isInstanceOf(InputFieldMeta.class);
		InputFieldMeta inputFieldMeta = (InputFieldMeta) field;
		assertThat(inputFieldMeta.getMaskField()).isNotNull();
		assertThat(inputFieldMeta.getMask().getMask()).isNotNull();
		assertThat(inputFieldMeta.getMask().getType()).isNotNull();
	}


	@Test
	void testTextField() throws Exception {
		String fields = IOUtils.toString(
				getClass().getResourceAsStream("TextField.json"),
				Charset.defaultCharset()
		);
		final FieldMeta field = JsonUtils.readValue(FieldMeta.class, fields);
		assertThat(field).isInstanceOf(TextFieldMeta.class);
		TextFieldMeta textFieldMeta = (TextFieldMeta) field;
		assertThat(textFieldMeta.getMaxInputRows()).isNotNull();
		assertThat(textFieldMeta.getMinInputRows()).isNotNull();
		assertThat(textFieldMeta.getPopover()).isNotNull();
	}

	@Test
	void testInlinePickList() throws Exception {
		String fields = IOUtils.toString(
				getClass().getResourceAsStream("InlinePickListField.json"),
				Charset.defaultCharset()
		);
		final FieldMeta field = JsonUtils.readValue(FieldMeta.class, fields);
		assertThat(field).isInstanceOf(InlinePickListFieldMeta.class);
		InlinePickListFieldMeta inlinePickListFieldMeta = (InlinePickListFieldMeta) field;
		assertThat(inlinePickListFieldMeta.getSearchSpec()).isNotNull();
	}

	@Test
	void testHiddenField() throws Exception {
		String fields = IOUtils.toString(
				getClass().getResourceAsStream("HiddenField.json"),
				Charset.defaultCharset()
		);
		final FieldMeta field = JsonUtils.readValue(FieldMeta.class, fields);
		assertThat(field).isInstanceOf(HiddenFieldMeta.class);
		HiddenFieldMeta textFieldMeta = (HiddenFieldMeta) field;
		assertThat(textFieldMeta.getPopupBcName()).isNotNull();
		assertThat(textFieldMeta.getPickMap()).isNotEmpty();
	}

	@Test
	void testNumberField() throws Exception {
		String fields = IOUtils.toString(
				getClass().getResourceAsStream("NumberField.json"),
				Charset.defaultCharset()
		);
		final FieldMeta field = JsonUtils.readValue(FieldMeta.class, fields);
		assertThat(field).isInstanceOf(NumberFieldMeta.class);
		NumberFieldMeta textFieldMeta = (NumberFieldMeta) field;
		assertThat(textFieldMeta.getDigits()).isNotNull();
		assertThat(textFieldMeta.getNullable()).isNotNull();
	}

	@Test
	void testDiffTextField() throws Exception {
		String fields = IOUtils.toString(
				getClass().getResourceAsStream("DiffTextField.json"),
				Charset.defaultCharset()
		);
		final FieldMeta field = JsonUtils.readValue(FieldMeta.class, fields);
		assertThat(field).isInstanceOf(DiffTextFieldMeta.class);
		DiffTextFieldMeta textFieldMeta = (DiffTextFieldMeta) field;
		assertThat(textFieldMeta.getDiffSource()).isNotNull();
		assertThat(textFieldMeta.getDiffTarget()).isNotNull();
	}

	@Test
	void testDictionaryField() throws Exception {
		String fields = IOUtils.toString(
				getClass().getResourceAsStream("DictionaryField.json"),
				Charset.defaultCharset()
		);
		final FieldMeta field = JsonUtils.readValue(FieldMeta.class, fields);
		assertThat(field).isInstanceOf(DictionaryFieldMeta.class);
		DictionaryFieldMeta textFieldMeta = (DictionaryFieldMeta) field;
		assertThat(textFieldMeta.getDictionaryName()).isNotNull();
	}

	@Test
	void testFileField() throws Exception {
		String fields = IOUtils.toString(
				getClass().getResourceAsStream("FileUploadField.json"),
				Charset.defaultCharset()
		);
		final FieldMeta field = JsonUtils.readValue(FieldMeta.class, fields);
		assertThat(field).isInstanceOf(UploadFileFieldMeta.class);
		UploadFileFieldMeta textFieldMeta = (UploadFileFieldMeta) field;
		assertThat(textFieldMeta.getFileIdKey()).isNotNull();
		assertThat(textFieldMeta.getFileSource()).isNotNull();
	}

	@Test
	void testRadioField() throws Exception {
		String fields = IOUtils.toString(
				getClass().getResourceAsStream("RadioField.json"),
				Charset.defaultCharset()
		);
		final FieldMeta field = JsonUtils.readValue(FieldMeta.class, fields);
		assertThat(field).isInstanceOf(EmptyFieldMeta.class);
		EmptyFieldMeta emptyFieldMeta = (EmptyFieldMeta) field;
		assertThat(emptyFieldMeta.getTitle()).isNotNull();
	}

}
*/
