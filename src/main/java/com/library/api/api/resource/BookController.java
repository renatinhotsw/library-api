package com.library.api.api.resource;

import com.library.api.api.dto.BookDTO;
import com.library.api.exceptions.ApiErrors;
import com.library.api.exceptions.BusinessException;
import com.library.api.model.Book;
import com.library.api.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@EnableAutoConfiguration
public class BookController {

    private BookService service;
    private ModelMapper modelMapper;

    public BookController(BookService service, ModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto){
        Book entity = modelMapper.map(dto,Book.class);
        Book entitySave = service.save(entity);

        return modelMapper.map(entitySave,BookDTO.class);
    }

    @GetMapping("{id}")
    public BookDTO get(@PathVariable Long id){
      return service
                .getById(id)
                .map( book -> modelMapper.map(book,BookDTO.class))
                .orElseThrow( ()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PutMapping("{id}")
    public BookDTO update(@PathVariable Long id, BookDTO dto){
      return service.getById(id).map(book -> {
            book.setAuthor(dto.getAuthor());
            book.setTitle(dto.getTitle());
            Book upd = service.update(book);
            return modelMapper.map(upd,BookDTO.class);

      }).orElseThrow( ()-> new ResponseStatusException(HttpStatus.NOT_FOUND) );
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
       Book book = service.getById(id).orElseThrow( ()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
       service.delete(book);
    }



    @GetMapping
    public Page<BookDTO> findBook(BookDTO dto, Pageable pageRequest){
        Book filter = modelMapper.map(dto,Book.class);
        Page<Book> result = service.find(filter,pageRequest);

        List<BookDTO> list = result.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity,BookDTO.class))
                .collect(Collectors.toList());
        
        //List<T> content, Pageable pageable, long total (parametros de PageImpl)
        return new PageImpl<BookDTO>(list,pageRequest,result.getTotalElements());
    }
}
