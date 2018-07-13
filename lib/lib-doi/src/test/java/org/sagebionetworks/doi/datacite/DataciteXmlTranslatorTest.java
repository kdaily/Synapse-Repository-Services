package org.sagebionetworks.doi.datacite;

import org.apache.commons.io.IOUtils;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.doi.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sagebionetworks.doi.datacite.DataciteXmlTranslatorImpl.*;
import static org.sagebionetworks.doi.datacite.DataciteMetadataConstants.*;

public class DataciteXmlTranslatorTest {

	private DocumentBuilder documentBuilder;
	private DataciteDoi expectedDoi;
	private DoiCreator c1;
	private DoiCreator c2;
	private DoiNameIdentifier nameIdObject1;
	private String nameIdentifier1;
	private List<DoiTitle> titles;
	private long publicationYear;
	private DoiResourceType resourceType;


	@Before
	public void before() throws Exception {
		// Create a new document builder for each test
		documentBuilder = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder();

		// Create expected metadata to compare to results
		expectedDoi = new DoiV2();

		// Set fields to match the XML resource loaded below "DataciteSample1.xml"
		// Creators
		c1 = new DoiCreator();
		c1.setCreatorName("Last, First");
		nameIdObject1 = new DoiNameIdentifier();
		nameIdentifier1 = "0123-4567-8987-789X";
		nameIdObject1.setIdentifier(nameIdentifier1);
		nameIdObject1.setNameIdentifierScheme(NameIdentifierScheme.ORCID);
		DoiNameIdentifier nameIdObject2 = new DoiNameIdentifier();
		nameIdObject2.setIdentifier("9876-5432-1012-456X");
		nameIdObject2.setNameIdentifierScheme(NameIdentifierScheme.ISNI);
		List<DoiNameIdentifier> nameIds = new ArrayList<>();
		nameIds.add(nameIdObject1);
		nameIds.add(nameIdObject2);
		c1.setNameIdentifiers(nameIds);
		c2 = new DoiCreator();
		c2.setCreatorName("Sample name");
		List<DoiCreator> creators = new ArrayList<>();
		creators.add(c1);
		creators.add(c2);
		expectedDoi.setCreators(creators);
		// Titles
		titles = new ArrayList<>();
		DoiTitle t1 = new DoiTitle();
		DoiTitle t2 = new DoiTitle();
		t1.setTitle("Some title 1");
		t2.setTitle("Some other title 2");
		titles.add(t1);
		titles.add(t2);
		expectedDoi.setTitles(titles);
		// Publication year
		publicationYear = 2000L;
		expectedDoi.setPublicationYear(publicationYear);
		// Resource type
		resourceType = new DoiResourceType();
		resourceType.setResourceTypeGeneral(DoiResourceTypeGeneral.Dataset);
		expectedDoi.setResourceType(resourceType);
	}

	@Test
	public void getNameIdTest() throws Exception {
		// Name identifier element
		Document dom = documentBuilder.parse(new InputSource(new StringReader("<nameIdentifier schemeURI=\"http://orcid.org/\" nameIdentifierScheme=\"ORCID\">" + nameIdentifier1 + "</nameIdentifier>")));
		Element id1 = (Element)dom.getElementsByTagName(NAME_IDENTIFIER).item(0);
		// Unit under test
		assertEquals(nameIdObject1, getNameIdentifier(id1));
	}

	@Test
	public void getCreatorTest() throws Exception {
		// Creator element with just a name
		Document dom = documentBuilder.parse(new InputSource(new StringReader("<creator><creatorName>" + c2.getCreatorName() + "</creatorName></creator>")));
		Element creator = (Element)dom.getElementsByTagName(CREATOR).item(0);
		// Unit under test
		assertEquals(c2, getCreator(creator));
	}

	@Test
	public void getCreatorWithMultipleIdentifiersTest() throws Exception {
		Document dom = documentBuilder.parse(new InputSource(new StringReader(
				"<creator><creatorName>" + c1.getCreatorName() + "</creatorName>" +
						"<nameIdentifier schemeURI=\"" + getSchemeUri(c1.getNameIdentifiers().get(0).getNameIdentifierScheme()) + "\" nameIdentifierScheme=\"" + c1.getNameIdentifiers().get(0).getNameIdentifierScheme().name() + "\">" + c1.getNameIdentifiers().get(0).getIdentifier() + "</nameIdentifier>" +
						"<nameIdentifier schemeURI=\"" + getSchemeUri(c1.getNameIdentifiers().get(1).getNameIdentifierScheme()) + "\" nameIdentifierScheme=\"" + c1.getNameIdentifiers().get(1).getNameIdentifierScheme().name() + "\">" + c1.getNameIdentifiers().get(1).getIdentifier() + "</nameIdentifier></creator>"
		)));
		Element creator = (Element)dom.getElementsByTagName(CREATOR).item(0);
		// Unit under test
		assertEquals(c1, getCreator(creator));
	}

	@Test
	public void getCreatorsTest() throws Exception {
		Document dom = documentBuilder.parse(new InputSource(new StringReader("<creators><creator><creatorName>Author 1</creatorName></creator><creator><creatorName>2, Author</creatorName></creator></creators>")));
		List<DoiCreator> expected = new ArrayList<>();
		DoiCreator creator1 = new DoiCreator();
		creator1.setCreatorName("Author 1");
		DoiCreator creator2 = new DoiCreator();
		creator2.setCreatorName("2, Author");
		expected.add(creator1);
		expected.add(creator2);

		assertEquals(expected, getCreators(dom));
	}

	@Test
	public void getTitlesTest() throws Exception {
		Document dom = documentBuilder.parse(new InputSource(new StringReader("<titles><title>"+ titles.get(0).getTitle() +"</title><title>" + titles.get(1).getTitle() + "</title></titles>")));
		assertEquals(titles, getTitles(dom));
	}

	@Test
	public void getPubYearTest() throws Exception {
		Document dom = documentBuilder.parse(new InputSource(new StringReader("<publicationYear>" + publicationYear + "</publicationYear>")));
		assertEquals(Long.valueOf(publicationYear), Long.valueOf(getPublicationYear(dom)));
	}

	@Test
	public void getResourceTypeTest() throws Exception {
		Document dom = documentBuilder.parse(new InputSource(new StringReader("<resourceType resourceTypeGeneral=\"" + resourceType.getResourceTypeGeneral().name() + "\"></resourceType>")));
		assertEquals(resourceType, getResourceType(dom));
	}

	@Test
	public void testNoUndefinedSchemes() {
		for (NameIdentifierScheme e : NameIdentifierScheme.values()) {
			assertNotNull(getSchemeUri(e));
		}
	}

	@Test
	public void testGetSchemeUri() {
		assertEquals(ORCID_URI, getSchemeUri(NameIdentifierScheme.ORCID));
		assertEquals(ISNI_URI, getSchemeUri(NameIdentifierScheme.ISNI));
		// If adding a new scheme, test it with its URI pair here
	}

	// Tests the entire class
	@Test
	public void translateEntireDomTest() throws Exception{
		// Load the resource containing XML
		ClassLoader loader = this.getClass().getClassLoader();
		String xml = IOUtils.toString(loader.getResourceAsStream("DataciteSample1.xml"));

		DataciteXmlTranslatorImpl translator = new DataciteXmlTranslatorImpl();
		// Unit under test
		DataciteDoi metadata = translator.translate(xml);
		assertEquals(expectedDoi, metadata);
	}


}
