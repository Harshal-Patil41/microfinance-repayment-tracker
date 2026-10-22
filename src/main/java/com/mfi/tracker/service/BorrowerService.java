package com.mfi.tracker.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mfi.tracker.exception.NotFoundException;
import com.mfi.tracker.model.Borrower;
import com.mfi.tracker.repository.BorrowerRepository;

@Service
@Transactional
public class BorrowerService {

    private final BorrowerRepository repository;

    public BorrowerService(BorrowerRepository repository) {
        this.repository = repository;
    }

    public List<Borrower> findAll() {
        return repository.findAll(Sort.by("id"));
    }

    public Borrower get(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Borrower " + id + " was not found"));
    }

    public long count() {
        return repository.count();
    }

    public Borrower create(Borrower input) {
        return repository.save(new Borrower(input.getName().trim(), input.getPhone().trim(), input.getVillage().trim()));
    }
}
