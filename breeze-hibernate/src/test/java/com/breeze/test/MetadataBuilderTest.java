package com.breeze.test;

import java.util.HashMap;
import java.util.List;

import org.hibernate.SessionFactory;

import com.breeze.hib.MetadataBuilder;
import com.breeze.metadata.Metadata;
import com.breeze.metadata.RawMetadata;
import com.breeze.util.JsonGson;
import com.breeze.test.Serializer;

import junit.framework.TestCase;

public class MetadataBuilderTest extends TestCase {

	public static final String NORTHWINDIB_METADATA_SER = "src/test/resources/northwindib_metadata.ser";
	public static final String NORTHWINDIB_METADATA_JSON = "src/test/resources/northwindib_metadata.json";
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void exportMetadata(RawMetadata metadata)
	{
		Serializer.write(metadata, NORTHWINDIB_METADATA_SER);
	}
	
	public RawMetadata importMetadata()
	{
		Object obj = Serializer.read(NORTHWINDIB_METADATA_SER);
		return (RawMetadata) obj;
	}
	
	public void exportMetadataString(String json)
	{
		Serializer.writeString(json, NORTHWINDIB_METADATA_JSON);
	}
	
	public String importMetadataString() {
		return Serializer.readString(NORTHWINDIB_METADATA_JSON);
	}
	
	public String toJson(RawMetadata metadata) {
		String json = JsonGson.toJson(metadata);
		return json;

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
		RawMetadata rawMetadata = metadata.getRawMetadata();
		assertNotNull(metadata);
		assertNotNull(rawMetadata.get("localQueryComparisonOptions"));
		assertTrue(rawMetadata.get("localQueryComparisonOptions") instanceof String);
		assertTrue(rawMetadata.get("structuralTypes") instanceof List);
		assertTrue(rawMetadata.get("resourceEntityTypeMap") instanceof HashMap);
		assertTrue(rawMetadata.foreignKeyMap instanceof HashMap);
		String jsonMetadata = JsonGson.toJson(rawMetadata);
		
		// compare to known good metadata
//		String json = toJson(metadata);
//		
//		String storedMetadata = importMetadataString();
//		assertNotNull(storedMetadata);
//
//		assertEquals(storedMetadata, json);
	}

}
