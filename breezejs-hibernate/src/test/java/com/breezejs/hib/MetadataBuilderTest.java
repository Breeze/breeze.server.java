package com.breezejs.hib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.hibernate.SessionFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.breezejs.Metadata;
import com.breezejs.testutil.Serializer;
import com.breezejs.util.Json;

import junit.framework.TestCase;

public class MetadataBuilderTest extends TestCase {

	public static final String NORTHWINDIB_METADATA_SER = "src/test/resources/northwindib_metadata.ser";
	public static final String NORTHWINDIB_METADATA_JSON = "src/test/resources/northwindib_metadata.json";
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void exportMetadata(Metadata metadata)
	{
		Serializer.write(metadata, NORTHWINDIB_METADATA_SER);
	}
	
	public Metadata importMetadata()
	{
		Object obj = Serializer.read(NORTHWINDIB_METADATA_SER);
		return (Metadata) obj;
	}
	
	public void exportMetadataString(String json)
	{
		Serializer.writeString(json, NORTHWINDIB_METADATA_JSON);
	}
	
	public String importMetadataString() {
		return Serializer.readString(NORTHWINDIB_METADATA_JSON);
	}
	
	public String toJson(Metadata metadata) {
		try {
			String json = Json.toJson(metadata, false, false);
			// reformat for pretty-printing
			String jsonPretty = new JSONObject(json).toString(4);
			return jsonPretty;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/* only run this when you want to change the stored metadata
	public void testExportMetadata() {
		SessionFactory sf = StaticConfigurator.getSessionFactory();
		MetadataBuilder mb = new MetadataBuilder(sf);
		Metadata metadata = mb.buildMetadata();
		exportMetadata(metadata);
		
		Metadata metadata2 = importMetadata();
		
		assertNotNull(metadata2);
	}
	*/

	/* only run this when you want to change the stored metadata
	public void testExportMetadataJson() {
		SessionFactory sf = StaticConfigurator.getSessionFactory();
		MetadataBuilder mb = new MetadataBuilder(sf);
		Metadata metadata = mb.buildMetadata();
		String json = toJson(metadata);
		exportMetadataString(json);
		
		String json2 = importMetadataString();
		
		assertNotNull(json2);
		assertEquals(json, json2);
	}
	*/
	
	
	/**
	 * Tests metadata from the NorthwindIB configuration
	 */
	public void testBuildMetadata() {
		SessionFactory sf = StaticConfigurator.getSessionFactory();
		
		MetadataBuilder mb = new MetadataBuilder(sf);
		
		Metadata metadata = mb.buildMetadata();

		assertNotNull(metadata);
		assertNotNull(metadata.get("localQueryComparisonOptions"));
		assertTrue(metadata.get("localQueryComparisonOptions") instanceof String);
		assertTrue(metadata.get("structuralTypes") instanceof List);
		assertTrue(metadata.get("resourceEntityTypeMap") instanceof HashMap);
		assertTrue(metadata.foreignKeyMap instanceof HashMap);
		
		// compare to known good metadata
		String json = toJson(metadata);
		
		String storedMetadata = importMetadataString();
		assertNotNull(storedMetadata);

		assertEquals(storedMetadata, json);
	}

}
