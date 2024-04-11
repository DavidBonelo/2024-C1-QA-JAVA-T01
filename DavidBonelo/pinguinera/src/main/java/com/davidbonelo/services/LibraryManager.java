package com.davidbonelo.services;

import com.davidbonelo.models.LibraryItem;

import java.util.Collections;
import java.util.List;

public class LibraryManager {
    List<LibraryItem> Books = Collections.emptyList();
    List<LibraryItem> Novels = Collections.emptyList();

    public List<LibraryItem> filterBooksByAuthor(String author) {
        return Books.stream().filter(b -> b.getAuthor().equals(author)).toList();
    }
}
