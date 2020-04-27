package com.library.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.api.api.dto.BookDTO;
import com.library.api.exceptions.BusinessException;
import com.library.api.model.Book;
import com.library.api.model.repository.BookRepository;
import com.library.api.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookTestService {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setService(){
        this.service = new BookServiceImpl( repository );
    }

    @Test
    @DisplayName("salvar livro")
    public void saveBookTest(){

        //cenario
        Book book = Book.builder()
                .isbn("123")
                .author("renato")
                .title("as aventuras").build();

        Mockito.when( repository.existsByIsbn( Mockito.anyString()) ).thenReturn(false);

        Mockito.when(repository.save(book))
                .thenReturn(
                        Book.builder()
                        .id(1L)
                        .isbn("123")
                        .title("as aventuras")
                        .author("renato").build()
                );

        //execucao
        Book savedBook = service.save(book);

        //verificacao
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("as aventuras");
        assertThat(savedBook.getAuthor()).isEqualTo("renato");
    }

    @Test
    @DisplayName("lança erro ao cadastrar isbn duplicado")
    public void shouldNotSaveBookWithDuplicateIsbn(){
        //cenario
        Book book = Book.builder()
                .isbn("123")
                .author("renato")
                .title("as aventuras").build();

        Mockito.when( repository.existsByIsbn( Mockito.anyString() ) ).thenReturn(true);

        //execucao
        Throwable exception =  Assertions.catchThrowable( ()-> service.save(book) );

        //verificacao
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("isbn já cadastrada");

        Mockito.verify(repository,Mockito.never()).save(book);
    }

    @Test
    @DisplayName("busca livro por id")
    public void getByIdTest(){
        //cenario
        Long id = 1l;

        Book book = Book.builder()
                .isbn("123")
                .author("renato")
                .title("as aventuras").build();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        //execucao
        Optional<Book> foundBook = service.getById(id);

        //verificacao
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }


    @Test
    @DisplayName("lança erro ao nao encontrar livro por id")
    public void bookNotFoundgetByIdTest(){
        //cenario
        Long id = 1l;

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        //execucao
        Optional<Book> book = service.getById(id);

        //verificacao
        assertThat(book.isPresent()).isFalse();

    }

    @Test
    @DisplayName("deleta um livro com id")
    public void deleteBook(){
        //cenario
        Book book = Book.builder()
                .id(1L)
                .isbn("123")
                .title("as aventuras")
                .author("renato").build();

        //execucao
       org.junit.jupiter.api.Assertions.assertDoesNotThrow( ()-> service.delete(book) );

        //verificacao
        Mockito.verify(repository,Mockito.times(1)).delete(book);

    }

    @Test
    @DisplayName("erro ao tentar deletar livro inexistente ")
    public void deleteInvalidBookTest(){
        //cenario
        Book book = new Book();

        //execucao
        org.junit.jupiter.api.Assertions
                .assertThrows(IllegalArgumentException.class, ()-> service.delete(book));

        //verificacao
        Mockito.verify(repository,Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("atualiza um livro com id")
    public void updateBook(){
        //cenario
        Book book = Book.builder().id(1L).build();

        Book updateBook = Book.builder()
                .id(1L)
                .isbn("123")
                .title("as aventuras")
                .author("renato").build();

        Mockito.when(repository.save(updateBook)).thenReturn(updateBook);

        //execucao
        book = service.update(updateBook);

        //verificacao
       assertThat(book.getId()).isEqualTo(updateBook.getId());
       assertThat(book.getAuthor()).isEqualTo(updateBook.getAuthor());
       assertThat(book.getTitle()).isEqualTo(updateBook.getTitle());
       assertThat(book.getIsbn()).isEqualTo(updateBook.getIsbn());


    }

    @Test
    @DisplayName("erro ao tentar atualizar livro inexistente ")
    public void updateInvalidBookTest(){
        //cenario
        Book book = new Book();

        //execucao
        org.junit.jupiter.api.Assertions
                .assertThrows(IllegalArgumentException.class, ()-> service.update(book));

        //verificacao
        Mockito.verify(repository,Mockito.never()).save(book);
    }

    @Test
    @DisplayName("filtra livros por title e author")
    public void findBookTest(){

        PageRequest pageRequest = PageRequest.of(0,10);

        Book book = Book.builder()
                .id(1L)
                .isbn("123")
                .title("as aventuras")
                .author("renato").build();

        Page<Book> page = new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0,10),1);
        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Book> result = service.find(book,pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(Arrays.asList(book));
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

}