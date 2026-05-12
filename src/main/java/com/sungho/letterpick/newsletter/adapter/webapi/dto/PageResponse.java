package com.sungho.letterpick.newsletter.adapter.webapi.dto;

import org.springframework.data.domain.Slice;

import static java.util.Objects.requireNonNull;

public record PageResponse(
        int number,
        int size,
        boolean hasNext
) {

    public static PageResponse from(Slice<?> slice) {
        requireNonNull(slice);

        return new PageResponse(
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext()
        );
    }
}
