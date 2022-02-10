package com.dynatrace.openkit.util.json.objects;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * @author matthias.hochrieser
 */
public class JSONValueWriterTest {

	JSONValueWriter writer;

	@Before
	public void init() {
		writer = new JSONValueWriter();
	}

	@Test
	public void checkOpenArrayCharacter() {
		writer.openArray();

		// then
		assertThat(writer.toString(), is("["));
	}

	@Test
	public void checkCloseArrayCharacter() {
		writer.closeArray();

		// then
		assertThat(writer.toString(), is("]"));
	}

	@Test
	public void checkOpenObjectCharacter() {
		writer.openObject();

		// then
		assertThat(writer.toString(), is("{"));
	}

	@Test
	public void checkCloseObjectCharacter() {
		writer.closeObject();

		// then
		assertThat(writer.toString(), is("}"));
	}

	@Test
	public void checkElementSeperatorCharacter() {
		writer.insertElementSeperator();

		// then
		assertThat(writer.toString(), is(","));
	}

	@Test
	public void checkKeyValueSeperatorCharacter() {
		writer.insertKeyValueSeperator();

		// then
		assertThat(writer.toString(), is(":"));
	}

	@Test
	public void checkValueFormatting() {
		writer.insertValue("false");

		// then
		assertThat(writer.toString(), is("false"));
	}

	@Test
	public void checkStringValueFormatting() {
		writer.insertStringValue("false");

		// then
		assertThat(writer.toString(), is("\"false\""));
	}

	@Test
	public void checkKeyFormatting() {
		writer.insertKey("Key");

		// then
		assertThat(writer.toString(), is("\"Key\""));
	}

}
