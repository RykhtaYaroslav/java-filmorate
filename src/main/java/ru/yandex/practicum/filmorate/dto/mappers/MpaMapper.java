package ru.yandex.practicum.filmorate.dto.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.mpa.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MpaMapper {
    public static MpaRatingDto mapToMpaDto(MpaRating mpaRating) {
        MpaRatingDto mpaRatingDto = new MpaRatingDto();

        mpaRatingDto.setId(mpaRating.getId());
        mpaRatingDto.setName(mpaRating.toString());

        return mpaRatingDto;
    }
}
