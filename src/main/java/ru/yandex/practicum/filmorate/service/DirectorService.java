package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.repositories.director.DirectorRepository;
import ru.yandex.practicum.filmorate.dto.director.DirectorCreateRequest;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.DirectorUpdateRequest;
import ru.yandex.practicum.filmorate.dto.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorService {
    private final DirectorRepository directorRepository;

    public Collection<DirectorDto> findAll() {
        log.debug("Запрос на получение всех режиссеров");
        Collection<Director> directors = directorRepository.findAll();

        if (directors.isEmpty()) {
            log.info("Список режиссеров пуст");
            return Collections.emptySet();
        }

        log.info("Возвращено {} режиссеров", directors.size());
        return directors.stream()
                .map(DirectorMapper::mapToDirectorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public DirectorDto findById(Long id) {
        log.debug("Запрос на поиск режиссера по id={}", id);
        Optional<Director> optionalDirector = directorRepository.findById(id);

        return optionalDirector.map(DirectorMapper::mapToDirectorDto)
                .orElseThrow(() -> {
                    log.warn("Режиссер с id={} не найден", id);
                    return new NotFoundException(String.format("Режиссер с id = %d не найден", id));
                });
    }

    public DirectorDto create(DirectorCreateRequest request) {
        log.debug("Запрос на создание режиссера: {}", request);

        Director director = DirectorMapper.mapToDirector(request);
        DirectorDto created = DirectorMapper.mapToDirectorDto(directorRepository.create(director));

        log.info("Режиссер {} (id={}) успешно создан", created.getName(), created.getId());
        return created;
    }

    public DirectorDto update(DirectorUpdateRequest request) {
        log.debug("Запрос на обновление режиссера id={}", request.getId());
        findById(request.getId());

        Director director = DirectorMapper.mapToDirector(request);

        Director updDirector = directorRepository.update(director);
        log.info("Данные режиссера id={} успешно обновлены", updDirector.getId());
        return DirectorMapper.mapToDirectorDto(updDirector);
    }

    public void delete(Long id) {
        log.debug("Запрос на удаление режиссера id={}", id);
        directorRepository.delete(id);
        log.info("Режиссер id={} успешно удалён", id);
    }
}
