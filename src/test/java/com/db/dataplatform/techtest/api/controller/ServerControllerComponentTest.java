package com.db.dataplatform.techtest.api.controller;

import com.db.dataplatform.techtest.TestDataHelper;
import com.db.dataplatform.techtest.server.api.controller.ServerController;
import com.db.dataplatform.techtest.server.api.model.DataBody;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.api.model.DataHeader;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class ServerControllerComponentTest {

	public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
	public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
	public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");

	@Mock
	private Server serverMock;

	private DataEnvelope testDataEnvelope;
	private ObjectMapper objectMapper;
	private MockMvc mockMvc;
	private ServerController serverController;

	@Before
	public void setUp() throws NoSuchAlgorithmException, IOException {
		serverController = new ServerController(serverMock);
		mockMvc = standaloneSetup(serverController).build();
		objectMapper = Jackson2ObjectMapperBuilder
				.json()
				.build();

		testDataEnvelope = TestDataHelper.createTestDataEnvelopeApiObject();

		when(serverMock.saveDataEnvelope(any(DataEnvelope.class))).thenReturn(true);
	}

	@Test
	public void testPushDataPostCallWorksAsExpected() throws Exception {
		String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);

		MvcResult mvcResult = mockMvc.perform(post(URI_PUSHDATA)
				.content(testDataEnvelopeJson)
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andReturn();

		boolean checksumPass = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());
		assertThat(checksumPass).isTrue();
	}

	@Test
	public void testPatchPostCallWorksAsExpected() throws Exception {

		String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);
		when(serverMock.updateBlockType(anyString(), anyString())).thenReturn(true);

		MvcResult mvcResult = mockMvc.perform(patch(URI_PATCHDATA.expand("name" , BlockTypeEnum.BLOCKTYPEA.name()))
				.content(testDataEnvelopeJson)
				.contentType(MediaType.TEXT_PLAIN))
				.andExpect(status().isOk())
				.andReturn();

		boolean response = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());
		assertThat(response).isTrue();
	}


	@Test
	public void testGetDataInvalidBlockType() throws Exception {
		MvcResult mvcResult = mockMvc.perform(get(URI_GETDATA.expand("ddd")))
				.andExpect(status().is(400))
				.andReturn();
		assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
	}

	@Test
	public void testGetDataWorksAsExpected() throws Exception {
		DataBody b = new DataBody("foo" ,"X");
		DataHeader h = new DataHeader("A" , BlockTypeEnum.BLOCKTYPEA);
		DataEnvelope e = new DataEnvelope(h, b);

		when(serverMock.findBy(BlockTypeEnum.BLOCKTYPEA))
				.thenReturn(Arrays.asList(e));
		MvcResult mvcResult = mockMvc.perform(get(URI_GETDATA.expand(BlockTypeEnum.BLOCKTYPEA.name())))
				.andExpect(status().is(200))
				.andReturn();
		List<DataEnvelope> envelope = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
				new TypeReference<List<DataEnvelope>>() { });

		assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
		assertThat(envelope.size()).isEqualTo(1);
		assertThat(envelope.get(0).getDataBody().getDataBody()).isEqualTo("foo");
		assertThat(envelope.get(0).getDataHeader().getName()).isEqualTo("A");
		assertThat(envelope.get(0).getDataHeader().getBlockType()).isEqualTo(BlockTypeEnum.BLOCKTYPEA);

	}



}
