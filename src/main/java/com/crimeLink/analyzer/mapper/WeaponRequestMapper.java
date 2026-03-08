package com.crimeLink.analyzer.mapper;

import com.crimeLink.analyzer.dto.WeaponRequestDto;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.entity.WeaponRequest;
import com.crimeLink.analyzer.entity.WeaponRequestStatus;

public class WeaponRequestMapper {
    public static WeaponRequestDto mapToWeaponRequestDto(WeaponRequest entity) {

        WeaponRequestDto dto = new WeaponRequestDto();

        dto.setRequestId(entity.getRequestId());
        dto.setWeaponSerial(entity.getWeaponSerial());
        dto.setRequestedById(entity.getRequestedBy().getUserId());
        dto.setRequestNote(entity.getRequestNote());
        dto.setStatus(entity.getStatus().name());
        dto.setRequestedAt(entity.getRequestedAt());
        dto.setResolvedAt(entity.getResolvedAt());
        return dto;
    }

    public static WeaponRequest mapToWeaponRequest
    (WeaponRequestDto dto, Weapon weapon, User user) {

        WeaponRequest entity = new WeaponRequest();
        
        entity.setWeapon(weapon);
        entity.setRequestedBy(user);
        entity.setRequestNote(dto.getRequestNote());
        entity.setStatus(WeaponRequestStatus.valueOf(dto.getStatus()));
        entity.setRequestedAt(dto.getRequestedAt());
        entity.setResolvedAt(dto.getResolvedAt());
        return entity;
    }
}
