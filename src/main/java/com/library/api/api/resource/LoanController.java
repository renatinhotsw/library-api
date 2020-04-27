package com.library.api.api.resource;

import com.library.api.api.dto.LoanDTO;
import com.library.api.model.Book;
import com.library.api.model.Loan;
import com.library.api.service.BookService;
import com.library.api.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private LoanService loanService;
    private BookService bookService;

    public LoanController(LoanService loanService,BookService bookService) {
        this.loanService = loanService;
        this.bookService = bookService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long emprestar(@RequestBody @Valid LoanDTO dto){
        Book book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow( ()->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST,"bock not found for passed isbn"));
        Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = loanService.save(entity);

        return savedLoan.getId();
    }
}
