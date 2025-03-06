package com.ll.hotel.domain.hotel.option.entity;

import com.ll.hotel.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class BaseOption extends BaseEntity {
    @NotBlank(message = "필수 항목입니다.")
    @Size(max = 255, message = "최대 255자까지 작성 가능합니다.")
    @EqualsAndHashCode.Include
    @Column(unique = true, nullable = false, length = 255)
    protected String name;

    public BaseOption(String name) {
        this.name = name;
    }
}
