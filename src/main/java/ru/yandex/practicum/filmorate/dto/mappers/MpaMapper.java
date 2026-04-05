package ru.yandex.practicum.filmorate.dto.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.mpa.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

@UtilityClass
public class MpaMapper {
    public MpaRatingDto mapToMpaDto(MpaRating mpaRating) {
        MpaRatingDto mpaRatingDto = new MpaRatingDto();

        mpaRatingDto.setId(mpaRating.getId());
        mpaRatingDto.setName(mpaRating.toString());

        return mpaRatingDto;
    }
}
