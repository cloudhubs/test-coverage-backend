package com.groupfour.testcoveragetool;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import com.groupfour.testcoveragetool.group.APIType;
import com.groupfour.testcoveragetool.group.elasticsearch.ElasticSearchReader;

class ElasticsearchTests {
	ElasticSearchReader ers = new ElasticSearchReader();

	private static final int MAX_LENGTH = 100; // maximum length of generated text
    private static final String[] REQUEST_TYPES = {"GET", "POST", "PUT", "DELETE"};
    
	@Test
	void testGetEndpointCapture() {
		String requestType = ElasticSearchReader.findRequestType("ABDCDFLAL:JKDSLKG GET afalf;alkfdjladlfkjal;djf");
		
		assertEquals(requestType, APIType.GET.toString());
	}
	@Test
	void testPostEndpointCapture() {
		String requestType = ElasticSearchReader.findRequestType("ABDCDFLAL:JKDSLKG POST afalf;alkfdjladlfkjal;djf");
		
		assertEquals(requestType, APIType.POST.toString());
	}
	@Test
	void testPutEndpointCapture() {
		String requestType = ElasticSearchReader.findRequestType("ABDCDFLAL:JKDSLKG PUT afalf;alkfdjladlfkjal;djf");
		
		assertEquals(requestType, APIType.PUT.toString());
	}
	@Test
	void testDeleteEndpointCapture() {
		String requestType = ElasticSearchReader.findRequestType("ABDCDFLAL:JKDSLKG DELETE afalf;alkfdjladlfkjal;djf");
		
		assertEquals(requestType, APIType.DELETE.toString());
	}

    @Test
    public void testExtractEndpointWhenValidStringIsProvided() {
        String requestType = getRandomRequestType();
        String mapping = "/mapping/123/";
        String inputString = generateInputString(requestType, mapping);
        String endpoint = ElasticSearchReader.extractEndpoint(inputString);
        assertEquals(requestType + " " + mapping, endpoint);
    }

    @Test
    public void testExtractEndpointWhenMappingHasNoTrailingSlash() {
        String requestType = getRandomRequestType();
        String mapping = "/mapping/123";
        String inputString = generateInputString(requestType, mapping);
        String endpoint = ElasticSearchReader.extractEndpoint(inputString);
        assertEquals(requestType + " " + mapping, endpoint);
    }

    @Test
    public void testExtractEndpointWhenMappingHasMultipleSegments() {
        String requestType = getRandomRequestType();
        String mapping = "/mapping/123/sub/456/";
        String inputString = generateInputString(requestType, mapping);
        String endpoint = ElasticSearchReader.extractEndpoint(inputString);
        assertEquals(requestType + " " + mapping, endpoint);
    }

    @Test
    public void testExtractEndpointWhenNullIsProvided() {
        assertThrows(NullPointerException.class, () -> ElasticSearchReader.extractEndpoint(null));
    }

    @Test
    public void testExtractEndpointWhenEmptyStringIsProvided() {
    	String s = "";
        assertThrows(IllegalArgumentException.class, () -> ElasticSearchReader.extractEndpoint(s));
    }

    @Test
    public void testExtractEndpointWhenOnlySpaceIsProvided() {
        assertThrows(NullPointerException.class, () -> ElasticSearchReader.extractEndpoint(" "));
    }

    private String generateInputString(String requestType, String mapping) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append(generateRandomString(random.nextInt(MAX_LENGTH)));
        sb.append(requestType);
        sb.append(generateRandomString(random.nextInt(MAX_LENGTH)));
        sb.append(mapping);
        sb.append(" ");
        sb.append(generateRandomString(random.nextInt(MAX_LENGTH)));
        return sb.toString();
    }

    private String getRandomRequestType() {
        Random random = new Random();
        return REQUEST_TYPES[random.nextInt(REQUEST_TYPES.length)];
    }

    private String generateRandomString(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+{}[]|;:<>,.?~";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(new Random().nextInt(chars.length())));
        }
        return sb.toString();
    }
}
