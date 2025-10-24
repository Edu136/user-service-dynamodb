package br.unibh.userservice.service;

import lombok.Getter;

import java.util.List;

@Getter
public class PaginatedResult<T> {
    private final List<T> items;
    private final String nextKey;

    public PaginatedResult(List<T> items, String nextKey) {
        this.items = items;
        this.nextKey = nextKey;
    }
}
