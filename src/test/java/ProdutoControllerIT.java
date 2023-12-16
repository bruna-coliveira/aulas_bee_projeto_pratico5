import dto.ProdutoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import repository.ProdutoRepository;
import org.springframework.http.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = TestMongoDBSettings.class)
public class ProdutoControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String produtoJson;

    private ProdutoDTO produtoDTO;
    private ProdutoDTO produtoDTOReturn;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        produtoRepository.deleteAll();
        criarProdutoDeTeste();
    }


    @Test
    public void testCadastrarProdutoEVerificar() throws Exception {

        criarProdutoNaBase();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/produtos/consultar/{nome}",
                                produtoDTO.getNome()))
                .andExpect(status().isOk())
                .andReturn();

        String produtoRet = result.getResponse().getContentAsString();
        assert produtoRet.contains(produtoDTO.getNome());

    }

    @Test
    public void testDeletarUmProduto() throws Exception {
        criarProdutoNaBase();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/produtos/{id}", produtoDTOReturn.getId()))
                .andExpect(status().isOk())
                .andReturn();
        String ret = result.getResponse().getContentAsString();
        assert ret.contains("Produto deletado");

    }

    @Test
    public void testBuscarProdutoPeloNomeNaoEncontrado() throws Exception {
        criarProdutoNaBase();
        produtoDTO.setNome("Nome diferente!");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/produtos/consultar/{nome}",
                                produtoDTO.getNome()))
                .andExpect(status().isNotFound())
                .andReturn();
        String ret = result.getResponse().getContentAsString();
        assert ret.isEmpty();

    }

    @Test
    public void testCadastrarProdutoInvalido() throws Exception {
        criarProdutoDTOInvalido();
        produtoJson = objectMapper.writeValueAsString(produtoDTO);
        MvcResult result;
        result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isBadRequest())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody, containsString("Informe o nome do produto"));
        assertThat(responseBody, containsString("A descrição deve ter no mínimo 10 caracteres"));
        assertThat(responseBody, containsString("O preço dever ser no mínimo 1"));

    }

    private void criarProdutoDTOInvalido() {
        produtoDTO = new ProdutoDTO();
        produtoDTO.setDescricao("Prod");
        produtoDTO.setPreco(1);
    }

    private void criarProdutoNaBase() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isOk())
                .andReturn();
        produtoDTOReturn = objectMapper.readValue(result.getResponse().getContentAsString(), ProdutoDTO.class);

    }

    private void criarProdutoDeTeste() throws JsonProcessingException {
        produtoDTO = new ProdutoDTO();
        produtoDTO.setNome("ProdutoIT");
        produtoDTO.setDescricao("ProdutoIT descrição");
        produtoDTO.setPreco(5.99);

        produtoJson = objectMapper.writeValueAsString(produtoDTO);

    }

}