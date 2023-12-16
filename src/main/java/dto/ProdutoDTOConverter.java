package dto;

import model.Produto;
import org.modelmapper.ModelMapper;

public class ProdutoDTOConverter {
    private ModelMapper modelMapper = new ModelMapper();
    public ProdutoDTO convertToDTO(Produto produto) {
        return modelMapper.map(produto, ProdutoDTO.class);
    }
}
