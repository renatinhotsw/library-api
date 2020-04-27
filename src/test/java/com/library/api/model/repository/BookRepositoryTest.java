package com.library.api.model.repository;

import com.library.api.model.Book;
import org.aspectj.apache.bcel.Repository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.PersistenceContext;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("retorna verdadeiro caso exista o livro na base com isbn informado")
    public void returnTrueWhenIsbnExists(){
        //cenario
        String isnb = "123";
        Book book = Book.builder().isbn("123").title("as aventuras").author("renato").build();
        entityManager.persist(book);

        //execucao
        boolean b = repository.existsByIsbn(isnb);

        //verificacao
        assertThat(b).isTrue();

    }

    @Test
    @DisplayName("retorna verdadeiro caso exista o livro na base com isbn informado")
    public void returnFalseWhenIsbnExists(){
        //cenario
        String isnb = "123";
        Book book = Book.builder().isbn("123").title("as aventuras").author("renato").build();
        //entityManager.persist(book);

        //execucao
        boolean b = repository.existsByIsbn(isnb);

        //verificacao
        assertThat(b).isFalse();
    }

    @Test
    @DisplayName("obtem um livro persitido na base")
    public void findByIdTest(){
        //cenario
        Book book = Book.builder().isbn("123").title("as aventuras").author("renato").build();
        entityManager.persist(book);

        //execucao
       Optional<Book> foundBook = repository.findById(book.getId());

       //verificacao
        assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("deve salvar livro")
    public void saveBookTest(){
        Book book = Book.builder().isbn("123").title("as aventuras").author("renato").build();

        Book savedBook = repository.save(book);
        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("deve deletar um livro")
    public void deleteBookTest(){
        //cenario
        Book book = Book.builder().isbn("123").build();
        entityManager.persist(book);

        Book foundBook = entityManager.find(Book.class,book.getId());

        //execucao
        repository.delete(foundBook);

        Book deletedBook = entityManager.find(Book.class,book.getId());
        assertThat(deletedBook).isNull();

    }

}
