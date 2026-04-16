package ru.yandex.practicum.filmorate.dto.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.director.DirectorCreateRequest;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.DirectorForFilmRequest;
import ru.yandex.practicum.filmorate.dto.director.DirectorUpdateRequest;
import ru.yandex.practicum.filmorate.model.Director;

@UtilityClass
public final class DirectorMapper {

    public Director mapToDirector(DirectorCreateRequest request) {
        Director director = new Director();
        director.setName(request.getName());
        return director;
    }

    public Director mapToDirector(DirectorUpdateRequest request) {
        Director director = new Director();
        director.setId(request.getId());
        director.setName(request.getName());
        return director;
    }

    public DirectorDto mapToDirectorDto(Director director) {
        DirectorDto dto = new DirectorDto();
        dto.setId(director.getId());
        dto.setName(director.getName());
        return dto;
    }

    public Director mapToDirectorForFilm(DirectorForFilmRequest directorForFilmRequest) {
        Director director = new Director();
        director.setId(directorForFilmRequest.getId());
        return director;
    }
 }
