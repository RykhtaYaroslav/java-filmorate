package ru.yandex.practicum.filmorate.dto.mappers;

import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.model.enums.Genre;

public class GenreMapper {
    public static GenreDto mapToDto(Genre genre) {
        GenreDto genreDto = new GenreDto();

        genreDto.setId(genre.getId());
        genreDto.setName(genre.getName());

        return genreDto;
    }
}
