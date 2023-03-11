package com.test.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAliasType;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.json.JSONObject;
import org.json.XML;


@SpringBootApplication
public class TestApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);

		String inputFile = "inputOutput/input.xml";
		String outputFile = "inputOutput/output.json";
		Boolean humanReadable = true;
		Boolean skipChannel = true;
		String[] skipFields = {"color", "example_item_2", "etc..."};

		inputXMLoutputJSON(inputFile, outputFile, humanReadable, skipChannel, skipFields);
	}

	private static Gson generateJSON(Boolean hr, String[] sf) {
		GsonBuilder builder = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
			@Override
			public boolean shouldSkipField(FieldAttributes field) {
				List<String> fieldNames = Arrays.asList(sf);
				return fieldNames.contains(field.getName());
			}
			@Override
			public boolean shouldSkipClass(Class<?> classSkip) {
				return false; // do not skip any classes by default
			}
		});
		if (hr) {
			builder.setPrettyPrinting();
		}
		return builder.create();
	}

	private static void inputXMLoutputJSON(String iFile, String oFile, Boolean hr, Boolean sc, String[] sf) {
		// Read the input XML file
		System.out.println("Beginning to parse XML file...");
        String xmlString = null;
        try {
            xmlString = new String(Files.readAllBytes(Paths.get(iFile)));
        } catch (IOException e) {
			System.out.println("Parsing of XML file aborted.");
            e.printStackTrace();
        }

        // Parse the XML file into a string
        XStream xstream = new XStream();
		xstream.allowTypes(new Class[] { XML_input.class });
        xstream.processAnnotations(XML_input.class);

        // Convert the XML string into Java object
		System.out.println("Converting XML input...");
        XML_input xmlInput = (XML_input) xstream.fromXML(xmlString);

		// Manipulate the Java object
        for (Item item_p : xmlInput.channel.items) {

			// Price
			if (item_p.price_ != null) {
				item_p.price = new Price();
				item_p.price.value = item_p.price_.replace("EUR", "").trim();
				item_p.price.currency = "EUR";
			}

			// Specifications
			Specification specification = new Specification();
			specification.type = "color";
			specification.value = item_p.color;
			item_p.specifications = new ArrayList<Specification>();
			item_p.specifications.add(specification);

			// Physical Measurements
			PhysicalMeasurements physicalMasurement = new PhysicalMeasurements();
			physicalMasurement.type = "length";
			physicalMasurement.value = item_p.size;
			physicalMasurement.unit = "not specified (we can assume: m)";
			item_p.physicalMeasurements = new ArrayList<PhysicalMeasurements>();
			item_p.physicalMeasurements.add(physicalMasurement);

			// Images
			item_p.images = new ArrayList<>();
			item_p.images.add(item_p.image_link);

			// Category
			item_p.categories = new ArrayList<>();
			item_p.categories.add(item_p.google_product_category);
			
			// Markets
			item_p.markets = new ArrayList<>();
			item_p.markets.add(item_p.shipping.country);

			// Documentation
			item_p.documentation = new ArrayList<>();
			item_p.documentation.add(item_p.manual);
		}


        // Create a new Gson instance
		//Gson gson = new Gson();
		Gson gson = generateJSON(hr, sf); // hr = humanReadable, sf = skipFields

        // Convert object into JSON string
		System.out.println("Converting to JSON string...");
        String jsonOutput = gson.toJson(xmlInput);
		if (sc) {	// if skipChannel = true
			JsonArray itemArray = new JsonArray();
			for (Item item : xmlInput.channel.items) {
				itemArray.add(gson.toJsonTree(item));
			}
			jsonOutput = gson.toJson(itemArray);
		} else {
			jsonOutput = gson.toJson(xmlInput);
		}
		
        // Write JSON string to output file
		System.out.println("Saving to output file...");
        try (FileWriter fileWriter = new FileWriter(new File(oFile))) {
            fileWriter.write(jsonOutput);
			System.out.println("Output file saved.");
        } catch (IOException e) {
			System.out.println("Saving aborted.");
            e.printStackTrace();
        }
	}

	public static void simpleParseXMLtoJSON(String iFile, String oFile) {
		// Read the input XML file
        String xmlString;
		try {
			xmlString = new String(Files.readAllBytes(Paths.get(iFile)));
			System.out.println("Parsing...");

			// Convert the XML string to a JSON object
			JSONObject jsonObject = XML.toJSONObject(xmlString);
			
			// Write the JSON object to the output file
			try (FileWriter fileWriter = new FileWriter(new File(oFile))) {
				if (jsonObject != null) fileWriter.write(jsonObject.toString(4));
				System.out.println("Finished writing.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

// Define the classes that represent the XML elements using XStream annotations
@XStreamAliasType("rss")
class XML_input {
    @XStreamAlias("channel")
    public Channel channel;
}

@XStreamAliasType("channel")
class Channel {

    @XStreamAlias("title")
    public String title;
    
    @XStreamAlias("description")
    public String description;
    
    @XStreamImplicit(itemFieldName="item")
	public Item[] items;
}

@XStreamAliasType("item")
class Item {

	@XStreamAlias("g:brand")
    private String brand;

	@XStreamAlias("g:id")
    private String external_id;

	@XStreamAlias("g:title")
    public String name;

	@XStreamAlias("g:description")
    private String description;

	public List<String> categories;

	public List<String> images;

	public List<String> documentation;

	public List<Specification> specifications;

	public List<PhysicalMeasurements> physicalMeasurements;

	public Price price;

	@XStreamAlias("g:gtin")
    
	private String gtin;

	public List<String> markets;

	@XStreamAlias("g:manual")
    public String manual;

	@XStreamAlias("g:availability")
    private String availability;

	@XStreamAlias("g:availabilityDate")
    private String availabilityDate;

    @XStreamAlias("g:price")
    public String price_;

    @XStreamAlias("g:price_ek")
    public String price_ek;

	@XStreamAlias("g:mpn")
    private String mpn;

	@XStreamAlias("g:shipping")
    public Shipping shipping;

	@XStreamAlias("g:google_product_category")
    public String google_product_category;

	@XStreamAlias("g:product_type")
    public String product_type;

	@XStreamAlias("g:item_group_id")
    private String item_group_id;

	@XStreamAlias("g:size")
    public String size;

    @XStreamAlias("g:color")
    public String color;

    @XStreamAlias("g:material")
    public String material;

	@XStreamAlias("g:link")
    private String link;

    @XStreamAlias("g:image_link")
    public String image_link;

    @XStreamAlias("g:condition")
    public String condition;

    // getters and setters omitted
}

@XStreamAliasType("g:shipping")
class Shipping {
    @XStreamAlias("g:country")
    public String country;
}

class Specification {
    String type;
    String value;
}

class PhysicalMeasurements {
    String type;
    String value;
	String unit;
}

class Price {
    String value;
	String currency;
}
