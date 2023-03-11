package com.test.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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
		Boolean skipChannel = false;

		testFn(inputFile, outputFile, skipChannel);
	}

	private static void testFn(String iFile, String oFile, Boolean b) {
		// Read the input XML file
        String xmlString = null;
        try {
            xmlString = new String(Files.readAllBytes(Paths.get(iFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Create a new XStream instance and configure it to use annotations
        XStream xstream = new XStream();
		xstream.allowTypesByWildcard(new String[] {"com.test.test.**"});	// TESTING
		xstream.processAnnotations(XML_input.class);

        // Convert the XML string into an Item object
        XML_input xmlInput = (XML_input) xstream.fromXML(xmlString);
        Item item_p = xmlInput.channel.item;

		// Price
		item_p.price = new Price();
		item_p.price.value = item_p.price_.replace("EUR", "").trim();
		item_p.price.currency = "EUR";

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
		physicalMasurement.unit = "not specified";
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


        // Create a new Gson instance
        //Gson gson = new Gson();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Convert the Item object into a JSON string
        String jsonOutput = gson.toJson(xmlInput);
		if (b) {	// if skipChannel = true
			JsonObject data = gson.fromJson(jsonOutput, JsonObject.class);
			JsonObject itemData = data.getAsJsonObject("channel").getAsJsonObject("item");
			jsonOutput = gson.toJson(itemData);
		}
		
        // Write the JSON string to the output file
        try (FileWriter fileWriter = new FileWriter(new File(oFile))) {
            fileWriter.write(jsonOutput);
            System.out.println("Finished writing.");
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public static void parseXMLtoJSON(String iFile, String oFile) {
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

@XStreamAlias("rss")
class XML_input {
    @XStreamAlias("channel")
    public Channel channel;

	public Item getItem() {
		return channel.item;
	}
}

@XStreamAlias("channel")
class Channel {

    @XStreamAlias("title")
    public String title;
    
    @XStreamAlias("description")
    public String description;
    
    @XStreamAlias("item")
    public Item item;
}

@XStreamAlias("item")
class Item {

	@XStreamAlias("g:brand")
    private String brand;

	@XStreamAlias("g:id")
    private String external_id;

	@XStreamAlias("g:item_group_id")
    private String item_group_id;

	@XStreamAlias("g:title")
    private String name;

	@XStreamAlias("g:description")
    private String description;

	@XStreamAlias("g:manual")
    public String manual;
	
	@XStreamAlias("g:google_product_category")
    public String google_product_category;

	@XStreamAlias("g:product_type")
    public String product_type;

	@XStreamAlias("g:availability")
    private String availability;

	@XStreamAlias("g:availabilityDate")
    private String availabilityDate;

    @XStreamAlias("g:price")
    public String price_;

    @XStreamAlias("g:price_ek")
    public String price_ek;

    @XStreamAlias("g:image_link")
    public String image_link;

    @XStreamAlias("g:condition")
    public String condition;

    @XStreamAlias("g:size")
    public String size;

    @XStreamAlias("g:color")
    public String color;

    @XStreamAlias("g:material")
    public String material;

	@XStreamAlias("g:gtin")
    private String gtin;

	@XStreamAlias("g:mpn")
    private String mpn;

	@XStreamAlias("g:link")
    private String link;

	@XStreamAlias("g:shipping")
    public Shipping shipping;

	public Price price;

	public List<String> markets;

	public List<String> categories;

	public List<String> images;

	public List<String> documentation;

	public List<Specification> specifications;

	public List<PhysicalMeasurements> physicalMeasurements;

    public Item (
			String brand, String external_id, String item_group_id, String name, String description, String manual,
			String google_product_category, String product_type, String availability, String availabilityDate,
			String price_, String price_ek, String image_link, String gtin, String mpn, String link
		) {
		this.brand = brand;
		this.external_id = external_id;
		this.item_group_id = item_group_id;
        this.name = name;
        this.description = description;
        this.manual = manual;
        this.google_product_category = google_product_category;
        this.product_type = product_type;
        this.availability = availability;
        this.availabilityDate = availabilityDate;
        this.price_ = price_;
        this.price_ek = price_ek;
        this.image_link = image_link;
        this.gtin = gtin;
		this.mpn = mpn;
		this.link = link;
    }

    // getters and setters ommited
}

@XStreamAlias("g:shipping")
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
