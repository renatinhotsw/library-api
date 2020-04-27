package com.library.api.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.api.api.dto.BookDTO;
import com.library.api.exceptions.BusinessException;
import com.library.api.model.Book;
import com.library.api.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    private String BOOK_API = "/api/books";
    private ModelMapper modelMapper;

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("cria um livro")
    public void createBookTest() throws Exception{

        BookDTO dto = BookDTO.builder().author("renato").title("as aventuras").isbn("123").build();
        Book savedBook = Book.builder().id(10L).author("renato").title("as aventuras").isbn("123").build();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);
        String json = new ObjectMapper().writeValueAsString(dto); // transforma uma string em json

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json); // passa json com dados do livro

        mvc
                .perform(request)
                .andExpect( status().isCreated() )
                .andExpect( jsonPath("id").value(10) )
                .andExpect( jsonPath("title").value(dto.getTitle()) )
                .andExpect( jsonPath("author").value(dto.getAuthor()) )
                .andExpect( jsonPath("isbn").value(dto.getIsbn()) );

    }

    @Test
    @DisplayName("lançar erro ao criar livro inválido")
    public void createInvalidBookTest() throws Exception{
        //cenario
        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Lança erro ao cadastrar isbn duplicada")
    public void createBookWithDuplicateIsbn() throws Exception{
        BookDTO dto = BookDTO.builder().author("renato").title("as aventuras").isbn("123").build();
        String json = new ObjectMapper().writeValueAsString(dto);
        String msg = "isbn já cadastrada";

        BDDMockito.given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(msg));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors",hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(msg));
    }

    @Test
    @DisplayName("obtem informacoes do livro")
    public void getBookDetailsTest() throws Exception{
        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .author("renato")
                .title("as aventuras")
                .isbn("123").build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/"+id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(book.getId()))
                .andExpect(jsonPath("author").value(book.getAuthor()))
                .andExpect(jsonPath("title").value(book.getTitle()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));
    }

    @Test
    @DisplayName("lanca erro ao não encontrar o livro com id informado")
    public void bookNotFoundTest() throws Exception{

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/"+1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("exclui um livro")
    public void deleteBookTest() throws Exception{

        BDDMockito.given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.of(Book.builder().id(1L).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/"+1));

        mvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("retorna que o livro nao foi encontrado pra delecao")
    public void deleteNotFoundBookTest() throws Exception{

        BDDMockito.given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/"+1));

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Atualiza dados do livro")
    public void updateBookTest() throws Exception{
        Long id = 1L;

        Book book = Book.builder()
                .id(id)
                .author("renato")
                .title("as aventuras")
                .isbn("123").build();

        String json = new ObjectMapper().writeValueAsString(book);
        Book bookUpdate = Book.builder()
                .id(id)
                .title("update title")
                .author("update author")
                .isbn("567")
                .build();

        BDDMockito.given(service.getById(id))
                .willReturn(Optional.of(bookUpdate));
        BDDMockito.given(service.update(bookUpdate)).willReturn(bookUpdate);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/"+1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value(bookUpdate.getTitle()))
                .andExpect(jsonPath("author").value(bookUpdate.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookUpdate.getIsbn()));
    }

    @Test
    @DisplayName("NotFound - Atualiza dados do livro")
    public void updateBookNotFoundTest() throws Exception{

        Book book = Book.builder()
                .id(1l)
                .author("renato")
                .title("as aventuras")
                .isbn("123").build();

        String json = new ObjectMapper().writeValueAsString(book);

        BDDMockito.given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/"+1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("filtra livros por parametros")
    public void findBooksTest() throws Exception{

        //cenario
        Long id = 1L;

        Book book = Book.builder()
                .id(id)
                .author("renato")
                .title("as aventuras")
                .isbn("123").build();

        BDDMockito.given( service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)) )
                .willReturn( new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0,100),1));

        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                             book.getTitle(),
                             book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

}
