package de.adorsys.ledgers.middleware.rest.utils;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class CustomPageImpl<T> {
    private static final long serialVersionUID = 1L;
    private int number;
    private int size;
    private int totalPages;
    private int numberOfElements;
    private long totalElements;
    private boolean previousPage;
    private boolean firstPage;
    private boolean nextPage;
    private boolean lastPage;
    private List<T> content;
    private Pageable pageable;

    public Page<T> pageImpl() {
        return new PageImpl<>(getContent(), PageRequest.of(getNumber(), getSize()), getTotalElements());
    }

    public CustomPageImpl(Page<T> page) {
        this.number = page.getNumber();
        this.size = page.getSize();
        this.totalPages = page.getTotalPages();
        this.numberOfElements = page.getNumberOfElements();
        this.totalElements = page.getTotalElements();
        this.previousPage = page.hasPrevious();
        this.nextPage = page.hasNext();
        this.lastPage = page.isLast();
        this.content = page.getContent();
    }

    public void setPageable() {
        this.pageable = PageRequest.of(this.number, this.size);
    }
}
