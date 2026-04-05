package ru.yandex.practicum.filmorate.dto.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.model.enums.Genre;

@UtilityClass
public class GenreMapper {
    public GenreDto mapToDto(Genre genre) {
        GenreDto genreDto = new GenreDto();

        genreDto.setId(genre.getId());
        genreDto.setName(genre.getName());

        return genreDto;
    }
}